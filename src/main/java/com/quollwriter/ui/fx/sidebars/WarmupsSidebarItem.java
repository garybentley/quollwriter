package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.geometry.*;
import javafx.scene.layout.*;

import org.reactfx.*;

import com.quollwriter.data.Warmup;
import com.quollwriter.data.Project;
import com.quollwriter.data.Chapter;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.BlankNamedObject;
import com.quollwriter.data.Note;
import com.quollwriter.data.CollectionEvent;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.ObjectReference;
import com.quollwriter.data.Book;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class WarmupsSidebarItem extends ProjectObjectsSidebarItem<WarmupProjectViewer>
{

    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;
    private Map<Chapter, List<Subscription>> eventSourceSubscriptions = new HashMap<> ();
    private boolean ignoreChaptersEvents = false;

    public WarmupsSidebarItem (WarmupProjectViewer pv,
                               IPropertyBinder     binder)
    {

        super (pv,
               binder);

        pv.addEventHandler (AbstractViewer.KeyboardNavigationEvent.MOVE_UP_EVENT,
                            ev ->
        {

            NamedObject n = this.tree.selectedObjectProperty ().getValue ();

            if ((n == null)
                ||
                (!(n instanceof Chapter))
               )
            {

                return;

            }

            this.moveChapterUp ((Chapter) n);

            ev.consume ();

        });

        pv.addEventHandler (AbstractViewer.KeyboardNavigationEvent.MOVE_DOWN_EVENT,
                            ev ->
        {

            NamedObject n = this.tree.selectedObjectProperty ().getValue ();

            if ((n == null)
                ||
                (!(n instanceof Chapter))
               )
            {

                return;

            }

            this.moveChapterDown ((Chapter) n);

            ev.consume ();

        });

        this.addListChangeListener (pv.getProject ().getBooks ().get (0).getChapters (),
                                    ev ->
        {

            if (this.ignoreChaptersEvents)
            {

                return;

            }

            while (ev.next ())
            {

                if (ev.wasAdded ())
                {

                    this.tree.getRoot ().getChildren ().addAll (ev.getFrom (),
                                                                ev.getAddedSubList ().stream ()
                                                                    .map (c -> this.createTreeItem (c))
                                                                    .collect (Collectors.toList ()));

                }

                if (ev.wasRemoved ())
                {

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

            this.selectItem ();

        });

        this.tree = NamedObjectTree.builder ()
            .project (this.viewer.getProject ())
            .root (this.createTree ())
            .onDragDropped (n ->
            {

                try
                {

                    Chapter c = (Chapter) n;

                    // We switch off the listener here so that we can reuse the same treeitem that has it's state.
                    // Otherwise a fresh treeitem would be created and the state would be lost.
                    this.ignoreChaptersEvents = true;
                    Book b = c.getBook ();

                    TreeItem<NamedObject> it = this.tree.getTreeItemForObject (c);

                    b.moveChapter (c,
                                   it.getParent ().getChildren ().indexOf (it));
                    this.ignoreChaptersEvents = false;

                    try
                    {

                        this.viewer.updateChapterIndexes (b);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to update chapter indexes for book: " +
                                              b,
                                              e);

                    }

                    this.viewer.fireProjectEvent (ProjectEvent.Type.warmup,
                                                  ProjectEvent.Action.move);

                } finally {

                    this.ignoreChaptersEvents = false;

                }

            })
            .canExport (n -> true)
            .canImport ((nOver, nImport) ->
            {

                if ((nOver instanceof Chapter)
                    &&
                    (nImport instanceof Chapter)
                   )
                {

                    Chapter c = (Chapter) nOver;
                    // TODO Support adding chapters from other projects via drag-n-drop.
                    if (c.getBook ().equals (this.viewer.getProject ().getBooks ().get (0)))
                    {

                        return true;

                    }

                }

                return false;

            })
            .labelProvider (treeItem ->
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

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    l.requestFocus ();

                    this.viewer.viewObject (n);

                });

                List<String> prefix = Arrays.asList (warmups,sidebar,warmups,treepopupmenu,items);

                if (n instanceof Chapter)
                {

                    Chapter c = (Chapter) n;

                    //l.setFocusTraversable (true);
/*
TODO?
                    l.setOnMouseExited (ev ->
                    {

                        ScheduledFuture pshow = (ScheduledFuture) l.getProperties ().get ("previewpopupshow");

                        if (pshow != null)
                        {

                            pshow.cancel (true);

                        }

                        QuollPopup qp = (QuollPopup) l.getProperties ().get ("previewpopup");

                        l.getProperties ().put ("previewpopuphide",
                                                Environment.schedule (() ->
                        {

                            if (qp != null)
                            {

                                qp.close ();

                            }

                        },
                        500,
                        -1));

                    });

                    l.setOnMouseEntered (ev ->
                    {

                        if (UserProperties.getAsBoolean (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME,
                                                         null))
                        {

                            String prevformat = UserProperties.get (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
                                                                    Constants.DEFAULT_CHAPTER_INFO_PREVIEW_FORMAT);

                            StringProperty sp = new SimpleStringProperty ();
                            sp.setValue (UIUtils.getChapterInfoPreview (c,
                                                                        new StringWithMarkup (prevformat),
                                                                        pv));

                            UIUtils.setTooltip (l,
                                                sp);

                        } else {

                            UIUtils.setTooltip (l,
                                                null);

                        }

                    });
*/
                    Runnable setIcon = () ->
                    {

                        l.setIconClassName (StyleClassNames.CHAPTER);

                        if (c.getEditPosition () > 0)
                        {

                            l.setIconClassName (StyleClassNames.EDITPOSITION);

                        }

                        if (c.isEditComplete ())
                        {

                            l.setIconClassName (StyleClassNames.EDITCOMPLETE);

                        }

                    };

                    if (!c.isEditComplete () && c.getEditPosition () > 0)
                    {

                        l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, true);

                    }

                    if (c.isEditComplete ())
                    {

                        l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, true);

                    }

                    UIUtils.runLater (setIcon);

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

                        UIUtils.runLater (setIcon);

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

                        UIUtils.runLater (setIcon);

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

                        if (newv)
                        {

                            UIUtils.runLater (setIcon);

                        } else {

                            l.setIconClassName (StyleClassNames.CHAPTER);

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

                        UIUtils.runLater (setIcon);

                    });

                }

                return l;

            })
            .onClick ((n, ev) ->
            {

                this.viewer.viewObject (n);

            })
            .contextMenuItemSupplier (n ->
            {

                Set<MenuItem> its = new LinkedHashSet<> ();

                List<String> prefix = Arrays.asList (warmups,sidebar,warmups,treepopupmenu,items);

                if (n instanceof Chapter)
                {

                    final Chapter c = (Chapter) n;

                    final String chapterObjTypeName = Environment.getObjectTypeName (c).getValue ();

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                        .iconName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    c);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,converttoproject)))
                        .iconName (StyleClassNames.CONVERT)
                        .onAction (ev ->
                        {

                            this.viewer.convertWarmupToProject (this.viewer.getWarmupForChapter (c));

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,rename)))
                        .iconName (StyleClassNames.RENAME)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (WarmupProjectViewer.CommandId.renamewarmup,
                                                    this.viewer.getWarmupForChapter (c));

                        })
                        .build ());

                    if (this.viewer.isEditing (c))
                    {

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,close)))
                            .iconName (StyleClassNames.CLOSE)
                            .onAction (ev ->
                            {

                                this.viewer.runCommand (ProjectViewer.CommandId.closepanel,
                                                        c);

                            })
                            .build ());

                    }

                    its.add (UIUtils.createMoveMenu (move ->
                                                     {

                                                         Book b = c.getBook ();

                                                         if (move.equals (StyleClassNames.MOVETOP))
                                                         {

                                                             this.ignoreChaptersEvents = true;
                                                             try
                                                             {

                                                                 TreeItem<NamedObject> oitem = this.tree.getTreeItemForObject (c);

                                                                 int ind = oitem.getParent ().getChildren ().indexOf (oitem);

                                                                 TreeItem<NamedObject> parent = oitem.getParent ();

                                                                 parent.getChildren ().remove (oitem);

                                                                 parent.getChildren ().add (0,
                                                                                            oitem);

                                                                 b.moveChapter (c,
                                                                                0);

                                                             } finally {

                                                                 this.ignoreChaptersEvents = false;

                                                             }

                                                             this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                                                                           ProjectEvent.Action.move);

                                                             return;

                                                         }

                                                         if (move.equals (StyleClassNames.MOVEBOTTOM))
                                                         {

                                                             this.ignoreChaptersEvents = true;
                                                             try
                                                             {

                                                                 TreeItem<NamedObject> oitem = this.tree.getTreeItemForObject (c);

                                                                 TreeItem<NamedObject> parent = oitem.getParent ();

                                                                 parent.getChildren ().remove (oitem);

                                                                 parent.getChildren ().add (parent.getChildren ().size (),
                                                                                            oitem);

                                                                 b.moveChapter (c,
                                                                                b.getChapters ().size () - 1);

                                                             } finally {

                                                                 this.ignoreChaptersEvents = false;

                                                             }

                                                             this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                                                                           ProjectEvent.Action.move);

                                                             return;

                                                         }

                                                         int ind = b.getChapters ().indexOf (c);

                                                         if (move.equals (StyleClassNames.MOVEUP))
                                                         {

                                                             this.moveChapterUp (c);
                                                             return;

                                                         }

                                                         if (move.equals (StyleClassNames.MOVEDOWN))
                                                         {

                                                             this.moveChapterDown (c);

                                                             return;

                                                         }

                                                     },
                                                     getUILanguageStringProperty (Utils.newList (prefix,move)),
                                                     prefix));

