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
    private IntegerProperty paragraphCountProp = null;
    private int     sentenceCount = 0;
    private int     standardPageCount = 0;
    private int     paragraphCount = 0;
    private int spellingErrorCount = 0;
    public Map<String, Integer> wordFrequency = null;
    private ObjectProperty<ReadabilityIndices> readabilityIndicesProp = null;
    private IntegerProperty spellingErrorCountProp = null;
    private int problemFinderProblemsCount = 0;
    private IntegerProperty problemFinderProblemsCountProp = null;

    public ChapterCounts ()
    {

        this.wordCountProp = new SimpleIntegerProperty (0);
        //this.wordCount);
        this.sentenceCountProp = new SimpleIntegerProperty (0);
        //this.sentenceCount);
        this.standardPageCountProp = new SimpleIntegerProperty (0);
        this.paragraphCountProp = new SimpleIntegerProperty (0);
        //this.standardPageCount);
        this.standardPageCountProp = new SimpleIntegerProperty (0);
        this.readabilityIndicesProp = new SimpleObjectProperty<> (null);
        this.spellingErrorCountProp = new SimpleIntegerProperty (0);
        this.readabilityIndicesProp.setValue (new ReadabilityIndices ());
        this.problemFinderProblemsCountProp = new SimpleIntegerProperty (0);
        //this.paragraphCount);

    }

    public ChapterCounts (String t)
    {

        this ();
        if (t != null)
        {

            TextIterator ti = new TextIterator (t);
            this.setWordCount (ti.getWordCount ());
            this.setSentenceCount (ti.getSentenceCount ());
            this.setParagraphCount (ti.getParagraphCount ());
            this.wordFrequency = ti.getWordFrequency ();

        }

    }

    public void setProblemFinderProblemsCount (int v)
    {

        this.problemFinderProblemsCount = v;

        UIUtils.runLater (() ->
        {

            this.problemFinderProblemsCountProp.setValue (v);

        });

    }

    public IntegerProperty problemFinderProblemsCountProperty ()
    {

        return this.problemFinderProblemsCountProp;

    }

    public int getProblemFinderProblemsCount ()
    {

        return this.problemFinderProblemsCount;

    }

    public void setSpellingErrorCount (int v)
    {

        this.spellingErrorCount = v;

        UIUtils.runLater (() ->
        {

            this.spellingErrorCountProp.setValue (v);

        });

    }

    public int getSpellingErrorCount ()
    {

        return this.spellingErrorCount;

    }

    public IntegerProperty spellingErrorCountProp ()
    {

        return this.spellingErrorCountProp;

    }

    public void setReadabilityIndices (ReadabilityIndices ri)
    {

        UIUtils.runLater (() ->
        {

            this.readabilityIndicesProp.setValue (ri);

        });

    }

    public ReadabilityIndices getReadabilityIndices ()
    {

        return this.readabilityIndicesProp.getValue ();

    }

    public ObjectProperty<ReadabilityIndices> readabilityIndicesProperty ()
    {

        return this.readabilityIndicesProp;

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

    public int getParagraphCount ()
    {

        return this.paragraphCount;

    }

    public IntegerProperty paragraphCountProperty ()
    {

        return this.paragraphCountProp;

    }

    public void setParagraphCount (int v)
    {

        this.paragraphCount = v;
        UIUtils.runLater (() ->
        {

            this.paragraphCountProp.setValue (v);

        });

    }

    public void add (ChapterCounts c)
    {

        //this.chapter = null;
        //this.wordCount += c.wordCount;
        this.setWordCount (this.getWordCount () + c.getWordCount ());
        //this.sentenceCount += c.sentenceCount;
        this.setSentenceCount (this.getSentenceCount () + c.getSentenceCount ());
        //this.standardPageCount += c.standardPageCount;
        this.setStandardPageCount (this.getStandardPageCount () + c.getStandardPageCount ());
        this.setSpellingErrorCount (this.getSpellingErrorCount () + c.getSpellingErrorCount ());

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
