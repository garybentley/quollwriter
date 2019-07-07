package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.nio.file.*;
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

public abstract class AbstractProjectViewer extends AbstractViewer implements PropertyChangedListener
{

    private static final Map<String, Class> viewerTypes = new HashMap<> ();

    public interface CommandId extends AbstractViewer.CommandId
    {

        String textproperties = "textproperties";
        String print = "print";
        String chaptersoverwordcounttarget = "chaptersoverwordcounttarget";
        String chaptersoverreadabilitytarget = "chaptersoverreadabilitytarget";
        String openproject = "openproject";
        String closeproject = "closeproject";
        String closepanel = "closepanel";
        String showwordcounts = "showwordcounts";

    }

    protected Project project = null;
    private BooleanProperty spellCheckingEnabledProp = null;
    private StringProperty projectSpellCheckLanguageProp = null;
    private BooleanProperty inFullScreenModeProp = null;
    private BooleanProperty distractionFreeModeProp = null;
    private BooleanProperty typeWriterScrollingEnabledProp = null;
    private StringProperty titleProp = null;

    private ChapterCounts         startWordCounts = new ChapterCounts ();
    private ChapterCounts allChapterCounts = new ChapterCounts ();
    private Map<Chapter, ChapterCounts> chapterCounts = new WeakHashMap<> ();
    private Map<Chapter, ReadabilityIndices> noEditorReadabilityIndices = new WeakHashMap<> ();

    private TabPane tabs = null;
    private Map<String, Panel> panels = new HashMap<> ();
    private ObjectProperty<Panel> currentPanelProp = null;
    private VBox toolbarWrapper = null;

    private ScheduledFuture autoSaveTask = null;
    private ScheduledFuture chapterCountsUpdater = null;

    private ProjectDictionaryProvider   projDict = null;
    private SynonymProvider       synProv = null;
    protected ObjectManager       dBMan = null;

    private Date                  sessionStart = new Date ();

    private TargetsData targets = null;
    private Map<Chapter, Date> chapterWordCountTargetWarned = new HashMap<> ();
    private ObservableSet<Chapter> chaptersOverWordCountTarget = FXCollections.observableSet (new HashSet<> ());
    private SetProperty<Chapter> chaptersOverWordCountTargetProp = null;
    private ObservableSet<Chapter> chaptersOverReadabilityTarget = FXCollections.observableSet (new HashSet<> ());
    private SetProperty<Chapter> chaptersOverReadabilityTargetProp = null;

    private ObjectProperty<Chapter> chapterCurrentlyEditedProp = null;
    private StringProperty selectedTextProp = null;
    private int sessionWordCount = 0;
    private IntegerProperty sessionWordCountProp = null;

