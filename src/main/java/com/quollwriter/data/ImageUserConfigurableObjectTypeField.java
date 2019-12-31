package com.quollwriter.data;

import java.util.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

public class ImageUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    protected ImageUserConfigurableObjectTypeField (Type type)
    {

        super (type);

    }

    public ImageUserConfigurableObjectTypeField ()
    {

        super (Type.image);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return new com.quollwriter.ui.userobjects.ImageUserConfigurableObjectFieldViewEditHandler (this,
                                                                    obj,
                                                                    field,
                                                                    viewer);

    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler2 (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {

        return new ImageUserConfigurableObjectFieldViewEditHandler (this,
                                                                    obj,
                                                                    field,
                                                                    viewer);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new ImageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.ImageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
