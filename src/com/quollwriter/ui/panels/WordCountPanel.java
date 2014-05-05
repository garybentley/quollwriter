package com.quollwriter.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.*;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.db.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.TabHeader;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;

import org.jfree.chart.*;

import com.quollwriter.ui.charts.*;

/**
 * Maybe also output:
 *
 * http://en.wikipedia.org/wiki/SMOG_Index
 * http://en.wikipedia.org/wiki/Automated_Readability_Index
 * http://en.wikipedia.org/wiki/Gunning_fog_index
 * http://en.wikipedia.org/wiki/Flesch-Kincaid_Readability_Test
 * http://en.wikipedia.org/wiki/Coleman-Liau_Index
 * Have a "Statistics" link?  "Readability" link?
 *
 * Online readability: http://www.online-utility.org/english/readability_test_and_improve.jsp
 *
 * See: RuleUtilities.getSyllableCountForWord.
 */
public class WordCountPanel extends QuollPanel
{

    private static Map<String, Class> chartTypeMappings = new HashMap ();

    static
    {
        
        Map<String, Class> m = chartTypeMappings;
        
        m.put (PerChapterWordCountsChart.CHART_TYPE,
               PerChapterWordCountsChart.class);
        m.put (AllWordCountsChart.CHART_TYPE,
               AllWordCountsChart.class);
        
    }
    
    public static final String PANEL_ID = Project.WORDCOUNTS_OBJECT_TYPE;

    private JSplitPane splitPane = null;
    private Box configPanel = null;
    private ChartPanel chartPanel = null;
    private Map<String, QuollChart> charts = new HashMap ();
    private QuollChart currentChart = null;
    
    public WordCountPanel(AbstractProjectViewer pv,
                          Project               p)
                   throws GeneralException
    {

        super (pv,
               p);

        final WordCountPanel _this = this;
               
        this.splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                         false);
        this.splitPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.splitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.splitPane.setOpaque (false);

        this.splitPane.setBorder (null);
        
        Box b = new Box (BoxLayout.Y_AXIS);
        
        b.setOpaque (false);
        b.setBorder (new EmptyBorder (0,
                                      7,
                                      10,
                                      0));
        
        
        Header h = UIUtils.createHeader ("Show",
                                         Constants.SUB_PANEL_TITLE,
                                         null,
                                         null);
        
        b.add (h);

        b.add (Box.createVerticalStrut (5));        
        
        Vector<QuollChart> charts = new Vector ();
        charts.add (new PerChapterWordCountsChart ());
        charts.add (new AllWordCountsChart ());
        
        final JComboBox chartSelect = new JComboBox (charts);

        chartSelect.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {
                
                QuollChart qc = (QuollChart) chartSelect.getSelectedItem ();
                
                try
                {
                
                    _this.showChart (qc.getType ());
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to show chart of type: " + qc.getType (),
                                          e);
                    
                    UIUtils.showErrorMessage (_this,
                                              "Unable to show chart.");
                    
                }
                
            }
            
        });

        chartSelect.setAlignmentX (Component.LEFT_ALIGNMENT);
        chartSelect.setAlignmentY (Component.TOP_ALIGNMENT);        
        chartSelect.setMaximumSize (chartSelect.getPreferredSize ());

        Box db = new Box (BoxLayout.X_AXIS);
        db.setAlignmentX (Component.LEFT_ALIGNMENT);
        db.add (Box.createHorizontalStrut (10));

        db.add (chartSelect);
        db.setMaximumSize (db.getPreferredSize ());

        b.add (db);

        b.add (Box.createVerticalStrut (10));        

        this.configPanel = new Box (BoxLayout.Y_AXIS);
        
        this.configPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.configPanel.setBorder (new EmptyBorder (6,
                                                     6,
                                                     6,
                                                     6));
        this.configPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                        Short.MAX_VALUE));
        this.configPanel.setAlignmentY (Component.TOP_ALIGNMENT);          
        b.add (this.configPanel);

        this.configPanel.setMinimumSize (new Dimension (250,
                                                        300));
        
        //b.add (Box.createVerticalGlue ());
        
        JScrollPane lscroll = new JScrollPane (b);
        lscroll.setBorder (null);
        lscroll.setOpaque (false);
        lscroll.getViewport ().setBorder (null);
        lscroll.getViewport ().setOpaque (false);
        lscroll.getVerticalScrollBar ().setUnitIncrement (20);
        lscroll.setAlignmentX (Component.LEFT_ALIGNMENT);
        lscroll.setMinimumSize (new Dimension (250, 300));
        
/*
        b.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));        
*/
        this.splitPane.setLeftComponent (lscroll);
        
        this.splitPane.setRightComponent (this.chartPanel);        
        
        this.add (this.splitPane,
                  0);

    }

    public QuollChart getChart (String type)
                                throws GeneralException
    {
        
        QuollChart ch = this.charts.get (type);
        
        if (ch != null)
        {
            
            return ch;
            
        }
        
        Class c = WordCountPanel.chartTypeMappings.get (type);
        
        if (c == null)
        {
            
            return null;
            
        }
        
        try
        {
        
            // Get a new instance.
            ch = (QuollChart) c.newInstance ();
            
            ch.init (this.projectViewer,
                     this);

        } catch (Exception e) {
            
            throw new GeneralException ("Unable to create/init: " +
                                        c.getName () +
                                        " for type: " +
                                        type,
                                        e);
            
        }
        
        this.charts.put (type,
                         ch);
        
        return ch;
        
    }
    
    public void updateChart (JFreeChart chart)
    {
        
        ChartPanel p = new ChartPanel (chart, true);
        p.setDisplayToolTips (true);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setOpaque (false);

        b.add (UIUtils.createHeader (Environment.replaceObjectNames (this.currentChart.getTitle ()),
                                     Constants.PANEL_TITLE,
                                     null, //this.currentChart.getIconType (),
                                     null));
        
        b.add (p);
        
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (5, 5, 10, 5));
        
        this.splitPane.setRightComponent (b);
        
    }
    
    public void showChart (String type)
                           throws GeneralException
    {
        
        QuollChart chart = this.getChart (type);
        
        if (chart == null)
        {
            
            Environment.logError ("Chart type: " + type + " not supported.");
            
            return;
            
        }
        
        this.currentChart = chart;
        
        this.updateChart (this.currentChart.getChart ());
                        
        this.configPanel.removeAll ();
        
        this.configPanel.add (this.currentChart.getControls ());
                                        
    }
    
