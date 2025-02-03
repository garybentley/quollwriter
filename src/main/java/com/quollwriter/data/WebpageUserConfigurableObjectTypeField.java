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
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
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
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new WebpageUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public boolean isSearchable ()
    {

        return true;

    }

}
