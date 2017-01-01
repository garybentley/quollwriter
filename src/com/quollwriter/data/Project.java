package com.quollwriter.data;

import java.io.*;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

public class Project extends NamedObject
{

    public class XMLConstants
    {

        public static final String projects = "projects";
        public static final String directory = "directory";
        public static final String name = "name";
        public static final String lastEdited = "lastEdited";
        public static final String encrypted = "encrypted";
        public static final String noCredentials = "noCredentials";
        public static final String type = "type";
        public static final String id = "id";
        public static final String forEditor = "forEditor";
        public static final String editDueDate = "editDueDate";

    }

    public static final String LAST_EDITED = "lastEdited";    
    public static final String PROJECT_DIRECTORY = "projectDirectory";
    public static final String BACKUP_DIRECTORY = "backupDirectory";
    
    public static final String OBJECT_TYPE = "project";
    public static final String WORDCOUNTS_OBJECT_TYPE = "wordcounts";

    // TODO: Change these to be an enum
    public static final String NORMAL_PROJECT_TYPE = "normal";
    public static final String WARMUPS_PROJECT_TYPE = "warmups";
    public static final String EDITOR_PROJECT_TYPE = "editor";

    public static final String BOOK_ADDED = "book_added";

    private List<Book>         books = new ArrayList ();
    private int                lastBookId = 0;
    private File               projectDirectory = null;
    private File               backupDirectory = null;
    private Date               lastEdited = null;
    private boolean            backup = false;
    private boolean            encrypted = false;
    private String             backupVersion = null;
    private List<QCharacter>   characters = new ArrayList ();
    private List<Location>     locations = new ArrayList ();
    private List<QObject>      objects = new ArrayList ();
    private List<ResearchItem> researchItems = new ArrayList ();
    private List<IdeaType>     ideaTypes = new ArrayList ();
    private String             filePassword = null;
    private boolean            noCredentials = false;
    private String             type = Project.NORMAL_PROJECT_TYPE;
    //private String             editorsProjectId = null;
    //private EditorProject      editorProject = null;
    
    // TODO: Need a new object to encapsulate this stuff
    //private EditorEditor       forEditor = null;
    private Set<ProjectEditor> projectEditors = null;
    private ProjectVersion     projVer = null;
    
    // Key is the object type string, maps to a user config object type.
    private Map<String, UserConfigurableObjectType> userConfigObjTypes = new HashMap ();

    public Project (Element pEl)
                    throws  Exception
    {

        this ();
    
        String name = JDOMUtils.getChildElementContent (pEl,
                                                        XMLConstants.name);
        String type = JDOMUtils.getAttributeValue (pEl,
                                                   XMLConstants.type,
                                                   false);

        if (type.equals (""))
        {

            type = null;

        }
        
        String id = JDOMUtils.getAttributeValue (pEl,
                                                 XMLConstants.id,
                                                 false);

        if (!id.equals (""))
        {

            this.setId (id);

        }

        String directory = JDOMUtils.getChildElementContent (pEl,
                                                             XMLConstants.directory);

        boolean encrypted = JDOMUtils.getAttributeValueAsBoolean (pEl,
                                                                  XMLConstants.encrypted,
                                                                  false);

        boolean noCredentials = false;

        if (JDOMUtils.getAttribute (pEl,
                                    XMLConstants.noCredentials,
                                    false) != null)
        {

            noCredentials = JDOMUtils.getAttributeValueAsBoolean (pEl,
                                                                  XMLConstants.noCredentials);

        }

        String d = JDOMUtils.getAttributeValue (pEl,
                                                XMLConstants.lastEdited,
                                                false);
                                                
        File dir = new File (directory);

        this.setName (name);
        
        this.setProjectDirectory (dir);
        this.setEncrypted (encrypted);
        this.setNoCredentials (noCredentials);
        
        if (type != null)
        {

            this.setType (type);

            if (this.isEditorProject ())
            {
                
                this.projVer = new ProjectVersion ();
                                
                String dueDate = JDOMUtils.getAttributeValue (pEl,
                                                              XMLConstants.editDueDate,
                                                              false);
                
                if (!dueDate.equals (""))
                {

                    // TODO: Fix this otherwise I will go to hell...
                    this.projVer.setDueDate (new Date (Long.parseLong (dueDate)));

                }                
                
                String editorEmail = JDOMUtils.getChildElementContent (pEl,
                                                                       XMLConstants.forEditor);
                
                if (editorEmail == null)
                {
                    
                    throw new GeneralException ("Expected to find a child element: " +
                                                XMLConstants.forEditor +
                                                ", indicating who the project is being edited for.");
                    
                }
                
                // Get the editor.
                EditorEditor ed = EditorsEnvironment.getEditorByEmail (editorEmail);
                
                // If we are in debug mode then allow a null editor through.  This is to allow
                // testing of the send/receive cycle without having to resort to handling two
                // different accounts or having per user projects.xml files.                
                if ((!Environment.isDebugModeEnabled ())
                    &&
                    (ed == null)
                   )
                {
                    
                    throw new GeneralException ("Expected to find editor with email: " +
                                                editorEmail);
                    
                } else {
                    
                    ed = new EditorEditor ();
                    ed.setKey (new Long (0));
                    ed.setEmail (editorEmail);
                    
                }
                
                this.setForEditor (editorEmail);
                
                //this.forEditor = ed;
                
            }
            
        }

        if (!d.equals (""))
        {

            try
            {

                this.setLastEdited (new Date (Long.parseLong (d)));

            } catch (Exception e)
            {

                // Ignore it.

            }

        }

    }
   
