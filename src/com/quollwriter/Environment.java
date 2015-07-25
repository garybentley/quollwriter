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
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.text.*;

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

import com.quollwriter.editors.ui.*;

import org.jdom.*;

public class Environment
{

    public static String GZIP_EXTENSION = ".gz";

    private static Map<Project, AbstractProjectViewer> openProjects = new HashMap ();

    public static Map defaultObjectProperties = new HashMap ();

    public static com.gentlyweb.properties.Properties userProperties = new com.gentlyweb.properties.Properties ();

    private static Logger generalLog = null;
    private static Logger errorLog = null;
    private static Logger sqlLog = null;

    // Get rid of this value once bug reporting is via the web.
    public static boolean seenReportBugMessage = false;

    private static Version appVersion = null;
    //private static String appVersion = null;
    //private static boolean betaVersion = false;
    private static int    schemaVersion = 0;

    private static SimpleDateFormat dateFormatter = null;
    private static SimpleDateFormat timeFormatter = null;

    private static boolean  debugMode = false;
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
            
    private static Map<String, SynonymProvider> synonymProviders = new WeakHashMap ();
    
    private static Map<String, String> buttonLabels = new HashMap ();
    
    private static List<PropertyChangedListener> startupProgressListeners = new ArrayList ();
    private static int startupProgress = 0;
    
