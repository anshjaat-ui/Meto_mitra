package com.example.metrosathi.data.repository

import android.content.Context
import com.example.metrosathi.data.local.AppDatabase
import com.example.metrosathi.data.local.AppSettings
import com.example.metrosathi.data.local.PreferencesManager
import com.example.metrosathi.data.model.*
import com.example.metrosathi.data.seed.SeedDataLoader
import com.example.metrosathi.domain.PlannedRoute
import com.example.metrosathi.domain.RoutingEngine
import com.example.metrosathi.domain.SearchEngine
import com.example.metrosathi.domain.StationSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

data class FullStationDetails(
    val station: Station,
    val lines: List<Line>,
    val stationLines: List<StationLine>,
    val gates: List<Gate>,
    val parking: Parking?,
    val amenities: List<Amenity>,
    val nearbyPlaces: List<NearbyPlace>,
    val timings: List<Timing>,
    val interchanges: List<Interchange>
)

class MetroRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) {
    private val metroDao = database.metroDao()
    private val userDataDao = database.userDataDao()

    val appSettings: Flow<AppSettings> = preferencesManager.appSettings

    suspend fun initialize() = withContext(Dispatchers.IO) {
        SeedDataLoader.seedDatabaseIfNeeded(context, metroDao)
    }

    fun getAllCities(): Flow<List<City>> = metroDao.getAllCities()

    suspend fun getCityById(cityId: String): City? = metroDao.getCityById(cityId)

    suspend fun setSelectedCity(cityId: String) {
        preferencesManager.setSelectedCity(cityId)
    }

    fun getLinesForCity(cityId: String): Flow<List<Line>> = metroDao.getLinesByCity(cityId)

    fun getStationsForCity(cityId: String): Flow<List<Station>> = metroDao.getStationsByCity(cityId)

    suspend fun getStationById(stationId: String): Station? = metroDao.getStationById(stationId)

    fun getStationByIdFlow(stationId: String): Flow<Station?> = metroDao.getStationByIdFlow(stationId)

    fun getGatesForStation(stationId: String): Flow<List<Gate>> = metroDao.getGatesForStation(stationId)

    fun getParkingForStation(stationId: String): Flow<Parking?> = metroDao.getParkingForStation(stationId)

    fun getAmenitiesForStation(stationId: String): Flow<List<Amenity>> = metroDao.getAmenitiesForStation(stationId)

    fun getNearbyPlacesForStation(stationId: String): Flow<List<NearbyPlace>> = metroDao.getNearbyPlacesForStation(stationId)

    fun getTimingsForStation(stationId: String): Flow<List<Timing>> = metroDao.getTimingsForStation(stationId)

    suspend fun getFullStationDetails(stationId: String): FullStationDetails? = withContext(Dispatchers.IO) {
        val station = metroDao.getStationById(stationId) ?: return@withContext null
        val stLines = metroDao.getStationLinesForStationDirect(stationId)
        val allCityLines = metroDao.getLinesByCityDirect(station.cityId)
        val matchedLines = allCityLines.filter { line -> stLines.any { it.lineId == line.id } }
        val gates = metroDao.getGatesForStation(stationId).firstOrNull() ?: emptyList()
        val parking = metroDao.getParkingForStation(stationId).firstOrNull()
        val amenities = metroDao.getAmenitiesForStation(stationId).firstOrNull() ?: emptyList()
        val nearby = metroDao.getNearbyPlacesForStation(stationId).firstOrNull() ?: emptyList()
        val timings = metroDao.getTimingsForStation(stationId).firstOrNull() ?: emptyList()
        val interchanges = metroDao.getInterchangesForStationDirect(stationId)

        FullStationDetails(
            station = station,
            lines = matchedLines,
            stationLines = stLines,
            gates = gates,
            parking = parking,
            amenities = amenities,
            nearbyPlaces = nearby,
            timings = timings,
            interchanges = interchanges
        )
    }

    suspend fun planRoute(
        cityId: String,
        fromStationId: String,
        toStationId: String
    ): List<PlannedRoute> = withContext(Dispatchers.IO) {
        val stations = metroDao.getStationsByCityDirect(cityId)
        val lines = metroDao.getLinesByCityDirect(cityId)
        val stationLines = metroDao.getStationLinesByCity(cityId)
        val connections = metroDao.getConnectionsByCity(cityId)
        val interchanges = mutableListOf<Interchange>()
        for (st in stations) {
            interchanges.addAll(metroDao.getInterchangesForStationDirect(st.id))
        }
        val fareSlabs = metroDao.getFareSlabsByCity(cityId)

        val routingEngine = RoutingEngine(
            stations = stations,
            lines = lines,
            stationLines = stationLines,
            connections = connections,
            interchanges = interchanges,
            fareSlabs = fareSlabs
        )

        routingEngine.findRoutes(fromStationId, toStationId)
    }

