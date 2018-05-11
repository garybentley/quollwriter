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

import java.nio.channels.*;

import java.beans.*;

import java.io.*;
import java.nio.charset.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.*;
import java.util.jar.*;
import java.util.logging.*;
import java.util.prefs.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

import javax.imageio.*;

import javax.sound.sampled.*;

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

import static com.quollwriter.LanguageStrings.*;

import org.jdom.*;

public class Environment
{

    public static String GZIP_EXTENSION = ".gz";

    private static Landing landingViewer = null;
    private static Map<ProjectInfo, AbstractProjectViewer> openProjects = new HashMap<> ();
    private static Set<AbstractViewer> openViewers = new HashSet<> ();

    public static Map defaultObjectProperties = new HashMap ();

    //public static com.gentlyweb.properties.Properties userProperties = new com.gentlyweb.properties.Properties ();

    private static Logger generalLog = null;
    private static Logger errorLog = null;
    private static Logger sqlLog = null;

    // Get rid of this value once bug reporting is via the web.
    public static boolean seenReportBugMessage = false;

    private static Version appVersion = null;
    //private static String appVersion = null;
    //private static boolean betaVersion = false;
    private static int    schemaVersion = 0;
    private static int    projectInfoSchemaVersion = 0;

    private static SimpleDateFormat dateFormatter = null;
    private static SimpleDateFormat timeFormatter = null;

    private static boolean  debugMode = false;
    private static boolean doneVersionCheck = false;
    public static boolean  isWindows = false;
    public static boolean  isMac = false;
    public static boolean  isLinux = false;
    private static boolean isFirstUse = false;

    private static List<File> installJarFilesToDelete = new ArrayList ();

    private static Map<String, UserPropertyHandler> userPropertyHandlers = new HashMap ();

    private static DecimalFormat numFormat = new DecimalFormat ("###,###");
    private static DecimalFormat floatNumFormat = new DecimalFormat ("###,###.#");

    private static Map<String, Image> backgroundImages = new HashMap ();

    private static AchievementsManager achievementsManager = null;

    private static Map<String, String> objectTypeNamesSingular = new HashMap ();
    private static Map<String, String> objectTypeNamesPlural = new HashMap ();

    private static Map<String, SynonymProvider> synonymProviders = new WeakHashMap ();
    //private static DictionaryProvider defaultDictProv = null;

    private static List<PropertyChangedListener> startupProgressListeners = new ArrayList ();
    private static int startupProgress = 0;

    private static FileLock lock = null;

    private static ProjectInfoObjectManager projectInfoManager = null;

    private static Map<ProjectInfoChangedListener, Object> projectInfoChangedListeners = null;

    private static Map<ProjectEventListener, Object> userProjectEventListeners = null;

    // Just used in the maps above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();

    private static boolean playSoundOnKeyStroke = false;
    private static Clip keyStrokeSound = null;

    private static UserSession userSession = null;
    private static TargetsData targets = null;

    private static ScheduledThreadPoolExecutor generalTimer = null;

    private static ProjectTextProperties projectTextProps = null;
    private static FullScreenTextProperties fullScreenTextProps = null;

    private static List<Runnable> doOnShutdown = new ArrayList ();

    private static Set<UserConfigurableObjectType> userConfigObjTypes = new HashSet ();

    private static PropertyChangedListener userConfigurableObjectTypeNameListener = null;

    private static Set<Tag> tags = null;

    private static LanguageStrings uiLanguageStrings = null;
    private static LanguageStrings defaultUILanguageStrings = null;

    static
    {

        // We use a synchronized weak hash map here so that we don't have to worry about all the
        // references since they will be transient compared to the potential length of the service
        // running.

        // Where possible listeners should de-register as normal but this just ensure that objects
        // that don't have a controlled pre-defined lifecycle (as opposed say to AbstractSideBar)
        // won't leak.
        Environment.projectInfoChangedListeners = Collections.synchronizedMap (new WeakHashMap ());

        Environment.userProjectEventListeners = Collections.synchronizedMap (new WeakHashMap ());

    }

    public class XMLConstants
    {

        public static final String projects = "projects";
        public static final String type = "type";
        public static final String name = "name";
        public static final String object = "object";
        public static final String singular = "singular";
        public static final String plural = "plural";
        public static final String file = "file";
        public static final String files = "files";
        public static final String stats = "stats";
        public static final String stat = "stat";
        public static final String id = "id";

    }

