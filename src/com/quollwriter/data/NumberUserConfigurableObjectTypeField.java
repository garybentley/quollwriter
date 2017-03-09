package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import java.math.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class NumberUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String MAX = "max";
    public static final String MIN = "min";
            
    public NumberUserConfigurableObjectTypeField ()
    {
        
        super (Type.number);
        
    }

    @Override
    public boolean isSortable ()
    {
        
        return true;
        
    }
    
    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new NumberUserConfigurableObjectFieldViewEditHandler (this,
                                                                     obj,
                                                                     field,
                                                                     viewer);
        
    }
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new NumberUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
        
    public Double getMinimum ()
                       throws GeneralException     
    {
        
        return this.getDoubleDefinitionValue (MIN);
        
    }
    
    public void setMinimum (Double v)
    {
        
        this.setDefinitionValue (MIN,
                                 (v != null ? Environment.formatNumber (v) : null));
        
    }
    
    public Double getMaximum ()
                       throws GeneralException     
    {
        
        return this.getDoubleDefinitionValue (MAX);
        
    }
    
    public void setMaximum (Double v)
    {
        
        this.setDefinitionValue (MAX,
                                 (v != null ? Environment.formatNumber (v) : null));
        
    }

}
