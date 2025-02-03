package com.quollwriter.ui.fx;

import java.util.concurrent.*;

import java.io.*;
import java.util.*;
import java.nio.file.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;

public class LanguageStringsImageIdBox extends LanguageStringsIdBox<ImageValue, Path>
{

    private ImageSelector userValue = null;

    public LanguageStringsImageIdBox (final ImageValue              baseValue,
                                      final ImageValue              stringsValue,
                                      final LanguageStringsIdsPanel panel)
    {

        super (baseValue,
               stringsValue,
               panel);

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.IMAGE;

    }

    public Set<Form.Item> getFormItems ()
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (QuollHyperlink.builder ()
            .label ("View English Image Online")
            .onAction (ev ->
            {

                UIUtils.openURL (this.panel.getEditor (),
                                 Environment.getQuollWriterWebsite () + this.baseValue.getUrl ());

            })
            .build ()));

        this.userValue = ImageSelector.builder ()
            .withViewer (this.panel.getEditor ())
            .file ((this.stringsValue != null ? this.stringsValue.getImageFile () : null))
            .build ();
        this.userValue.imagePathProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateSideBar (this.baseValue);

        });

        items.add (new Form.Item (new SimpleStringProperty ("Your Image"),
                                  this.userValue));

        return items;

    }

    public void saveValue ()
                    throws GeneralException
    {

        Path uv = this.getUserValue ();

        if (uv != null)
        {

            if (this.stringsValue == null)
            {

                this.stringsValue = this.getEditor ().getUserStrings ().insertImageValue (this.baseValue.getId ());

            }

            this.stringsValue.setImageFile (uv);

        } else {

            this.getEditor ().getUserStrings ().removeNode (this.baseValue.getId ());

        }

    }

    @Override
    public Path getUserValue ()
    {

        return this.userValue.getImagePath ();

    }

    @Override
    public boolean hasErrors ()
    {

        Path s = this.getUserValue ();

        if (s == null)
        {

            return false;

        }

        return false;


    }

    public boolean showErrors (boolean requireUserValue)
    {

        Path s = this.getUserValue ();

        if ((s == null)
            &&
            (!requireUserValue)
           )
        {

            return false;

        }

        Set<String> errs = null;

        if (s == null)
        {

            errs = new LinkedHashSet<> ();

            errs.add ("Cannot show a preview, no value provided.");

        } else {

        }

        Node root = this.baseValue.getRoot ();

        this.updateSideBar (this.baseValue);

        return false;

    }

    public void showPreview ()
    {

        if (this.showErrors (false))
        {

            return;

        }

        Path s = this.getUserValue ();

        if (s == null)
        {

            this.updateSideBar (this.baseValue);

            return;

        }

        this.updateSideBar (this.baseValue);

    }

}
