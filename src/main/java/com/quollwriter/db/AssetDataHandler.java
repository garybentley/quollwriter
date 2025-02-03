package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class AssetDataHandler implements DataHandler<Asset, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, userobjecttypedbkey, name, description, markup, files, lastmodified, datecreated, properties, id, version FROM asset_v ";

    private ObjectManager objectManager = null;

    public AssetDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    private Asset getAsset (ResultSet rs,
                            Project   parent,
                            boolean   loadChildObjects)
                     throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            long typekey = rs.getLong (ind++);

            // Try and get the type from the project (the default now).
            UserConfigurableObjectType t = parent.getUserConfigurableObjectType (typekey);

            if (t == null)
            {

                // This isn't an error, the type may have been removed without the requesting project knowing about
                // the deletion.
                return null;

            }

            Asset f = new Asset (t);

            f.setKey (key);

            // Load the object fields.
            this.objectManager.setUserConfigurableObjectFields (f,
                                                                rs.getStatement ().getConnection ());

            f.setName (rs.getString (ind++));
            f.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));

            f.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            f.setLastModified (rs.getTimestamp (ind++));
            f.setDateCreated (rs.getTimestamp (ind++));
            f.setPropertiesAsString (rs.getString (ind++));
            f.setId (rs.getString (ind++));
            f.setVersion (rs.getString (ind++));
            f.setParent (parent);

            // Get all the notes.
            if (loadChildObjects)
            {

                this.objectManager.loadNotes (f,
                                              rs.getStatement ().getConnection ());

            }

            if (parent != null)
            {

                parent.addAsset (f);

            }

            return f;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load asset",
                                        e);

        }

    }

    public List<Asset> getObjects (Project    parent,
                                   Connection conn,
                                   boolean    loadChildObjects)
                            throws GeneralException
    {

        List<Asset> ret = new ArrayList<> ();

        try
        {

            List<Object> params = new ArrayList<> ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                // The field may be null because the type definition could be deleted without us knowing, i.e.
                // while another project is being edited.
                Asset f = this.getAsset (rs,
                                         parent,
                                         loadChildObjects);

                if (f != null)
                {

                    ret.add (f);

                }

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load assets for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public Asset getObjectByKey (long       key,
                                 Project    parent,
                                 Connection conn,
                                 boolean    loadChildObjects)
                          throws GeneralException
    {

        Asset t = null;

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                t = this.getAsset (rs,
                                   parent,
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

            throw new GeneralException ("Unable to load asset for key: " +
                                        key,
                                        e);

        }

        return t;

    }

    public void createObject (Asset      t,
                              Connection conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (t.getKey ());

        this.objectManager.executeStatement ("INSERT INTO asset (dbkey) VALUES (?)",
                                             params,
                                             conn);

    }

    public void deleteObject (Asset      t,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (t.getKey ());

        this.objectManager.executeStatement ("DELETE FROM asset WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (Asset      t,
                              Connection conn)
                       throws GeneralException
    {

        // Nothing to do...

    }

}
