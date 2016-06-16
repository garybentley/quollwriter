package com.quollwriter.data;

import java.util.*;

import com.quollwriter.text.*;

public class ChapterCounts
{

    //public Chapter chapter = null;
    public int     wordCount = 0;
    public int     sentenceCount = 0;
    public int     standardPageCount = 0;
    public Map<String, Integer> wordFrequency = null;

    public ChapterCounts ()
    {
        
    }
    
    public ChapterCounts (String t)
    {
        
        if (t != null)
        {
            
            TextIterator ti = new TextIterator (t);
            
            this.wordCount = ti.getWordCount ();
            this.sentenceCount = ti.getSentenceCount ();
            
            this.wordFrequency = ti.getWordFrequency ();
            
        }
        
    }
    
    public void add (ChapterCounts c)
    {

        //this.chapter = null;
        this.wordCount += c.wordCount;
        this.sentenceCount += c.sentenceCount;
        this.standardPageCount += c.standardPageCount;
     
        if (this.wordFrequency == null)
        {
            
            this.wordFrequency = new HashMap ();
            
        }
        
        if (c.wordFrequency != null)
        {
        
            for (Map.Entry<String, Integer> en : c.wordFrequency.entrySet ())
            {
                
                String w = en.getKey ();
                Integer oc = en.getValue ();
                
                Integer count = this.wordFrequency.get (w);
                
                int wc = 1;
                
                if (count != null)
                {
                        
                    wc += count.intValue ();
                    
                }
                
                if (oc != null)
                {
                    
                    wc += oc.intValue ();
                    
                }

                this.wordFrequency.put (w,
                                        Integer.valueOf (wc));
                
            }

        }
            
    }

}
