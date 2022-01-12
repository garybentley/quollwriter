package com.quollwriter.data;

import java.io.*;

import java.util.*;

import javax.swing.text.Position;
import javax.swing.text.Document;

import javafx.collections.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.comparators.*;

import org.incava.util.diff.*;

import org.dom4j.*;
import org.dom4j.tree.*;

import org.reactfx.*;

import com.quollwriter.text.*;

public class Chapter extends LegacyUserConfigurableObject
{

    public static final String PLAN_LEGACY_FIELD_ID = "plan";
    public static final String GOALS_LEGACY_FIELD_ID = "goals";

    public static final String OBJECT_TYPE = "chapter";
    public static final String INFORMATION_OBJECT_TYPE = "chapterinformation";
    public static final String EDIT_POSITION = "editposition";

    private Book              book = null;
    private StringWithMarkup  text = null;
    private StringWithMarkup  goals = null;
    private StringWithMarkup  plan = null;
    private ObjectProperty<StringWithMarkup> planProp = new SimpleObjectProperty<> ();
    private ObservableSet<OutlineItem> outlineItems = FXCollections.observableSet (new TreeSet<> (new ChapterItemSorter ()));
    private ObservableSet<Scene>       scenes = FXCollections.observableSet (new TreeSet<> (new ChapterItemSorter ()));
    private int editPosition = -1;

    private IntegerProperty editPositionProp = new SimpleIntegerProperty (-1);
    private Position textEditPos = null;
    private boolean editComplete = false;
    private BooleanProperty editCompleteProp = new SimpleBooleanProperty (false);
    private ObservableSet<Issue> problemFinderIgnores = FXCollections.observableSet (new HashSet<> ());

    private ProjectVersion projVersion = null;

    private EventSource<CollectionEvent<? extends ChapterItem>> chapterItemsEventSource = new EventSource<> ();
    private EventSource<ChapterItem.ChapterItemEvent> chapterItemsPositionEventSource = new EventSource<> ();
    private Map<ChapterItem, List<Subscription>> eventSourceSubscriptions = new HashMap<> ();

    public Chapter()
    {

        super (Chapter.OBJECT_TYPE);

        this.outlineItems.addListener ((SetChangeListener<OutlineItem>) ev ->
        {

            if (ev.wasRemoved ())
            {

                OutlineItem it = ev.getElementRemoved ();

                it.dispose ();

                this.chapterItemsEventSource.push (new CollectionEvent<ChapterItem> (this.outlineItems,
                                                                                     it,
                                                                                     CollectionEvent.Type.remove));

            }

            if (ev.wasAdded ())
            {

                OutlineItem it = ev.getElementAdded ();

                this.chapterItemsEventSource.push (new CollectionEvent<ChapterItem> (this.outlineItems,
                                                                                     it,
                                                                                     CollectionEvent.Type.add));

                it.addChangeListener (it.positionProperty (),
                                      (pr, oldv, newv) ->
                {

                    this.chapterItemsPositionEventSource.push (new ChapterItem.ChapterItemEvent (it,
                                                                                  ChapterItem.ChapterItemEvent.Type.positionchange,
                                                                                  pr,
                                                                                  oldv.intValue (),
                                                                                  newv.intValue ()));

                });

            }

        });

        this.scenes.addListener ((SetChangeListener<Scene>) ev ->
        {

            if (ev.wasAdded ())
            {

                Scene it = ev.getElementAdded ();

                this.chapterItemsEventSource.push (new CollectionEvent<ChapterItem> (this.scenes,
                                                                                     it,
                                                                                     CollectionEvent.Type.add));

                it.addChangeListener (it.positionProperty (),
                                      (pr, oldv, newv) ->
                {

                    this.chapterItemsPositionEventSource.push (new ChapterItem.ChapterItemEvent (it,
                                                                                    ChapterItem.ChapterItemEvent.Type.positionchange,
                                                                                    pr,
                                                                                    oldv.intValue (),
                                                                                    newv.intValue ()));

                });

                List<Subscription> subs = new ArrayList<> ();
                subs.add (it.outlineItemsPositionEvents ().subscribe (oev ->
                {

                    this.chapterItemsPositionEventSource.push (oev);

                }));
                subs.add (it.outlineItemsEvents ().subscribe (oev ->
                {

                    this.chapterItemsEventSource.push (oev);

                }));

                this.eventSourceSubscriptions.put (it,
                                                   subs);

            }

            if (ev.wasRemoved ())
            {

                Scene s = ev.getElementRemoved ();

                s.dispose ();

                // Need to unsubscribe from the scene AND the outline items set...
                this.eventSourceSubscriptions.get (s).stream ()
                    .forEach (ss -> ss.unsubscribe ());

                this.chapterItemsEventSource.push (new CollectionEvent<ChapterItem> (this.scenes,
                                                                                     ev.getElementRemoved (),
                                                                                     CollectionEvent.Type.remove));

            }

        });

        this.getNotes ().addListener ((SetChangeListener<Note>) ev ->
        {

            if (ev.wasAdded ())
            {

                Note it = ev.getElementAdded ();

                this.chapterItemsEventSource.push (new CollectionEvent<ChapterItem> (this.getNotes (),
                                                                                     it,
                                                                                     CollectionEvent.Type.add));

                it.addChangeListener (it.positionProperty (),
                                      (pr, oldv, newv) ->
                {

                    this.chapterItemsPositionEventSource.push (new ChapterItem.ChapterItemEvent (it,
                                                                                    ChapterItem.ChapterItemEvent.Type.positionchange,
                                                                                    pr,
                                                                                    oldv.intValue (),
                                                                                    newv.intValue ()));

                });

            }

            if (ev.wasRemoved ())
            {

                Note s = ev.getElementRemoved ();

                // NamedObject handles the removal and disposes.

                this.chapterItemsEventSource.push (new CollectionEvent<ChapterItem> (this.getNotes (),
                                                                                     ev.getElementRemoved (),
                                                                                     CollectionEvent.Type.remove));

            }

        });

    }

