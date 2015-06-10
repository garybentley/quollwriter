package com.quollwriter.editors.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.QPopup;

public class EditorProjectSideBar extends AbstractSideBar<EditorProjectViewer>
{
    
    private EditorProjectViewer viewer = null;
    private EditorChaptersAccordionItem chapters = null;
    private EditorInfoBox editorInfoBox = null;
    private EditorEditor editor = null;
    private JComponent content = null;
    private JLabel unsentLabel = null;
    private JLabel otherVersionsLabel = null;
    
    public EditorProjectSideBar (EditorProjectViewer v)
    {
        
        super (v);
        
        this.viewer = v;
        this.content = new Box (BoxLayout.Y_AXIS);
        
        this.content.setOpaque (false);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setMinimumSize (new Dimension (200,
                                            200));
        
        final EditorProjectSideBar _this = this;
        
        v.addProjectEventListener (new ProjectEventListener ()
        {
            
            public void eventOccurred (ProjectEvent ev)
            {
             
                if (ev.getContextObject () instanceof Note)
                {
                    
                    _this.showUnsentNotification ();
                    
                }
                
            }
            
        });
        
    }
    
    private void showUnsentNotification ()
    {
                
        // Get the unsent note count for the project (for this version).
        Set<Note> notes = null;
        
        try
        {
            
            notes = this.projectViewer.getUnsentComments ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to show unsent comments notification",
                                  e);
            
            return;
            
        }
        
        int count = notes.size ();
        
        if (count > 0)
        {                
        
            String l = "";
        
            if (count == 1)
            {
                
                l = "1 {comment} hasn't been sent, click to review/send it now";
                
            } else {
                
                l = count + " {comments} haven't been sent, click to review/send them now";
                
            }
                
            this.unsentLabel.setText (l);
        
        }
        
