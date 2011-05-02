package net.semanticmetadata.lire.imageanalysis.mser;

import ij.ImagePlus;
import net.semanticmetadata.lire.imageanalysis.mser.fourier.Fourier;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Calculates Maximally stable extremal regions
 * Algorithm based on Linear Time Maximally Stable Extremal Regions
 * by David Nist�r and Henrik Stew�nius (2008)
 *
 * User: Shotty
 * Date: 28.06.2010
 * Time: 09:48:00
 */
public class MSER
{
    public static int MAX_GREY = 256;

    private MSERParameter params;

    private ImageMask imageMask;
    private MSERHeap heap;
    private Stack<MSERComponent> componentStack;

    // all Extremal Regions
    private ArrayList<MSERGrowthHistory> ers = new ArrayList<MSERGrowthHistory>();
    // index of the parent in the arrayList of the extremal regions
//    private int[] ersp = null;
    private MSERGrowthHistory[] mers = null;

    // TODO: find a suitable value
    private static final double COARSE_ANGLE = Math.PI/4;


    public MSER()
    {
        params = new MSERParameter();
    }

    public MSER(int delta,
                double minArea, double maxArea,
                double maxVariation, double minDiversity,
                int maxEvolution, double areaThreshold,                    
                double minMargin, int edgeBlurSize )
    {
        params = new MSERParameter(delta, minArea, maxArea, maxVariation, minDiversity, 
                maxEvolution, areaThreshold, minMargin, edgeBlurSize);
    }

    /**
     * Helper method to prepare the member fields before calculating the MSERs.
     */
    private void prepare()
    {
        for (int i = 0; i < ers.size(); i++)
        {
            ers.get(i).setIndex(i);
        }

    }

