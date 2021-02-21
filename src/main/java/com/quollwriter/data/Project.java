package com.quollwriter.data;

import java.io.*;
import java.nio.file.*;

import java.util.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.beans.value.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import org.dom4j.*;
import org.dom4j.tree.*;

public class Project extends NamedObject
{

    public class XMLConstants
    {

        public static final String projects = "projects";
        public static final String directory = "directory";
        public static final String name = "name";
        public static final String lastEdited = "lastEdited";
        public static final String encrypted = "encrypted";
        public static final String noCredentials = "noCredentials";
        public static final String type = "type";
        public static final String id = "id";
        public static final String forEditor = "forEditor";
        public static final String editDueDate = "editDueDate";

    }

    public static final String LAST_EDITED = "lastEdited";
    public static final String PROJECT_DIRECTORY = "projectDirectory";
    public static final String BACKUP_DIRECTORY = "backupDirectory";

    public static final String OBJECT_TYPE = "project";
    public static final String WORDCOUNTS_OBJECT_TYPE = "wordcounts";

    // TODO: Change these to be an enum
    public static final String NORMAL_PROJECT_TYPE = "normal";
    public static final String WARMUPS_PROJECT_TYPE = "warmups";
    public static final String EDITOR_PROJECT_TYPE = "editor";

    public static final String BOOK_ADDED = "book_added";

    private List<Book>         books = new ArrayList<> ();
    private int                lastBookId = 0;
    //private File               projectDirectory = null;
    private ObjectProperty<File> backupDirectoryProp = null;
    private ObjectProperty<File> projectDirectoryProp = null;
    //private File               backupDirectory = null;
    //private Date               lastEdited = null;
    private boolean            backup = false;
    private boolean            encrypted = false;
    private String             backupVersion = null;
    /*
    private List<QCharacter>   characters = new ArrayList ();
    private List<Location>     locations = new ArrayList ();
    private List<QObject>      objects = new ArrayList ();
    private List<ResearchItem> researchItems = new ArrayList ();
    */
    private ObservableSet<IdeaType>     ideaTypes = FXCollections.observableSet (new LinkedHashSet<> ());

    private ObservableMap<UserConfigurableObjectType, ObservableSet<Asset>> assets = FXCollections.observableMap (new HashMap<> ());
    private String             filePassword = null;
    private boolean            noCredentials = false;
    private String             type = Project.NORMAL_PROJECT_TYPE;
    //private String             editorsProjectId = null;
    //private EditorProject      editorProject = null;

    // TODO: Need a new object to encapsulate this stuff
    //private EditorEditor       forEditor = null;
    private ObservableSet<ProjectEditor> projectEditors = FXCollections.observableSet (new LinkedHashSet<> ());
    private ObjectProperty<ProjectVersion> projVerProp = new SimpleObjectProperty<> ();

    private Map<Tag, ObservableSet<NamedObject>> taggedObjects = new HashMap<> ();

    private StringProperty toolbarLocationProp = null;
    private ObjectProperty<Date> lastEditedProp = null;
    private SimpleIntegerProperty chapterCountProp = null;
    private IntegerProperty backupsToKeepCountProp = null;
    private IntegerProperty autoBackupsTimeProp = null;
    private BooleanProperty autoBackupsEnabledProp = null;

    public Project (Element pEl)
                    throws  Exception
    {

        this ();

        String name = DOM4JUtils.childElementContent (pEl,
                                                         XMLConstants.name);
        String type = pEl.attributeValue (XMLConstants.type);

        String id = pEl.attributeValue (XMLConstants.id);

        if (id != null)
        {

            this.setId (id);

        }

        String directory = DOM4JUtils.childElementContent (pEl,
                                                              XMLConstants.directory);

        boolean encrypted = DOM4JUtils.attributeValueAsBoolean (pEl,
                                                                  XMLConstants.encrypted,
                                                                  false);

        boolean noCredentials = false;

        if (pEl.attribute (XMLConstants.noCredentials) != null)
        {

            noCredentials = DOM4JUtils.attributeValueAsBoolean (pEl,
                                                                  XMLConstants.noCredentials);

        }

        String d = pEl.attributeValue (XMLConstants.lastEdited);

        File dir = new File (directory);

        this.setName (name);

        this.setProjectDirectory (dir);
        this.setEncrypted (encrypted);
        this.setNoCredentials (noCredentials);

        if (type != null)
        {

            this.setType (type);

            if (this.isEditorProject ())
            {

                this.projVerProp.setValue (new ProjectVersion ());

                String dueDate = pEl.attributeValue (XMLConstants.editDueDate);

                if (dueDate != null)
                {

                    // TODO: Fix this otherwise I will go to hell...
                    this.projVerProp.getValue ().setDueDate (new Date (Long.parseLong (dueDate)));

                }

                String editorEmail = DOM4JUtils.childElementContent (pEl,
                                                                        XMLConstants.forEditor);

                if (editorEmail == null)
                {

                    throw new GeneralException ("Expected to find a child element: " +
                                                XMLConstants.forEditor +
                                                ", indicating who the project is being edited for.");

                }

                // Get the editor.
                EditorEditor ed = EditorsEnvironment.getEditorByEmail (editorEmail);

                // If we are in debug mode then allow a null editor through.  This is to allow
                // testing of the send/receive cycle without having to resort to handling two
                // different accounts or having per user projects.xml files.
                if ((!Environment.isDebugModeEnabled ())
                    &&
                    (ed == null)
                   )
                {

                    throw new GeneralException ("Expected to find editor with email: " +
                                                editorEmail);

                } else {

                    ed = new EditorEditor ();
                    ed.setKey (Long.valueOf (0));
                    ed.setEmail (editorEmail);

                }

                this.setForEditor (editorEmail);

                //this.forEditor = ed;

            }

        }

        if (!d.equals (""))
        {

            try
            {

                this.setLastEdited (new Date (Long.parseLong (d)));

            } catch (Exception e)
            {

                // Ignore it.

            }

        }

    }

