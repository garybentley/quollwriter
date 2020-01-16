package com.quollwriter.ui.userobjects;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class SelectUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<SelectUserConfigurableObjectTypeField, Set<String>>
{

    private SelectFormItem editItem = null;

    public SelectUserConfigurableObjectFieldViewEditHandler (SelectUserConfigurableObjectTypeField typeField,
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

        this.editItem.getComponent ().grabFocus ();

    }

    @Override
    public Set<FormItem> getInputFormItems (String         initValue,
                                            ActionListener formSave)
    {

        Set<String> sel = new LinkedHashSet ();

        if (this.getFieldValue () != null)
        {

            sel.addAll (this.getFieldValue ());

        } else {

            if (initValue != null)
            {

                sel.add (initValue);

            }

        }

        this.editItem = new SelectFormItem (this.typeField.getFormName (),
                                            new Vector<String> (this.typeField.getItems ()),
                                            (this.typeField.isAllowMulti () ? 5 : 1),
                                            sel,
                                            Short.MAX_VALUE,
                                            false,
                                            null);

        Set<FormItem> items = new LinkedHashSet ();

        items.add (this.editItem);

        return items;

    }

    @Override
    public Set<String> getInputFormItemErrors ()
    {

        return null;

    }

    @Override
    public Set<String> getInputSaveValue ()
    {

        return this.editItem.getValue ();

    }

    @Override
    public String valueToString (Set<String> val)
                          throws GeneralException
    {

        if (val == null)
        {

            return null;

        }

        try
        {

            return JSONEncoder.encode (val);

        } catch (Exception e) {

            throw new GeneralException ("Unable to encode values: " +
                                        val,
                                        e);

        }

    }

    @Override
    public Set<String> stringToValue (String s)
    {

        if (s == null)
        {

            return null;

        }

        return new LinkedHashSet ((Collection) JSONDecoder.decode (s));

    }

    @Override
    public Set<FormItem> getViewFormItems ()
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.view);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.select.getType ());

        Set<FormItem> items = new LinkedHashSet ();

        String label = Environment.getUIString (prefix,
                                                LanguageStrings.novalue);
        //"Not provided.";

        Set<String> value = null;

        if (this.field != null)
        {

            value = this.getFieldValue ();

        }

        if ((value != null)
            &&
            (value.size () > 0)
           )
        {

            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        UIUtils.createLabel (Utils.joinStrings (value, Environment.getUIString (prefix,
                                                                                                                LanguageStrings.valueseparator)))));

        } else {

            items.add (this.createNoValueItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.novalue)));

        }

        return items;

    }

}
