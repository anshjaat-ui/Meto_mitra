package com.example.metrosathi.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.metrosathi.data.local.AppSettings
import com.example.metrosathi.data.local.ThemeMode
import com.example.metrosathi.data.model.*
import com.example.metrosathi.data.repository.FullStationDetails
import com.example.metrosathi.data.repository.MetroRepository
import com.example.metrosathi.domain.PlannedRoute
import com.example.metrosathi.domain.StationSearchResult
import com.example.metrosathi.service.DestinationAlarmService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MetroUiState(
    val isLoading: Boolean = true,
    val selectedCity: City? = null,
    val allCities: List<City> = emptyList(),
    val lines: List<Line> = emptyList(),
    val stations: List<Station> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<StationSearchResult> = emptyList(),
    val fromStation: Station? = null,
    val toStation: Station? = null,
    val isPlanning: Boolean = false,
    val plannedRoutes: List<PlannedRoute> = emptyList(),
    val selectedRoute: PlannedRoute? = null,
    val selectedStationDetails: FullStationDetails? = null,
    val favourites: List<Favourite> = emptyList(),
    val recentSearches: List<RecentSearch> = emptyList(),
    val tripHistory: List<TripHistory> = emptyList(),
    val totalTripSpend: Int = 0,
    val cardBalance: CardBalance? = null,
    val isAlarmActive: Boolean = false,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val errorMessage: String? = null,
    val appSettings: AppSettings = AppSettings()
)

