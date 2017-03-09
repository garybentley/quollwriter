package com.quollwriter.data;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

/**
 * Models a text user configurable object field.
 */
public class ObjectNameUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public ObjectNameUserConfigurableObjectTypeField ()
    {
        
        super (Type.objectname);
        
    }

    @Override
    public boolean isSortable ()
    {
        
        return true;
        
    }
    
    @Override
    public boolean canDelete ()
    {
        
        return false;
        
    }    
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new ObjectNameUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new ObjectNameUserConfigurableObjectFieldViewEditHandler (this,
                                                                         obj,
                                                                         field,
                                                                         viewer);
        
    }

    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        
    
    @Override
    public void setNameField (boolean v)
    {
        
        // Ignore.
        
    }
    
    @Override
    public boolean isNameField ()
    {
        
        return true;
        
    }
        
}
