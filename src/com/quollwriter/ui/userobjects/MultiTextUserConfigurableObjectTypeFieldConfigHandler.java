package com.quollwriter.ui.userobjects;

import javax.swing.*;
import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class MultiTextUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{
    
    private CheckboxFormItem displayAsBullets = null;
    private CheckboxFormItem isOtherNames = null;
    
    private MultiTextUserConfigurableObjectTypeField field = null;
    
    public MultiTextUserConfigurableObjectTypeFieldConfigHandler (MultiTextUserConfigurableObjectTypeField f)
    {
        
        this.field = f;

        this.isOtherNames = new CheckboxFormItem (null,
                                                  this.replaceObjName ("Is other names/aliases for the %s"),
                                                  false,
                                                  this.replaceObjName ("Check this box to mark this field as other name or aliases for the %s.  Separate each name/alias with a new line, a comma or a semi-colon."));
        
        this.displayAsBullets = new CheckboxFormItem (null,
                                                      "Display as bullet points",
                                                      false,
                                                      "Check this box to display the text in the field as a series of bullet points.  Each separate line of text will be treated as a bullet point.");
                
    }
            
    public String getObjName ()
    {
        
        return this.field.getUserConfigurableObjectType ().getObjectTypeName ().toLowerCase ();
          
    }
    
    public String replaceObjName (String s)
    {
        
        return StringUtils.replaceString (s,
                                          "%s",
                                          this.getObjName ());
        
    }
            
    @Override
    public boolean updateFromExtraFormItems ()
    {
                    
        this.field.setDisplayAsBullets (this.displayAsBullets.isSelected ());
        this.field.setNameField (this.isOtherNames.isSelected ());
                    
        return true;           
        
    }
    
    @Override
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {
        
        Set<String> errors = new LinkedHashSet ();
                                             
        return errors;
        
    }
    
    @Override
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();

        this.displayAsBullets.setSelected (this.field.isDisplayAsBullets ());
        this.isOtherNames.setSelected (this.field.isNameField ());
                  
        nitems.add (this.displayAsBullets);

        nitems.add (this.isOtherNames);
     
        return nitems;
                                  
    }

    @Override
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();

        strs.add ("multi-line text");
        
        if (this.field.isNameField ())
        {
            
            strs.add (this.replaceObjName ("is other names/aliases for the %s"));

        }
            
        if (this.field.isDisplayAsBullets ())
        {
                        
            strs.add ("displayed as bullet points");
            
        }
                
        return Utils.joinStrings (strs,
                                  null);
                
    }
    
}