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
import com.quollwriter.ui.components.Accordion;
import com.quollwriter.ui.components.ScrollableBox;
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
public class StatisticsPanel extends BasicQuollPanel<AbstractViewer>
{
/*
    private static Map<String, Class> chartTypeMappings = new HashMap ();

    static
    {
        
        Map<String, Class> m = chartTypeMappings;
        
        m.put (PerChapterWordCountsChart.CHART_TYPE,
               PerChapterWordCountsChart.class);
        m.put (AllWordCountsChart.CHART_TYPE,
               AllWordCountsChart.class);
        m.put (ReadabilityIndicesChart.CHART_TYPE,
               ReadabilityIndicesChart.class);
        m.put (SessionWordCountChart.CHART_TYPE,
               SessionWordCountChart.class);
        m.put (SessionTimeChart.CHART_TYPE,
               SessionTimeChart.class);
        
    }
  */  
    public static final String PANEL_ID = "statistics";
    public static final String OLD_WORD_COUNT_PANEL_ID = Project.WORDCOUNTS_OBJECT_TYPE;

    private JSplitPane splitPane = null;
    private Box configPanel = null;
    private Box detailPanel = null;
    private ChartPanel chartPanel = null;
    private Map<String, QuollChart> charts = new LinkedHashMap ();
    private QuollChart currentChart = null;
    private JComboBox chartSelect = null;
    
    public StatisticsPanel (AbstractViewer  pv,
                            QuollChart...   charts)
                     throws GeneralException
    {
    
        super (pv,
               "Statistics",
               Constants.CHART_ICON_NAME);

        if (charts == null)
        {
            
            throw new GeneralException ("No charts provided.");
               
        }
        
        for (int i = 0; i < charts.length; i++)
        {

            QuollChart c = charts[i];
        
            this.charts.put (c.getType (),
                             c);
               
        }
               
    }
        
    @Override
    public void init ()
               throws GeneralException
    {
        
        super.init ();
        
        for (QuollChart c : this.charts.values ())
        {
            
            c.init (this);
            
        }
                
    }
    
    @Override
    public boolean isWrapContentInScrollPane ()
    {
        
        return false;
        
    }
    
    @Override
    public JComponent getContent ()
                           throws GeneralException
    {
          
        final StatisticsPanel _this = this;
               
        this.splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                         false);
        this.splitPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.splitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.splitPane.setOpaque (false);

        this.splitPane.setBorder (null);
        
        Box b = new ScrollableBox (BoxLayout.Y_AXIS);
        
        b.setBorder (UIUtils.createPadding (0, 5, 0, 0));
        b.setOpaque (false);
        
        Header h = UIUtils.createHeader ("Show",
                                         Constants.SUB_PANEL_TITLE,
                                         null,
                                         null);
        
        b.add (h);

        b.add (Box.createVerticalStrut (5));        
        
        Vector<QuollChart> charts = new Vector ();
        
        for (QuollChart c : this.charts.values ())
        {
            
            charts.add (c);
            
        }
        
        this.chartSelect = new JComboBox (charts);

