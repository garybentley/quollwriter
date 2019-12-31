package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;

public class ImageUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private ImageUserConfigurableObjectTypeField field = null;

    public ImageUserConfigurableObjectTypeFieldConfigHandler (ImageUserConfigurableObjectTypeField f)
    {

        this.field = f;

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

        return true;

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        return new SimpleStringProperty ("image");

    }

    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        Set<StringProperty> errors = new LinkedHashSet<> ();

        return errors;

    }

    public Set<Form.Item> getExtraFormItems ()
    {

        Set<Form.Item> nitems = new LinkedHashSet<> ();

        return nitems;

    }

}
