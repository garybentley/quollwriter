package com.quollwriter.ui.fx.viewers;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;

import javafx.geometry.*;
import javafx.css.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class FullScreenView extends Stage implements PopupsViewer, URLActionHandler, IPropertyBinder
{

    public static final float DEFAULT_X_BORDER_WIDTH = (7f / 100f);
    public static final float DEFAULT_Y_BORDER_WIDTH = (7f / 100f);
    public static final float DEFAULT_BORDER_OPACITY = 0.7f;

    private float                xBorderWidth = -1f;
    private float                yBorderWidth = -1f;
    private float                borderOpacity = -1f;

    private AbstractProjectViewer viewer = null;
    private PanelContent panel = null;

    private BackgroundPane background = null;

    private Pane popupPane = null;
    private Header header = null;
    private HBox sidebarsPane = null;
    private HBox info = null;
    private Label chapWords = null;
    private Label clockLabel = null;
    private Label sessWords = null;
    private Label headerClockLabel = null;

    private ScheduledFuture headerHideTimer = null;
    private ScheduledFuture updater = null;

    private ScheduledFuture sidebarHideTimer = null;
    private SideBar currentSideBar = null;

    private SimpleDateFormat     clockFormat = null;
    private Set<QuollPopup> popups = new HashSet<> ();

    private PropertyBinder propertyBinder = null;

    public FullScreenView (AbstractProjectViewer viewer,
                           Set<Node>             headerCons,
                           PanelContent          p)
    {

        final FullScreenView _this = this;

        this.propertyBinder = new PropertyBinder ();

        this.viewer = viewer;

        double swidth = Screen.getPrimary ().getVisualBounds ().getWidth ();
        double sheight = Screen.getPrimary ().getVisualBounds ().getHeight ();

        ToolBar tb = new ToolBar ();

        this.headerClockLabel = QuollLabel.builder ()
            .styleClassName (StyleClassNames.CLOCK)
            .build ();
        this.headerClockLabel.setMinSize (Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

        List<String> prefix = Arrays.asList (fullscreen,title,toolbar,buttons);

        Button but = QuollButton.builder ()
            .styleClassName (StyleClassNames.DISTRACTIONFREEENTER)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,distractionfreemodeenter,tooltip)))
            .onAction (ev ->
            {

                this.viewer.setDistractionFreeModeEnabled (!this.viewer.isDistractionFreeModeEnabled ());

                UserProperties.set (Constants.FULL_SCREEN_ENABLE_DISTRACTION_FREE_MODE_WHEN_EDITING_PROPERTY_NAME,
                                    this.viewer.isDistractionFreeModeEnabled ());

            })
            .build ();

        this.propertyBinder.addChangeListener (this.viewer.distractionFreeModeProperty (),
                                               (pr, oldv, newv) ->
        {

            but.getStyleClass ().remove (StyleClassNames.DISTRACTIONFREEENTER);
            but.getStyleClass ().remove (StyleClassNames.DISTRACTIONFREEEXIT);

            but.getStyleClass ().add (newv ? StyleClassNames.DISTRACTIONFREEEXIT : StyleClassNames.DISTRACTIONFREEENTER);

            UIUtils.setTooltip (but,
                                getUILanguageStringProperty (fullscreen,distractionfreemode, newv ? exit : enter));

        });

        tb.getItems ().addAll (this.headerClockLabel, but);

        if (headerCons != null)
        {

            tb.getItems ().addAll (headerCons);

        }

        if (EditorsEnvironment.isEditorsServiceAvailable ())
        {

            tb.getItems ().add (QuollButton.builder ()
                .styleClassName (StyleClassNames.CONTACTS)
                .tooltip (EditorsEnvironment.hasRegistered () ?
                            getUILanguageStringProperty (Utils.newList (prefix,showcontacts,tooltip))
                            :
                            getUILanguageStringProperty (Utils.newList (prefix,editorsserviceregister,tooltip)))
                .onAction (ev ->
                {

                    try
                    {

                        this.viewer.runCommand (AbstractViewer.CommandId.contacts);

                    } catch (Exception e) {

                        Environment.logError ("Unable to view editors",
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         this,
                                                         getUILanguageStringProperty (editors,vieweditorserror));
                                                  //"Unable to show the {editors}.");

                    }

                })
                .build ());

        }

        tb.getItems ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.FIND)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,find,tooltip)))
            .onAction (ev ->
            {

                this.viewer.runCommand (AbstractViewer.CommandId.find);

            })
            .build ());

        tb.getItems ().add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,fullscreenexit,tooltip)))
            .styleClassName (StyleClassNames.FULLSCREENEXIT)
            .onAction (ev ->
            {

                this.close ();

            })
            .build ());

        tb.getItems ().add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,editproperties,tooltip)))
            .styleClassName (StyleClassNames.EDITPROPERTIES)
            .onAction (ev ->
            {

                this.showProperties ();

            })
            .build ());

        this.header = Header.builder ()
            .toolbar (tb)
            .styleClassName (StyleClassNames.HEADER)
            .build ();
        this.header.setVisible (false);
        this.header.managedProperty ().bind (this.header.visibleProperty ());
        this.header.titleProperty ().bind (viewer.getProject ().nameProperty ());
        this.header.setPrefWidth (swidth * 0.8);

        this.header.setOnMouseEntered (ev ->
        {

            if (this.headerHideTimer != null)
            {

                this.headerHideTimer.cancel (true);

            }

        });

        this.header.setOnMouseExited (ev ->
        {

            this.hideHeader ();

        });

        Pane box = new Pane ();
        box.getStyleClass ().add (StyleClassNames.FULLSCREEN);
        VBox.setVgrow (this.header,
                       Priority.NEVER);

        this.background = new BackgroundPane (viewer);
        this.background.relocate (0, 0);
        this.background.setPrefSize (swidth, sheight);

        box.setOnContextMenuRequested (ev ->
        {

            ContextMenu m = new ContextMenu ();

            m.getItems ().add (QuollMenuItem.builder ()
                .label (fullscreen,popupmenu,items,editproperties)
                .styleClassName (StyleClassNames.EDITPROPERTIES)
                .onAction (eev ->
                {

                    this.viewer.runCommand (AbstractProjectViewer.CommandId.textproperties);

                })
                .build ());

            m.setAutoHide (true);

            m.show (box, ev.getScreenX (), ev.getScreenY ());

        });

        this.addEventHandler (ScrollEvent.SCROLL,
                              ev ->
        {

            if (ev.isShiftDown ())
            {

                if (ev.isControlDown ())
                {

                    if (ev.getDeltaX () > 0)
                    {

                        this.viewer.runCommand (AbstractViewer.CommandId.incrementfontsize);

                    } else {

                        this.viewer.runCommand (AbstractViewer.CommandId.decrementfontsize);

                    }

                } else {

                    // Ctrl NOT down.
                    double o = _this.background.getOpacity ();

                    if (ev.getDeltaX () > 0)
                    {

                        o += 0.05;

                    } else {

                        o -= 0.05;

                    }

                    if (o < 0)
                    {

                        o = 0.05;

                    }

                    if (o > 1)
                    {

                        o = 1;

                    }

                    _this.background.setOpacity (o);

                }

            }

            ev.consume ();

        });

