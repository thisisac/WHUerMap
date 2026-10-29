# WHUerMap Architecture

## 1. Scope

WHUerMap is split into a mobile client and an external spatial-data backend.
The client handles presentation, device location, coordinate conversion, request
construction, and route rendering. The backend owns campus data and network
analysis.

## 2. Layered Design

### Presentation layer

`activity_main.xml` defines the map-first interface. A `MapView` fills the
screen, while a floating card contains the start/end inputs and route actions.
The location FAB is independent of the route form so users can recenter the map
at any time.

`activity_settings.xml` provides height, location-frequency, and navigation
detail controls. Preferences are stored locally with `SharedPreferences`.

### Application/controller layer

`MainActivity` coordinates interaction state:

- `startPoint` and `endPoint` hold the current route endpoints.
- Temporary start/end markers are replaced when the user selects new points.
- `currentRouteLine` is removed before a new route is displayed.
- `currentGPSPoint` is updated by the Android location callback.

The activity requests runtime location permission, initializes osmdroid,
registers map events, and sends UI updates back to the main thread after
background work completes.

### Domain/data layer

`WhuRouteTask` builds an HTTP WFS request. `WhuRoute` parses the response into
an osmdroid `Polyline`. `WebMecator` is a stateless projection utility.
`NmeaParser` is a diagnostic parser for raw GNSS sentences.

## 3. Backend Data Flow

```text
WGS84 point
   │ WebMecator.webMecatorbl2xy
   ▼
EPSG:3857 point
   │ WhuRouteTask + WFS viewparams
   ▼
GeoServer SQL View
   │ PostgreSQL/PostGIS + pgRouting
   ▼
GeoJSON FeatureCollection
   │ WhuRoute + inverse projection
   ▼
osmdroid Polyline
```

The route calculation is deliberately outside the Android process. This keeps
the client small and allows road-network weights, restrictions, and algorithms
to evolve without shipping a new APK.

## 4. Coordinate Contract

The client assumes:

- User input and Android location callbacks use WGS84 latitude/longitude.
- WFS route parameters use EPSG:3857 meters.
- GeoJSON route coordinates use the same projected coordinate system.
- The WMS layer and map view use a compatible projection.

Breaking this contract causes visible displacement or failed routes. A backend
replacement must preserve the contract or update both `WebMecator` and the
GeoServer layer configuration.

## 5. Threading Model

Network I/O runs in `WhuRouteTask` on an `ExecutorService`. Route parsing and
overlay updates are delivered through `WhuRouteListner`; UI mutations are moved
back to the Android main thread. This prevents network waits from freezing map
gestures and avoids touching views from a worker thread.

## 6. Failure Boundaries

- Permission denied: the map can still open, but GPS updates are unavailable.
- WMS unavailable: the map cannot load the configured basemap.
- WFS timeout or HTTP error: no route is drawn and the user receives a failure
  message.
- Empty or malformed GeoJSON: no usable line is added.
- Endpoints outside the routable network: the backend may return no features.

Production deployments should add structured error responses, retry/backoff
policy, service health checks, and configuration injection.
