package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class MultiTextUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public MultiTextUserConfigurableObjectTypeField ()
    {
        
        super (Type.multitext);
                
    }

    protected MultiTextUserConfigurableObjectTypeField (Type type)
    {
        
        super (type);
                
    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new MultiTextUserConfigurableObjectFieldViewEditHandler (this,
                                                                        obj,
                                                                        field,
                                                                        viewer);
        
    }
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new MultiTextUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
            
    public void setDisplayAsBullets (boolean v)
    {
        
        this.setDefinitionValue ("displayAsBullets", v);        
        
    }
    
    public boolean isDisplayAsBullets ()
    {
    
        return this.getBooleanDefinitionValue ("displayAsBullets");        
        
    }
    
    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        
        
}
