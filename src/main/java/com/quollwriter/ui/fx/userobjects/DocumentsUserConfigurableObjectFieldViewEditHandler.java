package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.io.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class DocumentsUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<DocumentsUserConfigurableObjectTypeField, String>
{

    private DocumentsPanel editPanel = null;
    private DocumentsPanel viewPanel = null;

    public DocumentsUserConfigurableObjectFieldViewEditHandler (DocumentsUserConfigurableObjectTypeField typeField,
                                                                UserConfigurableObject                   obj,
                                                                UserConfigurableObjectField              field,
                                                                IPropertyBinder                          binder,
                                                                AbstractProjectViewer                    viewer)
    {

        super (typeField,
               obj,
               field,
               viewer);

        this.editPanel = new DocumentsPanel (this.obj,
                                             binder,
                                             this.viewer);

       this.viewPanel = new DocumentsPanel (this.obj,
                                            binder,
                                            this.viewer);

    }

    @Override
    public String getStyleClassName ()
    {

        return "documents";

    }

    @Override
    public void grabInputFocus ()
    {

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editPanel));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        return null;

    }

    @Override
    public String getInputSaveValue ()
    {

        return null;

    }

    @Override
    public String stringToValue (String s)
    {

        return null;

    }

    @Override
    public String valueToString (String val)
    {

        return null;

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.viewPanel));

        return items;

    }

}
