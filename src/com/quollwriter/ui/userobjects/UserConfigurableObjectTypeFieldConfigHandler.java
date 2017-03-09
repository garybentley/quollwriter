package com.quollwriter.ui.userobjects;

import javax.swing.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public interface UserConfigurableObjectTypeFieldConfigHandler
{
    
    public Set<FormItem> getExtraFormItems ();
    
    public boolean updateFromExtraFormItems ();
    
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType);
    
    public String getConfigurationDescription ();
        
}