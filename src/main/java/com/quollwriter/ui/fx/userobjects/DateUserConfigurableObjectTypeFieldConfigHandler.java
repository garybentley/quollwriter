package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class DateUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private DatePicker editDefDate = null;
    private DatePicker editMinDate = null;
    private DatePicker editMaxDate = null;

    private DateUserConfigurableObjectTypeField field = null;

    public DateUserConfigurableObjectTypeFieldConfigHandler (DateUserConfigurableObjectTypeField f)
    {

        this.field = f;

        this.editDefDate = new DatePicker (f.getDefault ());
        this.editMinDate = new DatePicker (f.getMinimum ());
        this.editMaxDate = new DatePicker (f.getMaximum ());

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.date.getType ());

        StringProperty p = new SimpleStringProperty ();
        p.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            Set<String> strs = new LinkedHashSet<> ();

            strs.add (getUILanguageStringProperty (Utils.newList (prefix,description)).getValue ());
            //"single line text");

            if (this.field.getMinimum () != null)
            {

                strs.add (String.format (getUILanguageStringProperty (Utils.newList (prefix,min,description)).getValue (),
                                         Environment.formatLocalDate (this.field.getMinimum ())));

                //strs.add ("min: " + Environment.formatDate (this.field.getMinimum ()));

            }

            if (this.field.getMaximum () != null)
            {

                strs.add (String.format (getUILanguageStringProperty (Utils.newList (prefix,max,description)).getValue (),
                                         Environment.formatLocalDate (this.field.getMaximum ())));
                //strs.add ("max: " + Environment.formatDate (this.field.getMaximum ()));

            }

            if (this.field.getDefault () != null)
            {

                strs.add (String.format (getUILanguageStringProperty (Utils.newList (prefix,_default,description)).getValue (),
                                         Environment.formatLocalDate (this.field.getDefault ())));
                //strs.add ("default: " + Environment.formatDate (this.field.getDefault ()));

            }

            String s = Utils.joinStrings (strs,
                                          null);

            return s;

        }));

        return p;

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

        this.field.setDefault (this.editDefDate.getValue ());
        this.field.setMinimum (this.editMinDate.getValue ());
        this.field.setMaximum (this.editMaxDate.getValue ());

        return true;

    }

    @Override
    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.date.getType (),errors);

        Set<StringProperty> errors = new LinkedHashSet<> ();

        LocalDate defVal = this.editDefDate.getValue ();
        LocalDate minVal = this.editMinDate.getValue ();
        LocalDate maxVal = this.editMaxDate.getValue ();

        if ((minVal != null)
            &&
            (maxVal != null)
            &&
            (minVal.isAfter (maxVal))
           )
        {

            errors.add (getUILanguageStringProperty (Utils.newList (prefix,max,greaterthanmin)));
            //errors.add ("The maximum date must be after the minimum.");

        }

        if ((defVal != null)
            &&
            (minVal != null)
            &&
            (minVal.isAfter (defVal))
           )
        {

            errors.add (getUILanguageStringProperty (Utils.newList (prefix,_default,greaterthanmin)));
            //errors.add ("The default date must be after the minimum.");

        }

        if ((defVal != null)
            &&
            (maxVal != null)
            &&
            (defVal.isAfter (maxVal))
           )
        {

            errors.add (getUILanguageStringProperty (Utils.newList (prefix,_default,lessthanmax)));
            //errors.add ("The default date must be before the maximum.");

        }

        return errors;

    }

    @Override
    public Set<Form.Item> getExtraFormItems ()
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.date.getType ());

        Set<Form.Item> nitems = new LinkedHashSet<> ();

        nitems.add (new Form.Item (getUILanguageStringProperty (Utils.newList (prefix,_default,text)),
                                   this.editDefDate));
        //"Default");
        nitems.add (new Form.Item (getUILanguageStringProperty (Utils.newList (prefix,min,text)),
                                   this.editMinDate));
        //"Minimum");
        nitems.add (new Form.Item (getUILanguageStringProperty (Utils.newList (prefix,max,text)),
                                   this.editMaxDate));

        return nitems;

    }

}
