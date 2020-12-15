package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public abstract class AbstractUserConfigurableObjectFieldViewEditHandler<E extends UserConfigurableObjectTypeField, T> implements UserConfigurableObjectFieldViewEditHandler<E, T>
{

    protected UserConfigurableObjectField field = null;
    protected E typeField = null;
    protected AbstractProjectViewer viewer = null;
    protected UserConfigurableObject obj = null;

    public AbstractUserConfigurableObjectFieldViewEditHandler (E                           typeField,
                                                               UserConfigurableObject      obj,
                                                               UserConfigurableObjectField field,
                                                               AbstractProjectViewer       viewer)
    {

        this.field = field;
        this.viewer = viewer;

        this.typeField = typeField;

        if (obj == null)
        {

            throw new NullPointerException ("User object cannot be null.");

        }

        this.obj = obj;

    }

    @Override
    public Set<String> getNamesFromFieldValue ()
                                        throws GeneralException
    {

        return new LinkedHashSet<> ();

    }

    @Override
    public void updateFieldFromInput ()
                               throws GeneralException
    {

        if (this.field == null)
        {

            this.field = new UserConfigurableObjectField (this.typeField);

            this.obj.addField (this.field);

        }

        this.field.setValue (this.valueToString (this.getInputSaveValue ()));

    }

    @Override
    public T getFieldValue ()
                     throws GeneralException
    {

        return (this.field != null ? this.stringToValue (this.field.getValue ()) : null);

    }

    @Override
    public UserConfigurableObjectField getField ()
    {

        return this.field;

    }

    @Override
    public E getTypeField ()
    {

        return this.typeField;

    }

    public Form.Item createNoValueItem ()
    {

        return this.createNoValueItem (null);

    }

    public Form.Item createNoValueItem (StringProperty label)
    {

        return new Form.Item (this.typeField.formNameProperty (),
                              QuollLabel.builder ()
                                .styleClassName (StyleClassNames.INFORMATION)
                                .label (label != null ? label : getUILanguageStringProperty (form,view,novalue))
                                .build ());

    }

}
