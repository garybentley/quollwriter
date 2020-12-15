package com.quollwriter.editors.ui.sidebars;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public abstract class ProjectSentReceivedSideBar<E extends EditorMessage, V extends ProjectSentReceivedViewer> extends SideBarContent<ProjectSentReceivedViewer>
{

    public static final String SIDEBAR_ID = "projectsentreceived";

    private ProjectCommentsChaptersSidebarItem chapters = null;
    private EditorInfoBox editorInfoBox = null;
    private EditorEditor editor = null;
    private VBox content = null;
    protected E message = null;

    public ProjectSentReceivedSideBar (ProjectSentReceivedViewer v,
                                       E                         message)
    {

        super (v);

        this.editor = message.getEditor ();
        this.message = message;

        this.content = new VBox ();
        this.getChildren ().add (this.content);

        this.editorInfoBox = new EditorInfoBox (this.editor,
                                                this.viewer,
                                                true,
                                                this.getBinder ());

        Node messageC = this.getMessageDetails (this.message);

        this.chapters = new ProjectCommentsChaptersSidebarItem (this.viewer,
                                                                this.getItemsTitle (),
                                                                this.getBinder ());

        QScrollPane sp = new QScrollPane (this.chapters.getAccordionItem ());

        this.content.getChildren ().addAll (this.editorInfoBox, messageC, sp);

    }

    public E getMessage ()
    {

        return this.message;

    }

    public abstract Node getMessageDetails (E message);

    public abstract StringProperty getItemsTitle ();

    public abstract StringProperty getTitle ();

    public abstract String getStyleClassName ();

    public abstract String getStyleSheet ();

    @Override
    public SideBar createSideBar ()
    {
        
System.out.println ("HERE SIDEBAR: " + this.getTitle ().getValue ());
        SideBar sb = SideBar.builder ()
            .title (this.getTitle ())
            .activeTitle (this.getTitle ())
            .styleClassName (this.getStyleClassName ())
            //.styleSheet (this.getStyleSheet ())
            .headerIconClassName (StyleClassNames.COMMENTS)
            .styleSheet (StyleClassNames.PROJECT, StyleClassNames.EDITORPROJECT, this.getStyleSheet ())
            .withScrollPane (false)
            .canClose (false)
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

        sb.addEventHandler (SideBar.SideBarEvent.CLOSE_EVENT,
                            ev ->
        {

            this.dispose ();

        });

        return sb;

    }

}
