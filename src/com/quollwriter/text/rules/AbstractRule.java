package com.quollwriter.text.rules;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import com.gentlyweb.xml.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;

import com.quollwriter.text.*;

import org.jdom.*;


public abstract class AbstractRule<E extends TextBlock> implements Rule<E>
{

    public class XMLConstants
    {

        public static final String root = "rule";
        public static final String description = "description";
        public static final String id = "id";
        public static final String user = "user";
        public static final String summary = "summary";
        public static final String createType = "createType";

    }

    protected String  summary = null;
    protected String  desc = null;
    protected String  id = null;
    protected boolean userRule = false;
    private String defaultSummary = null;

    public AbstractRule ()
    {

    }

    public abstract String getEditFormTitle (boolean add);

    public abstract List<FormItem> getFormItems ();

    public abstract String getFormError ();

    public String getDefaultSummary ()
    {

        return this.defaultSummary;

    }

    @Override
    public void setSummary (String s)
    {

        this.summary = s;

    }

    @Override
    public String getSummary ()
    {

        return this.summary;

    }

    @Override
    public void setUserRule (boolean u)
    {

        this.userRule = u;

    }

    @Override
    public boolean isUserRule ()
    {

        return this.userRule;

    }

    public void setDescription (String d)
    {

        this.desc = d;

    }

    public void setId (String id)
    {

        this.id = id;

    }

    public String getId ()
    {

        return this.id;

    }

    public String getDescription ()
    {

        return this.desc;

    }

    public void init (Element root)
               throws JDOMException
    {

        this.id = JDOMUtils.getAttributeValue (root,
                                               XMLConstants.id);

        this.userRule = JDOMUtils.getAttributeValueAsBoolean (root,
                                                              XMLConstants.user,
                                                              false);

        this.desc = JDOMUtils.getChildElementContent (root,
                                                      XMLConstants.description,
                                                      !this.userRule);

        this.summary = JDOMUtils.getChildElementContent (root,
                                                         XMLConstants.summary,
                                                         true);

        if (!this.userRule)
        {

            this.defaultSummary = this.summary;

        }

    }

    public Element getAsElement ()
    {

        Element root = new Element (XMLConstants.root);

        root.setAttribute (XMLConstants.id,
                           this.id);
        root.setAttribute (XMLConstants.createType,
                           this.getClass ().getName ());

        if (this.userRule)
        {

            root.setAttribute (XMLConstants.user,
                               Boolean.toString (this.userRule));

        }

        Element summ = new Element (XMLConstants.summary);

        root.addContent (summ);

        summ.addContent (this.summary);

        if (this.desc != null)
        {

            Element desc = new Element (XMLConstants.description);

            root.addContent (desc);

            desc.addContent (this.desc);

        }

        return root;

    }

    public Form getEditForm (final ActionListener onSaveComplete,
                             final ActionListener onCancel,
                             final boolean        add)
    {

        final AbstractRule _this = this;

        List<FormItem> items = new ArrayList ();

        final JTextField summary = com.quollwriter.ui.UIUtils.createTextField ();

        summary.setText (this.getSummary ());

        items.add (new FormItem ("Summary",
                                 summary));

        items.addAll (this.getFormItems ());

        final TextArea desc = com.quollwriter.ui.UIUtils.createTextArea (null,
                                                                         3,
                                                                         -1);

        desc.setText (this.getDescription ());

        items.add (new FormItem ("Description",
                                 desc));

        String title = this.getEditFormTitle (add);

        if (title == null)
        {

            title = (add ? "Add Rule" : "Edit Rule");

        }

        final Form f = new Form (title,
                                 null,
                                 items,
                                 null,
                                 Form.SAVE_CANCEL_BUTTONS);

        f.addFormListener (new FormAdapter ()
        {

            public void actionPerformed (FormEvent ev)
            {

                if (ev.getActionCommand ().equals (FormEvent.CANCEL_ACTION_NAME))
                {

                    if (onCancel != null)
                    {

                        onCancel.actionPerformed (new ActionEvent (_this, 1, "cancelled"));

                    }

                }

                String error = _this.getFormError ();

                if (error != null)
                {

                    f.showError (error);

                    return;

                }

                _this.setDescription (desc.getText ().trim ());

                String summ = summary.getText ().trim ();

                if (summ.length () == 0)
                {

                    summ = _this.getSummary ();

                }

                if (summ == null)
                {

                    summ = _this.getDefaultSummary ();

                }

                if (summ == null)
                {

                    f.showError ("Please enter a summary.");

                    return;

                }

                _this.setSummary (summ);

                if (onSaveComplete != null)
                {

                    onSaveComplete.actionPerformed (new ActionEvent (_this, 1, "saved"));

                }

            }

        });

        return f;

    }

}