        this.chartSelect.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {
                
                QuollChart qc = (QuollChart) _this.chartSelect.getSelectedItem ();
                
                String type = qc.getType ();
                
                try
                {
                
                    QuollChart chart = _this.charts.get (type);
                    
                    if (chart == null)
                    {
                        
                        Environment.logError ("Chart type: " + type + " not supported.");
                        
                        return;
                        
                    }
                    
                    _this.currentChart = chart;
                    
                    _this.updateChart (_this.currentChart.getChart (false),
                                       _this.currentChart.getDetail (false));
                                    
                    _this.configPanel.removeAll ();
                    
                    _this.configPanel.add (_this.currentChart.getControls (false));
                                        
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to show chart of type: " + type,
                                          e);
                    
                    UIUtils.showErrorMessage (_this,
                                              "Unable to show chart.");
                    
                }
                
            }
            
        });

        this.chartSelect.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.chartSelect.setAlignmentY (Component.TOP_ALIGNMENT);        
        //this.chartSelect.setMaximumSize (chartSelect.getPreferredSize ());

        Box db = new Box (BoxLayout.X_AXIS);
        db.setAlignmentX (Component.LEFT_ALIGNMENT);
        db.add (Box.createHorizontalStrut (10));

        db.add (this.chartSelect);
        db.setMaximumSize (db.getPreferredSize ());

        b.add (db);

        b.add (Box.createVerticalStrut (10));        

        this.configPanel = new Box (BoxLayout.Y_AXIS);
        
        this.configPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.configPanel.setBorder (UIUtils.createPadding (6,
                                                     6,
                                                     6,
                                                     6));
        
        this.configPanel.setAlignmentY (Component.TOP_ALIGNMENT);
        b.add (this.configPanel);
        
        this.detailPanel = new Box (BoxLayout.Y_AXIS);
        this.detailPanel.setAlignmentY (Component.TOP_ALIGNMENT);
        this.detailPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.detailPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                        Short.MAX_VALUE));
        b.add (this.detailPanel);
        b.add (Box.createVerticalGlue ());
        
        JScrollPane sp = UIUtils.createScrollPane (b);
        
        sp.setBorder (null);
        sp.setMinimumSize (new Dimension (200,
                                          300));
                
        this.splitPane.setLeftComponent (sp);
        //sp.setMinimumSize (new Dimension (50, 200));
        sp.setPreferredSize (new Dimension (200, 300));
        this.splitPane.setRightComponent (this.chartPanel);        
                
        javax.swing.plaf.basic.BasicSplitPaneDivider div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, UIUtils.getComponentColor ()));                
                
        return this.splitPane;

    }

    public void updateChart (JFreeChart chart)
    {
    
        this.updateChart (chart,
                          null);
        
    }
    
    public void updateChart (JFreeChart chart,
                             JComponent detail)
    {
        
        if (this.currentChart == null)
        {
            
            return;
            
        }
        
        Box b = new Box (BoxLayout.Y_AXIS);
               
        ChartPanel cp = new ChartPanel (chart, true);
        cp.setDisplayToolTips (true);
        cp.setAlignmentX (Component.LEFT_ALIGNMENT);
        cp.setOpaque (false);

        cp.setPreferredSize (new Dimension (300,
                                            500));
        cp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                          Short.MAX_VALUE));
        cp.setBorder (UIUtils.createPadding (0, 0, 10, 0));
                                          
        JScrollPane sp = UIUtils.createScrollPane (cp);
        
        sp.setBorder (null);
                                     
        this.setTitle ("Statistics - " + this.currentChart.getTitle ());                               
                                          
        b.add (UIUtils.createHeader (Environment.replaceObjectNames (this.currentChart.getTitle ()),
                                     Constants.PANEL_TITLE,
                                     null,
                                     null));
        b.add (sp);
        
        this.detailPanel.removeAll ();
    
        if (detail != null)
        {
            
            detail.setAlignmentX (Component.LEFT_ALIGNMENT);
            detail.setAlignmentY (Component.TOP_ALIGNMENT);
            detail.setOpaque (false);
            detail.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                  Short.MAX_VALUE));
    
            this.detailPanel.add (UIUtils.createHeader ("Detail",
                                           Constants.PANEL_TITLE,
                                           null,
                                           null));
            this.detailPanel.add (detail);

        } 
                
        this.splitPane.setRightComponent (sp);
        
    }
    
    public void showChart (String type)
                           throws GeneralException
    {
        
        QuollChart chart = this.charts.get (type);
        
        if (chart == null)
        {
            
            Environment.logError ("Chart type: " + type + " not supported.");
            
            return;
            
        }
        
        this.chartSelect.setSelectedItem (chart);

    }
    
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

    public List<Component> getTopLevelComponents ()
    {

        return new ArrayList ();

    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

    }

    @Override
    public String getPanelId ()
    {

        // Gonna regret this...
        return StatisticsPanel.PANEL_ID;

    }

    @Override
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

        this.setReadyForUse (true);
        
    }

    @Override
    public void getState (Map<String, Object> m)
    {

        m.put (Constants.SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.splitPane.getDividerLocation ());

    }

    @Override
    public String getTitle ()
    {
        
        return "Statistics";
        
    }
    
    @Override
    public String getIconType ()
    {

        return Constants.CHART_ICON_NAME;

    }

    @Override
    public void close ()
    {


    }

}
