package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.metrosathi.data.local.AppDatabase
import com.example.metrosathi.data.local.PreferencesManager
import com.example.metrosathi.data.local.ThemeMode
import com.example.metrosathi.data.repository.MetroRepository
import com.example.metrosathi.ui.MetroViewModel
import com.example.metrosathi.ui.MetroViewModelFactory
import com.example.metrosathi.ui.screens.*
import com.example.ui.theme.MetroSathiTheme

enum class MetroScreen(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    MAP("map", "Map", Icons.Default.Map),
    PLAN("plan", "Plan", Icons.Default.Route),
    STATIONS("stations", "Stations", Icons.Default.Train),
    MORE("more", "More", Icons.Default.MoreHoriz),

    // Sub-screens
    CITY_SELECT("city_select", "City", Icons.Default.LocationCity),
    ROUTE_RESULT("route_result", "Result", Icons.Default.DirectionsTransit),
    STATION_DETAIL("station_detail", "Station", Icons.Default.Info),
    NEAREST("nearest", "Nearest", Icons.Default.NearMe),
    FARE("fare", "Fare", Icons.Default.Calculate),
    ALARM("alarm", "Alarm", Icons.Default.Alarm),
    CARD_BALANCE("card_balance", "Card Balance", Icons.Default.CreditCard),
    TRIP_HISTORY("trip_history", "History", Icons.Default.History),
    RULES("rules", "Rules", Icons.Default.Gavel),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val preferencesManager = PreferencesManager(this)
        val repository = MetroRepository(this, database, preferencesManager)

        setContent {
            val viewModel: MetroViewModel = viewModel(factory = MetroViewModelFactory(repository))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (uiState.appSettings.themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            MetroSathiTheme(darkTheme = useDarkTheme) {
                MetroAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MetroAppContent(viewModel: MetroViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(MetroScreen.HOME) }
    var screenBackStack by remember { mutableStateOf(listOf(MetroScreen.HOME)) }

    fun navigateTo(screen: MetroScreen) {
        if (screen != currentScreen) {
            screenBackStack = screenBackStack + screen
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (screenBackStack.size > 1) {
            val updated = screenBackStack.dropLast(1)
            screenBackStack = updated
            currentScreen = updated.last()
        } else {
            currentScreen = MetroScreen.HOME
        }
    }

    BackHandler(enabled = currentScreen != MetroScreen.HOME) {
        navigateBack()
    }

    val isTopLevelScreen = currentScreen in listOf(
        MetroScreen.HOME,
        MetroScreen.MAP,
        MetroScreen.PLAN,
        MetroScreen.STATIONS,
        MetroScreen.MORE
    )

    Scaffold(
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val bottomItems = listOf(
                        MetroScreen.HOME,
                        MetroScreen.MAP,
                        MetroScreen.PLAN,
                        MetroScreen.STATIONS,
                        MetroScreen.MORE
                    )
                    bottomItems.forEach { item ->
                        val selected = currentScreen == item
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                screenBackStack = listOf(item)
                                currentScreen = item
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            modifier = Modifier.testTag("nav_${item.route}")
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MetroScreen.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        onStationClick = { stationId ->
                            viewModel.selectStation(stationId)
                            navigateTo(MetroScreen.STATION_DETAIL)
                        },
                        onNavigateToPlan = { navigateTo(MetroScreen.PLAN) },
                        onNavigateToMap = { navigateTo(MetroScreen.MAP) },
                        onNavigateToNearest = { navigateTo(MetroScreen.NEAREST) },
                        onNavigateToFare = { navigateTo(MetroScreen.FARE) },
                        onNavigateToCardBalance = { navigateTo(MetroScreen.CARD_BALANCE) },
                        onNavigateToAlarm = { navigateTo(MetroScreen.ALARM) },
                        onNavigateToRules = { navigateTo(MetroScreen.RULES) },
                        onNavigateToCitySelect = { navigateTo(MetroScreen.CITY_SELECT) },
                        onFavouriteClick = { fav ->
                            val fromSt = uiState.stations.find { it.id == fav.fromStationId }
                            val toSt = uiState.stations.find { it.id == fav.toStationId }
                            if (fromSt != null) viewModel.setFromStation(fromSt)
                            if (toSt != null) viewModel.setToStation(toSt)
                            viewModel.planRoute(fav.fromStationId, fav.toStationId)
                            navigateTo(MetroScreen.ROUTE_RESULT)
                        }
                    )
                }

                MetroScreen.MAP -> {
                    MetroMapScreen(
                        uiState = uiState,
                        onStationSelected = { stationId ->
                            viewModel.selectStation(stationId)
                            navigateTo(MetroScreen.STATION_DETAIL)
                        },
                        onSetFromStation = { st ->
                            viewModel.setFromStation(st)
                            navigateTo(MetroScreen.PLAN)
                        },
                        onSetToStation = { st ->
                            viewModel.setToStation(st)
                            navigateTo(MetroScreen.PLAN)
                        }
                    )
                }

                MetroScreen.PLAN -> {
                    RoutePlannerScreen(
                        uiState = uiState,
                        onFromStationChange = { viewModel.setFromStation(it) },
                        onToStationChange = { viewModel.setToStation(it) },
                        onSwapStations = { viewModel.swapStations() },
                        onPlanRoute = {
                            viewModel.planRoute()
                            if (uiState.errorMessage == null) {
                                navigateTo(MetroScreen.ROUTE_RESULT)
                            }
                        },
                        onUseMyLocation = {
                            navigateTo(MetroScreen.NEAREST)
                        },
                        onStationClick = { stationId ->
                            viewModel.selectStation(stationId)
                            navigateTo(MetroScreen.STATION_DETAIL)
                        }
                    )
                }

                MetroScreen.STATIONS -> {
                    StationsDirectoryScreen(
                        uiState = uiState,
                        onStationClick = { stationId ->
                            viewModel.selectStation(stationId)
                            navigateTo(MetroScreen.STATION_DETAIL)
                        }
                    )
                }

                MetroScreen.MORE -> {
                    MoreMenuScreen(
                        uiState = uiState,
                        onNavigateToNearest = { navigateTo(MetroScreen.NEAREST) },
                        onNavigateToFare = { navigateTo(MetroScreen.FARE) },
                        onNavigateToCardBalance = { navigateTo(MetroScreen.CARD_BALANCE) },
                        onNavigateToAlarm = { navigateTo(MetroScreen.ALARM) },
                        onNavigateToTripHistory = { navigateTo(MetroScreen.TRIP_HISTORY) },
                        onNavigateToRules = { navigateTo(MetroScreen.RULES) },
                        onNavigateToSettings = { navigateTo(MetroScreen.SETTINGS) },
                        onNavigateToCitySelect = { navigateTo(MetroScreen.CITY_SELECT) }
                    )
                }

                MetroScreen.CITY_SELECT -> {
                    CitySelectScreen(
                        cities = uiState.allCities,
                        selectedCityId = uiState.selectedCity?.id ?: "delhi",
                        onCitySelected = { cityId ->
                            viewModel.selectCity(cityId)
                            navigateBack()
                        },
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.ROUTE_RESULT -> {
                    RouteResultScreen(
                        uiState = uiState,
                        onSelectAlternativeRoute = { viewModel.selectRoute(it) },
                        onSaveFavourite = { viewModel.saveCurrentRouteToFavourites() },
                        onLogTrip = { viewModel.logCurrentTrip() },
                        onSetAlarmForDestination = { dest ->
                            viewModel.startDestinationAlarm(context, dest, 1)
                            navigateTo(MetroScreen.ALARM)
                        },
                        onStationClick = { stationId ->
                            viewModel.selectStation(stationId)
                            navigateTo(MetroScreen.STATION_DETAIL)
                        },
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.STATION_DETAIL -> {
                    StationDetailScreen(
                        details = uiState.selectedStationDetails,
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.NEAREST -> {
                    NearestStationScreen(
                        uiState = uiState,
                        onSetLocation = { lat, lng -> viewModel.setUserLocation(lat, lng) },
                        onStationClick = { stationId ->
                            viewModel.selectStation(stationId)
                            navigateTo(MetroScreen.STATION_DETAIL)
                        },
                        onSetFromStation = { st ->
                            viewModel.setFromStation(st)
                            navigateTo(MetroScreen.PLAN)
                        },
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.FARE -> {
                    FareCalculatorScreen(
                        uiState = uiState,
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.ALARM -> {
                    TravelAlarmScreen(
                        uiState = uiState,
                        onStartAlarm = { ctx, st, ahead -> viewModel.startDestinationAlarm(ctx, st, ahead) },
                        onStopAlarm = { ctx -> viewModel.stopDestinationAlarm(ctx) },
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.CARD_BALANCE -> {
                    CardBalanceScreen(
                        uiState = uiState,
                        onUpdateBalance = { viewModel.updateCardBalance(it) },
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.TRIP_HISTORY -> {
                    TripHistoryScreen(
                        uiState = uiState,
                        onClearHistory = { viewModel.clearTripHistory() },
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.RULES -> {
                    RulesFaqScreen(
                        uiState = uiState,
                        onBack = { navigateBack() }
                    )
                }

                MetroScreen.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onNavigateToCitySelect = { navigateTo(MetroScreen.CITY_SELECT) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onSetFontScale = { viewModel.setFontScale(it) },
                        onClearAllData = {
                            viewModel.clearTripHistory()
                            viewModel.clearRecentSearches()
                        },
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }
}
