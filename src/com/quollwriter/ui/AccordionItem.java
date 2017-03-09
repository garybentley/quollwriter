package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.GradientPainter;
import com.quollwriter.ui.components.Header;

public abstract class AccordionItem extends Box
{
    
    protected Header header = null;
    //private List<JMenuItem> headerMenuItems = new ArrayList ();
    private String title = null;
    //private JComponent content = null;
    private boolean inited = false;
    
    public AccordionItem (String    title)
    {

        this (title,
              (String) null);
    
    }
    
    public AccordionItem (String    title,
                          ImageIcon icon)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.initHeader (title,
                         icon);
                                
    }
    
    public AccordionItem (String title,
                          String iconType)
    {
        
        super (BoxLayout.Y_AXIS);
                
        ImageIcon ii = null;
        
        if (iconType != null)
        {
            
            ii = Environment.getIcon (iconType,
                                      Constants.ICON_SIDEBAR);
            
        }
        
        this.initHeader (title,
                         ii);
                                
    }

    public abstract JComponent getContent ();    
    
    public void initFromSaveState (String ss)
    {
        
        this.init ();
        
        Map<String, Object> state = (Map<String, Object>) JSONDecoder.decode (ss);
        
        Boolean vis = (Boolean) state.get ("contentVisible");
        
        this.setContentVisible ((vis != null ? vis : false));
        
    }
    
    public Map<String, Object> getSaveStateAsMap ()
    {
        
        Map<String, Object> ss = new HashMap ();
        
        ss.put ("contentVisible",
                this.isContentVisible ());
            
        return ss;
            
    }
    
    public String getSaveState ()
    {
        
        try
        {
        
            return JSONEncoder.encode (this.getSaveStateAsMap ());
        
        } catch (Exception e) {
            
            Environment.logError ("Unable to encode save state: " +
                                  this.getSaveStateAsMap (),
                                  e);
            
            return "";
            
        }
        
    }
    
    private void initHeader (String    title,
                             ImageIcon icon)
    {
        
        this.title = Environment.replaceObjectNames (title);
        
        final Header h = new Header (this.title,
                                     icon,
                                     null);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));

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
    
    public String getId ()
    {
        
        return null;
        
    }
    
    public boolean isContentVisible ()
    {
        
        return this.getContent ().isVisible ();
        
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
    
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {
        
    }
    
    public void setSaveState (String s)
    {
        
        Map<String, Object> m = (Map<String, Object>) JSONDecoder.decode (s);
        
        this.setContentVisible ((Boolean) m.get ("contentVisible"));
        
    }
    
    public void init ()
    {

        if (this.inited)
        {
            
            return;
            
        }
    
        final AccordionItem _this = this;
        
        this.header.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        this.header.setToolTipText ("Click to open/close the items below.");
        
        this.header.addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
                
                _this.fillHeaderPopupMenu (m,
                                           ev);
                
            }
            
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                JComponent c = _this.getContent ();
                
                if (c != null)
                {

                    c.setVisible (!c.isVisible ());

                    _this.revalidate ();
                    _this.repaint ();                    

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
    
    public JComponent getHeaderControls ()
    {
        
        return (JComponent) this.header.getControls ();
        
    }
    
}