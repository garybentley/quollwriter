package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import java.io.*;
import java.nio.file.*;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

public class FileUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<FileUserConfigurableObjectTypeField, String>
{

    private QuollFileField editItem = null;

    public FileUserConfigurableObjectFieldViewEditHandler (FileUserConfigurableObjectTypeField typeField,
                                                           UserConfigurableObject              obj,
                                                           UserConfigurableObjectField         field,
                                                           AbstractProjectViewer               viewer)
    {

        super (typeField,
               obj,
               field,
               viewer);

    }

    @Override
    public void grabInputFocus ()
    {

        if (this.editItem != null)
        {

            this.editItem.requestFocus ();

        }

    }

    @Override
    public Set<Form.Item> getInputFormItems (String   initValue,
                                             Runnable formSave)
                                      throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        Path file = this.viewer.getProjectFile ((this.getFieldValue () != null ? this.getFieldValue () : initValue));

        this.editItem = QuollFileField.builder ()
            .limitTo (QuollFileField.Type.file)
            .initialFile (file)
            .build ();

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editItem));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        return null;

    }

    @Override
    public String getInputSaveValue ()
                              throws GeneralException
    {

        Path f = this.editItem.getFile ();

        if (f == null)
        {

            if (this.getFieldValue () != null)
            {

                this.viewer.deleteProjectFile (this.getFieldValue ());

            }

            return null;

        }

        Path currF = this.viewer.getProjectFile (this.getFieldValue ());

        boolean save = false;

        // Do we have a selected file and don't have a saved one?
        // Or, do we have both and have they changed?
        if ((f != null)
            &&
            ((currF == null)
             ||
             (f.compareTo (currF) != 0)
            )
           )
        {

            String v = this.getFieldValue ();

            String fn = null;

            if (currF == null)
            {

                fn = UUID.randomUUID ().toString () + "." + Utils.getFileType (f);

            } else {

                String t = Utils.getFileType (currF);

                fn = v.substring (0,
                                  v.length () - t.length ()) + Utils.getFileType (f);

            }

            try
            {

                // Copy the new file into place.
                this.viewer.saveToProjectFilesDirectory (f,
                                                         fn);

            } catch (Exception e) {

                throw new GeneralException ("Unable to copy file: " +
                                            f +
                                            " to project files dir with filename: " +
                                            fn,
                                            e);

            }

            f = this.viewer.getProjectFile (fn);

        }

        return f.getFileName ().toString ();

    }

    @Override
    public String stringToValue (String s)
    {

        // It's just the file name.
        return s;

    }

    @Override
    public String valueToString (String f)
                          throws GeneralException
    {

        return f;

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
                                     throws GeneralException
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        Path f = null;

        if (this.getFieldValue () != null)
        {

            f = this.viewer.getProjectFile (this.getFieldValue ());

        }

        String url = null;

        if (f != null)
        {

            try
            {

                url = f.toUri ().toURL ().toExternalForm ();

            } catch (Exception e) {

                Environment.logError ("Unable to convert file to a url string: " +
                                      f,
                                      e);

                url = "file://" + f.toString ();

            }

        }

        if (url != null)
        {

            String _url = url;

            items.add (new Form.Item (this.typeField.formNameProperty (),
                                      QuollLabel2.builder ()
                                        .label (new SimpleStringProperty (f.getFileName ().toString ()))
                                        .styleClassName (StyleClassNames.HYPERLINK)
                                        .onAction (ev ->
                                        {

                                            try
                                            {

                                                UIUtils.openURL (this.viewer,
                                                                 _url,
                                                                 null);

                                            } catch (Exception e) {

                                                Environment.logError ("Unable to open url: " + _url,
                                                                      e);

                                            }

                                        })
                                        .build ()));

        } else {

            items.add (this.createNoValueItem ());

        }

        return items;

    }

}
