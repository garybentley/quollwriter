package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.tips.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.achievements.rules.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractViewer extends VBox implements ViewerCreator, Stateful
{

    public enum HeaderControl
    {
        contacts,
        reportbetabug,
        reportbug,
        dowarmup,
        fullscreen,
        find
    };

    // Using an interface to reduce typing :)
    public interface CommandId
    {

        String debug = "debug";
        String debugmode = "debugmode";
        String whatsnew = "whatsnew";
        String vieweditors = "vieweditors";
        String editors = "editors";
        String contacts = "contacts";
        String dowarmup = "dowarmup";
        String showstatistics = "showstatistics";
        String statistics = "statistics";
        String charts = "charts";
        String reportbug = "reportbug";
        String contactsupport = "contactsupport";
        String contact = "contact";
        String about = "about";
        String achievements = "achievements";
        String viewachievements = "viewachievements";
        String options = "options";
        String showoptions = "showoptions";
        String showundealtwitheditormessages = "showundealtwitheditormessages";
        String warmup = "warmup";
        String editobjectnames = "editobjectnames";
        String showinviteeditor = "showinviteeditor";
        String targets = "targets";
        String viewtargets = "viewtargets";
        String dictionarymanager = "dictionarymanager";
        String debugconsole = "debugconsole";
        String newproject = "newproject";
        String manageprojectstatuses = "manageprojectstatuses";
        String importfile = "importfile";
        String closepopup = "closepopup";
        String incrementfontsize = "incrementfontsize";
        String decrementfontsize = "decrementfontsize";
        String resetfontsize = "resetfontsize";
        String edittags = "edittags";
        String find = "find";
        String fullscreen = "fullscreen";

    }

    private Viewer viewer = null;
    private ObjectProperty<Header> headerProp = null;
    private ScheduledThreadPoolExecutor generalTimer = null;
    private AchievementsPopup achievementsPopup = null;

    private Map<String, Command> actionMap = new HashMap<> ();
    private Tips tips = null;

    private Map<String, SideBar> sideBars = new HashMap<> ();
    private Stack<SideBar>  activeSideBars = new Stack<> ();
    private SideBar       currentOtherSideBar = null;
    private SideBar       mainSideBar = null;

    private StackPane sidebarsPane = null;
    private StackPane otherSidebarsPane = null;
    private SplitPane parentPane = null;
    private Node content = null;
    private VBox notifications = null;

    private EditorsSideBar editorsSideBar = null;

    private Set<QuollPopup> popups = new HashSet<> ();

    private Set<ProjectEventListener> projectEventListeners = new HashSet ();
    private boolean ignoreProjectEvents = false;

    private Pane popupPane = null;

    public AbstractViewer ()
    {

        final AbstractViewer _this = this;

        this.generalTimer = new ScheduledThreadPoolExecutor (2,
                                                             new ThreadFactory ()
        {

            @Override
            public Thread newThread (Runnable r)
            {

                Thread t = new Thread (r);

                t.setDaemon (true);
                t.setPriority (Thread.MIN_PRIORITY);
                t.setName ("Viewer-general-" + t.getId ());

                return t;

            }

        });

        this.setFillWidth (true);

        this.notifications = new VBox ();

        this.getChildren ().add (this.notifications);
        this.notifications.getStyleClass ().add (StyleClassNames.NOTIFICATIONS);
        VBox.setVgrow (this.notifications,
                       Priority.NEVER);

        this.sidebarsPane = new StackPane ();
        this.sidebarsPane.getStyleClass ().add (StyleClassNames.SIDEBARS);
        this.sidebarsPane.managedProperty ().bind (this.sidebarsPane.visibleProperty ());
        SplitPane.setResizableWithParent (this.sidebarsPane, false);

        this.otherSidebarsPane = new StackPane ();
        this.otherSidebarsPane.getStyleClass ().add (StyleClassNames.OTHERSIDEBARS);
        this.otherSidebarsPane.managedProperty ().bind (this.otherSidebarsPane.visibleProperty ());
        SplitPane.setResizableWithParent (this.otherSidebarsPane, false);

        this.popupPane = new Pane ();
        this.popupPane.getStyleClass ().add (StyleClassNames.POPUPPANE);

        this.parentPane = new SplitPane ();
        SplitPane.setResizableWithParent (this.parentPane, true);
        this.parentPane.getStyleClass ().add (StyleClassNames.CONTENT);
        this.parentPane.prefWidthProperty ().bind (this.popupPane.widthProperty ());
        this.parentPane.prefHeightProperty ().bind (this.popupPane.heightProperty ());
        VBox.setVgrow (this.popupPane,
                       Priority.ALWAYS);
        this.popupPane.getChildren ().add (this.parentPane);

        this.getChildren ().add (this.popupPane);

        try
        {

            this.tips = new Tips (this);

        } catch (Exception e) {

            Environment.logError ("Unable to init tips",
                                  e);

        }

        this.updateForDebugMode ();

        // When we update our user property, update the layout property.
        UserProperties.uiLayoutProperty ().addListener ((prop, oldv, newv) ->
        {

            _this.updateLayout ();

        });

        this.addEventHandler (ScrollEvent.SCROLL,
                              ev ->
        {

            if ((ev.isShiftDown ())
                &&
                (ev.isControlDown ())
               )
            {

                if (ev.getDeltaX () > 0)
                {

                    this.runCommand (CommandId.incrementfontsize);

                } else {

                    this.runCommand (CommandId.decrementfontsize);

                }

                ev.consume ();

            }

        });

    }

    public class QPopupWindow extends PopupWindow
    {

    }

    public QuollPopup getPopupById (String id)
    {

        return this.popups.stream ()
            .filter (p -> p.getPopupId () != null && p.getPopupId ().equals (id))
            .findFirst ()
            .orElse (null);

    }

    public void showPopup (QuollPopup p,
                           Node       n,
                           Side       s)
    {

        Bounds b = n.localToScene (n.getBoundsInLocal ());
        b = this.sceneToLocal (b);

        p.setVisible (false);
        this.addPopup (p);

        this.showPopup (p,
                        b.getMinX (),
                        b.getMaxY ());

    }

    public void showPopup (QuollPopup p,
                           double     x,
                           double     y)
    {

        p.setVisible (false);
        this.addPopup (p);
        p.applyCss ();
        p.requestLayout ();

        UIUtils.runLater (() ->
        {

            double _x = x;
            double _y = y;

            Bounds nb = p.getLayoutBounds ();

            Bounds b = this.getLayoutBounds ();

            if ((y + nb.getHeight ()) > b.getHeight ())
            {

                _y = b.getHeight () - nb.getHeight ();

            }

            if ((x + nb.getWidth ()) > b.getWidth ())
            {

                _x = b.getWidth () - nb.getWidth ();

            }

            if (_x == -1)
            {

                _x = ((b.getWidth () / 2) - (nb.getWidth () / 2d));

            }

            if (_y == -1)
            {

                _y = ((b.getHeight () / 2) - (nb.getHeight () / 2d));

            }

            if (_x < 0)
            {

                _x = 0;

            }

            if (_y < 0)
            {

                _y = 0;

            }

            // Casting to a whole number is required here to prevent blurring.
            p.relocate ((int) _x,
                        (int) _y);
            p.setVisible (true);

        });

    }

    public void removePopup (QuollPopup p)
    {

        p.setVisible (false);
        this.popups.remove (p);
        this.popupPane.getChildren ().remove (p);

    }

    public void addPopup (QuollPopup p)
    {

        this.popups.add (p);

        //p.setManaged (false);

        if (!this.popupPane.getChildren ().contains (p))
        {

            this.popupPane.getChildren ().add (p);

        }

    }

    public void setContent (Node n)
    {

        this.content = n;

        this.updateLayout ();

    }

    /**
     * Run the specified command, the command must map to one of the enum values.
     *
     * @param command The command to run.
     */
    public void runCommand (String   command)
    {

        this.runCommand (command,
                         null);

    }

    /**
     * Run the specified command.
     *
     * @param command The command to run.
     * @param doAfter An optional runnable to execute after the command has completed.
     */
    public void runCommand (String   command,
                            Runnable doAfter)
    {

        Command r = this.actionMap.get (command);

        if (r == null)
        {

            throw new IllegalArgumentException (String.format ("Command %1$s is not supported.", command));

        }

        // TODO: Check error handling, maybe wrap in a try/catch.
        r.run (doAfter);

    }

    protected void addActionMapping (Command command)
    {

        for (String c : command.commandIds ())
        {

            this.actionMap.put (c,
                                command);

        }

    }

    protected void addActionMapping (Runnable  action,
                                     String... commands)
    {

        this.addActionMapping (Command.create (action,
                                               commands));

    }

    protected void addKeyMapping (String           command,
                                  KeyCode           code,
                                  KeyCombination.Modifier... modifiers)
    {

        this.addKeyMapping (() -> runCommand (command),
                            code,
                            modifiers);

    }

    protected void addKeyMapping (Runnable          action,
                                  KeyCode           code,
                                  KeyCombination.Modifier... modifiers)
    {

        Scene s = this.getScene ();
        s.getAccelerators ().put (new KeyCodeCombination (code,
                                                          modifiers),
                                  action);

    }

    private void initKeyMappings ()
    {

        this.addKeyMapping (CommandId.debug,
                            KeyCode.F12, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (CommandId.debugmode,
                            KeyCode.F1, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (CommandId.whatsnew,
                            KeyCode.F11);
        this.addKeyMapping (CommandId.showoptions,
                            KeyCode.F3);
        this.addKeyMapping (CommandId.vieweditors,
                            KeyCode.F7);
        this.addKeyMapping (CommandId.dowarmup,
                            KeyCode.F10);
        this.addKeyMapping (CommandId.closepopup,
                            KeyCode.F4);
        this.addKeyMapping (CommandId.resetfontsize,
                            KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN);
        this.addKeyMapping (CommandId.incrementfontsize,
                            KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
        this.addKeyMapping (CommandId.decrementfontsize,
                            KeyCode.MINUS, KeyCombination.CONTROL_DOWN);

    }

    private void initActionMappings ()
    {

        final AbstractViewer _this = this;

        this.addActionMapping (() ->
        {

            UserProperties.setUIBaseFontSize (UserProperties.getAsFloat (Constants.DEFAULT_UI_BASE_FONT_SIZE_PROPERTY_NAME));

        },
        CommandId.resetfontsize);

        this.addActionMapping (() ->
        {
/*
TODO
            QuollPopup qp = this.getPopupById (TagsManager.POPUP_ID);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            new TagsManager (this.viewer).show ();
*/
            this.fireProjectEvent (ProjectEvent.Type.tags,
                                   ProjectEvent.Action.edit);

        },
        CommandId.edittags);

        this.addActionMapping (() ->
        {

            float f = UserProperties.getUIBaseFontSize ();

            f += f / 20;

            UserProperties.setUIBaseFontSize (f);

        },
        CommandId.incrementfontsize);

        this.addActionMapping (() ->
        {

            float f = UserProperties.getUIBaseFontSize ();

            f -= f / 20;

            UserProperties.setUIBaseFontSize (f);

        },
        CommandId.decrementfontsize);

        this.addActionMapping (() ->
        {

            NewProjectPopup p = new NewProjectPopup (_this);
            p.show ();

        },
        CommandId.newproject);

        this.addActionMapping (() ->
        {

            this.lookupAll ("." + StyleClassNames.QPOPUP).stream ()
                .max ((o1, o2) ->
                {

                    if (o1 == null)
                    {

                        return 1;

                    }

                    if (o2 == null)
                    {

                        return -1;

                    }

                    return o1.viewOrderProperty ().getValue ().compareTo (o2.viewOrderProperty ().getValue ());

                })
                .ifPresent (n ->
                {

                    QuollPopup p = (QuollPopup) n;
                    p.close ();

                });

        },
        CommandId.closepopup);

        this.addActionMapping (() -> this.showWarmupPromptSelect (),
                               CommandId.dowarmup,
                               CommandId.warmup);
        this.addActionMapping (() -> new DebugConsole (_this),
                               CommandId.debug);
        this.addActionMapping (() ->
        {

            Environment.setDebugModeEnabled (!Environment.isDebugModeEnabled ());

            _this.updateForDebugMode ();

            // Add a notification.
            _this.addNotification (getUILanguageStringProperty (debugmode, Environment.isDebugModeEnabled () ? enabled : disabled),
                                   Constants.BUG_ICON_NAME,
                                   10);

        },
        CommandId.debugmode);

        this.addActionMapping (() ->
        {
/*
			UserProperties.set (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
								"0");
*/
            _this.showWhatsNew (true);

        },
        CommandId.whatsnew);

        this.addActionMapping (() -> this.showContactSupport (),
                               CommandId.contactsupport,
                               CommandId.contact);
        this.addActionMapping (() -> _this.showObjectTypeNameChanger (),
                               CommandId.editobjectnames);
        /* TODO
        this.addActionMapping (() -> EditorsUIUtils.showInviteEditor (_this),
                               Command.showinviteeditor);
        */
        this.addActionMapping (() ->
        {

            try
            {

                _this.viewEditors ();

            } catch (Exception e) {

                Environment.logError ("Unable to show editors",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (editors,vieweditorserror));

            }

        },
        CommandId.vieweditors,
        CommandId.editors,
        CommandId.contacts,
        CommandId.showundealtwitheditormessages);

        this.addActionMapping (() -> _this.showReportProblem (),
                               CommandId.reportbug);

        this.addActionMapping (() -> _this.showAbout (),
                               CommandId.about);

        this.addActionMapping (() -> _this.showDictionaryManager (),
                               CommandId.dictionarymanager);

    }

    public Command getActionMapping (String id)
    {

        return this.actionMap.get (id);

    }

    public void handleHTMLPanelAction (String v)
    {

        StringTokenizer t = new StringTokenizer (v,
                                                 ",;");

        if (t.countTokens () > 1)
        {

            while (t.hasMoreTokens ())
            {

                this.handleHTMLPanelAction (t.nextToken ().trim ());

            }

            return;

        }

        try
        {

            // TODO: Make this work with the action mappings.
            if (v.startsWith ("options"))
            {

                String section = null;

                int dot = v.indexOf ('.');

                if (dot > 1)
                {

                    section = v.substring (dot + 1);

                }

                this.showOptions (section);

                return;

            }

            this.runCommand (v);

        } catch (Exception e) {

            Environment.logError ("Unable to perform action: " +
                                  v,
                                  e);

        }

    }

    public ObjectProperty<Header> headerProperty ()
    {

        return this.headerProp;

    }

    @Override
    public State getState ()
    {

        State s = this.getViewer ().getState ();

        if (s == null)
        {

            s = new State ();

        }

        return s;

    }

    /**
     * Init and show the viewer.
     */
    @Override
    public void init (State s)
               throws GeneralException
    {

        this.getViewer ().init (s);

        this.initActionMappings ();
        this.initKeyMappings ();

        Environment.doNewsAndVersionCheck (this);

        this.handleWhatsNew ();

        this.handleShowTips ();

        // We show later to ensure that the init has worked.
        Environment.registerViewer (this);

        UIUtils.runLater (() ->
        {

            this.updateLayout ();
            this.show ();

        });

    }

    public void close (Runnable afterClose)
    {

        this.notifications.setVisible (false);

        this.generalTimer.shutdown ();

        this.getViewer ().close ();

        // Fire an event.
        this.fireEvent (new Viewer.ViewerEvent (this.getViewer (),
                                                Viewer.ViewerEvent.CLOSE_EVENT));

    }

    public void fireProjectEventLater (final ProjectEvent.Type   type,
                                       final ProjectEvent.Action action)
    {

        final AbstractViewer _this = this;

        UIUtils.runLater (() -> _this.fireProjectEvent (type, action));

    }

    public void fireProjectEvent (ProjectEvent.Type   type,
                                  ProjectEvent.Action action,
                                  Object              contextObject)
    {

        this.fireProjectEvent (new ProjectEvent (this,
                                                 type,
                                                 action,
                                                 contextObject));

    }

    public void fireProjectEvent (ProjectEvent.Type   type,
                                  ProjectEvent.Action action)
    {

        this.fireProjectEvent (new ProjectEvent (this,
                                                 type,
                                                 action));

    }

    public void setIgnoreProjectEvents (boolean v)
    {

        this.ignoreProjectEvents = v;

    }

    public void fireProjectEvent (ProjectEvent ev)
    {

        if (this.ignoreProjectEvents)
        {

            return;

        }

        Environment.fireUserProjectEvent (ev);

    }

    /**
     * The layout is done in terms of preferred sizes and resize weights.
     * We then call resetToPreferredSizes on the split panes to resize.
     * The resize weights ensure that the tabs get all the extra space (without it one of the sidebars
     * could get the space).  Weights of 0.98f and 0.02f are used to ensure that the sidebars aren't shrunk below
     * their current size (can't work out why it does that...)
     *
     * @{link splitpane2} is nested within the right half of {@link splitpane}.
     *
     * A zero sized filler is used to when a "half" isn't used to ensure that the relevant splitpane
     * doesn't freak out. See {@link createLayoutFiller()}.
     */
    public void updateLayout ()
    {

        String layout = UserProperties.uiLayoutProperty ().getValue ();

        this.parentPane.getItems ().clear ();

        if (layout.equals (Constants.LAYOUT_PS_CH_OS))
        {

            // Add the main sidebar, the content and the other sidebar (if present).
            if (this.mainSideBar != null)
            {

                this.parentPane.getItems ().add (this.sidebarsPane);

            }

            this.parentPane.getItems ().addAll (this.content);

            if (this.currentOtherSideBar != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPane);

            }

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_OS_CH_PS))
        {

            if (this.currentOtherSideBar != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPane);

            }

            if (this.mainSideBar != null)
            {

                this.parentPane.getItems ().add (this.sidebarsPane);

            }

            this.parentPane.getItems ().addAll (this.content);

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_PS_OS_CH))
        {

            if (this.mainSideBar != null)
            {

                this.parentPane.getItems ().add (this.sidebarsPane);

            }

            if (other != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPane);

            }

            this.parentPane.getItems ().add (this.content);

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_CH_OS_PS))
        {

            this.parentPane.getItems ().add (this.content);

            if (other != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPane);

            }

            if (this.mainSideBar != null)
            {

                this.parentPane.getItems ().add (this.sidebarsPane);

            }

        }

        if (layout.equals (Constants.LAYOUT_PS_CH))
        {

            double w = 0;

            SideBar sb = (this.currentOtherSideBar != null ? this.currentOtherSideBar : this.mainSideBar);

            if (sb != null)
            {

                w = this.sidebarsPane.getWidth ();

                this.sidebarsPane.getChildren ().clear ();
                this.sidebarsPane.getChildren ().add (sb);

                this.parentPane.getItems ().add (this.sidebarsPane);

            }

            this.parentPane.getItems ().add (this.content);

            if (sb != null)
            {

                double _w = w;

                UIUtils.runLater (() ->
                {

                    double mw = _w;

                    if (mw < 1)
                    {

                        mw = Math.min (sb.prefWidth (this.parentPane.getHeight ()), _w);

                    }

                    double pw = this.parentPane.getWidth ();

                    this.parentPane.setDividerPositions (mw / pw);

                });

            }

        }

        if (layout.equals (Constants.LAYOUT_CH_PS))
        {

            this.parentPane.getItems ().addAll (this.content);

            if (this.mainSideBar != null)
            {

                this.parentPane.getItems ().add (this.sidebarsPane);

            }

        }

    }

    public Notification addNotification (Node    comp,
                                         String  styleName,
                                         int     duration)
    {

        return this.addNotification (comp,
                                     styleName,
                                     duration,
                                     null);

    }

    /**
     * Adds a notification to the notification area, the action listener can be used
     * to remove the notification, it can be safely called with a null event.
     *
     * @param comp The component to add to the notification.
     * @param styleName The class name for the type of notification, supported values are "information" and "notify".
     * @param duration The time, in seconds, that the notification should be shown for, if less than 1 then
     *                 the notification won't be auto removed.
     * @return An action listener that can be called to remove the notification.
     */
    public Notification addNotification (Node       comp,
                                         String     styleName,
                                         int        duration,
                                         Set<Node> controls)
    {

        Notification n = Notification.builder ()
                .inViewer (this)
                .content (comp)
                .styleName (styleName)
                .duration (duration)
                .controls (controls)
                .build ();

        this.addNotification (n);

        n.init ();

        return n;

    }

    public void removeAllNotifications ()
    {

        this.notifications.getChildren ().removeAll ();

    }

    public void removeNotification (Notification n)
    {

        UIUtils.runLater (() ->
        {

            this.notifications.getChildren ().remove (n);

            if (this.notifications.getChildren ().size () == 0)
            {

                this.notifications.setVisible (false);

            }

        });

    }

    public void addNotification (Notification n)
    {

        UIUtils.runLater (() ->
        {

            this.notifications.getChildren ().add (0,
                                                   n);

            this.notifications.setVisible (true);

            n.init ();

        });

    }

    public Notification addNotification (StringProperty text,
                                         String         type,
                                         int            duration)
    {

        return this.addNotification (text,
                                     type,
                                     duration,
                                     null);

    }

    public Notification addNotification (StringProperty    text,
                                         String            type,
                                         int               duration,
                                         EventHandler<MouseEvent> clickListener)
    {

        // TODO Improve to use markdown/html?
        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .text (text)
            .build ();
        t.setOnMouseClicked (clickListener);

        return this.addNotification (t,
                                     type,
                                     duration);

    }

    private void handleWhatsNew ()
    {

        boolean showWhatsNew = false;

        String whatsNewVersion = UserProperties.get (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);
        //Environment.getProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);

        if (whatsNewVersion != null)
        {

            Version lastViewed = new Version (whatsNewVersion);

            if (lastViewed.isNewer (Environment.getQuollWriterVersion ()))
            {

                showWhatsNew = true;

            }

        }

        if (showWhatsNew)
        {

            this.showWhatsNew (false);

        }

    }

    private void handleShowTips ()
    {

        if (Environment.isFirstUse ())
        {

            return;

        }

        if ((this.tips != null)
            &&
            (UserProperties.getAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME))
           )
        {

            final AbstractViewer _this = this;

            try
            {

                String tipText = this.tips.getNextTip ();

                final Text text = new Text (tipText);

                ButtonBar bb = new ButtonBar ();

                QuollButton next = QuollButton.builder ()
                    .styleClassName (StyleClassNames.NEXT)
                    .tooltip (tipspanel,LanguageStrings.next,tooltip)
                    .buttonType (ButtonBar.ButtonData.NEXT_FORWARD)
                    .build ();

                QuollButton off = QuollButton.builder ()
                    .styleClassName (StyleClassNames.STOP)
                    .tooltip (tipspanel,stop,tooltip)
                    .buttonType (ButtonBar.ButtonData.FINISH)
                    .build ();

                Set<Node> controls = new LinkedHashSet<> ();

                bb.getButtons ().addAll (next, off);

                controls.add (bb);

                Notification n = Notification.builder ()
                    .styleName (StyleClassNames.TIPS)
                    .content (text)
                    .duration (90)
                    .controls (controls)
                    .inViewer (this)
                    .build ();

                next.setOnAction (ev ->
                {

                    String t = _this.tips.getNextTip ();

                    if (t != null)
                    {

                        text.setText (t);

                        n.restartTimer ();

                        _this.fireProjectEvent (ProjectEvent.Type.tips,
                                                ProjectEvent.Action.show);

                    }

                });

                off.setOnAction (ev ->
                {

                    java.util.List<String> prefix = Arrays.asList (tipspanel,stop,popup);

                    ComponentUtils.createQuestionPopup (getUILanguageStringProperty (prefix, LanguageStrings.title),
                                                        //"Stop showing tips?",
                                                        StyleClassNames.STOP,
                                                        getUILanguageStringProperty (prefix,LanguageStrings.text),
                                                        //"Stop showing tips when Quoll Writer starts?<br /><br />They can enabled at any time in the <a href='action:options.start'>Options panel</a>.",
                                                        getUILanguageStringProperty (prefix,buttons,confirm),
                                                        //"Yes, stop showing them",
                                                        getUILanguageStringProperty (prefix,buttons,cancel),
                                                        //"No, keep them",
                                                        // On confirm...
                                                        fev ->
                                                        {

                                                              _this.fireProjectEvent (ProjectEvent.Type.tips,
                                                                                      ProjectEvent.Action.off);

           													  UserProperties.set (Constants.SHOW_TIPS_PROPERTY_NAME,
        													  					  false);

                                                              _this.removeNotification (n);

                                                         },
                                                         _this);

                });

            } catch (Exception e) {

                Environment.logError ("Unable to show tips",
                                      e);

            }

        }

    }

    /**
     * Un-schedule the scheduledfuture (gained from a call to Environment.schedule).
     *
     * @param r The scheduledfuture to remove from the executor service.
     * @returns Whether it was successfully removed.
     */
    public void unschedule (ScheduledFuture f)
    {

        if (f == null)
        {

            return;

        }

        // Let the task run to completion.
        f.cancel (false);

        this.generalTimer.purge ();

    }

    /**
     * Schedule the runnable to run after delay and repeat (use -1 or 0 for no repeat).
     *
     * @param r The runnable to run.
     * @param delay The delay, in millis.
     * @param repeat The repeat time, in millis.
     */
    public ScheduledFuture schedule (final Runnable r,
                                     final long     delay,
                                     final long     repeat)
    {

        if (this.generalTimer == null)
        {

            Environment.logError ("Unable to schedule timer is no longer valid.");

            return null;

        }

        if (this.generalTimer.isShutdown ())
        {

            throw new IllegalStateException ("Unable to run due to previous shutdown of timer: " + r);

        }

        if (r == null)
        {

            Environment.logError ("Unable to schedule timer, runnable is null.");

            return null;

        }

        if (repeat < 1)
        {

            return this.generalTimer.schedule (r,
                                               delay,
                                               TimeUnit.MILLISECONDS);

        } else {

            return this.generalTimer.scheduleAtFixedRate (r,
                                                          delay,
                                                          repeat,
                                                          TimeUnit.MILLISECONDS);

        }

    }

    public void showWhatsNew (boolean v)
    {

        QuollPopup qp = this.getPopupById (WhatsNewPopup.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        try
        {

            WhatsNewPopup p = new WhatsNewPopup (this,
                                                 v);

            p.show ();

        } catch (Exception e) {

            // Not good but not the end of the world but shouldn't stop things from going on.
            Environment.logError ("Unable to init whats new",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (whatsnew,actionerror));
                                      //"Unable to show What's New, please contact Quoll Writer support for assistance.");

            return;

        }

    }

    public void showContactSupport ()
    {

        new ContactSupportPopup (this).show ();

    }

    public void showDictionaryManager ()
    {

        // TODO

    }

    public void showAbout ()
    {

        // TODO
        QuollPopup qp = this.getPopupById (AboutPopup.POPUP_ID);

        if (qp != null)
        {

            qp.show ();
            qp.toFront ();
            return;

        }

        new AboutPopup (this).show ();

    }

    public void showObjectTypeNameChanger ()
    {

        // TODO

    }

    public void showWarmupPromptSelect ()
    {

        QuollPopup qp = this.getPopupById (DoWarmupExercisePopup.POPUP_ID);

        if (qp != null)
        {

            qp.show ();
            qp.toFront ();
            return;

        }

        new DoWarmupExercisePopup (this).show ();

    }

    public void showReportProblem ()
    {

        this.showReportProblem (null);

    }

    public void showReportProblem (String bugText)
    {

        // TODO
        new ReportBugPopup (this).show ();

    }

    private void updateForDebugMode ()
    {
/*
TODO
        this.headerProp.getValue ().getStyleClass ().remove (StyleClassNames.DEBUG);

        if (Environment.isDebugModeEnabled ())
        {

            this.headerProp.getValue ().getStyleClass ().add (StyleClassNames.DEBUG);

        }
*/
    }

    public Node getTitleHeaderControl (HeaderControl control)
    {

        final AbstractViewer _this = this;

        List<String> prefix = Arrays.asList (project, LanguageStrings.title,toolbar,buttons);

        if (control == HeaderControl.reportbug)
        {

            return QuollButton.builder ()
                .tooltip (prefix,bug,tooltip)
                .styleClassName (StyleClassNames.BUG)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.reportbug);

                })
                .build ();

        }

        if (control == HeaderControl.find)
        {

            return QuollButton.builder ()
                .tooltip (prefix,find,tooltip)
                .styleClassName (StyleClassNames.FIND)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.find);

                })
                .build ();

        }

        if (control == HeaderControl.fullscreen)
        {

            return QuollButton.builder ()
                .tooltip (prefix,fullscreen,tooltip)
                .styleClassName (StyleClassNames.FULLSCREEN)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.fullscreen);

                })
                .build ();

        }

        if (control == HeaderControl.dowarmup)
        {

            return QuollButton.builder ()
                .tooltip (prefix,warmup,tooltip)
                .styleClassName (StyleClassNames.WARMUP)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.dowarmup);

                })
                .build ();

        }

        if (control == HeaderControl.reportbetabug)
        {

            return null;

        }

        if (control == HeaderControl.contacts)
        {

            if (EditorsEnvironment.isEditorsServiceAvailable ())
            {

                String type = LanguageStrings.showcontacts;

                if (!EditorsEnvironment.hasRegistered ())
                {

                    type = LanguageStrings.editorsserviceregister;

                }

                //String toolTip = (EditorsEnvironment.hasRegistered () ? "Click to show my {contacts}" : "Click to register for the Editors Service.");
                return QuollButton.builder ()
                    .tooltip (prefix,type,tooltip)
                    .styleClassName (StyleClassNames.CONTACTS)
                    .onAction (ev ->
                    {

                        if ((_this.isSideBarVisible (EditorsSideBar.SIDEBAR_ID))
                            &&
                            (!EditorsEnvironment.isUserLoggedIn ())
                           )
                        {

                            EditorsEnvironment.goOnline (null,
                                                         null,
                                                         null,
                                                         null);

                            return;

                        }

                        _this.runCommand (CommandId.contacts);

                    })
                    .build ();

            }

            return null;

        }

        return null;

    }

    public abstract String getStyleClassName ();

    public abstract Supplier<Set<MenuItem>> getSettingsMenuSupplier ();

    public abstract Supplier<Set<Node>> getTitleHeaderControlsSupplier ();

    public abstract StringProperty titleProperty ();

    public abstract SideBar getMainSideBar ();

    public abstract void showOptions (String sect)
                               throws GeneralException;

    //public abstract Set<String> getTitleHeaderControlIds ();

    public boolean isSideBarVisible (String id)
    {

        if ((this.currentOtherSideBar != null)
            &&
            (this.currentOtherSideBar.getSideBarId ().equals (id))
           )
        {

            return true;

        }

        // TODO Peek at the top of the active sidebars and see if it's our sidebar.

        return false;

    }

    private boolean isUILayoutShowSingleSidebar ()
    {

        String layout = this.getUILayout ();

        if (layout.equals (Constants.LEFT))
        {

            layout = Constants.LAYOUT_PS_CH;

        }

        if (layout.equals (Constants.RIGHT))
        {

            layout = Constants.LAYOUT_CH_PS;

        }

        return (layout.equals (Constants.LAYOUT_PS_CH)
                ||
                layout.equals (Constants.LAYOUT_CH_PS));

    }

    public void setMainSideBar (SideBar sb)
    {

        this.mainSideBar = sb;
        this.addSideBar (sb);
        this.sidebarsPane.getChildren ().clear ();
        this.sidebarsPane.getChildren ().add (sb);
        this.updateLayout ();

    }

    public void showSideBar (SideBar sb)
    {

        this.showSideBar (sb,
                          null);

    }

    public void showSideBar (SideBar  sb,
                             Runnable doAfterView)
    {

        this.showSideBar (sb.getSideBarId (),
                          doAfterView);

    }

    public void showSideBar (String id)
    {

        this.showSideBar (id,
                          null);

    }

    public void showSideBar (String   id,
                             Runnable doAfterView)
    {

        if ((this.currentOtherSideBar != null)
            &&
            (this.currentOtherSideBar.getSideBarId ().equals (id))
           )
        {

            this.currentOtherSideBar.setVisible (true);
            return;

        }

        SideBar b = this.sideBars.get (id);

        if (b == null)
        {

            throw new IllegalArgumentException ("Unable to show sidebar: " +
                                                id +
                                                ", no sidebar found with that id.");

        }

        if (this.currentOtherSideBar != null)
        {

            this.currentOtherSideBar.setVisible (false);
            this.currentOtherSideBar.fireEvent (new SideBar.SideBarEvent (this,
                                                                          this.currentOtherSideBar,
                                                                          SideBar.SideBarEvent.HIDE_EVENT));

        }

        if (id.equals (this.getMainSideBarId ()))
        {

            // Need to check the layout.  If we are only showing one sidebar then set the current other
            // to null.
            if (this.isUILayoutShowSingleSidebar ())
            {

                this.currentOtherSideBar = null;

            }

        } else {

            this.currentOtherSideBar = b;
            this.currentOtherSideBar.setVisible (true);

            this.otherSidebarsPane.getChildren ().removeAll ();
            this.otherSidebarsPane.getChildren ().add (this.currentOtherSideBar);

            this.activeSideBars.remove (b);

            this.activeSideBars.push (b);

            this.currentOtherSideBar.fireEvent (new SideBar.SideBarEvent (this,
                                                                          this.currentOtherSideBar,
                                                                          SideBar.SideBarEvent.SHOW_EVENT));

        }

        this.updateLayout ();

/*
TODO
        if (this.fsf != null)
        {

            this.fsf.showSideBar ();

        } else {

            this.updateLayout ();

		}
*/
        if (doAfterView != null)
        {

            UIUtils.runLater (doAfterView);

        }

    }

    public int getActiveSideBarCount ()
    {

        int c = this.activeSideBars.size ();
/*
TODO Clean up?
        String l = UserProperties.uiLayoutProperty ().getValue ();

        if ((l.equals (Constants.LEFT))
            ||
            (l.equals (Constants.RIGHT))
            ||
            (l.equals (Constants.LAYOUT_PS_CH))
            ||
            (l.equals (Constants.LAYOUT_CH_PS))
           )
        {

            c++;

        }
*/
        return c;

    }

    public ContextMenu getShowOtherSideBarsSelector ()
    {

        final AbstractViewer _this = this;

        ContextMenu m = new ContextMenu ();

        String l = UserProperties.uiLayoutProperty ().getValue ();

        if (this.currentOtherSideBar != null)
        {

            if ((l.equals (Constants.LEFT))
                ||
                (l.equals (Constants.RIGHT))
                ||
                (l.equals (Constants.LAYOUT_PS_CH))
                ||
                (l.equals (Constants.LAYOUT_CH_PS))
               )
            {

                if (this.mainSideBar != null)
                {

                    QuollMenuItem mi = QuollMenuItem.builder ()
                        .label (this.mainSideBar.activeTitleProperty ())
                        .styleClassName (this.mainSideBar.getStyleClassName ())
                        .onAction (ev ->
                        {

                            _this.showMainSideBar ();

                        })
                        .build ();

                    m.getItems ().add (mi);

                }

            }

        }

        // Means we are showing the main sidebar and the other sidebar.
        // Exclude those from the list.
        for (SideBar sb : this.activeSideBars)
        {

            if ((this.currentOtherSideBar != null)
                &&
                (this.currentOtherSideBar == sb)
               )
            {

                continue;

            }

            final SideBar _sb = sb;

            QuollMenuItem mi = QuollMenuItem.builder ()
                .label (sb.activeTitleProperty ())
                .styleClassName (sb.getStyleClassName ())
                .onAction (ev ->
                {

                    _this.showSideBar (_sb.getSideBarId ());

                })
                .build ();

            m.getItems ().add (mi);

        }

        return m;

    }

    public void showMainSideBar ()
    {

        if (this.getMainSideBarId () != null)
        {

            this.showSideBar (this.getMainSideBarId ());
            return;

        }

        this.updateLayout ();

    }

    public String getMainSideBarId ()
    {

        if (this.mainSideBar == null)
        {

            this.mainSideBar = this.getMainSideBar ();

        }

        if (this.mainSideBar != null)
        {

            return this.mainSideBar.getSideBarId ();

        }

        return null;

    }

    public SideBar getActiveOtherSideBar ()
    {

        if (this.activeSideBars.size () > 0)
        {

            return this.activeSideBars.peek ();

        }

        return null;

    }

    public void closeSideBar ()
    {

        if (this.currentOtherSideBar != null)
        {

            this.currentOtherSideBar.fireEvent (new SideBar.SideBarEvent (this,
                                                                          this.currentOtherSideBar,
                                                                          SideBar.SideBarEvent.HIDE_EVENT));

            this.currentOtherSideBar.setVisible (false);

        }

        this.currentOtherSideBar = null;

        this.showMainSideBar ();

    }

    public SideBar getSideBarById (String id)
    {

        return this.sideBars.get (id);

    }

    public void removeSideBar (String id)
    {

        this.removeSideBar (this.getSideBarById (id));

    }

    public void removeSideBar (SideBar sb)
    {

        if (sb == null)
        {

            return;

        }

        this.sideBars.remove (sb.getSideBarId ());

        this.activeSideBars.remove (sb);

        if (this.currentOtherSideBar == sb)
        {

            this.currentOtherSideBar = null;

        }

        try
        {

            sb.fireEvent (new SideBar.SideBarEvent (this,
                                                    sb,
                                                    SideBar.SideBarEvent.HIDE_EVENT));

            sb.fireEvent (new SideBar.SideBarEvent (this,
                                                    sb,
                                                    SideBar.SideBarEvent.CLOSE_EVENT));

        } catch (Exception e) {

            Environment.logError ("Unable to close sidebar: " + sb.getSideBarId (),
                                  e);

        }

        SideBar _sb = (this.activeSideBars.size () > 0 ? this.activeSideBars.peek () : null);

        if (_sb != null)
        {

            this.showSideBar (_sb.getSideBarId ());

        } else {

/*
TODO
            if (this.fsf == null)
            {

                // TODO this.setUILayout (this.layout);

            }
*/
        }

    }

    public Set<SideBar> getSideBars ()
    {

        return new HashSet<> (this.sideBars.values ());

    }

    public void addSideBar (SideBarContent sb)
    {

        this.addSideBar (sb.getSideBar ());

    }

    private void addSideBar (SideBar sb)
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

        String sid = sb.getSideBarId ();

        if (sid == null)
        {

            throw new IllegalArgumentException ("Sidebar must have an id.");

        }

        SideBar _sb = this.getSideBarById (sid);

        if (_sb != null)
        {

            throw new IllegalArgumentException ("Already have a sidebar with id: " + sid);

        }

        this.sideBars.put (sid,
                           sb);

    }

