package com.quollwriter.ui.forms;

import java.util.Vector;

import javax.swing.*;

public class ComboBoxFormItem extends FormItem<String>
{
    
    private JComboBox combo = null;
    
    public ComboBoxFormItem (String         label,
                             Vector<String> values,
                             String         defaultValue,
                             String         helpText)
    {
        
        super (label,
               true,
               helpText);
        
        this.combo = new JComboBox (values);
        
        if (defaultValue != null)
        {
            
            this.combo.setSelectedItem (defaultValue);
            
        }
        
    }

    public String getValue ()
    {
        
        return this.combo.getSelectedItem ().toString ();
        
    }
    
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
