package com.quollwriter.data;

import java.io.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

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
        public static final String backupService = "backupService";
        public static final String editorsProjectId = "editorsProjectId";

    }

    public static final String OBJECT_TYPE = "project";
    public static final String WORDCOUNTS_OBJECT_TYPE = "wordcounts";

    public static final String NORMAL_PROJECT_TYPE = "normal";
    public static final String WARMUPS_PROJECT_TYPE = "warmups";
    public static final String EDITOR_PROJECT_TYPE = "editor";

    public static final String BOOK_ADDED = "book_added";

    private List<Book>         books = new ArrayList ();
    private int                lastBookId = 0;
    private File               projectDirectory = null;
    private Date               lastEdited = null;
    private boolean            backup = false;
    private boolean            encrypted = false;
    private String             backupVersion = null;
    private List<QCharacter>   characters = new ArrayList ();
    private List<Location>     locations = new ArrayList ();
    private List<QObject>      objects = new ArrayList ();
    private List<ResearchItem> researchItems = new ArrayList ();
    private List<IdeaType>     ideaTypes = new ArrayList ();
    private String             filePassword = null;
    private boolean            noCredentials = false;
    private String             type = Project.NORMAL_PROJECT_TYPE;
    private String             backupService = null;
    private String             editorsProjectId = null;
    private EditorProject      editorProject = null;
    
    public Project (Element pEl)
                    throws  Exception
    {

        this ();
    
        String name = JDOMUtils.getChildElementContent (pEl,
                                                        XMLConstants.name);
        String type = JDOMUtils.getAttributeValue (pEl,
                                                   XMLConstants.type,
                                                   false);

        if (type.equals (""))
        {

            type = null;

        }
        
        String directory = JDOMUtils.getChildElementContent (pEl,
                                                             XMLConstants.directory);

        boolean encrypted = JDOMUtils.getAttributeValueAsBoolean (pEl,
                                                                  XMLConstants.encrypted,
                                                                  false);

        boolean noCredentials = false;

        if (JDOMUtils.getAttribute (pEl,
                                    XMLConstants.noCredentials,
                                    false) != null)
        {

            noCredentials = JDOMUtils.getAttributeValueAsBoolean (pEl,
                                                                  XMLConstants.noCredentials);

        }

        String d = JDOMUtils.getAttributeValue (pEl,
                                                XMLConstants.lastEdited,
                                                false);

        String bs = JDOMUtils.getAttributeValue (pEl,
                                                 XMLConstants.backupService,
                                                 false);
                                                
        File dir = new File (directory);

        this.name = name;
        
        this.setProjectDirectory (dir);
        this.setEncrypted (encrypted);
        this.setNoCredentials (noCredentials);

        if (!bs.equals (""))
        {
            
            this.setBackupService (bs);
            
        }
        
        if (type != null)
        {

            this.setType (type);

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

        String id = JDOMUtils.getAttributeValue (pEl,
                                                 XMLConstants.editorsProjectId,
                                                 false);
        
        if (!id.equals (""))
        {
            
            this.editorsProjectId = id;
            
        }
        
    }
    
    public Project()
    {

        super (Project.OBJECT_TYPE);

    }

    public Project(String name)
    {

        super (Project.OBJECT_TYPE,
               name);

    }

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
    
    public void setEditorsProjectId (String id)
    {
        
        this.editorsProjectId = id;
        
    }
    
    public String getEditorsProjectId ()
    {
        
        return this.editorsProjectId;
        
    }
    
    public void setBackupService (String b)
    {
        
        this.backupService = b;
        
    }
    
    public String getBackupService ()
    {
        
        return this.backupService;
        
    }
    
    public String getType ()
    {

        return this.type;

    }

    public void setType (String t)
    {

        this.type = t;

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

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

        Set<NamedObject> ret = new TreeSet (new NamedObjectSorter ());

        for (Book b : this.books)
        {

            ret.add (b);

            ret.addAll (b.getAllNamedChildObjects ());

        }

        for (QCharacter c : this.characters)
        {

            ret.add (c);

            ret.addAll (c.getAllNamedChildObjects ());

        }

        for (Location l : this.locations)
        {

            ret.add (l);

            ret.addAll (l.getAllNamedChildObjects ());

        }

        for (QObject o : this.objects)
        {

            ret.add (o);

            ret.addAll (o.getAllNamedChildObjects ());

        }

        for (ResearchItem r : this.researchItems)
        {

            ret.add (r);

            ret.addAll (r.getAllNamedChildObjects ());

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
            
            return ((Chapter) d).getBook ().getChapterByKey (d.getKey ()) != null;
            
        }
        
        if (d instanceof QCharacter)
        {
            
            return this.getCharacter (d.getKey ()) != null;
        
        }

        if (d instanceof Location)
        {
            
            return this.getLocation (d.getKey ()) != null;
        
        }

        if (d instanceof QObject)
        {
            
            return this.getQObject (d.getKey ()) != null;
        
        }

        if (d instanceof ResearchItem)
        {
            
            return this.getResearchItem (d.getKey ()) != null;
        
        }

        return false;
        
    }
    
    public void removeObject (DataObject d)
    {

        if (d instanceof IdeaType)
        {

            this.getIdeaTypes ().remove (d);

        }

        if (d instanceof QCharacter)
        {

            this.getCharacters ().remove (d);

        }

        if (d instanceof Location)
        {

            this.getLocations ().remove (d);

        }

        if (d instanceof QObject)
        {

            this.getQObjects ().remove (d);

        }

        if (d instanceof ResearchItem)
        {

            this.getResearchItems ().remove (d);

        }

    }

    public boolean hasAsset (Asset a)
    {

        return this.getAssetByName (a.getName (),
                                    a.getObjectType ()) != null;

    }

    public Asset getAssetByName (String n,
                                 String objType)
    {

        if (objType.equals (Location.OBJECT_TYPE))
        {

            return this.getLocationByName (n);

        }

        if (objType.equals (QCharacter.OBJECT_TYPE))
        {

            return this.getCharacterByName (n);

        }

        if (objType.equals (QObject.OBJECT_TYPE))
        {

            return this.getQObjectByName (n);

        }

        if (objType.equals (ResearchItem.OBJECT_TYPE))
        {

            return this.getResearchItemByName (n);

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

        return this.lastEdited;

    }

    public void setLastEdited (Date d)
    {

        this.lastEdited = d;

    }

    public File getProjectDirectory ()
    {

        return this.projectDirectory;

    }

    public void setProjectDirectory (File dir)
    {

        this.projectDirectory = dir;

    }

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

    public List<IdeaType> getIdeaTypes ()
    {
        
        return this.ideaTypes;
        
    }

    public void addIdeaType (IdeaType it)
    {
        
        this.ideaTypes.add (it);
        
    }

    public List<ResearchItem> getResearchItems ()
    {

        return this.researchItems;

    }

    public void addAsset (Asset a)
    {

        if (a instanceof QCharacter)
        {

            this.addCharacter ((QCharacter) a);

        }

        if (a instanceof Location)
        {

            this.addLocation ((Location) a);

        }

        if (a instanceof ResearchItem)
        {

            this.addResearchItem ((ResearchItem) a);

        }

        if (a instanceof QObject)
        {

            this.addQObject ((QObject) a);

        }

        a.setProject (this);

    }

    public void addResearchItem (ResearchItem c)
    {

        c.setProject (this);

        this.getResearchItems ().add (c);

    }

    public void addQObject (QObject c)
    {

        c.setProject (this);

        this.getQObjects ().add (c);

    }

    public void addCharacter (QCharacter c)
    {

        c.setProject (this);

        this.getCharacters ().add (c);

    }

    public void addLocation (Location c)
    {

        c.setProject (this);

        this.getLocations ().add (c);

    }

/*
    private void setResearchItems (List<ResearchItem> a)
    {

        this.researchItems = a;

    }
*/
    private void setCharacters (List<QCharacter> a)
    {

        this.characters = a;

    }

    private void setLocations (List<Location> a)
    {

        this.locations = a;

    }

    private void setBooks (List b)
    {

        this.books = b;

    }

    public String toString ()
    {

        return Project.OBJECT_TYPE + "(key: " + this.getKey () + ", name: " + this.getName () + ", dir: " + this.projectDirectory + ", encrypted: " + this.encrypted + ", lastEdited: " + this.lastEdited + ", backupVersion: " + this.backupVersion + ", backupService: " + this.backupService + ")";

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

        b.setProject (this);

        this.books.add (b);

        this.firePropertyChangedEvent (Project.BOOK_ADDED,
                                       null,
                                       b);

    }

    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + ((null == this.projectDirectory) ? 0 : this.projectDirectory.hashCode ());

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

        return this.projectDirectory.equals (po.projectDirectory);

    }

    public Element getAsJDOMElement ()
    {
        
        Element pEl = new Element (Project.OBJECT_TYPE);

        Element nEl = new Element (Environment.XMLConstants.name);
        pEl.addContent (nEl);
        nEl.addContent (this.getName ());

        if (this.getType () == null)
        {

            this.setType (Project.NORMAL_PROJECT_TYPE);

        }

        pEl.setAttribute (XMLConstants.type,
                          this.getType ());

        if (this.getBackupService () != null)
        {
            
            pEl.setAttribute (XMLConstants.backupService,
                              this.getBackupService ());
                          
        }
        
        Element dEl = new Element (XMLConstants.directory);
        pEl.addContent (dEl);
        dEl.addContent (this.getProjectDirectory ().getPath ());

        Date lastEdited = this.getLastEdited ();

        if (lastEdited != null)
        {

            pEl.setAttribute (XMLConstants.lastEdited,
                              String.valueOf (lastEdited.getTime ()));

        }

        pEl.setAttribute (XMLConstants.encrypted,
                          Boolean.valueOf (this.isEncrypted ()).toString ());

        if (this.isNoCredentials ())
        {

            pEl.setAttribute (XMLConstants.noCredentials,
                              Boolean.valueOf (this.isNoCredentials ()).toString ());

        }

        if (this.editorsProjectId != null)
        {
            
            pEl.setAttribute (XMLConstants.editorsProjectId,
                              this.editorsProjectId);
            
        }
        
        return pEl;
        
    }
    
}
