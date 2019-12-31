package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;

import javax.swing.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.viewers.*;

public class DateUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String def = "def";
    public static final String max = "max";
    public static final String min = "min";

    public DateUserConfigurableObjectTypeField ()
    {

        super (Type.date);

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

        return new com.quollwriter.ui.userobjects.DateUserConfigurableObjectFieldViewEditHandler (this,
                                                                   obj,
                                                                   field,
                                                                   viewer);

    }

    @Override
    public UserConfigurableObjectFieldViewEditHandler getViewEditHandler2 (UserConfigurableObject      obj,
                                                                          UserConfigurableObjectField field,
                                                                          AbstractProjectViewer       viewer)
    {

        return new DateUserConfigurableObjectFieldViewEditHandler (this,
                                                                   obj,
                                                                   field,
                                                                   viewer);

    }

    @Override
    public com.quollwriter.ui.userobjects.UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ()
    {

        return new com.quollwriter.ui.userobjects.DateUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    @Override
    public UserConfigurableObjectTypeFieldConfigHandler getConfigHandler2 ()
    {

        return new DateUserConfigurableObjectTypeFieldConfigHandler (this);

    }

    public void setMaximum (Date d)
    {

        this.setDefinitionValue (max, (d != null ? Environment.formatDate (d) : null));

    }

    public Date getMaximum ()
    {

        return this.getDateDefinitionValue (max);

    }

    public void setMinimum (Date d)
    {

        this.setDefinitionValue (min, (d != null ? Environment.formatDate (d) : null));

    }

    public Date getMinimum ()
    {

        return this.getDateDefinitionValue (min);

    }

    public void setDefault (Date d)
    {

        this.setDefinitionValue (def, (d != null ? Environment.formatDate (d) : null));

    }

    public Date getDefault ()
    {

        return this.getDateDefinitionValue (def);

    }

}
