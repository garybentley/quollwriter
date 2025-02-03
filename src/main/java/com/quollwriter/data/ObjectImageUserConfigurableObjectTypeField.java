package com.quollwriter.data;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

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
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
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
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new ObjectImageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
