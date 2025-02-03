package com.quollwriter.data;

import java.util.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;

public class LinkedToUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    protected LinkedToUserConfigurableObjectTypeField (Type type)
    {

        super (type);

    }

    public LinkedToUserConfigurableObjectTypeField ()
    {

        super (Type.linkedto);

    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                           IPropertyBinder            binder,
                                                                          AbstractProjectViewer       viewer)
    {

        return new LinkedToUserConfigurableObjectFieldViewEditHandler (this,
                                                                       obj,
                                                                       field,
                                                                       binder,
                                                                       viewer);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new LinkedToUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
