# ONVIF Java Client: **Asynchronous Library** for **IP Camera Control**

This is a **lightweight, asynchronous Java client library** designed to simplify communication with **ONVIF-compliant IP cameras** and network video devices. By handling the complexity of the SOAP/XML protocol internally, this library allows developers to integrate robust camera control (PTZ, Imaging, Device Management) into their Java applications using simple, non-blocking method calls.

**Beyond Simple Control**: While the library is designed for simple control using OnvifDevice objects, you also have the flexibility to make direct requests using a device's XAddr (web service address) and Token information when necessary.

**Asynchronous Processing**:
A core design principle of this library is non-blocking asynchronous operation.

---

### ✅ Tested On
* **Vendor:** UNIVIEW
* **Model:** IPC6222ER-X20
* **Firmware:** IPC_HCMN1102-R5028P07D1603C02

---

## Installation

### Maven:

```Java
<dependency>
    <groupId>io.github.hyeon-mogu</groupId>
    <artifactId>onvif-java</artifactId>
    <version>1.0.2</version>
</dependency>
```

### Gradle:

```Java
implementation group: 'io.github.hyeon-mogu', name: 'onvif-java', version: '1.0.2'
```

---

## Key Features

- **Standard WS-Security Authentication**: Securely authenticates using the WS-Security (Username Token Profile) Password Digest method, adhering to the strict ONVIF standard.

- **Asynchronous & Non-Blocking**: Non-blocking callback mechanism ensures application responsiveness during network operations.

- **ONVIF Device Discovery**: Automatically detects and connects to ONVIF-compliant IP cameras on the local network.

- **Comprehensive PTZ Control**: Full support for real-time Pan/Tilt/Zoom movements and Stop functionality.

- **PTZ Preset Management**: Comprehensive preset management including saving positions (Save), moving to saved positions (Goto), retrieving the full list of presets (GetPresets), and deleting existing presets (Remove).

- **PTZ Status Inquiry**: Retrieve the camera's current PTZ position, move status (e.g., IDLE/MOVING), and current UTC time.

- **Advanced Imaging Control**: Set and retrieve detailed camera parameters like Brightness, Exposure, Focus, and WDR.

- **Device & Media Management**: Retrieve essential information, including Device Information, Media Profiles, and Snapshot URIs.

---

## Code Example

### DiscoveryManager

```java
DiscoveryManager discoveryManager = new DiscoveryManager();
discoveryManager.setDiscoveryTimeout(30000); // Set timeout (30 seconds)

discoveryManager.discover(new DiscoveryListener() {
	@Override
	public void onDiscoveryStarted() {
		logger.info("Discovery started...");
	}

	@Override
	public void onDevicesFound(List<Device> devices) {
		for(Device dv : devices) {
			logger.info("Found Device: {}", dv.getHostName());
		}
	}
    // Error handling goes in onError()
});
```

---

### OnvifManager

```java
OnvifManager onvifManager = new OnvifManager();

onvifManager.getCapabilities(onvifDevice, new OnvifCapabilitiesListener() {
	
	@Override
	public void onDeviceCapabilitiesReceived(OnvifDevice onvifDevice, OnvifCapabilities onvifCapabilities) {
		logger.info("Device XADDR: {}", onvifCapabilities.getXaddr());
		logger.info("Device MaxProfiles: {}", onvifCapabilities.getMaximumNumberOfProfiles());
		
	}
});

onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {

	@Override
	public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles) {
		OnvifMediaProfile omp = mediaProfiles.get(0);	//example

		onvifManager.getSnapshotURI(device, omp, new OnvifSnapshotURIListener() {

			@Override
			public void onMediaSnapshotReceived(OnvifDevice device, OnvifMediaProfile profile, String uri) {
				logger.info("==== Snapshot Uri: {}", uri);
			}
		});
	}
});

// GetDeviceInformation, GetMediaStreamURI, GetServices, etc..
```

---

### PTZManager(PTZ operation)

```java
PtzManager ptzManager = new PtzManager();

// Pan/tilt
ptzManager.move(onvifDevice, PtzType.UP_RIGHT, new PtzResponseListener() {
	
	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("PTZ result: {}", ptzResponse.getMessage());
		logger.info("PTZ boolean: {}", ptzResponse.isSuccess());
		// etc . .
	}
});

// Zoom
ptzManager.move(onvifDevice, PtzType.ZOOM_IN, new PtzResponseListener() {
	
	@Override
	public void onResponse(PtzResponse ptzResponse) {
		// ptzResponse..
	}
});

// Stop
ptzManager.stop(onvifDevice, new PtzResponseListener() {
	
	@Override
	public void onResponse(PtzResponse ptzResponse) {
		// ptzResponse..
	}
});
```

---

### PTZManager(Preset operation)

```java
PresetCommand pcGet = new PresetCommand(PresetAction.GET, null);
ptzManager.preset(onvifDevice, pcGet, new PtzResponseListener() {

	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("Preset GET result: {}", ptzResponse.getMessage()); 
	}
});

PresetCommand pcSave = new PresetCommand(PresetAction.SAVE, "example");
ptzManager.preset(onvifDevice, pcSave, new PtzResponseListener() {

	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("Preset SAVE result xml: {}", ptzResponse.getRawXml());
		// etc . .
	}
});

PresetCommand pcMove = new PresetCommand(PresetAction.MOVE, "example");
ptzManager.preset(onvifDevice, pcMove, new PtzResponseListener() {

	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("Preset MOVE result xml: {}", ptzResponse.getRawXml());
		// etc . .
	}
});
```

---

### PTZManager(Status Inquiry)

```java
PtzManager ptzManager = new PtzManager();

ptzManager.getStatus(onvifDevice, new PtzResponseListener() {
	
	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("PTZ Status result: {}", ptzResponse.getMessage());
		// Message: Position: (Pan: 0.1, Tilt: 0.2, Zoom: 0.0), MoveStatus: (PanTilt: IDLE, Zoom: IDLE), Time: ...
	}
});
```

---

### ImagingManager

```java
ImagingManager imagingManager = new ImagingManager();

ImagingSettings settingsToUpdate = new ImagingSettings();
settingsToUpdate.setBrightness(128); // Set a specific value
settingsToUpdate.setExposureMode("AUTO"); // Set an enumeration value

// Set Imaging Settings
imagingManager.setImagingSettings(onvifDevice, settingsToUpdate, new ImagingSettingRequestListener() {
	@Override
	public void onResponse(ImagingResponse imagingResponse) {
		logger.info("Imaging settings updated result xml: {}", imagingResponse.getXml());
	}
});

// Get Imaging Settings
imagingManager.getImagingSettings(onvifDevice, new ImagingSettingsListener() {

	@Override
	public void onResponse(OnvifDevice onvifDevice, ImagingSettings imagingSettings) {
		logger.info("All ImagingSettings: {}", imagingSettings.toString());
	}
});

// Continuous Focus
imagingManager.focusContinuousMove(onvifDevice, 0.5, new ImagingFocusResponseListener() {
	
	@Override
	public void onResponse(ImagingResponse imagingResponse) {
		logger.info("Focus boolean: {}", imagingResponse.isSuccess());
	}
});
```

---
 
## Requirements

- **Java 8 or higher**  
- An **ONVIF-compliant network camera**  