package com.quollwriter.data;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

public class Session
{
    
    private Date startTime = null;
    private Date endTime = null;
    private int wordCount = 0;
    
    public Session ()
    {
                
    }
    
    public Session (Date start,
                    Date end,
                    int  wc)
    {
        
        this.startTime = start;
        this.endTime = end;
        this.wordCount = wc;
        
    }
            
    @Override     
    public String toString ()
    {
        
        return "start: " + this.startTime + ", end: " + this.endTime + ", wordcount: " + this.wordCount;
        
    }
    
    public long getSessionDuration ()
    {
        
        if (this.startTime == null)
        {
            
            return 0;
            
        }
        
        Date e = (this.endTime != null ? this.endTime : new Date ());
        
        return e.getTime () - this.startTime.getTime ();
        
    }
    
    public Date getEnd ()
    {
        
        return this.endTime;
        
    }
    
    public Date getStart ()
    {
        
        return this.startTime;
        
    }
    
    public int getWordCount ()
    {
        
        return this.wordCount;

    }
    
    public void start (Date startTime)
    {
        
        this.startTime = startTime;
        
    }

    public void end (Date endTime)
    {
        
        this.endTime = endTime;
        
    }
        
}