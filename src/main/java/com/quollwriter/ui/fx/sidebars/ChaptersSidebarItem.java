package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.beans.property.*;

import org.reactfx.*;

import com.quollwriter.data.Scene;
import com.quollwriter.data.OutlineItem;
import com.quollwriter.data.Project;
import com.quollwriter.data.Chapter;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.Note;
import com.quollwriter.data.ChapterItem;
import com.quollwriter.data.CollectionEvent;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChaptersSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;
    private Map<Chapter, List<Subscription>> eventSourceSubscriptions = new HashMap<> ();

    public ChaptersSidebarItem (ProjectViewer   pv,
                                IPropertyBinder binder)
    {

        super (pv,
               binder);

        pv.getProject ().getBooks ().get (0).getChapters ().stream ()
            .forEach (c -> this.addListenersForChapter (c));

        this.addListChangeListener (pv.getProject ().getBooks ().get (0).getChapters (),
                                    ev ->
        {

            while (ev.next ())
            {

                if (ev.wasAdded ())
                {

                    ev.getAddedSubList ().stream ()
                        .forEach (c ->
                        {

                            // TODO
                            this.addListenersForChapter (c);

                        });

                    this.tree.getRoot ().getChildren ().addAll (ev.getFrom (),
                                                                ev.getAddedSubList ().stream ()
                                                                    .map (c -> this.createTreeItem (c))
                                                                    .collect (Collectors.toList ()));

                }

                if (ev.wasRemoved ())
                {

                    ev.getRemoved ().stream ()
                        .forEach (c ->
                        {

                            this.removeListenersForChapter (c);

                        });

                    this.tree.getRoot ().getChildren ().removeAll (this.tree.getRoot ().getChildren ().subList (ev.getFrom (),
                                                                                                                ev.getTo () + 1));

                }

                this.countProp.setValue (pv.getProject ().getBooks ().get (0).getChapters ().size ());

            }

        });

        this.countProp = new SimpleIntegerProperty (0);

        this.addChangeListener (this.viewer.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            if (newv != null)
            {

                if (newv.getContent () instanceof NamedObjectPanelContent)
                {

                    NamedObjectPanelContent nc = (NamedObjectPanelContent) newv.getContent ();

                    if (nc.getObject () instanceof Chapter)
                    {

                        this.tree.select ((Chapter) nc.getObject ());

                        return;

                    }

                }

            }

            this.tree.clearSelection ();

        });

        this.tree = new QuollTreeView<> ();
        this.tree.setShowRoot (false);
        this.tree.setCellProvider (treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            if (n instanceof Project)
            {

                return new Label ();

            }

            QuollLabel l = QuollLabel.builder ()
                .label (n.nameProperty ())
                .styleClassName (n.getObjectType ())
                .build ();

            l.setOnMouseClicked (ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                this.viewer.viewObject (n);

            });

            List<String> prefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,items);

            if (n instanceof Chapter)
            {

                Chapter c = (Chapter) n;

                l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, (!c.isEditComplete () && c.getEditPosition () > 0));
                l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, c.isEditComplete ());

                this.addChangeListener (UserProperties.showEditPositionIconInChapterListProperty (),
                                        (pr, oldv, newv) ->
                {

                    l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, false);

                    if ((newv)
                        &&
                        (c.getEditPosition () > -1)
                       )
                    {

                        l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, true);

                    }

                });

                this.addChangeListener (c.editPositionProperty (),
                                        (pr, oldv, newv) ->
                {

                    if (UserProperties.getAsBoolean (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME))
                    {

                        if (!c.isEditComplete ())
                        {

                            l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, newv.intValue () > 0);

                        }

                    } else {

                        l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, false);

                    }

                });

                this.addChangeListener (UserProperties.showEditCompleteIconInChapterListProperty (),
                                        (pr, oldv, newv) ->
                {

                    l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, false);

                    if ((newv)
                        &&
                        (c.isEditComplete ())
                       )
                    {

                        l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, true);

                    }

                });

                this.addChangeListener (c.editCompleteProperty (),
                                        (pr, oldv, newv) ->
                {

                    if (UserProperties.getAsBoolean (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME))
                    {

                        l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, newv);

                    } else {

                        l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, false);

                    }

                });

            }

            l.setOnContextMenuRequested (eev ->
            {

                ContextMenu m = new ContextMenu ();

                if (n instanceof Note)
                {

                    java.util.List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,notes,items);

                    final Note note = (Note) n;

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.viewObject (n);

                        })
                        .build ());

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                        .styleClassName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.editObject (n);

                        })
                        .build ());

                    Menu tm = UIUtils.createTagsMenu (n,
                                                      this.viewer);

                    if (tm != null)
                    {

                        m.getItems ().add (tm);

                    }

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.editObject (n);

                        })
                        .build ());

                }

                if (n instanceof Chapter)
                {

                    final Chapter c = (Chapter) n;

                    final String chapterObjTypeName = Environment.getObjectTypeName (c).getValue ();

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                        .styleClassName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    c);

                        })
                        .build ());

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                        .styleClassName (StyleClassNames.INFO)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.showchapterinfo,
                                                    c);

                        })
                        .build ());

                    if (!c.isEditComplete ())
                    {

                        m.getItems ().add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,seteditcomplete)))
                            .styleClassName (StyleClassNames.EDITCOMPLETE)
                            .onAction (ev ->
                            {

                                this.viewer.setChapterEditComplete (c,
                                                                    true);

                            })
                            .build ());

                    } else {

                        m.getItems ().add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,seteditneeded)))
                            .styleClassName (StyleClassNames.EDITNEEDED)
                            .onAction (ev ->
                            {

                                this.viewer.setChapterEditComplete (c,
                                                                    false);

                            })
                            .build ());

                    }

                    if ((c.getEditPosition () > 0)
                        &&
                        (!c.isEditComplete ())
                       )
                    {

                        m.getItems ().add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,removeeditposition)))
                            .styleClassName (StyleClassNames.REMOVEEDITPOSITION)
                            .onAction (ev ->
                            {

                                this.viewer.removeChapterEditPosition (c);

                            })
                            .build ());

                    }

                    Menu tm = UIUtils.createTagsMenu (n,
                                                      this.viewer);

                    if (tm != null)
                    {

                        m.getItems ().add (tm);

                    }

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,newbelow)))
                        .styleClassName (StyleClassNames.ADD)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.newchapter,
                                                    c);

                        })
                        .build ());

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,rename)))
                        .styleClassName (StyleClassNames.RENAME)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.renamechapter,
                                                    c);

                        })
                        .build ());

                    if (this.viewer.isEditing (c))
                    {

                        m.getItems ().add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,close)))
                            .styleClassName (StyleClassNames.CLOSE)
                            .onAction (ev ->
                            {

                                this.viewer.runCommand (ProjectViewer.CommandId.closepanel,
                                                        c);

                            })
                            .build ());

                    }

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.deletechapter,
                                                    c);

                        })
                        .build ());

                }

                if (n instanceof OutlineItem)
                {

                    List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,outlineitems,items);

                    final OutlineItem oi = (OutlineItem) n;

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    oi);

                        })
                        .build ());

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,edit)))
                        .styleClassName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.editoutlineitem,
                                                    oi);

                        })
                        .build ());

                    Menu tm = UIUtils.createTagsMenu (n,
                                                      this.viewer);

                    if (tm != null)
                    {

                        m.getItems ().add (tm);

                    }

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.deleteoutlineitem,
                                                    oi);

                        })
                        .build ());

                }

                if (n instanceof Scene)
                {

                    List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,scenes,items);

                    final Scene s = (Scene) n;

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    s);

                        })
                        .build ());

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,edit)))
                        .styleClassName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.editobject,
                                                    s);

                        })
                        .build ());

                    Menu tm = UIUtils.createTagsMenu (n,
                                                      this.viewer);

                    if (tm != null)
                    {

                        m.getItems ().add (tm);

                    }

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.deletescene,
                                                    s);

                        })
                        .build ());

                }

                m.setAutoHide (true);

                if (l.getContextMenu () != null)
                {

                    l.getContextMenu ().hide ();

                }

                m.show (this.tree, eev.getScreenX (), eev.getScreenY ());

                l.setContextMenu (m);

            });

            return l;

        });

    }

    private void removeListenersForChapter (Chapter c)
    {

        this.eventSourceSubscriptions.get (c).stream ()
            .forEach (s -> s.unsubscribe ());

        this.eventSourceSubscriptions.remove (c);

    }

    private void addListenersForChapter (Chapter c)
    {

        List<Subscription> subs = new ArrayList<> ();

        subs.add (c.chapterItemsEvents ().subscribe (ev ->
        {

            if (ev.getType () == CollectionEvent.Type.remove)
            {

                // Remove the item.

            }

            if (ev.getType () == CollectionEvent.Type.add)
            {

                // Add the item.

            }

        }));

       subs.add (c.chapterItemsPositionEvents ().subscribe (ev ->
       {

           // TODO Check for position change.

       }));

       this.eventSourceSubscriptions.put (c,
                                          subs);
    }

    private void selectChapter (Chapter c)
    {

        this.tree.select (c);


    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.CHAPTER;

    }

    @Override
    public StringProperty getTitle ()
    {

        return Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE);

    }

    @Override
    public String getId ()
    {

        return Chapter.OBJECT_TYPE;

    }

    @Override
    public void init (State ss)
    {

        super.init (ss);

        this.reloadTree ();

    }

    @Override
    public BooleanProperty showItemCountOnHeader ()
    {

        // TODO Make a style?
        return new SimpleBooleanProperty (true);

    }

    @Override
    public State getState ()
    {

        State ss = super.getState ();

        if (ss == null)
        {

            ss = new State ();

        }

        return ss;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        List<String> prefix = Arrays.asList (project,sidebar,chapters,headerpopupmenu,items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings._new)))
                .styleClassName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.newchapter);

                })
                .build ());

            return items;

        };

    }

    @Override
    public void reloadTree ()
    {

        // Set the root of the tree.
        this.tree.setRoot (this.createTree ());

    }

    private TreeItem<NamedObject> createTreeItem (Chapter c)
    {

        TreeItem<NamedObject> ci = new TreeItem<> (c);

        List<ChapterItem> items = new ArrayList<> (c.getScenes ());

        items.addAll (c.getOutlineItems ());

        Collections.sort (items,
                          new ChapterItemSorter ());

        for (ChapterItem citem : items)
        {

            TreeItem<NamedObject> cii = new TreeItem<> (citem);

            ci.getChildren ().add (cii);

            if (citem instanceof Scene)
            {

                Scene s = (Scene) citem;

                for (OutlineItem oitem : s.getOutlineItems ())
                {

                    TreeItem<NamedObject> oii = new TreeItem<> (oitem);

                    cii.getChildren ().add (oii);

                }

            }

        }

        return ci;

    }

    private TreeItem<NamedObject> createTree ()
    {

        List<Chapter> objs = new ArrayList<> (this.viewer.getProject ().getBooks ().get (0).getChapters ());

        TreeItem<NamedObject> root = new TreeItem<> (this.viewer.getProject ());

        for (Chapter c : objs)
        {

            root.getChildren ().add (this.createTreeItem (c));

        }

        this.countProp.setValue (objs.size ());

        return root;

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.countProp;

    }

    @Override
    public boolean isDragEnabled ()
    {

        return true;

    }

