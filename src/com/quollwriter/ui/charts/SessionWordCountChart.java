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
import org.jfree.ui.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.FormItem;

public class SessionWordCountChart extends AbstractQuollChart<AbstractViewer>
{

    public static final String CHART_TYPE = "session-word-count";
    public static final String CHART_TITLE = "Session Word Count";
        
    private JFreeChart chart = null;
    private JComponent controls = null;
    private JComboBox      displayB = null;
    private JCheckBox          showAvg = null;
    private JCheckBox          showTarget = null;
    private JComponent detail = null;
    
    public SessionWordCountChart (AbstractViewer v)
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
        
        final SessionWordCountChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);
        b.setOpaque (false);

        Header h = UIUtils.createBoldSubHeader (Environment.replaceObjectNames ("For"),
                                                null);
        h.setAlignmentY (Component.TOP_ALIGNMENT);
        
        b.add (h);
        
        Vector displayItems = new Vector ();
        displayItems.add ("This week");
        displayItems.add ("Last week");
        displayItems.add ("This month");
        displayItems.add ("Last month");
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

        this.showAvg = UIUtils.createCheckBox ("Show Average",
                                               new ActionListener ()
                                               {
                                                
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.updateChart ();                                                        
                                                        
                                                    }
                                                
                                               });
        
        this.showTarget = UIUtils.createCheckBox ("Show Target",
                                                  new ActionListener ()
                                                  {
                                                
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        TargetsData targets = Environment.getUserTargets ();
                                                        
                                                        if ((targets.getMySessionWriting () == 0)
                                                           )
                                                        {
                                                            
                                                            UIUtils.createQuestionPopup (_this.viewer,
                                                                                         "Set up Target",
                                                                                         Constants.TARGET_ICON_NAME,
                                                                                         "You currently have no writing targets set up.<br /><br />Would you like to set the targets now?<br /><br />Note: Targets can be accessed at any time from the {Project} menu.",
                                                                                         "Yes, show me",
                                                                                         "No, not now",
                                                                                         new ActionListener ()
                                                                                         {
                                                                                            
                                                                                            @Override public void actionPerformed (ActionEvent ev)
                                                                                            {
                                                                                                
                                                                                                try
                                                                                                {
                                                                                                
                                                                                                    _this.viewer.viewTargets ();
                                                                                                    
                                                                                                } catch (Exception e) {
                                                                                                    
                                                                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                                                                              "Unable to show targets.");
                                                                                                    
                                                                                                    Environment.logError ("Unable to show targets",
                                                                                                                          e);
                                                                                                    
                                                                                                }
                                                                                                
                                                                                            }
                                                                                            
                                                                                         },
                                                                                         null,
                                                                                         null,
                                                                                         null);
                                                            
                                                            _this.showTarget.setSelected (false);
                                                                                         
                                                            return;                                                            
                                                            
                                                        }
                                                        
                                                        _this.updateChart ();                                                                                                                
                                                        
                                                    }
                                                
                                                  });

        Box opts = new Box (BoxLayout.Y_AXIS);
            
        b.add (opts);
                            
        opts.setBorder (UIUtils.createPadding (0, 5, 0, 0));                            
                                                   
        opts.add (this.showAvg);
        opts.add (this.showTarget);
        
        this.controls = b;
        
    }
        
    private void createChart ()
                       throws GeneralException
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
        
        long sessions = 0;
        long totalWords = 0;
        long totalTime = 0;
        long sessionsAboveTarget = 0;
        long longSessions = 0;
        long maxTime = 0;
        long maxWords = 0;
        Session maxWordsSession = null;
        Session longestSession = null;
        int target = Environment.getUserTargets ().getMySessionWriting ();
        
        final TimePeriodValuesCollection ds = new TimePeriodValuesCollection ();
                                                            
        try
        {

            TimePeriodValues vals = new TimePeriodValues ("Sessions");
        
            for (Session s : Environment.getSessions (days))
            {

                int wc = s.getWordCount ();
            
                if (wc == 0)
                {
                    
                    continue;
                    
                }
            
                if (wc > target)
                {
                    
                    sessionsAboveTarget++;
                    
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
            
                totalTime += time;
                totalWords += wc;
                sessions++;
            
                if (time > 60 * 60 * 1000)
                {
                    
                    longSessions++;
                    
                }
            
                if (time > maxTime)
                {
                    
                    maxTime = time;
                    
                    longestSession = s;
                    
                }
                                
                if (wc > maxWords)
                {
                
                    maxWords = wc;
                    maxWordsSession = s;
                        
                }
                            
                vals.add (new SimpleTimePeriod (s.getStart (),
                                                s.getEnd ()),
                          wc);

            }
            
            ds.addSeries (vals);

        } catch (Exception e)
        {

            Environment.logError ("Unable to get sessions",
                                  e);

            UIUtils.showErrorMessage (this.parent,
                                      "Unable to sessions");

            return;

        }
        
        if (minDate == null)
        {
            
            minDate = new Date ();
            
        }
        
        this.chart = QuollChartUtils.createTimeSeriesChart ("Date",
                                                            "Word Count",
                                                            ds);
        
        this.chart.removeLegend ();
        
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
        
            long avgWords = (sessions > 0 ? totalWords / sessions : 0);
        
            String t = "";
            
            if (target > 0)
            {

                double diffAvg = avgWords - target;
                
                t = String.format (", %s%s target",
                                   (diffAvg < 0 ? "" : "+"),
                                   Environment.formatNumber (diffAvg));
                
            }
        
            xyplot.addRangeMarker (QuollChartUtils.createMarker (String.format ("Avg %s%s",
                                                                                Environment.formatNumber (avgWords),
                                                                                t),
                                                                 avgWords,
                                                                 0));
                    
        }
        
        if (this.showTarget.isSelected ())
        {
                                
            if (target > maxWords)
            {
                
                ((NumberAxis) xyplot.getRangeAxis()).setUpperBound (target + 100);
                
            }
            xyplot.addRangeMarker (QuollChartUtils.createMarker (String.format ("Target %s",
                                                                                Environment.formatNumber (target)),
                                                                 target,
                                                                 -1));
                    
        }

        this.chart.setBackgroundPaint (UIUtils.getComponentColor ());
        
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

                StringBuilder b = new StringBuilder ();

                b.append (dateFormat.format (ts.getTimePeriod (item).getStart ()));
                b.append (", ");

                b.append (Utils.formatAsDuration (n.intValue ()));

                return b.toString ();

            }

        };

        ((XYBarRenderer) xyplot.getRenderer ()).setSeriesToolTipGenerator (0,
                                                                           ttgen);        

        Set<JComponent> items = new LinkedHashSet ();
      
        items.add (this.createDetailLabel (String.format ("%s - Session%s",
                                                    Environment.formatNumber (sessions),
                                                    (sessions == 1 ? "" : "s"))));
        
        if (this.showTarget.isSelected ())
        {
                
            items.add (this.createDetailLabel (String.format ("%s - Sessions above word target",
                                                        Environment.formatNumber (sessionsAboveTarget))));
                    
        }
                
        if (sessions > 0)
        {
        
            // Work out number of sessions over target.
            
            items.add (this.createDetailLabel (String.format ("%s - Session%s over 1 hr",
                                                        Environment.formatNumber (longSessions),
                                                        (longSessions == 1 ? "" : "s"))));
        
            items.add (this.createDetailLabel (String.format ("%s words, %s - Average session",
                                                        Environment.formatNumber (totalWords / sessions),
                                                        Utils.formatAsDuration (totalTime / sessions))));
                                
            items.add (this.createDetailLabel (String.format ("%s words, %s, %s, - Longest session",
                                                        Environment.formatNumber (longestSession.getWordCount ()),
                                                        Utils.formatAsDuration (longestSession.getSessionDuration ()),
                                                        Environment.formatDateTime (longestSession.getStart ()))));
            
            items.add (this.createDetailLabel (String.format ("%s words, %s, %s - Session with most words",
                                                        Environment.formatNumber (maxWordsSession.getWordCount ()),
                                                        Utils.formatAsDuration (maxWordsSession.getSessionDuration ()),
                                                        Environment.formatDateTime (maxWordsSession.getStart ()))));
    
        }
                                     
        this.detail = QuollChartUtils.createDetailPanel (items);

    }
        
    public String getTitle ()
    {
        
        return CHART_TITLE;
        
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
            
            this.detail = null;
            
        }
        
        if (this.detail == null)
        {
            
            this.createChart ();
            
        }

        return this.detail;
        
    }
    
    public String toString ()
    {
        
        return Environment.replaceObjectNames (this.getTitle ());
        
    }
    
}