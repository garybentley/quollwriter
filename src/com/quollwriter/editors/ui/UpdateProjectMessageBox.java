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
        
    private Box responseBox = null;        
    private ProjectSentReceivedViewer sentViewer = null;
        
    public UpdateProjectMessageBox (UpdateProjectMessage     mess,
                                    AbstractProjectViewer viewer)
    {
        
        super (mess,
               viewer);
        
    }
        
    public boolean isAutoDealtWith ()
    {
        
        return false;
        
    }    
            
    public void doUpdate ()
    {

        this.responseBox.setVisible (!this.message.isDealtWith ());
                
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
        
        String text = "{Project} update sent";//Sent {project} update";
        
        if (!this.message.isSentByMe ())
        {
            
            text = "{Project} update received"; //Received {project} update";
                            
        }
        
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.EDIT_ICON_NAME);
        
        this.add (h);
                
        JComponent bp = NewProjectMessageBox.getProjectMessageDetails (this.message,
                                                                       this.projectViewer);
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));        
        
        this.add (bp);                             
            
        this.responseBox = new Box (BoxLayout.Y_AXIS);
        
        this.responseBox.setVisible (false);
                
        this.add (this.responseBox);            
                        
        if (this.message.isSentByMe ())
        {
            
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
            
            ActionListener updateOrView = new ActionListener ()
            {
                  
                @Override                                                  
                public void actionPerformed (ActionEvent ev)
                {
        
                    EditorsUIUtils.showProjectUpdate (_this.message,
                                                      _this.projectViewer,
                                                      null);
                                
                }
                
            };
            
            // Not sent by me.            
            if (!this.message.isDealtWith ())
            {
                                                                
                JButton update = UIUtils.createButton ("Update the {project}",
                                                       null);
    
                update.setToolTipText ("Click to update the {project}");
                update.addActionListener (updateOrView);

                JButton[] buts = new JButton[] { update };
    
                JComponent bb = UIUtils.createButtonBar2 (buts,
                                                          Component.LEFT_ALIGNMENT);
                            
                bb.setAlignmentX (Component.LEFT_ALIGNMENT);
                
                this.responseBox.add (bb);
                this.responseBox.setVisible (true);
                
            } 
                     
        }
        
    }
        
}