/*
    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv)
    {

        final AssetAccordionItem _this = this;

        return new DragActionHandler<Asset> ()
        {

            @Override
            public boolean canImportForeignObject (NamedObject obj)
            {

                // Allow support of addition of same object type.
                return false;

            }

            @Override
            public boolean importForeignObject (NamedObject obj,
                                                int         insertRow)
            {

                return false;

            }

            @Override
            public boolean handleMove (int   fromRow,
                                       int   toRow,
                                       Asset object)
                                throws GeneralException
            {

                _this.sortField = null;

                QuollPanel p = _this.viewer.getCurrentlyVisibleTab ();

                if (p != null)
                {

                    if (p instanceof ProjectObjectQuollPanel)
                    {

                        ProjectObjectQuollPanel pp = (ProjectObjectQuollPanel) p;

                        if (pp.getForObject ().equals (object))
                        {

                            return true;

                        }

                    }

                }

                _this.getTree ().removeSelectionPath (UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) _this.getTree ().getModel ().getRoot (),
                                                      object));

                return true;

            }

            @Override
            public boolean performAction (int         removeRow,
                                          NamedObject removeObject,
                                          int         insertRow,
                                          NamedObject insertObject)
                                   throws GeneralException
            {

                return true;

            }

        };

    }
*/

}
