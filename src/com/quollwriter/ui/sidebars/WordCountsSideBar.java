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

public class WordCountsSideBar extends AbstractSideBar 
{

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
    
    public WordCountsSideBar (AbstractProjectViewer v)
    {
        
        super (v);
        
    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public void onClose ()
    {
        
        
    }
    
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
        
        ChapterCounts achc = this.projectViewer.getAllChapterCounts (true);
        
        // When shutting down we may get a null back, just return.
        if (achc == null)
        {
            
            this.timer.stop ();
                        
            this.timer = null;
                        
            return;
            
        }
        
        this.sessionWordCount.setText (Environment.formatNumber ((achc.wordCount - this.projectViewer.getStartWordCounts ().wordCount)));
        
        final Chapter c = this.projectViewer.getChapterCurrentlyEdited ();
        
        if (c != null)
        {

            AbstractEditorPanel qep = this.projectViewer.getEditorForChapter (c);

            String sel = qep.getEditor ().getSelectedText ();

            this.selectedItems.setVisible (false);
            this.selectedReadability.setVisible (false);
            
            if (!sel.equals (""))
            {
                
                ChapterCounts sc = UIUtils.getChapterCounts (sel);
                
                this.selectedWordCount.setText (Environment.formatNumber (sc.wordCount));
                
                this.selectedItems.setVisible (true);
                
                if (sc.wordCount > 100)
                {
                    
                    // Show the readability.
                    this.selectedReadability.setVisible (true);
                                        
                    ReadabilityIndices ri = UIUtils.getReadabilityIndices (sel);
            
                    this.selectedFleschKincaid.setText (Environment.formatNumber (ri.getFleschKincaidGradeLevel ()));
            
                    this.selectedGunningFog.setText (Environment.formatNumber (ri.getGunningFogIndex ()));
            
                    this.selectedFleschReadingEase.setText (Environment.formatNumber (ri.getFleschReadingEase ()));
                    
                }
                
            } 
            
            ChapterCounts chc = this.projectViewer.getChapterCounts (c,
                                                                     true);
            
            this.chapterWordCount.setText (Environment.formatNumber (chc.wordCount) + " / " + Environment.formatNumber (Environment.getPercent (chc.wordCount, achc.wordCount)) + "%");
            
            this.chapterPages.setText (Environment.formatNumber (chc.a4PageCount));
            
            ReadabilityIndices ri = this.projectViewer.getReadabilityIndices (c);
          
            this.chapterFleschKincaid.setText (Environment.formatNumber (ri.getFleschKincaidGradeLevel ()));
            
            this.chapterGunningFog.setText (Environment.formatNumber (ri.getGunningFogIndex ()));
            
            this.chapterFleschReadingEase.setText (Environment.formatNumber (ri.getFleschReadingEase ()));
            
            this.chapterItem.setTitle (qep.getTitle ());

            this.chapterItem.setVisible (true);
            
            if (this.chapterSparkLine.getComponents ().length == 0)
            {
            
                try
                {
        
                    ChapterDataHandler dh = (ChapterDataHandler) this.projectViewer.getDataHandler (Chapter.OBJECT_TYPE);
        
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
        
                        String d = "";
                        
                        if (wordDiff != 0)
                        {
                            
                            d = ", <b>" + (wordDiff > 0 ? "+" : "-") + Environment.formatNumber (wordDiff) + "</b> words";    
                                    
                        }
                        
                        this.chapterSparkLineLabel.setText ("<html>past 7 days" + d + "</html>");
                                
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
        
                } catch (Exception e)
                {
        
                    Environment.logError ("Unable to generate 7 day activity sparkline",
                                          e);
        
                }

            }
            
        } else {
            
            this.chapterItem.setVisible (false);
            
        }
        
        ReadabilityIndices ri = this.projectViewer.getAllReadabilityIndices ();
                
        this.allChaptersFleschKincaid.setText (Environment.formatNumber (ri.getFleschKincaidGradeLevel ()));
        
        this.allChaptersFleschReadingEase.setText (Environment.formatNumber (ri.getFleschReadingEase ()));
        
        this.allChaptersGunningFog.setText (Environment.formatNumber (ri.getGunningFogIndex ()));
        
        this.allChaptersWordCount.setText (Environment.formatNumber (achc.wordCount));

        this.allChaptersPages.setText (Environment.formatNumber (achc.a4PageCount));

    }

    private JComponent getItem (String     label,
                                JComponent value)
    {
        
        String cols = "right:max(60px;p), 6px, p:grow";
        
        String rows = "p";
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.setDefaultDialogBorder ();

        CellConstraints cc = new CellConstraints ();        
        
        value.setFont (value.getFont ().deriveFont (Font.BOLD));
                           
        b.add (value,
               cc.xy (1, 1));

        b.addLabel (label,
                    cc.xy (3, 1));

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (10, 10, 10, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;
        
    }
    
    private JComponent getWords (JLabel     wordCount,
                                 JLabel     pagesCount,
                                 JComponent cp,
                                 JLabel     sparkLineLabel,
                                 int        wordCountDiff,
                                 int        days)
    {

        String cols = "right:max(60px;p), 6px, p:grow";
        
        String rows = "p, 6px, p, 6px, p";
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.setDefaultDialogBorder ();

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
        
        sparkLineLabel.setText ("<html>past " + days + " days" + diff + "</html>");
        
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
        
        String cols = "right:max(60px;p), 6px, p:grow";
        
        String rows = "p, 6px, p, 6px, p";
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.setDefaultDialogBorder ();

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
    /*
    private JComponent getItem (String     label,
                                JComponent item)
    {
        
        String cols = "right:70px, 10px, p:grow";

        String rows = "bottom:p";

        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.setDefaultDialogBorder ();

        CellConstraints cc = new CellConstraints ();        
        
        b.addLabel (label,
                    cc.xy (1,
                           1));
        b.add (item,
               cc.xy (3,
                      1));
        
        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
                
        return p;
        
    }
    */
    public List<JButton> getHeaderControls ()
    {

        final WordCountsSideBar _this = this;
        
        List<JButton> buts = new ArrayList ();        
        
        JButton b = UIUtils.createButton ("chart",
                                          Constants.ICON_SIDEBAR,
                                          "Click to view the detail",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                    _this.projectViewer.viewWordCountHistory ();
                                                
                                              }
                                            
                                          });

        buts.add (b);

        return buts;        
        
    }

