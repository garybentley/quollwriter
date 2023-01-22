package com.quollwriter.ui.userobjects;

import javax.swing.*;
import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private CheckboxFormItem displayAsBullets = null;

    private ObjectDescriptionUserConfigurableObjectTypeField field = null;

    public ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler (ObjectDescriptionUserConfigurableObjectTypeField f)
    {

        this.field = f;

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.objectdesc.getType ());
        prefix.add (LanguageStrings.bulletpoints);

        this.displayAsBullets = new CheckboxFormItem (null,
                                                      Environment.getUIString (prefix,
                                                                               LanguageStrings.text),
        //"Display as bullet points",
                                                      false,
                                                      Environment.getUIString (prefix,
                                                                               LanguageStrings.tooltip));
                                                      //"Check this box to display the text in the field as a series of bullet points.  Each separate line of text will be treated as a bullet point.");

    }

    public String getObjName ()
    {

        return this.field.getUserConfigurableObjectType ().getObjectTypeName ().toLowerCase ();

    }

    public String replaceObjName (String s)
    {

        return String.format (s,
                              this.getObjName ());

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

        this.field.setDisplayAsBullets (this.displayAsBullets.isSelected ());

        return true;

    }

    @Override
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        Set<String> errors = new LinkedHashSet ();

        return errors;

    }

    @Override
    public Set<FormItem> getExtraFormItems ()
    {

        Set<FormItem> nitems = new LinkedHashSet ();

        this.displayAsBullets.setSelected (this.field.isDisplayAsBullets ());

        nitems.add (this.displayAsBullets);

        return nitems;

    }

    @Override
    public String getConfigurationDescription ()
    {

        Set<String> strs = new LinkedHashSet ();

        strs.add (this.replaceObjName (Environment.getUIString (LanguageStrings.form,
                                                                LanguageStrings.config,
                                                                LanguageStrings.types,
                                                                UserConfigurableObjectTypeField.Type.objectdesc.getType (),
                                                                LanguageStrings.description)));
                                       //"is the %s description"));

        if (this.field.isDisplayAsBullets ())
        {

            strs.add (Environment.getUIString (LanguageStrings.form,
                                               LanguageStrings.config,
                                               LanguageStrings.types,
                                               UserConfigurableObjectTypeField.Type.objectdesc.getType (),
                                               LanguageStrings.bulletpoints,
                                               LanguageStrings.description));
            //"displayed as bullet points");

        }

        return Utils.joinStrings (strs,
                                  null);

    }

}
