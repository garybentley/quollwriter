package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class ProjectDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public ProjectDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Project getProject (ResultSet rs,
                                boolean   loadChildObjects)
                         throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Project p = new Project ();

            p.setKey (key);
            p.setName (rs.getString (ind++));
            p.setType (rs.getString (ind++));
            p.setLastEdited (rs.getTimestamp (ind++));
            p.setDescription (rs.getString (ind++));

            p.setLastModified (rs.getTimestamp (ind++));
            p.setDateCreated (rs.getTimestamp (ind++));
            p.setPropertiesAsString (rs.getString (ind++));

            if (loadChildObjects)
            {

                Connection conn = rs.getStatement ().getConnection ();

                List books = this.objectManager.getObjects (Book.class,
                                                            p,
                                                            conn,
                                                            loadChildObjects);

                for (int i = 0; i < books.size (); i++)
                {

                    p.addBook ((Book) books.get (i));

                }

                List chars = this.objectManager.getObjects (QCharacter.class,
                                                            p,
                                                            conn,
                                                            loadChildObjects);

                for (int i = 0; i < chars.size (); i++)
                {

                    p.addCharacter ((QCharacter) chars.get (i));

                }

                List locs = this.objectManager.getObjects (Location.class,
                                                           p,
                                                           conn,
                                                           loadChildObjects);

                for (int i = 0; i < locs.size (); i++)
                {

                    p.addLocation ((Location) locs.get (i));

                }

                List objs = this.objectManager.getObjects (QObject.class,
                                                           p,
                                                           conn,
                                                           loadChildObjects);

                for (int i = 0; i < objs.size (); i++)
                {

                    p.addQObject ((QObject) objs.get (i));

                }

                this.objectManager.loadNotes (p,
                                              conn);

                List ris = this.objectManager.getObjects (ResearchItem.class,
                                                          p,
                                                          conn,
                                                          loadChildObjects);

                for (int i = 0; i < ris.size (); i++)
                {

                    p.addResearchItem ((ResearchItem) ris.get (i));

                }

                List its = this.objectManager.getObjects (IdeaType.class,
                                                          p,
                                                          conn,
                                                          loadChildObjects);

                for (int i = 0; i < its.size (); i++)
                {

                    p.addIdeaType ((IdeaType) its.get (i));

                }

            }

            return p;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load project",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        throw new GeneralException ("Not supported");

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        ResultSet rs = null;

        try
        {

            rs = this.objectManager.executeQuery ("SELECT dbkey, name, type, lastedited, description, lastmodified, datecreated, properties FROM project_v",
                                                  null,
                                                  conn);

            if (rs.next ())
            {

                Project p = this.getProject (rs,
                                             loadChildObjects);

                return p;

            }

            return null;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load project",
                                        e);

        } finally
        {

            try
            {

                if (rs != null)
                {

                    rs.close ();

                }

            } catch (Exception e)
            {

            }

        }

    }

    public List<WordCount> getWordCounts (Project p,
                                          int     daysPast)
                                   throws GeneralException
    {

        try
        {

            Connection conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (p.getKey ());

            String whereDays = "";

            if (daysPast != 0)
            {

                whereDays = " AND start > DATEADD ('DAY', ?, CURRENT_DATE) ";
                params.add (daysPast);

            }

            ResultSet rs = this.objectManager.executeQuery ("SELECT sum(count), start FROM wordcount WHERE projectdbkey = ? " + whereDays + " GROUP BY start ORDER BY start",
                                                            params,
                                                            conn);

            List<WordCount> ret = new ArrayList ();

            while (rs.next ())
            {

                int ind = 1;

                WordCount c = new WordCount ();
                c.setCount (rs.getInt (ind++));

                c.setEnd (rs.getTimestamp (ind++));
                c.setProject (p);

                ret.add (c);

            }

            try
            {

                conn.close ();

            } catch (Exception e)
            {
            }

            return ret;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load word counts for project: " +
                                        p,
                                        e);

        }

    }

    public Project getProject (Connection conn)
                        throws GeneralException
    {

        return (Project) this.getObjectByKey (-1,
                                              conn,
                                              true);

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Project p = (Project) d;

        List params = new ArrayList ();
        params.add (p.getKey ());
        params.add (Environment.getSchemaVersion ());
        params.add (p.getType ());

        this.objectManager.executeStatement ("INSERT INTO project (dbkey, schema_version, type) VALUES (?, ?, ?)",
                                             params,
                                             conn);

        // Get all the other objects.
        for (QCharacter c : p.getCharacters ())
        {

            this.objectManager.saveObject (c,
                                           conn);

        }

        for (Location l : p.getLocations ())
        {

            this.objectManager.saveObject (l,
                                           conn);

        }

        for (QObject q : p.getQObjects ())
        {

            this.objectManager.saveObject (q,
                                           conn);

        }

        for (ResearchItem i : p.getResearchItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

        for (Book b : p.getBooks ())
        {

            this.objectManager.saveObject (b,
                                           conn);

        }

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {


    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Project p = (Project) d;

        List params = new ArrayList ();
        params.add (p.getLastEdited ());
        params.add (p.getKey ());
        this.objectManager.executeStatement ("UPDATE project SET lastedited = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
