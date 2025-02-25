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
    private Map<Long, ProjectEditor> cache = new HashMap<> ();

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
                                             Arrays.asList (pe.getKey (),
                                                            pe.getEditor ().getKey (),
                                                            pe.getForProjectId (),
                                                            pe.getForProjectName (),
                                                            pe.getStatus ().getType (),
                                                            pe.getStatusMessage (),
                                                            pe.isCurrent (),
                                                            (pe.getEditorFrom () != null ? pe.getEditorFrom () : new java.util.Date ()),
                                                            pe.getEditorTo ()),
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

        this.objectManager.executeStatement ("DELETE FROM projecteditor WHERE dbkey = ?",
                                             Arrays.asList (d.getKey ()),
                                             conn);

        this.cache.remove (d.getKey ());

    }

    @Override
    public void updateObject (ProjectEditor pe,
                              Connection    conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE projecteditor " +
                                             "SET forprojectname = ?, " +
                                             "    status = ?, " +
                                             "    statusmessage = ?, " +
                                             "    current = ?, " +
                                             "    editorfrom = ?, " +
                                             "    editorto = ? " +
                                             "WHERE dbkey = ?",
                                             Arrays.asList (pe.getForProjectName (),
                                                            pe.getStatus ().getType (),
                                                            pe.getStatusMessage (),
                                                            pe.isCurrent (),
                                                            pe.getEditorFrom (),
                                                            pe.getEditorTo (),
                                                            pe.getKey ()),
                                             conn);

    }

    @Override
    public List<ProjectEditor> getObjects (ProjectInfo p,
                                           Connection  conn,
                                           boolean     loadChildObjects)
                                    throws GeneralException
    {

        List<ProjectEditor> ret = new ArrayList<> ();

        ResultSet rs = null;

        try
        {

            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE forprojectid = ? ORDER BY editorfrom",
                                                  Arrays.asList (p.getId ()),
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

            String sm = rs.getString (ind++);

            // Hack to prevent json decoding problems.
            if (sm.startsWith ("{Project}"))
            {

                sm = Utils.replaceString (sm,
                                          "{Project}",
                                          Environment.getUIString (LanguageStrings.objectnames,LanguageStrings.singular,LanguageStrings.project));

            }

            pe.setStatusMessage (sm);
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

            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                  Arrays.asList (key),
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

    public void deleteFromAllProjects (EditorEditor ed,
                                       Connection   conn)
                                throws GeneralException
    {

        List<ProjectEditor> ret = new ArrayList<> ();

        ResultSet rs = null;

        try
        {

            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE editordbkey = ?",
                                                  Arrays.asList (ed.getKey ()),
                                                  conn);

            while (rs.next ())
            {

                ret.add (this.getProjectEditor (rs));

            }

        } catch (Exception e) {

            throw new GeneralException ("Unable to load project editors for: " +
                                        ed,
                                        e);

        }

        for (ProjectEditor pe : ret)
        {

            this.objectManager.deleteObject (pe,
                                             true,
                                             conn);

        }

    }

}
