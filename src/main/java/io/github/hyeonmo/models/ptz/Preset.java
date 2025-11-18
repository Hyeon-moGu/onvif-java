package io.github.hyeonmo.models.ptz;

public class Preset {
    private String token;
    private String name;

    public Preset(String token, String name) {
        this.token = token;
        this.name = name;
    }

    public String getToken() { return token; }
    public String getName() { return name; }
}