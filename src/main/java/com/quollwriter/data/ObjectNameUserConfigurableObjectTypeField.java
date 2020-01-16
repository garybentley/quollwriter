package com.quollwriter.data;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

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
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new ObjectNameUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public ObjectNameUserConfigurableObjectFieldViewEditHandler getViewEditHandler2 (UserConfigurableObject      obj,
                                                                           UserConfigurableObjectField field,
                                                                           IPropertyBinder             binder,
                                                                           AbstractProjectViewer       viewer)
    {

        return new ObjectNameUserConfigurableObjectFieldViewEditHandler (this,
                                                                         obj,
                                                                         field,
                                                                         viewer);

    }

    @Override
    // TODO Remove
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.ObjectNameUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    // TODO Remove
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return new com.quollwriter.ui.userobjects.ObjectNameUserConfigurableObjectFieldViewEditHandler (this,
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
