package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public class ProjectDataHandler implements DataHandler<Project, NamedObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, name, type, lastedited, description, markup, files, id, lastmodified, datecreated, properties FROM project_v ";
    private ObjectManager objectManager = null;

    public ProjectDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Project getProject (ResultSet      rs,
                                ProjectVersion pv,
                                boolean        loadChildObjects)
                         throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Project p = new Project ();

            p.setKey (key);
            p.setName (rs.getString (ind++));
            p.setType (rs.getString (ind++));
            p.setLastEdited (rs.getTimestamp (ind++));
            p.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            p.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));
            p.setId (rs.getString (ind++));

            p.setLastModified (rs.getTimestamp (ind++));
            p.setDateCreated (rs.getTimestamp (ind++));
            p.setPropertiesAsString (rs.getString (ind++));

            Connection conn = rs.getStatement ().getConnection ();

            rs.close ();

            if (pv != null)
            {

                p.setProjectVersion (pv);

            }

            if (p.isEditorProject ())
            {

                if (pv == null)
                {

                    ProjectVersionDataHandler pvh = (ProjectVersionDataHandler) this.objectManager.getHandler (ProjectVersion.class);

                    p.setProjectVersion (pvh.getLatest (conn));

                }
                /*
                // Get the editor email.
                String edEmail = p.getProperty (Constants.FOR_EDITOR_EMAIL_PROPERTY_NAME);

                if (edEmail == null)
                {

                    // This is a strange situation, what to do?

                }

                EditorEditor ed = EditorsEnvironment.getEditorByEmail (edEmail);

                p.setForEditor (ed);
                */
            } else {

                // Get the project editors.
                p.setProjectEditors (EditorsEnvironment.getProjectEditors (p.getId ()));

            }

            if (loadChildObjects)
            {
/*
                this.objectManager.getObjects (UserConfigurableObjectType.class,
                                               p,
                                               conn,
                                               loadChildObjects);

                // If we have no user config object types then create the ones we need.
                // We should always have at least the chapter type!
                UserConfigurableObjectType chapT = p.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);

                if (chapT.getKey () == null)
                {

                    // Init the user config object types.
                    this.createUserConfigurableObjectTypes (p,
                                                            conn);

                }
  */

                this.objectManager.getObjects (Book.class,
                                               p,
                                               conn,
                                               loadChildObjects);

                if (Environment.hasUserConfigurableObjectType (QCharacter.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (QCharacter.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                if (Environment.hasUserConfigurableObjectType (Location.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (Location.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                if (Environment.hasUserConfigurableObjectType (QObject.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (QObject.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                if (Environment.hasUserConfigurableObjectType (ResearchItem.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (ResearchItem.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                this.objectManager.getObjects (Asset.class,
                                               p,
                                               conn,
                                               loadChildObjects);

                this.objectManager.loadNotes (p,
                                              conn);

                this.objectManager.getObjects (IdeaType.class,
                                               p,
                                               conn,
                                               loadChildObjects);

                List<Link> links = (List<Link>) this.objectManager.getObjects (Link.class,
                                               p,
                                               conn,
                                               true);

                for (Link l : links)
                {

                    l.getObject1 ().addLink (l);

                }

/*
                p.setEditorProject ((EditorProject) this.objectManager.getObjectByKey (EditorProject.class,
                                                                                       -1,
                                                                                       conn,
                                                                                       loadChildObjects));
  */
            }

            // Do a check of the positions of the chapter items.
            // There is a situation where the position could be greater than the length of the chapter
            // (if you create a chapter, add some items but then don't save the text).
            Set<NamedObject> chaps = p.getAllNamedChildObjects (Chapter.class);

            for (NamedObject n : chaps)
            {

                Chapter c = (Chapter) n;

                int l = 0;

                String t = c.getChapterText ();

                if (t != null)
                {

                    l = t.length ();

                }

                Set<NamedObject> items = c.getAllNamedChildObjects ();
/*
                for (NamedObject ni : items)
                {

                    ChapterItem i = (ChapterItem) ni;

                    if ((i.getPosition () >= l)
                         ||
                        (i.getPosition () < 0)
                       )
                    {

                        i.setPosition ((l > 0 ? l : 0));

                        try
                        {

                            this.objectManager.saveObject (i,
                                                           conn);

                        } catch (Exception e) {

                            Environment.logError ("Unable to update item: " +
                                                  i,
                                                  e);

                        }

                    }

                    if (i instanceof Scene)
                    {

                        Scene s = (Scene) i;

                        Set<OutlineItem> oitems = s.getOutlineItems ();

                        if (oitems != null)
                        {

                            for (OutlineItem oi : oitems)
                            {

                                if ((oi.getPosition () >= l)
                                     ||
                                    (oi.getPosition () < 0)
                                   )
                                {

                                    oi.setPosition ((l > 0 ? l : 0));

                                    try
                                    {

                                        this.objectManager.saveObject (oi,
                                                                       conn);

                                    } catch (Exception e) {

                                        Environment.logError ("Unable to update item: " +
                                                              oi,
                                                              e);

                                    }

                                }

                            }

                        }

                    }

                }
                */

            }

            return p;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load project",
                                        e);

        }

    }
    /*
    private void createUserConfigurableObjectTypes (Project    p,
                                                    Connection conn)
                                             throws GeneralException
    {

        UserConfigurableObjectType chapterType = p.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);

        this.objectManager.saveObject (chapterType,
                                       conn);

        // Save the chapter type against all current chapters.
        this.objectManager.setUserConfigurableObjectTypeForObjectsOfType (Chapter.OBJECT_TYPE,
                                                                          chapterType,
                                                                          conn);

        UserConfigurableObjectType characterType = p.getUserConfigurableObjectType (QCharacter.OBJECT_TYPE);

        this.objectManager.saveObject (characterType,
                                       conn);

        // Save the character type against all current characters.
        this.objectManager.setUserConfigurableObjectTypeForObjectsOfType (QCharacter.OBJECT_TYPE,
                                                                          characterType,
                                                                          conn);

        UserConfigurableObjectType locType = p.getUserConfigurableObjectType (Location.OBJECT_TYPE);

        this.objectManager.saveObject (locType,
                                       conn);

        // Save the location type against all current locations.
        this.objectManager.setUserConfigurableObjectTypeForObjectsOfType (Location.OBJECT_TYPE,
                                                                          locType,
                                                                          conn);

        UserConfigurableObjectType qobjType = p.getUserConfigurableObjectType (QObject.OBJECT_TYPE);

        this.objectManager.saveObject (qobjType,
                                       conn);

        // Save the location type against all current locations.
        this.objectManager.setUserConfigurableObjectTypeForObjectsOfType (QObject.OBJECT_TYPE,
                                                                          qobjType,
                                                                          conn);

        UserConfigurableObjectType riType = p.getUserConfigurableObjectType (ResearchItem.OBJECT_TYPE);

        this.objectManager.saveObject (riType,
                                       conn);

        // Save the research item type against all current research items.
        this.objectManager.setUserConfigurableObjectTypeForObjectsOfType (ResearchItem.OBJECT_TYPE,
                                                                          riType,
                                                                          conn);

        // Notes

        // Links?

        // Idea Type

        // Ideas

        // Editors?

        // Outline items

        // Scenes

    }
    */
    @Override
    public List<Project> getObjects (NamedObject parent,
                                     Connection  conn,
                                     boolean     loadChildObjects)
                              throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported");

    }

    @Override
    public Project getObjectByKey (long        key,
                                   NamedObject parent,
                                   Connection  conn,
                                   boolean     loadChildObjects)
                            throws GeneralException
    {

        // There can be only one...
        return this._getProject (null,
                                 conn,
                                 loadChildObjects);

    }

    public List<WordCount> getWordCounts (Project p,
                                          int     daysPast)
                                   throws GeneralException
    {

        Connection conn = null;

        try
        {

            conn = this.objectManager.getConnection ();

        } catch (Exception e) {

            this.objectManager.throwException (null,
                                               "Unable to get connection",
                                               e);

        }

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (p.getKey ());

            String whereDays = "";

            if (daysPast != 0)
            {

                whereDays = " AND start > DATEADD ('DAY', ?, CURRENT_DATE) ";
                params.add (daysPast);

            }

            ResultSet rs = this.objectManager.executeQuery ("SELECT sum(count), start FROM wordcount WHERE projectdbkey = ? " + whereDays + " GROUP BY start ORDER BY start",
                                                            params,
                                                            conn);

            List<WordCount> ret = new ArrayList<> ();

            while (rs.next ())
            {

                int ind = 1;

                WordCount c = new WordCount ();
                c.setCount (rs.getInt (ind++));

                c.setEnd (rs.getTimestamp (ind++));
                c.setProject (p);

                ret.add (c);

            }

            return ret;

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to load word counts for project: " +
                                               p,
                                               e);

        } finally {

            this.objectManager.releaseConnection (conn);

        }

        return null;

    }

    private Project _getProject (ProjectVersion pv,
                                 Connection     conn,
                                 boolean        loadChildObjects)
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

            if (rs.next ())
            {

                Project p = this.getProject (rs,
                                             pv,
                                             loadChildObjects);

                return p;

            }

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to load project",
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

    public Project getProjectAtVersion (ProjectVersion pv,
                                        Connection     conn)
                                 throws GeneralException
    {

        return this._getProject (pv,
                                 conn,
                                 true);

    }

    public Project getProject (Connection conn)
                        throws GeneralException
    {

        return this._getProject (null,
                                 conn,
                                 true);

    }

    @Override
    public void createObject (Project    p,
                              Connection conn)
                       throws GeneralException
    {

        if ((p.isEditorProject ())
            &&
            ((p.getForEditor () == null)
             &&
             (p.getProperty (Constants.FOR_EDITOR_EMAIL_PROPERTY_NAME) == null)
            )
           )
        {

            throw new IllegalStateException ("If the project is for an editor then the forEditor must be specified.");

        }

        this.objectManager.executeStatement ("INSERT INTO project (dbkey, schema_version, type) VALUES (?, ?, ?)",
                                             Arrays.asList (p.getKey (),
                                                            Environment.getSchemaVersion (),
                                                            p.getType ()),
                                             conn);

        // Need to create the project version first since the chapters rely on it.
        if (p.getProjectVersion () != null)
        {

            this.objectManager.saveObject (p.getProjectVersion (),
                                           conn);

        }

        // Get all the assets.
        Set<UserConfigurableObjectType> asTypes = p.getAssetTypes ();

        for (UserConfigurableObjectType t : asTypes)
        {

            Set<Asset> assets = p.getAssets (t);

            for (Asset a : assets)
            {

                this.objectManager.saveObject (a,
                                               conn);

            }

        }
        /*

        // Get all the other objects.
        for (QCharacter c : p.getCharacters ())
        {

            this.objectManager.saveObject (c,
                                           conn);

        }

        for (Location l : p.getLocations ())
        {

            this.objectManager.saveObject (l,
                                           conn);

        }

        for (QObject q : p.getQObjects ())
        {

            this.objectManager.saveObject (q,
                                           conn);

        }

        for (ResearchItem i : p.getResearchItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }
*/
        for (Book b : p.getBooks ())
        {

            this.objectManager.saveObject (b,
                                           conn);

        }

    }

    @Override
    public void deleteObject (Project    p,
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        throw new GeneralException ("Not supported");

    }

    @Override
    public void updateObject (Project    p,
                              Connection conn)
                       throws GeneralException
    {

        this.objectManager.executeStatement ("UPDATE project SET lastedited = ? WHERE dbkey = ?",
                                             Arrays.asList (p.getLastEdited (),
                                                            p.getKey ()),
                                             conn);

    }

}
