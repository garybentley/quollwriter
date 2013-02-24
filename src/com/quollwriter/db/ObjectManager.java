package com.quollwriter.db;

import java.io.*;

import java.sql.*;

import java.util.*;

import javax.sql.*;

import com.gentlyweb.xml.JDOMUtils;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.*;

import org.jdom.*;


public class ObjectManager
{

    public class XMLConstants
    {

        public static final String from = "from";
        public static final String to = "to";
        public static final String item = "item";
        public static final String log = "log";
        public static final String sql = "sql";
        public static final String file = "file";
        public static final String canFail = "canFail";

    }

    private DataSource               ds = null;
    private File                     dir = null;
    private Map<String, DataHandler> handlers = new HashMap ();
    private String                   sequenceName = null;
    private GenericObjectPool        connectionPool = null;
    private Project                  project = null;
    private Map<Class, DataHandler>  actionLogHandlers = new HashMap ();

    public ObjectManager()
    {

        // Load the handlers.
        this.handlers.put (Chapter.OBJECT_TYPE,
                           new ChapterDataHandler (this));
        this.handlers.put (Chapter.class.getName (),
                           this.handlers.get (Chapter.OBJECT_TYPE));

        this.handlers.put (Book.OBJECT_TYPE,
                           new BookDataHandler (this));
        this.handlers.put (Book.class.getName (),
                           this.handlers.get (Book.OBJECT_TYPE));

        this.handlers.put (Link.OBJECT_TYPE,
                           new LinkDataHandler (this));
        this.handlers.put (Link.class.getName (),
                           this.handlers.get (Link.OBJECT_TYPE));

        this.handlers.put (Project.OBJECT_TYPE,
                           new ProjectDataHandler (this));
        this.handlers.put (Project.class.getName (),
                           this.handlers.get (Project.OBJECT_TYPE));

        this.handlers.put (Location.OBJECT_TYPE,
                           new LocationDataHandler (this));
        this.handlers.put (Location.class.getName (),
                           this.handlers.get (Location.OBJECT_TYPE));

        this.handlers.put (QCharacter.OBJECT_TYPE,
                           new CharacterDataHandler (this));
        this.handlers.put (QCharacter.class.getName (),
                           this.handlers.get (QCharacter.OBJECT_TYPE));

        this.handlers.put (Scene.OBJECT_TYPE,
                           new SceneDataHandler (this));
        this.handlers.put (Scene.class.getName (),
                           this.handlers.get (Scene.OBJECT_TYPE));

        this.handlers.put (Note.OBJECT_TYPE,
                           new NoteDataHandler (this));
        this.handlers.put (Note.class.getName (),
                           this.handlers.get (Note.OBJECT_TYPE));

        this.handlers.put (OutlineItem.OBJECT_TYPE,
                           new OutlineItemDataHandler (this));
        this.handlers.put (OutlineItem.class.getName (),
                           this.handlers.get (OutlineItem.OBJECT_TYPE));

        this.handlers.put (QObject.OBJECT_TYPE,
                           new ObjectDataHandler (this));
        this.handlers.put (QObject.class.getName (),
                           this.handlers.get (QObject.OBJECT_TYPE));

        this.handlers.put (ResearchItem.OBJECT_TYPE,
                           new ResearchItemDataHandler (this));
        this.handlers.put (ResearchItem.class.getName (),
                           this.handlers.get (ResearchItem.OBJECT_TYPE));

        this.handlers.put (Warmup.OBJECT_TYPE,
                           new WarmupDataHandler (this));
        this.handlers.put (Warmup.class.getName (),
                           this.handlers.get (Warmup.OBJECT_TYPE));
        this.handlers.put (IdeaType.OBJECT_TYPE,
                           new IdeaTypeDataHandler (this));
        this.handlers.put (IdeaType.class.getName (),
                           this.handlers.get (IdeaType.OBJECT_TYPE));
        this.handlers.put (Idea.OBJECT_TYPE,
                           new IdeaDataHandler (this));
        this.handlers.put (Idea.class.getName (),
                           this.handlers.get (Idea.OBJECT_TYPE));

        this.actionLogHandlers.put (ResearchItem.class,
                                    this.handlers.get (ResearchItem.OBJECT_TYPE));
        this.actionLogHandlers.put (QObject.class,
                                    this.handlers.get (QObject.OBJECT_TYPE));
        this.actionLogHandlers.put (OutlineItem.class,
                                    this.handlers.get (OutlineItem.OBJECT_TYPE));
        this.actionLogHandlers.put (Note.class,
                                    this.handlers.get (Note.OBJECT_TYPE));
        this.actionLogHandlers.put (Scene.class,
                                    this.handlers.get (Scene.OBJECT_TYPE));
        this.actionLogHandlers.put (QCharacter.class,
                                    this.handlers.get (QCharacter.OBJECT_TYPE));
        this.actionLogHandlers.put (Location.class,
                                    this.handlers.get (Location.OBJECT_TYPE));
        this.actionLogHandlers.put (Project.class,
                                    this.handlers.get (Project.OBJECT_TYPE));
        this.actionLogHandlers.put (Book.class,
                                    this.handlers.get (Book.OBJECT_TYPE));
        this.actionLogHandlers.put (Chapter.class,
                                    this.handlers.get (Chapter.OBJECT_TYPE));
        this.actionLogHandlers.put (Warmup.class,
                                    this.handlers.get (Warmup.OBJECT_TYPE));
        this.actionLogHandlers.put (IdeaType.class,
                                    this.handlers.get (IdeaType.OBJECT_TYPE));
        this.actionLogHandlers.put (Idea.class,
                                    this.handlers.get (Idea.OBJECT_TYPE));

    }