    public Project()
    {

        super (Project.OBJECT_TYPE);

        this.initLegacyObjectTypes ();
        
    }

    public Project(String name)
    {

        super (Project.OBJECT_TYPE,
               name);

        this.initLegacyObjectTypes ();
               
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
    private void initLegacyObjectTypes ()
    {
        
        // Create the chapter type.
        UserConfigurableObjectType chapterType = new UserConfigurableObjectType ();
        
        chapterType.setObjectTypeName (Environment.getObjectTypeName (Chapter.OBJECT_TYPE));
        chapterType.setObjectTypePluralName (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));
        chapterType.setLayout (null);
        chapterType.setUserObjectType (Chapter.OBJECT_TYPE);
        
        // Add the fields.
        // The chapter doesn't have a name field.
                
        // Description
        MultiTextUserConfigurableObjectTypeField descF = new MultiTextUserConfigurableObjectTypeField ();
        
        descF.setSearchable (true);
        descF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        descF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        chapterType.addConfigurableField (descF);
        
        // Plan
        MultiTextUserConfigurableObjectTypeField planF = new MultiTextUserConfigurableObjectTypeField ();
        
        planF.setSearchable (true);
        planF.setDisplayAsBullets (true);
        planF.setFormName (Chapter.PLAN_LEGACY_FIELD_FORM_NAME);
        planF.setLegacyFieldId (Chapter.PLAN_LEGACY_FIELD_ID);
        
        chapterType.addConfigurableField (planF);

        MultiTextUserConfigurableObjectTypeField goalsF = new MultiTextUserConfigurableObjectTypeField ();
        
        goalsF.setSearchable (true);
        goalsF.setDisplayAsBullets (true);
        goalsF.setFormName (Chapter.GOALS_LEGACY_FIELD_FORM_NAME);
        goalsF.setLegacyFieldId (Chapter.GOALS_LEGACY_FIELD_ID);
        
        chapterType.addConfigurableField (goalsF);
        
        this.addUserConfigurableObjectType (Chapter.OBJECT_TYPE,
                                            chapterType);
                
        // Now characters.
        UserConfigurableObjectType characterType = new UserConfigurableObjectType ();
        
        characterType.setObjectTypeName (Environment.getObjectTypeName (QCharacter.OBJECT_TYPE));
        characterType.setObjectTypePluralName (Environment.getObjectTypeNamePlural (QCharacter.OBJECT_TYPE));
        characterType.setLayout (null);
        characterType.setUserObjectType (QCharacter.OBJECT_TYPE);
        
        // Name
        TextUserConfigurableObjectTypeField nameF = new TextUserConfigurableObjectTypeField ();
        
        nameF.setPrimaryNameField (true);
        nameF.setNameField (true);
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);
        
        characterType.addConfigurableField (nameF);
                        
