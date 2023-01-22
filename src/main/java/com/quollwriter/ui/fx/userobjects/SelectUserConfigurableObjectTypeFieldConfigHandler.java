package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import java.math.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class SelectUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private QuollTextArea editItems = null;
    private QuollCheckBox editAllowMulti = null;
    private SelectUserConfigurableObjectTypeField field = null;

    public SelectUserConfigurableObjectTypeFieldConfigHandler (SelectUserConfigurableObjectTypeField field)
    {

        this.field = field;

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.select.getType ());

        this.editItems = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (Utils.newList (prefix,tooltip)))
            .build ();

        this.editAllowMulti = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,allowmulti,text)))
            .build ();

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

        Set<String> items = new LinkedHashSet<> ();

        StringTokenizer t = new StringTokenizer (this.editItems.getText (),
                                                 ";,");

        while (t.hasMoreTokens ())
        {

            items.add (t.nextToken ().trim ());

        }

        this.field.setItems (items);

        this.field.setAllowMulti (this.editAllowMulti.isSelected ());

        return true;

    }

    @Override
    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        Set<StringProperty> errors = new LinkedHashSet ();

        StringTokenizer t = new StringTokenizer (this.editItems.getText (),
                                                 ";,");

        if (t.countTokens () == 0)
        {

            List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.select.getType (),LanguageStrings.errors);

            errors.add (getUILanguageStringProperty (Utils.newList (prefix,novalue)));
            //"At least one item must be specified.");

        }

        return errors;

    }

    @Override
    public Set<Form.Item> getExtraFormItems ()
    {

        Set<Form.Item> nitems = new LinkedHashSet<> ();

        Collection<String> items = this.field.getItems ();

        StringBuilder b = new StringBuilder ();

        if (items != null)
        {

            for (String i : items)
            {

                if (b.length () > 0)
                {

                    b.append (", ");

                }

                b.append (i);

            }

        }

        if (b.length () > 0)
        {

            this.editItems.setText (b.toString ());

        }

        nitems.add (new Form.Item (getUILanguageStringProperty (form,config,types,UserConfigurableObjectTypeField.Type.select.getType (),text),
                                   this.editItems));

        this.editAllowMulti.setSelected (this.field.isAllowMulti ());

        nitems.add (new Form.Item (this.editAllowMulti));

        return nitems;

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        List<String> prefix = Arrays.asList (form,config,types,UserConfigurableObjectTypeField.Type.select.getType ());

        StringProperty p = new SimpleStringProperty ();
        p.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            Set<String> strs = new LinkedHashSet<> ();

            strs.add (getUILanguageStringProperty (Utils.newList (prefix,description)).getValue ());

            if (this.field.isAllowMulti ())
            {

                strs.add (getUILanguageStringProperty (Utils.newList (prefix,allowmulti,description)).getValue ());

            }

            Collection<String> items = this.field.getItems ();

            if (items.size () > 0)
            {

                StringBuilder b = new StringBuilder (getUILanguageStringProperty (Utils.newList (prefix,multi,text)).getValue ());
                //"items: ");

                int i = 0;

                for (String v : items)
                {

                    if (i == 5)
                    {

                        break;

                    }

                    if (i > 0)
                    {

                        b.append (getUILanguageStringProperty (Utils.newList (prefix,multi,valueseparator)).getValue ());
                                  //", ");

                    }

                    b.append (v);

                    i++;

                }

                if (items.size () > 5)
                {

                    b.append (String.format (getUILanguageStringProperty (Utils.newList (prefix,multi,more,text)).getValue (),
                                             //", +%s others",
                                             Environment.formatNumber (items.size () - 5)));

                }

                strs.add (b.toString ());

            }

            String s = Utils.joinStrings (strs,
                                          null);

            return s;
/* TODO?
                return String.format (s,
                                      this.getObjName ());
*/
        }));

        return p;

    }

}
