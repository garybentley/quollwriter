package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.components.FormItem;

public class WebpageUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{
    
    public WebpageUserConfigurableObjectTypeField ()
    {
        
        super (Type.webpage);
                    
    }

    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        

    public void initConfiguration (Map initVal)
    {
                    
    }
    
    public void fillConfiguration (Map m)
    {
                                         
    }
    
    public String getConfigurationDescription ()
    {
        
        return "";
        
    }
    
    public boolean updateFromExtraFormItems ()
    {
                    
        return true;           
        
    }
    
    public Set<String> getExtraFormItemErrors ()
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
