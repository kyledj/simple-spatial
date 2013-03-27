package org.simple.spatial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import org.junit.Test;
import org.simple.spatial.jackson.SimpleSpatialMapper;
import org.simple.spatial.model.Result;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestResultSerialization {
    private ObjectMapper mapper = SimpleSpatialMapper.getInstance();

    @Test
    public void testResultSerialize1() throws Exception {
        Point myPoint = SpatialContext.GEO.makePoint(122.01, -45.01);
        Result res = new Result("someid", "sometext", myPoint);

        String serialized = mapper.writeValueAsString(res);

        assertTrue(serialized.contains("\"lat\":122.01"));
        assertTrue(serialized.contains("\"lon\":-45.01"));
    }

    @Test
    public void testResultSerialize() throws Exception {
        Point firstPoint = SpatialContext.GEO.makePoint(122.01, -45.01);
        Point secondPoint = SpatialContext.GEO.makePoint(121.01, -44.01);

        List<Result> res = ImmutableList.of(
            new Result("firstid", "firstname", firstPoint),
            new Result("secondid", "secondname", secondPoint)
        );

        String serialized = mapper.writeValueAsString(res);

        assertTrue(serialized.contains("\"lat\":122.01"));
        assertTrue(serialized.contains("\"lon\":-45.01"));
        assertTrue(serialized.contains("\"lat\":121.01"));
        assertTrue(serialized.contains("\"lon\":-44.01"));
    }

}
