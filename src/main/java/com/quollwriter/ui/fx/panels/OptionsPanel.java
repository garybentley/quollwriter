package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class OptionsPanel extends PanelContent<AbstractViewer>
{

    public static final String PANEL_ID = "options";
    private Options options = null;

    public OptionsPanel (AbstractViewer        viewer,
                         Set<Node>             headerControls,
                         Options.Section.Id... sects)
    {

        super (viewer);

        Header h = Header.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.options,title))
            .controls (headerControls)
            .build ();

        VBox b = new VBox ();
        b.getChildren ().add (h);

        VBox bb = new VBox ();
        bb.getStyleClass ().add (StyleClassNames.SECTIONS);
        VBox.setVgrow (bb,
                       Priority.ALWAYS);

        this.options = new Options (viewer,
                                    this.getBinder (),
                                    sects);

        bb.getChildren ().add (this.options);

        ScrollPane sp = new ScrollPane (bb);
        sp.vvalueProperty ().addListener ((pr, oldv, newv) ->
        {

            h.pseudoClassStateChanged (StyleClassNames.SCROLLING_PSEUDO_CLASS, newv.doubleValue () > 0);

        });
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        b.getChildren ().add (sp);

        this.getChildren ().add (b);

    }

    public void showSection (String sectId)
    {

        // TODO

    }

    public void showSection (Options.Section.Id sectId)
    {

    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.options,title))
            .content (this)
            .styleClassName (StyleClassNames.OPTIONS)
            .panelId (PANEL_ID)
            // TODO .headerControls ()
            .toolbar (() ->
            {

                return new LinkedHashSet<Node> ();

            })
            .build ();

        panel.addEventHandler (Panel.PanelEvent.CLOSE_EVENT,
                              ev ->
        {

            this.options.dispose ();

        });

        return panel;

    }

}
