package com.quollwriter.ui.fx.userobjects;

import java.io.*;
import java.nio.file.*;

import java.util.*;

import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ObjectImageUserConfigurableObjectFieldViewEditHandler extends ImageUserConfigurableObjectFieldViewEditHandler
{

    public ObjectImageUserConfigurableObjectFieldViewEditHandler (ImageUserConfigurableObjectTypeField typeField,
                                                                  UserConfigurableObject               obj,
                                                                  UserConfigurableObjectField          field,
                                                                  AbstractProjectViewer                viewer)
    {

        super (typeField,
               obj,
               field,
               viewer);

    }

    //@Override
    /*
    TODO Remove?
    public Set<Form.Item> getViewFormItems2 ()
    {

        List<String> prefix = Arrays.asList (form,view,types,UserConfigurableObjectTypeField.Type.objectimage.getType ());

        Set<Form.Item> items = new LinkedHashSet<> ();

        Path pf = this.viewer.getProjectFile (this.getFieldValue ());

        Image v = null;

        try
        {

            v = UIUtils.getImage (pf);

        } catch (Exception e) {

            Environment.logError ("Unable to get image for file: " + pf,
                                  e);

        }

        if (v != null)
        {

            ImageView icon = new ImageView (v);

            icon.addEventHandler (MouseEvent.MOUSE_PRESSED,
                                  ev ->
            {

                try
                {

                    UIUtils.showFile (this.viewer,
                                      pf);

                } catch (Exception e) {

                    Environment.logError ("Unable to show image: " +
                                          pf +
                                          ", for: " +
                                          this.field,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                              //"Unable to show image.");

                }

            });

            UIUtils.setTooltip (icon,
                                getUILanguageStringProperty (Utils.newList (prefix,tooltip)));
            //"Click to view the full image");
            items.add (new Form.Item (this.typeField.formNameProperty (),
                                      icon));

        } else {

            items.add (this.createNoValueItem (getUILanguageStringProperty (Utils.newList (prefix,novalue))));

        }

        return items;

    }
*/
/*
 TODO Remove
    public Node getViewImageNode ()
    {

        return this.getViewFormItems ().iterator ().next ().control;

    }

    public Node getInputImageNode ()
    {

        return this.getInputFormItems (null,
                                       null).iterator ().next ().control;

    }
*/
}
