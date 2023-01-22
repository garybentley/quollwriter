package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import java.math.*;
import java.text.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class NumberUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private Spinner<Double> minsp = null;
    private Spinner<Double> maxsp = null;

    private NumberUserConfigurableObjectTypeField field = null;

    public NumberUserConfigurableObjectTypeFieldConfigHandler (NumberUserConfigurableObjectTypeField f)
    {

        this.field = f;

        this.minsp = new Spinner<> (-1 * Double.MAX_VALUE,
                                    Double.MAX_VALUE,
                                    0d);
        this.minsp.setEditable (true);

        try
        {

            if (this.field.getMinimum () != null)
            {

                this.minsp.getValueFactory ().setValue (this.field.getMinimum ());

            }

        } catch (Exception e) {

            this.minsp.getValueFactory ().setValue (0d);

            Environment.logError ("Unable to get minimum.",
                                  e);

        }

        this.maxsp = new Spinner<> (-1 * Double.MAX_VALUE,
                                    Double.MAX_VALUE,
                                    0d);
        this.maxsp.setEditable (true);

        try
        {

            if (this.field.getMaximum () != null)
            {

                this.maxsp.getValueFactory ().setValue (this.field.getMaximum ());

            }

        } catch (Exception e) {

            this.maxsp.getValueFactory ().setValue (0d);

            Environment.logError ("Unable to get maximum.",
                                  e);

        }

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.number.getType ());

        StringProperty p = new SimpleStringProperty ();
        p.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            Set<String> strs = new LinkedHashSet<> ();

            strs.add (getUILanguageStringProperty (Utils.newList (prefix,description)).getValue ());

            if (this.field.getMinimum () != null)
            {

                strs.add (String.format (getUILanguageStringProperty (Utils.newList (prefix,min,description)).getValue (),
                                         Environment.formatNumber (this.field.getMinimum ())));

            }

            if (this.field.getMaximum () != null)
            {

                strs.add (String.format (getUILanguageStringProperty (Utils.newList (prefix,max,description)).getValue (),
                                         Environment.formatNumber (this.field.getMaximum ())));
                //strs.add ("max: " + Environment.formatNumber (this.field.getMaximum ()));

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

        try
        {

            this.field.setMinimum (this.minsp.getValue ());

            this.field.setMaximum (this.maxsp.getValue ());

        } catch (Exception e) {

            // This should never happen since we check the value first for errors.
            // This is just to satisfy the compiler.
            Environment.logError ("Unable to set minimum/maximum value",
                                  e);

            return false;

        }

        return true;

    }
/*
TODO Remove?
    private Double convertMax ()
                        throws GeneralException
    {

        return this.convert (this.maxsp);

    }

    private Double convertMin ()
                        throws GeneralException
    {

        return this.convert (this.minsp);

    }

    private Double convert (TextFormItem f)
                     throws GeneralException
    {

        if (f.getText () == null)
        {

            return null;

        }

        String v = f.getText ().trim ();

        if (v.length () > 0)
        {

            return Environment.parseToDouble (v);

        }

        return null;

    }
*/
    @Override
    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.number.getType (),errors);

        Set<StringProperty> errors = new LinkedHashSet<> ();

        Double min = this.minsp.getValue ();
/*
TODO Remove?
        try
        {

            min = this.convertMin ();

        } catch (Exception e) {

            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings.min,
                                                 LanguageStrings.invalidvalue));
            //"The minimum value doesn't look like a number.");

        }
*/
        Double max = this.maxsp.getValue ();
/*
TOOD Remove?
        try
        {

            max = this.convertMax ();

        } catch (Exception e) {

            errors.add (Environment.getUIString (prefix,
                                                 LanguageStrings.max,
                                                 LanguageStrings.invalidvalue));
            //errors.add ("The maximum value doesn't look like a number.");

        }

        if (errors.size () > 0)
        {

            return errors;

        }
*/
        if ((min != null)
            &&
            (max != null)
            &&
            (max.compareTo (min) < 0)
           )
        {

            errors.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.max,greaterthanmin)));
            //errors.add ("The maximum value must be greater than the minimum.");

        }

        return errors;

    }

    @Override
    public Set<Form.Item> getExtraFormItems ()
    {

        Set<Form.Item> nitems = new LinkedHashSet<> ();

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.number.getType ());

        nitems.add (new Form.Item (getUILanguageStringProperty (Utils.newList (prefix,min,text)),
                                   this.minsp));
        nitems.add (new Form.Item (getUILanguageStringProperty (Utils.newList (prefix,max,text)),
                                   this.maxsp));

        return nitems;

    }

}
