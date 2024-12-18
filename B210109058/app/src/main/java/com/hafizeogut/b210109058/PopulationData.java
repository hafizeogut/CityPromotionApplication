package com.hafizeogut.b210109058;

public class PopulationData {
    String id;
    int year, population;
    public PopulationData(String id, int year, int population) {
        this.id = id;
        this.population = population;
        this.year = year;
    }



    public void setPopulation(int population) {
        this.population = population;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getPopulation() {
        return population;
    }

    public PopulationData() {
    }


}
