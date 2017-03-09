package com.quollwriter.ui.userobjects;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

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

    @Override
    public boolean updateFromExtraFormItems ()
    {
    
        return true;
    
    }
    
    @Override    
    public String getConfigurationDescription ()
    {
        
        return "Is the object image";
        
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
