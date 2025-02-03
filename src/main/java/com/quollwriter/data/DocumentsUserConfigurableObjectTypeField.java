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
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          IPropertyBinder             binder,
                                                                          AbstractProjectViewer       viewer)
    {

        return new DocumentsUserConfigurableObjectFieldViewEditHandler (this,
                                                                        obj,
                                                                        field,
                                                                        binder,
                                                                        viewer);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new DocumentsUserConfigurableObjectTypeFieldConfigHandler (this);

    }

}
