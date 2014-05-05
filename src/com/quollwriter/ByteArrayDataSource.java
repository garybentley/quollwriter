package com.quollwriter;

import java.io.*;

import javax.activation.*;

public class ByteArrayDataSource implements DataSource
{
    
    private byte[] bytes = null;
    
    public ByteArrayDataSource (byte[] bytes)
    {
        
        this.bytes = bytes;
        
    }
    
    public InputStream getInputStream ()
                                       throws IOException
    {
        
        return new ByteArrayInputStream (this.bytes);
        
    }
    
    public OutputStream getOutputStream ()
                                         throws IOException
    {
        
        throw new UnsupportedOperationException ("Write is not supported.");
        
    }
    
    public String getName ()
    {
        
        return null;
        
    }
    
    public String getContentType ()
    {
        
        return "application/octet-stream";
        
    }
    
}