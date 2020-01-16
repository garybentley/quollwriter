package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.viewers.*;

public class ObjectImageUserConfigurableObjectTypeField extends ImageUserConfigurableObjectTypeField
{

    public ObjectImageUserConfigurableObjectTypeField ()
    {

        super (Type.objectimage);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return new com.quollwriter.ui.userobjects.ObjectImageUserConfigurableObjectFieldViewEditHandler (this,
                                                                          obj,
                                                                          field,
                                                                          viewer);

    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler2 (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          IPropertyBinder             binder,
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
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.ObjectImageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new ObjectImageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
