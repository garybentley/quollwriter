package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.events.*;

public class UpdateProjectMessageBox extends MessageBox<UpdateProjectMessage>
{
        
    private ProjectSentReceivedViewer sentViewer = null;
        
    public UpdateProjectMessageBox (UpdateProjectMessage     mess,
                                    AbstractProjectViewer viewer)
    {
        
        super (mess,
               viewer);
        
    }
        
    public boolean isAutoDealtWith ()
    {
        
        return true;
        
    }    
            
    public void doUpdate ()
    {
                
    }
    
    public void doInit ()
    {
        
        final UpdateProjectMessageBox _this = this;
                
        Project proj = null;
        
        try
        {
                        
            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.NORMAL_PROJECT_TYPE : Project.EDITOR_PROJECT_TYPE));
                                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project for id: " +
                                  this.message.getForProjectId (),
                                  e);
                        
        }
        
        final Project fproj = proj;
        
        String text = "Update sent";//Sent {project} update";
        
        if (!this.message.isSentByMe ())
        {
            
            text = "Update received"; //Received {project} update";
                            
        }
        
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    null);
                                                    //Constants.PROJECT_ICON_NAME);
        
        this.add (h);
                
        JComponent bp = NewProjectMessageBox.getProjectMessageDetails (this.message,
                                                                       this.projectViewer);
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));        
        
        this.add (bp);                             
                        
/*
        String plural = "";
        
        if (this.message.getChapters ().size () > 1)
        {
            
            plural = "s";
            
        }

        // Show:
        //   * Sent
        //   * Version (optional)
        //   * Word/chapter count
        //   * Due by (optional)
        //   * Notes
        
        String rows = "p"; 
        
        ProjectVersion projVer = this.message.getProjectVersion ();
        String verName = projVer.getName ();
        
        if (verName != null)
        {
            
            rows += ", 6px, p";
            
        }
        
        rows += ", 6px, p";
        
        if (projVer.getDueDate () != null)
        {
            
            rows += ", 6px, p";
            
        }
        
        String notes = projVer.getDescription ();
        
        if (notes != null)
        {
            
            rows += ", 6px, top:p";
            
        }
        
        FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                        rows);

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;
               
        builder.addLabel (Environment.replaceObjectNames ("<html><i>{Project}</i></html>"),
                          cc.xy (1,
                                 row));
        
        JLabel openProj = UIUtils.createClickableLabel (this.message.getForProjectName (),
                                                        null,
                                                        new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
            
                if (fproj != null)
                {
            
                    try
                    {
            
                        Environment.openProject (fproj);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to open project: " +
                                              fproj,
                                              e);
                        
                    }
                    
                }
                
            }
            
        });        
        
        openProj.setToolTipText (Environment.replaceObjectNames ("Click to open the {project}"));
        
        builder.add (openProj,
                     cc.xy (3,
                            row));
        
        row += 2;
        
        builder.addLabel (Environment.replaceObjectNames ("<html><i>Sent</i></html>"),
                          cc.xy (1,
                                 row));
        
        builder.addLabel (Environment.formatDateTime (this.message.getWhen ()),
                          cc.xy (3,
                                 row));
        
        row += 2;

        if (verName != null)
        {
            
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Version</i></html>"),
                              cc.xy (1,
                                     row));
            
            builder.addLabel (verName,
                              cc.xy (3,
                                     row));
            
            row += 2;
            
        }
        
        builder.addLabel (Environment.replaceObjectNames (Environment.formatNumber (this.message.getWordCount ()) + " words, " + this.message.getChapters ().size () + " {chapter" + plural + "}"),
                          cc.xy (3,
                                 row));
        
        row += 2;
        
        if (projVer.getDueDate () != null)
        {
        
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Due by</i></html>"),
                              cc.xy (1,
                                     row));
    
            builder.addLabel ((projVer.getDueDate () != null ? Environment.formatDate (projVer.getDueDate ()) : "<i>Not specified.</i>"),
                              cc.xy (3,
                                     row));

            row += 2;
                                     
        }
                                     
        if (notes != null)
        {
    
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Notes</i></html>"),
                              cc.xy (1,
                                     row));
    
            JComponent nc = UIUtils.createHelpTextPane (notes,
                                                        this.projectViewer);
            nc.setBorder (null);
    
            builder.add (nc,
                         cc.xy (3,
                                row));

        }
       
        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        bp.setBorder (UIUtils.createPadding (5, 5, 0, 5));
        
        this.add (bp);             
        */

        if (!this.message.isSentByMe ())
        {
            /*
            JLabel viewProj = UIUtils.createClickableLabel ("Click to view what you sent",
                                                            Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            new ActionListener ()
                                                            {
              
            @Override                                                  
            public void actionPerformed (ActionEvent ev)
            {

                // Load up the project with the specific text.
                // See if we have a project viewer for the project.
                Project proj = null;
                
                try
                {
                    
                    proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                       Project.NORMAL_PROJECT_TYPE);
                
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get project for: " +
                                          _this.message.getForProjectId (),
                                          e);
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to show the {project}, please contact Quoll Writer support for assistance.");

                    return;                        
                    
                }

                try
                {
                
                    Environment.openProject (proj);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to open project: " +
                                          proj,
                                          e);
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to show the {project}, please contact Quoll Writer support for assistance.");

                    return;                        
                    
                }
                
            }});
                            
            viewProj.setBorder (UIUtils.createPadding (5, 10, 5, 5));
            
            this.add (viewProj);            
                                                 
        } else {
           */ 
            
            JLabel viewUpd = UIUtils.createClickableLabel ("Click to view the update",
                                                            Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            new ActionListener ()
                                                            {
              
            @Override                                                  
            public void actionPerformed (ActionEvent ev)
            {

                EditorsUIUtils.showProjectUpdate (_this.message,
                                                  _this.projectViewer,
                                                  null);
                            
            }});
                            
            viewUpd.setBorder (UIUtils.createPadding (6, 10, 0, 0));
            
            this.add (viewUpd);            
                     
        }
        
    }
        
}