/*
                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,move)))
                        .styleClassName (StyleClassNames.MOVE)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.deletechapter,
                                                    c);

                        })
                        .build ());
*/
                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                        .iconName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (WarmupProjectViewer.CommandId.deletewarmup,
                                                    this.viewer.getWarmupForChapter (c));

                        })
                        .build ());

                }

                return its;

            })
            .build ();

        this.selectItem ();

    }

    private void moveChapterDown (Chapter c)
    {

        Book b = c.getBook ();

        int ind = b.getChapters ().indexOf (c);

        if (ind == b.getChapters ().size () - 1)
        {

            return;

        }

        ind++;
        if (ind > b.getChapters ().size () - 1)
        {

            ind = b.getChapters ().size () - 1;

        }

        this.ignoreChaptersEvents = true;
        try
        {

            TreeItem<NamedObject> oitem = this.tree.getTreeItemForObject (c);

            TreeItem<NamedObject> parent = oitem.getParent ();

            parent.getChildren ().remove (oitem);

            parent.getChildren ().add (ind,
                                       oitem);

            b.moveChapter (c,
                           ind);

        } finally {

            this.ignoreChaptersEvents = false;

        }

        this.viewer.fireProjectEvent (ProjectEvent.Type.warmup,
                                      ProjectEvent.Action.move);

    }

    private void moveChapterUp (Chapter c)
    {

        int ind = c.getBook ().getChapters ().indexOf (c);

        if (ind == 0)
        {

            return;

        }

        ind--;
        if (ind < 0)
        {

            ind = 0;

        }

        this.ignoreChaptersEvents = true;
        try
        {

            TreeItem<NamedObject> oitem = this.tree.getTreeItemForObject (c);

            TreeItem<NamedObject> parent = oitem.getParent ();

            parent.getChildren ().remove (oitem);

            parent.getChildren ().add (ind,
                                       oitem);

            c.getBook ().moveChapter (c,
                                      ind);

        } finally {

            this.ignoreChaptersEvents = false;

        }

        this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                      ProjectEvent.Action.move);

    }

    private void selectItem ()
    {

        this.tree.select (null);
        this.tree.requestLayout ();

        Panel p = this.viewer.getCurrentPanel ();

        if (p == null)
        {

            return;

        }

        if (p != null)
        {

            if (p.getContent () instanceof NamedObjectPanelContent)
            {

                NamedObjectPanelContent nc = (NamedObjectPanelContent) p.getContent ();

                if (nc.getObject () instanceof Chapter)
                {

                    this.tree.select ((Chapter) nc.getObject ());
                    this.tree.requestLayout ();

                    return;

                }

            }

        }

    }

    private void removeListenersForChapter (Chapter c)
    {

        if (this.eventSourceSubscriptions.get (c) != null)
        {

            this.eventSourceSubscriptions.get (c).stream ()
                .forEach (s -> s.unsubscribe ());

            this.eventSourceSubscriptions.remove (c);

        }

    }

    private void selectChapter (Chapter c)
    {

        this.tree.select (c);


    }

    @Override
    public boolean canImport (NamedObject o)
    {
/*
        if (o instanceof Chapter)
        {

            Chapter c = (Chapter) o;

            // TODO Support drag-n-drop import from other projects.
            if (c.getParent ().equals (this.objType.getParent ()))
            {

                return true;

            }

        }
*/
        return false;

    }

    @Override
    public void importObject (NamedObject o)
    {

        new IllegalStateException ("Shouldnt be possible.");

    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public List<String> getStyleClassNames ()
    {

        return Arrays.asList (StyleClassNames.WARMUP);

    }

    @Override
    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (warmups,sidebar,warmups,title);

    }

    @Override
    public String getId ()
    {

        return Warmup.OBJECT_TYPE;

    }

    @Override
    public void init (State ss)
    {

        super.init (ss);

        if (ss != null)
        {

            String exp = ss.getAsString ("expanded");

            if (exp != null)
            {

                Arrays.stream (exp.split (","))
                    .forEach (v ->
                    {

                        NamedObject o = this.getObjectForReference (v);

                        if (o != null)
                        {

                            TreeItem<NamedObject> ti = this.tree.getTreeItemForObject (o);

                            if (ti != null)
                            {

                                ti.setExpanded (true);

                            }

                        }

                    });

            }

        }

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

        ss.set ("expanded",
                this.tree.getExpandedTreeItems ().stream ()
                    .map (ti -> ti.getValue ().getObjectReference ().asString ())
                    .collect (Collectors.joining (",")));

        return ss;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        List<String> prefix = Arrays.asList (warmups,sidebar,warmups,headerpopupmenu,items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings._new)))
                .iconName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.viewer.runCommand (WarmupProjectViewer.CommandId.warmup);

                })
                .build ());

            return items;

        };

    }

    private TreeItem<NamedObject> createTreeItem (Chapter c)
    {

        TreeItem<NamedObject> ci = new TreeItem<> (c);

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

    public NamedObject getObjectForReference (String s)
    {

        if (s == null)
        {

            return null;

        }

        ObjectReference ref = ObjectReference.parseObjectReference (s);

        return (NamedObject) this.viewer.getProject ().getObjectForReference (ref);

    }

}
