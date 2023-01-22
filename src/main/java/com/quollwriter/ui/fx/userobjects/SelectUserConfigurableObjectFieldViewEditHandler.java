package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class SelectUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<SelectUserConfigurableObjectTypeField, Set<String>>
{

    private ListView<String> editItem = null;

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

        this.editItem.requestFocus ();

    }

    @Override
    public String getStyleClassName ()
    {

        return "list";

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
                                      throws GeneralException
    {

        Set<String> sel = new LinkedHashSet<> ();

        if (this.getFieldValue () != null)
        {

            sel.addAll (this.getFieldValue ());

        } else {

            if (initValue != null)
            {

                sel.add (initValue);

            }

        }

        if (this.typeField.isAllowMulti ())
        {



        }

        ObservableList<String> vals = FXCollections.observableList (new ArrayList<> (this.typeField.getItems ()));

        this.editItem = new ListView<> (vals);
        this.editItem.setEditable (false);

        this.editItem.getSelectionModel ().setSelectionMode (this.typeField.isAllowMulti () ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);

        if (this.getFieldValue () != null)
        {

            for (String v : this.getFieldValue ())
            {

                this.editItem.getSelectionModel ().select (v);

            }

        }

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editItem));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        return null;

    }

    @Override
    public Set<String> getInputSaveValue ()
    {

        return new LinkedHashSet<> (this.editItem.getSelectionModel ().getSelectedItems ());

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
    public Set<Form.Item> getViewFormItems (Runnable formSave)
                                     throws GeneralException
    {

        List<String> prefix = Arrays.asList (form,view,types,UserConfigurableObjectTypeField.Type.select.getType ());

        Set<Form.Item> items = new LinkedHashSet<> ();

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

            items.add (new Form.Item (this.typeField.formNameProperty (),
                                      QuollLabel2.builder ()
                                        .label (new SimpleStringProperty (Utils.joinStrings (value, getUILanguageStringProperty (Utils.newList (prefix,valueseparator)).getValue ())))
                                        .build ()));

        } else {

            items.add (this.createNoValueItem (getUILanguageStringProperty (Utils.newList (prefix,novalue))));

        }

        return items;

    }

}
