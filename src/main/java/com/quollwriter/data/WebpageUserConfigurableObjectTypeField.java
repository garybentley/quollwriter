package com.quollwriter.data;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

public class WebpageUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public WebpageUserConfigurableObjectTypeField ()
    {

        super (Type.webpage);

    }

    @Override
    public boolean isSortable ()
    {

        return true;

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return new com.quollwriter.ui.userobjects.WebpageUserConfigurableObjectFieldViewEditHandler (this,
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

        return new WebpageUserConfigurableObjectFieldViewEditHandler (this,
                                                                      obj,
                                                                      field,
                                                                      viewer);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.WebpageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new WebpageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public boolean isSearchable ()
    {

        return true;

    }

}
