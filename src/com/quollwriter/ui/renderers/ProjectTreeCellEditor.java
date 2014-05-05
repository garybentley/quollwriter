package com.quollwriter.ui.renderers;

import java.awt.Component;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public class ProjectTreeCellEditor extends DefaultTreeCellEditor implements CellEditorListener
{

    private AbstractProjectViewer projectViewer = null;
    private NamedObject   editing = null;
    private Chapter       lastChapter = null;

    public ProjectTreeCellEditor (AbstractProjectViewer pv,
                                  JTree                 tree)
    {

        super (tree,
               new ProjectTreeCellRenderer (false));

        this.projectViewer = pv;

        this.addCellEditorListener (this);

    }

    public void setLastChapter (Chapter c)
    {

        this.lastChapter = c;

    }
    
    public void editingCanceled (ChangeEvent ev)
    {

        if (this.lastPath == null)
        {
            
            return;
            
        }
    
        ((DefaultMutableTreeNode) this.lastPath.getLastPathComponent ()).setUserObject (this.editing);

        this.editing = null;

    }

    public void editingStopped (ChangeEvent ev)
    {

        String n = this.getCellEditorValue ().toString ().trim ();

        ((DefaultMutableTreeNode) this.lastPath.getLastPathComponent ()).setUserObject (this.editing);

        // See if there is another chapter with that name.
        if (n.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            this.tree.startEditingAtPath (this.lastPath);

            return;

        }

        String oldName = this.editing.getName ();

        boolean result = false;

        if (this.editing instanceof Chapter)
        {

            result = this.handleChapter ((Chapter) this.editing,
                                         n);

        } else if (this.editing instanceof Asset)
        {

            result = this.handleAsset ((Asset) this.editing,
                                       n);

        } else if (!(this.editing instanceof TreeParentNode))
        {

            try
            {

                this.editing.setName (n);

                this.projectViewer.saveObject (this.editing,
                                               true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to change name of: " +
                                      this.editing +
                                      " to: " +
                                      n,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to change name.");

                result = false;

            }

        } else if (this.editing instanceof TreeParentNode)
        {

            result = this.handleTreeParentNode ((TreeParentNode) this.editing,
                                                n);

        } else
        {

            Environment.logError ("Type: " +
                                  this.editing +
                                  " not supported yet.");

            result = false;

        }

        if (!result)
        {

            this.editing.setName (oldName);

            this.tree.startEditingAtPath (this.lastPath);

            return;

        }

        this.editing = null;

    }

    private boolean handleAsset (Asset  a,
                                 String newName)
    {

        Asset other = this.projectViewer.getProject ().getAssetByName (newName.toLowerCase (),
                                                                       a.getObjectType ());

        if (other != null)
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Already have a " + Environment.getObjectTypeName (a).toLowerCase () + " called: " +
                                      other.getName ());

            return false;

        }

        Set<String> oldNames = a.getAllNames ();        
        
        a.setName (newName);
        //a.setProject (this.projectViewer.getProject ());

        try
        {

            // Add the asset to the project.
            //this.projectViewer.getProject ().addAsset (a);

            this.projectViewer.saveObject (a,
                                           true);

            //((DefaultTreeModel) this.tree.getModel ()).removeNodeFromParent ((DefaultMutableTreeNode) this.lastPath.getLastPathComponent ());

            this.projectViewer.reloadTreeForObjectType (a.getObjectType ());                

        } catch (Exception e)
        {

            Environment.logError ("Unable to add new " + Environment.getObjectTypeName (a).toLowerCase () + " with name: " +
                                  newName,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (a) + ".");

            return false;

        }

        this.projectViewer.updateProjectDictionaryForNames (oldNames,
                                                            a);
                
        return true;
        
/*        
        //this.projectViewer.viewObject (a);

        } else
        {

            try
            {

                a.setName (newName);

                this.projectViewer.saveObject (a,
                                               true);

                // Tell the model that the node has changed name.

            } catch (Exception e)
            {

                Environment.logError ("Unable to change name of: " +
                                      a +
                                      " to: " +
                                      newName,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to change " + Environment.getObjectTypeName (a).toLowerCase () + " name.");

                return false;

            }

        }

        this.projectViewer.addWordToDictionary (a.getName (),
                                                "project");
        this.projectViewer.addWordToDictionary (a.getName () + "'s",
                                                "project");

        return true;
*/
    }

    private boolean handleTreeParentNode (TreeParentNode n,
                                          String         newName)
    {

        if (n.getForObjectType ().equals (Note.OBJECT_TYPE))
        {

            if (this.projectViewer.getObjectTypesHandler (Note.OBJECT_TYPE).renameType (n.getName (),
                                                                                        newName,
                                                                                        true))
            {

                n.setName (newName);

            } else
            {

                return false;

            }

        }

        return true;

    }

    private boolean handleChapter (Chapter c,
                                   String  newName)
    {

        if (c.getKey () == null)
        {

            // Adding a new chapter.
            Chapter nc = null;

            try
            {

                c.setName (newName);

                nc = this.lastChapter.getBook ().createChapterAfter (this.lastChapter,
                                                                     c);

                this.projectViewer.saveObject (this.lastChapter.getBook (),
                                               true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to add new chapter with name: " +
                                      newName,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to add new {chapter}.");

                return false;

            }

            try
            {

                this.projectViewer.viewObject (nc);

            } catch (Exception e)
            {

                Environment.logError ("Unable to edit chapter: " +
                                      nc,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to edit {chapter}.");

            }

            this.lastChapter = null;

            return true;

        }

        Chapter other = this.projectViewer.getProject ().getBook (0).getChapterByName (newName.toLowerCase ());

        if (other != null)
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Already have a " + Environment.getObjectTypeName (other).toLowerCase () + " called: " +
                                      other.getName ());

            return false;

        }        
        
        try
        {

            c.setName (newName);

            // Inform the chapter tree that something has changed.
            this.projectViewer.reloadTreeForObjectType (c.getObjectType ());                

            this.projectViewer.saveObject (c,
                                           true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to change name of: " +
                                  c +
                                  " to: " +
                                  newName,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to change {chapter} name.");

            return false;

        }

        return true;

    }

    protected void prepareForEditing ()
    {

        if (this.editingComponent instanceof JTextField)
        {

            JTextField j = (JTextField) this.editingComponent;

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.lastPath.getLastPathComponent ();

            NamedObject n = (NamedObject) node.getUserObject ();

            this.editing = n;

            j.setText (n.getName ());

        }

        super.prepareForEditing ();

    }

    protected void startEditingTimer ()
    {

        // Don't do anything.

    }

    protected boolean shouldStartEditingTimer (EventObject ev)
    {

        return false;

    }

    protected boolean canEditImmediately (EventObject ev)
    {

        if (ev == null)
        {

            return true;

        }

        // No you can't.
        return false;

    }

}
;
