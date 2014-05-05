package com.quollwriter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.image.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.*;
import java.util.jar.*;
import java.util.logging.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

import javax.imageio.*;

import com.gentlyweb.logging.Logger;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.looks.*;
import com.jgoodies.looks.windows.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import com.quollwriter.importer.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.rules.*;

import com.quollwriter.db.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.GradientPanel;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.Runner;

import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;

import org.jdom.*;


public class Environment
{

    public static String GZIP_EXTENSION = ".gz";

    private static Map<Project, AbstractProjectViewer> openProjects = new HashMap ();

    public static Map defaultObjectProperties = new HashMap ();

    public static com.gentlyweb.properties.Properties userProperties = new com.gentlyweb.properties.Properties ();
    public static IconProvider                        iconProvider = new DefaultIconProvider ();

    private static Logger generalLog = null;
    private static Logger errorLog = null;
    private static Logger sqlLog = null;

    // Get rid of this value once bug reporting is via the web.
    public static boolean seenReportBugMessage = false;

    private static String appVersion = null;
    private static int    schemaVersion = 0;

    private static SimpleDateFormat dateFormatter = null;
    private static SimpleDateFormat timeFormatter = null;

    public static boolean  debugMode = false;
    private static boolean doneVersionCheck = false;
    public static boolean  isWindows = false;
    public static boolean  isMac = false;
    public static boolean  isLinux = false;

    private static List<File> installJarFilesToDelete = new ArrayList ();

    private static boolean upgradeRequired = false;
    
    private static DecimalFormat numFormat = new DecimalFormat ("###,###");
    private static DecimalFormat floatNumFormat = new DecimalFormat ("###,###.0");

    private static Map<String, Image> backgroundImages = new HashMap ();

    //public static String quollWriterWebsite = null;

    private static AchievementsManager achievementsManager = null;

    private static Map<String, String> objectTypeNamesSingular = new HashMap ();
    private static Map<String, String> objectTypeNamesPlural = new HashMap ();
        
    private static EditorsWebServiceHandler editorsHandler = null;
    public static boolean editorServiceAvailable = false;
    
    private static Map<String, SynonymProvider> synonymProviders = new WeakHashMap ();
    
    private static Map<String, String> buttonLabels = new HashMap ();
    
    static
    {
        
        Map m = Environment.buttonLabels;
        
        m.put (Constants.CANCEL_BUTTON_LABEL_ID,
               "Cancel");
        m.put (Constants.CONFIRM_BUTTON_LABEL_ID,
               "Ok, got it");
        
    }
    
    public class XMLConstants
    {
/*
        public static final String directory = "directory";
        public static final String lastEdited = "lastEdited";
        public static final String encrypted = "encrypted";
        public static final String noCredentials = "noCredentials";
        public static final String backupService = "backupService";
*/
        public static final String projects = "projects";
        public static final String type = "type";
        public static final String name = "name";
        public static final String object = "object";
        public static final String singular = "singular";
        public static final String plural = "plural";

    }
    
    public static boolean isDistractionFreeModeEnabled ()
    {
        
        for (AbstractProjectViewer pv : Environment.openProjects.values ())
        {
            
            if (pv.isDistractionFreeModeEnabled ())
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
    
    public static String getUrlFileAsString (URL    url)
                                             throws Exception
    {
        
        URLConnection c = url.openConnection ();
        
        BufferedInputStream bin = new BufferedInputStream (c.getInputStream ());
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
        
        IOUtils.streamTo (bin,
                          bout,
                          4096);

        // This "should" be ok.
        return new String (bout.toByteArray ());
                
    }
    
    public static Set<String> getWritingGenres ()
    {
        
        String gt = Environment.getProperty (Constants.WRITING_GENRES_PROPERTY_NAME);
        
        Set<String> gitems = new LinkedHashSet ();
        
        StringTokenizer t = new StringTokenizer (gt,
                                                 ",");
        
        while (t.hasMoreTokens ())
        {
            
            gitems.add (t.nextToken ().trim ());
            
        }
        
        return gitems;
        
    }
    
    public static EditorsWebServiceHandler getEditorsWebServiceHandler ()
    {
        
        return Environment.editorsHandler;
        
    }
    
    public static AbstractProjectViewer getProjectViewer (Project p)
    {
        
        return Environment.openProjects.get (p);
        
    }

    public static Book createTestBook ()
                                       throws Exception
    {
        
        Element root = JDOMUtils.getStringAsElement (Environment.getResourceFileAsString (Constants.TEST_BOOK_FILE));

        String name = JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.name);
        
        Book b = new Book ();
        
        b.setName (name);
        
        List chEls = JDOMUtils.getChildElements (root,
                                                 Chapter.OBJECT_TYPE,
                                                 false);
        
        for (int i = 0; i < chEls.size (); i++)
        {
            
            Element el = (Element) chEls.get (i);
            
            name = JDOMUtils.getAttributeValue (el,
                                                XMLConstants.name);
            
            Chapter ch = new Chapter ();
            ch.setName (name);
            
            String text = JDOMUtils.getChildContent (el);
            
            ch.setText (text);
            
            b.addChapter (ch);
            
        }
        
        return b;
        
    }
    
    public static boolean areDifferent (Comparable o,
                                        Comparable n)
    {

        if ((o == null) &&
            (n == null))
        {

            return false;

        }

        if ((o != null) &&
            (n == null))
        {

            return true;

        }

        if ((o == null) &&
            (n != null))
        {

            return true;

        }

        return o.compareTo (n) != 0;

    }

    public static void setUpgradeRequired ()
    {
        
        Environment.upgradeRequired = true;
        
    }
    
    public static void addInstallJarToDelete (File oldFile,
                                              File newFile)
    {

        Environment.installJarFilesToDelete.add (oldFile);

    }

    public static Date zeroTimeFieldsForDate (Date d)
    {

        GregorianCalendar gc = new GregorianCalendar ();
        gc.setTime (d);

        Environment.zeroTimeFields (gc);

        return gc.getTime ();

    }

    public static void zeroTimeFields (GregorianCalendar gc)
    {

        // Zero-out the non date fields.
        gc.set (Calendar.HOUR_OF_DAY,
                0);
        gc.set (Calendar.MINUTE,
                0);
        gc.set (Calendar.SECOND,
                0);
        gc.set (Calendar.MILLISECOND,
                0);

    }

    public static void setUserObjectTypeNames (Map<String, String> singular,
                                               Map<String, String> plural)
                                               throws Exception
    {
        
        Element root = new Element ("object-names");
        
        Map<String, Element> els = new HashMap ();
        
        for (String ot : singular.keySet ())
        {
            
            ot = ot.toLowerCase ();
            
            String s = singular.get (ot);
            
            Element el = els.get (ot);
                        
            if (el == null)
            {
                
                el = new Element (XMLConstants.object);
                
                el.setAttribute (XMLConstants.type,
                                 ot);
                
                els.put (ot,
                         el);
                
                root.addContent (el);
                
            }

            Element sel = new Element (XMLConstants.singular);
            
            CDATA cd = new CDATA (s);
            
            sel.addContent (cd);
            
            el.addContent (sel);
            
        }

        for (String ot : plural.keySet ())
        {
            
            ot = ot.toLowerCase ();
            
            String p = plural.get (ot);
            
            Element el = els.get (ot);
                        
            if (el == null)
            {
                
                el = new Element (XMLConstants.object);
                
                el.setAttribute (XMLConstants.type,
                                 ot);
                
                els.put (ot,
                         el);
                
                root.addContent (el);
                
            }

            Element pel = new Element (XMLConstants.plural);
            
            CDATA cd = new CDATA (p);
            
            pel.addContent (cd);
            
            el.addContent (pel);
            
        }
        
        JDOMUtils.writeElementToFile (root,
                                      Environment.getUserObjectTypeNamesFile (),
                                      true);
                
        // Now force a reload.
        Environment.loadObjectTypeNames (root);
                
    }
        
    public static Map<String, String> getObjectTypeNamePlurals ()
    {
        
        return new HashMap (Environment.objectTypeNamesPlural);
        
    }
    
    public static Map<String, String> getObjectTypeNames ()
    {
        
        return new HashMap (Environment.objectTypeNamesSingular);
        
    }
    
    public static String getObjectTypeName (DataObject t)
    {
        
        if (t == null)
        {
            
            return null;
            
        }
        
        return Environment.getObjectTypeName (t.getObjectType ());
        
    }

    public static String getObjectTypeName (String t)
    {

        if (t == null)
        {
            
            return null;
            
        }
    
        t = t.toLowerCase ();
        
        return Environment.objectTypeNamesSingular.get (t);
/*    
        if (Warmup.OBJECT_TYPE.equals (t))
        {

            return "Warm-up";

        }

        if (QCharacter.OBJECT_TYPE.equals (t))
        {

            return "Character";

        }

        if (Location.OBJECT_TYPE.equals (t))
        {

            return "Location";

        }

        if (Chapter.OBJECT_TYPE.equals (t))
        {

            return "Chapter";

        }

        if (QObject.OBJECT_TYPE.equals (t))
        {

            return "Item";

        }

        if (ResearchItem.OBJECT_TYPE.equals (t))
        {

            return "Research Item";

        }

        if (OutlineItem.OBJECT_TYPE.equals (t))
        {

            return "Plot Outline Item";

        }

        if (Note.OBJECT_TYPE.equals (t))
        {

            return "Note";

        }

        if (Project.OBJECT_TYPE.equals (t))
        {

            return "Project";

        }

        if (Scene.OBJECT_TYPE.equals (t))
        {

            return "Scene";

        }

        return null;
*/
    }

    public static int getPercent (float t,
                                  float b)
    {
        
        return (int) ((t / b) * 100);
        
    }
    
    public static String getObjectTypeNamePlural (DataObject t)
    {
        
        if (t == null)
        {
            
            return null;
            
        }
        
        return Environment.getObjectTypeNamePlural (t.getObjectType ());
        
    }
    
    public static String getObjectTypeNamePlural (String t)
    {

        if (t == null)
        {
            
            return null;
            
        }
    
        t = t.toLowerCase ();
       
        return Environment.objectTypeNamesPlural.get (t);
/*    
        if (Warmup.OBJECT_TYPE.equals (t))
        {

            return "Warm-ups";

        }    
    
        if (QCharacter.OBJECT_TYPE.equals (t))
        {

            return "Characters";

        }

        if (Scene.OBJECT_TYPE.equals (t))
        {

            return "Scenes";

        }

        if (Project.OBJECT_TYPE.equals (t))
        {

            return "Projects";

        }

        if (Location.OBJECT_TYPE.equals (t))
        {

            return "Locations";

        }

        if (Chapter.OBJECT_TYPE.equals (t))
        {

            return "Chapters";

        }

        if (QObject.OBJECT_TYPE.equals (t))
        {

            return "Items";

        }

        if (ResearchItem.OBJECT_TYPE.equals (t))
        {

            return "Research";

        }

        if (OutlineItem.OBJECT_TYPE.equals (t))
        {

            return "Plot Outline Items";

        }

        if (Note.OBJECT_TYPE.equals (t))
        {

            return "Notes";

        }

        return null;
*/
    }

    public static String getObjectTypeName (NamedObject n)
    {

        if (n == null)
        {

            return null;

        }

        return Environment.getObjectTypeName (n.getObjectType ());

    }

    public static String getObjectTypeNamePlural (NamedObject n)
    {

        if (n == null)
        {

            return null;

        }

        return Environment.getObjectTypeNamePlural (n.getObjectType ());

    }

    public static Map getOpenProjects ()
    {

        return new HashMap (Environment.openProjects);

    }

    public static boolean projectsEqual (Project p1,
                                         Project p2)
    {

        if ((p1 == null) ||
            (p2 == null))
        {

            return false;

        }

        if (p1.getName ().equalsIgnoreCase (p2.getName ()))
        {

            return true;

        }

        return false;

    }

    public static void removeProject (Project p)
                               throws Exception
    {

        Set<Project> projs = Environment.getAllProjects ();

        Set<Project> nprojs = new LinkedHashSet ();

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project pr = iter.next ();

            if (!Environment.projectsEqual (pr,
                                            p))
            {

                nprojs.add (pr);

            }

        }

        Environment.saveProjectsFile (nprojs);

    }

