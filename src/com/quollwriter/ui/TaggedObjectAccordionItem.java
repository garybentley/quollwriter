package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class TaggedObjectAccordionItem extends ProjectObjectsAccordionItem<ProjectViewer> implements ProjectEventListener
{

    public static final String ID_PREFIX = "tag:";

    private Comparator<NamedObject> sorter = null;
    private Tag tag = null;

    public TaggedObjectAccordionItem (Tag           tag,
                                      ProjectViewer pv)
    {

        super (tag.getName (),
               Environment.getIcon (Constants.TAG_ICON_NAME,
                                    Constants.ICON_SIDEBAR),
               pv);

        this.tag = tag;

        Environment.addUserProjectEventListener (this);

        this.sorter = NamedObjectSorter.getInstance ();

    }

    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        // TODO: Change to use a name change instead.
        if ((this.tag.equals (ev.getContextObject ()))
            &&
            (ev.getType ().equals (ProjectEvent.TAG))
            &&
            (ev.getAction ().equals (ProjectEvent.CHANGED))
           )
        {

            this.updateTitle ();

        }

    }

    @Override
    public String getTitle ()
    {

        return this.tag.getName ();

    }

    @Override
    public String getId ()
    {

        return ID_PREFIX + this.tag.getKey ();

    }

    @Override
    public void initFromSaveState (String ss)
    {

        super.initFromSaveState (ss);

        Map<String, Object> state = (Map<String, Object>) JSONDecoder.decode (ss);

        Collection<String> objRefs = (Collection<String>) state.get ("order");

        if (objRefs != null)
        {

            final Map<String, Number> order = new HashMap ();

            int c = 0;

            for (String ref : objRefs)
            {

                order.put (ref,
                           c++);

            }

            this.sorter = new Comparator<NamedObject> ()
            {

                @Override
                public boolean equals (Object o)
                {

                    return this.equals (o);

                }

                @Override
                public int compare (NamedObject o1,
                                    NamedObject o2)
                {

                    Number nv1 = order.get (o1.getObjectReference ().asString ());

                    Number nv2 = order.get (o2.getObjectReference ().asString ());

                    if ((nv1 == null)
                        ||
                        (nv2 == null)
                       )
                    {

                        return NamedObjectSorter.getInstance ().compare (o1, o2);

                    }

                    return nv1.intValue () - nv2.intValue ();

                }

            };

        }

        this.reloadTree ();

    }

    @Override
    public Map<String, Object> getSaveStateAsMap ()
    {

        Map<String, Object> ss = super.getSaveStateAsMap ();

        DefaultMutableTreeNode r = (DefaultMutableTreeNode) ((DefaultTreeModel) this.tree.getModel ()).getRoot ();

        Set<String> objRefs = new LinkedHashSet ();

        for (int i = 0; i < r.getChildCount (); i++)
        {

            DefaultMutableTreeNode n = (DefaultMutableTreeNode) r.getChildAt (i);

            NamedObject oo = (NamedObject) n.getUserObject ();

            objRefs.add (oo.getObjectReference ().asString ());

        }

        if (objRefs.size () > 0)
        {

            ss.put ("order",
                    objRefs);

        }

        return ss;

    }

    @Override
    public void initTree ()
    {

        this.reloadTree ();

        this.tree.setCellRenderer (new ProjectTreeCellRenderer (true));


    }

    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        final TaggedObjectAccordionItem _this = this;

        java.util.List<String> prefix = Arrays.asList (project,sidebar,tags,headerpopupmenu,items);

        m.add (UIUtils.createMenuItem (getUIString (prefix,rename),
                                       //"Rename",
                                       Constants.EDIT_ICON_NAME,
                                       new ActionListener ()
                                       {

                                           @Override
                                           public void actionPerformed (ActionEvent ev)
                                           {

                                                new RenameTagActionHandler (_this.tag,
                                                                            _this.viewer).actionPerformed (ev);

                                           }

                                       }));

        m.add (UIUtils.createMenuItem (getUIString (prefix,delete),
                                       //"Delete this Tag",
                                       Constants.DELETE_ICON_NAME,
                                       new ActionListener ()
                                       {

                                           @Override
                                           public void actionPerformed (ActionEvent ev)
                                           {

                                                java.util.List<String> prefix = Arrays.asList (project,actions,deletetag);

                                                Map<String, ActionListener> buttons = new LinkedHashMap ();

                                                buttons.put (getUIString (prefix,allprojects),
                                                             //"Delete from all {projects}",
                                                             new ActionListener ()
                                                             {

                                                                @Override
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    try
                                                                    {

                                                                        Environment.deleteTag (_this.tag);

                                                                    } catch (Exception e) {

                                                                        Environment.logError ("Unable to delete tag: " +
                                                                                              _this.tag,
                                                                                              e);

                                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                                  getUIString (prefix,actionerror));
                                                                                                  //"Unable to delete tag.");

                                                                    }

                                                                }

                                                             });

                                                buttons.put (getUIString (prefix,thisproject),
                                                //"Just this {project}",
                                                             new ActionListener ()
                                                             {

                                                                @Override
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    try
                                                                    {

                                                                        _this.viewer.removeTag (_this.tag);

                                                                    } catch (Exception e) {

                                                                        Environment.logError ("Unable to remove tag: " +
                                                                                              _this.tag,
                                                                                              e);

                                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                                  getUIString (prefix,actionerror));
                                                                                                  //"Unable to delete tag.");

                                                                    }

                                                                }

                                                             });

                                                buttons.put (getUIString (LanguageStrings.buttons,cancel),
                                                             new ActionListener ()
                                                             {

                                                                @Override
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                }

                                                             });

                                                UIUtils.createQuestionPopup (_this.viewer,
                                                                             getUIString (prefix,title),
                                                //"Remove tag",
                                                                             Constants.DELETE_ICON_NAME,
                                                                             String.format (getUIString (prefix,text),
                                                                                            //"Do you wish to remove tag <b>%s</b> from <b>ALL</b> {projects} or just this one?",
                                                                                            _this.tag.getName ()),
                                                                             buttons,
                                                                             null,
                                                                             null);

                                           }

                                        }));

    }

    @Override
    public void reloadTree ()
    {

        Set<NamedObject> taggedObjects = this.viewer.getProject ().getAllObjectsWithTag (this.tag);

        if (this.sorter != null)
        {

            List<NamedObject> sobjs = new ArrayList ();

            if (taggedObjects != null)
            {

                sobjs.addAll (taggedObjects);

            }

            Collections.sort (sobjs,
                              this.sorter);

            taggedObjects = new LinkedHashSet (sobjs);

        }

        DefaultMutableTreeNode pn = UIUtils.createTreeNode (this.viewer.getProject (),
                                                            null,
                                                            null,
                                                            false);

        UIUtils.createTree (taggedObjects,
                            pn);

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (pn);

    }

    public boolean showItemCountOnHeader ()
    {

        return true;

    }

    public int getItemCount ()
    {

        int c = this.viewer.getProject ().getAllObjectsWithTag (this.tag).size ();

        return c;

    }

    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv)
    {

        final TaggedObjectAccordionItem _this = this;

        return new DragActionHandler<NamedObject> ()
        {

            @Override
            public boolean canImportForeignObject (NamedObject obj)
            {

                return !obj.hasTag (_this.tag);

            }

            @Override
            public boolean importForeignObject (NamedObject obj,
                                                int         insertRow)
                                         throws GeneralException
            {

                int c = _this.getItemCount ();

                obj.addTag (_this.tag);

                _this.viewer.saveObject (obj,
                                         true);

                if (c == 0)
                {

                    _this.update ();

                } else {

                    // Add at the appropriate row.
                    DefaultTreeModel model = ((DefaultTreeModel) _this.tree.getModel ());

                    TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                    obj);

                    DefaultMutableTreeNode n = new DefaultMutableTreeNode (obj);

                    model.insertNodeInto (n,
                                          (DefaultMutableTreeNode) model.getRoot (),
                                          insertRow);

                    _this.tree.getSelectionModel ().clearSelection ();

                    _this.updateItemCount (_this.getItemCount ());

                }

                _this.viewer.openObjectSection (TaggedObjectAccordionItem.ID_PREFIX + _this.tag);

                return true;

            }

            @Override
            public boolean handleMove (int         fromRow,
                                       int         toRow,
                                       NamedObject object)
            {

                return true;

            }

            @Override
            public boolean performAction (int         removeRow,
                                          NamedObject removeObject,
                                          int         insertRow,
                                          NamedObject insertObject)
            {

                return true;

            }

        };

    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final TaggedObjectAccordionItem _this = this;

        final TreePath tp = _this.tree.getPathForLocation (ev.getX (),
                                                           ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            java.util.List<String> prefix = Arrays.asList (project,sidebar,tags,treepopupmenu,items);

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final NamedObject d = (NamedObject) node.getUserObject ();

            m.add (UIUtils.createMenuItem (getUIString (prefix,view),
                                           //"View",
                                            Constants.VIEW_ICON_NAME,
                                            new ActionListener ()
                                            {

                                               @Override
                                               public void actionPerformed (ActionEvent ev)
                                               {

                                                   _this.viewer.viewObject (d);

                                               }

                                            }));
/*
            m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                    LanguageStrings.edit),
                                           //"Edit",
                                           Constants.EDIT_ICON_NAME,
                                           new ActionListener ()
                                           {

                                                @Override
                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.viewer.editObject (d);

                                                }

                                           }));
*/
            m.add (UIUtils.createTagsMenu (d,
                                           this.viewer));

            m.add (UIUtils.createMenuItem (getUIString (prefix,remove),
                                           //"Remove",
                                            Constants.DELETE_ICON_NAME,
                                            new ActionListener ()
                                            {

                                               @Override
                                               public void actionPerformed (ActionEvent ev)
                                               {

                                                    try
                                                    {

                                                        d.removeTag (_this.tag);

                                                        _this.viewer.saveObject (d,
                                                                                 true);

                                                        _this.update ();

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to remove tag: " +
                                                                              _this.tag,
                                                                              e);

                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  getUIString (project,actions,deletetag,actionerror));
                                                                                  //"Unable to remove tag.");

                                                    }


                                               }

                                            }));

        }

    }

    @Override
    public TreeCellEditor getTreeCellEditor (ProjectViewer pv)
    {

        return new ProjectTreeCellEditor (pv,
                                          tree);

    }

    public int getViewObjectClickCount (Object d)
    {

        return 1;

    }

    @Override
    public boolean isAllowObjectPreview ()
    {

        return true;

    }

    @Override
    public boolean isTreeEditable ()
    {

        return false;

    }

    @Override
    public boolean isDragEnabled ()
    {

        return true;

    }

}
