package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.GradientPainter;
import com.quollwriter.ui.components.Header;

public class AccordionItem extends Box
{
    
    protected Header header = null;
    private List<JMenuItem> headerMenuItems = new ArrayList ();
    private String title = null;
    private JComponent content = null;
    private boolean inited = false;
    
    public AccordionItem (String title,
                          String iconType)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.title = Environment.replaceObjectNames (title);
        
        ImageIcon ii = null;
        
        if (iconType != null)
        {
            
            ii = Environment.getIcon (iconType,
                                      Constants.ICON_SIDEBAR);
            
        }
        
        final Header h = new Header (this.title,
                                     ii,
                                     null);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (14d)).deriveFont (Font.PLAIN));
        h.setPaintProvider (null);
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setPadding (new Insets (5, 5, 0, 0));
        h.setPaintProvider (new GradientPainter (UIUtils.getComponentColor (), 
                                                 UIUtils.getComponentColor ()));
        
        // end new
        h.setAlignmentX (Component.LEFT_ALIGNMENT);        
        h.setAlignmentY (Component.TOP_ALIGNMENT);        

        this.header = h;
                
    }

    public AccordionItem (Header     h,
                          JComponent content)
    {
        
        super (BoxLayout.Y_AXIS);

        this.header = h;

        this.content = content;
        
    }
    
    public AccordionItem (String     title,
                          String     iconType,
                          JComponent content)
    {

        this (title,
              iconType);
    
        this.content = content;
    
    }
    
    public boolean isContentVisible ()
    {
        
        return this.getComponent (1).isVisible ();
        
    }
        
    public void setIconType (String i)
    {
        
        if (i != null)
        {
            
            ImageIcon ii = Environment.getIcon (i,
                                                Constants.ICON_SIDEBAR);
            
            this.header.setIcon (ii);
            
        }
        
    }
    
    public String getTitle ()
    {
        
        return this.title;
        
    }

    public void setTitle (String s)
    {
        
        this.title = Environment.replaceObjectNames (s);
        
        this.header.setTitle (s);
        
    }
    
    public Header getHeader ()
    {

        return this.header;        
        
    }
    
    public JComponent getContent ()
    {
        
        return this.content;
        
    }
    
    public void setContent (JComponent c)
    {
        
        if (!this.inited)
        {
            
            this.init ();
                        
        }
        
        boolean vis = true;
        
        if (this.content != null)
        {
            
            vis = this.content.isVisible ();

            this.remove (this.content);
            
        }
                        
        this.content = c;
        
        this.content.setVisible (vis);
        
        this.add (c,
                  1);

        this.validate ();
        this.repaint ();
        
    }
    
    public void setContentVisible (boolean v)
    {
        
        JComponent c = this.getContent ();

        if (c != null)
        {
        
            c.setVisible (v);
          
        }
        
        this.validate ();
        this.repaint ();
        
    }
    
    public void init ()
    {

        final AccordionItem _this = this;
        
        this.header.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        this.header.setToolTipText ("Click to open/close the items below.");
        this.header.addMouseListener (new MouseAdapter ()
        {

            public void mouseReleased (MouseEvent ev)
            {

                if (ev.isPopupTrigger ())
                {

                    _this.createHeaderPopupMenu (ev);

                } else {
                    
                    JComponent c = _this.getContent ();
                    
                    if (c != null)
                    {
                    
                        _this.setContentVisible (!c.isVisible ());
            
                        _this.revalidate ();
                        _this.repaint ();                    

                    }
                    
                }

            }

            public void mousePressed (MouseEvent ev)
            {

                if (ev.isPopupTrigger ())
                {

                    _this.createHeaderPopupMenu (ev);

                }

            }
            
        });
        
        JComponent c = this.getContent ();

        if (c != null)
        {
            
            c.setAlignmentX (Component.LEFT_ALIGNMENT);
            
        }
        
        this.add (this.header);
                
        if (c != null)
        {
        
            this.add (c);
        
        }
        
        this.inited = true;
        
    }
    
    public void setHeaderControls (JComponent c)
    {
        
        this.header.setControls (c);
        
    }
    
    public void addHeaderPopupMenuItem (String         title,
                                        String         icon,
                                        ActionListener action)
    {
        
        this.headerMenuItems.add (UIUtils.createMenuItem (title,
                                                          icon,
                                                          action));
                                                                   
    }
    
    private void createHeaderPopupMenu (MouseEvent ev)
    {
        
        if (this.headerMenuItems.size () == 0)
        {
            
            return;
            
        }
        
        final JPopupMenu m = new JPopupMenu ();

        for (JMenuItem mi : this.headerMenuItems)
        {
            
            m.add (mi);
            
        }
        
        m.show ((Component) ev.getSource (),
                ev.getX (),
                ev.getY ());

        
    }
    
}