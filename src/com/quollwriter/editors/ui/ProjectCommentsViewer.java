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
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;

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
            
            verName = String.format (" (%s)",
                                     verName);
            
        } else {
            
            verName = "";
            
        }
    
        return String.format ("{Comments} on%s: %s",
                              verName,
                              this.proj.getName ());

    }
    
}
