package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class NoteDataHandler implements DataHandler<Note, NamedObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT objectdbkey, dbkey, name, description, lastmodified, datecreated, properties, due, dealtwith, type, position, end_position, id, version FROM note_v ";
    private ObjectManager objectManager = null;

    public NoteDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Note getNote (ResultSet rs,
                          boolean   loadChildObjects)
                   throws GeneralException
    {

        try
        {

            int ind = 1;

            long objKey = rs.getLong (ind++);

            // Create a bogus object (to hold the object key)
            NamedObject obj = new BlankNamedObject ();
            obj.setKey (objKey);

            long key = rs.getLong (ind++);

            Note n = new Note ();
            n.setObject (obj);
            n.setKey (key);
            n.setName (rs.getString (ind++));
            n.setDescription (rs.getString (ind++));

            n.setLastModified (rs.getTimestamp (ind++));
            n.setDateCreated (rs.getTimestamp (ind++));
            n.setPropertiesAsString (rs.getString (ind++));

            n.setDue (rs.getTimestamp (ind++));
            n.setDealtWith (rs.getTimestamp (ind++));
            n.setType (rs.getString (ind++));
            n.setPosition (rs.getInt (ind++));
            n.setEndPosition (rs.getInt (ind++));
            n.setId (rs.getString (ind++));
            n.setVersion (rs.getString (ind++));            

            return n;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load note",
                                        e);

        }

    }

    public List<Note> getObjects (NamedObject parent,
                                  Connection  conn,
                                  boolean     loadChildObjects)
                           throws GeneralException
    {

        try
        {

            List<Note> ret = new ArrayList ();
        
            List params = new ArrayList ();
            params.add (parent.getKey ());
            params.add (parent.getVersion ());
        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE objectdbkey = ? AND objectversion = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getNote (rs,
                                       loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

            return ret;
                
        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get notes for: " +
                                        parent,
                                        e);

        }
            
    }

    public Note getObjectByKey (int         key,
                                NamedObject parent,
                                Connection conn,
                                boolean    loadChildObjects)
                         throws GeneralException
    {

        try
        {
    
            List params = new ArrayList ();
            params.add (key);
        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                            params,
                                                            conn);
    
            if (rs.next ())
            {
    
                return this.getNote (rs,
                                     loadChildObjects);
    
            }
    
            return null;
        
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to get note with key: " +
                                        key,
                                        e);
                
        }
        
    }

    public Set<Note> getDealtWith (ProjectVersion pv,
                                   boolean        isDealtWith,
                                   Connection     conn)
                            throws GeneralException
    {

        if (pv == null)
        {
            
            throw new IllegalArgumentException ("Must provide a project version");
            
        }
    
        boolean closeConn = false;
        
        if (conn == null)
        {
            
            conn = this.objectManager.getConnection ();
            
            closeConn = true;
            
        }
        
        try
        {

            Set<Note> ret = new LinkedHashSet ();
        
            List params = new ArrayList ();
            params.add (pv.getKey ());
        
            ResultSet rs = this.objectManager.executeQuery (String.format ("%s WHERE dealtwith IS %s NULL AND objectdbkey IN (SELECT dbkey FROM chapter WHERE projectversiondbkey = ?) ORDER BY datecreated",
                                                                           STD_SELECT_PREFIX,
                                                                           (isDealtWith ? "NOT" : "")),
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getNote (rs,
                                       true));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

            return ret;
                                                                                    
        } catch (Exception e) {
            
            this.objectManager.throwException (conn,
                                               "Unable to get notes for project version: " +
                                               pv,
                                               e);
            
        } finally {
                        
            if (closeConn)
            {
                
                this.objectManager.releaseConnection (conn);
                
            }
            
        }        
        
        return null;
        
    }
    
    public int getDealtWithCount (ProjectVersion projVer,
                                  boolean        isDealtWith,
                                  Connection     conn)
                           throws GeneralException
    {
        
        if (projVer == null)
        {
            
            throw new IllegalArgumentException ("Must provide a project version");
            
        }
        
        boolean closeConn = false;
        
        if (conn == null)
        {
            
            conn = this.objectManager.getConnection ();
            
            closeConn = true;
            
        }
        
        try
        {

            List params = new ArrayList ();
            params.add (projVer.getKey ());
        
            ResultSet rs = this.objectManager.executeQuery (String.format ("SELECT COUNT(*) FROM note_v WHERE dealtwith IS %s NULL AND objectdbkey IN (SELECT dbkey FROM chapter WHERE projectversiondbkey = ?)",
                                                                           (isDealtWith ? "NOT" : "")),
                                                            params,
                                                            conn);
        
            if (rs.next ())
            {

                return rs.getInt (1); 

            }
                        
        } catch (Exception e) {
            
            this.objectManager.throwException (conn,
                                               "Unable to get count of notes for project version: " +
                                               projVer,
                                               e);
            
        } finally {
                        
            if (closeConn)
            {
                
                this.objectManager.releaseConnection (conn);
                
            }
            
        }        
        
        return 0;
        
    }
    
    /**
     * Get a count of the dealt with notes for all versions except that passed in.
     */
    public int getDealtWithCountForOtherVersions (ProjectVersion projVer,
                                                  boolean        isDealtWith,
                                                  Connection     conn)
                                           throws GeneralException
    {
        
        if (projVer == null)
        {
            
            throw new IllegalArgumentException ("Must provide a project version");
            
        }
        
        boolean closeConn = false;
        
        if (conn == null)
        {
            
            conn = this.objectManager.getConnection ();
            
            closeConn = true;
            
        }
        
        try
        {

            List params = new ArrayList ();
            params.add (projVer.getKey ());
        
            ResultSet rs = this.objectManager.executeQuery (String.format ("SELECT COUNT(*) FROM note_v WHERE dealtwith IS %s NULL AND objectdbkey IN (SELECT dbkey FROM chapter WHERE projectversiondbkey <> ?)",
                                                                           (isDealtWith ? "NOT" : "")),
                                                            params,
                                                            conn);
        
            if (rs.next ())
            {

                return rs.getInt (1); 

            }
                        
        } catch (Exception e) {
            
            this.objectManager.throwException (conn,
                                               "Unable to get count of notes for project version: " +
                                               projVer,
                                               e);
            
        } finally {
                        
            if (closeConn)
            {
                
                this.objectManager.releaseConnection (conn);
                
            }
            
        }        
        
        return 0;
        
    }

    public void setObjectNotesToLatest (DataObject d,
                                        boolean    latest,
                                        Connection conn)
                                 throws GeneralException
    {
        
        if (d == null)
        {
            
            return;
            
        }
        
        boolean closeConn = false;
        
        if (conn == null)
        {
            
            conn = this.objectManager.getConnection ();
            
            closeConn = true;
            
        }
        
        try
        {

            List params = new ArrayList ();
            params.add (latest);
            params.add (Note.OBJECT_TYPE);
            params.add (d.getKey ());
            params.add (d.getVersion ());
        
            this.objectManager.executeStatement ("UPDATE dataobject SET latest = ? WHERE objecttype = ? AND dbkey IN (SELECT dbkey FROM note WHERE objectdbkey = ? AND objectversion = ?)",
                                                 params,
                                                 conn);
        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to update notes to be latest for object: " +
                                               d,
                                               e);
            
        } finally {
            
            if (closeConn)
            {
                
                this.objectManager.releaseConnection (conn);
                
            }
            
        }
        
    }
    
    public void createObject (Note       d,
                              Connection conn)
                       throws GeneralException
    {

        Note n = (Note) d;

        List params = new ArrayList ();
        params.add (n.getKey ());
        params.add (n.getDue ());
        params.add (n.getDealtWith ());
        params.add (n.getType ());
        params.add (n.getPosition ());
        params.add (n.getEndPosition ());
        params.add (n.getObject ().getKey ());
        params.add (n.getObject ().getVersion ());

        this.objectManager.executeStatement ("INSERT INTO note (dbkey, due, dealtwith, type, position, end_position, objectdbkey, objectversion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (Note       d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        Note n = (Note) d;

        List params = new ArrayList ();
        params.add (n.getKey ());

        this.objectManager.executeStatement ("DELETE FROM note WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (Note       d,
                              Connection conn)
                       throws GeneralException
    {

        Note n = (Note) d;

        List params = new ArrayList ();
        params.add (n.getDue ());
        params.add (n.getDealtWith ());
        params.add (n.getType ());
        params.add (n.getObject ().getKey ());
        params.add (n.getObject ().getVersion ());
        params.add (n.getPosition ());
        params.add (n.getEndPosition ());
        params.add (n.getKey ());

        this.objectManager.executeStatement ("UPDATE note SET due = ?, dealtwith = ?, type = ?, objectdbkey = ?, objectversion = ?, position = ?, end_position = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
