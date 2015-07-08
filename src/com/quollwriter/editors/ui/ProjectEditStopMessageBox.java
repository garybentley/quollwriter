package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

//@MessageBoxFor(Message=ProjectEditStopMessage.class)
public class ProjectEditStopMessageBox extends MessageBox<ProjectEditStopMessage>
{
        
    private AbstractProjectViewer commentsViewer = null;
    private Box responseBox = null;
        
    public ProjectEditStopMessageBox (ProjectEditStopMessage     mess,
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

        if (this.message.isDealtWith ())
        {
            
            if (this.responseBox != null)
            {
                
                this.responseBox.setVisible (false);
                
            }
            
        }
                
    }
    
    public void doInit ()
    {
        
        final ProjectEditStopMessageBox _this = this;
        
        Project proj = null;
        
        try
        {
                        
            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               null);
                                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project for id: " +
                                  this.message.getForProjectId (),
                                  e);
                        
        }
        
        final Project fproj = proj;
            
        String title = "Stopped editing {project}";
                                
        JComponent h = UIUtils.createBoldSubHeader (title,
                                                    Constants.CANCEL_ICON_NAME);
        
        this.add (h);
                        
        String reason = this.message.getReason ();
                                    
        String rows = "top:p";
        
        if (reason != null)
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
        
        if (reason != null)
        {
            
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Message</i></html>"),
                              cc.xy (1,
                                     row));            
            
            builder.addLabel (String.format ("<html>%s</html>",
                                             reason),
                              cc.xy (3,
                                     row));
            
            row += 2;
            
        }
        
        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));
        
        this.add (bp);                     
        
        if (!this.message.isDealtWith ())
        {
            
            // Show the response.
            this.responseBox = new Box (BoxLayout.Y_AXIS);
            
            this.add (this.responseBox);
            
            JTextPane rdesc = UIUtils.createHelpTextPane (String.format ("<b>%s</b> has stopped editing {project} <b>%s</b>.",
                                                                         this.message.getEditor ().getShortName (),
                                                                         this.message.getForProjectName ()),
                                                         this.projectViewer);        
            
            this.responseBox.add (Box.createVerticalStrut (5));
                                
            Box rdescb = new Box (BoxLayout.Y_AXIS);
            rdescb.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            rdescb.add (rdesc);
            rdescb.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.responseBox.add (rdescb);
            
            rdesc.setBorder (null);
            rdesc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                          rdesc.getPreferredSize ().height));                

            final EditorEditor ed = this.message.getEditor ();    
                                 
            this.responseBox.setBorder (UIUtils.createPadding (5, 0, 0, 0));
            
            JButton ok = new JButton ("Ok, got it");
    
            ok.addActionListener (new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    try
                    {
    
                        Project p = Environment.getProjectById (_this.message.getForProjectId (),
                                                                Project.NORMAL_PROJECT_TYPE);
                                    
                        ProjectEditor pe = EditorsEnvironment.getProjectEditor (p,
                                                                                ed);
                        
                        pe.setCurrent (false);
                        pe.setEditorTo (new Date ());
                        pe.setStatusMessage (String.format ("Stopped editing: %s",
                                                            Environment.formatDate (pe.getEditorTo ())));
                        
                        EditorsEnvironment.updateProjectEditor (pe);
                                              
                        _this.message.setDealtWith (true);
                        
                        EditorsEnvironment.updateMessage (_this.message);
                                                    
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);
                        
                        UIUtils.showErrorMessage (_this.projectViewer,
                                                  "Unable to update {contact}, please contact Quoll Writer support for assitance.");
                        
                        return;
                        
                    }
    
                }
    
            });
                        
            JButton[] buts = new JButton[] { ok };
                                                    
            JPanel bb = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); 
            bb.setOpaque (false);                            
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            bb.setBorder (UIUtils.createPadding (5, 0, 0, 0));
            
            this.responseBox.add (bb);                         
            
        }
                                                                            
    }
    
}