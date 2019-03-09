package com.quollwriter;

import java.nio.channels.*;

import java.io.*;
import javax.imageio.*;
import java.net.*;

import java.text.*;
import java.util.*;
import java.util.function.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.media.*;
import javafx.application.*;
import javafx.scene.image.*;

import org.jdom.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.utils.*;

import com.gentlyweb.logging.Logger;

import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.editors.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.text.rules.*;
import com.quollwriter.importer.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.events.ProjectInfoChangedEvent;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

/*
 THis is the new version...
 */

public class Environment
{

    public static PrintStream out = null;

    private static Logger generalLog = null;
    private static Logger errorLog = null;
    private static Logger sqlLog = null;

    private static Version appVersion = null;
    //private static String appVersion = null;
    //private static boolean betaVersion = false;
    private static int    schemaVersion = 0;
    private static int    projectInfoSchemaVersion = 0;

    private static SimpleDateFormat dateFormatter = null;
    private static SimpleDateFormat timeFormatter = null;

    private static BooleanProperty debugModeProp = new SimpleBooleanProperty ();
    private static boolean doneVersionCheck = false;
    public static boolean  isWindows = false;
    public static boolean  isMac = false;
    public static boolean  isLinux = false;
    private static boolean isFirstUse = false;

    private static long styleSheetLastModified = 0;

    private static DecimalFormat numFormat = new DecimalFormat ("###,###");
    private static DecimalFormat floatNumFormat = new DecimalFormat ("###,###.#");

    private static FileLock lock = null;
    private static ScheduledThreadPoolExecutor generalTimer = null;

    private static AllProjectsViewer allProjectsViewer = null;
    private static SetProperty<AbstractViewer> openViewersProp = null;
    private static MapProperty<ProjectInfo, AbstractProjectViewer> openProjectsProp = null;
    private static SetProperty<ProjectInfo> allProjectsProp = null;

    private static ObservableMap<ProjectInfo, AbstractProjectViewer> openProjects = null;
    private static ObservableSet<ProjectInfo> allProjects = null;
    private static ObservableSet<AbstractViewer> openViewers = null;

    private static Set<Image> windowIcons = null;

    private static IntegerProperty startupProgressProp = null;
    private static BooleanProperty startupCompleteProp = null;

    private static BooleanProperty nightModeProp = null;

    public static Map<String, com.gentlyweb.properties.Properties> defaultObjectProperties = new HashMap<> ();

    private static ProjectInfoObjectManager projectInfoManager = null;

    private static UserSession userSession = null;

    private static TargetsData targets = null;

    // TODO Needs it's own manager.
    private static Set<Tag> tags = null;

    private static AchievementsManager achievementsManager = null;

    private static Set<UserConfigurableObjectType> userConfigObjTypes = new HashSet<> ();
    private static Map<String, String> objectTypeNamesSingular = new HashMap<> ();
    private static Map<String, String> objectTypeNamesPlural = new HashMap<> ();

    private static BooleanProperty playSoundOnKeyStrokeProp = null;
    private static ObjectProperty<Path> keyStrokeSoundPathProp = null;
    private static AudioClip keyStrokeSound = null;

    private static Map<String, com.quollwriter.ui.UserPropertyHandler> userPropertyHandlers = new HashMap<> ();
    private static ProjectTextProperties projectTextProps = null;
    private static FullScreenTextProperties fullScreenTextProps = null;

    // TODO Probably not needed, bind to the property. private static List<PropertyChangedListener> startupProgressListeners = new ArrayList<> ();

    private static Map<String, SynonymProvider> synonymProviders = new WeakHashMap<> ();

    private static Map<ProjectEventListener, Object> userProjectEventListeners = null;

    private static Path userQuollWriterDirPath = null;

    // Just used in the maps above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();

    private static int startupProgress = 0;