    public Chapter(Book b)
    {

        this ();

        this.setBook (b);

    }

    public Chapter(Book   b,
                   String name)
    {

        this (b);

        this.setName (name);

    }

    protected Chapter(String objType)
    {

        super (objType);

    }

    public EventStream<CollectionEvent<? extends ChapterItem>> chapterItemsEvents ()
    {

        return this.chapterItemsEventSource;

    }

    public EventStream<ChapterItem.ChapterItemEvent> chapterItemsPositionEvents ()
    {

        return this.chapterItemsPositionEventSource;

    }

    public boolean isFieldSupported (String id)
    {

        if ((id.equals (DESCRIPTION_LEGACY_FIELD_ID))
            ||
            (id.equals (ALIASES_LEGACY_FIELD_ID))
            ||
            (id.equals (PLAN_LEGACY_FIELD_ID))
            ||
            (id.equals (GOALS_LEGACY_FIELD_ID))
           )
        {

            return true;

        }

        return false;

    }

    public void initProblemFinderIgnoreDocumentPositions (Document doc)
                                                   throws Exception
    {

        for (Issue i : this.problemFinderIgnores)
        {

            i.setStartPosition (doc.createPosition (i.getStartIssuePosition ()));

        }

    }

    public Set<Issue> getProblemFinderIgnores (Rule forRule)
    {

        Set<Issue> ignores = new LinkedHashSet<> ();

        for (Issue i : this.problemFinderIgnores)
        {

            if (i.getRuleId ().equals (forRule.getId ()))
            {

                ignores.add (i);

            }

        }

        return ignores;

    }

    public ObservableSet<Issue> getProblemFinderIgnores ()
    {

        return this.problemFinderIgnores;

    }

    public void setProblemFinderIgnores (Set<Issue> ignores)
    {

        this.problemFinderIgnores.clear ();

        this.problemFinderIgnores.addAll (ignores);

    }

    public void setProjectVersion (ProjectVersion v)
    {

        this.projVersion = v;

    }

    public ProjectVersion getProjectVersion ()
    {

        if (this.projVersion == null)
        {

            return this.getBook ().getProject ().getProjectVersion ();

        }

        return this.projVersion;

    }

    /**
     * If a chapter item has it's position changed (say via a drag/drop operation to move it)
     * then the sets need to be "reindexed" since the ordering is dependent on the position and
     * it is only done once when the item is added (probably for performance sake).
     *
     * This method recreates the sets backing the scenes/notes/outline items and calls the
     * same reindex method on child objects, so re-initing the entire tree.
     */
    /*
    public synchronized void reindex ()
    {

        super.reindex ();

        TreeSet<Scene> sscenes = new TreeSet (new ChapterItemSorter ());

        sscenes.addAll (this.scenes);

        this.scenes = sscenes;

        TreeSet<OutlineItem> ooutlineItems = new TreeSet (new ChapterItemSorter ());

        ooutlineItems.addAll (this.outlineItems);

        this.outlineItems = ooutlineItems;

        for (Scene s : this.scenes)
        {

            s.reindex ();

        }

        for (OutlineItem it : this.outlineItems)
        {

            it.reindex ();

        }

    }
*/
    public boolean isEditComplete ()
    {

        return this.editComplete;

    }