    public Project()
    {

        super (Project.OBJECT_TYPE);

        this.init ();

    }

    public Project(String name)
    {

        super (Project.OBJECT_TYPE,
               name);

        this.init ();

    }

    private void init ()
    {

        this.projectDirectoryProp = new SimpleObjectProperty<> ();
        this.backupDirectoryProp = new SimpleObjectProperty<> ();
        this.chapterCountProp = new SimpleIntegerProperty ();

        this.addSetChangeListener (Environment.getUserConfigurableObjectTypes (),
                                   ev ->
        {

            if (ev.wasAdded ())
            {

                this.assets.put (ev.getElementAdded (),
                                 FXCollections.observableSet (new LinkedHashSet<> ()));

            }

            if (ev.wasRemoved ())
            {

                this.assets.remove (ev.getElementRemoved ());

            }

        });

        Environment.getUserConfigurableObjectTypes ().stream ()
            .forEach (t ->
            {

                this.assets.put (t,
                                 FXCollections.observableSet (new LinkedHashSet<> ()));

            });

        this.addSetChangeListener (Environment.getAllTags (),
                                   ev ->
        {

            Tag t = ev.getElementRemoved ();

            if (t != null)
            {

                this.taggedObjects.remove (t);

            }

        });

        this.lastEditedProp = new SimpleObjectProperty<> ();

    }

    public void setBackupsToKeepCount (int c)
    {

        this.backupsToKeepCountProperty ().setValue (c);

    }

    public int getBackupsToKeepCount ()
    {

        return this.backupsToKeepCountProperty ().getValue ();

    }

    public IntegerProperty backupsToKeepCountProperty ()
    {

        if (this.backupsToKeepCountProp == null)
        {

            this.backupsToKeepCountProp = new SimpleIntegerProperty ();

            com.gentlyweb.properties.AbstractProperty p = this.getProperties ().getPropertyObj (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME);

            if (p != null)
            {

                // Has the property, try and use it.
                if (p instanceof com.gentlyweb.properties.IntegerProperty)
                {

                    this.backupsToKeepCountProp.setValue (((com.gentlyweb.properties.IntegerProperty) p).getInteger ());

                } else {

                    com.gentlyweb.properties.StringProperty sp = (com.gentlyweb.properties.StringProperty) p;

                    String v = sp.getValue ();

                    int iv = -1;

                    // Pre 2.6.5
                    if (!v.equals ("All"))
                    {

                        try
                        {

                            iv = Integer.parseInt (v);

                        } catch (Exception e) {

                            // Ignore.

                        }

                    }

                    this.backupsToKeepCountProp.setValue (iv);

                }

            } else {

                this.backupsToKeepCountProp.setValue (UserProperties.getBackupsToKeepCount ());

            }

            this.backupsToKeepCountProp.addListener ((pr, oldv, newv) ->
            {

                try
                {

                    this.setProperty (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME,
                                      newv.intValue ());

                } catch (Exception e) {

                    Environment.logError ("Unable to set property to: " +
                                          newv,
                                          e);

                }

            });

        }

        return this.backupsToKeepCountProp;

    }

    public int getAutoBackupsTime ()
    {

        return this.autoBackupsTimeProperty ().getValue ();

    }

    public void setAutoBackupsTime (long t)
    {

        this.autoBackupsTimeProperty ().setValue ((int) t);

    }

