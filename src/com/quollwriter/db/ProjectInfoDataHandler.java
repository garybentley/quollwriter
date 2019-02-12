package com.quollwriter.db;

import java.sql.*;
import java.io.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public class ProjectInfoDataHandler implements DataHandler<ProjectInfo, NamedObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, directory, backupdirectory, status, statistics, encrypted, nocredentials, icon, type, foreditor, lastedited, description, markup, id, lastmodified, datecreated, properties FROM projectinfo_v ";
    private ObjectManager objectManager = null;

    /**
     * A cache, since the same projEditor can be viewed in multiple places and updated in multiple places
     * we need a single object for the editor so that we have one object -> multiple viewers.
     */
    private Map<Long, ProjectInfo> cache = new HashMap<> ();

    public ProjectInfoDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private ProjectInfo getProjectInfo (ResultSet      rs,
                                        boolean        loadChildObjects)
                                 throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            ProjectInfo p = this.cache.get (key);

            if (p != null)
            {

                return p;

            }

            p = new ProjectInfo ();

            p.setKey (key);
            p.setName (rs.getString (ind++));
            p.setProjectDirectory (new File (rs.getString (ind++)));
            p.setBackupDirectory (new File (rs.getString (ind++)));
            p.setStatus (rs.getString (ind++));

            // Get the statistics which should look something like:
            //
            // <statistics>
            //   <stat id="xxx">[value]</stat>
            // </statistics>
            p.setStatistics (Utils.getStatisticsFromXML (rs.getString (ind++)));

            p.setEncrypted (rs.getBoolean (ind++));
            p.setNoCredentials (rs.getBoolean (ind++));

            String ic = rs.getString (ind++);

            if (ic != null)
            {

                p.setIcon (new File (ic));

            }

            p.setType (rs.getString (ind++));

            String edEmail = rs.getString (ind++);

            if (edEmail != null)
            {

                EditorEditor ed = EditorsEnvironment.getEditorByEmail (edEmail);

                p.setForEditor (ed);

            }

            p.setLastEdited (rs.getTimestamp (ind++));
            p.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            p.setId (rs.getString (ind++));

            p.setLastModified (rs.getTimestamp (ind++));
            p.setDateCreated (rs.getTimestamp (ind++));
            p.setPropertiesAsString (rs.getString (ind++));

            this.cache.put (key,
                            p);

            return p;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load project info",
                                        e);

        }

    }

    @Override
    public List<ProjectInfo> getObjects (NamedObject parent,
                                         Connection  conn,
                                         boolean     loadChildObjects)
                              throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.objectManager.getConnection ();
            closeConn = true;

        }

        ResultSet rs = null;

        try
        {

            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX,
                                                  null,
                                                  conn);

            List<ProjectInfo> ret = new ArrayList<> ();

            while (rs.next ())
            {

                ProjectInfo p = this.getProjectInfo (rs,
                                                     loadChildObjects);

                ret.add (p);

            }

            return ret;

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to load project info",
                                               e);

        } finally
        {

            if (closeConn)
            {

                this.objectManager.releaseConnection (conn);

            }

        }

        return null;

    }

    @Override
    public ProjectInfo getObjectByKey (long        key,
                                       NamedObject parent,
                                       Connection  conn,
                                       boolean     loadChildObjects)
                                throws GeneralException
    {

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.objectManager.getConnection ();
            closeConn = true;

        }

        ResultSet rs = null;

        try
        {

            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                  Arrays.asList (key),
                                                  conn);

            if (rs.next ())
            {

                return this.getProjectInfo (rs,
                                            loadChildObjects);

            }

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to load project info for key: " +
                                               key,
                                               e);

        } finally
        {

            if (closeConn)
            {

                this.objectManager.releaseConnection (conn);

            }

        }

        return null;

    }

    @Override
    public void createObject (ProjectInfo p,
                              Connection  conn)
                       throws GeneralException
    {

        if ((p.isEditorProject ())
            &&
            (p.getForEditor () == null)
           )
        {

            throw new IllegalStateException ("If the project is for an editor then the forEditor must be specified.");

        }

        List<Object> params = new ArrayList<> ();
        params.add (p.getKey ());
        params.add (p.getLastEdited ());
        params.add (p.isEncrypted ());
        params.add (p.isNoCredentials ());
        params.add (p.getProjectDirectory ().getPath ());
        params.add (p.getBackupDirectory ().getPath ());
        params.add (p.getStatus ());
        params.add (Utils.getStatisticsAsXML (p.getStatistics ()));
        params.add ((p.getIcon () != null ? p.getIcon ().getPath () : null));
        params.add (p.getType ());
        params.add ((p.getForEditor () != null ? p.getForEditor ().getEmail () : null));

        this.objectManager.executeStatement ("INSERT INTO projectinfo (dbkey, lastedited, encrypted, nocredentials, directory, backupdirectory, status, statistics, icon, type, foreditor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);

        this.cache.put (p.getKey (),
                        p);

    }

    @Override
    public void deleteObject (ProjectInfo p,
                              boolean     deleteChildObjects,
                              Connection  conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("DELETE FROM projectinfo WHERE dbkey = ?",
                                             Arrays.asList (p.getKey ()),
                                             conn);

        this.cache.remove (p.getKey ());

    }

    @Override
    public void updateObject (ProjectInfo p,
                              Connection  conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE projectinfo SET lastedited = ?, directory = ?, backupdirectory = ?, status = ?, statistics = ?, icon = ? WHERE dbkey = ?",
                                             Arrays.asList (p.getLastEdited (),
                                                            p.getProjectDirectory ().getPath (),
                                                            p.getBackupDirectory ().getPath (),
                                                            p.getStatus (),
                                                            Utils.getStatisticsAsXML (p.getStatistics ()),
                                                            (p.getIcon () != null ? p.getIcon ().getPath () : null),
                                                            p.getKey ()),
                                             conn);

    }

}