    public void setEditComplete (boolean b)
    {

        this.editComplete = b;
        this.editCompleteProp.setValue (b);

    }

    public BooleanProperty editCompleteProperty ()
    {

        return this.editCompleteProp;

    }

    /*
    public String getMarkup ()
    {

        return this.markup;

    }

    public void setMarkup (String m)
    {

        this.markup = m;

    }
*/
    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

        Chapter c = (Chapter) old;

        this.addFieldChangeElement (root,
                                    "goals",
                                    ((old != null) ? c.getGoals () : null),
                                    this.goals);

        this.addFieldChangeElement (root,
                                    "plan",
                                    ((old != null) ? c.getPlan () : null),
                                    this.plan);

        if (old != null)
        {

            String ot = c.getChapterText ();

            if (ot == null)
            {

                ot = "";

            }

            ot = TextUtilities.stripNonValidXMLCharacters (ot);

            String nt = this.getChapterText ();

            if (nt == null)
            {

                nt = "";

            }

            nt = TextUtilities.stripNonValidXMLCharacters (nt);

            String[] oldText = ot.split ("\\n");
            String[] newText = nt.split ("\\n");

            List diffs = new Diff (oldText,
                                   newText).diff ();

            if (diffs.size () > 0)
            {

                Element fieldEl = new DefaultElement ("field");

                root.add (fieldEl);

                fieldEl.addAttribute ("name",
                                      "text");

                fieldEl.addAttribute ("type",
                                      "diff");

                for (int i = 0; i < diffs.size (); i++)
                {

                    Difference d = (Difference) diffs.get (i);

                    if (d.getDeletedEnd () == Difference.NONE)
                    {

                        // This is an addition.
                        for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
                        {

                            Element el = new DefaultElement ("change");
                            fieldEl.add (el);
                            el.addAttribute ("type",
                                             "add");
                            el.addAttribute ("line",
                                             String.valueOf (k));
                            el.add (new DefaultCDATA (newText[k]));

                        }

                        continue;

                    }

                    if (d.getAddedEnd () == Difference.NONE)
                    {

                        // This is a deletion.
                        for (int k = d.getDeletedStart (); k < (d.getDeletedEnd () + 1); k++)
                        {

                            Element el = new DefaultElement ("change");
                            fieldEl.add (el);
                            el.addAttribute ("type",
                                             "remove");
                            el.addAttribute ("line",
                                             String.valueOf (k));
                            el.add (new DefaultCDATA (oldText[k]));

                        }

                        continue;

                    }

                    // This is a modification.
                    for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
                    {

                        Element el = new DefaultElement ("change");
                        fieldEl.add (el);
                        el.addAttribute ("type",
                                         "change");
                        el.addAttribute ("line",
                                         String.valueOf (k));

                        Element oel = new DefaultElement ("old");
                        el.add (oel);

                        ot = "";

                        if (k < oldText.length)
                        {

                            ot = oldText[k];

                        }

                        oel.add (new DefaultCDATA (ot));

                        Element nel = new DefaultElement ("new");
                        el.add (nel);

                        nt = "";

                        if (k < newText.length)
                        {

                            nt = newText[k];

                        }

                        nel.add (new DefaultCDATA (nt));

                    }

                }

            }

        } else
        {

            this.addFieldChangeElement (root,
                                        "text",
                                        null,
                                        this.getChapterText ());

        }

    }

    @Override
    public DataObject getObjectForReference (ObjectReference r)
    {

        DataObject d = super.getObjectForReference (r);

        if (d != null)
        {

            return d;

        }

        for (Scene s : this.scenes)
        {

            d = s.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (OutlineItem i : this.outlineItems)
        {

            d = i.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        return null;

    }

    public Set<ChapterItem> getChapterItemsWithPositionGreaterThan (int pos)
    {

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () > pos)
            {

                items.add (it);

            }

        }

        for (Scene s : this.scenes)
        {

            if (s.getPosition () > pos)
            {

                items.add (s);

            }

        }

        for (Note n : this.getNotes ())
        {

            if (n.getPosition () > pos)
            {

                items.add (n);

            }

        }

        return items;

    }

    public Set<ChapterItem> getChapterItemsWithPositionBetween (int start,
                                                                int end)
    {

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if ((it.getPosition () >= start)
                &&
                (it.getPosition () <= end)
               )
            {

                items.add (it);

            }

        }

        for (Scene s : this.scenes)
        {

            if ((s.getPosition () >= start)
                &&
                (s.getPosition () <= end)
               )
            {

                items.add (s);

            }

        }

        for (Note n : this.getNotes ())
        {

            if ((n.getPosition () >= start)
                &&
                (n.getPosition () <= end)
               )
            {

                items.add (n);

            }

        }

        return items;

    }

    public Set<OutlineItem> getItemsFromPositionToNextScene (int pos)
    {

        Set<OutlineItem> items = new TreeSet<> (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () >= pos)
            {

                items.add (it);

            }

        }

        Scene s = this.getLastScene (pos);

        if (s != null)
        {

            for (OutlineItem it : s.getOutlineItems ())
            {

                if (it.getPosition () >= pos)
                {

                    items.add (it);

                }

            }

        }

        return items;

    }

    public Scene getSceneAt (int pos)
    {

        for (Scene s : this.scenes)
        {

            if (s.getPosition () == pos)
            {

                return s;

            }

        }

        return null;

    }

    public Set<Scene> getScenesAt (int pos)
    {

        Set<Scene> scenes = new TreeSet<Scene> (new ChapterItemSorter ());

        for (Scene s : this.scenes)
        {

            if (s.getPosition () == pos)
            {

                scenes.add (s);

            }

        }

        return scenes;

    }

    public OutlineItem getOutlineItemAt (int pos)
    {

        for (Scene s : this.scenes)
        {

            OutlineItem it = s.getOutlineItemAt (pos);

            if (it != null)
            {

                return it;

            }

        }

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () == pos)
            {

                return it;

            }

        }

        return null;

    }

    public Set<OutlineItem> getOutlineItemsAt (int pos)
    {

        Set<OutlineItem> its = new TreeSet<> (new ChapterItemSorter ());

        for (Scene s : this.scenes)
        {

            its.addAll (s.getOutlineItemsAt (pos));

        }

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () == pos)
            {

                its.add (it);

            }

        }

        return its;

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        Set<ChapterItem> objs = new TreeSet<> (new ChapterItemSorter ());

        for (Scene s : this.scenes)
        {

            objs.add (s);

        }

        for (OutlineItem oi : this.outlineItems)
        {

            objs.add (oi);

        }

        for (Note n : this.getNotes ())
        {

            objs.add (n);

        }

        return new LinkedHashSet<NamedObject> (objs);

        /*
        List<NamedObject> objs = new ArrayList<> ();
        objs.addAll (this.scenes);
        objs.addAll (this.outlineItems);
        objs.addAll (this.getNotes ());

        Collections.sort (objs,
                          new ChapterItemSorter ());

        return new TreeSet<NamedObject> (objs);
        */