    public MSERGrowthHistory[] extractMSER(ImagePlus image)
    {
        ers.clear();
//        ersp = null;
        mers = null;

        int imageLength = image.getWidth()*image.getHeight();

        // create the accessible pixel mask
        imageMask = new ImageMask(image);

        // create the heap of boundary pixels
        heap = new MSERHeap(MAX_GREY);
        // create component stack
        componentStack = new Stack<MSERComponent>();
        // push empty component on stack
        // with level (grey value) which is not possible
        componentStack.push(new MSERComponent(MAX_GREY));

        // starting point is 0/0
        BoundaryPixel currentPixel = imageMask.getBoundaryPixel(0);
        imageMask.getAccess(currentPixel.getIndex());
        int currentLevel = imageMask.getValue(currentPixel.getIndex());

        // push component with currentLevel on stack
        componentStack.push(new MSERComponent(currentLevel));

        while (currentPixel != null)
        {
            // explore the remaining edges
            BoundaryPixel nextEdge = currentPixel.getNextBoundary();
            while (nextEdge != null)
            {
                if (!imageMask.hasAccess(nextEdge.getIndex()))
                {
                    // if pixel has no access yet, access it now
                    imageMask.getAccess(nextEdge.getIndex());
                    
                    // if grayValue of edge is lower than current gray level
                    if (imageMask.getValue(nextEdge.getIndex()) < currentLevel)
                    {
                        // enter the current pixel on boundary heap
                        heap.push(currentPixel, imageMask.getValue(currentPixel.getIndex()));
                        // edge is new pixel to consider
                        currentPixel = nextEdge;
                        currentLevel = imageMask.getValue(currentPixel.getIndex());
                        // push new Component onto Stack
                        componentStack.push(new MSERComponent(currentLevel));
                    }
                    else
                    {
                        // push the pixel on the heap
                        heap.push(nextEdge, imageMask.getValue(nextEdge.getIndex()));
                    }
                }

                // get the next edge
                nextEdge = currentPixel.getNextBoundary();
            }

            assert(imageMask.getValue(currentPixel.getIndex()) == currentLevel);
            assert(componentStack.peek().getGreyLevel() == currentLevel);

//            System.out.println("ComponentStack.size:" + componentStack.size());
            // accumulate the current pixel to the component on top of the stack
            componentStack.peek().addPixel(currentPixel);

            // pop the boundary heap
            currentPixel = heap.pop();

            if (currentPixel != null &&
                    imageMask.getValue(currentPixel.getIndex()) > currentLevel)
            {
                // process all components on the component stack until we reach the higher gray level
                processStack(imageMask.getValue(currentPixel.getIndex()));
                // afterwards, this grey level is the current one
                currentLevel = imageMask.getValue(currentPixel.getIndex());
            }
            else if (currentPixel == null)
            {
                // no pixels left, so just process the rest of the stack
                processStack(MAX_GREY);
            }
        }

//        System.out.println("ERS found: " + ers.size());

        // now. calculate the maximally stable ones
        prepare();
        float[] areavar = new float[ers.size()];
        boolean[] maxstable = new boolean[ers.size()];
        int[] top = new int[ers.size()];
        int[] bottom = new int[ers.size()];

        for (int i = 0; i < ers.size(); i++)
        {
            top[i] = imageLength;
        }

        /*
         * Calculate the R+delta and R-delta as described in [2].
         *
         */
        for (int i = 0; i < ers.size(); i++)
        {
            // always starting region
            MSERGrowthHistory region = ers.get(i);
            int valr0 = region.maxGreyValue;

            // always current region
            MSERGrowthHistory actRegion = region;
            int valri = actRegion.maxGreyValue;

            // always parent region of actRegion
            MSERGrowthHistory parent = actRegion.getParent();
            // always greyValue of direct parent of the LOW region
            int valr1 = parent.maxGreyValue;

            while (true)
            {
                // always greyValue of the parent of the current regions
                int valp = parent.maxGreyValue;

                /**
                 * Region is R-delta of actRegion
                 * VAL (r0) <= VAL (ri) - DELTA < VAL (r1)
                 */
                if (valr0 <= valri - params.delta && valri - params.delta < valr1)
                {
                    bottom[actRegion.getIndex()] = Math.max(bottom[actRegion.getIndex()], region.getSize());
                }

                /**
                 * actRegion is R+delta of region
                 * VAL(ri) <= VAL(x0) + DELTA < VAL(ri + 1)
                 */
                if (valri <= valr0 + params.delta && valr0 + params.delta < valp)
                {
                    top[region.getIndex()] = actRegion.getSize();
                }

                /**
                 * Stop if going on is useless
                 *
                 * VAL(r1) <= VAL(ri) - DELTA  --> No bottom can be found anymore
                 * VAL(ri) > VAL(r0) + DELTA --> no top can be found anymore
                 */
                if (valr1 <= valri - params.delta && valr0 + params.delta < valri)
                {
                    break;
                }

                /**
                 * The root is reached
                 */
                if (actRegion == parent)
                {
                    break;
                }

                actRegion = parent; // current region is now the parent
                valri = actRegion.maxGreyValue; // grey value of current region;
                parent = actRegion.getParent(); // get parent of current region
            }
        }

        /*
           * Calculate the areavariation
           */
        for (int i = 0; i < ers.size(); i++)
        {
            MSERGrowthHistory er = ers.get(i);

            int atop = top[er.getIndex()];
            int abot = bottom[er.getIndex()];

            areavar[i] = (float) (atop - abot) / (float) er.getSize();
            maxstable[i] = true;
        }

        // To take into account, that an ER is maximally stable if it has a local minimum regarding any of its child.
        // But it makes no difference.
        //boolean[] visited = new boolean[ers.size()];*/	//initialized to false

        /*
           * Remove not maximally stable ERs.
           */
        int maxcount = ers.size();
        for (int i = 0; i < ers.size(); i++)
        {
            MSERGrowthHistory er = ers.get(i);
            float var = areavar[i];
            float pvar = areavar[er.getParent().getIndex()];
            int notstable;

            if (er.getParent().maxGreyValue == er.maxGreyValue + 1)
            {
                if ((var < pvar) /*&& (!visited[ersp[i]])*/)
                {
                    notstable = er.getParent().getIndex();
                }
                else
                {
                    notstable = i;
                    /*visited[ersp[i]] = true;*/
                }

                if (maxstable[notstable])
                {
                    maxcount--;
                    maxstable[notstable] = false;
                }
            }
        }

        int imaxsize = (int) (imageLength * params.maxArea);
        int iminsize = (int) (imageLength * params.minArea);

        /*
           * Here clean up is done to remove low descriptive MSERs.
           * ERs are ordered in ers by increasing size (because of how they are added. Not absolute but relative to parent - child resp. set - subset relation).
           * For removing duplicates we must go from larger to smaller ones, otherwise is can happen that a child is removed because it is too similar with
           * its parent and then the parent is removed because of an other reason.
           */
        for (int i = ers.size() - 1; i >= 0; i--)
        { //for(int i = 0; i < ers.size(); i++){

            if (maxstable[i])
            {
                boolean remove = false;

                MSERGrowthHistory er = ers.get(i);

                // Remove too big ones
                if (er.getSize() > imaxsize)
                {
                    remove = true;
                }

                // Remove too small ones
                // should at least have 25 px that mser!!
                if (er.getSize() < iminsize || er.getSize() < 25)
                {
                    remove = true;
                }

                // Remove with high area variation
                if (areavar[i] >= params.maxVariation)
                {
                    remove = true;
                }

                // Remove duplicates
                if (!remove)
                {
                    int parent = er.getParent().getIndex();
                    if (parent != i)
                    {

                        // get first STABLE parent - or go through till the root
                        while (!maxstable[parent] && parent != ers.get(parent).getParent().getIndex())
                        {
                            parent = ers.get(parent).getParent().getIndex();
                        }
                        // if a stable parent was found
                        if (maxstable[parent])
                        {
                            int parea = ers.get(parent).getSize();
                            float d = (float) (parea - er.getSize()) / (float) er.getSize();
                            if ((d < params.minDiversity))
                            {    //&& ( parea < imaxsize )
                                remove = true;
                            }
                        }
                    }
                }

                if (remove)
                {
                    maxstable[i] = false;
                    maxcount--;
                }

            }
        }

        int idx = 0;
        mers = new MSERGrowthHistory[maxcount];
        for (int i = 0; i < ers.size(); i++)
        {
            if (maxstable[i])
            {
                mers[idx++] = ers.get(i);
//                System.out.println(i);
//                for (ImagePoint ip : ers.get(i).getPoints())
//                {
//                    System.out.print(ip.getIndex());
//                }
            }
        }
//        System.out.println("Found maximally stable regions:" + maxcount);

        return mers;
    }

