package com.quollwriter.ui.charts;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.jfree.chart.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.axis.*;
import org.jfree.data.category.*;
import org.jfree.chart.renderer.category.*;

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

public class PerChapterWordCountsChart extends AbstractQuollChart<AbstractProjectViewer>
{

    public static final String CHART_TYPE = "per-chapter-word-counts";
    public static final String CHART_TITLE = "{Chapter} Word Counts";
        
    private JFreeChart chart = null;
    private JComponent detail = null;
    private JComponent controls = null;
    private JComboBox      displayB = null;
    private JTree          chapters = null;
    private JCheckBox          showAvg = null;
    private JCheckBox          showTarget = null;
    private JComponent opts = null;
    
    public PerChapterWordCountsChart (AbstractProjectViewer pv)
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
        
        final PerChapterWordCountsChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);
        b.setOpaque (false);

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
                                                        
                                                        TargetsData targets = _this.viewer.getProjectTargets ();
                                                        
                                                        if (targets.getMaxChapterCount () == 0)
                                                        {
                                                            
                                                            UIUtils.createQuestionPopup (_this.viewer,
                                                                                         "Set up Target",
                                                                                         Constants.TARGET_ICON_NAME,
                                                                                         "You currently have no {chapter} word count target set up.<br /><br />Would you like to set the target now?<br /><br />Note: Targets can be accessed at any time from the {Project} menu.",
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

        this.opts = new Box (BoxLayout.Y_AXIS);
            
        b.add (this.opts);
                            
        this.opts.setBorder (UIUtils.createPadding (0, 10, 0, 0));                            
                                
        this.opts.add (this.showAvg);
        this.opts.add (this.showTarget);                
        
        Header h = UIUtils.createBoldSubHeader (Environment.replaceObjectNames ("For"),
                                                null);
        h.setAlignmentY (Component.TOP_ALIGNMENT);
        
        b.add (h);
        
        Vector displayItems = new Vector ();
        displayItems.add ("Now");
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

                _this.opts.setVisible (_this.displayB.getSelectedIndex () == 0);
            
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

        this.chapters = UIUtils.createTree ();
        this.chapters.setModel (new DefaultTreeModel (UIUtils.createTree (this.viewer.getProject ().getBook (0),
                                                       new ArrayList (), /* exclude */
                                                       this.viewer.getProject ().getBook (0).getChapters (), /* init */
                                                       true)));

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

        this.chapters.setOpaque (false);

        this.chapters.setRootVisible (false);
        this.chapters.setShowsRootHandles (false);
        this.chapters.setBorder (UIUtils.createPadding (0, 5, 0, 0));

        // Never toggle.
        this.chapters.setToggleClickCount (-1);

        this.chapters.setMaximumSize (this.chapters.getPreferredSize ());
        this.chapters.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        b.add (this.chapters);

        this.controls = b;
        
    }
        
    private void createChart ()
                       throws GeneralException
    {
        
        if (this.displayB.getSelectedIndex () == 0)
        {
            
            this.createCurrentChart ();
            
            return;
            
        }
        
        this.createHistoryChart ();
        
    }
    
    private void createCurrentChart ()
                              throws GeneralException
    {

        final PerChapterWordCountsChart _this = this;
    
        ChapterDataHandler dh = (ChapterDataHandler) this.viewer.getDataHandler (Chapter.class);

        Set<Chapter> selected = new HashSet ();

        UIUtils.getSelectedObjects ((DefaultMutableTreeNode) this.chapters.getModel ().getRoot (),
                                    selected);
        
        int chapterCount = 0;
        int totalWords = 0;
        int maxWords = 0;
        Chapter maxChap = null;
                                    
        final DefaultCategoryDataset ds = new DefaultCategoryDataset ();
                                    
        try
        {

            for (Book book : this.viewer.getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    if (!selected.contains (c))
                    {

                        continue;

                    }

                    ChapterCounts cc = this.viewer.getChapterCounts (c);
                            
                    if (cc.wordCount > 0)
                    {
                                        
                        chapterCount++;
                        
                    }
                    
                    totalWords += cc.wordCount;
                    
                    if (cc.wordCount > maxWords)
                    {
                        
                        maxChap = c;
                        maxWords = cc.wordCount;
                        
                    }
                                        
                    ds.addValue (cc.wordCount,
                                 "Chapters",
                                 c.getName ());
                    
                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to get word counts",
                                  e);

            UIUtils.showErrorMessage (this.parent,
                                      "Unable to show word counts");

            return;

        }
        
        this.chart = QuollChartUtils.createBarChart (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE),
                                                     "Word Count",
                                                     ds);
            
        this.chart.setBackgroundPaint (UIUtils.getComponentColor ());               
        this.chart.removeLegend ();
                
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot ();

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions (CategoryLabelPositions.STANDARD);//CategoryLabelPositions.createUpRotationLabelPositions (Math.PI / 0.5));        
        
        plot.setOrientation (PlotOrientation.HORIZONTAL);
        plot.setRangeAxisLocation (AxisLocation.BOTTOM_OR_LEFT);
        
        QuollChartUtils.customizePlot (plot);
            
        CategoryToolTipGenerator ttgen = new StandardCategoryToolTipGenerator ()
        {

            public String generateToolTip (CategoryDataset dataset,
                                           int             row,
                                           int             column)
            {

                DefaultCategoryDataset dsc = (DefaultCategoryDataset) dataset;

                Number n = dsc.getValue (row,
                                         column);

                StringBuilder b = new StringBuilder ();

                b.append (Environment.formatNumber (n.intValue ()));
                b.append (" words ");

                return b.toString ();

            }

        };
            
        ((CategoryItemRenderer) plot.getRenderer ()).setSeriesToolTipGenerator (0,
                                                                                ttgen);        
            
        TargetsData ptargs = this.viewer.getProjectTargets ();
    
        int targetWords = ptargs.getMaxChapterCount ();

        double avgWords = 0;
        
        if (chapterCount > 0)
        {
            
            avgWords = totalWords / chapterCount;
            
        }

        double diffAvgWords = avgWords - targetWords;        
        
        if (this.showAvg.isSelected ())
        {
        
            String tgf = "";
        
            if (targetWords > 0)
            {
                                
                tgf = String.format (", %s%s target",
                                     (diffAvgWords < 0 ? "" : "+"),
                                     Environment.formatNumber ((long) diffAvgWords));
                
            }
        
            plot.addRangeMarker (QuollChartUtils.createMarker (String.format ("Avg %s%s",
                                                                              Environment.formatNumber ((long) avgWords),
                                                                              tgf),
                                                               avgWords,
                                                               1,
                                                               org.jfree.ui.RectangleAnchor.TOP_RIGHT));

        }
            
        if (this.showTarget.isSelected ())
        {
                        
            if (targetWords > 0)
            {
        
                plot.addRangeMarker (QuollChartUtils.createMarker (String.format ("Target %s",
                                                                                  Environment.formatNumber (targetWords)),
                                                                   targetWords,
                                                                   -1,
                                                                   org.jfree.ui.RectangleAnchor.BOTTOM_LEFT));

            }
                                    
        }

        int over = 0;
        
        for (Chapter c : selected)
        {

            ChapterCounts cc = this.viewer.getChapterCounts (c);
        
            if (cc.wordCount > targetWords)
            {
                
                over++;
                
            }
            
        }
                
        //((NumberAxis) plot.getRangeAxis ()).setAutoRangeIncludesZero (true);
                
        Set<JComponent> items = new LinkedHashSet ();
                                                     
        if ((targetWords > 0)
            &&
            (over > 0)
           )
        {

            String t = String.format ("%s {Chapter%s} over target word count",
                                      Environment.formatNumber (over),
                                      (over == 1 ? "" : "s"));
        
            // TODO: Fix this nonsense.
            ActionListener _null = null;
        
            final JLabel l = this.createWarningLabel (UIUtils.createClickableLabel (t,
                                                                                    null,
                                                                                    _null));
            
            UIUtils.makeClickable (l,
                                   new ActionListener ()
            {
               
               @Override
               public void actionPerformed (ActionEvent ev)
               {
                   
                   Targets.showChaptersOverWordTarget (_this.viewer,
                                                       l);
                   
               }
               
            });
            
            l.setToolTipText ("Click to view the " + t);
                
            items.add (l);
            
        }
        
        items.add (this.createDetailLabel (String.format ("%s - Average word count",
                                                          Environment.formatNumber ((long) avgWords))));
        
        this.detail = QuollChartUtils.createDetailPanel (items);

    }
    
    private void createHistoryChart ()
                              throws GeneralException
    {
        
        int days = -1;
        
        Date minDate = null;
        Date maxDate = new Date ();
        
        // This week.
        if (this.displayB.getSelectedIndex () == 1)
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
        if (this.displayB.getSelectedIndex () == 2)
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
        if (this.displayB.getSelectedIndex () == 3)
        {

            GregorianCalendar gc = new GregorianCalendar ();
                    
            days = gc.get (Calendar.DATE);
                    
            gc.set (Calendar.DATE,
                    1);
            
            minDate = gc.getTime ();

            days++;
            
        }

        // Last month.
        if (this.displayB.getSelectedIndex () == 4)
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
        if (this.displayB.getSelectedIndex () == 5)
        {
            
            days = 1;

        }
        
        ChapterDataHandler dh = (ChapterDataHandler) this.viewer.getDataHandler (Chapter.class);

        Set selected = new HashSet ();

        UIUtils.getSelectedObjects ((DefaultMutableTreeNode) this.chapters.getModel ().getRoot (),
                                    selected);

        final TimeSeriesCollection tscc = new TimeSeriesCollection ();

        try
        {

            for (Book book : this.viewer.getProject ().getBooks ())
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
                                                                   0);// * days);

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

            UIUtils.showErrorMessage (this.parent,
                                      "Unable to show word counts");

            return;

        }

        this.chart = QuollChartUtils.createTimeSeriesChart ("Date",
                                                            "Word Count",
                                                            tscc);
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
        
        //QuollChartUtils.customizePlot (xyplot);
        
        this.detail = null;
                
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