    public AbstractProjectViewer ()
    {

        final AbstractProjectViewer _this = this;

        this.titleProp = new SimpleStringProperty ();
        this.currentPanelProp = new SimpleObjectProperty<> ();
        this.selectedTextProp = new SimpleStringProperty ();
        this.sessionWordCountProp = new SimpleIntegerProperty (0);

        this.chapterCurrentlyEditedProp = new SimpleObjectProperty<> ();

        this.currentPanelProp.addListener ((pr, oldv, newv) ->
        {

            if (newv.getContent () instanceof ChapterEditorPanelContent)
            {

                ChapterEditorPanelContent pc = (ChapterEditorPanelContent) newv.getContent ();

                // Need to cast here because otherwise we have to get into generics...
                this.chapterCurrentlyEditedProp.setValue ((Chapter) pc.getObject ());

            } else {

                this.chapterCurrentlyEditedProp.setValue (null);

            }

        });

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

        this.inFullScreenModeProp = new SimpleBooleanProperty (false);
        this.typeWriterScrollingEnabledProp = new SimpleBooleanProperty (false);
        this.distractionFreeModeProp = new SimpleBooleanProperty (false);

        this.projectSpellCheckLanguageProp = new SimpleStringProperty (null);

        this.projectSpellCheckLanguageProp.addListener ((pr, oldv, newv) ->
        {

            // This forces changes to be propagated.

        });

        this.tabs = new TabPane ();
        this.setContent (this.tabs);
        this.tabs.setTabClosingPolicy (TabPane.TabClosingPolicy.ALL_TABS);
        this.tabs.setSide (UserProperties.tabsLocationProperty ().getValue ().equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);
        this.tabs.setTabDragPolicy (TabPane.TabDragPolicy.REORDER);
        this.tabs.getTabs ().addListener ((ListChangeListener<Tab>) ev ->
        {

            if (this.tabs.getSelectionModel ().getSelectedIndex () < 0)
            {

                return;

            }

            Tab t = _this.tabs.getTabs ().get (_this.tabs.selectionModelProperty ().getValue ().getSelectedIndex ());

            Panel qp = (Panel) t.getContent ();

            this.currentPanelProp.setValue (qp);

            PanelContent pc = qp.getContent ();

            this.selectedTextProp.unbind ();

            if (pc instanceof ChapterEditorPanelContent)
            {

                ChapterEditorPanelContent cec = (ChapterEditorPanelContent) pc;

                this.selectedTextProp.bind (cec.selectedTextProperty ());

            }

            qp.fireEvent (new Panel.PanelEvent (qp,
                                                Panel.PanelEvent.SHOW_EVENT));

        });

        this.tabs.selectionModelProperty ().getValue ().selectedIndexProperty ().addListener ((ind, oldi, newi) ->
        {

            if (newi.intValue () < 0)
            {

                return;

            }

            Tab t = _this.tabs.getTabs ().get (_this.tabs.selectionModelProperty ().getValue ().getSelectedIndex ());

            Panel qp = (Panel) t.getContent ();

            this.currentPanelProp.setValue (qp);

            qp.fireEvent (new Panel.PanelEvent (qp,
                                                Panel.PanelEvent.SHOW_EVENT));

        });

        UIUtils.doOnKeyReleased (this.tabs,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.removeCurrentPanel (null);

                                 });

        this.toolbarWrapper = new VBox ();

        UserProperties.tabsLocationProperty ().addListener ((p, oldv, newv) ->
        {

            // TODO Update the tabs location.
            this.tabs.setSide (newv.equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);

        });

        this.chaptersOverWordCountTargetProp = new SimpleSetProperty<> (this.chaptersOverWordCountTarget);
        this.chaptersOverReadabilityTargetProp = new SimpleSetProperty<> (this.chaptersOverReadabilityTarget);

        this.initActionMappings ();

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

