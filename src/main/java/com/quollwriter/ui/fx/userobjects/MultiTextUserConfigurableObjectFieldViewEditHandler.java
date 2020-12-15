package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class MultiTextUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<MultiTextUserConfigurableObjectTypeField, StringWithMarkup>
{

    private QuollTextArea editItem = null;

    public MultiTextUserConfigurableObjectFieldViewEditHandler (MultiTextUserConfigurableObjectTypeField typeField,
                                                                UserConfigurableObject                   obj,
                                                                UserConfigurableObjectField              field,
                                                                AbstractProjectViewer                    viewer)
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

        Set<String> ret = new LinkedHashSet ();

        StringWithMarkup sm = this.getFieldValue ();

        if (sm != null)
        {

            String st = sm.getText ();

            if (st != null)
            {

                StringTokenizer t = new StringTokenizer (st,
                                                         "\n;,");

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

            this.editItem.getTextEditor ().requestFocus ();

        }

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
                                      throws GeneralException
    {

        Node item = null;

        this.editItem = QuollTextArea.builder ()
            .withViewer (this.viewer)
            .formattingEnabled (true)
            //.spellCheckEnabled (true)
            .build ();

        if (this.typeField.isNameField ())
        {

            this.editItem.getStyleClass ().add (StyleClassNames.NAME);
            this.editItem.setFormattingEnabled (false);
            this.editItem.setSpellCheckEnabled (false);

            VBox b = new VBox ();
            b.getChildren ().add (this.editItem);

            b.getChildren ().add (QuollLabel.builder ()
                .label (getUILanguageStringProperty (form,addedit,types,UserConfigurableObjectTypeField.Type.multitext.getType (),othernames,tooltip))
                .styleClassName (StyleClassNames.INFORMATION)
                .build ());

            item = b;

        } else {

            item = this.editItem;

        }

        UIUtils.addDoOnReturnPressed (this.editItem,
                                      formSave);

        if (this.getFieldValue () != null)
        {

            this.editItem.setText (this.getFieldValue ());

        } else {

            if (initValue != null)
            {

                this.editItem.setText (initValue);

            }

        }

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  item));

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

        return this.editItem.getTextWithMarkup ();

    }

    @Override
    public String valueToString (StringWithMarkup val)
                          throws GeneralException
    {

        try
        {

            return JSONEncoder.encode (val);

        } catch (Exception e) {

            throw new GeneralException ("Unable to encode to string",
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

        StringWithMarkup text = this.getFieldValue ();

        Set<Form.Item> items = new LinkedHashSet<> ();

        Form.Item item = null;

        if ((text != null)
            &&
            (text.hasText ())
           )
        {

            Node t = null;

            if (this.typeField.isDisplayAsBullets ())
            {

                t = UIUtils.getAsBulletPoints (text,
                                               this.viewer,
                                               this.obj);

            } else {

                t = UIUtils.getAsText (text,
                                       this.viewer,
                                       this.obj);

            }

            item = new Form.Item (this.typeField.formNameProperty (),
                                  t);

        } else {

            item = this.createNoValueItem (getUILanguageStringProperty (form,view,types,UserConfigurableObjectTypeField.Type.multitext.getType (),novalue));

        }

        items.add (item);

        return items;

    }

}
