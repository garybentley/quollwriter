package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.renderers.*;

public class LinkedToAccordionItem extends AccordionItem
{
        
    private ProjectViewer projectViewer = null;
    private NamedObject object = null;
    private JTree viewTree = null;
    private JTree editTree = null;
    private Box content = null;
    private Box view = null;
    private Box edit = null;
        
    public LinkedToAccordionItem (ProjectViewer pv,
                                  NamedObject   c)
    {
        
        super ("Linked to",
               Constants.LINK_ICON_NAME);
            
        this.object= c;
        this.projectViewer = pv;

        this.viewTree = UIUtils.createLinkedToTree (this.projectViewer,
                                                    this.object,
                                                    false);

        this.viewTree.setAlignmentX (Component.LEFT_ALIGNMENT);
                        
        this.content = new Box (BoxLayout.Y_AXIS);
            
        this.content.setBorder (new EmptyBorder (5, 0, 10, 0));
        
        this.view = new Box (BoxLayout.Y_AXIS);

        this.view.add (this.viewTree);
        
        this.edit = new Box (BoxLayout.Y_AXIS);
        
        this.content.add (this.view);
        this.content.add (this.edit);
        
        this.edit.setVisible (false);

        final LinkedToAccordionItem _this = this;
                
        this.editTree = UIUtils.createLinkedToTree (this.projectViewer,
                                                    this.object,
                                                    true);
                                                    
        this.edit.add (this.editTree);

        this.editTree.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                       Short.MAX_VALUE));

        JButton save = UIUtils.createButton (Constants.SAVE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Click to save the links",
                                             new ActionAdapter ()
                                             {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                                                                        
                                                    _this.save ();
                                                    
                                                }
                                                
                                             });

        JButton cancel = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to cancel",
                                               new ActionAdapter ()
                                               {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                    _this.update (_this.object);
                                                    
                                                  }
                                                  
                                               });

        List<JButton> buts = new ArrayList ();

        buts.add (save);
        buts.add (cancel);

        JToolBar tb = UIUtils.createButtonBar (buts);

        tb.setAlignmentX (Component.LEFT_ALIGNMENT);        
        
        Box buttons = new Box (BoxLayout.X_AXIS);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        buttons.add (Box.createHorizontalGlue ());
        buttons.add (tb);

        buttons.setBorder (new CompoundBorder (new MatteBorder (1,
                                                                0,
                                                                0,
                                                                0,
                                                                UIUtils.getColor ("#dddddd")),
                                               new EmptyBorder (3,
                                                                3,
                                                                3,
                                                                3)));
                                                                
                                                                
        this.edit.add (buttons);        

        this.edit.setBorder (new EmptyBorder (3, 0, 0, 0));

        Header h = this.getHeader ();
                                                  
        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));
        
    }
        
    public void update (NamedObject o)
    {
        
        this.object = o;
        
        this.updateTree ();
        
    }

    private void save ()
    {

        // Get all the link items from the tree.
        DefaultTreeModel dtm = (DefaultTreeModel) this.editTree.getModel ();

        Set s = new HashSet ();

        try
        {

            UIUtils.getSelectedObjects ((DefaultMutableTreeNode) dtm.getRoot (),
                                        this.object,
                                        s);

        } catch (Exception e)
        {

            Environment.logError ("Unable to get objects to link to for: " +
                                  this.object,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to add/edit object.");

            return;

        }

        // Save the links
        try
        {

            this.projectViewer.saveLinks (this.object,
                                          s);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save links: " +
                                  this.object,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to save links.");

            return;

        }
        
        java.util.Set<NamedObject> otherObjects = this.object.getOtherObjectsInLinks ();

        this.projectViewer.refreshObjectPanels (otherObjects);

        this.updateTree ();        
        
    }
    
    private void edit ()
    {
        
        if (this.edit.isVisible ())
        {
            
            this.save ();
            
            return;
            
        }
                
        this.setContentVisible (true);
        
        this.editTree.setModel (UIUtils.getLinkedToTreeModel (this.projectViewer,
                                                              this.object,
                                                              true));

        UIUtils.expandPathsForLinkedOtherObjects (this.editTree,
                                                  this.object);
                                                              
        this.view.setVisible (false);
        this.edit.setVisible (true);
        this.validate ();
        this.repaint ();        

        UIUtils.scrollIntoView (this);
        
    }
    
    public void updateTree ()
    {

        UIUtils.scrollIntoView (this);
        
        this.viewTree.setModel (UIUtils.getLinkedToTreeModel (this.projectViewer,
                                                              this.object,
                                                              false));

        UIUtils.expandAllNodesWithChildren (this.viewTree);

        this.edit.setVisible (false);
        this.view.setVisible (true);
        this.validate ();
        this.repaint ();        
        
    }
    
    public JComponent getContent ()
    {
        
        return this.content;
        
    }
    
    @Override
    public void init ()
    {
        
        super.init ();

        final LinkedToAccordionItem _this = this;
        
        ActionListener action = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.edit ();
                
            }
            
        };        
                
        List<JButton> conts = new ArrayList ();
        
        conts.add (UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                         Constants.ICON_SIDEBAR,
                                         "Click to edit",
                                         action));

        this.setHeaderControls (UIUtils.createButtonBar (conts));                
        
        UIUtils.expandAllNodesWithChildren (this.viewTree);
/*
        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         this.getPreferredSize ().height));
  */     
        //this.setMaximumSize (this.getPreferredSize ());        
        
    }
    
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        final LinkedToAccordionItem _this = this;
        
        ActionListener action = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.edit ();
                
            }
            
        };        
        
        m.add (UIUtils.createMenuItem ("Edit",
                                       Constants.EDIT_ICON_NAME,
                                       action));
    
    }
    
    
}