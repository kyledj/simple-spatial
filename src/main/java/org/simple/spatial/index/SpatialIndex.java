package org.simple.spatial.index;

import com.google.common.collect.Lists;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.simple.spatial.model.Result;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Simple encapusulation of a RAMDirectory storage of documents with
 * a single text field, lat and long coordinates
 */
public class SpatialIndex implements QueryIndex {

    private static final Version INDEX_VERSION = Version.LUCENE_41;
    private static final SpatialContext CTX = SpatialContext.GEO;
    private SpatialStrategy strategy;
    private Directory directory;

    private IndexWriter writer;
    private IndexReader reader;

    private boolean closed;

    private static final IndexWriterConfig WRITER_CONFIG = new IndexWriterConfig(
            INDEX_VERSION,
            new StandardAnalyzer(INDEX_VERSION));

    public static Builder newBuilder() {
        return new Builder();
    }

    public SpatialIndex(SpatialStrategy strategy, Directory directory) throws IOException {
        this.strategy = strategy;
        this.directory = directory;

        writer = new IndexWriter(this.directory, WRITER_CONFIG);
        refreshReader();
        closed = false;
    }

    /***
     * Add a document to the index
     * @param name Name for this location
     * @param lat
     * @param lon
     * @throws IOException
     */
    public void addDocument(String name, Double lat, Double lon) throws IOException {
        if (closed) {
            throw new IllegalStateException("Index has been closed");
        }

        Point newPoint = CTX.makePoint(lat, lon);
        Document doc = new Document();
        // Use a random UUID for an id
        String newId = UUID.randomUUID().toString();
        doc.add(new StringField("id", newId, Field.Store.YES));
        doc.add(new StringField("name", name, Field.Store.YES));
        doc.add(new StringField("lat", lat.toString(), Field.Store.YES));
        doc.add(new StringField("lon", lon.toString(), Field.Store.YES));

        for (IndexableField field: strategy.createIndexableFields(newPoint)) {
            doc.add(field);
        }

        writer.addDocument(doc);
        writer.commit();

        // Lol. So slow. But simple.
        refreshReader();
    }

    @Override
    public List<Result> radiusQuery(Double lat, Double lon, Double radiusKm) throws IOException {
        if (closed) {
            throw new IllegalStateException("Index has been closed");
        }

        Double degDist = DistanceUtils.dist2Degrees(radiusKm, DistanceUtils.EARTH_MEAN_RADIUS_KM);

        Circle area = CTX.makeCircle(lat, lon, degDist);
        SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, area);

        // Create a filter based on the circle
        Filter areaFilter = strategy.makeFilter(args);
        // Create a sortable value source based on distance from the center
        ValueSource source = strategy.makeDistanceValueSource(area.getCenter());

        List<Result> results;
        IndexSearcher searcher = new IndexSearcher(reader);

        Sort distSort = new Sort(source.getSortField(false)).rewrite(searcher);
        TopDocs docs = searcher.search(new MatchAllDocsQuery(), areaFilter, 20, distSort);
        results = docsToResultList(docs, searcher);

        return results;
    }

    public int count() {
        return writer.numDocs();
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        directory.close();
    }

    private synchronized void refreshReader() throws IOException {
        reader = DirectoryReader.open(writer, true);
    }

    private static List<Result> docsToResultList(TopDocs docs, IndexSearcher searcher) throws IOException {
        List<Result> results = Lists.newArrayList();
        for(ScoreDoc d: docs.scoreDocs) {
            results.add(docToResult(searcher.doc(d.doc)));
        }

        return results;
    }

    private static Result docToResult(Document doc) {
        return new Result(
               doc.get("id"),
               doc.get("name"),
               SpatialContext.GEO.makePoint(
                       Double.parseDouble(doc.get("lat")),
                       Double.parseDouble(doc.get("lon"))
               )
        );
    }

    public static final class Builder {
        private SpatialStrategy strategy;
        private Directory directory;

        private Builder() { }

        public Builder setSpatialStrategy(SpatialStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder setDirectory(Directory directory) {
            this.directory = directory;
            return this;
        }

        public SpatialIndex build() throws IOException {

            // Local references, so we don't go building with the
            // same objects each time
            SpatialStrategy thisStrat = strategy;
            Directory thisDir = directory;

            // Use a GeohashPrefixTree strategy by default
            if (null == strategy) {
                // 11 max levels gives sub-meter granularity
                SpatialPrefixTree grid = new GeohashPrefixTree(CTX, 11);
                thisStrat = new RecursivePrefixTreeStrategy(grid, "myGeoField");
            }

            if (null == directory) {
                thisDir = new RAMDirectory();
            }

            return new SpatialIndex(thisStrat, thisDir);
        }
    }
}
