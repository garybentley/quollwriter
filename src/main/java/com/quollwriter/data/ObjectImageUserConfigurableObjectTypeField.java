package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class ObjectImageUserConfigurableObjectTypeField extends ImageUserConfigurableObjectTypeField
{

    public ObjectImageUserConfigurableObjectTypeField ()
    {
        
        super (Type.objectimage);
                    
    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new ObjectImageUserConfigurableObjectFieldViewEditHandler (this,
                                                                          obj,
                                                                          field,
                                                                          viewer);
        
    }

    public boolean isObjectImage ()
    {
        
        return true;
        
    }
        
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new ObjectImageUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
}
