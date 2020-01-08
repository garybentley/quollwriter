package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;

import org.reactfx.*;

import com.quollwriter.data.Scene;
import com.quollwriter.data.OutlineItem;
import com.quollwriter.data.Project;
import com.quollwriter.data.Chapter;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.BlankNamedObject;
import com.quollwriter.data.Note;
import com.quollwriter.data.ChapterItem;
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

public class ChaptersSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;
    private Map<Chapter, List<Subscription>> eventSourceSubscriptions = new HashMap<> ();
    private Map<Chapter, NoteTreeLabel> noteTreeLabels = new HashMap<> ();
    private boolean ignoreChaptersEvents = false;

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

            if (this.ignoreChaptersEvents)
            {

                return;

            }

            while (ev.next ())
            {

                if (ev.wasAdded ())
                {

                    ev.getAddedSubList ().stream ()
                        .forEach (c ->
                        {

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
                            NoteTreeLabel l = this.noteTreeLabels.remove (c);

                            if (l != null)
                            {

                                if (l.listenerHandle != null)
                                {

                                    l.listenerHandle.dispose ();

                                }

                            }

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

            this.selectItem ();

        });

        this.addChangeListener (UserProperties.showNotesInChapterListProperty (),
                                (pr, oldv, newv) ->
        {

            if (newv)
            {

                // Add a notes branch.
                this.viewer.getProject ().getBooks ().get (0).getChapters ().stream ()
                    .forEach (c ->
                    {

                        if ((c.getNotes ().size () > 0)
                            &&
                            (this.noteTreeLabels.get (c) == null)
                           )
                        {

                            TreeItem<NamedObject> noteLabel = this.createNotesItem (c);

                            // Notes always go at the bottom.
                            this.tree.getTreeItemForObject (c).getChildren ().add (noteLabel);

                        }

                    });

            } else {

                // Remove the notes branch.
                this.viewer.getProject ().getBooks ().get (0).getChapters ().stream ()
                    .forEach (c ->
                    {

                        NoteTreeLabel l = this.noteTreeLabels.get (c);

                        if (l != null)
                        {

                            this.tree.removeObject (l);

                            if (l.listenerHandle != null)
                            {

                                l.listenerHandle.dispose ();

                            }

                            this.noteTreeLabels.remove (c);

                        }

                    });

            }

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

                    this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                                  ProjectEvent.Action.move);

                } finally {

                    this.ignoreChaptersEvents = false;

                }

            })
            .canExport (n -> true)
            .canImport (n ->
            {

                if (n instanceof Chapter)
                {

                    Chapter c = (Chapter) n;
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

                if (n instanceof NoteTreeLabel)
                {

                    NoteTreeLabel nl = (NoteTreeLabel) n;

                    QuollLabel l = QuollLabel.builder ()
                        .styleClassName (StyleClassNames.NOTES)
                        .build ();

                    StringBinding b = Bindings.createStringBinding (() ->
                    {

                        String v = "%1$s (%2$s)";

                        return String.format (v,
                                              getUILanguageStringProperty (objectnames,plural, Note.OBJECT_TYPE).getValue (),
                                              Environment.formatNumber (nl.chapter.getNotes ().size ()));

                    },
                    UILanguageStringsManager.uilangProperty (),
                    Environment.objectTypeNameChangedProperty ());

                    l.textProperty ().bind (b);

                    nl.listenerHandle = this.addSetChangeListener (nl.chapter.getNotes (),
                                                                   ev ->
                    {

                        b.invalidate ();

                    });

                    return l;

                }

                QuollLabel l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .styleClassName (n.getObjectType ())
                    .build ();

                if (n instanceof Note)
                {

                    Note _n = (Note) n;

                    if (_n.isEditNeeded ())
                    {

                        l.getStyleClass ().add (StyleClassNames.EDITNEEDEDNOTE);

                    }

                }

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

                return l;

            })
            .onClick ((n, ev) ->
            {

                if (n instanceof NoteTreeLabel)
                {

                    TreeItem<NamedObject> ti = this.tree.getTreeItemForObject (n);

                    ti.setExpanded (!ti.isExpanded ());

                } else {

                    this.viewer.viewObject (n);

                }

            })
            .contextMenuItemSupplier (n ->
            {

                Set<MenuItem> its = new LinkedHashSet<> ();

                List<String> prefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,items);

                if (n instanceof Note)
                {

                    List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,notes,items);

                    final Note note = (Note) n;

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.viewObject (n);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,edit)))
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

                        its.add (tm);

                    }

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.showDeleteChapterItemPopup (note,
                                                                    this.tree.getCellForObject (note));

                        })
                        .build ());

                }

                if (n instanceof Chapter)
                {

                    final Chapter c = (Chapter) n;

                    final String chapterObjTypeName = Environment.getObjectTypeName (c).getValue ();

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                        .styleClassName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    c);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
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

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,seteditcomplete)))
                            .styleClassName (StyleClassNames.EDITCOMPLETE)
                            .onAction (ev ->
                            {

                                this.viewer.setChapterEditComplete (c,
                                                                    true);

                            })
                            .build ());

                    } else {

                        its.add (QuollMenuItem.builder ()
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

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,gotoeditposition)))
                            .styleClassName (StyleClassNames.GOTOEDITPOSITION)
                            .onAction (ev ->
                            {

                                this.viewer.editChapter (c,
                                                         () ->
                                                         {

                                                            ProjectChapterEditorPanelContent p = this.viewer.getEditorForChapter (c);

                                                            p.getEditor ().moveTo (c.getEditPosition ());
                                                            p.getEditor ().requestFollowCaret ();

                                                         });

                            })
                            .build ());

                        its.add (QuollMenuItem.builder ()
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

                        its.add (tm);

                    }

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,newbelow)))
                        .styleClassName (StyleClassNames.ADD)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.newchapter,
                                                    c);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
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

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,close)))
                            .styleClassName (StyleClassNames.CLOSE)
                            .onAction (ev ->
                            {

                                this.viewer.runCommand (ProjectViewer.CommandId.closepanel,
                                                        c);

                            })
                            .build ());

                    }

                    its.add (QuollMenuItem.builder ()
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

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    oi);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
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

                        its.add (tm);

                    }

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.showDeleteChapterItemPopup (oi,
                                                                    this.tree.getCellForObject (oi));

                        })
                        .build ());

                }

                if (n instanceof Scene)
                {

                    List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,scenes,items);

                    final Scene s = (Scene) n;

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                                    s);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
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

                        its.add (tm);

                    }

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.showDeleteChapterItemPopup (s,
                                                                    this.tree.getCellForObject (s));

                        })
                        .build ());

                }

                return its;

            })
            .build ();

        this.selectItem ();

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
                this.tree.removeObject (ev.getSource ());

            }

            if (ev.getType () == CollectionEvent.Type.add)
            {

                TreeItem<NamedObject> pti = null;

                ChapterItem ci = ev.getSource ();

                int ind = 0;

                if (ci instanceof OutlineItem)
                {

                    OutlineItem oi = (OutlineItem) ci;

                    if (oi.getScene () != null)
                    {

                        pti = this.tree.getTreeItemForObject (oi.getScene ());
                        ind = new ArrayList (oi.getScene ().getOutlineItems ()).indexOf (oi);

                    } else {

                        ind = new ArrayList (c.getOutlineItems ()).indexOf (oi);

                    }

                }

                if (ci instanceof Scene)
                {

                    ind = new ArrayList (ci.getChapter ().getScenes ()).indexOf (ci);

                }

                if (ci instanceof Note)
                {

                    if (!UserProperties.isShowNotesInChapterList ())
                    {

                        // Don't do anything.
                        return;

                    }

                    // TODO Add in positioning.
                    pti = this.tree.getTreeItemForObject (this.noteTreeLabels.get (c));
                    ind = new ArrayList (c.getNotes ()).indexOf (ci);

                }

                if (pti == null)
                {

                    pti = this.tree.getTreeItemForObject (ci.getChapter ());

                }

                TreeItem<NamedObject> ti = this.createTreeItem (ci);

                //int ind = pti.getChildren ().indexOf (this.tree.getTreeItemForObject (this.getChapterItemBefore (ci)));

                if (ind < 0)
                {

                    ind = 0;

                } else {

                    //ind++;

                }

                pti.getChildren ().add (ind,
                                        ti);

            }

        }));

       subs.add (c.chapterItemsPositionEvents ().subscribe (ev ->
       {

           // TODO Check for position change.

       }));

       this.eventSourceSubscriptions.put (c,
                                          subs);
    }

    private ChapterItem getChapterItemBefore (ChapterItem ci)
    {

        Set<ChapterItem> items = new TreeSet (Collections.reverseOrder (new ChapterItemSorter ()));

        int start = 0;
        int end = ci.getEndPosition ();
        Chapter ch = ci.getChapter ();

        if ((ci instanceof Scene)
            ||
            (ci instanceof OutlineItem)
           )
        {

            for (OutlineItem it : ch.getOutlineItems ())
            {

                if ((it.getPosition () >= start)
                    &&
                    (it.getPosition () <= end)
                   )
                {

                    items.add (it);

                }

            }

            for (Scene s : ch.getScenes ())
            {

                if ((s.getPosition () >= start)
                    &&
                    (s.getPosition () <= end)
                   )
                {

                    items.add (s);

                }

            }

        }

        if (ci instanceof Note)
        {

            for (Note n : ch.getNotes ())
            {

                if ((n.getPosition () >= start)
                    &&
                    (n.getPosition () <= end)
                   )
                {

                    items.add (n);

                }

            }

        }

        if (items.size () > 0)
        {

            return items.iterator ().next ();

        }

        return null;

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

    private TreeItem<NamedObject> createTreeItem (ChapterItem citem)
    {

        TreeItem<NamedObject> cii = new TreeItem<> (citem);

        if (citem instanceof Scene)
        {

            Scene s = (Scene) citem;

            for (OutlineItem oitem : s.getOutlineItems ())
            {

                TreeItem<NamedObject> oii = new TreeItem<> (oitem);

                cii.getChildren ().add (oii);

            }

        }

        return cii;

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

            ci.getChildren ().add (this.createTreeItem (citem));

        }

        if ((UserProperties.isShowNotesInChapterList ())
            &&
            ((c.getNotes ().size () > 0))
           )
        {

            ci.getChildren ().add (this.createNotesItem (c));

        }

        return ci;

    }

    private TreeItem<NamedObject> createNotesItem (Chapter c)
    {

        NoteTreeLabel l = new NoteTreeLabel (c);
        TreeItem<NamedObject> noteLabel = new TreeItem<> (l);
        this.noteTreeLabels.put (c, l);
        // Add the current notes.
        c.getNotes ().stream ()
            .forEach (n -> noteLabel.getChildren ().add (new TreeItem<> (n)));

        return noteLabel;

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

        if (s.startsWith (NoteTreeLabel.OBJECT_TYPE))
        {

            s = s.substring (s.indexOf ("/") + 1);

            ObjectReference ref = ObjectReference.parseObjectReference (s);

            NamedObject o = (NamedObject) this.viewer.getProject ().getObjectForReference (ref);

            if (o instanceof Chapter)
            {

                return this.noteTreeLabels.get ((Chapter) o);

            }

            return null;

        }

        ObjectReference ref = ObjectReference.parseObjectReference (s);

        return (NamedObject) this.viewer.getProject ().getObjectForReference (ref);

    }

    private static class NoteTreeLabel extends BlankNamedObject
    {

        public static final String OBJECT_TYPE = "notetreelabel";

        public Chapter chapter = null;
        public IPropertyBinder.ListenerHandle listenerHandle = null;

        public NoteTreeLabel (Chapter c)
        {

            super (OBJECT_TYPE,
                   "note-tree-label");

            this.chapter = c;

        }

        @Override
        public boolean equals (Object o)
        {

            if (o instanceof NoteTreeLabel)
            {

                NoteTreeLabel no = (NoteTreeLabel) o;

                if (no.chapter.equals (this.chapter))
                {

                    return true;

                }

            }

            return false;

        }

        @Override
        public int hashCode ()
        {

            return Objects.hash (this.getName (), this.chapter);

        }

        @Override
        public ObjectReference getObjectReference ()
        {

            return new ObjectReference (this.getObjectType () + "/" + this.chapter.getObjectReference ().asString (),
                                        1L,
                                        this.chapter.getObjectReference ());

        }

    }

}
