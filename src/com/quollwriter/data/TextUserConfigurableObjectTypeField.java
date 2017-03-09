package com.quollwriter.data;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

/**
 * Models a text user configurable object field.
 */
public class TextUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public TextUserConfigurableObjectTypeField ()
    {
        
        super (Type.text);
        
    }

    @Override
    public boolean isSortable ()
    {
        
        return true;
        
    }
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new TextUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new TextUserConfigurableObjectFieldViewEditHandler (this,
                                                                   obj,
                                                                   field,
                                                                   viewer);
        
    }

    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        

}
