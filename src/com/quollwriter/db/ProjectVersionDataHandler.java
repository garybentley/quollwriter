package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import javax.swing.text.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

public class ProjectVersionDataHandler implements DataHandler<ProjectVersion, Project>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, due, name, description, lastmodified, datecreated, properties, id, version, latest FROM projectversion_v ";

    private ObjectManager objectManager = null;

    public ProjectVersionDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private ProjectVersion getProjectVersion (ResultSet rs)
                                       throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getInt (ind++);

            ProjectVersion pv = new ProjectVersion ();
            pv.setDueDate (rs.getTimestamp (ind++));
            pv.setName (rs.getString (ind++));
            pv.setKey (key);
            pv.setDescription (rs.getString (ind++));

            pv.setLastModified (rs.getTimestamp (ind++));
            pv.setDateCreated (rs.getTimestamp (ind++));
            pv.setPropertiesAsString (rs.getString (ind++));
            pv.setId (rs.getString (ind++));
            pv.setVersion (rs.getString (ind++));
            pv.setLatest (rs.getBoolean (ind++));
            
            return pv;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load project version",
                                        e);

        }

    }

    @Override    
    public List<ProjectVersion> getObjects (Project     proj,
                                            Connection  conn,
                                            boolean     loadChildObjects)
                                     throws GeneralException
    {

        try
        {
    
            List<ProjectVersion> ret = new ArrayList ();
    
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " ORDER BY datecreated",
                                                            null,
                                                            conn);
    
            while (rs.next ())
            {
    
                ret.add (this.getProjectVersion (rs));
    
            }
    
            if (loadChildObjects)
            {
                
                // Get all the chapters with the version.
                
            }
    
            try
            {
    
                rs.close ();
    
            } catch (Exception e)
            {
            }

            return ret;
        
        } catch (Exception e) {
            
            throw new GeneralException ("Uanble to get all project versions",
                                        e);
            
        }

    }

    @Override
    public ProjectVersion getObjectByKey (int        key,
                                          Project    proj,
                                          Connection conn,
                                          boolean    loadChildObjects)
                                   throws GeneralException
    {

        try
        {
    
            ProjectVersion pv = null;
    
            List params = new ArrayList ();
            params.add (key);
        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                            params,
                                                            conn);
                                                                
            if (rs.next ())
            {
    
                pv = this.getProjectVersion (rs);
        
            }
    
            try
            {
    
                rs.close ();
    
            } catch (Exception e)
            {
            }
    
            return pv;
        
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to get project version: " +
                                        key,
                                        e);
            
        }

    }

    public ProjectVersion getById (String id)
                            throws GeneralException
    {

        Connection conn = this.objectManager.getConnection ();    
    
        try
        {
    
            ProjectVersion pv = null;
    
            List params = new ArrayList ();
            params.add (id);
        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE id = ?",
                                                            params,
                                                            conn);
                                                                
            if (rs.next ())
            {
    
                pv = this.getProjectVersion (rs);
        
            }
    
            try
            {
    
                rs.close ();
    
            } catch (Exception e)
            {
            }
    
            return pv;
        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get project version for id: " +
                                               id,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
            
        }
        
        return null;

    }

    public ProjectVersion getLatest (Connection conn)
                              throws GeneralException
    {
        
        boolean closeConn = false;
        
        if (conn == null)
        {
            
            conn = this.objectManager.getConnection ();
            closeConn = true;
            
        }
    
        try
        {
    
            ProjectVersion pv = null;
            
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE latest = TRUE",
                                                            null,
                                                            conn);
                                                                
            if (rs.next ())
            {
    
                pv = this.getProjectVersion (rs);
        
            }
    
            try
            {
    
                rs.close ();
    
            } catch (Exception e)
            {
            }
    
            return pv;
        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get latest project version",
                                               e);

        } finally {
            
            if (closeConn)
            {
                
                this.objectManager.releaseConnection (conn);
                
            }
            
        }
        
        return null;

    }

    @Override
    public void createObject (ProjectVersion pv,
                              Connection     conn)
                       throws GeneralException
    {

        List params = new ArrayList ();
        params.add (ProjectVersion.OBJECT_TYPE);
        
        // Mark all other versions as previous.
        this.objectManager.executeStatement ("UPDATE dataobject SET latest = FALSE WHERE objecttype = ?",
                                             params,
                                             conn);
        
        params = new ArrayList ();
        params.add (pv.getKey ());
        params.add (pv.getDueDate ());
        
        this.objectManager.executeStatement ("INSERT INTO projectversion (dbkey, due) VALUES (?, ?)",
                                             params,
                                             conn);

        this.objectManager.setLatestVersion (pv,
                                             true,
                                             conn);
                                             
    }

    @Override    
    public void deleteObject (ProjectVersion pv,
                              boolean        deleteChildObjects,
                              Connection     conn)
                       throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported for project versions.");
/*
        ProjectVersion pv = (ProjectVersion) d;
    
        ChapterDataHandler cdh = (ChapterDataHandler) this.objectManager.getHandler (Chapter.class);

        List params = new ArrayList ();
        params.add (pv.getKey ());
            
        this.objectManager.executeStatement ("DELETE FROM projectversion WHERE dbkey = ?",
                                             params,
                                             conn);

        this.versions.remove (pv.getKey ());
  */                                           
    }

    @Override
    public void updateObject (ProjectVersion pv,
                              Connection     conn)
                       throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported for project versions.");
    
    }

}
