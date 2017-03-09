package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.math.*;

import javax.swing.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.components.FormItem;

public class SelectUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String ITEMS = "items";
    public static final String ALLOWMULTI = "allowmulti";

    public SelectUserConfigurableObjectTypeField ()
    {
        
        super (Type.select);
        
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
        
        return new SelectUserConfigurableObjectFieldViewEditHandler (this,
                                                                     obj,
                                                                     field,
                                                                     viewer);
        
    }
        
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new SelectUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
        
    public void setItems (Collection<String> its)
    {
        
        this.setDefinitionValue (ITEMS, new ArrayList (its));
        
    }
    
    public Collection<String> getItems ()
    {
        
        return (Collection<String>) this.getDefinitionValue (ITEMS);
        
    }
    
    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        
                
    public boolean isAllowMulti ()
    {
        
        return this.getBooleanDefinitionValue (ALLOWMULTI);
        
    }
    
    public void setAllowMulti (boolean v)
    {
        
        this.setDefinitionValue (ALLOWMULTI, v);
        
    }
    
}
