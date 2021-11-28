package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.time.format.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.*;

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
    public String getStyleClassName ()
    {

        return StyleClassNames.DATE;

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
                                      throws GeneralException
    {

        this.editItem = new DatePicker (this.getFieldValue () != null ? this.getFieldValue () : this.typeField.getDefault ());

        VBox b = new VBox ();
        b.getChildren ().add (this.editItem);

        this.editItem.prefWidthProperty ().bind (b.widthProperty ());

        LocalDate min = this.typeField.getMinimum ();
        LocalDate max = this.typeField.getMaximum ();

        if ((min != null)
            ||
            (max != null)
           )
        {

            StringProperty t = UILanguageStringsManager.createStringPropertyWithBinding (() ->
            {

                StringBuilder _b = new StringBuilder ();

                if (min != null)
                {

                    _b.append (getUILanguageStringProperty (Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.date.getType (),LanguageStrings.min,description),
                                                           Environment.formatLocalDate (min)).getValue ());

                }

                if (max != null)
                {

                    if (_b.length () > 0)
                    {

                        _b.append (" ");

                    }

                    _b.append (getUILanguageStringProperty (Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.date.getType (),LanguageStrings.max,description),
                                                           Environment.formatLocalDate (max)).getValue ());

                }

                return _b.toString ();

            });

            QuollLabel l = QuollLabel.builder ()
                .label (t)
                .styleClassName (StyleClassNames.INFORMATION)
                .build ();

            b.getChildren ().add (l);

        }

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  b));

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

        return this.editItem.getValue ();

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
    public Set<Form.Item> getViewFormItems (Runnable formSave)
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
