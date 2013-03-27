package org.simple.spatial.index;

import org.simple.spatial.model.Result;

import java.io.IOException;
import java.util.List;

public interface QueryIndex {
    List<Result> radiusQuery(Double lat, Double lon, Double radiusKm) throws IOException;
}
