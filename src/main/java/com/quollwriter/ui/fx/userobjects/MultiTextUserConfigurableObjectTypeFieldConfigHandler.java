package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class MultiTextUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private QuollCheckBox displayAsBullets = null;
    private QuollCheckBox isOtherNames = null;

    private MultiTextUserConfigurableObjectTypeField field = null;

    public MultiTextUserConfigurableObjectTypeFieldConfigHandler (MultiTextUserConfigurableObjectTypeField f)
    {

        this.field = f;

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.multitext.getType ());

        this.isOtherNames = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,othernames,text),
                                                 this.field.getUserConfigurableObjectType ().getObjectTypeName ()))
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,othernames,tooltip),
                                                   this.field.getUserConfigurableObjectType ().getObjectTypeName ()))
            .build ();

        this.displayAsBullets = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,bulletpoints,text)))
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,bulletpoints,tooltip)))
            .build ();

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
    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        Set<StringProperty> errors = new LinkedHashSet<> ();

        return errors;

    }

    @Override
    public Set<Form.Item> getExtraFormItems ()
    {

        Set<Form.Item> nitems = new LinkedHashSet<> ();

        this.displayAsBullets.setSelected (this.field.isDisplayAsBullets ());
        this.isOtherNames.setSelected (this.field.isNameField ());

        nitems.add (new Form.Item (this.displayAsBullets));

        nitems.add (new Form.Item (this.isOtherNames));

        return nitems;

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.multitext.getType ());

        StringProperty p = new SimpleStringProperty ();
        p.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            Set<String> strs = new LinkedHashSet<> ();

            strs.add (getUILanguageStringProperty (Utils.newList (prefix,description)).getValue ());
            //"single line text");

            if (this.field.isNameField ())
            {

                strs.add (getUILanguageStringProperty (Utils.newList (prefix,othernames,description)).getValue ());
                                               //"is other names/aliases for the %s"));

            }

            if (this.field.isDisplayAsBullets ())
            {

                strs.add (getUILanguageStringProperty (Utils.newList (prefix,bulletpoints,description)).getValue ());
                                                   //"displayed as bullet points");

            }

            String s = Utils.joinStrings (strs,
                                          null);

            return String.format (s,
                                  this.getObjName ());

        }));

        return p;

    }

}
