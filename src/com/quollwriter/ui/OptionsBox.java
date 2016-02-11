package com.quollwriter.ui;

import java.awt.Component;

import javax.swing.*;
import javax.swing.border.*;

public class OptionsBox extends Box
{
    
    private AbstractViewer viewer = null;
    
    public OptionsBox (AbstractViewer viewer)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.viewer = viewer;
        
    }
    
    public void addMain (String     text,
                         JComponent item)
    {
                
        if (this.getComponentCount () > 0)
        {
            
            this.add (Box.createVerticalStrut (10));
                
        }
        
        this.add (this.mainItem (this.text (text)));
                
        this.add (Box.createVerticalStrut (5));

        this.add (this.subItem (this.wrap (item),
                                5));
        
    }

    public void addMain (JComponent item)
    {
                
        if (this.getComponentCount () > 0)
        {
            
            this.add (Box.createVerticalStrut (10));
                
        }
                        
        this.add (this.subItem (this.wrap (item),
                                5));
        
    }

    public void addSub (JComponent header,
                        JComponent items)
    {
                
        if (this.getComponentCount () > 0)
        {
            
            this.add (Box.createVerticalStrut (10));
                
        }
        
        this.add (this.subItem (this.wrap (header),
                                5));
                
        this.add (Box.createVerticalStrut (5));

        this.add (this.subItem (this.wrap (items),
                                10));
        
    }
    
    public void addSub (String     text,
                        JComponent items)
    {
                
        if (this.getComponentCount () > 0)
        {
            
            this.add (Box.createVerticalStrut (10));
                
        }
        
        this.add (this.subItem (this.text (text),
                                5));
                
        this.add (Box.createVerticalStrut (5));

        this.add (this.subItem (this.wrap (items),
                                10));
        
    }

    public void addSub (JComponent items)
    {
                
        if (this.getComponentCount () > 0)
        {
            
            this.add (Box.createVerticalStrut (10));
                
        }
                        
        this.add (this.subItem (this.wrap (items),
                                5));
        
    }

    private JComponent mainItem (JComponent c)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);    
    
        c.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 0),
                                         c.getBorder ()));
        
        return c;
        
    }

    private JComponent subItem (JComponent c,
                                int        indent)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);    
    
        c.setBorder (new CompoundBorder (UIUtils.createPadding (0, indent, 0, 0),
                                         c.getBorder ()));
        
        return c;
        
    }
    
    private JComponent wrap (JComponent c)
    {
        
        if ((c instanceof JComboBox)
            ||
            (c instanceof JCheckBox)
           )
        {

            //c.setMaximumSize (c.getPreferredSize ());

        }

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);

        if (!(c instanceof Box))
        {

            Box _b = new Box (BoxLayout.X_AXIS);
            //_b.add (Box.createHorizontalStrut (5));
            _b.add (c);
            _b.add (Box.createHorizontalGlue ());
            _b.setAlignmentX (Component.LEFT_ALIGNMENT);
            _b.setAlignmentY (Component.TOP_ALIGNMENT);

            c = _b;
                        
        } else {
            
            //c.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            
        }
        
        return c;
    
    }
    
    private JTextPane text (String text)
    {

        JTextPane t = UIUtils.createHelpTextPane (text,
                                                  this.viewer);
        t.setBorder (UIUtils.createPadding (0,
                                      0,
                                      0,
                                      0));

        return t;

    }
    
}