class MetroViewModel(private val repository: MetroRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MetroUiState())
    val uiState: StateFlow<MetroUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            observeRepositoryData()
        }
    }

    private fun observeRepositoryData() {
        // App Settings
        viewModelScope.launch {
            repository.appSettings.collect { settings ->
                _uiState.update { it.copy(appSettings = settings) }
                loadCityData(settings.selectedCityId)
            }
        }

        // All Cities
        viewModelScope.launch {
            repository.getAllCities().collect { cities ->
                _uiState.update { it.copy(allCities = cities) }
            }
        }

        // Favourites
        viewModelScope.launch {
            repository.getFavourites().collect { favs ->
                _uiState.update { it.copy(favourites = favs) }
            }
        }

        // Recent Searches
        viewModelScope.launch {
            repository.getRecentSearches().collect { recent ->
                _uiState.update { it.copy(recentSearches = recent) }
            }
        }

        // Trip History & Spend
        viewModelScope.launch {
            repository.getTripHistory().collect { trips ->
                _uiState.update { it.copy(tripHistory = trips) }
            }
        }
        viewModelScope.launch {
            repository.getTotalSpend().collect { spend ->
                _uiState.update { it.copy(totalTripSpend = spend) }
            }
        }
    }

    fun selectCity(cityId: String) {
        viewModelScope.launch {
            repository.setSelectedCity(cityId)
            loadCityData(cityId)
        }
    }

    private fun loadCityData(cityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val city = repository.getCityById(cityId)
            _uiState.update { it.copy(selectedCity = city) }

            // Observe lines & stations for this city
            repository.getLinesForCity(cityId).collect { lines ->
                _uiState.update { it.copy(lines = lines) }
            }
        }

        viewModelScope.launch {
            repository.getStationsForCity(cityId).collect { stations ->
                _uiState.update {
                    it.copy(
                        stations = stations,
                        isLoading = false,
                        // Pick default from & to stations if empty
                        fromStation = it.fromStation ?: stations.firstOrNull(),
                        toStation = it.toStation ?: stations.getOrNull(1)
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getCardBalanceFlow(cityId).collect { bal ->
                _uiState.update { it.copy(cardBalance = bal) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val cityId = _uiState.value.selectedCity?.id ?: "delhi"
        viewModelScope.launch {
            val results = repository.searchStations(
                cityId = cityId,
                query = query,
                userLat = _uiState.value.userLat,
                userLng = _uiState.value.userLng
            )
            _uiState.update { it.copy(searchResults = results) }
            if (query.length > 2) {
                repository.addRecentSearch(query)
            }
        }
    }

    fun setFromStation(station: Station) {
        _uiState.update { it.copy(fromStation = station) }
    }

    fun setToStation(station: Station) {
        _uiState.update { it.copy(toStation = station) }
    }

    fun swapStations() {
        val currFrom = _uiState.value.fromStation
        val currTo = _uiState.value.toStation
        _uiState.update { it.copy(fromStation = currTo, toStation = currFrom) }
    }

    fun planRoute(fromStationId: String? = null, toStationId: String? = null) {
        val cityId = _uiState.value.selectedCity?.id ?: "delhi"
        val fromId = fromStationId ?: _uiState.value.fromStation?.id ?: return
        val toId = toStationId ?: _uiState.value.toStation?.id ?: return

        if (fromId == toId) {
            _uiState.update { it.copy(errorMessage = "Please select two different stations") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPlanning = true, errorMessage = null) }
            val routes = repository.planRoute(cityId, fromId, toId)
            if (routes.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isPlanning = false,
                        errorMessage = "No direct or interchange route found."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isPlanning = false,
                        plannedRoutes = routes,
                        selectedRoute = routes.first()
                    )
                }
            }
        }
    }

    fun selectRoute(route: PlannedRoute) {
        _uiState.update { it.copy(selectedRoute = route) }
    }

    fun selectStation(stationId: String) {
        viewModelScope.launch {
            val details = repository.getFullStationDetails(stationId)
            _uiState.update { it.copy(selectedStationDetails = details) }
        }
    }

    fun saveCurrentRouteToFavourites() {
        val route = _uiState.value.selectedRoute ?: return
        val fromSt = route.allStationsInOrder.firstOrNull() ?: return
        val toSt = route.allStationsInOrder.lastOrNull() ?: return
        val cityName = _uiState.value.selectedCity?.name ?: "Metro"

        viewModelScope.launch {
            repository.saveFavourite(
                title = "${fromSt.name} → ${toSt.name}",
                fromId = fromSt.id,
                toId = toSt.id,
                fromName = fromSt.name,
                toName = toSt.name,
                cityName = cityName
            )
        }
    }

    fun deleteFavourite(id: Long) {
        viewModelScope.launch {
            repository.deleteFavourite(id)
        }
    }

    fun logCurrentTrip() {
        val route = _uiState.value.selectedRoute ?: return
        val fromSt = route.allStationsInOrder.firstOrNull()?.name ?: "Origin"
        val toSt = route.allStationsInOrder.lastOrNull()?.name ?: "Destination"
        val cityId = _uiState.value.selectedCity?.id ?: "delhi"
        val cityName = _uiState.value.selectedCity?.name ?: "Metro"

        viewModelScope.launch {
            repository.logTrip(fromSt, toSt, route.smartCardFare, route.totalTimeMinutes, cityName)
            repository.deductFareFromCard(cityId, route.smartCardFare.toDouble())
        }
    }

    fun updateCardBalance(newBalance: Double) {
        val cityId = _uiState.value.selectedCity?.id ?: "delhi"
        viewModelScope.launch {
            repository.updateCardBalance(cityId, newBalance)
        }
    }

    fun clearTripHistory() {
        viewModelScope.launch {
            repository.clearTripHistory()
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            repository.setFontScale(scale)
        }
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(userLat = lat, userLng = lng) }
        // If nearest station needed, auto-update
        onSearchQueryChange(_uiState.value.searchQuery)
    }

    fun startDestinationAlarm(context: Context, station: Station, aheadCount: Int = 1) {
        val intent = Intent(context, DestinationAlarmService::class.java).apply {
            action = DestinationAlarmService.ACTION_START
            putExtra(DestinationAlarmService.EXTRA_STATION_NAME, station.name)
            putExtra(DestinationAlarmService.EXTRA_STATION_LAT, station.latitude)
            putExtra(DestinationAlarmService.EXTRA_STATION_LNG, station.longitude)
            putExtra(DestinationAlarmService.EXTRA_AHEAD_COUNT, aheadCount)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _uiState.update { it.copy(isAlarmActive = true) }
    }

    fun stopDestinationAlarm(context: Context) {
        val intent = Intent(context, DestinationAlarmService::class.java).apply {
            action = DestinationAlarmService.ACTION_STOP
        }
        context.startService(intent)
        _uiState.update { it.copy(isAlarmActive = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class MetroViewModelFactory(private val repository: MetroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
