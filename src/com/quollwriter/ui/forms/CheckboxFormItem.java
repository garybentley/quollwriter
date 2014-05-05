package com.quollwriter.ui.forms;

import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.*;

public class CheckboxFormItem extends FormItem<Set<String>>
{
    
    private Set<JCheckBox> boxes = null;
    private int maxCount = 0;
    private boolean required = false;
    private Box container = new Box (BoxLayout.Y_AXIS);
    
    public CheckboxFormItem (String label,
                             Vector<String> items,
                             Set<String>    selected,
                             boolean        itemsRequired,
                             String         helpText)
    {
        
        super (label,
               itemsRequired,
               helpText);

        this.boxes = new LinkedHashSet ();
               
        for (String s : items)
        {
            
            if (this.boxes.size () > 0)
            {
                
                this.container.add (Box.createVerticalStrut (5));
                
            }
            
            JCheckBox b = new JCheckBox (s, (selected != null ? selected.contains (s) : false));
            
            b.setOpaque (false);
            
            this.boxes.add (b);
            
            this.container.add (b);
            
        }
            
        this.required = itemsRequired;
    
    }

    public JComponent getComponent ()
    {

        return this.container;

    }

    public Set<String> getValue ()
    {
                    
        Set<String> ret = new LinkedHashSet ();
        
        for (JCheckBox b : this.boxes)
        {
            
            if (b.isSelected ())
            {
            
                ret.add (b.getText ());
                
            }
            
        }
        
        return ret;
        
    }
    
    public boolean hasError ()
    {
        
        if (!this.required)
        {
            
            return false;
            
        }
        
        for (JCheckBox b : this.boxes)
        {
            
            if (b.isSelected ())
            {

                return false;
            
            }
            
        }
            
        return true;
        
    }
    
    public void updateRequireLabel (JLabel requireLabel)
    {
    
        this.setError (this.hasError ());
    
    }
    
}
