package org.simple.spatial.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.spatial4j.core.shape.Point;

public class SimpleSpatialMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        SimpleModule module = new SimpleModule("SimpleSpatialModule");
        module.addSerializer(Point.class, new PointSerializer());
    }
    public static ObjectMapper getInstance() {
        return MAPPER;
    }
}
