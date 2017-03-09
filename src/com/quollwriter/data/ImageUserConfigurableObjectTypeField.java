package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class ImageUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    protected ImageUserConfigurableObjectTypeField (Type type)
    {
        
        super (type);
        
    }

    public ImageUserConfigurableObjectTypeField ()
    {
        
        super (Type.image);
                    
    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new ImageUserConfigurableObjectFieldViewEditHandler (this,
                                                                    obj,
                                                                    field,
                                                                    viewer);
        
    }
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new ImageUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
}
