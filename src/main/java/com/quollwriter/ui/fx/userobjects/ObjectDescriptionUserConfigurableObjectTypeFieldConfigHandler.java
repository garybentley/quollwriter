package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private QuollCheckBox displayAsBullets = null;

    private ObjectDescriptionUserConfigurableObjectTypeField field = null;

    public ObjectDescriptionUserConfigurableObjectTypeFieldConfigHandler (ObjectDescriptionUserConfigurableObjectTypeField f)
    {

        this.field = f;

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.objectdesc.getType (),bulletpoints);

        this.displayAsBullets = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,tooltip)))
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

        nitems.add (new Form.Item (this.displayAsBullets));

        return nitems;

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        StringProperty p = new SimpleStringProperty ();
        p.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            Set<String> strs = new LinkedHashSet<> ();

            strs.add (getUILanguageStringProperty (form,config,types,UserConfigurableObjectTypeField.Type.objectdesc.getType (),description).getValue ());

            if (this.field.isDisplayAsBullets ())
            {

                strs.add (getUILanguageStringProperty (form,config,types,UserConfigurableObjectTypeField.Type.objectdesc.getType (),bulletpoints,description).getValue ());
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
