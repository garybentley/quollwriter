package com.quollwriter.ui.panels;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.Header;

public class HelpTextPanel extends QuollPanel
{
    
    private String panelId = null;
    private String text = null;
    private String iconType = null;
    private String title = null;
    
    public HelpTextPanel (AbstractProjectViewer pv,
                          String                title,
                          String                text,
                          String                iconType,
                          String                panelId)
    {
        
        super (pv,
               null);
        
        this.title = title;
        this.panelId = panelId;
        this.iconType = iconType;
        this.text = text;
        
    }

    @Override
    public String getPanelId ()
    {

        return this.panelId;
    
    }
    
    public void close ()
    {
        
    }

    public void init ()
    {

        Header header = UIUtils.createHeader (this.title,
                                              Constants.PANEL_TITLE,
                                              this.iconType,
                                              null);

        this.add (header);    
    
        Box edBox = new ScrollableBox (BoxLayout.Y_AXIS);

        edBox.setOpaque (true);
        edBox.setBackground (UIUtils.getComponentColor ());

        edBox.setBorder (UIUtils.createPadding (0, 10, 5, 5));
        
        JTextPane help = UIUtils.createHelpTextPane (this.text,
                                                     this.projectViewer);
        
        help.setBorder (null);
        
        edBox.add (help);
        
        final JScrollPane sp = UIUtils.createScrollPane (edBox);
        sp.setBorder (null);    
            
        UIUtils.doLater (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                sp.getVerticalScrollBar ().setValue (0);
                
            }
            
        });            
            
        this.add (sp);
        
    }

    public void getState (Map<String, Object> s)
    {
        
    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {
        
    }

    public boolean saveUnsavedChanges ()
                                       throws Exception
    {
        
        return false;
        
    }

    public String getIconType ()
    {
        
        return this.iconType;
        
    }

    public String getTitle ()
    {
        
        return this.title;
        
    }
    
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {
                
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
    
}