        this.unsentLabel.setVisible (count > 0);
        
    }
        
    public JComponent getContent ()
    {
        
        return this.content;
                 
    }
    
    private void showOtherVersionsSelector ()
    {
        
        final EditorProjectSideBar _this = this;
        
        java.util.List<ProjectVersion> pvs = null;
        
        try
        {
            
            // TODO: Encapsulate this better.
            pvs = (java.util.List<ProjectVersion>) this.viewer.getObjectManager ().getObjects (ProjectVersion.class,
                                                                              this.viewer.getProject (),
                                                                              null,
                                                                              false);
                                                     
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project versions for project: " +
                                  this.viewer.getProject (),
                                  e);

            return;            
                                                     
        }        
        
        int c = 0;
        
        ProjectVersion currPv = this.viewer.getProject ().getProjectVersion ();        
        
        Set<ProjectVersion> others = new LinkedHashSet ();
        
        for (ProjectVersion pv : pvs)
        {
                        
            if ((currPv != null)
                &&
                (pv.equals (currPv))
               )
            {
                
                continue;
                
            }
            
            others.add (pv);
            
            c++;

        }
        
        if (c == 0)
        {
            
            return;
            
        }
        
        if (c == 1)
        {
            
            this.viewer.switchToProjectVersion (others.iterator ().next ());
            
        } else {
            
            final Vector<ProjectVersion> projVers = new Vector (others);
            
            final JList<ProjectVersion> projVersL = new JList (projVers);

            projVersL.setSelectedValue (projVersL,
                                        false);

            projVersL.addListSelectionListener (new ListSelectionListener ()
            {
               
                public void valueChanged (ListSelectionEvent ev)
                {
                    
                    ProjectVersion pv = projVersL.getSelectedValue ();
            
                    _this.viewer.switchToProjectVersion (pv);
                    
                    UIUtils.closePopupParent (projVersL);
            
                }
                
            });
            
            projVersL.setCellRenderer (new DefaultListCellRenderer ()
            {
               
                private Map<String, ImageIcon> images = new HashMap ();
               
                public Component getListCellRendererComponent (JList   list,
                                                               Object  value,
                                                               int     index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus)
                {
                    
                    Box b = new Box (BoxLayout.Y_AXIS);

                    if (index < projVers.size () - 1)
                    {
                    
                        b.setBorder (UIUtils.createBottomLineWithPadding (3, 3, 3, 3));
                        
                    } else {
                        
                        b.setBorder (UIUtils.createPadding (3, 3, 3, 3));
                        
                    }
                    
                    ProjectVersion pv = (ProjectVersion) value;
                    
                    Header h = UIUtils.createBoldSubHeader (pv.getName (),
                                                            null);

                    b.add (h);

                    h.setPreferredSize (new Dimension (Short.MAX_VALUE,
                                                     h.getPreferredSize ().height));
                    
                    StringBuilder s = new StringBuilder ();
                    
                    if (pv.getDueDate () != null)
                    {
                        
                        s.append (String.format ("Due by: %s",
                                                 Environment.formatDate (pv.getDueDate ())));
                        
                    }
                    
                    try
                    {
                        
                        int c = _this.viewer.getUnsentComments (pv).size ();
                        
                        if (c > 0)
                        {
                            
                            if (c == 1)
                            {
                                
                                s.append (", 1 unsent comment");
                                
                            } else {
                            
                                s.append (String.format (", %s unsent comments",
                                                         Environment.formatNumber (c)));
                                
                            }
                                        
                        } else {
                            
                            s.append (", all comments sent");
                            
                        }
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get unsent comments for project version: " +
                                              pv,
                                              e);
                        
                    }
                    JLabel info = UIUtils.createInformationLabel (s.toString ());
                    
                    info.setBorder (UIUtils.createPadding (0, 5, 0, 5));
                    
                    b.add (info);
                    
                    b.setToolTipText (String.format ("<html>Click to view version <b>%s</b>.</html>",
                                                     pv.getName ()));
                    
                    return b;
                
                }
                
            });            

            final QPopup qp = UIUtils.createClosablePopup ("Select a version to view",
                                                           Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                Constants.ICON_POPUP),
                                                           null);
            
            Box content = new Box (BoxLayout.Y_AXIS);                
        
            UIUtils.setAsButton (projVersL);
            
            content.add (projVersL);
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
            qp.setContent (content);
    
            content.setPreferredSize (new Dimension (UIUtils.DEFAULT_POPUP_WIDTH,
                                                     content.getPreferredSize ().height));
    
            _this.viewer.showPopupAt (qp,
                                      otherVersionsLabel,
                                      false);
            
            qp.setDraggable (_this.viewer);
            
        }        
        
    }
    
    private void showOtherVersionsLabel ()
    {
        
        java.util.List<ProjectVersion> pvs = null;
        
        try
        {
            
            // TODO: Encapsulate this better.
            pvs = (java.util.List<ProjectVersion>) this.viewer.getObjectManager ().getObjects (ProjectVersion.class,
                                                                              this.viewer.getProject (),
                                                                              null,
                                                                              false);
                                                     
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project versions for project: " +
                                  this.viewer.getProject (),
                                  e);

            return;            
                                                     
        }        
        
        int c = 0;
        
        ProjectVersion currPv = this.viewer.getProject ().getProjectVersion ();        
        
        ProjectVersion otherPv = null;
        
        for (ProjectVersion pv : pvs)
        {
                        
            if ((currPv != null)
                &&
                (pv.equals (currPv))
               )
            {
                
                continue;
                
            }
            
            otherPv = pv;
            
            c++;

        }
        
        if (c > 0)
        {
                        
            String l = "";
        
            if (c == 1)
            {
                
                l = String.format ("1 other version (<b>%s</b>) is available, click to view it",
                                   otherPv.getName ());
                                
            } else {
                
                l = c + " other versions are available, click to select one";
                
            }
                        
            this.otherVersionsLabel.setText (l);
                        
        }   
            
        this.otherVersionsLabel.setVisible (c > 0);
        
    }
    
    @Override
    public void init ()
               throws GeneralException
    {
        
        super.init ();
                
        this.editor = this.viewer.getProject ().getForEditor ();
                
        this.editorInfoBox = new EditorInfoBox (this.editor,
                                                this.viewer).init ();
        
        this.editorInfoBox.addBasicPopupListener ();        
        
        this.editorInfoBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          this.editorInfoBox.getPreferredSize ().height));
        this.editorInfoBox.setBorder (UIUtils.createPadding (5, 5, 5, 0));
                
        this.chapters = new EditorChaptersAccordionItem (this.viewer);
        this.chapters.init ();
        
        this.content.add (this.editorInfoBox);
        
        ProjectVersion projVer = this.viewer.getProject ().getProjectVersion ();
        
        if (projVer != null)
        {
            
            JComponent pvp = EditorsUIUtils.getProjectVersionPanel (projVer,
                                                                    this.projectViewer);

            pvp.setBorder (UIUtils.createPadding (5, 5, 5, 5));
                                                
            this.content.add (pvp);
            
        }
        
        final EditorProjectSideBar _this = this;
        
        //Get the due by/response message (properties from the project)
        //Get the project description (sent by editor)
         
        // Create a box to indicate when there are comments to send.
        this.unsentLabel = UIUtils.createClickableLabel ("",
                                                         Environment.getIcon (Constants.COMMENT_ICON_NAME,
                                                                              Constants.ICON_MENU),
                                                         new ActionListener ()
                                                         {
                                                                
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                                 
                                                                EditorsUIUtils.showSendUnsentComments (_this.projectViewer,
                                                                                                       null);
                                                                    
                                                            }
                                                                
                                                         });
        
        this.unsentLabel.setBorder (UIUtils.createPadding (5, 10, 5, 5));
        
        this.showUnsentNotification ();
        
        this.content.add (this.unsentLabel);
        
        this.otherVersionsLabel = UIUtils.createClickableLabel ("",
                                                                Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                     Constants.ICON_MENU),
                                                                new ActionListener ()
                                                                {
                                                                
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {
                                                                 
                                                                        _this.showOtherVersionsSelector ();
                                                                        
                                                                    }
                                                                    
                                                                });
        
        this.otherVersionsLabel.setBorder (UIUtils.createPadding (5, 10, 5, 5));
        
        this.showOtherVersionsLabel ();
        
        this.content.add (this.otherVersionsLabel);
                
        JComponent sp = this.wrapInScrollPane (this.chapters);
        sp.setBorder (null);
        sp.setMinimumSize (new Dimension (200,
                                          300));

        this.content.add (sp);
        this.content.setBorder (new EmptyBorder (0, 5, 0, 5));
        
    }
    
    public void reloadTreeForObjectType (String objType)
    {

        this.chapters.update (); 

    }

    public void panelShown (MainPanelEvent ev)
    {

        this.chapters.setObjectSelectedInTree (ev.getPanel ().getForObject ());
        
    }            
    
    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {
        
        JTree tree = this.getTreeForObjectType (treeObjType);
        
        if (tree == null)
        {
            
            return;
            
        }
        
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                           obj));
        
    }
    
    public JTree getTreeForObjectType (String objType)
    {
                        
        return this.chapters.getTree ();
        
    }

    @Override
    public void sideBarShown (SideBarEvent ev)
    {
        
        this.otherSideBarsButton.setVisible (false);
        
    }    
    
    @Override
    public boolean removeOnClose ()
    {
        
        return false;
        
    }

    @Override
    public void onHide ()
    {
        
    }
    
    @Override
    public void onShow ()
    {
        
    }
    
    @Override
    public void onClose ()
    {
        
        
    }

    public boolean canClose ()
    {
        
        return false;
        
    }
        
    public String getTitle ()
    {
        
        return "Editing for";
        
    }

    public String getIconType ()
    {
        
        return null;//Constants.EDITORS_ICON_NAME;
        
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
    {
        
        return null;
        
    }
     
}