package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.*;

import java.io.*;
import java.net.*;
import java.beans.*;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.sound.sampled.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ItemAdapter;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.Accordion;
import com.quollwriter.achievements.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

import com.quollwriter.editors.messages.*;
import java.util.Date;
import com.quollwriter.editors.ui.*;

public class OptionsPanel extends QuollPanel<AbstractViewer>
{
        
    public static final String PANEL_ID = "options";
    
    private Accordion accordion = null;
    private JScrollPane scrollPane = null;
    private JCheckBox sendErrorsToSupport = null;
    
    //private Map<Section, Accordion.Item> sections = new HashMap ();
    
    //private Set<Section> sectIds = null;
    
    private Options options = null;
    
    public OptionsPanel (AbstractViewer     viewer,
                         Options.Section... sectIds)
                  throws GeneralException
    {

        super (viewer);
        
        this.options = new Options (viewer,
                                    sectIds);
                
    }
        
    public void showSection (String name)
    {
        
        this.showSection (Options.Section.valueOf (name));
        
    }
        
    public void showSection (Options.Section name)
    {

        this.options.showSection (name);
    /*
        final Accordion.Item item = this.sections.get (name);
        
        if (item != null)
        {
            
            item.setOpenContentVisible (true);            
            
            this.validate ();
            this.repaint ();
            
            final Border origBorder = item.getBorder ();
            
            final Color col = UIUtils.getBorderHighlightColor ();
            
            final int r = col.getRed ();
            final int g = col.getGreen ();
            final int b = col.getBlue ();
            
            PropertyChangeListener l = new PropertyChangeListener ()
            {
            
                @Override
                public void propertyChange (PropertyChangeEvent ev)
                {
                    
                    Color c = new Color (r,
                                         g,
                                         b,
                                        ((Number) ev.getNewValue ()).intValue ());
                                            
                    item.setBorder (new CompoundBorder (new MatteBorder (3, 3, 3, 3, c),
                                                        UIUtils.createPadding (3, 3, 3, 3)));
                    
                }
            
            };            
                                                          
            Timer cycle = UIUtils.createCyclicAnimator (l,
                                                        l,
                                                        60,
                                                        1500,
                                                        0,
                                                        255,
                                                        2,            
                                                        new ActionListener ()
                                                        {
                                                           
                                                           @Override
                                                           public void actionPerformed (ActionEvent ev)
                                                           {
                                                               
                                                               item.setBorder (origBorder);
                                                               
                                                           }
                                                           
                                                        });
            
            // Scroll to it and open.
            item.scrollRectToVisible (item.getBounds ());
            
            cycle.start ();
            
        }
        */
    }
        
    public void init ()
    {
               
        this.options.init ();               
        
        this.add (this.options);
        
    }
            
    private void setContentBorder (JComponent box)
    {
        
        box.setBorder (UIUtils.createPadding (7, 0, 10, 0));
        
    }
                        
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

    public List<Component> getTopLevelComponents ()
    {

        return null;

    }

    public <T extends NamedObject> void refresh (T n)
    {


    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

        final OptionsPanel _this = this;
    
        JButton b = UIUtils.createToolBarButton ("options",
                                                       Environment.replaceObjectNames ("This is just a test item so you can see how the toolbar looks when you change it's location."),
                                                       null,
                                                       new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (_this,
                                     "This button is here as a test so you can see what the toolbar looks like when you move it.");
            
            }

        });

        toolBar.add (b);
    
        b = UIUtils.createToolBarButton ("options",
                                                       Environment.replaceObjectNames ("This is just a test item so you can see how the toolbar looks when you change it's location."),
                                                       null,
                                                       new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (_this,
                                     "This button is here as a test so you can see what the toolbar looks like when you move it.");
            
            }

        });

        toolBar.add (b);

        b = UIUtils.createToolBarButton ("options",
                                                       Environment.replaceObjectNames ("This is just a test item so you can see how the toolbar looks when you change it's location."),
                                                       null,
                                                       new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (_this,
                                     "This button is here as a test so you can see what the toolbar looks like when you move it.");
            
            }

        });

        toolBar.add (b);
    
    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        return true;

    }

    public String getPanelId ()
    {

        return OptionsPanel.PANEL_ID;

    }

    public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
    {

        final OptionsPanel _this = this;
    
        this.options.setState (s);
    
        this.setReadyForUse (true);
    /*
        this.accordion.setState (s.get ("sections"));
    
        SwingUtilities.invokeLater (new Runnable ()
        {
        
            public void run ()
            {

                int o = 0;
                
                try
                {
                    
                    o = Integer.parseInt (s.get ("scroll"));
                    
                } catch (Exception e) {
                    
                    return;
                    
                }

                _this.scrollPane.getVerticalScrollBar ().setValue (o);

                _this.setReadyForUse (true);
                
            }
            
        });
    */
    }

    public void getState (Map<String, Object> m)
    {

        this.options.getState (m);

/*    
        m.put ("sections",
               this.options.getAccordionState ());
        m.put ("scroll",
               this.scrollPane.getVerticalScrollBar ().getValue ());
  */  
    }

    public String getTitle ()
    {
        
        return "Options";
        
    }
    
    public String getIconType ()
    {

        return "options";

    }

    @Override
    public void close ()
    {


    }    
}