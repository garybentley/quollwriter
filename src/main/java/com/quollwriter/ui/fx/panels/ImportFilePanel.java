package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ImportFilePanel extends PanelContent<AbstractViewer>
{

    public static final String PANEL_ID = "importfile";

    public ImportFilePanel (AbstractViewer viewer)
    {

        super (viewer);

        Label header = new Label ();
        header.getStyleClass ().add (StyleClassNames.TITLE);
        header.textProperty ().bind (getUILanguageStringProperty (general,importfile));

        this.getChildren ().add (header);

    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            .content (this)
            .title (general,importfile)
            .styleClassName (StyleClassNames.IMPORTFILE)
            .panelId (PANEL_ID)
            .build ();

        return panel;

    }

}