/*
 TODO Still needed?
    @Override
    public boolean isEditorsVisible ()
    {

        return this.isEditorsSideBarVisible ();

    }

    public boolean isEditorsSideBarVisible ()
    {

        EditorsSideBar sb = (EditorsSideBar) this.sideBars.get (EditorsSideBar.ID);

        if (sb != null)
        {

            return sb.isShowing ();

        }

        return false;

    }
*/

  public void sendMessageToEditor (final EditorEditor ed)
                            throws GeneralException
  {

      final AbstractViewer _this = this;

      this.runCommand (AbstractViewer.CommandId.vieweditors,
                       () ->
      {

          try
          {

              _this.editorsSideBar.showChatBox (ed);

          } catch (Exception e) {

              Environment.logError ("Unable to show editor: " +
                                    ed,
                                    e);

              ComponentUtils.showErrorMessage (_this,
                                                getUILanguageStringProperty (editors,vieweditorerror));
                                                //"Unable to show Editor");

          }

      });

  }

  public boolean viewEditors ()
                       throws GeneralException
  {

      // See if the user has an account or has already registered, if so show the sidebar
      // otherwise show the register.
      if (!EditorsEnvironment.hasRegistered ())
      {

          try
          {

              // TODO EditorsUIUtils.showRegister (this);

          } catch (Exception e) {

              Environment.logError ("Unable to show editor register",
                                    e);

              ComponentUtils.showErrorMessage (this,
                                               getUILanguageStringProperty (editors,showregistererror));
                                                //"Unable to show the editors register panel, please contact Quoll Writer support for assistance.");

              return false;

          }

          return true;

      }

      if (this.editorsSideBar == null)
      {

          this.editorsSideBar = new EditorsSideBar (this);

          this.addSideBar (this.editorsSideBar);

      }

      this.showSideBar (EditorsSideBar.SIDEBAR_ID);

      return true;

    }

    public void viewEditor (final EditorEditor ed)
                     throws GeneralException
    {

        final AbstractViewer _this = this;

        this.runCommand (AbstractViewer.CommandId.vieweditors,
                         () ->
        {

            try
            {

                _this.editorsSideBar.showEditor (ed);

            } catch (Exception e) {

                Environment.logError ("Unable to show editor: " +
                                      ed,
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (editors,vieweditorerror));
                                                 //"Unable to show Editor");

            }

        });

    }

    private String getUILayout ()
    {

        String sidebarLoc = UserProperties.get (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        String uiLayout = Constants.LAYOUT_CH_PS;

        // Legacy, pre-v2.5
        if (sidebarLoc != null)
        {

            if (sidebarLoc.equals (Constants.RIGHT))
            {

                uiLayout = Constants.LAYOUT_CH_PS;

            }

            UserProperties.remove (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        } else {

            uiLayout = UserProperties.get (Constants.UI_LAYOUT_PROPERTY_NAME);

        }

        // Legacy, pre-2.5
        if (uiLayout.equals (Constants.LEFT))
        {

            uiLayout = Constants.LAYOUT_PS_CH;

        }

        if (uiLayout.equals (Constants.RIGHT))
        {

            uiLayout = Constants.LAYOUT_CH_PS;

        }

        return uiLayout;

    }
/*
TODO Not needed, is a function of the sidebar itself...
    public void addToSidebarWrapper (Side where,
                                     Node node)
    {

        if (where == Side.TOP)
        {

            this.sidebarsPane.getChildren ().add (0,
                                             node);

            return;

        }

        if (where == Side.BOTTOM)
        {

            this.sidebarsPaneWrapper.getChildren ().add (node);

            return;

        }

        throw new IllegalArgumentException ("Side: " + where + " is not supported.");

    }
*/
    public Viewer getViewer ()
    {

        if (this.viewer == null)
        {

            throw new IllegalStateException ("Viewer has not been created.");

        }

        return this.viewer;

    }

    @Override
    public Viewer createViewer ()
                         throws GeneralException
    {

        final AbstractViewer _this = this;

        // Get the header controls.

        Supplier<Set<Node>> hcsupp = this.getTitleHeaderControlsSupplier ();

        Set<Node> headerCons = new LinkedHashSet<> ();

        if (hcsupp != null)
        {

            headerCons.addAll (hcsupp.get ());

        }

        List<String> prefix = Arrays.asList (project, LanguageStrings.title,toolbar,buttons);

        QuollMenuButton context = QuollMenuButton.builder ()
            .styleClassName (StyleClassNames.VIEWERMENU)
            .tooltip (prefix,projectmenu,tooltip)
            .items (() ->
            {
        //Environment.setNightModeEnabled (!Environment.nightModeProperty ().get ());

                Supplier<Set<MenuItem>> menuItemSupp = _this.getSettingsMenuSupplier ();

                Set<MenuItem> items = new LinkedHashSet<> ();

                if (menuItemSupp != null)
                {

                     items.addAll (menuItemSupp.get ());

                 }

                if (items.size () > 0)
                {

                    items.add (new SeparatorMenuItem ());

                }

                List<String> mprefix = Arrays.asList (LanguageStrings.project,settingsmenu,LanguageStrings.items);

                items.add (QuollMenuItem.builder ()
                    .label (mprefix,options)
                    .styleClassName (StyleClassNames.OPTIONS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.options);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (mprefix,achievements)
                    .styleClassName (StyleClassNames.ACHIEVEMENTS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.viewachievements);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (mprefix,whatsnew)
                    .styleClassName (StyleClassNames.WHATSNEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.whatsnew);

                    })
                    .build ());

                QuollMenu helpMenu = QuollMenu.builder ()
                    .label (mprefix,help)
                    .styleClassName (StyleClassNames.HELP)
                    .build ();

                items.add (helpMenu);

                // Report Bug/Problem
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,reportbug)
                    .styleClassName (StyleClassNames.REPORTBUG)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.reportbug);

                    })
                    .build ());

                // Contact Support
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,contactsupport)
                    .styleClassName (StyleClassNames.CONTACTSUPPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.contactsupport);

                    })
                    .build ());

                // View the User Guide
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,viewuserguide)
                    .styleClassName (StyleClassNames.VIEWUSERGUIDE)
                    .onAction (ev ->
                    {

                        UIUtils.openURL (_this,
                                         "help:getting-started");

                    })
                    .build ());

                // Keyboard shortcuts
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,keyboardshortcuts)
                    .styleClassName (StyleClassNames.KEYBOARDSHORTCUTS)
                    .onAction (ev ->
                    {

                        UIUtils.openURL (_this,
                                         "help:keyboard-shortcuts");

                    })
                    .build ());

                // About Quoll Writer
                items.add (QuollMenuItem.builder ()
                    .label (mprefix,about)
                    .styleClassName (StyleClassNames.ABOUT)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.about);

                    })
                    .build ());

                if (Environment.isDebugModeEnabled ())
                {

                    // Debug Console
                    items.add (QuollMenuItem.builder ()
                        .label ("Debug Console")
                        .styleClassName (StyleClassNames.DEBUGCONSOLE)
                        .onAction (ev ->
                        {

                            _this.runCommand (AbstractViewer.CommandId.debugconsole);

                        })
                        .build ());

                }

                return items;

            })
            .build ();
        headerCons.add (context);

        Set<Node> visItems = new LinkedHashSet<> ();
        visItems.add (context);

        ConfigurableToolbar ctb = ConfigurableToolbar.builder ()
            .items (headerCons)
            .visibleItems (headerCons)
            .withViewer (this)
            .build ();

        Viewer v = Viewer.builder ()
            //.headerControls (headerCons)
            .headerToolbar (ctb)
            .styleClassName (this.getStyleClassName ())
            .content (this)
            .title (this.titleProperty ())
            .build ();

        v.setOnCloseRequest (ev ->
        {

            ev.consume ();
            _this.close (null);

        });

        v.setResizable (true);

        v.init (null);

        Environment.registerViewer (this);

        this.viewer = v;

        return v;

    }

    public void setIconified (boolean v)
    {

        this.getViewer ().setIconified (v);

    }

    public void toFront ()
    {

        this.getViewer ().toFront ();

    }

    public void show ()
    {

        this.getViewer ().show ();

    }

    public void showAchievement (AchievementRule ar)
    {

        if (this.achievementsPopup == null)
        {

            this.achievementsPopup = new AchievementsPopup (this);

        }

        this.achievementsPopup.showAchievement (ar);

    }

    public void removeProjectEventListener (ProjectEventListener l)
    {

        this.projectEventListeners.remove (l);

    }

    public void addProjectEventListener (ProjectEventListener l)
    {

        this.projectEventListeners.add (l);

    }

}
