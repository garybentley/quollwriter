package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.util.*;

import com.gentlyweb.utils.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class AppearsInChaptersUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<AppearsInChaptersUserConfigurableObjectTypeField, String>
{

    private AppearsInChaptersPanel editItem = null;

    public AppearsInChaptersUserConfigurableObjectFieldViewEditHandler (AppearsInChaptersUserConfigurableObjectTypeField typeField,
                                                                        UserConfigurableObject              obj,
                                                                        UserConfigurableObjectField         field,
                                                                        AbstractProjectViewer               viewer)
    {

        super (typeField,
               obj,
               field,
               viewer);

    }

    @Override
    public void grabInputFocus ()
    {

        if (this.editItem != null)
        {

            this.editItem.requestFocus ();

        }

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        return null;

    }

    @Override
    public String getInputSaveValue ()
    {

        return null;

    }

    @Override
    public String stringToValue (String s)
    {

        return null;

    }

    @Override
    public String valueToString (String val)
    {

        return null;

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  new AppearsInChaptersPanel (this.obj,
                                                              this.viewer)));

        return items;

    }

}
