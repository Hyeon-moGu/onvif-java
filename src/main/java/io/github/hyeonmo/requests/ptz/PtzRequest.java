package io.github.hyeonmo.requests.ptz;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.AuthXMLBuilder;
import io.github.hyeonmo.OnvifManager;
import io.github.hyeonmo.listeners.media.OnvifMediaProfilesListener;
import io.github.hyeonmo.listeners.ptz.PtzResponseListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.parsers.ptz.PtzParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public class PtzRequest {

	private static int PTZ_REQUEST_TIMEOUT = 5000;

	private int ptzRequestTimeout = PTZ_REQUEST_TIMEOUT;

	private PtzParser ptzParser;

	private final OnvifManager onvifManager;

	private OkHttpClient httpClient;

	private static final MediaType SOAP_MEDIA_TYPE = MediaType.parse("application/soap+xml; charset=utf-8");
    private static final int ERROR_CODE_PROFILE = -100;

	public PtzRequest(OnvifManager onvifManager) {
		this.onvifManager = onvifManager;
	    this.ptzParser = new PtzParser();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .build();
	}

	public void move(OnvifDevice onvifDevice, PtzType ptzType, PtzResponseListener ptzResponseListener) {
    	String xaddr = onvifDevice.getAddresses().get(0);

        onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {

            @Override
            public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles) {
            	if (mediaProfiles == null || mediaProfiles.isEmpty()) {
            	    ptzResponseListener.onError(
            	        ERROR_CODE_PROFILE,
            	        "No media profiles found to get Profile Token."
            	    );
            	    return;
            	}
            	String profileToken = mediaProfiles.get(0).getToken();

            	AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
        		String soapXml = builder.getAuthHeader() + builder.getPtzMoveBody(profileToken,ptzType) + builder.getAuthEnd();

        		sendSoap(xaddr, soapXml, ptzResponseListener, "move");
            }
        });
    }

	public void stop(OnvifDevice onvifDevice, PtzResponseListener ptzResponseListener) {
    	String xaddr = onvifDevice.getAddresses().get(0);

        onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {

            @Override
            public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles) {
            	if (mediaProfiles == null || mediaProfiles.isEmpty()) {
            	    ptzResponseListener.onError(
            	        ERROR_CODE_PROFILE,
            	        "No media profiles found to get Profile Token"
            	    );
            	    return;
            	}
                String profileToken = mediaProfiles.get(0).getToken();

            	AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
        		String soapXml = builder.getAuthHeader() + builder.getPtzStopBody(profileToken) + builder.getAuthEnd();

        		sendSoap(xaddr, soapXml, ptzResponseListener, "stop");
            }
        });
    }

    public void move(String xaddr, String profileToken, String userName, String password, PtzType ptzType, PtzResponseListener ptzResponseListener) {
    	AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);

		String soapXml = builder.getAuthHeader() + builder.getPtzMoveBody(profileToken, ptzType) + builder.getAuthEnd();

		sendSoap(xaddr, soapXml, ptzResponseListener, "move");
    }

    public void stop(String xaddr, String profileToken, String userName, String password, PtzResponseListener ptzResponseListener) {
    	AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);

		String soapXml = builder.getAuthHeader() + builder.getPtzStopBody(profileToken) + builder.getAuthEnd();
		sendSoap(xaddr, soapXml, ptzResponseListener, "stop");
    }

    public void preset(OnvifDevice onvifDevice, PresetCommand presetCommand, PtzResponseListener ptzResponseListener) {
    	String xaddr = onvifDevice.getAddresses().get(0);

        onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {

            @Override
            public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles) {
            	if (mediaProfiles == null || mediaProfiles.isEmpty()) {
            	    ptzResponseListener.onError(
            	        ERROR_CODE_PROFILE,
            	        "No media profiles found to get Profile Token"
            	    );
            	    return;
            	}
                String profileToken = mediaProfiles.get(0).getToken();

            	AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());

        		String soapXml = builder.getAuthHeader() + builder.getPresetBody(profileToken, presetCommand) + builder.getAuthEnd();
        		sendSoap(xaddr, soapXml, ptzResponseListener, "preset");
            }
        });
    }

    public void preset(String xaddr, String profileToken, String userName, String password, PresetCommand presetCommand, PtzResponseListener ptzResponseListener) {
    	AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);

		String soapXml = builder.getAuthHeader() + builder.getPresetBody(profileToken, presetCommand) + builder.getAuthEnd();
		sendSoap(xaddr, soapXml, ptzResponseListener, "preset");
    }

    public void getStatus(OnvifDevice onvifDevice, PtzResponseListener ptzResponseListener) {
    	String xaddr = onvifDevice.getAddresses().get(0);

        onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {

            @Override
            public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles) {
            	if (mediaProfiles == null || mediaProfiles.isEmpty()) {
            	    ptzResponseListener.onError(
            	        ERROR_CODE_PROFILE,
            	        "No media profiles found to get Profile Token"
            	    );
            	    return;
            	}
                String profileToken = mediaProfiles.get(0).getToken();

            	AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());

        		String soapXml = builder.getAuthHeader() + builder.getPtzStatusBody(profileToken) + builder.getAuthEnd();
        		sendSoap(xaddr, soapXml, ptzResponseListener, "status");
            }
        });
    }

    public void getStatus(String xaddr, String profileToken, String userName, String password, PtzResponseListener ptzResponseListener) {
    	AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);

		String soapXml = builder.getAuthHeader() + builder.getPtzStatusBody(profileToken) + builder.getAuthEnd();
		sendSoap(xaddr, soapXml, ptzResponseListener, "status");
    }

    private void sendSoap(String urlStr, String soapXml, PtzResponseListener listener, String type) {
        RequestBody body = RequestBody.create(SOAP_MEDIA_TYPE, soapXml);

        Request request = new Request.Builder()
            .url(urlStr)
            .addHeader("Content-Type", SOAP_MEDIA_TYPE.toString())
            .post(body)
            .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError(-1, "Network failure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    listener.onError(response.code(), "HTTP Error " + response.code() + ", Body: " + responseBody);
                } else {
                    listener.onResponse(ptzParser.parser(type, responseBody));
                }
            }
        });
    }

    // Methods
    public int getPtzRequestTimeout() {
        return ptzRequestTimeout;
    }

    public void setPtzRequestTimeout(int to) {
        ptzRequestTimeout = to;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
            } catch (Exception ignored) {
            }
            httpClient = null;
        }
    }
}
