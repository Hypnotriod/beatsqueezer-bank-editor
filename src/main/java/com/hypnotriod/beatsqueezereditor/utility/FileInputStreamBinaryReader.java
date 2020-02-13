package com.hypnotriod.beatsqueezereditor.utility;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Ilya Pikin
 */
public class FileInputStreamBinaryReader {

    private final byte[] buffer = new byte[8];
    private FileInputStream fileStream;

    public FileInputStream getFileStream() {
        return fileStream;
    }

    public int available() throws IOException {
        return fileStream.available();
    }

    public FileInputStreamBinaryReader(FileInputStream fileStream) {
        this.fileStream = fileStream;
    }

    public int readInt32() throws IOException {
        fileStream.read(buffer, 0, 4);
        return (buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8)
                | ((buffer[2] & 0xFF) << 16) | (buffer[3] << 24);
    }

    public short readInt16() throws IOException {
        fileStream.read(buffer, 0, 2);
        return (short) ((buffer[0] & 0xFF) | (buffer[1] << 8));
    }

    public char readChar() throws IOException {
        return (char) fileStream.read();
    }

    public byte[] readBytes(int numBytes) throws IOException {
        byte[] bytes = new byte[numBytes];
        fileStream.read(bytes);
        return bytes;
    }

    public void skip(long numBytes) throws IOException {
        fileStream.skip(numBytes);
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
