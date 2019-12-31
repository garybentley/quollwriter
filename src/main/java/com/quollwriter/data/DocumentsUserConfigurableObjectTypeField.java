package com.quollwriter.data;

import java.util.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.viewers.*;

public class DocumentsUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    protected DocumentsUserConfigurableObjectTypeField (Type type)
    {

        super (type);

    }

    public DocumentsUserConfigurableObjectTypeField ()
    {

        super (Type.documents);

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

        return new DocumentsUserConfigurableObjectFieldViewEditHandler (this,
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

        return new DocumentsUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
