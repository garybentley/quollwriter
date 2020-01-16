package com.quollwriter.ui.fx.viewers;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;

import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.css.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.beans.binding.*;
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

public class ProjectFullScreenContent extends AbstractViewer.Content<AbstractProjectViewer>
{

    public static final float DEFAULT_BORDER_OPACITY = 0.7f;
    public static final double MIN_X_BORDER_WIDTH = 0.05d;
    public static final double MAX_X_BORDER_WIDTH = 0.4d;
    public static final double MIN_Y_BORDER_WIDTH = 0.05d;
    public static final double MAX_Y_BORDER_WIDTH = 0.4d;
    public static final double MOVE_INCR = 0.002d;

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

    private IPropertyBinder propertyBinder = null;
    private boolean ignoreChange = false;

    public ProjectFullScreenContent (AbstractProjectViewer viewer,
                                     String                styleClassName,
                                     Region                mainContent)
    {

        super (viewer);
        this.propertyBinder = viewer.getBinder ();

        this.propertyBinder.addChangeListener (this.viewer.currentPanelProperty (),
                                               (pr, oldv, newv) ->
        {

            if (oldv != null)
            {

                this.header.getStyleClass ().remove (oldv.getStyleClassName ());

            }

            if (newv != null)
            {

                PanelContent p = newv.getContent ();

                if (p instanceof AssetViewPanel)
                {

                    UserConfigurableObjectType type = ((AssetViewPanel) p).getObject ().getUserConfigurableObjectType ();

                    this.header.getIcon ().imageProperty ().unbind ();
                    this.header.getIcon ().imageProperty ().bind (type.icon24x24Property ());

                }

                this.header.titleProperty ().unbind ();
                this.header.titleProperty ().bind (newv.titleProperty ());
                this.header.getIcon ().imageProperty ().unbind ();
                this.header.getStyleClass ().add (newv.getStyleClassName ());

            }

            this.showHeader ();

            this.headerHideTimer = this.viewer.schedule (() ->
            {

                this.hideHeader ();

            },
            3000,
            -1);

        });

        //double swidth = Screen.getPrimary ().getVisualBounds ().getWidth ();
        //double sheight = Screen.getPrimary ().getVisualBounds ().getHeight ();

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
/*
        if (headerCons != null)
        {

            tb.getItems ().addAll (headerCons);

        }
*/
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

                        ComponentUtils.showErrorMessage (this.viewer,
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

        this.getStyleClass ().add (StyleClassNames.FULLSCREEN);
        VBox.setVgrow (this.header,
                       Priority.NEVER);

        this.background = new BackgroundPane (viewer);

        try
        {
System.out.println ("FBG: " + UserProperties.getFullScreenBackground ());
            this.background.setBackgroundObject (UserProperties.getFullScreenBackground ());

        } catch (Exception e) {

            Environment.logError ("Unable to set background to: " + UserProperties.getFullScreenBackground (),
                                  e);

        }

        this.background.opacityProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                this.ignoreChange = true;

                UserProperties.setFullScreenOpacity (newv.doubleValue ());

            } finally {

                this.ignoreChange = false;

            }

        });

        this.background.getBackgroundObject ().backgroundProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                this.ignoreChange = true;

                UserProperties.setFullScreenBackground (this.background.getBackgroundObject ().getBackgroundObject ());

            } finally {

                this.ignoreChange = false;

            }

        });

        this.propertyBinder.addChangeListener (UserProperties.fullScreenBackgroundProperty (),
                                               (pr, oldv, newv) ->
        {

            if (this.ignoreChange)
            {

                return;

            }

            try
            {

                this.background.setBackgroundObject (newv);

            } catch (Exception e) {

                Environment.logError ("Unable to set background object to: " + newv,
                                      e);

            }

        });

        this.propertyBinder.addChangeListener (UserProperties.fullScreenOpacityProperty (),
                                               (pr, oldv, newv) ->
        {

            if (this.ignoreChange)
            {

                return;

            }

            this.background.setOpacity (newv.doubleValue ());

        });

        this.background.setDragImportAllowed (true);
        this.background.prefWidthProperty ().bind (this.widthProperty ());
        this.background.prefHeightProperty ().bind (this.heightProperty ());
/*
        this.background.relocate (0, 0);
        this.background.setPrefSize (swidth, sheight);
*/
        this.setOnMouseClicked (ev ->
        {

            if (this.getProperties ().get ("context-menu") != null)
            {

                ((ContextMenu) this.getProperties ().get ("context-menu")).hide ();

            }

        });

        this.setOnContextMenuRequested (ev ->
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

            m.getItems ().add (QuollMenuItem.builder ()
                .label (fullscreen,popupmenu,items,selectbackground)
                .styleClassName (StyleClassNames.SELECTBG)
                .onAction (eev ->
                {

                    this.background.showBackgroundSelector ();

                })
                .build ());

            m.setAutoHide (true);

            this.getProperties ().put ("context-menu", m);

            m.setAutoFix (true);
            m.setAutoHide (true);
            m.setHideOnEscape (true);
            m.show (this,
                    ev.getScreenX (),
                    ev.getScreenY ());

        });
