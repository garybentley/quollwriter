package com.quollwriter.ui.events;

import java.util.*;

import com.quollwriter.ui.*;

public class WordCountTimerEvent extends EventObject
{
    
    private int wordCount = -1;
    private int wordPerc = -1;
    private int minCount = -1;
    private int minPerc = -1;
    private WordCountTimer timer = null;
    
    public WordCountTimerEvent (WordCountTimer source,
                                int            wordCount,
                                int            wordPercentage,
                                int            minuteCount,
                                int            minPercentage)
    {
        
        super (source);
        
        this.timer = timer;
        this.wordCount = wordCount;
        this.wordPerc = wordPercentage;
        this.minCount = minuteCount;
        this.minPerc = minPercentage;
        
    }
    
    public WordCountTimer getTimer ()
    {
        
        return this.timer;
        
    }
    
    public int getWordCount ()
    {
        
        return this.wordCount;
        
    }
    
    public int getWordPercentage ()
    {
        
        return this.wordPerc;
        
    }
    
    public int getMinuteCount ()
    {
        
        return this.minCount;
        
    }
    
    public int getMinutePercentage ()
    {
        
        return this.minPerc;
        
    }
    
}