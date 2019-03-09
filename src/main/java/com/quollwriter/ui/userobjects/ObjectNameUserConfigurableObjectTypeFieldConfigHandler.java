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

public class ObjectNameUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private ObjectNameUserConfigurableObjectTypeField field = null;
    
    public ObjectNameUserConfigurableObjectTypeFieldConfigHandler (ObjectNameUserConfigurableObjectTypeField field)
    {
        
        this.field = field;
        
    }
          
    public String getObjName ()
    {
        
        return this.field.getUserConfigurableObjectType ().getObjectTypeName ().toLowerCase ();
          
    }
    
    public String replaceObjName (String s)
    {
        
        return String.format (s,
                              this.getObjName ());
        
    }
            
    @Override
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();
                
        strs.add (this.replaceObjName (Environment.getUIString (LanguageStrings.form,
                                                                LanguageStrings.config,
                                                                LanguageStrings.types,
                                                                UserConfigurableObjectTypeField.Type.objectname.getType (),
                                                                LanguageStrings.description)));
        //"is the %s name"));
        
        return Utils.joinStrings (strs,
                                  null);
        
    }
    
    @Override
    public boolean updateFromExtraFormItems ()
    {
                                      
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
          
        return nitems;
                                  
    }

}
