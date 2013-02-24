package com.quollwriter.ui.charts;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;

public class AllWordCountsChart implements QuollChart
{

    public static final String CHART_TYPE = "all-word-count";
    public static final String CHART_TITLE = "Total Word Count";
    
    private AbstractProjectViewer projectViewer = null;
    private WordCountPanel wcp = null;

    private JComboBox      displayB = null;
    
    private JFreeChart chart = null;
    private JComponent controls = null;
    
    public AllWordCountsChart ()
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
        
        final AllWordCountsChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);

        Header h = UIUtils.createBoldSubHeader ("Display",
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

                _this.createChart ();
                
                _this.wcp.updateChart (_this.chart);
            
            }

        });

        this.displayB.setSelectedIndex (0);

        Box db = new Box (BoxLayout.X_AXIS);
        db.setAlignmentX (Component.LEFT_ALIGNMENT);
        db.setAlignmentY (Component.TOP_ALIGNMENT);

        db.add (Box.createHorizontalStrut (5));

        db.add (displayB);

        b.add (db);

        b.add (Box.createVerticalStrut (10));

        JComponent c = (JComponent) Box.createVerticalGlue ();
        c.setOpaque (false);
        b.add (c);

        this.controls = b;
        
    }
    
    private void createChart ()
    {
        
        int days = 0;

        if (displayB.getSelectedIndex () == 0)
        {

            days = -7;

        }

        if (displayB.getSelectedIndex () == 1)
        {

            days = -30;

        }

        if (displayB.getSelectedIndex () == 2)
        {

            days = -90;

        }

        final TimeSeriesCollection tsc = new TimeSeriesCollection ();

        try
        {

            TimeSeries ts = new TimeSeries ("Date");

            ProjectDataHandler pdh = (ProjectDataHandler) this.projectViewer.getDataHandler (Project.OBJECT_TYPE);

            // Get all the word counts for the project.
            List<WordCount> wordCounts = pdh.getWordCounts (this.projectViewer.getProject (),
                                                            days);

            for (WordCount wc : wordCounts)
            {

                if (ts.getValue (new Day (wc.getEnd ())) == null)
                {

                    ts.add (new Day (wc.getEnd ()),
                            wc.getCount ());

                }

            }

            ts.setRangeDescription (null);
            tsc.addSeries (ts);

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
                                                            tsc);
                
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
        
        return this.getTitle ();
        
    }

}