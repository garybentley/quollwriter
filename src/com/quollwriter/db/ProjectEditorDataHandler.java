package com.quollwriter.db;

import java.sql.*;
import java.io.*;
import java.util.*;

import java.math.*;
import javax.crypto.*;
import java.security.*;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.openpgp.operator.bc.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;

public class ProjectEditorDataHandler implements DataHandler<ProjectEditor, ProjectInfo>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, editordbkey, forprojectid, forprojectname, status, statusmessage, current, editorfrom, editorto FROM projecteditor_v";

    private ObjectManager objectManager = null;

    /**
     * A cache, since the same projEditor can be viewed in multiple places and updated in multiple places
     * we need a single object for the editor so that we have one object -> multiple viewers.
     */
    private Map<Long, ProjectEditor> cache = new HashMap ();
    
    public ProjectEditorDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }
        
    @Override
    public void createObject (ProjectEditor pe,
                              Connection    conn)
                       throws GeneralException
    {
                
        if (pe.getForProjectId () == null)
        {
        
            throw new GeneralException ("No for project id specified.");
            
        }
        
        if (pe.getEditor () == null)
        {
            
            throw new GeneralException ("No editor is specified.");
            
        }
        
        List params = new ArrayList ();
        params.add (pe.getKey ());
        params.add (pe.getEditor ().getKey ());
        params.add (pe.getForProjectId ());
        params.add (pe.getForProjectName ());
        params.add (pe.getStatus ().getType ());
        params.add (pe.getStatusMessage ());
        params.add (pe.isCurrent ());
        params.add ((pe.getEditorFrom () != null ? pe.getEditorFrom () : new java.util.Date ()));
        params.add (pe.getEditorTo ());
                
        this.objectManager.executeStatement ("INSERT INTO projecteditor " +
                                             "(dbkey, " +
                                             " editordbkey, " +
                                             " forprojectid, " +
                                             " forprojectname, " +
                                             " status, " +
                                             " statusmessage, " +
                                             " current, " +
                                             " editorfrom, " +
                                             " editorto) " +
                                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);        
        
        this.cache.put (pe.getKey (),
                        pe);        
        
    }

    @Override
    public void deleteObject (ProjectEditor d,
                              boolean       deleteChildObjects,
                              Connection    conn)
                       throws GeneralException
    {
    
        List params = new ArrayList ();
        params.add (d.getKey ());
    
        this.objectManager.executeStatement ("DELETE FROM projecteditor WHERE dbkey = ?",
                                             params,
                                             conn);

        this.cache.remove (d.getKey ());
        
    }

    @Override
    public void updateObject (ProjectEditor pe,
                              Connection    conn)
                       throws GeneralException
    {
        
        List params = new ArrayList ();

        params.add (pe.getForProjectName ());
        params.add (pe.getStatus ().getType ());
        params.add (pe.getStatusMessage ());
        params.add (pe.isCurrent ());
        params.add (pe.getEditorFrom ());
        params.add (pe.getEditorTo ());
        params.add (pe.getKey ());
        
        this.objectManager.executeStatement ("UPDATE projecteditor " +
                                             "SET forprojectname = ?, " +
                                             "    status = ?, " +
                                             "    statusmessage = ?, " +
                                             "    current = ?, " +
                                             "    editorfrom = ?, " +
                                             "    editorto = ? " +
                                             "WHERE dbkey = ?",
                                             params,
                                             conn);        
        
    }

    @Override
    public List<ProjectEditor> getObjects (ProjectInfo p,
                                           Connection  conn,
                                           boolean     loadChildObjects)
                                    throws GeneralException
    {
                
        List<ProjectEditor> ret = new ArrayList ();

        ResultSet rs = null;
        
        try
        {

            List params = new ArrayList ();
            params.add (p.getId ());
        
            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE forprojectid = ? ORDER BY editorfrom",
                                                  params,
                                                  conn);

            while (rs.next ())
            {

                ret.add (this.getProjectEditor (rs));

            }
            
        } catch (Exception e) {

            throw new GeneralException ("Unable to load project editors for: " +
                                        p,
                                        e);

        } finally {
            
            try
            {

                rs.close ();

            } catch (Exception e)
            {
                
            }
        
        }            

        return ret;
        
    }
    
    private ProjectEditor getProjectEditor (ResultSet rs)
                                     throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            ProjectEditor pe = this.cache.get (key);
            
            if (pe != null)
            {
                
                return pe;
                
            }
            
            pe = new ProjectEditor ();
            
            long edKey = rs.getLong (ind++);
            
            EditorEditor ed = EditorsEnvironment.getEditorByKey (edKey);
            
            pe.setKey (key);
            pe.setEditor (ed);
            pe.setForProjectId (rs.getString (ind++));
            pe.setForProjectName (rs.getString (ind++));
            pe.setStatus (ProjectEditor.Status.valueOf (rs.getString (ind++)));
            pe.setStatusMessage (rs.getString (ind++));
            pe.setCurrent (rs.getBoolean (ind++));
            pe.setEditorFrom (rs.getTimestamp (ind++));
            pe.setEditorTo (rs.getTimestamp (ind++));
            
            this.cache.put (key,
                            pe);
            
            return pe;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load project editor",
                                        e);

        }
            
    }
        
    @Override
    public ProjectEditor getObjectByKey (long        key,
                                         ProjectInfo p,
                                         Connection  conn,
                                         boolean     loadChildObjects)
                                  throws GeneralException
    {
        
        ResultSet rs = null;
        
        try
        {

            List params = new ArrayList ();
            params.add (key);
        
            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                  params,
                                                  conn);

            if (rs.next ())
            {

                return this.getProjectEditor (rs);

            }
            
            return null;
            
        } catch (Exception e) {

            throw new GeneralException ("Unable to get project editor with key: " +
                                        key,
                                        e);

        } finally {
            
            try
            {

                rs.close ();

            } catch (Exception e)
            {
                
            }
        
        }            
                
    }

}
