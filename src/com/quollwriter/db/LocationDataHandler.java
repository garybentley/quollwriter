package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class LocationDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public LocationDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Location getLocation (ResultSet rs,
                                  boolean   loadChildObjects)
                           throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Location l = new Location ();
            l.setKey (key);
            l.setName (rs.getString (ind++));
            l.setDescription (rs.getString (ind++));

            l.setLastModified (rs.getTimestamp (ind++));
            l.setDateCreated (rs.getTimestamp (ind++));
            l.setPropertiesAsString (rs.getString (ind++));

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

            throw new GeneralException ("Unable to load location",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<Location> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties FROM location_v WHERE projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getLocation (rs,
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

            throw new GeneralException ("Unable to load locations for: " +
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

        Location i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties FROM location_v  WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getLocation (rs,
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

            throw new GeneralException ("Unable to load location for key: " +
                                        key,
                                        e);

        }

        return i;

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Location l = (Location) d;

        List params = new ArrayList ();
        params.add (l.getKey ());
        params.add (l.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO location (dbkey, projectdbkey) VALUES (?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        Location l = (Location) d;

        List params = new ArrayList ();
        params.add (l.getKey ());

        this.objectManager.executeStatement ("DELETE FROM location WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Location l = (Location) d;

        List params = new ArrayList ();
        params.add (l.getKey ());
        /*
        this.objectManager.executeStatement ("UPDATE location SET ? WHERE dbkey = ?",
                                             params,
                                             conn);
         */
    }

}
