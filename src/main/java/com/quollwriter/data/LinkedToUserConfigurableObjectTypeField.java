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
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return null;

    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler2 (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {

        return new LinkedToUserConfigurableObjectFieldViewEditHandler (this,
                                                                       obj,
                                                                       field,
                                                                       viewer);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return null;

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new LinkedToUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
