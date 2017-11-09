package com.quollwriter.ui.charts;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.jfree.chart.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;
import org.jfree.data.category.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.Header;

public class ReadabilityIndicesChart extends AbstractQuollChart<AbstractProjectViewer>
{

    public static final String CHART_TYPE = "readability-index";
    //public static final String CHART_TITLE = "Readability";

    private JFreeChart chart = null;
    private JComponent detail = null;
    private JComponent controls = null;
    private JCheckBox showGF = null;
    private JCheckBox showFK = null;
    private JTree          chapters = null;
    private JCheckBox          showAvg = null;
    private JCheckBox          showTargets = null;

    public ReadabilityIndicesChart (AbstractProjectViewer pv)
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

        final java.util.List<String> prefix = Arrays.asList (charts,readability,labels);

        final ReadabilityIndicesChart _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);
        b.setOpaque (false);

        this.showGF = UIUtils.createCheckBox (getUIString (prefix,showgf),
                                            //"Show Gunning Fog",
                                               new ActionListener ()
                                               {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.updateChart ();

                                                    }

                                               });

        this.showGF.setSelected (true);

        this.showGF.addActionListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (!_this.showGF.isSelected ())
                {

                    _this.showFK.setSelected (true);

                }
            }

        });

        this.showFK = UIUtils.createCheckBox (getUIString (prefix,showfk),
                                            //"Show Flesch-Kincaid",
                                               new ActionListener ()
                                               {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.updateChart ();

                                                    }

                                               });

        this.showFK.setSelected (true);

        this.showFK.addActionListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (!_this.showFK.isSelected ())
                {

                    _this.showGF.setSelected (true);

                }
            }

        });

        this.showAvg = UIUtils.createCheckBox (getUIString (prefix,showaverage),
                                            //"Show Average",
                                               new ActionListener ()
                                               {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.updateChart ();

                                                    }

                                               });

        this.showTargets = UIUtils.createCheckBox (getUIString (prefix,showtargets),
                                                //)"Show Targets",
                                                  new ActionListener ()
                                                  {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        java.util.List<String> prefix = Arrays.asList (charts,readability,notargets,popup);

                                                        TargetsData targets = _this.viewer.getProjectTargets ();

                                                        if ((targets.getReadabilityGF () == 0)
                                                            &&
                                                            (targets.getReadabilityFK () == 0)
                                                           )
                                                        {

                                                            UIUtils.createQuestionPopup (_this.viewer,
                                                                                         getUIString (prefix,title),
                                                                                         //"Set up Targets",
                                                                                         Constants.TARGET_ICON_NAME,
                                                                                         getUIString (prefix,text),
                                                                                         //"You currently have no readability targets set up.<br /><br />Would you like to set the targets now?<br /><br />Note: Targets can be accessed at any time from the {Project} menu.",
                                                                                         getUIString (prefix,buttons,confirm),
                                                                                         //"Yes, show me",
                                                                                         getUIString (prefix,buttons,cancel),
                                                                                         //"No, not now",
                                                                                         new ActionListener ()
                                                                                         {

                                                                                            @Override public void actionPerformed (ActionEvent ev)
                                                                                            {

                                                                                                try
                                                                                                {

                                                                                                    _this.viewer.viewTargets ();

                                                                                                } catch (Exception e) {

                                                                                                    Environment.logError ("Unable to show targets",
                                                                                                                          e);

                                                                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                                                                              getUIString (charts,readability,notargets,actionerror));
                                                                                                                              //"Unable to show targets.");

                                                                                                }

                                                                                            }

                                                                                         },
                                                                                         null,
                                                                                         null,
                                                                                         null);

                                                            _this.showTargets.setSelected (false);

                                                            return;

                                                        }

                                                        _this.updateChart ();

                                                    }

                                                  });

        Box opts = new Box (BoxLayout.Y_AXIS);

        b.add (opts);

        opts.setBorder (UIUtils.createPadding (0, 10, 0, 0));

        opts.add (this.showFK);
        opts.add (this.showGF);
        opts.add (this.showAvg);
        opts.add (this.showTargets);

        b.add (Box.createVerticalStrut (10));

        Header h = UIUtils.createBoldSubHeader (getUIString (prefix,forchapters),
                                                //"For these {Chapters}"),
                                                null);
        h.setOpaque (false);
        h.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (h);

        Set<Chapter> init = new LinkedHashSet ();

        for (Chapter c : this.viewer.getProject ().getBook (0).getChapters ())
        {

            ChapterCounts cc = _this.viewer.getChapterCounts (c);

            if (cc.wordCount > Constants.MIN_READABILITY_WORD_COUNT)
            {

                init.add (c);

            }

        }

        this.chapters = new JTree (UIUtils.createTree (this.viewer.getProject ().getBook (0),
                                                       new ArrayList (), /* exclude */
                                                       init, /* init */
                                                       true));

        this.chapters.setOpaque (false);
        ToolTipManager.sharedInstance().registerComponent (this.chapters);

        this.chapters.getModel ().addTreeModelListener (new TreeModelAdapter ()
        {

            public void treeNodesChanged (TreeModelEvent ev)
            {

                // Don't care what has changed, just trigger an update to the
                // chart.
                _this.updateChart ();

            }

        });

        SelectableProjectTreeCellRenderer rend = new SelectableProjectTreeCellRenderer ()
        {

            @Override
            public boolean shouldEnable (Object v)
            {

                if (v instanceof Chapter)
                {

                    Chapter c = (Chapter) v;

                    ChapterCounts cc = _this.viewer.getChapterCounts (c);

                    return cc.wordCount >= Constants.MIN_READABILITY_WORD_COUNT;

                }

                return true;

            }

            @Override
            public String getToolTipText (Object v)
            {

                if (v instanceof Chapter)
                {

                    Chapter c = (Chapter) v;

                    ChapterCounts cc = _this.viewer.getChapterCounts (c);

                    if (cc.wordCount < Constants.MIN_READABILITY_WORD_COUNT)
                    {

                        return getUIString (charts,readability,excluded,single,tooltip);
/*
                        if (cc.wordCount == 0)
                        {

                            return Environment.replaceObjectNames ("This {chapter} is excluded because it has no words.");

                        } else {

                            return String.format (getUIString (charts,readability,exlcuded,tooltip)
                                                //"This {chapter} is excluded because it only has %s word%s.",
                                                  Environment.formatNumber (cc.wordCount));
                                                //(cc.wordCount == 1 ? "" : "s")));

                        }
*/
                    }

                }

                return super.getToolTipText (v);

            }

        };

        rend.setShowIcons (false);
        this.chapters.setCellRenderer (rend);

        this.chapters.addMouseListener (new MouseEventHandler ()
        {

            private void selectAllChildren (DefaultTreeModel       model,
                                            DefaultMutableTreeNode n,
                                            boolean                v)
            {

                Enumeration<DefaultMutableTreeNode> en = n.children ();

                while (en.hasMoreElements ())
                {

                    DefaultMutableTreeNode c = en.nextElement ();

                    SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                    s.selected = v;

                    // Tell the model that something has changed.
                    model.nodeChanged (c);

                    // Iterate.
                    this.selectAllChildren (model,
                                            c,
                                            v);

                }

            }

            @Override
            public void handlePress (MouseEvent ev)
            {

                TreePath tp = _this.chapters.getPathForLocation (ev.getX (),
                                                                 ev.getY ());

                if (tp != null)
                {

                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                    // Tell the model that something has changed.
                    DefaultTreeModel model = (DefaultTreeModel) _this.chapters.getModel ();

                    SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                    if (s.obj instanceof Chapter)
                    {

                        Chapter c = (Chapter) s.obj;

                        ChapterCounts cc = _this.viewer.getChapterCounts (c);

                        if (cc.wordCount < Constants.MIN_READABILITY_WORD_COUNT)
                        {

                            return;

                        }


                    }

                    s.selected = !s.selected;

                    model.nodeChanged (n);

                }

            }

        });

        //UIUtils.addSelectableListener (this.chapters);

        this.chapters.setRootVisible (false);
        this.chapters.setShowsRootHandles (false);
        this.chapters.setScrollsOnExpand (true);
        this.chapters.setBorder (UIUtils.createPadding (0, 5, 0, 0));

        // Never toggle.
        this.chapters.setToggleClickCount (-1);

        this.chapters.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (this.chapters);

        this.controls = b;

    }

    private void createChart ()
                       throws GeneralException
    {

        java.util.List<String> prefix = Arrays.asList (charts,readability,labels);

        final ReadabilityIndicesChart _this = this;

        ChapterDataHandler dh = (ChapterDataHandler) this.viewer.getDataHandler (Chapter.class);

        Set<Chapter> selected = new HashSet ();

        UIUtils.getSelectedObjects ((DefaultMutableTreeNode) this.chapters.getModel ().getRoot (),
                                    selected);

        int chapterCount = 0;
        float totalFK = 0;
        float totalGF = 0;
        float maxFK = 0;
        float maxGF = 0;
        float showMax = 0;
        int filteredCount = 0;

        final DefaultCategoryDataset ds = new DefaultCategoryDataset ();

        try
        {

            for (Book book : this.viewer.getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ChapterCounts cc = this.viewer.getChapterCounts (c);

                    // Filter out chapters with less than the min readability word count.
                    if (cc.wordCount < Constants.MIN_READABILITY_WORD_COUNT)
                    {

                        filteredCount++;

                        continue;

                    }

                    if (!selected.contains (c))
                    {

                        continue;

                    }

                    ReadabilityIndices ri = this.viewer.getReadabilityIndices (c);

                    float fk = ri.getFleschKincaidGradeLevel ();
                    float gf = ri.getGunningFogIndex ();

                    chapterCount++;
                    totalFK += fk;
                    totalGF += gf;

                    maxFK = Math.max (maxFK, fk);
                    maxGF = Math.max (maxGF, gf);

                    if (this.showFK.isSelected ())
                    {

                        ds.addValue (fk,
                                     getUIString (prefix, LanguageStrings.fk),
                                     //"Flesch-Kincaid",
                                     c.getName ());

                    }

                    if (this.showGF.isSelected ())
                    {

                        ds.addValue (gf,
                                     getUIString (prefix, LanguageStrings.gf),
                                     //"Gunning Fog",
                                     c.getName ());

                    }

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to get word counts",
                                  e);

            UIUtils.showErrorMessage (this.parent,
                                      "Unable to word counts");

            return;

        }

        this.chart = QuollChartUtils.createBarChart (getUIString (prefix,yaxis),
                                                    //Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE),
                                                     getUIString (prefix,xaxis),
                                                     //"Reading Level",
                                                     ds);

        this.chart.setBackgroundPaint (UIUtils.getComponentColor ());

        CategoryPlot plot = (CategoryPlot) this.chart.getPlot ();

        QuollChartUtils.customizePlot (plot);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions (CategoryLabelPositions.STANDARD);//CategoryLabelPositions.createUpRotationLabelPositions (Math.PI / 0.5));

        plot.setOrientation (PlotOrientation.HORIZONTAL);
        plot.setRangeAxisLocation (AxisLocation.BOTTOM_OR_LEFT);

        TargetsData ptargs = this.viewer.getProjectTargets ();

        int targetGF = ptargs.getReadabilityGF ();
        int targetFK = ptargs.getReadabilityFK ();

        double avgGF = 0;
        double avgFK = 0;

        if (chapterCount > 0)
        {

            avgGF = totalGF / chapterCount;
            avgFK = totalFK / chapterCount;

        }

        double diffAvgGF = avgGF - targetGF;

        boolean showGF = this.showGF.isSelected ();
        boolean showFK = this.showFK.isSelected ();

        if (this.showAvg.isSelected ())
        {

            String tgf = "";

            if ((targetGF > 0)
                &&
                (showGF)
               )
            {

                tgf = String.format (getUIString (charts,readability,markers,averagegfsuffix),
                                    //", %s%s target",
                                     (diffAvgGF < 0 ? "" : "+") + Environment.formatNumber (diffAvgGF));

            }

            RectangleAnchor anch = RectangleAnchor.TOP_LEFT;

            if (avgGF > avgFK)
            {

                anch = RectangleAnchor.TOP_RIGHT;

            }

            plot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (charts,readability,markers,averagegf),
                                                                                //"Avg GF %s%s",
                                                                              Environment.formatNumber (avgGF),
                                                                              tgf),
                                                               avgGF,
                                                               1,
                                                               anch));

            String tfk = "";

            if ((targetFK > 0)
                &&
                (showFK)
               )
            {

                double diffAvgFK = avgFK - targetFK;

                tfk = String.format (getUIString (charts,readability,markers,averagefksuffix),
                                    //", %s%s target",
                                     (diffAvgFK < 0 ? "" : "+") + Environment.formatNumber (diffAvgFK));

            }

            anch = RectangleAnchor.TOP_LEFT;

            if (avgFK > avgGF)
            {

                anch = RectangleAnchor.TOP_RIGHT;

            }

            plot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (charts,readability,markers,averagefk),
                                                                            //"Avg FK %s%s",
                                                                              Environment.formatNumber (avgFK),
                                                                              tfk),
                                                               avgFK,
                                                               0,
                                                               anch));

        }

        if (this.showTargets.isSelected ())
        {

            RectangleAnchor anch = RectangleAnchor.BOTTOM_LEFT;

            if (targetGF > targetFK)
            {

                anch = RectangleAnchor.BOTTOM_RIGHT;

            }

            if ((targetGF > 0)
                &&
                (showGF)
               )
            {

                plot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (charts,readability,markers,targetgf),
                                                                                //"GF Target %s",
                                                                                  Environment.formatNumber (targetGF)),
                                                                   targetGF,
                                                                   -1,
                                                                   anch));

            }

            anch = RectangleAnchor.BOTTOM_LEFT;

            if (targetFK > targetGF)
            {

                anch = RectangleAnchor.BOTTOM_RIGHT;

            }

            if (targetGF > maxGF)
            {

                showMax = targetGF + 1;

            }

            if ((targetFK > 0)
                &&
                (showFK)
               )
            {

                plot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (charts,readability,markers,targetfk),
                                                                                //"FK Target %s",
                                                                                  Environment.formatNumber (targetFK)),
                                                                   targetFK,
                                                                   -1,
                                                                   anch));

            }

            if ((targetFK > maxFK)
                &&
                (targetFK > showMax)
               )
            {

                showMax = targetFK + 1;

            }

            if (showMax > 0)
            {

                ((NumberAxis) plot.getRangeAxis()).setUpperBound (showMax);

            }

        }

        int overGF = 0;
        int overFK = 0;

        for (Chapter c : selected)
        {

            ReadabilityIndices ri = this.viewer.getReadabilityIndices (c);

            float fk = ri.getFleschKincaidGradeLevel ();
            float gf = ri.getGunningFogIndex ();

            if (fk > targetFK)
            {

                overFK++;

            }

            if (gf > targetGF)
            {

                overGF++;

            }

        }

        ((NumberAxis) plot.getRangeAxis ()).setAutoRangeIncludesZero (true);

        Set<JComponent> items = new LinkedHashSet ();

        if ((targetFK > 0)
            &&
            (overFK > 0)
            &&
            (showFK)
           )
        {

            String t = String.format (getUIString (charts,readability,labels,overtargetfk),
                                     //"%s {Chapter%s} over target Flesch-Kincaid",
                                      Environment.formatNumber (overFK));
                                      //(overFK == 1 ? singular : plural));

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

                                           Targets.showChaptersOverReadabilityTarget (_this.viewer,
                                                                                      l);

                                       }

                                    });

            l.setToolTipText (getUIString (charts,readability,labels,clicktoview));
                            //"Click to view");

            items.add (l);

        }

        if ((targetGF > 0)
            &&
            (overGF > 0)
            &&
            (showGF)
           )
        {

            String t = String.format (getUIString (charts,readability,labels,overtargetgf),
            //"%s {Chapter%s} over target Gunning Fog",
                                      Environment.formatNumber (overGF));
                                      //(overGF == 1 ? "" : "s"));

            // TODO: Fix this nonsense.
            ActionListener _null = null;

            final JLabel l = this.createDetailLabel (UIUtils.createClickableLabel (t,
                                                                                   null,
                                                                                   _null));

            l.setIcon (Environment.getIcon (Constants.ERROR_ICON_NAME,
                                            Constants.ICON_MENU));

            UIUtils.makeClickable (l,
                                    new ActionListener ()
                                    {

                                       @Override
                                       public void actionPerformed (ActionEvent ev)
                                       {

                                           Targets.showChaptersOverReadabilityTarget (_this.viewer,
                                                                                      l);

                                       }

                                    });

            l.setToolTipText (getUIString (charts,readability,labels,clicktoview));
            //"Click to view the " + t);

            items.add (l);

        }

        if (filteredCount > 0)
        {

            items.add (this.createWarningLabel (String.format (getUIString (charts,readability,excluded,plural,text),
                                                //"%s {chapters} have less than %s words and have been excluded",
                                                               Environment.formatNumber (filteredCount))));
                                                //Environment.formatNumber (Constants.MIN_READABILITY_WORD_COUNT))));

        }

        if (showFK)
        {

            items.add (this.createDetailLabel (String.format (getUIString (charts,readability,labels,averagefk),
                                                            //"%s - Average Flesch-Kincaid",
                                                              Environment.formatNumber (avgFK))));

        }

        if (showGF)
        {

            items.add (this.createDetailLabel (String.format (getUIString (charts,readability,labels,averagegf),
                                                            //"%s - Average Gunning Fog",
                                                              Environment.formatNumber (avgGF))));

        }

        this.detail = QuollChartUtils.createDetailPanel (items);

    }

    public String getTitle ()
    {

        return getUIString (charts,readability,title);
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

        return this.getTitle ();

    }

}
