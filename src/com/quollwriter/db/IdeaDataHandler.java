package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class IdeaDataHandler implements DataHandler<Idea, IdeaType>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, description, markup, files, rating, lastmodified, datecreated FROM idea_v ";

    private ObjectManager objectManager = null;

    public IdeaDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Idea getIdea (ResultSet rs,
                          IdeaType  ideaType)
                   throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Idea i = new Idea ();

            i.setKey (key);

            i.setType (ideaType);
            i.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            i.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));            
            i.setRating (rs.getInt (ind++));

            i.setLastModified (rs.getTimestamp (ind++));
            i.setDateCreated (rs.getTimestamp (ind++));

            if (ideaType != null)
            {
            
                ideaType.addIdea (i);
                
            }
            
            return i;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load idea",
                                        e);

        }

    }

    @Override
    public List<Idea> getObjects (IdeaType parent,
                                  Connection  conn,
                                  boolean     loadChildObjects)
                           throws GeneralException
    {

        List<Idea> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE ideatypedbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getIdea (rs,
                                       (IdeaType) parent));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load ideas for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    @Override
    public Idea getObjectByKey (long       key,
                                IdeaType   it,
                                Connection conn,
                                boolean    loadChildObjects)
                         throws GeneralException
    {

        List params = new ArrayList ();
        params.add (key);

        ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                        params,
                                                        conn);

        try
        {

            if (rs.next ())
            {

                return this.getIdea (rs,
                                     it);

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get idea: " + key,
                                        e);

        }

        return null;

    }

    @Override
    public void createObject (Idea       d,
                              Connection conn)
                       throws GeneralException
    {

        Idea i = (Idea) d;

        List params = new ArrayList ();
        params.add (i.getKey ());
        params.add (i.getType ().getKey ());
        params.add (i.getRating ());

        this.objectManager.executeStatement ("INSERT INTO idea (dbkey, ideatypedbkey, rating) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    @Override
    public void deleteObject (Idea       d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (d.getKey ());

        this.objectManager.executeStatement ("DELETE FROM idea WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (Idea       d,
                              Connection conn)
                       throws GeneralException
    {

        Idea i = (Idea) d;

        List params = new ArrayList ();
        params.add (i.getRating ());
        params.add (i.getKey ());
        params.add (i.getType ().getKey ());

        this.objectManager.executeStatement ("UPDATE idea SET rating = ? WHERE dbkey = ? AND ideatypedbkey = ?",
                                             params,
                                             conn);

    }

}
