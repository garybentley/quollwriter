package com.quollwriter.ui.charts;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.axis.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;

public class AllWordCountsChart extends AbstractQuollChart<AbstractProjectViewer>
{

    public static final String CHART_TYPE = "all-word-count";
    //public static final String CHART_TITLE = "Total Word Count";

    private JComboBox      displayB = null;

    private JFreeChart chart = null;
    private JComponent controls = null;

    public AllWordCountsChart (AbstractProjectViewer pv)
    {

        super (pv);

    }

    public void init (StatisticsPanel wcp)
               throws GeneralException
    {

        super.init (wcp);

        this.createControls ();

    }

    private void createControls ()
    {

        final AllWordCountsChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);

        Header h = UIUtils.createBoldSubHeader (Environment.getUIString (LanguageStrings.charts,
                                                                         LanguageStrings.allwordcounts,
                                                                         LanguageStrings.labels,
                                                                         LanguageStrings._for),
                                                //"For",
                                                null);
        h.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (h);

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.times);

        Vector displayItems = new Vector ();
        displayItems.add (Environment.getUIString (prefix,
                                                   LanguageStrings.thisweek));
                        //"This week");
        displayItems.add (Environment.getUIString (prefix,
                                                   LanguageStrings.lastweek));
        //displayItems.add ("Last week");
        displayItems.add (Environment.getUIString (prefix,
                                                   LanguageStrings.thismonth));
        //displayItems.add ("This month");
        displayItems.add (Environment.getUIString (prefix,
                                                   LanguageStrings.lastmonth));
        //displayItems.add ("Last month");
        displayItems.add (Environment.getUIString (prefix,
                                                   LanguageStrings.alltime));
        //displayItems.add ("All time");

        b.add (Box.createVerticalStrut (5));

        this.displayB = new JComboBox (displayItems);
        this.displayB.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.displayB.setMaximumSize (displayB.getPreferredSize ());
        this.displayB.setAlignmentY (Component.TOP_ALIGNMENT);

        this.displayB.setSelectedIndex (0);

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

        int days = -1;

        Date minDate = null;
        Date maxDate = new Date ();

        // This week.
        if (this.displayB.getSelectedIndex () == 0)
        {

            // Work out how many days there have been this week.
            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DAY_OF_WEEK) - gc.getFirstDayOfWeek ();

            if (days < 0)
            {

                days -= 7;

            }

            gc.add (Calendar.DATE,
                    -1 * days);

            minDate = gc.getTime ();

            days++;

        }

        // Last week
        if (this.displayB.getSelectedIndex () == 1)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DAY_OF_WEEK) - gc.getFirstDayOfWeek ();

            if (days < 0)
            {

                days -= 7;

            }

            gc.add (Calendar.DATE,
                    (-1 * days) - 1);

            maxDate = gc.getTime ();

            days += 7;

            gc.add (Calendar.DATE,
                    -6);

            minDate = gc.getTime ();

            days++;

        }

        // This month.
        if (this.displayB.getSelectedIndex () == 2)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DATE);

            gc.set (Calendar.DATE,
                    1);

            minDate = gc.getTime ();

            days++;

        }

        // Last month.
        if (this.displayB.getSelectedIndex () == 3)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            gc.add (Calendar.MONTH,
                    -1);

            days = gc.getActualMaximum (Calendar.DATE);

            gc.set (Calendar.DATE,
                    days);

            maxDate = gc.getTime ();

            gc.set (Calendar.DATE,
                    1);

            minDate = gc.getTime ();

            days++;

        }

        // All time
        if (this.displayB.getSelectedIndex () == 4)
        {

            days = 1;

        }

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.charts);
        prefix.add (LanguageStrings.allwordcounts);
        prefix.add (LanguageStrings.labels);

        final TimeSeriesCollection tsc = new TimeSeriesCollection ();

        try
        {

            TimeSeries ts = new TimeSeries (Environment.getUIString (prefix,
                                                                     LanguageStrings.xaxis));
                                            //"Date");

            ProjectDataHandler pdh = (ProjectDataHandler) this.viewer.getDataHandler (Project.class);

            // Get all the word counts for the project.
            List<WordCount> wordCounts = pdh.getWordCounts (this.viewer.getProject (),
                                                            0);

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

            UIUtils.showErrorMessage (this.parent,
                                      "Unable to word counts");

            return;

        }

        this.chart = QuollChartUtils.createTimeSeriesChart (Environment.getUIString (prefix,
                                                                                     LanguageStrings.xaxis),
                                                        //"Date",
                                                            Environment.getUIString (prefix,
                                                                                     LanguageStrings.yaxis),
                                                        //"Word Count",
                                                            tsc);

        this.chart.setBackgroundPaint (UIUtils.getComponentColor ());

        XYPlot plot = (XYPlot) this.chart.getPlot ();

        PeriodAxis axis = (PeriodAxis) plot.getDomainAxis ();

        if (minDate != null)
        {

            axis.setLowerBound (minDate.getTime ());

            axis.setUpperBound (maxDate.getTime ());

        }

        plot.setBackgroundPaint (UIUtils.getComponentColor ());
        plot.setDomainGridlinePaint (Environment.getBorderColor ());
        plot.setRangeGridlinePaint (Environment.getBorderColor ());
        plot.setAxisOffset (new RectangleInsets (5D,
                                                 5D,
                                                 5D,
                                                 5D));

        Font f = QuollChartUtils.getLabelFont ();

        plot.setDomainCrosshairVisible (true);
        plot.setRangeCrosshairVisible (true);
        plot.setDomainGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.setRangeGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.getDomainAxis ().setLabelFont (f);
        plot.getDomainAxis ().setTickLabelFont (f);
        plot.getRangeAxis ().setLabelFont (f);
        plot.getRangeAxis ().setTickLabelFont (f);

        this.chart.removeLegend ();

    }

    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.charts,
                                        LanguageStrings.allwordcounts,
                                        LanguageStrings.title);
                                        //CHART_TITLE;

    }

    public String getType ()
    {

        return CHART_TYPE;

    }

    public JComponent getControls (boolean update)
    {

        if (update)
        {

            this.controls = null;

        }

        if (this.controls == null)
        {

            this.createControls ();

        }

        return this.controls;

    }

    public JFreeChart getChart (boolean update)
    {

        if (update)
        {

            this.chart = null;

        }

        if (this.chart == null)
        {

            this.createChart ();

        }

        return this.chart;

    }

    public JComponent getDetail (boolean update)
    {

        return null;

    }

    public String toString ()
    {

        return this.getTitle ();

    }

}
