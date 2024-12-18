package com.hafizeogut.b210109058;

import java.util.List;

public class City {
    String name,description;
    List<String> images;

    public City(String name, String description, List<String> images) {
        this.name = name;
        this.description = description;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public City() {
    }
}
