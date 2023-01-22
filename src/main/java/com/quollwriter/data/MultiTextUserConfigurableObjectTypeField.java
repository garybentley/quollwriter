package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

public class MultiTextUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public MultiTextUserConfigurableObjectTypeField ()
    {

        super (Type.multitext);

    }

    protected MultiTextUserConfigurableObjectTypeField (Type type)
    {

        super (type);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          com.quollwriter.ui.AbstractProjectViewer       viewer)
    {

        return new com.quollwriter.ui.userobjects.MultiTextUserConfigurableObjectFieldViewEditHandler (this,
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

        return new MultiTextUserConfigurableObjectFieldViewEditHandler (this,
                                                                        obj,
                                                                        field,
                                                                        viewer);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.MultiTextUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new MultiTextUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    public void setDisplayAsBullets (boolean v)
    {

        this.setDefinitionValue ("displayAsBullets", v);

    }

    public boolean isDisplayAsBullets ()
    {

        return this.getBooleanDefinitionValue ("displayAsBullets");

    }

    @Override
    public boolean isSearchable ()
    {

        return true;

    }

}
