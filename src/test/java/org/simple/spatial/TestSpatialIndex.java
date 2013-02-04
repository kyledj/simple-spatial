package org.simple.spatial;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simple.spatial.index.SpatialIndex;
import org.simple.spatial.model.Result;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSpatialIndex {

    private SpatialIndex ndx;

    @Before
    public void setUp() throws IOException {
        ndx = SpatialIndex.newBuilder().setDirectory(new RAMDirectory()).build();
        addTestDocs(ndx);
    }

    @After
    public void tearDown() throws IOException {
        ndx.close();
        ndx = null;
    }

    @Test
    public void TestCount() throws IOException {
        assertEquals(4, ndx.count());
    }

    @Test
    public void TestFindAll() throws IOException {
        List<Result> results = ndx.radiusQuery(-122.681934, 45.525450, 5000.0);
        assertEquals(4, results.size());
    }

    @Test
    public void TestFindOne() throws IOException {
        List<Result> results = ndx.radiusQuery(-121.693153, 45.373252, 1.0);
        assertEquals(1, results.size());
        assertEquals("Mount Hood", results.get(0).getName());
    }

    @Test
    public void TestFindThree() throws IOException {
        List<Result> results = ndx.radiusQuery(-122.670078, 45.517534, 10.0);
        assertEquals(3, results.size());
    }

    private static void addTestDocs(SpatialIndex ndx) throws IOException {
        ndx.addDocument("UA", -122.681934, 45.525450);
        ndx.addDocument("Morrisson Bridge", -122.670078, 45.517534);
        ndx.addDocument("Mount Tabor", -122.593861, 45.511760);
        ndx.addDocument("Mount Hood", -121.693153, 45.373252);
    }

}
