package com.quollwriter.ui.userobjects;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import java.math.*;
import java.text.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class NumberUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private TextFormItem minsp = null;
    private TextFormItem maxsp = null;
    
    private NumberUserConfigurableObjectTypeField field = null;
            
    public NumberUserConfigurableObjectTypeFieldConfigHandler (NumberUserConfigurableObjectTypeField f)
    {
        
        this.field = f;

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.number.getType ());

        this.minsp = new TextFormItem (Environment.getUIString (prefix,
                                                                LanguageStrings.min,
                                                                LanguageStrings.text),
                                       null);
        //"Minimum", null);
        this.maxsp = new TextFormItem (Environment.getUIString (prefix,
                                                                LanguageStrings.max,
                                                                LanguageStrings.text),
                                       null);
        //"Maximum", null);
        
    }

    @Override    
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();
        
        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.number.getType ());
        
        strs.add (Environment.getUIString (prefix,
                                           LanguageStrings.description));
        //"number");
        
        try
        {
        
            if (this.field.getMinimum () != null)
            {
                
                strs.add (String.format (Environment.getUIString (prefix,
                                                                  LanguageStrings.min,
                                                                  LanguageStrings.description),
                                         Environment.formatNumber (this.field.getMinimum ())));
                
            }
            
            if (this.field.getMaximum () != null)
            {
                            
                strs.add (String.format (Environment.getUIString (prefix,
                                                                  LanguageStrings.max,
                                                                  LanguageStrings.description),
                                         Environment.formatNumber (this.field.getMaximum ())));
                //strs.add ("max: " + Environment.formatNumber (this.field.getMaximum ()));
                
            }

        } catch (Exception e) {
            
            Environment.logError ("Unable to format number",
                                  e);
            
        }
        
        return Utils.joinStrings (strs,
                                  null);

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {
        
        try
        {
            
            this.field.setMinimum (this.convertMin ());
        
            this.field.setMaximum (this.convertMax ());
            
        } catch (Exception e) {
            
            // This should never happen since we check the value first for errors.
            // This is just to satisfy the compiler.
            Environment.logError ("Unable to set minimum/maximum value",
                                  e);
            
            return false;
            
        }
        
        return true;
        
    }
    
    private Double convertMax ()
                        throws GeneralException
    {
        
        return this.convert (this.maxsp);
        
    }
    
    private Double convertMin ()
                        throws GeneralException
    {
        
        return this.convert (this.minsp);
        
    }

    private Double convert (TextFormItem f)
                     throws GeneralException
    {
        
        if (f.getText () == null)
        {
            
            return null;
            
        }
        
        String v = f.getText ().trim ();
                
        if (v.length () > 0)
        {
            
            return Environment.parseToDouble (v);
            
        }
        
        return null;
        
    }
    
    @Override
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {
        
        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.number.getType ());
        prefix.add (LanguageStrings.errors);
        
        Set<String> errors = new LinkedHashSet ();

        Double min = null;
        
        try
        {
        
            min = this.convertMin ();
            
        } catch (Exception e) {
            
            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings.min,
                                                 LanguageStrings.invalidvalue));
            //"The minimum value doesn't look like a number.");
            
        }
        
        Double max = null;
        
        try
        {
            
            max = this.convertMax ();
            
        } catch (Exception e) {
            
            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings.max,
                                                 LanguageStrings.invalidvalue));
            //errors.add ("The maximum value doesn't look like a number.");
            
        }            
        
        if (errors.size () > 0)
        {
            
            return errors;
            
        }
        
        if ((min != null)
            &&
            (max != null)
            &&
            (max.compareTo (min) < 0)
           )
        {
        
            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings.max,
                                                 LanguageStrings.greaterthanmin));
            //errors.add ("The maximum value must be greater than the minimum.");
            
        }
        
        return errors;
        
    }

    @Override
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();

        Double min = null;
        
        try
        {
            
            min = this.field.getMinimum ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get the minimum value.",
                                  e);
            
        }
        
        Double max = null;
        
        try
        {
            
            max = this.field.getMaximum ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get the maximum value.",
                                  e);
            
        }
        
        if (min != null)
        {
        
            this.minsp.setText (Environment.formatNumber (min));
            
        }
        
        if (max != null)
        {
            
            this.maxsp.setText (Environment.formatNumber (max));
        
        }
        
        nitems.add (this.minsp);
            
        nitems.add (this.maxsp);

        return nitems;
    
    }

}
