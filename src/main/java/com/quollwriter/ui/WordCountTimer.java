package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.events.*;

public class WordCountTimer
{

    private javax.swing.Timer                      timer = null;
    private int                                    initWordCount = 0;
    private long startTime = 0;
    private int                                    wordCount = 0;
    private int                                    minsCount = 0;
    private java.util.Set<WordCountTimerListener> listeners = new java.util.LinkedHashSet ();
    private int percentComplete = 0;
    private int wordsRemaining = 0;
    private int minsRemaining = 0;
    private AbstractProjectViewer projectViewer = null;

    private int startCount = 0;

    public WordCountTimer (AbstractProjectViewer pv,
                           int                   mins,
                           int                   words)
    {

        this.projectViewer = pv;
        this.wordCount = words;
        this.minsCount = mins;

        final WordCountTimer _this = this;

        this.timer = new javax.swing.Timer (5 * 1000,
                                            new ActionAdapter ()
                                            {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    int mp = 0;

                                                    int remM = 0;

                                                    if (_this.minsCount > 0)
                                                    {

                                                        long diff = System.currentTimeMillis () - _this.startTime;

                                                        int ms = (int) (diff / (60 * 1000));

                                                        if (ms > 0)
                                                        {

                                                            mp = (int) (((float) ms / (float) _this.minsCount) * 100);

                                                        }

                                                        _this.minsRemaining = _this.minsCount - ms;

                                                    }

                                                    int wp = 0;

                                                    int wc = 0;

                                                    if (_this.wordCount > 0)
                                                    {

                                                        // Get the word count.
                                                        ChapterCounts cc = _this.projectViewer.getAllChapterCounts ();

                                                        // Can happen if shutting down.
                                                        if (cc == null)
                                                        {

                                                            return;

                                                        }

                                                        wc = cc.getWordCount ();

                                                        wc -= _this.initWordCount;

                                                        if (wc > 0)
                                                        {

                                                            wp = (int) (((float) wc / (float) _this.wordCount) * 100);

                                                        }

                                                        _this.wordsRemaining = _this.wordCount - wc;

                                                    }

                                                    int max = Math.max (mp,
                                                                        wp);

                                                    _this.percentComplete = max;

                                                    if (max >= 100)
                                                    {

                                                        _this.timer.stop ();

                                                        _this.projectViewer.fireProjectEvent (Warmup.OBJECT_TYPE,
                                                                                              ProjectEvent.TIME_REACHED);
                                                        _this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                                                              ProjectEvent.TIME_REACHED);

                                                        WordCountTimerEvent wev = new WordCountTimerEvent (_this,
                                                                                                           wc,
                                                                                                           wp,
                                                                                                           _this.minsCount,
                                                                                                           mp);

                                                        for (WordCountTimerListener l : new java.util.ArrayList<WordCountTimerListener> (_this.listeners))
                                                        {

                                                            l.timerFinished (wev);

                                                        }

                                                    } else {

                                                        WordCountTimerEvent wev = new WordCountTimerEvent (_this,
                                                                                                           wc,
                                                                                                           wp,
                                                                                                           _this.minsCount,
                                                                                                           mp);

                                                        for (WordCountTimerListener l : _this.listeners)
                                                        {

                                                            l.timerUpdated (wev);

                                                        }

                                                    }

                                                }

                                            });

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

            this.timer.stop ();

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

        final WordCountTimer _this = this;

        this.percentComplete = 0;
        this.wordsRemaining = this.wordCount;
        this.minsRemaining = this.minsCount;

        this.initWordCount = this.projectViewer.getAllChapterCounts ().getWordCount ();

        this.startTime = System.currentTimeMillis ();

        // Start the thread that monitors progress.
        this.timer.start ();

        WordCountTimerEvent wev = new WordCountTimerEvent (_this,
                                                           this.wordCount,
                                                           -1,
                                                           this.minsCount,
                                                           -1);

        for (WordCountTimerListener l : this.listeners)
        {

            l.timerStarted (wev);

        }

        // Only fire in full screen mode.
        if (this.projectViewer.isInFullScreen ())
        {

            this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                 (this.startCount > 1 ? ProjectEvent.TIMER_RESTART : ProjectEvent.TIMER_STARTED));

        }

    }

    public void removeTimerListener (WordCountTimerListener l)
    {

        this.listeners.remove (l);

    }

    public void addTimerListener (WordCountTimerListener l)
    {

        this.listeners.add (l);

    }

}
