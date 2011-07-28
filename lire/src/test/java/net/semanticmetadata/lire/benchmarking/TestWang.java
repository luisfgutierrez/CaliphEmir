/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.imageanalysis.sift.SiftFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.*;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ...
 * Date: 18.09.2008
 * Time: 12:09:17
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestWang extends TestCase {
    private String indexPath = "wang-index";
    // if you don't have the images you can get them here: http://wang.ist.psu.edu/docs/related.shtml
    private String testExtensive = "./wang-1000";
    private ChainedDocumentBuilder builder;
    private int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};

    public void testGenQueries() {

    }

    protected void setUp() throws Exception {
        super.setUp();
        // set to all queries ... approach "leave one out"
        sampleQueries = new int[1000];
        for (int i = 0; i < sampleQueries.length; i++) {
            sampleQueries[i] = i;

        }
        // Setting up DocumentBuilder:
        builder = new ChainedDocumentBuilder();
//        builder.addBuilder(new CEDDDocumentBuilder());
//        builder.addBuilder(new SurfDocumentBuilder());
        builder.addBuilder(new MSERDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());

        // from Arthur:
//        builder.addBuilder(new GenericDocumentBuilder(FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST"));
//        builder.addBuilder(new GenericDocumentBuilder(JpegCoefficientHistogram.class, "FIELD_JPEGCOEFFHIST"));

//        builder.addBuilder(new SimpleDocumentBuilder(true, true, true));
//        builder.addBuilder(new SiftDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder());
    }

    public void testIndexWang() throws IOException {
        // indexing
        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        indexFiles(images, builder, indexPath);
//        in case of sift ...
//        SiftFeatureHistogramBuilder sh1 = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh1.index();
//        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh.index();
        MSERFeatureHistogramBuilder sh = new MSERFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
        sh.index();

        System.out.println("-< Indexing finished >--------------");
//        System.out.println("SiftFeatureHistogramBuilder sh1 = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 1000);");
        testMAP();
    }

    public void testProgram() throws IOException {
//        for (int i = 50; i<200; i+=20)
//            doParams(i, 100);

//        for (int i = 1000; i<20001; i+=1000)
//            doParams(500, i);
//
//        for (int i = 1000; i<20001; i+=1000)
//            doParams(10000, i);

        doParams(1000, 1000);

    }

    public void doParams(int numDocs, int numClusters) throws IOException {
        SiftFeatureHistogramBuilder sh1 = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), numDocs, numClusters);
        sh1.index();
        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), numDocs, numClusters);
        sh.index();
        System.out.println("*******************************************");
        System.out.println("SiftFeatureHistogramBuilder sh1 = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), " + numDocs + ", " + numClusters + ");");
        computeMAP(new SurfVisualWordsImageSearcher(1000), "Surf BoVW");
        computeMAP(new SiftVisualWordsImageSearcher(1000), "Sift BoVW");
        System.out.println("*******************************************");
    }

    public void testSiftLocalFeatureHistogram() {

    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
//        DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
//        DocumentBuilder builder = DocumentBuilderFactory.getFastDocumentBuilder();
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) System.out.println(count + " files indexed.");
//            if (count == 200) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();
    }

    public void testMAP() throws IOException {
//        SimpleColorHistogram.DEFAULT_DISTANCE_FUNCTION = SimpleColorHistogram.DistanceFunction.JSD;
//        computeMAP(ImageSearcherFactory.createColorHistogramImageSearcher(1000), "Color Histogram - JSD");
//        SimpleColorHistogram.DEFAULT_DISTANCE_FUNCTION = SimpleColorHistogram.DistanceFunction.L1;
//        computeMAP(ImageSearcherFactory.createColorHistogramImageSearcher(1000), "Color Histogram - L1");
//        SimpleColorHistogram.DEFAULT_DISTANCE_FUNCTION = SimpleColorHistogram.DistanceFunction.L2;
//        computeMAP(ImageSearcherFactory.createColorHistogramImageSearcher(1000), "Color Histogram - L2");
//        computeMAP(ImageSearcherFactory.createTamuraImageSearcher(1000), "Tamura");
//        computeMAP(ImageSearcherFactory.createGaborImageSearcher(1000), "Gabor");
//        computeMAP(ImageSearcherFactory.createDefaultCorrelogramImageSearcher(1000), "color correlogram");
//        computeMAP(ImageSearcherFactory.createWeightedSearcher(1000, 1f, 0f, 0f), "scalable color");
//        computeMAP(ImageSearcherFactory.createWeightedSearcher(1000, 0f, 1f, 0f), "color layout");
//        computeMAP(ImageSearcherFactory.createWeightedSearcher(1000, 0f, 0f, 1f), "edge Hist");
//        computeMAP(new GenericImageSearcher(1000, FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST"), "FuzzyColorHistogram");
//        computeMAP(new GenericImageSearcher(1000, JpegCoefficientHistogram.class, "FIELD_JPEGCOEFFHIST"), "JpegCoefficientHistogram");
//        computeMAP(new CEDDImageSearcher(1000), "CEDD");
        computeMAP(new VisualWordsImageSearcher(1000, DocumentBuilder.FIELD_NAME_MSER_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS), "MSER BoVW");    // used for MSER!!!
//        computeMAP(new SiftVisualWordsImageSearcher(1000), "Sift BoVW");
//        computeMAP(ImageSearcherFactory.createFCTHImageSearcher(1000), "FCTH");

    }

    public void computeMAP(ImageSearcher searcher, String prefix) throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));

