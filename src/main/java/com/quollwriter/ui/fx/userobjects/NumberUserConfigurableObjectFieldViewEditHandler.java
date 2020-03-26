package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import java.math.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class NumberUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<NumberUserConfigurableObjectTypeField, Double>
{

    private Spinner<Double> editItem = null;

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

            this.editItem.requestFocus ();

        }

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
                                      throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();
/*
TODO Remove?
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
*/

        double min = -1 * Double.MAX_VALUE;

        Double fMin = this.typeField.getMinimum ();

        if (fMin != null)
        {

            try
            {

                min = Math.max (fMin, min);

            } catch (Exception e) {

                Environment.logError ("Unable to get minimum.",
                                      e);

            }

        }

        double max = Double.MAX_VALUE;

        Double fMax = this.typeField.getMaximum ();

        if (fMax != null)
        {

            try
            {

                max = Math.min (fMax, max);

            } catch (Exception e) {

                Environment.logError ("Unable to get maximum.",
                                      e);

            }

        }

        Double v = this.getFieldValue ();

        double _v = 0;

        if (v != null)
        {

            _v = v.doubleValue ();

        }

        this.editItem = new Spinner<> (min,
                                       max,
                                       _v);
        this.editItem.setEditable (true);

        UIUtils.addDoOnReturnPressed (this.editItem.getEditor (),
                                      formSave);

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editItem));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        List<String> prefix = Arrays.asList (form,addedit,types,UserConfigurableObjectTypeField.Type.number.getType (),errors);

        Set<StringProperty> errors = new LinkedHashSet<> ();

        Double val = this.editItem.getValue ();
/*
TODO Needed ?
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
*/
        return errors;

    }

    @Override
    public Double getInputSaveValue ()
    {

        return this.editItem.getValue ();
/*
TODO Remove?
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
*/
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
    public Set<Form.Item> getViewFormItems ()
                                     throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        Double d = this.getFieldValue ();

        if (d != null)
        {

            items.add (new Form.Item (this.typeField.formNameProperty (),
                                      QuollLabel2.builder ()
                                        .label (new SimpleStringProperty (Environment.formatNumber (d)))
                                        .build ()));

        } else {

            items.add (this.createNoValueItem (getUILanguageStringProperty (form,view,types,UserConfigurableObjectTypeField.Type.number.getType (),novalue)));

        }

        return items;

    }

}
