package com.quollwriter.data;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

public class Session
{
    
    private int currentSessionWordCount = 0;
    private ChapterCounts endWordCount = null;
    private Date startTime = null;
    private Date endTime = null;
    private int endOfHourWordCount = 0;
    
    public Session ()
    {
                
    }
    
    public int getSessionWordCount ()
    {
        
        Map<ProjectInfo, AbstractProjectViewer> pvs = Environment.getOpenProjects ();
        
        int c = this.currentSessionWordCount;
        
        for (AbstractProjectViewer pv : pvs.values ())
        {
            
            c += pv.getSessionWordCount ();
            
        }
        
        return c;
        
    }
    
    public void start (Date startTime)
    {
        
        this.startTime = startTime;
        
    }

    public void end (ChapterCounts endWordCount,
                     Date          endTime)
    {
        
        this.endWordCount = endWordCount;
        this.endTime = endTime;
        
    }
    
    public void updateSessionWordCount (int wordCount)
    {
        
        this.currentSessionWordCount += wordCount;
        
    }
    
}