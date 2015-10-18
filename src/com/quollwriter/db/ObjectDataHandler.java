package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class ObjectDataHandler implements DataHandler<QObject, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, markup, files, lastmodified, datecreated, properties, type, id, version FROM qobject_v ";
    private ObjectManager objectManager = null;

    public ObjectDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private QObject getQObject (ResultSet rs,
                                Project   proj,
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
            l.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            l.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            l.setLastModified (rs.getTimestamp (ind++));
            l.setDateCreated (rs.getTimestamp (ind++));
            l.setPropertiesAsString (rs.getString (ind++));
            l.setType (rs.getString (ind++));
            l.setId (rs.getString (ind++));
            l.setVersion (rs.getString (ind++));            

            if (proj != null)
            {
                
                proj.addQObject (l);
                
            }
            
            // Get all the notes.
            if (loadChildObjects)
            {
                
                this.objectManager.loadNotes (l,
                                              rs.getStatement ().getConnection ());
                 
            }

            return l;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load qobject",
                                        e);

        }

    }

    @Override
    public List<QObject> getObjects (Project     parent,
                                     Connection  conn,
                                     boolean     loadChildObjects)
                              throws GeneralException
    {

        List<QObject> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getQObject (rs,
                                          parent,
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

    @Override
    public QObject getObjectByKey (int        key,
                                   Project    proj,
                                   Connection conn,
                                   boolean    loadChildObjects)
                            throws GeneralException
    {

        QObject i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);
            //params.add (proj.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?", // AND projectdbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getQObject (rs,
                                     proj,
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

    @Override
    public void createObject (QObject    o,
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (o.getKey ());
        params.add (o.getType ());
        params.add (o.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO qobject (dbkey, type, projectdbkey) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    @Override
    public void deleteObject (QObject    o,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (o.getKey ());

        this.objectManager.executeStatement ("DELETE FROM qobject WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (QObject    o,
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (o.getType ());
        params.add (o.getKey ());
        this.objectManager.executeStatement ("UPDATE qobject SET type = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
