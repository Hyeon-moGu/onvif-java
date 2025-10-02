package io.github.hyeonmo.requests;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.AuthXMLBuilder;
import io.github.hyeonmo.OnvifManager;
import io.github.hyeonmo.listeners.OnvifMediaProfilesListener;
import io.github.hyeonmo.listeners.PtzResponseListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.PresetCommand;
import io.github.hyeonmo.models.PtzType;
import io.github.hyeonmo.parsers.PtzParser;
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
            	    ptzResponseListener.onResponse(
            	        ptzParser.parser("move", "No media profiles found")
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
            	    ptzResponseListener.onResponse(
            	        ptzParser.parser("stop", "No media profiles found")
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
            	    ptzResponseListener.onResponse(
            	        ptzParser.parser("stop", "No media profiles found")
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

    private void sendSoap(String urlStr, String soapXml, PtzResponseListener listener, String type) {
        RequestBody body = RequestBody.create(MediaType.parse("application/soap+xml; charset=utf-8"), soapXml);
        Request request = new Request.Builder().url(urlStr).post(body).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onResponse(ptzParser.parser(type, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    listener.onResponse(ptzParser.parser(type, "Unexpected response: " + response));
                } else {
                    listener.onResponse(ptzParser.parser(type, response.body() != null ? response.body().string() : ""));
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