    suspend fun calculateFareForDistance(cityId: String, distanceKm: Double): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val slabs = metroDao.getFareSlabsByCity(cityId)
        val matched = slabs.firstOrNull { distanceKm >= it.minKm && distanceKm < it.maxKm } ?: slabs.lastOrNull()
        if (matched != null) {
            Pair(matched.tokenFare, matched.smartCardFare)
        } else {
            Pair(30, 27)
        }
    }

    suspend fun searchStations(
        cityId: String,
        query: String,
        userLat: Double? = null,
        userLng: Double? = null
    ): List<StationSearchResult> = withContext(Dispatchers.IO) {
        val stations = metroDao.getStationsByCityDirect(cityId)
        val lines = metroDao.getLinesByCityDirect(cityId)
        val allNearby = mutableListOf<NearbyPlace>()
        for (st in stations) {
            val places = metroDao.getNearbyPlacesForStation(st.id).firstOrNull() ?: emptyList()
            allNearby.addAll(places)
        }
        SearchEngine.searchStations(
            query = query,
            stations = stations,
            lines = lines,
            nearbyPlaces = allNearby,
            userLat = userLat,
            userLng = userLng
        )
    }

    // Favourites
    fun getFavourites(): Flow<List<Favourite>> = userDataDao.getAllFavourites()

    suspend fun saveFavourite(
        title: String,
        fromId: String,
        toId: String,
        fromName: String,
        toName: String,
        cityName: String
    ): Long = withContext(Dispatchers.IO) {
        val existing = userDataDao.findFavourite(fromId, toId)
        if (existing != null) {
            existing.id
        } else {
            userDataDao.insertFavourite(
                Favourite(
                    title = title,
                    fromStationId = fromId,
                    toStationId = toId,
                    fromStationName = fromName,
                    toStationName = toName,
                    cityName = cityName
                )
            )
        }
    }

    suspend fun deleteFavourite(id: Long) = withContext(Dispatchers.IO) {
        userDataDao.deleteFavourite(id)
    }

    // Trip History
    fun getTripHistory(): Flow<List<TripHistory>> = userDataDao.getAllTrips()

    fun getTotalSpend(): Flow<Int> = userDataDao.getTotalSpend()

    suspend fun logTrip(
        fromName: String,
        toName: String,
        farePaid: Int,
        timeMin: Int,
        cityName: String
    ): Long = withContext(Dispatchers.IO) {
        userDataDao.insertTrip(
            TripHistory(
                fromStationName = fromName,
                toStationName = toName,
                farePaid = farePaid,
                travelTimeMinutes = timeMin,
                cityName = cityName
            )
        )
    }

    suspend fun clearTripHistory() = withContext(Dispatchers.IO) {
        userDataDao.clearTripHistory()
    }

    // Card Balance
    fun getCardBalanceFlow(cityId: String): Flow<CardBalance?> = userDataDao.getCardBalanceFlow(cityId)

    suspend fun getCardBalance(cityId: String): CardBalance? = withContext(Dispatchers.IO) {
        userDataDao.getCardBalance(cityId)
    }

    suspend fun updateCardBalance(cityId: String, newBalance: Double) = withContext(Dispatchers.IO) {
        userDataDao.saveCardBalance(
            CardBalance(
                cityId = cityId,
                balance = newBalance,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deductFareFromCard(cityId: String, fare: Double) = withContext(Dispatchers.IO) {
        val current = userDataDao.getCardBalance(cityId)
        val oldBal = current?.balance ?: 100.0
        val updated = (oldBal - fare).coerceAtLeast(0.0)
        userDataDao.saveCardBalance(
            CardBalance(
                cityId = cityId,
                balance = updated,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    // Recent Searches
    fun getRecentSearches(): Flow<List<RecentSearch>> = userDataDao.getRecentSearches()

    suspend fun addRecentSearch(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            userDataDao.insertRecentSearch(RecentSearch(query = query.trim()))
        }
    }

    suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        userDataDao.clearRecentSearches()
    }

    // Settings
    suspend fun setLanguage(lang: String) = preferencesManager.setLanguage(lang)
    suspend fun setThemeMode(mode: com.example.metrosathi.data.local.ThemeMode) = preferencesManager.setThemeMode(mode)
    suspend fun setFontScale(scale: Float) = preferencesManager.setFontScale(scale)
    suspend fun setAlarmAheadStations(count: Int) = preferencesManager.setAlarmAheadStations(count)
    suspend fun setPremium(isPremium: Boolean) = preferencesManager.setPremium(isPremium)
}
