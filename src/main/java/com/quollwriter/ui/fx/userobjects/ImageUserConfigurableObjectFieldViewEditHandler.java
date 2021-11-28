package com.quollwriter.ui.fx.userobjects;

import java.util.*;
import java.util.function.*;

import java.io.*;
import java.nio.file.*;

import javax.imageio.*;

import javafx.beans.property.*;

import javafx.geometry.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

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
    private QuollImageView icon = null;

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

        this.editItem.setDragDropLabelText (getUILanguageStringProperty (form,addedit,types,UserConfigurableObjectTypeField.Type.image.getType (),drop));

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.editItem));

        return items;

    }

    @Override
    public Set<StringProperty> getInputFormItemErrors ()
    {

        return null;

    }

    private String updateForPath (Path f)
                             throws GeneralException
    {

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
    public String getInputSaveValue ()
                              throws GeneralException
    {

        Path f = this.editItem.getImagePath ();

        return this.updateForPath (f);

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
    public Set<Form.Item> getViewFormItems (Runnable formSave)
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
        this.icon = new QuollImageView ();

        try
        {

            this.icon.setImage (pf);
            this.icon.setDragDropLabelText (getUILanguageStringProperty (form,view,types,UserConfigurableObjectTypeField.Type.image.getType (),drop));

        } catch (Exception e) {

            Environment.logError ("Unable to show background: " + pf,
                                  e);

        }

        this.icon.widthProperty ().addListener ((pr, oldv, newv) ->
        {
            UIUtils.forceRunLater (() -> this.icon.requestLayout ());
        });

        this.icon.imagePathProperty ().addListener ((pr, oldv, newv) ->
        {

            if (this.editItem != null)
            {

                try
                {

                    this.editItem.setImage (newv);

                    this.updateFieldFromInput ();

                    UIUtils.runLater (formSave);

                } catch (Exception e) {

                    // TODO Handle properly.
                    Environment.logError ("Unable to set edit image: " + newv,
                                          e);

                }

            } else {

                try
                {

                    String f = this.updateForPath (newv);
                    this.field.setValue (this.valueToString (f));

                    UIUtils.runLater (formSave);

                } catch (Exception e) {

                    // TODO Handle properly.
                    Environment.logError ("Unable to set image: " + newv,
                                          e);

                }

            }

        });

        this.icon.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
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
                                  this.icon.getImagePath ());

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

        UIUtils.setTooltip (this.icon,
                            getUILanguageStringProperty (Utils.newList (prefix,tooltip)));

        items.add (new Form.Item (this.typeField.formNameProperty (),
                                  this.icon));

        return items;

    }

    @Override
    public Supplier<Set<MenuItem>> getViewContextMenuItems ()
    {

        return () ->
        {

            final List<String> prefix = Arrays.asList (form,view,types,UserConfigurableObjectTypeField.Type.image.getType ());

            Set<MenuItem> its = new LinkedHashSet<> ();

            Path pf = null;

            try
            {

                pf = this.viewer.getProjectFile (this.getFieldValue ());

            } catch (Exception e) {

                Environment.logError ("Unable to show image: " +
                                      this.field,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Utils.newList (prefix,actionerror)));

                return its;

            }

            if (pf != null)
            {

                Path _pf = pf;

                its.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.VIEW)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,popupmenu,items,view)))
                    .onAction (eev ->
                    {

                        try
                        {

                            UIUtils.showFile (this.viewer,
                                              _pf);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show image: " +
                                                  _pf +
                                                  ", for: " +
                                                  this.field,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                                      //"Unable to show image.");

                        }

                    })
                    .build ());

                its.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.DELETE)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,popupmenu,items,remove)))
                    .onAction (eev ->
                    {

                        try
                        {

                            this.icon.setImage ((Path) null);

                            if (this.editItem != null)
                            {

                                this.editItem.setImage ((Path) null);

                            }

                            this.field.setValue (null);

                            // TODO Improve this...
                            try
                            {

                                this.viewer.saveObject (this.obj,
                                                        true);

                                this.viewer.fireProjectEvent (ProjectEvent.Type.asset,
                                                              ProjectEvent.Action.edit,
                                                              this.obj);

                            } catch (Exception e)
                            {

                                Environment.logError ("Unable to save: " +
                                                      this.obj,
                                                      e);

                                ComponentUtils.showErrorMessage (this.viewer,
                                                                 getUILanguageStringProperty (assets,save,actionerror));
                                                          //"Unable to save.");

                                return;

                            }

                        } catch (Exception e) {

                            Environment.logError ("Unable to remove image: " +
                                                  _pf,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                                      //"Unable to show image.");

                        }

                    })
                    .build ());

            }

            return its;

        };

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.IMAGE;

    }

}
