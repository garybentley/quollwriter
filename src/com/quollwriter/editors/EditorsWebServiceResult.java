package com.quollwriter.editors;

import java.net.*;
import java.util.*;

import com.quollwriter.*;

public class EditorsWebServiceResult
{
    
    public int code = -1;
    private boolean error = false;
    private List reasons = null;
    private String errorType = null;
    private Object retObject = null;
    private EditorsWebServiceCall call = null;
        
    public EditorsWebServiceResult (EditorsWebServiceCall call,
                                    int                   resCode,
                                    String                res)
                             throws Exception
    {
        
        this.call = call;
        this.code = resCode;
        
        if (this.code != HttpURLConnection.HTTP_OK)
        {
         
            this.error = true;
            Object err = JSONDecoder.decode (res);

            if (err instanceof String)
            {

                this.errorType = "Unknown";
                this.reasons = new ArrayList ();
                
                this.reasons.add (err);
                
            }
            
            if (err instanceof Map)
            {
            
                Map merr = (Map) err;
                
                this.errorType = (String) (merr).get ("errorType");
                
                this.reasons = (List) (merr).get ("reasons");

            }
                                        
            return;
            
        }
        
        this.retObject = JSONDecoder.decode (res);
        
    }
    
    public EditorsWebServiceCall getCall ()
    {
        
        return this.call;
        
    }
    
    public String toString ()
    {
        
        if (this.error)
        {
        
            return this.code + ":" + this.errorType + ":" + this.getErrorMessage ();
        
        }
        
        return this.retObject + "";
        
    }
    
    public boolean isSuccess ()
    {
        
        String s = this.getReturnObjectAsString ();
        
        return "SUCCESS".equals (s);
        
    }
    
    public String getReturnObjectAsString ()
    {
        
        if (this.retObject == null)
        {
            
            return null;
            
        }
        
        return this.retObject.toString ();
        
    }
    
    public Object getReturnObject ()
    {
        
        return this.retObject;
        
    }
    
    public int getReturnCode ()
    {
        
        return this.code;
        
    }
    
    public String getErrorType ()
    {
        
        return this.errorType;
        
    }
    
    public boolean isNoDataFoundError ()
    {
        
        if (!this.error)
        {
            
            return false;
            
        }
        
        if (this.errorType == null)
        {
            
            return false;
            
        }
        
        return "NoDataFound".equals (this.errorType);
        
    }
    
    public String getErrorMessage ()
    {
        
        String m = null;
        
        if (this.reasons != null)
        {
            
            if (this.errorType.equals ("InvalidParameters"))
            {
                
                StringBuilder b = new StringBuilder ();
                
                for (int i = 0; i < this.reasons.size (); i++)
                {
                    
                    Map r = (Map) this.reasons.get (i);
                    
                    b.append (r.get ("error"));
                    
                    if (i > 0)
                    {
                        
                        b.append ("  ");
                        
                    }
                    
                }
                
                m = b.toString ();
                
            } else {
                
                StringBuilder b = new StringBuilder ();
                
                for (int i = 0; i < this.reasons.size (); i++)
                {
                    
                    String s = (String) this.reasons.get (i);
                    
                    b.append (s);
                    
                    if (i > 0)
                    {
                        
                        b.append ("  ");
                        
                    }
                    
                }                
                
                m = b.toString ();
                
            }
            
        }
        
        return m;
        
    }
    
    public boolean isError ()
    {
        
        return this.error;
        
    }
    
}