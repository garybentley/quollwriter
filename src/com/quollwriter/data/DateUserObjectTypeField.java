package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;

import javax.swing.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.FormItem;

public class DateUserObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String def = "def";
    public static final String max = "max";
    public static final String min = "min";

    private JDateChooser editDefDate = null;
    private JDateChooser editMinDate = null;
    private JDateChooser editMaxDate = null;
    private Date defDate = null;
    private Date minDate = null;
    private Date maxDate = null;

    public DateUserObjectTypeField ()
    {
        
        super (Type.date);
        
        this.editDefDate = new JDateChooser ();

        this.editDefDate.setMinSelectableDate (new Date ());
        this.editDefDate.getCalendarButton ().setMargin (new java.awt.Insets (3, 3, 3, 3));
        this.editDefDate.setIcon (Environment.getIcon ("date",
                                                       16));
                    
        this.editMinDate = new JDateChooser ();

        this.editMinDate.setMinSelectableDate (new Date ());
        this.editMinDate.getCalendarButton ().setMargin (new java.awt.Insets (3, 3, 3, 3));
        this.editMinDate.setIcon (Environment.getIcon ("date",
                                                       16));
        
        this.editMaxDate = new JDateChooser ();

        this.editMaxDate.setMinSelectableDate (new Date ());
        this.editMaxDate.getCalendarButton ().setMargin (new java.awt.Insets (3, 3, 3, 3));
        this.editMaxDate.setIcon (Environment.getIcon ("date",
                                                       16));
        
    }

    public void initConfiguration (Map initVal)
    {
        
        this.defDate = (Date) initVal.get (def);
        this.minDate = (Date) initVal.get (min);
        this.maxDate = (Date) initVal.get (max);
        
    }
    
    public void fillConfiguration (Map m)
    {
                           
        if (this.defDate != null)
        {
            
            m.put (def,
                   this.defDate);
               
        }

        if (this.minDate != null)
        {
            
            m.put (min,
                   this.minDate);
            
        }

        if (this.maxDate != null)
        {
            
            m.put (max,
                   this.maxDate);
                 
        }
        
    }
    
    public String getConfigurationDescription ()
    {
        
        return "|todo|";
        
    }
    
    public boolean updateFromExtraFormItems ()
    {
                    
        this.defDate = this.editDefDate.getDate ();
        this.minDate = this.editMinDate.getDate ();
        this.maxDate = this.editMaxDate.getDate ();
                    
        return true;           
        
    }
    
    public Set<String> getExtraFormItemErrors ()
    {
        
        Set<String> errors = new LinkedHashSet ();
                      
        Date defVal = this.editDefDate.getDate ();
        Date minVal = this.editMinDate.getDate ();
        Date maxVal = this.editMaxDate.getDate ();
        
        if ((minVal != null)
            &&
            (maxVal != null)
            &&
            (minVal.after (maxVal))
           )
        {
            
            errors.add ("The maximum date must be after the minimum.");
            
        }
        
        if ((defVal != null)
            &&
            (minVal != null)
            &&
            (minVal.after (defVal))
           )
        {
            
            errors.add ("The default date must be after the minimum.");
                            
        }
        
        if ((defVal != null)
            &&
            (maxVal != null)
            &&
            (defVal.after (maxVal))
           )
        {
            
            errors.add ("The default date must be before the minimum.");
                            
        }

        return errors;
        
    }
    
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();
     
        nitems.add (new FormItem ("Default",
                                  this.editDefDate));

        nitems.add (new FormItem ("Minimum",
                                  this.editMinDate));

        nitems.add (new FormItem ("Maximum",
                                  this.editMaxDate));
     
        return nitems;
                                  
    }

}
