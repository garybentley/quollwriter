package com.quollwriter.ui.userobjects;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class TextUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private TextUserConfigurableObjectTypeField field = null;
    private CheckboxFormItem isOtherNames = null;

    public TextUserConfigurableObjectTypeFieldConfigHandler (TextUserConfigurableObjectTypeField field)
    {

        this.field = field;

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.text.getType ());
        prefix.add (LanguageStrings.othernames);

        this.isOtherNames = new CheckboxFormItem (null,
                                                  this.replaceObjName (Environment.getUIString (prefix,
                                                                                                LanguageStrings.text)),
                                                                                                //"Is other names/aliases for the %s"),
                                                  false,
                                                  this.replaceObjName (Environment.getUIString (prefix,
                                                                                                LanguageStrings.tooltip)));
        //"Check this box to mark this field as other name or aliases for the %s.  Separate each name/alias with a new line, a comma or a semi-colon."));

    }

    public String getObjName ()
    {

        return this.field.getUserConfigurableObjectType ().getObjectTypeName ().toLowerCase ();

    }

    public String replaceObjName (String s)
    {

        return String.format (s,
                              this.getObjName ());
        /*
        return StringUtils.replaceString (s,
                                          "%s",
                                          this.getObjName ());
        */
    }

    @Override
    public String getConfigurationDescription ()
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.config);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.text.getType ());

        Set<String> strs = new LinkedHashSet ();

        strs.add (Environment.getUIString (prefix,
                                           LanguageStrings.description));
        //"single line text");

        if (this.field.isNameField ())
        {

            strs.add (this.replaceObjName (Environment.getUIString (prefix,
                                                                    LanguageStrings.othernames,
                                                                    LanguageStrings.description)));
                                           //"is other names/aliases for the %s"));

        }

        return Utils.joinStrings (strs,
                                  null);

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

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

        this.isOtherNames.setSelected (this.field.isNameField ());

        nitems.add (this.isOtherNames);

        return nitems;

    }

}