    protected void processStack(int newPixelGreyLevel)
    {
//        System.out.println("newPixelGreyLevel:" + newPixelGreyLevel);
        // while the new pixel grey level is bigger than the grey level of the top stack
        while (newPixelGreyLevel > componentStack.peek().getGreyLevel())
        {
//            System.out.println("secondComp.greyLevel:" + componentStack.peek().getGreyLevel());
            // pop top-stack element for access to the second component
            MSERComponent topOfStack = componentStack.pop();

            // top of stack is ER
            topOfStack.addHistory();
            ers.add(topOfStack.getHistory());

            if (newPixelGreyLevel < componentStack.peek().getGreyLevel())
            {
                // set top of stack gray level to newPixelGreyLevel
                topOfStack.setGreyLevel(newPixelGreyLevel);
                // push it on the stack again and return
                componentStack.push(topOfStack);
            }
            else
            {
                // merge two components
                // the winner is the one which is bigger - but only finished EXTREMAL Regions are
                // taken into account - this means that the component still on the stack
                // cannot be considered finished, so the history size is taken (if available)
                if (topOfStack.getSize() >= componentStack.peek().getPastSize())
                {
                    // topOfStack is winner
                    MSERComponent second = componentStack.pop();
                    topOfStack.mergeComponents(second, second.getGreyLevel());

                    // add the topOfStack
                    componentStack.push(topOfStack);
                }
                else
                {
                    // second stack is winner
                    componentStack.peek().mergeComponents(topOfStack, componentStack.peek().getGreyLevel());
                }
            }
        }
    }

    public List<MSERFeature> computeMSERFeatures(BufferedImage image)
    {
        ImagePlus ip = new ImagePlus("image", image);
        MSERGrowthHistory[] msers = extractMSER(ip);
        ip.close();

        return computeFourier(msers, image.getWidth(), image.getHeight());
    }

    public List<MSERFeature> computeFourier(MSERGrowthHistory[] msers, int width, int height)
    {
        List<MSERFeature> features = new ArrayList<MSERFeature>();

        // compute Fourier
        for (MSERGrowthHistory mser : msers)
        {
            // getBorderPoints closes the shape already
            // is this a problem with coarsenPoly ??
            double[][] coarsedPoly = coarsenPoly(
                    pointToMatrix(mser.getBorderPoints(width, height)), COARSE_ANGLE);

            // TODO: check if there is a problem with coarsenPoly
            // TODO: if not, check if the coarsedPoly is closed after this
            // calculate the Fourier descriptors
            Fourier f = new Fourier(matrixToPoints(coarsedPoly, true));

            // compute Fourier with 32 variables
            f.computeFourier(32);
            // create affine invariant variables
            f.createInvariants2(1);

            features.add(new MSERFeature(mser, f.getInvariants()));
        }

        return features;
    }

