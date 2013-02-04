package org.simple.spatial;

import com.spatial4j.core.shape.Point;

public class Result {
    private final String id;
    private final String text;
    private final Point location;

    public Result(String id, String text, Point location) {
        this.id = id;
        this.text = text;
        this.location = location;
    }

    public String getText() {
        return text;
    }

    public Double getLat() {
        return location.getX();
    }

    public Double getLon() {
        return location.getY();
    }
}
