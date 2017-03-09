package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class ResearchItemDataHandler implements DataHandler<ResearchItem, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, markup, files, lastmodified, datecreated, properties, url, id, version FROM researchitem_v ";

    private ObjectManager objectManager = null;

    public ResearchItemDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private ResearchItem getResearchItem (ResultSet rs,
                                          Project   proj,
                                          boolean   loadChildObjects)
                                   throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            ResearchItem l = new ResearchItem ();
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
            
            if (l.getLegacyField (ResearchItem.WEB_PAGE_LEGACY_FIELD_ID) == null)
            {
                
                l.setUrl (rs.getString (ind++));
                
            } else {
                
                ind++;
                
            }
            
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

            throw new GeneralException ("Unable to load research item",
                                        e);

        }

    }

    @Override
    public ResearchItem getObjectByKey (long       key,
                                        Project    proj,
                                        Connection conn,
                                        boolean    loadChildObjects)
                                 throws GeneralException
    {

        ResearchItem r = null;

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

                r = this.getResearchItem (rs,
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

            throw new GeneralException ("Unable to load research item for key: " +
                                        key,
                                        e);

        }

        return r;

    }

    @Override
    public List<ResearchItem> getObjects (Project     parent,
                                          Connection  conn,
                                          boolean     loadChildObjects)
                                   throws GeneralException
    {

        List<ResearchItem> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getResearchItem (rs,
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

            throw new GeneralException ("Unable to load research items for parent: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    @Override
    public void createObject (ResearchItem r,
                              Connection   conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (r.getKey ());
        params.add (r.getUrl ());
        params.add (r.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO researchitem (dbkey, url, projectdbkey) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    @Override
    public void deleteObject (ResearchItem r,
                              boolean      deleteChildObjects,                              
                              Connection   conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (r.getKey ());

        this.objectManager.executeStatement ("DELETE FROM researchitem WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (ResearchItem r,
                              Connection   conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (r.getUrl ());
        params.add (r.getKey ());

        this.objectManager.executeStatement ("UPDATE researchitem SET url = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
