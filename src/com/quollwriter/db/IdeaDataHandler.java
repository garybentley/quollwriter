package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class IdeaDataHandler implements DataHandler
{

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
            i.setDescription (rs.getString (ind++));
            i.setRating (rs.getInt (ind++));

            i.setLastModified (rs.getTimestamp (ind++));
            i.setDateCreated (rs.getTimestamp (ind++));

            return i;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load idea",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<Idea> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, description, rating, lastmodified, datecreated FROM idea_v WHERE ideatypedbkey = ?",
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

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        List params = new ArrayList ();
        params.add (key);

        ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, description, rating, lastmodified, datecreated FROM idea_v WHERE dbkey = ?",
                                                        params,
                                                        conn);

        try
        {

            if (rs.next ())
            {

                return this.getIdea (rs,
                                     null);

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get idea: " + key,
                                        e);

        }

        return null;

    }

    public void createObject (DataObject d,
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

    public void deleteObject (DataObject d,
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

    public void updateObject (DataObject d,
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