            viewerClass.getDeclaredConstructor ().newInstance ();

        } catch (Exception e) {

            throw new IllegalArgumentException ("Viewer class: " + viewerClass.getName () + ", for project type: " + projType + " does not have a zero arg constructor or cannot be created",
                                                e);

        }

        AbstractProjectViewer.viewerTypes.put (projType,
                                               viewerClass);

    }

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

        java.util.List<String> names = new ArrayList ();

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

        List<Chapter> chapters = b.getChapters ();

		for (Chapter c : chapters)
		{

			this.initChapterCounts (c);

		}

        b.getChapters ().addListener ((ListChangeListener<Chapter>) ev ->
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

                }

                return;

            }

        });

        // We have to do something special here for the initial counts.
        // Each chapter has its counts inited in a separate thread.
        this.schedule (() ->
        {

            this.startWordCounts = new ChapterCounts ();

            for (Chapter c : chapters)
    		{

                String t = c.getText ().getText ();

                ChapterCounts ncc = new ChapterCounts (t);

                this.startWordCounts.add (ncc);

    		}

        },
        1,
        -1);

	}

    public void printChapter (Chapter c)
    {

        // TODO
        // See javafx.print javafx.print.PrinterJob

    }

    private void initActionMappings ()
    {

        final AbstractProjectViewer _this = this;

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

            Environment.showAllProjectsViewer ();

        },
        CommandId.openproject);

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
        this.dBMan.init (new java.io.File (p.getProjectDirectory ().getPath (), Constants.PROJECT_DB_FILE_NAME_PREFIX),
                         username,
                         password,
                         filePassword,
                         Environment.getSchemaVersion ());

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

        this.initProperties ();

        Environment.incrStartupProgress ();

		this.project.setFilePassword (filePassword);
        this.project.setProjectDirectory (p.getProjectDirectory ());
		this.project.setBackupDirectory (p.getBackupDirPath ().toFile ());
        // TODO getBackupDirectory ());
        //this.proj.setFilePassword (filePassword);
        this.project.setEncrypted (p.isEncrypted ());
        this.project.setNoCredentials (p.isNoCredentials ());

        this.project.addPropertyChangedListener (this);

        Environment.incrStartupProgress ();

        this.handleOpenProject ();

        this.init (null);

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

            java.awt.Font ft = new java.awt.Font (f,
                                java.awt.Font.PLAIN,
                                12);

            if (!ft.getFamily ().equalsIgnoreCase (f))
            {

                ComponentUtils.showMessage (this,
                                            getUILanguageStringProperty (Arrays.asList (fontunavailable,text),
                                                                         f));
                //"The font <b>" + f + "</b> selected for use in {chapters} is not available on this computer.<br /><br />To select a new font, switch to a chapter tab then <a href='action:textproperties'>click here to change the text properties</a>.");

            }

        }

        //this.handleWhatsNew ();

        //this.handleShowTips ();

        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (ProjectEvent.Type.projectobject,
                               ProjectEvent.Action.open,
                               this.project);

        // Register ourselves with the environment.
        try
        {

            Environment.addOpenedProject (this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add opened project: " +
                                  this.project,
                                  e);

            // TODO Show an error

            return;

        }

		UIUtils.runLater (onOpen);

		// Check to see if any chapters have overrun the target.
		UIUtils.runLater (() ->
		{

			try
			{

				int wc = _this.getProjectTargets ().getMaxChapterCount ();

				Set<Chapter> wchaps = _this.getChaptersOverWordTarget ();

				int s = wchaps.size ();

				if ((wc > 0)
					&&
					(s > 0)
				   )
				{

					for (Chapter c : wchaps)
					{

						_this.chapterWordCountTargetWarned.put (c,
																new Date ());

					}

                    String t = LanguageStrings.single;

                    if (s > 1)
                    {

                        t = LanguageStrings.multiple;

                    }

                    BasicHtmlTextFlow m = BasicHtmlTextFlow.builder ()
                        .text (getUILanguageStringProperty (Arrays.asList (LanguageStrings.targets, chaptersoverwcmaximum,t),
                                                            wchaps.size (),
                                                            wc))
                        .build ();

					final Notification n = _this.addNotification (m,
																  StyleClassNames.WORDCOUNTS,
																  90);

                      m.setOnMouseClicked (ev ->
                      {

                          n.removeNotification ();

                          Set<Chapter> chaps = this.getChaptersOverWordTarget ();

                          if (chaps.size () == 0)
                          {

                              return;

                          }

                          this.showChaptersOverWordCountTarget ();

                      });

				}

				Set<Chapter> rchaps = _this.getChaptersOverReadabilityTarget ();

				s = rchaps.size ();

				if (s > 0)
				{

                    String t = LanguageStrings.single;

                    if (s > 1)
                    {

                        t = LanguageStrings.multiple;

                    }

                    BasicHtmlTextFlow m = BasicHtmlTextFlow.builder ()
                        .text (getUILanguageStringProperty (Arrays.asList (LanguageStrings.targets, chaptersoverreadabilitymaximum,t),
                                                            rchaps.size ()))
                        .build ();

					final Notification n = _this.addNotification (m,
																  StyleClassNames.READABILITY,
																  90);

                      m.setOnMouseClicked (ev ->
                      {

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

        this.scheduleTargetReachedCheck ();

    }

    private void scheduleTargetReachedCheck ()
    {

        final AbstractProjectViewer _this = this;

        this.schedule (() ->
		{

			try
			{

				int wc = _this.getProjectTargets ().getMaxChapterCount ();

				if ((wc > 0)
					&&
					(_this.getProjectTargets ().isShowMessageWhenMaxChapterCountExceeded ())
				   )
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

					if (over.size () > 0)
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

                            Text t = new Text ();
                            t.textProperty ().bind (getUILanguageStringProperty (messIds,
                                                                                 reps));

                            Set<Button> buttons = new LinkedHashSet<> ();
                            Button showDetail = QuollButton.builder ()
                                .label (LanguageStrings.buttons,showdetail)
                                .buttonType (ButtonBar.ButtonData.OK_DONE)
                                .build ();

                            QuollPopup qp = ComponentUtils.showMessage (_this,
                                                                        StyleClassNames.WORDCOUNTS,
                                                                        getUILanguageStringProperty (LanguageStrings.targets, LanguageStrings.popup,title),
                                                                        t,
                                                                        buttons);

                            showDetail.setOnAction (ev ->
                            {

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
                            Project p,
                            String  filePassword)
                     throws Exception
    {

        this.newProject (saveDir,
                         p,
                         filePassword,
                         null);

    }

    public void newProject (Path     saveDir,
                            Project  p,
                            String   filePassword,
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
                                                p,
                                                filePassword);

        this.project = this.dBMan.getProject ();
		this.project.setFilePassword (filePassword);

        this.initProperties ();

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

        this.handleNewProject ();

        this.init (null);

        this.setIgnoreProjectEvents (false);

        // TODO Change to pass Project.
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
                            boolean     doInTransaction)
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
                               null);

        // TODO Needed? this.refreshObjectPanels (otherObjects);

    }

    public void setLinks (NamedObject o)
    {

        try
        {

            this.dBMan.getLinks (o);

        } catch (Exception e)
        {

            Environment.logError ("Unable to set links for: " +
                                  o,
                                  e);

        }

    }


/*
TODO Needed?
    public void refreshObjectPanels (final Set<NamedObject> objs)
    {

        final AbstractProjectViewer _this = this;

        if ((objs != null) &&
            (objs.size () > 0))
        {

            UIUtils.runLater (() ->
            {

				// For each one determine if it is visible.
				for (NamedObject n : objs)
				{

					NamedObjectPanelContent qp = _this.getPanelForObject (n);

					if (qp != null)
					{

						qp.refresh ();

					}

				}

            });

        }

    }
*/
    public ChapterEditorPanelContent getEditorForChapter (Chapter c)
    {

        NamedObjectPanelContent p = this.getPanelForObject (c);

        if (p instanceof ChapterEditorPanelContent)
        {

            return (ChapterEditorPanelContent) p;

        }

        return null;

    }

    public NamedObjectPanelContent getPanelForObject (NamedObject n)
    {

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
				(pqp.getObject () == n)
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

        Panel qp = (Panel) this.tabs.getSelectionModel ().getSelectedItem ().getContent ();

        return qp;

    }

    public Tab addPanel (final NamedObjectPanelContent qp)
                  throws GeneralException
    {

        Tab tab = this.addPanel (qp.getPanel ());

        // TODO Remove

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

        qp.unsavedChangesProperty ().addListener ((val, oldv, newv) ->
        {

            if (newv)
            {

                tab.getStyleClass ().add (StyleClassNames.HASCHANGES);

            } else {

                tab.getStyleClass ().remove (StyleClassNames.HASCHANGES);

            }

        });

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

        final String panelId = qp.getPanelId ();

        final String s = this.project.getProperty (panelId + "-state");

        State state = null;

        if (s != null)
        {

            state = new State (s);

        }

        qp.getContent ().init (state);

        Tab tab = new Tab ();
        tab.textProperty ().bind (qp.titleProperty ());
        tab.getStyleClass ().add (qp.getStyleClassName ());
        tab.setContent (qp);

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

        this.tabs.getTabs ().add (0,
                                  tab);

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
            TODO
            if ((_this.fsf != null)
                &&
                (p == _this.fsf.getPanel ().getChild ())
               )
            {

                // In full screen, restore first.
                _this.restoreFromFullScreen (_this.fsf.getPanel ());

                // Add a blank instead.
                _this.fsf.showBlankPanel ();

            }
            */

            ComponentUtils.createQuestionPopup (getUILanguageStringProperty (closepanel,confirmpopup,title),
                                                //"Save before closing?",
                                                StyleClassNames.SAVE,
                                                getUILanguageStringProperty (Arrays.asList (closepanel,confirmpopup,text),
                                                                             p.getPanel ().titleProperty ().getValue ()),
                                                        //"The %s has unsaved changes.  Save before closing?",
                                                        //Environment.getObjectTypeName (p.getForObject ())),
                                                getUILanguageStringProperty (closepanel,confirmpopup,buttons,save),
                                                //"Yes, save the changes",
                                                getUILanguageStringProperty (closepanel,confirmpopup,buttons,discard),
                                                //"No, discard the changes",
                                                fev ->
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

                                                },
                                                _this);

            return;

        }

        this.closePanel (p.getPanel (),
                         onClose);

    }

    public void closePanel (Panel qp,
                            Runnable   onClose)
    {

        // Get the state.
        Map m = new LinkedHashMap ();

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

        qp.fireEvent (new Panel.PanelEvent (qp,
                                            Panel.PanelEvent.CLOSE_EVENT));

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

		this.panels.remove (panelId);

        if (onRemove != null)
        {

            UIUtils.runLater (onRemove);

        }

	}

    private void updateToolbarForPanel (Panel qp)
    {

        this.toolbarWrapper.getChildren ().removeAll ();

        if (qp != null)
        {

            ToolBar tb = new ToolBar ();
            tb.getStyleClass ().add (StyleClassNames.TOOLBAR);
            tb.getStyleClass ().add (qp.getStyleClassName ());

            Set<Node> items = qp.getToolBarItems (this.inFullScreenModeProp.getValue ());

            if (items != null)
            {

                tb.getItems ().addAll (items);

            }

            this.toolbarWrapper.getChildren ().add (tb);

            this.toolbarWrapper.setVisible (tb.getItems ().size () > 0);

        } else
        {

            this.toolbarWrapper.setVisible (false);

        }

    }

    public Panel getPanel (String panelId)
    {

        return this.panels.get (panelId);

    }

    protected void setPanelVisible (PanelContent pc)
    {

        this.setPanelVisible (pc.getPanel ());

    }

    protected void setPanelVisible (Panel qp)
    {

        this.updateToolbarForPanel (qp);

        if (!this.inFullScreenModeProp.getValue ())
        {

            int tInd = this.getTabIndexForPanelId (qp.getPanelId ());

            this.tabs.selectionModelProperty ().getValue ().select (tInd);

        } else {

            try
            {

                // TODO this.showInFullScreen (qp);

            } catch (Exception e) {

                Environment.logError ("Unable to show panel: " + qp +
                                      " in full screen",
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 getUILanguageStringProperty (fullscreen,showpanelactionerror));
                                          //"Unable to show in full screen");

            }

        }

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

        for (ChapterCounts cc : this.chapterCounts.values ())
		{

            wc += cc.getWordCount ();
            pc += cc.getStandardPageCount ();
            sc += cc.getSentenceCount ();

		}

        this.allChapterCounts.setWordCount (wc);
        this.allChapterCounts.setSentenceCount (sc);
        this.allChapterCounts.setStandardPageCount (pc);

        this.sessionWordCount = this.allChapterCounts.getWordCount () - this.startWordCounts.getWordCount ();

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

        if (this.project == null)
        {

            return null;

        }

        Book b = this.project.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        ReadabilityIndices ri = new ReadabilityIndices ();

        for (Chapter c : chapters)
        {

            ri.add (this.getReadabilityIndices (c));

        }

        return ri;

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

        ChapterEditorPanelContent qep = this.getEditorForChapter (c);

        ReadabilityIndices ri = null;

        if (qep != null)
        {

            ri = qep.getReadabilityIndices ();

            if (ri != null)
            {

                this.noEditorReadabilityIndices.remove (c);

                return ri;

            }

        }

        ri = this.noEditorReadabilityIndices.get (c);

        if (ri == null)
        {

            // Cache the value.
            ri = this.getReadabilityIndices (c.getChapterText ());

            this.noEditorReadabilityIndices.put (c,
                                                 ri);

        }

        return ri;

    }

    public void initChapterCounts (final Chapter c)
	{

        ChapterCounts cc = new ChapterCounts ();

        cc.wordCountProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateAllChapterCounts ();

        });

        cc.sentenceCountProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateAllChapterCounts ();

        });

        cc.standardPageCountProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateAllChapterCounts ();

        });

        this.chapterCounts.put (c,
                                cc);

        this.schedule (() ->
        {

            try
            {

                final String t = this.getCurrentChapterText (c);

                final ChapterCounts ncc = new ChapterCounts (t);

                cc.setWordCount (ncc.getWordCount ());

                cc.setSentenceCount (ncc.getSentenceCount ());
                cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                           t));

                this.updateAllChapterCounts ();

            } catch (Exception e) {

                Environment.logError ("Unable to get a4 page count for chapter: " +
                                      c,
                                      e);

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

/*
        final AbstractProjectViewer _this = this;

        final String t = this.getCurrentChapterText (c);

        final ChapterCounts cc = new ChapterCounts (t);

        ChapterCounts _cc = _this.getChapterCounts (c);

        _cc.setWordCount (cc.getWordCount ());
        _cc.setSentenceCount (cc.getSentenceCount ());

        _this.updateAllChapterCounts ();

        // Don't try and calculate the a4 page count before the window is ready otherwise
        // strange errors result.  The initChapterCounts and scheduleA4PageCountUpdate will handle the initial counts.
        this.unschedule (this.chapterCountsUpdater);

        Runnable r = new Runnable ()
        {

            @Override
            public void run ()
            {

                try
                {

                    _cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (c,
                                                                                t));

                    _this.updateAllChapterCounts ();

                } catch (Exception e) {

                    Environment.logError ("Unable to get a4 page count for chapter: " +
                                          c,
                                          e);

                }

            }

        };

        this.chapterCountsUpdater = _this.schedule (r,
                                                    // Start in 2 seconds
                                                    0 * Constants.SEC_IN_MILLIS,
                                                    // Do it once.
                                                    0);
*/
	}

	public int getChapterA4PageCount (Chapter c)
	{

		return UIUtils.getA4PageCountForChapter (c,
												 this.getCurrentChapterText (c));

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

        if (state != null)
        {

            sb.init (new State (state));

        }

    }

    public BooleanProperty distractionFreeModeProperty ()
    {

        return this.distractionFreeModeProp;

    }

    public boolean isDistractionFreeModeEnabled ()
    {

        /*
        TODO
        if (this.fsf != null)
        {

            return this.fsf.isDistractionFreeModeEnabled ();

        }
        */

        return false;

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

				if (proj.getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME))
				{

					// Get the last backup.
					java.io.File last = dBMan.getLastBackupFile (proj);

					long lastDate = (last != null ? last.lastModified () : proj.getDateCreated ().getTime ());

					if ((System.currentTimeMillis () - lastDate) > Utils.getTimeAsMillis (proj.getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME)))
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
                         throws GeneralException
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

                this.setPanelVisible (qp);

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
                                     throws GeneralException
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

        AtomicBoolean b = new AtomicBoolean (false);

        this.tabs.getTabs ().stream ()
            .forEach (t ->
            {

                Panel p = (Panel) t.getContent ();

                if (p.getPanelId ().equals (pid))
                {

                    this.tabs.getSelectionModel ().select (t);
                    b.set (true);

                }

            });

        return b.get ();

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

        this.projectSpellCheckLanguageProp.setValue (c);

    }

    private void initDictionaryProvider ()
    {

        final String lang = this.getProjectSpellCheckLanguage ();

        try
        {

            if (!DictionaryProvider.isLanguageInstalled (lang))
            {

                final AbstractProjectViewer _this = this;

                // Turn off spell checking until the download is complete.
                this.setProjectSpellCheckLanguage (null);

                // Download them.
                DictionaryProvider.downloadDictionaryFiles (this.getProjectSpellCheckLanguage (),
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
                                                                                                //"Unable to set spell check language to <b>" +
                                                                                                //lang +
                                                                                                //"</b>.<br /><br />Please contact Quoll Writer support for assistance.");

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
/*
TODO REmove
    public void saveDefaultProjectProperty (String name,
                                            String value)
    {

        try
        {

            Environment.saveDefaultProperty (Project.OBJECT_TYPE,
                                              name,
                                              value);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save default " + Project.OBJECT_TYPE + " properties",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                      getUILanguageStringProperty (options,savepropertyerror));
                                      //"Unable to save default project properties");

        }


    }

    public void saveDefaultProjectProperty (String  name,
                                            Boolean value)
    {

        try
        {

            Environment.saveDefaultProperty (Project.OBJECT_TYPE,
                                              name,
                                              value);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save default " + Project.OBJECT_TYPE + " properties",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                      getUILanguageStringProperty (options,savepropertyerror));
                                      //"Unable to save default project properties");

        }


    }
*/
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

        return Environment.getProjectTextProperties ();

        // TODO return (this.fsf != null ? Environment.getFullScreenTextProperties () : Environment.getProjectTextProperties ());

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

            c.getChildren ().add (BasicHtmlTextFlow.builder ()
                .text (getUILanguageStringProperty (closeproject,confirmpopup,prefix))
                .withViewer (this)
                .build ());

            c.getChildren ().addAll (this.panels.values ().stream ()
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

                    return QuollHyperlink.builder ()
                        .label (p.titleProperty ())
                        .onAction (ev ->
                        {

                            this.viewObject (np.getObject ());

                        })
                        .build ();

                })
                .collect (Collectors.toList ()));

            c.getChildren ().add (BasicHtmlTextFlow.builder ()
                .text (getUILanguageStringProperty (closeproject,confirmpopup,suffix))
                .withViewer (this)
                .build ());

    /*
            for (Panel p : this.panels.values ())
            {

    			if (p.getContent () instanceof NamedObjectPanelContent)
    			{

    				NamedObjectPanelContent pqp = (NamedObjectPanelContent) p;

    				if (pqp.hasUnsavedChanges ())
    				{

    					hasChanges = true;

    					if (pqp.getForObject () instanceof NamedObject)
    					{

    						b.append ("<li>" + pqp.getTitle () + "</li>");

    					}

    				}

    			}

            }
    */
            if (hasChanges.get ())
            {

                final AbstractProjectViewer _this = this;

                Set<Button> buttons = new LinkedHashSet<> ();

                buttons.add (QuollButton.builder ()
                    .label (getUILanguageStringProperty (LanguageStrings.buttons,savechanges))
                    .onAction (ev ->
                    {

                        this.closeInternal (true,
                                            afterClose);

                    })
                    .build ());

                buttons.add (QuollButton.builder ()
                    .label (getUILanguageStringProperty (LanguageStrings.buttons,discardchanges))
                    .onAction (ev ->
                    {

                        this.closeInternal (false,
                                            afterClose);

                    })
                    .build ());

                ComponentUtils.createQuestionPopup (getUILanguageStringProperty (closeproject,confirmpopup,title),
                                                    StyleClassNames.PROJECT,
                                                    c,
                                                    buttons,
                                                    null,
                                                    null,
                                                    null,
                                                    this);

                return;

            }

        }

        this.closeInternal (false,
                            afterClose);

    }

    private boolean closeInternal (boolean  saveUnsavedChanges,
                                   Runnable afterClose)
    {

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

						if (showError)
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

        for (Panel qp : new LinkedHashSet<> (this.panels.values ()))
        {

            // We close, rather than remove.  The close calls remove.
            this.closePanel (qp,
                             null);

        }

        this.project.setLastEdited (new Date ());

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

                this.project.setProperty ("sidebarState-" + sb.getSideBarId (),
                                          sb.getState ().asString ());

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

        this.dBMan.createActionLogEntry (n,
                                         m,
                                         null,
                                         null);

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

        Set<Word> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            DictionaryProvider2 dp = this.getDictionaryProvider ();

            if (dp != null)
            {

                SpellChecker sc = null; // TODO dp.getSpellChecker ();

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

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (this.project == null)
        {

            throw new IllegalStateException ("No project set.");

        }

        this.initChapterCounts ();
        this.initDictionaryProvider ();
        this.targets = new TargetsData (this.project.getProperties ());
        this.startAutoBackups ();

        this.titleProp.bind (this.project.nameProperty ());

        this.restoreSideBars ();
        this.restoreTabs ();

        this.scheduleA4PageCountUpdate ();

        // This is done here because the achievements manager needs the project.
        try
        {

            Environment.getAchievementsManager ().addProjectViewer (this);

        } catch (Exception e) {

            throw new GeneralException ("Unable to add viewer to achievements manager.",
                                        e);

        }

        super.init (s);

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

        State state = null;

        String id = sb.getId ();

        if (id != null)
        {

            state = new State (this.project.getProperty ("sidebarState-" + id));

        }

        sb.init (state);

        // TODO Make this nicer.
        if (sb.getContent () instanceof SideBarContent)
        {

            ((SideBarContent) sb.getContent ()).init (state);

        }

        super.setMainSideBar (sb);

    }

    @Override
    public void addSideBar (SideBarContent sb)
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

        State state = null;

        String id = sb.getId ();

        if (id != null)
        {

            state = new State (this.project.getProperty ("sidebarState-" + id));

        }

        sb.init (state);

        super.addSideBar (sb);

    }

    public void exitFullScreen ()
    {

        try
        {
/*
            if (this.fsf != null)
            {

                this.fsf.close ();

                this.tabs.setVisible (true);

                this.fullScreenOverlay.setVisible (false);

            }

            this.setUILayout (this.layout);
*/
        } catch (Exception e) {

            Environment.logError ("Unable to exit full screen",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (fullscreen,actions,exit,actionerror));

        } finally {

            this.setVisible (true);

        }

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        return () ->
        {

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (this.getTitleHeaderControl (HeaderControl.contacts));
            controls.add (this.getTitleHeaderControl (HeaderControl.find));
            controls.add (this.getTitleHeaderControl (HeaderControl.fullscreen));

            return controls;

        };

    }

    public void viewStatistics (String chartType)
                         throws GeneralException
    {

        if (this.showPanel (StatisticsPanel.PANEL_ID))
        {

            return;

        }

        StatisticsPanel a = new StatisticsPanel (this,
                                                 null,
                                                 new SessionWordCountChart (this));
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

    public SetProperty<Chapter> chaptersOverWordCountTargetProperty ()
    {

        return this.chaptersOverWordCountTargetProp;

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

    public abstract void viewObject (DataObject d,
                                     Runnable   doAfterView);

    public abstract void handleOpenProject ()
                                     throws Exception;

    public abstract void handleNewProject ()
                                    throws Exception;

}
