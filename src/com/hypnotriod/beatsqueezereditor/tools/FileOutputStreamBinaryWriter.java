
package com.hypnotriod.beatsqueezereditor.tools;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Илья
 */
public class FileOutputStreamBinaryWriter 
{
    private final byte[] _buffer = new byte[8];
    
    private FileOutputStream _fileStream;
    
    public FileOutputStreamBinaryWriter(FileOutputStream fileOutputStream)
    {
        _fileStream = fileOutputStream;
    }
    
    public void writeInt64(long value) throws IOException
    {
        _buffer[0] = (byte)(value & 0xFF);
        _buffer[1] = (byte)((value >> 8) & 0xFF);
        _buffer[2] = (byte)((value >> 16) & 0xFF);
        _buffer[3] = (byte)((value >> 24) & 0xFF);
        _buffer[4] = (byte)((value >> 32) & 0xFF);
        _buffer[5] = (byte)((value >> 40) & 0xFF);
        _buffer[6] = (byte)((value >> 48) & 0xFF);
        _buffer[7] = (byte)((value >> 56) & 0xFF);
        _fileStream.write(_buffer, 0, 8);
    }
    
    public void writeInt32(int value) throws IOException
    {
        _buffer[0] = (byte)(value & 0xFF);
        _buffer[1] = (byte)((value >> 8) & 0xFF);
        _buffer[2] = (byte)((value >> 16) & 0xFF);
        _buffer[3] = (byte)((value >> 24) & 0xFF);
        _fileStream.write(_buffer, 0, 4);
    }
    
    public void writeInt16(short value) throws IOException
    {
        _buffer[0] = (byte)(value & 0xFF);
        _buffer[1] = (byte)((value >> 8) & 0xFF);
        _fileStream.write(_buffer, 0, 2);
    }
    
    public void writeString(String value) throws IOException
    {
        for(int i = 0; i < value.length(); i++)
            _buffer[i] = (byte)value.charAt(i);
        _fileStream.write(_buffer, 0, value.length());
    }
    
    public void flush() throws IOException
    {
        _fileStream.flush();
    }
    
    public void write(byte[] b, int off, int len) throws IOException
    {
        _fileStream.write(b, off, len);
    }
    
    public void dispose()
    {
        if(_fileStream != null) {
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
