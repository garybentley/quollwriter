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
        this.options = new Options (viewer,
                                    this.getBinder (),
                                    headerControls,
                                    sects);
        VBox.setVgrow (this.options,
                       Priority.ALWAYS);

        this.getChildren ().add (this.options);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        if (s == null)
        {

            s = new State ();

        }

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

    }

    public void showSection (String sectId)
    {

        if (sectId == null)
        {

            return;

        }

        Options.Section.Id id = Options.Section.Id.valueOf (sectId);

        if (id == null)
        {

            throw new IllegalArgumentException ("Section id: " + sectId + ", is not valid.");

        }

        this.options.showSection (id);

    }

    public void showSection (Options.Section.Id sectId)
    {

        if (sectId == null)
        {

            return;

        }
        
        this.options.showSection (sectId);

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
