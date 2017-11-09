package com.quollwriter.ui.charts;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.jfree.chart.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.ui.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.Header;

public class SessionTimeChart extends AbstractQuollChart<AbstractViewer>
{

    public static final String CHART_TYPE = "session-time";
    //public static final String CHART_TITLE = "Session Length";

    private JFreeChart chart = null;
    private JComponent controls = null;
    private JComboBox      displayB = null;
    private JCheckBox          showAvg = null;
    private JCheckBox          showZeroWordCount = null;
    private JComponent detail = null;

    public SessionTimeChart (AbstractViewer v)
    {

        super (v);

    }

    public void init (StatisticsPanel wcp)
               throws GeneralException
    {

        super.init (wcp);

        this.createControls ();

    }

    private void createControls ()
    {

        final SessionTimeChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);
        b.setOpaque (false);

        Header h = UIUtils.createBoldSubHeader (getUIString (charts,sessionlength,labels,_for),
                                                //"For",
                                                null);
        h.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (h);

        Vector displayItems = new Vector ();
        displayItems.add (getUIString (times,thisweek));//"This week");
        displayItems.add (getUIString (times,lastweek));//"Last week");
        displayItems.add (getUIString (times,thismonth));//"This month");
        displayItems.add (getUIString (times,lastmonth));//"Last month");
        displayItems.add (getUIString (times,alltime));//"All time");

        b.add (Box.createVerticalStrut (5));

        this.displayB = new JComboBox (displayItems);
        this.displayB.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.displayB.setMaximumSize (displayB.getPreferredSize ());

        this.displayB.setAlignmentY (Component.TOP_ALIGNMENT);

        this.displayB.addActionListener (new ActionListener ()
        {

            @Override
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

        this.showAvg = UIUtils.createCheckBox (getUIString (charts,sessionlength,labels,showaverage),//"Show Average",
                                               new ActionListener ()
                                               {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.updateChart ();

                                                    }

                                               });

        this.showZeroWordCount = UIUtils.createCheckBox (getUIString (charts,sessionlength,labels,showzero),//"Show zero word count sessions",
                                                  new ActionListener ()
                                                  {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.updateChart ();

                                                    }

                                                  });

        Box opts = new Box (BoxLayout.Y_AXIS);

        b.add (opts);

        opts.setBorder (UIUtils.createPadding (0, 10, 0, 0));

        opts.add (this.showAvg);
        opts.add (this.showZeroWordCount);

