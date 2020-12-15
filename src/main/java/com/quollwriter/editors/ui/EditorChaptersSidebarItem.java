package com.quollwriter.editors.ui;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.geometry.*;

import org.reactfx.*;
import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class EditorChaptersSidebarItem extends ProjectObjectsSidebarItem<EditorProjectViewer>
{

    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;
    private Map<Chapter, List<Subscription>> eventSourceSubscriptions = new HashMap<> ();
    private Map<Chapter, NoteTreeLabel> noteTreeLabels = new HashMap<> ();
    private boolean ignoreChaptersEvents = false;

    public EditorChaptersSidebarItem (EditorProjectViewer pv,
                                      IPropertyBinder     binder)
    {

        super (pv,
               binder);

        pv.getProject ().getBooks ().get (0).getChapters ().stream ()
            .forEach (c -> this.addListenersForChapter (c));

        this.countProp = new SimpleIntegerProperty (0);

        this.addChangeListener (this.viewer.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            this.selectItem ();

        });

        this.tree = NamedObjectTree.builder ()
            .project (this.viewer.getProject ())
            .root (this.createTree ())
            .onDragDropped (n -> {})
            .canExport (n -> false)
            .canImport ((nOver, nImport) -> false)
            .labelProvider (treeItem ->
            {

                NamedObject n = treeItem.getValue ();

                if (n instanceof Project)
                {

                    return new Label ();

                }

                QuollLabel l = QuollLabel.builder ()
                    .styleClassName (n.getObjectType ())
                    .build ();

                if (n instanceof Note)
                {

                    Note _n = (Note) n;

                    l.setIconClassName (StyleClassNames.COMMENT);
                    l.getStyleClass ().add (StyleClassNames.COMMENT);
                    l.textProperty ().bind (n.nameProperty ());

                }

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    l.requestFocus ();

                    this.viewer.viewObject (n);

                });

                List<String> prefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,items);

                if (n instanceof Chapter)
                {

                    Chapter c = (Chapter) n;

                    this.addSetChangeListener (c.getNotes (),
                                               ev ->
                    {

                        this.updateChapterLabel (c,
                                                 l);

                    });

                    this.addChangeListener (c.nameProperty (),
                                            (pr, oldv, newv) ->
                    {

                        this.updateChapterLabel (c,
                                                 l);

                    });

                    this.updateChapterLabel (c,
                                             l);

                    //l.setFocusTraversable (true);

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

                if (n instanceof Note)
                {

                    List<String> prefix = Arrays.asList (editors,project,sidebar,comments,treepopupmenu,comments,items);

                    final Note note = (Note) n;

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                        .iconName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.viewer.viewObject (n);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                        .iconName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            this.viewer.editComment (note);

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                        .iconName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.showDeleteComment (note);

                        })
                        .build ());

                }

                if (n instanceof Chapter)
                {

                    List<String> prefix = Arrays.asList (editors,project,sidebar,comments,treepopupmenu,chapters,items);

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

                    if (!c.isEditComplete ())
                    {

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,seteditcomplete)))
                            .iconName (StyleClassNames.EDITCOMPLETE)
                            .onAction (ev ->
                            {

                                this.viewer.setChapterEditComplete (c,
                                                                    true);

                            })
                            .build ());

                    } else {

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,seteditneeded)))
                            .iconName (StyleClassNames.EDITNEEDED)
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
                            .iconName (StyleClassNames.GOTOEDITPOSITION)
                            .onAction (ev ->
                            {

                                this.viewer.editChapter (c,
                                                         () ->
                                                         {

                                                            EditorChapterPanel p = this.viewer.getEditorForChapter (c);

                                                            p.getEditor ().moveTo (c.getEditPosition ());
                                                            p.getEditor ().requestFollowCaret ();

                                                         });

                            })
                            .build ());

                        its.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,removeeditposition)))
                            .iconName (StyleClassNames.REMOVEEDITPOSITION)
                            .onAction (ev ->
                            {

                                this.viewer.removeChapterEditPosition (c);

                            })
                            .build ());

                    }

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

        if (this.eventSourceSubscriptions.get (c) != null)
        {

            this.eventSourceSubscriptions.get (c).stream ()
                .forEach (s -> s.unsubscribe ());

            this.eventSourceSubscriptions.remove (c);

        }

    }

    private void updateChapterLabel (Chapter c,
                                     Label   l)
    {

        l.textProperty ().unbind ();

        String v = "%1$s (%2$s)";

        l.setText (String.format (v,
                                  c.getName (),
                                  Environment.formatNumber (c.getNotes ().size ())));

        //l.setAccessibleText ("TEXTING");//l.getText ());


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

                ChapterItem ci = ev.getSource ();

                TreeItem<NamedObject> pti = this.tree.getTreeItemForObject (ci.getChapter ());

                int ind = new ArrayList (c.getNotes ()).indexOf (ci);

                TreeItem<NamedObject> ti = new TreeItem<> (ci);

                if (ind < 0)
                {

                    ind = 0;

                }

                pti.getChildren ().add (ind,
                                        ti);

            }

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
    public boolean canImport (NamedObject o)
    {

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

        return Arrays.asList (StyleClassNames.CHAPTER);

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

            return items;

        };

    }

    private TreeItem<NamedObject> createTreeItem (Chapter c)
    {

        TreeItem<NamedObject> ci = new TreeItem<> (c);

        ci.getChildren ().addAll (c.getNotes ().stream ()
            .map (n -> new TreeItem<NamedObject> (n))
            .collect (Collectors.toList ()));

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

        if (s.startsWith (NoteTreeLabel.OBJECT_TYPE))
        {

            int ind = s.indexOf ("-");

            if (ind > -1)
            {

                s = s.substring (0,
                                 ind);

            }

            StringTokenizer t = new StringTokenizer (s,
                                                     "/");

            t.nextToken ();

            String k = t.nextToken ();

            try
            {

                Chapter c = this.viewer.getProject ().getChapterByKey (Long.parseLong (k));

                if (c != null)
                {

                    return this.noteTreeLabels.get (c);

                }

            } catch (Exception e) {

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

            return new ObjectReference (this.getObjectType () + "/" + this.chapter.getKey (),
                                        1L,
                                        this.chapter.getObjectReference ());

        }

    }

}
