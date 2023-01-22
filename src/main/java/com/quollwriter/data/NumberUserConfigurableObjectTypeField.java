package com.quollwriter.data;

import java.util.*;

import java.math.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.viewers.*;

public class NumberUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String MAX = "max";
    public static final String MIN = "min";

    public NumberUserConfigurableObjectTypeField ()
    {

        super (Type.number);

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

        return new com.quollwriter.ui.userobjects.NumberUserConfigurableObjectFieldViewEditHandler (this,
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

        return new NumberUserConfigurableObjectFieldViewEditHandler (this,
                                                                     obj,
                                                                     field,
                                                                     viewer);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.NumberUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new NumberUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    public Double getMinimum ()
                       throws GeneralException
    {

        return this.getDoubleDefinitionValue (MIN);

    }

    public void setMinimum (Double v)
    {

        this.setDefinitionValue (MIN,
                                 (v != null ? Environment.formatNumber (v) : null));

    }

    public Double getMaximum ()
                       throws GeneralException
    {

        return this.getDoubleDefinitionValue (MAX);

    }

    public void setMaximum (Double v)
    {

        this.setDefinitionValue (MAX,
                                 (v != null ? Environment.formatNumber (v) : null));

    }

}