    public void setProject (Project p)
    {

        this.project = p;

    }

    public DataHandler getHandler (Class c)
    {

        return this.handlers.get (c.getName ());

    }

    public DataHandler getHandler (String objType)
    {

        return this.handlers.get (objType);

    }

    public void updateChapterIndexes (Book b)
                               throws GeneralException
    {

        BookDataHandler bdh = (BookDataHandler) this.getHandler (Book.class);

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

            bdh.updateChapterIndexes (b,
                                      conn);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to chapter indexes for book: " +
                                        b,
                                        e);

        } finally
        {

            try
            {

                if (conn != null)
                {

                    this.releaseConnection (conn);

                }

            } catch (Exception e)
            {

            }

        }

    }

    public List<? extends NamedObject> getObjects (Class       c,
                                                   NamedObject n,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        DataHandler dh = this.getHandler (c);

        if (dh == null)
        {

            throw new GeneralException ("Class: " +
                                        c.getName () +
                                        " is not supported.");

        }

        boolean releaseConn = false;

        if (conn == null)
        {

            conn = this.getConnection ();
            releaseConn = true;

        }

        List objs = dh.getObjects (n,
                                   conn,
                                   loadChildObjects);

        if (releaseConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {

            }

        }

        return objs;

    }

    public NamedObject getObjectByKey (Class      c,
                                       int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        DataHandler dh = this.getHandler (c);

        if (dh == null)
        {

            throw new GeneralException ("Class: " +
                                        c.getName () +
                                        " is not supported.");

        }

        boolean releaseConn = false;

        if (conn == null)
        {

            conn = this.getConnection ();
            releaseConn = true;

        }

        NamedObject n = dh.getObjectByKey (key,
                                           conn,
                                           loadChildObjects);

        if (releaseConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {

            }

        }

        return n;

    }

    private void addSchemaVersion ()
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT * FROM project");

            ResultSet rs = ps.executeQuery ();

            ps = c.prepareStatement ("ALTER TABLE project ADD schema_version SMALLINT DEFAULT 1");

            ps.executeUpdate ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to add schema_version to project",
                                  e);

        } finally
        {

            try
            {

                if (c != null)
                {

                    this.releaseConnection (c);

                }

            } catch (Exception e)
            {
            }

        }

    }

    public void init (File   dir,
                      String username,
                      String password,
                      String filePassword)
               throws GeneralException
    {

        this.dir = dir.getParentFile ();

        String url = "jdbc:h2:" + dir.getPath () + ";AUTOCOMMIT=OFF";

        String pwd = password;

        if (filePassword != null)
        {

            url = url + ";CIPHER=AES";

            // The password acts as the file password.
            pwd = filePassword.replace (' ',
                                        '_') + " " + password.replace (' ',
                                                                       '_');

        }

        if (pwd != null)
        {

            pwd = pwd.trim ();

        }

        try
        {

            Class.forName ("org.h2.Driver");

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load driver: org.h2.Driver",
                                        e);

        }

        this.connectionPool = new GenericObjectPool (null);
        this.connectionPool.setMaxActive (20);
        this.connectionPool.setMaxIdle (10);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory (url,
                                                                                  username,
                                                                                  pwd);

        new PoolableConnectionFactory (connectionFactory,
                                       this.connectionPool,
                                       null,
                                       null,
                                       false,
                                       false);

        this.ds = new PoolingDataSource (this.connectionPool);

        PoolingDataSource pds = (PoolingDataSource) this.ds;

        pds.setAccessToUnderlyingConnectionAllowed (true);

        // Check to see if the project exists, if not create the schema.
        // Get the schema version.
        int schemaVersion = this.getSchemaVersion ();

        if (schemaVersion == -1)
        {

            this.createSchema ();

        } else
        {

            if (schemaVersion < Environment.getSchemaVersion ())
            {

                this.updateSchema (schemaVersion,
                                   Environment.getSchemaVersion ());

            }

        }

        // Get the sequence name.
        this.sequenceName = this.getSequenceName ();

        if (this.sequenceName == null)
        {

            throw new GeneralException ("Unable to find sequence name");

        }

    }

    private String getSequenceName ()
                             throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT sequence_name FROM information_schema.sequences");

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                return rs.getString (1);

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get sequence name.",
                                        e);

        } finally
        {

            if (c != null)
            {

                try
                {

                    this.releaseConnection (c);

                } catch (Exception e)
                {
                }

            }

        }

        return null;

    }

    public void updateLinks (NamedObject d,
                             Set<Link>   newLinks)
                      throws GeneralException
    {

        try
        {

            Connection c = this.getConnection ();

            this.deleteLinks (d,
                              c);

            Iterator<Link> iter = newLinks.iterator ();

            while (iter.hasNext ())
            {

                Link l = iter.next ();

                if ((this.project.getObjectForReference (l.getObject1 ().getObjectReference ()) == null) ||
                    (this.project.getObjectForReference (l.getObject2 ().getObjectReference ()) == null))
                {

                    continue;

                }

                this.saveObject (l,
                                 c);

                // Get either side.
                l.getObject1 ().addLink (l);
                l.getObject2 ().addLink (l);

            }

            this.releaseConnection (c);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to set links for: " +
                                        d,
                                        e);

        }

    }

    public void getLinks (NamedObject d,
                          Project     p,
                          Connection  conn)
                   throws GeneralException
    {

        if (d.getKey () == null)
        {

            return;

        }

        if (d.getLinks ().size () > 0)
        {

            return;

        }

        boolean closeConn = false;

        if (conn == null)
        {

            closeConn = true;

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to get connection",
                                            e);

            }

        }


        try
        {

            d.clearLinks ();

            List params = new ArrayList ();
            params.add (d.getKey ());
            params.add (d.getObjectType ());
            params.add (d.getKey ());
            params.add (d.getObjectType ());

            ResultSet rs = this.executeQuery ("SELECT object1dbkey, object1objtype, object2dbkey, object2objtype, dbkey FROM link WHERE (object1dbkey = ? AND object1objtype = ?) OR (object2dbkey = ? AND object2objtype = ?)",
                                              params,
                                              conn);

            while (rs.next ())
            {

                long   o1key = rs.getLong (1);
                String o1type = rs.getString (2);
                long   o2key = rs.getLong (3);
                String o2type = rs.getString (4);
                long   key = rs.getLong (5);

                ObjectReference o1 = new ObjectReference (o1type,
                                                          o1key,
                                                          null);
                ObjectReference o2 = new ObjectReference (o2type,
                                                          o2key,
                                                          null);

                NamedObject d1 = (NamedObject) p.getObjectForReference (o1);
                NamedObject d2 = (NamedObject) p.getObjectForReference (o2);

                if ((d1 == null) ||
                    (d2 == null))
                {

                    continue;

                }

                Link l = new Link (d1,
                                   d2);
                l.setKey (key);

                d.addLink (l);

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get links for: " +
                                        d,
                                        e);

        } finally
        {

            if (closeConn)
            {

                try
                {

                    this.releaseConnection (conn);

                } catch (Exception e)
                {
                }

            }

        }

    }

    public void getLinks (NamedObject d,
                          Project     p)
                   throws GeneralException
    {

        try
        {

            Connection c = this.getConnection ();

            this.getLinks (d,
                           p,
                           c);

            this.releaseConnection (c);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get links for: " +
                                        d,
                                        e);

        }

    }

    public long getNewKey (Connection c)
                    throws GeneralException
    {

        boolean releaseConn = false;

        if (c == null)
        {

            releaseConn = true;

            c = this.getConnection ();

        }

        long k = -1;

        try
        {

            PreparedStatement ps = c.prepareStatement ("SELECT NEXT VALUE FOR " + this.sequenceName);

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                k = rs.getLong (1);

            }

            try
            {

                ps.close ();

            } catch (Exception ee)
            {

                // Ignore.

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get new key",
                                        e);

        } finally
        {

            if (releaseConn)
            {

                try
                {

                    this.releaseConnection (c);

                } catch (Exception e)
                {
                }

            }

        }

        if (k == -1)
        {

            throw new GeneralException ("Unable to get new key value from sequence: " + this.sequenceName);

        }

        return k;

    }

    public int getSchemaVersion ()
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT schema_version FROM project");

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                return rs.getInt (1);

            }

        } catch (Exception e)
        {

        } finally
        {

            if (c != null)
            {

                try
                {

                    this.releaseConnection (c);

                } catch (Exception e)
                {
                }

            }

        }

        return -1;

    }

    public Connection getConnection ()
                              throws GeneralException
    {

        try
        {

            return this.ds.getConnection ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get connection",
                                        e);

        }

    }

    public void releaseConnection (Connection conn)
                            throws GeneralException
    {

        try
        {

            conn.commit ();

            conn.close ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to release connection",
                                        e);

        }

    }

    public void deleteLinks (NamedObject n,
                             Connection  conn)
                      throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            closeConn = true;

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to get connection",
                                            e);

            }

        }

        this.getLinks (n,
                       this.project,
                       conn);

        Set<Link> links = n.getLinks ();

        Iterator<Link> iter = links.iterator ();

        while (iter.hasNext ())
        {

            Link l = iter.next ();

            this.deleteObject (l,
                               false,
                               conn);

            iter.remove ();

            // Get either side.
            l.getObject1 ().removeLink (l);
            l.getObject2 ().removeLink (l);

        }

        if (closeConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {
            }

        }

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            closeConn = true;

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to get connection",
                                            e);

            }

        }

        DataHandler dh = this.handlers.get (d.getClass ().getName ());

        if (dh == null)
        {

            throw new GeneralException ("Class: " +
                                        d.getClass ().getName () +
                                        " is not supported.");

        }

        dh.deleteObject (d,
                         deleteChildObjects,
                         conn);

        try
        {

            PreparedStatement ps = null;

            if (d instanceof NamedObject)
            {

                NamedObject n = (NamedObject) d;

                for (Note nn : n.getNotes ())
                {

                    this.deleteObject (nn,
                                       false,
                                       conn);

                }

                this.deleteLinks (n,
                                  conn);

                List params = new ArrayList ();
                params.add (d.getKey ());

                this.executeStatement ("DELETE FROM actionlog WHERE onobjectdbkey = ?",
                                       params,
                                       conn);

                this.executeStatement ("DELETE FROM namedobject WHERE dbkey = ?",
                                       params,
                                       conn);

            }

            List params = new ArrayList ();
            params.add (d.getKey ());
            params.add (d.getObjectType ());

            this.executeStatement ("DELETE FROM dataobject WHERE dbkey = ? AND objecttype = ?",
                                   params,
                                   conn);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to delete namedobject/dataobject entries for: " +
                                        d,
                                        e);

        }

        if (closeConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {

            }

        }

    }

    public void saveObjects (List<DataObject> objs,
                             Connection       conn)
                      throws GeneralException
    {


        boolean closeConn = false;

        if (conn == null)
        {

            closeConn = true;

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to get connection",
                                            e);

            }

        }

        for (DataObject d : objs)
        {

            this.saveObject (d,
                             conn);

        }

        if (closeConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {

            }

        }

    }

    public void createActionLogEntry (NamedObject n,
                                      String      message,
                                      Element     changesEl,
                                      Connection  conn)
    {

        boolean closeConn = false;

        if (conn == null)
        {

            closeConn = true;

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to log action for: " +
                                      n +
                                      " with message: " +
                                      message +
                                      " and change element: " +
                                      changesEl,
                                      e);

                return;

            }

        }

        try
        {

            // Create a log.
            List params = new ArrayList ();

            params.add (null);

            if (n != null)
            {

                params.set (0,
                            n.getKey ());

            }

            long time = System.currentTimeMillis ();

            params.add (new Timestamp (time));
            params.add (message);

            String m = null;

            if (changesEl != null)
            {

                try
                {

                    m = JDOMUtils.getElementAsString (changesEl);

                } catch (Exception e)
                {

                }

            }

            params.add (m);

            params.add (n.getKey () + n.getObjectType () + time + message + m);

            params.add (50);

            // To decompress the message, do: utf8tostring(expand(?)).
            this.executeStatement ("INSERT INTO actionlog (onobjectdbkey, when, message, changes, digest) VALUES (?, ?, ?, compress (stringtoutf8 (?)), hash('SHA256', stringtoutf8(?), ?))",
                                   params,
                                   conn);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create action log entry for: " +
                                  n,
                                  e);

        }

        if (closeConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {

            }

        }

    }

    public void saveObject (DataObject d,
                            Connection conn)
                     throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            closeConn = true;

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to get connection",
                                            e);

            }

        }

        DataHandler dh = this.handlers.get (d.getClass ().getName ());

        if (dh == null)
        {

            throw new GeneralException ("Class: " +
                                        d.getClass ().getName () +
                                        " is not supported.");

        }

        boolean create = false;

        if (d instanceof DataObject)
        {

            if (d.getKey () == null)
            {

                create = true;

                // Create the relevant items in dataobject and namedobject.
                d.setKey (this.getNewKey (conn));

                try
                {

                    List params = new ArrayList ();
                    params.add (d.getKey ());
                    params.add (d.getObjectType ());
                    params.add (new java.util.Date ());
                    params.add (d.getPropertiesAsString ());

                    this.executeStatement ("INSERT INTO dataobject (dbkey, objecttype, datecreated, properties) VALUES (?, ?, ?, ?)",
                                           params,
                                           conn);

                    if (d instanceof NamedObject)
                    {

                        NamedObject n = (NamedObject) d;

                        params = new ArrayList ();
                        params.add (n.getKey ());
                        params.add (n.getName ());
                        params.add (n.getDescription ());

                        this.executeStatement ("INSERT INTO namedobject (dbkey, name, description) VALUES (?, ?, ?)",
                                               params,
                                               conn);

                        if (this.actionLogHandlers.containsKey (n.getClass ()))
                        {

                            Element changeEl = n.getChanges (null);

                            this.createActionLogEntry (n,
                                                       "Created new: " + Environment.getObjectTypeName (n.getObjectType ()),
                                                       changeEl,
                                                       conn);

                        }

                        if (!(n instanceof Link))
                        {

                            for (Note nn : n.getNotes ())
                            {

                                this.saveObject (nn,
                                                 conn);

                            }

                            this.updateLinks (n,
                                              n.getLinks ());

                        }

                    }

                } catch (Exception e)
                {

                    throw new GeneralException ("Unable to create dataobject/namedobject records for: " +
                                                d,
                                                e);

                }

            } else
            {

                // Update the dataobject and namedobject.
                try
                {

                    List params = new ArrayList ();
                    params.add (d.getPropertiesAsString ());
                    params.add (d.getKey ());

                    this.executeStatement ("UPDATE dataobject SET properties = ? WHERE dbkey = ?",
                                           params,
                                           conn);

                    if (d instanceof NamedObject)
                    {

                        NamedObject n = (NamedObject) d;

                        if (this.actionLogHandlers.containsKey (n.getClass ()))
                        {

                            NamedObject oldN = this.getObjectByKey (n.getClass (),
                                                                    n.getKey ().intValue (),
                                                                    conn,
                                                                    false);

                            Element changeEl = n.getChanges (oldN);

                            if (changeEl != null)
                            {

                                this.createActionLogEntry (n,
                                                           "Updated: " + Environment.getObjectTypeName (n.getObjectType ()),
                                                           changeEl,
                                                           conn);

                            }

                        }

                        params = new ArrayList ();
                        params.add (n.getName ());
                        params.add (n.getDescription ());
                        params.add (n.getKey ());

                        this.executeStatement ("UPDATE namedobject SET name = ?, description = ? WHERE dbkey = ?",
                                               params,
                                               conn);

                        for (Note nn : n.getNotes ())
                        {

                            this.saveObject (nn,
                                             conn);

                        }

                    }

                } catch (Exception e)
                {

                    throw new GeneralException ("Unable to update dataobject/namedobject records for: " +
                                                d,
                                                e);

                }

            }

        }

        if (create)
        {

            dh.createObject (d,
                             conn);

        } else
        {

            dh.updateObject (d,
                             conn);

        }

        if (closeConn)
        {

            try
            {

                this.releaseConnection (conn);

            } catch (Exception e)
            {

            }

        }

    }

    public Project getProject ()
                        throws GeneralException
    {

        ProjectDataHandler pdh = new ProjectDataHandler (this);

        try
        {

            Connection c = this.getConnection ();

            this.project = pdh.getProject (c);

            this.releaseConnection (c);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get project",
                                        e);

        }

        this.project.setProjectDirectory (this.dir);

        return this.project;

    }

    public ResultSet executeQuery (String     sql,
                                   List       params,
                                   Connection conn)
                            throws GeneralException
    {

        PreparedStatement ps = null;

        try
        {

            ps = conn.prepareStatement (sql);

            if (params != null)
            {

                for (int i = 0; i < params.size (); i++)
                {

                    ps.setObject (i + 1,
                                  params.get (i));

                }

            }

            return ps.executeQuery ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to execute sql: " +
                                        sql +
                                        " with params: " +
                                        params,
                                        e);

        } finally
        {

            Environment.logSQLStatement (sql,
                                         params);

        }

    }

    public void executeStatement (String     sql,
                                  List       params,
                                  Connection conn)
                           throws GeneralException
    {

        boolean releaseConn = false;

        if (conn == null)
        {

            try
            {

                conn = this.getConnection ();

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to get connection",
                                            e);

            }

        }

        PreparedStatement ps = null;

        try
        {

            ps = conn.prepareStatement (sql);

            if (params != null)
            {

                for (int i = 0; i < params.size (); i++)
                {

                    Object o = params.get (i);

                    if (!(o instanceof Timestamp))
                    {

                        if (o instanceof java.util.Date)
                        {

                            o = new Timestamp (((java.util.Date) o).getTime ());

                        }

                    }

                    ps.setObject (i + 1,
                                  o);

                }

            }

            ps.executeUpdate ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to execute sql: " +
                                        sql +
                                        "\nusing params: " +
                                        params,
                                        e);

        } finally
        {

            try
            {

                if (ps != null)
                {

                    //ps.close ();

                }

            } catch (Exception e)
            {

            }

            if (releaseConn)
            {

                try
                {

                    this.releaseConnection (conn);

                } catch (Exception e)
                {

                }

            }

            Environment.logSQLStatement (sql,
                                         params);

        }

    }

    private void createSchema ()
                        throws GeneralException
    {

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get connection",
                                        e);

        }

        // Get the schema creation script.
        this.runUpgradeScript (0,
                               1,
                               conn);

        this.runCreateViewsScript (conn);

        try
        {

            this.releaseConnection (conn);

        } catch (Exception e)
        {
            e.printStackTrace ();
        }

    }

    private void updateSchema (int oldVersion,
                               int newVersion)
                        throws GeneralException
    {

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get connection",
                                        e);

        }

        int oVer = oldVersion;
        int nVer = oVer++;

        // Get the update script.
        while (nVer < (newVersion + 1))
        {

            // Get the script.
            this.runUpgradeScript (oVer,
                                   nVer,
                                   conn);

            oVer = nVer;
            nVer++;

        }

        // Run the create views script.
        this.runCreateViewsScript (conn);

        try
        {

            this.releaseConnection (conn);

        } catch (Exception e)
        {

        }

    }

    private void runCreateViewsScript (Connection conn)
                                throws GeneralException
    {

        try
        {

            String f = Constants.UPDATE_SCRIPTS_DIR + "/create-views.xml";

            String xml = Environment.getResourceFileAsString (f);

            if (xml == null)
            {

                return;

            }

            Element root = JDOMUtils.getStringAsElement (xml);

            this.runScriptElements (root,
                                    conn);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create views",
                                        e);

        }

    }

    private void runUpgradeScript (int        oldVersion,
                                   int        newVersion,
                                   Connection conn)
                            throws GeneralException
    {

        try
        {

            String f = Constants.UPDATE_SCRIPTS_DIR + "/" + oldVersion + "-" + newVersion + ".xml";

            String xml = Environment.getResourceFileAsString (f);

            if (xml == null)
            {

                return;

            }

            Element root = JDOMUtils.getStringAsElement (xml);

            int from = JDOMUtils.getAttributeValueAsInt (root,
                                                         XMLConstants.from);
            int to = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.to);

            if ((oldVersion != from) ||
                (newVersion != to))
            {

                throw new GeneralException ("Resource file: " +
                                            f +
                                            " has versions: " +
                                            from +
                                            "/" +
                                            to +
                                            " but expect: " +
                                            oldVersion +
                                            "/" +
                                            newVersion);

            }

            this.runScriptElements (root,
                                    conn);

            List params = new ArrayList ();
            params.add (newVersion);

            this.executeStatement ("UPDATE project SET schema_version = ?",
                                   params,
                                   conn);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to update from version: " +
                                        oldVersion +
                                        " to: " +
                                        newVersion,
                                        e);

        }

    }

    private void runScriptElements (Element    root,
                                    Connection conn)
                             throws GeneralException,
                                    JDOMException
    {

        List itemEls = JDOMUtils.getChildElements (root,
                                                   XMLConstants.item,
                                                   false);

        for (int i = 0; i < itemEls.size (); i++)
        {

            Element el = (Element) itemEls.get (i);

            String log = JDOMUtils.getChildElementContent (el,
                                                           XMLConstants.log);

            Element sqlEl = JDOMUtils.getChildElement (el,
                                                       XMLConstants.sql);

            boolean canFail = JDOMUtils.getAttributeValueAsBoolean (el,
                                                                    XMLConstants.canFail,
                                                                    false);

            // See if there is a file attribute.
            String file = JDOMUtils.getAttributeValue (sqlEl,
                                                       XMLConstants.file,
                                                       false);

            String sql = null;

            if (!file.equals (""))
            {

                // Get the file.

                sql = Environment.getResourceFileAsString (Constants.SCHEMA_DIR + file);

            } else
            {

                sql = JDOMUtils.getChildElementContent (el,
                                                        XMLConstants.sql);

            }

            if (sql == null)
            {

                throw new GeneralException ("Expected to find sql for item: " +
                                            JDOMUtils.getPath (el) +
                                            " from update script: " +
                                            file);

            }

            // Run it.
            try
            {

                this.executeStatement (sql,
                                       null,
                                       conn);

            } catch (Exception e)
            {

                if (canFail)
                {

                    Environment.logError ("Unable to execute sql: " +
                                          sql +
                                          " for item: " +
                                          JDOMUtils.getPath (el) +
                                          " from update script: " +
                                          file +
                                          ", not fatal, continuing.",
                                          e);

                } else
                {

                    throw new GeneralException ("Unable to execute sql: " +
                                                sql +
                                                " for item: " +
                                                JDOMUtils.getPath (el) +
                                                " from update script: " +
                                                file,
                                                e);

                }

            }

            Environment.logMessage (log);

        }

    }

    public File createBackup ()
                       throws Exception
    {

        File dir = new File (this.dir.getPath () + "/versions");

        dir.mkdirs ();

        // Indicate that this directory can be deleted.
        Utils.createQuollWriterDirFile (dir);

        File[] files = dir.listFiles ();

        int ver = 0;

        if (files != null)
        {

            for (int i = 0; i < files.length; i++)
            {

                File f = files[i];

                if ((f.getName ().startsWith ("backup")) &&
                    (f.getName ().endsWith (".zip")))
                {

                    // Split the filename, get the version.
                    String n = f.getName ().substring (6,
                                                       f.getName ().length () - 4);

                    try
                    {

                        int v = Integer.parseInt (n);

                        if (v > ver)
                        {

                            ver = v;

                        }

                    } catch (Exception e)
                    {

                    }

                }

            }

        }

        ver++;

        File f = new File (dir.getPath () + "/backup" + ver + ".zip");

        try
        {

            Connection c = this.getConnection ();

            this.executeStatement ("BACKUP TO '" + f.getPath () + "'",
                                   null,
                                   c);

            this.releaseConnection (c);

            this.createActionLogEntry (this.project,
                                       "Created backup, written to file: " + f.getPath (),
                                       null,
                                       null);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create backup to: " +
                                        f,
                                        e);

        }

        return f;

    }

    public void closeConnectionPool ()
    {

        if (this.connectionPool != null)
        {

            try
            {

                this.connectionPool.clear ();
                this.connectionPool.close ();

            } catch (Exception e)
            {
                e.printStackTrace ();
                // Ignore.

            }

        }

    }

    public void saveWordCounts (java.util.Date start,
                                java.util.Date end)
    {

        if ((this.project == null) ||
            (this.project.getKey () == null))
        {

            return;

        }

        Connection conn = null;

        try
        {

            List wcs = new ArrayList ();

            conn = this.getConnection ();

            start = Environment.zeroTimeFieldsForDate (start);
            end = Environment.zeroTimeFieldsForDate (end);

            List params = new ArrayList ();
            params.add (start);
            params.add (this.project.getKey ());

            this.executeStatement ("DELETE FROM wordcount WHERE start = ? AND projectdbkey = ?",
                                   params,
                                   conn);

            for (Book book : this.project.getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ChapterCounts cc = UIUtils.getChapterCounts (c.getText ());

                    params = new ArrayList ();
                    params.add (this.project.getKey ());

                    params.add (c.getKey ());

                    params.add (cc.wordCount);
                    params.add (start);
                    params.add (end);

                    this.executeStatement ("INSERT INTO wordcount (projectdbkey, chapterdbkey, count, start, end) VALUES (?, ?, ?, ?, ?)",
                                           params,
                                           conn);

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to save session word counts for project: " +
                                  this.project,
                                  e);

        } finally
        {

            if (conn != null)
            {

                try
                {

                    this.releaseConnection (conn);

                } catch (Exception e)
                {
                }

            }

        }

    }

    public void loadNotes (NamedObject n,
                           Connection  conn)
                    throws GeneralException
    {

        List notes = this.getObjects (Note.class,
                                      n,
                                      conn,
                                      false);

        for (int i = 0; i < notes.size (); i++)
        {

            n.addNote ((Note) notes.get (i));

        }

    }

}
