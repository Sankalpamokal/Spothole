package com.example.spothole;

public class ImageUpload {
    public  String category;
    public String url;


    public ImageUpload(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    public ImageUpload(){}
}

