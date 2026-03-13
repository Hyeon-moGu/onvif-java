# ONVIF Java Async Client

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hyeon-mogu/onvif-java.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.hyeon-mogu/onvif-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**onvif-java** is a high-performance, asynchronous Java library that simplifies network camera control by abstracting complex SOAP/XML protocols into an **elegant, non-blocking Fluent API**.

Built on **Java 8+** and `CompletableFuture`, it provides a developer-friendly interface for critical ONVIF operations—including seamless device discovery, secure WS-Security authentication, and real-time PTZ/Imaging control—without the overhead of heavy dependencies.

---

## Features

- **Robust Security**: Full support for **WS-Security (Password Digest)**, ensuring secure communication.
- **Clock Sync**: Automatically handles camera time offsets to prevent `401 Unauthorized` errors.
- **Reactive & Non-Blocking**: Built on `CompletableFuture` for high-performance automation.
- **Auto Discovery**: Instantly detects ONVIF devices via multicast **WS-Discovery**.
- **Fluent PTZ**: Effortlessly manage Pan/Tilt/Zoom, status retrieval, and presets.
- **Media Access**: Rapidly fetch **RTSP stream URIs** and **JPEG snapshots**.
- **Imaging Control**: Programmatically adjust brightness, exposure, contrast, and focus.

---

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.hyeon-mogu</groupId>
    <artifactId>onvif-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle:

```gradle
implementation group: 'io.github.hyeon-mogu', name: 'onvif-java', version: '1.2.0'
```

---

## Usage Guide

The **`OnvifClient`** is your entry point. All operations return `CompletableFuture`, allowing for powerful, non-blocking automation.

### 1. Direct Connection
If you already know your camera's IP, connect directly and fetch its model information.

```java
OnvifClient.connect("192.168.1.100")
    .credentials("admin", "password")
    .buildAsync() // Syncs clock & pre-fetches profiles
    .thenCompose(device -> device.device().getDeviceInformation())
    .thenAccept(info -> {
        System.out.println("Connected to: " + info.getManufacturer() + " " + info.getModel());
    })
    .exceptionally(ex -> {
        System.err.println("Setup failed: " + ex.getMessage());
        return null;
    });
```

### 2. Network Discovery
Dynamically find all ONVIF cameras on your local network.

```java
OnvifClient.discover(30000)
    .thenAccept(devices -> {
        System.out.println("Found " + devices.size() + " cameras.");
        devices.forEach(d -> System.out.println(" - " + d.getHostName()));
    });
```

### 3. Practical Operations
Get the first profile and fetch a high-res snapshot.

```java
device.media().getMediaProfiles()
    .thenCompose(profiles -> {
        OnvifMediaProfile mainProfile = profiles.get(0);
        return device.media().getSnapshotURI(mainProfile);
    })
    .thenAccept(uri -> System.out.println("Snapshot URL: " + uri));
```

### 4. PTZ Movement Control
Precise control over Pan, Tilt, and Zoom.

```java
device.ptz().move(PtzType.UP)
    .thenCompose(res -> CompletableFuture.runAsync(() -> {
        try { Thread.sleep(1000); } catch (Exception e) {}
    }))
    .thenCompose(v -> device.ptz().stop())
    .join();
```

---
 
## Requirements

- **Java 8 or higher**  
- An **ONVIF-compliant network camera**  

---

### Tested On
* **Vendor:** UNIVIEW
* **Model:** IPC
* **Firmware:** ONVIF_V3.0.0
