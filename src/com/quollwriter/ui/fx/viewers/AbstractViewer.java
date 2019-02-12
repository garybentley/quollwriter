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

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractViewer extends VBox implements ViewerCreator, Stateful
{

    private Viewer viewer = null;

    public enum HeaderControl
    {
        contacts,
        reportbetabug,
        reportbug,
        dowarmup
    };

    public enum Command
    {

        debug,
        debugmode,
        whatsnew,
        vieweditors,
        editors,
        contacts,
        dowarmup,
        showstatistics,
        statistics,
        charts,
        reportbug,
        contactsupport,
        contact,
        about,
        achievements,
        viewachievements,
        options,
        showoptions,
        showundealtwitheditormessages,
        warmup,
        editobjectnames,
        showinviteeditor,
        targets,
        viewtargets,
        dictionarymanager,
        debugconsole;
    }

    private ObjectProperty<Header> headerProp = null;
    private ScheduledThreadPoolExecutor generalTimer = null;
    private Map<Command, Runnable> actionMap = new HashMap<> ();
    private Tips tips = null;

    private boolean ignoreProjectEvents = false;

    private Map<String, SideBar> sideBars = new HashMap<> ();
    private Stack<SideBar>  activeSideBars = new Stack ();
    private SideBar       currentOtherSideBar = null;
    private SideBar       mainSideBar = null;

    private StackPane sidebarsPane = null;
    private StackPane otherSidebarsPane = null;
    private VBox sidebarsPaneWrapper = null;
    private VBox otherSidebarsPaneWrapper = null;
    private SplitPane parentPane = null;
    private Node content = null;
    private VBox notifications = null;

    private EditorsSideBar editorsSideBar = null;

    private SimpleStringProperty layoutProp = null;

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

        this.getStyleClass ().add (StyleClassNames.VIEWER);
        this.setFillWidth (true);

        this.notifications = new VBox ();

        this.getChildren ().add (notifications);
        VBox.setVgrow (this.notifications,
                       Priority.NEVER);

        this.parentPane = new SplitPane ();

        this.sidebarsPane = new StackPane ();
        this.sidebarsPane.getStyleClass ().add (StyleClassNames.SIDEBARS);
        this.sidebarsPane.setVisible (false);
        this.sidebarsPaneWrapper = new VBox ();
        this.sidebarsPaneWrapper.getChildren ().add (this.sidebarsPane);

        this.otherSidebarsPane = new StackPane ();
        this.otherSidebarsPane.getStyleClass ().add (StyleClassNames.OTHERSIDEBARS);
        this.otherSidebarsPane.setVisible (false);
        this.otherSidebarsPaneWrapper = new VBox ();
        this.otherSidebarsPaneWrapper.getChildren ().add (this.otherSidebarsPane);

        this.getChildren ().add (this.parentPane);
        VBox.setVgrow (this.parentPane,
                       Priority.ALWAYS);

        try
        {

            this.tips = new Tips (this);

        } catch (Exception e) {

            Environment.logError ("Unable to init tips",
                                  e);

        }

        this.initActionMappings ();

        this.updateForDebugMode ();

        // We set the value in init which triggers the layout.
        this.layoutProp = new SimpleStringProperty ();

        // When the layout property changes, update the layout.
        this.layoutProp.addListener ((prop, oldv, newv) -> this.updateLayout ());

        // When we update our user property, update the layout property.
        // TODO Have a better bind.
        UserProperties.addListener (ev ->
        {

            this.layoutProp.setValue (ev.getProperty ().getValue ());

        });

    }

    public void setContent (Node n)
    {

        if (this.content != null)
        {

            this.parentPane.getItems ().remove (this.content);

        }

        SplitPane.setResizableWithParent (n, true);
        this.parentPane.getItems ().add (n);
        this.content = n;

        // TODO Update for the sidebars...

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
     * Run the specified command, the command must map to one of the enum values.
     *
     * @param command The command to run.
     * @param doAfter An optional runnable to execute after the command has completed.
     */
    public void runCommand (String   command,
                            Runnable doAfter)
    {

        this.runCommand (Command.valueOf (command));

    }

    /**
     * Run the specified command.
     *
     * @param command The command to run.
     */
    public void runCommand (Command  command)
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
    public void runCommand (Command  command,
                            Runnable doAfter)
    {

        Runnable r = this.actionMap.get (command);

        if (r == null)
        {

            throw new IllegalArgumentException (String.format ("Command %1$s is not supported.", command.toString ()));

        }

        Runnable _r = new Runnable ()
        {

            @Override
            public void run ()
            {

                // TODO: Check error handling, maybe wrap in a try/catch.
                r.run ();

                if (doAfter != null)
                {

                    doAfter.run ();

                }

            }

        };

        UIUtils.runLater (_r);

    }

    protected void addActionMapping (Runnable   action,
                                     Command... commands)
    {

        for (Command c : commands)
        {

            this.actionMap.put (c,
                                action);

        }

    }

    protected void addKeyMapping (Command           command,
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

        this.addKeyMapping (Command.debug,
                            KeyCode.F12, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (Command.debugmode,
                            KeyCode.F1, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (Command.whatsnew,
                            KeyCode.F11);
        this.addKeyMapping (Command.showoptions,
                            KeyCode.F3);
        this.addKeyMapping (Command.vieweditors,
                            KeyCode.F7);
        this.addKeyMapping (Command.dowarmup,
                            KeyCode.F10);

    }

    private void initActionMappings ()
    {

        final AbstractViewer _this = this;

        this.addActionMapping (() -> this.showWarmupPromptSelect (),
                               Command.dowarmup,
                               Command.warmup);
        this.addActionMapping (() -> new DebugConsole (_this),
                               Command.debug);
        this.addActionMapping (() ->
        {

            Environment.setDebugModeEnabled (!Environment.isDebugModeEnabled ());

            _this.updateForDebugMode ();

            // Add a notification.
            _this.addNotification (getUILanguageStringProperty (debugmode, Environment.isDebugModeEnabled () ? enabled : disabled),
                                   Constants.BUG_ICON_NAME,
                                   10);

        },
        Command.debugmode);

        this.addActionMapping (() ->
        {

			UserProperties.set (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
								"0");

            _this.showWhatsNew (true);

        },
        Command.whatsnew);

        this.addActionMapping (() -> this.showContactSupport (),
                               Command.contactsupport,
                               Command.contact);
        this.addActionMapping (() -> _this.showObjectTypeNameChanger (),
                               Command.editobjectnames);
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
        Command.vieweditors,
        Command.editors,
        Command.contacts,
        Command.showundealtwitheditormessages);

        this.addActionMapping (() -> _this.showReportProblem (),
                               Command.reportbug);

        this.addActionMapping (() -> _this.showAbout (),
                               Command.about);

        this.addActionMapping (() -> _this.showDictionaryManager (),
                               Command.dictionarymanager);

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

        return new State ();

    }

    /**
     * Init and show the viewer.
     */
    @Override
    public void init (State s)
               throws GeneralException
    {

        this.getViewer ().init (s);

        this.layoutProp.setValue (this.getUILayout ());

        Environment.doNewsAndVersionCheck (this);

        this.handleWhatsNew ();

        this.handleShowTips ();

        this.show ();

    }

    public void close (Runnable afterClose)
    {

        this.notifications.setVisible (false);

        this.generalTimer.shutdown ();

        this.close ();

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
if (true)
{
    /// xxx
    return;
}
        String layout = this.layoutProp.getValue ();

        // Legacy, pre-2.3
        if (layout.equals (Constants.LEFT))
        {

            layout = Constants.LAYOUT_PS_CH;

        }

        if (layout.equals (Constants.RIGHT))
        {

            layout = Constants.LAYOUT_CH_PS;

        }

        this.parentPane.getItems ().removeAll ();

        if (layout.equals (Constants.LAYOUT_PS_CH_OS))
        {

            // Add the main sidebar, the content and the other sidebar (if present).
            this.parentPane.getItems ().addAll (this.sidebarsPaneWrapper, this.content);

            if (other != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPaneWrapper);

            }

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_OS_CH_PS))
        {

            if (other != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPaneWrapper);

            }

            this.parentPane.getItems ().addAll (this.sidebarsPaneWrapper, this.content);

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_PS_OS_CH))
        {

            this.parentPane.getItems ().add (this.sidebarsPaneWrapper);

            if (other != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPaneWrapper);

            }

            this.parentPane.getItems ().add (this.content);

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_CH_OS_PS))
        {

            this.parentPane.getItems ().add (this.content);

            if (other != null)
            {

                this.parentPane.getItems ().add (this.otherSidebarsPaneWrapper);

            }

            this.parentPane.getItems ().add (this.sidebarsPaneWrapper);

            // Update the divider locations?

        }

        if (layout.equals (Constants.LAYOUT_PS_CH))
        {

            this.parentPane.getItems ().addAll (this.sidebarsPaneWrapper, this.content);

        }

        if (layout.equals (Constants.LAYOUT_CH_PS))
        {

            this.parentPane.getItems ().addAll (this.content, this.sidebarsPaneWrapper);

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

        this.notifications.getChildren ().remove (n);

        if (this.notifications.getChildren ().size () == 0)
        {

            this.notifications.setVisible (false);

        }

    }

    public void addNotification (Notification n)
    {

        this.notifications.getChildren ().add (0,
                                               n);

        this.notifications.setVisible (true);

        n.init ();

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
        Text t = new Text ();
        t.textProperty ().bind (text);
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
                                                        () ->
                                                        {

                                                              _this.fireProjectEvent (ProjectEvent.Type.tips,
                                                                                      ProjectEvent.Action.off);

           													  UserProperties.set (Constants.SHOW_TIPS_PROPERTY_NAME,
        													  					  false);

                                                              _this.removeNotification (n);

                                                         },
                                                         off);

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

        // TODO

    }

    public void showContactSupport ()
    {

        // TODO

    }

    public void showDictionaryManager ()
    {

        // TODO

    }

    public void showAbout ()
    {

        // TODO

    }

    public void showObjectTypeNameChanger ()
    {

        // TODO

    }

    public void showWarmupPromptSelect ()
    {

        // TODO

    }

    public void showReportProblem ()
    {

        this.showReportProblem (null);

    }

    public void showReportProblem (String bugText)
    {

        // TODO

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

        java.util.List<String> prefix = Arrays.asList (project, LanguageStrings.title,toolbar,buttons);

        if (control == HeaderControl.reportbug)
        {

            return QuollButton.builder ()
                .tooltip (prefix,bug,tooltip)
                .styleClassName (StyleClassNames.BUG)
                .onAction (ev ->
                {

                    this.runCommand (Command.reportbug);

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

                    this.runCommand (Command.dowarmup);

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

                        if ((_this.isSideBarVisible (EditorsSideBar.ID))
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

                        this.runCommand (Command.contacts);

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

    public void showOptions (String section)
    {

        // TODO

    }

    //public abstract boolean showOptions (String section);

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

            this.currentOtherSideBar.fireEvent (new SideBar.SideBarEvent (this,
                                                                          this.currentOtherSideBar,
                                                                          SideBar.SideBarEvent.SHOW_EVENT));

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

            this.activeSideBars.remove (b);

            this.activeSideBars.push (b);

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
        b.fireEvent (new SideBar.SideBarEvent (this,
                                               b,
                                               SideBar.SideBarEvent.HIDE_EVENT));

        if (doAfterView != null)
        {

            UIUtils.runLater (doAfterView);

        }

    }

    public int getActiveSideBarCount ()
    {

        int c = this.activeSideBars.size ();

        String l = this.layoutProp.getValue ();

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

        return c;

    }

    public ContextMenu getShowOtherSideBarsSelector ()
    {

        final AbstractViewer _this = this;

        ContextMenu m = new ContextMenu ();

        String l = this.layoutProp.getValue ();

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

        this.showSideBar (this.getMainSideBarId ());

    }

    public String getMainSideBarId ()
    {

        if (this.mainSideBar == null)
        {

            this.mainSideBar = this.getMainSideBar ();

        }

        return this.mainSideBar.getSideBarId ();

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

        }

        this.currentOtherSideBar = null;

        this.showMainSideBar ();

    }

    public SideBar getSideBar (String id)
    {

        return this.sideBars.get (id);

    }

    public void removeSideBar (String id)
    {

        this.removeSideBar (this.getSideBar (id));

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

    public void addSideBar (SideBarContent sb)
    {

        this.addSideBar (sb.getSideBar ());

    }

    public void addSideBar (SideBar sb)
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

/* TODO Do when creating sidebar.
        String state = null;

        String id = sb.getSiId ();

        if (id != null)
        {

            state = this.proj.getProperty ("sidebarState-" + id);

        }
*/
        // TODO Do when creating sidebar... sb.init (state);

        //sb.setName (name);

        this.sideBars.put (sb.getSideBarId (),
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

      this.runCommand (Command.vieweditors,
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

      this.showSideBar (EditorsSideBar.ID);

      return true;

    }

    public void viewEditor (final EditorEditor ed)
                     throws GeneralException
    {

        final AbstractViewer _this = this;

        this.runCommand (Command.vieweditors,
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

    public void addToSidebarWrapper (Side where,
                                     Node node)
    {

        if (where == Side.TOP)
        {

            this.sidebarsPaneWrapper.getChildren ().add (0,
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

    public Viewer getViewer ()
    {

        if (this.viewer == null)
        {

            this.viewer = this.createViewer ();

        }

        return this.viewer;

    }

    @Override
    public Viewer createViewer ()
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

        headerCons.add (QuollButton.builder ()
            .styleClassName (StyleClassNames.CONTEXTMENU)
            .tooltip (prefix,projectmenu,tooltip)
            .onAction (evv ->
            {

                ContextMenu menu = new ContextMenu ();

                Supplier<Set<MenuItem>> menuItemSupp = _this.getSettingsMenuSupplier ();

                Set<MenuItem> items = null;

                if (menuItemSupp != null)
                {

                     items = menuItemSupp.get ();

                 }

                if (items != null)
                {

                    menu.getItems ().addAll (items);

                    if (items.size () > 0)
                    {

                        menu.getItems ().add (new SeparatorMenuItem ());

                    }

                }

                List<String> mprefix = Arrays.asList (LanguageStrings.project,settingsmenu,LanguageStrings.items);

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,options)
                    .styleClassName (StyleClassNames.OPTIONS)
                    .onAction (ev ->
                    {

                        _this.runCommand (Command.options);

                    })
                    .build ());

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,achievements)
                    .styleClassName (StyleClassNames.ACHIEVEMENTS)
                    .onAction (ev ->
                    {

                        _this.runCommand (Command.viewachievements);

                    })
                    .build ());

                menu.getItems ().add (new SeparatorMenuItem ());

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,whatsnew)
                    .styleClassName (StyleClassNames.WHATSNEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (Command.whatsnew);

                    })
                    .build ());

                QuollMenu helpMenu = QuollMenu.builder ()
                    .label (mprefix,help)
                    .styleClassName (StyleClassNames.HELP)
                    .build ();

                menu.getItems ().add (helpMenu);

                // Report Bug/Problem
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,reportbug)
                    .styleClassName (StyleClassNames.REPORTBUG)
                    .onAction (ev ->
                    {

                        _this.runCommand (Command.reportbug);

                    })
                    .build ());

                // Contact Support
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,contactsupport)
                    .styleClassName (StyleClassNames.CONTACTSUPPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (Command.contactsupport);

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
                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,about)
                    .styleClassName (StyleClassNames.ABOUT)
                    .onAction (ev ->
                    {

                        _this.runCommand (Command.about);

                    })
                    .build ());

                if (Environment.isDebugModeEnabled ())
                {

                    // Debug Console
                    menu.getItems ().add (QuollMenuItem.builder ()
                        .label ("Debug Console")
                        .styleClassName (StyleClassNames.DEBUGCONSOLE)
                        .onAction (ev ->
                        {

                            _this.runCommand (Command.debugconsole);

                        })
                        .build ());

                }

                menu.show ((Node) evv.getSource (), Side.BOTTOM, 0, 0);

            })
            .build ());

        Viewer v = Viewer.builder ()
            .headerControls (headerCons)
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

    public void close ()
    {

        this.getViewer ().close ();

    }

    public void show ()
    {

        //this.getViewer ().sizeToScene ();

        this.getViewer ().show ();

    }

}
