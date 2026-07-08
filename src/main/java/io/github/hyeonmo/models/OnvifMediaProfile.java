package io.github.hyeonmo.models;

public class OnvifMediaProfile {

    //Constants
    public static final String TAG = OnvifMediaProfile.class.getSimpleName();

    //Attributes
    private final String name;
    private final String token;
    private final String videoSourceToken;
    private final String ptzConfigurationToken;

    //Constructors

    public OnvifMediaProfile(String name, String token, String videoSourceToken) {
        this(name, token, videoSourceToken, "");
    }

    public OnvifMediaProfile(String name, String token, String videoSourceToken, String ptzConfigurationToken) {
        this.name = name;
        this.token = token;
        this.videoSourceToken = videoSourceToken;
        this.ptzConfigurationToken = ptzConfigurationToken;
    }

    //Properties

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getVideoSourceToken() {
    	return videoSourceToken;
    }

    public String getPtzConfigurationToken() {
        return ptzConfigurationToken;
    }

    @Override
    public String toString() {
        return "OnvifMediaProfile{" +
                "name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", sourceToken='" + videoSourceToken + '\'' +
                ", ptzConfigurationToken='" + ptzConfigurationToken + '\'' +
                '}';
    }
}
