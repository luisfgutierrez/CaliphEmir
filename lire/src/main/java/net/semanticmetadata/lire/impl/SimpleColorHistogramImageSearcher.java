package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:17:02
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleColorHistogramImageSearcher extends AbstractImageSearcher {
    private Logger logger = Logger.getLogger(getClass().getName());

    private int maxHits = 10;
    private TreeSet<SimpleResult> docs;

    public SimpleColorHistogramImageSearcher(int maxHits) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        logger.finer("Starting extraction of ColorHistogram from image");
        SimpleColorHistogram colorHistogram = new SimpleColorHistogram();
        // Scaling image is especially with the correlogram features very important!
        BufferedImage bimg = image;
        if (Math.max(image.getHeight(), image.getWidth()) > SimpleColorHistogramDocumentBuilder.MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, SimpleColorHistogramDocumentBuilder.MAX_IMAGE_DIMENSION);
        }
        colorHistogram.extract(bimg);
        logger.fine("Extraction from image finished");

        float maxDistance = findSimilar(reader, colorHistogram);
        return new SimpleImageSearchHits(this.docs, maxDistance);
    }

    /**
     * @param reader
     * @param colorHistogram
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    private float findSimilar(IndexReader reader, SimpleColorHistogram colorHistogram) throws IOException {
        float maxDistance = -1f, overallMaxDistance = -1f;
        boolean hasDeletions = reader.hasDeletions();

        // clear result set ...
        docs.clear();

        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            // bugfix by Roman Kern
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }

            Document d = reader.document(i);
            float distance = getDistance(d, colorHistogram);
            assert(distance >=0);
            // calculate the overall max distance to normalize score afterwards
            if (overallMaxDistance < distance) {
                overallMaxDistance = distance;
            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = distance;
            }
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult(distance, d));
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                this.docs.remove(this.docs.last());
                // add the new one ...
                this.docs.add(new SimpleResult(distance, d));
                // and set our new distance border ...
                maxDistance = this.docs.last().getDistance();
            }
        }
        return maxDistance;
    }

    private float getDistance(Document d, SimpleColorHistogram colorHistogram) {
        // TODO: We can do this for each field ... if there is a region based approach.
        float distance = 0f;
        SimpleColorHistogram sch = new SimpleColorHistogram();
        String[] schString = d.getValues(DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        if (schString != null && schString.length > 0) {
            sch.setStringRepresentation(schString[0]);
            distance = colorHistogram.getDistance(sch);
        } else {
            logger.warning("No feature stored in this document!");
        }
        return distance;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleColorHistogram sch = new SimpleColorHistogram();

        String[] schString = doc.getValues(DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        if (schString != null && schString.length > 0)
            sch.setStringRepresentation(schString[0]);
        float maxDistance = findSimilar(reader, sch);

        return new SimpleImageSearchHits(this.docs, maxDistance);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        // get the first document:
        if (!IndexReader.indexExists(reader.directory()))
            throw new FileNotFoundException("No index found at this specific location.");
        Document doc = reader.document(0);

        SimpleColorHistogram sch = new SimpleColorHistogram();
        String[] schString = doc.getValues(DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        if (schString != null && schString.length > 0)
            sch.setStringRepresentation(schString[0]);

        HashMap<Float, List<String>> duplicates = new HashMap<Float, List<String>>();

        // find duplicates ...
        boolean hasDeletions = reader.hasDeletions();

        int docs = reader.numDocs();
        int numDuplicates = 0;
        for (int i = 0; i < docs; i++) {
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }
            Document d = reader.document(i);
            float distance = getDistance(d, sch);

            if (!duplicates.containsKey(distance)) {
                duplicates.put(distance, new LinkedList<String>());
            } else {
                numDuplicates++;
            }
            duplicates.get(distance).add(d.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

        if (numDuplicates == 0) return null;

        LinkedList<List<String>> results = new LinkedList<List<String>>();
        for (float f : duplicates.keySet()) {
            if (duplicates.get(f).size() > 1) {
                results.add(duplicates.get(f));
            }
        }
        return new SimpleImageDuplicates(results);
    }

}