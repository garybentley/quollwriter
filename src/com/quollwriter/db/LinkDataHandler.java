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

        List params = new ArrayList ();
        params.add (l.getKey ());
        params.add (l.getObject1 ().getKey ());
        params.add (l.getObject1 ().getObjectType ());
        params.add (l.getObject2 ().getKey ());
        params.add (l.getObject2 ().getObjectType ());

        this.objectManager.executeStatement ("INSERT INTO link (dbkey, object1dbkey, object1objtype, object2dbkey, object2objtype) VALUES (?, ?, ?, ?, ?)",
                                             params,
                                             conn);

    }

    @Override
    public void deleteObject (Link       d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (d.getKey ());

        this.objectManager.executeStatement ("DELETE FROM link WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public void updateObject (Link       l,
                              Connection conn)
                       throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported");
    
    }

}
