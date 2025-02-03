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
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          IPropertyBinder             binder,
                                                                          AbstractProjectViewer       viewer)
    {

        return new ImageUserConfigurableObjectFieldViewEditHandler (this,
                                                                    obj,
                                                                    field,
                                                                    viewer);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new ImageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