/*
        Set<NamedObject> objs = new TreeSet<> (new ChapterItemSorter ());

        objs.addAll (this.scenes);
        objs.addAll (this.outlineItems);
        objs.addAll (this.getNotes ());

        return objs;
*/
    }

    public void addChapterItem (ChapterItem item)
    {

        if (item.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.addNote ((Note) item);

        }

        if (item.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.addOutlineItem ((OutlineItem) item);

        }

        if (item.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.addScene ((Scene) item);

        }

    }

    public Set<? extends ChapterItem> getAllStructureItemsWithinRange (int min,
                                                                       int max)
    {

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () < min || it.getPosition () > max)
            {

                continue;

            }

            items.add (it);

        }

        for (Scene s : this.scenes)
        {

            if (s.getPosition () >= min && s.getPosition () <= max)
            {

                items.add (s);

            }

            Set<OutlineItem> oitems = s.getOutlineItems ();

            for (OutlineItem oit : oitems)
            {

                if (oit.getPosition () < min || oit.getPosition () > max)
                {

                    continue;

                }

                items.add (oit);

            }

        }

        return items;

    }

    public Set<? extends ChapterItem> getChapterItems (String objType)
    {

        if (objType.equals (OutlineItem.OBJECT_TYPE))
        {

            return this.getOutlineItems ();

        }

        if (objType.equals (Note.OBJECT_TYPE))
        {

            return this.getNotes ();

        }

        if (objType.equals (Scene.OBJECT_TYPE))
        {

            return this.getScenes ();

        }

        return null;

    }

    public void addNote (Note n)
    {

        for (Note _n : this.getNotes ())
        {

            if (_n.equals (n))
            {

                return;

            }

        }

        if ((n.getChapter () != null)
            &&
            (n.getChapter () != this)
           )
        {

            n.getChapter ().removeNote (n);

        }

        super.addNote (n);

    }

    public void addScene (Scene s)
    {

        // The contains call is unreliable... seems to fail sometimes...
        for (Scene _s : this.scenes)
        {

            if (_s.equals (s))
            {

                return;

            }

        }

        if ((s.getChapter () != null)
            &&
            (!s.getChapter ().equals (this))
           )
        {

            s.getChapter ().removeScene (s);

        }

        s.setChapter (this);
/*
        for (OutlineItem i : s.getOutlineItems ())
        {

            i.setChapter (this);

        }
*/
        this.scenes.add (s);

    }

    public void removeScene (Scene s)
    {

        s.getOutlineItems ().clear ();

        this.scenes.remove (s);

        s.dispose ();

    }

    public ObservableSet<Scene> getScenes ()
    {

        return this.scenes;
/*
        Set<Scene> items = new TreeSet (new ChapterItemSorter ());

        items.addAll (this.scenes);

        return items;
*/
    }

    public ObservableSet<OutlineItem> getOutlineItems ()
    {

        return this.outlineItems;

/*
        Set<OutlineItem> items = new TreeSet (new ChapterItemSorter ());

        items.addAll (this.outlineItems);

        return items;
*/
    }