    static
    {

        // We use a synchronized weak hash map here so that we don't have to worry about all the
        // references since they will be transient compared to the potential length of the service
        // running.

        // Where possible listeners should de-register as normal but this just ensure that objects
        // that don't have a controlled pre-defined lifecycle (as opposed say to AbstractSideBar)
        // won't leak.
        Environment.userProjectEventListeners = Collections.synchronizedMap (new WeakHashMap ());

        try
        {

            // Put a wrapper around System.out to ensure that non ascii characters show up correctly when debugging.
            Environment.out = new java.io.PrintStream (System.out, true, "utf-8");

        } catch (Exception e) {

            Environment.out = System.out;

        }

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

    public static void init ()
                      throws Exception
    {

        Environment.startupProgressProp = new SimpleIntegerProperty (0);
        Environment.startupCompleteProp = new SimpleBooleanProperty (false);

        Environment.openProjects = FXCollections.observableMap (new HashMap<> ());
        Environment.openProjectsProp = new SimpleMapProperty<> (Environment.openProjects);

        Environment.openViewers = FXCollections.observableSet (new HashSet<> ());
        Environment.openViewersProp = new SimpleSetProperty<> (Environment.openViewers);

        Environment.allProjects = FXCollections.observableSet (new HashSet<> ());
        Environment.allProjectsProp = new SimpleSetProperty<> (Environment.allProjects);

        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler ()
        {

            @Override
            public void uncaughtException (Thread    t,
                                           Throwable e)
            {

                Environment.printStackTrace (e);
                Environment.logError ("Unexcepted error from thread: " + t.getId () + "\n\nStack Trace: \n" + Utils.getStackTrace (e));

            }

        });

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

        // Setup the user QW dir.
        Environment.userQuollWriterDirPath = Paths.get (System.getProperty ("user.home"), Constants.QUOLL_WRITER_DIR_NAME);

        Files.createDirectories (Environment.userQuollWriterDirPath);

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

        Environment.isWindows = System.getProperty ("os.name").startsWith ("Windows");

        Environment.isMac = System.getProperty ("os.name").startsWith ("Mac");

        Environment.isLinux = System.getProperty ("os.name").startsWith ("Linux");

        System.setProperty ("prism.lcdtext", "false");

        // TODO Handle night mode.
        Environment.nightModeProp = new SimpleBooleanProperty (false);

        // Setup our stream handler for the objectref protocol.
        URL.setURLStreamHandlerFactory (new ObjectRefURLStreamHandlerFactory ());

        Environment.dateFormatter = new SimpleDateFormat ("d MMM yyyy");
        Environment.timeFormatter = new SimpleDateFormat ("HH:mm");

        // Try and get a lock on the file.
        // Change to Path?

        Path l = Environment.getUserPath ("___quollwriter_lock.lock");

        // Try and gain a lock.
        FileLock lock = FileChannel.open (l,
                                          StandardOpenOption.CREATE,
                                          StandardOpenOption.READ,
                                          StandardOpenOption.WRITE).tryLock ();

        if (lock == null)
        {

            throw new OverlappingFileLockException ();

        }

        Environment.lock = lock;

        l.toFile ().deleteOnExit ();

        Environment.appVersion = new Version (Utils.getResourceFileAsString (Constants.VERSION_FILE).trim ());

        try
        {

            Environment.schemaVersion = Integer.parseInt (Utils.getResourceFileAsString (Constants.SCHEMA_VERSION_FILE).trim ());

        } catch (Exception e)
        {

            // Ignore.

        }

        try
        {

            Environment.projectInfoSchemaVersion = Integer.parseInt (Utils.getResourceFileAsString (Constants.PROJECT_INFO_SCHEMA_VERSION_FILE).trim ());

        } catch (Exception e)
        {

            // Ignore.

        }

        com.gentlyweb.properties.Properties sysProps = new com.gentlyweb.properties.Properties (Utils.getResourceStream (Constants.DEFAULT_PROPERTIES_FILE),
                                                                                                null);

        sysProps.setId ("system");

        // Temporarily set the user properties to the system properties.
        UserProperties.init (sysProps);

        UILanguageStringsManager.init ();

        com.gentlyweb.properties.Properties userProps = sysProps;

        Environment.incrStartupProgress ();

        // See if this is first use.
        Environment.isFirstUse = (Environment.getProjectInfoSchemaVersion () == 0);

        // Get the username and password.
        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

        Environment.projectInfoManager = new ProjectInfoObjectManager ();

        // TODO Change to use path.
        Environment.projectInfoManager.init (Environment.getProjectInfoDBPath ().toFile (),
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
            Path pf = UserProperties.getUserPropertiesPath ();

            if (Files.exists (pf))
            {

                try
                {

                    userProps = new com.gentlyweb.properties.Properties (pf.toFile (),
                                                                         Constants.GZIP_EXTENSION);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to load user properties from file: " +
                                          pf,
                                          e);

                }

                Files.delete (pf);

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

        try
        {

            Environment.loadUserObjectTypeNames ();

        } catch (Exception e) {

            Environment.logError ("Unable to load user object type names.",
                                  e);

        }

        // Add a property listener for name changes to user config object types.
        /*
        TODO
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
*/
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
        com.gentlyweb.properties.Properties sysDefProjProps = new com.gentlyweb.properties.Properties (Utils.getResourceStream (Constants.DEFAULT_PROJECT_PROPERTIES_FILE),
                                                                              UserProperties.getProperties ());

        Path defUserPropsFile = UserProperties.getUserDefaultProjectPropertiesPath ();

        if (Files.exists (defUserPropsFile))
        {

            com.gentlyweb.properties.Properties userDefProjProps = new com.gentlyweb.properties.Properties (defUserPropsFile.toFile (),
                                                                                                            Constants.GZIP_EXTENSION);

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

        Environment.playSoundOnKeyStrokeProp = new SimpleBooleanProperty (UserProperties.getAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME));

        Environment.playSoundOnKeyStrokeProp.addListener ((p, oldv, newv) ->
        {

            UserProperties.set (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                                newv);

            Environment.fireUserProjectEvent (ProjectEvent.Type.typewritersound,
                                              (newv ? ProjectEvent.Action.on : ProjectEvent.Action.off));

        });

        Environment.keyStrokeSoundPathProp = new SimpleObjectProperty<Path> ();

        Environment.keyStrokeSoundPathProp.addListener ((p, oldv, newv) ->
        {

            if (newv == null)
            {

                UserProperties.remove (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

                return;

            }

            UserProperties.set (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME,
                                newv.toUri ().toString ());

            // TODO Add an event?

        });

        String sf = UserProperties.get (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        if (sf != null)
        {

            Environment.setKeyStrokeSoundFilePath (Paths.get (sf));

        }

        Environment.incrStartupProgress ();
/*
TODO
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
*/
        // Init our properties.

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

        Environment.schedule (() ->
        {

            try
            {

                Importer.init ();

            } catch (Exception e) {

                Environment.logError ("Unable to init importer",
                                      e);

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

        Environment.incrStartupProgress ();

        try
        {

            // Get the user editor properties.
            com.gentlyweb.properties.Properties eprops = new com.gentlyweb.properties.Properties ();

            Path edPropsFile = UserProperties.getUserEditorsPropertiesPath ();

            if (Files.exists (edPropsFile))
            {

                eprops = new com.gentlyweb.properties.Properties (edPropsFile.toFile (),
                                                                  Constants.GZIP_EXTENSION);

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

            Environment.initProjectsDBFromProjectsPath ();

        } catch (Exception e) {

            Environment.logError ("Unable to init project info from projects path",
                                  e);

        }

        if (Environment.isFirstUse)
        {

            Environment.isFirstUse = (Environment.getAllProjectInfos ().size () == 0);

        }

        Environment.targets = new TargetsData (UserProperties.getProperties ());

        Environment.startupCompleteProp.addListener ((val, oldv, newv) ->
        {

            if (newv)
            {

                Environment.userSession.start (new Date ());

                // See if we should be doing a warmup exercise.
                if (UserProperties.getAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME))
                {

                    UIUtils.runLater (() ->
                    {

                        AbstractViewer viewer = Environment.getFocusedViewer ();

                        if (viewer != null)
                        {

                            viewer.showWarmupPromptSelect ();

                            viewer.fireProjectEvent (ProjectEvent.Type.projectobject,
                                                     ProjectEvent.Action.warmuponstartup,
                                                     Warmup.OBJECT_TYPE);

                        }

                    });

                }

                Date d = new Date (System.currentTimeMillis () + (Constants.DAY_IN_MILLIS));

                d = Utils.zeroTimeFields (d);

                Environment.schedule (() ->
                {

                    try
                    {

                        Environment.projectInfoManager.addSession (Environment.userSession.createSnapshot ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to take session snapshot",
                                              e);

                    }

                },
                d.getTime (),
                // Run every 24 hours.  It will drift over the days but not by much.
                Constants.DAY_IN_MILLIS);

                Environment.schedule (() ->
                {

                    java.util.List<String> prefix = new ArrayList<> ();
                    prefix.add (LanguageStrings.targets);
                    prefix.add (LanguageStrings.types);

                    Set<StringProperty> met = new LinkedHashSet<> ();
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

                            met.add (getUILanguageStringProperty (Utils.newList (prefix,session)));
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

                            met.add (getUILanguageStringProperty (Utils.newList (prefix,daily)));
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

                                met.add (getUILanguageStringProperty (Utils.newList (prefix,weekly)));
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

                                met.add (getUILanguageStringProperty (Utils.newList (prefix,monthly)));
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

                            for (StringProperty m : met)
                            {

                                b.append (String.format ("<li>%s</li>",
                                                         m));

                            }

                            List<Object> repVals = new ArrayList<> ();
                            repVals.add (sessWC);
                            repVals.addAll (met);

                            AbstractViewer viewer = Environment.getFocusedViewer ();

                            ComponentUtils.showMessage (viewer,
                                                        getUILanguageStringProperty (Arrays.asList (LanguageStrings.targets,writingtargetreachedpopup,title)),
                                                        //"Writing targets reached",
                                                        getUILanguageStringProperty (Arrays.asList(LanguageStrings.targets,writingtargetreachedpopup,text),
                                                //"You have reached the following writing targets by writing <b>%s</b> words.<ul>%s</ul>Well done and keep it up!",
                                                                                     repVals));


                        }

                    } catch (Exception e) {

                        Environment.logError ("Unable to show writing targets reached popup",
                                              e);

                    }

                },
                5 * Constants.SEC_IN_MILLIS,
                5 * Constants.SEC_IN_MILLIS);

            }

        });

        // Get all the projects.
        Environment.initProjectInfos ();

        //URL url = UserProperties.getUserStyleSheetURL ();
        Path p = Paths.get ("d:/development/github/quollwriterv3/src/main/resources/data/default-style.css");

        if (Files.exists (p))
        {

            Environment.styleSheetLastModified = Files.getLastModifiedTime (p).toMillis ();

            Environment.schedule (() ->
            {

                long t = 0;

                try
                {

                    t = Files.getLastModifiedTime (p).toMillis ();

                } catch (Exception e) {

                    // Has the file been removed?
                    // TODO Unschedule.
                    Environment.printStackTrace (e);
                    return;

                }
/*
                Path tp = null;

                try
                {

                    tp = Files.createTempFile ("temp-user", ".css");

                    Files.write (tp, ".root { -fx-font-size: 20pt; }".getBytes (StandardCharsets.UTF_8));

                } catch (Exception e) {

                    Environment.printStackTrace (e);
                    return;

                }

                final Path _tp = tp;
*/
                if (t != Environment.styleSheetLastModified)
                {

                    Environment.doForOpenViewers (pv ->
                    {

                        try
                        {

                            // Update the stylesheet.
                            pv.getViewer ().removeStyleSheet (p.toUri ().toURL ());
                            pv.getViewer ().addStyleSheet (p.toUri ().toURL ());

                            //pv.getViewer ().addStyleSheet (_tp.toUri ().toURL ());

                        } catch (Exception e) {

                            e.printStackTrace ();

                        }

                    });

                    Environment.styleSheetLastModified = t;

                }

            },
            1000,
            1000);

        }

    }

    public static BooleanProperty nightModeProperty ()
    {

        return Environment.nightModeProp;

    }

    public static void setNightModeEnabled (boolean v)
    {

        Environment.nightModeProp.set (v);

    }

    public static void printStackTrace (Throwable e)
    {

        e.printStackTrace (Environment.out);

    }

    private static void initProjectInfos ()
                                   throws Exception
    {

        Set<ProjectInfo> all = new LinkedHashSet (Environment.projectInfoManager.getObjects (ProjectInfo.class,
                                                                                             null,
                                                                                             null,
                                                                                             true));

        Environment.allProjects.addAll (all);

    }

    public static void addProjectInfo (ProjectInfo pi)
    {

        Environment.allProjects.add (pi);

    }

    public static void doForOpenViewers (Consumer<AbstractViewer> action)
    {

        UIUtils.runLater (() ->
        {

            Environment.openViewers.stream ().forEach (action);

        });

    }

    public static void doForOpenProjects (Consumer<AbstractProjectViewer> action)
    {

        UIUtils.runLater (() ->
        {

            Environment.openProjects.values ().stream ().forEach (action);

        });

    }

    public static StringProperty uilangProperty ()
    {

        return UILanguageStringsManager.uilangProperty ();

    }

    public static SetProperty<ProjectInfo> allProjectsProperty ()
    {

        // TODO Return read only version.
        return Environment.allProjectsProp;

    }

    public static MapProperty<ProjectInfo, AbstractProjectViewer> openProjectsProperty ()
    {

        // TODO Return a read only version.
        return Environment.openProjectsProp;

    }

    public static Set<AbstractProjectViewer> getOpenProjectViewers ()
    {

        return new LinkedHashSet<> (Environment.openProjectsProp.values ());

    }

    public static AbstractProjectViewer getProjectViewer (ProjectInfo p)
    {

        return Environment.openProjectsProp.get (p);

    }

    public static AbstractProjectViewer getProjectViewer (Project p)
    {

        return Environment.openProjectsProp.get (Environment.getProjectInfo (p));

    }

    public static SetProperty<AbstractViewer> openViewersProperty ()
    {

        // TODO Return a read only version.
        return Environment.openViewersProp;

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

    public static void openProject (Project  p,
                                    Runnable onProjectOpen)
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

    public static void openProject (final String   projId,
                                    final String   projType,
                                    final Runnable onProjectOpen)
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

    public static void openProject (final ProjectInfo p)
                             throws Exception
    {

        Environment.openProject (p,
                                  null);

    }

    public static void openProject (final ProjectInfo p,
                                    final Runnable    onProjectOpen)
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

        AbstractProjectViewer pv = (AbstractProjectViewer) Environment.openProjectsProp.get (p);

        if (pv != null)
        {

            p.setOpening (false);
            pv.getViewer ().setIconified (false);
            pv.getViewer ().toFront ();

            if (onProjectOpen != null)
            {

                UIUtils.runLater (onProjectOpen);

            }

            return;

        } else
        {

            try
            {

                pv = AbstractProjectViewer.createProjectViewerForType (p);

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

                /*
                 TODO
                PasswordInputWindow.create (getUIString (prefix,
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

                                                    Environment.showAllProjects ();

                                                }

                                            }).resize ();
*/

                return;

            }

            try
            {

                pv.addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                                    (ev ->
                                    {

                                        Project proj = fpv.getProject ();

                                        ProjectInfo pi = Environment.getProjectInfo (fpv.getProject ().getId (),
                                                                                     fpv.getProject ().getType ());

                                        if (pi != null)
                                        {

                                            Environment.openProjects.remove (pi);

                                        }

                                        Environment.openViewersProp.remove (fpv);

                                        Environment.userSession.updateCurrentSessionWordCount (fpv.getSessionWordCount ());

                                        if (Environment.openProjects.size () == 0)
                                        {

                                            if (UserProperties.getAsBoolean (Constants.SHOW_PROJECTS_WINDOW_WHEN_NO_OPEN_PROJECTS_PROPERTY_NAME))
                                            {

                                                try
                                                {

                                                    Environment.showAllProjectsViewer ();

                                                } catch (Exception e) {

                                                    Environment.logError ("Unable to show all projects viewer",
                                                                          e);

                                                    // TODO Add message.

                                                }

                                                return;

                                            }

                                            Environment.closeDown ();

                                        }

                                    }));

                Viewer v = pv.createViewer ();

                pv.openProject (p,
                                null,
                                onProjectOpen);

                pv.init (null);

            } catch (Exception e) {

                try
                {

                    pv.close (() ->
                    {

                      // This prevents QW from going into a shutdown.
                      // TODO: Make this nicer.

                  });

                } catch (Exception ee) {

                    Environment.logError ("Unable to close project after try open, project: " + p,
                                          ee);

                }

                if (ObjectManager.isDatabaseAlreadyInUseException (e))
                {

                    ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                     getUILanguageStringProperty (Utils.newList (prefix,errors,projectalreadyopen)));
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
     // TODO Investigate how this differs from createProject.
    public static Project createNewProject (Path   saveDir,
                                            String name,
                                            String filePassword)
                                     throws Exception
    {

        Path projDir = saveDir.resolve (Utils.sanitizeForFilename (name));

        if (Files.exists (projDir))
        {

            throw new IllegalArgumentException ("A project with name: " +
                                                name +
                                                " already exists at: " +
                                                projDir);

        }

        Project p = new Project ();
        p.setName (name);

        // Get the username and password.
        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

        ObjectManager dBMan = new ObjectManager ();
        // TODO Change to use the path.
        dBMan.init (projDir.resolve (Constants.PROJECT_DB_FILE_NAME_PREFIX).toFile (),
                    username,
                    password,
                    filePassword,
                    0);

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (projDir);

        // TODO Change to use the path.
        p.setProjectDirectory (projDir.toFile ());
        p.setEncrypted (filePassword != null);

        Book b = new Book (p,
                           p.getName ());

        p.addBook (b);

        dBMan.saveObject (p,
                          null);

        dBMan.closeConnectionPool ();

        ProjectInfo pi = new ProjectInfo (p);

/*
TODO NEeded?
        Environment.fireProjectInfoChangedEvent (pi,
                                                 ProjectInfoChangedEvent.ADDED);
*/
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
     // TODO Investigate how this differs from createNewProject.
    public static ObjectManager createProject (Path    saveDir,
                                               Project p,
                                               String  filePassword)
                                        throws Exception
    {

        Path projDir = saveDir.resolve (Utils.sanitizeForFilename (p.getName ()));

        if (Files.exists (projDir))
        {

            throw new IllegalArgumentException ("A project with name: " +
                                                p.getName () +
                                                " already exists at: " +
                                                projDir);

        }

        // Get the username and password.
        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

        ObjectManager dBMan = new ObjectManager ();
        // TODO Change to use the path.
        dBMan.init (projDir.resolve (Constants.PROJECT_DB_FILE_NAME_PREFIX).toFile (),
                         username,
                         password,
                         filePassword,
                         0);

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (projDir);

        p.setProjectDirectory (projDir.toFile ());
        p.setEncrypted (filePassword != null);

        dBMan.setProject (p);

        dBMan.saveObject (p,
                          null);

        ProjectInfo pi = new ProjectInfo (p);

        Environment.projectInfoManager.saveObject (pi,
                                                   null);

/*
TODO Needed?
        Environment.fireProjectInfoChangedEvent (pi,
                                                 ProjectInfoChangedEvent.ADDED);
*/
        return dBMan;

    }

    @Deprecated
    public static void deleteProject (Project        p,
                                      java.awt.event.ActionListener onDelete)
    {

        Environment.deleteProject (p,
                                   () ->
                                   {

                                       com.quollwriter.ui.UIUtils.doLater (onDelete);

                                   });

    }

    public static void deleteProject (Project  p,
                                      Runnable onDelete)
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

    @Deprecated
    public static void deleteProject (final ProjectInfo pr,
                                      final java.awt.event.ActionListener onDelete)
    {

        Environment.deleteProject (pr,
                                   () ->
                                   {

                                       com.quollwriter.ui.UIUtils.doLater (onDelete);

                                   });

    }

    public static void deleteProject (final ProjectInfo pr,
                                      final Runnable    onDelete)
    {

        AbstractProjectViewer viewer = Environment.openProjects.get (pr);

        Runnable onClose = new Runnable ()
        {

            @Override
            public void run ()
            {

                // There is probably now (because of h2) a "projectdb.lobs.db" directory.
                // Add a can delete file to it.
                /*
                 TODO Is this still needed?
                try
                {

                    // TODO Change to use a path.
                    Utils.createQuollWriterDirFile (new File (pr.getProjectDirectory ().getPath () + "/projectdb.lobs.db"));

                } catch (Exception e)
                {

                    // Ignore for now.
                    Environment.logError ("Unable to add can delete dir file to: " +
                                          pr.getProjectDirectory ().getPath () + "/projectdb.lobs.db",
                                          e);

                }
*/
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

/*
TODO Needed?
                Environment.fireProjectInfoChangedEvent (pr,
                                                         ProjectInfoChangedEvent.DELETED);
*/
                if (onDelete != null)
                {

                    UIUtils.runLater (onDelete);

                } else {

                    Environment.showAllProjectsViewerIfNoOpenProjects ();

                }

            }

        };

        if (viewer != null)
        {

            viewer.close (true,
                          onClose);

        } else {

            UIUtils.runLater (onClose);

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
                                   // TODO Fix this.
                                   (Runnable) null);

    }

    public static void openObjectInProject (final ProjectInfo proj,
                                            final DataObject  obj)
                                     throws Exception
    {

        Environment.openProject (proj,
                                 () ->
                                 {

                                    // View the object.
                                    AbstractProjectViewer viewer = Environment.getProjectViewer (proj);

                                    try
                                    {

                                        viewer.viewObject (obj,
                                                           null);

                                    } catch (Exception e) {

                                        Environment.logError ("Unable to view object: " +
                                                              obj,
                                                              e);

                                        // TODO Show error

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

    public static ProjectInfo getWarmupsProject ()
                                          throws Exception
    {

        for (ProjectInfo pi : Environment.allProjects)
        {

            if (pi.isWarmupsProject ())
            {

                return pi;

            }

        }

        return null;

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
     /*
      TODO Remove since it's handled by the event.
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

            Environment.showAllProjectsViewerIfNoOpenProjects ();

        }

    }
*/

    public static void updateProjectInfo (ProjectInfo pi)
                                   throws GeneralException
    {

        Environment.projectInfoManager.saveObject (pi,
                                                   null);

    }

    private static void closeDown ()
    {

        if (Environment.openViewersProp.size () > 0)
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

        /*
        TODO Needed?
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
        */

        Platform.exit ();

    }

    public static void setUILanguage (String id)
                               throws Exception
    {

        UILanguageStringsManager.setUILanguage (id);

        Environment.loadUserObjectTypeNames ();

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

    public static Set<ProjectInfo> getAllProjectInfos (String limitToType)
                                                throws Exception
    {

        Set<ProjectInfo> ret = new LinkedHashSet<> ();

        if (limitToType != null)
        {

            for (ProjectInfo p : Environment.allProjects)
            {

                if (p.getType ().equals (limitToType))
                {

                    ret.add (p);

                }

            }

        } else {

            ret.addAll (Environment.allProjects);

        }

        return ret;

    }

    public static Set<ProjectInfo> getAllProjectInfos ()
                                                throws Exception
    {

        return Environment.getAllProjectInfos (null);

    }

    // LEGACY
    private static void initProjectsDBFromProjectsPath ()
                                                 throws Exception
    {

        Path f = Environment.getUserPath (Constants.PROJECTS_FILE_NAME);

        if (Files.notExists (f))
        {

            return;

        }

        // Get the projects file.
        Element root = JDOMUtils.getFileAsElement (f.toFile (),
                                                   Constants.GZIP_EXTENSION);

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

                ObjectManager om = Environment.createProjectObjectManager (pi,
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
        // TODO Check this.
        Files.move (f, f.getParent ().resolve (f.getFileName () + ".old"));

    }

    public static ReadOnlyBooleanProperty startupCompleteProperty ()
    {

        return Environment.startupCompleteProp.readOnlyBooleanProperty (Environment.startupCompleteProp);

    }

    public static ReadOnlyIntegerProperty startupProgressProperty ()
    {

        return Environment.startupProgressProp.readOnlyIntegerProperty (Environment.startupProgressProp);

    }

    public static void startupComplete ()
    {

        Environment.startupCompleteProp.setValue (true);
        Environment.startupProgressProp.setValue (100);

    }

    public static void incrStartupProgress ()
    {

        if (Environment.startupCompleteProperty ().getValue ())
        {

            return;

        }

        int v = Environment.startupProgressProp.getValue () + 9;

        if (v > 100)
        {

            v = 100;

        }

        Environment.startupProgressProp.setValue (v);

    }

    public static void setPlaySoundOnKeyStroke (boolean v)
    {

        Environment.playSoundOnKeyStrokeProp.setValue (v);

    }

    public static void setKeyStrokeSoundFilePath (Path p)
    {

        try
        {

            Environment.keyStrokeSound = null;

            if (p == null)
            {

                Environment.keyStrokeSoundPathProp.setValue (null);

                return;

            }

            AudioClip s = new AudioClip (p.toUri ().toString ());

            Environment.keyStrokeSound = s;

            Environment.keyStrokeSoundPathProp.setValue (p);

        } catch (Exception e) {

            Environment.logError ("Unable to set key stroke sound file: " +
                                  p,
                                  e);

            Environment.keyStrokeSoundPathProp.setValue (null);

        }

    }

    public static ObjectProperty<Path> keyStrokeSoundPathProperty ()
    {

        return Environment.keyStrokeSoundPathProp;

    }

    public static ReadOnlyBooleanProperty playSoundOnKeyStrokeProperty ()
    {

        return Environment.playSoundOnKeyStrokeProp;

    }

    public static boolean isDistractionFreeModeEnabled ()
    {

        for (AbstractProjectViewer pv : Environment.openProjects.values ())
        {

            if (pv.distractionFreeModeProperty ().getValue ())
            {

                return true;

            }

        }

        return false;

    }

    public static AllProjectsViewer getAllProjectsViewer ()
    {

        return Environment.allProjectsViewer;

    }

    public static AbstractViewer showAllProjectsViewer ()
    {

        if (Environment.allProjectsViewer == null)
        {

            try
            {

                Environment.allProjectsViewer = new AllProjectsViewer ();
                Environment.allProjectsViewer.init (null);

            } catch (Exception e) {

                Environment.logError ("Unable to create all projects viewer.",
                                      e);

                // TODO Show an error.

            }

        }

        Environment.allProjectsViewer.toFront ();

        return Environment.allProjectsViewer;

    }

    public static AbstractViewer getFocusedViewer ()
    {

        if (Environment.openViewersProp.size () == 0)
        {

            return null;

        }

        for (AbstractViewer viewer : Environment.openViewersProp)
        {

            if (viewer.focusedProperty ().getValue ())
            {

                return viewer;

            }

        }

        // Return the first viewer that is showing.
        for (AbstractViewer viewer : Environment.openViewersProp)
        {

            if (viewer.getViewer ().showingProperty ().getValue ())
            {

                return viewer;

            }

        }

        // What the derp... Return the first.
        return Environment.openViewersProp.iterator ().next ();

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

    public static int getProjectInfoSchemaVersion ()
    {

        Path p = Environment.getProjectInfoDBPath ();

        Path f = p.getParent ().resolve (p.getFileName () + Constants.H2_DB_FILE_SUFFIX);

        // See if we already have a project info db.
        if (Files.exists (f))
        {

            return Environment.projectInfoSchemaVersion;

        }

        // No file, so need to create the schema.
        return 0;

    }

    public static Path getLogDir ()
                           throws IOException
    {

        Path d = Environment.getUserPath (Constants.LOGS_DIR);

        Files.createDirectories (d);

        return d;

    }

    public static Path getProjectInfoDBPath ()
    {

        Path dir = null;

        String dv = UserProperties.get (Constants.PROJECT_INFO_DB_DIR_PROPERTY_NAME);

        if (dv != null)
        {

            dir = Paths.get (dv);

        } else {

            dir = Environment.userQuollWriterDirPath;

        }

        return dir.resolve (Constants.PROJECT_INFO_DB_FILE_NAME_PREFIX);

    }

    public static void saveUserProperties ()
                                    throws Exception
    {

        Environment.projectInfoManager.setUserProperties (UserProperties.getProperties ());//Environment.userProperties);

    }

    public static void resetObjectTypeNamesToDefaults ()
                                                throws IOException
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
        Path otf = UserProperties.getUserObjectTypeNamesPath ();

        // Remove the file.
        Files.deleteIfExists (otf);

        otf = Environment.getUserPath (Constants.LEGACY_OBJECT_TYPE_NAMES_FILE_NAME);

        Files.deleteIfExists (otf);

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

        Path f = UserProperties.getUserObjectTypeNamesPath ();

        if (Files.exists (f))
        {

            // Use this one.
            Map t = (Map) JSONDecoder.decode (IOUtils.getFile (f.toFile ()));

            Environment.objectTypeNamesSingular = (Map) t.get (LanguageStrings.singular);
            Environment.objectTypeNamesPlural = (Map) t.get (LanguageStrings.plural);

        } else {

            // Legacy: pre 2.6.2
            f = Environment.getUserPath (Constants.LEGACY_OBJECT_TYPE_NAMES_FILE_NAME);

            if (Files.exists (f))
            {

                Environment.loadLegacyObjectTypeNames (JDOMUtils.getStringAsElement (IOUtils.getFile (f.toFile ())));

                Environment.setUserObjectTypeNames (Environment.objectTypeNamesSingular,
                                                    Environment.objectTypeNamesPlural);

                Files.deleteIfExists (f);

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

        Environment.objectTypeNamesSingular.putAll (singular);
        Environment.objectTypeNamesPlural.putAll (plural);

    }

    public static File getGeneralLogFile ()
                                   throws IOException
    {

        return Environment.getLogDir ().resolve (Constants.GENERAL_LOG_NAME).toFile ();

    }

    public static File getSQLLogFile ()
                               throws IOException
    {

        return Environment.getLogDir ().resolve (Constants.SQL_LOG_NAME).toFile ();

    }

    public static File getErrorLogFile ()
                                 throws IOException
    {

        return Environment.getLogDir ().resolve (Constants.ERROR_LOG_NAME).toFile ();

    }

    public static Path getErrorLogPath ()
                                 throws IOException
    {

        return Environment.getLogDir ().resolve (Constants.ERROR_LOG_NAME);

    }

    private static void setUserObjectTypeNames (Map<String, String> singular,
                                                Map<String, String> plural)
                                         throws Exception
    {

        Map<String, Map<String, String>> t = new HashMap ();

        t.put (LanguageStrings.singular,
               singular);
        t.put (LanguageStrings.plural,
               plural);

        IOUtils.writeStringToFile (UserProperties.getUserObjectTypeNamesPath ().toFile (),
                                   JSONEncoder.encode (t),
                                   false);

        Environment.loadUserObjectTypeNames ();

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
    public static ObjectManager createProjectObjectManager (ProjectInfo p,
                                                            String      filePassword)
                                                     throws GeneralException
    {

        // TODO Investigate using this in the AbstractProjectViewer.

        // Get the username and password.
        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

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

    public static int getSchemaVersion ()
    {

        return Environment.schemaVersion;

    }

    public static void playKeyStrokeSound ()
    {

        if (Environment.keyStrokeSound != null)
        {

            Environment.keyStrokeSound.stop ();

            Environment.keyStrokeSound.play ();

        }

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

                updater = (QuollWriterUpdater) updaterCl.getDeclaredConstructor ().newInstance ();

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

            Thread t = new Thread (() ->
            {

                try
                {

                    // TODO _updater.doUpdate (viewer);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to perform update check",
                                          e);

                }

            });

            t.setDaemon (true);
            t.setPriority (Thread.MIN_PRIORITY);

            t.start ();

        }

    }

    public static BooleanProperty debugModeProperty ()
    {

        return Environment.debugModeProp;

    }

    public static boolean isDebugModeEnabled ()
    {

        return Environment.debugModeProp.getValue ();

    }

    public static void setDebugModeEnabled (boolean v)
    {

        Environment.debugModeProp.setValue (v);

        // TODO: Maybe have an EnvironmentEvent?

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

    public static SynonymProvider getSynonymProvider (String language)
                                                      throws GeneralException
    {

        if (language == null)
        {

            language = Constants.ENGLISH;

        }

        if (UILanguageStrings.isEnglish (language))
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

        String synCl = UserProperties.get (Constants.SYNONYM_PROVIDER_CLASS_PROPERTY_NAME);

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

                prov = (SynonymProvider) c.getDeclaredConstructor ().newInstance ();

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

    // TODO Remove this add instead add a listener for the close event on the viewer.
    public static void unregisterViewer (AbstractViewer v,
                                         Runnable       afterUnregister)
    {

        Environment.openViewers.remove (v);

        if (v == Environment.allProjectsViewer)
        {

            Environment.allProjectsViewer = null;

        }

        if (afterUnregister != null)
        {

            UIUtils.runLater (afterUnregister);

            return;

        }

        if (Environment.openViewersProp.size () == 0)
        {

            Environment.closeDown ();

        }

    }

    public static void registerViewer (AbstractViewer v)
    {

        Environment.openViewers.add (v);

    }

    public static String getDefaultTextAlignment ()
    {

        return getUIString (textalignments,left);

    }

    public static String getDefaultChapterName ()
    {

        return getUIString (general,defaultchaptername);

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

    public static void saveDefaultProperty (String  objType,
                                            String  propName,
                                            Boolean value)
                                     throws Exception
    {

        com.gentlyweb.properties.Properties props = Environment.getDefaultProperties (objType);

        props.setProperty (propName,
                           new com.gentlyweb.properties.BooleanProperty (propName,
                                                value));

        Environment.saveDefaultProperties (objType,
                                            props);

    }

    public static void saveDefaultProperty (String objType,
                                            String propName,
                                            String value)
                                     throws Exception
    {

        com.gentlyweb.properties.Properties props = Environment.getDefaultProperties (objType);

        props.setProperty (propName,
                           new com.gentlyweb.properties.StringProperty (propName,
                                               value));

        Environment.saveDefaultProperties (objType,
                                            props);

    }

    // TODO Improve this...
    public static void saveDefaultProperties (String                              objType,
                                              com.gentlyweb.properties.Properties props)
                                       throws Exception
    {

        // TODO Use a constant here.
        Path f = Environment.getUserPath ("default-" + objType + "-properties.xml");

        JDOMUtils.writeElementToFile (props.getAsJDOMElement (),
                                      f.toFile (),
                                      true);

    }

    // TODO Make a singleton accessible to any class.
    public static AchievementsManager getAchievementsManager ()
    {

        return Environment.achievementsManager;

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

    public static void fireUserProjectEvent (Object              source,
                                             ProjectEvent.Type   type,
                                             ProjectEvent.Action action,
                                             Object              contextObject)
    {

        Environment.fireUserProjectEvent (new ProjectEvent (source,
                                                            type,
                                                            action,
                                                            contextObject));

    }

    public static void fireUserProjectEvent (Object              source,
                                             ProjectEvent.Type   type,
                                             ProjectEvent.Action action)
    {

        Environment.fireUserProjectEvent (source,
                                           type,
                                           action,
                                           null);

    }

    public static void fireUserProjectEvent (ProjectEvent.Type   type,
                                             ProjectEvent.Action action)
    {

        Environment.fireUserProjectEvent (new ProjectEvent (null,
                                                            type,
                                                            action,
                                                            null));

    }

    public static void fireUserProjectEvent (final ProjectEvent ev)
    {

        UIUtils.runLater (() ->
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

        });

    }

    public static FullScreenTextProperties getFullScreenTextProperties ()
    {

        return Environment.fullScreenTextProps;

    }

    public static ProjectTextProperties getProjectTextProperties ()
    {

        return Environment.projectTextProps;

    }

    public static Path getUserQuollWriterDirPath ()
    {

        return Environment.userQuollWriterDirPath;

    }

    public static Path getUserPath (String name)
    {

        if (name.startsWith ("/"))
        {

            name = name.substring (1);

        }

        return Environment.userQuollWriterDirPath.resolve (name);

    }

    public static void removeSideBarFromAllProjectViewers (String id)
    {

        for (AbstractProjectViewer pv : Environment.openProjects.values ())
        {

            pv.removeSideBar (pv.getSideBar (id));

        }

    }

    public static StringProperty canOpenProject (Project p)
    {

        return Environment.canOpenProject (Environment.getProjectInfo (p));

    }

    public static StringProperty canOpenProject (ProjectInfo p)
    {

        java.util.List<String> prefix = Arrays.asList (LanguageStrings.project,LanguageStrings.actions,LanguageStrings.openproject,LanguageStrings.openerrors);

        if (p == null)
        {

            return getUILanguageStringProperty (Utils.newList (prefix,projectnotexist));
            //return "{Project} does not exist.";

        }

        if (!p.getProjectDirectory ().exists ())
        {

            return getUILanguageStringProperty (Utils.newList (prefix,projectdirnotexist),
                                                p.getProjectDirectory ());
            //return "Cannot find {project} directory <b>" + p.getProjectDirectory () + "</b>.";

        }

        if (!p.getProjectDirectory ().isDirectory ())
        {

            return getUILanguageStringProperty (Utils.newList (prefix,projectdirisfile),
                                                p.getProjectDirectory ());
            //return "Path to {project} <b>" + p.getProjectDirectory () + "</b> is a file, but a directory is expected.";

        }

        if (!Utils.getQuollWriterDirFile (p.getProjectDirectory ()).exists ())
        {

            return getUILanguageStringProperty (Utils.newList (prefix,invalidprojectdir),
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

            return getUILanguageStringProperty (Utils.newList (prefix,cantfindeditor),
                                  //"Unable to find {contact}: <b>%s</b> you are editing the {project} for.",
                                                p.getForEditor ().getEmail ());

        }

        return null;

    }

    // TODO Move to own helper class, no need for it to be here.
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

    @Deprecated
    public static String getUIString (String... ids)
    {

        return UILanguageStringsManager.getUIString (Arrays.asList (ids));

    }

    @Deprecated
    public static String getUIString (List<String> prefix,
                                      String       id)
    {

        return UILanguageStringsManager.getUIString (Utils.newList (prefix, id));
/*
TODO Remove
        List<String> ids = new ArrayList (prefix);

        ids.add (id);

        return Environment.getUIString (ids);
*/
    }

    @Deprecated
    public static String getUIString (List<String> prefix,
                                      String...    ids)
    {

        return UILanguageStringsManager.getUIString (Utils.newList (prefix, ids));
/*
TODO Remove
        List<String> _ids = new ArrayList (prefix);

        for (String s : ids)
        {

            _ids.add (s);

        }

        return Environment.getUIString (_ids);
*/
    }

/*
TODO Remove
    private static String getUIString (List<String> ids)
    {

        String s = Environment.uiLanguageStrings.getString (ids);

        if (s == null)
        {

            s = BaseStrings.toId (ids);

        }

        return s;

    }
*/

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
    /*
            try
            {

                details.put ("errorLog",
                             IOUtils.getFile (Environment.getErrorLogFile ()));
                details.put ("generalLog",
                             IOUtils.getFile (Environment.getGeneralLogFile ()));

            } catch (Exception e) {

                // NOt much we can do here!

            }
    */
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
                                                  (Runnable) null);

            } catch (Exception e) {

                // Nothing we can do.

            }

        }

    }

    public static void addDoOnShutdown (Runnable r)
    {

        // TODO Is this still needed?
        // Probably due to the updater...

    }

    @Deprecated
    public static void sendMessageToSupport (final String              type,
                                             final Map<String, Object> info,
                                             final java.awt.event.ActionListener      onComplete)
    {

        Environment.sendMessageToSupport (type,
                                          info,
                                          () ->
                                          {

                                              com.quollwriter.ui.fx.swing.SwingUIUtils.doLater (onComplete);

                                          });

    }

    public static void sendMessageToSupport (final String              type,
                                             final Map<String, Object> info,
                                             final Runnable            onComplete)
    {

        Thread t = new Thread (() ->
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
                Iterator<String> iter = info.keySet ().iterator ();

                while (iter.hasNext ())
                {

                    String k = iter.next ();

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

                URL u = Environment.getSupportUrl (Constants.SEND_MESSAGE_TO_SUPPORT_PAGE_PROPERTY_NAME);

                HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

                conn.setDoInput (true);
                conn.setDoOutput (true);

                conn.connect ();

                BufferedWriter bout = new BufferedWriter (new OutputStreamWriter (conn.getOutputStream (),
                                                                                  StandardCharsets.UTF_8));

                bout.write (data);
                bout.flush ();
                bout.close ();

                BufferedReader b = new BufferedReader (new InputStreamReader (conn.getInputStream (),
                                                                              StandardCharsets.UTF_8));

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

                    UIUtils.runLater (onComplete);

                }

            } catch (Exception e) {

                if (onComplete != null)
                {

                    UIUtils.runLater (onComplete);

                }

                if (!type.equals ("error"))
                {

                    Environment.logError ("Unable to send message to support",
                                          e);

                }

            }

        });

        t.setDaemon (true);
        t.start ();

    }

    // TODO Is this needed?
    public static void setUserObjectTypeIcon (String        objType,
                                              java.awt.image.BufferedImage image)
                                       throws Exception
    {

        javax.imageio.ImageIO.write (image,
                       "png",
                       Environment.getUserObjectTypeIconFilePath (objType).toFile ());

    }

    // TODO Is this needed?
    public static Path getUserObjectTypeIconFilePath (String objType)
    {

        return Environment.getUserPath (Constants.USER_OBJECT_TYPE_ICON_FILES_DIR).resolve (objType);

    }

    public static Set<UserConfigurableObjectType> getAssetUserConfigurableObjectTypes (boolean sortOnName)
    {

        Set<UserConfigurableObjectType> types = new LinkedHashSet<> ();

        for (UserConfigurableObjectType t : Environment.userConfigObjTypes)
        {

            if (t.isAssetObjectType ())
            {

                types.add (t);

            }

        }

        if (sortOnName)
        {

            List<UserConfigurableObjectType> stypes = new ArrayList<> (types);

            Collections.sort (stypes,
                              (o1, o2) -> o1.getObjectTypeName ().compareTo (o2.getObjectTypeName ()));

            types = new LinkedHashSet<> (stypes);

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

        // TODO type.removePropertyChangedListener (Environment.userConfigurableObjectTypeNameListener);

        String id = type.getObjectTypeId ();

        Environment.objectTypeNamesSingular.remove (id);

        Environment.objectTypeNamesPlural.remove (id);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (type,
                                          ProjectEvent.Type.userobjecttype,
                                          ProjectEvent.Action.delete,
                                          type);

    }

    public static void updateUserConfigurableObjectTypeFieldOrdering (UserConfigurableObjectType type)
                                                               throws GeneralException
    {

        UserConfigurableObjectTypeDataHandler dh = (UserConfigurableObjectTypeDataHandler) Environment.projectInfoManager.getHandler (type.getClass ());

        dh.updateFieldOrdering (type);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (type,
                                          ProjectEvent.Type.userobjecttype,
                                          ProjectEvent.Action.changed,
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
                                          ProjectEvent.Type.userobjecttype,
                                          ProjectEvent.Action.changed,
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
        // TODO type.addPropertyChangedListener (Environment.userConfigurableObjectTypeNameListener);

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
                                          ProjectEvent.Type.userobjecttype,
                                          ProjectEvent.Action._new,
                                          type);

    }

    public static void removeUserConfigurableObjectTypeField (UserConfigurableObjectTypeField field)
                                                       throws GeneralException
    {

        Environment.projectInfoManager.deleteObject (field,
                                                     true);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (field,
                                          ProjectEvent.Type.userobjecttypefield,
                                          ProjectEvent.Action.delete,
                                          field);

    }

    public static void updateUserConfigurableObjectTypeField (UserConfigurableObjectTypeField field)
                                                       throws GeneralException
    {

        ProjectEvent.Action ev = ProjectEvent.Action.changed;

        if (field.getKey () == null)
        {

            ev = ProjectEvent.Action._new;

        }

        Environment.projectInfoManager.saveObject (field);

        // Tell all projects about it.
        Environment.fireUserProjectEvent (field,
                                          ProjectEvent.Type.userobjecttypefield,
                                          ev,
                                          field);

        Environment.fireUserProjectEvent (field.getUserConfigurableObjectType (),
                                          ProjectEvent.Type.userobjecttype,
                                          ProjectEvent.Action.changed,
                                          field.getUserConfigurableObjectType ());

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

    // TODO Move the tag related stuff to a TagManager/Tags or similar.
    /**
     * Save a tag, this will either create or update it.
     *
     * @param tag The tag.
     * @throws GeneralException If the tag can't be saved.
     */
    public static void saveTag (Tag tag)
                         throws GeneralException
    {

        ProjectEvent.Action ev = ProjectEvent.Action.changed;

        if (tag.getKey () == null)
        {

            ev = ProjectEvent.Action._new;

        }

        Environment.projectInfoManager.saveObject (tag);

        if (ev.equals (ProjectEvent.Action._new))
        {

            Environment.tags.add (tag);

        }

        // Tell all projects about it.
        Environment.fireUserProjectEvent (tag,
                                          ProjectEvent.Type.tag,
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
                                          ProjectEvent.Type.tag,
                                          ProjectEvent.Action.delete,
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

            // TODO Remove nasty cast.
            Environment.tags = new LinkedHashSet<Tag> ((List<Tag>) Environment.projectInfoManager.getObjects (Tag.class,
                                                                                               null,
                                                                                               null,
                                                                                               true));

        }

        return Environment.tags;

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

    public static boolean openLastEditedProject ()
                                          throws Exception
    {

        List<ProjectInfo> projs = new ArrayList<> (Environment.getAllProjectInfos ());

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
            if (Environment.canOpenProject (p) != null)
            {

                return false;

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

// TODO If this needed?
    public static boolean checkCanOpenProject (ProjectInfo p,
                                               boolean     showLanding)
    {

        StringProperty r = Environment.canOpenProject (p);

        if (r != null)
        {

            // Do this first to ensure the error shows above it.
            if (showLanding)
            {

                // TODO Maybe pass the error as a runnable?  On show?
                Environment.showAllProjectsViewer ();

            }

            ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                             getUILanguageStringProperty (Arrays.asList (project,actions,openproject,openerrors,general),
                                                                          p.getName (),
                                                                          r));
                                      //"Unable to open {project} <b>" + p.getName () + "</b>, reason:<br /><br />" + r);

            return false;

        }

        return true;

    }

    public static boolean hasSynonymsDirectory (String lang)
    {

        Path f = Environment.getUserPath (Constants.THESAURUS_DIR).resolve (lang);

        return Files.exists (f) && Files.isDirectory (f);

    }

    @Deprecated
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

            return UserProperties.get (Constants.QUOLL_WRITER_DEBUG_WEBSITE_PROPERTY_NAME);

        } else {

            return UserProperties.get (Constants.QUOLL_WRITER_WEBSITE_PROPERTY_NAME);

        }

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

    public static ProjectInfo getProjectById (String id,
                                              String projType)
                                       throws Exception
    {

        if (id == null)
        {

            return null;

        }

        for (ProjectInfo p : Environment.allProjects)
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

    public static Path getDefaultSaveProjectDirPath ()
    {

        try
        {

            List<ProjectInfo> pis = new ArrayList<> (Environment.getAllProjectInfos (Project.NORMAL_PROJECT_TYPE));

            Collections.sort (pis,
                              new ProjectInfoSorter ());

            if (pis.size () > 0)
            {

                return pis.get (0).getProjectDirectory ().getParentFile ().toPath ();

            }

        } catch (Exception e) {

            Environment.logError ("Unable to get last edited project directory",
                                  e);

        }

        return Environment.getUserPath (Constants.DEFAULT_PROJECTS_DIR_NAME);

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

        // TODO Reexamine this... what if the project is already open?

        // Get the username and password.
        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

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

    // TODO Move to an ObjectTypeNameManager... singleton
    public static Map<String, String> getObjectTypeNamePlurals ()
    {

        return new HashMap (Environment.objectTypeNamesPlural);

    }

    public static Map<String, String> getObjectTypeNames ()
    {

        return new HashMap (Environment.objectTypeNamesSingular);

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

            v = getUIString (objectnames,plural,t);

        }

        return v;

        //return Environment.objectTypeNamesPlural.get (t);

    }

    public static Version getQuollWriterVersion ()
    {

        return Environment.appVersion;

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

            v = getUIString (objectnames,singular,t);

        }

        return v;

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

    public static int getSessionWordCount ()
    {

        return Environment.userSession.getCurrentSessionWordCount ();

    }

    public static List<Session> getSessions (int daysPast)
                                      throws GeneralException
    {

        return Environment.projectInfoManager.getSessions (daysPast);

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

    // TODO Use a property?
    public static DecimalFormat getNumberFormatter ()
    {

        return Environment.numFormat;

    }

    // TODO Move to UserPropertyHandler, have it register and store them there.
    public static com.quollwriter.ui.UserPropertyHandler getUserPropertyHandler (String userProp)
    {

        return Environment.userPropertyHandlers.get (userProp);

    }

    public static String getJSONFileAsString (String url)
                                       throws Exception
    {

        String data = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + url));

        if (data == null)
        {

            return null;

        }

        if (data.startsWith (Constants.JSON_RETURN_PREFIX))
        {

            data = data.substring (Constants.JSON_RETURN_PREFIX.length ());

        }

        return data;

    }

    public static String formatObjectToStringProperties (DataObject d)
    {

        Map<String, Object> props = new LinkedHashMap<> ();

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

    public static boolean isFirstUse ()
    {

        return Environment.isFirstUse;

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

        // TODO Change to use String.format.
        return new URL (UserProperties.get (prefName) + UserProperties.get (pagePropertyName) + (parms != null ? parms : ""));

    }

    // TODO Move to a BackupsManager.
    public static void restoreBackupForProject (ProjectInfo p,
                                                File        restoreFile)
                                         throws Exception
    {

        // Get the project db file.
        // TODO Change to use paths.

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

    // TODO Move to a BackupsManager.
    public static File createBackupForProject (Project p,
                                               boolean noPrune)
                                        throws Exception
    {

        return Environment.createBackupForProject (Environment.getProjectInfo (p),
                                                   noPrune);

    }

    // TODO Move to a BackupsManager.
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
                                              ProjectEvent.Type.backups,
                                              ProjectEvent.Action._new,
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

    // TODO Move to a better place.
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

    public static boolean isInFullScreen ()
    {

        // TODO
        return false;

    }

    public static void showAllProjectsViewerIfNoOpenProjects ()
    {

        // Show the welcome screen if there are no projects open.
        if (Environment.openProjectsProp.size () == 0)
        {

            Environment.showAllProjectsViewer ();

        }

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

    public static Path getProjectInfoDBFilePath ()
    {

        Path dir = null;

        String dv = UserProperties.get (Constants.PROJECT_INFO_DB_DIR_PROPERTY_NAME);

        if (dv != null)
        {

            dir = Paths.get (dv);

        } else {

            dir = Environment.getUserQuollWriterDirPath ();

        }

        return dir.resolve (Constants.PROJECT_INFO_DB_FILE_NAME_PREFIX);

    }

    public static URL getUpgradeURL (Version version)
                              throws Exception
    {

        String parms = "?version=" + version.getVersion ();

        return Environment.getSupportUrl (Constants.GET_UPGRADE_FILE_PAGE_PROPERTY_NAME,
                                          parms);

    }

    public static Book createTestBook ()
                                       throws Exception
    {

        Element root = JDOMUtils.getStringAsElement (Utils.getResourceFileAsString (Constants.TEST_BOOK_FILE));

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

    public static Set<Image> getWindowIcons ()
    {

        if (Environment.windowIcons == null)
        {

            Environment.windowIcons = new HashSet<> ();
            // TODO Improve to use a path.
            // TODO Add more icon sizes.
            Environment.windowIcons.add (new Image (Utils.getResourceStream (Constants.IMGS_DIR + Constants.WINDOW_ICON_PNG_NAME)));

        }

        return Environment.windowIcons;

    }

    @Deprecated
    public static int getIconPixelWidthForType (int type)
    {

        return 0;

    }

    @Deprecated
    public static javax.swing.ImageIcon getObjectIcon (String ot,
                                           int    iconType)
    {

        return null;

    }

    @Deprecated
    public static javax.swing.ImageIcon getObjectIcon (DataObject d,
                                           int        iconType)
    {

        return null;

    }

    @Deprecated
    public static String getButtonLabel (String l)
    {

        return l;

    }

    @Deprecated
    public static String getButtonLabel (String l,
                                         String t)
    {

        return l;

    }

    @Deprecated
    public static String replaceObjectNames (String t)
    {

        return t;

    }

    @Deprecated
    public static java.awt.Image getTransparentImage ()
    {

        return null;

    }

    @Deprecated
    public static java.awt.image.BufferedImage getNoEditorAvatarImage ()
    {

        return null;

    }

    @Deprecated
    public static javax.swing.ImageIcon getTypingIcon ()
    {

        return null;

    }

    @Deprecated
    public static javax.swing.ImageIcon getLoadingIcon ()
    {

        return null;

    }

    @Deprecated
    public static URL getIconURL (String  name,
                                  int     type)
    {

        return null;

    }

    @Deprecated
    public static javax.swing.ImageIcon getIcon (String name, int type)
    {

        return null;

    }

    @Deprecated
    public static java.awt.image.BufferedImage getImage (String fileName)
    {

        return null;

    }

    @Deprecated
    public static javax.swing.ImageIcon getAchievementIcon ()
    {

        return null;

    }

    @Deprecated
    public static javax.swing.ImageIcon getWindowIcon ()
    {

        return null;

    }

    @Deprecated
    public static String getWindowNameSuffix ()
    {

        return null;

    }

    @Deprecated
    public static Set<com.quollwriter.ui.BackgroundImage> getBackgroundImages ()
                                                            throws Exception
    {

        Set<com.quollwriter.ui.BackgroundImage> ret = new LinkedHashSet<> ();

        Set<String> bgImages = Utils.getResourceListing (Constants.BACKGROUND_THUMB_IMGS_DIR);

        for (String s : bgImages)
        {

            ret.add (new com.quollwriter.ui.BackgroundImage (s));

        }

        return ret;

    }

    @Deprecated
    public static java.awt.Image getBackgroundImage (String name)
    {

        // TODO Remove Image im = Environment.backgroundImages.get (name);

        java.awt.Image im = null;

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

        im = new javax.swing.ImageIcon (url).getImage ();
/*
TODO
        Environment.backgroundImages.put (name,
                                          im);
*/
        return im;

    }

    @Deprecated
    public static java.awt.Image getBackgroundThumbImage (String name)
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

        return new javax.swing.ImageIcon (url).getImage ();

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

            v = new WarmupProjectViewer ();

        }

        if (v == null)
        {

            throw new GeneralException ("Project type: " +
                                        p.getType () +
                                        " is not supported.");

        }

        v.init (null);

        return v;

    }

    public static ProjectInfo getProjectByDirectory (Path dir)
                                              throws Exception
    {

        if (dir == null)
        {

            return null;

        }

        for (ProjectInfo p : Environment.allProjects)
        {

            if (p.getProjectDirectory ().toPath ().equals (dir))
            {

                return p;

            }

        }

        return null;

    }

    public static Image getBackgroundImage (String name,
                                            double width,
                                            double height)
    {

        // TODO Cache the images?

        Image im = null;

        if (im != null)
        {

            return im;

        }

        if (name == null)
        {

            return null;

        }

        name = Constants.BACKGROUND_IMGS_DIR + name;

        InputStream s = Utils.getResourceStream (name);

        if (s == null)
        {

            // Can't find image, log the problem but keep going.
            Environment.logError ("Unable to find/load image: " +
                                  name +
                                  ", check images jar to ensure file is present.",
                                  // Gives a stack trace
                                  new Exception ());

            return null;

        }

        if ((width == -1)
            &&
            (height == -1)
           )
        {

            return new Image (s);

        }

        return new Image (s,
                          width,
                          height,
                          false,
                          true);

    }

}
