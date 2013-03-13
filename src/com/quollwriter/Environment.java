package com.quollwriter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Font;
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
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import com.quollwriter.importer.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.rules.*;

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

    public final static PrintStream nullOut = new PrintStream (new OutputStream ()
        {
            public void close ()
            {
            }

            public void flush ()
            {
            }

            public void write (byte[] b)
            {
            }

            public void write (byte[] b,
                               int    off,
                               int    len)
            {
            }

            public void write (int b)
            {
            }

        });

    // public static String WINDOW_NAME_PREFIX = "Quoll Writer - ";
    // public static String DEFAULT_CHAPTER_NAME = "Chapter 1";

    private static Map<Project, AbstractProjectViewer> openProjects = new HashMap ();

    public static Map defaultObjectProperties = new HashMap ();

    // public static DictionaryProvider dictionaryProvider = null;
    private static SynonymProvider                    synonymProvider = null;
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

    public static boolean  debugMode = false;
    private static boolean doneVersionCheck = false;
    public static boolean  isWindows = false;
    public static boolean  isMac = false;
    public static boolean  isLinux = false;

    private static List<File> installJarFilesToDelete = new ArrayList ();

    private static boolean upgradeRequired = false;
    
    private static DecimalFormat numFormat = new DecimalFormat ("###,###");

    private static Map<String, Image> backgroundImages = new HashMap ();

    public static String quollWriterWebsite = null;

    private static AchievementsManager achievementsManager = null;

    public class XMLConstants
    {

        public static final String projects = "projects";
        public static final String directory = "directory";
        public static final String name = "name";
        public static final String lastEdited = "lastEdited";
        public static final String encrypted = "encrypted";
        public static final String noCredentials = "noCredentials";
        public static final String type = "type";

    }

    public static AbstractProjectViewer getProjectViewer (Project p)
    {
        
        return Environment.openProjects.get (p);
        
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

    public static String getWordTypes (String word)
                                throws GeneralException
    {

        return Environment.synonymProvider.getWordTypes (word);

    }

    public static void setSynonymProviderUseCache (boolean c)
    {

        Environment.synonymProvider.setUseCache (c);

    }

    public static SynonymProvider getSynonymProvider ()
    {
        
        return Environment.synonymProvider;
        
    }

    public static boolean hasSynonym (String word)
                                      throws GeneralException
    {
        
        return Environment.synonymProvider.hasSynonym (word);
        
    }

    public static Synonyms getSynonyms (String word)
                                 throws GeneralException
    {

        return Environment.synonymProvider.getSynonyms (word);

    }

    public static String getObjectTypeName (String t)
    {

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

    }

    public static int getPercent (float t,
                                  float b)
    {
        
        return (int) ((t / b) * 100);
        
    }
    
    public static String getObjectTypeNamePlural (String t)
    {

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

    public static boolean synonymLookupsSupported ()
    {

        return Environment.synonymProvider != null;

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

                    if (start > 0)
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

    public static boolean openLastEditedProject ()
                                          throws Exception
    {

        List projs = new ArrayList (Environment.getAllProjects ());

        if (projs.size () > 0)
        {

            Collections.sort (projs,
                              new ProjectSorter ());

            Project p = (Project) projs.get (0);

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

    public static AbstractProjectViewer openProject (Project p)
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

            Startup.ss.setProgress (75);

            String password = null;

            if ((p.isEncrypted ()) &&
                (p.getFilePassword () == null))
            {

                Startup.ss.finish ();

                // Need to pop up a window to ask for the password.
                final JPasswordField pwdField = new JPasswordField ();

                Object[] array = { Environment.getObjectTypeName (Project.OBJECT_TYPE) + ": " + p.getName () + " is encrypted, please enter the password.", pwdField };

                // Ask for the password.
                JOptionPane op = new JOptionPane (array,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  JOptionPane.OK_CANCEL_OPTION);

                final JDialog d = op.createDialog (UIUtils.getFrameTitle ("Enter password for " + Environment.getObjectTypeName (Project.OBJECT_TYPE)));

                d.setIconImage (Environment.getWindowIcon ().getImage ());
                d.pack ();
                d.setVisible (true);
                d.toFront ();

                d.dispose ();

                password = new String (pwdField.getPassword ());

                if ((password != null) &&
                    (password.trim ().length () == 0))
                {

                    password = null;

                }

            }

            pv.openProject (p,
                            password);

            Startup.ss.setProgress (90);

            // Need to get the object again because the one passed in could be the one from the open.
            Environment.openProjects.put (pv.getProject (),
                                          pv);

        }

        return pv;

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

    public static void logMessage (String m)
    {
/*
        if (!Environment.debugMode)
        {

            return;

        }
*/
        Environment.generalLog.logInformationMessage (m);

    }

    public static void logError (String m)
    {

        if (Environment.errorLog == null)
        {
            
            System.err.println (m);
            
            return;
            
        }
    
        Environment.errorLog.logError (m,
                                       null,
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
            
            System.err.println (m);
            ex.printStackTrace ();
            
            return;
            
        }
    
        Environment.errorLog.logError (m,
                                       ex,
                                       null);

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

        if (p.getType ().equals (Project.WARMUPS_PROJECT_TYPE))
        {

            return new WarmupsViewer ();

        }

        throw new GeneralException ("Project type: " +
                                    p.getType () +
                                    " is not supported.");

    }

    public static void saveProjectsFile (Set<Project> projs)
                                  throws Exception
    {

        Element root = new Element (Environment.XMLConstants.projects);

        Iterator<Project> iter = projs.iterator ();

        while (iter.hasNext ())
        {

            Project p = iter.next ();

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

        }

        JDOMUtils.writeElementToFile (root,
                                      Environment.getProjectsFile (),
                                      true);

    }

    public static Set getAllProjects ()
                               throws Exception
    {

        Set ret = new LinkedHashSet ();

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

            File dir = new File (directory);

            Project p = new Project (name);
            p.setProjectDirectory (dir);
            p.setEncrypted (encrypted);
            p.setNoCredentials (noCredentials);

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

    public static File getLogDir ()
    {

        File d = new File (Environment.getUserQuollWriterDir ().getPath () + Constants.LOGS_DIR);

        d.mkdirs ();

        return d;

    }

    public static File getUserPropertiesFile ()
    {

        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.PROPERTIES_FILE_NAME);

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

    public static String formatDate (Date d)
    {

        return Environment.dateFormatter.format (d);

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

        Environment.dateFormatter = new SimpleDateFormat ("dd MMM yyyy HH:mm");

        UIManager.put("SplitPane.background", UIUtils.getComponentColor ());
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 1, 0));
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

        if (Environment.debugMode)
        {

            Environment.quollWriterWebsite = Environment.getProperty (Constants.QUOLL_WRITER_DEBUG_WEBSITE_PROPERTY_NAME);

        } else {

            Environment.quollWriterWebsite = Environment.getProperty (Constants.QUOLL_WRITER_WEBSITE_PROPERTY_NAME);
            
        }

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

        /*
        List dictFiles = new ArrayList ();

        String dFiles = Environment.getProperty (Constants.DICTIONARY_FILES_PROPERTY_NAME);

        if (dFiles != null)
        {

            StringTokenizer t = new StringTokenizer (dFiles,
                                                     ",");

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                dictFiles.add (Environment.class.getResourceAsStream (Constants.DICTIONARIES_DIR + tok));

            }

        }

        File userDict = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.USER_DICTIONARY_FILE_NAME);

        userDict.createNewFile ();

        Environment.dictionaryProvider = new DictionaryProvider (dictFiles,
                                                                 userDict);
         */
        Startup.ss.setProgress (40);

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

                Environment.synonymProvider = (SynonymProvider) c.newInstance ();

            } catch (Exception e)
            {

                // Record the error but don't barf, it's just a service that isn't available
                Environment.logError ("Unable to create new instance of synonym provider class: " +
                                      synCl +
                                      " specified by property: " +
                                      Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME,
                                      e);

            }

        }

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

    public static Image getTransparentImage ()
    {

        return new ImageIcon (Environment.class.getResource (Constants.IMGS_DIR + Constants.TRANSPARENT_PNG_NAME)).getImage ();

    }

    public static ImageIcon getLogo ()
    {

        return new ImageIcon (Environment.class.getResource (Constants.IMGS_DIR + Constants.LOGO_PNG_NAME));

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
                                                                                                                                                                                                                                          "_") + ".html'>View the changes.</a>");
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
                                                                                             120);

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

                                        /*
                                            int res = JOptionPane.showConfirmDialog (pv,
                                                                                     "A new version of " + Constants.QUOLL_WRITER_NAME + " is available.\n\nWould you like to download and install it now?",
                                                                                     "New Version of " + Constants.QUOLL_WRITER_NAME + " Available",
                                                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                                                     JOptionPane.QUESTION_MESSAGE);

                                            if (res != JOptionPane.YES_OPTION)
                                            {

                                                return;

                                            }
                                         */
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

    public static String formatNumber (Number n)
    {

        if (n == null)
        {
            
            return "N/A";
            
        }
    
        return Environment.numFormat.format (n);

    }

    public static void sendMessageToSupport (String type,
                                             Map    info)
                                      throws Exception
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

        if ((detail == null) ||
            (!detail.equals ("SUCCESS")))
        {

            throw new GeneralException ("Unable to get send message to support");

        }

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
