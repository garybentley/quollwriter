package com.quollwriter.data;

import java.text.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.text.*;


public class ReadabilityIndices
{

    public float syllableCount = 0;
    public float wordCount = 0;
    public float sentenceCount = 0;
    public float threeSyllableWordCount = 0;

    public void add (ReadabilityIndices ri)
    {
        
        this.sentenceCount += ri.sentenceCount;
        this.wordCount += ri.wordCount;
        this.syllableCount += ri.syllableCount;
        this.threeSyllableWordCount += ri.threeSyllableWordCount;
        
    }
    
    public void add (String str)
    {

        if (str == null)
        {

            return;

        }

        if (str.length () == 0)
        {

            return;

        }
        
        TextIterator ti = new TextIterator (str);
        this.wordCount += ti.getWordCount ();
        this.syllableCount += ti.getSyllableCount ();
        this.threeSyllableWordCount += ti.getThreeSyllableWordCount ();
        this.sentenceCount += ti.getSentenceCount ();
        
/*
        SentenceIterator si = new SentenceIterator (str);

        String s = si.next ();

        while (s != null)
        {

            this.sentenceCount++;
            
            List<String> words = null;
            
            words = TextUtilities.getAsWords (s);
                
            words = TextUtilities.stripPunctuation (words);
            
            this.wordCount += words.size ();

            for (String w : words)
            {

                int cc = TextUtilities.getSyllableCountForWord (w);

                if (cc > 2)
                {

                    this.threeSyllableWordCount++;

                }

                this.syllableCount += cc;

            }

            s = si.next ();

        }

        // Turn off the cache, it may have used quite a bit of memory.
        // Shouldn't be needed anymore.
        //Environment.setSynonymProviderUseCache (true);
        */
    }

    public int getSentenceCount ()
    {
        
        return (int) this.sentenceCount;
        
    }
    
    public int getWordCount ()
    {
        
        return (int) this.wordCount;
        
    }
    
    public float getFleschKincaidGradeLevel ()
    {

        if (this.wordCount <= 0)
        {
            
            return 0;
            
        }

        return (0.39f * (this.wordCount / this.sentenceCount)) + (11.8f * (this.syllableCount / this.wordCount)) - 15.59f;

    }

    public float getFleschReadingEase ()
    {

        if (this.wordCount <= 0)
        {
            
            return 0;
            
        }
    
        return (206.835f - (1.015f * (this.wordCount / this.sentenceCount)) - (84.6f * (this.syllableCount / this.wordCount)));

    }

    public float getGunningFogIndex ()
    {

        if (this.wordCount <= 0)
        {
            
            return 0;
            
        }
    
        return (0.4f * ((this.wordCount / this.sentenceCount) + ((this.threeSyllableWordCount / this.wordCount) * 100f))) - 0.5f;

    }

}
