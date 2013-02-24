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
import com.quollwriter.ui.panels.*;

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
                                                "Click to show the timer",
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
                                                                                  _this.showButton);

                                                    }
                                                });

        java.util.List<JButton> buts = new java.util.ArrayList ();
        buts.add (this.showButton);
        
        this.add (UIUtils.createButtonBar (buts));
        
        this.progressWrapper = new Box (BoxLayout.X_AXIS);
        this.progressWrapper.add (this.progress);
        
        this.progressWrapper.setBorder (new EmptyBorder (5, 5, 5, 5));
        this.progressWrapper.setVisible (false);
        
        this.add (this.progressWrapper);

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

        builder.addLabel ("and/or",
                          cc.xy (3,
                                 1));

        this.mins = WarmupPromptSelect.getTimeOptions ();

        builder.add (this.mins,
                     cc.xy (5,
                            1));

        JButton but = new JButton (Environment.getIcon (Constants.PLAY_ICON_NAME,
                                                        Constants.ICON_MENU));
        but.setToolTipText ("Click to start the timer");
        UIUtils.setAsButton2 (but);

        but.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.timer.addTimerListener (_this);

                _this.timer.start (WarmupPromptSelect.getMinsCount (_this.mins),
                                   WarmupPromptSelect.getWordCount (_this.words));
                      
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

        this.popup = UIUtils.createPopup ("Start the timer",
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

        this.timer.removeTimerListener (this);
        
        final WordCountTimerBox _this = this;
        
        this.progressWrapper.setVisible (false);
        
        this.showButton.setVisible (true);

        String t = null;
        String title = null;
    
        if (ev.getMinutePercentage () > ev.getWordPercentage ())
        {

            t = "You have completed " + ev.getMinuteCount () + " minutes of writing.  Congratulations!";

            title = "Word count reached";
            
        } else {

            t = "You have written " + ev.getWordCount () + " words.  Well done!";

            title = "Time is up";
            
        }

        JLabel l = new JLabel (t);
        l.setBorder (new EmptyBorder (10,
                                      10,
                                      10,
                                      10));
        l.setToolTipText ("Click to close this popup");
        
        QPopup p = UIUtils.createPopup (title,
                                        null,
                                        l,
                                        true,
                                        null);
        
        p.hideIn (10, true);
        
        this.parent.showPopupAt (p,
                                 new Point (10,
                                            10));
        
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

        String rem = "";

        int minsRemaining = this.timer.getMinutesRemaining ();
        
        if (minsRemaining == 0)
        {

            rem = "Less than 1 minute";

        } else {

            rem = minsRemaining + " minute" + ((minsRemaining > 1) ? "s" : "");

        }

        int wp = 0;

        int wc = 0;

        int wordsRemaining = this.timer.getWordsRemaining ();
        
        if (wordsRemaining > 0)
        {

            if (rem.length () > 0)
            {
                
                rem += ", ";
                
            }
        
            rem += wordsRemaining + " words";

        }

        this.progress.setToolTipText (rem + " remaining");

        this.progress.setValue (this.timer.getPercentComplete ());
        
    }
    
    public void close ()
    {
        
        this.timer.removeTimerListener (this);
        
    }
    
}
