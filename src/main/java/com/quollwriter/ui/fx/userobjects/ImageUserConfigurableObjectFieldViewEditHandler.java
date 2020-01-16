package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import java.io.*;
import java.nio.file.*;

import javax.imageio.*;

import javafx.beans.property.*;

import javafx.geometry.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ImageUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<ImageUserConfigurableObjectTypeField, String>
{

    private ImageSelector editItem = null;

    public ImageUserConfigurableObjectFieldViewEditHandler (ImageUserConfigurableObjectTypeField typeField,
                                                            UserConfigurableObject               obj,
                                                            UserConfigurableObjectField          field,
                                                            AbstractProjectViewer                viewer)
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

        this.editItem = ImageSelector.builder ()
            .file (this.viewer.getProjectFile ((this.getFieldValue () != null ? this.getFieldValue () : initValue)))
            .withViewer (this.viewer)
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

        Path f = this.editItem.getImagePath ();

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

    public boolean hasImage ()
                      throws GeneralException
    {

        final Path pf = this.viewer.getProjectFile (this.getFieldValue ());

        if ((pf == null)
            ||
            (Files.notExists (pf))
           )
        {

            return false;

        }

        Image v = null;

        try
        {

            v = UIUtils.getImage (pf);

        } catch (Exception e) {

            Environment.logError ("Unable to get image: " + pf,
                                  e);

        }

        return v != null;

    }

    @Override
    public Set<Form.Item> getViewFormItems ()
                                     throws GeneralException    
    {

        final List<String> prefix = Arrays.asList (form,view,types,UserConfigurableObjectTypeField.Type.image.getType ());

        final ImageUserConfigurableObjectFieldViewEditHandler _this = this;

        Set<Form.Item> items = new LinkedHashSet<> ();

        final Path pf = this.viewer.getProjectFile (this.getFieldValue ());
/*
        if ((pf == null)
            ||
            (Files.notExists (pf))
           )
        {

            items.add (this.createNoValueItem (getUILanguageStringProperty (Utils.newList (prefix,novalue))));

            return items;

        }
*/
        QuollImageView icon = new QuollImageView ();

        try
        {

            icon.setImage (pf);
            icon.setDragDropLabelText (getUILanguageStringProperty (form,view,types,UserConfigurableObjectTypeField.Type.image.getType (),drop));

        } catch (Exception e) {

            Environment.logError ("Unable to show background: " + pf,
                                  e);

        }

        icon.imagePathProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                this.editItem.setImage (newv);

                this.updateFieldFromInput ();

            } catch (Exception e) {

                // TODO Handle properly.
                Environment.logError ("Unable to set edit image: " + newv,
                                      e);

            }

        });

        icon.setOnMouseClicked (ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            if (ev.getClickCount () != 1)
            {

                return;

            }

            try
            {

                UIUtils.showFile (_this.viewer,
                                  icon.getImagePath ());

            } catch (Exception e) {

                Environment.logError ("Unable to show image: " +
                                      pf +
                                      ", for: " +
                                      _this.field,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                          //"Unable to show image.");

            }

        });

        UIUtils.setTooltip (icon,
                            getUILanguageStringProperty (Utils.newList (prefix,tooltip)));

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  icon));

        return items;

    }

}
