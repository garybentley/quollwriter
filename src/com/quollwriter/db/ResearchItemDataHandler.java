package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class ResearchItemDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public ResearchItemDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private ResearchItem getResearchItem (ResultSet rs,
                                          boolean   loadChildObjects)
                                   throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            ResearchItem l = new ResearchItem ();
            l.setKey (key);
            l.setName (rs.getString (ind++));
            l.setDescription (rs.getString (ind++));

            l.setLastModified (rs.getTimestamp (ind++));
            l.setDateCreated (rs.getTimestamp (ind++));
            l.setPropertiesAsString (rs.getString (ind++));
            l.setUrl (rs.getString (ind++));

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

            throw new GeneralException ("Unable to load research item",
                                        e);

        }

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        ResearchItem r = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, url FROM researchitem_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                r = this.getResearchItem (rs,
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

            throw new GeneralException ("Unable to load research item for key: " +
                                        key,
                                        e);

        }

        return r;

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<ResearchItem> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, url FROM researchitem_v WHERE projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getResearchItem (rs,
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

            throw new GeneralException ("Unable to load research items for parent: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        ResearchItem r = (ResearchItem) d;

        List params = new ArrayList ();
        params.add (r.getKey ());
        params.add (r.getUrl ());
        params.add (r.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO researchitem (dbkey, url, projectdbkey) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        ResearchItem r = (ResearchItem) d;

        List params = new ArrayList ();
        params.add (r.getKey ());

        this.objectManager.executeStatement ("DELETE FROM researchitem WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        ResearchItem r = (ResearchItem) d;

        List params = new ArrayList ();
        params.add (r.getUrl ());
        params.add (r.getKey ());

        this.objectManager.executeStatement ("UPDATE researchitem SET url = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
