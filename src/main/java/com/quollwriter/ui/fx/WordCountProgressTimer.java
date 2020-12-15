package com.quollwriter.ui.fx;

import java.util.*;
import java.util.concurrent.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class WordCountProgressTimer
{

    private static final double MIN_VALUE = 0.0001d;

    private int                                    initWordCount = 0;
    private long startTime = 0;
    private int                                    wordCount = 0;
    private int                                    minsCount = 0;
    private int percentComplete = 0;
    private int wordsRemaining = 0;
    private int minsRemaining = 0;
    private AbstractProjectViewer viewer = null;
    private int millisElapsed = 0;
    private int startCount = 0;

    private int ticks = 0;
    private int totalWords = 0;
    private int startWords = 0;

    private IntegerProperty progressProp = null;
    private ScheduledFuture timer = null;
    private ProgressBar progress = null;
    private boolean paused = false;

    public WordCountProgressTimer (AbstractProjectViewer pv,
                                   int                   mins,
                                   int                   words,
                                   ProgressBar           progress)
    {

        this.viewer = pv;
        this.wordCount = words;
        this.minsCount = mins;
        this.progress = progress;

        this.progressProp = new SimpleIntegerProperty (0);

        this.startTimer ();

    }

    public IntegerProperty progressProperty ()
    {

        return this.progressProp;

    }

    public void unpauseTimer ()
    {

        this.paused = false;
        //this.startTimer ();

    }

    public void pauseTimer ()
    {

        this.paused = true;

    }

    private void startTimer ()
    {

        if (this.timer != null)
        {

            this.timer.cancel (true);
            this.timer = null;

        }

        this.startTime = System.currentTimeMillis ();
        this.percentComplete = 0;
        this.wordsRemaining = this.wordCount;
        this.minsRemaining = this.minsCount;

        this.initWordCount = this.viewer.getAllChapterCounts ().getWordCount ();
        this.startWords = this.viewer.getAllChapterCounts ().getWordCount ();

        this.progress.setProgress (MIN_VALUE);
        this.progressProp.setValue (MIN_VALUE);

        this.timer = viewer.schedule (() ->
        {

            if (this.paused)
            {

                return;

            }

            this.ticks++;
            ChapterCounts cc = this.viewer.getAllChapterCounts ();

            if (cc != null)
            {

                this.totalWords += cc.getWordCount () - this.startWords;

            }

            this.updateTimer ();

            if (cc != null)
            {

                this.startWords = cc.getWordCount ();

            }

        },
        5 * Constants.SEC_IN_MILLIS,
        5 * Constants.SEC_IN_MILLIS);

    }

    public void stopTimer ()
    {

        this.timer.cancel (true);
        this.progress.setProgress (0);
        this.ticks = 0;

    }

    private void updateTimer ()
    {

        double minsPerc = 0;
        double wordsPerc = 0;

        StringBuilder b = new StringBuilder ();

        if (this.minsCount > 0)
        {

            double elapsedMillis = this.ticks * 5 * Constants.SEC_IN_MILLIS;

            double minsMillis = this.minsCount * 60 * Constants.SEC_IN_MILLIS;

            minsPerc = (elapsedMillis / minsMillis);

            int rem = (int) (minsMillis - elapsedMillis) / (60 * Constants.SEC_IN_MILLIS);

            this.minsRemaining = rem;
            StringProperty minsVal = null;

            if (rem <= 1)
            {

                minsVal = getUILanguageStringProperty (LanguageStrings.timer,remaining,time,less1min);

            } else {

                minsVal = getUILanguageStringProperty (Arrays.asList (LanguageStrings.timer,remaining,time,over1min),
                                                       Environment.formatNumber (rem));

            }

            b.append (minsVal.getValue ());

        }

        if (this.wordCount > 0)
        {

            wordsPerc = this.totalWords / this.wordCount;
            this.wordsRemaining = this.wordCount - this.totalWords;

            StringProperty wordsVal = getUILanguageStringProperty (Arrays.asList (LanguageStrings.timer,remaining,words),
                                                                   Environment.formatNumber ((this.wordCount - this.totalWords)));

            if (b.length () > 0)
            {

                b.append ("\n");

            }

            b.append (wordsVal.getValue ());

        }

        double perc = Math.max (minsPerc,
                                wordsPerc);

        if (perc < MIN_VALUE)
        {

            perc = MIN_VALUE;

        }
System.out.println ("PERC: " + perc);
        this.progress.setProgress (perc);

        UIUtils.setTooltip (this.progress,
                            new SimpleStringProperty (b.toString ()));

        //this.progressProp.setValue (this.percentComplete);

        if (perc >= 1)
        {

            this.timer.cancel (true);

            this.viewer.fireProjectEvent (ProjectEvent.Type.warmup,
                                          ProjectEvent.Action.timereached);
            this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                          ProjectEvent.Action.timereached);

        }


            if (true)
            {
                return;
            }

        int mp = 0;

        int remM = 0;

        if (this.minsCount > 0)
        {

            long diff = System.currentTimeMillis () - this.startTime;

            int ms = (int) (diff / (60 * 1000));

            if (ms > 0)
            {

                mp = (int) (((float) ms / (float) this.minsCount) * 100);

            }

            this.minsRemaining = this.minsCount - ms;

        }

        int wp = 0;

        int wc = 0;

        if (this.wordCount > 0)
        {

            // Get the word count.
            ChapterCounts cc = this.viewer.getAllChapterCounts ();

            // Can happen if shutting down.
            if (cc == null)
            {

                return;

            }

            wc = cc.getWordCount ();

            wc -= this.initWordCount;

            if (wc > 0)
            {

                wp = (int) (((float) wc / (float) this.wordCount) * 100);

            }

            this.wordsRemaining = this.wordCount - wc;

        }

        int max = Math.max (mp,
                            wp);

        this.percentComplete = max;

        double v = (double) this.percentComplete / 100d;

        if (v < MIN_VALUE)
        {

            v = MIN_VALUE;

        }

        this.progress.setProgress (v);

        this.progressProp.setValue (this.percentComplete);

        if (max >= 100)
        {

            this.timer.cancel (true);

            this.viewer.fireProjectEvent (ProjectEvent.Type.warmup,
                                          ProjectEvent.Action.timereached);
            this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                          ProjectEvent.Action.timereached);

        }

    }

    public int getStartCount ()
    {

        return this.startCount;

    }

    public int getPercentComplete ()
    {

        return this.percentComplete;

    }

    public int getWordsRemaining ()
    {

        return this.wordsRemaining;

    }

    public int getMinutesRemaining ()
    {

        return this.minsRemaining;

    }

    public int getMinutes ()
    {

        return this.minsCount;

    }

    public int getWords ()
    {

        return this.wordCount;

    }

    public void stop ()
    {

        if (this.timer != null)
        {

            this.timer.cancel (true);
            this.timer = null;

        }

    }

    public void start (int mins,
                       int words)
    {

        this.wordCount = words;
        this.minsCount = mins;

        this.start ();

    }

    public void start ()
    {

        this.startCount++;

        this.percentComplete = 0;
        this.wordsRemaining = this.wordCount;
        this.minsRemaining = this.minsCount;

        this.initWordCount = this.viewer.getAllChapterCounts ().getWordCount ();

        this.startTime = System.currentTimeMillis ();

        // Start the thread that monitors progress.
        this.startTimer ();

        // Only fire in full screen mode.
        if (this.viewer.isInFullScreenMode ())
        {

            this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                          (this.startCount > 1 ? ProjectEvent.Action.timerrestart : ProjectEvent.Action.timerstarted));

        }

    }

}
