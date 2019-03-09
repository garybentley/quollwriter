package com.quollwriter;

import java.net.*;
import java.io.*;

public class UrlDownloader
{
    
    private URL url = null;
    private DownloadListener list = null;
    private File toFile = null;
    private boolean stop = false;
    private boolean inProgress = false;
    
    public UrlDownloader (URL              url,
                          File             toFile,
                          DownloadListener l)
    {
        
        this.url = url;
        this.toFile = toFile;
        this.list = l;
        
    }    
        
    public boolean isInProgress ()
    {
        
        return this.inProgress;
        
    }
    
    public void start ()
    {

        this.stop = false;

        if (this.inProgress)
        {

            return;
            
        }
    
        this.inProgress = true;
    
        final UrlDownloader _this = this;
    
        new Thread (new Runnable ()
        {
            
            public void run ()
            {

                InputStream in = null;
                OutputStream out = null;
                
                try
                {
                
                    URLConnection conn = _this.url.openConnection ();
                    
                    int length = conn.getContentLength ();
                    
                    in = new BufferedInputStream (conn.getInputStream ());
                    out = new BufferedOutputStream (new FileOutputStream (_this.toFile));
                    
                    byte[] buf = new byte[8192];
        
                    int bRead = -1;
        
                    int total = 0;
        
                    while ((bRead = in.read (buf)) != -1)
                    {
                
                        if (_this.stop)
                        {
        
                            in.close ();
                            out.flush ();
                            out.close ();
                
                            return;
        
                        }
        
                        total += bRead;

                        out.write (buf,
                                   0,
                                   bRead);

                        if (_this.list != null)
                        {                                   
                        
                            _this.list.progress (total,
                                                 length);
                            
                        }
                    
                    }

                    in.close ();
                    out.flush ();
                    out.close ();

                    if (_this.list != null)
                    {
                        
                        _this.list.finished (length);
                        
                    }
                                        
                } catch (Exception e) {
                    
                    if (_this.list != null)
                    {
                    
                        _this.list.handleError (e);
                        
                    }
                    
                } finally {
                
                    _this.inProgress = false;
                    
                    try
                    {
                    
                        if (in != null)
                        {
                            
                            in.close ();
                            
                        }
    
                        if (out != null)
                        {
                            
                            out.flush ();
                            out.close ();
                            
                        }

                    } catch (Exception e) {}
                    
                }
                
            }
            
        }).start ();
        
    }
    
    public void stop ()
    {
        
        this.stop = true;
        
    }
    
}