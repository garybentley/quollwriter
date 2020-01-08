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

public class NotesSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;
    private Map<Chapter, List<Subscription>> eventSourceSubscriptions = new HashMap<> ();
    private StringProperty noteType = null;

    public NotesSidebarItem (ProjectViewer   pv,
                             StringProperty  noteType,
                             IPropertyBinder binder)
    {

        super (pv,
               binder);

        this.noteType = noteType;
        this.countProp = new SimpleIntegerProperty (0);

        this.tree = NamedObjectTree.builder ()
            .project (this.viewer.getProject ())
            .root (this.createTree ())
            .canExport (n -> (n instanceof Note))
            .labelProvider (treeItem ->
            {

                NamedObject n = treeItem.getValue ();

                if (n instanceof Project)
                {

                    return new Label ();

                }

                QuollLabel l = null;

                if (n instanceof Note)
                {

                    Note nl = (Note) n;

                    l = QuollLabel.builder ()
                        .styleClassName (nl.isEditNeeded () ? StyleClassNames.EDITNEEDEDNOTE : Note.OBJECT_TYPE)
                        .label (nl.nameProperty ())
                        .build ();

                    return l;

                }

                if (n instanceof Chapter)
                {

                    Chapter nl = (Chapter) n;

                    l = QuollLabel.builder ()
                        .styleClassName (Chapter.OBJECT_TYPE)
                        .build ();

                    StringBinding b = Bindings.createStringBinding (() ->
                    {

                        String v = "%1$s (%2$s)";

                        return String.format (v,
                                              //getUILanguageStringProperty (objectnames,plural, Chapter.OBJECT_TYPE).getValue (),
                                              nl.getName (),
                                              Environment.formatNumber (nl.getNotesForType (this.noteType.getValue ()).size ()));

                    },
                    this.noteType,
                    this.countProp,
                    nl.nameProperty (),
                    UILanguageStringsManager.uilangProperty (),
                    Environment.objectTypeNameChangedProperty ());

                    l.textProperty ().bind (b);

                }

                return l;

            })
            .onClick ((n, ev) ->
            {

                if (n instanceof Chapter)
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

                List<String> prefix = Arrays.asList (project,sidebar,notes,treepopupmenu,items);

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

                return its;

            })
            .build ();

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

                            this.addListenersForChapter (c);

                        });

                }

                if (ev.wasRemoved ())
                {

                    ev.getRemoved ().stream ()
                        .forEach (c ->
                        {

                            this.recreateTree ();
                            this.removeListenersForChapter (c);

                        });

                }

            }

        });

        this.updateCount ();

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

                this.recreateTree ();

            }

            // Need to check for note type changes.
            if (ev.getType () == CollectionEvent.Type.add)
            {

                TreeItem<NamedObject> pti = null;

                ChapterItem ci = ev.getSource ();

                if (!(ci instanceof Note))
                {

                    return;

                }

                Note n = (Note) ci;

                if (!n.getType ().equals (this.noteType.getValue ()))
                {

                    return;

                }

                this.recreateTree ();

            }

        }));

       subs.add (c.chapterItemsPositionEvents ().subscribe (ev ->
       {

           // TODO Check for position change.
           // Recreate the list.
           ChapterItem i = ev.getItem ();

           if (!(i instanceof Note))
           {

               return;

           }

           Note n = (Note) i;

           this.recreateTree ();

       }));

       this.eventSourceSubscriptions.put (c,
                                          subs);
    }

    private void updateCount ()
    {

        int count = 0;

        List<Chapter> objs = new ArrayList<> (this.viewer.getProject ().getBooks ().get (0).getChapters ());

        for (Chapter c : objs)
        {

            count += c.getNotes ().stream ()
                .filter (n -> n.getType ().equals (this.noteType.getValue ()))
                .collect (Collectors.toList ()).size ();

        }

        this.countProp.setValue (count);

    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.NOTES;

    }

    @Override
    public StringProperty getTitle ()
    {

        StringBinding b = Bindings.createStringBinding (() ->
        {

            String v = "%1$s %2$s";

            return String.format (v,
                                  this.noteType.getValue (),
                                  getUILanguageStringProperty (objectnames,plural, Note.OBJECT_TYPE).getValue ());

        },
        this.noteType,
        this.countProp,
        UILanguageStringsManager.uilangProperty (),
        Environment.objectTypeNameChangedProperty ());

        StringProperty p = new SimpleStringProperty ();
        p.bind (b);
        return p;

        //return Environment.getObjectTypeNamePlural (Note.OBJECT_TYPE);

    }

    @Override
    public String getId ()
    {

        return Note.OBJECT_TYPE + "/" + this.noteType.getValue ();

    }

    public static StringProperty getNoteTypeForId (String v)
    {

        if (v.startsWith (Note.OBJECT_TYPE + "/"))
        {

            String t = v.substring (Note.OBJECT_TYPE.length () + 1);

            return new SimpleStringProperty (t);
            //return UserProperties.getNoteTypeProperty (v);

        }

        return null;

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

                        NamedObject o = (NamedObject) this.viewer.getProject ().getObjectForReference (ObjectReference.parseObjectReference (v));

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

        List<String> prefix = Arrays.asList (project,sidebar,notes,headerpopupmenu,items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,manage)))
                .styleClassName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.viewer.showManageNoteTypes ();

                })
                .build ());

            return items;

        };

    }

    private TreeItem<NamedObject> createTreeItem (Chapter c)
    {

        TreeItem<NamedObject> ci = new TreeItem<> (c);

        List<Note> items = new ArrayList<> ();

        items.addAll (c.getNotesForType (this.noteType.getValue ()));

        if (items.size () == 0)
        {

            return null;

        }

        Collections.sort (items,
                          new ChapterItemSorter ());

        for (Note citem : items)
        {

            TreeItem<NamedObject> cii = new TreeItem<> (citem);

            ci.getChildren ().add (cii);

        }

        return ci;

    }

    private void recreateTree ()
    {

        Set<TreeItem<NamedObject>> expanded = null;

        if (this.tree != null)
        {

            expanded = this.tree.getExpandedTreeItems ();

        }

        this.tree.setRoot (this.createTree ());

        if (expanded != null)
        {

            expanded.stream ()
                .forEach (i ->
                {

                    TreeItem<NamedObject> ti = this.tree.getTreeItemForObject (i.getValue ());

                    if (ti != null)
                    {

                        ti.setExpanded (true);

                    }

                });

        }

    }

    private TreeItem<NamedObject> createTree ()
    {

        List<Chapter> objs = new ArrayList<> (this.viewer.getProject ().getBooks ().get (0).getChapters ());

        TreeItem<NamedObject> root = new TreeItem<> (this.viewer.getProject ());

        int count = 0;

        for (Chapter c : objs)
        {

            TreeItem<NamedObject> cti = this.createTreeItem (c);

            if (cti == null)
            {

                // No notes for this chapter.
                continue;

            }

            root.getChildren ().add (cti);

        }

        this.updateCount ();

        return root;

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.countProp;

    }

}
