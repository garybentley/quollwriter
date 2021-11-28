package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

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
    private IPropertyBinder binder = null;
    private Runnable formSave = null;

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

        this.editPanel = new DocumentsPanel (this.obj.getFiles (),
                                             binder,
                                             this.viewer);

       this.viewPanel = new DocumentsPanel (this.obj.getFiles (),
                                            binder,
                                            this.viewer);

       this.binder = new PropertyBinder ();

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
    public void updateFieldFromInput ()
                               throws GeneralException
    {

        this.obj.getFiles ().clear ();
        this.obj.getFiles ().addAll (this.editPanel.pathsProperty ());

        this.viewPanel.setPaths (this.obj.getFiles ());
        this.editPanel.setPaths (this.obj.getFiles ());

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editPanel));
        this.editPanel.setPaths (this.obj.getFiles ());
        this.formSave = null;
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
    public Set<Form.Item> getViewFormItems (Runnable formSave)
    {

        this.binder.dispose ();

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.viewPanel));
        this.viewPanel.setPaths (this.obj.getFiles ());
        this.formSave = formSave;

        this.binder.addSetChangeListener (this.viewPanel.pathsProperty (),
                                          ev ->
        {

            if (ev.wasAdded ())
            {

                Path f = ev.getElementAdded ();
                this.obj.getFiles ().add (f);

            }
            UIUtils.runLater (this.formSave);

        });

        return items;

    }

}
