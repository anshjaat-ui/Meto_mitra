package com.example.metrosathi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class City(
    @PrimaryKey val id: String,
    val name: String,
    val nameHi: String,
    val state: String,
    val systemName: String,
    val operationalLines: Int,
    val totalStations: Int,
    val networkLengthKm: Double,
    val dailyRidership: String,
    val defaultLat: Double,
    val defaultLng: Double,
    val fareFormulaType: String,
    val rechargeUrl: String,
    val helplineNumber: String,
    val womenHelpline: String
)

@Entity(tableName = "lines")
data class Line(
    @PrimaryKey val id: String,
    val cityId: String,
    val name: String,
    val nameHi: String,
    val lineNumber: String,
    val colorHex: String,
    val textColorHex: String,
    val terminalFrom: String,
    val terminalTo: String,
    val totalStations: Int,
    val isOperational: Boolean = true
)

@Entity(tableName = "stations")
data class Station(
    @PrimaryKey val id: String,
    val cityId: String,
    val name: String,
    val nameHi: String,
    val code: String,
    val latitude: Double,
    val longitude: Double,
    val layoutType: String, // "Underground", "Elevated", "At-Grade"
    val openingYear: Int,
    val isInterchange: Boolean,
    val platformsCount: Int,
    val schematicX: Float,
    val schematicY: Float,
    val hasParking: Boolean,
    val isWheelchairAccessible: Boolean,
    val contactNumber: String = "155370"
)

@Entity(tableName = "station_lines")
data class StationLine(
    @PrimaryKey val id: String,
    val stationId: String,
    val lineId: String,
    val sequenceOrder: Int,
    val platformNumber: String
)

@Entity(tableName = "connections")
data class Connection(
    @PrimaryKey val id: String,
    val cityId: String,
    val lineId: String,
    val fromStationId: String,
    val toStationId: String,
    val distanceKm: Double,
    val travelTimeSeconds: Int
)

@Entity(tableName = "interchanges")
data class Interchange(
    @PrimaryKey val id: String,
    val stationId: String,
    val fromLineId: String,
    val toLineId: String,
    val walkingTimeMinutes: Int,
    val directions: String
)

@Entity(tableName = "gates")
data class Gate(
    @PrimaryKey val id: String,
    val stationId: String,
    val gateNumber: String,
    val facingDirectionOrLandmark: String,
    val hasLift: Boolean,
    val hasEscalator: Boolean,
    val isWheelchairAccessible: Boolean,
    val nearbyLandmarks: String
)

@Entity(tableName = "parking")
data class Parking(
    @PrimaryKey val id: String,
    val stationId: String,
    val available: Boolean,
    val twoWheelerCapacity: Int,
    val fourWheelerCapacity: Int,
    val cycleCapacity: Int,
    val chargesDescription: String,
    val timings: String,
    val locationNotes: String
)

@Entity(tableName = "amenities")
data class Amenity(
    @PrimaryKey val id: String,
    val stationId: String,
    val type: String, // "WASHROOM", "DRINKING_WATER", "ATM", "FOOD_STALL", "PHARMACY", "WIFI", "CUSTOMER_CARE", "BABY_FEEDING", "LIFT"
    val description: String,
    val locationInsideStation: String,
    val isOperational: Boolean = true
)

@Entity(tableName = "nearby_places")
data class NearbyPlace(
    @PrimaryKey val id: String,
    val stationId: String,
    val name: String,
    val category: String, // "Malls & Markets", "Hospitals", "Religious Places", "Institutions", "Transit Hubs", "Parks & Tourism", "Police"
    val distanceMeters: Int,
    val walkingMinutes: Int,
    val recommendedGate: String
)

@Entity(tableName = "timings")
data class Timing(
    @PrimaryKey val id: String,
    val stationId: String,
    val lineId: String,
    val directionTerminal: String,
    val firstTrainWeekday: String,
    val lastTrainWeekday: String,
    val firstTrainSunday: String,
    val lastTrainSunday: String,
    val peakFrequencyMin: Int,
    val nonPeakFrequencyMin: Int
)

@Entity(tableName = "fare_slabs")
data class FareSlab(
    @PrimaryKey val id: String,
    val cityId: String,
    val minKm: Double,
    val maxKm: Double,
    val tokenFare: Int,
    val smartCardFare: Int,
    val holidayOrOffPeakFare: Int,
    val description: String
)

@Entity(tableName = "favourites")
data class Favourite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fromStationId: String,
    val toStationId: String,
    val fromStationName: String,
    val toStationName: String,
    val cityName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_history")
data class TripHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromStationName: String,
    val toStationName: String,
    val farePaid: Int,
    val travelTimeMinutes: Int,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val cityName: String
)

@Entity(tableName = "recent_searches")
data class RecentSearch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "card_balance")
data class CardBalance(
    @PrimaryKey val cityId: String,
    val balance: Double,
    val lowBalanceThreshold: Double = 50.0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
