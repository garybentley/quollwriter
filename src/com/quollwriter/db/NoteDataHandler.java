package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class NoteDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;
    private List<Note>    allNotes = new ArrayList ();
    private boolean       loaded = false;

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
            n.setType (rs.getString (ind++));
            n.setPosition (rs.getInt (ind++));
            n.setEndPosition (rs.getInt (ind++));

            return n;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load note",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        this.loadAllNotes (conn,
                           loadChildObjects);

        List<Note> ret = new ArrayList ();

        for (Note n : this.allNotes)
        {

            // Parent should be a "book" here.
            if (n.getObject ().getKey ().equals (parent.getKey ()))
            {

                ret.add (n);

            }

        }

        return ret;
/*
        List<Note> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, due, type, position FROM note_v WHERE objectdbkey = ? ORDER BY position",
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

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load notes for: " +
                                        parent,
                                        e);

        }

        return ret;
*/
    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        this.loadAllNotes (conn,
                           loadChildObjects);

        for (Note n : this.allNotes)
        {

            if (n.getObject ().getKey ().intValue () == key)
            {

                return n;

            }

        }

        return null;

/*
        Note i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, due, type, position, end_position FROM note_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getNote (rs,
                                  loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load note for key: " +
                                        key,
                                        e);

        }

        return i;
*/
    }

    private void loadAllNotes (Connection conn,
                               boolean    loadChildObjects)
                        throws GeneralException
    {

        if (!this.loaded)
        {

            this.loaded = true;

            try
            {

                ResultSet rs = this.objectManager.executeQuery ("SELECT objectdbkey, dbkey, name, description, lastmodified, datecreated, properties, due, type, position, end_position FROM note_v",
                                                                null,
                                                                conn);

                while (rs.next ())
                {

                    this.addNote (this.getNote (rs,
                                                loadChildObjects));

                }

                try
                {

                    rs.close ();

                } catch (Exception e)
                {
                }

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load all notes",
                                            e);

            }

        }

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Note n = (Note) d;

        List params = new ArrayList ();
        params.add (n.getKey ());
        params.add (n.getDue ());
        params.add (n.getType ());
        params.add (n.getPosition ());
        params.add (n.getEndPosition ());
        params.add (n.getObject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO note (dbkey, due, type, position, end_position, objectdbkey) VALUES (?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);

        this.addNote (n);

    }

    private void addNote (Note n)
    {

        this.allNotes.add (n);

    }

    public void deleteObject (DataObject d,
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

        this.allNotes.remove (n);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Note n = (Note) d;

        List params = new ArrayList ();
        params.add (n.getDue ());
        params.add (n.getType ());
        params.add (n.getObject ().getKey ());
        params.add (n.getPosition ());
        params.add (n.getEndPosition ());
        params.add (n.getKey ());

        this.objectManager.executeStatement ("UPDATE note SET due = ?, type = ?, objectdbkey = ?, position = ?, end_position = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
