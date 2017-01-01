package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class OutlineItemDataHandler implements DataHandler<OutlineItem, NamedObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT chapterdbkey, scenedbkey, position, dbkey, name, description, markup, files, lastmodified, datecreated, properties FROM outlineitem_v ";
    private ObjectManager     objectManager = null;

    public OutlineItemDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private OutlineItem getOutlineItem (ResultSet   rs,
                                        NamedObject parent,
                                        boolean     loadChildObjects)
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

            if (parent == null)
            {
                
                o = new OutlineItem ();
                
            } else {
                
                if (sceneKey > 0)
                {
    
                    Scene s = (Scene) parent;
                            
                    o = new OutlineItem (pos,
                                         s);
                                
                } else
                {
    
                    Chapter c = (Chapter) parent;
                                    
                    o = new OutlineItem (pos,
                                         c);
                        
                }
                
            }            
            
            o.setKey (key);
            o.setName (rs.getString (ind++));
            o.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            o.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            o.setLastModified (rs.getTimestamp (ind++));
            o.setDateCreated (rs.getTimestamp (ind++));
            o.setPropertiesAsString (rs.getString (ind++));

            if (parent != null)
            {
            
                if (sceneKey > 0)
                {
    
                    Scene s = (Scene) parent;
                                            
                    s.addOutlineItem (o);
                
                } else
                {
    
                    Chapter c = (Chapter) parent;
                                    
                    c.addOutlineItem (o);
    
                }

            }
                
            return o;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load outline item",
                                        e);

        }

    }

    public List<OutlineItem> getObjects (Scene       parent,
                                         Connection  conn,
                                         boolean     loadChildObjects)
                                  throws GeneralException
    {

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());
        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE scenedbkey = ?",
                                                            params,
                                                            conn);

            List<OutlineItem> items = new ArrayList ();
                                                            
            while (rs.next ())
            {

                items.add (this.getOutlineItem (rs,
                                                parent,
                                                loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

            return items;
            
        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load outline items for scene: " +
                                        parent,
                                        e);

        }
        
    }

    public List<OutlineItem> getObjects (Chapter     parent,
                                         Connection  conn,
                                         boolean     loadChildObjects)
                                  throws GeneralException
    {

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());
        
            // No scene allowed here.
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE chapterdbkey = ? AND scenedbkey IS NULL",
                                                            params,
                                                            conn);

            List<OutlineItem> items = new ArrayList ();
                                                            
            while (rs.next ())
            {

                items.add (this.getOutlineItem (rs,
                                                parent,
                                                loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

            return items;
            
        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load outline items for scene: " +
                                        parent,
                                        e);

        }
        
    }

    @Override
    public List<OutlineItem> getObjects (NamedObject parent,
                                         Connection  conn,
                                         boolean     loadChildObjects)
                                  throws GeneralException
    {

        if (parent instanceof Scene)
        {
            
            return this.getObjects ((Scene) parent,
                                    conn,
                                    loadChildObjects);
            
        }
    
        if (parent instanceof Chapter)
        {
            
            return this.getObjects ((Chapter) parent,
                                    conn,
                                    loadChildObjects);
            
        }
        
        throw new IllegalArgumentException ("Unsupported parent object type: " +
                                            parent);

    }

    @Override
    public OutlineItem getObjectByKey (long        key,
                                       NamedObject parent,
                                       Connection  conn,
                                       boolean     loadChildObjects)
                                throws GeneralException
    {

        try
        {
    
            List params = new ArrayList ();
            params.add (key);
        
            // No scene allowed here.
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                            params,
                                                            conn);
                                                            
            if (rs.next ())
            {
    
                return this.getOutlineItem (rs,
                                            parent,
                                            loadChildObjects);
    
            }
    
            return null;
        
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to get outline item with key: " +
                                        key,
                                        e);
            
        }
        
    }

    @Override
    public void createObject (OutlineItem o,
                              Connection  conn)
                       throws GeneralException
    {

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

    }

    @Override
    public void deleteObject (OutlineItem o,
                              boolean     deleteChildObjects,                              
                              Connection  conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (o.getKey ());

        this.objectManager.executeStatement ("DELETE FROM outlineitem WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (OutlineItem o,
                              Connection  conn)
                       throws GeneralException
    {

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
