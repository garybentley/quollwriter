package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import javafx.scene.image.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

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

                // Need to do this before assets.
                this.objectManager.getObjects (UserConfigurableObjectType.class,
                                               p,
                                               conn,
                                               loadChildObjects);

                 // Need to do this for existing projects.
                 this.initUserConfigurableObjectTypes (conn,
                                                       p);

                // Get the tags.
                p.setTags ((List<Tag>) this.objectManager.getObjects (Tag.class,
                                                                      p,
                                                                      conn,
                                                                      loadChildObjects));

                // Get the environment tags.
                Set<Tag> envTags = Environment.getAllTags ();

                if ((envTags.size () > 0)
                    &&
                    (p.getAllTags ().size () == 0)
                   )
                {

                    // Env -> Proj
                    Map<Long, Long> keyMapping = new HashMap<> ();

                    // Add the tags into this project.
                    for (Tag et : envTags)
                    {


                        Tag pt = new Tag ();
                        pt.setName (et.getName ());

                        this.objectManager.saveObject (pt,
                                                       conn);

                        pt.setProject (p);
                        p.addNewTag (pt);

                        keyMapping.put (et.getKey (),
                                        pt.getKey ());

                    }

                    p.setProperty ("tagKeyMapping",
                                   JSONEncoder.encode (keyMapping));

                }

