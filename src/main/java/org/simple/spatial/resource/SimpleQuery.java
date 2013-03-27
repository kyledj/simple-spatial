package org.simple.spatial.resource;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.simple.spatial.index.QueryIndex;
import org.simple.spatial.model.Result;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * A resource for searching a point index.
 */
@Path("/search/radius")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleQuery {
    private final QueryIndex indexer;

    public SimpleQuery(QueryIndex indexer) {
        this.indexer = indexer;
    }

    @GET
    @Timed
    public List<Result> radiusSearch(@QueryParam("lat") Optional<String> lat,
                               @QueryParam("long") Optional<String> lon,
                               @QueryParam("radius") Optional<String> radius) {
        boolean present = lat.isPresent();
        // Requires all three parameters
        if (!lat.isPresent() || !lon.isPresent() || !radius.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Double latDoub = tryParseDouble(lat.get());
        Double lonDoub = tryParseDouble(lon.get());
        Double radDoub = tryParseDouble(radius.get());

        List<Result> results;
        try {
            results = indexer.radiusQuery(latDoub, lonDoub, radDoub);
        } catch (IOException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return results;
    }

    private Double tryParseDouble(String val) {
        Double newDoub;
        try {
            newDoub = Double.parseDouble(val);
        } catch (IllegalArgumentException ex) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return newDoub;
    }
}
