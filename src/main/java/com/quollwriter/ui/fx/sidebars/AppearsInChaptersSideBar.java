package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AppearsInChaptersSideBar<E extends AbstractProjectViewer> extends SideBarContent<E>
{

    public static final String SIDEBAR_ID = "appearsinchapters";

    private Label noMatches = null;
    private VBox content = null;
    private ChapterFindResultsBox results = null;
    private NamedObject obj = null;
    private long start = 0;
    private ScheduledFuture updateCheck = null;

    public AppearsInChaptersSideBar (E           viewer,
                                     NamedObject obj)
    {

        super (viewer);

        this.start = System.currentTimeMillis ();
        this.obj = obj;
        VBox b = new VBox ();

        this.getBinder ().addChangeListener (this.obj.nameProperty (),
                                             (pr, oldv, newv) ->
        {

            this.search ();

        });

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

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (appearsinchapters,LanguageStrings.sidebar,text))
            .inViewer (viewer)
            .build ();

        b.getChildren ().addAll (desc, this.noMatches, sp);

        this.search ();

        this.getChildren ().add (b);

    }

    private void search ()
    {

        Map<Chapter, List<SentenceMatches>> snippets = this.viewer.getSentenceMatches (this.obj.getAllNames ());

        UIUtils.runLater (() ->
        {

            this.search (snippets);

        });

        if (this.updateCheck != null)
        {

            this.updateCheck.cancel (true);
            this.updateCheck = null;

        }

        this.updateCheck = Environment.schedule (() ->
        {

            if (this.viewer.getEditorsTextModifiedSince (this.start).size () > 0)
            {

                this.search ();

            }

            this.start = System.currentTimeMillis ();

        },
        2 * Constants.SEC_IN_MILLIS,
        5 * Constants.SEC_IN_MILLIS);

    }

    private void search (Map<Chapter, List<SentenceMatches>> snippets)
    {

        if (this.results != null)
        {

            this.results.dispose ();
            this.results = null;

        }

        this.content.getChildren ().clear ();

        if (snippets != null)
        {

            this.results = new ChapterFindResultsBox (snippets,
                                                      this.viewer);

            this.content.getChildren ().addAll (this.results.getContent ());

        }

        this.noMatches.setVisible (this.results == null || (snippets != null && snippets.size () == 0));

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (Arrays.asList (appearsinchapters,LanguageStrings.sidebar,LanguageStrings.title),
                                                            this.obj.nameProperty ());

        SideBar sb = SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.APPEARSINCHAPTERS)
            .styleSheet (StyleClassNames.APPEARSINCHAPTERS)
            .withScrollPane (false)
            .canClose (true)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (AppearsInChaptersSideBar.getSideBarIdForObject (this.obj))
            .build ();

        if (this.obj instanceof UserConfigurableObject)
        {
/*
            UIUtils.setBackgroundImage (sb.getHeader ().getIcon (),
                                        ((UserConfigurableObject) this.obj).getUserConfigurableObjectType ().icon16x16Property (),
                                        this.getBinder ());
*/
            //sb.getHeader ().getIcon ().imageProperty ().bind (((UserConfigurableObject) this.obj).getUserConfigurableObjectType ().icon16x16Property ());

            sb.getHeader ().getIcon ().setImage (((UserConfigurableObject) this.obj).getUserConfigurableObjectType ().icon16x16Property (),
                                                 this.getBinder ());

        } else {

            sb.getStyleClass ().add (this.obj.getObjectType ());

        }

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

        return sb;

    }

    public static String getSideBarIdForObject (NamedObject obj)
    {

        return SIDEBAR_ID + obj.getObjectReference ().asString ();

    }

    @Override
    public void dispose ()
    {

        super.dispose ();

        if (this.results != null)
        {

            this.results.dispose ();

        }

        if (this.updateCheck != null)
        {

            this.updateCheck.cancel (true);
            this.updateCheck = null;

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
