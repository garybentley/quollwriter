package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class TagDataHandler implements DataHandler<Tag, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, markup, files, lastmodified, datecreated, properties, id, version FROM tag_v ";
    private ObjectManager objectManager = null;

    public TagDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    private Tag getTag (ResultSet rs,
                        Project   parent,
                        boolean   loadChildObjects)
                 throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Tag n = new Tag ();
            n.setKey (key);
            n.setName (rs.getString (ind++));
            n.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            n.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            n.setLastModified (rs.getTimestamp (ind++));
            n.setDateCreated (rs.getTimestamp (ind++));
            n.setPropertiesAsString (rs.getString (ind++));

            n.setId (rs.getString (ind++));
            n.setVersion (rs.getString (ind++));
/*
            if (parent != null)
            {

                parent.addTag (n);

            }
*/
            return n;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load tag",
                                        e);

        }

    }

    public List<Tag> getObjects (Project     parent,
                                 Connection  conn,
                                 boolean     loadChildObjects)
                          throws GeneralException
    {

        try
        {

            List<Tag> ret = new ArrayList<> ();

            List params = new ArrayList ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX,
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getTag (rs,
                                      parent,
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

            throw new GeneralException ("Unable to get tags for: " +
                                        parent,
                                        e);

        }

    }

    public Tag getObjectByKey (long       key,
                               Project    parent,
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

                return this.getTag (rs,
                                    parent,
                                    loadChildObjects);

            }

            return null;

        } catch (Exception e) {

            throw new GeneralException ("Unable to get tag with key: " +
                                        key,
                                        e);

        }

    }

    public void createObject (Tag        n,
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (n.getKey ());

        this.objectManager.executeStatement ("INSERT INTO tag (dbkey) VALUES (?)",
                                             params,
                                             conn);

    }

    public void deleteObject (Tag        d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (d.getKey ());

        this.objectManager.executeStatement ("DELETE FROM tag WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (Tag        d,
                              Connection conn)
                       throws GeneralException
    {

        // Do nothing.

    }

}
