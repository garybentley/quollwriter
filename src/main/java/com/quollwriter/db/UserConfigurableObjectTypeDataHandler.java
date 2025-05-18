package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;

public class UserConfigurableObjectTypeDataHandler implements DataHandler<UserConfigurableObjectType, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, userobjtype, name, pluralname, description, markup, icon24x24, icon16x16, layout, assetobjtype, createshortcutkey, lastmodified, datecreated, properties, id, version FROM userobjecttype_v ";

    private ObjectManager objectManager = null;

    /**
     * A cache, since the same type can be used in multiple places
     * we need a single object so that we have one object -> multiple uses.
     */
    private Map<Long, UserConfigurableObjectType> cache = new HashMap ();

    public UserConfigurableObjectTypeDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    private UserConfigurableObjectType getUserConfigurableObjectType (ResultSet rs,
                                                                      Project   parent,
                                                                      boolean   loadChildObjects)
                                                               throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            UserConfigurableObjectType t = this.cache.get (key);

            if (t != null)
            {

                return t;

            }

            t = new UserConfigurableObjectType (parent);
            t.setKey (key);
            t.setUserObjectType (rs.getString (ind++));

            String n = rs.getString (ind++);
            t.setName (n);
            t.setObjectTypeName (n);
/*
            if (t.getUserObjectType ().equals ("chapter"))
            {
                t.setObjectTypeName (null);
            }
*/
            t.setObjectTypeNamePlural (rs.getString (ind++));
