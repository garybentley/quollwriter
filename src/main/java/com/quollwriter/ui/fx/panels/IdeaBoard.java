package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class IdeaBoard extends PanelContent<ProjectViewer>
{

    public static final String PANEL_ID = "ideaboard";

    public IdeaBoard (ProjectViewer viewer)
    {

        super (viewer);

    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            .title (getUILanguageStringProperty (ideaboard,title))
            .content (this)
            .styleClassName (StyleClassNames.IDEABOARD)
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
