package com.quollwriter.data;

import java.util.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

public class FileUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public FileUserConfigurableObjectTypeField ()
    {

        super (Type.file);

    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          IPropertyBinder             binder,
                                                                          AbstractProjectViewer       viewer)
    {

        return new FileUserConfigurableObjectFieldViewEditHandler (this,
                                                                   obj,
                                                                   field,
                                                                   viewer);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new FileUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
