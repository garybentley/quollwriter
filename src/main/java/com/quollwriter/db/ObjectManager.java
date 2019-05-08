package com.quollwriter.db;

import java.io.*;

import java.sql.*;

import java.util.*;

import javax.sql.*;

import com.gentlyweb.xml.JDOMUtils;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;

import com.quollwriter.editors.messages.*;

import com.quollwriter.ui.*;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.pool2.impl.*;

import org.h2.jdbc.*;

import org.jdom.*;

import org.josql.*;

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
    protected Map<Class, DataHandler> handlers = new HashMap<Class, DataHandler> ();
    private String                   sequenceName = null;
    private GenericObjectPool<PoolableConnection>        connectionPool = null;
    private Project                  project = null;
    private Map<Class, DataHandler>  actionLogHandlers = new HashMap ();

    public ObjectManager()
    {

        // TODO: Change to have a search model for handlers with an annotation and register using
        // annotation information.
        // Load the handlers.
        this.handlers.put (Chapter.class,
                           new ChapterDataHandler (this));
        //this.handlers.put (Chapter.class.getName (),
        //                   this.handlers.get (Chapter.OBJECT_TYPE));

        this.handlers.put (Book.class,
                           new BookDataHandler (this));
        //this.handlers.put (Book.class.getName (),
        //                   this.handlers.get (Book.OBJECT_TYPE));

        this.handlers.put (Link.class,
                           new LinkDataHandler (this));
        //this.handlers.put (Link.class.getName (),
        //                   this.handlers.get (Link.OBJECT_TYPE));

        this.handlers.put (Project.class,
                           new ProjectDataHandler (this));
        //this.handlers.put (Project.class.getName (),
        //                   this.handlers.get (Project.OBJECT_TYPE));

        this.handlers.put (Location.class,
                           new LocationDataHandler (this));
        //this.handlers.put (Location.class.getName (),
        //                   this.handlers.get (Location.OBJECT_TYPE));

        this.handlers.put (QCharacter.class,
                           new CharacterDataHandler (this));
        //this.handlers.put (QCharacter.class.getName (),
        //                   this.handlers.get (QCharacter.OBJECT_TYPE));

        this.handlers.put (Scene.class,
                           new SceneDataHandler (this));
        //this.handlers.put (Scene.class.getName (),
        //                   this.handlers.get (Scene.OBJECT_TYPE));

        this.handlers.put (Note.class,
                           new NoteDataHandler (this));
        //this.handlers.put (Note.class.getName (),
        //                   this.handlers.get (Note.OBJECT_TYPE));

        this.handlers.put (OutlineItem.class,
                           new OutlineItemDataHandler (this));
        //this.handlers.put (OutlineItem.class.getName (),
        //                   this.handlers.get (OutlineItem.OBJECT_TYPE));

        this.handlers.put (QObject.class,
                           new ObjectDataHandler (this));
        //this.handlers.put (QObject.class.getName (),
        //                   this.handlers.get (QObject.OBJECT_TYPE));

        this.handlers.put (ResearchItem.class,
                           new ResearchItemDataHandler (this));
        //this.handlers.put (ResearchItem.class.getName (),
        //                   this.handlers.get (ResearchItem.OBJECT_TYPE));

        this.handlers.put (Warmup.class,
                           new WarmupDataHandler (this));
        //this.handlers.put (Warmup.class.getName (),
        //                   this.handlers.get (Warmup.OBJECT_TYPE));
        this.handlers.put (IdeaType.class,
                           new IdeaTypeDataHandler (this));
        //this.handlers.put (IdeaType.class.getName (),
        //                   this.handlers.get (IdeaType.OBJECT_TYPE));
        this.handlers.put (Idea.class,
                           new IdeaDataHandler (this));

        this.handlers.put (ProjectVersion.class,
                           new ProjectVersionDataHandler (this));

        this.handlers.put (Asset.class,
                           new AssetDataHandler (this));

        this.handlers.put (Tag.class,
                           new TagDataHandler (this));

        this.handlers.put (UserConfigurableObjectType.class,
                           new UserConfigurableObjectTypeDataHandler (this));
        this.handlers.put (UserConfigurableObjectTypeField.class,
                           new UserConfigurableObjectTypeFieldDataHandler (this));
        this.handlers.put (TextUserConfigurableObjectTypeField.class,
                           new UserConfigurableObjectTypeFieldDataHandler (this));
        this.handlers.put (MultiTextUserConfigurableObjectTypeField.class,
                           new UserConfigurableObjectTypeFieldDataHandler (this));
        this.handlers.put (SelectUserConfigurableObjectTypeField.class,
                           new UserConfigurableObjectTypeFieldDataHandler (this));
        this.handlers.put (WebpageUserConfigurableObjectTypeField.class,
                           new UserConfigurableObjectTypeFieldDataHandler (this));
/*
        this.handlers.put (UserConfigurableObject.class,
                           new UserConfigurableObjectDataHandler (this));
                           */
        this.handlers.put (UserConfigurableObjectField.class,
                           new UserConfigurableObjectFieldDataHandler (this));

        this.actionLogHandlers.put (ResearchItem.class,
                                    this.handlers.get (ResearchItem.class));
        this.actionLogHandlers.put (QObject.class,
                                    this.handlers.get (QObject.class));
        this.actionLogHandlers.put (OutlineItem.class,
                                    this.handlers.get (OutlineItem.class));
        this.actionLogHandlers.put (Note.class,
                                    this.handlers.get (Note.class));
        this.actionLogHandlers.put (Scene.class,
                                    this.handlers.get (Scene.class));
        this.actionLogHandlers.put (QCharacter.class,
                                    this.handlers.get (QCharacter.class));
        this.actionLogHandlers.put (Location.class,
                                    this.handlers.get (Location.class));
        this.actionLogHandlers.put (Project.class,
                                    this.handlers.get (Project.class));
        this.actionLogHandlers.put (Book.class,
                                    this.handlers.get (Book.class));
        this.actionLogHandlers.put (Chapter.class,
                                    this.handlers.get (Chapter.class));
        this.actionLogHandlers.put (Warmup.class,
                                    this.handlers.get (Warmup.class));
        this.actionLogHandlers.put (IdeaType.class,
                                    this.handlers.get (IdeaType.class));
        this.actionLogHandlers.put (Idea.class,
                                    this.handlers.get (Idea.class));
/*
        this.actionLogHandlers.put (EditorProject.class,
                                    this.handlers.get (EditorProject.OBJECT_TYPE));
        this.actionLogHandlers.put (EditorEditor.class,
                                    this.handlers.get (EditorEditor.OBJECT_TYPE));
*/
    }

    public File getDBDir ()
    {

        return this.dir;

    }
    /*
    public DataHandler getHandler (Class c)
    {

        return this.handlers.get (c.getName ());

    }
*/
    /*
    public DataHandler getHandler (String objType)
    {

        return this.handlers.get (objType);

    }
*/
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

            this.throwException (conn,
                                 "Unable to chapter indexes for book: " +
                                 b,
                                 e);

        } finally{

            this.releaseConnection (conn);

        }

    }

    public List<? extends DataObject> getObjects (Class       c,
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

        try
        {

            List objs = dh.getObjects (n,
                                       conn,
                                       loadChildObjects);

            return objs;

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to get objects of type: " +
                                 c.getName () +
                                 " with parent: " +
                                 n,
                                 e);

            // Never hit... but doesn't matter.
            return null;

        } finally {

            if (releaseConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public DataObject getObjectByKey (Class      c,
                                      long       key,
                                      DataObject parent,
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

        try
        {

            return dh.getObjectByKey (key,
                                      parent,
                                      conn,
                                      loadChildObjects);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to get object of type: " +
                                 c.getName () +
                                 " with key: " +
                                 key +
                                 " and parent: " +
                                 parent,
                                 e);

            return null;

        } finally {

            if (releaseConn)
            {

                this.releaseConnection (conn);

            }

        }

    }
/*
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
*/

    /**
     * Create/init the db at the location specified by <b>dir</b>.  Secure the db using the username/password/filePassword
     * combination.  Use <b>newSchemaVersion</b> with value 0 to create the schema.
     *
     * @param dir The directory to save the db to.
     * @param username The db username.
     * @param password The db password.
     * @param filePassword The password used for encrypting/decrypting the db, set to null to be not encrypted.
     */
    public void init (File   dir,
                      String username,
                      String password,
                      String filePassword,
                      int    newSchemaVersion)
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

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory (url,
                                                                                  username,
                                                                                  pwd);

        PoolableConnectionFactory poolf = new PoolableConnectionFactory (connectionFactory,
                                                                         null);
/*
OLD? DBCP1
        PoolableConnectionFactory poolf = new PoolableConnectionFactory (connectionFactory,
                                       this.connectionPool,
                                       null,
                                       null,
                                       false,
                                       false);
*/

        this.connectionPool = new GenericObjectPool<> (poolf);
        // TODO Remove? this.connectionPool.setMaxActive (20);
        this.connectionPool.setMaxTotal (50);
        this.connectionPool.setMaxIdle (10);
        poolf.setPool (this.connectionPool);

        this.ds = new PoolingDataSource (this.connectionPool);

        PoolingDataSource pds = (PoolingDataSource) this.ds;

        pds.setAccessToUnderlyingConnectionAllowed (true);

        // Check to see if the project exists, if not create the schema.
        // Get the schema version.

        if (newSchemaVersion == 0)
        {

            this.createSchema ();

        } else
        {

            int schemaVersion = this.getSchemaVersion ();

            if (schemaVersion < newSchemaVersion)
            {

                this.updateSchema (schemaVersion,
                                   newSchemaVersion);

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

        // Hardcoding the name since it's possible to have more than 1 sequence.
        //return "KEY_SEQUENCE";

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

            return null;

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get sequence name.",
                                 e);

            return null;

        } finally
        {

            this.releaseConnection (c);

        }

    }

    public void updateLinks (NamedObject d,
                             Set<Link>   newLinks)
                      throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            this.deleteLinks (d,
                              c);

            Iterator<Link> iter = newLinks.iterator ();

            while (iter.hasNext ())
            {

                Link l = iter.next ();

                if ((l.getObject1 () == null)
                    ||
                    (l.getObject2 () == null)
                   )
                {

                    continue;

                }

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

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to update links for: " +
                                 d,
                                 e);

        } finally {

            this.releaseConnection (c);

        }

    }

    public void getLinks (NamedObject d,
                          Connection  conn)
                   throws GeneralException
    {

        if (d == null)
        {

            return;

        }

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

                // Only really need the project when we have actually found something.
                if (this.project == null)
                {

                    throw new IllegalStateException ("No project set yet, either call getProject or setProject first.");

                }

                long   o1key = rs.getLong (1);
                String o1type = rs.getString (2);
                long   o2key = rs.getLong (3);
                String o2type = rs.getString (4);
                long   key = rs.getLong (5);

                if (o1type == null)
                {

                    continue;

                }

                if (o2type == null)
                {

                    continue;

                }

                ObjectReference o1 = new ObjectReference (o1type,
                                                          o1key,
                                                          null);
                ObjectReference o2 = new ObjectReference (o2type,
                                                          o2key,
                                                          null);

                NamedObject d1 = (NamedObject) this.project.getObjectForReference (o1);
                NamedObject d2 = (NamedObject) this.project.getObjectForReference (o2);

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

            this.throwException (conn,
                                 "Unable to get links for: " +
                                 d,
                                 e);

        } finally
        {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public void getLinks (NamedObject d)
                   throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            this.getLinks (d,
                           c);

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get links for: " +
                                 d,
                                 e);

        } finally {

            this.releaseConnection (c);

        }

    }

    // Should merge this with getNewVersion
    private String getNewId (Connection c)
                      throws GeneralException
    {

        boolean releaseConn = false;

        if (c == null)
        {

            releaseConn = true;

            c = this.getConnection ();

        }

        String id = null;

        try
        {

            PreparedStatement rand = c.prepareStatement ("SELECT RANDOM_UUID()");

            int count = 0;

            while (true)
            {

                if (count == 20)
                {

                    throw new GeneralException ("Unable to get new id");

                }

                ResultSet rs = rand.executeQuery ();

                if (rs.next ())
                {

                    id = rs.getString (1);

                }

                List params = new ArrayList ();
                params.add (id);

                rs = this.executeQuery ("SELECT dbkey FROM dataobject WHERE id = ?",
                                        params,
                                        c);

                if (!rs.next ())
                {

                    return id;

                }

                count++;

            }

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get new id",
                                 e);

            return null;

        } finally
        {

            if (releaseConn)
            {

                this.releaseConnection (c);

            }

        }

    }

    // Should merge this with getNewId
    private String getNewVersion (Connection c)
                           throws GeneralException
    {

        boolean releaseConn = false;

        if (c == null)
        {

            releaseConn = true;

            c = this.getConnection ();

        }

        String id = null;

        try
        {

            PreparedStatement rand = c.prepareStatement ("SELECT RANDOM_UUID()");

            int count = 0;

            while (true)
            {

                if (count == 20)
                {

                    throw new GeneralException ("Unable to get new version");

                }

                ResultSet rs = rand.executeQuery ();

                if (rs.next ())
                {

                    id = rs.getString (1);

                }

                List params = new ArrayList ();
                params.add (id);

                rs = this.executeQuery ("SELECT dbkey FROM dataobject WHERE version = ?",
                                        params,
                                        c);

                if (!rs.next ())
                {

                    return id;

                }

                count++;

            }

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get new version",
                                 e);

            return null;

        } finally
        {

            if (releaseConn)
            {

                this.releaseConnection (c);

            }

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

            this.throwException (c,
                                 "Unable to get new key",
                                 e);

        } finally
        {

            if (releaseConn)
            {

                this.releaseConnection (c);

            }

        }

        if (k == -1)
        {

            throw new GeneralException ("Unable to get new key value from sequence: " + this.sequenceName);

        }

        return k;

    }

    public int getSchemaVersion ()
                          throws GeneralException
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

            this.throwException (c,
                                 "Unable to get schema version",
                                 e);

        } finally
        {

            this.releaseConnection (c);

        }

        return -1;

    }

    public static boolean isDatabaseAlreadyInUseException (Throwable e)
    {

        if (e instanceof JdbcSQLException)
        {

            JdbcSQLException ex = (JdbcSQLException) e;

            if (ex.getErrorCode () == org.h2.constant.ErrorCode.DATABASE_ALREADY_OPEN_1)
            {

                return true;

            }

            /*
             *176
            if (ex.getErrorCode () == org.h2.api.ErrorCode.DATABASE_ALREADY_OPEN_1)
            {

                return true;

            }
            */

        }

        Throwable cause = e.getCause ();

        if (cause != null)
        {

            return ObjectManager.isDatabaseAlreadyInUseException (cause);

        }

        return false;


    }

    public static boolean isEncryptionException (Throwable e)
    {

        if (e instanceof JdbcSQLException)
        {

            JdbcSQLException ex = (JdbcSQLException) e;

            if ((ex.getErrorCode () == org.h2.constant.ErrorCode.FILE_ENCRYPTION_ERROR_1)
                ||
                (ex.getErrorCode () == org.h2.constant.ErrorCode.WRONG_USER_OR_PASSWORD)
               )
            {

                return true;

            }

            /*
             *176
            if ((ex.getErrorCode () == org.h2.api.ErrorCode.FILE_ENCRYPTION_ERROR_1)
                ||
                (ex.getErrorCode () == org.h2.api.ErrorCode.WRONG_USER_OR_PASSWORD)
               )
            {

                return true;

            }
            */
        }

        Throwable cause = e.getCause ();

        if (cause != null)
        {

            return ObjectManager.isEncryptionException (cause);

        }

        return false;

    }

    public Connection getConnection ()
                              throws GeneralException
    {

        try
        {

            Connection conn = this.ds.getConnection ();
            conn.setAutoCommit (false);

            return conn;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get connection",
                                        e);

        }

    }

    public void releaseConnection (Connection conn)
    {

        if (conn == null)
        {

            return;

        }

        try
        {

            if (!conn.isClosed ())
            {

                conn.commit ();

            }

            conn.close ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to commit/release connection",
                                  e);

        }

    }

    public void throwException (Connection conn,
                                String     message,
                                Exception  cause)
                         throws GeneralException
    {

        if (conn != null)
        {

            try
            {

                if (!conn.isClosed ())
                {

                    conn.rollback ();

                }

                conn.close ();

            } catch (Exception e) {

                Environment.logError ("Unable to rollback connection",
                                      e);

            }

        }

        throw new GeneralException (message,
                                    cause);

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

                this.throwException (null,
                                     "Unable to get connection",
                                     e);

            }

        }

        try
        {

            this.getLinks (n,
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

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to delete links for: " +
                                 n,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public DataHandler getHandler (Class c)
    {

        DataHandler dh = this.handlers.get (c);

        if (dh == null)
        {

            Class sc = null;

            for (Class s : this.handlers.keySet ())
            {

                if (s.isAssignableFrom (c))
                {

                    sc = s;

                    break;

                }

            }

            if (sc != null)
            {

                dh = this.handlers.get (sc);

                this.handlers.put (c,
                                   dh);

            }

        }

        return dh;


    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects)
                       throws GeneralException
    {

        this.deleteObject (d,
                           deleteChildObjects,
                           null);

    }

    private void deleteAllUserConfigurableObjectFieldsForObject (UserConfigurableObject obj,
                                                                 Connection             conn)
                                                          throws GeneralException
    {

        UserConfigurableObjectFieldDataHandler dh = (UserConfigurableObjectFieldDataHandler) this.getHandler (UserConfigurableObjectField.class); //this.handlers.get (d.getClass ().getName ());

        if (dh == null)
        {

            throw new GeneralException ("Class is not supported.");

        }

        dh.deleteAllFieldsForObject (obj,
                                     conn);

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

        DataHandler dh = this.getHandler (d.getClass ()); //this.handlers.get (d.getClass ().getName ());

        if (dh == null)
        {

            throw new GeneralException ("Class: " +
                                        d.getClass ().getName () +
                                        " is not supported.");

        }

        try
        {

            // Fields are first.
            if (d instanceof UserConfigurableObject)
            {

                UserConfigurableObject o = (UserConfigurableObject) d;

                this.deleteObjects (o.getFields (),
                                    conn);

                this.deleteAllUserConfigurableObjectFieldsForObject (o,
                                                                     conn);

            }

            dh.deleteObject (d,
                             deleteChildObjects,
                             conn);

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

            this.throwException (conn,
                                 "Unable to delete namedobject/dataobject entries for: " +
                                 d,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public void deleteObjects (Collection<? extends DataObject> objs,
                               Connection                       conn)
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

        try
        {

            for (DataObject o : objs)
            {

                this.deleteObject (o,
                                   true,
                                   conn);

            }

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to delete objects: " +
                                 objs,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public void saveObjects (Collection<? extends DataObject> objs,
                             Connection                       conn)
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

        try
        {

            for (DataObject d : objs)
            {

                this.saveObject (d,
                                 conn);

            }

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to save objects: " +
                                 objs,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

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

            // This is just stupid.  Go duck a stick sql...
            try
            {

                this.throwException (conn,
                                     "Unable to create action log entry for: " +
                                     n,
                                     e);

            } catch (Exception ee) {

                Environment.logError ("Cant create action log entry, see cause for details.",
                                      ee);

            }

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public void setLatestVersion (DataObject d,
                                  boolean    latest,
                                  Connection conn)
                           throws GeneralException
    {

        if (d == null)
        {

            throw new IllegalArgumentException ("No object provided.");

        }

        if (d.getVersion () == null)
        {

            throw new IllegalArgumentException ("Object has no version.");

        }

        if (d.getKey () == null)
        {

            throw new IllegalArgumentException ("Object has no key.");

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

            List params = new ArrayList ();
            params.add (latest);
            params.add (d.getKey ());
            params.add (d.getObjectType ());
            params.add (d.getVersion ());

            this.executeStatement ("UPDATE dataobject SET latest = ? WHERE dbkey = ? AND objecttype = ? AND version = ?",
                                   params,
                                   conn);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to update object to latest version: " +
                                 d,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public void saveObject (DataObject d)
                     throws GeneralException
    {

        this.saveObject (d,
                         null);

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

        DataHandler dh = this.getHandler (d.getClass ());
        /*
        Class c = d.getClass ();

        while (true)
        {

            dh = this.getHandler (c);

            if (dh != null)
            {

                break;

            }

            c = c.getSuperclass ();

            if (c == null)
            {

                break;

            }

        }
        */
        //DataHandler dh = this.handlers.get (d.getClass ().getName ());

        if (dh == null)
        {

            throw new GeneralException ("Class: " +
                                        d.getClass ().getName () +
                                        " is not supported.");

        }

        boolean create = false;

        try
        {

            if (d instanceof DataObject)
            {

                if (d.getKey () == null)
                {

                    create = true;

                    // Create the relevant items in dataobject and namedobject.
                    d.setKey (this.getNewKey (conn));

                    if (d.getId () == null)
                    {

                        // Set the id/version.
                        String id = this.getNewId (conn);

                        if (id == null)
                        {

                            throw new GeneralException ("Unable to get new id for object: " +
                                                        d);

                        }

                        d.setId (id);

                    }

                    if (d.getVersion () == null)
                    {

                        String ver = this.getNewVersion (conn);

                        if (ver == null)
                        {

                            throw new GeneralException ("Unable to get new version for object: " +
                                                        d);

                        }

                        d.setVersion (ver);

                    }

                    // Maybe check for the combo being unique?
                    List params = new ArrayList ();
                    params.add (d.getKey ());
                    params.add (d.getObjectType ());
                    params.add (new java.util.Date ());
                    params.add (d.getPropertiesAsString ());
                    params.add (d.getId ());
                    params.add (d.getVersion ());

                    this.executeStatement ("INSERT INTO dataobject (dbkey, objecttype, datecreated, properties, id, version) VALUES (?, ?, ?, ?, ?, ?)",
                                           params,
                                           conn);

                    if (d instanceof NamedObject)
                    {

                        NamedObject n = (NamedObject) d;

                        params = new ArrayList ();
                        params.add (n.getKey ());
                        params.add (n.getName ());

                        if (n instanceof UserConfigurableObject)
                        {

                            UserConfigurableObject o = (UserConfigurableObject) n;

                            UserConfigurableObjectType t = o.getUserConfigurableObjectType ();

                            params.add (t.getKey ());

                        } else {

                            params.add (null);

                        }

                        StringWithMarkup descm = n.getDescription ();

                        String desc = null;
                        String markup = null;

                        if (descm != null)
                        {

                            desc = descm.getText ();

                            if (descm.getMarkup () != null)
                            {

                                markup = descm.getMarkup ().toString ();

                            }

                        }

                        params.add (desc);
                        params.add (markup);

                        params.add (Utils.getFilesAsXML (n.getFiles ()));

                        this.executeStatement ("INSERT INTO namedobject (dbkey, name, userobjecttypedbkey, description, markup, files) VALUES (?, ?, ?, ?, ?, ?)",
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

                } else
                {

                    // Update the dataobject and namedobject.
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

                            NamedObject oldN = (NamedObject) this.getObjectByKey (n.getClass (),
                                                                                  n.getKey ().intValue (),
                                                                                  n.getParent (),
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

                        StringWithMarkup descm = n.getDescription ();

                        String desc = null;
                        String markup = null;

                        if (descm != null)
                        {

                            desc = descm.getText ();

                            if (descm.getMarkup () != null)
                            {

                                markup = descm.getMarkup ().toString ();

                            }

                        }

                        params = new ArrayList ();
                        params.add (n.getName ());
                        params.add (n.getLastModified ());
                        params.add (desc);
                        params.add (markup);
                        params.add (Utils.getFilesAsXML (n.getFiles ()));
                        params.add (n.getKey ());

                        this.executeStatement ("UPDATE namedobject SET name = ?, lastmodified = ?, description = ?, markup = ?, files = ? WHERE dbkey = ?",
                                               params,
                                               conn);

                        for (Note nn : n.getNotes ())
                        {

                            this.saveObject (nn,
                                             conn);

                        }

                    }

                }

                if (d instanceof UserConfigurableObject)
                {

                    UserConfigurableObject o = (UserConfigurableObject) d;

                    this.saveObjects (o.getFields (),
                                      conn);

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

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to create/update object: " +
                                 d,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public Project getProjectAtVersion (ProjectVersion pv)
                                 throws GeneralException
    {

        if (pv == null)
        {

            return null;

        }

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

            long start = System.currentTimeMillis ();

            ProjectDataHandler pdh = (ProjectDataHandler) this.getHandler (Project.class);

            Project proj = pdh.getProjectAtVersion (pv,
                                                    conn);

            Environment.logDebugMessage ("Project db read for version: " +
                                         pv +
                                         " took: " + (System.currentTimeMillis () - start));

            return proj;

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to get project for version: " +
                                 pv,
                                 e);

        } finally {

            this.releaseConnection (conn);

        }

        return null;

    }

    public void setProject (Project p)
    {

        if (this.project != null)
        {

            throw new IllegalStateException ("Already have a project: " +
                                             this.project);

        }

        p.setProjectDirectory (this.dir);

        this.project = p;

    }

    public Project getProject ()
                        throws GeneralException
    {

        if (this.project != null)
        {

            return this.project;

        }

        Project proj = null;

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

            long start = System.currentTimeMillis ();

            ProjectDataHandler pdh = (ProjectDataHandler) this.getHandler (Project.class);

            proj = pdh.getProject (conn);

            Environment.logDebugMessage ("Project db read took: " + (System.currentTimeMillis () - start));

            if (proj == null)
            {

                return null;

            }

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to get project",
                                 e);

        } finally {

            this.releaseConnection (conn);

        }

        proj.setProjectDirectory (this.dir);

        this.project = proj;

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

            releaseConn = true;

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

            this.throwException (conn,
                                 "Unable to execute sql: " +
                                 sql +
                                 "\nusing params: " +
                                 params,
                                 e);

        } finally
        {

            if (releaseConn)
            {

                this.releaseConnection (conn);

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

        try
        {

            // Get the schema creation script.
            this.runUpgradeScript (0,
                                   1,
                                   conn,
                                   false);

            this.runCreateViewsScript (conn);

            // Finally update the schema version to the current version (we do 0-1 here to get
            // things rolling but it may not be the latest version).
            this.updateSchemaVersion (this.getLatestSchemaVersion (),
                                      conn);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to create schema",
                                 e);

        } finally {

            this.releaseConnection (conn);

        }

    }

    /**
     * Get the current/latest version of the schema that is available.  This is in contrast
     * to getSchemaVersion which should return the current version of the actual schema being
     * used.
     *
     * @returns The version.
     */
    public int getLatestSchemaVersion ()
    {

        return Environment.getSchemaVersion ();

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

        try
        {

            // Get the update script.
            while (nVer < (newVersion + 1))
            {

                // Get the script.
                this.runUpgradeScript (oVer,
                                       nVer,
                                       conn,
                                       false);

                oVer = nVer;
                nVer++;

            }

            // Run the create views script.
            this.runCreateViewsScript (conn);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to run update script: " + oVer + "-" + nVer,
                                 e);

        } finally {

            this.releaseConnection (conn);

        }

    }

    public String getCreateViewsFile ()
    {

        return Constants.UPDATE_SCRIPTS_DIR + "/create-views.xml";

    }

    private void runCreateViewsScript (Connection conn)
                                throws GeneralException
    {

        try
        {

            String f = this.getCreateViewsFile ();

            String xml = Utils.getResourceFileAsString (f);

            if (xml == null)
            {

                return;

            }

            Element root = JDOMUtils.getStringAsElement (xml);

            this.runScriptElements (root,
                                    conn,
                                    false);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create views",
                                        e);

        }

    }

    public void forceRunUpgradeScript (int version)
                                       throws GeneralException
    {

        if (!Environment.isDebugModeEnabled ())
        {

            throw new GeneralException ("This is a debug method and can only be called in debug mode.");

        }

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get connection",
                                        e);

        }

        try
        {

            this.runUpgradeScript ((version - 1),
                                   version,
                                   conn,
                                   true);

            // Run the create views script.
            this.runCreateViewsScript (conn);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to force run upgrade script: " + (version - 1) + "-" + version,
                                 e);

        } finally {

            this.releaseConnection (conn);

        }

    }

    public String getUpgradeScriptFile (int oldVersion,
                                        int newVersion)
    {

        return Constants.UPDATE_SCRIPTS_DIR + "/" + oldVersion + "-" + newVersion + ".xml";

    }

    private void runUpgradeScript (int        oldVersion,
                                   int        newVersion,
                                   Connection conn,
                                   boolean    allowAllToFail)
                            throws GeneralException
    {

        try
        {

            String f = this.getUpgradeScriptFile (oldVersion,
                                                  newVersion);

            String xml = Utils.getResourceFileAsString (f);

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
                                    conn,
                                    allowAllToFail);

            this.updateSchemaVersion (newVersion,
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

    public void updateSchemaVersion (int        newVersion,
                                     Connection conn)
                              throws Exception
    {

        List params = new ArrayList ();
        params.add (newVersion);

        this.executeStatement ("UPDATE project SET schema_version = ?",
                               params,
                               conn);

    }

    public String getSchemaFile (String file)
    {

        return Constants.SCHEMA_DIR + file;

    }

    private void runScriptElements (Element    root,
                                    Connection conn,
                                    boolean    allowAllToFail)
                             throws GeneralException,
                                    JDOMException
    {

        if ((allowAllToFail)
            &&
            (!Environment.isDebugModeEnabled ())
           )
        {

            throw new IllegalStateException ("allowAllToFail can only be set to true when in debug mode.");

        }

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

                sql = Utils.getResourceFileAsString (this.getSchemaFile (file));

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

                if ((canFail)
                    ||
                    (allowAllToFail)
                   )
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

    private List<File> getBackupFiles (Project project)
                                throws Exception
    {

        File dir = project.getBackupDirectory ();

        if (dir == null)
        {

            return null;

        }

        List<File> ret = new ArrayList ();

        File[] files = dir.listFiles ();

        if (files != null)
        {

            for (int i = 0; i < files.length; i++)
            {

                File f = files[i];

                if ((f.getName ().startsWith ("backup")) &&
                    (f.getName ().endsWith (".zip")))
                {

                    ret.add (f);

                }

            }

        }

        Query q = new Query ();
        q.parse (String.format ("SELECT * FROM %s ORDER BY lastModified DESC",
                                File.class.getName ()));

        QueryResults qr = q.execute (ret);

        ret = (List<File>) qr.getResults ();

        return ret;

    }

    public File getLastBackupFile (Project project)
                            throws Exception
    {

        List<File> files = this.getBackupFiles (project);

        if (files == null)
        {

            return null;

        }

        if (files.size () > 0)
        {

            return files.get (0);

        }

        return null;

    }

    public void pruneBackups (Project project,
                              int     keepCount)
                       throws Exception
    {

        File dir = project.getBackupDirectory ();

        if (dir == null)
        {

            return;

        }

        if (keepCount < 1)
        {

            return;

        }

        List<File> files = this.getBackupFiles (project);

        if (files == null)
        {

            return;

        }

        if (files.size () > keepCount)
        {

            files = files.subList (keepCount,
                                   files.size ());

            // Delete the files.
            for (File f : files)
            {

                if (f.delete ())
                {

                    this.createActionLogEntry (project,
                                               "Pruned backup, file: " + f.getPath (),
                                               null,
                                               null);

                }

            }

        }

    }

    public File createBackup (Project project,
                              int     keepCount)
                       throws Exception
    {

        File dir = project.getBackupDirectory ();

        if (dir == null)
        {

            return null;

        }

        dir.mkdirs ();

        // Indicate that this directory can be deleted.
        Utils.createQuollWriterDirFile (dir);

        File last = this.getLastBackupFile (project);

        int ver = 0;

        if (last != null)
        {

            // Split the filename, get the version.
            String n = last.getName ().substring (6,
                                                  last.getName ().length () - 4);

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

        ver++;

        File f = new File (dir.getPath () + "/backup" + ver + ".zip");

        Connection conn = null;

        try
        {

            conn = this.getConnection ();

            List params = new ArrayList ();
            params.add (f.getPath ());

            this.executeStatement ("BACKUP TO ?",
                                   params,
                                   conn);

            this.releaseConnection (conn);

            this.createActionLogEntry (project,
                                       "Created backup, written to file: " + f.getPath (),
                                       null,
                                       null);

            // See if there is a files dir.
            if (!Utils.isDirectoryEmpty (project.getFilesDirectory ()))
            {

                // Add the files dir to the backup.
                Utils.addDirToZip (f,
                                   project.getFilesDirectory ());

            }

            // Test the backup?

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to create backup to: " +
                                 f,
                                 e);

        }

        this.pruneBackups (project,
                           keepCount);

        return f;

    }

    public void setUserConfigurableObjectTypeForObjectsOfType (String                     objType,
                                                               UserConfigurableObjectType type,
                                                               Connection                 conn)
                                                        throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.getConnection ();

            closeConn = true;

        }

        try
        {

            List params = new ArrayList ();
            params.add (type.getKey ());
            params.add (objType);

            this.executeStatement ("UPDATE namedobject SET userobjecttypedbkey = ? WHERE dbkey IN (SELECT dbkey FROM dataobject WHERE objecttype = ?)",
                                   params,
                                   conn);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to set user config object type for: " +
                                 objType +
                                 " and: " +
                                 type,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

    }

    public void setUserConfigurableObjectFields (UserConfigurableObject obj,
                                                 Connection             conn)
                                          throws GeneralException
    {

        Set<UserConfigurableObjectField> fields = this.getUserConfigurableObjectFields (obj,
                                                                                        conn);

        for (UserConfigurableObjectField f : fields)
        {

            obj.addField (f);

        }

    }

    public Set<UserConfigurableObjectField> getUserConfigurableObjectFields (UserConfigurableObject obj,
                                                                             Connection             conn)
                                                                      throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.getConnection ();

            closeConn = true;

        }

        try
        {

            return new LinkedHashSet (this.getObjects (UserConfigurableObjectField.class,
                                                       obj,
                                                       conn,
                                                       true));

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to get user object fields for: " +
                                 obj,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

        return null;

    }

    public UserConfigurableObjectType getUserConfigurableObjectType (long        key,
                                                                     NamedObject obj,
                                                                     Connection  conn)
                                                              throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.getConnection ();

            closeConn = true;

        }

        try
        {

            return (UserConfigurableObjectType) this.getObjectByKey (UserConfigurableObjectType.class,
                                                                     key,
                                                                     obj,
                                                                     conn,
                                                                     true);

        } catch (Exception e) {

            this.throwException (conn,
                                 "Unable to get user config object type for: " +
                                 obj,
                                 e);

        } finally {

            if (closeConn)
            {

                this.releaseConnection (conn);

            }

        }

        return null;

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

                Environment.logError ("Unable to close pool",
                                      e);

            }

        }

    }
/*
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

            try
            {

                this.throwException (conn,
                                     "Unable to save session word counts for project: " +
                                     this.project,
                                     e);

            } catch (Exception ee) {

                Environment.logError ("Unable to save session word counts, see cause for details",
                                      ee);

            }

        } finally
        {

            this.releaseConnection (conn);

        }

    }
*/
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
