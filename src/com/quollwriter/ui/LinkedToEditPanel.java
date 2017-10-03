package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.Runner;

public class LinkedToEditPanel<E extends NamedObject, V extends AbstractProjectViewer> extends EditPanel
{
    
    private E obj = null;
    private V viewer = null;
    private JTree linkedToEditTree = null;
    private JTree linkedToViewTree = null;    
    
    public LinkedToEditPanel (E obj,
                              V viewer)
    {
        
        this.obj = obj;
        this.viewer = viewer;
        
    }
    
    @Override
    public void refreshViewPanel ()
    {

        List exclude = new ArrayList ();
        exclude.add (this.obj);

        // Painful but just about the only way.
        this.viewer.setLinks (this.obj);

        // Get all the "other objects" for the links the note has.
        Iterator<Link> it = this.obj.getLinks ().iterator ();

        Set links = new HashSet ();

        while (it.hasNext ())
        {

            links.add (it.next ().getOtherObject (this.obj));

        }

        DefaultTreeModel m = new DefaultTreeModel (UIUtils.createLinkToTree (this.viewer.getProject (),
                                                                             exclude,
                                                                             links,
                                                                             false));

        this.linkedToViewTree.setModel (m);

        UIUtils.expandAllNodesWithChildren (this.linkedToViewTree);

    }

    @Override
    public JComponent getSaveButton ()
    {
        
        final LinkedToEditPanel _thisep = this;
        
        IconProvider ip = this.getIconProvider ();
        
        JButton save = UIUtils.createButton (ip.getIcon (Constants.SAVE_ICON_NAME,
                                                      Constants.ICON_PANEL_SECTION_ACTION),
                                             String.format (Environment.getUIString (LanguageStrings.linkedto,
                                                                                     LanguageStrings.edit,
                                                                                     LanguageStrings.buttons,
                                                                                     LanguageStrings.save,
                                                                                     LanguageStrings.tooltip),
                                                            Environment.getObjectTypeName (this.obj)),
                                             //"Click to save the details",
                                             new ActionListener ()
                                             {
                                            
                                                @Override
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                  
                                                    _thisep.doSave ();
                                                  
                                                }
                                            
                                          });

        return save;
            
    }
    
    @Override
    public JComponent getCancelButton ()
    {
        
        final LinkedToEditPanel _thisep = this;
        
        IconProvider ip = this.getIconProvider ();        
        
        JButton cancel = UIUtils.createButton (ip.getIcon (Constants.CANCEL_ICON_NAME,
                                                           Constants.ICON_PANEL_SECTION_ACTION),
                                               Environment.getUIString (LanguageStrings.linkedto,
                                                                        LanguageStrings.edit,
                                                                        LanguageStrings.buttons,
                                                                        LanguageStrings.cancel,
                                                                        LanguageStrings.tooltip),                                              
                                               //"Click to cancel the editing",
                                               new ActionListener ()
                                               {
                                                
                                                  @Override
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                                    
                                                      _thisep.doCancel ();
                                                    
                                                  }
                                                
                                               });

        return cancel;
            
    }
    
    @Override
    public JComponent getEditButton ()
    {
        
        final LinkedToEditPanel _thisep = this;
        
        IconProvider ip = this.getIconProvider ();                            
        
        return UIUtils.createButton (ip.getIcon (Constants.EDIT_ICON_NAME,
                                                 Constants.ICON_PANEL_SECTION_ACTION),
                                     String.format (Environment.getUIString (LanguageStrings.linkedto,
                                                                             LanguageStrings.view,
                                                                             LanguageStrings.buttons,
                                                                             LanguageStrings.edit,
                                                                             LanguageStrings.tooltip),
                                                    Environment.getObjectTypeName (this.obj)),
                                     new ActionListener ()
                                     {
                                      
                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {
                                          
                                            _thisep.doEdit ();
                                          
                                        }
                                      
                                     });

    }            
    
    @Override
    public Set<FormItem> getEditItems ()
    {

        return null;

    }

    @Override
    public Set<FormItem> getViewItems ()
    {

        return null;

    }

    @Override
    public boolean handleSave ()
    {

        // Get all the link items from the tree.
        DefaultTreeModel dtm = (DefaultTreeModel) this.linkedToEditTree.getModel ();

        Set s = new HashSet ();

        try
        {

            UIUtils.getSelectedObjects ((DefaultMutableTreeNode) dtm.getRoot (),
                                        this.obj,
                                        s);

        } catch (Exception e)
        {

            Environment.logError ("Unable to get objects to link to for: " +
                                  this.obj,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.linkedto,
                                                               LanguageStrings.save,
                                                               LanguageStrings.actionerror));
                                      //"An internal error has occurred.\n\nUnable to add/edit object.");

            return false;

        }

        // Save the links
        try
        {

            this.viewer.saveLinks (this.obj,
                                   s);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save links: " +
                                  this.obj,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.linkedto,
                                                               LanguageStrings.save,
                                                               LanguageStrings.actionerror));                                              
                                      //"An internal error has occurred.\n\nUnable to save links.");

            return false;

        }

        this.refreshLinkedToTree ();

        java.util.Set<NamedObject> otherObjects = this.obj.getOtherObjectsInLinks ();

        this.viewer.refreshObjectPanels (otherObjects);

        return true;

    }

    public void refreshLinkedToTree ()
    {

        List exclude = new ArrayList ();
        exclude.add (this.obj);

        // Painful but just about the only way.
        this.viewer.setLinks (this.obj);

        // Get all the "other objects" for the links the note has.
        Iterator<Link> it = this.obj.getLinks ().iterator ();

        Set links = new HashSet ();

        while (it.hasNext ())
        {

            links.add (it.next ().getOtherObject (this.obj));

        }

        DefaultTreeModel m = new DefaultTreeModel (UIUtils.createLinkToTree (this.viewer.getProject (),
                                                                             exclude,
                                                                             links,
                                                                             false));

        this.linkedToViewTree.setModel (m);

        UIUtils.expandAllNodesWithChildren (this.linkedToViewTree);

    }
    
    @Override
    public boolean handleCancel ()
    {

        return true;

    }

    @Override
    public void handleEditStart ()
    {

        this.linkedToEditTree.setModel (UIUtils.getLinkedToTreeModel (this.viewer,
                                                                      this.obj,
                                                                      true));

        UIUtils.expandPathsForLinkedOtherObjects (this.linkedToEditTree,
                                                  this.obj);

    }

    @Override
    public IconProvider getIconProvider ()
    {

        DefaultIconProvider iconProv = new DefaultIconProvider ()
        {

            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {

                if (name.equals ("header"))
                {

                    name = Link.OBJECT_TYPE;

                }

                return super.getIcon (name,
                                      type);

            }

        };

        return iconProv;

    }

    @Override
    public String getEditTitle ()
    {

        return Environment.getUIString (LanguageStrings.linkedto,
                                        LanguageStrings.edit,
                                        LanguageStrings.title);

    }
    
    @Override
    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.linkedto,
                                        LanguageStrings.view,
                                        LanguageStrings.title);
        //"Linked to";

    }

    @Override
    public JComponent getEditPanel ()
    {

        this.linkedToEditTree = UIUtils.createLinkedToTree (this.viewer,
                                                            this.obj,
                                                            true);

        return this.linkedToEditTree;

    }

    @Override
    public JComponent getViewPanel ()
    {

        this.linkedToViewTree = UIUtils.createLinkedToTree (this.viewer,
                                                            this.obj,
                                                            false);

        UIUtils.expandAllNodesWithChildren (this.linkedToViewTree);

        return this.linkedToViewTree;

    }

}