    private static Point2D.Double[] matrixToPoints(double[][] coarsedPoly, boolean closeShape)
    {
        int n = (closeShape)? coarsedPoly[0].length+1 : coarsedPoly[0].length;
        int mod = coarsedPoly[0].length;
        Point2D.Double[] points = new Point2D.Double[n];

        for (int i = 0; i < n; i++)
        {
            points[i] = new Point2D.Double(coarsedPoly[0][i%mod],
                    coarsedPoly[1][i%mod]);
        }

        return points;
    }

    /**
     * implementation of cyclic translation for matrices.
     *
     * @param matrix    arbitrary typed matrix to be translated
     * @param m         Shift in y-Direction = first matrix-dimension!
     * @param n         Shift in x-Direction = second matrix-dimension!
     *
     * @return          cyclic translated matrix, e.g.
     *                        cycltrans([1 2 3; 4 5 6; 7 8 9], 1,1)
     *                        = [5 6 4; 8 9 7 ; 2 3 1]
     */
    protected static double[][] cycltrans( double[][] matrix, int m, int n)
    {
        int ys = matrix.length;
        int xs = matrix[0].length;

        m = ((m % ys)+ ys) % ys;
        n = ((n % xs)+ xs) % xs;

        double[][] doublematrix = new double[ys+ys][xs+xs];

        for (int i = 0; i < doublematrix.length; i++)
        {
            for (int j = 0; j < doublematrix[i].length; j++)
            {
                doublematrix[i][j] = matrix[i%ys][j%xs];
            }
        }

        double[][] res = new double[ys][xs];
        for (int i = 0; i < ys; i++)
        {
            for (int j = 0; j < xs; j++)
            {
                res[i][j] = doublematrix[i+m % ys][j+n % xs];
            }
        }

        printMatrix("RES", res);
        return res;
    }

    protected static double[][] coarsenPoly(double[][] p, double angle)
    {
        // finding the number of indices of p
        int n = p[0].length;

//        printMatrix("P", p);

        double[][] q = cycltrans(p, 0, 1);

//        printMatrix("Q", q);

        // finding the internal angles of the polygon p
        // formula: internal angle a = acos(-v1.v2/ |v1|.|v2|) where
        // v1 v2 are the line vectors of the sides of the polygon p
        double[][] v1 = new double[2][n];
        for (int i = 0; i < n; i++)
        {
            v1[0][i] = q[0][i] - p[0][i];
            v1[1][i] = q[1][i] - p[1][i];
        }

//        printMatrix("V1", v1);

        double[][] v2 = cycltrans(v1, 0, 1);

//        printMatrix("V2", v2);

        // num = v1.v2, dot product of the two line vectors
        double[][] dp = new double[2][n];
        double[] num = new double[n];
        for (int i = 0; i < n; i++)
        {
            dp[0][i] = v1[0][i] * v2[0][i];
            dp[1][i] = v1[1][i] * v2[1][i];
            num[i] = dp[0][i] + dp[1][i];
        }

//        printMatrix("DP", dp);
//        printArray("NUM", num);

        // l1 and l2 are | v1 |, | v2 | simultaneously
        double[] l1 = new double[n];
        double[] l2 = new double[n];
        double[] den = new double[n];

        for (int i = 0; i < n; i++)
        {
            l1[i] = Math.sqrt(v1[0][i] * v1[0][i] + v1[1][i] * v1[1][i]);
            l2[i] = Math.sqrt(v2[0][i] * v2[0][i] + v2[1][i] * v2[1][i]);
            den[i] = l1[i] * l2[i];
        }

//        printArray("L1", l1);
//        printArray("L2", l2);
//        printArray("DEN", den);

        // 'a' is a 1*s matrix with all the internal angles of polygon p.
        double[] a = new double[n];
        int counter = 0;

        for (int i = 0; i < n; i++)
        {
            a[i] = Math.acos(-num[i] / den[i]);
            if (a[i] <=(Math.PI-angle)) counter++;
        }

//        printArray("A", a);

        // selecting all indices of q with internal angle <= %pi-t into cq
        double[][] cq = new double[2][counter];
        counter = 0;
        for (int i = 0; i < n; i++)
        {
            if (a[i] < (Math.PI-angle))
            {
                cq[0][counter] = q[0][i];
                cq[1][counter++] = q[1][i];
            }
        }

//        printMatrix("CQ", cq);

        // if last of cq equals last of q then shift is necessary
        // else cq is result;
        if (cq[0][cq[0].length-1] == q[0][q[0].length-1] &&
                cq[1][cq[1].length-1] == q[1][q[1].length-1])
        {
            return cycltrans(cq, 0, -1);

        }
        else
        {
            return cq;
        }
    }




