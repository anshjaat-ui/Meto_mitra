# MetroSathi - Indian Metro Transit Companion 🚇

**MetroSathi** is a complete, offline-first Android application built with Kotlin and Jetpack Compose for commuters across Indian metro transit networks.

---

## 🌟 Key Features

1. **City Selection (17+ Cities)**:
   - Delhi-NCR (DMRC), Mumbai Metro (MMRDA), Bengaluru Namma Metro (BMRCL), Kolkata Metro, Chennai Metro (CMRL), Hyderabad Metro, Jaipur Metro, Lucknow Metro, Kochi Metro, Pune Metro, Ahmedabad Metro, Kanpur Metro, Agra Metro, Bhopal Metro, Indore Metro, Nagpur Metro, and Patna Metro.
   - 1-tap city switching from the top bar with persistent DataStore preferences.

2. **Full Metro Route Map (Dual Mode)**:
   - **Interactive Schematic Map**: Color-coded lines, station nodes, interchange transfer markers, multi-touch pinch-to-zoom, 2D pan, and fit-to-screen button.
   - **Geographic Map View**: Scaled OSM coordinate view.
   - Tap any station to open a bottom sheet quick summary with direct navigation actions.
   - Active route highlighting overlay and line visibility filters.

3. **Intelligent Route Planner**:
   - Dijkstra's graph shortest-path algorithm with configurable interchange transfer penalties.
   - Modes: **Fastest Route**, **Least Interchanges**, and **Fewest Stations**.
   - Step-by-step guidance: Line boarding, platform numbers, direction of travel, transfer concourse instructions, and deboarding exit gates.
   - 2-3 alternative routes for every query.
   - 1-tap WhatsApp route share and pin to saved favorites.

4. **Distance & Slab-Based Fare Calculator**:
   - Distance slab calculations per city fare rules.
   - Breakdown of **Token / QR** vs **Smart Card** (10% standard discount) vs **Holiday / Off-peak** fares.

5. **Station Guide (9 Comprehensive Tabs)**:
   - **Overview**: Station layout, opening year, code, platform count, and helpline.
   - **Gates & Exits**: Gate directions, landmarks, elevator & escalator availability, wheelchair accessibility.
   - **Parking**: Capacity (2-wheeler, 4-wheeler, bicycle), charges, and timings.
   - **Amenities**: Restrooms, drinking water fountains, ATMs, food courts, pharmacies, and baby care rooms.
   - **Nearby Places**: Categorized landmarks with walking distance and 1-tap Google Maps directions.
   - **Timings**: First and last trains per direction for weekdays and Sundays.
   - **Accessibility**: Ladies coach guidelines, tactile paving, and station support numbers.

6. **Transit Utilities**:
   - **Nearest Metro Station**: GPS-based Haversine distance calculator with estimated walking times.
   - **Destination Travel Alarm**: Foreground service location tracker with vibration and audio alarm before reaching destination.
   - **Metro Card Balance Reminder**: Balance tracker with low-balance alerts and links to official recharge portals.
   - **Trip History & Spend Tracker**: Monthly travel spending logs.
   - **Metro Rules & Helplines**: Luggage weight limits, fine tariffs, and 1-tap emergency dialer.

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose + Material Design 3 (Dynamic Color, Dark & Light mode)
- **Architecture:** MVVM + Clean Architecture (Data, Domain, Presentation)
- **Local Database:** Room 2.7.0 (Offline-first with JSON seed asset pre-population)
- **Key-Value Storage:** AndroidX DataStore Preferences
- **Location Services:** Google Play Services FusedLocationProviderClient
- **Background Tasks:** Android Foreground Service with notification channels
- **Testing:** JUnit, Robolectric local JVM testing

---

## 📂 Project Directory Structure

```
app/src/
├── main/
│   ├── AndroidManifest.xml
│   ├── assets/data/
│   │   ├── cities.json            # 17 Indian metro networks metadata
│   │   ├── delhi_metro.json       # Full Delhi-NCR Metro dataset
│   │   ├── mumbai_metro.json      # Mumbai Metro dataset
│   │   └── bengaluru_metro.json   # Namma Metro dataset
│   ├── java/com/
│   │   ├── MainActivity.kt        # Single activity + Compose navigation
│   │   ├── example/
│   │   │   ├── ui/theme/          # M3 Colors, Typography & Theme
│   │   │   └── metrosathi/
│   │   │       ├── data/
│   │   │       │   ├── model/     # Room Entities (City, Line, Station, Gate, etc.)
│   │   │       │   ├── local/     # AppDatabase, MetroDao, PreferencesManager
│   │   │       │   ├── seed/      # SeedDataLoader (asset parser)
│   │   │       │   └── repository/# MetroRepository
│   │   │       ├── domain/        # RoutingEngine (Dijkstra), SearchEngine
│   │   │       ├── service/       # DestinationAlarmService (Foreground GPS)
│   │   │       └── ui/
│   │   │           ├── MetroViewModel.kt
│   │   │           ├── components/
│   │   │           └── screens/
│   └── res/
│       ├── values/strings.xml     # English strings
│       └── values-hi/strings.xml  # Hindi (हिंदी) strings
tools/
└── gtfs_to_metrosathi.py          # Python converter for GTFS / OSM data packs
```

---

## 🚀 How to Add a New City Data Pack

1. Obtain standard GTFS feed files (`routes.txt`, `stops.txt`, `stop_times.txt`) or OpenStreetMap Overpass exports for the metro network.
2. Run the provided converter script:
   ```bash
   python3 tools/gtfs_to_metrosathi.py --city hyderabad --gtfs-dir ./gtfs_hyderabad/ --out-file app/src/main/assets/data/hyderabad_metro.json
   ```
3. Add the city entry to `cities.json` and register the filename in `SeedDataLoader.kt`.
4. Run the app — the Room database will automatically seed the new city on next launch!

---

## 📦 How to Build Release APK / AAB

1. Build Debug APK:
   ```bash
   gradle assembleDebug
   ```
2. Build Release APK:
   ```bash
   gradle assembleRelease
   ```
3. Run Unit Tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
