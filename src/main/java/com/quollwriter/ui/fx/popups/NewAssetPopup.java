package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class NewAssetPopup extends PopupContent<ProjectViewer>
{

    public static final String POPUP_ID = "createasset";
    private QuollTextField nameField = null;
    private Asset asset = null;

    public NewAssetPopup (Asset         asset,
                          ProjectViewer viewer)
    {

        super (viewer);

        this.asset = asset;
        this.nameField = QuollTextField.builder ()
            .build ();

    }

    public NewAssetPopup (UserConfigurableObjectType objType,
                          ProjectViewer              viewer)
    {

        this (new Asset (objType),
              viewer);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (Arrays.asList (assets,add,LanguageStrings.popup,title),
                                                 this.asset.getUserConfigurableObjectType ().nameProperty ()))
            .styleClassName (StyleClassNames.CREATEASSET)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        UIUtils.runLater (() ->
        {

            this.nameField.requestFocus ();

        });

        return p;

    }

}
