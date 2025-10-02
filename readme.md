# ONVIF-Java Library

This library is a powerful and easy-to-use Java tool for interacting with ONVIF-compliant devices, such as IP cameras. 
It's designed to abstract away the complexity of the SOAP protocol, allowing developers to control ONVIF devices through intuitive Java method calls, without needing to handle the intricacies of XML requests and parsing.

**Beyond Simple Control**: While the library is designed for simple control using OnvifDevice objects, you also have the flexibility to make direct requests using a device's XAddr (web service address) and Token information when necessary.

**Asynchronous Processing**:
A core design principle of this library is non-blocking asynchronous operation.

---

## Installation

### Maven:

```Java
<dependency>
    <groupId>io.github.hyeon-mogu</groupId>
    <artifactId>onvif-java</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Gradle:

```Java
implementation group: 'io.github.hyeon-mogu', name: 'onvif-java', version: '1.0.1'
```

---

## Key Features

- **ONVIF Device Discovery and Connection** : Automatically discovers and connects to ONVIF devices on the network

- **PTZ (Pan/Tilt/Zoom) Control** : Real-time control of the camera's direction and zoom

- **Preset Management** : Save specific camera positions as presets and quickly move to a saved position

- **Imaging Control** : Adjust camera imaging settings like focus and retrieve imaging capabilities and settings.

- **Device Information Retrieval** : Retrieve information from connected devices

---

## Code Example

### DiscoveryManager

```java
DiscoveryManager discoveryManager = new DiscoveryManager();
discoveryManager.setDiscoveryTimeout(30000);	// 30sec

discoveryManager.discover(new DiscoveryListener() {

	@Override
	public void onDiscoveryStarted() {
		logger.info("Discovery started");

	}

	@Override
	public void onDevicesFound(List<Device> devices) {
		for(Device dv : devices) {
			logger.info("Found: " + dv.getHostName());
		}
	}
});
```

---

### OnvifManager

```java
OnvifManager onvifManager = new OnvifManager();

onvifManager.getCapabilities(onvifDevice, new OnvifCapabilitiesListener() {
	
	@Override
	public void onDeviceCapabilitiesReceived(OnvifDevice onvifDevice, OnvifCapabilities onvifCapabilities) {
		logger.info("Device XADDR: " + onvifCapabilities.getXaddr());
		logger.info("Device MaxProfiles: " + onvifCapabilities.getMaximumNumberOfProfiles());
		
	}
});

onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {

	@Override
	public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles) {
		OnvifMediaProfile omp = mediaProfiles.get(0);	//example

		onvifManager.getSnapshotURI(device, omp, new OnvifSnapshotURIListener() {

			@Override
			public void onMediaSnapshotReceived(OnvifDevice device, OnvifMediaProfile profile, String uri) {
				logger.info("==== Snapshot Uri: " + uri);
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

// Pan-tilt
ptzManager.move(onvifDevice, PtzType.UP_RIGHT, new PtzResponseListener() {
	
	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("PTZ result: " + ptzResponse.getMessage());
		logger.info("PTZ boolean: " + ptzResponse.isSuccess());
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
PresetCommand pcSave = new PresetCommand(PresetAction.SAVE, "example");
ptzManager.preset(onvifDevice, pcSave, new PtzResponseListener() {

	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("Preset result xml: " + ptzResponse.getRawXml());
		// etc . .
	}
});

PresetCommand pcMove = new PresetCommand(PresetAction.MOVE, "example");
ptzManager.preset(onvifDevice, pcMove, new PtzResponseListener() {

	@Override
	public void onResponse(PtzResponse ptzResponse) {
		logger.info("Preset result xml: " + ptzResponse.getRawXml());
		// etc . .
	}
});
```

---

### ImagingManager

```java
ImagingManager imagingManager = new ImagingManager();

// ImagingSetting Information
imagingManager.getImagingSettings(onvifDevice, new ImagingSettingsListener() {

	@Override
	public void onResponse(OnvifDevice onvifDevice, ImagingSettings imagingSettings) {
		logger.info("All ImagingSettings: " + imagingSettings.toString());
		//etc . .
	}
});

// Continuous Focus
imagingManager.focusContinuousMove(onvifDevice, 0.5, new ImagingFocusResponseListener() {
	
	@Override
	public void onResponse(ImagingResponse imagingResponse) {
		logger.info("Focus boolean: " + imagingResponse.isSuccess());
		//etc . .
	}
});
```

---
 
## Requirements

- **Java 8 or higher**  
- An **ONVIF-compliant network camera**  
