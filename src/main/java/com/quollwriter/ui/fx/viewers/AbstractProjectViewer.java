package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.sql.Connection;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.nio.file.*;
import java.text.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.geometry.*;
import javafx.scene.image.*;

import org.dom4j.Element;

import org.apache.commons.io.file.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;
import com.quollwriter.events.PropertyChangedEvent;
import com.quollwriter.events.PropertyChangedListener;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.uistrings.UILanguageStrings;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.achievements.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractProjectViewer extends AbstractViewer implements PropertyChangedListener, PanelViewer
{

    private static final Map<String, Class> viewerTypes = new HashMap<> ();

    public interface HeaderControlButtonIds extends AbstractViewer.HeaderControlButtonIds
    {

        String timer = "timer";

    }

    public interface CommandId extends AbstractViewer.CommandId
    {

        String textproperties = "textproperties";
        String print = "print";
        String chaptersoverwordcounttarget = "chaptersoverwordcounttarget";
        String chaptersoverreadabilitytarget = "chaptersoverreadabilitytarget";
        String closeproject = "closeproject";
        String closepanel = "closepanel";
        String showwordcounts = "showwordcounts";
        String renameproject = "renameproject";
        String createbackup = "createbackup";
        String deleteproject = "deleteproject";
        String togglespellchecking = "togglespellchecking";

    }

    protected Project project = null;
    private BooleanProperty spellCheckingEnabledProp = null;
    private StringProperty projectSpellCheckLanguageProp = null;
    private BooleanProperty typeWriterScrollingEnabledProp = null;
    private StringProperty titleProp = null;

    private ChapterCounts         startWordCounts = new ChapterCounts ();
    private ChapterCounts allChapterCounts = new ChapterCounts ();
    //private ReadabilityIndices allReadabilityIndices = new ReadabilityIndices ();
    private Map<Chapter, ChapterCounts> chapterCounts = new WeakHashMap<> ();
    private Map<Chapter, ReadabilityIndices> noEditorReadabilityIndices = new WeakHashMap<> ();
    private Map<Chapter, ScheduledFuture> spellingReadabilityUpdaters = new HashMap<> ();

    private TabPane tabs = null;
    private Map<String, Panel> panels = new HashMap<> ();
    private ObjectProperty<Panel> currentPanelProp = null;
    //private VBox toolbarWrapper = null;
    private WindowedContent windowedContent = null;
    private ProjectFullScreenContent fullScreenContent = null;
    private ShowingInFullScreenPanel fsfplaceholder = null;

    private ScheduledFuture chapterCountsUpdater = null;

    private ProjectDictionaryProvider   projDict = null;
    private SynonymProvider       synProv = null;
    protected ObjectManager       dBMan = null;

    private Date                  sessionStart = new Date ();

    private TargetsData targets = null;
    private Map<Chapter, Date> chapterWordCountTargetWarned = new HashMap<> ();
    private ObservableSet<Chapter> chaptersOverWordCountTarget = FXCollections.observableSet (new HashSet<> ());
    private ObservableSet<Chapter> chaptersOverReadabilityTarget = FXCollections.observableSet (new HashSet<> ());

    private ObjectProperty<Chapter> chapterCurrentlyEditedProp = null;
    private StringProperty selectedTextProp = null;
    private int sessionWordCount = 0;
    private IntegerProperty sessionWordCountProp = null;

    private FindSideBar findSideBar = null;
    private Pane fullScreenPanelView = null;

    private WordCountProgressTimer wcTimer = null;

    public AbstractProjectViewer ()
    {

        final AbstractProjectViewer _this = this;

        this.titleProp = new SimpleStringProperty ();
        this.currentPanelProp = new SimpleObjectProperty<> ();
        this.selectedTextProp = new SimpleStringProperty ();
        this.sessionWordCountProp = new SimpleIntegerProperty (0);

        this.fullScreenPanelView = new Pane ();

        this.chapterCurrentlyEditedProp = new SimpleObjectProperty<> ();

        this.currentPanelProp.addListener ((pr, oldv, newv) ->
        {

            if ((newv != null)
                &&
                (newv.getContent () instanceof ChapterEditorPanelContent)
               )
            {

                ChapterEditorPanelContent pc = (ChapterEditorPanelContent) newv.getContent ();

                // Need to cast here because otherwise we have to get into generics...
                this.chapterCurrentlyEditedProp.setValue ((Chapter) pc.getObject ());

            } else {

                this.chapterCurrentlyEditedProp.setValue (null);

            }

        });

        this.wcTimer = new WordCountProgressTimer (this);

        this.spellCheckingEnabledProp = new SimpleBooleanProperty (false);

        this.spellCheckingEnabledProp.addListener ((v, oldv, newv) ->
        {

            try
            {

                _this.project.setProperty (Constants.SPELL_CHECKING_ENABLED_PROPERTY_NAME,
                                           newv);

            } catch (Exception e) {

                Environment.logError ("Unable to update spell checking property",
                                      e);

            }

        });

        //this.inFullScreenModeProp = new SimpleBooleanProperty (false);
        this.typeWriterScrollingEnabledProp = new SimpleBooleanProperty (false);
        //this.distractionFreeModeProp = new SimpleBooleanProperty (false);

        this.projectSpellCheckLanguageProp = new SimpleStringProperty (null);

        this.projectSpellCheckLanguageProp.addListener ((pr, oldv, newv) ->
        {

            // This forces changes to be propagated.

        });

        this.tabs = new TabPane ();
        this.tabs.setTabClosingPolicy (TabPane.TabClosingPolicy.ALL_TABS);
        this.tabs.setSide (UserProperties.tabsLocationProperty ().getValue ().equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);
        this.tabs.setTabDragPolicy (TabPane.TabDragPolicy.REORDER);

        this.tabs.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldi, newi) ->
        {

            if (oldi != null)
            {

                Panel p = (Panel) oldi.getContent ();

                // Remove the action mappings.
                p.getActionMappings ().keySet ().stream ()
                    .forEach (k -> this.getScene ().getAccelerators ().remove (k));

                this.restoreFromFullScreen (oldi);

            }

            if (newi != null)
            {

                Panel qp = (Panel) newi.getContent ();

                // Add the action mappings.
                this.getScene ().getAccelerators ().putAll (qp.getActionMappings ());

                PanelContent pc = qp.getContent ();

                this.selectedTextProp.unbind ();

                if (pc instanceof ChapterEditorPanelContent)
                {

                    ChapterEditorPanelContent cec = (ChapterEditorPanelContent) pc;

                    this.selectedTextProp.bind (cec.selectedTextProperty ());

                }

                this.currentPanelProp.setValue (qp);

                if (this.isInFullScreenMode ())
                {

                    this.showInFullScreen (newi);

                }

                qp.fireEvent (new Panel.PanelEvent (qp,
                                                    Panel.PanelEvent.SHOW_EVENT));

            } else {

                this.selectedTextProp.unbind ();

                this.currentPanelProp.setValue (null);

            }

        });

        UIUtils.doOnKeyReleased (this.tabs,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.removeCurrentPanel (null);

                                 });

        this.initActionMappings ();

    }

    public WordCountProgressTimer getWordCountTimer ()
    {

        return this.wcTimer;

    }

    public static void registerViewerType (String projType,
                                           Class  viewerClass)
    {

        if (!AbstractProjectViewer.class.isAssignableFrom (viewerClass))
        {

            throw new IllegalArgumentException ("Viewer class: " + viewerClass.getName () + ", for project type: " + projType + " is not a subclass of: " + AbstractProjectViewer.class.getName ());

        }

        try
        {

            if (viewerClass.getDeclaredConstructor () == null)
            {

                throw new IllegalArgumentException ("No zero arg constructor.");

            }

        } catch (Exception e) {

            throw new IllegalArgumentException ("Viewer class: " + viewerClass.getName () + ", for project type: " + projType + " does not have a zero arg constructor or cannot be created",
                                                e);

        }

        AbstractProjectViewer.viewerTypes.put (projType,
                                               viewerClass);

    }

    @Override
    public Panel getCurrentPanel ()
    {

        return this.currentPanelProp.getValue ();

    }

    public ObjectProperty<Panel> currentPanelProperty ()
    {

        return this.currentPanelProp;

    }

    public ObjectManager getObjectManager ()
    {

        return this.dBMan;

    }

    public void doAsTransaction (Consumer<Connection> doSomething)
    {

        Connection conn = null;

        try
        {

            conn = this.getObjectManager ().getConnection ();

            doSomething.accept (conn);

        } catch (Exception e) {

            Environment.logError ("Unable to do as transaction, why wasn't this caught?",
                                  e);

        } finally {

            this.getObjectManager ().releaseConnection (conn);

        }

    }

    public SynonymProvider getSynonymProvider ()
                                               throws Exception
    {

        if (this.synProv != null)
        {

            return this.synProv;

        }

        this.synProv = Environment.getSynonymProvider (this.getProjectSpellCheckLanguage ());

        return this.synProv;

    }

    public Synonyms getSynonyms (String word)
                                 throws GeneralException
    {

        if (this.synProv != null)
        {

            return this.synProv.getSynonyms (word);

        }

        return null;

    }

    public boolean synonymLookupsSupported ()
    {

        return this.synProv != null;

    }

    public ProjectDictionaryProvider getDictionaryProvider ()
    {

        return this.projDict;

    }

    public void updateProjectDictionaryForNames (Set<String> oldNames,
                                                 NamedObject object)
    {

        if (!(object instanceof Asset))
        {

            return;

        }

        if (this.projDict != null)
        {

            this.projDict.removeObjectNames (oldNames);
            this.projDict.addNamedObject (object);

        }

    }

    public void removeWordFromDictionary (String w)
    {

        if (this.projDict != null)
        {

            this.projDict.removeWord (w);

        }

    }

    public void addWordToDictionary (String w)
    {

        if (this.projDict != null)
        {

            this.projDict.addWord (w);

        }

    }

    public boolean isProjectLanguageEnglish ()
    {

        return UILanguageStringsManager.isLanguageEnglish (this.getProjectSpellCheckLanguage ());

    }

    public boolean isLanguageFunctionAvailable ()
    {

        if (!this.isProjectLanguageEnglish ())
        {
/*
TODO
            this.showNotificationPopup (getUIString (functionunavailable,title),
                                                                 //"Function unavailable",
                                        getUIString (functionunavailable,text),
                                        //"Sorry, this function is only available when your spellchecker language is English.<br /><br /><a href='action:contact'>Click here to contact me to help add support for your language</a>",
                                        20);
*/
            return false;

        }

        return true;

    }

    public String getProjectSpellCheckLanguage ()
    {

        return this.projectSpellCheckLanguageProp.getValue ();

    }

    /**
     * Set the spell check language for the project.  Note: this DOES NOT affect the project property:
     * {@link Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME}.  Also this does not affect the spell check enabled
     * flag.
     *
     * @param lang The language to use, set to <b>null</b> to switch off spell checking.
     * @param updateEditors Set to <b>true</b> to tell all the editor panels about the change.
     * @throws Exception If something goes wrong.
     */
    public void setProjectSpellCheckLanguage (String  lang)
                                       throws Exception
    {

        this.projectSpellCheckLanguageProp.setValue (lang);

        if (lang == null)
        {

            this.spellCheckingEnabledProp.setValue (false);

            return;

        }

        java.util.List<String> names = new ArrayList<> ();

        boolean isEnglish = UILanguageStrings.isEnglish (lang);

        // Get the names from the assets.
        Set<NamedObject> objs = this.project.getAllNamedChildObjects (Asset.class);

        for (NamedObject o : objs)
        {

            Set<String> onames = o.getAllNames ();

            for (String n : onames)
            {

                // Get the name, add it.
                names.add (n);

                if (isEnglish)
                {

                    names.add (n + "'s");

                }

            }

        }

        this.projDict = new ProjectDictionaryProvider (names,
                                                       new UserDictionaryProvider (lang));

    }

    private void initChapterCounts ()
	{

		if (this.project == null)
		{

			return;

		}

        if (this.project.getBooks ().size () == 0)
        {

            return;

        }

        Book b = this.project.getBooks ().get (0);

        ObservableList<Chapter> chapters = b.getChapters ();

        this.addListChangeListener (chapters,
                                    ev ->
        {

            while (ev.next ())
            {

                if (ev.wasAdded ())
                {

                    for (Chapter c : ev.getAddedSubList ())
                    {

                        this.initChapterCounts (c);

                    }

                    return;

                }

                if (ev.wasRemoved ())
                {

                    for (Chapter c : ev.getRemoved ())
                    {

                        this.chapterCounts.remove (c);
                        this.project.removeProperty ("chapter.counts." + c.getKey ());
                        this.spellingReadabilityUpdaters.remove (c);

                    }

                    return;

                }

            }

        });

        this.startWordCounts = new ChapterCounts ();

        for (Chapter c : chapters)
        {

            String p = this.project.getProperty ("chapter.counts." + c.getKey ());

            ChapterCounts cc = new ChapterCounts ();

            if (p == null)
            {

                StringWithMarkup tt = c.getText ();

                String t = "";

                if (tt != null)
                {

                    t = tt.getText ();

                }

                final ChapterCounts ncc = new ChapterCounts (t);
                cc.setWordCount (ncc.getWordCount ());

                cc.setSentenceCount (ncc.getSentenceCount ());
                cc.setParagraphCount (ncc.getParagraphCount ());
                cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                           t));

                cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                           t));

            } else {

                try
                {

                    State s = new State (p);
                    cc.init (s);

                } catch (Exception e) {

                    Environment.logError ("Unable to init chapter counts from: " +
                                          p,
                                          e);

                }

            }

            this.scheduleSpellingReadabilityUpdate (c);

            this.chapterCounts.put (c,
                                    cc);

            this.startWordCounts.add (cc);

        }

        this.updateAllChapterCounts ();

	}

    private void initChapterCounts (Chapter c)
    {

        final String t = this.getCurrentChapterText (c);

        final ChapterCounts cc = new ChapterCounts (t);
        cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                   t));

        this.chapterCounts.put (c,
                                cc);
        this.scheduleSpellingReadabilityUpdate (c);

    }

    public void scheduleUpdateChapterCounts (Chapter c)
    {

        this.schedule (() ->
        {

            ChapterCounts cc = this.chapterCounts.get (c);

            if (cc == null)
            {

                cc = new ChapterCounts ();

                this.chapterCounts.put (c,
                                        cc);

            }

            final String t = this.getCurrentChapterText (c);

            final ChapterCounts ncc = new ChapterCounts (t);
            cc.setWordCount (ncc.getWordCount ());

            cc.setSentenceCount (ncc.getSentenceCount ());
            cc.setParagraphCount (ncc.getParagraphCount ());
            cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                       t));

            this.updateAllChapterCounts ();
            this.scheduleSpellingReadabilityUpdate (c);

        },
        100,
        -1);

    }

    private void scheduleSpellingReadabilityUpdate (Chapter c)
    {

        // Is the update currently running?
        if (this.spellingReadabilityUpdaters.containsKey (c))
        {

            return;

        }

        this.spellingReadabilityUpdaters.put (c, this.schedule (() ->
        {

            ChapterCounts cc = this.chapterCounts.get (c);

            if (cc == null)
            {

                return;

            }

            long s = System.currentTimeMillis ();

            cc.setSpellingErrorCount (this.getSpellingErrors (c).size ());
            cc.setProblemFinderProblemsCount (this.getProblems (c).size ());

            cc.setReadabilityIndices (this.getReadabilityIndices (this.getCurrentChapterText (c)));

            ChapterEditorPanelContent ed = this.getEditorForChapter (c);

            this.spellingReadabilityUpdaters.remove (c);

            if (ed != null)
            {

                // This operation may take a while, so check to see if the text has changed since we started.
                if (ed.textLastModifiedTimeProperty ().getValue () > s)
                {

                    // Reschedule ourselves.
                    this.scheduleSpellingReadabilityUpdate (c);

                }

            }

        },
        1,
        -1));

    }

    private void scheduleSpellingReadabilityUpdate ()
    {

        // This updates the spelling error count and the problem finder problem count.
        this.schedule (() ->
        {

            Book b = this.project.getBooks ().get (0);

            ObservableList<Chapter> chapters = b.getChapters ();

    		for (Chapter c : chapters)
    		{

                ChapterCounts cc = this.chapterCounts.get (c);

                if (cc == null)
                {

                    continue;

                }

                cc.setSpellingErrorCount (this.getSpellingErrors (c).size ());
                cc.setProblemFinderProblemsCount (this.getProblems (c).size ());

                cc.setReadabilityIndices (this.getReadabilityIndices (this.getCurrentChapterText (c)));

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

    }

    public void printChapter (Chapter c)
    {

        // TODO
        /*
        PDDocument doc = new PDDocument ();
        PDPage page = new PDPage ();
        doc.addPage (page);
        */
        // See javafx.print javafx.print.PrinterJob
/*
        javafx.print.PrinterJob pj = javafx.print.PrinterJob.createPrinterJob ();

        if (pj.showPrintDialog (this.getViewer ()))
        {

            pj.printPage (this.getEditorForChapter (c));
            pj.endJob ();

        }
*/
    }

    private void initActionMappings ()
    {

        final AbstractProjectViewer _this = this;

        this.addActionMapping (() ->
        {

            QuollPopup qp = this.getPopupById (TagsManager.POPUP_ID);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            new TagsManager (this).show ();

            this.fireProjectEvent (ProjectEvent.Type.tags,
                                   ProjectEvent.Action.edit);

        },
        CommandId.edittags);

        this.addActionMapping (() ->
        {

            this.toggleSpellChecking ();

        },
        CommandId.togglespellchecking);

        this.addActionMapping (() ->
        {

            this.showFind ();

        },
        CommandId.find);

        this.addActionMapping (() ->
        {

            this.showTextProperties ();

        },
        CommandId.textproperties);

        this.addActionMapping (() ->
        {

            this.enterFullScreen ();

        },
        CommandId.fullscreen);

        this.addActionMapping (() ->
        {

            this.showWordCounts ();

        },
        CommandId.showwordcounts);

        this.addActionMapping (new CommandWithArgs<NamedObject> (objs ->
        {

            this.closePanel (objs[0],
                             null);

        },
        CommandId.closepanel));

        this.addActionMapping (() ->
        {

            try
            {

                _this.showOptions (null);

            } catch (Exception e) {

                Environment.logError ("Unable to view options",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (options,actionerror));

            }

        },
        CommandId.showoptions,
        CommandId.options);

        this.addActionMapping (() ->
        {

            this.close (null);

        },
        CommandId.closeproject);

        this.addActionMapping (() ->
        {

            try
            {

                _this.showChaptersOverWordCountTarget ();

            } catch (Exception e) {

                Environment.logError ("Unable to view chatpers over word count target",
                                      e);

            }

        },
        CommandId.chaptersoverwordcounttarget);

        this.addActionMapping (() ->
        {

            try
            {

                _this.showChaptersOverReadabilityTarget ();

            } catch (Exception e) {

                Environment.logError ("Unable to view chatpers over readability target",
                                      e);

            }

        },
        CommandId.chaptersoverreadabilitytarget);

        this.addActionMapping (() ->
        {

            try
            {

                _this.viewTargets ();

            } catch (Exception e) {

                Environment.logError ("Unable to view targets",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (LanguageStrings.targets,actionerror));
                                                  //"Unable to view targets.");

            }

        },
        CommandId.viewtargets,
        CommandId.targets);

        this.addActionMapping (() ->
        {

            try
            {

                _this.viewAchievements ();

            } catch (Exception e) {

                Environment.logError ("Unable to view achievements",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (achievements,actionerror));

            }

        },
        CommandId.viewachievements,
        CommandId.achievements);

        this.addActionMapping (new CommandWithArgs ((args) ->
        {

            String chartType = null;

            if (args != null)
            {

                if (args.length == 1)
                {

                    chartType = (String) args[0];

                }

            }

           try
           {

               _this.viewStatistics (chartType);

           } catch (Exception e) {

               Environment.logError ("Unable to view the statistics",
                                     e);

               ComponentUtils.showErrorMessage (_this,
                                                getUILanguageStringProperty (statistics,actionerror));
                                                 //"Unable to view the statistics");

           }

        },
        AbstractViewer.CommandId.showstatistics,
        AbstractViewer.CommandId.statistics,
        AbstractViewer.CommandId.charts));

    }

    public void changeProjectDirectory (Path   to)
                                 throws GeneralException
    {

        if (Files.exists (to))
        {

            throw new GeneralException ("To directory already exists: " + to);

        }

        this.saveObject (this.project,
                         true);

        this.dBMan.closeConnectionPool ();

        Path oldPath = this.project.getProjectDirectory ().toPath ();
        Path oldBackupsPath = this.project.getBackupDirectory ().toPath ();

        String backupsDir = null;
        boolean backupsIsSubDir = Utils.isSubDir (oldPath.toFile (), oldBackupsPath.toFile ());

        if (backupsIsSubDir)
        {

            backupsDir = oldBackupsPath.toString ().substring (oldPath.toString ().length () + 1);

        }

        // Rename the dir.
        try
        {

            PathUtils.copyDirectory (oldPath,
                                     to,
                                     StandardCopyOption.COPY_ATTRIBUTES);

        } catch (Exception e) {

            // Delete the to path, something has gone wrong.
            //delete to
            //reopen project.

            throw new GeneralException ("Unable to move project directory from: " +
                                        this.project.getProjectDirectory () +
                                        " to: " +
                                        to,
                                        e);

        }

        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

        try
        {

            // TODO Fix this!
            this.dBMan.init (to.resolve (Constants.PROJECT_DB_FILE_NAME_PREFIX).toFile (),
                             username,
                             password,
                             this.project.getFilePassword (),
                             Environment.getSchemaVersion ());

        } catch (Exception e) {

            this.dBMan.closeConnectionPool ();
            throw e;

        }

        this.dBMan.setProject (this.project);

        this.project.setProjectDirectory (to.toFile ());

        if (backupsIsSubDir)
        {

            // Need to resolve the backups against the new dir.
            this.project.setBackupDirectory (to.resolve (backupsDir).toFile ());

        }

        this.saveObject (this.project,
                         true);

        try
        {

            Utils.deleteDir (oldPath);

        } catch (Exception e) {

            Environment.logError ("Unable to delete old project dir: " + oldPath,
                                  e);

        }

        if (backupsIsSubDir)
        {

            try
            {

                Utils.deleteDir (oldBackupsPath);

            } catch (Exception e) {

                Environment.logError ("Unable to delete old backup dir: " + oldBackupsPath,
                                      e);

            }

        }

    }

    public void openProject (ProjectInfo    p,
                             String         filePassword)
                      throws Exception
	{

		this.openProject (p,
						  filePassword,
						  null);

	}

    public void openProject (ProjectInfo p,
                             String      filePassword,
							 Runnable    onOpen)
                      throws Exception
    {

        this.setIgnoreProjectEvents (true);

        // Get the username and password.
        String username = UserProperties.get (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = UserProperties.get (Constants.DB_PASSWORD_PROPERTY_NAME);

        if (p.isNoCredentials ())
        {

            username = null;
            password = null;
            filePassword = null;

        }

        if (!p.isEncrypted ())
        {

            filePassword = null;

        }

        this.dBMan = new ObjectManager ();

        // TODO Use Path?
        try
        {

            this.dBMan.init (p.getProjectDirectory ().resolve (Constants.PROJECT_DB_FILE_NAME_PREFIX).toFile (),
                             username,
                             password,
                             filePassword,
                             Environment.getSchemaVersion ());

        } catch (Exception e) {

            this.dBMan.closeConnectionPool ();
            throw e;

        }

        Environment.incrStartupProgress ();

        // Get the project.
        try
        {

            this.project = this.dBMan.getProject ();

        } catch (Exception e) {

            // This means we can't open the project and something is wrong, close the dbman to prevent
            // it locking the project open but unusable.
            this.dBMan.closeConnectionPool ();

            throw e;

        }

        Environment.incrStartupProgress ();

		this.project.setFilePassword (filePassword);
        this.project.setProjectDirectory (p.getProjectDirectory ().toFile ());
		this.project.setBackupDirectory (p.getBackupDirPath ().toFile ());
        // TODO getBackupDirectory ());
        //this.proj.setFilePassword (filePassword);
        this.project.setEncrypted (p.isEncrypted ());
        this.project.setNoCredentials (p.isNoCredentials ());

        this.project.addPropertyChangedListener (this);

        Environment.incrStartupProgress ();

        // This is done here because the achievements manager needs the project.
        // Also the achievements panel needs the achievements manager.
        if (this.addToAchievementsManager ())
        {

            try
            {

                Environment.getAchievementsManager ().addProjectViewer (this);

            } catch (Exception e) {

                Environment.logError ("Unable to add viewer to achievements manager.",
                                      e);

            }

        }

        this.handleOpenProject ();

        this.init (this.getInitState ());

        Environment.incrStartupProgress ();

        this.dBMan.createActionLogEntry (this.project,
                                         "Opened project",
                                         null,
                                         null);

        final AbstractProjectViewer _this = this;

        // Check the font used for the project and inform the user if we don't have it.
        // We do it here so it's done once.
        String f = this.project.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME);

        if (f != null)
        {
/*
TODO?
            java.awt.Font ft = new java.awt.Font (f,
                                java.awt.Font.PLAIN,
                                12);

            if (!ft.getFamily ().equalsIgnoreCase (f))
            {

                QuollPopup.messageBuilder ()
                    .headerIconClassName (StyleClassNames.WARNING)
                    .message (getUILanguageStringProperty (Arrays.asList (fontunavailable,text),
                                                           f))
                    .withViewer (this)
                    .closeButton ()
                    .build ();

            }
*/
        }

        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (ProjectEvent.Type.projectobject,
                               ProjectEvent.Action.open,
                               this.project);

		UIUtils.forceRunLater (onOpen);

        this.scheduleTargetReachedCheck ();

    }

    private void checkForChaptersOverWordCountCheck ()
    {

        // Check to see if any chapters have overrun the target.
		UIUtils.runLater (() ->
		{

			try
			{

				int wc = this.getProjectTargets ().getMaxChapterCount ();

				Set<Chapter> wchaps = this.getChaptersOverWordTarget ();

				int s = wchaps.size ();

				if ((wc > 0)
					&&
					(s > 0)
				   )
				{

					for (Chapter c : wchaps)
					{

						this.chapterWordCountTargetWarned.put (c,
															   new Date ());

					}

                    String t = LanguageStrings.single;

                    if (s > 1)
                    {

                        t = LanguageStrings.multiple;

                    }

                    QuollTextView m = QuollTextView.builder ()
                        .text (getUILanguageStringProperty (Arrays.asList (LanguageStrings.targets, chaptersoverwcmaximum,t),
                                                            wchaps.size (),
                                                            wc))
                        .build ();

					final Notification n = this.addNotification (m,
											   				     StyleClassNames.WORDCOUNTS,
																 90);

                      m.setOnMouseClicked (ev ->
                      {

                          if (ev.getButton () != MouseButton.PRIMARY)
                          {

                              return;

                          }

                          n.removeNotification ();

                          Set<Chapter> chaps = this.getChaptersOverWordTarget ();

                          if (chaps.size () == 0)
                          {

                              return;

                          }

                          this.showChaptersOverWordCountTarget ();

                      });

				}

				Set<Chapter> rchaps = this.getChaptersOverReadabilityTarget ();

				s = rchaps.size ();

				if (s > 0)
				{

                    String t = LanguageStrings.single;

                    if (s > 1)
                    {

                        t = LanguageStrings.multiple;

                    }

                    QuollTextView m = QuollTextView.builder ()
                    //BasicHtmlTextFlow m = BasicHtmlTextFlow.builder ()
                        .text (getUILanguageStringProperty (Arrays.asList (LanguageStrings.targets, chaptersoverreadabilitymaximum,t),
                                                            rchaps.size ()))
                        .inViewer (this)
                        .build ();

					final Notification n = this.addNotification (m,
																  StyleClassNames.READABILITY,
																  90);

                      m.setOnMouseClicked (ev ->
                      {

                          if (ev.getButton () != MouseButton.PRIMARY)
                          {

                              return;

                          }

                          n.removeNotification ();

                          Set<Chapter> chaps = this.getChaptersOverReadabilityTarget ();

                          if (chaps.size () == 0)
                          {

                              return;

                          }

                          this.showChaptersOverReadabilityTarget ();

                      });

				}

			} catch (Exception e) {

				Environment.logError ("Unable to display chapters over target notification",
									  e);

			}

		});

    }

    private void scheduleTargetReachedCheck ()
    {

        final AbstractProjectViewer _this = this;

        this.schedule (() ->
		{

			try
			{

				int wc = _this.getProjectTargets ().getMaxChapterCount ();

				if (wc > 0)
				{

					Book b = _this.project.getBooks ().get (0);

					final Set<Chapter> over = new LinkedHashSet ();

					java.util.List<Chapter> chapters = b.getChapters ();

					Date d = new Date ();

					// 15 minutes ago.
					long last = System.currentTimeMillis () - 15 * 60 * 1000;

					for (Chapter c : chapters)
					{

					    ChapterCounts cc = _this.getChapterCounts (c);

                        if (cc == null)
                        {

                            continue;

                        }

						final Chapter _c = c;

						if (cc.getWordCount () > wc)
						{

                            this.chaptersOverWordCountTarget.add (c);

							if (!_this.chapterWordCountTargetWarned.containsKey (c))
							{

								_this.chapterWordCountTargetWarned.put (c,
																		new Date ());

								over.add (c);

							}

						} else {

                            this.chaptersOverWordCountTarget.remove (c);

							Date od = _this.chapterWordCountTargetWarned.get (c);

							// Only remove if it's been 15 minutes since we last warned the user.
							// This provides a buffer so that they aren't constantly nagged about
							// it going over, for example if they've deleted/edited a sentence, removed
							// a word or two to go below the target then added some back in.
							if ((od != null)
								&&
								(od.getTime () < last)
							   )
							{

								_this.chapterWordCountTargetWarned.remove (c);

							}

						}

					}

					if ((over.size () > 0)
                        &&
					    (_this.getProjectTargets ().isShowMessageWhenMaxChapterCountExceeded ())
                       )
					{

						// Show a message.
						UIUtils.runLater (() ->
                        {

							final int s = over.size ();

                            String text = null;

                            List<String> messIds = null;
                            List<Object> reps = new ArrayList<> ();

							if (s == 1)
							{

								Chapter c = over.iterator ().next ();

                                messIds = Arrays.asList (LanguageStrings.targets, LanguageStrings.popup,singleoverlimit);
                                reps.add (c.getName ());
								//text = String.format ("{Chapter} <b>%s</b> is over the target word count, click to view it.",
									//				  c.getName ());

							} else {

                                messIds = Arrays.asList (LanguageStrings.targets, LanguageStrings.popup,multipleoverlimit);
                                reps.add (s);

                            }

                            Set<Button> buttons = new LinkedHashSet<> ();
                            Button showDetail = QuollButton.builder ()
                                .label (LanguageStrings.buttons,showdetail)
                                .buttonType (ButtonBar.ButtonData.OK_DONE)
                                .build ();
                            buttons.add (showDetail);

                            QuollPopup qp = QuollPopup.messageBuilder ()
                                .withViewer (_this)
                                .styleClassName (StyleClassNames.WORDCOUNTS)
                                .title (LanguageStrings.targets,popup,title)
                                .message (getUILanguageStringProperty (messIds,
                                                                       reps))
                                .buttons (buttons)
                                .build ();

                            showDetail.setOnAction (ev ->
                            {

                                if (over.size () == 1)
                                {

                                    _this.viewObject (over.iterator ().next ());
                                    qp.close ();
                                    return;

                                }

                                this.showChaptersOverWordCountTarget ();
                                qp.close ();

                            });

						});

					}

				}

			} catch (Exception e) {

				Environment.logError ("Unable to determine chapters that are over target",
									  e);

			}

		},
		30 * 1000,
		5 * 1000);

    }

    public void newProject (Path    saveDir,
                            Project p)
                     throws Exception
    {

        this.newProject (saveDir,
                         p,
                         null);

    }

    public void newProject (Path     saveDir,
                            Project  p,
                            Runnable onOpen)
                     throws Exception
    {

        if (p == null)
        {

            throw new GeneralException ("No project provided.");

        }

        if (p.getName () == null)
        {

            throw new GeneralException ("No project name provided.");

        }

        this.setIgnoreProjectEvents (true);

        this.dBMan = Environment.createProject (saveDir,
                                                p);

        this.project = this.dBMan.getProject ();

        if ((this.project.getBooks () == null) ||
            (this.project.getBooks ().size () == 0) ||
            (this.project.getBooks ().get (0).getChapters ().size () == 0))
        {

            Book b = null;

            if ((this.project.getBooks () != null)
                &&
                (this.project.getBooks ().size () > 0)
               )
            {

                b = this.project.getBooks ().get (0);

            } else {

                b = new Book (this.project,
                              this.project.getName ());

                this.project.addBook (b);

                this.saveObject (b,
                                 true);

            }

        }

        this.project.addPropertyChangedListener (this);

        // This is done here because the achievements manager needs the project.
        // Also the achievements panel needs the achievements manager.
        if (this.addToAchievementsManager ())
        {

            try
            {

                Environment.getAchievementsManager ().addProjectViewer (this);

            } catch (Exception e) {

                Environment.logError ("Unable to add viewer to achievements manager.",
                                      e);

            }

        }

        this.handleNewProject ();

        this.init (this.getInitState ());

        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (ProjectEvent.Type.projectobject,
                               ProjectEvent.Action._new,
                               this.project);

        // Register ourselves with the environment.
        try
        {

            Environment.addOpenedProject (this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add opened project (probably an error with the projects file): " +
                                  this.project,
                                  e);

        }

		UIUtils.runLater (onOpen);

    }

    private void scheduleA4PageCountUpdate ()
    {

        final AbstractProjectViewer _this = this;

        // Generate the A4 page counts.
        this.schedule (() ->
        {

            Book b = _this.project.getBooks ().get (0);

            java.util.List<Chapter> chapters = b.getChapters ();

            for (Chapter c : chapters)
            {

                String t = _this.getCurrentChapterText (c);

				ChapterCounts cc = _this.getChapterCounts (c);

                try
                {

                    cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                               t));

                } catch (Exception e) {

                    // Just ignore any errors, it's next to impossible to stop them.

                }

            }

        },
        1,
        0);

    }

    public Map<String, Panel> getPanels ()
    {

        return new LinkedHashMap<> (this.panels);

    }

    public void saveObject (NamedObject o,
                            Connection  conn)
                     throws GeneralException
    {

        if (o == null)
		{

			return;

		}

        java.util.Set<NamedObject> otherObjects = o.getOtherObjectsInLinks ();

        if (this.dBMan == null)
        {

            throw new IllegalStateException ("No object manager available.");

        }

        this.dBMan.saveObject (o,
                               conn);

    }

    public void saveObject (NamedObject o,
                            boolean     doInTransaction)
                     throws GeneralException
    {

        this.saveObject (o,
                         null);

    }

    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects)
                       throws GeneralException
    {

        if (o == null)
		{

			return;

		}

        if (this.dBMan == null)
        {

            throw new IllegalStateException ("No object manager available.");

        }

        this.dBMan.deleteObject (o,
                                 deleteChildObjects);

    }

    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects,
                              Connection  conn)
                       throws GeneralException
    {

        if (o == null)
		{

			return;

		}

        if (this.dBMan == null)
        {

            throw new IllegalStateException ("No object manager available.");

        }

        this.dBMan.deleteObject (o,
                                 deleteChildObjects,
                                 conn);

    }

    public Set<Note> getNotesForVersion (ProjectVersion pv)
                                  throws GeneralException
    {

        NoteDataHandler ndh = (NoteDataHandler) this.dBMan.getHandler (Note.class);

        return ndh.getNotesForVersion (pv,
                                       null);

    }

    public Set<Note> getDealtWithNotes (ProjectVersion pv,
                                        boolean        isDealtWith)
                                 throws GeneralException
    {

        NoteDataHandler ndh = (NoteDataHandler) this.dBMan.getHandler (Note.class);

        return ndh.getDealtWith (pv,
                                 isDealtWith,
                                 null);

    }

    public void updateChapterIndexes (Book b)
                               throws GeneralException
    {

        this.dBMan.updateChapterIndexes (b);

    }

    public Set<ChapterEditorPanelContent> getEditorsTextModifiedSince (long t)
    {

        return this.panels.values ().stream ()
            .map (p ->
            {

                Node c = p.getContent ();

                if (c instanceof ChapterEditorPanelContent)
                {

                    return (ChapterEditorPanelContent) c;

                }

                return null;

            })
            .filter (p ->
            {

                return (p != null)
                        &&
                       (p.textLastModifiedTimeProperty ().getValue () > t);

            })
            .collect (Collectors.toSet ());

    }

    public ChapterEditorPanelContent getEditorForChapter (Chapter c)
    {

        // TODO Make full screen aware?
        NamedObjectPanelContent p = this.getPanelForObject (c);

        if (p instanceof ChapterEditorPanelContent)
        {

            return (ChapterEditorPanelContent) p;

        }

        return null;

    }

    public NamedObjectPanelContent getPanelForObject (NamedObject n)
    {

        if (n == null)
        {

            throw new NullPointerException ("Expected an object to be passed.");

        }

        for (Panel qp : this.panels.values ())
        {

            Node c = qp.getContent ();

			NamedObjectPanelContent pqp = null;
/*
TODO
			if (qp instanceof FullScreenQuollPanel)
			{

				FullScreenQuollPanel fqp = (FullScreenQuollPanel) qp;

				if (fqp.getChild () instanceof ProjectObjectQuollPanel)
				{

					pqp = (ProjectObjectQuollPanel) fqp.getChild ();

				}

			}
*/
			// This is getting silly...
			// TODO: Fix this up.
            if (c instanceof NamedObjectPanelContent)
            {

                pqp = (NamedObjectPanelContent) c;

            }

			if ((pqp != null)
				&&
				(pqp.getObject ().equals (n))
			   )
			{

				return pqp;

			}

        }

        return null;

    }

    public void saveObjects (java.util.List<? extends NamedObject> objs,
                             boolean        doInTransaction)
                      throws GeneralException
    {

        this.dBMan.saveObjects (objs,
                                null);

    }

    public void toggleSpellChecking ()
    {

        this.setSpellCheckingEnabled (!this.isSpellCheckingEnabled ());

    }

    public boolean isSpellCheckingEnabled ()
    {

        return this.spellCheckingEnabledProp.getValue ();

    }

    public void setSpellCheckingEnabled (final boolean v)
    {

        this.spellCheckingEnabledProp.setValue (v);

        this.fireProjectEvent (ProjectEvent.Type.spellcheck,
                               (v ? ProjectEvent.Action.on : ProjectEvent.Action.off));

    }

    public StringProperty projectSpellCheckLanguageProperty ()
    {

        return this.projectSpellCheckLanguageProp;

    }

    public BooleanProperty spellCheckingEnabledProperty ()
    {

        return this.spellCheckingEnabledProp;

    }

    public Project getProject ()
    {

        return this.project;

    }

    public Panel getCurrentlyVisibleTab ()
    {

        Tab t = this.tabs.getSelectionModel ().getSelectedItem ();

        if (t == null)
        {

            return null;

        }

        Panel qp = (Panel) t.getContent ();

        return qp;

    }

    public Tab addPanel (final NamedObjectPanelContent qp)
                  throws GeneralException
    {

        Tab tab = this.addPanel (qp.getPanel ());

        tab.textProperty ().bind (Bindings.createStringBinding (() ->
        {

            String t = qp.getPanel ().titleProperty ().getValue ();

            if (qp.unsavedChangesProperty ().getValue ())
            {

                t = t + " *";

            }

            return t;

        },
        qp.getPanel ().titleProperty (),
        qp.unsavedChangesProperty ()));

        if (qp.iconProperty () != null)
        {

            ImageView iv = new ImageView ();
            iv.imageProperty ().bind (qp.iconProperty ());

            tab.setGraphic (iv);

        }

        // We use the panel itself to listen so when it is closed the listener
        // is removed.
        javafx.beans.value.ChangeListener<Boolean> l = (val, oldv, newv) ->
        {

            if (newv)
            {

                tab.getStyleClass ().add (StyleClassNames.HASCHANGES);

            } else {

                tab.getStyleClass ().remove (StyleClassNames.HASCHANGES);

            }

        };

        qp.addChangeListener (qp.unsavedChangesProperty (),
                              l);

        qp.addEventHandler (Panel.PanelEvent.SAVED_EVENT,
                            ev ->
        {

            tab.getStyleClass ().remove (StyleClassNames.HASCHANGES);

        });

        return tab;

    }

    public Tab addPanel (final PanelContent qp)
                  throws GeneralException
    {

        return this.addPanel (qp.getPanel ());

    }

    public Tab addPanel (final Panel qp)
                  throws GeneralException
    {

        final AbstractProjectViewer _this = this;

        Tab tab = new Tab ();
        tab.textProperty ().bind (qp.titleProperty ());
        tab.getStyleClass ().add (qp.getStyleClassName ());
        tab.setContent (qp);

        IconBox h = IconBox.builder ()
            .iconName (qp.getStyleClassName ())
            .build ();

        tab.setGraphic (h);

        final String panelId = qp.getPanelId ();

        final String s = this.project.getProperty (panelId + "-state");

        State state = null;

        if (s != null)
        {

            state = new State (s);

        }

        if (this.getViewer ().isShowing ())
        {

            qp.getContent ().init (state);

        } else {

            final State _state = state;

            this.getViewer ().showingProperty ().addListener ((pr, oldv, newv) ->
            {

                if (newv)
                {

                    UIUtils.runLater (() ->
                    {

                        try
                        {

                            qp.getContent ().init (_state);

                        } catch (Exception e) {

                            Environment.logError ("Unable to init panel: " +
                                                  qp +
                                                  " with state: " +
                                                  _state,
                                                  e);

                        }

                    });

                }

            });

        }

        this.tabs.getTabs ().add (0,
                                  tab);

        this.panels.put (panelId,
                         qp);

        tab.setOnCloseRequest (ev ->
        {

            ev.consume ();

            PanelContent pc = qp.getContent ();

            if (pc instanceof NamedObjectPanelContent)
            {

                this.closePanel ((NamedObjectPanelContent) pc,
                                 null);

                return;

            }

            this.closePanel (qp,
                             null);

        });

        return tab;

    }

    public void closePanel (NamedObject n,
                            Runnable    onClose)
    {

        NamedObjectPanelContent c = this.getPanelForObject (n);

        if (c == null)
        {

            return;

        }

        this.closePanel (c,
                         onClose);

    }

    public void closePanel (NamedObjectPanelContent p,
                            Runnable                onClose)
    {

        final AbstractProjectViewer _this = this;

        // Object already deleted, don't ask the question.
        if ((p.getObject ().getKey () != null)
            &&
            (!this.project.hasObject (p.getObject ()))
           )
        {

            this.closePanel (p.getPanel (),
                             onClose);

            return;

        }

        if (p.unsavedChangesProperty ().getValue ())
        {
/*
TODO Remove?
            if ((_this.fsf != null)
                &&
                (p == _this.fsf.getPanel ())
               )
            {

                // In full screen, restore first.
                _this.restoreFromFullScreen (_this.fsf.getPanel ());

                // Add a blank instead.
                // TODO? _this.fsf.showBlankPanel ();

            }
*/
            String popupId = "close-" + p.getPanelId ();

            QuollPopup qp = this.getPopupById (popupId);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            qp = QuollPopup.questionBuilder ()
                .popupId (popupId)
                .styleClassName (StyleClassNames.SAVE)
                .title (closepanel,confirmpopup,title)
                .message (getUILanguageStringProperty (Arrays.asList (closepanel,confirmpopup,text),
                                                       p.getPanel ().titleProperty ()))
                .withViewer (this)
                .confirmButtonLabel (closepanel,confirmpopup,buttons,save)
                .cancelButtonLabel (closepanel,confirmpopup,buttons,discard)
                .onCancel (fev ->
                {

                    _this.closePanel (p.getPanel (),
                                      onClose);

                })
                .onConfirm (fev ->
                {

                    try
                    {

                        p.saveObject ();

                    } catch (Exception e)
                    {

                        // What the hell to do here???
                        Environment.logError ("Unable to save: " +
                                              p.getObject (),
                                              e);

                        ComponentUtils.showErrorMessage (_this,
                                                         getUILanguageStringProperty (closepanel,actionerror));
                                                  //"Unable to save " +
                                                  //Environment.getObjectTypeName (p.getForObject ()));

                        return;

                    }

                    this.getPopupById (popupId).close ();

                    if (p.unsavedChangesProperty ().getValue ())
                    {

                        ComponentUtils.showErrorMessage (_this,
                                                         getUILanguageStringProperty (closepanel,actionerror));
                                                  //"Unable to save " +
                                                  //Environment.getObjectTypeName (p.getForObject ()));

                        return;

                    }

                    _this.closePanel (p.getPanel (),
                                      onClose);

                })
                .build ();

            return;

        }

        this.closePanel (p.getPanel (),
                         onClose);

    }

    public void closePanel (Panel qp,
                            Runnable   onClose)
    {

        // Get the state.
        String panelId = qp.getPanelId ();

        try
        {

            this.project.setProperty (panelId + "-state",
                                      qp.getState ().asString ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to save state for panel: " +
                                  panelId,
                                  e);

        }

        this.removePanel (qp,
                          onClose);

        Panel.PanelEvent pe = new Panel.PanelEvent (qp,
                                                    Panel.PanelEvent.CLOSE_EVENT);

        qp.fireEvent (pe);

        this.fireEvent (pe);

    }

    public void removeCurrentPanel (Runnable onRemove)
    {

        this.removePanel (this.currentPanelProp.getValue (),
                          onRemove);

    }

    public void removePanel (NamedObject n,
                             Runnable    onRemove)
    {

        this.removePanel (n.getObjectReference ().asString (),
                          onRemove);

    }

    private void removePanel (String   panelId,
                              Runnable onRemove)
    {

        Panel p = this.getPanel (panelId);

        if (p == null)
        {

            return;

        }

        this.removePanel (p,
                          onRemove);

    }

    public void removePanel (Panel    p,
                             Runnable onRemove)
    {

		String panelId = p.getPanelId ();

		int tInd = this.getTabIndexForPanelId (panelId);

		if (tInd > -1)
		{

			this.tabs.getTabs ().remove (tInd);

		}

        // Remove the action mappings.
        p.getActionMappings ().keySet ().stream ()
            .forEach (k -> this.getScene ().getAccelerators ().remove (k));

        p.fireEvent (new Panel.PanelEvent (p,
                                           Panel.PanelEvent.CLOSE_EVENT));

		this.panels.remove (panelId);

        if (onRemove != null)
        {

            UIUtils.runLater (onRemove);

        }

	}

    public Panel getPanel (String panelId)
    {

        return this.panels.get (panelId);

    }

    /**
	 * Determine the number of words written for this project during this session.
	 *
	 * @return The current word count - the start word count.
	 */
	public int getSessionWordCount ()
	{

		return this.sessionWordCount;

	}

    public IntegerProperty sessionWordCountProperty ()
    {

        return this.sessionWordCountProp;

    }

    public Set<ChapterCounts> getAllChapterCountsAsSet ()
	{

		return new LinkedHashSet<ChapterCounts> (this.chapterCounts.values ());

	}

    private void updateAllChapterCounts ()
    {

        int wc = 0;
        int pc = 0;
        int sc = 0;

        ReadabilityIndices ri = new ReadabilityIndices ();

        for (ChapterCounts cc : this.chapterCounts.values ())
		{

            wc += cc.getWordCount ();
            pc += cc.getStandardPageCount ();
            sc += cc.getSentenceCount ();

            ri.add (cc.getReadabilityIndices ());

		}

        this.allChapterCounts.setWordCount (wc);
        this.allChapterCounts.setSentenceCount (sc);
        this.allChapterCounts.setStandardPageCount (pc);

        this.sessionWordCount = this.allChapterCounts.getWordCount () - this.startWordCounts.getWordCount ();

        this.allChapterCounts.getReadabilityIndices ().updateFrom (ri);

        UIUtils.runLater (() ->
        {

            this.sessionWordCountProp.setValue (this.sessionWordCount);

        });

    }

    public ChapterCounts getAllChapterCounts ()
    {

        return this.allChapterCounts;

    }

    public ChapterCounts getStartWordCounts ()
    {

        return this.startWordCounts;

    }

    public ChapterCounts getChapterCounts (Chapter c)
    {

		return this.chapterCounts.get (c);

    }

    public ReadabilityIndices getAllReadabilityIndices ()
    {

        return this.allChapterCounts.getReadabilityIndices ();

    }

    public ReadabilityIndices getReadabilityIndices (String  text)
    {

        ReadabilityIndices ri = new ReadabilityIndices ();

        if (text != null)
        {

            ri.add (text);

        }

        return ri;


    }

    public ReadabilityIndices getReadabilityIndices (Chapter c)
    {

        ChapterCounts cc = this.chapterCounts.get (c);

        if (cc == null)
        {

            return null;

        }

        return cc.getReadabilityIndices ();

    }

	public int getChapterA4PageCount (Chapter c)
	{

		return UIUtils.getA4PageCountForChapter (c,
												 this.getCurrentChapterText (c));

	}

    public boolean isCurrentPanelChapterEditor ()
    {

        Panel p = this.getCurrentPanel ();

        if (p == null)
        {

            return false;

        }

        if (p.getContent () instanceof ChapterEditorPanelContent)
        {

            return true;

        }

        return false;

    }

    public String getCurrentChapterText (Chapter c)
	{

        ChapterEditorPanelContent qep = this.getEditorForChapter (c);

		StringWithMarkup t = null;

        if (qep != null)
        {

			t = qep.getText ();

        } else {

			t = (c.getText () != null ? c.getText () : null);

        }

        if (t != null)
        {

            return t.getText ();

        }

		return null;

	}

    protected int getTabIndexForObject (NamedObject n)
    {

        return this.getTabIndexForPanelId (n.getObjectReference ().asString ());

    }

    protected int getTabIndexForPanelId (String panelId)
    {

        for (int i = 0; i < this.tabs.getTabs ().size (); i++)
        {

            Tab t = this.tabs.getTabs ().get (i);

            Node n = t.getContent ();

            if (n instanceof Panel)
            {

                Panel p = (Panel) n;

                if (p.getPanelId ().equals (panelId))
                {

                    return i;

                }

            }

        }

        return -1;

    }

    public static AbstractProjectViewer createProjectViewerForType (String  t)
                                                             throws Exception
    {

        Class c = AbstractProjectViewer.viewerTypes.get (t);

        if (c == null)
        {

            throw new IllegalArgumentException ("No viewer class registered for type: " + t);

        }

        return (AbstractProjectViewer) c.getDeclaredConstructor ().newInstance ();

    }

    public static AbstractProjectViewer createProjectViewerForType (ProjectInfo p)
                                                             throws Exception
    {

        return AbstractProjectViewer.createProjectViewerForType (p.getType ());

    }

    public static AbstractProjectViewer createProjectViewerForType (Project p)
                                                             throws Exception
    {

        return AbstractProjectViewer.createProjectViewerForType (p.getType ());

    }

    public void initSideBar (SideBar sb)
    {

        String sid = sb.getSideBarId ();

        String state = null;

        if (sid != null)
        {

            state = this.project.getProperty ("sidebarState-" + sid);

        }

        State s = null;

        if (state != null)
        {

            s = new State (state);

        }

        sb.init (s);

    }

    public void setUseTypewriterScrolling (boolean v)
    {

        this.typeWriterScrollingEnabledProp.setValue (v);

    }

    public boolean isUseTypeWriterScrolling ()
    {

        return this.typeWriterScrollingEnabledProp.getValue ();

    }

    public BooleanProperty typeWriterScrollingEnabledProperty ()
    {

        return this.typeWriterScrollingEnabledProp;

    }

    private void startAutoBackups ()
	{

		final AbstractProjectViewer _this = this;

		this.schedule (() ->
		{

			try
			{

				// Get references first so that they can't change further on.
				Project proj = _this.project;
				ObjectManager dBMan = _this.dBMan;

				if ((proj == null)
					||
					(dBMan == null)
				   )
				{

					return;

				}

				if (proj.isAutoBackupsEnabled ())
				{

					// Get the last backup.
					Path last = dBMan.getLastBackupFile (proj);

					long lastDate = (last != null ? Files.getLastModifiedTime (last).toMillis () : proj.getDateCreated ().getTime ());

					if ((System.currentTimeMillis () - lastDate) > proj.getAutoBackupsTime ())
					{

                        BackupsManager.createBackupForProject (proj,
                                                               false);

						UIUtils.runLater (() ->
						{

							try
							{

								_this.addNotification (getUILanguageStringProperty (Arrays.asList (backups,autobackupnotification),
                                                                                    //"An automatic backup has been created.  <a href='%s:%s'>Click to view the backups.</a>",
                                                                                    Constants.ACTION_PROTOCOL,
                                                                                    Constants.BACKUPS_HTML_PANEL_ACTION),
													  StyleClassNames.INFORMATION,
													  30);

							} catch (Exception e) {

								// Sigh...

							}

						});

					}

				}

			} catch (Exception e) {

				if (_this.project == null)
				{

					// Means we have shutdown.
					return;

				}

				Environment.logError ("Unable to create backup for project: " +
									  _this.project,
									  e);

			}

		},
		// Start straight away.
		0,
		// Run every 10 mins.
		10 * 60 * 1000);

	}

    protected void restoreSideBars ()
                             throws GeneralException
    {

        // TODO

    }

    /**
     * Responsible for setting up the panels that should be open when the project is opened.
     * If you override this method ensure that you do:
     *   * Open the necessary panels.
     *   * Set the last opened tab (after opening it).
     */
    protected void restoreTabs ()
    {

        ProjectVersion pv = this.project.getProjectVersion ();

        String suffix = "";

        // See if we have a project version.
        if (pv != null)
        {

            suffix = ":" + pv.getKey ();

        }

        // Setup the tabs.
        String openTabs = this.getOpenTabsProperty ();

        this.openPanelsFromObjectIdList (openTabs);
        String lastOpen = this.project.getProperty (Constants.LAST_OPEN_TAB_PROPERTY_NAME + suffix);

        if (lastOpen != null)
        {

            final Panel qp = this.getPanel (lastOpen);

            if (qp != null)
            {

                int tInd = this.getTabIndexForPanelId (qp.getPanelId ());

                this.tabs.selectionModelProperty ().getValue ().select (tInd);

            }

        }

    }

    /**
     * Given a list of comma separated object ids, open the panels for the objects (if available).
     * So an example <b>ids</b> might be: chapter-1,character-2,chapter-3.
     * This will be parsed into 3 values and then the relevant objects searched for in the project.
     * If there is an associated object then it will be opened for viewing.
     *
     * @param ids The object ids, comma separated.
     */
    public void openPanelsFromObjectIdList (String ids)
    {

        if ((ids == null)
            ||
            (ids.trim ().equals (""))
           )
        {

            return;

        }

        List<String> objIds = new ArrayList<> ();

        // Split on :
        StringTokenizer t = new StringTokenizer (ids,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            objIds.add (tok);

        }

        Collections.reverse (objIds);

        for (String panelId : objIds)
        {

            ObjectReference r = null;

            try
            {

                r = ObjectReference.parseObjectReference (panelId);

            } catch (Exception e)
            {

                Environment.logError ("Unable to parse: " +
                                      panelId,
                                      e);

                continue;

            }

            // Pass it to the project.
            final DataObject d = this.project.getObjectForReference (r);

            if (d == null)
            {

                try
                {

                    this.openPanelForId (panelId);

                } catch (Exception e) {

                    Environment.logError ("Unable to open panel for id: " +
                                          panelId,
                                          e);

                    continue;

                }

            } else
            {

                this.viewObject (d);

            }

        }

    }

    public boolean showPanel (String pid)
    {

        Tab pt = null;
        Panel p = null;

        for (Tab t : this.tabs.getTabs ())
        {

            Panel tp = (Panel) t.getContent ();

            String tpid = tp.getPanelId ();

            if (tp.getContent () instanceof ShowingInFullScreenPanel)
            {

                tpid = ((ShowingInFullScreenPanel) tp.getContent ()).getOriginalPanel ().getPanelId ();

            }

            if (tpid.equals (pid))
            {

                this.tabs.getSelectionModel ().select (t);
                pt = t;
                p = tp;
                break;

            }

        }

        if (pt == null)
        {

            return false;

        }

        return true;

    }

    /**
     * Return the name of the property that is used to save the open tabs, this is needed because is the project
     * has a project version object then the name of the property will change.
     *
     * @return The open tabs property name in the form {@link Constants.OPENS_TAB_PROPERTY_NAME} + [":" + project version key].
     */
    public String getOpenTabsPropertyName ()
    {

        // See if we have a project version.
        ProjectVersion pv = this.project.getProjectVersion ();

        String suffix = "";

        if (pv != null)
        {

            suffix = ":" + pv.getKey ();

        }

        return Constants.OPEN_TABS_PROPERTY_NAME + suffix;

    }

    public String getOpenTabsProperty ()
    {

        String name = this.getOpenTabsPropertyName ();

        return this.project.getProperty (name);

    }

    public void saveProjectTargets ()
	{

		try
		{

			this.saveObject (this.project,
							 false);

		} catch (Exception e) {

			Environment.logError ("Unable to update project targets",
								  e);

		}

	}

    public TargetsData getProjectTargets ()
	{

		return this.targets;

	}

    public Set<Chapter> getChaptersOverReadabilityTarget ()
    {

        Set<Chapter> chaps = new LinkedHashSet ();

        if (this.getProject () == null)
        {

            // Closing down.
            return chaps;

        }

        TargetsData projTargets = this.getProjectTargets ();

        int tFK = projTargets.getReadabilityFK ();
        int tGF = projTargets.getReadabilityGF ();

        if ((tFK > 0)
            ||
            (tGF > 0)
           )
        {

            for (Book book : this.getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ReadabilityIndices ri = this.getReadabilityIndices (c);

                    ChapterCounts cc = this.getChapterCounts (c);

                    if (cc.getWordCount () < Constants.MIN_READABILITY_WORD_COUNT)
                    {

                        continue;

                    }

                    float fk = ri.getFleschKincaidGradeLevel ();
                    float gf = ri.getGunningFogIndex ();

                    if ((tFK > 0)
                        &&
                        (ri.getFleschKincaidGradeLevel () > tFK)
                       )
                    {

                        chaps.add (c);

                        continue;

                    }

                    if ((tGF > 0)
                        &&
                        (ri.getGunningFogIndex () > tGF)
                       )
                    {

                        chaps.add (c);

                        continue;

                    }

                }

            }

        }

        return chaps;

    }

    public Set<Chapter> getChaptersOverWordTarget ()
    {

        Set<Chapter> chaps = new LinkedHashSet ();

        if (this.getProject () == null)
        {

            // Closing down.
            return chaps;

        }

        TargetsData projTargets = this.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();

        if (tcc > 0)
        {

            for (Book book : this.getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ChapterCounts count = this.getChapterCounts (c);

                    if (count.getWordCount () > tcc)
                    {

                        chaps.add (c);

                    }

                }

            }

        }

        return chaps;

    }

    private void initProperties ()
    {

        this.spellCheckingEnabledProp.setValue (this.project.getPropertyAsBoolean (Constants.SPELL_CHECKING_ENABLED_PROPERTY_NAME));

        // TODO Bind...
        String c = this.project.getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

        // Get the property.
        if (c == null)
        {

            c = UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

        }

        if (UILanguageStrings.isEnglish (c))
        {

            c = Constants.ENGLISH;

        }

        // Handle legacy language values.
        if (("US English".equals (c))
            ||
            ("UK English".equals (c))
           )
        {

            c = Constants.ENGLISH;

        }

        this.projectSpellCheckLanguageProp.setValue (c);

    }

    private void initDictionaryProvider ()
    {

        final String lang = this.getProjectSpellCheckLanguage ();

        try
        {

            if ((!DictionaryProvider.isLanguageInstalled (lang))
                &&
                (lang != null)
               )
            {

                final AbstractProjectViewer _this = this;

                // Turn off spell checking until the download is complete.
                this.setProjectSpellCheckLanguage (null);

                // TODO Change to use UIUtils.downloadDictionaryFiles instead...

                // Download them.
                DictionaryProvider.downloadDictionaryFiles (lang,
                                                            this,
                                                            () ->
                                                            {

                                                                  try
                                                                  {

                                                                        _this.setProjectSpellCheckLanguage (null);

                                                                        _this.setProjectSpellCheckLanguage (lang);

                                                                  } catch (Exception e) {

                                                                      Environment.logError ("Unable to set spell check language to: " +
                                                                                            lang,
                                                                                            e);

                                                                      ComponentUtils.showErrorMessage (_this,
                                                                                                       getUILanguageStringProperty (Arrays.asList (spellchecker,unabletosetlanguage),
                                                                                                                                    lang));

                                                                  }

                                                              });

            } else {

                this.setProjectSpellCheckLanguage (lang);

            }

        } catch (Exception e) {

            Environment.logError ("Unable to check for spell check language",
                                  e);

        }

    }

    public boolean isHighlightWritingLine ()
    {

        return this.getTextProperties ().isHighlightWritingLine ();

    }

    /**
     * Get the correct text properties depending on whether the viewer is in full screen or not.
     *
     * @returns Normal project text properties if we are in normal mode or full screen text properties
     *          if we are in full screen mode.
     */
    public TextProperties getTextProperties ()
    {

        return (this.isInFullScreenMode () ? Environment.getFullScreenTextProperties () : Environment.getProjectTextProperties ());

    }

    public void viewObject (DataObject d)
    {

        this.viewObject (d,
                         null);

    }

    public void propertyChanged (PropertyChangedEvent ev)
    {

        DataObject d = (DataObject) ev.getSource ();

        if (d instanceof Project)
        {

            Project p = (Project) d;

            try
            {

                this.saveObject (p,
                                 true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save project: " +
                                      p,
                                      e);

            }

        }

    }

    @Override
    public void close (Runnable afterClose)
    {

        this.close (true,
                    afterClose);

    }

    public void close (boolean  preventCloseOnUnsavedChanges,
                       Runnable afterClose)
    {

        this.exitFullScreen ();

        if (preventCloseOnUnsavedChanges)
        {

            VBox c = new VBox ();

            AtomicBoolean hasChanges = new AtomicBoolean (false);

            String b = this.panels.values ().stream ()
                // We are only interested in named object panels.
                .filter (p ->
                {

                    if (p.getContent () instanceof NamedObjectPanelContent)
                    {

                        NamedObjectPanelContent np = (NamedObjectPanelContent) p.getContent ();

                        return np.hasUnsavedChanges ();

                    }

                    return false;

                })
                .map (p ->
                {

                    NamedObjectPanelContent np = (NamedObjectPanelContent) p.getContent ();

                    hasChanges.set (true);

                    return String.format ("<li><a href='%1$s://%2$s'>%3$s</a></li>",
                                          Constants.OBJECTREF_PROTOCOL,
                                          np.getObject ().getObjectReference ().asString (),
                                          np.getPanel ().titleProperty ().getValue ());

                })
                .collect (Collectors.joining (""));

            c.getChildren ().add (QuollTextView.builder ()
                .text (getUILanguageStringProperty (Arrays.asList (closeproject,confirmpopup,text),
                                                    b))
                .inViewer (this)
                .build ());

            if (hasChanges.get ())
            {

                String popupId = "close-window";

                QuollPopup qp = this.getPopupById (popupId);

                if (qp != null)
                {

                    qp.toFront ();
                    return;

                }

                QuollPopup.questionBuilder ()
                    .popupId (popupId)
                    .styleClassName (StyleClassNames.PROJECT)
                    .headerIconClassName (StyleClassNames.SAVE)
                    .title (closeproject,confirmpopup,title)
                    .message (c)
                    .withViewer (this)
                    .confirmButtonLabel (buttons,savechanges)
                    .cancelButtonLabel (buttons,discardchanges)
                    .onCancel (fev ->
                    {

                        this.closeInternal (false,
                                            afterClose);

                    })
                    .onConfirm (fev ->
                    {

                        this.closeInternal (true,
                                            afterClose);

                    })
                    .build ();

                return;

            }

        }

        this.closeInternal (false,
                            afterClose);

    }

    protected void saveTabsState ()
    {

        // Save it.
        try
        {

            Panel vqp = this.currentPanelProp.getValue ();

            String panelId = null;

            if (vqp != null)
            {

                panelId = vqp.getPanelId ();

            }

            // See if we have a project version.
            ProjectVersion pv = this.project.getProjectVersion ();

            String suffix = "";

            if (pv != null)
            {

                suffix = ":" + pv.getKey ();

            }

            this.project.setProperty (Constants.LAST_OPEN_TAB_PROPERTY_NAME + suffix,
                                      panelId);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save open tab id for project: " +
                                  this.project,
                                  e);

        }

        try
        {

            String s = this.tabs.getTabs ().stream ()
                .filter (t -> t.getContent () instanceof Panel)
                .map (t -> ((Panel) t.getContent ()).getPanelId ())
                .collect (Collectors.joining (","));

            this.project.setProperty (this.getOpenTabsPropertyName (),
                                      s);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save open tab ids for project: " +
                                  this.project,
                                  e);

        }

    }

    private boolean closeInternal (boolean  saveUnsavedChanges,
                                   Runnable afterClose)
    {

        if (this.project == null)
        {

            super.close (afterClose);
            return true;

        }

        if (saveUnsavedChanges)
        {

            // Save all.
            for (Panel p : this.panels.values ())
            {

				if (p.getContent () instanceof NamedObjectPanelContent)
				{

					NamedObjectPanelContent pqp = (NamedObjectPanelContent) p.getContent ();

					if (pqp.hasUnsavedChanges ())
					{

						boolean showError = false;

						try
						{

							pqp.saveObject ();

						} catch (Exception e)
						{

                            showError = true;

							Environment.logError ("Unable to save unsaved changes for: " +
												  pqp.getObject (),
												  e);

						}

						if ((showError)
                            ||
                            (pqp.hasUnsavedChanges ())
                           )
						{

							ComponentUtils.showErrorMessage (this,
													         getUILanguageStringProperty (Arrays.asList (closeproject,actionerror),
                                                                                          pqp.getPanel ().titleProperty ()));

							// Switch to the tab.
							this.viewObject (pqp.getObject ());

							return false;

						}

					}

				}

            }

        }

        // Get the state and save it.
        try
        {

            this.project.setProperty (Constants.PROJECT_STATE_PROPERTY_NAME,
                                      this.getState ().asString ());

        } catch (Exception e) {

            Environment.logError ("Unable to save project state.",
                                  e);

        }

        // Need to manually remove from the achievements managers since it updates the achievements property.
        try
        {

            Environment.getAchievementsManager ().removeViewer (this);

        } catch (Exception e) {

            Environment.logError ("Unable to remove viewer from achievements managers",
                                  e);

        }

        this.removeAllNotifications ();

        // Close all sidebars.
        for (SideBar sb : new ArrayList<SideBar> (this.getSideBars ()))
        {

            this.removeSideBar (sb);

        }

        this.saveTabsState ();

        for (Panel qp : new LinkedHashSet<> (this.panels.values ()))
        {

            // We close, rather than remove.  The close calls remove.
            this.closePanel (qp,
                             null);

        }

        this.project.setLastEdited (new Date ());

        try
        {

            for (Chapter c : this.chapterCounts.keySet ())
            {

                ChapterCounts cc = this.chapterCounts.get (c);

                if (cc != null)
                {

                    this.project.setProperty ("chapter.counts." + c.getKey (),
                                              cc.getState ().asString ());

                }

            }

        } catch (Exception e) {

            Environment.logError ("Unable to update chapter counts for project: " +
                                  this.project,
                                  e);

        }

        try
        {

            this.saveProject ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save project: " +
                                  this.project,
                                  e);

            return false;

        }

        this.project.removePropertyChangedListener (this);

        ChapterDataHandler ch = (ChapterDataHandler) this.dBMan.getHandler (Chapter.class);

        ch.saveWordCounts (this.project,
                           this.sessionStart,
                           new Date ());

        this.dBMan.createActionLogEntry (this.project,
                                         "Closed project",
                                         null,
                                         null);

        // Fire our last event.
        this.fireProjectEvent (ProjectEvent.Type.project,
                               ProjectEvent.Action.close);

        // Close all the db connections.
        this.dBMan.closeConnectionPool ();

        this.project = null;

        super.close (afterClose);

        return true;

    }

    @Override
    public void removeSideBar (SideBar sb)
    {

        if (sb != null)
        {

            try
            {

                State ss = sb.getState ();

                this.project.setProperty ("sidebarState-" + sb.getSideBarId (),
                                          ss.asString ());

            } catch (Exception e) {

                Environment.logError ("Unable to save state for sidebar: " +
                                      id,
                                      e);

            }

        }

        super.removeSideBar (sb);

    }

    @Override
    public StringProperty titleProperty ()
    {

        return this.titleProp;

    }

    public void createActionLogEntry (NamedObject n,
                                      String      m)
    {

        this.createActionLogEntry (n,
                                   m,
                                   null);

    }

    public void createActionLogEntry (NamedObject n,
                                      String      m,
                                      Connection  conn)
    {

        this.dBMan.createActionLogEntry (n,
                                         m,
                                         null,
                                         conn);

    }

    public void saveProject ()
                      throws GeneralException
    {

        this.dBMan.saveObject (this.project,
                               null);

    }

    public Map<Chapter, Set<Issue>> getProblemsForAllChapters ()
    {

        return this.getProblemsForAllChapters (null);

    }

    public Map<Chapter, Set<Issue>> getProblemsForAllChapters (Rule limitToRule)
    {

        Map<Chapter, Set<Issue>> probs = new LinkedHashMap ();

        if (this.project == null)
        {

            // Closing down.
            return probs;

        }

        for (Book book : this.project.getBooks ())
        {

            for (Chapter c : book.getChapters ())
            {

                Set<Issue> issues = null;

                if (limitToRule != null)
                {

                    issues = this.getProblems (c,
                                               limitToRule);

                } else {

                    issues = this.getProblems (c);

                }

                if (issues.size () > 0)
                {

                    probs.put (c, issues);

                }

            }

        }

        return probs;

    }

    public Set<Issue> getProblems (Chapter c,
                                   Rule    r)
    {

        Set<Issue> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            TextBlockIterator ti = new TextBlockIterator (ct);

            TextBlock b = null;

            while ((b = ti.next ()) != null)
            {

                java.util.List<Issue> issues = RuleFactory.getIssues (b,
                                                                      r,
                                                                      this.project.getProperties ());

                for (Issue i : issues)
                {

                    ret.add (i);

                    i.setChapter (c);

                }

            }

        }

        return ret;

    }

    public Set<Issue> getProblems (Chapter c)
    {

        Set<Issue> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            TextBlockIterator ti = new TextBlockIterator (ct);

            TextBlock b = null;

            while ((b = ti.next ()) != null)
            {

                if (b instanceof Paragraph)
                {

                    ret.addAll (RuleFactory.getParagraphIssues ((Paragraph) b,
                                                                this.project.getProperties ()));

                }

                if (b instanceof Sentence)
                {

                    ret.addAll (RuleFactory.getSentenceIssues ((Sentence) b,
                                                                this.project.getProperties ()));

                }

            }

        }

        return ret;

    }

    public Set<Word> getSpellingErrors (Chapter c)
    {

        Set<Word> ret = new LinkedHashSet<> ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            DictionaryProvider2 dp = this.getDictionaryProvider ();

            if (dp != null)
            {

                SpellChecker sc = dp.getSpellChecker ();

                if (sc != null)
                {

                    TextIterator ti = new TextIterator (ct);

                    for (Word w : ti.getWords ())
                    {

                        if (!sc.isCorrect (w))
                        {

                            ret.add (w);

                        }

                    }

                }

            }

        }

        return ret;

    }

    public void removePanel (NamedObject n)
    {

        this.removePanel (n,
                          null);

    }

    public void removePanel (NamedObjectPanelContent p)
    {

        this.removePanel (p.getPanel (),
                          null);

    }

    public void removeAllSideBarsForObject (NamedObject n)
    {

        this.getSideBars ().stream ()
            .filter (s ->
            {

                Node c = s.getContent ();

                if ((c instanceof NamedObjectSideBarContent)
                    &&
                    (((NamedObjectSideBarContent) c).getObject () == n)
                   )
                {

                    return true;

                }

                return false;

            })
            .forEach (s -> this.removeSideBar (s));

    }

    public void removeAllPanelsForObject (NamedObject n)
    {

        for (NamedObjectPanelContent p : this.getAllPanelsForObject (n))
        {

            this.removePanel (p);

        }

    }

    public Set<NamedObjectPanelContent> getAllPanelsForObject (NamedObject n)
    {

        return this.panels.values ().stream ()
            .filter (p ->
            {

                Node c = p.getContent ();

                if ((c instanceof NamedObjectPanelContent)
                    &&
                    (((NamedObjectPanelContent) c).getObject () == n)
                   )
                {

                    return true;

                }

                return false;

            })
            .map (p -> (NamedObjectPanelContent) p.getContent ())
            .collect (Collectors.toSet ());

    }

    private State getInitState ()
    {

        if (this.project == null)
        {

            throw new IllegalStateException ("No project set.");

        }

        State s = null;

        // We ignore the state and use the state inside the project instead.

        // Handle the legacy properties.
        if (this.project.getProperty (Constants.PROJECT_STATE_PROPERTY_NAME) == null)
        {

            s = new State ();

            if (this.project.getProperty (Constants.WINDOW_HEIGHT_PROPERTY_NAME) != null)
            {

                int wHeight = this.project.getPropertyAsInt (Constants.WINDOW_HEIGHT_PROPERTY_NAME);
                int wWidth = this.project.getPropertyAsInt (Constants.WINDOW_WIDTH_PROPERTY_NAME);
                int wTop = this.project.getPropertyAsInt (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME);
                int wLeft = this.project.getPropertyAsInt (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME);

                s.set (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
                       wHeight);
                s.set (Constants.WINDOW_WIDTH_PROPERTY_NAME,
                       wWidth);
                s.set (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME,
                       wTop);
                s.set (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME,
                       wLeft);
                this.project.removeProperty (Constants.WINDOW_HEIGHT_PROPERTY_NAME);
                this.project.removeProperty (Constants.WINDOW_WIDTH_PROPERTY_NAME);
                this.project.removeProperty (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME);
                this.project.removeProperty (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME);

            }

        } else {

            s = new State (this.project.getProperty (Constants.PROJECT_STATE_PROPERTY_NAME));

        }

        return s;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (this.project == null)
        {

            throw new IllegalStateException ("No project set.");

        }

        if (s == null)
        {

            throw new IllegalArgumentException ("Expected some state to be passed.");

        }

        super.init (s);

        this.initProperties ();

        // Needs to happen before initing th echapter counts since the chapter counts may need to do a spell check count.
        this.initDictionaryProvider ();

        this.initChapterCounts ();

        this.targets = new TargetsData (this.project.getProperties ());

        UIUtils.runLater (() ->
        {

            this.checkForChaptersOverWordCountCheck ();

        });

        this.startAutoBackups ();

        this.titleProp.bind (this.project.nameProperty ());

        this.restoreSideBars ();

        this.restoreTabs ();

        this.scheduleA4PageCountUpdate ();

        // This needs to go here since the call is made to the underlying viewer.
        this.addChangeListener (UserProperties.tabsLocationProperty (),
                                (p, oldv, newv) ->
        {

            // TODO Update the tabs location.
            this.tabs.setSide (newv.equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);

        });

        this.addKeyMapping (CommandId.find,
                            KeyCode.F1);
        this.addKeyMapping (CommandId.find,
                            KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.newproject,
                            KeyCode.N, KeyCombination.SHORTCUT_DOWN);

        this.setTitle (this.titleProp);
        this.windowedContent.setTitle (this.titleProp);

    }

    public void setMainSideBar (SideBarContent sb)
    {

        this.setMainSideBar (sb.getSideBar ());

    }

    @Override
    public void setMainSideBar (SideBar sb)
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

        this.initSideBar (sb);

        super.setMainSideBar (sb);

    }

    @Override
    public void addSideBar (SideBarContent sb)
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

        this.initSideBar (sb.getSideBar ());

        super.addSideBar (sb);

    }

    @Override
    public Node getTitleHeaderControl (HeaderControl control)
    {

        if (control == HeaderControl.timer)
        {

            return WordCountTimerButton.builder ()
                .inViewer (this)
                .styleClassName ("wordcounttimer")
                .buttonId (HeaderControlButtonIds.timer)
                .timer (this.wcTimer)
                .onMinutesComplete (() ->
                {

                    this.showNotificationPopup (getUILanguageStringProperty (timer,complete,time,popup,title),
                                                getUILanguageStringProperty (Arrays.asList (timer,complete,time,popup,text),
                                                                             Environment.formatNumber (this.wcTimer.getMinutes ())),
                                                30);

                })
                .onWordsComplete (() ->
                {

                    this.showNotificationPopup (getUILanguageStringProperty (timer,complete,LanguageStrings.words,popup,title),
                                                getUILanguageStringProperty (Arrays.asList (timer,complete,LanguageStrings.words,popup,text),
                                                                             Environment.formatNumber (this.wcTimer.getTotalWords ())),
                                                30);

                })
                .build ();

        }

        return super.getTitleHeaderControl (control);

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        return () ->
        {

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (this.getTitleHeaderControl (HeaderControl.contacts));
            controls.add (this.getTitleHeaderControl (HeaderControl.timer));
            controls.add (this.getTitleHeaderControl (HeaderControl.find));
            controls.add (this.getTitleHeaderControl (HeaderControl.fullscreen));
            controls.add (this.getTitleHeaderControl (HeaderControl.newproject));
            controls.add (this.getTitleHeaderControl (HeaderControl.openproject));
            controls.add (this.getTitleHeaderControl (HeaderControl.dowarmup));
            controls.add (this.getTitleHeaderControl (HeaderControl.statistics));
            controls.add (this.getTitleHeaderControl (HeaderControl.targets));
            controls.add (this.getTitleHeaderControl (HeaderControl.nightmode));
            controls.add (this.getTitleHeaderControl (HeaderControl.help));
            controls.add (this.getTitleHeaderControl (HeaderControl.close));
            controls.add (this.getTitleHeaderControl (HeaderControl.reportbug));

            return controls;

        };

    }

    public void viewStatistics (String chartType)
                         throws GeneralException
    {

        if (this.showPanel (StatisticsPanel.PANEL_ID))
        {

            if (chartType != null)
            {

                Panel p = this.getPanel (StatisticsPanel.PANEL_ID);

                StatisticsPanel sp = (StatisticsPanel) p.getContent ();

                sp.showChart (chartType);

            }

            return;

        }

        StatisticsPanel a = new StatisticsPanel (this,
                                                 null,
                                                 new PerChapterWordCountsChart (this),
                                                 new AllWordCountsChart (this),
                                                 new ReadabilityIndicesChart (this),
                                                 new SessionWordCountChart (this),
                                                 new SessionTimeChart (this));
        this.addPanel (a);
        this.showPanel (a.getPanelId ());

        if (chartType != null)
        {

            a.showChart (chartType);

        }

    }

    public void viewAchievements ()
                         throws GeneralException
    {

        if (this.showPanel (AchievementsPanel.PANEL_ID))
        {

            return;

        }

        AchievementsPanel a = new AchievementsPanel (this,
                                                     null);
        this.addPanel (a);
        this.showPanel (a.getPanelId ());

    }

    private void viewTargets ()
                       throws GeneralException
    {

        SideBar sb = this.getSideBarById (TargetsSideBar.SIDEBAR_ID);

        if (sb == null)
        {

            this.addSideBar (new TargetsSideBar (this));

        }

        this.showSideBar (TargetsSideBar.SIDEBAR_ID);

    }

    public void showTextProperties ()
    {

        SideBar sb = this.getSideBarById (TextPropertiesSideBar.SIDEBAR_ID);

        if (sb == null)
        {

            TextProperties props = this.getTextProperties ();

            this.addSideBar (new TextPropertiesSideBar (this,
                                                        props));

        }

        this.showSideBar (TextPropertiesSideBar.SIDEBAR_ID);

    }

    @Override
    public void showOptions (String section)
                      throws GeneralException
    {

        if (this.showPanel (OptionsPanel.PANEL_ID))
        {

            return;

        }

        OptionsPanel a = new OptionsPanel (this,
                                           null,
                                           Options.Section.Id.project,
     									   Options.Section.Id.look,
     									   Options.Section.Id.naming,
     									   Options.Section.Id.editing,
                                           Options.Section.Id.assets,
     									   Options.Section.Id.start,
     									   Options.Section.Id.editors,
     									   Options.Section.Id.itemsAndRules,
     									   Options.Section.Id.warmups,
     									   Options.Section.Id.achievements,
     									   Options.Section.Id.problems,
     									   Options.Section.Id.betas,
                                           Options.Section.Id.website);
        a.showSection (section);
        this.addPanel (a);
        this.showPanel (a.getPanelId ());

    }

    public void showChaptersOverReadabilityTarget ()
    {

        QuollPopup qp = this.getPopupById (ChaptersOverReadabilityTargetPopup.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        new ChaptersOverReadabilityTargetPopup (this).show ();

    }

    public void showChaptersOverWordCountTarget ()
    {

        QuollPopup qp = this.getPopupById (ChaptersOverWordCountTargetPopup.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        new ChaptersOverWordCountTargetPopup (this).show ();

    }

    public ObservableSet<Chapter> chaptersOverWordCountTarget ()
    {

        return this.chaptersOverWordCountTarget;

    }

    public void showWordCounts ()
    {

        SideBar sb = this.getSideBarById (WordCountsSideBar.SIDEBAR_ID);

        if (sb == null)
        {

            this.addSideBar (new WordCountsSideBar (this));

        }

        this.showSideBar (WordCountsSideBar.SIDEBAR_ID);

    }

    public void openPanelForId (String id)
                         throws GeneralException
    {

        if (id.equals (StatisticsPanel.PANEL_ID))
        {

            this.viewStatistics (null);
            return;

        }

        if (id.equals (OptionsPanel.PANEL_ID))
        {

            this.showOptions (null);

            return;

        }

        if (id.equals (AchievementsPanel.PANEL_ID))
        {

            this.viewAchievements ();
            return;

        }

    }

    public StringProperty selectedTextProperty ()
    {

        return this.selectedTextProp;

    }

    public String getSelectedText ()
    {

        if (this.selectedTextProp.isBound ())
        {

            return this.selectedTextProp.getValue ();

        }

        return null;

    }

    public ObjectProperty<Chapter> chapterCurrentlyEditedProperty ()
    {

        return this.chapterCurrentlyEditedProp;

    }

    public Chapter getChapterCurrentlyEdited ()
    {

        // TODO Do this properly...
        return this.chapterCurrentlyEditedProp.getValue ();

    }

    @Override
    public void enterFullScreen ()
    {

        super.enterFullScreen ();

        try
        {

            this.showInFullScreen (this.tabs.getSelectionModel ().getSelectedItem ());

        } catch (Exception e) {

            Environment.logError ("Unable to show in full screen",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (fullscreen,actionerror));
                                      //"Unable to enter full screen mode");

        }

    }

    @Override
    public void dispose ()
    {

        if (this.windowedContent != null)
        {

            this.windowedContent.dispose ();

        }

        super.dispose ();

    }

    @Override
    public ProjectFullScreenContent getFullScreenContent ()
    {

        if (this.fullScreenContent == null)
        {

            this.fullScreenContent = new ProjectFullScreenContent (this,
                                                                   this.getStyleClassName (),
                                                                   this.fullScreenPanelView);

            this.fullScreenContent.getHeader ().getControls ().setVisibleItems (UserProperties.fullScreenHeaderControlButtonIds ());

        }

        return this.fullScreenContent;

    }

    public Node getTitleHeaderControlByButtonId (String id)
    {

        WindowedContent wc = this.getWindowedContent ();

        QuollToolBar tb = wc.getHeader ().getToolbar ();

        return tb.getControlByButtonId (id);

    }

    public void addTitleHeaderControl (Node n,
                                       HPos side)
    {

        WindowedContent wc = this.getWindowedContent ();

        QuollToolBar tb = wc.getHeader ().getToolbar ();

        tb.addControl (n,
                       side);

    }

    public void addTitleHeaderControl (Node   n,
                                       String insertAt,
                                       HPos   side)
    {

        WindowedContent wc = this.getWindowedContent ();

        QuollToolBar tb = wc.getHeader ().getToolbar ();

        tb.addControl (n,
                       insertAt,
                       side);

    }

    @Override
    public WindowedContent getWindowedContent ()
    {

        if (this.windowedContent == null)
        {

            Supplier<Set<Node>> hcsupp = this.getTitleHeaderControlsSupplier ();

            Set<Node> headerCons = new LinkedHashSet<> ();

            if (hcsupp != null)
            {

                headerCons.addAll (hcsupp.get ());

            }

            this.windowedContent = new WindowedContent (this,
                                                        this.getStyleClassName (),
                                                        this.getStyleClassName (),
                                                        headerCons,
                                                        this.tabs);

        }

        return this.windowedContent;

    }

    public void showInFullScreenX (PanelContent qep)
                           throws GeneralException
    {

        if (qep == null)
        {

            throw new NullPointerException ("Panel must be provided.");

        }

    }
/*
TODO
    public void fullScreenClosed ()
    {

        this.getViewer ().show ();
        this.toFront ();
        this.setIconified (false);

        String sid = this.fsf.getCurrentSideBar ().getSideBarId ();

        this.fsf = null;

        // TODO?
        this.showSideBar (sid,
                          null);

    }
*/

    private void showInFullScreen (Tab t)
    {

        if ((!this.isInFullScreenMode ())
            ||
            (t == null)
           )
        {

            return;

        }

        Panel p = (Panel) t.getContent ();

        PanelContent pc = p.getContent ();

        boolean hasChanges = false;

        ChapterEditorPanelContent cep = null;

        if (pc instanceof ChapterEditorPanelContent)
        {

            cep = (ChapterEditorPanelContent) pc;

            hasChanges = cep.hasUnsavedChanges ();

        }

        if (pc instanceof ProjectChapterEditorPanelContent)
        {

            ProjectChapterEditorPanelContent edPanel = (ProjectChapterEditorPanelContent) pc;
            edPanel.bindTextPropertiesTo (this.getTextProperties ());
            // TODO this.setUseTypewriterScrolling (false);

        }

        // Put a placeholder panel in the tab.
        this.fsfplaceholder = new ShowingInFullScreenPanel (this,
                                                            p);
        t.setContent (this.fsfplaceholder.getPanel ());

        p.prefWidthProperty ().unbind ();
        p.prefHeightProperty ().unbind ();
        p.prefWidthProperty ().bind (this.fullScreenPanelView.widthProperty ());
        p.prefHeightProperty ().bind (this.fullScreenPanelView.heightProperty ());
        this.fullScreenPanelView.getChildren ().clear ();
        this.fullScreenPanelView.getChildren ().add (p);

        if (cep != null)
        {

            ChapterEditorPanelContent _cep = cep;
            boolean _hasChanges = hasChanges;

            UIUtils.forceRunLater (() ->
            {

                _cep.setHasUnsavedChanges (_hasChanges);
                _cep.requestLayout ();

            });

        }

    }

    private void restoreFromFullScreen (Tab t)
    {

        Panel p = (Panel) t.getContent ();

        if (p.getContent () instanceof ShowingInFullScreenPanel)
        {

            // Restore the panel.
            ShowingInFullScreenPanel fsfp = (ShowingInFullScreenPanel) p.getContent ();

            Panel fp = fsfp.getOriginalPanel ();

            fp.prefWidthProperty ().unbind ();
            fp.prefHeightProperty ().unbind ();

            boolean hasChanges = false;
            ChapterEditorPanelContent cep = null;

            if (fp.getContent () instanceof ChapterEditorPanelContent)
            {

                cep = (ChapterEditorPanelContent) fp.getContent ();

                hasChanges = cep.hasUnsavedChanges ();

            }

            t.setContent (fp);

            if (fp.getContent () instanceof ProjectChapterEditorPanelContent)
            {

                ProjectChapterEditorPanelContent edPanel = (ProjectChapterEditorPanelContent) fp.getContent ();

                edPanel.bindTextPropertiesTo (Environment.getProjectTextProperties ());

                this.setUseTypewriterScrolling (false);

            }

            ChapterEditorPanelContent _cep = cep;
            boolean _hasChanges = hasChanges;
            if (cep != null)
            {

                UIUtils.forceRunLater (() ->
                {

                    _cep.setHasUnsavedChanges (_hasChanges);
                    _cep.requestLayout ();

                });

            }

        }

    }

    @Override
    public void exitFullScreen ()
    {

        for (Tab t : this.tabs.getTabs ())
        {

            this.restoreFromFullScreen (t);

        }

        super.exitFullScreen ();

    }

    @Override
    public void showSideBar (String   id,
                             Runnable doAfterView)
    {

        super.showSideBar (id,
                           doAfterView);

    }

    public DataHandler getDataHandler (Class clazz)
                                throws GeneralException
    {

        if (this.dBMan == null)
        {

            return null;

        }

        return this.dBMan.getHandler (clazz);

    }

    public FindSideBar getFindSideBar ()
    {

        return this.findSideBar;

    }

    public void showFind ()
    {

        if (this.findSideBar == null)
        {

            this.findSideBar = new FindSideBar (this);

            this.addSideBar (this.findSideBar);

        }

        this.showSideBar (this.findSideBar.getSideBar ().getSideBarId ());

        UIUtils.forceRunLater (() ->
        {

            String sel = this.getSelectedText ();

            this.findSideBar.find (sel);

        });

    }

    public Map<Chapter, List<SentenceMatches>> getSentenceMatches (Set<String> s)
    {

        Map<Chapter, List<SentenceMatches>> data = new LinkedHashMap<> ();

        // Get all the books and chapters.
        List<Book> books = this.getProject ().getBooks ();

        for (int i = 0; i < books.size (); i++)
        {

            Book b = books.get (i);

            List<Chapter> chapters = b.getChapters ();

            for (Chapter c : chapters)
            {

                String t = this.getCurrentChapterText (c);

                if (t == null)
                {

                    continue;

                }

                List<SentenceMatches> snippets = TextUtilities.getSentenceMatches (s,
                                                                                   t);

                if ((snippets != null) &&
                    (snippets.size () > 0))
                {

                    data.put (c,
                              snippets);

                }

            }

        }

        return data;

    }

    public Map<Chapter, List<SentenceMatches>> getSentenceMatches (String  s)
    {

        Set<String> names = new HashSet<> ();
        names.add (s);

        return this.getSentenceMatches (names);

    }

    public void showAddNewTagPopup (NamedObject addTo,
                                    Node        showAt)
    {

        if (addTo == null)
        {

            throw new IllegalArgumentException ("Add to must be provided.");

        }

        Function<String, Set<String>> tags = v ->
        {

            Set<String> ret = new LinkedHashSet<> ();

            StringTokenizer t = new StringTokenizer (v.trim (),
                                                     ";,");

            while (t.hasMoreTokens ())
            {

                ret.add (t.nextToken ().trim ());

            }

            return ret;

        };

        QuollPopup.textEntryBuilder ()
            .withViewer (this)
            .styleClassName (StyleClassNames.ADD)
            .title (LanguageStrings.tags,actions,newtag,title)
            .description (getUILanguageStringProperty (Arrays.asList (LanguageStrings.tags,actions,newtag,text),
                                                       addTo.getName ()))
            .confirmButtonLabel (LanguageStrings.tags,actions,newtag,confirm)
            .validator (v ->
            {

                if ((v == null)
                    ||
                    (v.trim ().length () == 0)
                   )
                {

                    return getUILanguageStringProperty (LanguageStrings.tags,actions,newtag,errors,novalue);
                    //"Please enter a new tag.";

                }

                Set<String> ntags = tags.apply (v);

                if (ntags.size () == 0)
                {

                    return getUILanguageStringProperty (LanguageStrings.tags,actions,newtag,errors,novalue);
                    //"Please enter a new tag.";

                }

                return null;

            })
            .onConfirm (ev ->
            {

                TextField tf = (TextField) ev.getForm ().lookup ("#text");

                Set<String> ntags = tags.apply (tf.getText ());

                Set<Tag> newTags = new HashSet<> ();

                for (String s : ntags)
                {

                    Tag ot = null;

                    try
                    {

                        ot = this.project.getTagByName (s);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get tag for name: " +
                                              s,
                                              e);

                        continue;

                    }

                    if (ot != null)
                    {

                        continue;

                    }

                    Tag tag = new Tag ();
                    tag.setName (s);

                    try
                    {

                        this.saveObject (tag,
                                         true);

                        this.project.addNewTag (tag);

                        newTags.add (tag);

                    } catch (Exception e) {

                        Environment.logError ("Unable to add tag: " +
                                              tag,
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         getUILanguageStringProperty (LanguageStrings.tags,actions,newtag,actionerror));
                                                  //"Unable to add tag.");

                        ev.consume ();
                        return;

                    }

                    this.project.addTagToObject (tag,
                                                 addTo);

                }

                try
                {

                    this.saveObject (addTo,
                                     false);

                } catch (Exception e) {

                    Environment.logError ("Unable to update object: " +
                                          addTo,
                                          e);

                    ComponentUtils.showErrorMessage (this,
                                                     getUILanguageStringProperty (LanguageStrings.tags,actions,newtag,actionerror));
                                              //"Unable to add tags.");
                    ev.consume ();
                    return;

                }

                newTags.stream ()
                    .forEach (t ->
                    {

                        this.fireProjectEvent (ProjectEvent.Type.tag,
                                               ProjectEvent.Action._new,
                                               t);

                    });

            })
            .showAt (showAt,
                     Side.BOTTOM)
            .build ();

    }

    /**
     * Save a tag, this will either create or update it.
     *
     * @param tag The tag.
     * @throws GeneralException If the tag can't be saved.
     */
    public void saveTag (Tag tag)
                  throws GeneralException
    {

        ProjectEvent.Action ev = ProjectEvent.Action.changed;

        if (tag.getKey () == null)
        {

            ev = ProjectEvent.Action._new;

        }

        this.dBMan.saveObject (tag);

        if (ev.equals (ProjectEvent.Action._new))
        {

            this.project.addNewTag (tag);

        }

        // Tell all projects about it.
        this.fireProjectEvent (ProjectEvent.Type.tag,
                               ev,
                               tag);

    }

    /**
     * Delete a tag.
     *
     * @param tag The tag to delete.
     * @throws GeneralException If the delete goes wrong.
     */
    public void deleteTag (Tag tag)
                    throws GeneralException
    {

        this.dBMan.deleteObject (tag,
                                 true);

        this.project.removeTag (tag);

        // Tell all projects about it.
        this.fireProjectEvent (ProjectEvent.Type.tag,
                               ProjectEvent.Action.delete,
                               tag);

    }

    /**
     * Get a tag by name.
     *
     * @return The tag, if found.
     */
    public Tag getTagByName (String name)
    {

        return this.project.getTagByName (name);

    }

    public void deleteProjectFile (String fileName)
    {

        if (fileName == null)
        {

            return;

        }

        this.project.deleteFile (fileName);


    }

    public Path getProjectFile (String fileName)
    {

        if (fileName == null)
        {

            return null;

        }

        return this.project.getFile (fileName).toPath ();

    }

    public Path getProjectFilesDirectory ()
    {

        return this.project.getFilesDirectory ().toPath ();

    }

    public void saveToProjectFilesDirectory (Path   file,
                                             String fileName)
                                      throws GeneralException
    {

        this.project.saveToFilesDirectory (file,
                                           fileName);

    }

    public abstract boolean addToAchievementsManager ();

    public abstract void viewObject (DataObject d,
                                     Runnable   doAfterView);

    public abstract void handleOpenProject ()
                                     throws Exception;

    public abstract void handleNewProject ()
                                    throws Exception;

    public void deleteAllObjectsForType (UserConfigurableObjectType type)
                                  throws GeneralException
    {

        // Do nothing, let sub-classes override and provide the behavior.

    }

    @Override
    public void handleURLAction (String     v,
                                 MouseEvent ev)
    {

        try
        {

            if (v.equals (Constants.BACKUPS_HTML_PANEL_ACTION))
            {

                BackupsManager.showForProject (Environment.getProjectInfo (this.project),
                                               this);

                return;

            }

            if (v.equals ("textproperties"))
            {

                this.showTextProperties ();
                return;

            }

            if (v.equals ("find"))
            {

                this.showFind ();

                return;

            }

            if (v.equals ("spellcheckoff"))
            {

                this.setSpellCheckingEnabled (false);

                return;

            }

            if (v.equals ("spellcheckon"))
            {

                this.setSpellCheckingEnabled (true);

                return;

            }

            if (v.equals ("statistics"))
            {

                this.viewStatistics (null);

                return;

            }

            if (v.equals ("wordcounts"))
            {

                this.showWordCounts ();

                return;

            }

            if (v.equals ("wordcounthistory"))
            {

                this.viewStatistics (null);

                return;

            }

            if (v.equals ("projectoptions"))
            {

                this.showOptions ("project");

                return;

            }

            if (v.equals ("dictionarymanager"))
            {

                this.showDictionaryManager ();

                return;

            }

            if (v.equals ("enabletypewritersound"))
            {

				UserProperties.setPlaySoundOnKeyStroke (true);

				UserProperties.playKeyStrokeSound ();

                return;

            }

            if (v.equals ("fullscreen"))
            {

                try
                {

                    this.enterFullScreen ();

                } catch (Exception e) {

                    Environment.logError ("Unable to show in full screen mode",
                                          e);

                }

                return;

            }

            if (v.equals ("projectsidebar"))
            {

                this.showMainSideBar ();

                return;

            }

            super.handleURLAction (v,
                                   ev);

        } catch (Exception e) {

            Environment.logError ("Unable to perform action: " +
                                  v,
                                  e);

        }

    }

    public ChapterFindResultsBox findTextInChapters (String t)
    {

        // Get the snippets.
        Map<Chapter, List<SentenceMatches>> snippets = this.getSentenceMatches (t);

        if (snippets.size () > 0)
        {

            return new ChapterFindResultsBox (snippets,
                                              this);

        }

        return null;

    }

    protected void closeAllTabs ()
    {

        this.saveTabsState ();

        // Regardless of whether it should be saved call the close method
        // for the panel to allow it to close itself nicely.
        // Close after state so we can keep track of what is open.

        // Duplicate the values so we don't get a modification error for this.panels.
        Set<Panel> qpps = new LinkedHashSet<> (this.panels.values ());

        for (Panel qp : qpps)
        {

            this.closePanel (qp,
                             null);

        }

    }

    public boolean isEditing (Chapter c)
    {

        return this.getEditorForChapter (c) != null;

    }

    public Set<Chapter> snapshotChapters (Set<Chapter>   chapters,
                                          ProjectVersion pv)
                                   throws Exception
    {

        ChapterDataHandler ch = (ChapterDataHandler) this.dBMan.getHandler (Chapter.class);

        return ch.snapshot (chapters,
                            pv);

    }

    public void showDeleteUserConfigurableType (UserConfigurableObjectType type)
    {

        String pid = "deleteall" + type.getObjectReference ().asString ();

        QuollPopup.yesConfirmTextEntryBuilder ()
            .withViewer (this)
            .title (getUILanguageStringProperty (Arrays.asList (assets,deleteall,title),
                                                 type.objectTypeNamePluralProperty ()))
            .popupId (pid)
            .styleClassName (StyleClassNames.DELETE)
            .description (getUILanguageStringProperty (Arrays.asList (assets,deleteall,text),
                                                       type.objectTypeNamePluralProperty ()))
            .confirmButtonLabel (assets,deleteall,buttons,confirm)
            .cancelButtonLabel (assets,deleteall,buttons,cancel)
            .onConfirm (eev ->
            {

                try
                {

                    this.deleteAllObjectsForType (type);

                } catch (Exception e) {

                    Environment.logError ("Unable to remove all: " +
                                          type,
                                          e);

                    ComponentUtils.showErrorMessage (this,
                                                     getUILanguageStringProperty (assets,deleteall,actionerror));
                                              //String.format ("Unable to remove all %1$s.",
                                                //             type.getObjectTypeNamePlural ()));

                    return;

                }

                try
                {

                    this.project.removeUserConfigurableObjectType (type);

                    this.deleteObject (type,
                                       true);

                } catch (Exception e) {

                    Environment.logError ("Unable to remove user object type: " +
                                          type,
                                          e);

                    ComponentUtils.showErrorMessage (this,
                                                     getUILanguageStringProperty (assets,deleteall,actionerror));
                                              //"Unable to remove object.");

                    return;

                }

                this.getPopupById (pid).close ();

            })
            .build ();

    }

    public void showImportUserConfigurableTypes ()
    {

        String pid = ImportUserConfigurableObjectTypes.POPUP_ID;

        if (this.getPopupById (pid) != null)
        {

            return;

        }

        new ImportUserConfigurableObjectTypes (this).show ();

    }

    public void showAddNewUserConfigurableType ()
    {

        String pid = AddEditUserConfigurableObjectType.POPUP_ID;

        if (this.getPopupById (pid) != null)
        {

            return;

        }

        new AddEditUserConfigurableObjectType (this).show ();

    }

    public void showEditUserConfigurableType (UserConfigurableObjectType t)
    {

        String pid = AddEditUserConfigurableObjectType.getPopupIdForType (t);

        if (this.getPopupById (pid) != null)
        {

            return;

        }

        new AddEditUserConfigurableObjectType (t,
                                               this).show ();

    }

}
