# ONVIF Java Async Client

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hyeon-mogu/onvif-java.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.hyeon-mogu/onvif-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**onvif-java** is an asynchronous Java client library for ONVIF IP cameras and video devices. It supports device discovery, media profile and URI access, continuous and absolute PTZ control, imaging, and PullPoint event subscriptions over SOAP.

---

## Features

- **Robust Security**: Supports **WS-Security Password Digest** authentication
- **Clock Sync**: Automatically compensates for device time offsets to avoid `401 Unauthorized` errors
- **Reactive and Non-Blocking**: Uses `CompletableFuture` for async flows
- **Auto Discovery**: Finds ONVIF devices with multicast **WS-Discovery**
- **Device Information**: Reads capabilities, services, and system date/time
- **Media Access**: Retrieves **RTSP stream URIs** and **JPEG snapshot URIs**
- **PTZ Control**: Supports continuous/absolute move, stop, typed position/status, configuration ranges, and preset operations
- **Imaging Control**: Supports imaging settings and focus operations
- **Event Subscription**: Supports **PullPoint** subscriptions and event polling for ONVIF events

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.hyeon-mogu</groupId>
    <artifactId>onvif-java</artifactId>
    <version>1.4.1</version>
</dependency>
```

### Gradle

```gradle
implementation group: 'io.github.hyeon-mogu', name: 'onvif-java', version: '1.4.1'
```

---

## Usage Guide

The **`OnvifClient`** is the main entry point. Most operations return `CompletableFuture`, allowing fluent async composition.

Initialization attempts a system-time lookup without a WS-Security token before authenticated requests. If the lookup fails, initialization continues without clock correction. Device-based imaging requests also use the measured time offset.

Discovered service addresses retain their scheme, host, port, and query parameters; snapshot requests use the media service. `OnvifServices` exposes these addresses through its `getDeviceXaddr()`, `getMediaXaddr()`, `getPtzXaddr()`, `getImagingXaddr()`, and `getEventsXaddr()` accessors while retaining the existing path accessors. Setting a legacy path clears the corresponding address override.

### 1. Direct Connection

If you already know the camera IP or hostname, connect directly and fetch basic device information.

```java
OnvifClient.connect("192.168.1.100")
    .credentials("admin", "password")
    .buildAsync()
    .thenCompose(device -> device.device().getDeviceInformation())
    .thenAccept(info -> log.info("Connected to: {} {}", info.getManufacturer(), info.getModel()))
    .exceptionally(ex -> {
        log.error("Setup failed", ex);
        return null;
    });
```

### 2. Network Discovery

Discover ONVIF devices on the local network.

```java
OnvifClient.discover(30000)
    .thenAccept(devices -> {
        log.info("Found {} cameras.", devices.size());
        devices.forEach(device -> log.info(" - {}", device.getHostName()));
    });
```

### 3. Media Profiles, Streams and Snapshots

Fetch the first media profile and retrieve its RTSP stream URI or JPEG snapshot URI.

```java
device.media().getMediaProfiles()
    .thenCompose(profiles -> {
        OnvifMediaProfile profile = profiles.get(0);
        return device.media().getMediaStreamURI(profile);
    })
    .thenAccept(uri -> log.info("RTSP stream URL: {}", uri));

device.media().getMediaProfiles()
    .thenCompose(profiles -> device.media().getSnapshotURI(profiles.get(0)))
    .thenAccept(uri -> log.info("Snapshot URL: {}", uri));
```

The stream defaults to RTP unicast over RTSP. To pick the transport, use the overload `getMediaStreamURI(profile, StreamType, TransportProtocol)` — for example `TransportProtocol.TCP` for NAT/firewall traversal.

### 4. PTZ Control

#### Continuous Movement

Start moving in a direction, then explicitly stop the camera.

```java
device.ptz().move(PtzType.LEFT)
    .thenCompose(res -> CompletableFuture.runAsync(() -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }))
    .thenCompose(v -> device.ptz().stop())
    .join();
```

#### Absolute Position and Supported Ranges

Read the current Pan/Tilt/Zoom position as a typed value. Coordinate ranges are device-specific and can be queried from the active PTZ configuration.

```java
device.ptz().getPosition()
    .thenAccept(position -> log.info("Current PTZ position: {}", position));

device.ptz().getConfigurationOptions()
    .thenAccept(options -> {
        if (options.supportsAbsolutePanTilt()) {
            log.info("Pan range: {}", options.getAbsolutePanTiltSpace().getXRange());
            log.info("Tilt range: {}", options.getAbsolutePanTiltSpace().getYRange());
        }
        if (options.supportsAbsoluteZoom()) {
            log.info("Zoom range: {}", options.getAbsoluteZoomSpace().getXRange());
        }
    });
```

#### Absolute Movement

Move directly to an absolute coordinate. The library checks the target against the ranges advertised by the camera before sending `AbsoluteMove`; configuration options are cached per device and PTZ configuration token.

```java
PtzPosition target = new PtzPosition(0.5, -0.2, 0.3);

device.ptz().absoluteMove(target)
    .thenAccept(result -> log.info("Absolute move result: {}", result))
    .join();
```

Not every camera supports absolute Pan/Tilt or Zoom. Unsupported axes complete the future exceptionally with `OnvifUnsupportedException`, while out-of-range coordinates use `OnvifValidationException`.

### 5. Imaging Settings

Read the current imaging settings and apply them again.

```java
device.imaging().getImagingSettings()
    .thenCompose(settings -> device.imaging().setImagingSettings(settings))
    .thenAccept(result -> log.info("Imaging update result: {}", result))
    .join();
```

### 6. Event Subscription

Subscribe to motion-related ONVIF events using the fluent event API.

```java
device.event().subscribe("tns1:VideoSource/MotionAlarm", event -> {
    log.info("Event topic: {}", event.getTopic());
    log.info("Event data: {}", event.getData());

    if (event.isMotionRelated() && event.isMotionActive()) {
        log.info("Motion is active.");
    }
}).thenAccept(session -> {
    try {
        Thread.sleep(60000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }

    session.unsubscribe().join();
}).join();
```

Managed event sessions renew before the camera-reported expiry, using the interval between `CurrentTime` and `TerminationTime` so local clock differences do not affect scheduling. If those times are unavailable, renewal is attempted every 30 seconds. Polling and renewal each retry transient network failures at most three times, after 1, 2, and 4 seconds; authentication and SOAP failures stop the session immediately. A failing event listener is logged and does not stop delivery of subsequent events.

Use `session.getCompletion().whenComplete((ignored, error) -> { /* handle shutdown or terminal failure */ })` to observe the background session. `getSubscription()` remains the initial snapshot. Repeated `unsubscribe()` calls share one result, cancel in-flight polling/renewal HTTP calls, and stop scheduling. Remote unsubscribe remains best-effort, and `close()` still waits for it. A callback already running when shutdown starts may finish. Cancelling the future returned by `subscribe()` also cleans up a subscription endpoint that arrives later.

---

## Requirements

- **Java 8 or higher**
- An **ONVIF-compliant network camera**

---

## Tested On

- **Vendor:** UNIVIEW
- **Model:** IPC
- **Firmware:** ONVIF_V3.0.0
