package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;

import javax.swing.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class DateUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String def = "def";
    public static final String max = "max";
    public static final String min = "min";

    public DateUserConfigurableObjectTypeField ()
    {
        
        super (Type.date);
                
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
        
        return new DateUserConfigurableObjectFieldViewEditHandler (this,
                                                                   obj,
                                                                   field,
                                                                   viewer);
        
    }
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new DateUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
    public void setMaximum (Date d)
    {
        
        this.setDefinitionValue (max, (d != null ? Environment.formatDate (d) : null));
        
    }
    
    public Date getMaximum ()
    {
        
        return this.getDateDefinitionValue (max);
        
    }
    
    public void setMinimum (Date d)
    {
        
        this.setDefinitionValue (min, (d != null ? Environment.formatDate (d) : null));
        
    }
    
    public Date getMinimum ()
    {
        
        return this.getDateDefinitionValue (min);
        
    }
    
    public void setDefault (Date d)
    {
        
        this.setDefinitionValue (def, (d != null ? Environment.formatDate (d) : null));
        
    }
    
    public Date getDefault ()
    {

        return this.getDateDefinitionValue (def);
        
    }
    
}
