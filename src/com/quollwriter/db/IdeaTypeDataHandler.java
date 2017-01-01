package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class IdeaTypeDataHandler implements DataHandler<IdeaType, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, description, markup, sortby, icontype, lastmodified, datecreated, properties FROM ideatype_v ";

    private ObjectManager objectManager = null;

    public IdeaTypeDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private IdeaType getIdeaType (ResultSet rs,
                                  Project   p)
                           throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            IdeaType it = new IdeaType ();

            it.setKey (key);

            it.setName (rs.getString (ind++));
            it.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                     rs.getString (ind++)));
            it.setSortBy (rs.getString (ind++));
            it.setIconType (rs.getString (ind++));
            it.setLastModified (rs.getTimestamp (ind++));
            it.setDateCreated (rs.getTimestamp (ind++));
            it.setPropertiesAsString (rs.getString (ind++));

            if (p != null)
            {
                
                p.addIdeaType (it);
                
            }
            
            Connection conn = rs.getStatement ().getConnection ();

            this.objectManager.getObjects (Idea.class,
                                           it,
                                           conn,
                                           false);

            return it;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load ideatype",
                                        e);

        }

    }

    @Override
    public List<IdeaType> getObjects (Project parent,
                                      Connection  conn,
                                      boolean     loadChildObjects)
                               throws GeneralException
    {

        List<IdeaType> ret = new ArrayList ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " ORDER BY LOWER(name)",
                                                            null,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getIdeaType (rs,
                                           parent));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load ideatypes for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    @Override
    public IdeaType getObjectByKey (long       key,
                                    Project    proj,
                                    Connection conn,
                                    boolean    loadChildObjects)
                             throws GeneralException
    {

        IdeaType it = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                it = this.getIdeaType (rs,
                                       proj);

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load ideatype for key: " +
                                        key,
                                        e);

        }

        return it;

    }

    @Override
    public void createObject (IdeaType   it,
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (it.getKey ());
        params.add (it.getSortBy ());
        params.add (it.getIconType ());

        this.objectManager.executeStatement ("INSERT INTO ideatype (dbkey, sortby, icontype) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    @Override
    public void deleteObject (IdeaType   it,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        // Remove all the ideas.
        List<Idea> ideas = it.getIdeas ();

        for (Idea i : ideas)
        {

            this.objectManager.deleteObject (i,
                                             true,
                                             conn);

        }

        List params = new ArrayList ();
        params.add (it.getKey ());

        this.objectManager.executeStatement ("DELETE FROM ideatype WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (IdeaType   it,
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (it.getSortBy ());
        params.add (it.getIconType ());
        params.add (it.getKey ());

        this.objectManager.executeStatement ("UPDATE ideatype SET sortby = ?, icontype = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
