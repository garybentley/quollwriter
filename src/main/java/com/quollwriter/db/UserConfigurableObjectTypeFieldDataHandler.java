package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class UserConfigurableObjectTypeFieldDataHandler implements DataHandler<UserConfigurableObjectTypeField, UserConfigurableObjectType>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, type, orderby, name, definition, description, markup, lastmodified, datecreated, properties, id, version, userobjecttypedbkey FROM userobjecttypefield_v ";

    /**
     * A cache, since the same field can be used in multiple places
     * we need a single object so that we have one object -> multiple uses.
     */
    private Map<Long, UserConfigurableObjectTypeField> cache = new HashMap ();

    private ObjectManager objectManager = null;

    public UserConfigurableObjectTypeFieldDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    private UserConfigurableObjectTypeField getUserConfigurableObjectTypeField (ResultSet                  rs,
                                                                                UserConfigurableObjectType type,
                                                                                boolean                    loadChildObjects)
                                                                         throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            UserConfigurableObjectTypeField f = this.cache.get (key);

            if (f != null)
            {

                return f;

            }

            String t = rs.getString (ind++);

            f = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.valueOf (t));

            if (f == null)
            {

                throw new GeneralException ("Type: " + t + " is not supported.");

            }

            f.setKey (key);
            f.setOrder (rs.getInt (ind++));
            f.setFormName (rs.getString (ind++));

            String d = rs.getString (ind++);

            f.setDefinition ((Map) JSONDecoder.decode (d));//rs.getString (ind++)));
            f.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            f.setLastModified (rs.getTimestamp (ind++));
            f.setDateCreated (rs.getTimestamp (ind++));
            f.setPropertiesAsString (rs.getString (ind++));
            f.setId (rs.getString (ind++));
            f.setVersion (rs.getString (ind++));

            long typekey = rs.getLong (ind++);

            if (type == null)
            {

                type = (UserConfigurableObjectType) this.objectManager.getObjectByKey (UserConfigurableObjectType.class,
                                                                                       typekey,
                                                                                       null,
                                                                                       rs.getStatement ().getConnection (),
                                                                                       true);

                if (type == null)
                {

                    throw new GeneralException ("Unable to get config object type for: " +
                                                typekey +
                                                " and type field: " +
                                                key);

                }

            }

            f.setUserConfigurableObjectType (type);

            //TODO Remove type.addConfigurableField (f);

            this.cache.put (key,
                            f);

            return f;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object field",
                                        e);

        }

    }

    public List<UserConfigurableObjectTypeField> getObjects (UserConfigurableObjectType type,
                                                             Connection                 conn,
                                                             boolean                    loadChildObjects)
                                                      throws GeneralException
    {

        List<UserConfigurableObjectTypeField> ret = new ArrayList<> ();

        try
        {

            List params = new ArrayList ();
            params.add (type.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND userobjecttypedbkey = ? ORDER BY orderby",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getUserConfigurableObjectTypeField (rs,
                                                                  type,
                                                                  loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object fields for: " +
                                        type,
                                        e);

        }

        return ret;

    }

    public UserConfigurableObjectTypeField getObjectByKey (long                       key,
                                                           UserConfigurableObjectType type,
                                                           Connection                 conn,
                                                           boolean                    loadChildObjects)
                                                    throws GeneralException
    {

        UserConfigurableObjectTypeField t = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                t = this.getUserConfigurableObjectTypeField (rs,
                                                             type,
                                                             loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object field for key: " +
                                        key,
                                        e);

        }

        return t;

    }

    public void createObject (UserConfigurableObjectTypeField t,
                              Connection                      conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (t.getKey ());
        params.add (t.getOrder ());
        params.add (t.getUserConfigurableObjectType ().getKey ());
        params.add (t.getType ().getType ());
        params.add (JSONEncoder.encode (t.getDefinition ()));

        this.objectManager.executeStatement ("INSERT INTO userobjecttypefield (dbkey, orderby, userobjecttypedbkey, type, definition) VALUES (?, ?, ?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (UserConfigurableObjectTypeField t,
                              boolean                         deleteChildObjects,
                              Connection                      conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (t.getKey ());

        this.objectManager.executeStatement ("DELETE FROM userobjecttypefield WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (UserConfigurableObjectTypeField t,
                              Connection                      conn)
                       throws GeneralException
    {

        List params = new ArrayList ();

        params.add (t.getOrder ());
        params.add (JSONEncoder.encode (t.getDefinition ()));
        params.add (t.getKey ());

        this.objectManager.executeStatement ("UPDATE userobjecttypefield SET orderby = ?, definition = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}
