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

package net.semanticmetadata.lire.imageanalysis.utils;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * ...
 * Date: 28.05.2008
 * Time: 11:26:28
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Quantization {
    static int[] quantMatrix = {
            256, 815, 1799, 2853, 4619, 7693, 65793, 70426, 131586, 131587, 131588, 132101, 136751, 197379, 197380, 197640, 199428, 262410, 263172, 263173, 264194, 268289, 328451, 328965, 329226, 329987, 329988, 331800, 393735, 394503, 394758, 395273, 396566, 406557, 460290, 460551, 460552, 461327, 464652, 525060, 526089, 526344, 527113, 530191, 590443, 591368, 591622, 591623, 592137, 592906, 595754, 655884, 657168, 657682, 658445, 661002, 673065, 722440, 723215, 724237, 726300, 733013, 788233, 789285, 790573, 794119, 853036, 854560, 855823, 858381, 865053, 920079, 921357, 923672, 929032, 985625, 987143, 989211, 993868, 1051157, 1052688, 1054267, 1058340, 1115704, 1118228, 1119757, 1123124, 1138751, 1183766, 1185313, 1188716, 1201709, 1249565, 1251351, 1254988, 1271974, 1315643, 1318176, 1322582, 1378723, 1382168, 1385249, 1390692, 1445662, 1448539, 1452334, 1465660, 1512725, 1515561, 1520205, 1574058, 1579309, 1582653, 1589187, 1640312, 1645854, 1649715, 1663624, 1707275, 1712165, 1716524, 1770427, 1771778, 1777741, 1782324, 1836297, 1842979, 1847072, 1861447, 1907985, 1911855, 1921337, 1973537, 1977637, 1986060, 2038838, 2043040, 2050904, 2103558, 2108211, 2114083, 2167311, 2173486, 2178920, 2231567, 2238764, 2243720, 2296073, 2304041, 2308668, 2335941, 2369571, 2373942, 2387791, 2435126, 2439740, 2453437, 2500883, 2505291, 2517657, 2566442, 2570901, 2582102, 2632753, 2637409, 2650974, 2699062, 2703970, 2723749, 2765632, 2770756, 2824034, 2832438, 2838238, 2894378, 2899459, 2907553, 2961748, 2966646, 2981210, 3029058, 3034590, 3090479, 3096450, 3103971, 3158879, 3164162, 3178589, 3226681, 3232331, 3288367, 3294317, 3304007, 3357245, 3362887, 3417413, 3425596, 3434104, 3488578, 3494259, 3549128, 3557200, 3566169, 3620399, 3626300, 3681637, 3689282, 3699285, 3752524, 3758968, 3815733, 3821660, 3837126, 3885130, 3892594, 3948868, 3955018, 4010680, 4018393, 4032873, 4082523, 4090705, 4146723, 4153158, 4210290, 4216785, 4267285, 4280893, 4291740, 4345187, 4352854, 4409431, 4416121, 4473663, 4480329, 4533472, 4544366, 4557477, 4608825, 4617109, 4673119, 4680095, 4737588, 4744099, 4800184, 4808541, 4858651, 4873048, 4885874, 4937563, 4946291, 5002062, 5009343, 5066556, 5073288, 5129809, 5137827, 5191451, 5202543, 5232587, 5267558, 5281242, 5332357, 5341842, 5397353, 5405124, 5462372, 5469847, 5527669, 5535172, 5592920, 5600357, 5657937, 5665365, 5722851, 5730673, 5787711, 5796165, 5852746, 5861765, 5917814, 5927277, 5982828, 5992789, 6046768, 6058084, 6110512, 6123137, 6151119, 6188429, 6201782, 6253429, 6262252, 6318414, 6325872, 6383205, 6390658, 6447985, 6455680, 6512708, 6520466, 6577214, 6585729, 6642003, 6650782, 6705998, 6716057, 6770234, 6781328, 6834662, 6846607, 6897932, 6911881, 6940883, 6977391, 6996952, 7042999, 7066586, 7109007, 7155988, 7174830, 7222809, 7240827, 7292176, 7306897, 7362471, 7373760, 7431778, 7440769, 7498606, 7507829, 7565208, 7574432, 7632273, 7641248, 7699342, 7710661, 7765896, 7812121, 7832168, 7879711, 7898539, 7953344, 7965072, 8022635, 8031142, 8089196, 8096947, 8155982, 8163490, 8221808, 8229260, 8288116, 8295597, 8354428, 8362407, 8420452, 8428500, 8487036, 8495034, 8553615, 8562099, 8619639, 8628420, 8685148, 8694253, 8750747, 8761286, 8817545, 8863521, 8885187, 8942189, 8954058, 9014463, 9069897, 9085626, 9146810, 9206383, 9219752, 9280162, 9341831, 9398603, 9415625, 9477049, 9538969, 9597786, 9613790, 9674691, 9736869, 9796706, 9813455, 9872808, 9934767, 9994433, 10012623, 10070707, 10133161, 10195357, 10254426, 10272729, 10333157, 10396079, 10459604, 10520191, 10572591, 10599100, 10661556, 10724797, 10787736, 10848856, 10900256, 10925264, 10987708, 11050365, 11111280, 11131879, 11188938, 11251897, 11315848, 11379332, 11440756, 11493433, 11519712, 11582385, 11645863, 11710115, 11772043, 11828271, 11849428, 11909833, 11973303, 12028279, 12047593, 12106969, 12169649, 12224588, 12241623, 12303558, 12360129, 12376799, 12436195, 12494243, 12508128, 12567733, 12622691, 12648428, 12700873, 12763298, 12820106, 12879719, 12902116, 12964299, 13028297, 13092275, 13156527, 13220051, 13283982, 13347235, 13411687, 13472116, 13532691, 13556938, 13619135, 13677207, 13735505, 13755364, 13817044, 13878449, 13940124, 14000006, 14058847, 14082272, 14145756, 14210242, 14271148, 14323760, 14344157, 14408406, 14470603, 14528147, 14542575, 14607583, 14671063, 14673380, 14737377, 14738916, 14802650, 14803680, 14867400, 14869473, 14869474, 14870251, 14935011, 14935266, 14997208, 15000543, 15065297, 15066336, 15131611, 15132129, 15197408, 15197409, 15197410, 15197411, 15197669, 15197670, 15197671, 15197672, 15200216, 15266546, 15396318, 16777214};

    /**
     * Simple linear quantization for 3 component color spaces with int values (e.g. rgb).
     *
     * @param pixel         the actual pixel values in a int[3] array
     * @param numBins       the number of bins (quantization levels)
     * @param maxPixelValue upper border for pixel limits. pixel values in pixel[] are from [0-maxPixelValue]
     * @return the quantized values < numbins
     */
    public static int quantUniformly(int[] pixel, int numBins, double maxPixelValue) {
        assert (pixel.length == 3);
        double v = maxPixelValue + 1;
        double quant = (v * v * v) / (double) numBins;
        return (int) Math.floor((pixel[0] + pixel[1] * v + pixel[2] * v * v) / quant);
    }

    public static int quantDistributionBased(int[] pixel, int numBins, double maxPixelValue) {
        int p = (pixel[0] + pixel[1] * 256 + pixel[2] * 256 * 256);
        for (int i = 0; i < quantMatrix.length; i++) {
            if (quantMatrix[i] > p) {
                return i;
            }
        }
        return quantMatrix.length - 1;
    }

    public static double[] getHistogram(BufferedImage image) {
        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new UnsupportedOperationException("Color space not supported. Only RGB.");
        WritableRaster raster = image.getRaster();
        double[] result = new double[256 * 256 * 256];
        for (int i = 0; i < result.length; i++) {
            result[i] = 0;
        }
        int[] pixel = new int[3];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                result[(pixel[0] + pixel[1] * 256 + pixel[2] * 256 * 256)] += 1d;
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i] / (image.getWidth() * image.getHeight());
        }
        return result;
    }

    public static double[] getHistograms(String[] imageFile) throws IOException {
        double[] result = new double[256 * 256 * 256];
        for (int i = 0; i < result.length; i++) {
            result[i] = 0;
        }
        for (int i = 0; i < imageFile.length; i++) {
            double[] res = getHistogram(ImageIO.read(new FileInputStream(imageFile[i])));
            for (int j = 0; j < res.length; j++) {
                result[j] = res[j] + result[j];

            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i] / imageFile.length;
        }
        return result;
    }

    public static void getQuantizationMatrix(String[] imageFile, int numBins) throws IOException {
        double[] histograms = getHistograms(imageFile);
        double border = 1d / (double) numBins;
        double step = 1d / (double) numBins;
        double current = 0d;
        int bin = 0;
        System.out.println("int quantMatrix = {");
        for (int i = 0; i < histograms.length; i++) {
            current += histograms[i];
            if (current > border) {
                System.out.print(i + ", ");
                border += step;
//                System.out.println("Bin " + bin + " - color " + i);
                bin++;
            }
        }
        System.out.println(histograms.length + "};");
    }

    public static void main(String[] args) throws IOException {
        int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};
        String testExtensive = "./lire/wang-data-1000";
        String[] files = new String[50];
        for (int i = 0; i < files.length; i++) {
            files[i] = testExtensive + "/" + sampleQueries[i] + ".jpg";

        }
        getQuantizationMatrix(files, 512);
    }

}
