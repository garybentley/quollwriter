package com.quollwriter.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Set;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.FormItem;

public class WordCountsSideBar extends AbstractSideBar<AbstractProjectViewer>
{

    private final String COL_SPEC = "right:max(80px;p), 6px, p:grow";

    private JLabel sessionWordCount = null;
    private JLabel chapterWordCount = null;
    private JLabel chapterPages = null;
    private JLabel allChaptersWordCount = null;
    private JLabel allChaptersPages = null;
    private Timer timer = null;
    private AccordionItem chapterItem = null;
    private AccordionItem selectedItems = null;
    private JComponent chapterSparkLine = null;
    private JLabel chapterSparkLineLabel = null;
    private JLabel selectedWordCount = null;
    private JLabel chapterFleschKincaid = null;
    private JLabel chapterGunningFog = null;
    private JLabel chapterFleschReadingEase = null;
    private JLabel allChaptersFleschKincaid = null;
    private JLabel allChaptersGunningFog = null;
    private JLabel allChaptersFleschReadingEase = null;
    private JLabel selectedFleschKincaid = null;
    private JLabel selectedFleschReadingEase = null;
    private JLabel selectedGunningFog = null;
    private JComponent selectedReadability = null;
    private JComponent allReadability = null;
    private JComponent chapterReadability = null;
    private JComponent selectedReadabilityHeader = null;
    private JComponent allReadabilityHeader = null;
    private JComponent chapterReadabilityHeader = null;
    private JLabel editPointWordCount = null;
    private Box chapterEditPointBox = null;
    private Box allChaptersEditPointBox = null;
    private JLabel allEditPointWordCount = null;
    private JLabel allChaptersEditCount = null;
    
