package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class LocationDataHandler implements DataHandler<Location, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, lastmodified, datecreated, properties, id, version FROM location_v ";

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
            l.setName (rs.getString (ind++));
            l.setDescription (rs.getString (ind++));

            l.setLastModified (rs.getTimestamp (ind++));
            l.setDateCreated (rs.getTimestamp (ind++));
            l.setPropertiesAsString (rs.getString (ind++));
            l.setId (rs.getString (ind++));
            l.setVersion (rs.getString (ind++));            

            if (proj != null)
            {
                
                proj.addLocation (l);
                
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

        List<Location> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND projectdbkey = ?",
                                                            params,
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

    public Location getObjectByKey (int        key,
                                    Project    proj,
                                    Connection conn,
                                    boolean    loadChildObjects)
                             throws GeneralException
    {

        Location i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);
            //params.add (proj.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",// AND projectdbkey = ?",
                                                            params,
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

        List params = new ArrayList ();
        params.add (l.getKey ());
        params.add (l.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO location (dbkey, projectdbkey) VALUES (?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (Location   d,
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

    public void updateObject (Location   d,
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
