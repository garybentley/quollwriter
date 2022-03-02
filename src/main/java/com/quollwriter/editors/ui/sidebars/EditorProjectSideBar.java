package com.quollwriter.editors.ui.sidebars;

import java.util.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.collections.*;

import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorProjectSideBar extends BaseSideBar<EditorProjectViewer>
{

    public static final String SIDEBAR_ID = "editorproject";

    private EditorChaptersSidebarItem chapters = null;
    private EditorInfoBox editorInfoBox = null;
    private EditorEditor editor = null;
    private VBox content = null;
    private QuollHyperlink unsentLabel = null;
    private QuollHyperlink otherVersionsLabel = null;
    private ScrollPane sp = null;

    public EditorProjectSideBar (EditorProjectViewer v)
                          //throws GeneralException
    {

        super (v);

        this.content = new VBox ();
        VBox.setVgrow (this.content,
                       Priority.ALWAYS);
        this.setContent (this.content);

    }
/*
    @Override
    public String getSideBarId ()
    {

        return SIDEBAR_ID;

    }
*/

    @Override
    public void init (State s)
    {

        super.init (s);

        this.editor = this.viewer.getProject ().getForEditor ();

        this.editorInfoBox = new EditorInfoBox (this.editor,
                                                this.viewer,
                                                true,
                                                this.getBinder ());
        this.editorInfoBox.addFullPopupListener ();

        this.content.getChildren ().add (this.editorInfoBox);

        VBox b = new VBox ();
        this.content.getChildren ().add (b);

        ProjectVersion projVer = this.viewer.getProject ().getProjectVersion ();

        if (projVer != null)
        {

            Node pvp = EditorsUIUtils.getProjectVersionPanel (projVer,
                                                              this.viewer);
            b.getChildren ().add (pvp);

        }

        this.getBinder ().addChangeListener (this.viewer.getProject ().projectVersionProperty (),
                                             (pr, oldv, newv) ->
        {

            b.getChildren ().clear ();

            if (newv != null)
            {

                Node pvp = EditorsUIUtils.getProjectVersionPanel (newv,
                                                                  this.viewer);
                b.getChildren ().add (pvp);

            }

        });

        //Get the due by/response message (properties from the project)
        //Get the project description (sent by editor)

        // Create a box to indicate when there are comments to send.
        this.unsentLabel = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.COMMENT)
            .onAction (ev ->
            {

                EditorsUIUtils.showSendUnsentComments (this.viewer,
                                                       null);

            })
            .build ();

        this.showUnsentNotification ();

        this.viewer.getProject ().getBooks ().get (0).getChapters ().stream ()
            .forEach (c ->
            {

                // TODO Do we need to record and manage these?
                c.chapterItemsEvents ().subscribe (ev ->
                {

                    this.showUnsentNotification ();

                });

            });

        this.getBinder ().addChangeListener (this.viewer.getProject ().projectVersionProperty (),
                                             (pr, oldv, newv) ->
        {

            this.showUnsentNotification ();

        });

        this.content.getChildren ().add (this.unsentLabel);

        this.otherVersionsLabel = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.VIEW)
            .onAction (ev ->
            {

                this.showOtherVersionsSelector ();

            })
            .build ();

        this.showOtherVersionsLabel ();

        this.content.getChildren ().add (this.otherVersionsLabel);

        final EditorProjectSideBar _this = this;

        this.chapters = new EditorChaptersSidebarItem (this.viewer,
                                                       this.getBinder ());

        this.sp = new QScrollPane (this.chapters.getAccordionItem ());

        VBox.setVgrow (sp,
                       Priority.ALWAYS);
        this.content.getChildren ().add (sp);

        this.getBinder ().addChangeListener (this.viewer.getProject ().projectVersionProperty (),
                                             (pr, oldv, newv) ->
        {

            this.chapters = new EditorChaptersSidebarItem (this.viewer,
                                                           this.getBinder ());

            this.sp.setContent (this.chapters.getAccordionItem ());

        });

    }

    private void showUnsentNotification ()
    {

        if (this.editor.isPrevious ())
        {

            this.unsentLabel.setVisible (false);

            return;

        }

        // Get the unsent note count for the project (for this version).
        Set<Note> notes = null;

        try
        {

            notes = this.viewer.getUnsentComments ();

        } catch (Exception e) {

            Environment.logError ("Unable to show unsent comments notification",
                                  e);

            return;

        }

        int count = notes.size ();

        if (count > 0)
        {
/*
            String l = "";

            if (count == 1)
            {

                l = "1 {comment} hasn't been sent, click to review/send it now";

            } else {

                l = count + " {comments} haven't been sent, click to review/send them now";

            }
*/
            this.unsentLabel.textProperty ().unbind ();
            this.unsentLabel.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,project,LanguageStrings.sidebar,comments,unsent),
                                                                                Environment.formatNumber (count)));
                                                     //l);

        }

        this.unsentLabel.setVisible (count > 0);

    }

    private void showOtherVersionsSelector ()
    {

        final EditorProjectSideBar _this = this;

        List<ProjectVersion> pvs = null;

        try
        {

            // TODO: Encapsulate this better.
            pvs = (List<ProjectVersion>) this.viewer.getObjectManager ().getObjects (ProjectVersion.class,
                                                                              this.viewer.getProject (),
                                                                              null,
                                                                              false);

        } catch (Exception e) {

            Environment.logError ("Unable to get project versions for project: " +
                                  this.viewer.getProject (),
                                  e);

            return;

        }

        int c = 0;

        // Order in descending order.
        try
        {

            Query q = new Query ();
            q.parse (String.format ("SELECT * FROM %s ORDER BY dateCreated DESC",
                                    ProjectVersion.class.getName ()));

            QueryResults qr = q.execute (pvs);

            pvs = new ArrayList (qr.getResults ());

        } catch (Exception e) {

            Environment.logError ("Unable to sort project versions: " +
                                  pvs,
                                  e);

        }

        ProjectVersion currPv = this.viewer.getProject ().getProjectVersion ();

        List<ProjectVersion> others = new ArrayList<> ();

        for (ProjectVersion pv : pvs)
        {

            if ((currPv != null)
                &&
                (pv.equals (currPv))
               )
            {

                continue;

            }

            others.add (pv);

            c++;

        }

        if (c == 0)
        {

            return;

        }

        if (c == 1)
        {

            this.viewer.switchToProjectVersion (others.iterator ().next ());

        } else {

            String popupId = "other-versions";

            ShowObjectSelectPopup.<ProjectVersion>builder ()
                .withViewer (viewer)
                .title (editors,project,LanguageStrings.sidebar,comments,otherversions,popup,title)
                .headerIconClassName (StyleClassNames.VIEW)
                .styleClassName ("versionselect")
                .popupId (popupId)
                .objects (FXCollections.observableList (others))
                .cellProvider ((obj, popupContent) ->
                {

                    List<String> prefix = Arrays.asList (editors,project,LanguageStrings.sidebar,comments,otherversions,popup,labels);

                    VBox b = new VBox ();

                    UIUtils.setTooltip (b,
                                        getUILanguageStringProperty (Utils.newList (prefix,clicktoview),
                                                    //"<html>Click to view version <b>%s</b>.</html>",
                                                                     obj.getName ()));

                    b.setOnMouseClicked (ev ->
                    {

                        if (ev.getButton () != MouseButton.PRIMARY)
                        {

                            return;

                        }

                        this.viewer.switchToProjectVersion (obj);

                        popupContent.close ();

                    });

                    QuollLabel l = QuollLabel.builder ()
                        .label (obj.getName () != null ? obj.nameProperty () : getUILanguageStringProperty (Utils.newList (prefix,noversion)))
                        .styleClassName (StyleClassNames.TITLE)
                        .build ();

                    b.getChildren ().add (l);

                    StringBuilder s = new StringBuilder ();

                    if (obj.getDueDate () != null)
                    {

                        s.append (getUILanguageStringProperty (Utils.newList (prefix,dueby),
                                                //"Due by: %s.  ",
                                                               Environment.formatDate (obj.getDueDate ())).getValue ());

                    }

                    try
                    {

                        Set<Note> comms = this.viewer.getNotesForVersion (obj);

                        int unsent = 0;

                        for (Note n : comms)
                        {

                            if (!n.isDealtWith ())
                            {

                                unsent++;

                            }

                        }

                        String t = getUILanguageStringProperty (Utils.newList (prefix,comments),
                                                    //"%s {comment%s}",
                                                                Environment.formatNumber (comms.size ())).getValue ();
                                                  //(comms.size () == 1 ? "" : "s"));

                        if (unsent > 0)
                        {

                            t += getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.unsent),
                                                //", <span style='color: red;'><b>%s</b> unsent</span>",
                                                              Environment.formatNumber (unsent)).getValue ();

                        } else {

                            t += getUILanguageStringProperty (Utils.newList (prefix,sent)).getValue ();
                            //", all sent";

                        }

                        s.append (t);

                        b.getChildren ().add (QuollLabel.builder ()
                            .label (new SimpleStringProperty (s.toString ()))
                            .styleClassName (StyleClassNames.INFO)
                            .build ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to get unsent comments for project version: " +
                                              obj,
                                              e);

                    }

                    return b;

                })
                .showAt (this.otherVersionsLabel,
                         Side.BOTTOM)
                .build ()
                .show ();

        }

    }

    private void showOtherVersionsLabel ()
    {

        List<ProjectVersion> pvs = null;

        try
        {

            // TODO: Encapsulate this better.
            pvs = (java.util.List<ProjectVersion>) this.viewer.getObjectManager ().getObjects (ProjectVersion.class,
                                                                              this.viewer.getProject (),
                                                                              null,
                                                                              false);

        } catch (Exception e) {

            Environment.logError ("Unable to get project versions for project: " +
                                  this.viewer.getProject (),
                                  e);

            return;

        }

        int c = 0;

        ProjectVersion currPv = this.viewer.getProject ().getProjectVersion ();

        ProjectVersion otherPv = null;

        for (ProjectVersion pv : pvs)
        {

            if ((currPv != null)
                &&
                (pv.equals (currPv))
               )
            {

                continue;

            }

            otherPv = pv;

            c++;

        }

        if (c > 0)
        {
/*
            String l = "";

            if (c == 1)
            {

                l = String.format ("1 other version%s is available, click to view it",
                                   (otherPv.getName () != null ? String.format (" (<b>%s</b>)", otherPv.getName ()) : ""));

            } else {

                l = c + " other versions are available, click to select one";

            }
*/
            this.otherVersionsLabel.textProperty ().unbind ();
            this.otherVersionsLabel.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,project,LanguageStrings.sidebar,comments,labels,otherversions),
                                                                                       Environment.formatNumber (c)));
                                                            //l);

        }

        this.otherVersionsLabel.setVisible (c > 0);

    }

    @Override
    public SideBar createSideBar ()
    {

        return SideBar.builder ()
            .title (editors,project,LanguageStrings.sidebar,comments,title)
            .activeTitle (editors,project,LanguageStrings.sidebar,comments,title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.PROJECT)
            .headerIconClassName (StyleClassNames.PROJECT)
            .styleSheet (StyleClassNames.PROJECT, StyleClassNames.EDITORPROJECT)
            .withScrollPane (false)
            .canClose (true)
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

}
