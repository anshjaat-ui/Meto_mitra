package com.example.metrosathi.domain

import com.example.metrosathi.data.model.*
import java.util.PriorityQueue

enum class RouteMode {
    FASTEST,
    LEAST_INTERCHANGES,
    FEWEST_STATIONS
}

data class RouteLeg(
    val line: Line,
    val fromStation: Station,
    val toStation: Station,
    val stations: List<Station>,
    val directionTerminal: String,
    val platformNumber: String,
    val durationSeconds: Int,
    val distanceKm: Double
)

data class PlannedRoute(
    val id: String,
    val title: String,
    val mode: RouteMode,
    val totalTimeMinutes: Int,
    val totalDistanceKm: Double,
    val totalStationsCount: Int,
    val interchangeCount: Int,
    val tokenFare: Int,
    val smartCardFare: Int,
    val legs: List<RouteLeg>,
    val allStationsInOrder: List<Station>,
    val interchangeStationNames: List<String>
)

class RoutingEngine(
    private val stations: List<Station>,
    private val lines: List<Line>,
    private val stationLines: List<StationLine>,
    private val connections: List<Connection>,
    private val interchanges: List<Interchange>,
    private val fareSlabs: List<FareSlab>
) {
    private val stationMap = stations.associateBy { it.id }
    private val lineMap = lines.associateBy { it.id }

    // Map: stationId -> list of (lineId, sequenceOrder, platformNumber)
    private val stationLineMap: Map<String, List<StationLine>> = stationLines.groupBy { it.stationId }

    // Adjacency graph: fromStationId -> list of connections
    private val adjacency: Map<String, List<Connection>> = connections.groupBy { it.fromStationId }

    // Interchange map: (stationId, fromLineId, toLineId) -> walking time seconds
    private val interchangeMap: Map<Triple<String, String, String>, Int> =
        interchanges.associate { Triple(it.stationId, it.fromLineId, it.toLineId) to (it.walkingTimeMinutes * 60) }

    data class GraphNode(
        val stationId: String,
        val currentLineId: String?,
        val cost: Double,
        val timeSec: Int,
        val distanceKm: Double,
        val interchanges: Int,
        val stationsCount: Int,
        val path: List<ConnectionStep>
    ) : Comparable<GraphNode> {
        override fun compareTo(other: GraphNode): Int = cost.compareTo(other.cost)
    }

    data class ConnectionStep(
        val fromStationId: String,
        val toStationId: String,
        val lineId: String,
        val distanceKm: Double,
        val travelTimeSec: Int,
        val isInterchange: Boolean = false
    )

    fun findRoutes(fromStationId: String, toStationId: String): List<PlannedRoute> {
        if (fromStationId == toStationId) return emptyList()

        val results = mutableListOf<PlannedRoute>()

        // 1. Primary route: FASTEST
        val fastestRoute = runDijkstra(fromStationId, toStationId, RouteMode.FASTEST, penaltyWeight = 300)
        fastestRoute?.let { results.add(it) }

        // 2. Alternative route: LEAST_INTERCHANGES
        val leastInterchangesRoute = runDijkstra(fromStationId, toStationId, RouteMode.LEAST_INTERCHANGES, penaltyWeight = 1200)
        if (leastInterchangesRoute != null && results.none { isSameRoute(it, leastInterchangesRoute) }) {
            results.add(leastInterchangesRoute)
        }

        // 3. Alternative route: FEWEST_STATIONS
        val fewestStationsRoute = runDijkstra(fromStationId, toStationId, RouteMode.FEWEST_STATIONS, penaltyWeight = 180)
        if (fewestStationsRoute != null && results.none { isSameRoute(it, fewestStationsRoute) }) {
            results.add(fewestStationsRoute)
        }

        // Fallback: If only 1 route found and has alternatives by tweaking penalty
        if (results.size < 2) {
            val relaxedRoute = runDijkstra(fromStationId, toStationId, RouteMode.FASTEST, penaltyWeight = 600)
            if (relaxedRoute != null && results.none { isSameRoute(it, relaxedRoute) }) {
                results.add(relaxedRoute)
            }
        }

        return results
    }

    private fun isSameRoute(r1: PlannedRoute, r2: PlannedRoute): Boolean {
        if (r1.allStationsInOrder.size != r2.allStationsInOrder.size) return false
        return r1.allStationsInOrder.map { it.id } == r2.allStationsInOrder.map { it.id }
    }

    private fun runDijkstra(
        fromStationId: String,
        toStationId: String,
        mode: RouteMode,
        penaltyWeight: Int
    ): PlannedRoute? {
        // State tracking: (stationId, lineId) -> lowest cost
        val minCostMap = mutableMapOf<Pair<String, String>, Double>()
        val queue = PriorityQueue<GraphNode>()

        val initialLines = stationLineMap[fromStationId] ?: emptyList()
        for (sl in initialLines) {
            queue.add(
                GraphNode(
                    stationId = fromStationId,
                    currentLineId = sl.lineId,
                    cost = 0.0,
                    timeSec = 0,
                    distanceKm = 0.0,
                    interchanges = 0,
                    stationsCount = 1,
                    path = emptyList()
                )
            )
            minCostMap[Pair(fromStationId, sl.lineId)] = 0.0
        }

        var destinationNode: GraphNode? = null

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break

            if (current.stationId == toStationId) {
                destinationNode = current
                break
            }

            val currentKey = Pair(current.stationId, current.currentLineId ?: "")
            val recordedCost = minCostMap[currentKey] ?: Double.MAX_VALUE
            if (current.cost > recordedCost) continue

            // 1. Explore adjacent stations on network
            val outgoingEdges = adjacency[current.stationId] ?: emptyList()
            for (edge in outgoingEdges) {
                val nextStationId = edge.toStationId
                val edgeLineId = edge.lineId
                val isLineTransfer = current.currentLineId != null && current.currentLineId != edgeLineId

                val transferPenaltySec = if (isLineTransfer) {
                    val key = Triple(current.stationId, current.currentLineId ?: "", edgeLineId)
                    interchangeMap[key] ?: penaltyWeight
                } else {
                    0
                }

                val edgeWeight = when (mode) {
                    RouteMode.FASTEST -> edge.travelTimeSeconds.toDouble() + (if (isLineTransfer) penaltyWeight.toDouble() else 0.0)
                    RouteMode.LEAST_INTERCHANGES -> edge.travelTimeSeconds.toDouble() + (if (isLineTransfer) 5000.0 else 0.0)
                    RouteMode.FEWEST_STATIONS -> 100.0 + (if (isLineTransfer) 300.0 else 0.0)
                }

                val newCost = current.cost + edgeWeight
                val nextKey = Pair(nextStationId, edgeLineId)
                val existingCost = minCostMap[nextKey] ?: Double.MAX_VALUE

                if (newCost < existingCost) {
                    minCostMap[nextKey] = newCost
                    val step = ConnectionStep(
                        fromStationId = current.stationId,
                        toStationId = nextStationId,
                        lineId = edgeLineId,
                        distanceKm = edge.distanceKm,
                        travelTimeSec = edge.travelTimeSeconds + transferPenaltySec,
                        isInterchange = isLineTransfer
                    )
                    queue.add(
                        GraphNode(
                            stationId = nextStationId,
                            currentLineId = edgeLineId,
                            cost = newCost,
                            timeSec = current.timeSec + edge.travelTimeSeconds + transferPenaltySec,
                            distanceKm = current.distanceKm + edge.distanceKm,
                            interchanges = current.interchanges + (if (isLineTransfer) 1 else 0),
                            stationsCount = current.stationsCount + 1,
                            path = current.path + step
                        )
                    )
                }
            }
        }

        val dest = destinationNode ?: return null
        return buildPlannedRoute(dest, mode)
    }

    private fun buildPlannedRoute(destNode: GraphNode, mode: RouteMode): PlannedRoute {
        val steps = destNode.path
        if (steps.isEmpty()) {
            val st = stationMap[destNode.stationId]
            return PlannedRoute(
                id = "${destNode.stationId}_${mode.name}",
                title = mode.name,
                mode = mode,
                totalTimeMinutes = 0,
                totalDistanceKm = 0.0,
                totalStationsCount = 1,
                interchangeCount = 0,
                tokenFare = 10,
                smartCardFare = 10,
                legs = emptyList(),
                allStationsInOrder = listOfNotNull(st),
                interchangeStationNames = emptyList()
            )
        }

        val legs = mutableListOf<RouteLeg>()
        var currentLegSteps = mutableListOf<ConnectionStep>()
        var currentLineId = steps.first().lineId

        for (step in steps) {
            if (step.lineId != currentLineId) {
                // Finalize previous leg
                buildLeg(currentLineId, currentLegSteps)?.let { legs.add(it) }
                currentLegSteps = mutableListOf()
                currentLineId = step.lineId
            }
            currentLegSteps.add(step)
        }
        buildLeg(currentLineId, currentLegSteps)?.let { legs.add(it) }

        val allStations = mutableListOf<Station>()
        steps.firstOrNull()?.let { f -> stationMap[f.fromStationId]?.let { allStations.add(it) } }
        for (step in steps) {
            stationMap[step.toStationId]?.let { allStations.add(it) }
        }

        val interchangeStations = legs.dropLast(1).map { it.toStation.name }
        val fare = calculateFare(destNode.distanceKm)

        val title = when (mode) {
            RouteMode.FASTEST -> "Fastest Route"
            RouteMode.LEAST_INTERCHANGES -> "Least Interchanges"
            RouteMode.FEWEST_STATIONS -> "Fewest Stations"
        }

        return PlannedRoute(
            id = "${mode.name}_${System.currentTimeMillis()}",
            title = title,
            mode = mode,
            totalTimeMinutes = Math.max(1, (destNode.timeSec + 59) / 60),
            totalDistanceKm = (destNode.distanceKm * 10).toInt() / 10.0,
            totalStationsCount = allStations.size,
            interchangeCount = legs.size - 1,
            tokenFare = fare.first,
            smartCardFare = fare.second,
            legs = legs,
            allStationsInOrder = allStations,
            interchangeStationNames = interchangeStations
        )
    }

    private fun buildLeg(lineId: String, steps: List<ConnectionStep>): RouteLeg? {
        if (steps.isEmpty()) return null
        val line = lineMap[lineId] ?: return null
        val fromStation = stationMap[steps.first().fromStationId] ?: return null
        val toStation = stationMap[steps.last().toStationId] ?: return null

        val stationList = mutableListOf(fromStation)
        steps.forEach { step -> stationMap[step.toStationId]?.let { stationList.add(it) } }

        val duration = steps.sumOf { it.travelTimeSec }
        val distance = steps.sumOf { it.distanceKm }

        // Find terminal direction
        val direction = determineDirection(line, fromStation, toStation)
        val platform = stationLineMap[fromStation.id]?.find { it.lineId == lineId }?.platformNumber ?: "1"

        return RouteLeg(
            line = line,
            fromStation = fromStation,
            toStation = toStation,
            stations = stationList,
            directionTerminal = direction,
            platformNumber = platform,
            durationSeconds = duration,
            distanceKm = distance
        )
    }

    private fun determineDirection(line: Line, fromStation: Station, toStation: Station): String {
        val slList = stationLines.filter { it.lineId == line.id }.sortedBy { it.sequenceOrder }
        val fromSeq = slList.find { it.stationId == fromStation.id }?.sequenceOrder ?: 0
        val toSeq = slList.find { it.stationId == toStation.id }?.sequenceOrder ?: 0
        return if (toSeq >= fromSeq) line.terminalTo else line.terminalFrom
    }

    fun calculateFare(distanceKm: Double): Pair<Int, Int> {
        val slab = fareSlabs.firstOrNull { distanceKm >= it.minKm && distanceKm < it.maxKm }
            ?: fareSlabs.lastOrNull()

        if (slab != null) {
            return Pair(slab.tokenFare, slab.smartCardFare)
        }

        // Fallback standard Indian metro slab
        val token = when {
            distanceKm <= 2.0 -> 10
            distanceKm <= 5.0 -> 20
            distanceKm <= 12.0 -> 30
            distanceKm <= 21.0 -> 40
            distanceKm <= 32.0 -> 50
            else -> 60
        }
        val smartCard = (token * 0.9).toInt()
        return Pair(token, smartCard)
    }
}
