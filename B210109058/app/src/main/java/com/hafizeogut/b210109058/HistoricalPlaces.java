package com.hafizeogut.b210109058;

public class HistoricalPlaces {
    String  id,imageURL ,historicalPlace;

    public HistoricalPlaces(String id, String imageURL, String historicalPlace) {
        this.id = id;
        this.imageURL = imageURL;
        this.historicalPlace = historicalPlace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getHistoricalPlace() {
        return historicalPlace;
    }

    public void setHistoricalPlace(String historicalPlace) {
        this.historicalPlace = historicalPlace;
    }


    public HistoricalPlaces() {
    }
}