    public WordCountsSideBar (AbstractProjectViewer v)
    {
        
        super (v);
        
    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    /**
     * Start the timer and call {@link update()} later.
     */
    @Override
    public void onShow ()
    {
        
        final WordCountsSideBar _this = this;
        
        // Start the timer.
        this.timer.start ();
        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.update ();
                
            }
            
        });

    }
  
    @Override
    public void onHide ()
    {
        
    }
    
    /**
     * Stop the timer, we don't set the timer to null since {@link removeOnClose()} is false.
     */
    @Override
    public void onClose ()
    {
        
        // Pause the timer.
        this.timer.stop ();
        
        this.timer = null;
                    
    }
    
    /**
     * Always return false, we want to keep the counts around since it's a labour intensive thing.
     *
     * @returns Always false.
     */
    public boolean removeOnClose ()
    {
        
        return false;
        
    }

    public String getIconType ()
    {
        
        return Constants.WORDCOUNT_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Word Counts";
        
    }
    
    public void panelShown (MainPanelEvent ev)
    {
                
        if (ev.getPanel () instanceof AbstractEditorPanel)
        {

            this.chapterItem.setVisible (true);
            this.chapterSparkLine.removeAll ();
            
        } else {
            
            this.chapterItem.setVisible (false);
            
        }

    }
        
    private void update ()
    {
        
        if (!this.isVisible ())
        {
            
            return;
            
        }
    
        if (this.timer == null)
        {
            
            // Probably closing down.
            return;
            
        }
        
        ChapterCounts achc = this.viewer.getAllChapterCounts (true);
        
        // When shutting down we may get a null back, just return.
        if (achc == null)
        {
            
            this.timer.stop ();
                        
            this.timer = null;
                        
            return;
            
        }
        
        this.sessionWordCount.setText (Environment.formatNumber ((achc.wordCount - this.viewer.getStartWordCounts ().wordCount)));
        
        final Chapter c = this.viewer.getChapterCurrentlyEdited ();
        
        if (c != null)
        {

            AbstractEditorPanel qep = this.viewer.getEditorForChapter (c);

            String sel = qep.getEditor ().getSelectedText ();

            this.selectedItems.setVisible (false);
            this.selectedReadability.setVisible (false);
            this.selectedReadabilityHeader.setVisible (false);
            
            if (!sel.equals (""))
            {
                
                ChapterCounts sc = new ChapterCounts (sel);
                
                this.selectedWordCount.setText (Environment.formatNumber (sc.wordCount));
                
                this.selectedItems.setVisible (true);
                
                if ((sc.wordCount > Constants.MIN_READABILITY_WORD_COUNT)
                    &&
                    (this.viewer.isLanguageEnglish ())
                   )
                {
                    
                    // Show the readability.
                    this.selectedReadability.setVisible (true);
                    this.selectedReadabilityHeader.setVisible (true);
                                        
                    ReadabilityIndices ri = this.viewer.getReadabilityIndices (sel);
            
                    this.selectedFleschKincaid.setText (Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ())));
            
                    this.selectedGunningFog.setText (Environment.formatNumber (Math.round (ri.getGunningFogIndex ())));
            
                    this.selectedFleschReadingEase.setText (Environment.formatNumber (Math.round (ri.getFleschReadingEase ())));
                    
                }
                
            } 

            ChapterCounts chc = this.viewer.getChapterCounts (c,
                                                              true);
            
            this.chapterEditPointBox.setVisible (false);

            if (c.getEditPosition () > 0)
            {

                // Get the text.
                String editText = qep.getEditor ().getText ().substring (0, c.getEditPosition ());

                if (editText.trim ().length () > 0)
                {
                
                    ChapterCounts sc = new ChapterCounts (editText);
                    
                    this.editPointWordCount.setText (Environment.formatNumber (sc.wordCount) + " / " + Environment.formatNumber (Environment.getPercent (sc.wordCount, chc.wordCount)) + "%");
                
                    this.chapterEditPointBox.setVisible (true);

                }
                
            }
                        
            this.chapterWordCount.setText (Environment.formatNumber (chc.wordCount) + " / " + Environment.formatNumber (Environment.getPercent (chc.wordCount, achc.wordCount)) + "%");
            
            this.chapterPages.setText (Environment.formatNumber (chc.a4PageCount));
            
            this.chapterReadability.setVisible (false);
            this.chapterReadabilityHeader.setVisible (false);
            
            if ((this.viewer.isLanguageEnglish ())
                &&
                (chc.wordCount >= Constants.MIN_READABILITY_WORD_COUNT)
               )
            {
            
                this.chapterReadability.setVisible (true);
                this.chapterReadabilityHeader.setVisible (true);
            
                ReadabilityIndices ri = this.viewer.getReadabilityIndices (c);

                this.chapterFleschKincaid.setText (Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ())));
                
                this.chapterGunningFog.setText (Environment.formatNumber (Math.round (ri.getGunningFogIndex ())));
                
                this.chapterFleschReadingEase.setText (Environment.formatNumber (Math.round (ri.getFleschReadingEase ())));

            }
            
            this.chapterItem.setTitle (qep.getTitle ());

            this.chapterItem.setVisible (true);
            
            if (this.chapterSparkLine.getComponents ().length == 0)
            {
            
                try
                {
        
                    ChapterDataHandler dh = (ChapterDataHandler) this.viewer.getDataHandler (Chapter.class);
        
                    // TODO: Find a better way of handling this.
                    if (dh != null)
                    {

                        org.jfree.data.time.TimeSeries ts = new org.jfree.data.time.TimeSeries (c.getName ());
            
                        int diff = 0;
            
                        int min = Integer.MAX_VALUE;
                        int max = Integer.MIN_VALUE;
                    
                        // Get all the word counts for the chapter.
                        java.util.List<WordCount> wordCounts = dh.getWordCounts (c,
                                                                                 -7);
    
                        if (wordCounts.size () == 0)
                        {
                            
                            wordCounts.add (new WordCount (chc.wordCount,
                                                           null,
                                                           Environment.zeroTimeFieldsForDate (new Date ())));
                            
                        }
    
                        // Expand the dates back if necessary.
                        if (wordCounts.size () == 1)
                        {
                            
                            // Get a date for 7 days ago.
                            Date d = new Date (System.currentTimeMillis () - (7 * 24 * 60 * 60 * 1000));
                            
                            wordCounts.add (0,
                                            new WordCount (chc.wordCount,
                                                           null,
                                                           Environment.zeroTimeFieldsForDate (d)));
                            
                        }
                        
                        for (WordCount wc : wordCounts)
                        {
    
                            int count = wc.getCount ();
    
                            min = Math.min (min,
                                            count);
                            max = Math.max (max,
                                            count);
    
                            try
                            {
    
                                ts.add (new org.jfree.data.time.Day (wc.getEnd ()),
                                        count);
    
                            } catch (Exception e) {
                                
                                // Ignore, trying to add a duplicate day.
                                
                            }
    
                        }
            
                        diff = max - min;
    
                        int wordDiff = diff;
                        
                        if (diff == 0)
                        {
    
                            diff = 100;
    
                        }
                    
                        if ((min < Integer.MAX_VALUE) ||
                            (max > Integer.MIN_VALUE))
                        {
                                    
                            this.chapterSparkLineLabel.setText (String.format ("past 7 days, <b>%s%s</b> words",
                                                                               (wordDiff == 0 ? "" : (wordDiff > 0 ? "+" : "")),
                                                                               Environment.formatNumber (wordDiff)));
                                    
                            org.jfree.chart.ChartPanel cp = new org.jfree.chart.ChartPanel (UIUtils.createSparkLine (ts,
                                                                                                                     max + (diff / 2),
                                                                                                                     min - (diff / 2)));
            
                            cp.setToolTipText ("Word count activity for the past 7 days");
                            
                            cp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                              16));
                            cp.setPreferredSize (new Dimension (60,
                                                                16));
                            this.chapterSparkLine.add (cp);
            
                        }

                    }
        
                } catch (Exception e)
                {
        
                    Environment.logError ("Unable to generate 7 day activity sparkline",
                                          e);
        
                }

            }
            
        } else {
            
            this.chapterItem.setVisible (false);
            
        }
        
        this.allReadability.setVisible (false);
        this.allReadabilityHeader.setVisible (false);
        
        if ((this.viewer.isLanguageEnglish ())
            &&
            (achc.wordCount >= Constants.MIN_READABILITY_WORD_COUNT)
           )
        {
        
            this.allReadability.setVisible (true);
            this.allReadabilityHeader.setVisible (true);
        
            ReadabilityIndices ri = this.viewer.getAllReadabilityIndices ();
                    
            this.allChaptersFleschKincaid.setText (Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ())));
            
            this.allChaptersFleschReadingEase.setText (Environment.formatNumber (Math.round (ri.getFleschReadingEase ())));
            
            this.allChaptersGunningFog.setText (Environment.formatNumber (Math.round (ri.getGunningFogIndex ())));

        }
        
        this.allChaptersWordCount.setText (Environment.formatNumber (achc.wordCount));

        this.allChaptersPages.setText (Environment.formatNumber (achc.a4PageCount));

        this.allChaptersEditPointBox.setVisible (false);
                
        final StringBuilder buf = new StringBuilder ();

        Set<NamedObject> chapters = this.viewer.getProject ().getAllNamedChildObjects (Chapter.class);
        
        int editComplete = 0; 
        
        for (NamedObject n : chapters)
        {

            Chapter nc = (Chapter) n;
                    
            if (nc.getEditPosition () > 0)
            {
                                
                if (buf.length () > 0)
                {
                    
                    buf.append (" ");
                    
                }
        
                AbstractEditorPanel pan = this.viewer.getEditorForChapter (nc);        
                
                String t = null;
                
                if (pan != null)
                {
                    
                    t = pan.getEditor ().getText ();
                    
                } else {
                    
                    t = nc.getChapterText ();
                    
                }
                    
                if (nc.getEditPosition () <= t.length ())
                {
                    
                    buf.append (t.substring (0,
                                             nc.getEditPosition ()));
                    
                }

            }

            if (nc.isEditComplete ())
            {
                
                editComplete++;
                
            }
            
        }
                        
        String allEditText = buf.toString ().trim ();
        
        if (buf.length () > 0)
        {
        
            ChapterCounts allc = new ChapterCounts (buf.toString ());

            this.allEditPointWordCount.setText (Environment.formatNumber (allc.wordCount) + " / " + Environment.formatNumber (Environment.getPercent (allc.wordCount, achc.wordCount)) + "%");
            
            this.allChaptersEditCount.setText (Environment.formatNumber (editComplete) + " / " + Environment.formatNumber (Environment.getPercent (editComplete, chapters.size ())) + "%");
            
            this.allChaptersEditPointBox.setVisible (true);
                
        }
        
    }
    
    private JComponent getWords (JLabel     wordCount,
                                 JLabel     pagesCount,
                                 JComponent cp,
                                 JLabel     sparkLineLabel,
                                 int        wordCountDiff,
                                 int        days)
    {

        String cols = COL_SPEC;
        
        String rows = "p, 6px, p, 6px, p";
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();        

        wordCount.setFont (wordCount.getFont ().deriveFont (Font.BOLD));
                           
        b.add (wordCount,
               cc.xy (1, 1));

        b.addLabel ("words",
                    cc.xy (3, 1));

        pagesCount.setFont (pagesCount.getFont ().deriveFont (Font.BOLD));                    
                    
        b.add (pagesCount,
               cc.xy (1, 3));

        b.addLabel ("A4 pages",
                    cc.xy (3, 3));

        String diff = "";
        
        if (wordCountDiff != 0)
        {
            
            diff = ", <b>" + (wordCountDiff > 0 ? "+" : "-") + Environment.formatNumber (wordCountDiff) + "</b> words";    
                    
        }
        
        sparkLineLabel.setText ("past " + days + " days" + diff);
        
        b.add (sparkLineLabel,
               cc.xy (3, 5));
        
        b.add (cp,
               cc.xywh (1, 5, 1, 1));
                    
        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (10, 10, 10, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;
        
    }
    
    private JComponent getReadability (JLabel fleschKincaid,
                                       JLabel fleschReading,
                                       JLabel gunningFog)
    {
        
        String cols = COL_SPEC;
        
        String rows = "p, 6px, p, 6px, p";
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();        
        /*
        b.addLabel ("F-K",
                    cc.xy (1,
                           1));
        b.addLabel ("FR",
                    cc.xy (3,
                           1));
        b.addLabel ("GF",
                    cc.xy (5,
                           1));
        */

        fleschKincaid.setFont (fleschKincaid.getFont ().deriveFont (Font.BOLD));                    

        b.add (fleschKincaid,
               cc.xy (1, 1));
        
        b.addLabel ("Flesch-Kincaid",
                    cc.xy (3, 1));
        
        fleschReading.setFont (fleschReading.getFont ().deriveFont (Font.BOLD));                    

        b.add (fleschReading,
               cc.xy (1, 3));
        
        b.addLabel ("Flesch Reading",
                    cc.xy (3, 3));        
        
        gunningFog.setFont (gunningFog.getFont ().deriveFont (Font.BOLD));                    

        b.add (gunningFog,
               cc.xy (1, 5));

        b.addLabel ("Gunning Fog",
                    cc.xy (3, 5));                       
               
        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (10, 10, 10, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;
        
    }
    
    public List<JComponent> getHeaderControls ()
    {

        final WordCountsSideBar _this = this;
        
        List<JComponent> buts = new ArrayList ();        
        
        JButton b = UIUtils.createButton ("chart",
                                          Constants.ICON_SIDEBAR,
                                          "Click to view the detail",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                    _this.viewer.viewWordCountHistory ();
                                                
                                              }
                                            
                                          });

        buts.add (b);

        return buts;        
        
    }

    public JComponent getContent ()
    {

        final WordCountsSideBar _this = this;    
    
        Box box = new Box (BoxLayout.Y_AXIS);

        box.setMinimumSize (new Dimension (238,
                                           250));
        box.setPreferredSize (new Dimension (238,
                                             250));

        box.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                           Short.MAX_VALUE));
                
        final Chapter c = this.viewer.getChapterCurrentlyEdited ();

        this.sessionWordCount = UIUtils.createInformationLabel (null);
        this.chapterWordCount = UIUtils.createInformationLabel (null);
        this.chapterPages = UIUtils.createInformationLabel (null);
        this.allChaptersWordCount = UIUtils.createInformationLabel (null);
        this.allChaptersPages = UIUtils.createInformationLabel (null);
        this.selectedWordCount = UIUtils.createInformationLabel (null);
        this.chapterSparkLine = new Box (BoxLayout.X_AXIS);
        this.chapterSparkLine.setOpaque (false);
        this.chapterSparkLineLabel = UIUtils.createInformationLabel (null);
        this.selectedFleschKincaid = UIUtils.createInformationLabel (null);
        this.selectedFleschReadingEase = UIUtils.createInformationLabel (null);
        this.selectedGunningFog = UIUtils.createInformationLabel (null);
        this.editPointWordCount = UIUtils.createInformationLabel (null);
        this.allEditPointWordCount = UIUtils.createInformationLabel (null);
        this.allChaptersEditCount = UIUtils.createInformationLabel (null);
                
        List<JComponent> items = new ArrayList ();

        items.add (this.getItem ("words",
                                 this.selectedWordCount));
        
        this.selectedReadabilityHeader = this.createSubHeader ("Readability");
        
        items.add (this.selectedReadabilityHeader);
                
        this.selectedReadability = this.getReadability (this.selectedFleschKincaid,
                                                        this.selectedFleschReadingEase,
                                                        this.selectedGunningFog);
                
        items.add (this.selectedReadability);
                
        this.selectedItems = this.getItems ("Selected text",
                                            Constants.EDIT_ICON_NAME,
                                            items);
                      
        this.selectedItems.setVisible (false);
                      
        box.add (this.selectedItems);                          
        
        items = new ArrayList ();

        items.add (this.getItem ("words",
                                 this.sessionWordCount));
                
        AccordionItem it = this.getItems ("This session",
                                          Constants.CLOCK_ICON_NAME,
                                          items);
                      
        box.add (it);                          
                                   
        items = new ArrayList ();
        
        items.add (this.getWords (this.chapterWordCount,
                                  this.chapterPages,
                                  this.chapterSparkLine,
                                  this.chapterSparkLineLabel,
                                  0,
                                  7));
        
        this.chapterEditPointBox = new Box (BoxLayout.Y_AXIS);
        
        items.add (this.chapterEditPointBox);
        
        this.chapterEditPointBox.add (this.createSubHeader ("Edited"));
        
        this.chapterEditPointBox.add (this.getItem ("words",
                                                    this.editPointWordCount));
                
        //this.chapterEditPointBox.setVisible (false);

        UIUtils.setPadding (this.chapterEditPointBox, 0, 0, 10, 0);
                                                
        this.chapterFleschKincaid = UIUtils.createInformationLabel (null);
        this.chapterFleschReadingEase = UIUtils.createInformationLabel (null);
        this.chapterGunningFog = UIUtils.createInformationLabel (null);
                
        this.chapterReadabilityHeader = this.createSubHeader ("Readability");
                
        items.add (this.chapterReadabilityHeader);
        
        this.chapterReadability = this.getReadability (this.chapterFleschKincaid,
                                                       this.chapterFleschReadingEase,
                                                       this.chapterGunningFog);
        
        items.add (this.chapterReadability);
                
        this.chapterItem = this.getItems ("",
                                          Chapter.OBJECT_TYPE,
                                          items);
               
        if (c == null)
        {
            
            this.chapterItem.setVisible (false);
                      
        }
        
        box.add (this.chapterItem);                          

        items = new ArrayList ();

        JComponent sparkLine = new JPanel ();
        sparkLine.setBorder (null);
        sparkLine.setOpaque (false);
                   
        int wordCountDiff30 = 0;
                                 
        try
        {

            ProjectDataHandler dh = (ProjectDataHandler) this.viewer.getDataHandler (Project.class);

            // TODO: Find a better way of handling this.
            if (dh != null)
            {
                
                org.jfree.data.time.TimeSeries ts = new org.jfree.data.time.TimeSeries ("All");
    
                int diff = 0;
    
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
    
                // Get all the word counts for the project.
                java.util.List<WordCount> wordCounts = dh.getWordCounts (this.viewer.getProject (),
                                                                         -30);
    
                for (WordCount wc : wordCounts)
                {
    
                    int count = wc.getCount ();
    
                    min = Math.min (min,
                                    count);
                    max = Math.max (max,
                                    count);
    
                    ts.add (new org.jfree.data.time.Day (wc.getEnd ()),
                            count);
    
                }
    
                diff = max - min;
    
                if (diff == 0)
                {
    
                    diff = 100;
    
                }
    
                if ((min < Integer.MAX_VALUE) ||
                    (max > Integer.MIN_VALUE))
                {
    
                    wordCountDiff30 = max - min;    
                
                    org.jfree.chart.ChartPanel cp = new org.jfree.chart.ChartPanel (UIUtils.createSparkLine (ts,
                                                                                                             max + (diff / 2),
                                                                                                             min - (diff / 2)));
    
                    cp.setToolTipText ("Word count activity for the past 30 days");
                    cp.setMaximumSize (new Dimension (60,
                                                      16));
                    cp.setPreferredSize (new Dimension (60,
                                                        16));
    
                    sparkLine = cp;
                                                          
                }

            }
                
        } catch (Exception e)
        {

            Environment.logError ("Unable to generate 30 day activity sparkline",
                                  e);

        }

        items.add (this.getWords (this.allChaptersWordCount,
                                  this.allChaptersPages,
                                  sparkLine,
                                  UIUtils.createInformationLabel (null),                                  
                                  wordCountDiff30,
                                  30));        
        
        this.allChaptersFleschKincaid = UIUtils.createInformationLabel (null);
        this.allChaptersFleschReadingEase = UIUtils.createInformationLabel (null);
        this.allChaptersGunningFog = UIUtils.createInformationLabel (null);

        this.allChaptersEditPointBox = new Box (BoxLayout.Y_AXIS);
        
        items.add (this.allChaptersEditPointBox);
        
        this.allChaptersEditPointBox.add (this.createSubHeader ("Edited"));
        
        this.allChaptersEditPointBox.add (this.getItem ("words",
                                                        this.allEditPointWordCount));

        this.allChaptersEditPointBox.add (this.getItem ("{chapters}",
                                                        this.allChaptersEditCount));
                
        this.allChaptersEditPointBox.setVisible (false);

        this.allChaptersEditPointBox.setBorder (new EmptyBorder (0, 0, 10, 0));
        
        this.allReadabilityHeader = this.createSubHeader ("Readability");
        
        items.add (this.allReadabilityHeader);
        
        this.allReadability = this.getReadability (this.allChaptersFleschKincaid,
                                                   this.allChaptersFleschReadingEase,
                                                   this.allChaptersGunningFog);
        
        items.add (this.allReadability);
                                         
        box.add (this.getItems ("All {Chapters}",
                                Book.OBJECT_TYPE,
                                items));
                                    
        final JLabel history = UIUtils.createClickableLabel ("View Detail",
                                                             Environment.getIcon ("chart",
                                                                                  Constants.ICON_MENU));

        UIUtils.setPadding (history, 0, 5, 0, 0);
                                                                                  
        box.add (history);
        box.add (Box.createVerticalStrut (10));
        
        history.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                _this.viewer.viewWordCountHistory ();

            }

        });

        JLabel l = UIUtils.createClickableLabel ("Click to find out more about<br />the Readability indices",
                                                 Environment.getIcon ("help",
                                                                      Constants.ICON_MENU));

        UIUtils.setPadding (l, 0, 5, 0, 0);
                                                                      
        l.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                // Open the url.
                UIUtils.openURL (_this,
                                 "help://chapters/readability");

            }

        });
            
        box.add (l);

        box.add (Box.createVerticalGlue ());
            
        return this.wrapInScrollPane (box);
                    
    }
    
    private JComponent getItem (String     label,
                                JComponent value)
    {
        
        String cols = COL_SPEC;
        
        String rows = "p";
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();        
        
        value.setFont (value.getFont ().deriveFont (Font.BOLD));
                           
        b.add (value,
               cc.xy (1, 1));

        b.addLabel (Environment.replaceObjectNames (label),
                    cc.xy (3, 1));

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (6, 10, 0, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;
        
    }
    
    private AccordionItem getItems (String title,
                                    String iconType,
                                    List<JComponent> items)
    {        

        Box b = new Box (BoxLayout.Y_AXIS);

        for (JComponent c : items)
        {

            c.setAlignmentY (Component.TOP_ALIGNMENT);
        
            b.add (c);
                    
        }

        b.setOpaque (false);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.add (Box.createVerticalGlue ());
        
        AccordionItem it = new AccordionItem (title,
                                              iconType,
                                              b);
                                              
        Header h = it.getHeader ();
                                              
        //h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (12d)).deriveFont (java.awt.Font.PLAIN));

        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));
        
        it.init ();
        
        it.revalidate ();
        
        it.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         it.getPreferredSize ().height));      
        return it;        
        
    }
    
    private JLabel createSubHeader (String title)
    {
        
        JLabel ll = UIUtils.createInformationLabel ("<i>" + Environment.replaceObjectNames (title) + "</i>");        

        UIUtils.setPadding (ll, 0, 10, 0, 0);
        
        return ll;
        
    }
    
    public void init ()
               throws GeneralException
    {

        super.init ();
 
        if (this.timer != null)
        {
            
            return;
            
        }
    
        final WordCountsSideBar _this = this;
                
        this.timer = new Timer (2000,
                                new ActionAdapter ()
                                {
                                   
                                    public void actionPerformed (ActionEvent ev)
                                    {
                                   
                                        _this.update ();
                                        
                                    }
                                    
                                });
                
    }    
    
}