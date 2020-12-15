package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class ObjectNameUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<ObjectNameUserConfigurableObjectTypeField, String>
{

    private TextFormItem editItem = null;

    public ObjectNameUserConfigurableObjectFieldViewEditHandler (ObjectNameUserConfigurableObjectTypeField typeField,
                                                                 UserConfigurableObject                    obj,
                                                                 UserConfigurableObjectField               field,
                                                                 AbstractProjectViewer                     viewer)
    {

        super (typeField,
               obj,
               field,
               viewer);

    }

    public TextFormItem getInputFormItem ()
    {

        return this.editItem;

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

        Set<FormItem> items = new LinkedHashSet ();

        this.editItem = new TextFormItem (this.typeField.getFormName (),
                                          this.obj.getName () != null ? this.obj.getName () : initValue);

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
        prefix.add (UserConfigurableObjectTypeField.Type.objectname.getType ());
        prefix.add (LanguageStrings.errors);

        Set<String> errs = new LinkedHashSet ();

        String name = this.getInputSaveValue ();

        if (name == null)
        {

            errs.add (String.format (Environment.getUIString (prefix,
                                                              LanguageStrings.novalue),
                                     this.typeField.getFormName ()));
            //+ " must be provided.");

        } else  {

            Asset a = this.viewer.getProject ().getAssetByName (name,
                                                                this.obj.getUserConfigurableObjectType ());

            if ((a != null)
                &&
                (this.obj.getKey () != a.getKey ())
               )
            {

                errs.add (String.format (Environment.getUIString (prefix,
                                                                  LanguageStrings.valueexists),
                                         a.getObjectTypeName (),
                                         a.getName ()));

            }

        }

        return errs;

    }

    @Override
    public void updateFieldFromInput ()
    {

        this.obj.setName (this.getInputSaveValue ());

    }

    @Override
    public String getInputSaveValue ()
    {

        return this.editItem.getValue ();

    }

    @Override
    public String valueToString (String val)
    {

        return val;

    }

    @Override
    public String stringToValue (String s)
    {

        return s;

    }

    @Override
    public Set<FormItem> getViewFormItems ()
    {

        Set<FormItem> items = new LinkedHashSet ();

        String v = this.obj.getName ();

        items.add (new AnyFormItem (this.typeField.getFormName (),
                                    new JLabel (v)));

        return items;

    }

}
