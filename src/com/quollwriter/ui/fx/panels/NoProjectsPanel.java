package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class NoProjectsPanel extends PanelContent<AbstractViewer>
{

    public static final String PANEL_ID = "noprojects";

    public NoProjectsPanel (AbstractViewer viewer)
    {

        super (viewer);

    }

    @Override
    public Panel createPanel ()
    {

        StringProperty title = getUILanguageStringProperty (allprojects,noprojects);

        Panel panel = Panel.builder ()
            .title (title)
            .content (this)
            .styleClassName (StyleClassNames.NOPROJECTS)
            .panelId (PANEL_ID)
            // TODO .headerControls ()
            .toolbar (() ->
            {

                return new LinkedHashSet<Node> ();

            })
            .build ();

        return panel;

    }

}
