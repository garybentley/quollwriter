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

public class ObjectNameUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<ObjectNameUserConfigurableObjectTypeField, String>
{

    private QuollTextField nameEdit = null;

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
/*
TODO?
    public TextFormItem getInputFormItem ()
    {

        return this.editItem;

    }
*/

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.OBJECTNAME;

    }

    @Override
    public void grabInputFocus ()
    {

        this.nameEdit.requestFocus ();

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.nameEdit = QuollTextField.builder ()
            .text (this.obj.getName () != null ? this.obj.getName () : initValue)
            .build ();

        UIUtils.addDoOnReturnPressed (this.nameEdit,
                                      formSave);

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.nameEdit));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        List<String> prefix = Arrays.asList (form,addedit,types,UserConfigurableObjectTypeField.Type.objectname.getType (),errors);

        Set<StringProperty> errs = new LinkedHashSet<> ();

        String name = this.getInputSaveValue ();

        if ((name == null)
            ||
            (name.trim ().equals (""))
           )
        {

            errs.add (getUILanguageStringProperty (Utils.newList (prefix,novalue),
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

                errs.add (getUILanguageStringProperty (Utils.newList (prefix,valueexists),
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

        return this.nameEdit.getText ().trim ();

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
    public Set<Form.Item> getViewFormItems ()
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (new SimpleStringProperty (this.typeField.getFormName ()),
                                  QuollLabel.builder ()
                                    .styleClassName (StyleClassNames.OBJECTNAME)
                                    .label (this.obj.nameProperty ())
                                    .build ()));

        return items;

    }

}
