package com.example.metrosathi.domain

import com.example.metrosathi.data.model.Line
import com.example.metrosathi.data.model.NearbyPlace
import com.example.metrosathi.data.model.Station
import kotlin.math.*

data class StationSearchResult(
    val station: Station,
    val matchedLines: List<Line>,
    val matchedLandmark: String? = null,
    val distanceMeters: Int? = null,
    val walkMinutes: Int? = null
)

object SearchEngine {

    fun searchStations(
        query: String,
        stations: List<Station>,
        lines: List<Line>,
        nearbyPlaces: List<NearbyPlace>,
        userLat: Double? = null,
        userLng: Double? = null
    ): List<StationSearchResult> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) {
            return stations.map { station ->
                val linesForSt = findLinesForStation(station.id, lines)
                val dist = if (userLat != null && userLng != null) {
                    calculateDistanceMeters(userLat, userLng, station.latitude, station.longitude)
                } else null
                StationSearchResult(
                    station = station,
                    matchedLines = linesForSt,
                    distanceMeters = dist,
                    walkMinutes = dist?.let { calculateWalkMinutes(it) }
                )
            }.sortedBy { it.distanceMeters ?: Int.MAX_VALUE }
        }

        val results = mutableListOf<StationSearchResult>()
        val nearbyByStation = nearbyPlaces.groupBy { it.stationId }

        for (station in stations) {
            val linesForSt = findLinesForStation(station.id, lines)
            val stNameLower = station.name.lowercase()
            val stNameHiLower = station.nameHi.lowercase()
            val stCodeLower = station.code.lowercase()

            val dist = if (userLat != null && userLng != null) {
                calculateDistanceMeters(userLat, userLng, station.latitude, station.longitude)
            } else null

            var isMatch = false
            var matchedLandmark: String? = null

            // 1. Direct name or Hindi or Code match
            if (stNameLower.contains(cleanQuery) || stNameHiLower.contains(cleanQuery) || stCodeLower.contains(cleanQuery)) {
                isMatch = true
            }

            // 2. Transliteration / alternate keywords check
            if (!isMatch && matchesTransliteration(cleanQuery, stNameLower, stNameHiLower)) {
                isMatch = true
            }

            // 3. Line name match (e.g. user typed "yellow line" or "red")
            if (!isMatch) {
                val matchedLine = linesForSt.find {
                    it.name.lowercase().contains(cleanQuery) || it.nameHi.lowercase().contains(cleanQuery)
                }
                if (matchedLine != null) {
                    isMatch = true
                }
            }

            // 4. Landmark match
            if (!isMatch) {
                val places = nearbyByStation[station.id] ?: emptyList()
                val foundPlace = places.find {
                    it.name.lowercase().contains(cleanQuery) || it.category.lowercase().contains(cleanQuery)
                }
                if (foundPlace != null) {
                    isMatch = true
                    matchedLandmark = foundPlace.name
                }
            }

            if (isMatch) {
                results.add(
                    StationSearchResult(
                        station = station,
                        matchedLines = linesForSt,
                        matchedLandmark = matchedLandmark,
                        distanceMeters = dist,
                        walkMinutes = dist?.let { calculateWalkMinutes(it) }
                    )
                )
            }
        }

        // Sort: exact matches first, then proximity if location is provided
        return results.sortedWith(
            compareBy<StationSearchResult> {
                val name = it.station.name.lowercase()
                when {
                    name == cleanQuery -> 0
                    name.startsWith(cleanQuery) -> 1
                    name.contains(cleanQuery) -> 2
                    else -> 3
                }
            }.thenBy { it.distanceMeters ?: Int.MAX_VALUE }
        )
    }

    private fun findLinesForStation(stationId: String, lines: List<Line>): List<Line> {
        // Will match lines whose id is part of station or vice-versa, or matched from lines list
        return lines.filter { line ->
            stationId.contains(line.id.replace("delhi_", "").replace("mumbai_", "").replace("bengaluru_", ""))
        }.ifEmpty { lines.take(1) }
    }

    private fun matchesTransliteration(query: String, enName: String, hiName: String): Boolean {
        val pairs = listOf(
            "rajiv" to "राजीव",
            "chowk" to "चौक",
            "kashmere" to "कश्मीरी",
            "gate" to "गेट",
            "chandni" to "चांदनी",
            "badli" to "बादली",
            "hauz" to "हौज",
            "khas" to "खास",
            "noida" to "नोएडा",
            "gurgaon" to "गुड़गांव",
            "gurugram" to "गुरुग्राम",
            "aiims" to "एम्स",
            "cp" to "rajiv chowk",
            "delhi cantt" to "दिल्ली कैंट",
            "connaught" to "rajiv chowk",
            "majestic" to "nadaprabhu kempegowda",
            "andheri" to "अंधेरी",
            "ghatkopar" to "घाटकोपर"
        )

        for ((en, hi) in pairs) {
            if ((query.contains(en) || query.contains(hi)) &&
                (enName.contains(en) || hiName.contains(hi) || enName.contains(hi) || hiName.contains(en))
            ) {
                return true
            }
        }
        return false
    }

    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toInt()
    }

    fun calculateWalkMinutes(meters: Int): Int {
        // Average walking speed 4.8 km/h = 80 meters/min
        return max(1, (meters + 79) / 80)
    }
}
