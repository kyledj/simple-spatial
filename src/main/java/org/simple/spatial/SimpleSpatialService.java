package org.simple.spatial;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.simple.spatial.index.SpatialIndex;
import org.simple.spatial.resource.SimpleQuery;

public class SimpleSpatialService extends Service<SimpleSpatialServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new SimpleSpatialService().run(args);
    }

    @Override
    public void initialize(Bootstrap<SimpleSpatialServiceConfiguration> bootstrap) {
        bootstrap.setName("simple-spatial");
    }

    @Override
    public void run(SimpleSpatialServiceConfiguration configuration, Environment environment) throws Exception {
        SpatialIndex indexer = SpatialIndex.newBuilder().build();
        indexer.addDocument("Morrisson Bridge", -122.670078, 45.517534);
        indexer.addDocument("Mount Tabor", -122.593861, 45.511760);
        indexer.addDocument("Mount Hood", -121.693153, 45.373252);
        environment.addResource(new SimpleQuery(indexer));
    }
}
