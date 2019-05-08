package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class NewProjectPopup extends PopupContent
{

    public static final String POPUP_ID = "createproject";

    private NewProjectPanel projPanel = null;

    public NewProjectPopup (AbstractViewer viewer)
    {

        super (viewer);

        final NewProjectPopup _this = this;

        NewProjectPanel projPanel = new NewProjectPanel (viewer,
                                                         getUILanguageStringProperty (newproject, LanguageStrings.popup,text),
                                                         true);

        projPanel.setOnCreate (ev -> _this.getPopup ().close ());
        projPanel.setOnCancel (ev -> _this.getPopup ().close ());

        this.getChildren ().add (projPanel);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (newproject, LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.CREATEPROJECT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        return p;

    }

}
