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

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?", // AND projectdbkey = ?",
                                                            Arrays.asList (key),
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

        List<ResearchItem> ret = new ArrayList<> ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND projectdbkey = ?",
                                                            Arrays.asList (parent.getKey ()),
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

        this.objectManager.executeStatement ("INSERT INTO researchitem (dbkey, url, projectdbkey) VALUES (?, ?, ?)",
                                             Arrays.asList (r.getKey (),
                                                            r.getUrl (),
                                                            r.getProject ().getKey ()),
                                             conn);

    }

    @Override
    public void deleteObject (ResearchItem r,
                              boolean      deleteChildObjects,
                              Connection   conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("DELETE FROM researchitem WHERE dbkey = ?",
                                             Arrays.asList (r.getKey ()),
                                             conn);

    }

    @Override
    public void updateObject (ResearchItem r,
                              Connection   conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE researchitem SET url = ? WHERE dbkey = ?",
                                             Arrays.asList (r.getUrl (),
                                                            r.getKey ()),
                                             conn);

    }

}
