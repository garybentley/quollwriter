package com.quollwriter.ui.fx;

import java.util.concurrent.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

public class WordCountProgressTimer
{

    private int                                    initWordCount = 0;
    private long startTime = 0;
    private int                                    wordCount = 0;
    private int                                    minsCount = 0;
    private int percentComplete = 0;
    private int wordsRemaining = 0;
    private int minsRemaining = 0;
    private AbstractProjectViewer viewer = null;

    private int startCount = 0;

    private IntegerProperty progressProp = null;
    private ScheduledFuture timer = null;
    private ProgressBar progress = null;

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

        this.progress.setProgress (0);
        this.progressProp.setValue (0);

        this.timer = viewer.schedule (() ->
        {

            this.updateTimer ();

        },
        5 * Constants.SEC_IN_MILLIS,
        5 * Constants.SEC_IN_MILLIS);

    }

    private void updateTimer ()
    {

        int mp = 0;

        int remM = 0;

        if (this.minsCount > 0)
        {

            long diff = System.currentTimeMillis () - this.startTime;

            int ms = (int) (diff / (60 * 1000));

            if (ms > 0)
            {

                mp = (int) (((float) ms / (float) this.minsCount) * 100);
System.out.println ("MP: " + mp);
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
System.out.println ("WP: " + wp);
            }

            this.wordsRemaining = this.wordCount - wc;

        }

        int max = Math.max (mp,
                            wp);

        this.percentComplete = max;
System.out.println ("PERC: " + this.percentComplete);
        System.out.println ("SETTING: " + ((double) this.percentComplete / 100d));
        this.progress.setProgress ((double) this.percentComplete / 100d);

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