        // Aliases
        UserConfigurableObjectTypeField aliasesF = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.multitext);
        
        aliasesF.setNameField (true);
        aliasesF.setSearchable (true);
        aliasesF.setFormName (LegacyUserConfigurableObject.ALIASES_LEGACY_FIELD_FORM_NAME);
        aliasesF.setLegacyFieldId (LegacyUserConfigurableObject.ALIASES_LEGACY_FIELD_ID);
        
        characterType.addConfigurableField (aliasesF);
                
        // Description
        UserConfigurableObjectTypeField cdescF = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.multitext);
        
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        
        characterType.addConfigurableField (cdescF);
        
        this.addUserConfigurableObjectType (QCharacter.OBJECT_TYPE,
                                            characterType);
                        
        // Now locations.
        UserConfigurableObjectType locType = new UserConfigurableObjectType ();
        
        locType.setObjectTypeName (Environment.getObjectTypeName (Location.OBJECT_TYPE));
        locType.setObjectTypePluralName (Environment.getObjectTypeNamePlural (Location.OBJECT_TYPE));
        locType.setLayout (null);
        locType.setUserObjectType (Location.OBJECT_TYPE);
        
        // Name
        nameF = new TextUserConfigurableObjectTypeField ();
        
        nameF.setPrimaryNameField (true);
        nameF.setNameField (true);
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);        
        
        locType.addConfigurableField (nameF);
                                        
        // Description
        cdescF = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.multitext);
        
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        locType.addConfigurableField (cdescF);
        
        this.addUserConfigurableObjectType (Location.OBJECT_TYPE,
                                            locType);
                
        // Now qobjects.
        UserConfigurableObjectType qobjType = new UserConfigurableObjectType ();
        
        qobjType.setObjectTypeName (Environment.getObjectTypeName (QObject.OBJECT_TYPE));
        qobjType.setObjectTypePluralName (Environment.getObjectTypeNamePlural (QObject.OBJECT_TYPE));
        qobjType.setLayout (null);
        qobjType.setUserObjectType (QObject.OBJECT_TYPE);
        
        // Name
        nameF = new TextUserConfigurableObjectTypeField ();
        
        nameF.setPrimaryNameField (true);
        nameF.setNameField (true);
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);        
        
        qobjType.addConfigurableField (nameF);
        
        // Type
        SelectUserConfigurableObjectTypeField typeF = new SelectUserConfigurableObjectTypeField ();
        
        typeF.setLegacyFieldId (QObject.TYPE_LEGACY_FIELD_ID);
        typeF.setFormName (QObject.TYPE_LEGACY_FIELD_FORM_NAME);
        
        // Get the pre-defined types, they are stored in the user prefs.
        String nt = Environment.getProperty (Constants.OBJECT_TYPES_PROPERTY_NAME);

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
        
        qobjType.addConfigurableField (typeF);
                                        
        // Description
        cdescF = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.multitext);
        
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        qobjType.addConfigurableField (cdescF);
        
        this.addUserConfigurableObjectType (QObject.OBJECT_TYPE,
                                            qobjType);
        
        // Research items
        UserConfigurableObjectType riType = new UserConfigurableObjectType ();
        
        riType.setObjectTypeName (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE));
        riType.setObjectTypePluralName (Environment.getObjectTypeNamePlural (ResearchItem.OBJECT_TYPE));
        riType.setLayout (null);
        riType.setUserObjectType (ResearchItem.OBJECT_TYPE);
        
        // Name
        nameF = new TextUserConfigurableObjectTypeField ();
        
        nameF.setPrimaryNameField (true);
        nameF.setNameField (true);
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);        
        
        riType.addConfigurableField (nameF);
        
        // Web link
        WebpageUserConfigurableObjectTypeField webF = new WebpageUserConfigurableObjectTypeField ();
        
        webF.setLegacyFieldId (ResearchItem.WEB_PAGE_LEGACY_FIELD_ID);
        webF.setFormName (ResearchItem.WEB_PAGE_LEGACY_FIELD_FORM_NAME);
                
        riType.addConfigurableField (webF);
                                        
        // Description
        cdescF = UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.multitext);
        
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        riType.addConfigurableField (cdescF);
        
        this.addUserConfigurableObjectType (ResearchItem.OBJECT_TYPE,
                                            riType);
        
    }

    public ProjectEditor getProjectEditor (EditorEditor ed)
    {
        
        if (this.projectEditors == null)
        {

            return null;
        
        }
        
        for (ProjectEditor pe : this.projectEditors)
        {
            
            if (pe.getEditor () == ed)
            {
                
                return pe;
                
            }
            
        }
        
        return null;
        
    }
    
    public void addProjectEditor (ProjectEditor pe)
    {
        
        if (this.projectEditors == null)
        {
            
            this.projectEditors = new TreeSet ();
            
        }
        
        this.projectEditors.add (pe);
        
    }
    
    public void removeProjectEditor (ProjectEditor pe)
    {
        
        if (this.projectEditors == null)
        {
            
            return;
            
        }
        
        this.projectEditors.remove (pe);
        
    }
    
    public boolean isProjectEditor (EditorEditor ed)
    {
        
        if (this.projectEditors == null)
        {
            
            return false;
            
        }
        
        for (ProjectEditor pe : this.projectEditors)
        {
            
            if (pe.getEditor () == ed)
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
    
    public Set<ProjectEditor> getProjectEditors ()
    {
        
        if (this.projectEditors == null)
        {
            
            return null;
            
        }
        
        return new LinkedHashSet (this.projectEditors);

    }
    
    public void setProjectEditors (Collection<ProjectEditor> eds)
    {
        
        if (eds == null)
        {
            
            return;
            
        }
        
        this.projectEditors = new TreeSet (eds);
        
    }

    public void setForEditor (String editorEmail) //EditorEditor ed)
    {
        
/*        
        if ((this.forEditor != null)
            &&
            (ed == null)
           )
        {
            
            throw new IllegalArgumentException ("Cannot remove forEditor once it has been set.");
            
        }
        
        if ((this.forEditor != null)
            &&
            (!this.forEditor.getEmail ().equals (ed.getEmail ()))
           )
        {
            
            throw new IllegalArgumentException ("Cannot change the forEditor once it has been set.");
            
        }
                
        this.forEditor = ed;
*/
        try
        {

            this.setProperty (Constants.FOR_EDITOR_EMAIL_PROPERTY_NAME,
                              editorEmail);
                              //this.forEditor.getEmail ());

        } catch (Exception e) {
            
            // Not really the correct type of exception to throw but it shouldn't be
            // checked or handled "properly" either since it should always happen, this
            // is just the weird edge case when something terrible goes wrong.
            throw new IllegalArgumentException ("Unable to set the for editor",
                                                e);
            
        }
        
    }
    
    public String getEditResponseMessage ()
    {
        
        return this.getProperty (Constants.EDITOR_RESPONSE_MESSAGE_PROPERTY_NAME);
        
    }
    
    public void setEditResponseMessage (String m)
    {
                    
        try
        {
            
            this.setProperty (Constants.EDITOR_RESPONSE_MESSAGE_PROPERTY_NAME,
                              m);

        } catch (Exception e) {
            
            Environment.logError ("Unable to set response message: " +
                                  m,
                                  e);
            
        }
        
    }

    public void setProjectVersion (ProjectVersion pv)
    {
        
        this.projVer = pv;
        
    }
    
    public ProjectVersion getProjectVersion ()
    {
        
        return this.projVer;
        
    }
        
    public EditorEditor getForEditor ()
    {
        
        if (this.isEditorProject ())
        {
            
            // Get the editor email.
            String edEmail = this.getProperty (Constants.FOR_EDITOR_EMAIL_PROPERTY_NAME);

            if (edEmail == null)
            {
                
                // This is a strange situation, what to do?
                return null;
                
            }
            
            return EditorsEnvironment.getEditorByEmail (edEmail);

        }
        
        return null;
        
        //return this.forEditor;
        
    }
    
    public boolean isEditorProject ()
    {
        
        return this.type.equals (Project.EDITOR_PROJECT_TYPE);
        
    }
    
    public boolean isWarmupsProject ()
    {
        
        return this.type.equals (Project.WARMUPS_PROJECT_TYPE);
        
    }

    public boolean isNormalProject ()
    {
        
        return this.type.equals (Project.NORMAL_PROJECT_TYPE);
        
    }

    public int getChapterCount ()
    {
        
        int c = 0;
        
        if (this.books == null)
        {
            
            return c;
            
        }
        
        for (Book b : this.books)
        {
            
            c += b.getChapters ().size ();
            
        }
        
        return c;        
        
    }
    
    public ReadabilityIndices getAllProjectReadabilityIndices ()
    {
        
        ReadabilityIndices ri = new ReadabilityIndices ();
        
        if (this.books == null)
        {
            
            return ri;
            
        }
        
        for (Book b : this.books)
        {
            
            for (Chapter c : b.getChapters ())
            {
                
                ri.add (c.getChapterText ());
            }
            
        }

        return ri;
        
    }
    
    public int getEditedWordCount ()
    {
        
        if (this.books == null)
        {
            
            return 0;
            
        }
        
        final StringBuilder buf = new StringBuilder ();
        
        int editComplete = 0; 
        
        for (Book b : this.books)
        {
        
            for (Chapter c : b.getChapters ())
            {
                    
                if (c.getEditPosition () > 0)
                {
                                
                    if (buf.length () > 0)
                    {
                        
                        buf.append (" ");
                        
                    }
                            
                    String t = c.getChapterText ();
                    
                    if (t == null)
                    {
                        
                        continue;
                        
                    }
                    
                    if (c.getEditPosition () <= t.length ())
                    {
                        
                        buf.append (t.substring (0,
                                                 c.getEditPosition ()));
                        
                    }

                }
            
            }
            
        }
                                
        if (buf.length () > 0)
        {
        
            ChapterCounts allc = new ChapterCounts (buf.toString ());

            return allc.wordCount;
            
        }

        return 0;
        
    }
    
    public int getWordCount ()
    {
        
        int c = 0;
        
        if (this.books == null)
        {
            
            return c;
            
        }
        
        for (Book b : this.books)
        {
            
            c += b.getChapterWordCount ();
            
        }
        
        return c;
        
    }
    /*
    public void setEditorProject (EditorProject p)
    {
        
        this.editorProject = p;
        
        if (p != null)
        {
        
            p.setProject (this);
            
        }
        
    }
    
    public EditorProject getEditorProject ()
    {
        
        return this.editorProject;
        
    }
    */
    /*
    public void setEditorsProjectId (String id)
    {
        
        this.editorsProjectId = id;
        
    }
    
    public String getEditorsProjectId ()
    {
        
        return this.editorsProjectId;
        
    }
    */
    
    public String getType ()
    {

        return this.type;

    }

    public void setType (String t)
    {

        this.type = t;

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public NamedObject getObjectById (Class  ofType,
                                      String id)
    {
        
        Set<NamedObject> objs = this.getAllNamedChildObjects (ofType);

        for (NamedObject o : objs)
        {
            
            if (o.getId ().equals (id))
            {
                
                return o;
                
            }
            
        }
        
        return null;
        
    }
    
    public Set<NamedObject> getAllNamedChildObjects (Class ofType)
    {

        Set<NamedObject> ret = this.getAllNamedChildObjects ();

        Iterator<NamedObject> iter = ret.iterator ();

        while (iter.hasNext ())
        {

            if (!ofType.isAssignableFrom (iter.next ().getClass ()))
            {

                iter.remove ();

            }

        }

        return ret;

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        Set<NamedObject> ret = new TreeSet (new NamedObjectSorter ());

        for (Book b : this.books)
        {

            ret.add (b);

            ret.addAll (b.getAllNamedChildObjects ());

        }

        for (QCharacter c : this.characters)
        {

            ret.add (c);

            ret.addAll (c.getAllNamedChildObjects ());

        }

        for (Location l : this.locations)
        {

            ret.add (l);

            ret.addAll (l.getAllNamedChildObjects ());

        }

        for (QObject o : this.objects)
        {

            ret.add (o);

            ret.addAll (o.getAllNamedChildObjects ());

        }

        for (ResearchItem r : this.researchItems)
        {

            ret.add (r);

            ret.addAll (r.getAllNamedChildObjects ());

        }

        for (IdeaType it : this.ideaTypes)
        {

            ret.add (it);

            ret.addAll (it.getAllNamedChildObjects ());

        }

        return ret;

    }

    public boolean hasObject (DataObject d)
    {

        if (d instanceof IdeaType)
        {

            return this.getIdeaType (d.getKey ()) != null;
        
        }
        
        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;
            
            if (c.getBook () == null)
            {
                
                return false;
                
            }
        
            return c.getBook ().getChapterByKey (d.getKey ()) != null;
            
        }
        
        if (d instanceof QCharacter)
        {
            
            return this.getCharacter (d.getKey ()) != null;
        
        }

        if (d instanceof Location)
        {
            
            return this.getLocation (d.getKey ()) != null;
        
        }

        if (d instanceof QObject)
        {
            
            return this.getQObject (d.getKey ()) != null;
        
        }

        if (d instanceof ResearchItem)
        {
            
            return this.getResearchItem (d.getKey ()) != null;
        
        }

        return false;
        
    }
    
    public void removeObject (DataObject d)
    {

        if (d instanceof IdeaType)
        {

            this.getIdeaTypes ().remove (d);

        }

        if (d instanceof QCharacter)
        {

            this.getCharacters ().remove (d);

        }

        if (d instanceof Location)
        {

            this.getLocations ().remove (d);

        }

        if (d instanceof QObject)
        {

            this.getQObjects ().remove (d);

        }

        if (d instanceof ResearchItem)
        {

            this.getResearchItems ().remove (d);

        }

    }

    public boolean hasAsset (Asset a)
    {

        return this.getAssetByName (a.getName (),
                                    a.getObjectType ()) != null;

    }
    
    public Set<Asset> getAllAssetsByName (String n,
                                          String objType)
    {

        Set<Asset> assets = new LinkedHashSet ();
    
        if ((objType == null)
            ||
            (objType.equals (Location.OBJECT_TYPE))
           )
        {

            assets.addAll (this.getAllLocationsByName (n));

        }

        if ((objType == null)
            ||
            (objType.equals (QCharacter.OBJECT_TYPE))
           )
        {

            assets.addAll (this.getAllCharactersByName (n));

        }

        if ((objType == null)
            ||
            (objType.equals (QObject.OBJECT_TYPE))
           )
        {

            assets.addAll (this.getAllQObjectsByName (n));

        }

        if ((objType == null)
            ||
            (objType.equals (ResearchItem.OBJECT_TYPE))
           )
        {

            assets.addAll (this.getAllResearchItemsByName (n));

        }

        return assets;
        
    }
    
    public Asset getAssetByName (String n,
                                 String objType)
    {

        if (objType.equals (Location.OBJECT_TYPE))
        {

            return this.getLocationByName (n);

        }

        if (objType.equals (QCharacter.OBJECT_TYPE))
        {

            return this.getCharacterByName (n);

        }

        if (objType.equals (QObject.OBJECT_TYPE))
        {

            return this.getQObjectByName (n);

        }

        if (objType.equals (ResearchItem.OBJECT_TYPE))
        {

            return this.getResearchItemByName (n);

        }

        return null;

    }

    private Asset getAssetByName_Int (List<? extends Asset> assets,
                                      String                n)
    {

        n = n.toLowerCase ();

        for (Asset a : assets)
        {

            if (a.getName ().toLowerCase ().equals (n))
            {

                return a;

            }

        }

        return null;

    }

    private Set<? extends Asset> getAllAssetsByName_Int (List<? extends Asset> assets,
                                                         String                n)
    {

        Set<Asset> matched = new LinkedHashSet ();
    
        n = n.toLowerCase ();

        for (Asset a : assets)
        {

            if (a.getName ().equalsIgnoreCase (n))
            {

                matched.add (a);

                continue;
                
            }

            List<String> aliases = a.getAliasesAsList ();
            
            if (aliases != null)
            {
                
                for (String al : aliases)
                {
                    
                    if (al.equalsIgnoreCase (n))
                    {
        
                        matched.add (a);
        
                        continue;
        
                    }
                    
                }
                
            }
            
        }

        return matched;

    }

    public Set<QCharacter> getAllCharactersByName (String n)
    {

        return (Set<QCharacter>) this.getAllAssetsByName_Int (this.characters,
                                                              n);

    }
    
    public Set<Location> getAllLocationsByName (String n)
    {

        return (Set<Location>) this.getAllAssetsByName_Int (this.locations,
                                                            n);

    }

    public Set<QObject> getAllQObjectsByName (String n)
    {

        return (Set<QObject>) this.getAllAssetsByName_Int (this.objects,
                                                           n);

    }

    public Set<ResearchItem> getAllResearchItemsByName (String n)
    {

        return (Set<ResearchItem>) this.getAllAssetsByName_Int (this.researchItems,
                                                                n);

    }

    public QCharacter getCharacterByName (String n)
    {

        return (QCharacter) this.getAssetByName_Int (this.characters,
                                                     n);

    }

    public Location getLocationByName (String n)
    {

        return (Location) this.getAssetByName_Int (this.locations,
                                                   n);

    }

    public QObject getQObjectByName (String n)
    {

        return (QObject) this.getAssetByName_Int (this.objects,
                                                  n);

    }

    public ResearchItem getResearchItemByName (String n)
    {

        return (ResearchItem) this.getAssetByName_Int (this.researchItems,
                                                       n);

    }

    public boolean isNoCredentials ()
    {

        return this.noCredentials;

    }

    public void setNoCredentials (boolean v)
    {

        this.noCredentials = v;

    }

    public void setFilePassword (String p)
    {

        this.filePassword = p;

    }

    public String getFilePassword ()
    {

        return this.filePassword;

    }

    public boolean isEncrypted ()
    {

        return this.encrypted;

    }

    public void setEncrypted (boolean v)
    {

        this.encrypted = v;

    }

    public Date getLastEdited ()
    {

        return this.lastEdited;

    }

    public void setLastEdited (Date d)
    {

        Date oldDate = this.lastEdited;
    
        this.lastEdited = d;

        this.firePropertyChangedEvent (Project.LAST_EDITED,
                                       oldDate,
                                       this.lastEdited);
        
    }

    public File getProjectDirectory ()
    {

        return this.projectDirectory;

    }
    
    public void setProjectDirectory (File dir)
    {

        File oldDir = this.projectDirectory;
    
        this.projectDirectory = dir;

        this.firePropertyChangedEvent (Project.PROJECT_DIRECTORY,
                                       oldDir,
                                       this.projectDirectory);
        
    }
    
    public File getBackupDirectory ()
    {

        if (this.backupDirectory == null)
        {
            
            this.backupDirectory = new File (this.projectDirectory,
                                             "versions");
            
        }
    
        return this.backupDirectory;

    }

    public void setBackupDirectory (File dir)
    {

        File oldDir = this.backupDirectory;
    
        this.backupDirectory = dir;

        this.firePropertyChangedEvent (Project.BACKUP_DIRECTORY,
                                       oldDir,
                                       this.backupDirectory);
        
    }

    public List<QCharacter> getCharacters ()
    {

        return this.characters;

    }

    public List<Location> getLocations ()
    {

        return this.locations;

    }

    public List<QObject> getQObjects ()
    {

        return this.objects;

    }

    public List<IdeaType> getIdeaTypes ()
    {
        
        return this.ideaTypes;
        
    }
    
    public void addIdeaType (IdeaType it)
    {
        
        if (this.ideaTypes.contains (it))
        {
            
            return;
            
        }        
        
        this.ideaTypes.add (it);
        
        it.setProject (this);
        
    }

    public List<ResearchItem> getResearchItems ()
    {

        return this.researchItems;

    }

    public void addAsset (Asset a)
    {

        if (a instanceof QCharacter)
        {

            this.addCharacter ((QCharacter) a);

        }

        if (a instanceof Location)
        {

            this.addLocation ((Location) a);

        }

        if (a instanceof ResearchItem)
        {

            this.addResearchItem ((ResearchItem) a);

        }

        if (a instanceof QObject)
        {

            this.addQObject ((QObject) a);

        }

    }

    public void addResearchItem (ResearchItem c)
    {

        if (this.researchItems.contains (c))
        {
            
            return;
            
        }
    
        c.setProject (this);

        if (c.getUserConfigurableObjectType () == null)
        {        
        
            c.setUserConfigurableObjectType (this.getUserConfigurableObjectType (ResearchItem.OBJECT_TYPE));
            
        }
        
        this.researchItems.add (c);

    }

    public void addQObject (QObject c)
    {

        if (this.objects.contains (c))
        {
            
            return;
            
        }

        c.setProject (this);

        if (c.getUserConfigurableObjectType () == null)
        {
        
            c.setUserConfigurableObjectType (this.getUserConfigurableObjectType (QObject.OBJECT_TYPE));
            
        }
        
        this.objects.add (c);

    }

    public void addCharacter (QCharacter c)
    {

        if (this.characters.contains (c))
        {
            
            return;
            
        }
    
        c.setProject (this);

        if (c.getUserConfigurableObjectType () == null)
        {
        
            c.setUserConfigurableObjectType (this.getUserConfigurableObjectType (QCharacter.OBJECT_TYPE));
            
        }

        this.getCharacters ().add (c);

    }

    public void addLocation (Location c)
    {

        if (this.locations.contains (c))
        {
            
            return;
            
        }
    
        c.setProject (this);

        if (c.getUserConfigurableObjectType () == null)
        {

            c.setUserConfigurableObjectType (this.getUserConfigurableObjectType (Location.OBJECT_TYPE));
            
        }
        
        this.locations.add (c);

    }

/*
    private void setResearchItems (List<ResearchItem> a)
    {

        this.researchItems = a;

    }
*/

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);
        
        this.addToStringProperties (props,
                                    "type",
                                    this.type);
        
        this.addToStringProperties (props,
                                    "projectDir",
                                    (this.projectDirectory != null ? this.projectDirectory.getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "backupDir",
                                    (this.backupDirectory != null ? this.backupDirectory.getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "lastEdited",
                                    this.lastEdited);
        this.addToStringProperties (props,
                                    "encrypted",
                                    this.encrypted);
        this.addToStringProperties (props,
                                    "characters",
                                    this.characters.size ());
        this.addToStringProperties (props,
                                    "locations",
                                    this.locations.size ());
        this.addToStringProperties (props,
                                    "objects",
                                    this.objects.size ());
        this.addToStringProperties (props,
                                    "researchItems",
                                    this.researchItems.size ());
        this.addToStringProperties (props,
                                    "ideaTypes",
                                    this.ideaTypes.size ());
        
        EditorEditor ed = this.getForEditor ();
        
        if (ed != null)
        {
            
            this.addToStringProperties (props,
                                        "forEditor",
                                        ed.getEmail ());
            
        }
        
        if (this.projectEditors != null)
        {
            
            this.addToStringProperties (props,
                                        "projectEditors",
                                        this.projectEditors.size ());
            
        }

        this.addToStringProperties (props,
                                    "projectVersion",
                                    this.projVer);
        
    }
    
    public int getBookIndex (Book b)
    {

        return this.books.indexOf (b) + 1;

    }

    public List<Book> getBooks ()
    {

        return this.books;

    }

    public Chapter getChapterByKey (long k)
    {

        for (Book b : this.books)
        {

            Chapter c = b.getChapterByKey (k);

            if (c != null)
            {

                return c;

            }

        }

        return null;

    }

    public Book getBook (int i)
    {

        return (Book) this.books.get (i);

    }

    public QCharacter getCharacter (Long key)
    {

        for (QCharacter c : this.characters)
        {

            if (c.getKey ().equals (key))
            {

                return c;

            }

        }

        return null;

    }

    public ResearchItem getResearchItem (Long key)
    {

        for (ResearchItem r : this.researchItems)
        {

            if (r.getKey ().equals (key))
            {

                return r;

            }

        }

        return null;

    }

    public IdeaType getIdeaType (Long key)
    {
        
        for (IdeaType i : this.ideaTypes)
        {
            
            if (i.getKey () == key)
            {
                
                return i;
                
            }
            
        }
        
        return null;
        
    }
    
    public QObject getQObject (Long key)
    {

        for (QObject o : this.objects)
        {

            if (o.getKey ().equals (key))
            {

                return o;

            }

        }

        return null;

    }

    public Location getLocation (Long key)
    {

        for (Location l : this.locations)
        {

            if (l.getKey ().equals (key))
            {

                return l;

            }

        }

        return null;

    }

    public Book getBook (Long key)
    {

        for (Book b : this.books)
        {

            if (b.getKey ().equals (key))
            {

                return b;

            }

        }

        return null;

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        if (r == null)
        {

            return null;

        }

        DataObject d = super.getObjectForReference (r);

        if (d != null)
        {

            return d;

        }
        /*
        if (r.getObjectType ().equals (Project.WORDCOUNTS_OBJECT_TYPE))
        {

        WordCount w = new WordCount ();
        w.setKey (r.getKey ());
        w.setObjectType (Project.WORDCOUNTS_OBJECT_TYPE);

        return w;

        }
         */

        if (r.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
        {

            r = new ObjectReference (Chapter.OBJECT_TYPE,
                                     r.getKey (),
                                     null);

        }

        for (QCharacter c : this.characters)
        {

            d = c.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (Location l : this.locations)
        {

            d = l.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (QObject q : this.objects)
        {

            d = q.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (ResearchItem i : this.researchItems)
        {

            d = i.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (Book b : this.books)
        {

            d = b.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        for (IdeaType it : this.ideaTypes)
        {

            d = it.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        return null;

    }

    public void addBook (Book b)
    {

        if (this.books.contains (b))
        {
            
            return;
            
        }    
    
        b.setProject (this);

        this.books.add (b);

        this.firePropertyChangedEvent (Project.BOOK_ADDED,
                                       null,
                                       b);

    }

    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + ((null == this.projectDirectory) ? 0 : this.projectDirectory.hashCode ());

        return hash;

    }

    public boolean equals (Object o)
    {

        if (o == null)
        {

            return false;

        }

        if (!(o instanceof Project))
        {

            return false;

        }

        Project po = (Project) o;

        if (this.projectDirectory == null)
        {
            
            return false;
            
        }
        
        return this.projectDirectory.equals (po.projectDirectory);

    }

    public Element getAsJDOMElement ()
    {
        
        Element pEl = new Element (Project.OBJECT_TYPE);

        Element nEl = new Element (Environment.XMLConstants.name);
        pEl.addContent (nEl);
        nEl.addContent (this.getName ());

        if (this.getType () == null)
        {

            this.setType (Project.NORMAL_PROJECT_TYPE);

        }

        pEl.setAttribute (XMLConstants.type,
                          this.getType ());

        if (this.getId () != null)
        {                          
        
            pEl.setAttribute (XMLConstants.id,
                              this.getId ());

        }
                                  
        Element dEl = new Element (XMLConstants.directory);
        pEl.addContent (dEl);
        dEl.addContent (this.getProjectDirectory ().getPath ());

        EditorEditor ed = this.getForEditor ();
        
        if (ed != null)
        {
            
            Element fEl = new Element (XMLConstants.forEditor);
            pEl.addContent (fEl);
            fEl.addContent (ed.getEmail ());            
            
        }
        
        if ((this.isEditorProject ())
            &&
            (this.projVer != null)
           )
        {
            
            Date d = this.projVer.getDueDate ();
            
            if (d != null)
            {
                
                pEl.setAttribute (XMLConstants.editDueDate,
                                  d.getTime () + "");
                
            }
            
        }
        
        Date lastEdited = this.getLastEdited ();

        if (lastEdited != null)
        {

            pEl.setAttribute (XMLConstants.lastEdited,
                              String.valueOf (lastEdited.getTime ()));

        }

        pEl.setAttribute (XMLConstants.encrypted,
                          Boolean.valueOf (this.isEncrypted ()).toString ());

        if (this.isNoCredentials ())
        {

            pEl.setAttribute (XMLConstants.noCredentials,
                              Boolean.valueOf (this.isNoCredentials ()).toString ());

        }
/*
        if (this.editorsProjectId != null)
        {
            
            pEl.setAttribute (XMLConstants.editorsProjectId,
                              this.editorsProjectId);
            
        }
  */      
        return pEl;
        
    }
    
    public void addUserConfigurableObjectType (String                     objType,
                                               UserConfigurableObjectType uc)
    {
        
        this.userConfigObjTypes.put (objType,
                                     uc);
        
    }
    
    public void removeUserConfigurableObjectType (String objType)
    {
        
        this.userConfigObjTypes.remove (objType);
        
    }
    
    public UserConfigurableObjectType getUserConfigurableObjectType (String objType)
    {
        
        return this.userConfigObjTypes.get (objType);
        
    }

    /**
     * Create a new location, this only creates the object and doesn't add it to the list of
     * locations we have.  It also doesn't set the parent.  It WILL setup the correct config object type.
     *
     * @returns The new location.
     * @throws An exception if the location can't be created.
     */
    public Location createLocation ()
                             throws GeneralException
    {
        
        return (Location) this.createAsset (Location.OBJECT_TYPE);
        
    }
    
    /**
     * Create a new object, this only creates the object and doesn't add it to the list of
     * object we have.  It also doesn't set the parent.  It WILL setup the correct config object type.
     *
     * @returns The new object.
     * @throws An exception if the object can't be created.
     */
    public QObject createQObject ()
                           throws GeneralException
    {
        
        return (QObject) this.createAsset (QObject.OBJECT_TYPE);
        
    }

    /**
     * Create a new research item, this only creates the object and doesn't add it to the list of
     * research items we have.  It also doesn't set the parent.  It WILL setup the correct config object type.
     *
     * @returns The new research item.
     * @throws An exception if the object can't be created.
     */
    public ResearchItem createResearchItem ()
                                     throws GeneralException
    {
        
        return (ResearchItem) this.createAsset (ResearchItem.OBJECT_TYPE);
        
    }

    /**
     * Create a new character, this only creates the object and doesn't add it to the list of
     * characters we have.  It also doesn't set the parent.  It WILL setup the correct config object type.
     *
     * @returns The new character.
     * @throws An exception if the object can't be created.
     */
    public QCharacter createQCharacter ()
                                 throws GeneralException
    {
        
        return (QCharacter) this.createAsset (QCharacter.OBJECT_TYPE);
        
    }

    /**
     * Create a new asset, this only creates the object and doesn't add it to the list of
     * assets we have.  It also doesn't set the parent.  It WILL setup the correct config object type.
     *
     * @param objType The object type to create.
     * @returns The new asset.
     * @throws An exception if the asset type isn't supported or can't be created.
     */
    public Asset createAsset (String objType)
                       throws GeneralException
    {
        
        Asset a = Asset.createSubType (objType);
        
        a.setUserConfigurableObjectType (this.getUserConfigurableObjectType (objType));
        
        return a;
        
    }
    
}
