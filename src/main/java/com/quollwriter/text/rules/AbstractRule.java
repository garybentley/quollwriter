package com.quollwriter.text.rules;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.fx.components.Form;

// TODO import com.quollwriter.ui.fx.components.Form;

import org.dom4j.*;
import org.dom4j.tree.*;


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

    public abstract Set<com.quollwriter.ui.forms.FormItem> getFormItems ();

    public Set<Form.Item> getFormItems2 ()
    {

        return null;

    }

    public StringProperty getFormError2 ()
    {

        return null;

    }

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
               throws GeneralException
    {

        this.id = DOM4JUtils.attributeValue (root,
                                               XMLConstants.id);

        this.userRule = DOM4JUtils.attributeValueAsBoolean (root,
                                                              XMLConstants.user,
                                                              false);

        this.desc = DOM4JUtils.childElementContent (root,
                                                      XMLConstants.description,
                                                      false,
                                                      null);

        this.summary = DOM4JUtils.childElementContent (root,
                                                         XMLConstants.summary);

        if (!this.userRule)
        {

            this.defaultSummary = this.summary;

        }

    }

    public Element getAsElement ()
    {

        Element root = new DefaultElement (XMLConstants.root);

        root.addAttribute (XMLConstants.id,
                           this.id);
        root.addAttribute (XMLConstants.createType,
                           this.getClass ().getName ());

        if (this.userRule)
        {

            root.addAttribute (XMLConstants.user,
                               Boolean.toString (this.userRule));

        }

        Element summ = new DefaultElement (XMLConstants.summary);

        root.add (summ);

        summ.add (new DefaultCDATA (this.summary));

        if ((this.desc != null)
            &&
            (this.desc.length () > 0)
           )
        {

            Element desc = new DefaultElement (XMLConstants.description);

            root.add (desc);

            desc.add (new DefaultCDATA (this.desc));

        }

        return root;

    }

    public com.quollwriter.ui.forms.Form getEditForm (final java.awt.event.ActionListener        onSaveComplete,
                             final java.awt.event.ActionListener        onCancel,
                             final AbstractProjectViewer viewer,
                             final boolean               add)
    {

        final AbstractRule _this = this;

        Set<FormItem> items = new LinkedHashSet<> ();

        final TextFormItem summary = new TextFormItem (Environment.getUIString (LanguageStrings.form,
                                                                                LanguageStrings.labels,
                                                                                LanguageStrings.summary),
                                                       //"Summary",
                                                       this.getSummary ());

        items.add (summary);

        items.addAll (this.getFormItems ());

        final MultiLineTextFormItem desc = new MultiLineTextFormItem (Environment.getUIString (LanguageStrings.form,
                                                                                               LanguageStrings.labels,
                                                                                               LanguageStrings.description),
                                                                      //"Description",
                                                                      viewer,
                                                                      5);
        desc.setText (this.getDescription ());
        desc.setCanFormat (false);

        items.add (desc);

        Map<com.quollwriter.ui.forms.Form.Button, java.awt.event.ActionListener> buttons = new LinkedHashMap<> ();

        buttons.put (com.quollwriter.ui.forms.Form.Button.save,
                     new java.awt.event.ActionListener ()
                     {

                        @Override
                        public void actionPerformed (java.awt.event.ActionEvent ev)
                        {

                            com.quollwriter.ui.forms.Form f = (com.quollwriter.ui.forms.Form) ev.getSource ();

                            String error = _this.getFormError ();

                            if (error != null)
                            {

                                f.showError (error);

                                return;

                            }

                            _this.setDescription (desc.getText ().trim ());

                            String summ = summary.getText ();

                            if (summ == null)
                            {

                                summ = "";

                            } else {

                                summ = summ.trim ();

                            }

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

                                f.showError (Environment.getUIString (LanguageStrings.problemfinder,
                                                                      LanguageStrings.config,
                                                                      LanguageStrings.entersummaryerror));

                                return;

                            }

                            _this.setSummary (summ);

                            if (onSaveComplete != null)
                            {

                                onSaveComplete.actionPerformed (new java.awt.event.ActionEvent (_this, 1, "saved"));

                            }

                        }

                     });

        buttons.put (com.quollwriter.ui.forms.Form.Button.cancel,
                     new java.awt.event.ActionListener ()
                     {

                        @Override
                        public void actionPerformed (java.awt.event.ActionEvent ev)
                        {

                            if (onCancel != null)
                            {

                                onCancel.actionPerformed (new java.awt.event.ActionEvent (_this, 1, "cancelled"));

                            }

                            return;

                        }

                     });

        com.quollwriter.ui.forms.Form f = new com.quollwriter.ui.forms.Form (com.quollwriter.ui.forms.Form.Layout.stacked,
                           items,
                           buttons);

        return f;

    }

}
