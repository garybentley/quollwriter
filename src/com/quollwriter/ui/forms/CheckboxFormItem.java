package com.quollwriter.ui.forms;

import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;

import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.quollwriter.ui.*;

public class CheckboxFormItem extends FormItem<Boolean>
{
    
    private JCheckBox checkbox = null;

    public CheckboxFormItem (String label,
                             String checkboxText)
    {
        
        this (label,
              checkboxText,
              false,
              null);
        
    }
    
    public CheckboxFormItem (String  label,
                             String  checkboxText,
                             boolean selected,
                             String  helpText)
    {
        
        super (label,
               false,
               helpText);

        this.checkbox = UIUtils.createCheckBox (checkboxText);
                   
        if (helpText != null)
        {
            
            this.checkbox.setToolTipText (helpText);
                   
        }
        
    }

    public void addItemListener (ItemListener l)
    {
        
        this.checkbox.addItemListener (l);
        
    }
    
    public void removeItemListener (ItemListener l)
    {
        
        this.checkbox.removeItemListener (l);
        
    }
    
    public void setSelected (boolean v)
    {
        
        this.checkbox.setSelected (v);
        
    }
    
    public boolean isSelected ()
    {
        
        return this.checkbox.isSelected ();
        
    }
    
    @Override
    public JComponent getComponent ()
    {

        return this.checkbox;

    }

    @Override
    public Boolean getValue ()
    {
                  
        return this.checkbox.isSelected ();
        
    }
    
    public boolean hasError ()
    {
        
        return false;
        
    }
        
}
