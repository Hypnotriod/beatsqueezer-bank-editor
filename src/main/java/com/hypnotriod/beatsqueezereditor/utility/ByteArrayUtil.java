package com.hypnotriod.beatsqueezereditor.utility;

/**
 *
 * @author Ilya Pikin
 */
public class ByteArrayUtil {

    public static void writeValueToByteArray(byte[] bytes, long val, long offset, int bytesNum) {
        for (int i = 0; i < bytesNum; i++) {
            bytes[(int) offset + i]
                    = (byte) (((0xFFL << ((bytesNum - i - 1) * 8)) & val) >> ((bytesNum - i - 1) * 8));
        }
    }

    public static int readInt32(byte[] bytes, long offset) {
        return (bytes[(int) offset] & 0xFF) | ((bytes[(int) offset + 1] & 0xFF) << 8)
                | ((bytes[(int) offset + 2] & 0xFF) << 16) | (bytes[(int) offset + 3] << 24);
    }

    public static short readInt16(byte[] bytes, long offset) {
        return (short) ((bytes[(int) offset] & 0xFF) | (bytes[(int) offset + 1] << 8));
    }
}
