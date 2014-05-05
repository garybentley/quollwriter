package com.quollwriter.ui.forms;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

public abstract class FormItem<E> extends Box
{
    
    private JLabel requireLabel = null;
    private JLabel label = null;
    protected boolean notNull = false;
    private String helpText = null;
            
    public FormItem (String  label,
                     boolean notNull,
                     String  helpText)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.label = new JLabel (Environment.replaceObjectNames (label));
        this.requireLabel = new JLabel ();
        this.requireLabel.setForeground (UIUtils.getColor ("#999999"));
        this.helpText = helpText;
        this.notNull = notNull;
                        
    }

    public abstract void updateRequireLabel (JLabel l);
    public abstract JComponent getComponent ();
    public abstract boolean hasError ();
    public abstract E getValue ();
    
    public void scrollIntoView ()
    {
        
        Environment.scrollIntoView (this.label);
        //UIUtils.scrollIntoView (this.label);
        
    }
    
    public JLabel getLabel ()
    {
        
        return this.label;
        
    }
    
    public void init ()
    {
        
        this.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box b = new Box (BoxLayout.X_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.setBorder (new EmptyBorder (0,
                                      5,
                                      5,
                                      5));
        
        b.add (this.label);
            
        b.add (Box.createHorizontalStrut (5));
            
        b.add (this.requireLabel);
        
        this.add (b);
        
        this.add (this.setAsSubItem (this.createWrapper (this.getComponent ())));

        if (this.helpText != null)
        {
            
            this.add (Box.createVerticalStrut (2));                        
            this.add (this.createHelpText (this.helpText));                
            
        }

        this.add (Box.createVerticalStrut (15));                        
        
        this.updateRequireLabel (this.requireLabel);

        
    }
    
    public void updateRequireLabel ()
    {
        
        this.updateRequireLabel (this.requireLabel);
        
    }
    
    public void setError (boolean err)
    {
        
        if (err)
        {
            
            this.label.setForeground (Color.red);
            this.requireLabel.setForeground (Color.red);
            
        } else {
            
            this.label.setForeground (Color.black);
            this.requireLabel.setForeground (UIUtils.getColor ("#999999"));
            
        }            
        
    }

    private JComponent createWrapper (JComponent c)
    {
        
        if (c instanceof JComboBox)
        {

            c.setMaximumSize (c.getPreferredSize ());

        }

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);

        if (!(c instanceof Box))
        {

            Box _b = new Box (BoxLayout.X_AXIS);
            _b.add (Box.createHorizontalStrut (5));
            _b.add (c);
            _b.add (Box.createHorizontalGlue ());
            _b.setAlignmentX (Component.LEFT_ALIGNMENT);
            _b.setAlignmentY (Component.TOP_ALIGNMENT);

            c = _b;
                        
        } else {
            
            c.setBorder (new EmptyBorder (0, 5, 0, 0));
            
        }
        
        return c;
    
    }
    
    private JComponent setAsSubItem (JComponent c)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);    
    
        c.setBorder (new CompoundBorder (new EmptyBorder (0, 5, 0, 0),
                                         c.getBorder ()));
        
        return c;
        
    }
            
    protected JScrollPane createScrollPane (JComponent t)
    {

        JScrollPane sp = new JScrollPane (t);
        sp.setOpaque (false);
        sp.setBorder (new CompoundBorder (new EmptyBorder (0,
                                                           0,
                                                           0,
                                                           5),
                                          UIUtils.createLineBorder ()));
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.getVerticalScrollBar ().setUnitIncrement (20);
        sp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                          t.getPreferredSize ().height + sp.getInsets ().top + sp.getInsets ().bottom));
        //sp.setPreferredSize (sp.getMaximumSize ());

        return sp;

    }
    
    private JTextPane createHelpText (String text)
    {

        JTextPane t = UIUtils.createHelpTextPane (text,
                                                  null);
        t.setBorder (new EmptyBorder (0,
                                      10,
                                      0,
                                      5));
        t.enableInputMethods (false);
        t.setFocusable (false);

        return t;

    }
    
    
}
