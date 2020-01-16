package com.quollwriter.data;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.viewers.*;

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
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return new com.quollwriter.ui.userobjects.ObjectDescriptionUserConfigurableObjectFieldViewEditHandler (this,
                                                                                obj,
                                                                                field,
                                                                                viewer);

    }

    @Override
    public ObjectDescriptionUserConfigurableObjectFieldViewEditHandler getViewEditHandler2 (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          IPropertyBinder             binder,
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
