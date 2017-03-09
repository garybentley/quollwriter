package com.quollwriter.db;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.imageio.*;
import java.awt.image.*;
import javax.swing.*;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.bc.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.openpgp.operator.bc.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;

public class ProjectInfoObjectManager extends ObjectManager
{

    public static final String USER_PROPERTIES_OBJTYPE = "user-properties";

    // Key is the object type string, maps to a user config object type.
    private Map<String, UserConfigurableObjectType> userConfigObjTypes = new HashMap ();
    
    public ProjectInfoObjectManager ()
    {
        
        this.handlers.put (ProjectInfo.class,
                           new ProjectInfoDataHandler (this));
        
    }

    public void init (File   dir,
                      String username,
                      String password,
                      String filePassword,
                      int    newSchemaVersion)
               throws GeneralException
    {

        super.init (dir,
                    username,
                    password,
                    filePassword,
                    newSchemaVersion);
                    
        // Load the object types.  Each type will register itself with the Environment.
        this.getObjects (UserConfigurableObjectType.class,
                         null,
                         null,
                         true);
                            
    }

    public void updateLinks (NamedObject d,
                             Set<Link>   newLinks)
    {
        
    }

    public void deleteLinks (NamedObject n,
                             Connection  conn)
    {
        
    }
    
    public void getLinks (NamedObject d,
                          Project     p,
                          Connection  conn)
    {
        
    }
    
    public void updateSchemaVersion (int        newVersion,
                                     Connection conn)
                              throws Exception
    {

        boolean release = false;
    
        if (conn == null)
        {
            
            conn = this.getConnection ();
            release = true;
            
        }
    
        try
        {
    
            List params = new ArrayList ();
            params.add (newVersion);
    
            this.executeStatement ("UPDATE info SET schema_version = ?",
                                   params,
                                   conn);

        } catch (Exception e) {
            
            this.throwException (conn,
                                 "Unable to update schema version to: " +
                                 newVersion,
                                 e);
            
        } finally {
            
            if (release)
            {
                
                this.releaseConnection (conn);
                
            }            
            
        }
        
    }
    
    /**
     * Get the current/latest version of the schema that is available.  This is in contrast
     * to getSchemaVersion which should return the current version of the actual schema being
     * used.
     *
     * @returns The version.
     */
    public int getLatestSchemaVersion ()
    {
        
        return Environment.getProjectInfoSchemaVersion ();
        
    }
    