    static
    {
        
        Map m = Environment.buttonLabels;
        
        m.put (Constants.CANCEL_BUTTON_LABEL_ID,
               "Cancel");
        m.put (Constants.CONFIRM_BUTTON_LABEL_ID,
               "Ok, got it");
        m.put (Constants.SAVE_BUTTON_LABEL_ID,
               "Save");
        m.put (Constants.SEND_BUTTON_LABEL_ID,
               "Send");
        m.put (Constants.UPDATE_BUTTON_LABEL_ID,
               "Update");
        
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
        
    public static void addStartupProgressListener (PropertyChangedListener l)
    {
        
        Environment.startupProgressListeners.add (l);
        
    }
    
    private static void fireStartupProgressEvent ()
    {

        PropertyChangedEvent ev = new PropertyChangedEvent (new Object (),
                                                            "progress",
                                                            0,
                                                            Environment.startupProgress);
        
        for (PropertyChangedListener l : Environment.startupProgressListeners)
        {
            
            l.propertyChanged (ev);
            
        }
        
    }
        
    public static void incrStartupProgress ()
    {
        
        if (Environment.isStartupComplete ())
        {
            
            return;
            
        }
        
        Environment.startupProgress += 9;
        
        Environment.fireStartupProgressEvent ();
        
    }
    
    public static void startupComplete ()
    {
        
        if (Environment.isStartupComplete ())
        {
            
            return;
            
        }
        
        Environment.startupProgress = 100;

        Environment.fireStartupProgressEvent ();
                
    }

    public static boolean isStartupComplete ()
    {
        
        return Environment.startupProgress == 100;
        
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
    
    public static AbstractProjectViewer getFocusedProjectViewer ()
    {

        if (Environment.openProjects.size () == 0)
        {
            
            return null;
            
        }
    
        for (AbstractProjectViewer viewer : Environment.openProjects.values ())
        {
            
            if (viewer.isFocused ())
            {
                
                return viewer;
                
            }
            
        }
        
        // Return the first viewer that is showing.
        for (AbstractProjectViewer viewer : Environment.openProjects.values ())
        {
            
            if (viewer.isShowing ())
            {
                
                return viewer;
                
            }
            
        }
        
        // What the derp... Return the first.
        return Environment.openProjects.values ().iterator ().next ();
                
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
        
    public static AbstractProjectViewer getProjectViewer (Project p)
    {
        
        return Environment.openProjects.get (p);
        
    }

    public static void removeSideBarFromAllProjectViewers (String name)
    {
        
        for (AbstractProjectViewer pv : Environment.openProjects.values ())
        {
            
            pv.removeSideBar (pv.getSideBar (name));
            
        }        
        
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

        if ((p1.getId () != null)
            &&
            (p2.getId () != null)
           )
        {
            
            return p1.equals (p2);
            
        }
        
        if (p1.getName ().equalsIgnoreCase (p2.getName ()))
        {

            return true;

        }

        return false;

    }

    public static void deleteProject (Project pr)
    {

        Environment.deleteProject (pr,
                                   null);
        
    }
    
    public static void deleteProject (Project        pr,
                                      ActionListener onDelete)
    {
        
        AbstractProjectViewer viewer = Environment.openProjects.get (pr);
        
        if (viewer != null)
        {
            
            viewer.close (true,
                          null);
            
        }
        
        // Delete the directory.
        Utils.deleteDir (pr.getProjectDirectory ());

        // Remove the project from the list.
        try
        {

            Environment.removeProjectFromProjectsFile (pr);

        } catch (Exception e)
        {

            Environment.logError ("Unable to remove project: " +
                                  pr +
                                  " from list of projects.",
                                  e);

        }

        if (onDelete != null)
        {
            
            onDelete.actionPerformed (new ActionEvent (pr, 1, "deleted"));
            
        } else {
        
            Environment.showFindOrOpenIfNoOpenProjects ();

        }
        
    }
    
    public static void showFindOrOpenIfNoOpenProjects ()
    {
        
        // Show the welcome screen if there are no projects open.
        if (Environment.getOpenProjects ().size () == 0)
        {

            FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_OPEN | FindOrOpen.SHOW_NEW);

            f.pack ();

            // Allow the underlying Windowing manager determine where to put the window.
            f.setLocationByPlatform (true);

            f.setVisible (true);

        }
        
    }
    
    public static void removeProjectFromProjectsFile (Project p)
                               throws Exception
    {

        // Remove all the project editors for the project.
        EditorsEnvironment.removeProjectEditors (p);
    
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

            if (Environment.projectsEqual (p,
                                           proj))
            {
            
                p.setLastEdited (new Date ());
                                
                // Need to do it here since when we getAllProjects we do a fresh read
                // of the projects file.
                Environment.initProjectId (p,
                                           pv.getProject ());                                
                                
            }

        }

        Environment.saveProjectsFile (projs);

        if (Environment.openProjects.size () == 0)
        {

            // Go offline from the editors service (if logged in).
            EditorsEnvironment.goOffline ();                            
        
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

    public static void addProjectToListOfProjects (Project p)
                                            throws Exception
    {
        
        Set<Project> projs = Environment.getAllProjects ();

        projs.add (p);

        Environment.saveProjectsFile (projs);
        
    }
    
    public static void addOpenedProject (AbstractProjectViewer pv)
                                  throws Exception
    {

        Environment.openProjects.put (pv.getProject (),
                                      pv);

        Environment.addProjectToListOfProjects (pv.getProject ());

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

                boolean an = newot.startsWith ("an ");
                        
                if (an)
                {
                    
                    newot = newot.substring (3);
                                
                }
                
                if (newot.endsWith ("s"))
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

                    if (Character.isUpperCase (ot.charAt (0)))
                    {

                        newot = Character.toUpperCase (newot.charAt (0)) + newot.substring (1);

                    } else
                    {

                        newot = newot.toLowerCase ();

                    }

                }

                if (an)
                {
                    
                    if (TextUtilities.isVowel (newot.toLowerCase ().charAt (0)))
                    {
                        
                        newot = "an " + newot;
                        
                    } else {
                        
                        newot = "a " + newot;
                        
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
        
        if ((p.isEditorProject ())
            &&
            (EditorsEnvironment.getEditorByEmail (p.getForEditor ().getEmail ()) == null)
           )
        {
            
            return String.format ("Unable to find {contact}: <b>%s</b> you are editing the {project} for.",
                                  p.getForEditor ().getEmail ());
            
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

                Environment.startupComplete ();

                return false;

            }

            return true;

        }

        return false;

    }
    
    public static Project getProjectByName (String name)
                                     throws Exception
    {
        
        if (name == null)
        {
            
            return null;
            
        }
        
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
    
    public static Project getProjectById (String id,
                                          String projType)
                                   throws Exception
    {
        
        if (id == null)
        {
            
            return null;
            
        }
        
        Set<Project> projs = Environment.getAllProjects ();

        for (Project p : projs)
        {
            
            String pid = p.getId ();
            
            if (pid == null)
            {
                
                continue;
                
            }
            
            if (projType != null)
            {
                
                if (!p.getType ().equals (projType))
                {
                    
                    continue;
                    
                }
                
            }
            
            if (pid.equals (id))
            {
                
                return p;
                
            }
            
        }
        
        return null;
        
    }
        
    /** For a given project, get the project version object by its id.
     *
     * @param p The project.
     * @param id The project version id.
     * @param filePassword The password for the project file if it is encrypted.
     * @return The project version, if it can be found.
     * @throws Exception If something goes wrong.
     * TODO: Move the filepassword into the project and expect it there instead.
     */
    public static ProjectVersion getProjectVersionById (Project p,
                                                        String  id,
                                                        String  filePassword)
                                                 throws Exception
    {
        
        AbstractProjectViewer pv = Environment.getProjectViewer (p);
        
        ObjectManager om = null;
        
        if (pv != null)
        {
            
            // Load up the chapters.
            om = pv.getObjectManager ();
            
        } else {
                                          
            // Open the project.
            om = Environment.getProjectObjectManager (p,
                                                      filePassword);
            
            om.getProject ();
            
        }
        
        ProjectVersionDataHandler pdh = (ProjectVersionDataHandler) om.getHandler (ProjectVersion.class);
        
        return pdh.getById (id);
        
    }
    
    /**
     * For a given project get the text/data for the versions of the chapters passed in.  This assumes that the
     * text is not already available in the chapter object, for example when you only know the id and version.
     *
     * @param p The project.
     * @param chaps The chapters to look up.
     * @param filePassword The password for the project file if it is encrypted.
     * @return The set of versioned chapters.
     * @throws If something goes wrong.
     * TODO: Move the filepassword into the project and expect it there instead.
     */
    public static Set<Chapter> getVersionedChapters (Project             p,
                                                     Collection<Chapter> chaps,
                                                     String              filePassword)
                                              throws Exception
    {
        
        AbstractProjectViewer pv = Environment.getProjectViewer (p);
        
        ObjectManager om = null;
        
        if (pv != null)
        {
            
            // Load up the chapters.
            om = pv.getObjectManager ();
            
        } else {
                                          
            // Open the project.
            om = Environment.getProjectObjectManager (p,
                                                      filePassword);
            
            om.getProject ();
            
        }
        
        ChapterDataHandler cdh = (ChapterDataHandler) om.getHandler (Chapter.class);
        
        return cdh.getVersionedChapters (chaps);
        
    }
    
    /**
     * Update the chapters to the versions provided.  This creates new chapter objects with new keys but
     * keeps the id/version in the chapter.
     *
     * @param p The project.
     * @param projVer The new project version to update to.
     * @param chaps The chapters to update.
     * @param filePassword The password for the project file if it is encrypted.
     * @return The set of versioned chapters.
     * @throws If something goes wrong.
     * TODO: Move the filepassword into the project and expect it there instead.
     */
    public static Set<Chapter> updateToNewVersions (Project             p,
                                                    ProjectVersion      projVer,
                                                    Collection<Chapter> chaps,
                                                    String              filePassword)
                                             throws Exception
    {
        
        AbstractProjectViewer pv = Environment.getProjectViewer (p);
        
        ObjectManager om = null;
        
        if (pv != null)
        {
            
            // Load up the chapters.
            om = pv.getObjectManager ();
            
        } else {
                                        
            // Open the project.
            om = Environment.getProjectObjectManager (p,
                                                      filePassword);
            
            om.getProject ();
            
        }
        
        ChapterDataHandler cdh = (ChapterDataHandler) om.getHandler (Chapter.class);
        
        // Check to see if we already have a version with the specified id.
        ProjectVersionDataHandler pvdh = (ProjectVersionDataHandler) om.getHandler (ProjectVersion.class);
        
        if (pvdh.getById (projVer.getId ()) != null)
        {
            
            // Already have this version.
            return cdh.getChaptersForVersion (projVer,
                                              om.getProject ().getBook (0),
                                              null,
                                              true);
            
        }
                
        return cdh.updateToNewVersions (projVer,
                                        chaps);
        
    }

    /**
     * Get an object manager for the specified project and init it.
     *
     * @param p The project.
     * @param filePassword Optional, the password for the project if it is encrypted.
     * @returns The object manager for the project.
     * @throws An exception if the object manager cannot be inited.
     * TODO: Move the filepassword into the project and expect it there instead.
     */
    public static ObjectManager getProjectObjectManager (Project p,
                                                         String  filePassword)
                                                  throws GeneralException
    {
        
        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);
        
        ObjectManager dBMan = new ObjectManager ();
        dBMan.init (new File (p.getProjectDirectory ().getPath (), Constants.PROJECT_DB_FILE_NAME_PREFIX),
                    username,
                    password,
                    filePassword,
                    Environment.getSchemaVersion ());
        
        return dBMan;
    
    }
    
    /**
     * Creates a completely new project with the specified name at the <b>saveDir/name</b> location.
     * If the project is to be encrypted then a <b>filePassword</b> should be supplied.  The schema will be created.
     *
     * @param saveDir The directory to save the project to.
     * @param name The name of the project.
     * @param filePassword Optional, provide if the project is to be encrypted.
     * @returns The new project object created.
     * @throws An exception if the schema cannot be created or if the save location already exists.
     */
    public static Project createNewProject (File   saveDir,
                                            String name,
                                            String filePassword)
                                     throws Exception
    {
        
        File projDir = new File (saveDir,
                                 Utils.sanitizeForFilename (name));

        if (projDir.exists ())
        {
            
            throw new IllegalArgumentException ("A project with name: " +
                                                name +
                                                " already exists at: " +
                                                projDir);
                                 
        }
        
        Project p = new Project ();
        p.setName (name);
        
        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        ObjectManager dBMan = new ObjectManager ();
        dBMan.init (new File (projDir.getPath (), Constants.PROJECT_DB_FILE_NAME_PREFIX),
                    username,
                    password,
                    filePassword,
                    0);

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (projDir);

        p.setProjectDirectory (projDir);
        p.setEncrypted (filePassword != null);

        Book b = new Book (p,
                           p.getName ());
                
        p.addBook (b);
        
        dBMan.saveObject (p,
                          null);        
        
        dBMan.closeConnectionPool ();
        
        return p;
        
    }
    
    /**
     * Creates the specified project (containing the relevant information) at the <b>saveDir/p.getName ()</b> location.
     * If the project is to be encrypted then a <b>filePassword</b> should be supplied.  The schema will be created.
     *
     * @param saveDir The directory to save the project to.
     * @param p The project.
     * @param filePassword Optional, provide if the project is to be encrypted.
     * @throws An exception if the schema cannot be created or if a project already exists at the save location.
     */
    public static void createProject (File    saveDir,
                                      Project p,
                                      String  filePassword)
                               throws Exception
    {
        
        File projDir = new File (saveDir,
                                 Utils.sanitizeForFilename (p.getName ()));

        if (projDir.exists ())
        {
            
            throw new IllegalArgumentException ("A project with name: " +
                                                p.getName () +
                                                " already exists at: " +
                                                projDir);
                                 
        }
                
        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        ObjectManager dBMan = new ObjectManager ();
        dBMan.init (new File (projDir.getPath (), Constants.PROJECT_DB_FILE_NAME_PREFIX),
                         username,
                         password,
                         filePassword,
                         0);

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (projDir);

        p.setProjectDirectory (projDir);
        p.setEncrypted (filePassword != null);        
        
        dBMan.saveObject (p,
                          null);
        
        dBMan.closeConnectionPool ();
        
    }

    public static void openObjectInProject (final Project    proj,
                                            final DataObject obj)
                                     throws Exception
    {
        
        final DataObject dobj = obj;
         
        Project p = null;
                                    
        try
        {
            
            p = Environment.getProjectById (p.getId (),
                                            p.getType ());
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to get project for id: " +
                                        proj.getId (),
                                        e);
            
        }
        
        if (p == null)
        {
            
            throw new GeneralException ("Unable to get project for id: " +
                                        proj.getId ());
            
        }
        
        final Project fp = p;
    
        Environment.openProject (p,
                                 new ActionListener ()
                                 {
                                    
                                    public void actionPerformed (ActionEvent ev)
                                    {
                                        
                                        // View the object.
                                        AbstractProjectViewer viewer = Environment.getProjectViewer (fp);
                                        
                                        viewer.viewObject (obj);
                                        
                                    }
                                    
                                 });
                                
    }
    
    public static void openProject (File f)
    {
        
        // Check the file extension
  
    }
    
    public static void openProjectWithId (String id,
                                          String projType)
                                   throws Exception
    {
        
        Project p = Environment.getProjectById (id,
                                                projType);

        if (p == null)
        {
            
            return;            
                                                
        }
        
        Environment.openProject (p);
        
    }
    
    public static void  openProject (final Project p)
                              throws Exception
    {
        
        Environment.openProject (p,
                                 null);
        
    }

    public static void  openProject (final Project        p,
                                     final ActionListener onProjectOpen)
                              throws Exception
    {

        AbstractProjectViewer pv = (AbstractProjectViewer) Environment.openProjects.get (p);

        if (pv != null)
        {

            pv.setVisible (true);
            pv.setState (java.awt.Frame.NORMAL);
            pv.toFront ();
            
            if (onProjectOpen != null)
            {
                
                onProjectOpen.actionPerformed (new ActionEvent ("open", 1, "open"));
                
            }

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

            if (onProjectOpen != null)
            {
                
                pv.addProjectEventListener (new ProjectEventListener ()
                {
                   
                    @Override
                    public void eventOccurred (ProjectEvent ev)
                    {
                        
                        if ((ev.getType ().equals (Project.OBJECT_TYPE))
                            &&
                            (ev.getAction ().equals (ProjectEvent.OPEN))
                           )
                        {
                        
                            try
                            {
                            
                                onProjectOpen.actionPerformed (new ActionEvent (fpv, 1, "open"));
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to perform action after project open",
                                                      e);
                                
                            }

                        }
                        
                    }
                    
                });
                
            }
            
            Environment.incrStartupProgress ();
            
            String password = p.getFilePassword ();

            if ((p.isEncrypted ()) &&
                (password == null))
            {

                Environment.startupComplete ();

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
                       
            Environment.startupComplete ();

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

    public static boolean isDebugModeEnabled ()
    {
        
        return Environment.debugMode;
        
    }
    
    public static void setDebugModeEnabled (boolean v)
    {
        
        Environment.debugMode = v;
        
        // TODO: Maybe have an EnvironmentEvent?
        
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

    private static void initProjectId (Project envProj,
                                       Project realProj)
    {
        
        if ((envProj.getId () == null)
            &&
            (realProj.getId () != null)
           )
        {
            
            envProj.setId (realProj.getId ());
            
        }
        
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

            return new EditorProjectViewer ();

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
        
        if (!id.startsWith (Constants.BUTTON_LABEL_ID_PREFIX))
        {
            
            return id;
            
        }
        
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

            Project p = null;
            
            try
            {
                
                p = new Project (pEl);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to convert element: " +
                                      JDOMUtils.getPath (pEl) +
                                      " to a project",
                                      e);
                
                continue;
                
            }

            ret.add (p);

        }

        return ret;

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
    
    public static boolean getPropertyAsBoolean (String name)
    {
        
        return Environment.userProperties.getPropertyAsBoolean (name);
        
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
/*
    public static File getDictionaryDirectory (String lang)
    {
        
        return new File (Environment.getUserQuollWriterDir ().getPath () + Constants.DICTIONARIES_DIR + lang);
    
    }
  */  
    public static File getDictionaryFile (String lang)
    {
        
        return Environment.getUserFile (Constants.DICTIONARIES_DIR + lang + ".zip");
    
    }

    public static boolean hasSynonymsDirectory (String lang)
    {
        
        File f = Environment.getUserFile (Constants.THESAURUS_DIR + lang);
        
        return (f.exists () && f.isDirectory ());
    
    }

    public static File getLogDir ()
    {

        File d = Environment.getUserFile (Constants.LOGS_DIR);

        d.mkdirs ();

        return d;

    }

    public static File getUserFile (String name)
    {
        
        return new File (Environment.getUserQuollWriterDir ().getPath (), name);
        
    }
    
    public static File getUserObjectTypeNamesFile ()
    {
        
        return Environment.getUserFile (Constants.OBJECT_TYPE_NAMES_FILE_NAME);

    }
    
    public static File getUserPropertiesFile ()
    {

        return Environment.getUserFile (Constants.PROPERTIES_FILE_NAME);

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

    public static void setUserProperty (String  name,
                                        boolean value)
                                 throws Exception
    {
        
        Environment.userProperties.setProperty (name,
                                                new BooleanProperty (name,
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
        
        if (d == null)
        {
            
            return null;
            
        }
        
        try
        {
            
            return Environment.dateFormatter.parse (d);
        
        } catch (Exception e) {
            
            // Bugger
            Environment.logError ("Unable to parse date: " +
                                  d,
                                  e);
            
        }
        
        return null;
        
    }
    
    public static String formatDateTime (Date d)
    {
        
        return Environment.formatDate (d) + " " + Environment.formatTime (d);
        
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

        Environment.dateFormatter = new SimpleDateFormat ("d MMM yyyy");
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
        
        Environment.appVersion = new Version (Environment.getResourceFileAsString (Constants.VERSION_FILE).trim ());
        
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

        Environment.incrStartupProgress ();

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

        Environment.incrStartupProgress ();

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

        Environment.incrStartupProgress ();

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

        Environment.incrStartupProgress ();
                                            
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
/*
                UIUtils.getFontsComboBox (null,
                                          null);
*/
            }

        });

        t.setDaemon (true);
        t.setPriority (Thread.MIN_PRIORITY);

        t.start ();

        Environment.incrStartupProgress ();
        
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
        
        try
        {
        
            EditorsEnvironment.init ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to init editors environment",
                                  e);
            
        }
        
        Environment.addStartupProgressListener (new PropertyChangedListener ()
        {
           
            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (Environment.isStartupComplete ())
                {

                    // See if we should be doing a warmup exercise.
                    if (Environment.userProperties.getPropertyAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME))
                    {
            
                        UIUtils.doLater (new ActionListener ()
                        {
                            
                            public void actionPerformed (ActionEvent ev)
                            {            
            
                                AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                            
                                if (viewer != null)
                                {
                            
                                    viewer.showWarmupPromptSelect ();
                                
                                    viewer.fireProjectEvent (Warmup.OBJECT_TYPE,
                                                             ProjectEvent.WARMUP_ON_STARTUP);
            
                                }
                                                             
                            }
            
                        });
            
                    }        
                
                }
                
            }
            
        });
        
    }

    public static int getJavaVersion ()
    {
        
        String[] els = System.getProperty("java.version").split("\\.");
        
        return Integer.parseInt (els[1]);
        
    }
    
    public static String getQuollWriterWebsiteLink (String url,
                                                    String linkText)
    {
        
        if (linkText == null)
        {
            
            return String.format ("%s:%s",
                                  Constants.QUOLLWRITER_PROTOCOL,
                                  url);
            
        }
        
        return String.format ("<a href='%s:%s'>%s</a>",
                              Constants.QUOLLWRITER_PROTOCOL,
                              url,
                              linkText);
        
    }
    
    public static String getQuollWriterHelpLink (String url,
                                                 String linkText)
    {

        if (linkText == null)
        {
            
            return String.format ("%s:%s",
                                  Constants.HELP_PROTOCOL,
                                  url);
            
        }
    
        return String.format ("<a href='%s:%s'>%s</a>",
                              Constants.HELP_PROTOCOL,
                              url,
                              linkText);
        
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
        
        return Constants.ENGLISH.equalsIgnoreCase (language)
               ||
               Constants.BRITISH_ENGLISH.equalsIgnoreCase (language)
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

        return Environment.getSupportUrl (pagePropertyName,
                                          null);
    
    }
    
    public static URL getSupportUrl (String pagePropertyName,
                                     String parms)
                                     throws Exception
    {
                
        String prefName = Constants.SUPPORT_URL_BASE_PROPERTY_NAME;
        
        if (Environment.debugMode)
        {
            
            prefName = Constants.DEBUG_SUPPORT_URL_BASE_PROPERTY_NAME;
            
        }
                
        return new URL (Environment.getProperty (prefName) + Environment.getProperty (pagePropertyName) + (parms != null ? parms : ""));
        
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
        
            if (type == Constants.ICON_EDITOR_MESSAGE)
            {
                
                size = 20;
                
            }
        
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
                
            if (type == Constants.ICON_EDITORS_LIST_TAB_HEADER)
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

    public static ImageIcon getTypingIcon ()
    {

        URL url = Environment.class.getResource (Constants.IMGS_DIR + Constants.TYPING_GIF_NAME);

        if (url == null)
        {

            if (Environment.debugMode)
            {

                // Can't find image, log the problem but keep going.
                Environment.logError ("Unable to find loading image: " +
                                      Constants.TYPING_GIF_NAME +
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
                                  Constants.TYPING_GIF_NAME,
                                  e);
            
        }

        return null;        
        
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
    
        if (name == null)
        {
        
            return null;
            
        }
        
        if (name.equals (Constants.LOADING_GIF_NAME))
        {
            
            return Environment.getLoadingIcon ();
            
        }
    
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

    public static BufferedImage getNoEditorAvatarImage ()
    {
        
        return Environment.getImage (Constants.EDITOR_NO_AVATAR_IMAGE);
        
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

    public static Version getQuollWriterVersion ()
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
        
    public static URL getUpgradeURL (Version version)
                              throws Exception
    {

        String parms = "?version=" + version.getVersion ();
                
        return Environment.getSupportUrl (Constants.GET_UPGRADE_FILE_PAGE_PROPERTY_NAME,
                                          parms);
        
    }
    
    private static URL getNewsAndVersionCheckURL ()
                                           throws Exception
    {

        String parms = "?";
        
        if (Environment.userProperties.getPropertyAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME))
        {
            
            parms += "beta=true&";
            
        }
    
        String lastVersionCheckTime = Environment.userProperties.getProperty (Constants.LAST_VERSION_CHECK_TIME_PROPERTY_NAME);
        
        if (lastVersionCheckTime != null)
        {
            
            parms += "since=" + lastVersionCheckTime;
            
        }
    
        return Environment.getSupportUrl (Constants.GET_LATEST_VERSION_PAGE_PROPERTY_NAME,
                                          parms);
        
    }
    
    public static void doNewsAndVersionCheck (final AbstractProjectViewer pv)
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

                        URL u = Environment.getNewsAndVersionCheckURL ();

                        HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

                        conn.setDoInput (true);
                        conn.setDoOutput (true);

                        conn.connect ();

                        BufferedInputStream bin = new BufferedInputStream (conn.getInputStream ());
                        
                        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
                        
                        IOUtils.streamTo (bin,
                                          bout,
                                          8192);
                        
                        Environment.setUserProperty (Constants.LAST_VERSION_CHECK_TIME_PROPERTY_NAME,
                                                     String.valueOf (System.currentTimeMillis ()));                        
                        
                        String info = new String (bout.toByteArray (),
                                                  "utf-8");
                        
                        // Should be json.
                        Map data = (Map) JSONDecoder.decode (info);
                        
                        Map version = (Map) data.get ("version");

                        if (version != null)
                        {
                            
                            final Version newVersion = new Version ((String) version.get ("version"));
                                                        
                            final long size = ((Number) version.get ("size")).longValue ();

                            final String digest = (String) version.get ("digest");

                            if (Environment.getQuollWriterVersion ().isNewer (newVersion))
                            {

                                UIUtils.doLater (new ActionListener ()
                                {

                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        Box ib = new Box (BoxLayout.Y_AXIS);
                                        ib.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                          Short.MAX_VALUE));

                                        JTextPane p = UIUtils.createHelpTextPane (String.format ("A new version of %s is available.  <a href='help:version-changes/%s'>View the changes.</a>",
                                                                                                 Constants.QUOLL_WRITER_NAME,
                                                                                                 newVersion.getVersion ().replace (".",
                                                                                                                                   "_")),
                                                                                  pv);
                                        p.setAlignmentX (Component.LEFT_ALIGNMENT);

                                        p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                         Short.MAX_VALUE));
                                        p.setBorder (null);

                                        ib.add (p);
                                        ib.add (Box.createVerticalStrut (5));

                                        JButton installNow = new JButton ("Install now");
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
                                                                      digest).start ();

                                            }

                                        });

                                        installLater.addActionListener (removeNot);

                                    }

                                });

                            }
                            
                        }
                        
                        // Get the news.
                        List news = (List) data.get ("news");
                        
                        if (news != null)
                        {
                            
                            final Set<String> seenIds = new HashSet ();
                            
                            String seenNewsIds = Environment.userProperties.getProperty (Constants.SEEN_NEWS_IDS_PROPERTY_NAME);
                            
                            if (seenNewsIds == null)
                            {
                                
                                seenNewsIds = "";
                                
                            }
                            
                            StringTokenizer t = new StringTokenizer (seenNewsIds,
                                                                     ",");
                            
                            while (t.hasMoreTokens ())
                            {
                                
                                seenIds.add (t.nextToken ().trim ());
                                
                            }
                            
                            for (int i = 0; i < news.size (); i++)
                            {
                                
                                Map ndata = (Map) news.get (i);
                                
                                final String id = (String) ndata.get ("id");
                                
                                if (seenIds.contains (id))
                                {
                                    
                                    continue;
                                    
                                }
                                
                                String m = (String) ndata.get ("message");
                            
                                Box ib = new Box (BoxLayout.Y_AXIS);
                                ib.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                  Short.MAX_VALUE));

                                JTextPane p = UIUtils.createHelpTextPane (m,
                                                                          pv);
                                p.setAlignmentX (Component.LEFT_ALIGNMENT);

                                p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                 Short.MAX_VALUE));
                                p.setBorder (null);

                                ib.add (p);
                                ib.add (Box.createVerticalStrut (5));                                

                                JButton ok = new JButton ("Ok, got it");

                                Box bb = new Box (BoxLayout.X_AXIS);
                                bb.add (ok);

                                ib.add (bb);
                                bb.setAlignmentX (Component.LEFT_ALIGNMENT);
                                
                                final ActionListener removeNot = pv.addNotification (ib,
                                                                                     "notify",
                                                                                     -1);

                                ok.addActionListener (new ActionAdapter ()
                                {

                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        seenIds.add (id);
                                        
                                        StringBuilder sb = new StringBuilder ();
                                        
                                        for (String s : seenIds)
                                        {
                                            
                                            if (sb.length () > 0)
                                            {
                                                
                                                sb.append (",");
                                                
                                            }
                                            
                                            sb.append (s);
                                            
                                        }
                                    
                                        try
                                        {
                                    
                                            Environment.setUserProperty (Constants.SEEN_NEWS_IDS_PROPERTY_NAME,
                                                                         sb.toString ());
                                            
                                        } catch (Exception e) {
                                            
                                            Environment.logError ("Unable to update seen news ids to: " +
                                                                  sb,
                                                                  e);
                                            
                                        }
                                    
                                        removeNot.actionPerformed (ev);

                                    }

                                });
                            
                            }
                            
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
                              Environment.getQuollWriterVersion ().toString ());
                    info.put ("beta",
                              Environment.appVersion.isBeta ());
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
                                       Environment.getQuollWriterVersion ().toString ());
                    root.setAttribute ("beta",
                                       String.valueOf (Environment.appVersion.isBeta ()));
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
                        
                        UIUtils.doLater (onComplete);
                        
                    }
                
                } catch (Exception e) {
                    
                    if (onComplete != null)
                    {
                        
                        final ActionEvent ev = new ActionEvent (this,
                                                                0,
                                                                "error");
                        
                        UIUtils.doLater (onComplete);
                        
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
    
    public static AbstractProjectViewer getFullScreenProjectViewer ()
    {
        
        for (AbstractProjectViewer v : Environment.openProjects.values ())
        {
            
            if (v.isInFullScreen ())
            {
                
                return v;
                
            }
            
        }

        return null;
        
    }
    
    public static boolean isInFullScreen ()
    {
        
        return Environment.getFullScreenProjectViewer () != null;
        
    }

    public static void doForOpenProjects (String              projectType,
                                          ProjectViewerAction act)
    {

        for (Project p : Environment.openProjects.keySet ())
        {
    
            AbstractProjectViewer pv = Environment.openProjects.get (p);
    
            if (p.getType ().equals (projectType))
            {
                                
                act.doAction (pv);
                
            }
            
        }
        
    }
    
    public static void doForOpenProjectViewers (Class               projectViewerType,
                                                ProjectViewerAction act)
    {

        for (Project p : Environment.openProjects.keySet ())
        {
    
            AbstractProjectViewer pv = Environment.openProjects.get (p);
    
            if (projectViewerType.isAssignableFrom (pv.getClass ()))
            {
                                
                act.doAction (pv);
                
            }
            
        }        
        
    }

    public static String formatObjectToStringProperties (DataObject d)
    {
        
        Map<String, Object> props = new LinkedHashMap ();
        
        d.fillToStringProperties (props);        
        
        return Environment.formatObjectToStringProperties (props);
        
    }
    
    public static String formatObjectToStringProperties (Map<String, Object> props)
    {
        
        try
        {
        
            return JSONEncoder.encode (props,
                                       true,
                                       "");
        
        } catch (Exception e) {
            
            Environment.logError ("Unable to encode properties: " +
                                  props,
                                  e);
        
            return props + "";
                
        }
        
    }
    
}
