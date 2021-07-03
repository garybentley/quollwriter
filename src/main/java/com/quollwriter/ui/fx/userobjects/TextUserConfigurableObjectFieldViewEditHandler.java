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

public class TextUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<TextUserConfigurableObjectTypeField, StringWithMarkup>
{

    private QuollTextField editItem = null;

    public TextUserConfigurableObjectFieldViewEditHandler (TextUserConfigurableObjectTypeField typeField,
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
    public Set<String> getNamesFromFieldValue ()
                                        throws GeneralException
    {

        Set<String> ret = new LinkedHashSet<> ();

        StringWithMarkup sm = this.getFieldValue ();

        if (sm != null)
        {

            String st = sm.getText ();

            if (st != null)
            {

                StringTokenizer t = new StringTokenizer (st,
                                                         ";,");

                while (t.hasMoreTokens ())
                {

                    ret.add (t.nextToken ().trim ());

                }

            }

        }

        return ret;

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

        this.editItem = QuollTextField.builder ()
            .build ();

        UIUtils.addDoOnReturnPressed (this.editItem,
                                      formSave);


        StringWithMarkup text = this.getFieldValue ();

        if (text != null)
        {

            this.editItem.setText (text.getText ());

        } else {

            if (initValue != null)
            {

                this.editItem.setText (initValue);

            }

        }

        if (this.typeField.isNameField ())
        {

            List<String> prefix = Arrays.asList (form,addedit,types,UserConfigurableObjectTypeField.Type.text.getType (),othernames);

            UIUtils.setTooltip (this.editItem,
                                getUILanguageStringProperty (form,addedit,types,UserConfigurableObjectTypeField.Type.text.getType (),othernames,tooltip));
            //"Separate each name/alias with a comma or a semi-colon.");

        }

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
    public StringWithMarkup getInputSaveValue ()
    {

        return new StringWithMarkup (this.editItem.getText ());

    }

    @Override
    public String valueToString (StringWithMarkup val)
                          throws GeneralException
    {

        try
        {

            return JSONEncoder.encode (val);

        } catch (Exception e) {

            throw new GeneralException ("Unable to encode value to a string",
                                        e);

        }

    }

    @Override
    public StringWithMarkup stringToValue (String s)
    {

        return JSONDecoder.decodeToStringWithMarkup (s);

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
                                     throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        StringWithMarkup text = this.getFieldValue ();

        if ((text != null)
            &&
            (text.hasText ())
           )
        {

            QuollTextView t = QuollTextView.builder ()
                //BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
                //.withViewer (this.viewer)
                .text (text.getText ())
                //.noMarkup (true)
                .build ();

            items.add (new Form.Item (this.typeField.formNameProperty (),
                                      t));

        } else {

            items.add (this.createNoValueItem (getUILanguageStringProperty (form,view,types,UserConfigurableObjectTypeField.Type.text.getType (),novalue)));

        }

        return items;

    }

    @Override
    public String getStyleClassName ()
    {

        return "textfield";

    }

}
