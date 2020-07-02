package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class FindSideBar<E extends AbstractProjectViewer> extends SideBarContent<E>
{

    public static final String SIDEBAR_ID = "find";

    private ScheduledFuture timer = null;
    private String currentSearch = null;
    private TextField text = null;
    private Label noMatches = null;
    private VBox content = null;
    private Set<FindResultsBox> results = null;

    public FindSideBar (E viewer)
    {

        super (viewer);

        VBox b = new VBox ();

        this.text = QuollTextField.builder ()
            .styleClassName (StyleClassNames.FIND)
            .build ();

        VBox fb = new VBox ();
        fb.getStyleClass ().add (StyleClassNames.FINDBOX);
        fb.getChildren ().add (this.text);
        this.text.addEventHandler (KeyEvent.KEY_PRESSED,
                                   ev -> this.handleKeyPress ());

        this.text.setOnAction (ev -> this.search ());

        this.viewer.fireProjectEvent (ProjectEvent.Type.find,
                                      ProjectEvent.Action.show);

        this.content = new VBox ();
        this.content.getStyleClass ().add (StyleClassNames.RESULTS);

        ScrollPane sp = new ScrollPane (this.content);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        this.noMatches = QuollLabel.builder ()
            .label (getUILanguageStringProperty (objectfinder,novalue))
            .styleClassName (StyleClassNames.NOMATCHES)
            .build ();
        this.noMatches.managedProperty ().bind (this.noMatches.visibleProperty ());
        this.noMatches.setVisible (false);

        b.getChildren ().addAll (fb, this.noMatches, sp);

        this.getChildren ().add (b);

        String sel = this.viewer.getSelectedText ();

        if (sel != null)
        {

            this.text.setText (sel);
            this.search ();

        }

    }

    private void handleKeyPress ()
    {

        if (this.timer != null)
        {

            this.timer.cancel (true);

        }

        this.timer = this.viewer.schedule (() ->
        {

            this.search ();

        },
        750,
        -1);

    }

    private void search ()
    {

        String t = this.text.getText ().trim ();

        Set<FindResultsBox> results = this.viewer.findText (t);

        UIUtils.runLater (() ->
        {

            this.search (results);

            this.currentSearch = t;

        });

    }

    private void search (Set<FindResultsBox> results)
    {

        if (this.results != null)
        {

            this.results.stream ()
                .forEach (r -> r.dispose ());

        }

        this.content.getChildren ().clear ();

        this.content.getChildren ().addAll (results.stream ()
            .map (r -> r.getContent ())
            .collect (Collectors.toList ()));

        this.results = results;

        this.noMatches.setVisible (results.size () == 0);

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (Arrays.asList (objectfinder,LanguageStrings.sidebar,LanguageStrings.title),
                                                            (this.currentSearch != null ? ": " + this.currentSearch : ""));

        SideBar sb = SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.FIND)
            .styleSheet (StyleClassNames.FIND)
            .headerIconClassName (StyleClassNames.FIND)
            .withScrollPane (false)
            .canClose (true)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

        sb.addEventHandler (SideBar.SideBarEvent.CLOSE_EVENT,
                            ev ->
        {

            this.dispose ();

        });

        sb.addEventHandler (SideBar.SideBarEvent.HIDE_EVENT,
                            ev ->
        {

            this.dispose ();

        });

        sb.addEventHandler (SideBar.SideBarEvent.SHOW_EVENT,
                            ev ->
        {

            UIUtils.runLater (() ->
            {



                if (this.text.getScene () == null)
                {

                    // Needed because sometimes the scene is not set yet.
                    this.getBinder ().addChangeListener (this.text.sceneProperty (),
                                                         (pr, oldv, newv) ->
                    {

                        if ((oldv == null)
                            &&
                            (newv != null)
                           )
                        {

                            this.text.requestFocus ();

                        }

                    });

                } else {

                    this.text.requestFocus ();

                }

            });

        });

        return sb;

    }

    public void dispose ()
    {

        super.dispose ();

        if (this.results != null)
        {

            this.results.stream ()
                .forEach (r -> r.dispose ());

        }

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

}
