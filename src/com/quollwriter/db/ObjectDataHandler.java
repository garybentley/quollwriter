package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class ObjectDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public ObjectDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private QObject getQObject (ResultSet rs,
                                boolean   loadChildObjects)
                         throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            QObject l = new QObject ();
            l.setKey (key);
            l.setName (rs.getString (ind++));
            l.setDescription (rs.getString (ind++));

            l.setLastModified (rs.getTimestamp (ind++));
            l.setDateCreated (rs.getTimestamp (ind++));
            l.setPropertiesAsString (rs.getString (ind++));
            l.setType (rs.getString (ind++));

            // Get all the notes.
            if (loadChildObjects)
            {
                /*
                this.objectManager.loadNotes (l,
                                              rs.getStatement ().getConnection ());
                 */
            }

            return l;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load qobject",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<QObject> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, type FROM qobject_v WHERE projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getQObject (rs,
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

            throw new GeneralException ("Unable to load objects for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        QObject i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, type FROM qobject_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getQObject (rs,
                                     loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load object for key: " +
                                        key,
                                        e);

        }

        return i;

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        QObject o = (QObject) d;

        List params = new ArrayList ();
        params.add (o.getKey ());
        params.add (o.getType ());
        params.add (o.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO qobject (dbkey, type, projectdbkey) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        QObject o = (QObject) d;

        List params = new ArrayList ();
        params.add (o.getKey ());

        this.objectManager.executeStatement ("DELETE FROM qobject WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        QObject o = (QObject) d;

        List params = new ArrayList ();
        params.add (o.getType ());
        params.add (o.getKey ());
        this.objectManager.executeStatement ("UPDATE qobject SET type = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