    public static File getQuollWriterJarsDir ()
    {
        
        String userDir = System.getProperty ("user.dir");

        if (!userDir.endsWith ("jars"))
        {
            
            userDir += "/jars";
            
        }
        
        File dir = null;
        
        try
        {
            
            dir = new File (userDir).getCanonicalFile ();
            
        } catch (Exception e) {
            
            return null;
            
        }
        
        if ((!dir.exists ())
            ||
            (dir.isFile ())
           )
        {
            
            return null;
            
        }
        
        return dir;
        
    }
    
    public static void projectClosed (AbstractProjectViewer pv)
                               throws Exception
    {

        Project proj = pv.getProject ();

        Object r = Environment.openProjects.remove (proj);

        // Update the last modified date.
        Set<Project> projs = Environment.getAllProjects ();

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project p = iter.next ();

            if (p.getName ().equalsIgnoreCase (proj.getName ()))
            {

                p.setLastEdited (new Date ());

            }

        }

        Environment.saveProjectsFile (projs);

        if (Environment.openProjects.size () == 0)
        {

            if (Environment.isWindows)
            {

                File userDir = Environment.getQuollWriterJarsDir ();

                if (userDir == null)
                {
                    
                    return;
                    
                }
                
                if (Environment.upgradeRequired)
                {
                
                    Environment.upgradeRequired = false;
                
                    try
                    {

                        List args = new ArrayList ();
                        args.add (System.getProperty ("java.home") + "\\bin\\java.exe");
                        args.add ("-jar");
                        args.add (userDir + "\\QuollWriter-upgrade.jar");

                        ProcessBuilder pb = new ProcessBuilder (args);
                        pb.start ();

                    } catch (Exception e)
                    {

                    }

                }

            }

        }

    }

    public static void addOpenedProject (AbstractProjectViewer pv)
                                  throws Exception
    {

        Environment.openProjects.put (pv.getProject (),
                                      pv);

        Set<Project> projs = Environment.getAllProjects ();

        projs.add (pv.getProject ());

        Environment.saveProjectsFile (projs);

    }

    public static String replaceObjectNames (String t)
    {

        if (t == null)
        {

            return t;

        }

        StringBuilder b = new StringBuilder (t);

        int start = b.indexOf ("{");

        while (start > -1)
        {

            int end = b.indexOf ("}",
                                 start);

            if (end > -1)
            {

                String ot = b.substring (start + 1,
                                         end);

                String newot = ot.toLowerCase ();

                if (ot.endsWith ("s"))
                {

                    newot = Environment.getObjectTypeNamePlural (newot.substring (0,
                                                                                  newot.length () - 1));

                } else
                {

                    newot = Environment.getObjectTypeName (newot);

                }

                if (newot == null)
                {

                    newot = ot;

                } else
                {

                    if (start > -1)
                    {

                        if (Character.isUpperCase (ot.charAt (0)))
                        {

                            newot = Character.toUpperCase (newot.charAt (0)) + newot.substring (1);

                        } else
                        {

                            newot = newot.toLowerCase ();

                        }

                    }

                }

                b.replace (start,
                           end + 1,
                           newot);

                start += newot.length ();

            }

            start = b.indexOf ("{",
                               start);

        }

        return b.toString ();

    }

    public static String canOpenProject (Project p)
    {
        
        if (p == null)
        {
            
            return "{Project} does not exist.";
            
        }
        
        if (!p.getProjectDirectory ().exists ())
        {
            
            return "Cannot find {project} directory <b>" + p.getProjectDirectory () + "</b>.";
            
        }
        
        if (!p.getProjectDirectory ().isDirectory ())
        {
            
            return "Path to {project} <b>" + p.getProjectDirectory () + "</b> is a file, but a directory is expected.";
            
        }
        
        if (!Utils.getQuollWriterDirFile (p.getProjectDirectory ()).exists ())
        {
            
            return "{Project} directory <b>" + p.getProjectDirectory () + "</b> doesn't appear to be a valid Quoll Writer {project}.";
            
        }
        
        return null;
        
    }
    
    public static boolean checkCanOpenProject (Project p,
                                               boolean showFindOrOpen)
    {
                
        String r = Environment.canOpenProject (p);
        
        if (r != null)
        {

            // Do this first to ensure the error shows above it.
            if (showFindOrOpen)
            {
                
                FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_OPEN);
                f.setVisible (true);                                                
                
            }
            
            UIUtils.showErrorMessage (null,
                                      "Unable to open {project} <b>" + p.getName () + "</b>, reason:<br /><br />" + r);
                        
            return false;
            
        }
        
        return true;
        
    }
    
    public static boolean openLastEditedProject ()
                                          throws Exception
    {

        List projs = new ArrayList (Environment.getAllProjects ());

        if (projs.size () > 0)
        {

            Collections.sort (projs,
                              new ProjectSorter ());

            Project p = (Project) projs.get (0);

            // Check to see if the project directory exists.
            if (!Environment.checkCanOpenProject (p,
                                                  true))
            {
                
                // We return true here since we don't want further errors to be displayed or the find/open.
                return true;
                
            }
            
            try
            {

                // Get the first.
                Environment.openProject (p);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open project: " +
                                      p,
                                      e);

                Startup.ss.finish ();

                return false;

            }

            return true;

        }

        return false;

    }
    
    public static Project getProject (String name)
                                      throws Exception
    {
        
        Set<Project> projs = Environment.getAllProjects ();

        for (Project p : projs)
        {
            
            if (p.getName ().equalsIgnoreCase (name))
            {
                
                return p;
                
            }
            
        }
        
        return null;
        
    }
    
    public static void openProject (File f)
    {
        
        // Check the file extension
  
    }
    
    public static /*AbstractProjectViewer*/ void  openProject (final Project p)
                                              throws Exception
    {

        AbstractProjectViewer pv = (AbstractProjectViewer) Environment.openProjects.get (p);

        if (pv != null)
        {

            pv.setVisible (true);
            pv.setState (java.awt.Frame.NORMAL);
            pv.toFront ();

        } else
        {

            try
            {

                pv = Environment.getProjectViewerForType (p);

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to open project: " +
                                            p,
                                            e);

            }

            if (pv == null)
            {
                
                throw new GeneralException ("Unable to open project: " +
                                            p);
                
            }
            
            final AbstractProjectViewer fpv = pv;
            
            Startup.ss.setProgress (75);

            String password = p.getFilePassword ();

            
            if ((p.isEncrypted ()) &&
                (password == null))
            {

                Startup.ss.finish ();

                PasswordInputWindow.create ("Enter password",
                                            "lock",
                                            "{Project}: <b>" + p.getName () + "</b> is encrypted, please enter the password.",
                                            "Open",
                                            new ValueValidator<String> ()
                                            {
                                                
                                                public String isValid (String v)
                                                {
                                                    
                                                    if ((v == null)
                                                        ||
                                                        (v.trim ().equals (""))
                                                       )
                                                    {
                                                        
                                                        return "Please enter the password.";
                                                        
                                                    }
    
                                                    try
                                                    {
    
                                                        fpv.openProject (p,
                                                                         v);
                                            
                                                        // Need to get the object again because the one passed in could be the one from the open.
                                                        Environment.openProjects.put (fpv.getProject (),
                                                                                      fpv);
                                
                                                    } catch (Exception e) {
                                                        
                                                        if (ObjectManager.isEncryptionException (e))
                                                        {
                                                        
                                                            return "Password is not valid.";

                                                        }
                                                            
                                                        Environment.logError ("Cant open project: " +
                                                                              p,
                                                                              e);
                                                        
                                                        UIUtils.showErrorMessage (null,
                                                                                  "Sorry, the {project} can't be opened.  Please contact Quoll Writer support for assistance.");
                                                        
                                                        return null;
                                                        
                                                    }
                                                    
                                                    return null;
                                                    
                                                }
                                                
                                            },
                                            new ActionAdapter ()
                                            {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    // All handled by the validator.
                                                    
                                                }
                                                
                                            },
                                            new ActionAdapter ()
                                            {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                        
                                                    // Show the open/close.
                                                    FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_OPEN | FindOrOpen.SHOW_NEW);
                                                    f.setVisible (true);                                                
                                                    
                                                }
                                                
                                            });
                
                return;

            }

            pv.openProject (p,
                            password);

            Startup.ss.setProgress (90);

            // Need to get the object again because the one passed in could be the one from the open.
            Environment.openProjects.put (pv.getProject (),
                                          pv);

        }

        //return pv;

    }

    public static void addToAchievementsManager (AbstractProjectViewer viewer)
    {
        
        try
        {
        
            Environment.achievementsManager.addProjectViewer (viewer);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to init achievements for project: " +
                                  viewer.getProject (),
                                  e);
            
        }
        
    }

    public static void removeFromAchievementsManager (AbstractProjectViewer viewer)
    {
        
        try
        {
        
            Environment.achievementsManager.removeProjectViewer (viewer);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to remove from achievements: " +
                                  viewer.getProject (),
                                  e);
            
        }
        
    }

    public static void logDebugMessage (String m)
    {
        
        if (Environment.debugMode)
        {
            
            Environment.logMessage (m);
            
        }
        
    }
    
    public static void logMessage (String m)
    {

        if (Environment.debugMode)
        {

            //System.out.println (m);

        }

        Environment.generalLog.logInformationMessage (m);

    }

    public static void logError (String m)
    {

        if (Environment.errorLog == null)
        {
            
            return;
            
        }
    
        Environment.logError (m,
                              null);

    }

    public static void logSQLStatement (String s,
                                        List   params)
    {

        if (!Environment.debugMode)
        {

            return;

        }

        Environment.sqlLog.logInformationMessage ("SQL:=============================\n" + s + "\nPARAMS:\n" + params);

    }

    public static void logError (String    m,
                                 Exception ex)
    {

        if (Environment.errorLog == null)
        {
                        
            return;
            
        }
        
        Environment.errorLog.logError (m,
                                       ex,
                                       null);

        if ((!Environment.debugMode)
            &&
            (Environment.getUserProperties ().getPropertyAsBoolean (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME))
           )
        {

            Map details = new HashMap ();
            
            try
            {
                
                details.put ("errorLog",
                             IOUtils.getFile (Environment.getErrorLogFile ()));
                details.put ("generalLog",
                             IOUtils.getFile (Environment.getGeneralLogFile ()));

            } catch (Exception e) {
                
                // NOt much we can do here!                
                             
            }
            
            details.put ("reason",
                         m);
            
            if (ex != null)
            {
            
                details.put ("stackTrace",
                             Utils.getStackTrace (ex));
                
            }
                         
            try
            {                         
            
                Environment.sendMessageToSupport ("error",
                                                  details,
                                                  null);            

            } catch (Exception e) {
                
                // Nothing we can do.
                                       
            }
            
        }
        
    }

    public static com.gentlyweb.properties.Properties getDefaultProperties (String objType)
    {

        com.gentlyweb.properties.Properties props = (com.gentlyweb.properties.Properties) Environment.defaultObjectProperties.get (objType);

        if (props == null)
        {

            // Create some new blank properties.
            props = new com.gentlyweb.properties.Properties ();

            Environment.defaultObjectProperties.put (objType,
                                                     props);

        }

        if (props.getId () == null)
        {

            props.setId ("default-" + objType);

        }

        if (props.getParentProperties () == null)
        {

            props.setParentProperties (Environment.userProperties);

        }

        return props;

    }

    public static void renameProject (String oldName,
                                      String newName)
                               throws Exception
    {

        Set projs = Environment.getAllProjects ();

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project p = iter.next ();

            if (p.getName ().equals (oldName))
            {

                p.setName (newName);
                p.setProjectDirectory (new File (p.getProjectDirectory ().getParentFile () + "/" + Utils.sanitizeForFilename (newName)));

            }

        }

        Environment.saveProjectsFile (projs);

    }

    public static void changeProjectDir (Project proj,
                                         File    newDir)
                                  throws Exception
    {

        Set projs = Environment.getAllProjects ();

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project p = iter.next ();

            if (p.getName ().equalsIgnoreCase (proj.getName ()))
            {

                p.setProjectDirectory (newDir);

            }

        }

        Environment.saveProjectsFile (projs);

    }
    
    public static boolean hasProjectsFile ()
    {

        return Environment.getProjectsFile ().exists ();

    }

    private static File getUserDefaultProjectPropertiesFile ()
    {
        
        return new File (Environment.getUserQuollWriterDir () + "/" + Constants.DEFAULT_PROJECT_PROPERTIES_FILE_NAME);
        
    }
    
    private static File getProjectsFile ()
    {

        return new File (Environment.getUserQuollWriterDir () + "/" + Constants.PROJECTS_FILE_NAME);

    }

    public static AbstractProjectViewer getProjectViewerForType (Project p)
                                                          throws Exception
    {

        if (p.getType ().equals (Project.NORMAL_PROJECT_TYPE))
        {

            return new ProjectViewer ();

        }

        if (p.getType ().equals (Project.EDITOR_PROJECT_TYPE))
        {

            return new EditorViewer ();

        }

        if (p.getType ().equals (Project.WARMUPS_PROJECT_TYPE))
        {

            return new WarmupsViewer ();

        }

        throw new GeneralException ("Project type: " +
                                    p.getType () +
                                    " is not supported.");

    }

    public static String getButtonLabel (String preferredValue,
                                         String id)
    {
        
        if (preferredValue != null)
        {
            
            return preferredValue;
            
        }
        
        return Environment.getButtonLabel (id);
        
    }
    
    public static String getButtonLabel (String id)
    {
        
        return Environment.buttonLabels.get (id.toLowerCase ());
        
    }
    
    public static void saveProjectsFile (Set<Project> projs)
                                  throws Exception
    {

        Element root = new Element (Environment.XMLConstants.projects);

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project p = iter.next ();

            Element pEl = p.getAsJDOMElement ();
            root.addContent (pEl);
            /*
            Element pEl = new Element (Project.OBJECT_TYPE);
            root.addContent (pEl);

            Element nEl = new Element (Environment.XMLConstants.name);
            pEl.addContent (nEl);
            nEl.addContent (p.getName ());

            if (p.getType () == null)
            {

                p.setType (Project.NORMAL_PROJECT_TYPE);

            }

            pEl.setAttribute (Environment.XMLConstants.type,
                              p.getType ());

            if (p.getBackupService () != null)
            {
                
                pEl.setAttribute (Environment.XMLConstants.backupService,
                                  p.getBackupService ());
                              
            }
            
            Element dEl = new Element (Environment.XMLConstants.directory);
            pEl.addContent (dEl);
            dEl.addContent (p.getProjectDirectory ().getPath ());

            Date lastEdited = p.getLastEdited ();

            if (lastEdited != null)
            {

                pEl.setAttribute (Environment.XMLConstants.lastEdited,
                                  String.valueOf (lastEdited.getTime ()));

            }

            pEl.setAttribute (Environment.XMLConstants.encrypted,
                              Boolean.valueOf (p.isEncrypted ()).toString ());

            if (p.isNoCredentials ())
            {

                pEl.setAttribute (Environment.XMLConstants.noCredentials,
                                  Boolean.valueOf (p.isNoCredentials ()).toString ());

            }

            String id = p.getEditorsProjectId ();
            
            if (id != null)
            {
                
                pEl.setAttribute (Environment.XMLConstants.)
                
            }
            */
        }

        JDOMUtils.writeElementToFile (root,
                                      Environment.getProjectsFile (),
                                      true);

    }

    public static Set<Project> getAllProjects ()
                                        throws Exception
    {

        Set<Project> ret = new LinkedHashSet ();

        File f = Environment.getProjectsFile ();

        if (!f.exists ())
        {

            return ret;

        }

        // Get the projects file.
        Element root = JDOMUtils.getFileAsElement (f,
                                                   Environment.GZIP_EXTENSION);

        List pels = JDOMUtils.getChildElements (root,
                                                Project.OBJECT_TYPE,
                                                false);

        for (int i = 0; i < pels.size (); i++)
        {
        
            Element pEl = (Element) pels.get (i);

            Project p = new Project (pEl);
/*            
            String name = JDOMUtils.getChildElementContent (pEl,
                                                            Environment.XMLConstants.name);
            String type = JDOMUtils.getAttributeValue (pEl,
                                                       Environment.XMLConstants.type,
                                                       false);

            if (type.equals (""))
            {

                type = null;

            }

            String directory = JDOMUtils.getChildElementContent (pEl,
                                                                 Environment.XMLConstants.directory);

            boolean encrypted = JDOMUtils.getAttributeValueAsBoolean (pEl,
                                                                      Environment.XMLConstants.encrypted,
                                                                      false);

            boolean noCredentials = false;

            if (JDOMUtils.getAttribute (pEl,
                                        Environment.XMLConstants.noCredentials,
                                        false) != null)
            {

                noCredentials = JDOMUtils.getAttributeValueAsBoolean (pEl,
                                                                      Environment.XMLConstants.noCredentials);

            }

            String d = JDOMUtils.getAttributeValue (pEl,
                                                    Environment.XMLConstants.lastEdited,
                                                    false);

            String bs = JDOMUtils.getAttributeValue (pEl,
                                                     Environment.XMLConstants.backupService,
                                                     false);
                                                    
            File dir = new File (directory);

            Project p = new Project (name);
            p.setProjectDirectory (dir);
            p.setEncrypted (encrypted);
            p.setNoCredentials (noCredentials);

            if (!bs.equals (""))
            {
                
                p.setBackupService (bs);
                
            }
            
            if (type != null)
            {

                p.setType (type);

            }

            if (!d.equals (""))
            {

                try
                {

                    p.setLastEdited (new Date (Long.parseLong (d)));

                } catch (Exception e)
                {

                    // Ignore it.

                }

            }
*/
            ret.add (p);

        }

        return ret;

    }

    /**
     * A special call that looks for the English subdirectory in the dictionaries dir.
     */
    public static boolean hasEnglishDictionaryFiles ()
    {
        
        File dir = Environment.getDictionaryDirectory ("English");
            
        return dir.exists ();
        
    }
    
    public static Project getWarmupsProject ()
                                      throws Exception
    {

        Set<Project> projs = Environment.getAllProjects ();

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project p = iter.next ();

            if (p.getType ().equals (Project.WARMUPS_PROJECT_TYPE))
            {

                return p;

            }

        }

        return null;

    }

    public static String getProperty (String name)
    {

        return Environment.userProperties.getProperty (name);

    }

    public static File getUserQuollWriterDir ()
    {

        File d = new File (System.getProperty ("user.home") + "/" + Constants.QUOLL_WRITER_DIR_NAME + "/");

        d.mkdirs ();

        return d;

    }

    public static File getDictionaryDirectory (String lang)
    {
        
        return new File (Environment.getUserQuollWriterDir ().getPath () + Constants.DICTIONARIES_DIR + lang);
    
    }
    
    public static File getLogDir ()
    {

        File d = new File (Environment.getUserQuollWriterDir ().getPath () + Constants.LOGS_DIR);

        d.mkdirs ();

        return d;

    }

    public static File getUserObjectTypeNamesFile ()
    {
        
        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.OBJECT_TYPE_NAMES_FILE_NAME);

        return f;

    }
    
    public static File getUserPropertiesFile ()
    {

        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.PROPERTIES_FILE_NAME);

        f.getParentFile ().mkdirs ();

        return f;

    }
    
    public static File getEditorsAuthorAvatarImageFile (String suffix)
    {

        if (suffix == null)
        {
            
            return null;
                
        }
        
        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.EDITORS_AUTHOR_AVATAR_IMAGE_FILE_NAME_PREFIX + "." + suffix);

        f.getParentFile ().mkdirs ();

        return f;
        
    }

    public static File getEditorsAuthorAvatarImageFile ()
    {
        
        if (Environment.editorsHandler == null)
        {
            
            return null;
            
        }
        
        if (Environment.editorsHandler.getAccount () == null)
        {
            
            return null;
            
        }

        EditorAuthor au = Environment.editorsHandler.getAccount ().getAuthor ();
        
        if (au == null)
        {
            
            return null;
            
        }
                
        String t = Environment.getProperty (Constants.EDITORS_AUTHOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME);
                        
        return Environment.getEditorsAuthorAvatarImageFile (t);
        
    }

    public static File getEditorsAuthorFile ()
    {
        
        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.EDITORS_AUTHOR_FILE_NAME);
        
        f.getParentFile ().mkdirs ();
        
        return f;
        
    }
        
    public static File getEditorsEditorAvatarImageFile (String suffix)
    {

        if (suffix == null)
        {
            
            return null;
                
        }
        
        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.EDITORS_EDITOR_AVATAR_IMAGE_FILE_NAME_PREFIX + "." + suffix);

        f.getParentFile ().mkdirs ();

        return f;
        
    }

    public static File getEditorsEditorAvatarImageFile ()
    {
        
        if (Environment.editorsHandler == null)
        {
            
            return null;
            
        }
        
        if (Environment.editorsHandler.getAccount () == null)
        {
            
            return null;
            
        }

        EditorEditor ed = Environment.editorsHandler.getAccount ().getEditor ();
        
        if (ed == null)
        {
            
            return null;
            
        }
                
        String t = Environment.getProperty (Constants.EDITORS_EDITOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME);
                        
        return Environment.getEditorsEditorAvatarImageFile (t);
        
    }

    public static File getEditorsEditorFile ()
    {
        
        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.EDITORS_EDITOR_FILE_NAME);
        
        f.getParentFile ().mkdirs ();
        
        return f;
        
    }

    public static com.gentlyweb.properties.Properties getUserProperties ()
    {

        return Environment.userProperties;

    }

    public static void setUserProperty (String           name,
                                        AbstractProperty prop)
                                 throws Exception
    {
        
        Environment.userProperties.setProperty (name,
                                                prop);
        
        Environment.saveUserProperties (Environment.userProperties);
        
    }

    public static void setUserProperty (String name,
                                        String value)
                                 throws Exception
    {
        
        Environment.userProperties.setProperty (name,
                                                new StringProperty (name,
                                                                    value));
        
        Environment.saveUserProperties (Environment.userProperties);
        
    }

    public static void saveUserProperties (com.gentlyweb.properties.Properties props)
                                    throws Exception
    {

        if (props == null)
        {
            
            props = Environment.userProperties;
            
        }

        // Load the per user properties.
        File pf = Environment.getUserPropertiesFile ();

        JDOMUtils.writeElementToFile (props.getAsJDOMElement (),
                                      pf,
                                      true);

    }

    public static void saveDefaultProperties (String                              objType,
                                              com.gentlyweb.properties.Properties props)
                                       throws Exception
    {

        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/default-" + objType + "-properties.xml");

        JDOMUtils.writeElementToFile (props.getAsJDOMElement (),
                                      f,
                                      true);

    }

    public static Date parseDate (String d)
    {
        
        try
        {
            
            return Environment.dateFormatter.parse (d);
        
        } catch (Exception e) {
            
            // Bugger
            e.printStackTrace ();
            
        }
        
        return null;
        
    }
    
    public static String formatDate (Date d)
    {

        return Environment.dateFormatter.format (d);

    }

    public static String formatTime (Date d)
    {

        return Environment.timeFormatter.format (d);

    }

    public static void init ()
                      throws Exception
    {

        Header.defaultPaintLeftColor = UIUtils.getComponentColor (); //UIUtils.getColor ("#516CA3");
        GradientPanel.defaultPaintLeftColor = UIUtils.getComponentColor (); //UIUtils.getColor ("#516CA3");
        Header.defaultPaintRightColor = UIUtils.getComponentColor (); //UIUtils.getColor ("#516CA3");
        GradientPanel.defaultPaintRightColor = UIUtils.getComponentColor (); //UIUtils.getColor ("#516CA3");
        Header.defaultTitleColor = UIUtils.getTitleColor ();
        
        Environment.isWindows = System.getProperty ("os.name").startsWith ("Windows");

        Environment.isMac = System.getProperty ("os.name").startsWith ("Mac");

        Environment.isLinux = System.getProperty ("os.name").startsWith ("Linux");

        //System.setErr (Environment.nullOut);
        //System.setOut (Environment.nullOut);

        // Setup our stream handler for the objectref protocol.
        URL.setURLStreamHandlerFactory (new ObjectRefURLStreamHandlerFactory ());

        Environment.dateFormatter = new SimpleDateFormat ("dd MMM yyyy");
        Environment.timeFormatter = new SimpleDateFormat ("HH:mm");

        UIManager.put("SplitPane.background", UIUtils.getComponentColor ());
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 1, 0));
        //UIManager.put("TabbedPane.tabInsets", new Insets(0, 5, 0, 0));
        UIManager.put("Tree.selectionBackground", UIUtils.getColor ("#aaaaaa"));//UIUtils.getTitleColor ());
        UIManager.put("SplitPane.shadow", UIUtils.getColor ("#aaaaaa"));
        UIManager.put("Tree.textForeground", UIUtils.getTitleColor ());
        UIManager.put("Tree.rowHeight", 0);
        UIManager.put("Tree.leftChildIndent", 6);
        UIManager.put("ProgressBar.cellSpacing", 0);
        UIManager.put("PopupMenu.background", UIUtils.getComponentColor ());
        UIManager.put("PopupMenu.foreground", UIUtils.getTitleColor ());
        
        Environment.appVersion = Environment.getResourceFileAsString (Constants.VERSION_FILE).trim ();

        try
        {

            Environment.schemaVersion = Integer.parseInt (Environment.getResourceFileAsString (Constants.SCHEMA_VERSION_FILE).trim ());

        } catch (Exception e)
        {

            // Ignore.

        }

        // Load the default object type names.
        try
        {
            
            Environment.loadObjectTypeNames (JDOMUtils.getStringAsElement (Environment.getResourceFileAsString (Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE)));
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to load default object type names from resource file: " +
                                  Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE);
            
        }
        
        // Load the user specific ones, if present.
        File otf = Environment.getUserObjectTypeNamesFile ();
        
        if (otf.exists ())
        {
            
            try
            {
            
                Environment.loadObjectTypeNames (JDOMUtils.getFileAsElement (otf,
                                                                             Environment.GZIP_EXTENSION));

            } catch (Exception e) {
                
                Environment.logError ("Unable to load user object type names from file: " +
                                      otf,
                                      e);
                
            }
            
        }
        
        com.gentlyweb.properties.Properties sysProps = new com.gentlyweb.properties.Properties (Environment.class.getResourceAsStream (Constants.DEFAULT_PROPERTIES_FILE),
                                                                                                null);

        Startup.ss.setProgress (7);

        sysProps.setId ("system");

        // Load the per user properties.
        File pf = Environment.getUserPropertiesFile ();

        if (pf.exists ())
        {

            try
            {

                Environment.userProperties = new com.gentlyweb.properties.Properties (pf,
                                                                                      Environment.GZIP_EXTENSION);

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load user properties: " +
                                            pf,
                                            e);

            }

            Environment.userProperties.setId ("user");

            // Override the debug mode.
            if (Environment.userProperties.getProperty ("debugMode") != null)
            {

                Environment.debugMode = Environment.userProperties.getPropertyAsBoolean ("debugMode");

            }
                
        }

        // Load the "system" properties.
        Environment.userProperties.setParentProperties (sysProps);

        // Get the system default project properties.
        com.gentlyweb.properties.Properties sysDefProjProps = new com.gentlyweb.properties.Properties (Environment.class.getResourceAsStream (Constants.DEFAULT_PROJECT_PROPERTIES_FILE),
                                                                              Environment.userProperties);
        
        File defUserPropsFile = Environment.getUserDefaultProjectPropertiesFile ();
        
        if (defUserPropsFile.exists ())
        {
        
            com.gentlyweb.properties.Properties userDefProjProps = new com.gentlyweb.properties.Properties (defUserPropsFile,
                                                                                                            Environment.GZIP_EXTENSION);
            
            userDefProjProps.setParentProperties (sysDefProjProps);

            sysDefProjProps = userDefProjProps;
            
        }
        
        // Load the default project properties.
        Environment.defaultObjectProperties.put (Project.OBJECT_TYPE,
                                                 sysDefProjProps);

        Startup.ss.setProgress (10);

        File f = Environment.getErrorLogFile ();

        f.delete ();

        Environment.errorLog = new Logger ();
        Environment.errorLog.initLogFile (f);

        f = Environment.getGeneralLogFile ();

        f.delete ();

        Environment.generalLog = new Logger ();
        Environment.generalLog.initLogFile (f);

        f = Environment.getSQLLogFile ();

        f.delete ();

        Environment.sqlLog = new Logger ();
        Environment.sqlLog.initLogFile (f);

        Startup.ss.setProgress (15);

        if (Environment.isWindows)
        {

            try
            {

                Options.setUseSystemFonts (true);

                UIManager.setLookAndFeel (new WindowsLookAndFeel ());
 
            } catch (Exception e)
            {

                Environment.logError ("Unable to set laf to: " +
                                      WindowsLookAndFeel.class.getName (),
                                      e);

            }

        }

        System.setProperty ("swing.aatext",
                            "true");

        System.setProperty ("aawt.useSystemAAFontSettings",
                            "true");

        // Remove border around splitpane divider.
        UIManager.put ("SplitPaneDivider.border",
                       BorderFactory.createEmptyBorder ());

        UIManager.put ("Button.margin",
                       new java.awt.Insets (3,
                                            3,
                                            3,
                                            3));

        Startup.ss.setProgress (20);

        Startup.ss.setProgress (40);

        try
        {

            Prompts.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to init prompts",
                                  e);

        }

        try
        {

            RuleFactory.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to init rule factory",
                                  e);

        }

        try
        {
            
            Environment.achievementsManager = new AchievementsManager ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to init achievements manager",
                                  e);
            
        }

        // Set up the importers in the background.
        Thread t = new Thread (new Runner ()
            {

                public void run ()
                {

                    Importer.init ();

                    UIUtils.getFontsComboBox (null,
                                              null);

                }

            });

        t.setDaemon (true);
        t.setPriority (Thread.MIN_PRIORITY);

        t.start ();

        Startup.ss.setProgress (50);

        KeyboardFocusManager.getCurrentKeyboardFocusManager ().addKeyEventPostProcessor (new java.awt.KeyEventPostProcessor ()
        {
            
            public boolean postProcessKeyEvent (KeyEvent ev)
            {
            
                if (!(ev.getSource () instanceof JComponent))
                {
                
                    return true;
                
                }
                
                final JComponent focused = (JComponent) ev.getSource ();
                                
                Environment.scrollIntoView (focused);
/*                                        
                if (!(focused.getParent () instanceof JComponent))
                {
                    
                    return true;
                                                    
                }
                
                JComponent parent = (JComponent) focused.getParent ();
                
                if (parent == null)
                {
                    
                    return true;
                    
                }
                
                if (parent instanceof JViewport)
                {

                    parent = (JComponent) parent.getParent ();
                    
                }
                                
                parent.scrollRectToVisible (focused.getBounds ());
                */
                return true;
                
            }
        });
        
        if (Environment.editorServiceAvailable)
        {
        
            Environment.editorsHandler = new EditorsWebServiceHandler ();
            Environment.editorsHandler.init ();
            
        }
                                
    }

    public static String getQuollWriterWebsite ()
    {
        
        if (Environment.debugMode)
        {

            return Environment.getProperty (Constants.QUOLL_WRITER_DEBUG_WEBSITE_PROPERTY_NAME);

        } else {

            return Environment.getProperty (Constants.QUOLL_WRITER_WEBSITE_PROPERTY_NAME);
            
        }        
        
    }
    
    public static String getWordTypes (String word,
                                       String language)
                                       throws GeneralException
    {
        
        SynonymProvider sp = Environment.getSynonymProvider (language);
        
        if (sp != null)
        {
            
            return sp.getWordTypes (word);
            
        }
        
        return null;
        
    }
    
    public static SynonymProvider getSynonymProvider (String language)
                                                      throws GeneralException
    {
        
        if (language == null)
        {
            
            language = Constants.ENGLISH;
            
        }
        
        if (Environment.isEnglish (language))
        {
            
            language = Constants.ENGLISH;
            
        }
        
        // For now, until we are able to support other languages return null for non-english.
        if (!language.equals (Constants.ENGLISH))
        {
            
            return null;
            
        }
        
        SynonymProvider prov = Environment.synonymProviders.get (language.toLowerCase ());
        
        if (prov != null)
        {
            
            return prov;
            
        }
        
        String synCl = Environment.getProperty (Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME);

        if (synCl != null)
        {

            Class c = null;

            try
            {

                c = Class.forName (synCl);

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load synonym provider class: " +
                                            synCl +
                                            " specified by property: " +
                                            Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME,
                                            e);

            }

            if (!SynonymProvider.class.isAssignableFrom (c))
            {

                throw new GeneralException ("Expected synonym provider class: " +
                                            synCl +
                                            " specified by property: " +
                                            Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME +
                                            " to implement interface: " +
                                            SynonymProvider.class.getName ());

            }
            
            // Create the instance.
            try
            {

                prov = (SynonymProvider) c.newInstance ();

            } catch (Exception e)
            {

                // Record the error but don't barf, it's just a service that isn't available
                Environment.logError ("Unable to create new instance of synonym provider class: " +
                                      synCl +
                                      " specified by property: " +
                                      Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME,
                                      e);

            }
            
            if (!prov.isLanguageSupported (language))
            {
                
                return null;
                
            }
            
            try
            {
                
                
                
                prov.init (language);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to init synonym provider instance for language: " +
                                      language +
                                      ", class: " +
                                      synCl +
                                      ", specified by property: " +
                                      Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME,
                                      e);
                
            }
            
            Environment.synonymProviders.put (language.toLowerCase (),
                                              prov);
                        
        }        

        return prov;
        
    }
    
    public static void scrollIntoView (JComponent c)
    {
        
        if (c == null)
        {

            return;
                                        
        }

        if (!(c.getParent () instanceof JComponent))
        {
            
            return;
            
        }
        
        JComponent parent = (JComponent) c.getParent ();
        
        if (!(parent instanceof JComponent))
        {

            return;
                                            
        }
                
        if (parent == null)
        {

            return;
            
        }
        
        if (parent instanceof JViewport)
        {

            parent = (JComponent) parent.getParent ();
            
        }

        parent.scrollRectToVisible (c.getBounds ());        
        
    }
    
    public static boolean isEnglish (String language)
    {
        
        if (language == null)
        {
            
            return false;
            
        }
        
        return Constants.BRITISH_ENGLISH.equalsIgnoreCase (language)
               ||
               Constants.US_ENGLISH.equalsIgnoreCase (language);
        
    }
    
    public static void resetObjectTypeNamesToDefaults ()
    {

        Environment.objectTypeNamesSingular.clear ();
        Environment.objectTypeNamesPlural.clear ();
        
        // Load the default object type names.
        try
        {
            
            Environment.loadObjectTypeNames (JDOMUtils.getStringAsElement (Environment.getResourceFileAsString (Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE)));
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to load default object type names from resource file: " +
                                  Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE);
            
        }
        
        File otf = Environment.getUserObjectTypeNamesFile ();

        // Remove the file.
        otf.delete ();
        
    }
    
    public static void loadObjectTypeNames (Element root)
                                            throws  Exception
    {
        
        Map<String, String> singular = new HashMap ();
        Map<String, String> plural = new HashMap ();
        
        List els = JDOMUtils.getChildElements (root,
                                               XMLConstants.object,
                                               false);
        
        for (int i = 0; i < els.size (); i++)
        {
            
            Element el = (Element) els.get (i);
            
            String objType = JDOMUtils.getAttributeValue (el,
                                                          XMLConstants.type);
            
            objType = objType.toLowerCase ();
            
            String s = JDOMUtils.getChildElementContent (el,
                                                         XMLConstants.singular,
                                                         false);
            
            if (!s.equals (""))
            {
                
                singular.put (objType,
                              s);
                
            }

            String p = JDOMUtils.getChildElementContent (el,
                                                         XMLConstants.plural,
                                                         false);
            
            if (!p.equals (""))
            {
                
                plural.put (objType,
                            p);
                
            }
            
        }

        Environment.objectTypeNamesSingular.putAll (singular);
        Environment.objectTypeNamesPlural.putAll (plural);
        
    }
    
    public static URL getSupportUrl (String pagePropertyName)
                                     throws Exception
    {
                
        String prefName = Constants.SUPPORT_URL_BASE_PROPERTY_NAME;
        
        if (Environment.debugMode)
        {
            
            prefName = Constants.DEBUG_SUPPORT_URL_BASE_PROPERTY_NAME;
            
        }
        
        return new URL (Environment.getProperty (prefName) + Environment.getProperty (pagePropertyName));
        
    }
    
    public static File getGeneralLogFile ()
    {

        return new File (Environment.getLogDir ().getPath () + "/" + Constants.GENERAL_LOG_NAME);

    }

    public static File getSQLLogFile ()
    {

        return new File (Environment.getLogDir ().getPath () + "/" + Constants.SQL_LOG_NAME);

    }

    public static File getErrorLogFile ()
    {

        return new File (Environment.getLogDir ().getPath () + "/" + Constants.ERROR_LOG_NAME);

    }

    public static File getUserDictionaryFile ()
    {

        return new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.USER_DICTIONARY_FILE_NAME);

    }

    public static Set<BackgroundImage> getBackgroundImages ()
                                                            throws Exception
    {
        
        Set<BackgroundImage> ret = new LinkedHashSet ();
        
        Set<String> bgImages = Environment.getResourceListing (Constants.BACKGROUND_THUMB_IMGS_DIR);
        
        for (String s : bgImages)
        {
            
            ret.add (new BackgroundImage (s));
            
        }
        
        return ret;
        
    }
    
    public static Image getBackgroundImage (String name)
    {

        Image im = Environment.backgroundImages.get (name);

        if (im != null)
        {

            return im;

        }

        if (name == null)
        {

            return null;

        }

        name = Constants.BACKGROUND_IMGS_DIR + name;

        URL url = Environment.class.getResource (name);

        if (url == null)
        {

            // Can't find image, log the problem but keep going.
            Environment.logError ("Unable to find/load image: " +
                                  name +
                                  ", check images jar to ensure file is present.",

                                  // Gives a stack trace
                                  new Exception ());

            return null;

        }

        im = new ImageIcon (url).getImage ();

        Environment.backgroundImages.put (name,
                                          im);

        return im;

    }

    public static Image getBackgroundThumbImage (String name)
    {

        if (name == null)
        {

            return null;

        }

        name = Constants.BACKGROUND_THUMB_IMGS_DIR + name;

        URL url = Environment.class.getResource (name);

        if (url == null)
        {

            // Can't find image, log the problem but keep going.
            Environment.logError ("Unable to find/load image: " +
                                  name +
                                  ", check images jar to ensure file is present.",

                                  // Gives a stack trace
                                  new Exception ());

            return null;

        }

        return new ImageIcon (url).getImage ();

    }

    public static ImageIcon getAchievementIcon ()
    {
        
        URL url = Environment.class.getResource (Constants.ACHIEVEMENT_ICON_FILE);

        if (url == null)
        {

            // Can't find image, log the problem but keep going.
            Environment.logError ("Unable to find/load achievement image: " +
                                  Constants.ACHIEVEMENT_ICON_FILE +
                                  ", check images jar to ensure file is present.",

                                  // Gives a stack trace
                                  new Exception ());

            return null;

        }

        return new ImageIcon (url);        
        
    }

    public static ImageIcon getObjectIcon (String ot,
                                           int    iconType)
    {
        
        return Environment.getIcon (ot,
                                    iconType);
        
    }
    
    public static ImageIcon getObjectIcon (DataObject d,
                                           int        iconType)
    {
        
        String ot = d.getObjectType ();
        
        if (d instanceof Note)
        {
            
            Note n = (Note) d;
            
            if (n.isEditNeeded ())
            {
                
                ot = Constants.EDIT_NEEDED_NOTE_ICON_NAME;
                
            }
            
        }
        
        return Environment.getObjectIcon (ot,
                                          iconType);
        
    }
    
    public static URL getIconURL (String  name,
                                  int     type)
                                  //boolean large)
    {

        if (name == null)
        {

            return null;

        }

        if (name.indexOf ('.') == -1)
        {

            int size = 16;
        
            if (type == Constants.ICON_TOOLBAR)
            {
                
                size = 20;
                
            }

            if (type == Constants.ICON_MENU_INNER)
            {
                
                size = 16;
                
            }
            
            if (type == Constants.ICON_PANEL_MAIN)
            {
                
                size = 24;
                
            }

            if (type == Constants.ICON_NOTIFICATION)
            {
                
                size = 24;
                
            }

            if (type == Constants.ICON_ACHIEVEMENT_HEADER)
            {
                
                size = 24;
                
            }
        
            if (type == Constants.ICON_PANEL_ACTION) 
            {
                
                size = 20;
                
            }

            if (type == Constants.ICON_TITLE_ACTION) 
            {
                
                size = 24;
                
            }

            if (type == Constants.ICON_BG_SWATCH) 
            {
                
                size = 37;
                
            }
            
            if (type == Constants.ICON_FULL_SCREEN_ACTION)
            {
                
                size = 24;
                
            }
            
            if (type == Constants.ICON_TITLE) 
            {
                
                size = 24;
                
            }
                
            name = Constants.IMGS_DIR + name + size + ".png";

        } else
        {

            name = Constants.IMGS_DIR + name;

        }

        return Environment.class.getResource (name);

    }
    
    public static ImageIcon getLoadingIcon ()
    {

        URL url = Environment.class.getResource (Constants.IMGS_DIR + Constants.LOADING_GIF_NAME);

        if (url == null)
        {

            if (Environment.debugMode)
            {

                // Can't find image, log the problem but keep going.
                Environment.logError ("Unable to find loading image: " +
                                      Constants.LOADING_GIF_NAME +
                                      ", check images jar to ensure file is present.",
                                      // Gives a stack trace
                                      new Exception ());

            }

            return null;

        }

        try
        {

            // Can't use ImageIO here, won't animate only reads first frame.
            return new ImageIcon (url);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to find loading image: " +
                                  Constants.LOADING_GIF_NAME,
                                  e);
            
        }

        return null;        
        
    }
    
    public static ImageIcon getIcon (String  name,
                                     int     type)
    {
    
        URL url = Environment.getIconURL (name,
                                          type);

        if (url == null)
        {

            if (Environment.debugMode)
            {

                // Can't find image, log the problem but keep going.
                Environment.logError ("Unable to find/load image: " +
                                      name +
                                      ", type: " + type + 
                                      ", check images jar to ensure file is present.",
                                      // Gives a stack trace
                                      new Exception ());

            }

            return null;

        }

        try
        {

            return new ImageIcon (ImageIO.read (url));
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to find/load image: " +
                                  name,
                                  e);
            
        }

        return null;

    }

    public static ImageIcon getWindowIcon ()
    {

        return new ImageIcon (Environment.class.getResource (Constants.IMGS_DIR + Constants.WINDOW_ICON_PNG_NAME));

    }

    public static BufferedImage getImage (String fileName)
    {
        
        try
        {
        
            return ImageIO.read (Environment.class.getResource (fileName));
        
        } catch (Exception e) {
            
            Environment.logError ("Unable to read resource stream for image: " +
                                  fileName,
                                  e);
            
        }
        
        return null;
        
        //return new ImageIcon (Environment.class.getResource (fileName)).getImage ();
        
    }
    
    public static Image getTransparentImage ()
    {

        return new ImageIcon (Environment.class.getResource (Constants.IMGS_DIR + Constants.TRANSPARENT_PNG_NAME)).getImage ();

    }
    
    public static ImageIcon getLogo ()
    {

        return new ImageIcon (Environment.class.getResource (Constants.IMGS_DIR + Constants.LOGO_PNG_NAME));

    }

    public static ImageIcon get3rdPartyLogo (String type)
    {

        return new ImageIcon (Environment.class.getResource (Constants.IMGS_DIR + type + "-logo.png"));

    }
    
    public static String getWindowTitle (String title)
    {

        return title + Environment.getWindowNameSuffix ();

    }

    public static String getWindowNameSuffix ()
    {

        return " - " + Constants.QUOLL_WRITER_NAME;

    }

    public static Color getBorderColor ()
    {

        // #ABABAB
        // return new Color (1, 8, 128, 128);
        return com.quollwriter.ui.UIUtils.getBorderColor ();
        //return com.quollwriter.ui.UIUtils.getColor ("#aaaaaa");//"#CCCCCC");
        /* old
        return new Color (171,
                          171,
                          171);
        */
                          
    }

    public static Color getInnerBorderColor ()
    {

        return com.quollwriter.ui.UIUtils.getColor ("#CCCCCC");
                          
    }

    public static Color getHighlightColor ()
    {

        // #DAE4FC
        return new Color (218,
                          228,
                          252);

    }

    public static String getQuollWriterVersion ()
    {

        return Environment.appVersion;

    }

    public static void extractResourceToFile (String name,
                                              File   outFile)
                                       throws GeneralException
    {

        try
        {

            BufferedInputStream bin = new BufferedInputStream (Environment.class.getResourceAsStream (name));

            BufferedOutputStream bout = new BufferedOutputStream (new FileOutputStream (outFile));

            IOUtils.streamTo (bin,
                              bout,
                              4096);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to stream resource: " +
                                        name +
                                        " to output file: " +
                                        outFile,
                                        e);

        }

    }

    public static InputStream getResourceStream (String name)
    {

        return Environment.class.getResourceAsStream (name);

    }

    public static String getResourceFileAsString (String name)
                                           throws GeneralException
    {

        InputStream is = Environment.getResourceStream (name);

        if (is == null)
        {

            return null;

        }

        BufferedReader r = new BufferedReader (new InputStreamReader (is));

        StringBuilder b = new StringBuilder ();

        try
        {

            String line = r.readLine ();

            while (line != null)
            {

                b.append (line);
                b.append ('\n');

                line = r.readLine ();

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to read from resource: " +
                                        name,
                                        e);

        } finally
        {

            // Oh how I hate this...
            try
            {

                r.close ();

            } catch (Exception e)
            {

                // Ignore...  What could we do otherwise?

            }

        }

        return b.toString ();

    }

    public static int getSchemaVersion ()
    {

        return Environment.schemaVersion;

    }

    public static File writeStreamToTempFile (InputStream in)
                                       throws IOException
    {

        File f = File.createTempFile ("___" + Constants.QUOLL_WRITER_DIR_NAME + System.currentTimeMillis (),
                                      null);

        BufferedOutputStream bout = new BufferedOutputStream (new FileOutputStream (f));

        IOUtils.streamTo (new BufferedInputStream (in),
                          bout,
                          4096);

        bout.flush ();
        bout.close ();

        return f;

    }

    public static void doVersionCheck (final AbstractProjectViewer pv)
    {

        if (Environment.doneVersionCheck)
        {

            return;

        }

        if (Environment.userProperties.getPropertyAsBoolean (Constants.DO_AUTO_UPDATE_CHECK_PROPERTY_NAME))
        {

            Environment.doneVersionCheck = true;

            Runner r = new Runner ()
            {

                public void run ()
                {

                    try
                    {

                        URL u = Environment.getSupportUrl (Constants.GET_LATEST_VERSION_PAGE_PROPERTY_NAME);

                        HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

                        conn.setDoInput (true);
                        conn.setDoOutput (true);

                        conn.connect ();

                        BufferedReader b = new BufferedReader (new InputStreamReader (conn.getInputStream ()));

                        String detail = b.readLine ();

                        b.close ();

                        if ((detail == null) ||
                            (detail.length () == 0))
                        {

                            throw new GeneralException ("Unable to get latest version information from: " + u);

                        }

                        StringTokenizer tok = new StringTokenizer (detail,
                                                                   "|");

                        if (tok.countTokens () < 2)
                        {

                            // No upgrade information.
                            return;

                        }

                        final String newVersion = tok.nextToken ();

                        final String size = tok.nextToken ();

                        final String digest = tok.nextToken ();

                        int oldVer = Environment.getVersionAsInt (Environment.appVersion);

                        final int newVer = Environment.getVersionAsInt (newVersion);

                        if (newVer > oldVer)
                        {

                            SwingUtilities.invokeLater (new Runner ()
                                {

                                    public void run ()
                                    {

                                        Box ib = new Box (BoxLayout.Y_AXIS);
                                        ib.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                          Short.MAX_VALUE));

                                        JTextPane p = UIUtils.createHelpTextPane ("A new version of " + Constants.QUOLL_WRITER_NAME + " is available.  <a href='http://quollwriter.com/user-guide/version-changes/" + newVersion.replace (".",
                                                                                                                                                                                                                                          "_") + ".html'>View the changes.</a>",
                                                                                  pv);
                                        p.setAlignmentX (Component.LEFT_ALIGNMENT);

                                        p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                         Short.MAX_VALUE));
                                        p.setBorder (null);

                                        ib.add (p);
                                        ib.add (Box.createVerticalStrut (5));

                                        JButton installNow = new JButton ("Install Now");
                                        JButton installLater = new JButton ("Later");

                                        Box bb = new Box (BoxLayout.X_AXIS);
                                        bb.add (installNow);
                                        bb.add (Box.createHorizontalStrut (5));
                                        bb.add (installLater);
                                        bb.setAlignmentX (Component.LEFT_ALIGNMENT);

                                        ib.add (bb);
                                        ib.add (Box.createVerticalStrut (5));

                                        final ActionListener removeNot = pv.addNotification (ib,
                                                                                             "notify",
                                                                                             600);

                                        installNow.addActionListener (new ActionAdapter ()
                                        {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                removeNot.actionPerformed (ev);

                                                new GetLatestVersion (pv,
                                                                      newVersion,
                                                                      size,
                                                                      digest).init ();

                                            }

                                        });

                                        installLater.addActionListener (removeNot);

                                    }

                                });

                        }

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to perform update check",
                                              e);

                    }

                }

            };

            Thread t = new Thread (r);

            t.setDaemon (true);
            t.setPriority (Thread.MIN_PRIORITY);

            t.start ();

        }

    }

    public static long[] getAsLongs (List<Long> l)
    {

        long[] arr = new long[l.size ()];

        for (int i = 0; i < l.size (); i++)
        {

            arr[i] = l.get (i);

        }

        return arr;

    }

    public static DecimalFormat getNumberFormatter ()
    {

        return Environment.numFormat;

    }

    public static String formatNumber (int i)
    {
       
        return Environment.numFormat.format (i);
        
    }

    public static String formatNumber (long i)
    {
       
        return Environment.numFormat.format (i);
        
    }
    
    public static String formatNumber (float f)
    {
      
        return Environment.floatNumFormat.format (f);
        
    }

    public static String formatNumber (double f)
    {
      
        return Environment.floatNumFormat.format (f);
        
    }
