package com.quollwriter.data;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.text.*;
import com.quollwriter.*;

public class ChapterCounts implements Stateful
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

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set ("wordcount",
               this.getWordCount ());
        s.set ("sentencecount",
               this.getSentenceCount ());
        s.set ("paragraphcount",
               this.getParagraphCount ());
        s.set ("spellingerrorcount",
               this.getSpellingErrorCount ());
        s.set ("problemfinderproblemscount",
               this.getProblemFinderProblemsCount ());
        s.set ("pagecount",
               this.getStandardPageCount ());
        s.set ("syllablecount",
               this.getReadabilityIndices ().getSyllableCount ());
        s.set ("threesyllablewordcount",
               this.getReadabilityIndices ().getThreeSyllableWordCount ());

        return s;

    }

    @Override
    public void init (State s)
    {

        if (s == null)
        {

            return;

        }

        this.setWordCount (s.getAsInt ("wordcount",
                                       0));
        this.setSentenceCount (s.getAsInt ("sentencecount",
                                           0));
        this.setParagraphCount (s.getAsInt ("paragraphcount",
                                            0));
        this.setStandardPageCount (s.getAsInt ("pagecount",
                                               0));
        this.setSpellingErrorCount (s.getAsInt ("spellingerrorcount",
                                                0));
        this.setProblemFinderProblemsCount (s.getAsInt ("problemfinderproblemscount",
                                                        0));

        int sylc = s.getAsInt ("syllablecount",
                               0);
        int thrsylc = s.getAsInt ("threesyllablewordcount",
                                  0);

        ReadabilityIndices ri = new ReadabilityIndices (this.getWordCount (),
                                                        this.getSentenceCount (),
                                                        thrsylc,
                                                        sylc);

        this.readabilityIndicesProp.getValue ().updateFrom (ri);

    }

    @Override
    public String toString ()
    {

        return String.format ("wordcount: %1$s, sentencecount: %2$s, paragraphcount: %3$s, spellingerrorcount: %4$s, pagecount: %5$s, problemfinderproblemcount: %6$s",
                              Environment.formatNumber (this.wordCount),
                              Environment.formatNumber (this.sentenceCount),
                              Environment.formatNumber (this.paragraphCount),
                              Environment.formatNumber (this.spellingErrorCount),
                              Environment.formatNumber (this.standardPageCount),
                              Environment.formatNumber (this.problemFinderProblemsCount));

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
