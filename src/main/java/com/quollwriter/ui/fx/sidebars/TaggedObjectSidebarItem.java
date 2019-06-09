package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.scene.*;
import javafx.embed.swing.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TaggedObjectSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    public static final String ID_PREFIX = "tag:";

    private Comparator<NamedObject> sorter = null;
    private Tag tag = null;
    private Set<NamedObject> items = null;
    private IntegerProperty count = new SimpleIntegerProperty (0);
    private QuollTreeView<NamedObject> tree = null;

    public TaggedObjectSidebarItem (Tag           tag,
                                    ProjectViewer pv)
    {

        super (pv);

        TaggedObjectSidebarItem _this = this;

        this.tag = tag;

        this.sorter = NamedObjectSorter.getInstance ();

        ObservableSet<NamedObject> its = this.viewer.getProject ().getAllObjectsWithTag (this.tag);

        this.items = its;

        its.addListener ((SetChangeListener<NamedObject>) ev ->
        {

            // We are just adding a change listener here to help us trigger a reload.
            // TODO Add for specific add/removes then update tree as necessary.
            this.reloadTree ();

        });

        this.tree = new QuollTreeView<> ();
        this.tree.setShowRoot (false);
        this.tree.setCellProvider (treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            QuollLabel l = null;

            if (n instanceof UserConfigurableObjectType)
            {

                UserConfigurableObjectType uc = (UserConfigurableObjectType) n;

                l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .build ();

                l.graphicProperty ().bind (uc.icon16x16Property ());

            } else {

                l = QuollLabel.builder ()
                .label (n.nameProperty ())
                .styleClassName (n.getObjectType ())
                .build ();

            }

            List<String> prefix = Arrays.asList (project,sidebar,tags,treepopupmenu,LanguageStrings.items);

            ContextMenu m = new ContextMenu ();

            m.getItems ().add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                .styleClassName (StyleClassNames.VIEW)
                .onAction (ev ->
                {

                    this.viewer.viewObject (n);

                })
                .build ());

                Menu tm = UIUtils.createTagsMenu (n,
                                                  this.viewer);

                if (tm != null)
                {

                    m.getItems ().add (tm);

                }

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,remove)))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        try
                        {

                            n.removeTag (_this.tag);

                            _this.viewer.saveObject (n,
                                                     true);

                            _this.reloadTree ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to remove tag: " +
                                                  _this.tag,
                                                  e);

                            ComponentUtils.showErrorMessage (_this.viewer,
                                                             getUILanguageStringProperty (project,actions,deletetag,actionerror));
                                                      //"Unable to remove tag.");

                        }

                    })
                    .build ());

            l.setContextMenu (m);

            return l;

        });

    }

    @Override
    public StringProperty getTitle ()
    {

        return this.tag.nameProperty ();

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.count;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.TAG;

    }
/*
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
*/
    @Override
    public String getId ()
    {

        return ID_PREFIX + this.tag.getKey ();

    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public void init (State ss)
    {

        super.init (ss);

        if (ss != null)
        {

            Set<String> objRefs = ss.getAsSet ("order",
                                               String.class);

            if (objRefs != null)
            {

                final Map<String, Number> order = new HashMap<> ();

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

        }

        this.reloadTree ();

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        if (s == null)
        {

            s = new State ();

        }

        s.set ("order",
               this.tree.getRoot ().getChildren ().stream ()
                    .map (c -> c.getValue ().getObjectReference ().asString ())
                    .collect (Collectors.toList ()));

        return s;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        TaggedObjectSidebarItem _this = this;

        List<String> prefix = Arrays.asList (project,sidebar,tags,headerpopupmenu,LanguageStrings.items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,rename)))
                .styleClassName (StyleClassNames.EDIT)
                .onAction (ev ->
                {
/*
TODO
                    new RenameTagActionHandler (_this.tag,
                                                _this.viewer);
*/
                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                .styleClassName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    List<String> prefix2 = Arrays.asList (project,actions,deletetag);

                    Set<Button> buttons = new LinkedHashSet<> ();

                    buttons.add (QuollButton.builder ()
                                    .label (getUILanguageStringProperty (Utils.newList (prefix2,allprojects)))
                                    .onAction (eev ->
                                    {

                                        try
                                        {

                                            Environment.deleteTag (_this.tag);

                                        } catch (Exception e) {

                                            Environment.logError ("Unable to delete tag: " +
                                                                  _this.tag,
                                                                  e);

                                            ComponentUtils.showErrorMessage (_this.viewer,
                                                                             getUILanguageStringProperty (Utils.newList (prefix2,actionerror)));
                                                                      //"Unable to delete tag.");

                                        }

                                    })
                                    .build ());

                    buttons.add (QuollButton.builder ()
                                    .label (getUILanguageStringProperty (Utils.newList (prefix2,thisproject)))
                                    .onAction (eev ->
                                    {

                                        try
                                        {

                                            _this.viewer.removeTag (_this.tag);

                                        } catch (Exception e) {

                                            Environment.logError ("Unable to remove tag: " +
                                                                  _this.tag,
                                                                  e);

                                            ComponentUtils.showErrorMessage (_this.viewer,
                                                                             getUILanguageStringProperty (prefix2,actionerror));
                                                                      //"Unable to delete tag.");

                                        }

                                    })
                                    .build ());

                    ComponentUtils.createQuestionPopup (getUILanguageStringProperty (prefix2,title),
                                                        StyleClassNames.DELETE,
                                                        getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                                                     _this.tag.nameProperty ()),
                                                        buttons,
                                                        null,
                                                        null,
                                                        null,
                                                        _this.viewer);

                })
                .build ());

            return items;

        };

    }

    @Override
    public void reloadTree ()
    {

        Set<NamedObject> its = this.items;

        if (this.sorter != null)
        {

            List<NamedObject> sobjs = new ArrayList<> (its);

            Collections.sort (sobjs,
                              this.sorter);

            its = new LinkedHashSet<> (sobjs);

        }

        this.count.setValue (its.size ());

        // Set the root of the tree.
        TreeItem<NamedObject> root = new TreeItem<> (this.viewer.getProject ());

        for (NamedObject item : its)
        {

            root.getChildren ().add (new TreeItem<NamedObject> (item));

        }

        this.tree.setRoot (root);

    }

    public BooleanProperty showItemCountOnHeader ()
    {

        return new SimpleBooleanProperty (true);

    }
/*
    public int getItemCount ()
    {

        int c = this.viewer.getProject ().getAllObjectsWithTag (this.tag).size ();

        return c;

    }
*/
/*
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
*/
/*
    @Override
    public TreeCellEditor getTreeCellEditor (ProjectViewer pv)
    {

        return new ProjectTreeCellEditor (pv,
                                          tree);

    }
*/
/*
    public int getViewObjectClickCount (Object d)
    {

        return 1;

    }
*/

    @Override
    public boolean isDragEnabled ()
    {

        return true;

    }

}