        this.controls = b;

    }

    private void createChart ()
                       throws GeneralException
    {

        java.util.List<String> lprefix = Arrays.asList (charts,sessionlength,labels);
        java.util.List<String> mprefix = Arrays.asList (charts,sessionlength,markers);

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

        }

        // This month.
        if (this.displayB.getSelectedIndex () == 2)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DATE);

            gc.set (Calendar.DATE,
                    1);

            minDate = gc.getTime ();

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

        }

        // All time
        if (this.displayB.getSelectedIndex () == 4)
        {

            days = -1;

        }

        long avg = 0;
        long total = 0;
        long sessions = 0;
        long max = 0;
        Date maxSessStart = null;
        long zeroSessions = 0;
        long zeroSessionsTime = 0;
        long longSessions = 0;
        long totalWords = 0;

        final TimePeriodValuesCollection ds = new TimePeriodValuesCollection ();

        try
        {

            TimePeriodValues vals = new TimePeriodValues (getUIString (lprefix, LanguageStrings.sessions));//"Sessions");

            for (Session s : Environment.getSessions (days))
            {

                int wc = s.getWordCount ();

                if ((!this.showZeroWordCount.isSelected ())
                    &&
                    (wc == 0)
                   )
                {

                    continue;

                }

                if (days == -1)
                {

                    if (minDate == null)
                    {

                        minDate = s.getStart ();

                    }

                    if (s.getStart ().before (minDate))
                    {

                        minDate = s.getStart ();

                    }

                }

                long time = s.getEnd ().getTime () - s.getStart ().getTime ();

                if (time < 60 * 1000)
                {

                    continue;

                }

                if (wc == 0)
                {

                    zeroSessions++;
                    zeroSessionsTime += time;

                }

                totalWords += wc;

                total += time;

                if ((time > 1 * 60 * 60 * 1000)
                    &&
                    (wc > 0)
                   )
                {

                    longSessions++;

                }

                if (time > max)
                {

                    max = time;
                    maxSessStart = s.getStart ();

                }

                sessions++;

                vals.add (new SimpleTimePeriod (s.getStart (),
                                                s.getEnd ()),
                          time);

            }

            ds.addSeries (vals);

        } catch (Exception e)
        {

            Environment.logError ("Unable to get sessions",
                                  e);

            UIUtils.showErrorMessage (this.parent,
                                      "Unable to show sessions");

            return;

        }

        if (sessions > 0)
        {

            avg = total / sessions;

        }

        if (minDate == null)
        {

            minDate = new Date ();

        }

        this.chart = QuollChartUtils.createTimeSeriesChart (getUIString (lprefix,xaxis),//"Date",
                                                            getUIString (lprefix,yaxis),//"Duration",
                                                            ds);

        this.chart.removeLegend ();

        this.chart.setBackgroundPaint (UIUtils.getComponentColor ());

        XYPlot xyplot = (XYPlot) this.chart.getPlot ();

        PeriodAxis axis = (PeriodAxis) xyplot.getDomainAxis ();

        if (minDate != null)
        {

            axis.setLowerBound (minDate.getTime ());

            axis.setUpperBound (maxDate.getTime ());

        }

        QuollChartUtils.customizePlot (xyplot);

        if (this.showAvg.isSelected ())
        {

            xyplot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (mprefix,average),//"Avg %s",
                                                                                Utils.formatAsDuration (avg)),
                                                                 avg,
                                                                 0));

        }

        ((NumberAxis) xyplot.getRangeAxis ()).setAutoRangeIncludesZero (true);

        ((NumberAxis) xyplot.getRangeAxis ()).setNumberFormatOverride (new NumberFormat ()
        {

            @Override
            public StringBuffer format (double        number,
                                        StringBuffer  toAppendTo,
                                        FieldPosition pos)
            {

                return new StringBuffer (Utils.formatAsDuration (number));

            }

            @Override
            public StringBuffer format (long          number,
                                        StringBuffer  toAppendTo,
                                        FieldPosition pos)
            {

                return new StringBuffer (Utils.formatAsDuration (number));

            }

            @Override
            public Number parse(String source, ParsePosition parsePosition) {
                return null;
            }

        });

        long min = 60 * 1000;
        long hour = 60 * min;
        long day = 24 * hour;

        // Default 5 min tick.
        long tick = 5 * min;

        if (max > (hour))
        {

            tick = 20 * min;

        }

        if (max > (3 * hour))
        {

            tick = 30 * min;

        }

        if (max > (6 * hour))
        {

            tick = hour;

        }

        if (max > (12 * hour))
        {

            tick = 2 * hour;

        }

        if (max > (day))
        {

            tick = 3 * hour;

        }

        if (max > (2 * day))
        {

            tick = 6 * hour;

        }

        if (max > (3 * day))
        {

            tick = 12 * hour;

        }

        if (max > (5 * day))
        {

            tick = day;

        }

        ((NumberAxis) xyplot.getRangeAxis ()).setTickUnit (new NumberTickUnit (tick)
        {

            @Override
            public String valueToString (double number)
            {

                return Utils.formatAsDuration (number);

            }

        });

        final SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a, EEE, dd MMM yyyy");

        XYToolTipGenerator ttgen = new StandardXYToolTipGenerator ()
        {

            public String generateToolTip (XYDataset dataset,
                                           int       series,
                                           int       item)
            {

                TimePeriodValuesCollection tsc = (TimePeriodValuesCollection) dataset;

                TimePeriodValues ts = tsc.getSeries (series);

                Number n = ts.getValue (item);

                return String.format (getUIString (charts,sessionlength,tooltip),
                                      dateFormat.format (ts.getTimePeriod (item).getStart ()),
                                      Utils.formatAsDuration (n.intValue ()));

            }

        };

        ((XYBarRenderer) xyplot.getRenderer ()).setSeriesToolTipGenerator (0,
                                                                           ttgen);

        Set<JComponent> items = new LinkedHashSet ();

        items.add (this.createDetailLabel (String.format (getUIString (lprefix,numsessions),//"%s - Session%s",
                                                          Environment.formatNumber (sessions))));
                                                          //(sessions == 1 ? "" : "s"))));

        items.add (this.createDetailLabel (String.format (getUIString (lprefix,sessionsover1hr),//"%s - Session%s over 1 hour",
                                                          Environment.formatNumber (longSessions))));
                                                          //(longSessions == 1 ? "" : "s"))));

        items.add (this.createDetailLabel (String.format (getUIString (lprefix,totalwords),//"%s - Words total",
                                                          Environment.formatNumber (totalWords))));

        items.add (this.createDetailLabel (String.format (getUIString (lprefix,totalsessiontime),//"%s - Total session time",
                                                          Utils.formatAsDuration (total))));

        items.add (this.createDetailLabel (String.format (getUIString (lprefix,averagesessionlength),//"%s - Average session length",
                                                          Utils.formatAsDuration (avg))));

        if (maxSessStart != null)
        {

            items.add (this.createDetailLabel (String.format (getUIString (lprefix,longestsession),//"%s, %s - Longest session",
                                                              Utils.formatAsDuration (max),
                                                              Environment.formatDateTime (maxSessStart))));

        }

        if (this.showZeroWordCount.isSelected ())
        {

            items.add (this.createDetailLabel (String.format (getUIString (lprefix,numzerowordsessions),//"%s, %s - Zero word count sessions",
                                                              Environment.formatNumber (zeroSessions),
                                                              Utils.formatAsDuration (zeroSessionsTime))));

        }

        this.detail = QuollChartUtils.createDetailPanel (items);

    }

    public String getTitle ()
    {

        return getUIString (charts,sessionlength,title);
        //CHART_TITLE;

    }

    public String getType ()
    {

        return CHART_TYPE;

    }

    public JComponent getControls (boolean update)
                            throws GeneralException
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
                         throws GeneralException
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
                          throws GeneralException
    {

        if (update)
        {

            this.chart = null;

        }

        if (this.chart == null)
        {

            this.createChart ();

        }

        return this.detail;

    }

    public String toString ()
    {

        return this.getTitle ();

    }

}