/*
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

                if (p.hasUserConfigurableObjectType (QCharacter.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (QCharacter.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                if (p.hasUserConfigurableObjectType (Location.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (Location.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                if (p.hasUserConfigurableObjectType (QObject.OBJECT_TYPE))
                {

                    this.objectManager.getObjects (QObject.class,
                                                   p,
                                                   conn,
                                                   loadChildObjects);

                }

                if (p.hasUserConfigurableObjectType (ResearchItem.OBJECT_TYPE))
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

        try
        {

            this.initUserConfigurableObjectTypes (conn,
                                                  p);

        } catch (Exception e) {

            throw new GeneralException ("Unable to init user configurable object types for project: " + p,
                                        e);

        }

        // Save all the user config object types
        Set<UserConfigurableObjectType> types = p.getUserConfigurableObjectTypes ();

        for (UserConfigurableObjectType t : types)
        {

            this.objectManager.saveObject (t,
                                           conn);

        }

        // Get all the assets.
        Set<UserConfigurableObjectType> asTypes = p.getAssetTypes ();

        for (UserConfigurableObjectType t : asTypes)
        {

            Set<Asset> assets = p.getAssets (t);

            if (assets != null)
            {

                for (Asset a : assets)
                {

                    this.objectManager.saveObject (a,
                                                   conn);

                }

            }

        }

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

    // A method for moving the user object type definitions into the local project, introduced in v3.
    /* YYY
    */
    private void initUserConfigurableObjectTypes (Connection conn,
                                                  Project    project)
                                           throws Exception
    {

        if (project.isEditorProject ())
        {

            // Don't set up for editor projects
            return;

        }

        this.initUserConfigurableObjectTypesFromEnvironment (conn,
                                                             project);

        if (project.getUserConfigurableObjectType (Chapter.OBJECT_TYPE) == null)
        {

            // Init the legacy object types.
            this.initLegacyObjectTypes (conn,
                                        project);

            this.objectManager.saveObject (project,
                                           conn);
/*
            // Can we init from the environment types?
            if (Environment.getUserConfigurableObjectType (Chapter.OBJECT_TYPE) != null)
            {

                this.initUserConfigurableObjectTypesFromEnvironment (conn,
                                                                     project);

            } else {

            }
*/
        }

    }

    private void initUserConfigurableObjectTypesFromEnvironment (Connection conn,
                                                                 Project    project)
                                                          throws Exception
    {

        Map<Long, Long> mappings = new HashMap<> ();

        try
        {

            mappings = (Map<Long, Long>) JSONDecoder.decode (project.getProperty ("userConfigObjTypesEnvToProjKeyMap"));
System.out.println ("GOT MAPPINGS: " + mappings);
        } catch (Exception e) {

            // Ignore

        }

        if (mappings == null)
        {

            mappings = new HashMap<> ();

        }

        Map<Long, Long> tkmappings = mappings;

        // This means we don't have the user config types set up.
        Map<UserConfigurableObjectType, UserConfigurableObjectType> tmappings = new HashMap<> ();

        // Old (Env) -> New (Proj)
        Map<UserConfigurableObjectTypeField, UserConfigurableObjectTypeField> fmappings = new HashMap<> ();

        // Get current types/fields.
        Set<UserConfigurableObjectType> objTypes = Environment.getUserConfigurableObjectTypes ();

        // Create a mapping between type/field.
        for (UserConfigurableObjectType t : objTypes)
        {

            if (tkmappings.containsKey (t.getKey () + ""))
            {

                System.out.println ("ALREADY GOT: " + t.getKey () + ", " + t.getName ());
                continue;

            }
System.out.println ("ADDING: " + t.getKey () + ", " + t.getName ());
            UserConfigurableObjectType nt = new UserConfigurableObjectType (project);
            nt.setName (t.getName ());
            nt.setDescription (t.getDescription ());
            nt.setObjectTypeName (t.getObjectTypeName ());
            nt.setObjectTypeNamePlural (t.getObjectTypeNamePlural ());
            nt.setIcon16x16 (t.getIcon16x16 ());
            nt.setIcon24x24 (t.getIcon24x24 ());
            nt.setLayout (t.getLayout ());
            nt.setUserObjectType (t.getUserObjectType ());
            nt.setAssetObjectType (t.isAssetObjectType ());
            nt.setCreateShortcutKeyStroke (t.getCreateShortcutKeyStroke ());
            nt.setIgnoreFieldsState (true);

            // Create the new type.
            this.objectManager.saveObject (nt,
                                           conn);

            project.addUserConfigurableObjectType (nt);

            tmappings.put (t,
                           nt);

            UserConfigurableObjectTypeField oldnamefield = t.getPrimaryNameField ();
            if (oldnamefield != null)
            {

                // Add the primary name field.
                ObjectNameUserConfigurableObjectTypeField newnamefield = (ObjectNameUserConfigurableObjectTypeField) UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.objectname);

                newnamefield.setFormName (oldnamefield.getFormName ());
                newnamefield.setUserConfigurableObjectType (nt);

                Map<String, Object> ndefs = new HashMap<> ();
                ndefs.putAll (oldnamefield.getDefinition ());
                newnamefield.setDefinition (ndefs);

                this.objectManager.saveObject (newnamefield,
                                               conn);

                nt.setPrimaryNameField (newnamefield);

                fmappings.put (oldnamefield,
                               newnamefield);

            }

            // Get the fields and create them.
            for (UserConfigurableObjectType.FieldsColumn fc : t.getSortableFieldsColumns ())
            {

                List<UserConfigurableObjectTypeField> nfcfields = new ArrayList<> ();

                // Clone/create fields.
                for (UserConfigurableObjectTypeField fft : fc.fields ())
                {

                    // Get the field.
                    UserConfigurableObjectTypeField nft = fmappings.get (fft);

                    if (nft == null)
                    {

                        // Create the field.
                        nft = UserConfigurableObjectTypeField.Type.getNewFieldForType (fft.getType ());

                        nft.setFormName (fft.getFormName ());
                        nft.setUserConfigurableObjectType (nt);

                        Map<String, Object> defs = new HashMap<> ();
                        defs.putAll (fft.getDefinition ());
                        nft.setDefinition (defs);
                        nft.setDefaultValue (fft.getDefaultValue ());

                        nft.setOrder (fft.getOrder ());

                        this.objectManager.saveObject (nft,
                                                       conn);

                        fmappings.put (fft,
                                       nft);

                    }

                    nfcfields.add (nft);

                }

                UserConfigurableObjectType.FieldsColumn nfc = nt.addNewColumn (nfcfields);
                nfc.setTitle (fc.getTitle ());
                nfc.setShowFieldLabels (fc.isShowFieldLabels ());

            }

            nt.setIgnoreFieldsState (false);
            nt.updateSortableFieldsState ();

            // Save the type again.
            this.objectManager.saveObject (nt,
                                           conn);

            Set<Asset> assets = project.getAssets (t);

            if (assets != null)
            {

                for (Asset a : assets)
                {

                    // Set the type.
                    UserConfigurableObjectType at = a.getUserConfigurableObjectType ();

                    UserConfigurableObjectType nat = tmappings.get (at);

                    for (UserConfigurableObjectField f : a.getFields ())
                    {

                        UserConfigurableObjectTypeField tf = f.getUserConfigurableObjectTypeField ();

                        // Get our new field.
                        UserConfigurableObjectTypeField nf = fmappings.get (tf);

                        // Update the field.
                        if (nf != null)
                        {

                            f.setUserConfigurableObjectTypeField (nf);

                        }

                    }

                    // Update the type
                    a.setUserConfigurableObjectType (nat);

                    // Save the asset.
                    this.objectManager.saveObject (a,
                                                   conn);

                }

            }

        }

        // Map the type keys
        //Map<Long, Long> tkmappings = new HashMap<> ();

        // k -> env key
        // v -> proj key
        tmappings.forEach ((k, v) ->
        {

            tkmappings.put (k.getKey (),
                            v.getKey ());

        });

        project.setProperty ("userConfigObjTypesEnvToProjKeyMap",
                             JSONEncoder.encode (tkmappings));

        // Map the field keys
        Map<Long, Long> fkmappings = new HashMap<> ();

        // k -> env key
        // v -> proj key
        fmappings.forEach ((k, v) ->
        {

            fkmappings.put (k.getKey (),
                            v.getKey ());

        });

        project.setProperty ("userConfigObjTypeFieldsEnvToProjKeyMap",
                             JSONEncoder.encode (fkmappings));

        this.objectManager.saveObject (project,
                                       conn);

    }

    /**
    * Create the user configurable object types we need, namely for:
    *   - Chapter
    *   - QCharacter
    *   - QObject
    *   - ResearchItem
    *   - Location
    *
    * It will create each object and the minimum required fields.
    */
    private void initLegacyObjectTypes (Connection conn,
                                        Project    project)
                                 throws Exception
    {

       // If we have no user config object types then create the ones we need.
       // We should always have at least the chapter type!

       // Check to make sure that the legacy types have their icons setup.
       UserConfigurableObjectType assT = project.getUserConfigurableObjectType (QCharacter.OBJECT_TYPE);

       if (assT != null)
       {

           if (assT.icon16x16Property ().getValue () == null)
           {

               assT.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_CHARACTER_SMALL_ICON_IMAGE_NAME)));

           }

           if (assT.icon24x24Property ().getValue () == null)
           {

               assT.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_CHARACTER_LARGE_ICON_IMAGE_NAME)));

           }

       }

       assT = project.getUserConfigurableObjectType (Location.OBJECT_TYPE);

       if (assT != null)
       {

           if (assT.icon16x16Property ().getValue () == null)
           {

               assT.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_LOCATION_SMALL_ICON_IMAGE_NAME)));

           }

           if (assT.icon24x24Property ().getValue () == null)
           {

               assT.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_LOCATION_LARGE_ICON_IMAGE_NAME)));

           }

       }

       assT = project.getUserConfigurableObjectType (QObject.OBJECT_TYPE);

       if (assT != null)
       {

           if (assT.icon16x16Property ().getValue () == null)
           {

               assT.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_OBJECT_SMALL_ICON_IMAGE_NAME)));

           }

           if (assT.icon24x24Property ().getValue () == null)
           {

               assT.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_OBJECT_LARGE_ICON_IMAGE_NAME)));

           }

       }

       assT = project.getUserConfigurableObjectType (ResearchItem.OBJECT_TYPE);

       if (assT != null)
       {

           if (assT.icon16x16Property ().getValue () == null)
           {

               assT.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_RESEARCHITEM_SMALL_ICON_IMAGE_NAME)));

           }

           if (assT.icon24x24Property ().getValue () == null)
           {

               assT.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_RESEARCHITEM_LARGE_ICON_IMAGE_NAME)));

           }

       }

       Set<UserConfigurableObjectTypeField> fields = new LinkedHashSet<> ();

       // Create the chapter type.
       UserConfigurableObjectType chapterType = new UserConfigurableObjectType (project);

       //chapterType.setObjectTypeName (Environment.getObjectTypeName (Chapter.OBJECT_TYPE));
       //chapterType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));
       chapterType.setLayout (null);
       // TODO DO! chapterType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift H"));
       /*
       TODO
       chapterType.setIcon24x24 (Environment.getObjectIcon (Chapter.OBJECT_TYPE,
                                                            Constants.ICON_TITLE));
       chapterType.setIcon16x16 (Environment.getObjectIcon (Chapter.OBJECT_TYPE,
                                                            Constants.ICON_SIDEBAR));
       */
       chapterType.setUserObjectType (Chapter.OBJECT_TYPE);

       this.objectManager.saveObject (chapterType,
                                      conn);

       // Add the fields.
       // The chapter doesn't have a name field.

       // Description
       ObjectDescriptionUserConfigurableObjectTypeField descF = new ObjectDescriptionUserConfigurableObjectTypeField ();

       descF.setUserConfigurableObjectType (chapterType);

       descF.setSearchable (true);
       /*
       descF.setFormName (Environment.getUIString (LanguageStrings.chapters,
                                                   LanguageStrings.fields,
                                                   LanguageStrings.description));
       */
                       //LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
       descF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);

       this.objectManager.saveObject (descF,
                                      conn);

       // Plan
       MultiTextUserConfigurableObjectTypeField planF = new MultiTextUserConfigurableObjectTypeField ();

       planF.setUserConfigurableObjectType (chapterType);
       planF.setSearchable (true);
       planF.setDisplayAsBullets (true);
       /*
       planF.setFormName (Environment.getUIString (LanguageStrings.chapters,
                                                   LanguageStrings.fields,
                                                   LanguageStrings.plan));
       */
                       //Chapter.PLAN_LEGACY_FIELD_FORM_NAME);
       planF.setLegacyFieldId (Chapter.PLAN_LEGACY_FIELD_ID);

       this.objectManager.saveObject (planF,
                                      conn);

       MultiTextUserConfigurableObjectTypeField goalsF = new MultiTextUserConfigurableObjectTypeField ();

       goalsF.setUserConfigurableObjectType (chapterType);
       goalsF.setSearchable (true);
       goalsF.setDisplayAsBullets (true);
       /*
       goalsF.setFormName (Environment.getUIString (LanguageStrings.chapters,
                                                    LanguageStrings.fields,
                                                    LanguageStrings.goals));
       */
                           //Chapter.GOALS_LEGACY_FIELD_FORM_NAME);
       goalsF.setLegacyFieldId (Chapter.GOALS_LEGACY_FIELD_ID);

       this.objectManager.saveObject (goalsF,
                                      conn);

       chapterType.setConfigurableFields (Arrays.asList (descF, planF, goalsF));

       project.addUserConfigurableObjectType (chapterType);

       this.objectManager.saveObject (chapterType,
                                      conn);

       // Now characters.
       UserConfigurableObjectType characterType = new UserConfigurableObjectType (project);

       //characterType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (QCharacter.OBJECT_TYPE));
       characterType.setLayout (null);
       characterType.setAssetObjectType (true);
       characterType.setUserObjectType (QCharacter.OBJECT_TYPE);

       characterType.setObjectTypeName (getUIString (objectnames,singular,QCharacter.OBJECT_TYPE));
       characterType.setObjectTypeNamePlural (getUIString (objectnames,plural,QCharacter.OBJECT_TYPE));

       // TODO DO! characterType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift C"));
       characterType.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_CHARACTER_SMALL_ICON_IMAGE_NAME)));
       characterType.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_CHARACTER_LARGE_ICON_IMAGE_NAME)));

       /*
       TODO
       characterType.setIcon24x24 (Environment.getObjectIcon (QCharacter.OBJECT_TYPE,
                                                              Constants.ICON_TITLE));
       characterType.setIcon16x16 (Environment.getObjectIcon (QCharacter.OBJECT_TYPE,
                                                              Constants.ICON_SIDEBAR));
       */
       characterType.setUserObjectType (QCharacter.OBJECT_TYPE);

       this.objectManager.saveObject (characterType,
                                      conn);

       // Name
       ObjectNameUserConfigurableObjectTypeField nameF = new ObjectNameUserConfigurableObjectTypeField ();

       nameF.setUserConfigurableObjectType (characterType);

       /*
       nameF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                   LanguageStrings.legacyfields,
                                                   LanguageStrings.name));
       */
       //LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
       nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);

       this.objectManager.saveObject (nameF,
                                      conn);

       // Aliases
       UserConfigurableObjectTypeField aliasesF = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.multitext);

       aliasesF.setUserConfigurableObjectType (characterType);
       aliasesF.setNameField (true);
       aliasesF.setSearchable (true);
       /*
       aliasesF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                      LanguageStrings.legacyfields,
                                                      LanguageStrings.aliases));
       */
       //LegacyUserConfigurableObject.ALIASES_LEGACY_FIELD_FORM_NAME);
       aliasesF.setLegacyFieldId (LegacyUserConfigurableObject.ALIASES_LEGACY_FIELD_ID);

       this.objectManager.saveObject (aliasesF,
                                      conn);

       // Description
       ObjectDescriptionUserConfigurableObjectTypeField cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

       cdescF.setUserConfigurableObjectType (characterType);
       cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
       cdescF.setSearchable (true);
       /*
       cdescF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                    LanguageStrings.legacyfields,
                                                    LanguageStrings.description));
       */
       //LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);

       this.objectManager.saveObject (cdescF,
                                      conn);

       characterType.setConfigurableFields (Arrays.asList (nameF, aliasesF, cdescF));

       project.addUserConfigurableObjectType (characterType);

       this.objectManager.saveObject (characterType,
                                      conn);

       // Now locations.
       UserConfigurableObjectType locType = new UserConfigurableObjectType (project);

       //locType.setObjectTypeName (Environment.getObjectTypeName (Location.OBJECT_TYPE));
       //locType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (Location.OBJECT_TYPE));
       locType.setLayout (null);
       locType.setAssetObjectType (true);
       // TODO DO! locType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift L"));
       locType.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_LOCATION_SMALL_ICON_IMAGE_NAME)));
       locType.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_LOCATION_LARGE_ICON_IMAGE_NAME)));
       locType.setUserObjectType (Location.OBJECT_TYPE);
       locType.setObjectTypeName (getUIString (objectnames,singular,Location.OBJECT_TYPE));
       locType.setObjectTypeNamePlural (getUIString (objectnames,plural,Location.OBJECT_TYPE));

       this.objectManager.saveObject (locType,
                                      conn);

       // Name
       nameF = new ObjectNameUserConfigurableObjectTypeField ();

       nameF.setUserConfigurableObjectType (locType);

       /*
       nameF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                   LanguageStrings.legacyfields,
                                                   LanguageStrings.name));
       */
       //LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
       nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);

       this.objectManager.saveObject (nameF,
                                      conn);

       // Description
       cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

       cdescF.setUserConfigurableObjectType (locType);
       cdescF.setSearchable (true);
       /*
       cdescF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                    LanguageStrings.legacyfields,
                                                    LanguageStrings.description));
       */
       //LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
       cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);

       this.objectManager.saveObject (cdescF,
                                      conn);

       locType.setConfigurableFields (Arrays.asList (nameF, cdescF));

       project.addUserConfigurableObjectType (locType);

       this.objectManager.saveObject (locType,
                                      conn);

       // Now qobjects.
       UserConfigurableObjectType qobjType = new UserConfigurableObjectType (project);

       qobjType.setLayout (null);
       qobjType.setAssetObjectType (true);
       // TODO DO! qobjType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift I"));
       qobjType.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_OBJECT_SMALL_ICON_IMAGE_NAME)));
       qobjType.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_OBJECT_LARGE_ICON_IMAGE_NAME)));
       qobjType.setUserObjectType (QObject.OBJECT_TYPE);
       qobjType.setObjectTypeName (getUIString (objectnames,singular,QObject.OBJECT_TYPE));
       qobjType.setObjectTypeNamePlural (getUIString (objectnames,plural,QObject.OBJECT_TYPE));

       this.objectManager.saveObject (qobjType,
                                      conn);

       // Name
       nameF = new ObjectNameUserConfigurableObjectTypeField ();
       nameF.setUserConfigurableObjectType (qobjType);
       /*
       nameF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                   LanguageStrings.legacyfields,
                                                   LanguageStrings.name));
       */
       //LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
       nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);

       this.objectManager.saveObject (nameF,
                                      conn);

       // Type
       SelectUserConfigurableObjectTypeField typeF = new SelectUserConfigurableObjectTypeField ();

       typeF.setUserConfigurableObjectType (qobjType);
       typeF.setLegacyFieldId (QObject.TYPE_LEGACY_FIELD_ID);
       /*
       typeF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                   LanguageStrings.legacyfields,
                                                   LanguageStrings.type));
       */
       //QObject.TYPE_LEGACY_FIELD_FORM_NAME);

       // Get the pre-defined types, they are stored in the user prefs.
       String nt = UserProperties.get (Constants.OBJECT_TYPES_PROPERTY_NAME);

       List<String> ts = new ArrayList ();

       if (nt != null)
       {

           StringTokenizer t = new StringTokenizer (nt,
                                                    "|");

           while (t.hasMoreTokens ())
           {

               String tok = t.nextToken ().trim ();

               if (!ts.contains (tok))
               {

                   ts.add (tok);

               }

           }

       }

       Collections.sort (ts);

       typeF.setItems (ts);

       this.objectManager.saveObject (typeF,
                                      conn);

       // Description
       cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

       cdescF.setUserConfigurableObjectType (qobjType);
       cdescF.setSearchable (true);
       /*
       cdescF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                    LanguageStrings.legacyfields,
                                                    LanguageStrings.description));
       */
       //LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
       cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);

       this.objectManager.saveObject (cdescF,
                                      conn);

       qobjType.setConfigurableFields (Arrays.asList (nameF, typeF, cdescF));

       project.addUserConfigurableObjectType (qobjType);

       this.objectManager.saveObject (qobjType,
                                      conn);

       // Research items
       UserConfigurableObjectType riType = new UserConfigurableObjectType (project);

       //riType.setObjectTypeName (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE));
       //riType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (ResearchItem.OBJECT_TYPE));
       riType.setLayout (null);
       riType.setAssetObjectType (true);
       // TODO DO! riType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift R"));
       riType.setIcon16x16 (new Image (Utils.getResourceStream (Constants.LEGACY_RESEARCHITEM_SMALL_ICON_IMAGE_NAME)));
       riType.setIcon24x24 (new Image (Utils.getResourceStream (Constants.LEGACY_RESEARCHITEM_LARGE_ICON_IMAGE_NAME)));
       riType.setUserObjectType (ResearchItem.OBJECT_TYPE);
       riType.setObjectTypeName (getUIString (objectnames,singular,ResearchItem.OBJECT_TYPE));
       riType.setObjectTypeNamePlural (getUIString (objectnames,plural,ResearchItem.OBJECT_TYPE));

       this.objectManager.saveObject (riType,
                                      conn);

       // Name
       nameF = new ObjectNameUserConfigurableObjectTypeField ();

       nameF.setUserConfigurableObjectType (riType);
       /*
       nameF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                   LanguageStrings.legacyfields,
                                                   LanguageStrings.name));
       */
       //LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
       nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);

       this.objectManager.saveObject (nameF,
                                      conn);

       // Web link
       WebpageUserConfigurableObjectTypeField webF = new WebpageUserConfigurableObjectTypeField ();

       webF.setUserConfigurableObjectType (riType);
       webF.setLegacyFieldId (ResearchItem.WEB_PAGE_LEGACY_FIELD_ID);
       /*
       webF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                  LanguageStrings.legacyfields,
                                                  LanguageStrings.webpage));
       */
                         //ResearchItem.WEB_PAGE_LEGACY_FIELD_FORM_NAME);

       this.objectManager.saveObject (webF,
                                      conn);

       // Description
       cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

       cdescF.setUserConfigurableObjectType (riType);
       cdescF.setSearchable (true);
       /*
       cdescF.setFormName (Environment.getUIString (LanguageStrings.assets,
                                                    LanguageStrings.legacyfields,
                                                    LanguageStrings.description));
       */
       //LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
       cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);

       this.objectManager.saveObject (cdescF,
                                      conn);

       riType.setConfigurableFields (Arrays.asList (nameF, webF, cdescF));

       project.addUserConfigurableObjectType (riType);

       this.objectManager.saveObject (riType,
                                      conn);

   }

}