    public IntegerProperty autoBackupsTimeProperty ()
    {

        if (this.autoBackupsTimeProp == null)
        {

            this.autoBackupsTimeProp = new SimpleIntegerProperty ();

            com.gentlyweb.properties.AbstractProperty p = this.getProperties ().getPropertyObj (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME);

            if (p != null)
            {

                // Has the property, try and use it.
                if (p instanceof com.gentlyweb.properties.IntegerProperty)
                {

                    this.autoBackupsTimeProp.setValue (((com.gentlyweb.properties.IntegerProperty) p).getInteger ());

                } else {

                    this.autoBackupsTimeProp.setValue (Utils.getTimeAsMillis (this.getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME)));

                }

            } else {

                this.autoBackupsTimeProp.setValue (UserProperties.getAutoBackupsTime ());

            }

            this.autoBackupsTimeProp.addListener ((pr, oldv, newv) ->
            {

                try
                {

                    this.setProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                                      newv.intValue ());

                } catch (Exception e) {

                    Environment.logError ("Unable to update property to: " +
                                          newv,
                                          e);

                }

            });

        }

        return this.autoBackupsTimeProp;

    }

    public boolean isAutoBackupsEnabled ()
    {

        return this.autoBackupsEnabledProperty ().getValue ();

    }

    public void setAutoBackupsEnabled (boolean v)
    {

        this.autoBackupsEnabledProperty ().setValue (v);

    }

    public BooleanProperty autoBackupsEnabledProperty ()
    {

        if (this.autoBackupsEnabledProp == null)
        {

            this.autoBackupsEnabledProp = new SimpleBooleanProperty ();

            boolean p = this.getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);

            this.autoBackupsEnabledProp.setValue (p);

            this.autoBackupsEnabledProp.addListener ((pr, oldv, newv) ->
            {

                try
                {

                    this.setProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME,
                                      newv);

                } catch (Exception e) {

                    Environment.logError ("Unable to update property to: " +
                                          newv,
                                          e);

                }

            });

        }

        return this.autoBackupsEnabledProp;

    }


    public StringProperty toolbarLocationProperty ()
    {

        if (this.toolbarLocationProp == null)
        {

            this.toolbarLocationProp = new SimpleStringProperty ();
            this.toolbarLocationProp.setValue (this.getProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME));

        }

        return this.toolbarLocationProp;

    }

    public ProjectEditor getProjectEditor (EditorEditor ed)
    {

        if (this.projectEditors == null)
        {

            return null;

        }

        for (ProjectEditor pe : this.projectEditors)
        {

            if (pe.getEditor () == ed)
            {

                return pe;

            }

        }

        return null;

    }

    public void addProjectEditor (ProjectEditor pe)
    {

        this.projectEditors.add (pe);

    }

    public void removeProjectEditor (ProjectEditor pe)
    {

        this.projectEditors.remove (pe);

    }

    public boolean isProjectEditor (EditorEditor ed)
    {

        for (ProjectEditor pe : this.projectEditors)
        {

            if (pe.getEditor () == ed)
            {

                return true;

            }

        }

        return false;

    }

    public ObservableSet<ProjectEditor> getProjectEditors ()
    {

        return this.projectEditors;

    }

    public void setProjectEditors (Collection<ProjectEditor> eds)
    {

        if (eds == null)
        {

            return;

        }

        this.projectEditors.clear ();
        this.projectEditors.addAll (eds);

    }

    public void setForEditor (String editorEmail) //EditorEditor ed)
    {

/*
        if ((this.forEditor != null)
            &&
            (ed == null)
           )
        {

            throw new IllegalArgumentException ("Cannot remove forEditor once it has been set.");

        }

        if ((this.forEditor != null)
            &&
            (!this.forEditor.getEmail ().equals (ed.getEmail ()))
           )
        {

            throw new IllegalArgumentException ("Cannot change the forEditor once it has been set.");

        }

        this.forEditor = ed;
*/
        try
        {

            this.setProperty (Constants.FOR_EDITOR_EMAIL_PROPERTY_NAME,
                              editorEmail);
                              //this.forEditor.getEmail ());

        } catch (Exception e) {

            // Not really the correct type of exception to throw but it shouldn't be
            // checked or handled "properly" either since it should always happen, this
            // is just the weird edge case when something terrible goes wrong.
            throw new IllegalArgumentException ("Unable to set the for editor",
                                                e);

        }

    }

    public String getEditResponseMessage ()
    {

        return this.getProperty (Constants.EDITOR_RESPONSE_MESSAGE_PROPERTY_NAME);

    }

    public void setEditResponseMessage (String m)
    {

        try
        {

            this.setProperty (Constants.EDITOR_RESPONSE_MESSAGE_PROPERTY_NAME,
                              m);

        } catch (Exception e) {

            Environment.logError ("Unable to set response message: " +
                                  m,
                                  e);

        }

    }

    public void setProjectVersion (ProjectVersion pv)
    {

        this.projVerProp.setValue (pv);

    }

    public ProjectVersion getProjectVersion ()
    {

        return this.projVerProp.getValue ();

    }

    public ObjectProperty<ProjectVersion> projectVersionProperty ()
    {

        return this.projVerProp;

    }

    public EditorEditor getForEditor ()
    {

        if (this.isEditorProject ())
        {

            // Get the editor email.
            String edEmail = this.getProperty (Constants.FOR_EDITOR_EMAIL_PROPERTY_NAME);

            if (edEmail == null)
            {

                // This is a strange situation, what to do?
                return null;

            }

            return EditorsEnvironment.getEditorByEmail (edEmail);

        }

        return null;

        //return this.forEditor;

    }

    public boolean isEditorProject ()
    {

        return this.type.equals (Project.EDITOR_PROJECT_TYPE);

    }

    public boolean isWarmupsProject ()
    {

        return this.type.equals (Project.WARMUPS_PROJECT_TYPE);

    }

    public boolean isNormalProject ()
    {

        return this.type.equals (Project.NORMAL_PROJECT_TYPE);

    }

    private void updateChapterCount ()
    {

        int c = 0;

        if (this.books != null)
        {

            for (Book b : this.books)
            {

                c += b.getChapters ().size ();

            }

        }

        this.chapterCountProp.setValue (c);

    }

    public int getChapterCount ()
    {

        return this.chapterCountProp.getValue ();

    }

    public IntegerProperty chapterCountProperty ()
    {

        return this.chapterCountProp;

    }

    public ReadabilityIndices getAllProjectReadabilityIndices ()
    {

        ReadabilityIndices ri = new ReadabilityIndices ();

        if (this.books == null)
        {

            return ri;

        }

        for (Book b : this.books)
        {

            for (Chapter c : b.getChapters ())
            {

                ri.add (c.getChapterText ());
            }

        }

        return ri;

    }

    public int getEditedWordCount ()
    {

        if (this.books == null)
        {

            return 0;

        }

        final StringBuilder buf = new StringBuilder ();

        int editComplete = 0;

        for (Book b : this.books)
        {

            for (Chapter c : b.getChapters ())
            {

                if (c.getEditPosition () > 0)
                {

                    if (buf.length () > 0)
                    {

                        buf.append (" ");

                    }

                    String t = c.getChapterText ();

                    if (t == null)
                    {

                        continue;

                    }

                    if (c.getEditPosition () <= t.length ())
                    {

                        buf.append (t.substring (0,
                                                 c.getEditPosition ()));

                    }

                }

            }

        }

        if (buf.length () > 0)
        {

            ChapterCounts allc = new ChapterCounts (buf.toString ());

            return allc.getWordCount ();

        }

        return 0;

    }

    public int getWordCount ()
    {

        int c = 0;

        if (this.books == null)
        {

            return c;

        }

        for (Book b : this.books)
        {

            c += b.getChapterWordCount ();

        }

        return c;

    }
    /*
    public void setEditorProject (EditorProject p)
    {

        this.editorProject = p;

        if (p != null)
        {

            p.setProject (this);

        }

    }

    public EditorProject getEditorProject ()
    {

        return this.editorProject;

    }
    */
    /*
    public void setEditorsProjectId (String id)
    {

        this.editorsProjectId = id;

    }

    public String getEditorsProjectId ()
    {

        return this.editorsProjectId;

    }
    */

    public String getType ()
    {

        return this.type;

    }

    public void setType (String t)
    {

        this.type = t;

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public NamedObject getObjectById (Class  ofType,
                                      String id)
    {

        Set<NamedObject> objs = this.getAllNamedChildObjects (ofType);

        for (NamedObject o : objs)
        {

            if (o.getId ().equals (id))
            {

                return o;

            }

        }

        return null;

    }

    public static Set<NamedObject> getObjectsContaining (String  s,
                                                         Project p)
    {

        Set<NamedObject> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : p.getAllNamedChildObjects ())
        {

            if (n.contains (s))
            {

                ret.add (n);

            }

        }

        return ret;

    }

    public Set<Asset> getAssetsContaining (String  s)
    {

        Set<Asset> ret = new TreeSet<> (NamedObjectSorter.getInstance ());

        for (NamedObject n : this.getAllNamedChildObjects (Asset.class))
        {

            if (n.contains (s))
            {

                ret.add ((Asset) n);

            }

        }

        return ret;

    }

    public Set<Asset> getAssetsContaining (String                     s,
                                           UserConfigurableObjectType limitTo)
    {

        Set<Asset> ret = new TreeSet<> (NamedObjectSorter.getInstance ());

        if (limitTo != null)
        {

            if (!limitTo.isAssetObjectType ())
            {

                return ret;

            }

        }

        for (NamedObject n : this.getAllNamedChildObjects (limitTo))
        {

            if (n.contains (s))
            {

                ret.add ((Asset) n);

            }

        }

        return ret;

    }

    public Set<Note> getNotesContaining (String  s)
    {

        Set<Note> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : this.getAllNamedChildObjects (Note.class))
        {

            if (n.contains (s))
            {

                ret.add ((Note) n);

            }

        }

        return ret;

    }

    public Set<OutlineItem> getOutlineItemsContaining (String  s)
    {

        Set<OutlineItem> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : this.getAllNamedChildObjects (OutlineItem.class))
        {

            if (n.contains (s))
            {

                ret.add ((OutlineItem) n);

            }

        }

        return ret;

    }

    public Set<Scene> getScenesContaining (String  s)
    {

        Set<Scene> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : this.getAllNamedChildObjects (Scene.class))
        {

            if (n.contains (s))
            {

                ret.add ((Scene) n);

            }

        }

        return ret;

    }

    public ObservableSet<NamedObject> getAllObjectsWithTag (Tag tag)
    {

        ObservableSet<NamedObject> objs = this.taggedObjects.get (tag);

        if (objs != null)
        {

            return objs;

        }

        objs = FXCollections.observableSet (this.getAllNamedChildObjects ().stream ()
            .filter (o -> o.hasTag (tag))
            .collect (Collectors.toSet ()));

        this.taggedObjects.put (tag,
                                objs);

        return objs;

    }

    public Set<NamedObject> getAllNamedChildObjects (UserConfigurableObjectType withType)
    {

        Set<NamedObject> ret = this.getAllNamedChildObjects ();

        Iterator<NamedObject> iter = ret.iterator ();

        while (iter.hasNext ())
        {

            NamedObject o = iter.next ();

            if (!(o instanceof UserConfigurableObject))
            {

                iter.remove ();

                continue;

            }

            UserConfigurableObject uo = (UserConfigurableObject) o;

            if (!uo.getUserConfigurableObjectType ().equals (withType))
            {

                iter.remove ();

            }

        }

        return ret;

    }

    public Set<NamedObject> getAllNamedChildObjects (Class ofType)
    {

        Set<NamedObject> ret = this.getAllNamedChildObjects ();

        Iterator<NamedObject> iter = ret.iterator ();

        while (iter.hasNext ())
        {

            if (!ofType.isAssignableFrom (iter.next ().getClass ()))
            {

                iter.remove ();

            }

        }

        return ret;

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        Set<NamedObject> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (Book b : this.books)
        {

            // TODO Is this correct?  YOu can't do anything with a book so probably...
            //ret.add (b);

            ret.addAll (b.getAllNamedChildObjects ());

        }

        for (UserConfigurableObjectType t : this.assets.keySet ())
        {

            Set<Asset> as = this.assets.get (t);

            for (Asset a : as)
            {

                ret.add (a);

                ret.addAll (a.getAllNamedChildObjects ());

            }

        }

        for (IdeaType it : this.ideaTypes)
        {

            ret.add (it);

            ret.addAll (it.getAllNamedChildObjects ());

        }

        return ret;

    }

    public boolean hasObject (DataObject d)
    {

        if (d instanceof IdeaType)
        {

            return this.getIdeaType (d.getKey ()) != null;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if (c.getBook () == null)
            {

                return false;

            }

            return c.getBook ().getChapterByKey (d.getKey ()) != null;

        }

        if (d instanceof Asset)
        {

            Asset a = (Asset) d;

            Set<Asset> as = this.assets.get (a.getUserConfigurableObjectType ());

            if (as != null)
            {

                for (Asset _a : as)
                {

                    if (_a == a)
                    {

                        return true;

                    }

                }

            }

            return false;

        }

        return false;

    }

    public void removeObject (DataObject d)
    {

        if (d instanceof IdeaType)
        {

            this.getIdeaTypes ().remove (d);

        }

        if (d instanceof Asset)
        {

            Asset a = (Asset) d;

            Set<Asset> as = this.assets.get (a.getUserConfigurableObjectType ());

            if (as != null)
            {

                as.remove (a);

            }

        }

        if (d instanceof NamedObject)
        {

            NamedObject n = (NamedObject) d;

            for (Tag t : new ArrayList<> (n.getTags ()))
            {

                this.removeTagFromObject (t,
                                          n);

            }

        }

    }

    public boolean hasAsset (Asset a)
    {

        return this.getAssetByName (a.getName (),
                                    a.getUserConfigurableObjectType ()) != null;

    }

    public Set<NamedObject> getAllNamedObjectsByName (String n)
    {

        Set<NamedObject> ret = new LinkedHashSet ();

        Set<NamedObject> objs = this.getAllNamedChildObjects ();

        for (NamedObject o : objs)
        {

            Set<String> names = o.getAllNames ();

            for (String name : names)
            {

                if (n.equalsIgnoreCase (name))
                {

                    ret.add (o);

                }

            }

        }

        return ret;

    }

    public Set<Asset> getAllAssetsByName (String                     n,
                                          UserConfigurableObjectType type)
    {

        Set<Asset> assets = new LinkedHashSet<> ();

        if (type != null)
        {

            Asset as = this.getAssetByName (n,
                                            type);

            if (as != null)
            {

                assets.add (as);

            }

            return assets;

        }

        for (UserConfigurableObjectType t : this.assets.keySet ())
        {

            Asset as = this.getAssetByName (n,
                                            t);

            if (as != null)
            {

                assets.add (as);

            }

        }

        return assets;

    }

    public Asset getAssetByName (String                     n,
                                 UserConfigurableObjectType type)
    {

        Set<Asset> as = this.assets.get (type);

        if (as == null)
        {

            return null;

        }

        for (Asset a : as)
        {

            Set<String> names = a.getAllNames ();

            for (String name : names)
            {

                if (n.equalsIgnoreCase (name))
                {

                    return a;

                }

            }

        }

        return null;

    }

    private Asset getAssetByName_Int (List<? extends Asset> assets,
                                      String                n)
    {

        n = n.toLowerCase ();

        for (Asset a : assets)
        {

            if (a.getName ().toLowerCase ().equals (n))
            {

                return a;

            }

        }

        return null;

    }

    private Set<? extends Asset> getAllAssetsByName_Int (List<? extends Asset> assets,
                                                         String                n)
    {

        Set<Asset> matched = new LinkedHashSet ();

        n = n.toLowerCase ();

        for (Asset a : assets)
        {

            if (a.getName ().equalsIgnoreCase (n))
            {

                matched.add (a);

                continue;

            }

            List<String> aliases = a.getAliasesAsList ();

            if (aliases != null)
            {

                for (String al : aliases)
                {

                    if (al.equalsIgnoreCase (n))
                    {

                        matched.add (a);

                        continue;

                    }

                }

            }

        }

        return matched;

    }

    public Set<UserConfigurableObjectType> getAssetTypes ()
    {

        return this.assets.keySet ();

    }

    public ObservableSet<Asset> getAssets (UserConfigurableObjectType type)
    {

        return this.assets.get (type);

    }

    public ObservableMap<UserConfigurableObjectType, ObservableSet<Asset>> getAssets ()
    {

        return this.assets;

    }

