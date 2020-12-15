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
    private ScrollPane scrollPane = null;

    public OptionsPanel (AbstractViewer        viewer,
                         Set<Node>             headerControls,
                         Options.Section.Id... sects)
    {

        super (viewer);

        Header h = Header.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.options,title))
            .styleClassName (StyleClassNames.MAIN)
            .iconClassName (StyleClassNames.OPTIONS)
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

        this.scrollPane = new QScrollPane (bb);
        VBox.setVgrow (this.scrollPane,
                       Priority.ALWAYS);
        b.getChildren ().add (this.scrollPane);

        this.getChildren ().add (b);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        if (s == null)
        {

            s = new State ();

        }

        s.set (State.Key.scrollpanev,
               this.scrollPane.getVvalue ());

        s.set (State.Key.content,
               this.options.getState ());

        return s;

    }

    @Override
    public void init (State state)
               throws GeneralException
    {

        super.init (state);

        if (state == null)
        {

            state = new State ();

        }

        try
        {

            this.options.init (state.getAsState (State.Key.content));

        } catch (Exception e) {

            Environment.logError ("Unable to init options with state: " + state,
                                  e);

        }

        if (this.scrollPane != null)
        {

            double v = state.getAsFloat (State.Key.scrollpanev,
                                         0f);

            UIUtils.runLater (() ->
            {

                this.scrollPane.setVvalue (v);

            });

        }

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
            .styleSheet (StyleClassNames.OPTIONS)
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
