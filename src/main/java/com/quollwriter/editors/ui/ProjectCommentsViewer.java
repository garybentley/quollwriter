package com.quollwriter.editors.ui;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ProjectCommentsViewer extends ProjectSentReceivedViewer<ProjectCommentsMessage>
{

    public ProjectCommentsViewer (Project                proj,
                                  ProjectCommentsMessage message)
    {

        super (proj,
               message);

    }

    /*
    public void switchToProjectComments (ProjectCommentsMessage pcm)
    {

        if (pcm == null)
        {

            throw new IllegalArgumentException ("Expected a project comments message");

        }

        this.message = pcm;

        this.initTitle ();

        ProjectSentReceivedSideBar sb = null;

        try
        {

            sb = this.getSideBar ();

            sb.init ();

        } catch (Exception e) {

            Environment.logError ("Unable to init new editor project comments side bar",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to show comments, please contact Quoll Writer support for assistance.");

            // Need to close and reopen the project?

            return;

        }

        this.sideBar = sb;

        this.setMainSideBar (this.sideBar);

    }
    */

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
	{

        return () ->
        {

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (QuollButton.builder ()
                .tooltip (editors,projectcomments,title,toolbar,buttons,openproject,tooltip)
                .iconName (StyleClassNames.OPEN)
                .onAction (ev ->
                {

                    ProjectInfo proj = null;

                    try
                    {

                        proj = Environment.getProjectById (this.message.getForProjectId (),
                                                           this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project for: " +
                                              this.message.getForProjectId (),
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         getUILanguageStringProperty (editors,LanguageStrings.project,actions,openproject,openerrors,general));
                                                  //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                    try
                    {

                        Environment.openProject (proj);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project for: " +
                                              this.message.getForProjectId (),
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         getUILanguageStringProperty (editors,LanguageStrings.project,actions,openproject,openerrors,general));
                                                  //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ());

            controls.addAll (super.getTitleHeaderControlsSupplier ().get ());

            return controls;

        };

	}

    @Override
    public SideBar getMainSideBar ()
    {

        return new ProjectCommentsSideBar (this,
                                           this.message).getSideBar ();

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        // Show the first comment in the first chapter.
        this.viewObject (this.project.getBook (0).getChapters ().get (0).getNotes ().iterator ().next ());

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.COMMENTS;

    }

    @Override
    public StringProperty titleProperty ()
    {

        return UILanguageStringsManager.createStringPropertyWithBinding (() ->
        {

            String verName = this.getProject ().getProjectVersion ().getName ();

            if (verName != null)
            {

                verName = getUILanguageStringProperty (editors,projectcomments,(this.message.isSentByMe () ? sent : received),viewertitleversionwrapper,
                                        //" (%s)",
                                                       verName).getValue ();

            } else {

                verName = "";

            }

            return getUILanguageStringProperty (editors,projectcomments,(this.message.isSentByMe () ? sent : received),viewertitle,
                                //"{Comments} on%s: %s",
                                                verName,
                                                this.project.getName ()).getValue ();

        });

    }

}
