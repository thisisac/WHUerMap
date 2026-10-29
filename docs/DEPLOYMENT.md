# Deployment Guide

## Client Release

1. Replace the example WMS and WFS URLs with services you own or are authorized
   to use.
2. Confirm the map and route layer projection contract.
3. Test runtime location permission on target Android versions.
4. Build a release APK with Android Studio or the Gradle Wrapper.
5. Sign the APK with a private release keystore that is never committed.
6. Publish release notes with backend requirements and dataset licenses.

The repository intentionally does not include signing keys, server credentials,
or `local.properties`.

## Backend Requirements

The backend should provide a WMS layer for the campus basemap, a WFS route layer
or equivalent GeoJSON endpoint, a pedestrian-network route function, stable
coordinate-reference behavior, and monitoring for latency and errors.

For a typical deployment, PostgreSQL/PostGIS stores spatial features and
pgRouting or an equivalent component computes the route. GeoServer publishes
the data and exposes the request contract.

## Public Release Checklist

- Remove or replace course-era service URLs.
- Use HTTPS for production endpoints.
- Review map-data and POI-data licenses.
- Add a privacy policy if location data is logged or retained.
- Test empty routes, timeouts, permission denial, and malformed responses.
- Verify that release artifacts contain no secrets or local paths.

