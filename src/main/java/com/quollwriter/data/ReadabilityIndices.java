package com.quollwriter.data;

import java.text.*;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;

import com.quollwriter.text.*;

public class ReadabilityIndices
{

    public float syllableCount = 0;
    public float wordCount = 0;
    public float sentenceCount = 0;
    public float threeSyllableWordCount = 0;
    private float fkgl = 0;
    private float fre = 0;
    private float gfi = 0;
    private FloatProperty fkglProp = new SimpleFloatProperty (this.fkgl);
    private FloatProperty freProp = new SimpleFloatProperty (this.fre);
    private FloatProperty gfiProp = new SimpleFloatProperty (this.gfi);

    public void add (ReadabilityIndices ri)
    {

        this.sentenceCount += ri.sentenceCount;
        this.wordCount += ri.wordCount;
        this.syllableCount += ri.syllableCount;
        this.threeSyllableWordCount += ri.threeSyllableWordCount;

        this.update ();

    }

    public void add (String str)
    {

        if ((str != null)
            &&
            (str.length () > 0)
           )
        {

            TextIterator ti = new TextIterator (str);
            this.wordCount += ti.getWordCount ();
            this.syllableCount += ti.getSyllableCount ();
            this.threeSyllableWordCount += ti.getThreeSyllableWordCount ();
            this.sentenceCount += ti.getSentenceCount ();

        }

        this.update ();

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

    private void update ()
    {

        if (this.wordCount <= 0)
        {

            this.fkgl = 0;
            this.fre = 0;
            this.gfi = 0;

        } else {

            this.fkgl = (0.39f * (this.wordCount / this.sentenceCount)) + (11.8f * (this.syllableCount / this.wordCount)) - 15.59f;
            this.fre = (206.835f - (1.015f * (this.wordCount / this.sentenceCount)) - (84.6f * (this.syllableCount / this.wordCount)));
            this.gfi = (0.4f * ((this.wordCount / this.sentenceCount) + ((this.threeSyllableWordCount / this.wordCount) * 100f))) - 0.5f;

        }

        this.fkglProp.setValue (this.fkgl);
        this.freProp.setValue (this.fre);
        this.gfiProp.setValue (this.gfi);

    }

    public int getSentenceCount ()
    {

        return (int) this.sentenceCount;

    }

    public int getWordCount ()
    {

        return (int) this.wordCount;

    }

    public FloatProperty fleschKindcaidGradeLevelProperty ()
    {

        return this.fkglProp;

    }

    public FloatProperty fleschReadingEaseProperty ()
    {

        return this.freProp;

    }

    public FloatProperty gunningFogIndexProperty ()
    {

        return this.gfiProp;

    }

    public float getFleschKincaidGradeLevel ()
    {

        return this.fkgl;

    }

    public float getFleschReadingEase ()
    {

        return this.fre;

    }

    public float getGunningFogIndex ()
    {

        return this.gfi;

    }

}
