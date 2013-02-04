package org.simple.spatial.model;

import com.spatial4j.core.shape.Point;

public class Result {
    private final String id;
    private final String name;
    private final Point location;

    public Result(String id, String name, Point location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Double getLat() {
        return location.getX();
    }

    public Double getLon() {
        return location.getY();
    }
}