//        searcher = new SimpleImageSearcher(maxHits, 0f, 0f, 1f);
        // searcher = ImageSearcherFactory.createColorHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createGaborImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createCEDDImageSearcher(maxHits);
//        searcher = new ParallelImageSearcher(maxHits, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//               searcher = ImageSearcherFactory.createFCTHImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createFastCorrelogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createDefaultCorrelogramImageSearcher(maxHits);
//        searcher = new GenericImageSearcher(maxHits, FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST");
        Pattern p = Pattern.compile("([0-9]+).jpg");
        double map = 0;
        double errorRate = 0d;
        double precision10 = 0d;
        double[] pr10cat = new double[10];
        double[] pr10cnt = new double[10];
        for (int i = 0; i < pr10cat.length; i++) {
            pr10cat[i] = 0d;
            pr10cnt[i] = 0d;
        }
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
//            System.out.println("id = " + id + ": " + "("+i+")");
            String file = testExtensive + "/" + id + ".jpg";
//            Document document = builder.createDocument(new FileInputStream(file), file);
            ImageSearchHits hits = searcher.search(findDoc(reader, id + ".jpg"), reader);
            int goodOnes = 0;
            double avgPrecision = 0d;
            double precision10temp = 0d;
            int countResults = 0;
            for (int j = 0; j < hits.length(); j++) {
                Document d = hits.doc(j);
                String hitsId = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                Matcher matcher = p.matcher(hitsId);
                if (matcher.find())
                    hitsId = matcher.group(1);
                else
                    fail("Did not get the number ...");
                int testID = Integer.parseInt(hitsId);
                if (testID != id) countResults++;
                if ((testID != id) && ((int) Math.floor(id / 100) == (int) Math.floor(testID / 100))) {
                    goodOnes++;
                    // Only if there is a change in recall
                    avgPrecision += (double) goodOnes / (double) countResults;
//                    System.out.print("x");
                    if (j <= 10) {
                        precision10temp += 1d;
                    }
                } else {
                    if (j == 1) { // error rate
                        errorRate++;
                    }
                }
//                System.out.print(" (" + testID + ") ");
            }  // end for loop iterating results.
//            if (avgPrecision<=0) {
//                System.out.println("avgPrecision = " + avgPrecision);
//                System.out.println("goodOnes = " + goodOnes);
//            }
            assertTrue("Check if average precision is > 0", avgPrecision > 0);
            assertTrue("Check if goodOnes is > 0", goodOnes > 0);
            avgPrecision = avgPrecision / goodOnes;
            precision10 += precision10temp / 10d;
            // precision @ 10 for each category ...
            pr10cat[(int) Math.floor(id / 100)] += precision10temp / 10d;
            pr10cnt[(int) Math.floor(id / 100)] += 1d;
            map += avgPrecision;
