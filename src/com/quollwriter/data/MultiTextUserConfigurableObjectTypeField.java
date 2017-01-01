package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.FormItem;

public class MultiTextUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String ISOBJECTDESC = "isobjectdesc";

    private boolean isObjectDesc = false;
    private JCheckBox editIsObjectDesc = null;
    private boolean displayAsBullets = false;

    public MultiTextUserConfigurableObjectTypeField ()
    {
        
        super (Type.multitext);
        
        this.editIsObjectDesc = UIUtils.createCheckBox ("Is the object description");
        
    }

    public void setDisplayAsBullets (boolean v)
    {
        
        this.displayAsBullets = v;
        
    }
    
    public boolean isDisplayAsBullets ()
    {
    
        return this.displayAsBullets;
        
    }
    
    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        

    public void initConfiguration (Map initVal)
    {
                    
        this.isObjectDesc = (Boolean) initVal.get (ISOBJECTDESC);
                    
    }
    
    public void fillConfiguration (Map m)
    {
                    
        m.put (ISOBJECTDESC,
               this.isObjectDesc);
                 
    }
    
    public String getConfigurationDescription ()
    {
        
        return (this.isObjectDesc ? "Is the object description" : "");
        
    }
    
    public boolean updateFromExtraFormItems ()
    {
                    
        this.isObjectDesc = this.editIsObjectDesc.isSelected ();
                    
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
     
        nitems.add (new FormItem (null,
                                  this.editIsObjectDesc));
     
        return nitems;
                                  
    }

}
