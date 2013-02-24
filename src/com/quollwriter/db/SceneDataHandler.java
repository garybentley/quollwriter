package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class SceneDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;
    private List<Scene>   allScenes = new ArrayList ();
    private boolean       loaded = false;

    public SceneDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Scene getScene (ResultSet rs,
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
                                 (Chapter) this.objectManager.getObjectByKey (Chapter.class,
                                                                              chapterKey,
                                                                              rs.getStatement ().getConnection (),
                                                                              false));
            s.setKey (key);
            s.setName (rs.getString (ind++));
            s.setDescription (rs.getString (ind++));

            s.setLastModified (rs.getTimestamp (ind++));
            s.setDateCreated (rs.getTimestamp (ind++));
            s.setPropertiesAsString (rs.getString (ind++));

            // Get all the notes.
/*
            if (loadChildObjects)
            {

                Connection conn = rs.getStatement ().getConnection ();

                List items = this.objectManager.getObjects (OutlineItem.class,
                                                            s,
                                                            conn,
                                                            loadChildObjects);

                for (int i = 0; i < items.size (); i++)
                {

                    s.addOutlineItem ((OutlineItem) items.get (i));

                }

            }
*/
            return s;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load scene",
                                        e);

        }

    }

    private void loadAllScenes (Connection conn,
                                boolean    loadChildObjects)
                         throws GeneralException
    {

        if (!this.loaded)
        {

            this.loaded = true;

            try
            {

                ResultSet rs = this.objectManager.executeQuery ("SELECT chapterdbkey, position, dbkey, name, description, lastmodified, datecreated, properties FROM scene_v",
                                                                null,
                                                                conn);

                while (rs.next ())
                {

                    this.addScene (this.getScene (rs,
                                                  loadChildObjects));

                }

                try
                {

                    rs.close ();

                } catch (Exception e)
                {
                }

                // Doing it this way so that if the child needs to perform the lookup then
                // we won't go into a loop.
                if (loadChildObjects)
                {

                    for (Scene s : this.allScenes)
                    {

                        List items = this.objectManager.getObjects (OutlineItem.class,
                                                                    s,
                                                                    conn,
                                                                    loadChildObjects);

                        for (int i = 0; i < items.size (); i++)
                        {

                            s.addOutlineItem ((OutlineItem) items.get (i));

                        }

                    }

                }

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load all scenes",
                                            e);

            }

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        this.loadAllScenes (conn,
                            loadChildObjects);

        List<Scene> ret = new ArrayList ();

        for (Scene s : this.allScenes)
        {

            if (s.getChapter ().getKey ().equals (parent.getKey ()))
            {

                ret.add (s);

            }

        }

        return ret;

/*

        List<Scene> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, position FROM scene_v WHERE chapterdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getScene (rs,
                                        loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load scenes for: " +
                                        parent,
                                        e);

        }

        return ret;
*/
    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        this.loadAllScenes (conn,
                            loadChildObjects);

        for (Scene s : this.allScenes)
        {

            if (s.getKey ().intValue () == key)
            {

                return s;

            }

        }

        return null;

/*
        Scene s = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, position FROM scene_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                s = this.getScene (rs,
                                   loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load scene for key: " +
                                        key,
                                        e);

        }

        return s;
*/
    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Scene s = (Scene) d;

        List params = new ArrayList ();
        params.add (s.getKey ());
        params.add (s.getChapter ().getKey ());
        params.add (s.getPosition ());

        this.objectManager.executeStatement ("INSERT INTO scene (dbkey, chapterdbkey, position) VALUES (?, ?, ?)",
                                             params,
                                             conn);

        for (OutlineItem i : s.getOutlineItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

        this.addScene (s);

    }

    private void addScene (Scene s)
    {

        this.allScenes.add (s);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        Scene s = (Scene) d;

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

        List params = new ArrayList ();
        params.add (s.getKey ());

        this.objectManager.executeStatement ("DELETE FROM scene WHERE dbkey = ?",
                                             params,
                                             conn);

        this.allScenes.remove (s);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Scene s = (Scene) d;

        List params = new ArrayList ();
        params.add (s.getPosition ());
        params.add (s.getChapter ().getKey ());
        params.add (s.getKey ());

        this.objectManager.executeStatement ("UPDATE scene SET position = ?, chapterdbkey = ? WHERE dbkey = ?",
                                             params,
                                             conn);

        for (OutlineItem i : s.getOutlineItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

    }

}
