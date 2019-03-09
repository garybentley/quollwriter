package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class LocationDataHandler implements DataHandler<Location, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, markup, files, lastmodified, datecreated, properties, id, version FROM location_v ";

    private ObjectManager objectManager = null;

    public LocationDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Location getLocation (ResultSet rs,
                                  Project   proj,
                                  boolean   loadChildObjects)
                           throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Location l = new Location ();
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
            l.setId (rs.getString (ind++));
            l.setVersion (rs.getString (ind++));

            if (proj != null)
            {

                proj.addAsset (l);

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

            throw new GeneralException ("Unable to load location",
                                        e);

        }

    }

    public List<Location> getObjects (Project     parent,
                                      Connection  conn,
                                      boolean     loadChildObjects)
                               throws GeneralException
    {

        List<Location> ret = new ArrayList<> ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND projectdbkey = ?",
                                                            Arrays.asList (parent.getKey ()),
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getLocation (rs,
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

            throw new GeneralException ("Unable to load locations for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public Location getObjectByKey (long       key,
                                    Project    proj,
                                    Connection conn,
                                    boolean    loadChildObjects)
                             throws GeneralException
    {

        Location i = null;

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",// AND projectdbkey = ?",
                                                            Arrays.asList (key),
                                                            conn);

            if (rs.next ())
            {

                i = this.getLocation (rs,
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

            throw new GeneralException ("Unable to load location for key: " +
                                        key,
                                        e);

        }

        return i;

    }

    public void createObject (Location   d,
                              Connection conn)
                       throws GeneralException
    {

        Location l = (Location) d;

        this.objectManager.executeStatement ("INSERT INTO location (dbkey, projectdbkey) VALUES (?, ?)",
                                             Arrays.asList (l.getKey (),
                                                            l.getProject ().getKey ()),
                                             conn);

    }

    public void deleteObject (Location   d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        Location l = (Location) d;

        this.objectManager.executeStatement ("DELETE FROM location WHERE dbkey = ?",
                                             Arrays.asList (l.getKey ()),
                                             conn);

    }

    public void updateObject (Location   d,
                              Connection conn)
                       throws GeneralException
    {

    }

}
