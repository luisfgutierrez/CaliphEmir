package net.semanticmetadata.lire.utils;

import java.util.Arrays;

/**
 * Utility class for serialization issues.
 * Created by: Mathias Lux, mathias@juggle.at
 * Date: 19.03.2010
 * Time: 14:58:26
 */
public class SerializationUtils {
    /**
     * Converts a byte array with 4 elements to an int. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow).
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the input byte array
     * @return the resulting int
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(int)
     */
    public static int toInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        return (int) ( // NOTE: type cast not necessary for int
                (0xff & data[0]) << 24 |
                        (0xff & data[1]) << 16 |
                        (0xff & data[2]) << 8 |
                        (0xff & data[3]) << 0
        );
    }

    /**
     * Converts an int to a byte array with 4 elements. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow).
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the int to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toInt(byte[])
     */
    public static byte[] toBytes(int data) {
        return new byte[]{
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff),
        };
    }

    /**
     * Convenience method to transform an int[] array to a byte array for serialization.
     *
     * @param data the int[] to convert
     * @return the resulting byte[] 4 times in size (4 bytes per int)
     */
    public static byte[] toByteArray(int[] data) {
        byte[] tmp, result = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 4, 4);
        }
        return result;
    }

    /**
     * Convenience method to create an int[] array from a byte[] array.
     * @param data the byte[] array to decode
     * @return the decoded int[]
     */
    public static int[] toIntArray(byte[] data) {
        int[] result = new int[data.length / 4];
        byte[] tmp = new byte[4];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 4, tmp, 0, 4);
            result[i] = toInt(tmp);
        }
        return result;
    }

    /**
     * Converts a float to a byte array with 4 elements. Used to put floats into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toFloat(byte[])} to decode.
     *
     * @param data the float to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toFloat(byte[])
     */
    public static byte[] toBytes(float data) {
        return toBytes(Float.floatToRawIntBits(data));
    }

    /**
     * Converts a byte array with 4 elements to a float. Used to put floats into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toBytes(float)} to encode.
     *
     * @param data the input byte array
     * @return the resulting float
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(float)
     */
    public static float toFloat(byte[] data) {
        return Float.intBitsToFloat(toInt(data));
    }

    /**
     * Convenience method for creating a byte array from a float array.
     *
     * @param data the input float array
     * @return a byte array for serialization.
     */
    public static byte[] toByteArray(float[] data) {
        byte[] tmp, result = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 4, 4);
        }
        return result;
    }

    /**
     * Convenience method for creating a float array from a byte array.
     *
     * @param data
     * @return
     */
    public static float[] toFloatArray(byte[] data) {
        float[] result = new float[data.length / 4];
        byte[] tmp = new byte[4];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 4, tmp, 0, 4);
            result[i] = toFloat(tmp);
        }
        return result;
    }

    /**
     * Converts a double to a byte array with 4 elements. Used to put doubles into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toDouble(byte[])} to decode. Note that there is a loss
     * in precision as the double is converted to a float in the course of conversion.
     *
     * @param data the double to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toDouble(byte[])
     */
    public static byte[] toBytes(double data) {
        return toBytes(Float.floatToRawIntBits((float) data));
    }

    /**
     * Converts a byte array with 4 elements to a double. Used to put doubles into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toBytes(double)} to encode. Note that there is a loss
     * in precision as the double is converted to a float in the course of conversion.
     *
     * @param data the input byte array
     * @return the resulting float
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(double)
     */
    public static double toDouble(byte[] data) {
        return (double) Float.intBitsToFloat(toInt(data));
    }

    /**
     * Convenience method for creating a byte array from a double array.
     *
     * @param data the input float array
     * @return a byte array for serialization.
     */
    public static byte[] toByteArray(double[] data) {
        byte[] tmp, result = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            tmp = toBytes(data[i]);
            System.arraycopy(tmp, 0, result, i * 4, 4);
        }
        return result;
    }

    /**
     * Convenience method for creating a double array from a byte array.
     *
     * @param data
     * @return
     */
    public static double[] toDoubleArray(byte[] data) {
        double[] result = new double[data.length / 4];
        byte[] tmp = new byte[4];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(data, i * 4, tmp, 0, 4);
            result[i] = toDouble(tmp);
        }
        return result;
    }


    /**
     * Convenience method for creating a String from an array.
     *
     * @param array
     * @return
     */
    public static String arrayToString(int[] array) {
        return Arrays.toString(array);
//        StringBuilder sb = new StringBuilder(256);
//        for (int i = 0; i < array.length; i++) {
//            sb.append(array[i]);
//            sb.append(' ');
//        }
//        return sb.toString().trim();
    }
}