    public static void fireProjectInfoChangedEvent (final ProjectInfo proj,
                                                    final String      changeType)
    {

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent aev)
            {

                ProjectInfoChangedEvent ev = new ProjectInfoChangedEvent (proj,
                                                                          changeType);

                Set<ProjectInfoChangedListener> ls = null;

                // Get a copy of the current valid listeners.
                synchronized (Environment.projectInfoChangedListeners)
                {

                    ls = new LinkedHashSet (Environment.projectInfoChangedListeners.keySet ());

                }

                for (ProjectInfoChangedListener l : ls)
                {

                    l.projectInfoChanged (ev);

                }

            }

        });

    }

    public static void unregisterViewer (AbstractViewer v,
                                         ActionListener afterUnregister)
    {

        Environment.openViewers.remove (v);

        if (v == Environment.landingViewer)
        {

            Environment.landingViewer = null;

        }

        if (afterUnregister != null)
        {

            UIUtils.doLater (afterUnregister);

            return;

        }

        if (Environment.openViewers.size () == 0)
        {

            Environment.closeDown ();

        }

    }

    public static void registerViewer (AbstractViewer v)
    {

        Environment.openViewers.add (v);

    }

    public static void removeProjectInfoChangedListener (ProjectInfoChangedListener l)
    {

        Environment.projectInfoChangedListeners.remove (l);

    }

    public static void addProjectInfoChangedListener (ProjectInfoChangedListener l)
    {

        Environment.projectInfoChangedListeners.put (l,
                                                     Environment.listenerFillObj);

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

    public static AbstractViewer getFocusedViewer ()
    {

        if (Environment.landingViewer != null)
        {

            if (Environment.landingViewer.isFocused ())
            {

                return Environment.landingViewer;

            }

        }

        if (Environment.openViewers.size () == 0)
        {

            return null;

        }

        for (AbstractViewer viewer : Environment.openViewers)
        {

            if (viewer.isFocused ())
            {

                return viewer;

            }

        }

        // Return the first viewer that is showing.
        for (AbstractViewer viewer : Environment.openViewers)
        {

            if (viewer.isShowing ())
            {

                return viewer;

            }

        }

        // What the derp... Return the first.
        return Environment.openViewers.iterator ().next ();

    }

    public static String getUrlFileAsString (URL    url)
                                             throws Exception
    {

        URLConnection c = url.openConnection ();

        InputStream bin = c.getInputStream ();

        return Utils.getStreamAsString (bin,
                                        StandardCharsets.UTF_8);

    }

    public static AbstractProjectViewer getProjectViewer (ProjectInfo p)
    {

        return Environment.openProjects.get (p);

    }

    public static AbstractProjectViewer getProjectViewer (Project p)
    {

        return Environment.openProjects.get (Environment.getProjectInfo (p));

    }

    public static void removeSideBarFromAllProjectViewers (String id)
    {

        for (AbstractProjectViewer pv : Environment.openProjects.values ())
        {

            pv.removeSideBar (pv.getSideBar (id));

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

            ch.setText (new StringWithMarkup (text));

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

    public static void updateUserObjectTypeNames (Map<String, String> singular,
                                                  Map<String, String> plural)
                                           throws Exception
    {

        //Map<String, String> newSingular = new HashMap ();
        //newSingular.putAll (Environment.objectTypeNamesSingular);

        //newSingular.putAll (singular);

        //Map<String, String> newPlural = new HashMap ();
        //newPlural.putAll (Environment.objectTypeNamesPlural);

        //newPlural.putAll (plural);

        UserConfigurableObjectType type = Environment.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);

        // TODO: Fix this nonsense...
        if (singular.containsKey (Chapter.OBJECT_TYPE))
        {

            type.setObjectTypeName (singular.get (Chapter.OBJECT_TYPE));

        }

        if (plural.containsKey (Chapter.OBJECT_TYPE))
        {

            type.setObjectTypeNamePlural (plural.get (Chapter.OBJECT_TYPE));

        }

        Environment.updateUserConfigurableObjectType (type);

        Environment.setUserObjectTypeNames (singular,
                                            plural);


    }

    public static void setUserObjectTypeNames (Map<String, String> singular,
                                               Map<String, String> plural)
                                               throws Exception
    {

        Map<String, Map<String, String>> t = new HashMap ();

        t.put (LanguageStrings.singular,
               singular);
        t.put (LanguageStrings.plural,
               plural);

        IOUtils.writeStringToFile (Environment.getUserObjectTypeNamesFile (),
                                   JSONEncoder.encode (t),
                                   false);

        Environment.loadUserObjectTypeNames ();

    /*
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
*/
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

        if (t instanceof UserConfigurableObject)
        {

            UserConfigurableObject ut = (UserConfigurableObject) t;

            return ut.getObjectTypeName ();

        }

        if (t instanceof Note)
        {

            Note n = (Note) t;

            if (n.isEditNeeded ())
            {

                return Environment.getObjectTypeName ("editneeded" + t.getObjectType ());

            }

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

        if (t.equals ("qw"))
        {

            return Constants.QUOLL_WRITER_NAME;

        }

        String v = Environment.objectTypeNamesSingular.get (t);

        if (v == null)
        {

            v = Environment.getUIString (LanguageStrings.objectnames,
                                         LanguageStrings.singular,
                                         t);

        }

        return v;

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

        if (t instanceof UserConfigurableObjectType)
        {

            UserConfigurableObjectType ut = (UserConfigurableObjectType) t;

            if (ut.isLegacyObjectType ())
            {

                return Environment.getObjectTypeNamePlural (ut.getUserObjectType ());

            }

            return ut.getObjectTypeNamePlural ();

        }

        if ((t instanceof UserConfigurableObject)
            &&
            (!(t instanceof LegacyUserConfigurableObject))
           )
        {

            UserConfigurableObject ut = (UserConfigurableObject) t;

            return ut.getObjectTypePluralName ();

        }

        if (t instanceof Note)
        {

            Note n = (Note) t;

            if (n.isEditNeeded ())
            {

                return Environment.getObjectTypeNamePlural ("editneeded" + t.getObjectType ());

            }

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

        String v = Environment.objectTypeNamesPlural.get (t);

        if (v == null)
        {

            v = Environment.getUIString (LanguageStrings.objectnames,
                                         LanguageStrings.plural,
                                         t);

        }

        return v;

        //return Environment.objectTypeNamesPlural.get (t);

    }
/*
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
*/
    public static Map<ProjectInfo, AbstractProjectViewer> getOpenProjects ()
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

    public static void deleteProject (Project        p,
                                      ActionListener onDelete)
    {

        ProjectInfo pi = Environment.getProjectInfo (p.getId (),
                                                     p.getType ());

        if (pi == null)
        {

            return;

        }

        Environment.deleteProject (pi,
                                   onDelete);

    }

    public static void deleteProject (final ProjectInfo pr,
                                      final ActionListener onDelete)
    {

        AbstractProjectViewer viewer = Environment.openProjects.get (pr);

        ActionListener onClose = new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                // There is probably now (because of h2) a "projectdb.lobs.db" directory.
                // Add a can delete file to it.
                try
                {

                    Utils.createQuollWriterDirFile (new File (pr.getProjectDirectory ().getPath () + "/projectdb.lobs.db"));

                } catch (Exception e)
                {

                    // Ignore for now.
                    Environment.logError ("Unable to add can delete dir file to: " +
                                          pr.getProjectDirectory ().getPath () + "/projectdb.lobs.db",
                                          e);

                }

                // Delete the backup directory.
                Utils.deleteDir (pr.getBackupDirectory ());

                // Delete the files directory.
                Utils.deleteDir (pr.getFilesDirectory ());

                // Delete the directory.
                Utils.deleteDir (pr.getProjectDirectory ());

                // Remove the project from the list.
                try
                {

                    Environment.projectInfoManager.deleteObject (pr,
                                                                 false,
                                                                 null);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to delete project: " +
                                          pr,
                                          e);

                }

                Environment.fireProjectInfoChangedEvent (pr,
                                                         ProjectInfoChangedEvent.DELETED);

                if (onDelete != null)
                {

                    onDelete.actionPerformed (new ActionEvent (pr, 1, "deleted"));

                } else {

                    Environment.showLandingIfNoOpenProjects ();

                }

            }


        };

        if (viewer != null)
        {

            viewer.close (true,
                          onClose);

        } else {

            UIUtils.doLater (onClose);

        }

    }

    public static void deleteProject (Project pr)
    {

        if (pr == null)
        {

            return;

        }

        ProjectInfo p = Environment.getProjectInfo (pr);

        if (p == null)
        {

            return;

        }

        Environment.deleteProject (p,
                                   null);

    }

    public static ProjectInfo getProjectInfo (Project p)
    {

        ProjectInfo pi =  Environment.getProjectInfo (p.getId (),
                                                      p.getType ());

        if (pi != null)
        {

            pi.setFilePassword (p.getFilePassword ());

        }

        return pi;

    }

    public static ProjectInfo getProjectInfo (String id,
                                              String type)
    {

        try
        {

            for (ProjectInfo p : Environment.getAllProjectInfos ())
            {

                if (p.getId ().equals (id))
                {

                    if (type == null)
                    {

                        return p;

                    }

                    if (type.equals (p.getType ()))
                    {

                        return p;

                    }

                }

            }

        } catch (Exception e) {

            Environment.logError ("Unable to get all project infos to check for project: " +
                                  id +
                                  ", type: " +
                                  type,
                                  e);

        }

        return null;

    }

    public static void showLandingIfNoOpenProjects ()
    {

        // Show the welcome screen if there are no projects open.
        if (Environment.getOpenProjects ().size () == 0)
        {

            Environment.showLanding ();

        }

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

    public static void closeDown ()
    {

        if (Environment.openViewers.size () > 0)
        {

            throw new IllegalStateException ("Cannot closedown when there are open viewers.");

        }

        Environment.generalTimer.shutdown ();

        // Go offline from the editors service (if logged in).
        EditorsEnvironment.closeDown ();

        Environment.userSession.end (new Date ());

        try
        {

            Environment.projectInfoManager.addSession (Environment.userSession);

        } catch (Exception e) {

            Environment.logError ("Unable to add session",
                                  e);

        }

        Environment.projectInfoManager.closeConnectionPool ();

        if (Environment.doOnShutdown.size () > 0)
        {

            for (Runnable r : Environment.doOnShutdown)
            {

                try
                {

                    r.run ();

                } catch (Exception e) {

                    Environment.logError ("Unable to run on shutdown.",
                                          e);

                }

            }

        }

        System.exit (0);

    }

    /**
     * Inform the environment about a project closing.
     *
     * If <b>onClose</b> is provided then it is assumed that the caller
     * is doing something after the project has been deregistered with the
     * environment, for example opening another project or window.
     *
     * If <b>onClose</b> is not provided (null) then a check is made to see if
     * the projects window should be shown or should shutdown occur because
     * there are no projects open.
     *
     * @param pv The project viewer being closed.
     * @param onClose The action to take once the project is deregistered.
     * @throws Exception If something goes wrong (the list is long).
     */
    public static void projectClosed (AbstractProjectViewer pv,
                                      boolean               tryShowLanding)
                               throws Exception
    {

        Project proj = pv.getProject ();

        ProjectInfo p = Environment.getProjectInfo (pv.getProject ().getId (),
                                                    pv.getProject ().getType ());

        if (p != null)
        {

            Object r = Environment.openProjects.remove (p);

        }

        Environment.userSession.updateCurrentSessionWordCount (pv.getSessionWordCount ());

        if ((tryShowLanding)
            &&
            (UserProperties.getAsBoolean (Constants.SHOW_PROJECTS_WINDOW_WHEN_NO_OPEN_PROJECTS_PROPERTY_NAME))
           )
        {

            Environment.showLandingIfNoOpenProjects ();

        }

    }

    public static void addOpenedProject (AbstractProjectViewer pv)
                                  throws Exception
    {

        final Project proj = pv.getProject ();

        ProjectInfo p = Environment.getProjectInfo (proj.getId (),
                                                    proj.getType ());

        if (p == null)
        {

            // We don't have this project, so add it.
            p = new ProjectInfo (proj);

            try
            {

                Environment.projectInfoManager.saveObject (p,
                                                           null);

            } catch (Exception e) {

                Environment.logError ("Unable to add new project info for project: " +
                                      proj,
                                      e);

            }

        } else {

            p.setProject (proj);

        }

        Environment.openProjects.put (p,
                                      pv);

    }

    public static void updateProjectInfo (ProjectInfo pi)
                                   throws GeneralException
    {

        Environment.projectInfoManager.saveObject (pi,
                                                   null);

        Environment.fireProjectInfoChangedEvent (pi,
                                                 ProjectInfoChangedEvent.CHANGED);

    }

    public static void updateProjectInfos (List<ProjectInfo> pis)
                                    throws GeneralException
    {

        Environment.projectInfoManager.saveObjects (pis,
                                                    null);

        for (ProjectInfo pi : pis)
        {

            Environment.fireProjectInfoChangedEvent (pi,
                                                     ProjectInfoChangedEvent.CHANGED);

        }

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

                if (newot.equals ("qw"))
                {

                    newot = Constants.QUOLL_WRITER_NAME;

                }

/*
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
*/
                b.replace (start,
                           end + 1,
                           newot);

                start += newot.length ();

            } else {

                start++;

            }

            start = b.indexOf ("{",
                               start);

        }

        return b.toString ();

    }

    public static String canOpenProject (Project p)
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.openproject);
        prefix.add (LanguageStrings.openerrors);

        if (p == null)
        {

            return Environment.getUIString (prefix,
                                            LanguageStrings.projectnotexist);
                                            //"{Project} does not exist.";

        }

        if (!p.getProjectDirectory ().exists ())
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.projectdirnotexist),
                                  p.getProjectDirectory ());
                                            //"Cannot find {project} directory <b>" + p.getProjectDirectory () + "</b>.";

        }

        if (!p.getProjectDirectory ().isDirectory ())
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.projectdirisfile),
                                  p.getProjectDirectory ());
            //"Path to {project} <b>" + p.getProjectDirectory () + "</b> is a file, but a directory is expected.";

        }

        if (!Utils.getQuollWriterDirFile (p.getProjectDirectory ()).exists ())
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.invalidprojectdir),
                                  p.getProjectDirectory ());
            //"{Project} directory <b>" + p.getProjectDirectory () + "</b> doesn't appear to be a valid Quoll Writer {project}.";

        }

        if ((p.isEditorProject ())
            &&
            (EditorsEnvironment.getEditorByEmail (p.getForEditor ().getEmail ()) == null)
           )
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.cantfindeditor),
                                  //"Unable to find {contact}: <b>%s</b> you are editing the {project} for.",
                                  p.getForEditor ().getEmail ());

        }

        return null;

    }

    public static String canOpenProject (ProjectInfo p)
    {

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.openproject);
        prefix.add (LanguageStrings.openerrors);

        if (p == null)
        {

            return Environment.getUIString (prefix,
                                            LanguageStrings.projectnotexist);
            //return "{Project} does not exist.";

        }

        if (!p.getProjectDirectory ().exists ())
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.projectdirnotexist),
                                  p.getProjectDirectory ());
            //return "Cannot find {project} directory <b>" + p.getProjectDirectory () + "</b>.";

        }

        if (!p.getProjectDirectory ().isDirectory ())
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.projectdirisfile),
                                  p.getProjectDirectory ());
            //return "Path to {project} <b>" + p.getProjectDirectory () + "</b> is a file, but a directory is expected.";

        }

        if (!Utils.getQuollWriterDirFile (p.getProjectDirectory ()).exists ())
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.invalidprojectdir),
                                  p.getProjectDirectory ());
            //return "{Project} directory <b>" + p.getProjectDirectory () + "</b> doesn't appear to be a valid Quoll Writer {project}.";

        }

        if ((p.isEditorProject ())
            &&
            (p.getForEditor () != null)
            &&
            (EditorsEnvironment.getEditorByEmail (p.getForEditor ().getEmail ()) == null)
           )
        {

            return String.format (Environment.getUIString (prefix,
                                                           LanguageStrings.cantfindeditor),
                                  //"Unable to find {contact}: <b>%s</b> you are editing the {project} for.",
                                  p.getForEditor ().getEmail ());

        }

        return null;

    }

    public static boolean isFirstUse ()
    {

        return Environment.isFirstUse;

    }

    public static boolean checkCanOpenProject (ProjectInfo p,
                                               boolean     showLanding)
    {

        String r = Environment.canOpenProject (p);

        if (r != null)
        {

            // Do this first to ensure the error shows above it.
            if (showLanding)
            {

                Environment.showLanding ();

            }

            UIUtils.showErrorMessage (null,
                                      String.format (Environment.getUIString (LanguageStrings.project,
                                                                              LanguageStrings.actions,
                                                                              LanguageStrings.openproject,
                                                                              LanguageStrings.openerrors,
                                                                              LanguageStrings.general),
                                                     p.getName (),
                                                     r));
                                      //"Unable to open {project} <b>" + p.getName () + "</b>, reason:<br /><br />" + r);

            return false;

        }

        return true;

    }

    public static boolean openLastEditedProject ()
                                          throws Exception
    {

        List<ProjectInfo> projs = new ArrayList (Environment.getAllProjectInfos ());

        Collections.sort (projs,
                          new ProjectInfoSorter ());

        ProjectInfo p = null;

        if (projs.size () > 0)
        {

            p = projs.get (0);

        }

        if (p != null)
        {

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
                Environment.openProject (p,
                                         null);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open project: " +
                                      p,
                                      e);

                return false;

            }

            return true;

        }

        return false;

    }

    public static ProjectInfo getProjectById (String id,
                                              String projType)
                                       throws Exception
    {

        if (id == null)
        {

            return null;

        }

        Set<ProjectInfo> projs = Environment.getAllProjectInfos ();

        for (ProjectInfo p : projs)
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

    public static ProjectInfo getProjectByDirectory (File dir)
                                              throws Exception
    {

        if (dir == null)
        {

            return null;

        }

        Set<ProjectInfo> projs = Environment.getAllProjectInfos ();

        for (ProjectInfo p : projs)
        {

            if (p.getProjectDirectory ().equals (dir))
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
    public static ProjectVersion getProjectVersionById (ProjectInfo p,
                                                        String      id,
                                                        String      filePassword)
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
    public static Set<Chapter> getVersionedChapters (ProjectInfo         p,
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
    public static Set<Chapter> updateToNewVersions (ProjectInfo         p,
                                                    ProjectVersion      projVer,
                                                    Collection<Chapter> chaps,
                                                    String              filePassword)
                                             throws Exception
    {

        boolean closePool = false;

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

            closePool = true;

        }

        try
        {

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

        } finally {

            if (closePool)
            {

                if (om != null)
                {

                    om.closeConnectionPool ();

                }

            }

        }

    }

    public static void restoreBackupForProject (ProjectInfo p,
                                                File        restoreFile)
                                         throws Exception
    {

        // Get the project db file.

        File dbFile = new File (p.getProjectDirectory (),
                                Constants.PROJECT_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);

        if (!dbFile.exists ())
        {

            throw new GeneralException ("No project database file found at: " +
                                        dbFile +
                                        ", for project: " +
                                        p);

        }

        File oldDBFile = new File (dbFile.getPath () + ".old");

        // Rename to .old
        // TODO: Investigate using java.nio.file.Files.move instead.
        if (!dbFile.renameTo (oldDBFile))
        {

            throw new GeneralException ("Unable to rename project database file to: " +
                                        dbFile.getPath () + ".old" +
                                        ", for project: " +
                                        p);

        }

        try
        {

            Utils.extractZipFile (restoreFile,
                                  p.getProjectDirectory ());

            // See if there is a project db file in there now.
            if (!dbFile.exists ())
            {

                throw new GeneralException ("Backup file does not contain a valid project db file");

            }

            oldDBFile.delete ();

        } catch (Exception e) {

            // Try and rename back.
            oldDBFile.renameTo (dbFile);

            throw e;

        }

    }

    public static File createBackupForProject (Project p,
                                               boolean noPrune)
                                        throws Exception
    {

        return Environment.createBackupForProject (Environment.getProjectInfo (p),
                                                   noPrune);

    }

    public static File createBackupForProject (ProjectInfo p,
                                               boolean     noPrune)
                                        throws Exception
    {

        boolean closePool = false;

        AbstractProjectViewer pv = Environment.getProjectViewer (p);

        ObjectManager om = null;
        Project proj = null;

        if (pv != null)
        {

            // Load up the chapters.
            om = pv.getObjectManager ();

            proj = pv.getProject ();

        } else {

            if ((p.isEncrypted ())
                &&
                (p.getFilePassword () == null)
               )
            {

                throw new IllegalArgumentException ("The file password must be specified for encrypted projects when the project is not already open.");

            }

            // Open the project.
            try
            {

                om = Environment.getProjectObjectManager (p,
                                                          p.getFilePassword ());

                proj = om.getProject ();

            } catch (Exception e) {

                // Can't open the project.
                if (om != null)
                {

                    om.closeConnectionPool ();

                }

                throw e;

            }

            proj.setBackupDirectory (p.getBackupDirectory ());

            closePool = true;

        }

        String backupCount = proj.getProperty (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME);

        int count = -1;

        if ((backupCount != null)
            &&
            // Legacy, pre 2.6.5
            (!backupCount.equals ("All"))
           )
        {

            try
            {

                count = Integer.parseInt (backupCount);

            } catch (Exception e) {}

        }

        try
        {

            File f = om.createBackup (proj,
                                      (noPrune ? -1 : count));

            Environment.fireUserProjectEvent (proj,
                                              ProjectEvent.BACKUPS,
                                              ProjectEvent.NEW,
                                              proj);

            return f;

        } finally {

            if (closePool)
            {

                if (om != null)
                {

                    om.closeConnectionPool ();

                }

            }

        }

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
    public static ObjectManager getProjectObjectManager (ProjectInfo p,
                                                         String      filePassword)
                                                  throws GeneralException
    {

        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        if (p.isNoCredentials ())
        {

            username = null;
            password = null;

        }

        ObjectManager dBMan = new ObjectManager ();
        dBMan.init (new File (p.getProjectDirectory ().getPath (), Constants.PROJECT_DB_FILE_NAME_PREFIX),
                    username,
                    password,
                    filePassword,
                    Environment.getSchemaVersion ());

        try
        {

            dBMan.getProject ();

        } catch (Exception e) {

            dBMan.closeConnectionPool ();

            throw e;

        }

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

        ProjectInfo pi = new ProjectInfo (p);

        Environment.fireProjectInfoChangedEvent (pi,
                                                 ProjectInfoChangedEvent.ADDED);

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
    public static ObjectManager createProject (File    saveDir,
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

        dBMan.setProject (p);

        dBMan.saveObject (p,
                          null);

        ProjectInfo pi = new ProjectInfo (p);

        Environment.projectInfoManager.saveObject (pi,
                                                   null);

        Environment.fireProjectInfoChangedEvent (pi,
                                                 ProjectInfoChangedEvent.ADDED);

        return dBMan;

    }

    public static void openObjectInProject (final ProjectInfo proj,
                                            final DataObject  obj)
                                     throws Exception
    {

        Environment.openProject (proj,
                                 new ActionListener ()
                                 {

                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        // View the object.
                                        AbstractProjectViewer viewer = Environment.getProjectViewer (proj);

                                        viewer.viewObject (obj);

                                    }

                                 });

    }

    public static void openObjectInProject (final Project    proj,
                                            final DataObject obj)
                                     throws Exception
    {

        final DataObject dobj = obj;

        ProjectInfo p = null;

        try
        {

            p = Environment.getProjectInfo (proj);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get project info for project id: " +
                                        proj.getId (),
                                        e);

        }

        if (p == null)
        {

            throw new GeneralException ("Unable to get project info for project id: " +
                                        proj.getId ());

        }

        Environment.openObjectInProject (p,
                                         obj);

    }

    public static void openProjectWithId (String projId,
                                          String projType)
                                   throws Exception
    {

        Environment.openProject (projId,
                                 projType,
                                 null);

    }

    public static void openProject (Project p)
                             throws Exception
    {

        Environment.openProject (p,
                                 null);

    }

    public static void openProject (Project        p,
                                    ActionListener onProjectOpen)
                             throws Exception
    {

        if (p == null)
        {

            return;

        }

        Environment.openProject (p.getId (),
                                 p.getType (),
                                 onProjectOpen);

    }

    public static void openProject (final String         projId,
                                    final String         projType,
                                    final ActionListener onProjectOpen)
                             throws Exception
    {

        ProjectInfo p = Environment.getProjectInfo (projId,
                                                    projType);

        if (p != null)
        {

            Environment.openProject (p,
                                     onProjectOpen);

        }

    }

    public static void  openProject (final ProjectInfo p)
                              throws Exception
    {

        Environment.openProject (p,
                                 null);

    }

    public static void  openProject (final ProjectInfo    p,
                                     final ActionListener onProjectOpen)
                              throws Exception
    {

        if (p == null)
        {

            return;

        }

        if (p.isOpening ())
        {

            return;

        }

        AbstractProjectViewer pv = (AbstractProjectViewer) Environment.openProjects.get (p);

        if (pv != null)
        {

            p.setOpening (false);

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
            /*
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
            */
            Environment.incrStartupProgress ();

            java.util.List<String> prefix = new ArrayList ();
            prefix.add (LanguageStrings.project);
            prefix.add (LanguageStrings.actions);
            prefix.add (LanguageStrings.openproject);
            prefix.add (LanguageStrings.enterpasswordpopup);

            if (p.isEncrypted ())
            {

                Environment.startupComplete ();

                PasswordInputWindow.create (Environment.getUIString (prefix,
                                                                     LanguageStrings.title),
                                            //"Enter password",
                                            "lock",
                                            String.format (Environment.getUIString (prefix,
                                                                                    LanguageStrings.text),
                                                           p.getName ()),
                                            //"{Project}: <b>" + p.getName () + "</b> is encrypted, please enter the password.",
                                            Environment.getUIString (prefix,
                                                                     LanguageStrings.buttons,
                                                                     LanguageStrings.open),
                                            //"Open",
                                            new ValueValidator<String> ()
                                            {

                                                public String isValid (String v)
                                                {

                                                    if ((v == null)
                                                        ||
                                                        (v.trim ().equals (""))
                                                       )
                                                    {

                                                        return Environment.getUIString (prefix,
                                                                                        LanguageStrings.errors,
                                                                                        LanguageStrings.novalue);
                                                        //"Please enter the password.";

                                                    }

                                                    try
                                                    {

                                                        fpv.openProject (p,
                                                                         v,
                                                                         onProjectOpen);
                                                                                      /*
                                                        Environment.openProjects.put (p,
                                                                                      fpv);
                                */
                                                    } catch (Exception e) {

                                                        if (ObjectManager.isDatabaseAlreadyInUseException (e))
                                                        {

                                                            return Environment.getUIString (prefix,
                                                                                            LanguageStrings.errors,
                                                                                            LanguageStrings.projectalreadyopen);
                                                            //"Sorry, the {project} appears to already be open in Quoll Writer.  Please close all other instances of Quoll Writer first before trying to open the {project}.";

                                                        }

                                                        if (ObjectManager.isEncryptionException (e))
                                                        {

                                                            return Environment.getUIString (prefix,
                                                                                            LanguageStrings.errors,
                                                                                            LanguageStrings.invalidpassword);
                                                            //return "Password is not valid.";

                                                        }

                                                        Environment.logError ("Cant open project: " +
                                                                              p,
                                                                              e);

                                                        UIUtils.showErrorMessage (null,
                                                                                  Environment.getUIString (prefix,
                                                                                                           LanguageStrings.errors,
                                                                                                           LanguageStrings.general));
                                                                    //"Sorry, the {project} can't be opened.  Please contact Quoll Writer support for assistance.");

                                                        return null;

                                                    } finally {

                                                        p.setOpening (false);

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

                                                    Environment.showLanding ();

                                                }

                                            }).resize ();

                return;

            }

            try
            {

                pv.openProject (p,
                                null,
                                onProjectOpen);

            } catch (Exception e) {

                pv.close (false,
                          null);

                if (ObjectManager.isDatabaseAlreadyInUseException (e))
                {

                    UIUtils.showErrorMessage (null,
                                              Environment.getUIString (prefix,
                                                                       LanguageStrings.errors,
                                                                       LanguageStrings.projectalreadyopen));
                        //"Sorry, the {project} appears to already be open in Quoll Writer.  Please close all other instances of Quoll Writer first before trying to open the {project}.");

                    return;

                }

                throw e;

            } finally {

                p.setOpening (false);

            }

            Environment.startupComplete ();
            /*
            Environment.openProjects.put (p,
                                          pv);
*/
        }

        //return pv;

    }

    public static void addToAchievementsManager (AbstractProjectViewer viewer)
    {

        try
        {

            Environment.achievementsManager.addProjectViewer (viewer);

        } catch (Exception e) {

            Environment.logError ("Unable to init achievements for viewer: " +
                                  viewer,
                                  e);

        }

    }

    public static void removeFromAchievementsManager (AbstractViewer viewer)
    {

        try
        {

            Environment.achievementsManager.removeViewer (viewer);

        } catch (Exception e) {

            Environment.logError ("Unable to remove from achievements: " +
                                  viewer,
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

        if (Environment.isDebugModeEnabled ())
        {

            Environment.logMessage (m);

        }

    }

    public static void logMessage (String m)
    {

        if (Environment.isDebugModeEnabled ())
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

        if (!Environment.isDebugModeEnabled ())
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

        if ((!Environment.isDebugModeEnabled ())
            &&
            (UserProperties.getAsBoolean (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME))
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

            props.setParentProperties (UserProperties.getProperties ());

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

    public static void setUILanguage (String id)
                               throws Exception
    {

        LanguageStrings ls = null;

        ls = Environment.getUILanguageStrings (id);

        if (ls == null)
        {

            throw new GeneralException ("No language strings found for id: " +
                                        id);

        }

        Environment.uiLanguageStrings = ls;

        UserProperties.set (Constants.USER_UI_LANGUAGE_PROPERTY_NAME, id);

        Environment.loadUserObjectTypeNames ();

    }

    private static File getUILanguageStringsDir ()
    {

        File d = new File (Environment.getUserQuollWriterDir (),
                           Constants.UI_LANGUAGES_DIR_NAME);

        if (!d.exists ())
        {

            d.mkdirs ();

        }

        return d;

    }

    private static File getUserUILanguageStringsDir (Version v)
    {

        File d = new File (Environment.getUserQuollWriterDir (),
                           Constants.USER_UI_LANGUAGES_DIR_NAME + "/" + v.toString ());

        if (!d.exists ())
        {

            d.mkdirs ();

        }

        return d;

    }

    public static LanguageStrings getUserUILanguageStrings (Version v,
                                                            String  id)
                                                     throws Exception
    {

        File f = Environment.getUserUILanguageStringsFile (v,
                                                           id);

        if (f.exists ())
        {

            LanguageStrings ls = new LanguageStrings (f);
            ls.setUser (true);

            return ls;

        }

        return null;

    }

    private static void deleteUserUILanguageStrings (final LanguageStrings ls)
    {

        ActionListener remFile = new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                File f = Environment.getUserUILanguageStringsFile (ls.getQuollWriterVersion (),
                                                                   ls.getId ());

                if (f.exists ())
                {

                    f.delete ();

                }

            }

        };

        Set<AbstractViewer> viewers = new HashSet<> (Environment.openViewers);

        for (AbstractViewer v : viewers)
        {

            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if ((lse.getUserLanguageStrings ().getId ().equals (ls.getId ()))
                    &&
                    (lse.getUserLanguageStrings ().getQuollWriterVersion ().equals (ls.getQuollWriterVersion ()))
                   )
                {

                    lse.close (false,
                               remFile);

                    return;

                }

            }

        }

        remFile.actionPerformed (new ActionEvent (ls, 0, "do"));

        if (UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME).equals ("user-" + ls.getId ()))
        {

            try
            {

                // Need to set the language back to English.
                Environment.setUILanguage (LanguageStrings.ENGLISH_ID);

            } catch (Exception e) {

                Environment.logError ("Unable to set UI strings.",
                                      e);

                UIUtils.showErrorMessage (null,
                                          "Unable to reset user interface language to " + Constants.ENGLISH);

                return;

            }

            UIUtils.showMessage ((Component) null,
                                 "Restart recommended",
                                 "The user interface language has been reset to " + Constants.ENGLISH + ", a restart is recommended.");

        }

    }

    public static void deleteUserUILanguageStrings (LanguageStrings ls,
                                                    boolean         allVersions)
                                             throws Exception
    {

        if (!ls.isUser ())
        {

            throw new IllegalArgumentException ("Can only delete user language strings.");

        }

        if (allVersions)
        {

            Set<LanguageStrings> allLs = Environment.getAllUserLanguageStrings ();

            for (LanguageStrings _ls : allLs)
            {

                if (_ls.getId ().equals (ls.getId ()))
                {

                    Environment.deleteUserUILanguageStrings (_ls);

                }

            }

        } else {

            Environment.deleteUserUILanguageStrings (ls);

        }

    }

    public static LanguageStrings getUserUIEnglishLanguageStrings (Version v)
                                                            throws Exception
    {

        // If the version is the same as the QW version the user is running then
        if (v.equals (Environment.getQuollWriterVersion ()))
        {

            LanguageStrings def = Environment.getDefaultUILanguageStrings ();

            Environment.saveUserUILanguageStrings (def);

            return def;

            //return Environment.getUserUIEnglishLanguageStrings (v);

        }

        // See if there is a user strings file.
        File f = Environment.getUserUILanguageStringsFile (v,
                                                           LanguageStrings.ENGLISH_ID);

        if (f.exists ())
        {

            return new LanguageStrings (f);

        }

        return null;

    }

    public static LanguageStrings getDefaultUILanguageStrings ()
    {

        return Environment.defaultUILanguageStrings;

    }

    public static LanguageStrings getCurrentUILanguageStrings ()
    {

        return Environment.uiLanguageStrings;

    }

    public static LanguageStrings getUILanguageStrings (String id)
                                                 throws Exception
    {

        if (id.startsWith ("user-"))
        {

            id = id.substring ("user-".length ());

            return Environment.getUserUILanguageStrings (Environment.getQuollWriterVersion (),
                                                         id);

        }

        if (id.equals (LanguageStrings.ENGLISH_ID))
        {

            return Environment.getDefaultUILanguageStrings ();

        }

        File f = Environment.getUILanguageStringsFile (id);

        if (!f.exists ())
        {

            return null;

        }

        String data = Utils.getFileAsString (f,
                                             StandardCharsets.UTF_8);

        LanguageStrings s = new LanguageStrings (data);

        return s;

    }

    public static Set<LanguageStrings> getAllUserLanguageStrings (Version qwVer)
                                                           throws GeneralException
    {

        Set<LanguageStrings> ret = new LinkedHashSet<> ();

        for (LanguageStrings ls : Environment.getAllUserLanguageStrings ())
        {

            if (ls.getQuollWriterVersion ().equals (qwVer))
            {

                ret.add (ls);

            }

        }

        return ret;

    }

    public static Set<LanguageStrings> getAllUserLanguageStrings ()
                                                           throws GeneralException
    {

        Set<LanguageStrings> s = new TreeSet<> ();

        File d = new File (Environment.getUserQuollWriterDir (),
                           Constants.USER_UI_LANGUAGES_DIR_NAME);

        File[] files = d.listFiles ();

        if (files != null)
        {

            for (int i = 0; i < files.length; i++)
            {

                File fd = files[i];

                File[] dfiles = fd.listFiles ();

                if (dfiles != null)
                {

                    for (int j = 0; j < dfiles.length; j++)
                    {

                        LanguageStrings ls = new LanguageStrings (dfiles[j]);

                        if (ls.isEnglish ())
                        {

                            continue;

                        }

                        s.add (ls);

                    }

                }

            }

        }

        return s;

    }

    public static void saveUserUILanguageStrings (LanguageStrings ls)
                                           throws Exception
    {

        File f = Environment.getUserUILanguageStringsFile (ls);

        f.getParentFile ().mkdirs ();

        String json = JSONEncoder.encode (ls.getAsJSON ());

        Writer out = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (f),
                                                                 "utf-8"));

        char[] chars = json.toCharArray ();

        out.write (chars, 0, chars.length);
    	out.flush ();
    	out.close ();

    }

    public static void downloadUILanguageFile (final String         id,
                                               final ActionListener onComplete,
                                               final ActionListener onError)
    {

        Environment.schedule (new Runnable ()
        {

            @Override
            public void run ()
            {

                String lastMod = "";

                LanguageStrings ls = null;

                try
                {

                    ls = Environment.getUILanguageStrings (id);

                } catch (Exception e) {

                    Environment.logError ("Unable to get language strings: " + id,
                                          e);

                    onError.actionPerformed (new ActionEvent (getUIString (uilanguage,download,actionerror), 0, "error"));

                    return;

                }

                if (ls != null)
                {

                    Date d = ls.getLastModified ();

                    if (d == null)
                    {

                        d = ls.getDateCreated ();

                    }

                    lastMod = d.getTime () + "";

                }

                String url = Environment.getProperty (Constants.QUOLL_WRITER_GET_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                url = StringUtils.replaceString (url,
                                                 Constants.VERSION_TAG,
                                                 Environment.getQuollWriterVersion ().toString ());

                url = StringUtils.replaceString (url,
                                                 Constants.ID_TAG,
                                                 id);

                url = StringUtils.replaceString (url,
                                                 Constants.LAST_MOD_TAG,
                                                 lastMod);

                try
                {

                    String data = Environment.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + url));

                    if (data.startsWith (Constants.JSON_RETURN_PREFIX))
                    {

                        data = data.substring (Constants.JSON_RETURN_PREFIX.length ());

                    }

                    if (data.trim ().length () == 0)
                    {

                        Environment.logError ("No language strings data available for: " + id + ", " + Environment.getQuollWriterVersion ());

                        onError.actionPerformed (new ActionEvent (getUIString (uilanguage,download,actionerror), 0, "error"));

                        return;

                    }

                    // Will be a collection.
                    Collection col = null;

                    try
                    {

                        col = (Collection) JSONDecoder.decode (data);

                    } catch (Exception e) {

                        Environment.logError ("Unable to decode language strings data for id: " + id + ", " + Environment.getQuollWriterVersion (),
                                              e);

                        onError.actionPerformed (new ActionEvent (getUIString (uilanguage,download,actionerror), 0, "error"));

                        return;

                    }

                    Iterator iter = col.iterator ();

                    int updated = 0;

                    while (iter.hasNext ())
                    {

                        Map m = (Map) iter.next ();

                        String id = (String) m.get (":id");

                        if (id == null)
                        {

                            throw new GeneralException ("No id found.");

                        }

                        updated++;

                        File f = Environment.getUILanguageStringsFile (id);

                        Writer out = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (f),
                                                                                 StandardCharsets.UTF_8));

                        char[] chars = JSONEncoder.encode (m).toCharArray ();

                        out.write (chars, 0, chars.length);
                    	out.flush ();
                    	out.close ();

                    }

                    onComplete.actionPerformed (new ActionEvent (this, updated, "success"));

                } catch (Exception e) {

                    Environment.logError ("Unable to get user interface files for: " + id + ", " + Environment.getQuollWriterVersion (),
                                          e);

                    onError.actionPerformed (new ActionEvent (getUIString (uilanguage,download,actionerror), 0, "error"));

                }

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

    }

    public static Set<LanguageStrings> getAllUILanguageStrings ()
    {

        Set<LanguageStrings> ret = new LinkedHashSet<> ();

        File[] files = Environment.getUILanguageStringsDir ().listFiles ();

        if (files == null)
        {

            return ret;

        }

        for (int i = 0; i < files.length; i++)
        {

            File f = files[i];

            if (f.isFile ())
            {

                try
                {

                    ret.add (new LanguageStrings (f));

                } catch (Exception e) {

                    Environment.logError ("Unable to create strings from: " + f,
                                          e);

                    // Delete the file.
                    f.delete ();

                }

            }

        }

        return ret;

    }

    public static File getUILanguageStringsFile (String id)
    {

        return new File (Environment.getUILanguageStringsDir (),
                         id);

    }

    public static File getUserUILanguageStringsFile (Version qwVersion,
                                                     String  id)
    {

        if (id.equals (LanguageStrings.ENGLISH_ID))
        {

            id = id.substring (1);

        }

        return new File (Environment.getUserUILanguageStringsDir (qwVersion),
                         id);

    }

    public static File getUserUILanguageStringsFile (LanguageStrings ls)
    {

        return Environment.getUserUILanguageStringsFile (ls.getQuollWriterVersion (),
                                                         ls.getId ());

    }

    private static File getUserDefaultProjectPropertiesFile ()
    {

        return new File (Environment.getUserQuollWriterDir () + "/" + Constants.DEFAULT_PROJECT_PROPERTIES_FILE_NAME);

    }

    public static AbstractProjectViewer getProjectViewerForType (Project p)
                                                          throws Exception
    {

        AbstractProjectViewer v = null;

        if (p.getType ().equals (Project.NORMAL_PROJECT_TYPE))
        {

            v = new ProjectViewer ();

        }

        if (p.getType ().equals (Project.EDITOR_PROJECT_TYPE))
        {

            v = new EditorProjectViewer ();

        }

        if (p.getType ().equals (Project.WARMUPS_PROJECT_TYPE))
        {

            v = new WarmupsViewer ();

        }

        if (v == null)
        {

            throw new GeneralException ("Project type: " +
                                        p.getType () +
                                        " is not supported.");

        }

        v.init ();

        return v;

    }

    public static AbstractProjectViewer getProjectViewerForType (ProjectInfo p)
                                                          throws Exception
    {

        AbstractProjectViewer v = null;

        if (p.getType ().equals (Project.NORMAL_PROJECT_TYPE))
        {

            v = new ProjectViewer ();

        }

        if (p.getType ().equals (Project.EDITOR_PROJECT_TYPE))
        {

            v = new EditorProjectViewer ();

        }

        if (p.getType ().equals (Project.WARMUPS_PROJECT_TYPE))
        {

            v = new WarmupsViewer ();

        }

        if (v == null)
        {

            throw new GeneralException ("Project type: " +
                                        p.getType () +
                                        " is not supported.");

        }

        v.init ();

        return v;

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


        return Environment.getUIString (LanguageStrings.buttons,
                                        id);

    }

    private static void initProjectsDBFromProjectsFile ()
                                                 throws Exception
    {

        File f = new File (Environment.getUserQuollWriterDir () + "/" + Constants.PROJECTS_FILE_NAME);

        if (!f.exists ())
        {

            return;

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

            ProjectInfo pi = null;

            // Try and load the project.
            try
            {

                pi = new ProjectInfo (p);

                ObjectManager om = Environment.getProjectObjectManager (pi,
                                                                        null);

                // Now deal with the real project.
                pi = new ProjectInfo (om.getProject ());

                pi.setEncrypted (p.isEncrypted ());
                pi.setNoCredentials (p.isNoCredentials ());

                om.closeConnectionPool ();

            } catch (Exception e) {

                Environment.logError ("Unable to load project: " +
                                      p,
                                      e);

            }

            if (pi == null)
            {

                try
                {

                    pi = new ProjectInfo (p);

                } catch (Exception e) {

                    Environment.logError ("Unable to convert project: " +
                                          p +
                                          " to a project info",
                                          e);

                    continue;

                }

            }

            try
            {

                Environment.projectInfoManager.saveObject (pi,
                                                           null);

            } catch (Exception e) {

                Environment.logError ("Unable to load project: " +
                                      p +
                                      ", path: " +
                                      JDOMUtils.getPath (pEl) +
                                      " into the project db",
                                      e);

            }

        }

        // Remove the projects file (rename for now).
        f.renameTo (new File (f.getParentFile (), f.getName () + ".old"));

    }

    public static Set<ProjectInfo> getAllProjectInfos (String limitToType)
                                                throws Exception
    {

        Set<ProjectInfo> all = new LinkedHashSet (Environment.projectInfoManager.getObjects (ProjectInfo.class,
                                                                                             null,
                                                                                             null,
                                                                                             true));

        if (limitToType != null)
        {

            Set<ProjectInfo> pis = new LinkedHashSet ();

            for (ProjectInfo p : all)
            {

                if (p.getType ().equals (limitToType))
                {

                    pis.add (p);

                }

            }

            all = pis;

        }

        return all;

    }

    public static Set<ProjectInfo> getAllProjectInfos ()
                                                throws Exception
    {

        return Environment.getAllProjectInfos (null);

    }

    public static ProjectInfo getWarmupsProject ()
                                          throws Exception
    {

        Set<ProjectInfo> projs = Environment.getAllProjectInfos ();

        for (ProjectInfo pi : projs)
        {

            if (pi.isWarmupsProject ())
            {

                return pi;

            }

        }

        return null;

    }

    public static boolean getPropertyAsBoolean (String name)
    {

        return UserProperties.getAsBoolean (name);
        //return Environment.userProperties.getPropertyAsBoolean (name);

    }

    public static String getProperty (String name)
    {

        return UserProperties.get (name);
        //return Environment.userProperties.getProperty (name);

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

    /**
     * No longer used, since properties now stored in projects db.
     * This is only used for legacy versions that need to port the properties over
     * to the new storage method.
     */
    private static File getUserPropertiesFile ()
    {

        return Environment.getUserFile (Constants.PROPERTIES_FILE_NAME);

    }

    public static void saveUserProperties ()
                                    throws Exception
    {

        Environment.projectInfoManager.setUserProperties (UserProperties.getProperties ());//Environment.userProperties);

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

        // Start the timer, it is done here so that any other code that needs it can start running things
        // straightaway.
        Environment.generalTimer = new ScheduledThreadPoolExecutor (5,
                                                                    new ThreadFactory ()
        {

            @Override
            public Thread newThread (Runnable r)
            {

                Thread t = new Thread (r);

                t.setDaemon (true);
                t.setPriority (Thread.MIN_PRIORITY);
                t.setName ("Environment-general-" + t.getId ());

                return t;

            }

        });

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

        if (Environment.isWindows)
        {

            try
            {

                com.jgoodies.looks.Options.setUseSystemFonts (true);

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

        // Try and get a lock on the file.
        File l = new File (Environment.getUserQuollWriterDir (),
                           "___quollwriter_lock.lock");

        FileChannel ch = new RandomAccessFile (l, "rw").getChannel ();

        FileLock lock = ch.tryLock ();

        if (lock == null)
        {

            throw new OverlappingFileLockException ();

        }

        Environment.lock = lock;

        l.deleteOnExit ();

        Environment.appVersion = new Version (Environment.getResourceFileAsString (Constants.VERSION_FILE).trim ());

        try
        {

            Environment.schemaVersion = Integer.parseInt (Environment.getResourceFileAsString (Constants.SCHEMA_VERSION_FILE).trim ());

        } catch (Exception e)
        {

            // Ignore.

        }

        try
        {

            Environment.projectInfoSchemaVersion = Integer.parseInt (Environment.getResourceFileAsString (Constants.PROJECT_INFO_SCHEMA_VERSION_FILE).trim ());

        } catch (Exception e)
        {

            // Ignore.

        }

        com.gentlyweb.properties.Properties sysProps = new com.gentlyweb.properties.Properties (Environment.class.getResourceAsStream (Constants.DEFAULT_PROPERTIES_FILE),
                                                                                                null);

        sysProps.setId ("system");

        // Temporarily set the user properties to the system properties.
        UserProperties.init (sysProps);
        //Environment.userProperties = sysProps;

        com.gentlyweb.properties.Properties userProps = sysProps;

        Environment.incrStartupProgress ();

        Environment.defaultUILanguageStrings = new LanguageStrings (Environment.getResourceFileAsString (Constants.DEFAULT_UI_LANGUAGE_STRINGS_FILE));
/*
        Map<LanguageStrings.Value, Set<String>> errs = Environment.defaultUILanguageStrings.getErrors ();

        for (LanguageStrings.Value v : errs.keySet ())
        {

            System.out.println (v);
            System.out.println (errs.get (v));
            System.out.println ();
        }
*/

        Environment.uiLanguageStrings = Environment.defaultUILanguageStrings;

        // Load the default object type names.
        // Object type names may be needed when initing the legacy object types.
        /*
        try
        {

            Environment.loadObjectTypeNames (JDOMUtils.getStringAsElement (Environment.getResourceFileAsString (Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE)));

        } catch (Exception e) {

            Environment.logError ("Unable to load default object type names from resource file: " +
                                  Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE);

        }
*/
        // See if this is first use.
        Environment.isFirstUse = (Environment.getProjectInfoSchemaVersion () == 0);

        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        Environment.projectInfoManager = new ProjectInfoObjectManager ();

        Environment.projectInfoManager.init (Environment.getProjectInfoDBFile (),
                                             username,
                                             password,
                                             null,
                                             Environment.getProjectInfoSchemaVersion ());

        try
        {

            userProps = Environment.projectInfoManager.getUserProperties ();

        } catch (Exception e) {

            Environment.logError ("Unable to load user properties",
                                  e);

        }

        if (userProps == null)
        {

            // Check for legacy properties.xml.  Pre v2.5.
            File pf = Environment.getUserPropertiesFile ();

            if (pf.exists ())
            {

                try
                {

                    userProps = new com.gentlyweb.properties.Properties (pf,
                                                                         Environment.GZIP_EXTENSION);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to load user properties from file: " +
                                          pf,
                                          e);

                }

                pf.delete ();

            }

        }

        if (userProps == null)
        {

            userProps = new com.gentlyweb.properties.Properties ();

        }

        if (userProps != sysProps)
        {

            userProps.setId ("user");

            userProps.setParentProperties (sysProps);

        }

        if (userProps == null)
        {

            // If this is legacy and we can't load the properties file (is corrupted) then
            // use the system properties as the properties.
            userProps = sysProps;

        }

        UserProperties.init (userProps);

        // Do a save here so that if we are loading for the first time they will be saved.
        try
        {

            Environment.saveUserProperties ();

        } catch (Exception e) {

            Environment.logError ("Unable to save user properties",
                                  e);

        }

        // Load the user default, if appropriate.
        final String uilangid = UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

        if (uilangid != null)
        {

            if (!LanguageStrings.isEnglish (uilangid))
            {

                LanguageStrings ls = Environment.getUILanguageStrings (uilangid);

                if ((ls == null)
                    ||
                    // Have we updated QW and need to get newer versions?
                    ((ls != null)
                     &&
                     (Environment.getQuollWriterVersion ().isNewer (ls.getQuollWriterVersion ()))
                    )
                   )
                {

                    // Something has gone wrong, try and download again.
                    Environment.downloadUILanguageFile (uilangid,
                                                        new ActionListener ()
                                                        {

                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                try
                                                                {

                                                                    Environment.setUILanguage (uilangid);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to set ui language to: " + uilangid,
                                                                                          e);

                                                                    UIUtils.showErrorMessage (null,
                                                                                              getUIString (uilanguage,set,downloading,errors,download));
                                                                                              //"Warning!  Quoll Writer has been unable to re-download the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                                }

                                                                UIUtils.showMessage (null,
                                                                                     getUIString (uilanguage,set,downloading,redownload,confirmpopup,title),
                                                                                     //"Language strings re-downloaded",
                                                                                     getUIString (uilanguage,set,downloading,redownload,confirmpopup,text),
                                                                                     //"Quoll Writer has re-downloaded the User Interface language strings you are using because they were missing from your local system.  In the interim the User Interface has fallen back to using English.<br /><br />To return to using your selected language Quoll Writer must be restarted.",
                                                                                     null,
                                                                                     null);

                                                            }

                                                        },
                                                        // On error.
                                                        new ActionListener ()
                                                        {

                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                UIUtils.showErrorMessage (null,
                                                                                          getUIString (uilanguage,set,downloading,redownload,actionerror));
                                                                                          //"Warning!  Quoll Writer has been unable to re-download the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                            }

                                                        });

                } else {

                    Environment.uiLanguageStrings = ls;

                    if (!ls.isUser ())
                    {

                        // See if there is an update to the strings.
                        Environment.downloadUILanguageFile (uilangid,
                                                            new ActionListener ()
                                                            {

                                                                @Override
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    if (ev.getID () > 0)
                                                                    {

                                                                        try
                                                                        {

                                                                            Environment.setUILanguage (uilangid);

                                                                        } catch (Exception e) {

                                                                            Environment.logError ("Unable to set ui language to: " + uilangid,
                                                                                                  e);

                                                                            UIUtils.showErrorMessage (null,
                                                                                                      getUIString (uilanguage,set,downloading,update,actionerror));
                                                                                                      //"Warning!  Quoll Writer has been unable to update the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                                        }

                                                                        UIUtils.showMessage (null,
                                                                                             getUIString (uilanguage,set,downloading,update,confirmpopup,title),
                                                                                             //"Language strings updated",
                                                                                             getUIString (uilanguage,set,downloading,update,confirmpopup,text),
                                                                                             //"Quoll Writer has updated the User Interface language strings you are using because a new version was available.<br /><br />To make full use of the updated strings Quoll Writer must be restarted.",
                                                                                             null,
                                                                                             null);

                                                                    }

                                                                }

                                                            },
                                                            // On error.
                                                            new ActionListener ()
                                                            {

                                                                @Override
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                }

                                                            });

                    }

                }

            }

        }

        try
        {

            Environment.loadUserObjectTypeNames ();

        } catch (Exception e) {

            Environment.logError ("Unable to load user object type names.",
                                  e);

        }

        // Add a property listener for name changes to user config object types.
        Environment.userConfigurableObjectTypeNameListener = new PropertyChangedListener ()
        {

            @Override
            public void propertyChanged (PropertyChangedEvent ev)
            {

                UserConfigurableObjectType type = (UserConfigurableObjectType) ev.getSource ();

                String id = type.getObjectTypeId ();

                Environment.objectTypeNamesSingular.put (id,
                                                         type.getObjectTypeName ());

                Environment.objectTypeNamesPlural.put (id,
                                                       type.getObjectTypeNamePlural ());

            }

        };

        // Init our legacy object types, if needed.
        Environment.projectInfoManager.initLegacyObjectTypes ();

        // The user session needs the properties.
        Environment.userSession = new UserSession ();

        // Override the debug mode.
        /*
        if (UserProperties.get (Constants.DEBUG_MODE_PROPERTY_NAME) != null)
        {

            Environment.setDebugModeEnabled (UserProperties.getAsBoolean (Constants.DEBUG_MODE_PROPERTY_NAME));

        }
*/

        // Get the system default project properties.
        com.gentlyweb.properties.Properties sysDefProjProps = new com.gentlyweb.properties.Properties (Environment.class.getResourceAsStream (Constants.DEFAULT_PROJECT_PROPERTIES_FILE),
                                                                              UserProperties.getProperties ());

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

        // Create the text properties, they are derived from the user properties so need to be done after
        // the user props are inited.
        Environment.projectTextProps = new ProjectTextProperties ();

        Environment.fullScreenTextProps = new FullScreenTextProperties ();

        Environment.playSoundOnKeyStroke = UserProperties.getAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        String sf = UserProperties.get (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        try
        {

            File ksf = null;

            if (sf != null)
            {

                ksf = new File (sf);

            }

            Environment.setKeyStrokeSoundFile (ksf);

        } catch (Exception e)
        {

            Environment.logError ("Unable to get sound file to play on key stroke using file: " + sf,
                                  e);

        }

        Environment.incrStartupProgress ();

/*
        Environment.userPropertyHandlers.put (Constants.OBJECT_TYPES_PROPERTY_NAME,
                                              new UserPropertyHandler (Constants.OBJECT_TYPES_PROPERTY_NAME,
                                                                       null));
*/
        Environment.userPropertyHandlers.put (Constants.NOTE_TYPES_PROPERTY_NAME,
                                              new UserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME,
                                                                       null,
                                                                       notetypes,defaulttypes));
        Environment.userPropertyHandlers.put (Constants.PROJECT_STATUSES_PROPERTY_NAME,
                                              new UserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME,
                                                                       null,
                                                                       allprojects,defaultstatuses));
        Environment.userPropertyHandlers.put (Constants.TAGS_PROPERTY_NAME,
                                              new UserPropertyHandler (Constants.TAGS_PROPERTY_NAME,
                                                                       null,
                                                                       // Prevents the compiler whining...
                                                                       (String[]) null));

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

        Environment.schedule (new Runnable ()
        {

            @Override
            public void run ()
            {

                try
                {

                    Importer.init ();

                } catch (Exception e) {

                    Environment.logError ("Unable to init importer",
                                          e);

                }

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

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

                return true;

            }
        });

        try
        {

            // Get the user editor properties.
            com.gentlyweb.properties.Properties eprops = new com.gentlyweb.properties.Properties ();

            File edPropsFile = Environment.getUserEditorsPropertiesFile ();

            if (edPropsFile.exists ())
            {

                eprops = new com.gentlyweb.properties.Properties (edPropsFile,
                                                                  Environment.GZIP_EXTENSION);

            }

            eprops.setParentProperties (UserProperties.getProperties ());

            EditorsEnvironment.init (eprops);

        } catch (Exception e) {

            Environment.logError ("Unable to init editors environment",
                                  e);

        }

        // Pre 2.4.
        // See if there is a projects.xml file, if so load the db.
        try
        {

            Environment.initProjectsDBFromProjectsFile ();

        } catch (Exception e) {

            Environment.logError ("Unable to init project info from projects file",
                                  e);

        }

        if (Environment.isFirstUse)
        {

            Environment.isFirstUse = (Environment.getAllProjectInfos ().size () == 0);

        }

        Environment.targets = new TargetsData (UserProperties.getProperties ());

        Environment.addStartupProgressListener (new PropertyChangedListener ()
        {

            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (Environment.isStartupComplete ())
                {

                    Environment.userSession.start (new Date ());

                    // See if we should be doing a warmup exercise.
                    if (UserProperties.getAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME))
                    {

                        UIUtils.doLater (new ActionListener ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                AbstractViewer viewer = Environment.getFocusedViewer ();

                                if (viewer != null)
                                {

                                    viewer.showWarmupPromptSelect ();

                                    viewer.fireProjectEvent (Warmup.OBJECT_TYPE,
                                                             ProjectEvent.WARMUP_ON_STARTUP);

                                }

                            }

                        });

                    }

                    Date d = new Date (System.currentTimeMillis () + (Constants.DAY_IN_MILLIS));

                    d = Utils.zeroTimeFields (d);

                    Environment.schedule (new Runnable ()
                    {

                        @Override
                        public void run ()
                        {

                            try
                            {

                                Environment.projectInfoManager.addSession (Environment.userSession.createSnapshot ());

                            } catch (Exception e) {

                                Environment.logError ("Unable to take session snapshot",
                                                      e);

                            }

                        }

                    },
                    d.getTime (),
                    // Run every 24 hours.  It will drift over the days but not by much.
                    Constants.DAY_IN_MILLIS);

                    Environment.schedule (new Runnable ()
                    {

                        @Override
                        public void run ()
                        {

                            java.util.List<String> prefix = new ArrayList ();
                            prefix.add (LanguageStrings.targets);
                            prefix.add (LanguageStrings.types);

                            Set<String> met = new LinkedHashSet ();
                            int sessWC = 0;

                            try
                            {

                                if (!Environment.targets.isShowMessageWhenSessionTargetReached ())
                                {

                                    return;

                                }

                                sessWC = Environment.userSession.getCurrentSessionWordCount ();

                                // See if the user session has exceeded the session count.
                                if ((sessWC >= Environment.targets.getMySessionWriting ())
                                    &&
                                    (Environment.userSession.shouldShowSessionTargetReachedPopup ())
                                   )
                                {

                                    met.add (Environment.getUIString (prefix,
                                                                      LanguageStrings.session));
                                             //"Session");

                                    Environment.userSession.shownSessionTargetReachedPopup ();

                                }

                            } catch (Exception e) {

                                Environment.logError ("Unable show session target reached popup",
                                                      e);

                            }

                            // Check for the daily count.
                            // Get all sessions for today.
                            try
                            {

                                // The order is important here, the userSession check is cheaper
                                // than the past sessions check since it doesn't require a db lookup.
                                if ((Environment.userSession.shouldShowDailyTargetReachedPopup ())
                                    &&
                                    (Environment.getPastSessionsWordCount (0) >= Environment.targets.getMyDailyWriting ())
                                   )
                                {

                                    met.add (Environment.getUIString (prefix,
                                                                      LanguageStrings.daily));
                                    //"Daily");

                                    Environment.userSession.shownDailyTargetReachedPopup ();

                                }

                            } catch (Exception e) {

                                Environment.logError ("Unable to get past session word counts",
                                                      e);

                            }

                            // Get all sessions for this week.
                            try
                            {

                                // We perform the cheap check here since it will prevent the extra work
                                // with the calendar and db from having to be performed.
                                if (Environment.userSession.shouldShowWeeklyTargetReachedPopup ())
                                {

                                    GregorianCalendar gc = new GregorianCalendar ();

                                    int fd = gc.getFirstDayOfWeek ();

                                    int cd = gc.get (Calendar.DAY_OF_WEEK);

                                    int diff = cd - fd;

                                    if (diff < 0)
                                    {

                                        diff += 7;

                                    }

                                    if (Environment.getPastSessionsWordCount (diff) >= Environment.targets.getMyWeeklyWriting ())
                                    {

                                        met.add (Environment.getUIString (prefix,
                                                                          LanguageStrings.weekly));
                                                 //"Weekly");

                                        Environment.userSession.shownWeeklyTargetReachedPopup ();

                                    }

                                }

                            } catch (Exception e) {

                                Environment.logError ("Unable to get past session word counts",
                                                      e);

                            }

                            // Get all sessions for this month.
                            try
                            {

                                // As above, do the cheap check first to prevent the extra work from
                                // being done.
                                if (Environment.userSession.shouldShowMonthlyTargetReachedPopup ())
                                {

                                    GregorianCalendar gc = new GregorianCalendar ();

                                    int fd = gc.getFirstDayOfWeek ();

                                    int cd = gc.get (Calendar.DAY_OF_MONTH);

                                    int diff = cd - fd;

                                    if (Environment.getPastSessionsWordCount (diff) >= Environment.targets.getMyMonthlyWriting ())
                                    {

                                        AbstractViewer viewer = Environment.getFocusedViewer ();

                                        met.add (Environment.getUIString (prefix,
                                                                          LanguageStrings.monthly));
                                                 //"Monthly");

                                        Environment.userSession.shownMonthlyTargetReachedPopup ();

                                    }

                                }

                            } catch (Exception e) {

                                Environment.logError ("Unable to get past session word counts",
                                                      e);

                            }

                            try
                            {

                                if (met.size () > 0)
                                {

                                    StringBuilder b = new StringBuilder ();

                                    for (String m : met)
                                    {

                                        b.append (String.format ("<li>%s</li>",
                                                                 m));

                                    }

                                    AbstractViewer viewer = Environment.getFocusedViewer ();

                                    UIUtils.showMessage ((PopupsSupported) viewer,
                                                         Environment.getUIString (LanguageStrings.targets,
                                                                                  LanguageStrings.writingtargetreachedpopup,
                                                                                  LanguageStrings.title),
                                                         //"Writing targets reached",
                                                         String.format (Environment.getUIString (LanguageStrings.targets,
                                                                                                 LanguageStrings.writingtargetreachedpopup,
                                                                                                 LanguageStrings.text),
                                                        //"You have reached the following writing targets by writing <b>%s</b> words.<ul>%s</ul>Well done and keep it up!",
                                                                        Environment.formatNumber (sessWC),
                                                                        b.toString ()),
                                                         UIUtils.defaultLeftCornerShowPopupAt);


                                }

                            } catch (Exception e) {

                                Environment.logError ("Unable to show writing targets reached popup",
                                                      e);

                            }

                        }

                    },
                    5 * Constants.SEC_IN_MILLIS,
                    5 * Constants.SEC_IN_MILLIS);

                }

            }

        });

    }

    public static File getUserEditorsPropertiesFile ()
    {

        return Environment.getUserFile (Constants.EDITORS_PROPERTIES_FILE_NAME);

    }

    private static int getPastSessionsWordCount (int daysPast)
                                          throws GeneralException
    {

        List<Session> sess = Environment.projectInfoManager.getSessions (daysPast);

        int c = 0;

        Session last = null;

        for (Session s : sess)
        {

            c += s.getWordCount ();

            last = s;

        }

        c += Environment.userSession.getCurrentSessionWordCount ();

        return c;

    }

    public static UserPropertyHandler getUserPropertyHandler (String userProp)
    {

        return Environment.userPropertyHandlers.get (userProp);

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

        if (Environment.isDebugModeEnabled ())
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

    public static DictionaryProvider2 getDefaultDictionaryProvider ()
                                                             throws Exception
    {

        String lang = UserProperties.get (Constants.DEFAULT_SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

        if (lang == null)
        {

            lang = Constants.ENGLISH;

        }

        return new UserDictionaryProvider (lang);

/*
        if (Environment.defaultDictProv == null)
        {

            String lang = UserProperties.get (Constants.DEFAULT_SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

            if (lang == null)
            {

                lang = Constants.ENGLISH;

            }

            Environment.defaultDictProv = new DictionaryProvider (lang,
                                                                  null);

        }

        return Environment.defaultDictProv;
*/
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
/*
        // Load the default object type names.
        try
        {

            Environment.loadObjectTypeNames (JDOMUtils.getStringAsElement (Environment.getResourceFileAsString (Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE)));

        } catch (Exception e) {

            Environment.logError ("Unable to load default object type names from resource file: " +
                                  Constants.DEFAULT_OBJECT_TYPE_NAMES_FILE);

        }
*/
        File otf = Environment.getUserObjectTypeNamesFile ();

        // Remove the file.
        otf.delete ();

        otf = Environment.getUserFile (Constants.LEGACY_OBJECT_TYPE_NAMES_FILE_NAME);

        otf.delete ();

        Environment.loadUserConfigurableObjectTypeNames ();

        // Load the
        UserConfigurableObjectType chapterType = Environment.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);

        if (chapterType != null)
        {

            Environment.objectTypeNamesPlural.put (Chapter.OBJECT_TYPE,
                                                   getUIString (objectnames,plural,Chapter.OBJECT_TYPE));
            Environment.objectTypeNamesSingular.put (Chapter.OBJECT_TYPE,
                                                     getUIString (objectnames,singular,Chapter.OBJECT_TYPE));

        }


    }

    private static void loadUserObjectTypeNames ()
                                          throws Exception
    {

        File f = Environment.getUserObjectTypeNamesFile ();

        if (f.exists ())
        {

            // Use this one.
            Map t = (Map) JSONDecoder.decode (IOUtils.getFile (f));

            Environment.objectTypeNamesSingular = (Map) t.get (LanguageStrings.singular);
            Environment.objectTypeNamesPlural = (Map) t.get (LanguageStrings.plural);

        } else {

            // Legacy: pre 2.6.2
            f = Environment.getUserFile (Constants.LEGACY_OBJECT_TYPE_NAMES_FILE_NAME);

            if (f.exists ())
            {

                Environment.loadLegacyObjectTypeNames (JDOMUtils.getStringAsElement (IOUtils.getFile (f)));

                Environment.setUserObjectTypeNames (Environment.objectTypeNamesSingular,
                                                    Environment.objectTypeNamesPlural);

                f.delete ();

            }

        }

        Environment.loadUserConfigurableObjectTypeNames ();

    }

    private static void loadUserConfigurableObjectTypeNames ()
    {

        Map<String, String> singular = Environment.objectTypeNamesSingular;
        Map<String, String> plural = Environment.objectTypeNamesPlural;

        // Load the names from the configurable types.
        for (UserConfigurableObjectType t : Environment.userConfigObjTypes)
        {

            if (t.getUserObjectType () != null)
            {
/*
                plural.put (t.getUserObjectType (),
                            t.getObjectTypeNamePlural ());

                singular.put (t.getUserObjectType (),
                              t.getObjectTypeName ());
*/
            } else {

                if (t.isAssetObjectType ())
                {

                    plural.put ("asset:" + t.getKey (),
                                t.getObjectTypeNamePlural ());
                    singular.put ("asset:" + t.getKey (),
                                  t.getObjectTypeName ());

                }

            }

        }
/*
        // TODO: Fix this, special for now.
        UserConfigurableObjectType chapterType = Environment.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);

        if (chapterType != null)
        {
xxx
            plural.put (Chapter.OBJECT_TYPE,
                        chapterType.getObjectTypeNamePlural ());
            singular.put (Chapter.OBJECT_TYPE,
                          chapterType.getObjectTypeName ());

        }
*/

    }

    private static void loadLegacyObjectTypeNames (Element root)
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
/*
        // Load the names from the configurable types.
        for (UserConfigurableObjectType t : Environment.userConfigObjTypes)
        {

            if (t.getUserObjectType () != null)
            {

                plural.put (t.getUserObjectType (),
                            t.getObjectTypeNamePlural ());

                singular.put (t.getUserObjectType (),
                              t.getObjectTypeName ());

            } else {

                if (t.isAssetObjectType ())
                {

                    plural.put ("asset:" + t.getKey (),
                                t.getObjectTypeNamePlural ());
                    singular.put ("asset:" + t.getKey (),
                                  t.getObjectTypeName ());

                }

            }

        }

        // TODO: Fix this, special for now.
        UserConfigurableObjectType chapterType = Environment.getUserConfigurableObjectType (Chapter.OBJECT_TYPE);

        if (chapterType != null)
        {

            plural.put (Chapter.OBJECT_TYPE,
                        chapterType.getObjectTypeNamePlural ());
            singular.put (Chapter.OBJECT_TYPE,
                          chapterType.getObjectTypeName ());

        }
*/

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

        if (Environment.isDebugModeEnabled ())
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

    public static int getIconPixelWidthForType (int type)
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

        return size;

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

            int size = Environment.getIconPixelWidthForType (type);

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

            if (Environment.isDebugModeEnabled ())
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

            if (Environment.isDebugModeEnabled ())
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

            if (Environment.isDebugModeEnabled ())
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

        return com.quollwriter.ui.UIUtils.getBorderColor ();

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

        StringBuilder b = new StringBuilder ();

        BufferedReader r = null;

        try
        {

            r = new BufferedReader (new InputStreamReader (is, "utf-8"));

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

                if (r != null)
                {

                    r.close ();

                }

            } catch (Exception e)
            {

                // Ignore...  What could we do otherwise?

            }

        }

        return b.toString ();

    }

    public static File getProjectInfoDBFile ()
    {

        File dir = null;

        String dv = UserProperties.get (Constants.PROJECT_INFO_DB_DIR_PROPERTY_NAME);

        if (dv != null)
        {

            dir = new File (dv);

        } else {

            dir = Environment.getUserQuollWriterDir ();

        }

        return new File (dir, Constants.PROJECT_INFO_DB_FILE_NAME_PREFIX);

    }

    public static int getProjectInfoSchemaVersion ()
    {

        File f = new File (Environment.getProjectInfoDBFile ().getPath () + Constants.H2_DB_FILE_SUFFIX);

        // See if we already have a project info db.
        if (f.exists ())
        {

            return Environment.projectInfoSchemaVersion;

        }

        // No file, so need to create the schema.
        return 0;

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

        if (UserProperties.getAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME))
        {

            parms += "beta=true&";

        }

        String lastVersionCheckTime = UserProperties.get (Constants.LAST_VERSION_CHECK_TIME_PROPERTY_NAME);

        if (lastVersionCheckTime != null)
        {

            parms += "since=" + lastVersionCheckTime;

        }

        return Environment.getSupportUrl (Constants.GET_LATEST_VERSION_PAGE_PROPERTY_NAME,
                                          parms);

    }

    public static void doNewsAndVersionCheck (final AbstractViewer viewer)
    {

        if (Environment.doneVersionCheck)
        {

            return;

        }

        String updaterClass = UserProperties.get (Constants.UPDATER_CLASS_PROPERTY_NAME,
                                                  null);

        Class updaterCl = null;

        try
        {

            updaterCl = Class.forName (updaterClass);

            if (!QuollWriterUpdater.class.isAssignableFrom (updaterCl))
            {

                Environment.logError (String.format ("Expected class: %s, given by property: %s to be an instance of: %s",
                                                     updaterClass,
                                                     Constants.UPDATER_CLASS_PROPERTY_NAME,
                                                     QuollWriterUpdater.class.getName ()));

                updaterCl = null;

            }

        } catch (Exception e) {

            Environment.logError (String.format ("Unable to load class: %s given by property: %s",
                                                 updaterClass,
                                                 Constants.UPDATER_CLASS_PROPERTY_NAME),
                                  e);

        }

        QuollWriterUpdater updater = null;

        if (updaterCl != null)
        {

            try
            {

                updater = (QuollWriterUpdater) updaterCl.newInstance ();

            } catch (Exception e) {

                Environment.logError (String.format ("Unable to create new instance of: %s, given by property: %s",
                                                     updaterClass,
                                                     Constants.UPDATER_CLASS_PROPERTY_NAME),
                                      e);

            }

        }

        if ((UserProperties.getAsBoolean (Constants.DO_AUTO_UPDATE_CHECK_PROPERTY_NAME))
            &&
            (updater != null)
           )
        {

            Environment.doneVersionCheck = true;

            final QuollWriterUpdater _updater = updater;

            Runner r = new Runner ()
            {

                public void run ()
                {

                    try
                    {

                        _updater.doUpdate (viewer);

/*
Removed for now
TODO: Add back in when appropriate.
                        // Get the news.
                        List news = (List) data.get ("news");

                        if (news != null)
                        {

                            final Set<String> seenIds = new HashSet ();

                            String seenNewsIds = UserProperties.get (Constants.SEEN_NEWS_IDS_PROPERTY_NAME);

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
                                                                          viewer);
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

                                final ActionListener removeNot = viewer.addNotification (ib,
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

                                        UserProperties.set (Constants.SEEN_NEWS_IDS_PROPERTY_NAME,
                                                            sb.toString ());

                                        removeNot.actionPerformed (ev);

                                    }

                                });

                            }

                        }
*/
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

    public static Double parseToDouble (String v)
                                 throws GeneralException
    {

        if (v == null)
        {

            return null;

        }

        try
        {

            return Double.valueOf (Environment.floatNumFormat.parse (v,
                                                                     new ParsePosition (0)).doubleValue ());

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert: " +
                                        v +
                                        " to a double.",
                                        e);

        }

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

                        if (v != null)
                        {

                            el.addContent (v.toString ());

                        }

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

    public static Map<String, Set<String>> getAchievedAchievementIds (AbstractViewer viewer)
    {

        return Environment.achievementsManager.getAchievedAchievementIds (viewer);

    }

    public static void removeAchievedAchievement (String         achievementType,
                                                  String         id,
                                                  AbstractViewer viewer)
                                           throws Exception
    {

        Environment.achievementsManager.removeAchievedAchievement (achievementType,
                                                                   id,
                                                                   viewer);

    }

    public static void showAchievement (AchievementRule ar)
    {

        AbstractViewer v = Environment.getFocusedViewer ();

        if (v != null)
        {

            v.showAchievement (ar);

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

    public static FullScreenTextProperties getFullScreenTextProperties ()
    {

        return Environment.fullScreenTextProps;

    }

    public static ProjectTextProperties getProjectTextProperties ()
    {

        return Environment.projectTextProps;

    }

    public static boolean isInFullScreen ()
    {

        return Environment.getFullScreenProjectViewer () != null;

    }

    public static void doForOpenProjects (final String              projectType,
                                          final ProjectViewerAction act)
    {

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                for (ProjectInfo p : Environment.openProjects.keySet ())
                {

                    AbstractProjectViewer pv = Environment.openProjects.get (p);

                    if ((projectType == null)
                        ||
                        (p.getType ().equals (projectType))
                       )
                    {

                        act.doAction (pv);

                    }

                }

            }

        });

    }

    public static void doForOpenProjectViewers (final Class               projectViewerType,
                                                final ProjectViewerAction act)
    {

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                for (ProjectInfo p : Environment.openProjects.keySet ())
                {

                    AbstractProjectViewer pv = Environment.openProjects.get (p);

                    if ((projectViewerType == null)
                        ||
                        (projectViewerType.isAssignableFrom (pv.getClass ()))
                       )
                    {

                        act.doAction (pv);

                    }

                }

            }

        });

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

    public static void relaunchLanding ()
                                 throws Exception
    {

        if (Environment.landingViewer != null)
        {

            Environment.landingViewer.removeProjectEventListener (Environment.achievementsManager);

            Environment.landingViewer.close (false,
                                             null);

            Environment.landingViewer = null;

        }

        Environment.showLanding ();

    }

    public static Landing getLanding ()
    {

        return Environment.landingViewer;

    }

    public static void showLanding ()
    {

        if (Environment.landingViewer == null)
        {

            try
            {

                Environment.landingViewer = new Landing ();

                Environment.landingViewer.init ();

                Environment.landingViewer.addProjectEventListener (Environment.achievementsManager);

                Environment.userPropertyHandlers.get (Constants.PROJECT_STATUSES_PROPERTY_NAME).addPropertyChangedListener (new PropertyChangedListener ()
                {

                    @Override
                    public void propertyChanged (PropertyChangedEvent ev)
                    {

                        if (ev.getChangeType ().equals (UserPropertyHandler.VALUE_CHANGED))
                        {

                            List<ProjectInfo> toSave = new ArrayList ();

                            String oldValue = (String) ev.getOldValue ();
                            String newValue = (String) ev.getNewValue ();

                            // Change the type for all notes with the old type.
                            Set<ProjectInfo> pis = null;

                            try
                            {

                                pis = Environment.getAllProjectInfos ();

                            } catch (Exception e) {

                                Environment.logError ("Unable to save: " +
                                                      toSave +
                                                      " with new type: " +
                                                      ev.getNewValue (),
                                                      e);

                                UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                          Environment.getUIString (LanguageStrings.project,
                                                                                   LanguageStrings.actions,
                                                                                   LanguageStrings.changestatus,
                                                                                   LanguageStrings.actionerror));
                                //"Unable to change status");

                                return;

                            }
                            for (ProjectInfo pi : pis)
                            {

                                if (oldValue.equals (pi.getStatus ()))
                                {

                                    pi.setStatus (newValue);

                                    toSave.add (pi);

                                }

                            }

                            if (toSave.size () > 0)
                            {

                                try
                                {

                                    Environment.updateProjectInfos (toSave);

                                } catch (Exception e)
                                {

                                    Environment.logError ("Unable to save: " +
                                                          toSave +
                                                          " with new type: " +
                                                          ev.getNewValue (),
                                                          e);

                                    UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                              Environment.getUIString (LanguageStrings.project,
                                                                                       LanguageStrings.actions,
                                                                                       LanguageStrings.changestatus,
                                                                                       LanguageStrings.actionerror));
                                                              //"Unable to change status");

                                }

                            }

                        }

                    }

                });

            } catch (Exception e) {

                Environment.logError ("Unable to create landing viewer",
                                      e);

                UIUtils.showErrorMessage (null,
                                          Environment.getUIString (LanguageStrings.allprojects,
                                                                   LanguageStrings.actionerror));
                                          //"Unable to show all {projects}, please contact Quoll Writer support for assistance.");

                return;

            }

        }

        Environment.landingViewer.setVisible (true);

        Environment.landingViewer.toFront ();

    }

    public static File getDefaultSaveProjectDir ()
    {

        try
        {

            List<ProjectInfo> pis = new ArrayList (Environment.getAllProjectInfos (Project.NORMAL_PROJECT_TYPE));

            Collections.sort (pis,
                              new ProjectInfoSorter ());

            if (pis.size () > 0)
            {

                return pis.get (0).getProjectDirectory ().getParentFile ();

            }

        } catch (Exception e) {

            Environment.logError ("Unable to get last edited project directory",
                                  e);

        }

        return new File (Environment.getUserQuollWriterDir (),
                         Constants.DEFAULT_PROJECTS_DIR_NAME);

    }

    public static void removeUserProjectEventListener (ProjectEventListener l)
    {

        Environment.userProjectEventListeners.remove (l);

    }

    public static void addUserProjectEventListener (ProjectEventListener l)
    {

        Environment.userProjectEventListeners.put (l,
                                                   Environment.listenerFillObj);

    }

    public static void fireUserProjectEvent (Object source,
                                             String type,
                                             String action,
                                             Object contextObject)
    {

        Environment.fireUserProjectEvent (new ProjectEvent (source,
                                                            type,
                                                            action,
                                                            contextObject));

    }

    public static void fireUserProjectEvent (Object source,
                                             String type,
                                             String action)
    {

        Environment.fireUserProjectEvent (new ProjectEvent (source,
                                                            type,
                                                            action));

    }

    public static void fireUserProjectEvent (final ProjectEvent ev)
    {

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent aev)
            {

                Set<ProjectEventListener> ls = null;

                // Get a copy of the current valid listeners.
                synchronized (Environment.userProjectEventListeners)
                {

                    ls = new LinkedHashSet (Environment.userProjectEventListeners.keySet ());

                }

                for (ProjectEventListener l : ls)
                {

                    l.eventOccurred (ev);

                }

            }

        });

    }

    public static boolean isPlaySoundOnKeyStroke ()
    {

        return Environment.playSoundOnKeyStroke;

    }

    public static void playKeyStrokeSound ()
    {

        if (Environment.keyStrokeSound == null)
        {

            return;

        }

        if (!Environment.playSoundOnKeyStroke)
        {

            return;

        }

        try
        {

            if (Environment.keyStrokeSound.isRunning ())
            {

                Environment.keyStrokeSound.stop ();

            }

            Environment.keyStrokeSound.setFramePosition (0);

            Environment.keyStrokeSound.start ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to play key stroke sound",
                                  e);

            Environment.playSoundOnKeyStroke = false;

        }

    }

    public static void setKeyStrokeSoundFile (File f)
    {

        try
        {

            InputStream is = null;

            if (f != null)
            {

                try
                {

                    is = new BufferedInputStream (new FileInputStream (f));

                } catch (Exception e) {

                    // Ignore.

                }

            }

            if (is == null)
            {

                // Play the default.
                is = new BufferedInputStream (Environment.getResourceStream (Constants.DEFAULT_KEY_STROKE_SOUND_FILE));

            }

            // Get the clip.
            AudioInputStream ais = AudioSystem.getAudioInputStream (is);

            Environment.keyStrokeSound = AudioSystem.getClip ();

            Environment.keyStrokeSound.open (ais);

        } catch (Exception e) {

            Environment.logError ("Unable to set key stroke sound file",
                                  e);

            UserProperties.remove (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

            return;

        }

        if (f != null)
        {

            UserProperties.set (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME,
                                f.getPath ());

        }

    }

    public static void setPlaySoundOnKeyStroke (boolean v)
    {

        Environment.playSoundOnKeyStroke = v;

        UserProperties.set (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                            v);

        Environment.fireUserProjectEvent (new Object (),
                                          ProjectEvent.TYPE_WRITER_SOUND,
                                          (v ? ProjectEvent.ON : ProjectEvent.OFF));


    }

    public static String getI18nString (String k)
    {

        return k;

    }

    public static int getSessionWordCount ()
    {

        return Environment.userSession.getCurrentSessionWordCount ();

    }

    public static List<Session> getSessions (int daysPast)
                                      throws GeneralException
    {

        return Environment.projectInfoManager.getSessions (daysPast);

    }

    public static TargetsData getUserTargets ()
    {

        return Environment.targets;

    }

    public static void saveUserTargets ()
    {

        try
        {

            Environment.saveUserProperties ();

        } catch (Exception e) {

            Environment.logError ("Unable to update user properties",
                                  e);

        }

    }

    public static TargetsData getDefaultUserTargets ()
    {

        TargetsData td = new TargetsData (UserProperties.getProperties ().getParentProperties ());

        return td;

    }

    /**
     * Un-schedule the scheduledfuture, this is returned from Environment.schedule.
     *
     * @param f The scheduledfuture to remove from the executor service.
     * @returns Whether it was successfully removed.
     */
    public static void unschedule (ScheduledFuture f)
    {

        if (f == null)
        {

            return;

        }

        // Let it run to completion.
        f.cancel (false);

        Environment.generalTimer.purge ();

    }

    /**
     * Schedule the runnable to run after delay and repeat (use -1 or 0 for no repeat).
     *
     * @param r The runnable to run.
     * @param delay The delay, in millis.
     * @param repeat The repeat time, in millis.
     */
    public static ScheduledFuture schedule (final Runnable r,
                                            final long     delay,
                                            final long     repeat)
    {

        if (Environment.generalTimer == null)
        {

            Environment.logError ("Unable to schedule timer is no longer valid.");

            return null;

        }

        if (r == null)
        {

            Environment.logError ("Unable to schedule, runnable is null.");

            return null;

        }

        if (repeat < 1)
        {

            return Environment.generalTimer.schedule (r,
                                                      delay,
                                                      TimeUnit.MILLISECONDS);

        } else {

            return Environment.generalTimer.scheduleAtFixedRate (r,
                                                                 delay,
                                                                 repeat,
                                                                 TimeUnit.MILLISECONDS);

        }

    }

    public static void addDoOnShutdown (Runnable r)
    {

        Environment.doOnShutdown.add (r);

    }

    public static File getUserObjectTypeIconFile (String objType)
    {

        return new File (Environment.getUserQuollWriterDir () + "/" + Constants.USER_OBJECT_TYPE_ICON_FILES_DIR + "/" + objType);

    }

    public static void setUserObjectTypeIcon (String        objType,
                                              BufferedImage image)
                                       throws Exception
    {

        ImageIO.write (image,
                       "png",
                       Environment.getUserObjectTypeIconFile (objType));

    }

    public static Set<UserConfigurableObjectType> getAssetUserConfigurableObjectTypes (boolean sortOnName)
    {

        Set<UserConfigurableObjectType> types = new LinkedHashSet ();

        for (UserConfigurableObjectType t : Environment.userConfigObjTypes)
        {

            if (t.isAssetObjectType ())
            {

                types.add (t);

            }

        }

        if (sortOnName)
        {

            List<UserConfigurableObjectType> stypes = new ArrayList (types);

            Collections.sort (stypes,
                              new Comparator<UserConfigurableObjectType> ()
                              {

                                  @Override
                                  public int compare (UserConfigurableObjectType o1,
                                                      UserConfigurableObjectType o2)
                                  {

                                      return o1.getObjectTypeName ().compareTo (o2.getObjectTypeName ());

                                  }

                              });

            types = new LinkedHashSet (stypes);

        }

        return types;

    }

    public static UserConfigurableObjectTypeField getUserConfigurableObjectTypeField (long key)
                                                                               throws GeneralException
    {

        return (UserConfigurableObjectTypeField) Environment.projectInfoManager.getObjectByKey (UserConfigurableObjectTypeField.class,
                                                                                                key,
                                                                                                null,
                                                                                                null,
                                                                                                true);

    }

    public static UserConfigurableObjectType getUserConfigurableObjectType (long key)
                                                                     throws GeneralException
    {

        return (UserConfigurableObjectType) Environment.projectInfoManager.getObjectByKey (UserConfigurableObjectType.class,
                                                                                           key,
                                                                                           null,
                                                                                           null,
                                                                                           true);

    }

    public static boolean hasUserConfigurableObjectType (String userObjType)
    {

        return Environment.getUserConfigurableObjectType (userObjType) != null;

    }

    public static UserConfigurableObjectType getUserConfigurableObjectType (String userObjType)
    {

        for (UserConfigurableObjectType t : Environment.userConfigObjTypes)
        {

            if ((t.getUserObjectType () != null)
                &&
                (t.getUserObjectType ().equals (userObjType))
               )
            {

                return t;

            }

        }

        return null;

    }

    public static void removeUserConfigurableObjectType (UserConfigurableObjectType type)
                                                  throws GeneralException
    {

        Environment.userConfigObjTypes.remove (type);

        Environment.projectInfoManager.deleteObject (type,
                                                     true);

        type.removePropertyChangedListener (Environment.userConfigurableObjectTypeNameListener);

        String id = type.getObjectTypeId ();

        Environment.objectTypeNamesSingular.remove (id);

        Environment.objectTypeNamesPlural.remove (id);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (type,
                                          ProjectEvent.USER_OBJECT_TYPE,
                                          ProjectEvent.DELETE,
                                          type);

    }

    public static void updateUserConfigurableObjectType (UserConfigurableObjectType type)
                                                  throws GeneralException
    {

        if (!Environment.userConfigObjTypes.contains (type))
        {

            Environment.addUserConfigurableObjectType (type);

            return;

        }

        Environment.projectInfoManager.saveObject (type);

        String id = type.getObjectTypeId ();

        Environment.objectTypeNamesSingular.put (id,
                                                 type.getObjectTypeName ());

        Environment.objectTypeNamesPlural.put (id,
                                               type.getObjectTypeNamePlural ());

        // Tell all projects about it.
        Environment.fireUserProjectEvent (type,
                                          ProjectEvent.USER_OBJECT_TYPE,
                                          ProjectEvent.CHANGED,
                                          type);

    }

    public static void addUserConfigurableObjectType (UserConfigurableObjectType type)
                                               throws GeneralException
    {

        if (type.getKey () == null)
        {

            Environment.projectInfoManager.saveObject (type);

        }

        Environment.userConfigObjTypes.add (type);

        // Register ourselves as a listener for the name changes.
        type.addPropertyChangedListener (Environment.userConfigurableObjectTypeNameListener);

        String id = type.getObjectTypeId ();

        if (!type.isLegacyObjectType ())
        {

            Environment.objectTypeNamesSingular.put (id,
                                                     type.getObjectTypeName ());

            Environment.objectTypeNamesPlural.put (id,
                                                   type.getObjectTypeNamePlural ());

        }

        // Tell all projects about it.
        Environment.fireUserProjectEvent (type,
                                          ProjectEvent.USER_OBJECT_TYPE,
                                          ProjectEvent.NEW,
                                          type);

    }

    public static void removeUserConfigurableObjectTypeField (UserConfigurableObjectTypeField field)
                                                       throws GeneralException
    {

        Environment.projectInfoManager.deleteObject (field,
                                                     true);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (field,
                                          ProjectEvent.USER_OBJECT_TYPE_FIELD,
                                          ProjectEvent.DELETE,
                                          field);

    }

    public static void updateUserConfigurableObjectTypeField (UserConfigurableObjectTypeField field)
                                                       throws GeneralException
    {

        String ev = ProjectEvent.CHANGED;

        if (field.getKey () == null)
        {

            ev = ProjectEvent.NEW;

        }

        Environment.projectInfoManager.saveObject (field);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (field,
                                          ProjectEvent.USER_OBJECT_TYPE_FIELD,
                                          ev,
                                          field);

        Environment.fireUserProjectEvent (field.getUserConfigurableObjectType (),
                                          ProjectEvent.USER_OBJECT_TYPE,
                                          ProjectEvent.CHANGED,
                                          field.getUserConfigurableObjectType ());

    }

    /**
     * Save a tag, this will either create or update it.
     *
     * @param tag The tag.
     * @throws GeneralException If the tag can't be saved.
     */
    public static void saveTag (Tag tag)
                         throws GeneralException
    {

        String ev = ProjectEvent.CHANGED;

        if (tag.getKey () == null)
        {

            ev = ProjectEvent.NEW;

        }

        Environment.projectInfoManager.saveObject (tag);

        if (ev.equals (ProjectEvent.NEW))
        {

            Environment.tags.add (tag);

        }

        // Tell all projects about it.
        Environment.fireUserProjectEvent (tag,
                                          ProjectEvent.TAG,
                                          ev,
                                          tag);

    }

    /**
     * Delete a tag.
     *
     * @param tag The tag to delete.
     * @throws GeneralException If the delete goes wrong.
     */
    public static void deleteTag (Tag tag)
                           throws GeneralException
    {

        Environment.projectInfoManager.deleteObject (tag,
                                                     true);

        Environment.tags.remove (tag);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (tag,
                                          ProjectEvent.TAG,
                                          ProjectEvent.DELETE,
                                          tag);

    }

    /**
     * Get a tag by its key.
     *
     * @param key The key.
     * @return The tag.
     * @throws GeneralException If the tag can't be retrieved.
     */
    public static Tag getTagByKey (long key)
                            throws GeneralException
    {

        Set<Tag> tags = Environment.getAllTags ();

        for (Tag t : tags)
        {

            if (t.getKey () == key)
            {

                return t;

            }

        }

        return null;

    }

    /**
     * Get a tag by name.
     *
     * @return The tag, if found.
     * @throws GeneralException If the tags can't be retrieved from the db.
     */
    public static Tag getTagByName (String name)
                             throws GeneralException
    {

        Set<Tag> tags = Environment.getAllTags ();

        for (Tag t : tags)
        {

            if (t.getName ().equalsIgnoreCase (name))
            {

                return t;

            }

        }

        return null;

    }

    /**
     * Get all the tags.
     *
     * @return The tags.
     * @throws GeneralException if the tags can't be retrieved from the db.
     */
    public static Set<Tag> getAllTags ()
                                throws GeneralException
    {

        if (Environment.tags == null)
        {

            Environment.tags = new LinkedHashSet (Environment.projectInfoManager.getObjects (Tag.class,
                                                                                             null,
                                                                                             null,
                                                                                             true));

        }

        return Environment.tags;

    }

    public static String getDefaultTextAlignment ()
    {

        return getUIString (textalignments,left);

    }

    public static String getDefaultChapterName ()
    {

        return getUIString (general,defaultchaptername);

    }

    public static String getUIString (String... ids)
    {

        return Environment.getUIString (Arrays.asList (ids));

    }

    public static String getUIString (List<String> prefix,
                                      String       id)
    {

        List<String> ids = new ArrayList (prefix);

        ids.add (id);

        return Environment.getUIString (ids);

    }

    public static String getUIString (List<String> prefix,
                                      String...    ids)
    {

        List<String> _ids = new ArrayList (prefix);

        for (String s : ids)
        {

            _ids.add (s);

        }

        return Environment.getUIString (_ids);

    }

    private static String getUIString (List<String> ids)
    {

        String s = Environment.uiLanguageStrings.getString (ids);

        if (s == null)
        {

            s = LanguageStrings.toId (ids);

        }

        return s;

    }

    public static LanguageStringsEditor editUILanguageStrings (LanguageStrings userStrings,
                                                               Version         baseQWVersion)
    {

        LanguageStringsEditor lse = Environment.getUILanguageStringsEditor (userStrings);

        if (lse != null)
        {

            lse.toFront ();

            return lse;

        }

         try
         {

             LanguageStringsEditor _ls = new LanguageStringsEditor (userStrings,
                                                                    baseQWVersion);
             _ls.init ();

             return _ls;

         } catch (Exception e) {

             Environment.logError ("Unable to create language strings editor",
                                   e);

             UIUtils.showErrorMessage (null,
                                       getUIString (uilanguage,edit,actionerror));

            return null;

         }

    }

    public static LanguageStringsEditor getUILanguageStringsEditor (LanguageStrings ls)
    {

        for (AbstractViewer v : Environment.openViewers)
        {

            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if (lse.getUserLanguageStrings ().equals (ls))
                {

                    return lse;

                }

            }

        }

        return null;

    }

}
