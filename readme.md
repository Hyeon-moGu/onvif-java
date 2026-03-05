# ONVIF Java Async Client

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hyeon-mogu/onvif-java.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.hyeon-mogu/onvif-java)

A lightweight, asynchronous Java library designed to simplify the control of ONVIF-compliant IP cameras.

This library abstracts the complexity of the SOAP/XML protocol into simple, non-blocking Java APIs. It supports essential features such as device discovery, secure authentication, real-time PTZ control, and imaging parameter adjustments.

Designed for flexibility, operations can be performed seamlessly using managed `OnvifDevice` objects or by providing direct endpoints and credentials.

---

## Installation

### Maven:

```Java
<dependency>
    <groupId>io.github.hyeon-mogu</groupId>
    <artifactId>onvif-java</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle:

```Java
implementation group: 'io.github.hyeon-mogu', name: 'onvif-java', version: '1.1.0'
```

---

## Key Features

- **WS-Security Authentication**: Simple Username Token Profile Password Digest authentication.
- **Automated Clock Sync**: Automatically handles camera time offsets to prevent authentication errors.
- **Asynchronous & Non-Blocking**: Uses callbacks for responsive network operations without blocking the main thread.
- **Device Discovery**: Detects ONVIF devices on the local network via multicast WS-Discovery.
- **PTZ (Pan/Tilt/Zoom) Control**: Basic movement, status retrieval, and preset management.
- **Imaging Settings**: Read and update basic imaging parameters (Brightness, Exposure Modes, Focus, etc).
- **Media Information**: Fetch RTSP Stream URIs and JPEG Snapshot links seamlessly.

---

## Quick Start (Test Application)

Instead of setting up individual managers manually, you can explore all read-only capabilities of your camera instantly using the provided `OnvifTester.java` script.

The tester automatically discovers cameras on your network, synchronizes the time (for WS-Security), and swiftly retrieves essential data (RTSP, Snapshot, Profiles, Capabilities, PTZ Status, Presets, and Imaging Settings).

### Example

```text
========== ONVIF Device Discovery Started ==========
Scanning network... (Max 20 seconds)

Scan complete! Found 1 devices.

------------------------------------------------
Proceeding with single camera test...
Target IP: 192.168.1.x
Base URL: http://192.168.1.x:8080
Username: admin
------------------------------------------------
[Success] Camera Time: Thu Jan 01 01:00:00 UTC 2026
[Applied] Time Offset (Clock Sync): -1200ms
[Success] Device Info: Model=IPCamera, Manufacturer=IPC Professional, Firmware=ONVIF_V3.0.0.20170510
[Success] Capabilities Retrieved.
[Success] Found 4 Media Profiles
  - Profile: proname_ch0001 (Token: protoken_ch0001)
  - Profile: proname_ch0002 (Token: protoken_ch0002)
  - Profile: proname_ch0003 (Token: protoken_ch0003)
  - Profile: new profile (Token: protoken_ch0004)
[Success] RTSP Stream URI: rtsp://192.168.1.x:554/1/1
[Success] Snapshot JPEG URI: http://192.168.1.x/jpeg/pic_type00_01.jpg
[Success] PTZ Status (Coordinates): Position: (Pan: -1, Tilt: -1.25806451, Zoom: 0), MoveStatus: (PanTilt: IDLE, Zoom: IDLE), Time: 2026-01-01T01:00:00Z
[Success] PTZ Presets List: Preset(1: home), Preset(2: door)
[Success] Imaging Settings: {brightness=50.0, colorSaturation=50.0, contrast=50.0, exposureTime=0.0, maxExposureTime=83333.0, minGain=0.0, maxGain=16.0, autofocusMode='AUTO'}

[Test Complete] Terminating JVM cleanly...
```

For detailed usage on PTZ controls and Imaging settings, please refer to the source code of the manager classes (`OnvifManager`, `PtzManager`, `ImagingManager`).

---
 
## Requirements

- **Java 8 or higher**  
- An **ONVIF-compliant network camera**  

---

### Tested On
* **Vendor:** UNIVIEW
* **Model:** IPC6222ER-X20
* **Firmware:** ONVIF_V3.0.0.20170510