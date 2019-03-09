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

            // Load the object fields.
            this.objectManager.setUserConfigurableObjectFields (l,
                                                                rs.getStatement ().getConnection ());

            l.setName (rs.getString (ind++));
            l.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));

            l.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            l.setLastModified (rs.getTimestamp (ind++));
            l.setDateCreated (rs.getTimestamp (ind++));
            l.setPropertiesAsString (rs.getString (ind++));

            if (l.getLegacyField (QObject.TYPE_LEGACY_FIELD_ID) == null)
            {

                l.setType (rs.getString (ind++));

            } else {

                ind++;

            }

            l.setId (rs.getString (ind++));
            l.setVersion (rs.getString (ind++));

            // Get all the notes.
            if (loadChildObjects)
            {

                this.objectManager.loadNotes (l,
                                              rs.getStatement ().getConnection ());

            }

            if (proj != null)
            {

                proj.addAsset (l);

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

        List<QObject> ret = new ArrayList<> ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND projectdbkey = ?",
                                                            Arrays.asList (parent.getKey ()),
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
    public QObject getObjectByKey (long       key,
                                   Project    proj,
                                   Connection conn,
                                   boolean    loadChildObjects)
                            throws GeneralException
    {

        QObject i = null;

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?", // AND projectdbkey = ?",
                                                            Arrays.asList (key),
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

        this.objectManager.executeStatement ("INSERT INTO qobject (dbkey, type, projectdbkey) VALUES (?, ?, ?)",
                                             Arrays.asList (o.getKey (),
                                                            o.getType (),
                                                            o.getProject ().getKey ()),
                                             conn);

    }

    @Override
    public void deleteObject (QObject    o,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("DELETE FROM qobject WHERE dbkey = ?",
                                             Arrays.asList (o.getKey ()),
                                             conn);

    }

    @Override
    public void updateObject (QObject    o,
                              Connection conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE qobject SET type = ? WHERE dbkey = ?",
                                             Arrays.asList (o.getType (),
                                                            o.getKey ()),
                                             conn);

    }

}
