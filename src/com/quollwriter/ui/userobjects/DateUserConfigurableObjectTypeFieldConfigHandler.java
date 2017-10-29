package com.quollwriter.ui.userobjects;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.ArrayList;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class DateUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private DateFormItem editDefDate = null;
    private DateFormItem editMinDate = null;
    private DateFormItem editMaxDate = null;

    private DateUserConfigurableObjectTypeField field = null;

    public DateUserConfigurableObjectTypeFieldConfigHandler (DateUserConfigurableObjectTypeField f)
    {

        this.field = f;

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.date.getType ());

        this.editDefDate = new DateFormItem (Environment.getUIString (prefix,
                                                                      LanguageStrings._default,
                                                                      LanguageStrings.text));
        //"Default");
        this.editMinDate = new DateFormItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.min,
                                                                      LanguageStrings.text));
        //"Minimum");
        this.editMaxDate = new DateFormItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.max,
                                                                      LanguageStrings.text));
        //"Maximum");

    }

    @Override
    public String getConfigurationDescription ()
    {

        Set<String> strs = new LinkedHashSet ();

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.date.getType ());

        strs.add (Environment.getUIString (prefix,
                                           LanguageStrings.description));

        //strs.add ("date");

        if (this.field.getMinimum () != null)
        {

            strs.add (String.format (Environment.getUIString (prefix,
                                                              LanguageStrings.min,
                                                              LanguageStrings.description),
                                     Environment.formatDate (this.field.getMinimum ())));

            //strs.add ("min: " + Environment.formatDate (this.field.getMinimum ()));

        }

        if (this.field.getMaximum () != null)
        {

            strs.add (String.format (Environment.getUIString (prefix,
                                                              LanguageStrings.max,
                                                              LanguageStrings.description),
                                     Environment.formatDate (this.field.getMaximum ())));
            //strs.add ("max: " + Environment.formatDate (this.field.getMaximum ()));

        }

        if (this.field.getDefault () != null)
        {

            strs.add (String.format (Environment.getUIString (prefix,
                                                              LanguageStrings._default,
                                                              LanguageStrings.description),
                                     Environment.formatDate (this.field.getDefault ())));
            //strs.add ("default: " + Environment.formatDate (this.field.getDefault ()));

        }

        return Utils.joinStrings (strs,
                                  null);

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
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.date.getType ());
        prefix.add (LanguageStrings.errors);

        Set<String> errors = new LinkedHashSet ();

        Date defVal = this.editDefDate.getValue ();
        Date minVal = this.editMinDate.getValue ();
        Date maxVal = this.editMaxDate.getValue ();

        if ((minVal != null)
            &&
            (maxVal != null)
            &&
            (minVal.after (maxVal))
           )
        {

            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings.max,
                                                 LanguageStrings.greaterthanmin));
            //errors.add ("The maximum date must be after the minimum.");

        }

        if ((defVal != null)
            &&
            (minVal != null)
            &&
            (minVal.after (defVal))
           )
        {

            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings._default,
                                                 LanguageStrings.greaterthanmin));
            //errors.add ("The default date must be after the minimum.");

        }

        if ((defVal != null)
            &&
            (maxVal != null)
            &&
            (defVal.after (maxVal))
           )
        {

            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings._default,
                                                 LanguageStrings.lessthanmax));
            //errors.add ("The default date must be before the maximum.");

        }

        return errors;

    }

    @Override
    public Set<FormItem> getExtraFormItems ()
    {

        Set<FormItem> nitems = new LinkedHashSet ();

        this.editDefDate.setDate (this.field.getDefault ());
        this.editMinDate.setDate (this.field.getMinimum ());
        this.editMaxDate.setDate (this.field.getMaximum ());

        nitems.add (this.editDefDate);

        nitems.add (this.editMinDate);

        nitems.add (this.editMaxDate);

        return nitems;

    }

}
