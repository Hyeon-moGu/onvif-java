# ONVIF Java Async Client

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hyeon-mogu/onvif-java.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.hyeon-mogu/onvif-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**onvif-java** is a Java client library for ONVIF IP cameras and video devices. It provides asynchronous APIs for device discovery, media access, PTZ control, imaging, and event subscriptions over SOAP.

---

## Features

- **Robust Security**: Supports **WS-Security Password Digest** authentication
- **Clock Sync**: Automatically compensates for device time offsets to avoid `401 Unauthorized` errors
- **Reactive and Non-Blocking**: Uses `CompletableFuture` for async flows
- **Auto Discovery**: Finds ONVIF devices with multicast **WS-Discovery**
- **Device Information**: Reads capabilities, services, and system date/time
- **Media Access**: Retrieves **RTSP stream URIs** and **JPEG snapshot URIs**
- **PTZ Control**: Supports move, stop, status, and preset operations
- **Imaging Control**: Supports imaging settings and focus operations
- **Event Subscription**: Supports **PullPoint** subscriptions and event polling for ONVIF events

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.hyeon-mogu</groupId>
    <artifactId>onvif-java</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Gradle

```gradle
implementation group: 'io.github.hyeon-mogu', name: 'onvif-java', version: '1.3.0'
```

---

## Usage Guide

The **`OnvifClient`** is the main entry point. Most operations return `CompletableFuture`, allowing fluent async composition.

### 1. Direct Connection

If you already know the camera IP or hostname, connect directly and fetch basic device information.

```java
OnvifClient.connect("192.168.1.100")
    .credentials("admin", "password")
    .buildAsync()
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

Discover ONVIF devices on the local network.

```java
OnvifClient.discover(30000)
    .thenAccept(devices -> {
        System.out.println("Found " + devices.size() + " cameras.");
        devices.forEach(device -> System.out.println(" - " + device.getHostName()));
    });
```

### 3. Media Profiles and Snapshots

Fetch the first media profile and retrieve its snapshot URI.

```java
device.media().getMediaProfiles()
    .thenCompose(profiles -> {
        OnvifMediaProfile profile = profiles.get(0);
        return device.media().getSnapshotURI(profile);
    })
    .thenAccept(uri -> System.out.println("Snapshot URL: " + uri));
```

### 4. PTZ Movement Control

Move the camera and stop it after a short delay.

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

### 5. Imaging Settings

Read the current imaging settings and apply them again.

```java
device.imaging().getImagingSettings()
    .thenCompose(settings -> device.imaging().setImagingSettings(settings))
    .thenAccept(result -> System.out.println("Imaging update result: " + result))
    .join();
```

### 6. Event Subscription

Subscribe to motion-related ONVIF events using the fluent event API.

```java
device.event().subscribe("tns1:VideoSource/MotionAlarm", event -> {
    System.out.println("Event topic: " + event.getTopic());
    System.out.println("Event data: " + event.getData());

    if (event.isMotionRelated() && event.isMotionActive()) {
        System.out.println("Motion is active.");
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

---

## Requirements

- **Java 8 or higher**
- An **ONVIF-compliant network camera**

---

## Tested On

- **Vendor:** UNIVIEW
- **Model:** IPC
- **Firmware:** ONVIF_V3.0.0
