package com.quollwriter.ui.userobjects;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class DateUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private DateFormItem editDefDate = null;
    private DateFormItem editMinDate = null;
    private DateFormItem editMaxDate = null;

    private DateUserConfigurableObjectTypeField field = null;
    
    public DateUserConfigurableObjectTypeFieldConfigHandler (DateUserConfigurableObjectTypeField f)
    {
        
        this.field = f;
                
        this.editDefDate = new DateFormItem ("Default");
        this.editMinDate = new DateFormItem ("Minimum");
        this.editMaxDate = new DateFormItem ("Maximum");
                        
    }
    
    @Override
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();
        
        strs.add ("date");
        
        if (this.field.getMinimum () != null)
        {
            
            strs.add ("min: " + Environment.formatDate (this.field.getMinimum ()));
            
        }
        
        if (this.field.getMaximum () != null)
        {
                        
            strs.add ("max: " + Environment.formatDate (this.field.getMaximum ()));
            
        }
        
        if (this.field.getDefault () != null)
        {
                        
            strs.add ("default: " + Environment.formatDate (this.field.getDefault ()));
            
        }

        return Utils.joinStrings (strs,
                                  null);
                
    }
    
    @Override
    public boolean updateFromExtraFormItems ()
    {
                    
        this.field.setDefault (this.editDefDate.getValue ());
        this.field.setMinimum (this.editMinDate.getValue ());
        this.field.setMaximum (this.editMaxDate.getValue ());
                    
        return true;           
        
    }
    
    @Override
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {
        
        Set<String> errors = new LinkedHashSet ();
                      
        Date defVal = this.editDefDate.getValue ();
        Date minVal = this.editMinDate.getValue ();
        Date maxVal = this.editMaxDate.getValue ();
        
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
            
            errors.add ("The default date must be before the maximum.");
                            
        }

        return errors;
        
    }
    
    @Override
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();
     
        this.editDefDate.setDate (this.field.getDefault ());
        this.editMinDate.setDate (this.field.getMinimum ());
        this.editMaxDate.setDate (this.field.getMaximum ());
     
        nitems.add (this.editDefDate);

        nitems.add (this.editMinDate);

        nitems.add (this.editMaxDate);
     
        return nitems;
                                  
    }

}
