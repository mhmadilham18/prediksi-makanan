package com.sahabatgula.model;

import java.util.List;

public class ApiResponse {
    private Prediction best;
    private List<Prediction> prediction;
    private String status;

    // Getters
    public Prediction getBest() {
        return best;
    }

    public List<Prediction> getPrediction() {
        return prediction;
    }

    public String getStatus() {
        return status;
    }
}