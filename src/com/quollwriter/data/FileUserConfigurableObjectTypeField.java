package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class FileUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public FileUserConfigurableObjectTypeField ()
    {
        
        super (Type.file);
                    
    }
    
    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new FileUserConfigurableObjectFieldViewEditHandler (this,
                                                                   obj,
                                                                   field,
                                                                   viewer);
        
    }
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new FileUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
}
