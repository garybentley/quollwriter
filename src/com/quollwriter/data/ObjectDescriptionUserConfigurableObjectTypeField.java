package com.quollwriter.data;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

/**
 * Models a text user configurable object field.
 */
public class ObjectDescriptionUserConfigurableObjectTypeField extends MultiTextUserConfigurableObjectTypeField
{

    public ObjectDescriptionUserConfigurableObjectTypeField ()
    {
        
        super (Type.objectdesc);

        this.setDefinitionValue ("isObjectDesc", true);        
        
    }
    
    @Override
    public boolean canDelete ()
    {
        
        return true;
        
    }    
    
    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {
        
        return new ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler (this);
        
    }
    
    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {
        
        return new ObjectDescriptionUserConfigurableObjectFieldViewEditHandler (this,
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
                
    }
    
    @Override
    public boolean isNameField ()
    {
        
        return false;
        
    }
        
}
