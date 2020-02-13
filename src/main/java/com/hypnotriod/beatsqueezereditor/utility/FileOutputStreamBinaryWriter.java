package com.hypnotriod.beatsqueezereditor.utility;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Ilya Pikin
 */
public class FileOutputStreamBinaryWriter {

    private final byte[] buffer = new byte[8];

    private FileOutputStream fileStream;

    public FileOutputStreamBinaryWriter(FileOutputStream fileOutputStream) {
        fileStream = fileOutputStream;
    }

    public void writeInt64(long value) throws IOException {
        buffer[0] = (byte) (value & 0xFF);
        buffer[1] = (byte) ((value >> 8) & 0xFF);
        buffer[2] = (byte) ((value >> 16) & 0xFF);
        buffer[3] = (byte) ((value >> 24) & 0xFF);
        buffer[4] = (byte) ((value >> 32) & 0xFF);
        buffer[5] = (byte) ((value >> 40) & 0xFF);
        buffer[6] = (byte) ((value >> 48) & 0xFF);
        buffer[7] = (byte) ((value >> 56) & 0xFF);
        fileStream.write(buffer, 0, 8);
    }

    public void writeInt32(int value) throws IOException {
        buffer[0] = (byte) (value & 0xFF);
        buffer[1] = (byte) ((value >> 8) & 0xFF);
        buffer[2] = (byte) ((value >> 16) & 0xFF);
        buffer[3] = (byte) ((value >> 24) & 0xFF);
        fileStream.write(buffer, 0, 4);
    }

    public void writeInt16(short value) throws IOException {
        buffer[0] = (byte) (value & 0xFF);
        buffer[1] = (byte) ((value >> 8) & 0xFF);
        fileStream.write(buffer, 0, 2);
    }

    public void writeString(String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            buffer[i] = (byte) value.charAt(i);
        }
        fileStream.write(buffer, 0, value.length());
    }

    public void flush() throws IOException {
        fileStream.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        fileStream.write(b, off, len);
    }

    public void dispose() {
        if (fileStream != null) {
            try {
                fileStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                fileStream = null;
            }
        }
    }

}