/*
    private void setOutlineItems (List<OutlineItem> l)
    {

        this.outlineItems = l;

    }
*/
    public void removeChapterItem (ChapterItem i)
    {

        if (i.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.removeOutlineItem ((OutlineItem) i);

        }

        if (i.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.removeNote ((Note) i);

        }

        if (i.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.removeScene ((Scene) i);

        }

    }

    public void removeOutlineItem (OutlineItem i)
    {

        this.outlineItems.remove (i);

    }

    public void addOutlineItem (OutlineItem i)
    {

        if (i.getScene () != null)
        {

            i.getScene ().removeOutlineItem (i);

            i.setScene (null);

        }

        if ((i.getChapter () != null)
            &&
            (i.getChapter () != this)
           )
        {

            i.getChapter ().removeOutlineItem (i);

        }

        i.setChapter (this);

        for (OutlineItem oi : this.outlineItems)
        {

            if (oi.equals (i))
            {

                return;

            }

        }

        this.outlineItems.add (i);

    }

    public String getChapterText ()
    {

        return (this.text != null ? this.text.getText () : null);

    }

    public StringWithMarkup getText ()
    {

        return this.text;

    }

    public void setText (StringWithMarkup t)
    {

        if (t != null)
        {

            String _t = t.getText ();

            if (_t != null)
            {

                _t = com.quollwriter.text.TextUtilities.sanitizeText (_t);
/*
                _t = StringUtils.replaceString (_t,
                                                String.valueOf ('\r'),
                                                "");
  */
                t.update (_t,
                          t.getMarkup ());

            }

        }

        this.text = t;

        this.setLastModified (new Date ());

    }

    public StringWithMarkup getGoals ()
    {

        return this.goals;

    }

    public void setGoals (StringWithMarkup t)
    {

        if ((t != null)
            &&
            (t.hasText ())
           )
        {

            t.update (Utils.replaceString (t.getText (),
                                                 String.valueOf ('\r'),
                                                 ""),
                      t.getMarkup ());

        }

        UserConfigurableObjectField f = this.getLegacyField (GOALS_LEGACY_FIELD_ID);

        if (f == null)
        {

            UserConfigurableObjectTypeField type = this.getLegacyTypeField (GOALS_LEGACY_FIELD_ID);

            f = new UserConfigurableObjectField (type);

            f.setParent (this);
            this.fields.add (f);

        }

        try
        {

            f.setValue (JSONEncoder.encode (t));

        } catch (Exception e) {

            throw new IllegalArgumentException ("Unable to json encode string with markup: " +
                                                t,
                                                e);

        }

        this.goals = t;

        this.setLastModified (new Date ());

    }

    public ObjectProperty<StringWithMarkup> planProperty ()
    {

        return this.planProp;

    }

    public StringWithMarkup getPlan ()
    {

        return this.plan;

    }

    public void setPlan (StringWithMarkup t)
    {

        if ((t != null)
            &&
            (t.hasText ())
           )
        {

            t.update (Utils.replaceString (t.getText (),
                                                 String.valueOf ('\r'),
                                                 ""),
                      t.getMarkup ());

        }

        UserConfigurableObjectField f = this.getLegacyField (PLAN_LEGACY_FIELD_ID);

        if (f == null)
        {

            UserConfigurableObjectTypeField type = this.getLegacyTypeField (PLAN_LEGACY_FIELD_ID);

            f = new UserConfigurableObjectField (type);

            f.setParent (this);
            this.fields.add (f);

        }

        try
        {

            f.setValue (JSONEncoder.encode (t));

        } catch (Exception e) {

            throw new IllegalArgumentException ("Unable to json encode string with markup: " +
                                                t,
                                                e);

        }

        this.plan = t;
        this.planProp.setValue (t);

        this.setLastModified (new Date ());

    }
        /*
    @Override
    public String toString ()
    {

        return Environment.formatObjectToStringProperties (this);

    }
    */
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "editPosition",
                                    this.editPosition);
        this.addToStringProperties (props,
                                    "editComplete",
                                    this.editComplete);
        this.addToStringProperties (props,
                                    "textLength",
                                    this.getChapterLength ());
        this.addToStringProperties (props,
                                    "book",
                                    this.book);
        this.addToStringProperties (props,
                                    "scenes",
                                    this.scenes.size ());
        this.addToStringProperties (props,
                                    "outlineItems",
                                    this.outlineItems.size ());

    }

    public void setBook (Book b)
    {

        this.book = b;

        this.setParent (b);

    }

    public Book getBook ()
    {

        return this.book;

    }

    public Scene getNextScene (Scene s)
    {

        TreeSet<Scene> items = new TreeSet (new ChapterItemSorter ());

        items.addAll (this.scenes);

        return items.higher (s);

    }

    public Scene getLastScene (int position)
    {

        List<Scene> scs = new ArrayList<> (this.getScenes ());

        Collections.sort (scs,
                          new ChapterItemSorter ());
        Collections.reverse (scs);

        for (Scene s : scs)
        {

            if (position > s.getPosition ())
            {

                return s;

            }

        }

        return null;

    }

