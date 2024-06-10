package com.example.repro.ui.modelo;

public class Cancion {
    private String title;
    private String uri;

    public Cancion(String title, String uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }
}