/*
    public EditPanel getAllWordCountsChart (Project p)
                                     throws GeneralException
    {

        final WordCountPanel _this = this;

        final ChartPanel cp = new ChartPanel (null);

        cp.setOpaque (false);

        cp.setBorder (null);

        return new EditPanel (false)
        {

            public void refreshViewPanel ()
            {

            }

            public String getTitle ()
            {

                return "Total Word Counts";

            }

            public String getHelpText ()
            {

                return "";

            }

            public JComponent getEditPanel ()
            {

                return null;

            }

            public JComponent getViewPanel ()
            {

                Box b = new Box (BoxLayout.X_AXIS);
                b.setBackground (Color.WHITE);
                b.setOpaque (true);

                b.add (cp);

                b.add (Box.createHorizontalStrut (5));

                Box bb = new Box (BoxLayout.Y_AXIS);
                bb.setBorder (new EmptyBorder (5,
                                               5,
                                               5,
                                               5));
                bb.setOpaque (false);
                b.add (bb);

                Header h = UIUtils.createBoldSubHeader ("Display",
                                                        null);
                h.setAlignmentY (Component.TOP_ALIGNMENT);

                bb.add (h);

                Vector displayItems = new Vector ();
                displayItems.add ("Past 7 days");
                displayItems.add ("Past 30 days");
                displayItems.add ("Past 3 months");
                displayItems.add ("All");

                bb.add (Box.createVerticalStrut (5));

                final JComboBox displayB = new JComboBox (displayItems);
                displayB.setAlignmentX (Component.LEFT_ALIGNMENT);
                displayB.setMaximumSize (displayB.getPreferredSize ());
                displayB.setAlignmentY (Component.TOP_ALIGNMENT);

                displayB.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
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

                                ProjectDataHandler pdh = (ProjectDataHandler) _this.projectViewer.getDataHandler (Project.OBJECT_TYPE);

                                // Get all the word counts for the project.
                                List<WordCount> wordCounts = pdh.getWordCounts (_this.projectViewer.getProject (),
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

                                UIUtils.showErrorMessage (_this,
                                                          "Unable to word counts");

                                return;

                            }

                            // tsc.setXPosition (TimePeriodAnchor.MIDDLE);

                            cp.setChart (_this.createChart ("Date",
                                                            "Word Count",
                                                            tsc));

                        }

                    });

                displayB.setSelectedIndex (0);

                Box db = new Box (BoxLayout.X_AXIS);
                db.setAlignmentX (Component.LEFT_ALIGNMENT);
                db.setAlignmentY (Component.TOP_ALIGNMENT);

                db.add (Box.createHorizontalStrut (5));

                db.add (displayB);

                bb.add (db);

                bb.add (Box.createVerticalStrut (10));

                JComponent c = (JComponent) Box.createVerticalGlue ();
                c.setOpaque (false);
                bb.add (c);

                return b;

            }

            public java.util.List<FormItem> getEditItems ()
            {

                return null;

            }

            public java.util.List<FormItem> getViewItems ()
            {

                return null;

            }

            public boolean handleSave ()
            {

                return true;

            }

            public boolean handleCancel ()
            {

                return true;

            }

            public void handleEditStart ()
            {

            }

            public IconProvider getIconProvider ()
            {

                DefaultIconProvider iconProv = new DefaultIconProvider ();
                iconProv.putIcon ("header",
                                  Book.OBJECT_TYPE);

                return iconProv;

            }

        };

    }
*/
    public void init ()
                      throws GeneralException
    {
        
        this.showChart (PerChapterWordCountsChart.CHART_TYPE);
            
    }

    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

    public List<Component> getTopLevelComponents ()
    {

        return new ArrayList ();

    }

    public <T extends NamedObject> void refresh (T n)
    {


    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        return true;

    }

    public String getPanelId ()
    {

        // Gonna regret this...
        return Project.WORDCOUNTS_OBJECT_TYPE;

        /*
            ObjectReference r = new ObjectReference (Project.WORDCOUNTS_OBJECT_TYPE,
                                                     this.obj.getKey (),
                                                     null);

            return r.asString ();
         */
    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {

        try
        {

            int v = Integer.parseInt (s.get (Constants.SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME));

            this.splitPane.setDividerLocation (v);

        } catch (Exception e)
        {

        }

    }

    public void getState (Map<String, Object> m)
    {

        m.put (Constants.SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.splitPane.getDividerLocation ());

    }

    public String getTitle ()
    {
        
        return "Statistics";
        
    }
    
    public String getIconType ()
    {

        return "chart";

    }

    public void close ()
    {


    }

}
