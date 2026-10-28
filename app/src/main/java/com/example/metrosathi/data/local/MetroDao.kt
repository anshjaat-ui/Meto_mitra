package com.example.metrosathi.data.local

import androidx.room.*
import com.example.metrosathi.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MetroDao {
    // Cities
    @Query("SELECT * FROM cities ORDER BY name ASC")
    fun getAllCities(): Flow<List<City>>

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCityById(cityId: String): City?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<City>)

    // Lines
    @Query("SELECT * FROM lines WHERE cityId = :cityId")
    fun getLinesByCity(cityId: String): Flow<List<Line>>

    @Query("SELECT * FROM lines WHERE cityId = :cityId")
    suspend fun getLinesByCityDirect(cityId: String): List<Line>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<Line>)

    // Stations
    @Query("SELECT * FROM stations WHERE cityId = :cityId ORDER BY name ASC")
    fun getStationsByCity(cityId: String): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE cityId = :cityId ORDER BY name ASC")
    suspend fun getStationsByCityDirect(cityId: String): List<Station>

    @Query("SELECT * FROM stations WHERE id = :stationId")
    suspend fun getStationById(stationId: String): Station?

    @Query("SELECT * FROM stations WHERE id = :stationId")
    fun getStationByIdFlow(stationId: String): Flow<Station?>

    @Query("""
        SELECT * FROM stations 
        WHERE cityId = :cityId AND (
            name LIKE '%' || :query || '%' OR 
            nameHi LIKE '%' || :query || '%' OR 
            code LIKE '%' || :query || '%'
        ) 
        ORDER BY name ASC
    """)
    fun searchStations(cityId: String, query: String): Flow<List<Station>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<Station>)

    // Station Lines
    @Query("""
        SELECT sl.* FROM station_lines sl 
        INNER JOIN stations s ON sl.stationId = s.id 
        WHERE s.cityId = :cityId
    """)
    suspend fun getStationLinesByCity(cityId: String): List<StationLine>

    @Query("SELECT * FROM station_lines WHERE stationId = :stationId")
    fun getStationLinesForStation(stationId: String): Flow<List<StationLine>>

    @Query("SELECT * FROM station_lines WHERE stationId = :stationId")
    suspend fun getStationLinesForStationDirect(stationId: String): List<StationLine>

    @Query("SELECT * FROM station_lines WHERE lineId = :lineId ORDER BY sequenceOrder ASC")
    suspend fun getStationLinesForLine(lineId: String): List<StationLine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStationLines(stationLines: List<StationLine>)

    // Connections
    @Query("SELECT * FROM connections WHERE cityId = :cityId")
    suspend fun getConnectionsByCity(cityId: String): List<Connection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<Connection>)

    // Interchanges
    @Query("SELECT * FROM interchanges WHERE stationId = :stationId")
    fun getInterchangesForStation(stationId: String): Flow<List<Interchange>>

    @Query("SELECT * FROM interchanges WHERE stationId = :stationId")
    suspend fun getInterchangesForStationDirect(stationId: String): List<Interchange>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterchanges(interchanges: List<Interchange>)

    // Gates
    @Query("SELECT * FROM gates WHERE stationId = :stationId ORDER BY gateNumber ASC")
    fun getGatesForStation(stationId: String): Flow<List<Gate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGates(gates: List<Gate>)

    // Parking
    @Query("SELECT * FROM parking WHERE stationId = :stationId LIMIT 1")
    fun getParkingForStation(stationId: String): Flow<Parking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParking(parkingList: List<Parking>)

    // Amenities
    @Query("SELECT * FROM amenities WHERE stationId = :stationId")
    fun getAmenitiesForStation(stationId: String): Flow<List<Amenity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmenities(amenities: List<Amenity>)

    // Nearby Places
    @Query("SELECT * FROM nearby_places WHERE stationId = :stationId ORDER BY distanceMeters ASC")
    fun getNearbyPlacesForStation(stationId: String): Flow<List<NearbyPlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNearbyPlaces(places: List<NearbyPlace>)

    // Timings
    @Query("SELECT * FROM timings WHERE stationId = :stationId")
    fun getTimingsForStation(stationId: String): Flow<List<Timing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimings(timings: List<Timing>)

    // Fare Slabs
    @Query("SELECT * FROM fare_slabs WHERE cityId = :cityId ORDER BY minKm ASC")
    suspend fun getFareSlabsByCity(cityId: String): List<FareSlab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFareSlabs(slabs: List<FareSlab>)

    @Query("SELECT COUNT(*) FROM stations WHERE cityId = :cityId")
    suspend fun getStationCountForCity(cityId: String): Int
}

@Dao
interface UserDataDao {
    // Favourites
    @Query("SELECT * FROM favourites ORDER BY timestamp DESC")
    fun getAllFavourites(): Flow<List<Favourite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: Favourite): Long

    @Query("DELETE FROM favourites WHERE id = :id")
    suspend fun deleteFavourite(id: Long)

    @Query("SELECT * FROM favourites WHERE fromStationId = :fromId AND toStationId = :toId LIMIT 1")
    suspend fun findFavourite(fromId: String, toId: String): Favourite?

    // Trip History
    @Query("SELECT * FROM trip_history ORDER BY dateTimestamp DESC")
    fun getAllTrips(): Flow<List<TripHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripHistory): Long

    @Query("DELETE FROM trip_history")
    suspend fun clearTripHistory()

    @Query("SELECT COALESCE(SUM(farePaid), 0) FROM trip_history")
    fun getTotalSpend(): Flow<Int>

    // Recent Searches
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearch): Long

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    // Card Balance
    @Query("SELECT * FROM card_balance WHERE cityId = :cityId")
    fun getCardBalanceFlow(cityId: String): Flow<CardBalance?>

    @Query("SELECT * FROM card_balance WHERE cityId = :cityId")
    suspend fun getCardBalance(cityId: String): CardBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCardBalance(balance: CardBalance)
}
