package com.example.metrosathi.data.seed

import android.content.Context
import com.example.metrosathi.data.local.MetroDao
import com.example.metrosathi.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object SeedDataLoader {

    suspend fun seedDatabaseIfNeeded(context: Context, dao: MetroDao) = withContext(Dispatchers.IO) {
        // 1. Seed Cities if not present
        val delhiCount = dao.getStationCountForCity("delhi")
        if (delhiCount == 0) {
            seedCities(context, dao)
            seedCityPack(context, dao, "delhi_metro.json")
            seedCityPack(context, dao, "mumbai_metro.json")
            seedCityPack(context, dao, "bengaluru_metro.json")
        }
    }

    private suspend fun seedCities(context: Context, dao: MetroDao) {
        val jsonStr = readAssetFile(context, "data/cities.json") ?: return
        val array = JSONArray(jsonStr)
        val cities = mutableListOf<City>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            cities.add(
                City(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    nameHi = obj.optString("nameHi", obj.getString("name")),
                    state = obj.optString("state", ""),
                    systemName = obj.optString("systemName", ""),
                    operationalLines = obj.optInt("operationalLines", 1),
                    totalStations = obj.optInt("totalStations", 0),
                    networkLengthKm = obj.optDouble("networkLengthKm", 0.0),
                    dailyRidership = obj.optString("dailyRidership", ""),
                    defaultLat = obj.optDouble("defaultLat", 28.6139),
                    defaultLng = obj.optDouble("defaultLng", 77.2090),
                    fareFormulaType = obj.optString("fareFormulaType", "standard_slab"),
                    rechargeUrl = obj.optString("rechargeUrl", ""),
                    helplineNumber = obj.optString("helplineNumber", "155370"),
                    womenHelpline = obj.optString("womenHelpline", "1091")
                )
            )
        }
        dao.insertCities(cities)
    }

    private suspend fun seedCityPack(context: Context, dao: MetroDao, fileName: String) {
        val jsonStr = readAssetFile(context, "data/$fileName") ?: return
        val root = JSONObject(jsonStr)

        // Lines
        if (root.has("lines")) {
            val linesArr = root.getJSONArray("lines")
            val lines = mutableListOf<Line>()
            for (i in 0 until linesArr.length()) {
                val obj = linesArr.getJSONObject(i)
                lines.add(
                    Line(
                        id = obj.getString("id"),
                        cityId = obj.getString("cityId"),
                        name = obj.getString("name"),
                        nameHi = obj.optString("nameHi", obj.getString("name")),
                        lineNumber = obj.optString("lineNumber", "1"),
                        colorHex = obj.optString("colorHex", "#1E88E5"),
                        textColorHex = obj.optString("textColorHex", "#FFFFFF"),
                        terminalFrom = obj.optString("terminalFrom", ""),
                        terminalTo = obj.optString("terminalTo", ""),
                        totalStations = obj.optInt("totalStations", 0),
                        isOperational = obj.optBoolean("isOperational", true)
                    )
                )
            }
            dao.insertLines(lines)
        }

        // Stations
        if (root.has("stations")) {
            val stationsArr = root.getJSONArray("stations")
            val stations = mutableListOf<Station>()
            for (i in 0 until stationsArr.length()) {
                val obj = stationsArr.getJSONObject(i)
                stations.add(
                    Station(
                        id = obj.getString("id"),
                        cityId = obj.getString("cityId"),
                        name = obj.getString("name"),
                        nameHi = obj.optString("nameHi", obj.getString("name")),
                        code = obj.optString("code", ""),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        layoutType = obj.optString("layoutType", "Elevated"),
                        openingYear = obj.optInt("openingYear", 2010),
                        isInterchange = obj.optBoolean("isInterchange", false),
                        platformsCount = obj.optInt("platformsCount", 2),
                        schematicX = obj.optDouble("schematicX", 500.0).toFloat(),
                        schematicY = obj.optDouble("schematicY", 500.0).toFloat(),
                        hasParking = obj.optBoolean("hasParking", false),
                        isWheelchairAccessible = obj.optBoolean("isWheelchairAccessible", true),
                        contactNumber = obj.optString("contactNumber", "155370")
                    )
                )
            }
            dao.insertStations(stations)
        }

        // Station Lines
        if (root.has("station_lines")) {
            val arr = root.getJSONArray("station_lines")
            val list = mutableListOf<StationLine>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    StationLine(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        lineId = obj.getString("lineId"),
                        sequenceOrder = obj.getInt("sequenceOrder"),
                        platformNumber = obj.optString("platformNumber", "1")
                    )
                )
            }
            dao.insertStationLines(list)
        }

        // Connections
        if (root.has("connections")) {
            val arr = root.getJSONArray("connections")
            val list = mutableListOf<Connection>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Connection(
                        id = obj.getString("id"),
                        cityId = obj.getString("cityId"),
                        lineId = obj.getString("lineId"),
                        fromStationId = obj.getString("fromStationId"),
                        toStationId = obj.getString("toStationId"),
                        distanceKm = obj.getDouble("distanceKm"),
                        travelTimeSeconds = obj.getInt("travelTimeSeconds")
                    )
                )
            }
            dao.insertConnections(list)
        }

        // Interchanges
        if (root.has("interchanges")) {
            val arr = root.getJSONArray("interchanges")
            val list = mutableListOf<Interchange>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Interchange(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        fromLineId = obj.getString("fromLineId"),
                        toLineId = obj.getString("toLineId"),
                        walkingTimeMinutes = obj.optInt("walkingTimeMinutes", 4),
                        directions = obj.optString("directions", "")
                    )
                )
            }
            dao.insertInterchanges(list)
        }

        // Gates
        if (root.has("gates")) {
            val arr = root.getJSONArray("gates")
            val list = mutableListOf<Gate>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Gate(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        gateNumber = obj.getString("gateNumber"),
                        facingDirectionOrLandmark = obj.optString("facingDirectionOrLandmark", ""),
                        hasLift = obj.optBoolean("hasLift", false),
                        hasEscalator = obj.optBoolean("hasEscalator", true),
                        isWheelchairAccessible = obj.optBoolean("isWheelchairAccessible", false),
                        nearbyLandmarks = obj.optString("nearbyLandmarks", "")
                    )
                )
            }
            dao.insertGates(list)
        }

        // Parking
        if (root.has("parking")) {
            val arr = root.getJSONArray("parking")
            val list = mutableListOf<Parking>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Parking(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        available = obj.getBoolean("available"),
                        twoWheelerCapacity = obj.optInt("twoWheelerCapacity", 0),
                        fourWheelerCapacity = obj.optInt("fourWheelerCapacity", 0),
                        cycleCapacity = obj.optInt("cycleCapacity", 0),
                        chargesDescription = obj.optString("chargesDescription", ""),
                        timings = obj.optString("timings", ""),
                        locationNotes = obj.optString("locationNotes", "")
                    )
                )
            }
            dao.insertParking(list)
        }

        // Amenities
        if (root.has("amenities")) {
            val arr = root.getJSONArray("amenities")
            val list = mutableListOf<Amenity>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Amenity(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        type = obj.getString("type"),
                        description = obj.optString("description", ""),
                        locationInsideStation = obj.optString("locationInsideStation", ""),
                        isOperational = obj.optBoolean("isOperational", true)
                    )
                )
            }
            dao.insertAmenities(list)
        }

        // Nearby Places
        if (root.has("nearby_places")) {
            val arr = root.getJSONArray("nearby_places")
            val list = mutableListOf<NearbyPlace>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    NearbyPlace(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        name = obj.getString("name"),
                        category = obj.optString("category", "Transit Hubs"),
                        distanceMeters = obj.optInt("distanceMeters", 100),
                        walkingMinutes = obj.optInt("walkingMinutes", 2),
                        recommendedGate = obj.optString("recommendedGate", "Gate 1")
                    )
                )
            }
            dao.insertNearbyPlaces(list)
        }

        // Timings
        if (root.has("timings")) {
            val arr = root.getJSONArray("timings")
            val list = mutableListOf<Timing>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Timing(
                        id = obj.getString("id"),
                        stationId = obj.getString("stationId"),
                        lineId = obj.getString("lineId"),
                        directionTerminal = obj.optString("directionTerminal", ""),
                        firstTrainWeekday = obj.optString("firstTrainWeekday", "05:45 AM"),
                        lastTrainWeekday = obj.optString("lastTrainWeekday", "11:30 PM"),
                        firstTrainSunday = obj.optString("firstTrainSunday", "06:00 AM"),
                        lastTrainSunday = obj.optString("lastTrainSunday", "11:30 PM"),
                        peakFrequencyMin = obj.optInt("peakFrequencyMin", 3),
                        nonPeakFrequencyMin = obj.optInt("nonPeakFrequencyMin", 5)
                    )
                )
            }
            dao.insertTimings(list)
        }

        // Fare Slabs
        if (root.has("fare_slabs")) {
            val arr = root.getJSONArray("fare_slabs")
            val list = mutableListOf<FareSlab>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    FareSlab(
                        id = obj.getString("id"),
                        cityId = obj.getString("cityId"),
                        minKm = obj.getDouble("minKm"),
                        maxKm = obj.getDouble("maxKm"),
                        tokenFare = obj.getInt("tokenFare"),
                        smartCardFare = obj.getInt("smartCardFare"),
                        holidayOrOffPeakFare = obj.optInt("holidayOrOffPeakFare", obj.getInt("smartCardFare")),
                        description = obj.optString("description", "")
                    )
                )
            }
            dao.insertFareSlabs(list)
        }
    }

    private fun readAssetFile(context: Context, path: String): String? {
        return try {
            val isStream = context.assets.open(path)
            BufferedReader(InputStreamReader(isStream)).use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
