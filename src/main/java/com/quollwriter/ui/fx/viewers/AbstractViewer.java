package com.quollwriter.ui.fx.viewers;

import java.net.*;

import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.beans.value.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.UserConfigurableObjectType;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.tips.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.PropertyBinder;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractViewer extends VBox implements ViewerCreator,
                                                             Stateful,
                                                             PopupsViewer,
                                                             URLActionHandler,
                                                             IPropertyBinder
{

    public enum HeaderControl
    {
        contacts,
        reportbetabug,
        reportbug,
        dowarmup,
        fullscreen,
        find,
        close,
        wordcounttimer,
        help,
        tryout,
        submit,
        statistics,
        targets,
        openproject,
        newproject,
        nightmode,
        timer,
        onlynovalue,
        onlyerrors,
        filter,
        hideonescape
    };

    public interface HeaderControlButtonIds
    {

        String help = "help";
        String context = "context";
        String bug = "bug";
        String find = "find";
        String fullscreenenter = "fullscreenenter";
        String dowarmup = "dowarmup";
        String close = "close";
        String contacts = "contacts";
        String statistics = "statistics";
        String targets = "targets";
        String newproject = "newproject";
        String nightmode = "nightmode";
        String openproject = "openproject";

    }

    // Using an interface to reduce typing :)
    public interface CommandId
    {

        String close = "close";
        String cssviewer = "cssviewer";
        String exitfullscreen = "exitfullscreen";
        String showfullscreenheader = "showfullscreenheader";
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
        String viewobject = "viewobject";
        String editobject = "editobject";
        String deleteobject = "deleteobject";
        String nightmode = "nightmode";
        String newuserobject = "newuserobject";
        String managenotetypes = "managenotetypes";
        String moveup = "moveup";
        String movedown = "movedown";
        String openproject = "openproject";

    }

    private Viewer viewer = null;
    private ObjectProperty<Header> headerProp = null;
    private ScheduledThreadPoolExecutor generalTimer = null;
    private AchievementsPopup achievementsPopup = null;

    private Map<String, Command> actionMap = new HashMap<> ();
    private Tips tips = null;

    private Map<String, Object> tempOptions = new HashMap<> ();

    private Map<String, SideBar> sideBars = new HashMap<> ();
    private Stack<SideBar>  activeSideBars = new Stack<> ();
    private ObjectProperty<SideBar> currentOtherSideBarProp = null;
    private SideBar       mainSideBar = null;
    private BooleanProperty inFullScreenModeProp = null;

    private Set<QuollPopup> popups = new HashSet<> ();

    private Set<ProjectEventListener> projectEventListeners = new HashSet<> ();
    private boolean ignoreProjectEvents = false;

    private Pane popupPane = null;

    private PropertyBinder binder = new PropertyBinder ();

    private Content currentContent = null;
    private BooleanProperty distractionFreeModeProp = null;
    private Content windowedContent = null;
    private Content fullScreenContent = null;
    private State fullScreenState = null;

    public AbstractViewer ()
    {

        final AbstractViewer _this = this;

        this.inFullScreenModeProp = new SimpleBooleanProperty (false);
        this.distractionFreeModeProp = new SimpleBooleanProperty (false);

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

        this.popupPane = new Pane ();
        this.popupPane.getStyleClass ().add (StyleClassNames.POPUPPANE);
        VBox.setVgrow (this.popupPane,
                       Priority.ALWAYS);

        this.currentOtherSideBarProp = new SimpleObjectProperty<> ();

        this.getChildren ().add (this.popupPane);

        try
        {

            this.tips = new Tips (this);

        } catch (Exception e) {

            Environment.logError ("Unable to init tips",
                                  e);

        }

        this.updateForDebugMode ();

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

    public QuollMenuButton createViewerMenuButton ()
    {

        AbstractViewer _this = this;

        List<String> prefix = Arrays.asList (project, LanguageStrings.title,toolbar,buttons);

        QuollMenuButton context = QuollMenuButton.builder ()
            .iconName (StyleClassNames.VIEWERMENU)
            .tooltip (prefix,projectmenu,tooltip)
            .buttonId (HeaderControlButtonIds.context)
            .items (() ->
            {

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
                    .iconName (StyleClassNames.OPTIONS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.options);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (mprefix,achievements)
                    .iconName (StyleClassNames.ACHIEVEMENTS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.viewachievements);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (mprefix,(Environment.isNightModeEnabled () ? disablenightmode : enablenightmode))
                    .iconName ((Environment.isNightModeEnabled () ? StyleClassNames.SUN : StyleClassNames.MOON))
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.nightmode);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (mprefix,whatsnew)
                    .iconName (StyleClassNames.WHATSNEW)
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
                    .iconName (StyleClassNames.REPORTBUG)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.reportbug);

                    })
                    .build ());

                // Contact Support
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,contactsupport)
                    .iconName (StyleClassNames.CONTACTSUPPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.contactsupport);

                    })
                    .build ());

                // View the User Guide
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,viewuserguide)
                    .iconName (StyleClassNames.VIEWUSERGUIDE)
                    .onAction (ev ->
                    {

                        UIUtils.openURL (_this,
                                         "help:getting-started");

                    })
                    .build ());

                // Keyboard shortcuts
                helpMenu.getItems ().add (QuollMenuItem.builder ()
                    .label (mprefix,keyboardshortcuts)
                    .iconName (StyleClassNames.KEYBOARDSHORTCUTS)
                    .onAction (ev ->
                    {

                        UIUtils.openURL (_this,
                                         "help:keyboard-shortcuts");

                    })
                    .build ());

                // About Quoll Writer
                items.add (QuollMenuItem.builder ()
                    .label (mprefix,about)
                    .iconName (StyleClassNames.ABOUT)
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
                        .iconName (StyleClassNames.DEBUGCONSOLE)
                        .onAction (ev ->
                        {

                            _this.runCommand (AbstractViewer.CommandId.debugconsole);

                        })
                        .build ());

                }

                return items;

            })
            .build ();

        context.setOnShown (ev ->
        {

            UIUtils.addShowCSSViewerFilter (context.getContextMenu ());

        });

        return context;

    }

    public static abstract class Content<E extends AbstractViewer> extends Pane implements Stateful,
                                                                                           NotificationViewer,
                                                                                           SideBarViewer,
                                                                                           Disposable
    {

        protected E viewer = null;

        public Content (E viewer)
        {

            this.viewer = viewer;

        }

        public abstract void updateLayout ();

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.binder;

    }

    public void dispose ()
    {

        this.binder.dispose ();

    }

    public void closeAllPopups ()
    {

        Set<QuollPopup> ps = new HashSet<> (this.popups);

        ps.stream ()
            .forEach (p -> p.close ());

    }

    @Override
    public QuollPopup getPopupById (String id)
    {

        return this.popups.stream ()
            .filter (p -> p.getPopupId () != null && p.getPopupId ().equals (id))
            .findFirst ()
            .orElse (null);

    }

    /**
     * This expects the Bounds to be in local Viewer coords.
     */
    public void showPopup (QuollPopup p,
                           Bounds     b,
                           Side       s)
    {

        p.setVisible (false);
        this.addPopup (p);

        p.applyCss ();
        p.requestLayout ();

        UIUtils.runLater (() ->
        {

            double _x = b.getMinX ();
            double _y = b.getMinY ();

            Bounds nb = p.getLayoutBounds ();

            //Bounds b = this.getLayoutBounds ();

            if (s == Side.TOP)
            {

                _x = b.getMinX ();
                _y = b.getMinY () - nb.getHeight ();

            }

            if (s == Side.BOTTOM)
            {

                _x = b.getMinX ();
                _y = b.getMaxY ();

            }

            if (s == Side.LEFT)
            {

                _x = b.getMinX () - nb.getWidth ();
                _y = b.getMinY ();

            }

            if (s == Side.RIGHT)
            {

                _x = b.getMaxX ();
                _y = b.getMinY ();

            }

            this.showPopup (p,
                            _x,
                            _y);

        });

    }

    @Override
    public void showPopup (QuollPopup p,
                           Node       n,
                           Side       s)
    {

        if (n == null)
        {

            this.showPopup (p);
            return;

        }

        n.applyCss ();

        ((Region) n).requestLayout ();

        Bounds b = n.localToScreen (n.getBoundsInLocal ());
        b = this.popupPane.screenToLocal (b);

        double x = b.getMinX ();
        double y = b.getMinY ();
        if (s == Side.BOTTOM)
        {

            y += b.getHeight () + 2;

        }

        if (s == Side.TOP)
        {

            y -= b.getHeight () - p.getHeight () - 2;

        }

        if (s == Side.LEFT)
        {

            x -= b.getWidth () - p.getWidth () - 2;

        }

        if (s == Side.RIGHT)
        {

            x += b.getWidth () + 2;

        }

        this.showPopup (p,
                        x,
                        y);

    }

    public void showPopup (QuollPopup p,
                           Node       n,
                           double     offsetX,
                           double     offsetY,
                           Side       s)
    {

        if (n == null)
        {

            this.showPopup (p);
            return;

        }

        Bounds b = n.localToScreen (n.getBoundsInLocal ());
        b = this.popupPane.screenToLocal (b);

        p.setVisible (false);
        this.addPopup (p);
        this.showPopup (p,
                        b.getMinX () + offsetX,
                        b.getMaxY () + offsetY);

    }

    /**
     * Shows a popup in the middle of the parent window.
     */
    public void showPopup (QuollPopup p)
    {

        p.setVisible (false);
        this.addPopup (p);
        p.applyCss ();

        p.requestLayout ();

        UIUtils.forceRunLater (() ->
        {

            Bounds nb = p.getLayoutBounds ();

            Bounds b = this.getLayoutBounds ();
            this.showPopup (p,
                            ((b.getWidth () - nb.getWidth ()) / 2),
                            ((b.getHeight () - nb.getHeight ()) / 2));

        });

    }

    public Point2D screenToPopupLocal (double x,
                                       double y)
    {

        return this.popupPane.screenToLocal (x,
                                             y);

    }

    @Override
    public void showPopup (QuollPopup p,
                           double     x,
                           double     y)
    {

        if ((x == -1)
            &&
            (y == -1)
           )
        {

            this.showPopup (p);
            return;

        }

        p.setVisible (false);
        this.addPopup (p);
        p.applyCss ();
        p.requestLayout ();

        UIUtils.forceRunLater (() ->
        {

            double _x = x;
            double _y = y;

            Bounds nb = p.getLayoutBounds ();
            nb = p.getBoundsInParent ();

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

            p.fireEvent (new QuollPopup.PopupEvent (p,
                                                    QuollPopup.PopupEvent.SHOWN_EVENT));

        });

    }

    @Override
    public void removePopup (QuollPopup p)
    {

        p.setVisible (false);
        this.popups.remove (p);
        this.popupPane.getChildren ().remove (p);

    }

    @Override
    public void addPopup (QuollPopup p)
    {

        this.popups.add (p);

        //p.setManaged (false);

        if (!this.popupPane.getChildren ().contains (p))
        {

            this.popupPane.getChildren ().add (p);

        }

    }

    public void setTitle (StringProperty t)
    {

        this.getViewer ().titleProperty ().unbind ();
        this.getViewer ().titleProperty ().bind (t);

    }

    /**
     * Run the specified command, the command must map to one of the enum values.
     *
     * @param command The command to run.
     */
    public void runCommand (String   command)
    {

        this.runCommand (command,
                         (Runnable) null);

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

    public <E> void runCommand (String   command,
                                E...     args)
    {

        this.runCommand (command,
                         null,
                         args);

    }

    public <E> void runCommand (String   command,
                                Runnable doAfter,
                                E...     args)
    {

        Command c = this.actionMap.get (command);

        if (c == null)
        {

            throw new IllegalArgumentException (String.format ("Command %1$s is not supported.", command));

        }

        if (c instanceof CommandWithArgs)
        {

            CommandWithArgs v = (CommandWithArgs) c;

            // TODO Check error handling.
            v.run (doAfter,
                   args);

        }

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

    public void addKeyMapping (String           command,
                                  KeyCode           code,
                                  KeyCombination.Modifier... modifiers)
    {

        this.addKeyMapping (() -> runCommand (command),
                            code,
                            modifiers);

    }

    public void addKeyMapping (Runnable          action,
                                  KeyCode           code,
                                  KeyCombination.Modifier... modifiers)
    {

        Scene s = this.getScene ();

        KeyCodeCombination cc = new KeyCodeCombination (code,
                                                        modifiers);

        if (s.getAccelerators ().containsKey (cc))
        {

            throw new IllegalArgumentException ("Already have an action mapped to: " + cc);

        }

        s.getAccelerators ().put (cc,
                                  action);

    }

    private void initKeyMappings ()
    {

        this.getScene ().addEventHandler (KeyEvent.KEY_RELEASED,
                                          ev ->
        {

            if (ev.getCode () == KeyCode.ESCAPE)
            {

                int s = this.popupPane.getChildren ().size ();

                if (s > 0)
                {

                    QuollPopup p = null;

                    for (int i = s - 1; i > -1; i--)
                    {

                        Node n = this.popupPane.getChildren ().get (i);

                        if (n instanceof QuollPopup)
                        {

                            p = (QuollPopup) n;

                            break;

                        }

                    }

                    if (p != null)
                    {

                        if (p.isHideOnEscape ())
                        {

                            p.close ();
                            return;

                        }

                    }

                }

                this.closeSideBar ();

            }

        });

        this.addKeyMapping (CommandId.debugconsole,
                            KeyCode.F12, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (CommandId.debugmode,
                            KeyCode.F1, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (CommandId.cssviewer,
                            KeyCode.F2, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
        this.addKeyMapping (CommandId.showoptions,
                            KeyCode.F3);
        this.addKeyMapping (CommandId.vieweditors,
                            KeyCode.F7);
        this.addKeyMapping (CommandId.dowarmup,
                            KeyCode.F10);
        this.addKeyMapping (CommandId.closepopup,
                            KeyCode.F4);
        this.addKeyMapping (CommandId.fullscreen,
                            KeyCode.F5);
        this.addKeyMapping (CommandId.fullscreen,
                            KeyCode.F11);
        this.addKeyMapping (CommandId.nightmode,
                            KeyCode.F8);
        this.addKeyMapping (CommandId.showfullscreenheader,
                            KeyCode.F6);
        this.addKeyMapping (CommandId.exitfullscreen,
                            KeyCode.F9);
        this.addKeyMapping (CommandId.resetfontsize,
                            KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.incrementfontsize,
                            KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.decrementfontsize,
                            KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.moveup,
                            KeyCode.UP, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.movedown,
                            KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN);

    }

    public void handleHideOnEscape (KeyEvent ev)
    {



    }

    private void initActionMappings ()
    {

        final AbstractViewer _this = this;

        this.addActionMapping (() ->
        {

            this.close (null);

        },
        CommandId.close);

        this.addActionMapping (() ->
        {

            Environment.showAllProjectsViewer ();

        },
        CommandId.openproject);

        this.addActionMapping (() ->
        {

            this.fireEvent (new KeyboardNavigationEvent (KeyboardNavigationEvent.MOVE_UP_EVENT));

        },
        CommandId.moveup);

        this.addActionMapping (() ->
        {

            this.fireEvent (new KeyboardNavigationEvent (KeyboardNavigationEvent.MOVE_DOWN_EVENT));

        },
        CommandId.movedown);

        this.addActionMapping (() ->
        {
            Label l = new Label ();
            // This is an assumption that may not hold.
            UserProperties.setUIBaseFontSize ((double) l.getFont ().getSize () * (double) (72d/96d));

        },
        CommandId.resetfontsize);

        this.addActionMapping (() ->
        {

            if ((!UserProperties.getAsBoolean (Constants.SEEN_ASKED_NIGHT_MODE_ENABLE_PERMANENT_PROPERTY_NAME))
                &&
                (!Environment.nightModeProperty ().get ())
               )
            {

                QuollPopup.questionBuilder ()
                    .inViewer (this)
                    .title (getUILanguageStringProperty (nightmode,popup,title))
                    .message (getUILanguageStringProperty (nightmode,popup,description))
                    .confirmButtonLabel (getUILanguageStringProperty (nightmode,popup,buttons,confirm))
                    .cancelButtonLabel (getUILanguageStringProperty (nightmode,popup,buttons,cancel))
                    .headerIconClassName (StyleClassNames.MOON)
                    .onConfirm (ev ->
                    {

                        UserProperties.set (Constants.NIGHT_MODE_ENABLE_PERMENANTLY_PROPERTY_NAME,
                                            true);

                    })
                    .build ();

                UserProperties.set (Constants.SEEN_ASKED_NIGHT_MODE_ENABLE_PERMANENT_PROPERTY_NAME,
                                    true);

            }

            Environment.setNightModeEnabled (!Environment.nightModeProperty ().get ());

        },
        CommandId.nightmode);

        this.addActionMapping (() ->
        {

            // TODO

        },
        CommandId.newuserobject);

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

        this.addActionMapping (() ->
        {

            this.exitFullScreen ();

        },
        CommandId.exitfullscreen);

        this.addActionMapping (() ->
        {

            try
            {

                this.showOptions (null);

            } catch (Exception e) {

                Environment.logError ("Unable to show the options.",
                                      e);

            }

        },
        CommandId.options);

        this.addActionMapping (() -> this.showWarmupPromptSelect (),
                               CommandId.dowarmup,
                               CommandId.warmup);
        this.addActionMapping (() -> this.showDebugConsole (),
                               CommandId.debugconsole);
        this.addActionMapping (() ->
        {

            Environment.setDebugModeEnabled (!Environment.isDebugModeEnabled ());

            _this.updateForDebugMode ();

            // Add a notification.
            _this.addNotification (getUILanguageStringProperty (debugmode, Environment.isDebugModeEnabled () ? enabled : disabled),
                                   StyleClassNames.BUG,
                                   10);

        },
        CommandId.debugmode);

        this.addActionMapping (() ->
        {

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
        this.addActionMapping (() -> this.showManageNoteTypes (),
                               CommandId.managenotetypes);

    }

    public Command getActionMapping (String id)
    {

        return this.actionMap.get (id);

    }

    @Override
    public void handleURLAction (String     v,
                                 MouseEvent ev)
    {

        try
        {

            if (v.equals ("reportbug"))
            {

                this.runCommand (AbstractViewer.CommandId.reportbug);
                return;

            }

            if ((v.equals ("import"))
                ||
                (v.equals ("importfile"))
               )
            {

                this.runCommand (AbstractViewer.CommandId.importfile);
                return;

            }

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

        s.set ("windowed",
               this.windowedContent.getState ());

        if (this.fullScreenContent != null)
        {

            s.set ("fullscreen",
                   this.fullScreenState);

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

        // Get our windowed content here so that everything is already setup and ready to be shown.
        this.windowedContent = this.getWindowedContent ();

        this.windowedContent.prefWidthProperty ().bind (this.popupPane.widthProperty ());
        this.windowedContent.prefHeightProperty ().bind (this.popupPane.heightProperty ());

        this.popupPane.getChildren ().add (this.windowedContent);

        if (s != null)
        {

            this.windowedContent.init (s.getAsState ("windowed"));

            this.fullScreenState = s.getAsState ("fullscreen");

        }

        this.currentContent = this.windowedContent;

        this.getViewer ().init (s);

        this.initActionMappings ();
        this.initKeyMappings ();

        Environment.doNewsAndVersionCheck (this);

        this.handleWhatsNew ();

        this.handleShowTips ();

        // We show later to ensure that the init has worked.
        Environment.registerViewer (this);

        this.updateLayout ();

        UIUtils.runLater (() ->
        {

            this.show ();

        });

    }

    public void close (Runnable afterClose)
    {

        if (this.currentContent != null)
        {

            this.currentContent.dispose ();

        }

        this.generalTimer.shutdown ();

        this.dispose ();

        this.getViewer ().close ();

        this.viewer = null;

        UIUtils.runLater (afterClose);

    }

    public boolean isClosed ()
    {

        return this.viewer == null && this.generalTimer.isShutdown ();

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

        if (this.currentContent == null)
        {

            return;

        }

        this.currentContent.updateLayout ();

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

        this.currentContent.removeAllNotifications ();

    }

    public void removeNotification (Notification n)
    {

        this.currentContent.removeNotification (n);

    }

    public void addNotification (Notification n)
    {

        n.init ();

        this.currentContent.addNotification (n);

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

        QuollTextView t = QuollTextView.builder ()
            .inViewer (this)
            .text (text)
            .build ();
        t.setOnMouseClicked (clickListener);

        return this.addNotification (t,
                                     type,
                                     duration);

    }

    public void showNotificationPopup (StringProperty title,
                                       StringProperty message,
                                       int            showFor)
    {

        Node m = null;

        if (message != null)
        {

            m = BasicHtmlTextFlow.builder ()
                .styleClassName (StyleClassNames.MESSAGE)
                .text (message)
                .build ();

        }

        QuollPopup popup = QuollPopup.messageBuilder ()
            .title (title)
            .styleClassName (StyleClassNames.INFORMATION)
            .message (m)
            .closeButton ()
            .withClose (true)
            .removeOnClose (true)
            .noAutoShow ()
            .build ();

        if (m != null)
        {

            m.setOnMousePressed (ev ->
            {

                popup.close ();

            });

        }

        this.showPopup (popup,
                        10,
                        10);

        if (showFor > 0)
        {

            ScheduledFuture t = this.schedule (() ->
            {

                popup.close ();

            },
            showFor * Constants.SEC_IN_MILLIS,
            -1);

            popup.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                                   ev ->
            {

                t.cancel (true);

            });

        }

    }

    private void handleWhatsNew ()
    {

        // TODO
        boolean showWhatsNew = false;

        String whatsNewVersion = UserProperties.get (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);

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

                //java.nio.file.Path imgDir = java.nio.file.Paths.get ("d:/development/github/quollwriterv3/src/main/resources/imgs/");

                Function<String, String> formatter = text ->
                {

                    int ind = text.indexOf ("[");

                    while (ind > -1)
                    {

                        int end = text.indexOf ("]",
                                                ind + 1);

                        if (end < 0)
                        {

                            ind = text.indexOf ("[",
                                                ind + 1);

                            continue;

                        }

                        if (end > ind + 1)
                        {

                            String v = text.substring (ind + 1,
                                                       end);

                            StringTokenizer st = new StringTokenizer (v,
                                                                      ",;");

                            String icon = st.nextToken ().trim ().toLowerCase ();
                            URL u = null;

                            try
                            {

                                u = Utils.getResourceUrl (Constants.IMGS_DIR + icon + "16.png");

                            } catch (Exception e) {

                                Environment.logError ("Unable to get url for: " + icon,
                                                      e);

                            }

                            if (u != null)
                            {

                                v = String.format ("<img class='icon' src='%1$s' />",
                                                   u.toString (),
                                                   icon);

                                // Split up the value.
                                text = text.substring (0,
                                                       ind) + v + text.substring (end + 1);

                                ind = text.indexOf ("[",
                                                    ind + v.length ());

                            }

                        }

                    }

                    return text;

                };

                StringProperty tipText = this.tips.getNextTip ();

                VBox textc = new VBox ();

                QuollTextView text = QuollTextView.builder ()
                    .text (tipText)
                    .inViewer (this)
                    .formatter (formatter)
                    .build ();
                textc.getChildren ().add (text);

                QuollButton next = QuollButton.builder ()
                    .iconName (StyleClassNames.NEXT)
                    .tooltip (tipspanel,LanguageStrings.next,tooltip)
                    .buttonType (ButtonBar.ButtonData.NEXT_FORWARD)
                    .build ();

                QuollButton off = QuollButton.builder ()
                    .iconName (StyleClassNames.STOP)
                    .tooltip (tipspanel,stop,tooltip)
                    .buttonType (ButtonBar.ButtonData.FINISH)
                    .build ();

                Notification n = Notification.builder ()
                    .styleName (StyleClassNames.TIPS)
                    .content (textc)
                    .duration (90)
                    .withControl (next)
                    .withControl (off)
                    .inViewer (this)
                    .build ();

                this.addNotification (n);

                next.setOnAction (ev ->
                {

                    StringProperty t = this.tips.getNextTip ();

                    if (t != null)
                    {

                        textc.getChildren ().clear ();
                        QuollTextView ntext = QuollTextView.builder ()
                            .text (t)
                            .inViewer (this)
                            .formatter (formatter)
                            .build ();
                        textc.getChildren ().add (ntext);

                        n.restartTimer ();

                        this.fireProjectEvent (ProjectEvent.Type.tips,
                                               ProjectEvent.Action.show);

                    }

                });

                off.setOnAction (ev ->
                {

                    String pid = "tipsstop";

                    if (this.getPopupById (pid) != null)
                    {

                        return;

                    }

                    QuollPopup.questionBuilder ()
                        .styleClassName (StyleClassNames.STOP)
                        .popupId (pid)
                        .title (tipspanel,stop,popup, LanguageStrings.title)
                        .confirmButtonLabel (getUILanguageStringProperty (tipspanel,stop,popup,buttons,confirm))
                        .cancelButtonLabel (getUILanguageStringProperty (tipspanel,stop,popup,buttons,cancel))
                        .message (getUILanguageStringProperty (tipspanel,stop,popup,LanguageStrings.text))
                        .withViewer (this)
                        .onConfirm (fev ->
                        {

                              this.fireProjectEvent (ProjectEvent.Type.tips,
                                                     ProjectEvent.Action.off);

                              UserProperties.set (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                  false);

                              this.removeNotification (n);

                              this.getPopupById (pid).close ();

                         })
                        .build ();

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

    public ScheduledFuture scheduleImmediately (Runnable r)
    {

        return this.schedule (r,
                              0,
                              -1);

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

            return;

        }

    }

    public void showContactSupport ()
    {

        new ContactSupportPopup (this).show ();

    }

    public void showManageNoteTypes ()
    {

        QuollPopup qp = this.getPopupById (NoteTypesManager.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        try
        {

            new NoteTypesManager (this).show ();

        } catch (Exception e) {

            Environment.logError ("Unable to show note types manager",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (notetypes,manage,actionerror));

        }

    }

    public void showDictionaryManager ()
    {

        QuollPopup qp = this.getPopupById (DictionaryManager.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        try
        {

            new DictionaryManager (this,
                                   new UserDictionaryProvider ()).show ();

        } catch (Exception e) {

            Environment.logError ("Unable to show dictionary manager",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (dictionary,manage,actionerror));

        }

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
        QuollPopup qp = this.getPopupById (ObjectTypeNameChangePopup.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        new ObjectTypeNameChangePopup (this).show ();

    }

    public void showDebugConsole ()
    {

        QuollPopup qp = this.getPopupById (DebugConsolePopup.POPUP_ID);

        if (qp != null)
        {

            qp.show ();
            qp.toFront ();
            return;

        }

        new DebugConsolePopup (this).show ();

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

        // TODO Should allow for multiple?
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

        if (control == HeaderControl.help)
        {

            return QuollButton.builder ()
                .tooltip (prefix,help,tooltip)
                .iconName (StyleClassNames.HELP)
                .buttonId (HeaderControlButtonIds.help)
                .onAction (ev ->
                {

                    UIUtils.openURL (_this,
                                     "help:getting-started");

                })
                .build ();

        }

        if (control == HeaderControl.nightmode)
        {

            QuollButton qb = QuollButton.builder ()
                .tooltip (prefix,(Environment.isNightModeEnabled () ? disablenightmode : enablenightmode),tooltip)
                .iconName ((Environment.isNightModeEnabled () ? StyleClassNames.SUN : StyleClassNames.MOON))
                .buttonId (HeaderControlButtonIds.nightmode)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.nightmode);

                })
                .build ();

            this.getBinder ().addChangeListener (Environment.nightModeProperty (),
                                                 (pr, oldv, newv) ->
            {

                qb.setIconName (Environment.isNightModeEnabled () ? StyleClassNames.SUN : StyleClassNames.MOON);
                UIUtils.setTooltip (qb,
                                    getUILanguageStringProperty (Utils.newList (prefix,(Environment.isNightModeEnabled () ? disablenightmode : enablenightmode),tooltip)));

            });

            return qb;

        }

        if (control == HeaderControl.newproject)
        {

            return QuollButton.builder ()
                .tooltip (prefix,newproject,tooltip)
                .iconName (StyleClassNames.NEW)
                .buttonId (HeaderControlButtonIds.newproject)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.newproject);

                })
                .build ();

        }

        if (control == HeaderControl.openproject)
        {

            return QuollButton.builder ()
                .tooltip (prefix,openproject,tooltip)
                .iconName (StyleClassNames.OPEN)
                .buttonId (HeaderControlButtonIds.openproject)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.openproject);

                })
                .build ();

        }

        if (control == HeaderControl.statistics)
        {

            return QuollButton.builder ()
                .tooltip (prefix,statistics,tooltip)
                .iconName (StyleClassNames.STATISTICS)
                .buttonId (HeaderControlButtonIds.statistics)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.statistics);

                })
                .build ();

        }

        if (control == HeaderControl.targets)
        {

            return QuollButton.builder ()
                .tooltip (prefix,targets,tooltip)
                .iconName (StyleClassNames.TARGETS)
                .buttonId (HeaderControlButtonIds.targets)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.targets);

                })
                .build ();

        }

        if (control == HeaderControl.reportbug)
        {

            return QuollButton.builder ()
                .tooltip (prefix,bug,tooltip)
                .iconName (StyleClassNames.BUG)
                .buttonId (HeaderControlButtonIds.bug)
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
                .iconName (StyleClassNames.FIND)
                .buttonId (HeaderControlButtonIds.find)
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
                .iconName (StyleClassNames.FULLSCREENENTER)
                .buttonId (HeaderControlButtonIds.fullscreenenter)
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
                .iconName (StyleClassNames.WARMUP)
                .buttonId (HeaderControlButtonIds.dowarmup)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.dowarmup);

                })
                .build ();

        }

        if (control == HeaderControl.close)
        {

            return QuollButton.builder ()
                .tooltip (prefix,close,tooltip)
                .iconName (StyleClassNames.CLOSE)
                .buttonId (HeaderControlButtonIds.close)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.close);

                })
                .build ();

        }

        if (control == HeaderControl.contacts)
        {

            String type = LanguageStrings.showcontacts;

            if (!EditorsEnvironment.hasRegistered ())
            {

                type = LanguageStrings.editorsserviceregister;

            }

            return QuollButton.builder ()
                .tooltip (prefix,type,tooltip)
                .iconName (StyleClassNames.CONTACTS)
                .buttonId (HeaderControlButtonIds.contacts)
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

        throw new IllegalArgumentException ("Header control: " + control + ", not supported.");

    }

    public abstract Set<FindResultsBox> findText (String t);

    public abstract Content getWindowedContent ();
    public abstract Content getFullScreenContent ();

    public abstract String getStyleClassName ();

    public abstract Supplier<Set<MenuItem>> getSettingsMenuSupplier ();

    public abstract Supplier<Set<Node>> getTitleHeaderControlsSupplier ();

    public abstract StringProperty titleProperty ();

    public final SideBar getMainSideBar ()
    {

        return this.mainSideBar;

    }

    public abstract void showOptions (String sect)
                               throws GeneralException;

    public abstract void deleteAllObjectsForType (UserConfigurableObjectType t)
                                           throws GeneralException;

    public boolean isSideBarVisible (String id)
    {

        if ((this.currentOtherSideBarProp.getValue () != null)
            &&
            (this.currentOtherSideBarProp.getValue ().getSideBarId ().equals (id))
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

    public String getCurrentOtherSideBarId ()
    {

        if (this.currentOtherSideBarProp.getValue () != null)
        {

            return this.currentOtherSideBarProp.getValue ().getSideBarId ();

        }

        return null;

    }

    public void showSideBar (String   id,
                             Runnable doAfterView)
    {

        if ((this.currentOtherSideBarProp.getValue () != null)
            &&
            (this.currentOtherSideBarProp.getValue ().getSideBarId ().equals (id))
           )
        {

            this.currentOtherSideBarProp.getValue ().setVisible (true);
            this.currentOtherSideBarProp.getValue ().fireEvent (new SideBar.SideBarEvent (this,
                                                                                          this.currentOtherSideBarProp.getValue (),
                                                                                          SideBar.SideBarEvent.SHOW_EVENT));

            if (this.fullScreenContent != null)
            {

                this.fullScreenContent.showSideBar (this.currentOtherSideBarProp.getValue ());

            }

            return;

        }

        SideBar b = this.sideBars.get (id);

        if (b == null)
        {

            throw new IllegalArgumentException ("Unable to show sidebar: " +
                                                id +
                                                ", no sidebar found with that id.");

        }

        if (this.currentOtherSideBarProp.getValue () != null)
        {

            this.currentOtherSideBarProp.getValue ().setVisible (false);
            this.currentOtherSideBarProp.getValue ().fireEvent (new SideBar.SideBarEvent (this,
                                                                                          this.currentOtherSideBarProp.getValue (),
                                                                                          SideBar.SideBarEvent.HIDE_EVENT));

        }

        if (id.equals (this.getMainSideBarId ()))
        {

            // Need to check the layout.  If we are only showing one sidebar then set the current other
            // to null.
            if (this.isUILayoutShowSingleSidebar ())
            {

                this.currentOtherSideBarProp.setValue (null);

            }

        } else {

            this.currentOtherSideBarProp.setValue (b);
            this.currentOtherSideBarProp.getValue ().setVisible (true);

            this.activeSideBars.remove (b);

            this.activeSideBars.push (b);

            this.currentOtherSideBarProp.getValue ().fireEvent (new SideBar.SideBarEvent (this,
                                                                                          this.currentOtherSideBarProp.getValue (),
                                                                                          SideBar.SideBarEvent.SHOW_EVENT));

        }

        this.currentOtherSideBarProp.setValue (b);

        this.currentContent.showSideBar (b);

        //this.updateLayout ();

        if (doAfterView != null)
        {

            UIUtils.runLater (doAfterView);

        }

    }

    public ObjectProperty<SideBar> currentOtherSideBarProperty ()
    {

        return this.currentOtherSideBarProp;

    }

    public SideBar getCurrentOtherSideBar ()
    {

        return this.currentOtherSideBarProp.getValue ();

    }

    public int getActiveSideBarCount ()
    {

        int c = this.activeSideBars.size ();

        return c;

    }

    public ContextMenu getShowOtherSideBarsSelector ()
    {

        final AbstractViewer _this = this;

        ContextMenu m = new ContextMenu ();

        String l = UserProperties.uiLayoutProperty ().getValue ();

        if (this.mainSideBar != null)
        {

            QuollMenuItem mi = QuollMenuItem.builder ()
                .label (this.mainSideBar.activeTitleProperty ())
                .iconName (this.mainSideBar.getStyleClassName ())
                .onAction (ev ->
                {

                    _this.showMainSideBar ();

                })
                .build ();
            // TODO mi.setGraphic (new ImageView (this.mainSideBar.getHeader ().getIcon ().getImage ()));
            //mi.setGraphic (this.mainSideBar.getHeader ().getIcon ());

            m.getItems ().add (mi);

        }

        // Means we are showing the main sidebar and the other sidebar.
        // Exclude those from the list.
        for (SideBar sb : this.activeSideBars)
        {

            if ((this.currentOtherSideBarProp.getValue () != null)
                &&
                (this.currentOtherSideBarProp.getValue () == sb)
               )
            {

                continue;

            }

            final SideBar _sb = sb;

            QuollMenuItem mi = QuollMenuItem.builder ()
                .label (sb.activeTitleProperty ())
                .iconName (sb.getStyleClassName ())
                .onAction (ev ->
                {

                    _this.showSideBar (_sb.getSideBarId ());

                })
                .build ();
            // TODO mi.setGraphic (new ImageView (sb.getHeader ().getIcon ().getImage ()));

            m.getItems ().add (mi);

        }

        return m;

    }

    public Set<MenuItem> getShowOtherSideBarsMenuItems ()
    {

        Set<MenuItem> its = new LinkedHashSet<> ();

        String l = UserProperties.uiLayoutProperty ().getValue ();

        if (this.mainSideBar != null)
        {

            QuollMenuItem mi = QuollMenuItem.builder ()
                .label (this.mainSideBar.activeTitleProperty ())
                .iconName (this.mainSideBar.getStyleClassName ())
                .onAction (ev ->
                {

                    this.showMainSideBar ();

                })
                .build ();
            // TODO mi.setGraphic (new ImageView (this.mainSideBar.getHeader ().getIcon ().getImage ()));
            //mi.setGraphic (this.mainSideBar.getHeader ().getIcon ());
            its.add (mi);

        }

        // Means we are showing the main sidebar and the other sidebar.
        // Exclude those from the list.
        for (SideBar sb : this.activeSideBars)
        {

            if ((this.currentOtherSideBarProp.getValue () != null)
                &&
                (this.currentOtherSideBarProp.getValue () == sb)
               )
            {

                continue;

            }

            final SideBar _sb = sb;

            QuollMenuItem mi = QuollMenuItem.builder ()
                .label (sb.activeTitleProperty ())
                .iconName (sb.getStyleClassName ())
                .onAction (ev ->
                {

                    this.showSideBar (sb.getSideBarId ());

                })
                .build ();
            // TODO mi.setGraphic (new ImageView (sb.getHeader ().getIcon ().getImage ()));
            its.add (mi);

        }

        return its;

    }

    public void showMainSideBar ()
    {

        if (this.getMainSideBarId () != null)
        {

            this.showSideBar (this.getMainSideBarId ());

        }

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

        if (this.currentOtherSideBarProp.getValue () != null)
        {

            this.currentOtherSideBarProp.getValue ().fireEvent (new SideBar.SideBarEvent (this,
                                                                                          this.currentOtherSideBarProp.getValue (),
                                                                                          SideBar.SideBarEvent.HIDE_EVENT));

            this.currentOtherSideBarProp.getValue ().setVisible (false);

        }

        this.currentOtherSideBarProp.setValue (null);

        this.currentContent.showSideBar (this.currentOtherSideBarProp.getValue ());

        this.updateLayout ();

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

        if (sb == this.mainSideBar)
        {

            this.mainSideBar = null;
            this.updateLayout ();

        }

        if (this.currentOtherSideBarProp.getValue () == sb)
        {

            this.currentOtherSideBarProp.setValue (null);

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

        this.updateLayout ();

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

    public boolean isEditorsSideBarVisible ()
    {

        if (this.currentOtherSideBarProp.getValue () == null)
        {

            return false;

        }

        if (this.currentOtherSideBarProp.getValue ().getSideBarId ().equals (EditorsSideBar.SIDEBAR_ID))
        {

            return true;

        }

        return false;

    }

  public void sendMessageToEditor (final EditorEditor ed)
                            throws GeneralException
  {

      final AbstractViewer _this = this;

      this.runCommand (AbstractViewer.CommandId.vieweditors,
                       () ->
      {

          try
          {

              this.getEditorsSideBar ().showChatBox (ed);

          } catch (Exception e) {

              Environment.logError ("Unable to show editor: " +
                                    ed,
                                    e);

              ComponentUtils.showErrorMessage (_this,
                                                getUILanguageStringProperty (editors,editor,view,actionerror));
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

              EditorsUIUtils.showRegister (this);

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

      if (this.getEditorsSideBar () == null)
      {

          this.addSideBar (new EditorsSideBar (this));

      }

      this.showSideBar (EditorsSideBar.SIDEBAR_ID);

      return true;

    }

    private EditorsSideBar getEditorsSideBar ()
    {

        SideBar sb = this.getSideBarById (EditorsSideBar.SIDEBAR_ID);

        if ((sb != null)
            &&
            (sb.getContent () instanceof EditorsSideBar)
           )
        {

            return (EditorsSideBar) sb.getContent ();

        }

        return null;

    }

    public void viewEditor (final EditorEditor ed)
                     throws GeneralException
    {

        this.runCommand (AbstractViewer.CommandId.vieweditors,
                         () ->
        {

            try
            {

                this.getEditorsSideBar ().showEditor (ed);

            } catch (Exception e) {

                Environment.logError ("Unable to show editor: " +
                                      ed,
                                      e);

                ComponentUtils.showErrorMessage (this,
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

        if (this.viewer != null)
        {

            return this.viewer;

        }

        Viewer v = Viewer.builder ()
            //.headerControls (headerCons)
            //.headerToolbar (ctb)
            .styleClassName (this.getStyleClassName ())
            .content (this)
            .title (this.titleProperty ())
            .build ();

        v.setOnCloseRequest (ev ->
        {

            ev.consume ();
            this.close (null);

        });

        v.setResizable (true);

        v.init (null);

        this.viewer = v;

        // Best place for this?
        v.getScene ().addEventFilter (javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                                      ev ->
        {

            if (!Environment.isDebugModeEnabled ())
            {

                return;

            }

            if (!ev.isShortcutDown ())
            {

                return;

            }

            try
            {

                ev.consume ();

                Environment.showCSSViewer (this,
                                           (Node) ev.getTarget ());

            } catch (Exception e) {

                Environment.logError ("Unable to show css viewer for node: " + ev.getTarget (),
                                      e);

            }

        });

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

        UIUtils.runLater (() ->
        {

            this.achievementsPopup.showAchievement (ar);

        });

    }

    public void removeProjectEventListener (ProjectEventListener l)
    {

        this.projectEventListeners.remove (l);

    }

    public void addProjectEventListener (ProjectEventListener l)
    {

        this.projectEventListeners.add (l);

    }

    public boolean hasTempOption (String name)
    {

        return this.getTempOption (name) != null;

    }

    public boolean isTempOption (String name)
    {

        Object o = this.getTempOption (name);

        if (o == null)
        {

            return false;

        }

        if (o instanceof Boolean)
        {

            return ((Boolean) o).booleanValue ();

        }

        return false;

    }

    public Object getTempOption (String name)
    {

        return this.tempOptions.get (name);

    }

    public void setTempOption (String name,
                               Object value)
    {

        this.tempOptions.put (name,
                              value);

    }

    public BooleanProperty distractionFreeModeProperty ()
    {

        return this.distractionFreeModeProp;

    }

    public boolean isDistractionFreeModeEnabled ()
    {

        return this.distractionFreeModeProp.getValue ();

    }

    public void setDistractionFreeModeEnabled (boolean v)
    {

        this.distractionFreeModeProp.setValue (v);

        UserProperties.set (Constants.FULL_SCREEN_ENABLE_DISTRACTION_FREE_MODE_WHEN_EDITING_PROPERTY_NAME,
                            v);

    }

    public boolean isInFullScreenMode ()
    {

        return this.inFullScreenModeProp.getValue ();

    }

    public BooleanProperty inFullScreenModeProperty ()
    {

        return this.inFullScreenModeProp;

    }

    public void exitFullScreen ()
    {

        if (!this.isInFullScreenMode ())
        {

            return;

        }

        this.closeAllPopups ();
        this.fullScreenState = this.fullScreenContent.getState ();
        this.fullScreenContent.setVisible (false);
        this.windowedContent.setVisible (true);
        this.currentContent = this.windowedContent;
        this.currentContent.updateLayout ();

        this.getViewer ().setFullScreen (false);
        this.inFullScreenModeProp.setValue (false);

        EditorsEnvironment.fullScreenExited ();
        this.fireEvent (new Viewer.ViewerEvent (this.viewer,
                                                Viewer.ViewerEvent.FULL_SCREEN_EXITED_EVENT));
        this.fireProjectEventLater (ProjectEvent.Type.fullscreen,
                                    ProjectEvent.Action.exit);

    }

    public void enterFullScreen ()
    {

        if (this.isInFullScreenMode ())
        {

            this.exitFullScreen ();
            return;

        }

        if (this.fullScreenContent == null)
        {

            this.fullScreenContent = this.getFullScreenContent ();

            if (this.fullScreenContent == null)
            {

                throw new IllegalStateException ("Unable to enter full screen, no full screen content available.");

            }

            this.fullScreenContent.prefWidthProperty ().bind (this.popupPane.widthProperty ());
            this.fullScreenContent.prefHeightProperty ().bind (this.popupPane.heightProperty ());
            this.popupPane.getChildren ().add (this.fullScreenContent);

        }

        this.getViewer ().setFullScreenExitHint ("");
        this.windowedContent.setVisible (false);

        try
        {

            this.fullScreenContent.init (this.fullScreenState);

        } catch (Exception e) {

            Environment.logError ("Unable to init full screen with state: " + this.fullScreenState,
                                  e);

        }

        this.currentContent = this.fullScreenContent;
        this.currentContent.updateLayout ();
        this.getViewer ().setFullScreen (true);

        this.fullScreenContent.setVisible (true);
        this.inFullScreenModeProp.setValue (true);

        EditorsEnvironment.fullScreenEntered ();

        this.fireProjectEventLater (ProjectEvent.Type.fullscreen,
                                    ProjectEvent.Action.enter);
        this.fireEvent (new Viewer.ViewerEvent (this.viewer,
                                                Viewer.ViewerEvent.FULL_SCREEN_ENTERED_EVENT));

    }

    public static class KeyboardNavigationEvent extends Event
    {

        public static final EventType<KeyboardNavigationEvent> MOVE_UP_EVENT = new EventType<> ("kbnav.moveup");
        public static final EventType<KeyboardNavigationEvent> MOVE_DOWN_EVENT = new EventType<> ("kbnav.movedown");

        public KeyboardNavigationEvent (EventType<KeyboardNavigationEvent> type)
        {

            super (type);

        }

    }

}
