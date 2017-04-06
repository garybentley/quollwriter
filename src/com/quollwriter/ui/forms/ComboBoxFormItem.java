package com.quollwriter.ui.forms;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class ComboBoxFormItem extends FormItem<String>
{
    
    private JComboBox combo = null;
    
    public ComboBoxFormItem (String             label,
                             Collection<String> values)
    {
        
        this (label,
              values,
              null,
              null);
        
    }
    
    public ComboBoxFormItem (String             label,
                             Collection<String> values,
                             String             defaultValue,
                             String             helpText)
    {
        
        super (label,
               true,
               helpText);
        
        this.combo = new JComboBox (new Vector (values));
        
        if (defaultValue != null)
        {
            
            this.combo.setSelectedItem (defaultValue);
            
        }
        
    }

    public int getSelectedIndex ()
    {
        
        return this.combo.getSelectedIndex ();
        
    }

    public void setSelectedItem (String v)
    {
        
        this.combo.setSelectedItem (v);
        
    }
    
    public void setEditable (boolean v)
    {
        
        this.combo.setEditable (v);
        
    }

    public void addItemListener (ItemListener l)
    {
        
        this.combo.addItemListener (l);
        
    }
    
    @Override
    public String getValue ()
    {
        
        return this.combo.getSelectedItem ().toString ();
        
    }
    
    @Override
    public JComponent getComponent ()
    {

        return this.combo;
    
    }

    public boolean hasError ()
    {

        return false;
    
    }

    public void updateRequireLabel (JLabel requireLabel)
    {

    }
    
}
