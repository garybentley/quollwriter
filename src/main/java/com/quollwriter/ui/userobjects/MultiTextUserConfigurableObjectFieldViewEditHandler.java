package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;

public class MultiTextUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<MultiTextUserConfigurableObjectTypeField, StringWithMarkup>
{

    private MultiLineTextFormItem editItem = null;

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

            this.editItem.getTextArea ().grabFocus ();

        }

    }

    @Override
    public Set<FormItem> getInputFormItems (String         initValue,
                                            ActionListener formSave)
    {

        this.editItem = new MultiLineTextFormItem (this.typeField.getFormName (),
                                                   this.viewer,
                                                   (this.typeField.isNameField () ? 3 : 7));

        this.editItem.setCanFormat (true);
        this.editItem.setAutoGrabFocus (false);

        UIUtils.addDoActionOnReturnPressed (this.editItem.getTextArea (),
                                            formSave);

        if (this.typeField.isNameField ())
        {

            this.editItem.setSpellCheckEnabled (false);

            this.editItem.setToolTipText (Environment.getUIString (LanguageStrings.form,
                                                                   LanguageStrings.addedit,
                                                                   LanguageStrings.types,
                                                                   UserConfigurableObjectTypeField.Type.multitext.getType (),
                                                                   LanguageStrings.othernames,
                                                                   LanguageStrings.tooltip));
            //"Separate each name/alias with a new line, comma or a semi-colon.");

        }

        if (this.getFieldValue () != null)
        {

            this.editItem.setText (this.getFieldValue ());

        } else {

            if (initValue != null)
            {

                this.editItem.setText (initValue);

            }

        }

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
    public StringWithMarkup getInputSaveValue ()
    {

        return this.editItem.getValue ();

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
    public Set<FormItem> getViewFormItems ()
    {

        StringWithMarkup text = this.getFieldValue ();

        Set<FormItem> items = new LinkedHashSet ();

        FormItem item = null;

        if ((text != null)
            &&
            (text.hasText ())
           )
        {

            JComponent t = null;

            if (this.typeField.isDisplayAsBullets ())
            {

                Markup m = text.getMarkup ();

                StringBuilder b = new StringBuilder ("<ul>");

                TextIterator iter = new TextIterator (text.getText ());

                for (Paragraph p : iter.getParagraphs ())
                {

                    b.append ("<li>");
                    b.append (p.markupAsHTML (m));
                    b.append ("</li>");

                }

                b.append ("</ul>");

                t = UIUtils.createObjectDescriptionViewPane (b.toString (),
                                                             this.field.getParentObject (),
                                                             this.viewer,
                                                             null);

            } else {

                t = UIUtils.createObjectDescriptionViewPane (text,
                                                             this.field.getParentObject (),
                                                             this.viewer,
                                                             null);

            }

            t.setBorder (null);

            item = new AnyFormItem (this.typeField.getFormName (),
                                    t);

        } else {

            item = this.createNoValueItem (Environment.getUIString (LanguageStrings.form,
                                                                    LanguageStrings.view,
                                                                    LanguageStrings.types,
                                                                    UserConfigurableObjectTypeField.Type.multitext.getType (),
                                                                    LanguageStrings.novalue));

        }

        items.add (item);

        return items;

    }

}
