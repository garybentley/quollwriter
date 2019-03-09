package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class SceneDataHandler implements DataHandler<Scene, Chapter>
{

    private final static String STD_SELECT_PREFIX = "SELECT chapterdbkey, position, dbkey, name, description, markup, files, lastmodified, datecreated, properties FROM scene_v ";
    private ObjectManager objectManager = null;

    public SceneDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Scene getScene (ResultSet rs,
                            Chapter   parent,
                            boolean   loadChildObjects)
                     throws GeneralException
    {

        try
        {

            int ind = 1;

            int chapterKey = rs.getInt (ind++);
            int pos = rs.getInt (ind++);

            long key = rs.getLong (ind++);

            Scene s = new Scene (pos,
                                 parent);
            s.setKey (key);
            s.setName (rs.getString (ind++));
            s.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            s.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            s.setLastModified (rs.getTimestamp (ind++));
            s.setDateCreated (rs.getTimestamp (ind++));
            s.setPropertiesAsString (rs.getString (ind++));

            if (parent != null)
            {

                parent.addScene (s);

            }

            // Get all the notes.

            if (loadChildObjects)
            {

                Connection conn = rs.getStatement ().getConnection ();

                this.objectManager.getObjects (OutlineItem.class,
                                               s,
                                               conn,
                                               loadChildObjects);

            }

            return s;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load scene",
                                        e);

        }

    }

    @Override
    public List<Scene> getObjects (Chapter     parent,
                                   Connection  conn,
                                   boolean     loadChildObjects)
                            throws GeneralException
    {

        try
        {

            List<Scene> ret = new ArrayList<> ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE chapterdbkey = ?",
                                                            Arrays.asList (parent.getKey ()),
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getScene (rs,
                                        parent,
                                        loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

            return ret;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to scenes for chapter: " +
                                        parent,
                                        e);

        }

    }

    @Override
    public Scene getObjectByKey (long       key,
                                 Chapter    parent,
                                 Connection conn,
                                 boolean    loadChildObjects)
                          throws GeneralException
    {

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                            Arrays.asList (key),
                                                            conn);

            if (rs.next ())
            {

                return this.getScene (rs,
                                      parent,
                                      loadChildObjects);

            }

            return null;

        } catch (Exception e) {

            throw new GeneralException ("Unable to get scene with key: " +
                                        key,
                                        e);

        }

    }

    @Override
    public void createObject (Scene      s,
                              Connection conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("INSERT INTO scene (dbkey, chapterdbkey, position) VALUES (?, ?, ?)",
                                             Arrays.asList (s.getKey (),
                                                            s.getChapter ().getKey (),
                                                            s.getPosition ()),
                                             conn);

        for (OutlineItem i : s.getOutlineItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

    }

    @Override
    public void deleteObject (Scene      s,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        if (deleteChildObjects)
        {

            // Delete the outline items.
            for (OutlineItem i : s.getOutlineItems ())
            {

                this.objectManager.deleteObject (i,
                                                 true,
                                                 conn);

            }

        } else {

            // Remove the scene from the item.
            for (OutlineItem i : s.getOutlineItems ())
            {

                i.setScene (null);

                this.objectManager.saveObject (i,
                                               conn);

            }


        }

        this.objectManager.executeStatement ("DELETE FROM scene WHERE dbkey = ?",
                                             Arrays.asList (s.getKey ()),
                                             conn);

    }

    @Override
    public void updateObject (Scene      s,
                              Connection conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE scene SET position = ?, chapterdbkey = ? WHERE dbkey = ?",
                                             Arrays.asList (s.getPosition (),
                                                            s.getChapter ().getKey (),
                                                            s.getKey ()),
                                             conn);

        for (OutlineItem i : s.getOutlineItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

    }

}
