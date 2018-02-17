package com.quollwriter.editors.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Vector;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class ProjectCommentsViewer extends ProjectSentReceivedViewer<ProjectCommentsMessage>
{

    public static final String OPEN_PROJECT_HEADER_CONTROL_ID = "openProject";

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
    public Set<String> getTitleHeaderControlIds ()
	{

        Set<String> ids = new LinkedHashSet<> ();

        ids.add (OPEN_PROJECT_HEADER_CONTROL_ID);

        ids.addAll (super.getTitleHeaderControlIds ());

        return ids;

    }

    @Override
    public JComponent getTitleHeaderControl (String id)
    {

        if (id == null)
        {

            return null;

        }

        java.util.List<String> prefix = Arrays.asList (editors,projectcomments,title,toolbar,buttons);

        final ProjectCommentsViewer _this = this;

        JComponent c = null;

        if (id.equals (OPEN_PROJECT_HEADER_CONTROL_ID))
        {

            return UIUtils.createButton (Constants.OPEN_PROJECT_ICON_NAME,
                                         Constants.ICON_TITLE_ACTION,
                                         getUIString (prefix,openproject,tooltip),
                                              //"Click to open the find",
                                              new ActionAdapter ()
                                              {

                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      ProjectInfo proj = null;

                                                      try
                                                      {

                                                          proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                                                             _this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE);

                                                      } catch (Exception e) {

                                                          Environment.logError ("Unable to get project for: " +
                                                                                _this.message.getForProjectId (),
                                                                                e);

                                                          UIUtils.showErrorMessage (_this,
                                                                                    getUIString (editors,project,actions,openproject,openerrors,general));
                                                                                    //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                                                          return;

                                                      }

                                                      try
                                                      {

                                                          Environment.openProject (proj);

                                                      } catch (Exception e) {

                                                          Environment.logError ("Unable to get project for: " +
                                                                                _this.message.getForProjectId (),
                                                                                e);

                                                          UIUtils.showErrorMessage (_this,
                                                                                    getUIString (editors,project,actions,openproject,openerrors,general));
                                                                                    //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                                                          return;

                                                      }

                                                  }

                                              });

        }

        return super.getTitleHeaderControl (id);

    }

    @Override
    public ProjectSentReceivedSideBar getSideBar ()
    {

        return new ProjectCommentsSideBar (this,
                                           this.message);

    }

    @Override
    public void init ()
               throws Exception
    {

        super.init ();

        // Show the first comment in the first chapter.
        this.viewObject (this.proj.getBook (0).getChapters ().get (0).getNotes ().iterator ().next ());

    }

    @Override
    public String getViewerIcon ()
    {

        return Constants.COMMENT_ICON_NAME;

    }

    @Override
    public String getViewerTitle ()
    {

        String verName = this.getProject ().getProjectVersion ().getName ();

        if (verName != null)
        {

            verName = String.format (getUIString (editors,projectcomments,(this.message.isSentByMe () ? sent : received),viewertitleversionwrapper),
                                    //" (%s)",
                                     verName);

        } else {

            verName = "";

        }

        return String.format (getUIString (editors,projectcomments,(this.message.isSentByMe () ? sent : received),viewertitle),
                            //"{Comments} on%s: %s",
                              verName,
                              this.proj.getName ());

    }

}