/*   
    public static String formatNumber (Number n)
    {

        if (n == null)
        {
            
            return "N/A";
            
        }
    
        if (n instanceof Float)
        {
            
            return Environment.floatNumFormat.format (n);
            
        }
    
        return Environment.numFormat.format (n);

    }
*/
    public static void sendMessageToSupport (final String         type,
                                             final Map            info,
                                             final ActionListener onComplete)
    {

        Thread t = new Thread (new Runnable ()
        {
            
            public void run ()
            {
                
                try
                {
            
                    info.put ("quollWriterVersion",
                              Environment.getQuollWriterVersion ().trim ());
                    info.put ("javaVersion",
                              System.getProperty ("java.version"));
                    info.put ("osName",
                              System.getProperty ("os.name"));
                    info.put ("osVersion",
                              System.getProperty ("os.version"));
                    info.put ("osArch",
                              System.getProperty ("os.arch"));
            
                    Element root = new Element ("message");
                    root.setAttribute ("quollWriterVersion",
                                       Environment.getQuollWriterVersion ().trim ());
                    root.setAttribute ("javaVersion",
                                       System.getProperty ("java.version"));
                    root.setAttribute ("osName",
                                       System.getProperty ("os.name"));
                    root.setAttribute ("osVersion",
                                       System.getProperty ("os.version"));
                    root.setAttribute ("osArch",
                                       System.getProperty ("os.arch"));
                    root.setAttribute ("type",
                                       type);
            
                    // Encode as XML.
                    Iterator iter = info.keySet ().iterator ();
            
                    while (iter.hasNext ())
                    {
            
                        Object k = iter.next ();
            
                        Object v = info.get (k);
            
                        Element el = new Element (k.toString ());
            
                        el.addContent (v.toString ());
            
                        root.addContent (el);
            
                    }
            
                    // Get as a string.
                    String data = JDOMUtils.getElementAsString (root);
            
                    List l = new ArrayList ();
            
                    URL u = Environment.getSupportUrl (Constants.SEND_MESSAGE_TO_SUPPORT_PAGE_PROPERTY_NAME);
            
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection ();
            
                    conn.setDoInput (true);
                    conn.setDoOutput (true);
            
                    conn.connect ();
            
                    BufferedWriter bout = new BufferedWriter (new OutputStreamWriter (conn.getOutputStream ()));
            
                    bout.write (data);
                    bout.flush ();
                    bout.close ();
            
                    BufferedReader b = new BufferedReader (new InputStreamReader (conn.getInputStream ()));
            
                    String detail = b.readLine ();
            
                    b.close ();
            
                    if (((detail == null)
                          ||
                         (!detail.equals ("SUCCESS"))
                        )
                        &&
                        (!type.equals ("error"))
                       )
                    {
            
                        throw new GeneralException ("Unable to get send message to support for url: " +
                                                    u +
                                                    ", response is: " +
                                                    detail);
            
                    }                
                
                    if (onComplete != null)
                    {
                        
                        final ActionEvent ev = new ActionEvent (this,
                                                                0,
                                                                "success");
                        
                        SwingUtilities.invokeLater (new Runnable ()
                        {
                            
                            public void run ()
                            {
                                
                                onComplete.actionPerformed (ev);
                                
                            }
                            
                        });
                        
                    }
                
                } catch (Exception e) {
                    
                    if (onComplete != null)
                    {
                        
                        final ActionEvent ev = new ActionEvent (this,
                                                                0,
                                                                "error");
                        
                        SwingUtilities.invokeLater (new Runnable ()
                        {
                            
                            public void run ()
                            {
                                
                                onComplete.actionPerformed (ev);
                                
                            }
                            
                        });
                        
                    }
    
                    if (!type.equals ("error"))
                    {
                        
                        Environment.logError ("Unable to send message to support",
                                              e);
                        
                    }
                    
                }
                
            }
            
        });
        
        t.setDaemon (true);
        t.start ();

    }


    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Set<String> getResourceListing (String path)
                                           throws URISyntaxException,
                                                  GeneralException,
                                                  IOException
    {

        URL dirURL = Environment.class.getResource (path);

        if (dirURL == null)
        {

            throw new GeneralException ("Unable to find resource: " + path);

        }

        if (dirURL.getProtocol ().equals ("jar"))
        {

            /* A JAR path */
            String jarPath = dirURL.getPath ().substring (5,
                                                          dirURL.getPath ().indexOf ("!")); // strip out only the JAR file

            JarFile jar = new JarFile (URLDecoder.decode (jarPath,
                                                          "UTF-8"));

            Enumeration<JarEntry> entries = jar.entries (); // gives ALL entries in jar

            Set<String> result = new HashSet<String> (); // avoid duplicates in case it is a subdirectory

            while (entries.hasMoreElements ())
            {

                String name = "/" + entries.nextElement ().getName ();

                if (name.startsWith (path))
                { // filter according to the path

                    String entry = name.substring (path.length ());

                    if (entry.length () == 0)
                    {

                        continue;

                    }

                    int checkSubdir = entry.indexOf ("/");

                    if (checkSubdir >= 0)
                    {

                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring (0,
                                                 checkSubdir);

                    }

                    result.add (entry);
                }

            }

            return result;

        }

        throw new UnsupportedOperationException ("Cannot list files for URL " + dirURL);
    }

    public static Map<String, Set<String>> getAchievedAchievementIds (AbstractProjectViewer viewer)
    {
        
        return Environment.achievementsManager.getAchievedAchievementIds (viewer);
        
    }

    public static void removeAchievedAchievement (String                achievementType,
                                                  String                id,
                                                  AbstractProjectViewer viewer)
                                                  throws                Exception
    {
        
        Environment.achievementsManager.removeAchievedAchievement (achievementType,
                                                                   id,
                                                                   viewer);
        
    }

    public static void showAchievement (AchievementRule ar)
    {

        for (AbstractProjectViewer viewer : Environment.openProjects.values ())
        {
            
            viewer.showAchievement (ar);
            
        }
        
    }

    public static void eventOccurred (ProjectEvent ev)
    {
        
        Environment.achievementsManager.eventOccurred (ev);
        
    }

    public static Set<AchievementRule> getPerProjectAchievementRules ()
    {
        
        return Environment.achievementsManager.getPerProjectRules ();
        
    }

    public static Set<AchievementRule> getUserAchievementRules ()
    {
        
        return Environment.achievementsManager.getUserRules ();
        
    }

    public static AchievementsManager getAchievementsManager ()
    {
        
        return Environment.achievementsManager;
        
    }

    public static int getVersionAsInt (String version)
    {

        List<Integer> parts = Environment.getVersionParts (version);
        
        int mult = 100;
        
        int t = 0;
        
        for (int i = 0; i < parts.size (); i++)
        {
            
            int p = parts.get (i);
            
            t += p * mult;
                        
            mult /= 10;
            
        }
        
        return t;
                
    }
    
    public static String expandVersion (String v)
    {

        char[] chs = v.toCharArray ();
    
        int c = 0;
    
        for (int i = 0; i < chs.length; i++)
        {
        
            if (chs[i] == '.')
            {
                
                c++;
                
            }
            
        }
        
        if (c < 2)
        {
            
            for (int i = c; i < 2; i++)
            {
                
                v += ".0";
                
            }
            
        }

        return v;
        
    }
    
    public static List<Integer> getVersionParts (String v)
    {
        
        if (v == null)
        {
            
            v = "0";
            
        }
        
        v = Environment.expandVersion (v);
        
        StringTokenizer t = new StringTokenizer (v,
                                                 ".");
        
        List<Integer> parts = new ArrayList ();
        
        while (t.hasMoreTokens ())
        {
            
            parts.add (Integer.parseInt (t.nextToken ()));
            
        }
        
        return parts;
        
    }
    
    public static boolean isNewVersionGreater (String oldVersion,
                                               String newVersion)
    {

        List<Integer> oldVer = Environment.getVersionParts (oldVersion);
        List<Integer> newVer = Environment.getVersionParts (newVersion);
    
        for (int i = 0; i < newVer.size (); i++)
        {
            
            if (newVer.get (i) == oldVer.get (i))
            {
                
                continue;
                
            }
            
            if (newVer.get (i) > oldVer.get (i))
            {
                
                return true;
                
            } else {
                
                return false;
                
            }
            
        }
    
        return false;

    }

    public static boolean isInFullScreen ()
    {
        
        for (AbstractProjectViewer v : Environment.openProjects.values ())
        {
            
            if (v.isInFullScreen ())
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }

}
