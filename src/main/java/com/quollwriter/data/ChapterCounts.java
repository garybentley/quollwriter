package com.quollwriter.data;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.text.*;

public class ChapterCounts
{

    //public Chapter chapter = null;
    private int     wordCount = 0;
    private IntegerProperty wordCountProp = null;
    private IntegerProperty sentenceCountProp = null;
    private IntegerProperty standardPageCountProp = null;
    private int     sentenceCount = 0;
    private int     standardPageCount = 0;
    public Map<String, Integer> wordFrequency = null;

    public ChapterCounts ()
    {

        this.wordCountProp = new SimpleIntegerProperty (this.wordCount);
        this.sentenceCountProp = new SimpleIntegerProperty (this.sentenceCount);
        this.standardPageCountProp = new SimpleIntegerProperty (this.standardPageCount);

    }

    public ChapterCounts (String t)
    {

        this ();
        if (t != null)
        {

            TextIterator ti = new TextIterator (t);

            this.setWordCount (ti.getWordCount ());
            this.setSentenceCount (ti.getSentenceCount ());
            this.wordFrequency = ti.getWordFrequency ();

        }

    }

    public void setWordCount (int c)
    {

        this.wordCount = c;

        UIUtils.runLater (() ->
        {

            this.wordCountProp.setValue (c);

        });

    }

    public int getWordCount ()
    {

        return this.wordCount;

    }

    public IntegerProperty wordCountProperty ()
    {

        return this.wordCountProp;

    }

    public void setSentenceCount (int c)
    {

        this.sentenceCount = c;

        UIUtils.runLater (() ->
        {

            this.sentenceCountProp.setValue (c);

        });

    }

    public int getSentenceCount ()
    {

        return this.sentenceCount;

    }

    public IntegerProperty sentenceCountProperty ()
    {

        return this.sentenceCountProp;

    }

    public void setStandardPageCount (int c)
    {

        this.standardPageCount = c;

        UIUtils.runLater (() ->
        {

            this.standardPageCountProp.setValue (c);

        });

    }

    public int getStandardPageCount ()
    {

        return this.standardPageCount;

    }

    public IntegerProperty standardPageCountProperty ()
    {

        return this.standardPageCountProp;

    }

    public void add (ChapterCounts c)
    {

        //this.chapter = null;
        this.wordCount += c.wordCount;
        this.setWordCount (this.wordCount);
        this.sentenceCount += c.sentenceCount;
        this.setSentenceCount (this.sentenceCount);
        this.standardPageCount += c.standardPageCount;
        this.setStandardPageCount (this.standardPageCount);

        if (this.wordFrequency == null)
        {

            this.wordFrequency = new HashMap<> ();

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
