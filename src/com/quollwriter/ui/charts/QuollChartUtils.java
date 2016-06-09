package com.quollwriter.ui.charts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Component;

import java.util.*;

import java.text.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.ui.*;
import org.jfree.data.category.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.FormItem;

public class QuollChartUtils
{
    
    public static JComponent createDetailPanel (Set<JComponent> items)
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
                                   
        for (JComponent c : items)
        {                           
                
            b.add (c);
            
            c.setBorder (UIUtils.createPadding (0, 5, 5, 0));

        }
        
        b.setOpaque (false);
        b.setBorder (UIUtils.createPadding (0, 5, 5, 0));
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.add (Box.createVerticalGlue ());
        b.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));

        return b;        
        
    }

    public static JComponent createDetailPanel (Set<FormItem> items,
                                                int           maxRows)
    {
        
        String cols = "right:max(80px;p), 6px, p:grow";

        StringBuilder rb = new StringBuilder ();
        
        for (int i = 0; i < items.size (); i++)
        {
            
            rb.append ("p");
            
            if (i < items.size () - 1)
            {
                
                rb.append (", 6px, ");
                
            }
            
        }
        
        String rows = rb.toString ();
        
        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        Box _b = new Box (BoxLayout.Y_AXIS);
        
        
        
        CellConstraints cc = new CellConstraints ();        

        int r = 1;
                                   
        for (FormItem i : items)
        {                           
                
            _b.add (UIUtils.createLabel (i.label.toString ()));
            
            ((JComponent) i.component).setBorder (UIUtils.createPadding (3, 10, 7, 0));
            
            _b.add (i.component);
                /*
            b.add (i.component,
                   cc.xy (1, r));

            if (i.label instanceof Component)
            {                   
            
                b.add ((Component) i.label,
                       cc.xy (3, r));

            } else {
                            
                b.addLabel ((String) i.label,
                            cc.xy (3, r));
        
            }
        
            r += 2;
        */
        }
        
        JPanel p = b.getPanel ();
        _b.setOpaque (false);
        _b.setBorder (UIUtils.createPadding (0, 15, 15, 0));
    _b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        return _b;
        
    }
    
    public static Font getLabelFont ()
    {
        
        JLabel l = new JLabel ();
        
        return l.getFont ().deriveFont (14f);
        
    }
    
    public static Paint getSeriesPaint (int ind)
    {
        
        if (ind == 0)
        {
            
            return UIUtils.getColor ("#D1DBBD");
            
        }
        
        if (ind == 1)
        {
            
            return UIUtils.getColor ("#348AA7");
            
        }

        if (ind == -1)
        {
            
            return UIUtils.getColor ("#ED7D3A");
            
        }

        return java.awt.Color.black;
        
    }
    
    public static void customizePlot (XYPlot plot)
    {
        
        Font f = QuollChartUtils.getLabelFont ();
        
        plot.setBackgroundPaint (UIUtils.getComponentColor ());        
        plot.setDomainGridlinePaint (Environment.getBorderColor ());
        plot.setRangeGridlinePaint (Environment.getBorderColor ());
        plot.setAxisOffset (new RectangleInsets (5D,
                                                 5D,
                                                 5D,
                                                 5D));

        plot.setDomainCrosshairVisible (true);
        plot.setRangeCrosshairVisible (true);
        plot.setDomainGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.setRangeGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.getDomainAxis ().setLabelFont (f);
        plot.getDomainAxis ().setTickLabelFont (f);
        plot.getRangeAxis ().setLabelFont (f);
        plot.getRangeAxis ().setTickLabelFont (f);
                
        XYBarRenderer rend = new XYBarRenderer (0.05);
        rend.setShadowVisible (false);
        rend.setDrawBarOutline (true);
        
        QuollChartUtils.customizeSeriesPaints (rend);
        
        rend.setBarPainter (new StandardXYBarPainter ());
        
        plot.setRenderer (rend);                
                
    }
    
    public static ValueMarker createMarker (String label,
                                            double value,
                                            int    series)
    {

        return QuollChartUtils.createMarker (label,
                                             value,
                                             series,
                                             RectangleAnchor.TOP_LEFT);
    
    }
    
    public static ValueMarker createMarker (String          label,
                                            double          value,
                                            int             series,
                                            RectangleAnchor anchor)
    {
        
        JLabel l = new JLabel (label);
        l.setFont (l.getFont ().deriveFont (14f));
        
        Dimension s = l.getPreferredSize ();
        
        ValueMarker m = new ValueMarker (value);
        m.setLabel (label);
        m.setLabelFont (l.getFont ());

        if (anchor == RectangleAnchor.TOP_LEFT)
        {
            
            m.setLabelOffset (new org.jfree.ui.RectangleInsets ((s.height / 2) + 3, (s.width / 2) + 5, 0, 0));
            
        }
        
        if (anchor == RectangleAnchor.TOP_RIGHT)
        {
            
            m.setLabelOffset (new org.jfree.ui.RectangleInsets ((s.height / 2) + 3, -1 * ((s.width / 2) + 5), 0, 0));
            
            // TOP_RIGHT doesn't work so change to TOP_LEFT and -1 the left value.
            anchor = RectangleAnchor.TOP_LEFT;
            
        }

        if (anchor == RectangleAnchor.BOTTOM_LEFT)
        {
            
            m.setLabelOffset (new org.jfree.ui.RectangleInsets (0, (s.width / 2) + 5, (s.height / 2) + 3, 0));
            
        }

        if (anchor == RectangleAnchor.BOTTOM_RIGHT)
        {
            
            m.setLabelOffset (new org.jfree.ui.RectangleInsets (0, -1 * ((s.width / 2) + 5), (s.height / 2) + 3, 0));
            
            anchor = RectangleAnchor.BOTTOM_LEFT;
            
        }

        m.setPaint (QuollChartUtils.getSeriesPaint (series));
        m.setOutlinePaint (UIUtils.getInnerBorderColor ());
        m.setStroke (new BasicStroke (2));
        m.setOutlineStroke (new BasicStroke (1));
        m.setLabelAnchor (anchor);
        
        return m;
        
    }
    
    private static void customizeSeriesPaints (AbstractRenderer rend)
    {

        rend.setSeriesOutlinePaint (0, UIUtils.getBorderColor ());
        rend.setSeriesPaint (0, QuollChartUtils.getSeriesPaint (0));
        
        rend.setSeriesOutlinePaint (1, UIUtils.getBorderColor ());
        rend.setSeriesPaint (1, QuollChartUtils.getSeriesPaint (1));

    }
    
    public static void customizePlot (CategoryPlot plot)
    {
        
        Font f = QuollChartUtils.getLabelFont ();
        
        plot.setBackgroundPaint (UIUtils.getComponentColor ());        
        plot.setDomainGridlinePaint (Environment.getBorderColor ());
        plot.setRangeGridlinePaint (Environment.getBorderColor ());
        plot.setAxisOffset (new RectangleInsets (5D,
                                                 5D,
                                                 5D,
                                                 5D));

        plot.setDomainCrosshairVisible (true);
        plot.setRangeCrosshairVisible (true);
        plot.setDomainGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.setRangeGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.getDomainAxis ().setLabelFont (f);
        plot.getDomainAxis ().setTickLabelFont (f);
        plot.getRangeAxis ().setLabelFont (f);
        plot.getRangeAxis ().setTickLabelFont (f);
                                
        BarRenderer rend = new BarRenderer ();
        rend.setShadowVisible (false);
        rend.setDrawBarOutline (true);
        rend.setItemMargin (0);
        rend.setBarPainter (new StandardBarPainter ());
        rend.setMaximumBarWidth (0.10);
        rend.setMinimumBarLength (0.05);
        
        QuollChartUtils.customizeSeriesPaints (rend);
        
        plot.setRenderer (rend);                
                
    }

    public static JFreeChart createBarChart (String          xAxisTitle,
                                             String          yAxisTitle,
                                             CategoryDataset ds)
    {
        
        // Create the correct freechart.
        JFreeChart chart = ChartFactory.createBarChart (null,
                                                        xAxisTitle,
                                                        yAxisTitle,
                                                        ds,
                                                        PlotOrientation.VERTICAL,
                                                        true,
                                                        true,
                                                        true);
        
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint (UIUtils.getComponentColor ());
        plot.setDomainGridlinePaint (Environment.getBorderColor ());
        plot.setRangeGridlinePaint (Environment.getBorderColor ());
        /*
        plot.setAxisOffset (new RectangleInsets (5D,
                                                 5D,
                                                 5D,
                                                 5D));
*/
        plot.setDomainCrosshairVisible (true);
        plot.setRangeCrosshairVisible (true);
        plot.setDomainGridlinePaint (UIUtils.getColor ("#cfcfcf"));
        plot.setRangeGridlinePaint (UIUtils.getColor ("#cfcfcf"));
                                                                             
        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setNumberFormatOverride (Environment.getNumberFormatter ());
        
        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin (0);
        /*
        // set up gradient paints for series...
        final GradientPaint gp0 = new GradientPaint(
            0.0f, 0.0f, Color.blue, 
            0.0f, 0.0f, Color.lightGray
        );
        final GradientPaint gp1 = new GradientPaint(
            0.0f, 0.0f, Color.green, 
            0.0f, 0.0f, Color.lightGray
        );
        final GradientPaint gp2 = new GradientPaint(
            0.0f, 0.0f, Color.red, 
            0.0f, 0.0f, Color.lightGray
        );
        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        renderer.setSeriesPaint(2, gp2);
*/
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / -0.6)
        );        
        
        return chart;
        
    }
    
    public static JFreeChart createTimeSeriesChart (String    xAxisTitle,
                                                    String    yAxisTitle,
                                                    XYDataset tsc)
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
        xyplot.setBackgroundPaint (UIUtils.getComponentColor ());
        xyplot.setDomainGridlinePaint (Environment.getBorderColor ());
        xyplot.setRangeGridlinePaint (Environment.getBorderColor ());
        /*
        xyplot.setAxisOffset (new RectangleInsets (5D,
                                                   5D,
                                                   5D,
                                                   5D));
                                                   */
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