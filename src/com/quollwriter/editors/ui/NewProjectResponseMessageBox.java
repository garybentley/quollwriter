package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.*;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;

// Use an annotation?
//@MessageBox(class=NewProjectResponseMessage)
public class NewProjectResponseMessageBox extends MessageBox<NewProjectResponseMessage>
{
        
    public NewProjectResponseMessageBox (NewProjectResponseMessage mess,
                                         AbstractProjectViewer     viewer)
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

        Project proj = null;
        
        try
        {
                        
            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));
                                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project for id: " +
                                  this.message.getForProjectId (),
                                  e);
                        
        }
        
        final Project fproj = proj;
        
        boolean accepted = this.message.isAccepted ();
        String resMessage = this.message.getResponseMessage ();

        String iconName = (accepted ? Constants.ACCEPTED_ICON_NAME : Constants.REJECTED_ICON_NAME);
        
        String message = "";
        
        if (this.message.isSentByMe ())
        {
            
            String text = "Accepted";
            
            if (!accepted)
            {

                text = "Rejected";
                
            }

            message = text + " {project}";
            
        } else {
            
            message = "{Project} accepted";
            
            if (!accepted)
            {
                
                message = "{Project} rejected";
                
            }
            
        }

        JComponent h = UIUtils.createBoldSubHeader (message,
                                                    iconName);        
        
        this.add (h);

        String rows = "top:p";
        
        if (resMessage != null)
        {
            
            rows += ", 6px, p";
            
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

        if (resMessage != null)
        {
        
            builder.addLabel (Environment.replaceObjectNames ("<html><i>{Message}</i></html>"),
                              cc.xy (1,
                                     row));
                            
            JComponent nc = UIUtils.createHelpTextPane (resMessage,
                                                        this.projectViewer);
            nc.setBorder (null);
    
            builder.add (nc,
                         cc.xy (3,
                                row));
                            
        }
                            
        Border border = new EmptyBorder (5, 5, 0, 5);
        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        bp.setBorder (border);
        
        this.add (bp);             
        
        final NewProjectResponseMessageBox _this = this;
        
        if (this.message.isSentByMe ())
        {
        
            JLabel viewProj = UIUtils.createClickableLabel ("Click to view the {project}",
                                                            Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                
                    Project proj = null;
                    
                    try
                    {
                    
                        proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                           Project.EDITOR_PROJECT_TYPE);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get project: " +
                                              _this.message.getForProjectId (),
                                              e);
                        
                        UIUtils.showErrorMessage (Environment.getFocusedProjectViewer (),
                                                  "Unable to open {project}, please contact Quoll Writer support for assistance.");
                        
                        return;
                        
                    }
                    
                    try
                    {
                    
                        Environment.openProject (proj);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get project: " +
                                              _this.message.getForProjectId (),
                                              e);
                        
                        UIUtils.showErrorMessage (Environment.getFocusedProjectViewer (),
                                                  "Unable to open {project}, please contact Quoll Writer support for assistance.");
                        
                        return;
                                    
                    }

                }
                
            });        
            
            viewProj.setBorder (UIUtils.createPadding (5, 0, 5, 5));
            
            this.add (viewProj);

        }
        
        if (!this.message.isSentByMe ())
        {
            
            
            
        }
    }
    
}