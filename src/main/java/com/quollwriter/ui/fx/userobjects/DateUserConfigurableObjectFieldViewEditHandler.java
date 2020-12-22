package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.time.format.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class DateUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<DateUserConfigurableObjectTypeField, LocalDate>
{

    private DatePicker editItem = null;

    public DateUserConfigurableObjectFieldViewEditHandler (DateUserConfigurableObjectTypeField typeField,
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
                                      throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.editItem = new DatePicker (this.getFieldValue () != null ? this.getFieldValue () : this.typeField.getDefault ());

/*
        if (initValue != null)
        {

            this.editItem.setDate (this.stringToValue (initValue));

        }
*/
        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editItem));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        // TODO Handle max/min dates?

        return null;

    }

    @Override
    public LocalDate getInputSaveValue ()
    {

        //LocalDate l = this.editItem.getValue ();
        //Instant i = (Instant) l.adjustInto (Instant.ofEpochMilli (0));

        //return Utils.localDateToDate (l);

        return this.editItem.getValue ();

        //return Date.from (l.atStartOfDay (ZoneId.systemDefault ()).toInstant ());

        //return new Date (i.toEpochMilli ());

    }

    @Override
    public LocalDate stringToValue (String s)
    {

        return UserConfigurableObjectTypeField.parseDate (s);

    }

    @Override
    public String valueToString (LocalDate val)
    {

        if (val == null)
        {

            return null;

        }

        return UserConfigurableObjectTypeField.formatDate (val);

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
                                     throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        LocalDate d = this.getFieldValue ();

        if (d != null)
        {

            items.add (new Form.Item (this.typeField.formNameProperty (),
                                      QuollLabel2.builder ()
                                        .label (new SimpleStringProperty (Environment.formatLocalDate (d)))
                                        .build ()));

        } else {

            items.add (this.createNoValueItem (getUILanguageStringProperty (form,view,types,UserConfigurableObjectTypeField.Type.date.getType (),novalue)));

        }

        return items;

    }

}
