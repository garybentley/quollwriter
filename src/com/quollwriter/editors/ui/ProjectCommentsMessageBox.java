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
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

//@MessageBoxFor(Message=ProjectCommentsMessage.class)
public class ProjectCommentsMessageBox extends MessageBox<ProjectCommentsMessage>
{
        
    private JComponent commentsTree = null;
    private AbstractProjectViewer commentsViewer = null;
        
    public ProjectCommentsMessageBox (ProjectCommentsMessage mess,
                                      AbstractViewer         viewer)
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
                
    }
    
    public void doInit ()
                 throws GeneralException
    {
        
        final ProjectCommentsMessageBox _this = this;
            
        ProjectInfo proj = null;
        
        try
        {
                        
            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));
                                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project info for project with id: " +
                                  this.message.getForProjectId (),
                                  e);
                        
        }            
                        
        final String text = String.format ("%s {Comment%s}", // %s
                                           this.message.getComments ().size (),
                                           (this.message.getComments ().size () > 1 ? "s" : ""));
                                           //(this.message.isSentByMe () ? "sent" : "received"));
                
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.COMMENT_ICON_NAME);
        
        this.add (h);
        
        // Show
        //   * Sent/Received
        //   * Version (optional)
        //   * Notes (optional)
        //   * View comments
        
        ProjectVersion pv = this.message.getProjectVersion ();
        String genComm = this.message.getGeneralComment ();

        String verName = pv.getName ();        
                                    
        String rows = "p, 6px, p";
        
        if (verName != null)
        {
            
            rows += ", 6px, p";
            
        }
                
        if (genComm != null)
        {
            
            rows += ", 6px, top:p";
            
        }
        
        rows += ", 6px, p";
        
        EditorEditor ed = this.message.getEditor ();
        
        // We are wimping out here
        String projVerName = this.message.getProjectVersion ().getName ();
                
        String projVerId = this.message.getProjectVersion ().getId ();
                        
        FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                        rows);

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;
             
        builder.addLabel (Environment.replaceObjectNames ("<html><i>{Project}</i></html>"),
                          cc.xy (1,
                                 row));
        
        if (proj != null)
        {
        
            JLabel openProj = UIUtils.createClickableLabel (message.getForProjectName (),
                                                            null,
                                                            new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                
                    ProjectInfo proj = null;
                    
                    try
                    {
                                    
                        proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                           (_this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));
                                                
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get project info for project with id: " +
                                              _this.message.getForProjectId (),
                                              e);
                                    
                    }
                                    
                    if (proj != null)
                    {
                
                        try
                        {
                
                            Environment.openProject (proj);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to open project: " +
                                                  proj,
                                                  e);
                            
                        }
                        
                    }
                    
                }
                
            });        
            
            openProj.setToolTipText (Environment.replaceObjectNames ("Click to open the {project}"));
            
            builder.add (openProj,
                         cc.xy (3,
                                row));
            
        } else {
            
            builder.addLabel (String.format ("<html>%s</html>",
                                             message.getForProjectName ()),
                              cc.xy (3,
                                     row));
            
        }

        row += 2;             
             
        builder.addLabel (String.format ("<html><i>%s</i></html>",
                                         (this.message.isSentByMe () ? "Sent" : "Received")),
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
            
            builder.addLabel (projVerName,
                              cc.xy (3,
                                     row));
            
            row += 2;
            
        }
        
        if (genComm != null)
        {
            
            TextIterator ti = new TextIterator (genComm);
            
            if (ti.getSentenceCount () > 1)
            {
                
                genComm = ti.getFirstSentence ().getText ();

            }
                
            JComponent mess = UIUtils.createHelpTextPane (genComm,
                                                          _this.viewer);

            mess.setBorder (null);
                                                          
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Notes</i></html>"),
                              cc.xy (1,
                                     row));
            
            builder.add (mess,
                         cc.xy (3,
                                row));

            row += 2;            
            
        }
        
        if (proj != null)
        {
        
            JLabel viewComments = UIUtils.createClickableLabel ("Click to view the {comments}",
                                                                Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                     Constants.ICON_CLICKABLE_LABEL),
                                                                new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                
                    _this.message.setDealtWith (true);
                    
                    try
                    {
                    
                        EditorsEnvironment.updateMessage (_this.message);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to update message: " +
                                              _this.message,
                                              e);
                        
                    }
                
                    if (_this.commentsViewer != null)
                    {
                        
                        _this.commentsViewer.setExtendedState (JFrame.NORMAL);
                        _this.commentsViewer.toFront ();
                        
                        return;
                        
                    }
                    
                    EditorsUIUtils.showProjectComments (_this.message,
                                                        _this.viewer,
                                                        new ActionListener ()
                                                        {
                                                            
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                                
                                                                _this.commentsViewer = (AbstractProjectViewer) ev.getSource ();
                                                                
                                                                _this.commentsViewer.addWindowListener (new WindowAdapter ()
                                                                {
                                                                    
                                                                    public void windowClosed (WindowEvent ev)
                                                                    {
                                                                        
                                                                        _this.commentsViewer = null;
                                                                        
                                                                    }
                                                                    
                                                                });                                                                
                                                                
                                                            }
                                                            
                                                        });
                