/*
    public Set<QCharacter> getAllCharactersByName (String n)
    {

        return (Set<QCharacter>) this.getAllAssetsByName_Int (this.characters,
                                                              n);

    }

    public Set<Location> getAllLocationsByName (String n)
    {

        return (Set<Location>) this.getAllAssetsByName_Int (this.locations,
                                                            n);

    }

    public Set<QObject> getAllQObjectsByName (String n)
    {

        return (Set<QObject>) this.getAllAssetsByName_Int (this.objects,
                                                           n);

    }

    public Set<ResearchItem> getAllResearchItemsByName (String n)
    {

        return (Set<ResearchItem>) this.getAllAssetsByName_Int (this.researchItems,
                                                                n);

    }
*/
/*
    public QCharacter getCharacterByName (String n)
    {

        return (QCharacter) this.getAssetByName_Int (this.characters,
                                                     n);

    }

    public Location getLocationByName (String n)
    {

        return (Location) this.getAssetByName_Int (this.locations,
                                                   n);

    }

    public QObject getQObjectByName (String n)
    {

        return (QObject) this.getAssetByName_Int (this.objects,
                                                  n);

    }

    public ResearchItem getResearchItemByName (String n)
    {

        return (ResearchItem) this.getAssetByName_Int (this.researchItems,
                                                       n);

    }
*/
    public boolean isNoCredentials ()
    {

        return this.noCredentials;

    }

    public void setNoCredentials (boolean v)
    {

        this.noCredentials = v;

    }

    public void setFilePassword (String p)
    {

        this.filePassword = p;
        this.setEncrypted (p != null);

    }

    public String getFilePassword ()
    {

        return this.filePassword;

    }

    public boolean isEncrypted ()
    {

        return this.encrypted;

    }

    public void setEncrypted (boolean v)
    {

        this.encrypted = v;

    }

    public Date getLastEdited ()
    {

        return this.lastEditedProp.getValue ();

    }

    public ObjectProperty<Date> lastEditedProperty ()
    {

        return this.lastEditedProp;

    }

    public void setLastEdited (Date d)
    {

        Date oldDate = this.lastEditedProp.getValue ();

        this.lastEditedProp.setValue (d);

        this.firePropertyChangedEvent (Project.LAST_EDITED,
                                       oldDate,
                                       this.lastEditedProp.getValue ());

    }

    public ObjectProperty<File> projectDirectoryProperty ()
    {

        return this.projectDirectoryProp;

    }

    public File getProjectDirectory ()
    {

        return this.projectDirectoryProp.getValue ();

    }

    public void setProjectDirectory (File dir)
    {

        File oldDir = this.projectDirectoryProp.getValue ();

        this.projectDirectoryProp.setValue (dir);

        this.firePropertyChangedEvent (Project.PROJECT_DIRECTORY,
                                       oldDir,
                                       this.projectDirectoryProp.getValue ());

    }

    // TODO Remove
    public void saveToFilesDirectory (File   file,
                                      String fileName)
                               throws GeneralException
    {

        if ((file == null)
            ||
            (!file.exists ())
            ||
            (file.isDirectory ())
           )
        {

            return;

        }

        File dir = this.getFilesDirectory ();

        dir.mkdirs ();

        try
        {

            Utils.createQuollWriterDirFile (dir);

        } catch (Exception e) {

            throw new GeneralException ("Unable to create dir: " + dir,
                                        e);

        }

        File f = new File (dir,
                           fileName);

        try
        {

            Files.copy (file.toPath (),
                        f.toPath ());

        } catch (Exception e) {

            throw new GeneralException ("Unable to copy file: " + file + " to: " + f,
                                        e);

        }

    }

    public void saveToFilesDirectory (Path   file,
                                      String fileName)
                               throws GeneralException
    {

        if ((file == null)
            ||
            (Files.notExists (file))
            ||
            (Files.isDirectory (file))
           )
        {

            return;

        }

        Path dir = this.getFilesDirectory ().toPath ();

        try
        {

            Files.createDirectories (dir);

        } catch (Exception e) {

            throw new GeneralException ("Unable to create dirs for: " + dir,
                                        e);

        }

        Utils.createQuollWriterDirFile (dir);

        Path f = dir.resolve (fileName);

        try
        {

            Files.copy (file,
                        f,
                        StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {

            throw new GeneralException ("Unable to copy file: " + file + " to: " + f,
                                        e);

        }

    }

    public void deleteFile (String fileName)
    {

        this.getFile (fileName).delete ();

    }

    public File getFile (String fileName)
    {

        return new File (this.getFilesDirectory (),
                         fileName);

    }

    public File getFilesDirectory ()
    {

        return new File (this.projectDirectoryProp.getValue (),
                         Constants.PROJECT_FILES_DIR_NAME);

    }

    public File getBackupDirectory ()
    {

        File d = this.backupDirectoryProp.getValue ();

        if (d == null)
        {

            d = new File (this.getProjectDirectory (),
                          "versions");
            this.backupDirectoryProp.setValue (d);

        }

        return this.backupDirectoryProp.getValue ();

    }

    public ObjectProperty<File> backupDirectoryProperty ()
    {

        return this.backupDirectoryProp;

    }

    public void setBackupDirectory (File dir)
    {

        File oldDir = this.backupDirectoryProp.getValue ();

        this.backupDirectoryProp.setValue (dir);

        this.firePropertyChangedEvent (Project.BACKUP_DIRECTORY,
                                       oldDir,
                                       this.backupDirectoryProp.getValue ());

    }
/*
    public List<QCharacter> getCharacters ()
    {

        return this.characters;

    }

    public List<Location> getLocations ()
    {

        return this.locations;

    }

    public List<QObject> getQObjects ()
    {

        return this.objects;

    }
*/
    public ObservableSet<IdeaType> getIdeaTypes ()
    {

        return this.ideaTypes;

    }

    public void addIdeaType (IdeaType it)
    {

        if (this.ideaTypes.contains (it))
        {

            return;

        }

        this.ideaTypes.add (it);

        it.setProject (this);

    }

    public void removeIdeaType (IdeaType it)
    {

        this.ideaTypes.remove (it);
        it.setProject (null);

    }

    public void addAsset (Asset a)
    {

        Set<Asset> as = this.assets.get (a.getUserConfigurableObjectType ());

        if (as == null)
        {

            throw new IllegalArgumentException ("Unable to find user config type: " +
                                                a.getUserConfigurableObjectType ());

        }

        a.setProject (this);

        as.add (a);

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "type",
                                    this.type);

        this.addToStringProperties (props,
                                    "projectDir",
                                    (this.projectDirectoryProp.getValue () != null ? this.projectDirectoryProp.getValue ().getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "backupDir",
                                    (this.backupDirectoryProp.getValue () != null ? this.backupDirectoryProp.getValue ().getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "filesDir",
                                    (this.getFilesDirectory () != null ? this.getFilesDirectory ().getPath () : "Not set"));

        this.addToStringProperties (props,
                                    "lastEdited",
                                    this.lastEditedProp.getValue ());
        this.addToStringProperties (props,
                                    "encrypted",
                                    this.encrypted);

        for (UserConfigurableObjectType t : this.assets.keySet ())
        {

            Set<Asset> as = this.assets.get (t);

            this.addToStringProperties (props,
                                        t.getObjectTypeId (),
                                        as.size ());

        }

        this.addToStringProperties (props,
                                    "ideaTypes",
                                    this.ideaTypes.size ());

        EditorEditor ed = this.getForEditor ();

        if (ed != null)
        {

            this.addToStringProperties (props,
                                        "forEditor",
                                        ed.getEmail ());

        }

        if (this.projectEditors != null)
        {

            this.addToStringProperties (props,
                                        "projectEditors",
                                        this.projectEditors.size ());

        }

        this.addToStringProperties (props,
                                    "projectVersion",
                                    this.projVerProp.getValue ());

    }

    public int getBookIndex (Book b)
    {

        return this.books.indexOf (b) + 1;

    }

    public List<Book> getBooks ()
    {

        return this.books;

    }

    public Chapter getChapterByKey (long k)
    {

        for (Book b : this.books)
        {

            Chapter c = b.getChapterByKey (k);

            if (c != null)
            {

                return c;

            }

        }

        return null;

    }

    public Book getBook (int i)
    {

        return (Book) this.books.get (i);

    }
/*
    public QCharacter getCharacter (Long key)
    {

        for (QCharacter c : this.characters)
        {

            if (c.getKey ().equals (key))
            {

                return c;

            }

        }

        return null;

    }
    */
/*
    public ResearchItem getResearchItem (Long key)
    {

        for (ResearchItem r : this.researchItems)
        {

            if (r.getKey ().equals (key))
            {

                return r;

            }

        }

        return null;

    }
*/
    public IdeaType getIdeaType (Long key)
    {

        for (IdeaType i : this.ideaTypes)
        {

            if (i.getKey () == key)
            {

                return i;

            }

        }

        return null;

    }
    /*
    public QObject getQObject (Long key)
    {

        for (QObject o : this.objects)
        {

            if (o.getKey ().equals (key))
            {

                return o;

            }

        }

        return null;

    }
    */
/*
    public Location getLocation (Long key)
    {


        for (Location l : this.locations)
        {

            if (l.getKey ().equals (key))
            {

                return l;

            }

        }

        return null;

    }
*/
    public Book getBook (Long key)
    {

        for (Book b : this.books)
        {

            if (b.getKey ().equals (key))
            {

                return b;

            }

        }

        return null;

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        if (r == null)
        {

            return null;

        }

        DataObject d = super.getObjectForReference (r);

        if (d != null)
        {

            return d;

        }
        /*
        if (r.getObjectType ().equals (Project.WORDCOUNTS_OBJECT_TYPE))
        {

        WordCount w = new WordCount ();
        w.setKey (r.getKey ());
        w.setObjectType (Project.WORDCOUNTS_OBJECT_TYPE);

        return w;

        }
         */

        if (r.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
        {

            r = new ObjectReference (Chapter.OBJECT_TYPE,
                                     r.getKey (),
                                     null);

        }

        for (UserConfigurableObjectType t : this.assets.keySet ())
        {

            Set<Asset> as = this.assets.get (t);

            for (Asset a : as)
            {

                d = a.getObjectForReference (r);

                if (d != null)
                {

                    return d;

                }

            }

        }
        /*
        for (QCharacter c : this.characters)
        {

            d = c.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (Location l : this.locations)
        {

            d = l.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (QObject q : this.objects)
        {

            d = q.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (ResearchItem i : this.researchItems)
        {

            d = i.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }
*/
        for (Book b : this.books)
        {

            d = b.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (IdeaType it : this.ideaTypes)
        {

            d = it.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        return null;

    }

    public void addBook (Book b)
    {

        if (this.books.contains (b))
        {

            return;

        }

        b.setProject (this);

        this.books.add (b);

        b.getChapters ().addListener ((ListChangeListener<Chapter>) ch ->
        {

            this.updateChapterCount ();

        });

        this.firePropertyChangedEvent (Project.BOOK_ADDED,
                                       null,
                                       b);

    }

    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + ((null == this.projectDirectoryProp.getValue ()) ? 0 : this.projectDirectoryProp.getValue ().hashCode ());

        return hash;

    }

    public boolean equals (Object o)
    {

        if (o == null)
        {

            return false;

        }

        if (!(o instanceof Project))
        {

            return false;

        }

        Project po = (Project) o;

        if (this.projectDirectoryProp.getValue () == null)
        {

            return false;

        }

        return this.projectDirectoryProp.getValue ().equals (po.projectDirectoryProp.getValue ());

    }

    public Element getAsElement ()
    {

        Element pEl = new DefaultElement (Project.OBJECT_TYPE);

        Element nEl = new DefaultElement (Environment.XMLConstants.name);
        pEl.add (nEl);
        nEl.add (new DefaultCDATA (this.getName ()));

        if (this.getType () == null)
        {

            this.setType (Project.NORMAL_PROJECT_TYPE);

        }

        pEl.addAttribute (XMLConstants.type,
                          this.getType ());

        if (this.getId () != null)
        {

            pEl.addAttribute (XMLConstants.id,
                              this.getId ());

        }

        Element dEl = new DefaultElement (XMLConstants.directory);
        pEl.add (dEl);
        dEl.add (new DefaultCDATA (this.getProjectDirectory ().getPath ()));

        EditorEditor ed = this.getForEditor ();

        if (ed != null)
        {

            Element fEl = new DefaultElement (XMLConstants.forEditor);
            pEl.add (fEl);
            fEl.add (new DefaultCDATA (ed.getEmail ()));

        }

        if ((this.isEditorProject ())
            &&
            (this.projVerProp.getValue () != null)
           )
        {

            Date d = this.projVerProp.getValue ().getDueDate ();

            if (d != null)
            {

                pEl.addAttribute (XMLConstants.editDueDate,
                                  d.getTime () + "");

            }

        }

        Date lastEdited = this.getLastEdited ();

        if (lastEdited != null)
        {

            pEl.addAttribute (XMLConstants.lastEdited,
                              String.valueOf (lastEdited.getTime ()));

        }

        pEl.addAttribute (XMLConstants.encrypted,
                          Boolean.valueOf (this.isEncrypted ()).toString ());

        if (this.isNoCredentials ())
        {

            pEl.addAttribute (XMLConstants.noCredentials,
                              Boolean.valueOf (this.isNoCredentials ()).toString ());

        }
/*
        if (this.editorsProjectId != null)
        {

            pEl.setAttribute (XMLConstants.editorsProjectId,
                              this.editorsProjectId);

        }
  */
        return pEl;

    }

    public String getLanguageCodeForSpellCheckLanguage ()
    {

        String lang = this.getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

        if (lang == null)
        {

            lang = "en";

            return lang;

        }

        if ((lang.equals ("English"))
            ||
            (lang.equals ("US English"))
            ||
            (lang.equals ("UK English"))
           )
        {

            lang = "en";

        }

        if (lang.equals ("Czech"))
        {

            lang = "cs";

        }

        if (lang.equals ("Dutch"))
        {

            lang = "nl";

        }

        if (lang.equals ("French"))
        {

            lang = "fr";

        }

        if (lang.equals ("German"))
        {

            lang = "de";

        }

        if (lang.equals ("Italian"))
        {

            lang = "it";

        }

        if (lang.equals ("Polish"))
        {

            lang = "pl";

        }

        if (lang.equals ("Russian"))
        {

            lang = "ru";

        }

        if (lang.equals ("Spanish"))
        {

            lang = "es";

        }

        return lang;

    }

    public void addTagToObject (Tag         tag,
                                NamedObject n)
    {

        Set<NamedObject> objs = this.taggedObjects.get (tag);

        if (objs == null)
        {

            objs = new HashSet<> ();

            this.taggedObjects.put (tag,
                                    FXCollections.observableSet (objs));

        }

        this.taggedObjects.get (tag).add (n);

    }

    public void removeTagFromObject (Tag         tag,
                                     NamedObject n)
    {

        Set<NamedObject> objs = this.taggedObjects.get (tag);

        if (objs != null)
        {

            objs.remove (n);

        }

    }

}
