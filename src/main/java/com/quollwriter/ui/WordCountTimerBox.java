package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;

import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.panels.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class WordCountTimerBox extends Box implements WordCountTimerListener
{

    private JButton                                showButton = null;
    private QPopup                                 popup = null;
    private JProgressBar                           progress = null;
    private AbstractEditorPanel                    editor = null;
    private JComboBox                              words = null;
    private JComboBox                              mins = null;
    private PopupsSupported                        parent = null;
    private WordCountTimer                         timer = null;
    private Box progressWrapper = null;

    public WordCountTimerBox (PopupsSupported     parent,
                              int                 iconType,
                              WordCountTimer      timer)
    {

        super (BoxLayout.X_AXIS);

        final WordCountTimerBox _this = this;

        this.parent = parent;

        this.progress = new JProgressBar ();

        this.progress.setMaximumSize (new Dimension (75,
                                                     this.progress.getPreferredSize ().height));
        this.progress.setPreferredSize (new Dimension (75,
                                                       this.progress.getPreferredSize ().height));

        //this.progress.setBorderPainted (false);
        this.timer = timer;

        this.timer.addTimerListener (this);

        // This is all kinds of wrong, but it's the ONLY way to prevent a nasty
        // looking progress bar from being drawn.  The metal ui respects the
        // foreground and border settings, the windows ui one does not (jgoodies doesn't provide a ui).
        /*
        this.progress.setUI (new javax.swing.plaf.metal.MetalProgressBarUI ()
            {

                public int getCellSpacing ()
                {

                    return 0;

                }

            });
*/

        this.showButton = UIUtils.createButton ("timer",
                                                iconType,
                                                getUIString (LanguageStrings.timer,
                                                             LanguageStrings.buttons,
                                                             LanguageStrings.show,
                                                             LanguageStrings.tooltip),
                                                //"Click to show the timer",
                                                new ActionAdapter ()
                                                {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        if (_this.popup == null)
                                                        {

                                                            _this.createPopup ();

                                                        }

                                                        if (_this.popup.isVisible ())
                                                        {

                                                            _this.popup.setVisible (false);

                                                            return;

                                                        }

                                                        _this.parent.showPopupAt (_this.popup,
                                                                                  _this.showButton,
                                                                                  false);

                                                    }
                                                });

        this.add (UIUtils.createButtonBar (Arrays.asList (this.showButton)));

        this.progressWrapper = new Box (BoxLayout.X_AXIS);
        this.progressWrapper.add (this.progress);

        this.progressWrapper.setBorder (UIUtils.createPadding (5, 5, 5, 5));
        this.progressWrapper.setVisible (false);

        this.add (this.progressWrapper);

        this.progress.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void fillPopup (JPopupMenu menu,
                                   MouseEvent ev)
            {

                menu.add (UIUtils.createMenuItem (getUIString (LanguageStrings.timer,
                                                               LanguageStrings.popupmenu,
                                                               LanguageStrings.items,
                                                               LanguageStrings.stop),
                                                    //"Stop",
                                                  Constants.CLOSE_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                      public void actionPerformed (ActionEvent ev)
                                                      {

                                                           _this.timer.stop ();

                                                           _this.timerFinished (null);

                                                      }

                                                  }));

            }

        });

    }

    public void setBarHeight (int h)
    {

        this.progress.setMaximumSize (new Dimension (75,
                                                     h));
        this.progress.setPreferredSize (new Dimension (75,
                                                       h));

    }

    public JButton getShowButton ()
    {

        return this.showButton;

    }

    private void createPopup ()
    {

        final WordCountTimerBox _this = this;

        FormLayout fl = new FormLayout ("p, 6px, p, 6px, p, 6px, p",
                                        "p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        this.words = WarmupPromptSelect.getWordsOptions ();

        builder.add (this.words,
                     cc.xy (1,
                            1));

        builder.addLabel (getUIString (LanguageStrings.timer,
                                       LanguageStrings.labels,
                                       LanguageStrings.andor),
                        //"and/or",
                          cc.xy (3,
                                 1));

        this.mins = WarmupPromptSelect.getTimeOptions ();

        builder.add (this.mins,
                     cc.xy (5,
                            1));

        JButton but = new JButton (Environment.getIcon (Constants.PLAY_ICON_NAME,
                                                        Constants.ICON_MENU));
        but.setToolTipText (getUIString (LanguageStrings.timer,
                                         LanguageStrings.buttons,
                                         LanguageStrings.start,
                                         LanguageStrings.tooltip));
        //"Click to start the timer");
        UIUtils.setAsButton2 (but);

        but.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                int mins = WarmupPromptSelect.getMinsCount (_this.mins);
                int words = WarmupPromptSelect.getWordCount (_this.words);

                if ((mins == 0)
                    &&
                    (words == 0)
                   )
                {

                    UIUtils.showErrorMessage (_this.parent,
                                              getUIString (LanguageStrings.timer,
                                                           LanguageStrings.errors,
                                                           LanguageStrings.invalidstate));
                                              //"o_O  The timer can't be unlimited for both time and words.");

                    return;

                }

                _this.timer.addTimerListener (_this);

                _this.timer.start (mins,
                                   words);

                _this.popup.setVisible (false);

            }

        });

        builder.add (but,
                     cc.xy (7,
                            1));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        p.setBorder (new EmptyBorder (5,
                                      5,
                                      5,
                                      5));

        this.popup = UIUtils.createPopup (getUIString (LanguageStrings.timer,
                                                       LanguageStrings.popup,
                                                       LanguageStrings.title),
                                        //"Start the timer",
                                          "timer",
                                          p,
                                          true,
                                          null);

        this.popup.setVisible (false);

    }

    public boolean isPopupVisible ()
    {

        if (this.popup == null)
        {

            return false;

        }

        return this.popup.isVisible ();

    }

    public void timerFinished (WordCountTimerEvent ev)
    {

        //this.timer.removeTimerListener (this);

        final WordCountTimerBox _this = this;

        this.progressWrapper.setVisible (false);

        this.showButton.setVisible (true);

        String t = null;
        String title = null;

        if (ev != null)
        {

            if (ev.getWordPercentage () >= 100)
            {

                t = String.format (getUIString (LanguageStrings.timer,
                                                LanguageStrings.complete,
                                                LanguageStrings.words,
                                                LanguageStrings.popup,
                                                LanguageStrings.text),
                                   Environment.formatNumber (ev.getWordCount ()));
                //t = "You have completed " + ev.getMinuteCount () + " minutes of writing.  Congratulations!";

                title = getUIString (LanguageStrings.timer,
                                     LanguageStrings.complete,
                                     LanguageStrings.words,
                                     LanguageStrings.popup,
                                     LanguageStrings.title);
                //"Word count reached";

            } else {

                t = String.format (getUIString (LanguageStrings.timer,
                                                LanguageStrings.complete,
                                                LanguageStrings.time,
                                                LanguageStrings.popup,
                                                LanguageStrings.text),
                                   Environment.formatNumber (ev.getMinuteCount ()));
                //"You have written " + ev.getWordCount () + " words.  Well done!";

                title = getUIString (LanguageStrings.timer,
                                     LanguageStrings.complete,
                                     LanguageStrings.time,
                                     LanguageStrings.popup,
                                     LanguageStrings.title);
                //"Time is up";

            }

            JLabel l = new JLabel (t);
            l.setBorder (UIUtils.createPadding (10, 10, 10, 10));
            l.setToolTipText (getUIString (actions,clicktoclose));
            //"Click to close this popup");

            final QPopup p = UIUtils.createPopup (title,
                                                  Constants.INFO_ICON_NAME,
                                                  l,
                                                  true,
                                                  null);

            p.hideIn (30, true);

            l.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {

                    p.removeFromParent ();

                }

            });

            this.parent.showPopupAt (p,
                                     new Point (10,
                                                10),
                                     true);

        }

        this.validate ();

        this.repaint ();

    }

    public void timerStarted (WordCountTimerEvent ev)
    {

        this.progress.setValue (0);
        this.progress.setForeground (UIUtils.getColor ("#516CA3"));
        this.progress.setToolTipText ("");

        this.progressWrapper.setVisible (true);

        this.showButton.setVisible (false);

        this.validate ();

        this.repaint ();

    }

    public void timerUpdated (WordCountTimerEvent ev)
    {

        this.progressWrapper.setVisible (true);

        this.showButton.setVisible (false);

        int mp = 0;

        int remM = 0;

        String trem = "";
        String wrem = "";

        int minsRemaining = this.timer.getMinutesRemaining ();

        if (minsRemaining == 0)
        {

            trem = getUIString (LanguageStrings.timer,
                                LanguageStrings.remaining,
                                LanguageStrings.time,
                                LanguageStrings.less1min);
                //"Less than 1 minute";

        } else {

            trem = String.format (getUIString (LanguageStrings.timer,
                                               LanguageStrings.remaining,
                                               LanguageStrings.time,
                                               LanguageStrings.over1min),
                                  Environment.formatNumber (minsRemaining));
                               //minsRemaining + " minute" + ((minsRemaining > 1) ? "s" : "");

        }

        int wp = 0;

        int wc = 0;

        int wordsRemaining = this.timer.getWordsRemaining ();

        if (wordsRemaining > 0)
        {
/*
            if (rem.length () > 0)
            {

                rem += ", ";

            }
*/
            wrem = String.format (getUIString (LanguageStrings.timer,
                                               LanguageStrings.remaining,
                                               LanguageStrings.words),
                                  Environment.formatNumber (wordsRemaining));

            //rem += wordsRemaining + " words";

        }

        this.progress.setToolTipText (trem + "  " + wrem);
        //rem + " remaining");

        this.progress.setValue (this.timer.getPercentComplete ());

    }

    public void close ()
    {

        this.timer.removeTimerListener (this);

    }

}
