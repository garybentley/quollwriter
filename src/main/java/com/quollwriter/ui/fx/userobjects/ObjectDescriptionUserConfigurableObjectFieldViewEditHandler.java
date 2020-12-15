package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;

public class ObjectDescriptionUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<ObjectDescriptionUserConfigurableObjectTypeField, StringWithMarkup>
{

    private QuollTextArea editItem = null;

    public ObjectDescriptionUserConfigurableObjectFieldViewEditHandler (ObjectDescriptionUserConfigurableObjectTypeField typeField,
                                                                        UserConfigurableObject                           obj,
                                                                        UserConfigurableObjectField                      field,
                                                                        AbstractProjectViewer                            viewer)
    {

        super (typeField,
               obj,
               field,
               viewer);

    }

    @Override
    public void updateFieldFromInput ()
    {

        this.obj.setDescription (this.getInputSaveValue ());

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
    {

        this.editItem = QuollTextArea.builder ()
            .styleClassName (StyleClassNames.OBJECTDESCRIPTION)
            .withViewer (this.viewer)
            .formattingEnabled (true)
            //.spellCheckEnabled (true)
            .build ();
        this.editItem.setText (this.obj.getDescription ());
        VBox.setVgrow (this.editItem,
                       Priority.ALWAYS);

        UIUtils.addDoOnReturnPressed (this.editItem,
                                      formSave);

        if (this.typeField.isNameField ())
        {

            this.editItem.setFormattingEnabled (false);
            this.editItem.setSpellCheckEnabled (false);

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
    public StringWithMarkup getInputSaveValue ()
    {

        StringWithMarkup sm = this.editItem.getTextWithMarkup ();

        if (sm.getText ().length () == 0)
        {

            return null;

        }

        return sm;

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
    public StringWithMarkup getFieldValue ()
    {

        return this.obj.getDescription ();

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
    {

        StringWithMarkup text = this.obj.getDescription ();

        Set<Form.Item> items = new LinkedHashSet<> ();

        Form.Item item = null;

        if ((text != null)
            &&
            (text.hasText ())
           )
        {

            Node n = null;

            if (this.typeField.isDisplayAsBullets ())
            {

                n = UIUtils.getAsBulletPoints (text,
                                               this.viewer,
                                               this.obj);

                n.getStyleClass ().addAll (StyleClassNames.OBJECTDESCRIPTION);

            } else {

                n = UIUtils.getAsText (text,
                                       this.viewer,
                                       this.obj);

                n.getStyleClass ().addAll (StyleClassNames.OBJECTDESCRIPTION);

            }

            item = new Form.Item (this.typeField.formNameProperty (),
                                  n);

        } else {

            item = this.createNoValueItem ();

        }

        items.add (item);

        return items;

    }

    public Node getInputTextNode (Runnable formSave)
    {

        Set<Form.Item> its = this.getInputFormItems (null,
                                                     formSave);

        return its.iterator ().next ().control;

    }

    public Node getViewTextNode ()
    {

        return this.getViewFormItems ().iterator ().next ().control;

    }

}
