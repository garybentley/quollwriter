package com.quollwriter.data;

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
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
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
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
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
