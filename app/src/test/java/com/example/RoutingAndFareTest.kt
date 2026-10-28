package com.example

import com.example.metrosathi.data.model.*
import com.example.metrosathi.domain.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoutingAndFareTest {

    private lateinit var routingEngine: RoutingEngine
    private val testStations = mutableListOf<Station>()
    private val testLines = mutableListOf<Line>()
    private val testStationLines = mutableListOf<StationLine>()
    private val testConnections = mutableListOf<Connection>()
    private val testInterchanges = mutableListOf<Interchange>()
    private val testFareSlabs = mutableListOf<FareSlab>()

    @Before
    fun setup() {
        // Line 1: Yellow Line (A -> B -> C)
        // Line 2: Blue Line (D -> B -> E) -> Station B is interchange
        val lineY = Line("delhi_yellow", "delhi", "Yellow Line", "येलो लाइन", "2", "#FDD835", "#000000", "A", "C", 3)
        val lineB = Line("delhi_blue", "delhi", "Blue Line", "ब्लू लाइन", "3", "#1E88E5", "#FFFFFF", "D", "E", 3)
        testLines.addAll(listOf(lineY, lineB))

        val stA = Station("st_a", "delhi", "Station A", "स्टेशन ए", "STA", 28.7, 77.1, "Elevated", 2010, false, 2, 100f, 100f, true, true)
        val stB = Station("st_b", "delhi", "Station B", "स्टेशन बी", "STB", 28.6, 77.2, "Underground", 2010, true, 4, 200f, 200f, true, true)
        val stC = Station("st_c", "delhi", "Station C", "स्टेशन सी", "STC", 28.5, 77.2, "Underground", 2010, false, 2, 300f, 300f, false, true)
        val stD = Station("st_d", "delhi", "Station D", "स्टेशन डी", "STD", 28.6, 77.0, "Elevated", 2010, false, 2, 100f, 200f, true, true)
        val stE = Station("st_e", "delhi", "Station E", "स्टेशन ई", "STE", 28.6, 77.4, "Elevated", 2010, false, 2, 300f, 200f, true, true)
        testStations.addAll(listOf(stA, stB, stC, stD, stE))

        testStationLines.add(StationLine("sl_1", "st_a", "delhi_yellow", 1, "1"))
        testStationLines.add(StationLine("sl_2", "st_b", "delhi_yellow", 2, "2"))
        testStationLines.add(StationLine("sl_3", "st_c", "delhi_yellow", 3, "2"))

        testStationLines.add(StationLine("sl_4", "st_d", "delhi_blue", 1, "1"))
        testStationLines.add(StationLine("sl_5", "st_b", "delhi_blue", 2, "3"))
        testStationLines.add(StationLine("sl_6", "st_e", "delhi_blue", 3, "2"))

        // Connections on Yellow Line
        testConnections.add(Connection("c_1", "delhi", "delhi_yellow", "st_a", "st_b", 4.0, 300))
        testConnections.add(Connection("c_1_r", "delhi", "delhi_yellow", "st_b", "st_a", 4.0, 300))
        testConnections.add(Connection("c_2", "delhi", "delhi_yellow", "st_b", "st_c", 3.0, 240))
        testConnections.add(Connection("c_2_r", "delhi", "delhi_yellow", "st_b", "st_c", 3.0, 240))

        // Connections on Blue Line
        testConnections.add(Connection("c_3", "delhi", "delhi_blue", "st_d", "st_b", 5.0, 360))
        testConnections.add(Connection("c_3_r", "delhi", "delhi_blue", "st_b", "st_d", 5.0, 360))
        testConnections.add(Connection("c_4", "delhi", "delhi_blue", "st_b", "st_e", 6.0, 420))
        testConnections.add(Connection("c_4_r", "delhi", "delhi_blue", "st_e", "st_b", 6.0, 420))

        // Interchange at station B
        testInterchanges.add(Interchange("ic_1", "st_b", "delhi_yellow", "delhi_blue", 4, "Transfer concourse"))
        testInterchanges.add(Interchange("ic_2", "st_b", "delhi_blue", "delhi_yellow", 4, "Transfer concourse"))

        // Fare Slabs
        testFareSlabs.add(FareSlab("fs_1", "delhi", 0.0, 2.0, 10, 9, 8, "0 - 2 km"))
        testFareSlabs.add(FareSlab("fs_2", "delhi", 2.0, 5.0, 20, 18, 16, "2 - 5 km"))
        testFareSlabs.add(FareSlab("fs_3", "delhi", 5.0, 12.0, 30, 27, 24, "5 - 12 km"))
        testFareSlabs.add(FareSlab("fs_4", "delhi", 12.0, 100.0, 50, 45, 40, "12+ km"))

        routingEngine = RoutingEngine(
            stations = testStations,
            lines = testLines,
            stationLines = testStationLines,
            connections = testConnections,
            interchanges = testInterchanges,
            fareSlabs = testFareSlabs
        )
    }

    @Test
    fun testDirectRoutePlanning() {
        val routes = routingEngine.findRoutes("st_a", "st_c")
        assertFalse("Route should be found", routes.isEmpty())

        val primary = routes.first()
        assertEquals(3, primary.totalStationsCount)
        assertEquals(0, primary.interchangeCount)
        assertEquals(7.0, primary.totalDistanceKm, 0.1)
        assertEquals(30, primary.tokenFare)
        assertEquals(27, primary.smartCardFare)
    }

    @Test
    fun testInterchangeRoutePlanning() {
        // Route from Station D (Blue line) to Station C (Yellow line) via Station B interchange
        val routes = routingEngine.findRoutes("st_d", "st_c")
        assertFalse("Route with interchange should be found", routes.isEmpty())

        val primary = routes.first()
        assertEquals(3, primary.totalStationsCount)
        assertEquals(1, primary.interchangeCount)
        assertEquals("Station B", primary.interchangeStationNames.first())
        assertEquals(8.0, primary.totalDistanceKm, 0.1)
    }

    @Test
    fun testFareCalculation() {
        val shortFare = routingEngine.calculateFare(1.5)
        assertEquals(10, shortFare.first)
        assertEquals(9, shortFare.second)

        val midFare = routingEngine.calculateFare(8.0)
        assertEquals(30, midFare.first)
        assertEquals(27, midFare.second)
    }

    @Test
    fun testSearchEngineHindiAndEnglish() {
        val resultsEn = SearchEngine.searchStations(
            query = "station a",
            stations = testStations,
            lines = testLines,
            nearbyPlaces = emptyList()
        )
        assertEquals(1, resultsEn.size)
        assertEquals("Station A", resultsEn.first().station.name)

        val resultsHi = SearchEngine.searchStations(
            query = "स्टेशन",
            stations = testStations,
            lines = testLines,
            nearbyPlaces = emptyList()
        )
        assertEquals(5, resultsHi.size)
    }
}
