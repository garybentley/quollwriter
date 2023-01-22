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

        try
        {

            Project project = null;
            String suffix = "";
            List params = new ArrayList ();

            if (parent instanceof Project)
            {

                project = (Project) parent;

            } else {

                if (parent instanceof NamedObject)
                {

                    NamedObject d = (NamedObject) parent;

                    project = this.objectManager.getProject ();
                    suffix = "WHERE (object1dbkey = ? AND object1objtype = ?) OR (object2dbkey = ? AND object2objtype = ?)";
                    params.add (d.getKey ());
                    params.add (d.getObjectType ());
                    params.add (d.getKey ());
                    params.add (d.getObjectType ());

                }

            }

            if (project == null)
            {

                throw new IllegalArgumentException ("Unable to find project.");

            }

            List<Link> ret = new ArrayList<> ();

            ResultSet rs = this.objectManager.executeQuery ("SELECT object1dbkey, object1objtype, object2dbkey, object2objtype, dbkey FROM link " + suffix,
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                  long   o1key = rs.getLong (1);
                  String o1type = rs.getString (2);
                  long   o2key = rs.getLong (3);
                  String o2type = rs.getString (4);
                  long   key = rs.getLong (5);

                  if (o1type == null)
                  {

                      continue;

                  }

                  if (o2type == null)
                  {

                      continue;

                  }

                  ObjectReference o1 = new ObjectReference (o1type,
                                                            o1key,
                                                            null);
                  ObjectReference o2 = new ObjectReference (o2type,
                                                            o2key,
                                                            null);

                  NamedObject d1 = (NamedObject) project.getObjectForReference (o1);
                  NamedObject d2 = (NamedObject) project.getObjectForReference (o2);

                  if ((d1 == null) ||
                      (d2 == null))
                  {

                      continue;

                  }

                  Link l = new Link (d1,
                                     d2);
                  l.setKey (key);

                  ret.add (l);

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

            return ret;

        } catch (Exception e) {

            throw new GeneralException ("Unable to load links for: " +
                                        parent,
                                        e);

        }

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

    public void deleteLink (NamedObject obj1,
                            NamedObject obj2,
                            Connection  conn)
                     throws GeneralException
    {

        this.objectManager.executeStatement ("DELETE FROM link WHERE (object1dbkey = ? AND object1objtype = ? AND object2dbkey = ? AND object2objtype = ?) OR (object1dbkey = ? AND object1objtype = ? AND object2dbkey = ? AND object2objtype = ?)",
                                             Arrays.asList (obj1.getKey (),
                                                            obj1.getObjectType (),
                                                            obj2.getKey (),
                                                            obj2.getObjectType (),
                                                            obj2.getKey (),
                                                            obj2.getObjectType (),
                                                            obj1.getKey (),
                                                            obj1.getObjectType ()),
                                            conn);

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