//            System.out.println(" " + avgPrecision + " (" + map / (i + 1) + ")");
        }
        map = map / sampleQueries.length;
        errorRate = errorRate / sampleQueries.length;
        precision10 = precision10 / sampleQueries.length;
        System.out.print(prefix + " - ");
        System.out.print("map = " + map);
        System.out.print(" precision@10 = " + precision10);
        System.out.println(" - errorRate = " + errorRate);
        // precision@10 per category
        for (int i = 0; i < pr10cat.length; i++) {
            double v = 0;
            if (pr10cnt[i] > 0)
                v = pr10cat[i] / pr10cnt[i];
            System.out.println(i + ": " + v);
        }
    }

    public void testParallelMAP() throws IOException {

        int maxHits = 1000;
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        ParallelImageSearcher searcher;
//        searcher = new SimpleImageSearcher(maxHits, 0f, 0f, 1f);
//        searcher = ImageSearcherFactory.createColorHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createGaborImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createCEDDImageSearcher(maxHits);
        searcher = new ParallelImageSearcher(maxHits, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//               searcher = ImageSearcherFactory.createFCTHImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createFastCorrelogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createDefaultCorrelogramImageSearcher(maxHits);
//        searcher = new GenericImageSearcher(maxHits, FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST");
        Pattern p = Pattern.compile("([0-9]+).jpg");
        double map = 0;
        double errorRate = 0d;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
//            System.out.println("id = " + id + ": " + "("+i+")");
            String file = testExtensive + "/" + id + ".jpg";
            String[] files = {id + ".jpg", (id + 1) + ".jpg", (id + 2) + ".jpg", (id + 3) + ".jpg", (id + 4) + ".jpg"};
            ImageSearchHits[] hits = searcher.search(findDocs(reader, files), reader);
            for (int k = 0; k < hits.length; k++) {
                int currentID = id + k;
                ImageSearchHits h = hits[k];
                int goodOnes = 0;
                double avgPrecision = 0;
                for (int j = 0; j < h.length(); j++) {
                    Document d = h.doc(j);
                    String hitsId = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                    Matcher matcher = p.matcher(hitsId);
                    if (matcher.find())
                        hitsId = matcher.group(1);
                    else
                        fail("Did not get the number ...");
                    int testID = Integer.parseInt(hitsId);
                    if ((testID != currentID) && ((int) Math.floor(id / 100) == (int) Math.floor(testID / 100))) {
                        goodOnes++;
                        // Only if there is a change in recall
                        avgPrecision += (double) goodOnes / (double) (j + 1);
//                    System.out.print("x");
                    } else {
                        if (j == 1) { // error rate
                            errorRate++;
                        }
                    }
//                System.out.print(" (" + testID + ") ");
                }
                assertTrue(goodOnes > 0);
                avgPrecision = avgPrecision / goodOnes;
                assertTrue(avgPrecision > 0);
                map += avgPrecision;
//                System.out.println(" " + avgPrecision + " (" + map / (i + 1) + ")");
            }
        }
        assertTrue(sampleQueries.length > 0);
        map = map / sampleQueries.length;
        errorRate = errorRate / sampleQueries.length;
        System.out.println("map = " + map);
        System.out.println("errorRate = " + errorRate);
    }

    public void tttestMAPLocalFeatureHistogram() throws IOException {
        int maxSearches = 200;
        int maxHits = 100;
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        IndexSearcher is = new IndexSearcher(reader);
        ImageSearcher searcher;
        searcher = new SiftLocalFeatureHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createColorHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createCEDDImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createFCTHImageSearcher(maxHits);
        Pattern p = Pattern.compile("\\\\\\d+\\.jpg");
        double map = 0;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
            System.out.print("id = " + id + ": ");
            String file = testExtensive + "/" + id + ".jpg";

            ImageSearchHits hits = searcher.search(findDoc(reader, id + ".jpg"), reader);
            int goodOnes = 0;
            double avgPrecision = 0;
            for (int j = 0; j < hits.length(); j++) {
                Document d = hits.doc(j);
                String hitsId = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                Matcher matcher = p.matcher(hitsId);
                if (matcher.find())
                    hitsId = hitsId.substring(matcher.start() + 1, hitsId.lastIndexOf("."));
                else
                    fail("Did not get the number ...");
                int testID = Integer.parseInt(hitsId);
//                System.out.print(". " + hitsId + "/"  + d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]+ " ");
                if ((int) Math.floor(id / 100) == (int) Math.floor(testID / 100)) {
                    goodOnes++;
                    System.out.print("x");
                } else {
                    System.out.print("o");
                }
//                System.out.print(" (" + testID + ") ");
                avgPrecision += (double) goodOnes / (double) (j + 1);
            }
            avgPrecision = avgPrecision / hits.length();
            map += avgPrecision;
            System.out.println(" " + avgPrecision + " (" + map / (i + 1) + ")");
        }
        map = map / sampleQueries.length;
        System.out.println("map = " + map);
    }

    private Document findDoc(IndexReader reader, String file) throws IOException {
        for (int i = 0; i < reader.numDocs(); i++) {
            Document document = reader.document(i);
            String s = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            if (s.endsWith(File.separator + file)) {
//                System.out.println("s = " + s);
                return document;
            }
        }
        return null;
    }

    private Document[] findDocs(IndexReader reader, String[] file) throws IOException {
        Document[] result = new Document[file.length];
        for (int i = 0; i < reader.numDocs(); i++) {
            Document document = reader.document(i);
            String s = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            for (int j = 0; j < result.length; j++) {
                if (s.endsWith("\\" + file[j])) {
//                System.out.println("s = " + s);
                    result[j] = document;
                }
            }
        }
        return result;
    }

    public void tttestGetDistribution() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("data.csv"));
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        // get the first document:
        if (!IndexReader.indexExists(reader.directory()))
            throw new FileNotFoundException("No index found at this specific location.");

        CEDD cedd1 = new CEDD();
        FCTH fcth1 = new FCTH();

        CEDD cedd2 = new CEDD();
        FCTH fcth2 = new FCTH();

        JCD jcd1 = new JCD();
        JCD jcd2 = new JCD();
        String[] cls;


        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            if (reader.isDeleted(i)) {
                continue;
            }
            Document doc = reader.document(i);
            cls = doc.getValues(DocumentBuilder.FIELD_NAME_CEDD);
            if (cls != null && cls.length > 0)
                cedd1.setStringRepresentation(cls[0]);
            cls = doc.getValues(DocumentBuilder.FIELD_NAME_FCTH);
            if (cls != null && cls.length > 0)
                fcth1.setStringRepresentation(cls[0]);

            for (int j = i + 1; j < docs; j++) {
                if (reader.isDeleted(j)) {
                    continue;
                }
                Document doc2 = reader.document(j);
                cls = doc2.getValues(DocumentBuilder.FIELD_NAME_CEDD);
                if (cls != null && cls.length > 0)
                    cedd2.setStringRepresentation(cls[0]);
                cls = doc2.getValues(DocumentBuilder.FIELD_NAME_FCTH);
                if (cls != null && cls.length > 0)
                    fcth2.setStringRepresentation(cls[0]);
                jcd1.init(cedd1, fcth1);
                jcd2.init(cedd2, fcth2);
                bw.write(cedd1.getDistance(cedd2) + ";" + fcth1.getDistance(fcth2) + ";" + jcd1.getDistance(jcd2) + "\n");
            }
            if (i % 100 == 0) System.out.println(i + " entries processed ... ");
        }
        bw.close();
    }

    public void tttestGetSampleQueries() {
        for (int i = 0; i < 200; i++) {
            System.out.print((int) (Math.random() * 1000) + ", ");
        }
    }

}
