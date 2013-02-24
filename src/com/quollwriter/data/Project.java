package com.quollwriter.data;

import java.io.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import org.jdom.*;


public class Project extends NamedObject
{

    public static final String OBJECT_TYPE = "project";
    public static final String WORDCOUNTS_OBJECT_TYPE = "wordcounts";

    public static final String NORMAL_PROJECT_TYPE = "normal";
    public static final String WARMUPS_PROJECT_TYPE = "warmups";

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

    public Project()
    {

        super (Project.OBJECT_TYPE);

    }

    public Project(String name)
    {

        super (Project.OBJECT_TYPE,
               name);

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

        return Project.OBJECT_TYPE + "(key: " + this.getKey () + ", name: " + this.getName () + ", dir: " + this.projectDirectory + ", encrypted: " + this.encrypted + ", lastEdited: " + this.lastEdited + ", backupVersion: " + this.backupVersion + ")";

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

}
