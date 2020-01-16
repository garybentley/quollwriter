package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import java.math.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class NumberUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<NumberUserConfigurableObjectTypeField, Double>
{

    private TextFormItem editItem = null;

    public NumberUserConfigurableObjectFieldViewEditHandler (NumberUserConfigurableObjectTypeField typeField,
                                                             UserConfigurableObject                obj,
                                                             UserConfigurableObjectField           field,
                                                             AbstractProjectViewer                 viewer)
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

            this.editItem.grabFocus ();

        }

    }

    @Override
    public Set<FormItem> getInputFormItems (String         initValue,
                                            ActionListener formSave)
    {

        Set<FormItem> items = new LinkedHashSet ();

        String v = null;

        if (this.getFieldValue () != null)
        {

            v = Environment.formatNumber (this.getFieldValue ());

        } else {

            if (initValue != null)
            {

                try
                {

                    v = Environment.formatNumber (this.stringToValue (initValue));

                } catch (Exception e) {

                    Environment.logError ("Unable to convert value: " +
                                          initValue +
                                          " to a number.",
                                          e);

                }

            }

        }

        this.editItem = new TextFormItem (this.typeField.getFormName (),
                                          v);

        UIUtils.addDoActionOnReturnPressed (this.editItem.getTextField (),
                                            formSave);

        items.add (this.editItem);

        return items;

    }

    @Override
    public Set<String> getInputFormItemErrors ()
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.addedit);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.number.getType ());
        prefix.add (LanguageStrings.errors);

        Set<String> errors = new LinkedHashSet ();

        String val = this.editItem.getValue ();

        Double v = null;

        if ((val != null)
            &&
            (val.trim ().length () > 0)
           )
        {

            try
            {

                v = Environment.parseToDouble (val);

            } catch (Exception e) {

                errors.add (String.format (Environment.getUIString (prefix,
                                                                    LanguageStrings.invalidvalue),
                                           //"%s does not appear to be a number.",
                                           this.typeField.getFormName ()));

            }

        }

        // Check for the maximum/minimum.
        if (v != null)
        {

            Double max = null;

            try
            {

                max = this.typeField.getMaximum ();

            } catch (Exception e) {

                Environment.logError ("Unable to get maximum",
                                      e);

            }

            if (max != null)
            {

                if (v > max)
                {

                    errors.add (String.format (Environment.getUIString (prefix,
                                                                        LanguageStrings.max),
                                               //"%s must be a maximum of %s",
                                               this.typeField.getFormName (),
                                               Environment.formatNumber (max)));

                }

            }

            Double min = null;

            try
            {

                min = this.typeField.getMinimum ();

            } catch (Exception e) {

                Environment.logError ("Unable to get minimum",
                                      e);

            }

            if (min != null)
            {

                if (v < min)
                {

                    errors.add (String.format (Environment.getUIString (prefix,
                                                                        LanguageStrings.min),
                                               //"%s must be a minimum of %s",
                                               this.typeField.getFormName (),
                                               Environment.formatNumber (min)));

                }

            }

        }

        return errors;

    }

    @Override
    public Double getInputSaveValue ()
    {

        String val = this.editItem.getValue ();

        Double v = null;

        if ((val != null)
            &&
            (val.trim ().length () > 0)
           )
        {

            try
            {

                return Environment.parseToDouble (val);

            } catch (Exception e) {

                Environment.logError ("Unable to convert: " + val + " to a double.",
                                      e);

            }

        }

        return null;

    }

    @Override
    public String valueToString (Double val)
    {

        if (val == null)
        {

            return null;

        }

        return Environment.formatNumber (val);

    }

    @Override
    public Double stringToValue (String s)
                          throws GeneralException
    {

        if (s == null)
        {

            return null;

        }

        try
        {

            return Environment.parseToDouble (s);

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert: " +
                                        s +
                                        " to a double.",
                                        e);

        }

    }

    @Override
    public Set<FormItem> getViewFormItems ()
    {

        Set<FormItem> items = new LinkedHashSet ();

        Double d = this.getFieldValue ();

        if (d != null)
        {

            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        UIUtils.createLabel (Environment.formatNumber (d))));

        } else {

            items.add (this.createNoValueItem (Environment.getUIString (LanguageStrings.form,
                                                                        LanguageStrings.view,
                                                                        LanguageStrings.types,
                                                                        UserConfigurableObjectTypeField.Type.number.getType (),
                                                                        LanguageStrings.novalue)));

        }

        return items;

    }

}
