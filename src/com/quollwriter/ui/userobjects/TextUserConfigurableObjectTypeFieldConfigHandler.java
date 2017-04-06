package com.quollwriter.ui.userobjects;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class TextUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private TextUserConfigurableObjectTypeField field = null;
    private CheckboxFormItem isOtherNames = null;
    
    public TextUserConfigurableObjectTypeFieldConfigHandler (TextUserConfigurableObjectTypeField field)
    {
        
        this.field = field;

        this.isOtherNames = new CheckboxFormItem (null,
                                                  this.replaceObjName ("Is other names/aliases for the %s"),
                                                  false,
                                                  this.replaceObjName ("Check this box to mark this field as other name or aliases for the %s.  Separate each name/alias with a new line, a comma or a semi-colon."));
        
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
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();

        strs.add ("single line text");
                
        if (this.field.isNameField ())
        {
            
            strs.add (this.replaceObjName ("is other names/aliases for the %s"));

        }
                
        return Utils.joinStrings (strs,
                                  null);
        
    }
    
    @Override
    public boolean updateFromExtraFormItems ()
    {
                    
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

        this.isOtherNames.setSelected (this.field.isNameField ());
                  
        nitems.add (this.isOtherNames);
     
        return nitems;
                                  
    }

}
