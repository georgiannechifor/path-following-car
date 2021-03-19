package com.example.pathfollowingcar.server;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DrawingDTO {
    @SerializedName("id")
    private int id;

    @SerializedName("points")
    private ArrayList<String> points;

    public int getId() {
        return id;
    }

    public ArrayList<String> getPoints() {
        return points;
    }
}
