package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class LinkDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public LinkDataHandler(ObjectManager om)
    {

        this.objectManager = om;

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

        throw new GeneralException ("Not supported");

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Link l = (Link) d;

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

    public void deleteObject (DataObject d,
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

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

    }

}
