package com.quollwriter.ui.charts;

import java.awt.Color;

import java.util.*;

import java.text.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.ui.*;
import org.jfree.data.category.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import com.quollwriter.*;
import com.quollwriter.ui.*;

public class QuollChartUtils
{
    
    public static JFreeChart createTimeSeriesChart (String               xAxisTitle,
                                                    String               yAxisTitle,
                                                    TimeSeriesCollection tsc)
    {

        // Create the correct freechart.
        JFreeChart chart = ChartFactory.createTimeSeriesChart (null,
                                                               xAxisTitle,
                                                               yAxisTitle,
                                                               tsc,
                                                               true,
                                                               true,
                                                               true);

        XYPlot xyplot = (XYPlot) chart.getPlot ();
        xyplot.setBackgroundPaint (UIUtils.getColor ("#f0f6fc"));
        xyplot.setDomainGridlinePaint (Environment.getBorderColor ());
        xyplot.setRangeGridlinePaint (Environment.getBorderColor ());
        xyplot.setAxisOffset (new RectangleInsets (5D,
                                                   5D,
                                                   5D,
                                                   5D));
        xyplot.setDomainCrosshairVisible (true);
        xyplot.setRangeCrosshairVisible (true);
        xyplot.setDomainGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        xyplot.setRangeGridlinePaint (UIUtils.getColor ("#cfcfcf"));

        final SimpleDateFormat dateFormat = new SimpleDateFormat ("dd MMM yyyy");

        ((NumberAxis) xyplot.getRangeAxis ()).setNumberFormatOverride (Environment.getNumberFormatter ());

        XYLineAndShapeRenderer xyitemrenderer = new XYLineAndShapeRenderer (true,
                                                                            true);
        xyitemrenderer.setUseFillPaint (true);

        XYToolTipGenerator ttgen = new StandardXYToolTipGenerator ()
        {

            public String generateToolTip (XYDataset dataset,
                                           int       series,
                                           int       item)
            {

                TimeSeriesCollection tsc = (TimeSeriesCollection) dataset;

                TimeSeries ts = tsc.getSeries (series);

                Number n = ts.getValue (item);
                Number p = Integer.valueOf (0);

                if (item > 0)
                {

                    p = ts.getValue (item - 1);

                }

                StringBuilder b = new StringBuilder ();

                if (ts.getRangeDescription () != null)
                {

                    b.append (ts.getRangeDescription ());
                    b.append (" - ");

                }

                b.append (dateFormat.format (ts.getTimePeriod (item).getEnd ()));
                b.append (", ");

                b.append (Environment.formatNumber (n.intValue ()));
                b.append (" words");

                int v = n.intValue () - p.intValue ();

                if (v != 0)
                {

                    b.append (" (");

                    if (v < 0)
                    {

                        b.append (v * -1);
                        b.append (" removed");

                    } else
                    {

                        b.append (v);
                        b.append (" added");

                    }

                    b.append (")");

                }

                return b.toString ();

            }

        };

        xyplot.setRenderer (xyitemrenderer);

        List colors = new ArrayList ();
        colors.add (UIUtils.getColor ("#f5b800"));
        colors.add (UIUtils.getColor ("#7547ff"));
        colors.add (UIUtils.getColor ("#9c4f4f"));
        colors.add (UIUtils.getColor ("#99cc99"));
        colors.add (UIUtils.getColor ("#cc6600"));

        for (int i = 0; i < tsc.getSeriesCount (); i++)
        {

            if (i < (colors.size () - 1))
            {

                xyitemrenderer.setSeriesPaint (i,
                                               (Color) colors.get (i));

            }

            xyitemrenderer.setSeriesStroke (i,
                                            new java.awt.BasicStroke (2f));
            xyitemrenderer.setSeriesShapesFilled (i,
                                                  true);
            xyitemrenderer.setSeriesToolTipGenerator (i,
                                                      ttgen);
            xyitemrenderer.setSeriesShape (i,
                                           new java.awt.geom.Ellipse2D.Float (-3,
                                                                              -3,
                                                                              6,
                                                                              6));
            /*
            if (i > 0)
            {

                xyitemrenderer.setSeriesShape (i,
                                               xyitemrenderer.lookupSeriesShape (0));

            }
             */
        }

        PeriodAxis periodaxis = new PeriodAxis (xAxisTitle);

        periodaxis.setAutoRangeTimePeriodClass (Day.class);

        PeriodAxisLabelInfo[] aperiodaxislabelinfo = new PeriodAxisLabelInfo[3];
        aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo (Day.class,
                                                           new SimpleDateFormat ("d"));
        aperiodaxislabelinfo[1] = new PeriodAxisLabelInfo (Month.class,
                                                           new SimpleDateFormat ("MMM"));
        aperiodaxislabelinfo[2] = new PeriodAxisLabelInfo (Year.class,
                                                           new SimpleDateFormat ("yyyy"));
        periodaxis.setLabelInfo (aperiodaxislabelinfo);
        xyplot.setDomainAxis (periodaxis);

        return chart;

    }    
    
}