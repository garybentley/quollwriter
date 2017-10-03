package com.quollwriter.ui.userobjects;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class WebpageUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{
    
    private WebpageUserConfigurableObjectTypeField field = null;
    
    public WebpageUserConfigurableObjectTypeFieldConfigHandler (WebpageUserConfigurableObjectTypeField f)
    {
        
        this.field = f;
                    
    }
    
    @Override
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();
        
        strs.add (Environment.getUIString (LanguageStrings.form,
                                           LanguageStrings.config,
                                           LanguageStrings.types,
                                           LanguageStrings.webpage,
                                           LanguageStrings.description));
        //"web page");
        
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
