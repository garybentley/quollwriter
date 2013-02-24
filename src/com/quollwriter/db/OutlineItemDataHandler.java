package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class OutlineItemDataHandler implements DataHandler
{

    private ObjectManager     objectManager = null;
    private List<OutlineItem> allOutlineItems = new ArrayList ();
    private boolean           loaded = false;

    public OutlineItemDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private OutlineItem getOutlineItem (ResultSet rs,
                                        boolean   loadChildObjects)
                                 throws GeneralException
    {

        try
        {

            int ind = 1;

            int chapterKey = rs.getInt (ind++);
            int sceneKey = rs.getInt (ind++);
            int pos = rs.getInt (ind++);

            long key = rs.getLong (ind++);

            OutlineItem o = null;

            if (sceneKey > 0)
            {

                o = new OutlineItem (pos,
                                     (Scene) this.objectManager.getObjectByKey (Scene.class,
                                                                                sceneKey,
                                                                                rs.getStatement ().getConnection (),
                                                                                false));

            } else
            {

                o = new OutlineItem (pos,
                                     (Chapter) this.objectManager.getObjectByKey (Chapter.class,
                                                                                  chapterKey,
                                                                                  rs.getStatement ().getConnection (),
                                                                                  false));

            }

            o.setKey (key);
            o.setName (rs.getString (ind++));
            o.setDescription (rs.getString (ind++));

            o.setLastModified (rs.getTimestamp (ind++));
            o.setDateCreated (rs.getTimestamp (ind++));
            o.setPropertiesAsString (rs.getString (ind++));

            return o;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load outline item",
                                        e);

        }

    }

    private void loadAllOutlineItems (Connection conn,
                                      boolean    loadChildObjects)
                               throws GeneralException
    {

        if (!this.loaded)
        {

            this.loaded = true;

            try
            {

                ResultSet rs = this.objectManager.executeQuery ("SELECT chapterdbkey, scenedbkey, position, dbkey, name, description, lastmodified, datecreated, properties FROM outlineitem_v",
                                                                null,
                                                                conn);

                while (rs.next ())
                {

                    this.addOutlineItem (this.getOutlineItem (rs,
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
/*
                    for (OutlineItem i : this.allOutlineItems.get ())
                    {

                    }
*/
                }

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load all outline items",
                                            e);

            }

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        this.loadAllOutlineItems (conn,
                                  loadChildObjects);

        List<OutlineItem> ret = new ArrayList ();

        for (OutlineItem o : this.allOutlineItems)
        {

            if (parent instanceof Scene)
            {

                if (o.getScene () != null)
                {

                    if (o.getScene ().getKey ().equals (parent.getKey ()))
                    {

                        ret.add (o);

                    }

                }

            }

            if (parent instanceof Chapter)
            {

                if (o.getScene () == null)
                {

                    if (o.getChapter ().getKey ().equals (parent.getKey ()))
                    {

                        ret.add (o);

                    }

                }

            }

        }

        return ret;
/*
        List<OutlineItem> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            String sql = "SELECT dbkey, name, description, lastmodified, datecreated, properties, position FROM outlineitem_v WHERE ";

            if (parent instanceof Scene)
            {

                sql = sql + " scenedbkey = ?";

            } else {

                sql = sql + " chapterdbkey = ? AND scenedbkey IS NULL";

            }

            sql = sql + " ORDER BY position";

            ResultSet rs = this.objectManager.executeQuery (sql,
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getOutlineItem (rs,
                                              loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load outline items for: " +
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

        this.loadAllOutlineItems (conn,
                                  loadChildObjects);

        for (OutlineItem o : this.allOutlineItems)
        {

            if (o.getKey ().intValue () == key)
            {

                return o;

            }

        }

        return null;

/*
        OutlineItem i = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, position FROM outlineitem_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getOutlineItem (rs,
                                         loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load outline item for key: " +
                                        key,
                                        e);

        }

        return i;
*/
    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        OutlineItem o = (OutlineItem) d;

        List params = new ArrayList ();
        params.add (o.getKey ());
        params.add (o.getPosition ());
        params.add (o.getChapter ().getKey ());

        Long sk = null;

        if (o.getScene () != null)
        {

            sk = o.getScene ().getKey ();

        }

        params.add (sk);

        this.objectManager.executeStatement ("INSERT INTO outlineitem (dbkey, position, chapterdbkey, scenedbkey) VALUES (?, ?, ?, ?)",
                                             params,
                                             conn);

        this.addOutlineItem (o);

    }

    private void addOutlineItem (OutlineItem o)
    {

        this.allOutlineItems.add (o);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        OutlineItem o = (OutlineItem) d;

        List params = new ArrayList ();
        params.add (o.getKey ());

        this.objectManager.executeStatement ("DELETE FROM outlineitem WHERE dbkey = ?",
                                             params,
                                             conn);

        this.allOutlineItems.remove (o);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        OutlineItem o = (OutlineItem) d;

        List params = new ArrayList ();
        params.add (o.getPosition ());
        params.add (o.getChapter ().getKey ());

        Long sk = null;

        if (o.getScene () != null)
        {

            sk = o.getScene ().getKey ();

        }

        params.add (sk);
        params.add (o.getKey ());
        this.objectManager.executeStatement ("UPDATE outlineitem SET position = ?, chapterdbkey = ?, scenedbkey = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
