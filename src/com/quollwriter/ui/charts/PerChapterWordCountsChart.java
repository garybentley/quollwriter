package com.quollwriter.ui.charts;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.jfree.chart.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;

public class PerChapterWordCountsChart implements QuollChart
{

    public static final String CHART_TYPE = "per-chapter-word-counts";
    public static final String CHART_TITLE = "{Chapter} Word Counts";
    
    private AbstractProjectViewer projectViewer = null;
    private WordCountPanel wcp = null;
    
    private JFreeChart chart = null;
    private JComponent controls = null;
    private JComboBox      displayB = null;
    private JTree          chapters = null;
    
    public PerChapterWordCountsChart ()
    {
        
    }

    public void init (AbstractProjectViewer pv,
                      WordCountPanel        wcp)
                      throws                GeneralException
    {
        
        this.projectViewer = pv;
        this.wcp = wcp;

        this.createControls ();
        
        this.createChart ();
                
    }
    
    private void createControls ()
    {
        
        final PerChapterWordCountsChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);
        b.setOpaque (false);

        Header h = UIUtils.createBoldSubHeader (Environment.replaceObjectNames ("For"),
                                                null);
        h.setAlignmentY (Component.TOP_ALIGNMENT);
        
        b.add (h);
        
        Vector displayItems = new Vector ();
        displayItems.add ("Past 7 days");
        displayItems.add ("Past 30 days");
        displayItems.add ("Past 3 months");
        displayItems.add ("All time");

        b.add (Box.createVerticalStrut (5));

        this.displayB = new JComboBox (displayItems);
        this.displayB.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.displayB.setMaximumSize (displayB.getPreferredSize ());

        this.displayB.setAlignmentY (Component.TOP_ALIGNMENT);

        this.displayB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.updateChart ();
                
            }

        });

        Box db = new Box (BoxLayout.X_AXIS);
        db.setAlignmentX (Component.LEFT_ALIGNMENT);
        db.setAlignmentY (Component.TOP_ALIGNMENT);

        db.add (Box.createHorizontalStrut (5));

        db.add (this.displayB);
        db.setMaximumSize (db.getPreferredSize ());

        b.add (db);

        b.add (Box.createVerticalStrut (10));

        h = UIUtils.createBoldSubHeader (Environment.replaceObjectNames ("For these {Chapters}"),
                                         null);
        h.setOpaque (false);
        h.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (h);

        this.chapters = new JTree (UIUtils.createTree (this.projectViewer.getProject ().getBook (0),
                                                       new ArrayList (), /* exclude */
                                                       this.projectViewer.getProject ().getBook (0).getChapters (), /* init */
                                                       true));

        this.chapters.getModel ().addTreeModelListener (new TreeModelAdapter ()
        {

            public void treeNodesChanged (TreeModelEvent ev)
            {

                // Don't care what has changed, just trigger an update to the
                // chart.
                _this.updateChart ();

            }

        });

        SelectableProjectTreeCellRenderer rend = new SelectableProjectTreeCellRenderer ();
        
        rend.setShowIcons (false);
        
        this.chapters.setCellRenderer (rend);
        UIUtils.addSelectableListener (this.chapters);

        this.chapters.setOpaque (true);

        this.chapters.setRootVisible (false);
        this.chapters.setShowsRootHandles (false);
        this.chapters.setScrollsOnExpand (true);
        this.chapters.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                     Short.MAX_VALUE));
        this.chapters.setBorder (new EmptyBorder (0, 5, 0, 0));
                                                     
        // Never toggle.
        this.chapters.setToggleClickCount (-1);

        b.add (this.chapters);

        this.controls = b;
        
    }
    
    private void updateChart ()
    {
        
        try
        {
            
            this.createChart ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to create chart",
                                  e);
            
            UIUtils.showErrorMessage (this.projectViewer,
                                      "Unable to show chart, please contact Quoll Writer support for assistance.");
            
        }
        
        this.wcp.updateChart (this.chart);
        
    }
    
    private void createChart ()
                       throws GeneralException
    {
        
        int days = 0;

        if (this.displayB.getSelectedIndex () == 0)
        {

            days = -7;

        }

        if (this.displayB.getSelectedIndex () == 1)
        {

            days = -30;

        }

        if (this.displayB.getSelectedIndex () == 2)
        {

            days = -90;

        }

        ChapterDataHandler dh = (ChapterDataHandler) this.wcp.getProjectViewer ().getDataHandler (Chapter.class);

        Set selected = new HashSet ();

        UIUtils.getSelectedObjects ((DefaultMutableTreeNode) this.chapters.getModel ().getRoot (),
                                    selected);

        final TimeSeriesCollection tscc = new TimeSeriesCollection ();

        try
        {

            for (Book book : this.wcp.getProjectViewer ().getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    if (!selected.contains (c))
                    {

                        continue;

                    }

                    TimeSeries ts = new TimeSeries (c.getName ());

                    // Get all the word counts for the chapter.
                    List<WordCount> wordCounts = dh.getWordCounts (c,
                                                                   days);

                    for (WordCount wc : wordCounts)
                    {

                        if (ts.getValue (new Day (wc.getEnd ())) == null)
                        {

                            ts.add (new Day (wc.getEnd ()),
                                    wc.getCount ());

                        }

                    }

                    ts.setRangeDescription (c.getName ());

                    tscc.addSeries (ts);

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to get word counts",
                                  e);

            UIUtils.showErrorMessage (this.wcp,
                                      "Unable to word counts");

            return;

        }

        this.chart = QuollChartUtils.createTimeSeriesChart ("Date",
                                                            "Word Count",
                                                            tscc);
                
    }
    
    public String getTitle ()
    {
        
        return CHART_TITLE;
        
    }
    
    public String getType ()
    {
        
        return CHART_TYPE;
        
    }
    
    public JComponent getControls ()
    {
        
        return this.controls;
        
    }
    
    public JFreeChart getChart ()
    {
        
        return this.chart;
        
    }
    
    public String toString ()
    {
        
        return Environment.replaceObjectNames (this.getTitle ());
        
    }
    
}