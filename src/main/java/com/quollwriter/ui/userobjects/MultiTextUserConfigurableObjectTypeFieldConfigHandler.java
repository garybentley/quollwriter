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

public class MultiTextUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private CheckboxFormItem displayAsBullets = null;
    private CheckboxFormItem isOtherNames = null;

    private MultiTextUserConfigurableObjectTypeField field = null;

    public MultiTextUserConfigurableObjectTypeFieldConfigHandler (MultiTextUserConfigurableObjectTypeField f)
    {

        this.field = f;

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.multitext.getType ());

        this.isOtherNames = new CheckboxFormItem (null,
                                                  this.replaceObjName (Environment.getUIString (prefix,
                                                                                                LanguageStrings.othernames,
                                                                                                LanguageStrings.text)),
                                                                       //"Is other names/aliases for the %s"),
                                                  false,
                                                  this.replaceObjName (Environment.getUIString (prefix,
                                                                                                LanguageStrings.othernames,
                                                                                                LanguageStrings.tooltip)));
                                                  //"Check this box to mark this field as other name or aliases for the %s.  Separate each name/alias with a new line, a comma or a semi-colon."));

        this.displayAsBullets = new CheckboxFormItem (null,
                                                      Environment.getUIString (prefix,
                                                                               LanguageStrings.bulletpoints,
                                                                               LanguageStrings.text),
        //"Display as bullet points",
                                                      false,
                                                      Environment.getUIString (prefix,
                                                                               LanguageStrings.bulletpoints,
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
        this.field.setNameField (this.isOtherNames.isSelected ());

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
        this.isOtherNames.setSelected (this.field.isNameField ());

        nitems.add (this.displayAsBullets);

        nitems.add (this.isOtherNames);

        return nitems;

    }

    @Override
    public String getConfigurationDescription ()
    {

        Set<String> strs = new LinkedHashSet ();

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.multitext.getType ());

        strs.add (Environment.getUIString (prefix,
                                           LanguageStrings.description));
        //"multi-line text");

        if (this.field.isNameField ())
        {

            strs.add (this.replaceObjName (Environment.getUIString (prefix,
                                                                    LanguageStrings.othernames,
                                                                    LanguageStrings.description)));
            //"is other names/aliases for the %s"));

        }

        if (this.field.isDisplayAsBullets ())
        {

            strs.add (Environment.getUIString (prefix,
                                               LanguageStrings.bulletpoints,
                                               LanguageStrings.description));
                                               //"displayed as bullet points");

        }

        return Utils.joinStrings (strs,
                                  null);

    }

}