    public JComponent getContent ()
    {

        final WordCountsSideBar _this = this;    
    
        Box box = new Box (BoxLayout.Y_AXIS);
                
        final Chapter c = this.projectViewer.getChapterCurrentlyEdited ();

        this.sessionWordCount = new JLabel ();
        this.chapterWordCount = new JLabel ();
        this.chapterPages = new JLabel ();
        this.allChaptersWordCount = new JLabel ();        
        this.allChaptersPages = new JLabel ();
        this.selectedWordCount = new JLabel ();
        this.chapterSparkLine = new Box (BoxLayout.X_AXIS);
        this.chapterSparkLine.setOpaque (false);
        this.chapterSparkLineLabel = new JLabel ();
        this.selectedFleschKincaid = new JLabel ();
        this.selectedFleschReadingEase = new JLabel ();
        this.selectedGunningFog = new JLabel ();
                
        List<JComponent> items = new ArrayList ();

        items.add (this.getItem ("words",
                                 this.selectedWordCount));

        JLabel ll = new JLabel ("<html><i>Readability</i></html>");
        
        ll.setBorder (new EmptyBorder (0, 10, 0, 0));
        
        items.add (ll);
                
        this.selectedReadability = this.getReadability (this.selectedFleschKincaid,
                                                        this.selectedFleschReadingEase,
                                                        this.selectedGunningFog);
                
        items.add (this.selectedReadability);
                
        this.selectedItems = this.getItems ("Selected text",
                                            "edit",
                                            items);
                      
        this.selectedItems.setVisible (false);
                      
        box.add (this.selectedItems);                          
        
        items = new ArrayList ();

        items.add (this.getItem ("words",
                                 this.sessionWordCount));
                
        AccordionItem it = this.getItems ("This session",
                                          "clock",
                                          items);
                      
        box.add (it);                          
                           
        items = new ArrayList ();
        
        items.add (this.getWords (this.chapterWordCount,
                                  this.chapterPages,
                                  this.chapterSparkLine,
                                  this.chapterSparkLineLabel,
                                  0,
                                  7));
                
        this.chapterFleschKincaid = new JLabel ();
        this.chapterFleschReadingEase = new JLabel ();
        this.chapterGunningFog = new JLabel ();
        
        ll = new JLabel ("<html><i>Readability</i></html>");
        
        ll.setBorder (new EmptyBorder (0, 10, 0, 0));
        
        items.add (ll);
        
        items.add (this.getReadability (this.chapterFleschKincaid,
                                        this.chapterFleschReadingEase,
                                        this.chapterGunningFog));
                
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

            ProjectDataHandler dh = (ProjectDataHandler) this.projectViewer.getDataHandler (Project.OBJECT_TYPE);

            org.jfree.data.time.TimeSeries ts = new org.jfree.data.time.TimeSeries ("All");

            int diff = 0;

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            // Get all the word counts for the project.
            java.util.List<WordCount> wordCounts = dh.getWordCounts (this.projectViewer.getProject (),
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

        } catch (Exception e)
        {

            Environment.logError ("Unable to generate 30 day activity sparkline",
                                  e);

        }

        items.add (this.getWords (this.allChaptersWordCount,
                                  this.allChaptersPages,
                                  sparkLine,
                                  new JLabel (),                                  
                                  wordCountDiff30,
                                  30));        
        
        this.allChaptersFleschKincaid = new JLabel ();
        this.allChaptersFleschReadingEase = new JLabel ();
        this.allChaptersGunningFog = new JLabel ();

        ll = new JLabel ("<html><i>Readability</i></html>");
        
        ll.setBorder (new EmptyBorder (0, 10, 0, 0));
        
        items.add (ll);
        
        items.add (this.getReadability (this.allChaptersFleschKincaid,
                                        this.allChaptersFleschReadingEase,
                                        this.allChaptersGunningFog));
                                         
        box.add (this.getItems ("All Chapters",
                                Book.OBJECT_TYPE,
                                items));
                                    
        final JLabel history = UIUtils.createClickableLabel ("View Detail",
                                                             Environment.getIcon ("chart",
                                                                                  Constants.ICON_MENU));

        history.setBorder (new EmptyBorder (0, 5, 0, 0));
                                                                                  
        box.add (history);
        box.add (Box.createVerticalStrut (10));
        
        history.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                _this.projectViewer.viewWordCountHistory ();

            }

        });

        JLabel l = UIUtils.createClickableLabel ("<html>Click to find out more about<br />the Readability indices</html>",
                                                 Environment.getIcon ("help",
                                                                      Constants.ICON_MENU));

        l.setBorder (new EmptyBorder (0, 5, 0, 0));
                                                                      
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
    
    public void init ()
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
        
        this.timer.start ();
        
        this.update ();
        
    }    
    
}