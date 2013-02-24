package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class IdeaTypeDataHandler implements DataHandler
{

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
            it.setDescription (rs.getString (ind++));
            it.setSortBy (rs.getString (ind++));
            it.setIconType (rs.getString (ind++));
            it.setLastModified (rs.getTimestamp (ind++));
            it.setDateCreated (rs.getTimestamp (ind++));
            it.setPropertiesAsString (rs.getString (ind++));

            Connection conn = rs.getStatement ().getConnection ();

            List ideas = this.objectManager.getObjects (Idea.class,
                                                        it,
                                                        conn,
                                                        false);

            for (int i = 0; i < ideas.size (); i++)
            {

                it.addIdea ((Idea) ideas.get (i));

            }

            return it;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load ideatype",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<IdeaType> ret = new ArrayList ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, sortby, icontype, lastmodified, datecreated, properties FROM ideatype_v ORDER BY LOWER(name)",
                                                            null,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getIdeaType (rs,
                                           (Project) parent));

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

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        IdeaType it = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, sortby, icontype, lastmodified, datecreated, properties FROM ideatype_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                it = this.getIdeaType (rs,
                                       null);

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

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        IdeaType it = (IdeaType) d;

        List params = new ArrayList ();
        params.add (it.getKey ());
        params.add (it.getSortBy ());
        params.add (it.getIconType ());

        this.objectManager.executeStatement ("INSERT INTO ideatype (dbkey, sortby, icontype) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        // Remove all the ideas.
        IdeaType it = (IdeaType) d;

        List<Idea> ideas = it.getIdeas ();

        for (Idea i : ideas)
        {

            this.objectManager.deleteObject (i,
                                             true,
                                             conn);

        }

        List params = new ArrayList ();
        params.add (d.getKey ());

        this.objectManager.executeStatement ("DELETE FROM ideatype WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        IdeaType it = (IdeaType) d;

        List params = new ArrayList ();
        params.add (it.getSortBy ());
        params.add (it.getIconType ());
        params.add (it.getKey ());

        this.objectManager.executeStatement ("UPDATE ideatype SET sortby = ?, icontype = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
