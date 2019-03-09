package com.quollwriter.ui.forms;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.ui.*;
import com.quollwriter.text.*;

public class MultiLineTextFormItem extends FormItem<StringWithMarkup>
{

    private TextArea text = null;

    public MultiLineTextFormItem (Object label,
                                  int    rows)
    {

        this (label,
              null,
              rows);

    }

    public MultiLineTextFormItem (Object                label,
                                  AbstractProjectViewer viewer,
                                  int                   rows)
    {

        this (label,
              viewer,
              null,
              rows,
              -1,
              false,
              null);

    }

    public MultiLineTextFormItem (Object   label,
                                  String   placeholder,
                                  int      rows,
                                  int      maxChars,
                                  boolean  requireValue,
                                  String   helpText)
    {

        this (label,
              null,
              placeholder,
              rows,
              maxChars,
              requireValue,
              helpText);

    }

    public MultiLineTextFormItem (Object                label,
                                  AbstractProjectViewer viewer,
                                  String                placeholder,
                                  int                   rows,
                                  int                   maxChars,
                                  boolean               requireValue,
                                  String                helpText)
    {

        super (label,
               requireValue,
               null);

        this.text = new TextArea (placeholder,
                                  rows,
                                  maxChars);

        this.text.setAutoGrabFocus (false);

        if (viewer != null)
        {

            this.setSynonymProviderAndSpellChecker (viewer);

        }

    }

    public void setToolTipText (String t)
    {

        this.text.setToolTipText (t);

    }

    @Override
    public void grabFocus ()
    {

        this.text.grabFocus ();

    }

    public TextArea getTextArea ()
    {

        return this.text;

    }

    public void setAutoGrabFocus (boolean v)
    {

        this.text.setAutoGrabFocus (v);

    }

    public void setSynonymProviderAndSpellChecker (AbstractProjectViewer viewer)
    {

        this.setDictionaryProvider (viewer.getDictionaryProvider ());
        this.setSpellCheckEnabled (viewer.isSpellCheckingEnabled ());

        try
        {

            this.setSynonymProvider (viewer.getSynonymProvider ());

        } catch (Exception e) {

            Environment.logError ("Unable to set synonym provider.",
                                  e);

        }

    }

    public void setSpellCheckEnabled (boolean v)
    {

        this.text.setSpellCheckEnabled (v);

    }

    public void setSynonymProvider (SynonymProvider sp)
    {

        this.text.setSynonymProvider (sp);

    }

    public void setDictionaryProvider (DictionaryProvider2 dp)
    {

        this.text.setDictionaryProvider (dp);

    }

    public JComponent getComponent ()
    {

        return this.text;

    }

    public String getText ()
    {

        return this.text.getText ();

    }

    public void setCanFormat (boolean v)
    {

        this.text.setCanFormat (v);

    }

    public void setText (StringWithMarkup v)
    {

        this.text.setText (v);

    }

    public void setText (String v)
    {

        this.text.setText (v);

    }

    @Override
    public StringWithMarkup getValue ()
    {

        return this.text.getTextWithMarkup ();

    }

    public boolean hasError ()
    {

        return false;
        /*
        boolean err = false;

        StringWithMarkup v = this.getValue ();

        if ((v.getText ().trim ().length () == 0)
            &&
            (this.requireValue)
           )
        {

            err = true;

        }

        if (this.maxType == null)
        {

            return err;

        }

        if ((v != null)
            &&
            (this.maxType != null)
           )
        {

            if (this.maxType.equals ("chars"))
            {

                if (v.length () > this.maxCount)
                {

                    err = true;

                }

            }

            if (this.maxType.equals ("words"))
            {

                if (TextUtilities.getWordCount (v) > this.maxCount)
                {

                    err = true;

                }

            }

        }

        return err;
        */
    }

}
