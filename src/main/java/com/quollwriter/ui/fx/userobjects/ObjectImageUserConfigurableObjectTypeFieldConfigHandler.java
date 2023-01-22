package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ObjectImageUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private ObjectImageUserConfigurableObjectTypeField field = null;

    public ObjectImageUserConfigurableObjectTypeFieldConfigHandler (ObjectImageUserConfigurableObjectTypeField f)
    {

        this.field = f;

    }

    public String getObjName ()
    {

        return this.field.getUserConfigurableObjectType ().getObjectTypeName ().toLowerCase ();

    }

    public String replaceObjName (String s)
    {

        return String.format (s,
                              this.getObjName ());

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

        return true;

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        return getUILanguageStringProperty (form,config,types,UserConfigurableObjectTypeField.Type.objectimage.getType (),description);

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
