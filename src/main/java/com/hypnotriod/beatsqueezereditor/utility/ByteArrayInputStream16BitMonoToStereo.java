package com.hypnotriod.beatsqueezereditor.utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * @author Ilya Pikin
 */
public class ByteArrayInputStream16BitMonoToStereo extends ByteArrayInputStream {

    private byte[] bytesIn = null;

    public ByteArrayInputStream16BitMonoToStereo(byte[] bytes) {
        super(bytes);
    }

    public ByteArrayInputStream16BitMonoToStereo(byte[] bytes, int i, int i1) {
        super(bytes, i, i1);
    }

    @Override
    public int read(byte[] bytes, int start, int end) {
        int result;
        if (bytesIn == null || bytesIn.length != bytes.length / 2) {
            bytesIn = new byte[bytes.length / 2];
        }
        result = super.read(bytesIn, start / 2, end / 2);
        doubleArray(bytesIn, bytes);
        return result != -1 ? result * 2 : -1;
    }

    @Override
    public long skip(long l) {
        return super.skip(l / 2) * 2;
    }

    private void doubleArray(byte[] bytesIn, byte[] bytesOut) {
        int j;
        for (int i = 0; i < bytesIn.length; i += 2) {
            j = i * 2;
            bytesOut[j++] = bytesIn[i];
            bytesOut[j++] = bytesIn[i + 1];
            bytesOut[j++] = bytesIn[i];
            bytesOut[j++] = bytesIn[i + 1];
        }
    }

    @Override
    public void reset() {
        bytesIn = null;
        super.reset();
    }

    @Override
    public void close() throws IOException {
        bytesIn = null;
        super.close();
    }
}
