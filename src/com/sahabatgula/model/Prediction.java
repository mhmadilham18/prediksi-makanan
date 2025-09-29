package com.sahabatgula.model;

public class Prediction {
    private int id;
    private String name;
    private String slug;
    private double confidence;

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "name='" + name + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}