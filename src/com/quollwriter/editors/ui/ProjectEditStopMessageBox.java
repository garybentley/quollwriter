package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

//@MessageBoxFor(Message=ProjectEditStopMessage.class)
public class ProjectEditStopMessageBox extends MessageBox<ProjectEditStopMessage>
{
        
    private AbstractProjectViewer commentsViewer = null;
        
    public ProjectEditStopMessageBox (ProjectEditStopMessage     mess,
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
        bp.setBorder (UIUtils.createPadding (5, 5, 0, 5));
        
        this.add (bp);             
                                                                    
    }
    
}