package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

public class UserConfigurableObjectFieldDataHandler implements DataHandler<UserConfigurableObjectField, UserConfigurableObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, userobjecttypefielddbkey, value, name, description, markup, lastmodified, datecreated, properties, id, version FROM userobjectfield_v ";

    private ObjectManager objectManager = null;

    public UserConfigurableObjectFieldDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    public void deleteAllFieldsForObject (UserConfigurableObject obj,
                                          Connection             conn)
    {

        List<UserConfigurableObjectField> ret = new ArrayList ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey FROM userobjectfield_v WHERE namedobjectdbkey = ?",
                                                            Arrays.asList (obj.getKey ()),
                                                            conn);

            // Create a fake field.
            UserConfigurableObjectType _t = new UserConfigurableObjectType ();
            _t.setAssetObjectType (true);
            _t.setKey (1l);
            TextUserConfigurableObjectTypeField _tf = new TextUserConfigurableObjectTypeField ();
            _tf.setUserConfigurableObjectType (_t);

            while (rs.next ())
            {

                UserConfigurableObjectField _f = new UserConfigurableObjectField (_tf);

                _f.setKey (rs.getLong (1));

                this.objectManager.deleteObject (_f,
                                                 true,
                                                 conn);

            }

        } catch (Exception e) {

            Environment.logError ("Unable to delete object fields: " + obj.getKey (),
                                  e);

        }

    }

    private UserConfigurableObjectField getUserConfigurableObjectField (ResultSet              rs,
                                                                        UserConfigurableObject parent,
                                                                        boolean                loadChildObjects)
                                                                 throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            long typekey = rs.getLong (ind++);

            UserConfigurableObjectTypeField tf = (UserConfigurableObjectTypeField) Environment.getUserConfigurableObjectTypeField (typekey);

            if (tf == null)
            {

                // This isn't an error, the field may have been removed without the requesting project knowing about
                // the deletion.
                return null;

            }

            UserConfigurableObjectField f = new UserConfigurableObjectField (tf);

            f.setKey (key);

            String v = rs.getString (ind++);

            if (v != null)
            {

                f.setValue (tf.getViewEditHandler (parent,
                                                   f,
                                                   null).stringToValue (v));

            }

            f.setName (rs.getString (ind++));

            f.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            f.setLastModified (rs.getTimestamp (ind++));
            f.setDateCreated (rs.getTimestamp (ind++));
            f.setPropertiesAsString (rs.getString (ind++));
            f.setId (rs.getString (ind++));
            f.setVersion (rs.getString (ind++));
            f.setParent (parent);

            return f;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user object field",
                                        e);

        }

    }

    public List<UserConfigurableObjectField> getObjects (UserConfigurableObject parent,
                                                         Connection             conn,
                                                         boolean                loadChildObjects)
                                                  throws GeneralException
    {

        List<UserConfigurableObjectField> ret = new ArrayList<> ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND namedobjectdbkey = ?",
                                                            Arrays.asList (parent.getKey ()),
                                                            conn);

            while (rs.next ())
            {

                // The field may be null because the field definition could be deleted without us knowing, i.e.
                // while another project is being edited.
                UserConfigurableObjectField f = this.getUserConfigurableObjectField (rs,
                                                                                     parent,
                                                                                     loadChildObjects);

                if (f != null)
                {

                    ret.add (f);

                }

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user object fields for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public UserConfigurableObjectField getObjectByKey (long                   key,
                                                       UserConfigurableObject parent,
                                                       Connection             conn,
                                                       boolean                loadChildObjects)
                                                throws GeneralException
    {

        UserConfigurableObjectField t = null;

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",
                                                            Arrays.asList (key),
                                                            conn);

            if (rs.next ())
            {

                t = this.getUserConfigurableObjectField (rs,
                                                         parent,
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

            throw new GeneralException ("Unable to load user object field for key: " +
                                        key,
                                        e);

        }

        return t;

    }

    public void createObject (UserConfigurableObjectField t,
                              Connection                  conn)
                       throws GeneralException
    {

        if (t.getParent () == null)
        {

            throw new GeneralException ("The parent user configurable object must be specified for the field: " +
                                        t);

        }

        if (t.getUserConfigurableObjectTypeField () == null)
        {

            throw new GeneralException ("The object type field must be specified for field: " +
                                        t);

        }

        List<Object> params = new ArrayList<> ();
        params.add (t.getKey ());
        params.add (t.getUserConfigurableObjectTypeField ().getKey ());
        params.add (t.getParent ().getKey ());

        params.add (t.getUserConfigurableObjectTypeField ().getViewEditHandler (t.getParentObject (),
                                                                                t,
                                                                                null).valueToString (t.getValue ()));

        this.objectManager.executeStatement ("INSERT INTO userobjectfield (dbkey, userobjecttypefielddbkey, namedobjectdbkey, value) VALUES (?, ?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (UserConfigurableObjectField t,
                              boolean                     deleteChildObjects,
                              Connection                  conn)
                       throws GeneralException
    {

        Set<String> filesToDelete = new HashSet<> ();

        for (String fn : t.getProjectFileNames ())
        {

            if (fn == null)
            {

                continue;

            }

            filesToDelete.add (fn);

        }

        this.objectManager.executeStatement ("DELETE FROM userobjectfield WHERE dbkey = ?",
                                             Arrays.asList (t.getKey ()),
                                             conn);

        for (String fn : filesToDelete)
        {

            this.objectManager.getProject ().deleteFile (fn);

        }

    }

    public void updateObject (UserConfigurableObjectField t,
                              Connection                  conn)
                       throws GeneralException
    {

        String val = t.getUserConfigurableObjectTypeField ().getViewEditHandler (t.getParentObject (),
                                                                                 t,
                                                                                 null).valueToString (t.getValue ());

        this.objectManager.executeStatement ("UPDATE userobjectfield SET value = ? WHERE dbkey = ?",
                                             Arrays.asList (val, t.getKey ()),
                                             conn);

    }

}