/*
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

                    this.background.setOpacity (o);

                }

            }

            ev.consume ();

        });
        */

/*
        VBox.setVgrow (b.content,
                       Priority.ALWAYS);
*/
        // Get the current panel from the viewer and add to a full screen panel.

        //this.popupPane = new Pane ();
        //this.popupPane.getStyleClass ().add (StyleClassNames.POPUPPANE);
        //this.background.relocate (0, 0);
        //this.background.setPrefSize (swidth, sheight);

        this.sidebarsPane = new HBox ();
        this.sidebarsPane.getStyleClass ().add (StyleClassNames.SIDEBARS);
        this.sidebarsPane.pseudoClassStateChanged (StyleClassNames.LEFT_PSEUDO_CLASS, true);
        this.sidebarsPane.managedProperty ().bind (this.sidebarsPane.visibleProperty ());
        this.sidebarsPane.setVisible (false);
        DragResizer dr = DragResizer.makeResizable (this.sidebarsPane,
                                                    Side.RIGHT);
        dr.newWidthProperty ().addListener ((pr, oldv, newv) ->
        {

            double mw = this.sidebarsPane.getMinWidth ();

            if (mw < 0)
            {

                mw = this.sidebarsPane.minWidth (this.sidebarsPane.getHeight ());

            }

            if (newv.doubleValue () > mw)
            {

                this.sidebarsPane.setPrefWidth (Math.round (newv.doubleValue ()) + this.sidebarsPane.getInsets ().getLeft () + this.sidebarsPane.getInsets ().getRight ());

            }

        });

        this.sidebarsPane.addEventHandler (MouseEvent.MOUSE_EXITED,
                                           ev ->
        {

            if ((this.sidebarsPane.localToScene (this.sidebarsPane.getBoundsInLocal ()).contains (this.sidebarsPane.localToScene (ev.getX (), ev.getY ())))
                ||
                (dr.isDragging ())
               )
            {

                return;

            }

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

        Pane mainWrapper = new Pane ();
        /*
        mainContent.prefWidthProperty ().bind (Bindings.createDoubleBinding (() ->
        {

            return (double) Math.round (this.getWidth () - (2 * this.getWidth () * this.xBorderWidthProp.getValue ()));

        },
        this.xBorderWidthProp));

        mainContent.prefHeightProperty ().bind (Bindings.createDoubleBinding (() ->
        {

            return (double) Math.round (this.getHeight () - (2 * this.getHeight () * this.yBorderWidthProp.getValue ()));

        },
        this.yBorderWidthProp));
*/

        ChangeListener<Number> resizeRelocate = (pr, oldv, newv) ->
        {

            double h = this.getHeight ();
            double w = this.getWidth ();
            double ybw = UserProperties.getFullScreenYBorderWidth ();
            double xbw = UserProperties.getFullScreenXBorderWidth ();
            double cx = w * xbw;
            double cy = h * ybw;

            mainContent.setPrefHeight ((double) Math.round (h - (2 * h * ybw)));
            mainContent.setPrefWidth ((double) Math.round (w - (2 * w * xbw)));
            mainWrapper.relocate (Math.round (cx), Math.round (cy));

        };

        this.propertyBinder.addChangeListener (UserProperties.fullScreenXBorderWidthProperty (),
                                               resizeRelocate);
        this.propertyBinder.addChangeListener (UserProperties.fullScreenYBorderWidthProperty (),
                                               resizeRelocate);

        mainContent.relocate (3, 3);
        mainWrapper.getChildren ().add (mainContent);
        mainWrapper.prefWidthProperty ().bind (Bindings.createDoubleBinding (() ->
        {

            return mainContent.prefWidthProperty ().getValue () + 6;

        },
        mainContent.prefWidthProperty ()));

        mainWrapper.prefHeightProperty ().bind (Bindings.createDoubleBinding (() ->
        {

            return mainContent.prefHeightProperty ().getValue () + 6;

        },
        mainContent.prefHeightProperty ()));

        mainWrapper.setVisible (false);

        DragResizer mdr = DragResizer.makeResizable (mainWrapper,
                                                     Side.TOP,
                                                     Side.BOTTOM,
                                                     Side.LEFT,
                                                     Side.RIGHT);
/*
TODO
        mdr.setOnDoubleClick (s ->
        {

            if ((s == Side.TOP)
                ||
                (s == Side.BOTTOM)
               )
            {

                double bw = this.yBorderWidthProp.getValue ();

                if ((bw >= MIN_Y_BORDER_WIDTH)
                   )
                {

                    this.yBorderWidthProp.setValue (MAX_Y_BORDER_WIDTH);

                }

                if (bw <= MAX_Y_BORDER_WIDTH)
                {

                    this.yBorderWidthProp.setValue (MIN_Y_BORDER_WIDTH);

                }

            }

        });
*/
        mdr.setOnDraggingLeft (() ->
        {

            if (mdr.getDragZone () == Side.RIGHT)
            {

                UserProperties.incrementFullScreenXBorderWidth ();

            }

            if (mdr.getDragZone () == Side.LEFT)
            {

                UserProperties.decrementFullScreenXBorderWidth ();

            }

        });

        mdr.setOnDraggingRight (() ->
        {

            if (mdr.getDragZone () == Side.RIGHT)
            {

                UserProperties.decrementFullScreenXBorderWidth ();

            }

            if (mdr.getDragZone () == Side.LEFT)
            {

                UserProperties.incrementFullScreenXBorderWidth ();

            }

        });

        mdr.setOnDraggingUp (() ->
        {

            if (mdr.getDragZone () == Side.TOP)
            {

                UserProperties.decrementFullScreenYBorderWidth ();

            }

            if (mdr.getDragZone () == Side.BOTTOM)
            {

                UserProperties.incrementFullScreenYBorderWidth ();

            }

        });

        mdr.setOnDraggingDown (() ->
        {

            if (mdr.getDragZone () == Side.TOP)
            {

                UserProperties.incrementFullScreenYBorderWidth ();

            }

            if (mdr.getDragZone () == Side.BOTTOM)
            {

                UserProperties.decrementFullScreenYBorderWidth ();

            }

        });

        UIUtils.runLater (() ->
        {
System.out.println ("TIME2: " + System.currentTimeMillis ());
long s= System.currentTimeMillis ();
            double h = this.getHeight ();//Screen.getPrimary ().getVisualBounds ().getHeight ();
            this.sidebarsPane.relocate (0,
                                        h * 0.1);
            this.sidebarsPane.setPrefSize (-1,
                                           h * 0.8);
            this.sidebarsPane.setMaxSize (Region.USE_PREF_SIZE,
                                          Region.USE_PREF_SIZE);

            this.info.relocate (10,
                                this.getHeight () - this.info.getLayoutBounds ().getHeight ());

            double w = this.getWidth ();
            double ybw = UserProperties.getFullScreenYBorderWidth ();
            double xbw = UserProperties.getFullScreenXBorderWidth ();
            double cx = w * xbw;
            double cy = h * ybw;

            mainContent.setPrefHeight ((double) Math.round (h - (2 * h * ybw)));
            mainContent.setPrefWidth ((double) Math.round (w - (2 * w * xbw)));
            mainWrapper.relocate (Math.round (cx), Math.round (cy));
            mainWrapper.setVisible (true);
System.out.println ("TOOK: " + (System.currentTimeMillis () - s));
        });
System.out.println ("TIME: " + System.currentTimeMillis ());
        this.getChildren ().addAll (this.background, mainWrapper, this.header, this.sidebarsPane, this.info);//, /*this.content*/this.popupPane);

        this.setOnMouseMoved (ev ->
        {

            if (ev.getX () <= 20)
            {

                this.showSideBar ();

            }

            if (ev.getY () <= 20)
            {

                this.showHeader ();

            }

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

    public void close ()
    {

        this.updater.cancel (true);

        this.viewer.exitFullScreen ();

        EditorsEnvironment.fullScreenExited ();

        this.viewer.fireProjectEventLater (ProjectEvent.Type.fullscreen,
                                           ProjectEvent.Action.exit);

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

        UIUtils.runLater (() ->
        {

            int w = (int) this.getWidth ();

            this.header.setPrefWidth (this.getWidth () * 0.8);
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

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (fullscreen,actions,showproperties,actionerror));
                                      //"Unable to show properties.");

        }

    }
/*
TODO Remove
    @Override
    public void handleURLAction (String     v,
                                 MouseEvent ev)
    {

        this.viewer.handleURLAction (v,
                                     ev);

    }
*/
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

    @Override
    public void showSideBar (SideBar sb)
    {

        this.showSideBar ();

    }

    @Override
    public void updateLayout ()
    {

    }

    @Override
    public void removeAllNotifications ()
    {

    }

    @Override
    public void removeNotification (Notification n)
    {

    }

    @Override
    public void addNotification (Notification n)
    {

    }

    @Override
    public State getState ()
    {

        State s = new State ();
/*
        s.set (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME,
               this.xBorderWidthProp.getValue ());
        s.set (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME,
               this.yBorderWidthProp.getValue ());
        s.set (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME,
               (float) this.background.getOpacity ());
*/
        s.set (Constants.FULL_SCREEN_SIDEBAR_WIDTH_PROPERTY_NAME,
               (int) this.sidebarsPane.getBoundsInLocal ().getWidth ());
/*
        s.set ("background",
               this.background.getState ());
*/
        return s;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

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

        //this.background.init (bgstate);
        this.background.setOpacity (UserProperties.getFullScreenOpacity ());

        try
        {

            this.background.setBackgroundObject (UserProperties.getFullScreenBackground ());

        } catch (Exception e) {

            Environment.logError ("Unable to set full screen background to: " +
                                  UserProperties.getFullScreenBackground (),
                                  e);

        }

        this.background.getBackgroundObject ().backgroundProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setFullScreenBackground (this.background.getBackgroundObject ().getBackgroundObject ());

        });

    }

    @Override
    public void dispose ()
    {

    }

}
