package com.hypnotriod.beatsqueezereditor.tools;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Ilya Pikin
 */
public class FileInputStreamBinaryReader {

    private final byte[] _buffer = new byte[8];
    private FileInputStream _fileStream;

    public FileInputStream getFileStream() {
        return _fileStream;
    }

    public int available() throws IOException {
        return _fileStream.available();
    }

    public FileInputStreamBinaryReader(FileInputStream fileStream) {
        _fileStream = fileStream;
    }

    public int readInt32() throws IOException {
        _fileStream.read(_buffer, 0, 4);
        return (_buffer[0] & 0xFF) | ((_buffer[1] & 0xFF) << 8)
                | ((_buffer[2] & 0xFF) << 16) | (_buffer[3] << 24);
    }

    public short readInt16() throws IOException {
        _fileStream.read(_buffer, 0, 2);
        return (short) ((_buffer[0] & 0xFF) | (_buffer[1] << 8));
    }

    public char readChar() throws IOException {
        return (char) _fileStream.read();
    }

    public byte[] readBytes(int numBytes) throws IOException {
        byte[] bytes = new byte[numBytes];
        _fileStream.read(bytes);
        return bytes;
    }

    public void skip(long numBytes) throws IOException {
        _fileStream.skip(numBytes);
    }

    public void dispose() {
        if (_fileStream != null) {
            try {
                _fileStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                _fileStream = null;
            }
        }
    }
}