    public static void main (String[] args)
    {

        double[][] matrix = new double[3][2];

        matrix[0] = new double[] {1, 2};
        matrix[1] = new double[] {4, 5};
        matrix[2] = new double[] {7, 8};

//        printMatrix("1", matrix);

        matrix = cycltrans(matrix, 1, 0);

//        printMatrix("2", matrix);

        Point2D.Double[] poly = new Point2D.Double[10];
        poly[0] = new Point2D.Double(4, 0);
        poly[1] = new Point2D.Double(2.427051, 1.7633558);
        poly[2] = new Point2D.Double(1.236068, 3.8042261);
        poly[3] = new Point2D.Double(-0.9270510, 2.8531695);
        poly[4] = new Point2D.Double(-3.236068, 2.351141);
//        poly[5] = new Point2D.Double(-3, 4.441D-16);
        poly[5] = new Point2D.Double(-3, 4.441e-16);
        poly[6] = new Point2D.Double(-3.236068, -2.351141);
        poly[7] = new Point2D.Double(-0.9270510, -2.8531695);
        poly[8] = new Point2D.Double(1.236068, -3.8042261);
        poly[9] = new Point2D.Double(2.427051, -1.7633558);

        poly = matrixToPoints(coarsenPoly(pointToMatrix(poly), Math.PI/4), true);

//        printPoints("POLY", poly);

/*
        // gerades F
        Point2D.Double[] EF =
                {
                        new Point2D.Double(0/2.,0/2.),
                        new Point2D.Double(2/2.,0/2.),
                        new Point2D.Double(2/2.,5/2.),
                        new Point2D.Double(5/2.,5/2.),
                        new Point2D.Double(5/2.,7/2.),
                        new Point2D.Double(2/2.,7/2.),
                        new Point2D.Double(2/2.,9/2.),
                        new Point2D.Double(7/2.,9/2.),
                        new Point2D.Double(7/2.,11/2.),
                        new Point2D.Double(0/2.,11/2.),
                        new Point2D.Double(0/2.,0/2.) // X(N) == X(0))
                };

        testFourier(EF, 5);

*/

    }

    private static double[][] pointToMatrix(Point2D.Double[] poly)
    {
        double[][] res = new double[2][poly.length];
        for (int i = 0; i < poly.length; i++)
        {
            res[0][i] = poly[i].getX();
            res[1][i] = poly[i].getY();
        }

        return res;
    }

    private static double[][] pointToMatrix(ImagePoint[] poly)
    {
        double[][] res = new double[2][poly.length];
        for (int i = 0; i < poly.length; i++)
        {
            res[0][i] = poly[i].getX();
            res[1][i] = poly[i].getY();
        }

        return res;
    }

    private static void printMatrix(String name, double[][] matrix)
    {
//        System.out.println("MATRIX " + name + ":");
//        for (int i = 0; i < matrix.length; i++)
//        {
//            for (int j = 0; j < matrix[i].length; j++)
//            {
//                System.out.print(matrix[i][j]);
//                System.out.print(",");
//            }
//            System.out.println();
//        }
    }

    private static void printArray(String name, double[] array)
    {
//        System.out.println("ARRAY " + name + ":");
//        for (int i = 0; i < array.length; i++)
//        {
//            System.out.print(array[i]);
//            System.out.print(",");
//            System.out.println();
//        }
    }

    private static void printPoints(String name, Point2D.Double[] points)
    {
//        System.out.println("points " + name + ":");
//        for (int i = 0; i < points.length; i++)
//        {
//            System.out.println(points[i].toString());
//        }
    }
}