/*                
                    if (_this.message.isSentByMe ())
                    {
                        
                        // Show a popup with the comments in it.
    
                        Project p = _this.projectViewer.getProject ();
                        
                        // Need to now "fill out" the chapters and notes to have the correct values.
                        // We use the ids to get the keys.
                        for (Chapter c : _this.message.getChapters ())
                        {
                            
                            NamedObject ch = (NamedObject) p.getObjectById (Chapter.class,
                                                                            c.getId ());
                            
                            if (ch != null)
                            {
                                            
                                c.setName (ch.getName ());
                
                            }
                                        
                        }
                        
                        JTree tree = EditorsUIUtils.createViewTree (_this.message.getChapters (),
                                                                    _this.projectViewer);
                
                        UIUtils.expandAllNodesWithChildren (tree);
                        
                        Dimension pref = tree.getPreferredSize ();
                        
                        if (pref.height > 300)
                        {
                            
                            pref.height = 300;
                            
                        } else {
                            
                            pref.height += 5;
                                        
                        }
                        
                        final JScrollPane sp = UIUtils.createScrollPane (tree);
                                                                         
                        sp.setPreferredSize (pref);        
                        sp.setBorder (UIUtils.createPadding (5, 0, 0, 0));
                        
                        UIUtils.createClosablePopup (text,
                                                     Environment.getIcon (Constants.COMMENT_ICON_NAME,
                                                                          Constants.ICON_POPUP),
                                                     null,
                                                     sp,
                                                     _this.viewer,
                                                     null);
                        
                    } else {
                        
                        if (_this.commentsViewer != null)
                        {
                            
                            _this.commentsViewer.setExtendedState (JFrame.NORMAL);
                            _this.commentsViewer.toFront ();
                            
                            return;
                            
                        }
                        
                        EditorsUIUtils.showProjectComments (_this.message,
                                                            _this.viewer,
                                                            new ActionListener ()
                                                            {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                    
                                                                    _this.commentsViewer = (AbstractProjectViewer) ev.getSource ();
                                                                    
                                                                    _this.commentsViewer.addWindowListener (new WindowAdapter ()
                                                                    {
                                                                        
                                                                        public void windowClosed (WindowEvent ev)
                                                                        {
                                                                            
                                                                            _this.commentsViewer = null;
                                                                            
                                                                        }
                                                                        
                                                                    });                                                                
                                                                    
                                                                }
                                                                
                                                            });
    
                    }
                    */
                }
                
            });        
            
            viewComments.setBorder (UIUtils.createPadding (0, 10, 0, 0));
            
            builder.add (viewComments,
                         cc.xywh (1,
                                  row,
                                  3,
                                  1));

        }
        
        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));               
        
        this.add (bp);             

    }
    
}