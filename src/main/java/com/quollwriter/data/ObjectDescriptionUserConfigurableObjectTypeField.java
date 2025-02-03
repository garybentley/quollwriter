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
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public ObjectDescriptionUserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
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