/*
TODO Remove
    public Scene getLastScene (int position)
    {

        Scene last = null;

        for (Scene s : this.getScenes ())
        {

            if (s.getPosition () >= position)
            {

                return last;

            } else
            {

                last = s;

            }

        }

        return last;

    }
*/
    public List<Note> getNotesBetween (int start,
                                       int end)
    {

        List<Note> newNotes = new ArrayList ();

        for (Note n : this.getNotes ())
        {

            if ((n.getPosition () >= start) &&
                (n.getPosition () <= end))
            {

                newNotes.add (n);

            }

        }

        return newNotes;

    }

    public void setTextEditPosition (Position p)
    {

        this.textEditPos = p;

        if (p == null)
        {

            this.editPosition = -1;
            this.editPositionProp.setValue (-1);

        }

    }

    public IntegerProperty editPositionProperty ()
    {

        return this.editPositionProp;

    }

    public void setEditPosition (int p)
    {

        int oldPos = this.editPosition;

        this.editPositionProp.setValue (p);

        this.editPosition = p;

        //this.textEditPos = null;

        this.firePropertyChangedEvent (EDIT_POSITION,
                                       oldPos,
                                       p);
                                       //this.editPosition);

    }

    public int getEditPosition ()
    {
/*
        if (this.textEditPos != null)
        {

            return this.textEditPos.getOffset ();

        }
*/
        return this.editPositionProp.getValue ();

    }

    public int getChapterLength ()
    {

        if (this.text == null)
        {

            return 0;

        }

        if (this.text.getText () == null)
        {

            return 0;

        }

        return this.text.getText ().length ();

    }

    public boolean isPositionAtChapterEnd (int p)
    {

        int cl = this.getChapterLength ();

        if (cl == 0)
        {

            return p == cl;

        }

        return p >= cl - 1;

    }

}