/*
        VBox.setVgrow (b.content,
                       Priority.ALWAYS);
*/
        // Get the current panel from the viewer and add to a full screen panel.

        this.popupPane = new Pane ();
        this.popupPane.getStyleClass ().add (StyleClassNames.POPUPPANE);
        this.background.relocate (0, 0);
        this.background.setPrefSize (swidth, sheight);

        this.sidebarsPane = new HBox ();
        this.sidebarsPane.getStyleClass ().add (StyleClassNames.SIDEBARS);
        this.sidebarsPane.pseudoClassStateChanged (StyleClassNames.LEFT_PSEUDO_CLASS, true);
        this.sidebarsPane.managedProperty ().bind (this.sidebarsPane.visibleProperty ());
        this.sidebarsPane.setVisible (false);
        DragResizer dr = DragResizer.makeResizable (this.sidebarsPane);
        dr.draggingProperty ().addListener ((pr, oldv, newv) ->
        {

            if (!newv)
            {
System.out.println ("HERE1");
                this.hideSideBar ();

            } else {
System.out.println ("HERE2");
                this.cancelHideSideBar ();

            }

        });

        this.sidebarsPane.addEventHandler (MouseEvent.MOUSE_EXITED,
                                           ev ->
        {

            this.hideSideBar ();

        });

        this.sidebarsPane.addEventHandler (MouseEvent.MOUSE_ENTERED,
                                           ev ->
        {

            if (this.sidebarHideTimer != null)
            {

                this.sidebarHideTimer.cancel (true);
                this.sidebarHideTimer = null;

            }

        });

        UIUtils.runLater (() ->
        {

            double h = Screen.getPrimary ().getVisualBounds ().getHeight ();
            this.sidebarsPane.relocate (0,
                                        h * 0.1);
            this.sidebarsPane.setPrefSize (-1,
                                           h * 0.8);
            this.sidebarsPane.setMaxSize (Region.USE_PREF_SIZE,
                                          Region.USE_PREF_SIZE);

        });

        this.info = new HBox ();
        this.info.getStyleClass ().add (StyleClassNames.INFORMATION);
        this.chapWords = QuollLabel.builder ()
            .styleClassName (StyleClassNames.CHAPTER)
            .build ();
        this.clockLabel = QuollLabel.builder ()
            .styleClassName (StyleClassNames.CLOCK)
            .build ();
        this.sessWords = QuollLabel.builder ()
            .styleClassName (StyleClassNames.SESSION)
            .build ();
        this.info.setVisible (false);
        this.info.getChildren ().addAll (this.clockLabel, this.sessWords, this.chapWords);

        UIUtils.runLater (() ->
        {

            this.info.relocate (10,
                                Screen.getPrimary ().getVisualBounds ().getHeight () - this.info.getLayoutBounds ().getHeight ());

        });

        box.getChildren ().addAll (this.background, this.header, this.sidebarsPane, this.info, /*this.content*/this.popupPane);

        javafx.scene.Scene s = new javafx.scene.Scene (box);

        this.setX (0);
        this.setY (0);
        this.setWidth (swidth);
        this.setHeight (sheight);
		this.setScene (s);
        this.setMinWidth (swidth);
        this.setMinHeight (sheight);
        this.setMaxWidth (swidth);
        this.setMaxHeight (sheight);
        this.sizeToScene ();

        this.propertyBinder.addSetChangeListener (Environment.getStyleSheets (),
                                                  ev ->
        {

            if (ev.wasAdded ())
            {

                this.addStyleSheet (ev.getElementAdded ());

            }

            if (ev.wasRemoved ())
            {

                this.removeStyleSheet (ev.getElementRemoved ());

            }

        });

        Environment.getStyleSheets ().stream ()
            .forEach (u ->
            {

                this.addStyleSheet (u);

            });

        s.setOnMouseMoved (ev ->
        {

            if (ev.getScreenX () <= 20)
            {

                this.showSideBar ();

            }

            if (ev.getScreenY () <= 20)
            {

                this.showHeader ();

            }

        });

        this.propertyBinder.addChangeListener (UserProperties.uiBaseFontSizeProperty (),
                                               (pr, oldv, newv) ->
        {

            this.updateUIBaseFontSize ();

        });

        this.updateUIBaseFontSize ();

        // Listen to the night mode property, add a psuedo class when it is enabled.
        this.propertyBinder.addChangeListener (Environment.nightModeProperty (),
                                               (val, oldv, newv) ->
        {

            box.pseudoClassStateChanged (StyleClassNames.NIGHT_MODE_PSEUDO_CLASS, newv);

        });

        this.getIcons ().addAll (Environment.getWindowIcons ());

        this.initStyle (StageStyle.TRANSPARENT);

        // Hide the window by default, this allows subclasses to set things up.
        this.hide ();

        this.setOnCloseRequest (ev ->
        {

            ev.consume ();
            this.close ();

        });

        // TODO Make an option.
        this.clockFormat = new SimpleDateFormat ("h:mm a");

        this.updater = this.viewer.schedule (() ->
        {

            UIUtils.runLater (() ->
            {

                this.updateUI ();

            });

        },
        500,
        500);

        this.switchTo (p);

        this.addKeyMapping (() ->
        {

            this.close ();

        },
        KeyCode.F9);

        this.addKeyMapping (() ->
        {

            this.close ();

        },
        KeyCode.ESCAPE);

        this.addKeyMapping (() ->
        {

            this.viewer.runCommand (AbstractViewer.CommandId.resetfontsize);

        },
        KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN);

        this.addKeyMapping (() ->
        {

            this.viewer.runCommand (AbstractViewer.CommandId.incrementfontsize);

        },
        KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);

        this.addKeyMapping (() ->
        {

            this.viewer.runCommand (AbstractViewer.CommandId.decrementfontsize);

        },
        KeyCode.MINUS, KeyCombination.CONTROL_DOWN);

    }

    @Override
    public PropertyBinder getBinder ()
    {

        return this.propertyBinder;

    }

    private void cancelHideSideBar ()
    {

        if (this.sidebarHideTimer != null)
        {

            this.sidebarHideTimer.cancel (true);
            this.sidebarHideTimer = null;

        }

    }

    private void hideSideBar ()
    {

        this.cancelHideSideBar ();

        this.sidebarHideTimer = this.viewer.schedule (() ->
        {

            UIUtils.runLater (() ->
            {

                this.sidebarsPane.setVisible (false);

            });

        },
        750,
        -1);

    }

    public PanelContent getPanel ()
    {

        return this.panel;

    }

    private void updateUI ()
    {

        this.clockLabel.setText (this.clockFormat.format (new Date ()));
        this.headerClockLabel.setText (this.clockFormat.format (new Date ()));

        List<String> prefix = Arrays.asList (fullscreen,LanguageStrings.info);
        this.info.setVisible (UserProperties.getAsBoolean (Constants.FULL_SCREEN_SHOW_TIME_WORD_COUNT_PROPERTY_NAME));

        String t = "";

        TargetsData td = Environment.getUserTargets ();

        int sessWC = Environment.getSessionWordCount ();

        if ((td.getMySessionWriting () > 0)
            &&
            (sessWC != 0)
           )
        {

            int diff = sessWC - td.getMySessionWriting ();

            t = String.format (", %s%s",
                               (diff == 0 ? "" : (diff > 0 ? "+" : "")),
                               diff);

        }

        UIUtils.setTooltip (this.sessWords,
                            getUILanguageStringProperty (Utils.newList (prefix,sessionwordcount,tooltip)));
        //"Session word count");

        String pl = LanguageStrings.words;

        if (sessWC == 1)
        {

            pl = LanguageStrings.word;

        }

        this.sessWords.setText (String.format ("%s %s%s",
                                          Environment.formatNumber (sessWC),
                                          getUILanguageStringProperty (Utils.newList (prefix,pl)).getValue (),
                                          //(sessWC == 1 ? "" : "s"),
                                          t));

        chapWords.setVisible (false);

        if (this.panel instanceof NamedObjectPanelContent)
        {

            NamedObjectPanelContent pc = (NamedObjectPanelContent) this.panel;

            if (pc.getObject () instanceof Chapter)
            {

                Chapter c = (Chapter) pc.getObject ();
                this.chapWords.setVisible (true);

                ChapterCounts cc = this.viewer.getChapterCounts (c);

                int maxChapWC = this.viewer.getProjectTargets ().getMaxChapterCount ();

                t = "";

                if ((maxChapWC > 0)
                    &&
                    (cc.getWordCount () > maxChapWC)
                   )
                {

                    t = String.format (", %s",
                                       Environment.formatNumber (cc.getWordCount () - maxChapWC));

                }

                UIUtils.setTooltip (chapWords,
                                    getUILanguageStringProperty (Utils.newList (prefix,chapterwordcount,tooltip)));
                //"{Chapter} word count");

                pl = LanguageStrings.words;

                if (cc.getWordCount () == 1)
                {

                    pl = LanguageStrings.word;

                }

                this.chapWords.setText (String.format ("%s %s%s",
                                                  Environment.formatNumber (cc.getWordCount ()),
                                                  getUILanguageStringProperty (Utils.newList (prefix,pl)).getValue (),
                                                  t));

            }

        }

    }

    public void init ()
               throws GeneralException
    {

/*
        Project proj = this.viewer.getProject ();

        this.xBorderWidth = proj.getPropertyAsFloat (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME);

        if (this.xBorderWidth <= 0)
        {

            this.xBorderWidth = DEFAULT_X_BORDER_WIDTH;

        }

        this.yBorderWidth = proj.getPropertyAsFloat (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME);

        if (this.yBorderWidth <= 0)
        {

            this.yBorderWidth = DEFAULT_Y_BORDER_WIDTH;

        }

        this.borderOpacity = proj.getPropertyAsFloat (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME);
*/

        this.xBorderWidth = UserProperties.getAsFloat (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME);

        if (this.xBorderWidth <= 0)
        {

            this.xBorderWidth = DEFAULT_X_BORDER_WIDTH;

        }

        this.yBorderWidth = UserProperties.getAsFloat (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME);

        if (this.yBorderWidth <= 0)
        {

            this.yBorderWidth = DEFAULT_Y_BORDER_WIDTH;

        }

        this.borderOpacity = UserProperties.getAsFloat (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME);

        if (this.borderOpacity <= 0)
        {

            this.borderOpacity = DEFAULT_BORDER_OPACITY;

        }

        this.background.setOpacity (this.borderOpacity);

        Object bgObj = null;

        String b = UserProperties.get (Constants.FULL_SCREEN_BG_PROPERTY_NAME);

        if (b == null)
        {

            // Legacy < v2
            Path _f = UserProperties.getAsFile (Constants.FULL_SCREEN_BG_IMAGE_PROPERTY_NAME);
            File f = null;

            if (_f != null)
            {

                f = _f.toFile ();

            }

            if ((f != null)
                &&
                (f.exists ())
                &&
                (f.isFile ())
               )
            {

                bgObj = f;

            }

        } else {

            bgObj = b;

        }

        try
        {

            this.background.setBackgroundObject (bgObj);

        } catch (Exception e) {

            Environment.logError ("Unable to set background object to: " + bgObj,
                                  e);

        }

        this.titleProperty ().bind (this.viewer.getViewer ().titleProperty ());

        this.show ();

    }

    private void updateUIBaseFontSize ()
    {

        this.getScene ().getRoot ().setStyle (String.format ("-fx-font-size: %1$spt;",
                                                             UserProperties.getUIBaseFontSize ()));

        UIUtils.runLater (() ->
        {

             this.info.relocate (10,
                                 Screen.getPrimary ().getVisualBounds ().getHeight () - this.info.getLayoutBounds ().getHeight ());

        });

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
        b = this.popupPane.sceneToLocal (b);

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

            Bounds b = this.popupPane.getLayoutBounds ();

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

        if (!this.popupPane.getChildren ().contains (p))
        {

            this.popupPane.getChildren ().add (p);

        }

    }

    public void close ()
    {

        // Set the properties.
        try
        {

            UserProperties.set (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME,
                       this.xBorderWidth);
            UserProperties.set (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME,
                       this.yBorderWidth);
            UserProperties.set (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME,
                       (float) this.background.getOpacity ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to save user properties",
                                  e);

        }

        try
        {

            UserProperties.set (Constants.FULL_SCREEN_SIDEBAR_WIDTH_PROPERTY_NAME,
                                (int) this.sidebarsPane.getBoundsInLocal ().getWidth ());

            this.viewer.saveProject ();

        } catch (Exception e)
        {

            // Ignore.

        }

        this.propertyBinder.dispose ();

        this.updater.cancel (true);

        this.currentSideBar.prefWidthProperty ().unbind ();
        this.currentSideBar.prefHeightProperty ().unbind ();

        this.hide ();

        this.viewer.fullScreenClosed ();

        EditorsEnvironment.fullScreenExited ();

        this.restorePanel ();

        this.viewer.fireProjectEventLater (ProjectEvent.Type.fullscreen,
                                           ProjectEvent.Action.exit);

    }

    public void restorePanel ()
    {

        if (this.panel != null)
        {

            this.viewer.restoreFromFullScreen (this.panel);

        }

    }

    public void removeStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().remove (url.toExternalForm ());

    }

    public void addStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().add (url.toExternalForm ());

    }

    public void setDistractionFreeModeEnabled (boolean v)
    {

        this.setDistractionFreeModeEnabledForChildPanel (v);

/*
TODO ? psuedo class
        this.distModeButton.setIcon (Environment.getIcon ((this.distractionFreeModeEnabled ? Constants.DISTRACTION_FREE_EXIT_ICON_NAME : Constants.DISTRACTION_FREE_ICON_NAME),
                                     Constants.ICON_TITLE_ACTION));
*/

        // See if they have been in this mode before, if not then show the help popup.
        this.showFirstTimeInDistractionFreeModeInfoPopup ();

        if (v)
        {

            this.viewer.fireProjectEvent (ProjectEvent.Type.distractionfree,
                                          ProjectEvent.Action.enter);

        }

    }

    private void showFirstTimeInDistractionFreeModeInfoPopup ()
    {

        String propName = Constants.SEEN_FIRST_TIME_IN_DISTRACTION_FREE_MODE_INFO_POPUP_PROPERTY_NAME;

        if ((this.viewer.isDistractionFreeModeEnabled ())
            &&
            (this.panel instanceof ChapterEditorPanelContent)
            &&
            (!this.viewer.getProject ().getPropertyAsBoolean (propName))
           )
        {

            List<String> prefix = Arrays.asList (fullscreen,distractionfreemode,firsttimepopup);

            this.viewer.showNotificationPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                               getUILanguageStringProperty (Utils.newList (prefix,text)),
                                                      //"Since this is your first time using <i>Distraction Free Mode</i> it is recommended you spend a bit of time reading about how it works.  It may work in different ways to how you expect and/or how other writing applications implement similar modes.<br /><br /><a href='help:full-screen-mode/distraction-free-mode'>Click here to find out more</a>",
                                               -1);

            UserProperties.set (propName,
                                true);

            return;

        }

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

        QuollPopup popup = QuollPopup.builder ()
            .title (title)
            .styleClassName (StyleClassNames.INFORMATION)
            .content (m)
            .removeOnClose (true)
            .build ();

        if (m != null)
        {

            m.setOnMousePressed (ev ->
            {

                popup.close ();

            });

        }

        this.viewer.showPopup (popup,
                              10,
                              10);

        if (showFor > 0)
        {

            ScheduledFuture t = this.viewer.schedule (() ->
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

    private void setDistractionFreeModeEnabledForChildPanel (boolean v)
    {

        if (this.panel instanceof ChapterEditorPanelContent)
        {

            ChapterEditorPanelContent p = (ChapterEditorPanelContent) this.panel;

            // TODO             this.viewer.setUseTypewriterScrolling (v);


        }

        if (this.panel instanceof ProjectChapterEditorPanelContent)
        {

            ProjectChapterEditorPanelContent p = (ProjectChapterEditorPanelContent) this.panel;

            this.viewer.setUseTypewriterScrolling (v);
            p.showIconColumn (!v);

        }

    }

    public void switchTo (PanelContent p)
    {

        if (p instanceof AssetViewPanel)
        {

            UserConfigurableObjectType type = ((AssetViewPanel) p).getObject ().getUserConfigurableObjectType ();

            this.header.getIcon ().imageProperty ().unbind ();
            this.header.getIcon ().imageProperty ().bind (type.icon24x24Property ());

        } else {

            if (this.panel != null)
            {

                this.header.getStyleClass ().remove (this.panel.getPanel ().getStyleClassName ());
                this.header.getStyleClass ().add (p.getPanel ().getStyleClassName ());

            }

        }

        this.restorePanel ();

        this.panel = p;

        this.initPanel ();

        this.showHeader ();

        this.headerHideTimer = this.viewer.schedule (() ->
        {

            this.hideHeader ();

        },
        3000,
        -1);

    }

    private void initPanel ()
    {

        if (this.panel != null)
        {

            this.header.getStyleClass ().remove (this.panel.getPanel ().getStyleClassName ());

        }

        this.header.titleProperty ().bind (this.panel.getPanel ().titleProperty ());
        this.header.getStyleClass ().add (this.panel.getPanel ().getStyleClassName ());

    }

    private void hideHeader ()
    {

        this.header.setVisible (false);

        if (this.headerHideTimer != null)
        {

            this.headerHideTimer.cancel (true);
            this.headerHideTimer = null;

        }

    }

    private void showHeader ()
    {

        if (this.headerHideTimer != null)
        {

            this.headerHideTimer.cancel (false);
            this.headerHideTimer = null;

        }

        if (this.header.isVisible ())
        {

            return;

        }

        int w = (int) Screen.getPrimary ().getVisualBounds ().getWidth ();

        UIUtils.runLater (() ->
        {

            this.header.relocate (w * 0.1,
                                  0);
            this.header.setVisible (true);

        });

    }

    public void showProperties ()
    {

        try
        {

            this.viewer.runCommand (AbstractProjectViewer.CommandId.textproperties);

        } catch (Exception e) {

            Environment.logError ("Unable to show properties",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             this,
                                             getUILanguageStringProperty (fullscreen,actions,showproperties,actionerror));
                                      //"Unable to show properties.");

        }

    }

    @Override
    public void handleURLAction (String     v,
                                 MouseEvent ev)
    {

        this.viewer.handleURLAction (v,
                                     ev);

    }

    public void addKeyMapping (Runnable          action,
                                  KeyCode           code,
                                  KeyCombination.Modifier... modifiers)
    {

        javafx.scene.Scene s = this.getScene ();
        s.getAccelerators ().put (new KeyCodeCombination (code,
                                                          modifiers),
                                  action);

    }

    public void showSideBar ()
    {

        String cid = this.viewer.getCurrentOtherSideBarId ();

        if (cid == null)
        {

            cid = this.viewer.getMainSideBarId ();

        }

        this.showSideBar (cid,
                          null);

    }

    public void showSideBar (String   id,
                             Runnable doAfterView)
    {

        SideBar b = this.viewer.getSideBarById (id);

        if (b == null)
        {

            throw new IllegalArgumentException ("Unable to show sidebar: " +
                                                id +
                                                ", no sidebar found with that id.");

        }

        if (this.currentSideBar != null)
        {

            this.currentSideBar.prefWidthProperty ().unbind ();
            this.currentSideBar.prefHeightProperty ().unbind ();

        }

        this.sidebarsPane.getChildren ().clear ();
        ScrollPane c = new ScrollPane (b);
        HBox.setHgrow (c,
                       Priority.ALWAYS);
        this.sidebarsPane.getChildren ().add (c);
        b.prefWidthProperty ().bind (c.widthProperty ());
        b.prefHeightProperty ().bind (c.heightProperty ());

        this.currentSideBar = b;
        this.sidebarsPane.setVisible (true);

    }

    public SideBar getCurrentSideBar ()
    {

        return this.currentSideBar;

    }

}