/*
Not needed, ProjectDataHandler handles this.
            if ((t.getObjectTypeNamePlural () != null)
                &&
                (t.getObjectTypeNamePlural ().equals ("objectnames.plural.null"))
                &&
                (t.getUserObjectType () != null)
               )
            {

                // See if we can get the type.
                // Fix issue with plural being null
                t.setObjectTypeNamePlural (com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty (LanguageStrings.objectnames,
                                                                                                 LanguageStrings.plural,
                                                                                                 t.getUserObjectType ()).getValue ());

            }

            if ((t.getObjectTypeName () != null)
                &&
                (t.getObjectTypeName ().equals ("objectnames.singular.null"))
                &&
                (t.getUserObjectType () != null)
               )
            {

                // See if we can get the type.
                // Fix issue with plural being null
                t.setObjectTypeName (com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty (LanguageStrings.objectnames,
                                                                                                                     LanguageStrings.singular,
                                                                                                                     t.getUserObjectType ()).getValue ());

            }
*/
            /*
            if (t.getUserObjectType ().equals ("chapter"))
            {
                t.setObjectTypeNamePlural (null);
            }
            */
            t.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));

            t.setIcon24x24 (UIUtils.getImage (rs.getBytes (ind++)));
            t.setIcon16x16 (UIUtils.getImage (rs.getBytes (ind++)));
            t.setLayout (rs.getString (ind++));
            t.setAssetObjectType (rs.getBoolean (ind++));
            // TODO Need to remove this.
            rs.getString (ind++);
            // TODO t.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke (rs.getString (ind++)));
            t.setLastModified (rs.getTimestamp (ind++));
            t.setDateCreated (rs.getTimestamp (ind++));
            t.setPropertiesAsString (rs.getString (ind++));
            t.setId (rs.getString (ind++));
            t.setVersion (rs.getString (ind++));

            Connection conn = rs.getStatement ().getConnection ();

            t.setConfigurableFields ((List<UserConfigurableObjectTypeField>) this.objectManager.getObjects (UserConfigurableObjectTypeField.class,
                                                                                                            t,
                                                                                                            conn,
                                                                                                            true));

            if (parent != null)
            {

                parent.addUserConfigurableObjectType (t);

            } else {

                // Keep this around, needed so we can get the environment type.
                Environment.addUserConfigurableObjectType (t);

            }

            this.cache.put (key,
                            t);

            return t;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load user configurable object type",
                                        e);

        }

    }

    public List<UserConfigurableObjectType> getObjects (Project     parent,
                                                        Connection  conn,
                                                        boolean     loadChildObjects)
                                                 throws GeneralException
    {

        List<UserConfigurableObjectType> ret = new ArrayList<> ();

        try
        {

            List<Object> params = new ArrayList<> ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getUserConfigurableObjectType (rs,
                                                             parent,
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

            throw new GeneralException ("Unable to load user configurable object types for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public UserConfigurableObjectType getObjectByKey (long        key,
                                                      Project     parent,
                                                      Connection  conn,
                                                      boolean     loadChildObjects)
                                               throws GeneralException
    {

        UserConfigurableObjectType t = null;

        try
        {

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE AND dbkey = ?",
                                                            Arrays.asList (key),
                                                            conn);

            if (rs.next ())
            {

                t = this.getUserConfigurableObjectType (rs,
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

            throw new GeneralException ("Unable to load user configurable object type for key: " +
                                        key,
                                        e);

        }

        return t;

    }

    public void createObject (UserConfigurableObjectType t,
                              Connection                 conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("INSERT INTO userobjecttype (dbkey, userobjtype, pluralname, icon24x24, icon16x16, layout, assetobjtype, createshortcutkey) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                             Arrays.asList (t.getKey (),
                                                            t.getUserObjectType (),
                                                            t.getActualObjectTypeNamePlural (),
                                                            UIUtils.getImageBytes (t.getIcon24x24 ()),
                                                            UIUtils.getImageBytes (t.getIcon16x16 ()),
                                                            t.getLayout (),
                                                            t.isAssetObjectType (),
                                                            Utils.keyStrokeToString (t.getCreateShortcutKeyStroke ())),
                                             conn);

        // Save the fields.
        this.objectManager.saveObjects (t.getConfigurableFields (),
                                        conn);

    }

    public void deleteObject (UserConfigurableObjectType t,
                              boolean                    deleteChildObjects,
                              Connection                 conn)
                       throws GeneralException
    {

        // Delete the fields first.
        this.objectManager.deleteObjects (t.getConfigurableFields (),
                                          conn);

        this.objectManager.executeStatement ("DELETE FROM userobjecttype WHERE dbkey = ?",
                                             Arrays.asList (t.getKey ()),
                                             conn);

    }

    public void updateObject (UserConfigurableObjectType t,
                              Connection                 conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE userobjecttype SET pluralname = ?, icon24x24 = ?, icon16x16 = ?, layout = ?, createshortcutkey = ? WHERE dbkey = ?",
                                             Arrays.asList (t.getActualObjectTypeNamePlural (),
                                                            UIUtils.getImageBytes (t.getIcon24x24 ()),
                                                            UIUtils.getImageBytes (t.getIcon16x16 ()),
                                                            t.getLayout (),
                                                            Utils.keyStrokeToString (t.getCreateShortcutKeyStroke ()),
                                                            t.getKey ()),
                                             conn);

        // Save the fields.
        this.objectManager.saveObjects (t.getConfigurableFields (),
                                        conn);

    }
/*
TODO Remove
    public void updateFieldOrdering (UserConfigurableObjectType t)
                              throws GeneralException
    {

        Connection conn = null;

        try
        {

            conn = this.objectManager.getConnection ();

            this.objectManager.executeStatement ("UPDATE userobjecttypefield SET orderby = NULL WHERE userobjecttypedbkey = ?",
                                                 Arrays.asList (t.getKey ()),
                                                 conn);

            for (UserConfigurableObjectTypeField f : t.getConfigurableFields ())
            {

                 this.objectManager.executeStatement ("UPDATE userobjecttypefield SET orderby = ? WHERE dbkey = ? AND userobjecttypedbkey = ?",
                                                      Arrays.asList (f.getOrder (),
                                                                     f.getKey (),
                                                                     t.getKey ()),
                                                      conn);

            }

        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to update field orderbys for type: " +
                                               t.getKey (),
                                               e);

        } finally {

            this.objectManager.releaseConnection (conn);


        }

    }
*/
}
