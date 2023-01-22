package com.quollwriter.ui.forms;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import java.util.Date;

import javax.swing.*;
import javax.swing.text.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.text.*;

public class DateFormItem extends FormItem<Date>
{
    
    private JDateChooser chooser = null;
        
    public DateFormItem (Object label)
    {
        
        this (label,
              null,
              null,
              null);
        
    }
    
    public DateFormItem (Object     label,
                         Date       minimum,
                         Date       maximum,
                         Date       def)
    {
        
        super (label,
               false,
               null);

        this.chooser = new JDateChooser ();

        if (minimum != null)
        {
            
            this.chooser.setMinSelectableDate (minimum);
            
        }
        
        if (maximum != null)
        {
            
            this.chooser.setMaxSelectableDate (maximum);
            
        }

        if (def != null)
        {
            
            this.chooser.setDate (def);
            
        }
        
        this.chooser.getCalendarButton ().setMargin (new java.awt.Insets (3, 3, 3, 3));
        this.chooser.setIcon (Environment.getIcon ("date",
                                                   16));               
                       
    }
            
    @Override
    public JComponent getComponent ()
    {
        
        return this.chooser;
        
    }
    
    public void setDate (Date d)
    {
        
        this.chooser.setDate (d);
        
    }
    
    @Override
    public Date getValue ()
    {
        
        return this.chooser.getDate ();
    
    }
    
    @Override
    public boolean hasError ()
    {
    
        return false;
                    
    }
        
}