    /**
     * Get the current version of the project info schema.  This is the actual version of
     * the schema for the db.
     *
     * @return The version.
     */
    public int getSchemaVersion ()
                          throws GeneralException    
    {
        
        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT schema_version FROM info");

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                return rs.getInt (1);

            }
            
        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get schema version",
                                 e);
                
        } finally
        {

            this.releaseConnection (c);

        }
            
        return -1;
        
        
    }
            
    public String getSchemaFile (String file)
    {
        
        return Constants.PROJECT_INFO_SCHEMA_DIR + file;
        
    }
    
    public String getCreateViewsFile ()
    {
        
        return Constants.PROJECT_INFO_UPDATE_SCRIPTS_DIR + "/create-views.xml";
        
    }
    
    public String getUpgradeScriptFile (int oldVersion,
                                        int newVersion)
    {
        
        return Constants.PROJECT_INFO_UPDATE_SCRIPTS_DIR + "/" + oldVersion + "-" + newVersion + ".xml";
        
    }
 
    public void addSession (Session s)
                     throws GeneralException
    {
    
        Connection conn = null;
    
        try
        {

            conn = this.getConnection ();

        } catch (Exception e) {
            
            this.throwException (null,
                                 "Unable to get connection",
                                 e);
            
        }

        try
        {
        
            List params = new ArrayList ();
            params.add (s.getStart ());
            params.add (s.getEnd ());
            params.add (s.getWordCount ());
    
            this.executeStatement ("INSERT INTO session (start, end, wordcount) VALUES (?, ?, ?)",
                                   params,
                                   conn);

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to save session: " +
                                 s,
                                 e);

        } finally {
            
            this.releaseConnection (conn);
            
        }
        
    }
 
    public List<Session> getSessions (int     daysPast)
                               throws GeneralException
    {

        Connection conn = null;
    
        try
        {

            conn = this.getConnection ();

        } catch (Exception e) {
            
            this.throwException (null,
                                 "Unable to get connection",
                                 e);
            
        }
                        
        try
        {
            
            List params = new ArrayList ();

            String whereDays = "";

            // 0 means today.
            // -1 means all time
            // 1 means yesterday
            if (daysPast > -1)
            {

                whereDays = " AND start >= DATEADD ('DAY', ?, CURRENT_DATE) ";
                params.add (-1 * daysPast);

            }

            ResultSet rs = this.executeQuery (String.format ("SELECT start, end, wordcount FROM session WHERE 1 = 1 %s ORDER BY start",
                                                             whereDays),
                                              params,
                                              conn);

            List<Session> ret = new ArrayList ();

            Session last = null;
            
            while (rs.next ())
            {

                int ind = 1;

                Session s = new Session (rs.getTimestamp (ind++), // start
                                         rs.getTimestamp (ind++), // end
                                         rs.getInt (ind++)); // word count

                if (last != null)
                {
                    
                    // If the time difference less than 2s?
                    if ((s.getStart ().getTime () - last.getEnd ().getTime ()) < 2 * Constants.SEC_IN_MILLIS)
                    {
                        
                        Session _s = new Session (last.getStart (),
                                                  s.getEnd (),
                                                  s.getWordCount () + last.getWordCount ());
                        
                        // Merge the two together.
                        ret.remove (last);

                        s = _s;
                                                                 
                    }
                    
                }
                
                last = s;
                                         
                ret.add (s);

            }

            return ret;

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to load sessions",
                                 e);

        } finally {
            
            this.releaseConnection (conn);
            
        }
        
        return null;

    }
 
    public com.gentlyweb.properties.Properties getUserProperties ()
                                  throws GeneralException
    {
        
        Connection conn = null;
    
        try
        {

            conn = this.getConnection ();

        } catch (Exception e) {
            
            this.throwException (null,
                                 "Unable to get connection",
                                 e);
            
        }
                        
        try
        {
            
            ResultSet rs = this.executeQuery ("SELECT properties FROM dataobject WHERE objecttype = 'user-properties'",
                                              null,
                                              conn);

            if (rs.next ())
            {

                int ind = 1;
            
                String p = rs.getString (ind++);
            
                com.gentlyweb.properties.Properties props = new com.gentlyweb.properties.Properties (new ByteArrayInputStream (p.getBytes ()),
                                                                                                     null);
            
                props.setId ("user");
            
                return props;
            
            }

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to load user properties",
                                 e);

        } finally {
            
            this.releaseConnection (conn);
            
        }
        
        return null;
        
    }
    
    public void setUserProperties (com.gentlyweb.properties.Properties props)
                            throws GeneralException
    {
        
        Connection conn = null;
    
        try
        {

            conn = this.getConnection ();

        } catch (Exception e) {
            
            this.throwException (null,
                                 "Unable to get connection",
                                 e);
            
        }
                        
        try
        {
            
            List params = new ArrayList ();
            params.add (USER_PROPERTIES_OBJTYPE);
            
            ResultSet rs = this.executeQuery ("SELECT dbkey FROM dataobject WHERE objecttype = ?",
                                              params,
                                              conn);

            params = new ArrayList ();
                                              
            String t = JDOMUtils.getElementAsString (props.getAsJDOMElement ());
            
            if (rs.next ())
            {

                params.add (t);
                params.add (USER_PROPERTIES_OBJTYPE);

                this.executeStatement ("UPDATE dataobject SET properties = ? WHERE objecttype = ?",
                                       params,
                                       conn);                
            
            } else {
                
                params.add (this.getNewKey (conn));
                params.add (USER_PROPERTIES_OBJTYPE);
                params.add (new java.util.Date ());
                params.add (t);

                this.executeStatement ("INSERT INTO dataobject (dbkey, objecttype, datecreated, properties) VALUES (?, ?, ?, ?)",
                                       params,
                                       conn);
                                
            }
        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to save user properties",
                                 e);

        } finally {
            
            this.releaseConnection (conn);
            
        }        
        
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
    public void initLegacyObjectTypes ()
                                throws GeneralException
    {
        
        // If we have no user config object types then create the ones we need.
        // We should always have at least the chapter type!
        UserConfigurableObjectType chapT = Environment.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);
        
        if (chapT != null)
        {
         
            // Already inited.
            return;
            
        }
        
        
        // Create the chapter type.
        UserConfigurableObjectType chapterType = new UserConfigurableObjectType ();
        
        chapterType.setObjectTypeName (Environment.getObjectTypeName (Chapter.OBJECT_TYPE));
        chapterType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));
        chapterType.setLayout (null);
        chapterType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift H"));
        chapterType.setIcon24x24 (Environment.getObjectIcon (Chapter.OBJECT_TYPE,
                                                             Constants.ICON_TITLE));
        chapterType.setIcon16x16 (Environment.getObjectIcon (Chapter.OBJECT_TYPE,
                                                             Constants.ICON_SIDEBAR));
        
        chapterType.setUserObjectType (Chapter.OBJECT_TYPE);
        
        // Add the fields.
        // The chapter doesn't have a name field.
                
        // Description
        ObjectDescriptionUserConfigurableObjectTypeField descF = new ObjectDescriptionUserConfigurableObjectTypeField ();
        
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
        
        Environment.addUserConfigurableObjectType (chapterType);
                
        // Now characters.
        UserConfigurableObjectType characterType = new UserConfigurableObjectType ();
        
        characterType.setObjectTypeName (Environment.getObjectTypeName (QCharacter.OBJECT_TYPE));
        characterType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (QCharacter.OBJECT_TYPE));
        characterType.setLayout (null);
        characterType.setAssetObjectType (true);
        characterType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift C"));
        characterType.setIcon24x24 (Environment.getObjectIcon (QCharacter.OBJECT_TYPE,
                                                               Constants.ICON_TITLE));
        characterType.setIcon16x16 (Environment.getObjectIcon (QCharacter.OBJECT_TYPE,
                                                               Constants.ICON_SIDEBAR));
        characterType.setUserObjectType (QCharacter.OBJECT_TYPE);
        
        // Name
        ObjectNameUserConfigurableObjectTypeField nameF = new ObjectNameUserConfigurableObjectTypeField ();
        
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
        ObjectDescriptionUserConfigurableObjectTypeField cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();
                
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        
        characterType.addConfigurableField (cdescF);
        
        Environment.addUserConfigurableObjectType (characterType);
                        
        // Now locations.
        UserConfigurableObjectType locType = new UserConfigurableObjectType ();
        
        locType.setObjectTypeName (Environment.getObjectTypeName (Location.OBJECT_TYPE));
        locType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (Location.OBJECT_TYPE));
        locType.setLayout (null);
        locType.setAssetObjectType (true);
        locType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift L"));
        locType.setIcon24x24 (Environment.getObjectIcon (Location.OBJECT_TYPE,
                                                         Constants.ICON_TITLE));
        locType.setIcon16x16 (Environment.getObjectIcon (Location.OBJECT_TYPE,
                                                         Constants.ICON_SIDEBAR));        
        locType.setUserObjectType (Location.OBJECT_TYPE);
        
        // Name
        nameF = new ObjectNameUserConfigurableObjectTypeField ();
        
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);        
        
        locType.addConfigurableField (nameF);
                                        
        // Description
        cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();
        
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        locType.addConfigurableField (cdescF);
        
        Environment.addUserConfigurableObjectType (locType);
                
        // Now qobjects.
        UserConfigurableObjectType qobjType = new UserConfigurableObjectType ();
        
        qobjType.setObjectTypeName (Environment.getObjectTypeName (QObject.OBJECT_TYPE));
        qobjType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (QObject.OBJECT_TYPE));
        qobjType.setLayout (null);
        qobjType.setAssetObjectType (true);
        qobjType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift I"));
        qobjType.setIcon24x24 (Environment.getObjectIcon (QObject.OBJECT_TYPE,
                                                          Constants.ICON_TITLE));
        qobjType.setIcon16x16 (Environment.getObjectIcon (QObject.OBJECT_TYPE,
                                                          Constants.ICON_SIDEBAR));        
        qobjType.setUserObjectType (QObject.OBJECT_TYPE);
        
        // Name
        nameF = new ObjectNameUserConfigurableObjectTypeField ();
        
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);        
        
        qobjType.addConfigurableField (nameF);
        
        // Type
        SelectUserConfigurableObjectTypeField typeF = new SelectUserConfigurableObjectTypeField ();
        
        typeF.setLegacyFieldId (QObject.TYPE_LEGACY_FIELD_ID);
        typeF.setFormName (QObject.TYPE_LEGACY_FIELD_FORM_NAME);
        
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
        
        qobjType.addConfigurableField (typeF);
                                        
        // Description
        cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();
        
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        qobjType.addConfigurableField (cdescF);
        
        Environment.addUserConfigurableObjectType (qobjType);
        
        // Research items
        UserConfigurableObjectType riType = new UserConfigurableObjectType ();
        
        riType.setObjectTypeName (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE));
        riType.setObjectTypeNamePlural (Environment.getObjectTypeNamePlural (ResearchItem.OBJECT_TYPE));
        riType.setLayout (null);
        riType.setAssetObjectType (true);
        riType.setCreateShortcutKeyStroke (KeyStroke.getKeyStroke ("ctrl shift R"));
        riType.setIcon24x24 (Environment.getObjectIcon (ResearchItem.OBJECT_TYPE,
                                                        Constants.ICON_TITLE));
        riType.setIcon16x16 (Environment.getObjectIcon (ResearchItem.OBJECT_TYPE,
                                                        Constants.ICON_SIDEBAR));        
        riType.setUserObjectType (ResearchItem.OBJECT_TYPE);
        
        // Name
        nameF = new ObjectNameUserConfigurableObjectTypeField ();
        
        nameF.setFormName (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_FORM_NAME);
        nameF.setLegacyFieldId (LegacyUserConfigurableObject.NAME_LEGACY_FIELD_ID);        
        
        riType.addConfigurableField (nameF);
        
        // Web link
        WebpageUserConfigurableObjectTypeField webF = new WebpageUserConfigurableObjectTypeField ();
        
        webF.setLegacyFieldId (ResearchItem.WEB_PAGE_LEGACY_FIELD_ID);
        webF.setFormName (ResearchItem.WEB_PAGE_LEGACY_FIELD_FORM_NAME);
                
        riType.addConfigurableField (webF);
                                        
        // Description
        cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();
        
        cdescF.setSearchable (true);
        cdescF.setFormName (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_FORM_NAME);
        cdescF.setLegacyFieldId (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID);
        
        riType.addConfigurableField (cdescF);
        
        Environment.addUserConfigurableObjectType (riType);
        
    }
        
}