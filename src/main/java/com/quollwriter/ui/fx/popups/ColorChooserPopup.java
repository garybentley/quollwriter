package com.quollwriter.ui.fx.popups;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ColorChooserPopup extends PopupContent
{

    public static final String POPUP_ID = "colorchooser";

    private QuollColorChooser chooser = null;

    public ColorChooserPopup (AbstractViewer viewer,
                              Color          selected,
                              boolean        showUserColors)
    {

        super (viewer);

        final ColorChooserPopup _this = this;

        this.chooser = QuollColorChooser.builder ()
            .color (selected)
            .showUserColors (showUserColors)
            .build ();

        this.chooser.addEventHandler (Form.FormEvent.CANCEL_EVENT,
        ev ->
        {

            _this.getPopup ().close ();

        });

        this.getChildren ().add (this.chooser);

    }

    public QuollColorChooser getChooser ()
    {

        return this.chooser;

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (colorchooser,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.COLORCHOOSER)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.setPrefWidth (300);
        p.requestFocus ();

        return p;

    }

}
