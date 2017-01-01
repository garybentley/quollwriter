package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.components.FormItem;

public class FileUserObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String ISOBJECTIMAGE = "isobjectimage";

    private boolean isObjectImage = false;

    public FileUserObjectTypeField ()
    {
        
        super (Type.file);
                    
    }

    public void initConfiguration (Map initVal)
    {
                    
        this.isObjectImage = (Boolean) initVal.get (ISOBJECTIMAGE);
                    
    }
    
    public void fillConfiguration (Map m)
    {
                    
        m.put (ISOBJECTIMAGE,
               this.isObjectImage);
                 
    }
    
    public String getConfigurationDescription ()
    {
        
        return (this.isObjectImage ? "Is the object image" : "");
        
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
