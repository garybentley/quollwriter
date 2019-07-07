package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class LinkDataHandler implements DataHandler<Link, NamedObject>
{

    private ObjectManager objectManager = null;

    public LinkDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    @Override
    public List<Link> getObjects (NamedObject parent,
                                  Connection  conn,
                                  boolean     loadChildObjects)
                           throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported");

    }

    @Override
    public Link getObjectByKey (long        key,
                                NamedObject parent,
                                Connection  conn,
                                boolean     loadChildObjects)
                         throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported");

    }

    @Override
    public void createObject (Link       l,
                              Connection conn)
                       throws GeneralException
    {

        // Check to see if it already exists. ???

        this.objectManager.executeStatement ("INSERT INTO link (dbkey, object1dbkey, object1objtype, object2dbkey, object2objtype) VALUES (?, ?, ?, ?, ?)",
                                             Arrays.asList (l.getKey (),
                                                            l.getObject1 ().getKey (),
                                                            l.getObject1 ().getObjectType (),
                                                            l.getObject2 ().getKey (),
                                                            l.getObject2 ().getObjectType ()),
                                             conn);

    }

    @Override
    public void deleteObject (Link       d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("DELETE FROM link WHERE dbkey = ?",
                                             Arrays.asList (d.getKey ()),
                                             conn);

    }

    @Override
    public void updateObject (Link       l,
                              Connection conn)
                       throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported");

    }

    public void deleteAllLinks (NamedObject obj,
                                Connection  conn)
                         throws GeneralException
    {

        this.objectManager.executeStatement ("DELETE FROM link WHERE (object1dbkey = ? AND object1objtype = ?) OR (object2dbkey = ? AND object2objtype = ?)",
                                             Arrays.asList (obj.getKey (),
                                                            obj.getObjectType (),
                                                            obj.getKey (),
                                                            obj.getObjectType ()),
                                            conn);

    }

}
