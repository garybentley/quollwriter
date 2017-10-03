package com.quollwriter.ui.userobjects;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class ObjectImageUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private ObjectImageUserConfigurableObjectTypeField field = null;
    
    public ObjectImageUserConfigurableObjectTypeFieldConfigHandler (ObjectImageUserConfigurableObjectTypeField f)
    {
        
        this.field = f;
                    
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
    public boolean updateFromExtraFormItems ()
    {
    
        return true;
    
    }
    
    @Override    
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();
                
        strs.add (this.replaceObjName (Environment.getUIString (LanguageStrings.form,
                                                                LanguageStrings.config,
                                                                LanguageStrings.types,
                                                                UserConfigurableObjectTypeField.Type.objectimage.getType (),
                                                                LanguageStrings.description)));
        
        return Utils.joinStrings (strs,
                                  null);
        
        //return "Is the object image";
        
    }
        
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {
        
        Set<String> errors = new LinkedHashSet ();
                             
        return errors;
        
    }
    
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();
                            
        return nitems;
                                  
    }

}
