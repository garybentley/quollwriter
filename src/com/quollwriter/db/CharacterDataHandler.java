package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class CharacterDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public CharacterDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private QCharacter getCharacter (ResultSet rs,
                                     boolean   loadChildObjects)
                              throws GeneralException
    {

        try
        {

            int ind = 1;

            QCharacter c = new QCharacter ();

            long key = rs.getLong (ind++);

            c.setKey (key);
            c.setName (rs.getString (ind++));
            c.setDescription (rs.getString (ind++));

            c.setLastModified (rs.getTimestamp (ind++));
            c.setDateCreated (rs.getTimestamp (ind++));
            c.setPropertiesAsString (rs.getString (ind++));

            c.setAliases (rs.getString (ind++));

            // Get all the notes.
            if (loadChildObjects)
            {
                /*
                this.objectManager.loadNotes (c,
                                              rs.getStatement ().getConnection ());
                 */
            }

            return c;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load character",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<QCharacter> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, aliases FROM character_v WHERE projectdbkey = ?",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getCharacter (rs,
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

            throw new GeneralException ("Unable to load characters for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        QCharacter c = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, aliases FROM character_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                c = this.getCharacter (rs,
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

            throw new GeneralException ("Unable to load character for key: " +
                                        key,
                                        e);

        }

        return c;

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        QCharacter c = (QCharacter) d;

        List params = new ArrayList ();
        params.add (c.getKey ());
        params.add (c.getAliases ());
        params.add (c.getProject ().getKey ());

        this.objectManager.executeStatement ("INSERT INTO character (dbkey, aliases, projectdbkey) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        QCharacter c = (QCharacter) d;

        List params = new ArrayList ();
        params.add (c.getKey ());

        this.objectManager.executeStatement ("DELETE FROM character WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        QCharacter c = (QCharacter) d;

        List params = new ArrayList ();
        params.add (c.getAliases ());
        params.add (c.getKey ());
        
        this.objectManager.executeStatement ("UPDATE character SET aliases = ? WHERE dbkey = ?",
                                             params,
                                             conn);
         
    }

}
