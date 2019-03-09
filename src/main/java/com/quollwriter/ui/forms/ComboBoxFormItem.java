package com.quollwriter.ui.forms;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class ComboBoxFormItem<E> extends FormItem<E>
{
    
    private JComboBox<E> combo = null;
    
    public ComboBoxFormItem (String        label,
                             Collection<E> values)
    {
        
        this (label,
              values,
              null,
              null);
        
    }
    
    public ComboBoxFormItem (String        label,
                             Collection<E> values,
                             E             defaultValue,
                             String        helpText)
    {
        
        super (label,
               true,
               helpText);
        
        this.combo = new JComboBox<E> (new Vector (values));
        
        if (defaultValue != null)
        {
            
            this.combo.setSelectedItem (defaultValue);
            
        }
        
    }

    public void setRenderer (ListCellRenderer<E> r)
    {
        
        this.combo.setRenderer (r);
        
    }
    
    public int getSelectedIndex ()
    {
        
        return this.combo.getSelectedIndex ();
        
    }

    public void setSelectedItem (E v)
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
    public E getValue ()
    {
        
        return (E) this.combo.getSelectedItem ();//.toString ();
        
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
