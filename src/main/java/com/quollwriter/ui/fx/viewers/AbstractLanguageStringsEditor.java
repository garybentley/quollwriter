package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;

import javafx.collections.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.uistrings.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractLanguageStringsEditor<B extends AbstractLanguageStrings, U extends AbstractLanguageStrings> extends AbstractViewer implements RefValueProvider, PanelViewer
{

    public interface CommandId extends AbstractViewer.CommandId
    {

        String showMain = "showMain";
        String openproject = "openproject";
        String textproperties = "textproperties";

    }

	public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 500;
	public static final int PROJECT_BOX_WIDTH = 250;

	public static final String MAIN_CARD = "main";
	public static final String OPTIONS_CARD = "options";

    public static final String SUBMIT_HEADER_CONTROL_ID = "submit";
    public static final String USE_HEADER_CONTROL_ID = "use";
    public static final String HELP_HEADER_CONTROL_ID = "help";
    public static final String FIND_HEADER_CONTROL_ID = "find";

    public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

    private WindowedContent windowedContent = null;

    private Header                title = null;
    private LanguageStringsSideBar mainSideBar = null;
    private FindSideBar                findSideBar = null;
	private StackPane                cards = null;
    protected Map<Id, LanguageStringsIdsPanel> panels = new HashMap<> ();
	private Id currentCard = null;
    ///private Finder                finder = null;
    protected U userStrings = null;
    protected B baseStrings = null;
    protected ObjectProperty<Filter<Node>> nodeFilterProp = null;
    private StringProperty nodeFilterTextProp = null;
    private ObservableMap<Node, Number> errCounts = FXCollections.observableHashMap ();
    private ObservableMap<Node, Number> userCounts = FXCollections.observableHashMap ();
    private boolean inited = false;
    private boolean updatingPreviews = false;
    private ObjectProperty<Panel> currentPanelProp = null;
    private State state = null;
    private DictionaryProvider2 dictionaryProvider = null;
    private Notification filterNotification = null;

    public AbstractLanguageStringsEditor (U userStrings)
    {

        this (userStrings,
              (B) userStrings.getDerivedFrom ());

        this.nodeFilterProp = new SimpleObjectProperty<> ();
        this.nodeFilterTextProp = new SimpleStringProperty ();

        try
        {

            if (userStrings.getSpellcheckLanguage () != null)
            {

                this.dictionaryProvider = new UserDictionaryProvider (userStrings.getSpellcheckLanguage ());

            } else {

                this.dictionaryProvider = null;

            }

        } catch (Exception e) {

            Environment.logError ("Unable to create user dictionary",
                                  e);

        }

        userStrings.spellcheckLanguageProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv != null)
            {

                try
                {

                    this.dictionaryProvider = new UserDictionaryProvider (newv);

                } catch (Exception e) {

                    Environment.logError ("Unable to set language: " + newv,
                                          e);

                }

            } else {

                this.dictionaryProvider = null;

            }

        });

        this.initActionMappings ();

        this.nodeFilterProp.addListener ((pr, oldv, newv) ->
        {

            this.removeNotification (this.filterNotification);

            if (newv != null)
            {

                QuollTextView text = QuollTextView.builder ()
                    .text (this.nodeFilterTextProp)
                    .build ();

                text.setOnMouseClicked (ev ->
                {

                    this.nodeFilterTextProp.setValue (null);
                    this.nodeFilterProp.setValue (null);

                });

                this.filterNotification = this.addNotification (text,
                                                                StyleClassNames.FILTER,
                                                                -1);

            }

        });

    }

    public AbstractLanguageStringsEditor (U userStrings,
                                          B baseStrings)
    {

        this.userStrings = userStrings;
        this.baseStrings = baseStrings;

        this.baseStrings = (B) this.userStrings.getDerivedFrom ();

        this.currentPanelProp = new SimpleObjectProperty<> ();

        this.cards = new StackPane ();

        this.initActionMappings ();

    }

    public Filter<Node> getNodeFilter ()
    {

        return this.nodeFilterProp.getValue ();

    }

    public ObjectProperty<Filter<Node>> nodeFilterProperty ()
    {

        return this.nodeFilterProp;

    }

    private void initActionMappings ()
    {

        this.addActionMapping (() ->
        {

            // Just ignore...

        },
        CommandId.fullscreen);

        this.addActionMapping (() ->
        {

            // Just ignore...

        },
        CommandId.textproperties);

        this.addActionMapping (() ->
        {

            try
            {

                this.viewAchievements ();

            } catch (Exception e) {

                Environment.logError ("Unable to view achievements",
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 getUILanguageStringProperty (achievements,actionerror));

            }

        },
        CommandId.viewachievements,
        CommandId.achievements);

        this.addActionMapping (() ->
        {

            try
            {

                this.showOptions (null);

            } catch (Exception e) {

                Environment.logError ("Unable to view options",
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 getUILanguageStringProperty (options,actionerror));

            }

        },
        CommandId.showoptions,
        CommandId.options);

    }

    @Override
    public void showOptions (String section)
                      throws GeneralException
    {

        if (this.currentPanelProp.getValue ().getPanelId ().equals (OptionsPanel.PANEL_ID))
        {

            return;

        }

        Set<javafx.scene.Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .iconName (StyleClassNames.CLOSE)
            .tooltip (getUILanguageStringProperty (actions,clicktoclose))
            .onAction (ev ->
            {

                this.showIds (this.currentCard);

            })
            .build ());

        OptionsPanel a = new OptionsPanel (this,
                                           cons,
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
        State ps = null;

        if (this.state != null)
        {

            ps = this.state.getAsState (OptionsPanel.PANEL_ID);

        }

        a.init (ps);

        a.showSection (section);

        UIUtils.doOnKeyReleased (a,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.showIds (this.currentCard);

                                 });

        this.cards.getChildren ().add (a.getPanel ());

        this.currentPanelProp.setValue (a.getPanel ());
        a.getPanel ().toFront ();

    }

    public U getUserStrings ()
    {

        return this.userStrings;

    }

    public B getBaseStrings ()
    {

        return this.baseStrings;

    }

    public abstract void tryOut ();

    public abstract void save ()
                        throws Exception;

    public abstract void submit (Runnable onSuccess,
                                 Runnable onFailure);

    public abstract void delete ()
                          throws Exception;

    public abstract void showReportProblemForId (String id);

    public abstract void onForwardLabelClicked ()
                                         throws Exception;

    @Override
    public ProjectFullScreenContent getFullScreenContent ()
    {

        return null;

    }

    public void showForwardLabel (StringProperty text)
    {

        this.mainSideBar.showForwardLabel (text);

    }

    public int getErrorCount (Node n)
    {

        int c = 0;

        // Get the card.
        Number num = this.errCounts.get (n);

        if (num != null)
        {

            //return num.intValue ();

        }

        LanguageStringsIdsPanel panel = this.panels.get (new Id (n.getNodeId ()));

        if (panel != null)
        {

            c = panel.getErrorCount ();

        } else {

            if (this.nodeFilterProp.getValue () != null)
            {

                if (!this.nodeFilterProp.getValue ().accept (n))
                {

                    return 0;

                }

            }

            Set<Value> vals = n.getValues (this.nodeFilterProp.getValue ());

            if (vals.size () == 0)
            {

                return 0;

            }

            for (Value nv : vals)
            {

                Value uv = this.userStrings.getValue (nv.getId (),
                                                      true);

                if (uv instanceof TextValue)
                {

                    TextValue _nv = this.baseStrings.getTextValue (uv.getId ());

                    int x = BaseStrings.getErrors (((TextValue) uv).getRawText (),
                                                 BaseStrings.toId (nv.getId ()),
                                                 _nv.getSCount (),
                                                 this).size ();
                    c += x;

                }

            }

        }

        this.errCounts.put (n,
                            c);

        return c;

    }

    public int getUserValueCount (Node n)
    {

        int c = 0;

        Number num = this.userCounts.get (n);

        if (num != null)
        {

            return num.intValue ();

        }

        LanguageStringsIdsPanel panel = this.panels.get (new Id (n.getNodeId ()));


        if (panel != null)
        {

            c = panel.getUserValueCount ();

        } else {

            if (this.nodeFilterProp.getValue () != null)
            {

                if (!this.nodeFilterProp.getValue ().accept (n))
                {

                    return 0;

                }

            }

            Set<Value> vals = n.getValues (this.nodeFilterProp.getValue ());

            if (vals.size () == 0)
            {

                return 0;

            }

            for (Value nv : vals)
            {

                Value uv = this.userStrings.getValue (nv.getId (),
                                                      true);

                if (uv != null)
                {

                    c++;

                }

            }

        }

        this.userCounts.put (n,
                             c);

        return c;

    }

    public Set<Value> getValuesForNode (Node n)
    {

        Set<Value> vals = n.getValues (this.nodeFilterProp.getValue ());

        return vals;

    }

    public void showAllStrings ()
    {

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to update view."));

            return;

        }

        // Clear out the panel cache.
        this.panels = new HashMap<> ();

        this.currentCard = null;
        this.cards.getChildren ().clear ();

        this.nodeFilterTextProp.setValue (null);
        this.nodeFilterProp.setValue (null);

    }

    public void updateSideBar (final Node n)
    {

        //this.errCounts.remove (n);
        this.userCounts.remove (n);

        this.userCounts.put (n,
                             this.getUserValueCount (n));

        this.errCounts.put (n,
                            this.getErrorCount (n));

    }

    public String getPreviewText (String t)
    {

        return BaseStrings.buildText (t,
                                      this);

    }

    public void updatePreviews ()
    {

        final AbstractLanguageStringsEditor<B, U> _this = this;

        UIUtils.runLater (() ->
        {

            if (_this.updatingPreviews)
            {

                return;

            }

            try
            {

                _this.updatingPreviews = true;

                for (LanguageStringsIdsPanel p : _this.panels.values ())
                {

                    p.updatePreviews ();

                }

            } catch (Exception e) {

                Environment.logError ("Unable to update previews.",
                                      e);

            } finally {

                _this.updatingPreviews = false;

            }

        });

    }

    @Override
    public String getString (String id)
    {

        List<String> idparts = BaseStrings.getIdParts (id);

        // See if we have a panel.
        if (idparts.size () > 0)
        {

            for (LanguageStringsIdsPanel p : this.panels.values ())
            {

                String t = p.getIdValue (id);

                if (t != null)
                {

                    return this.getPreviewText (t);

                }

            }

        }

        return this.userStrings.getString (id);

    }

    @Override
    public int getSCount (String id)
    {

        return this.baseStrings.getSCount (id);

    }

    @Override
    public boolean isIdValid (String id)
    {

        return this.baseStrings.getNode (id) != null;

    }

    @Override
    public String getRawText (String id)
    {

        for (LanguageStringsIdsPanel p : this.panels.values ())
        {

            String t = p.getIdValue (id);

            if (t != null)
            {

                return t;

            }

        }

        return this.userStrings.getRawText (id, true);

    }

    public void showIds (Id id)
    {

        this.showIds (id,
                      null);

    }

    public void showIds (Id       id,
                         Runnable onShow)
    {

        //String id = node.getNodeId ();
        LanguageStringsIdsPanel p = this.panels.get (id);

        if (p == null)
        {

            p = this.createIdsPanel (id);

            try
            {

                State ps = null;

                if (this.state != null)
                {

                    ps = this.state.getAsState ("ids-" + id.getId ());

                }

                p.init (ps);

            } catch (Exception e) {

                Environment.logError ("Unable to show ids for id: " +
                                      id,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 "Unable to show panel.");

                return;

            }

            this.panels.put (id,
                             p);

            this.cards.getChildren ().add (p.getPanel ());

        }

        this.showCard (id);

        UIUtils.runLater (onShow);

    }

    public void showId (Id id)
    {

        final AbstractLanguageStringsEditor _this = this;

        Node n = this.baseStrings.getNode (id);

        while (n.getParent () != null)
        {

            n = n.getParent ();

            if (n.getParent () == null)
            {

                break;

            }

        }

        this.showIds (new Id (n.getNodeId ()),
                      () ->
        {

            this.scrollToNode (id);

        });

    }

    public void scrollToNode (Id id)
    {

        LanguageStringsIdsPanel c = this.panels.get (this.currentCard);

        c.scrollToNode (id.getId ());

    }

    public void showMainCard ()
    {

        this.showCard (new Id (0, MAIN_CARD, false));

    }

    public void showCard (Id id)
    {

        Panel p = this.panels.get (id).getPanel ();
        p.toFront ();

		this.currentCard = id;

        this.currentPanelProp.setValue (p);

    }

    public ObjectProperty<Panel> currentPanelProperty ()
    {

        return this.currentPanelProp;

    }

    protected void setBaseStrings (B ls)
    {

        this.baseStrings = ls;

    }

	@Override
    public void init (State s)
			   throws GeneralException
    {

        String ps = UserProperties.get ("languagestringeditor-" + this.userStrings.getId ());

        s = new State (ps);

		super.init (s);

        this.state = s;

        this.mainSideBar = new LanguageStringsSideBar (this,
                                                       this.baseStrings);

        super.setMainSideBar (this.mainSideBar.getSideBar ());

        this.mainSideBar.init (s.getAsState ("sidebar"));

        String cardId = s.getAsString ("currentcard");

        if (cardId != null)
        {

            this.showIds (new Id (0, cardId, false));

        }

        this.addKeyMapping (CommandId.find,
                            KeyCode.F1);
        this.addKeyMapping (CommandId.find,
                            KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.newproject,
                            KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.openproject,
                            KeyCode.O, KeyCombination.SHORTCUT_DOWN);
        this.addActionMapping (() ->
        {

            this.showFind ();

        },
        CommandId.find);

        this.addActionMapping (() ->
        {

            this.showMainCard ();

        },
        CommandId.showMain);

    }

    @Override
    public void removeSideBar (SideBar sb)
    {

        if (sb != null)
        {

            try
            {

                State ss = sb.getState ();

                UserProperties.set ("sidebarState-" + id,
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
    public Supplier<Set<javafx.scene.Node>> getTitleHeaderControlsSupplier ()
    {

        return () ->
        {

            Set<javafx.scene.Node> controls = new LinkedHashSet<> ();

            controls.add (this.getTitleHeaderControl (HeaderControl.filter));
            controls.add (this.getTitleHeaderControl (HeaderControl.submit));
            controls.add (this.getTitleHeaderControl (HeaderControl.find));
            controls.add (this.getTitleHeaderControl (HeaderControl.tryout));
            controls.add (this.getTitleHeaderControl (HeaderControl.help));

            return controls;

        };

    }

	@Override
    public javafx.scene.Node getTitleHeaderControl (HeaderControl control)
	{

		if (control == null)
		{

			return null;

		}

        if (control == HeaderControl.filter)
        {

            RadioMenuItem nofilter = new RadioMenuItem ("Do not filter");
            RadioMenuItem novalue = new RadioMenuItem ("Show strings with no value");
            RadioMenuItem errors = new RadioMenuItem ("Only show strings with an error");

            IconBox ib = IconBox.builder ()
                .styleClassName (StyleClassNames.CANCEL)
                .build ();
            //nofilter.setGraphic (ib);
            nofilter.setOnAction (ev ->
            {

                this.nodeFilterTextProp.setValue (null);
                this.nodeFilterProp.setValue (null);

            });

            ib = IconBox.builder ()
                .iconName (StyleClassNames.NOVALUE)
                .build ();
            novalue.setGraphic (ib);
            novalue.setOnAction (ev ->
            {

                this.nodeFilterTextProp.setValue ("Only showing strings that have no value set, click to show all the strings.");
                this.nodeFilterProp.setValue (n ->
                {

                    if (n.getAllValues ().size () > 0)
                    {

                        return true;

                    }

                    String t = this.getRawText (BaseStrings.toId (n.getId ()));

                    return (t == null)
                            ||
                            (t.equals (""));

                });

            });

            ib = IconBox.builder ()
                .iconName (StyleClassNames.ERROR)
                .build ();
            errors.setGraphic (ib);
            errors.setOnAction (ev ->
            {

                this.nodeFilterTextProp.setValue ("Only showing strings that have one or more errors, click to show all the strings.");
                this.nodeFilterProp.setValue (n ->
                {

                    if (n.getAllValues ().size () > 0)
                    {

                        return true;

                    }

                    if (!(n instanceof TextValue))
                    {

                        return false;

                    }

                    TextValue nv = (TextValue) n;

                    String t = this.getRawText (BaseStrings.toId (n.getId ()));

                    if (t == null)
                    {

                        return false;

                    }

                    int x = BaseStrings.getErrors (t,
                                                 BaseStrings.toId (n.getId ()),
                                                 nv.getSCount (),
                                                 this).size ();

                    return x > 0;

                });

            });

            nofilter.setSelected (true);

            ToggleGroup tp = new ToggleGroup ();
            tp.getToggles ().addAll (nofilter, novalue, errors);

            this.nodeFilterProp.addListener ((pr, oldv, newv) ->
            {

                if (newv == null)
                {

                    nofilter.setSelected (true);

                }

            });

            return QuollMenuButton.builder ()
                .tooltip ("Click to filter the strings")
                .iconName (StyleClassNames.FILTER)
                .buttonId ("filter")
                .items (() ->
                {

                    Set<MenuItem> its = new LinkedHashSet<> ();
                    its.add (novalue);
                    its.add (errors);
                    its.add (nofilter);
                    return its;

                })
                .build ();
/*
            return QuollButton.builder ()
                .tooltip ("Click to limit the view to only those strings with a value")
                .iconName (StyleClassNames.CANCEL)
                .buttonId ("onlynovalue")
                .onAction (ev ->
                {

                    this.mainSideBar.setNodeFilter (n ->
                    {

                        if (n.getAllValues ().size () > 0)
                        {

                            return true;

                        }

                        String t = this.getRawText (BaseStrings.toId (n.getId ()));

                        return (t == null)
                                ||
                                (t.equals (""));

                    });

                })
                .build ();
*/
        }

        if (control == HeaderControl.submit)
        {

            return QuollButton.builder ()
                .tooltip ("Click to submit the strings")
                .iconName (StyleClassNames.SUBMIT)
                .buttonId ("submitstrings")
                .onAction (ev ->
                {

                    this.submit (null,
                                 null);

                })
                .build ();

        }

        if (control == HeaderControl.tryout)
        {

            return QuollButton.builder ()
                .tooltip ("Click to try out your strings")
                .iconName (StyleClassNames.TRYOUT)
                .buttonId ("tryoutstrings")
                .onAction (ev ->
                {

                    this.tryOut ();

                })
                .build ();

        }

		return super.getTitleHeaderControl (control);

	}

    public U getUserLanguageStrings ()
    {

        return this.userStrings;

    }
/*
	@Override
	public void showOptions (String section)
	{

        throw new UnsupportedOperationException ("Not supported.");

	}
*/
	@Override
    public void close (Runnable afterClose)
    {

        this.close (true,
                    afterClose);

    }

    public void close (boolean save,
                       Runnable afterClose)
	{

        if (save)
        {

            try
            {

                this.save ();

            } catch (Exception e) {

                Environment.logError ("Unable to save language strings: " +
                                      this.userStrings,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 "Unable to save language strings.");

                return;

            }

        }

        try
        {

            State s = this.getState ();
            UserProperties.set ("languagestringeditor-" + this.userStrings.getId (),
                                s.asString ());

        } catch (Exception e) {

            Environment.logError ("Unable to set state",
                                  e);

        }

		super.close (afterClose);

	}

    @Override
    public State getState ()
    {

        State s = super.getState ();

        if (this.currentCard != null)
        {

            s.set ("currentcard",
                   this.currentCard.getId ());

        }

        for (Id id : this.panels.keySet ())
        {

            LanguageStringsIdsPanel p = this.panels.get (id);

            s.set ("ids-" + id.getId (),
                   p.getState ());

        }

        s.set ("sidebar",
               this.mainSideBar.getState ());

        return s;

    }

    @Override
    public void handleURLAction (String     v,
                                 MouseEvent ev)
    {

        try
        {

            if (v.equals ("import"))
            {

				//this.showImport ();

			}

        } catch (Exception e) {

            Environment.logError ("Unable to perform action: " +
                                  v,
                                  e);

        }

		super.handleURLAction (v,
                               ev);

	}

    public void showFind (String v)
    {

        if (this.findSideBar == null)
        {

            this.findSideBar = new FindSideBar (this);

            this.addSideBar (this.findSideBar);

        }

        this.showSideBar (this.findSideBar.getSideBar ().getSideBarId ());

        this.findSideBar.find (v);

    }

    public void showFind ()
    {

        this.showFind (null);

    }

    private LanguageStringsIdsPanel createIdsPanel (Id id)
    {

        final AbstractLanguageStringsEditor _this = this;

        return new LanguageStringsIdsPanel (this,
                                            this.baseStrings.getNode (id));
//                                            this.userStrings.getNode (id).getValues (this.nodeFilter));

    }

    @Override
    public WindowedContent getWindowedContent ()
    {

        if (this.windowedContent == null)
        {

            Supplier<Set<javafx.scene.Node>> hcsupp = this.getTitleHeaderControlsSupplier ();

            Set<javafx.scene.Node> headerCons = new LinkedHashSet<> ();

            if (hcsupp != null)
            {

                headerCons.addAll (hcsupp.get ());

            }

            this.windowedContent = new WindowedContent (this,
                                                        this.getStyleClassName (),
                                                        StyleClassNames.EDIT,
                                                        headerCons,
                                                        this.cards);

            this.windowedContent.setTitle (this.titleProperty ());

            this.windowedContent.getHeader ().getControls ().setVisibleItems (UserProperties.languageStringsEditorHeaderControlButtonIds ());

            this.windowedContent.getHeader ().getControls ().setOnConfigurePopupClosed (ev ->
            {

                UserProperties.setLanguageStringsEditorHeaderControlButtonIds (this.windowedContent.getHeader ().getControls ().getVisibleItemIds ());

            });

        }

        return this.windowedContent;

    }

    public FindSideBar getFindSideBar ()
    {

        return this.findSideBar;

    }

    protected void showMessage (String title,
                                String message)
    {

        QuollPopup.messageBuilder ()
            .inViewer (this.isClosed () ? null : this)
            .title (new SimpleStringProperty (title))
            .message (new SimpleStringProperty (message))
            .closeButton ()
            .build ()
            .show ();

    }

    @Override
    public void deleteAllObjectsForType (UserConfigurableObjectType t)
                                  throws GeneralException
    {

        // Do nothing.

    }

    @Override
    public Panel getCurrentPanel ()
    {

        return this.currentPanelProp.getValue ();

    }

    protected QuollPopup showProgressPopup (String title,
                                            String headerIconClassName,
                                            String message)
    {

        VBox b = new VBox ();
        b.getChildren ().add (QuollTextView.builder ()
            .text (new SimpleStringProperty (message))
            .build ());
        ProgressBar pb = new ProgressBar ();
        pb.setProgress (ProgressBar.INDETERMINATE_PROGRESS);

        QuollPopup qp = QuollPopup.messageBuilder ()
            .message (b)
            .headerIconClassName (headerIconClassName)
            .title (new SimpleStringProperty (title))
            .inViewer (this)
            .removeOnClose (true)
            .hideOnEscape (true)
            .build ();

        qp.getProperties ().put ("progress-bar",
                                 pb);

        qp.show ();

        return qp;

    }

    @Override
    public Set<FindResultsBox> findText (String t)
    {

        Set<FindResultsBox> res = new LinkedHashSet<> ();

        Map<String, Section> sects = new HashMap<> ();

        for (Section sect : (Set<Section>) this.baseStrings.getSections ())
        {

            sects.put (sect.id,
                       sect);

        }

        Set<Value> results = this.baseStrings.find (t);

        Set<Value> uresults = this.userStrings.find (t);

        for (Value v : uresults)
        {

            results.add (this.baseStrings.getValue (v.getId ()));

        }

        Map<Section, Map<Node, List<Value>>> vals = new HashMap<> ();

        for (Value v : results)
        {

            Node r = v.getRoot ();

            Section s = sects.get (r.getSection ());

            Map<Node, List<Value>> svs = vals.get (s);

            if (svs == null)
            {

                svs = new LinkedHashMap<> ();

                vals.put (s,
                          svs);

            }

            Set<String> tlns = r.getTopLevelNodes ();

            if (tlns != null)
            {

                for (String tln : tlns)
                {

                    String tlnid = r.getNodeId () + "." + tln;

                    Node tlnn = this.baseStrings.getNode (tlnid);

                    if (BaseStrings.toId (v.getId ()).startsWith (tlnid))
                    {

                        List<Value> l = svs.get (tlnn);

                        if (l == null)
                        {

                            l = new ArrayList<> ();

                            svs.put (tlnn,
                                     l);

                        }

                        l.add (v);

                    }

                }

            } else {

                List<Value> l = svs.get (r);

                if (l == null)
                {

                    l = new ArrayList<> ();

                    svs.put (r,
                             l);

                }

                l.add (v);

            }

        }

        res.add (new LanguageStringsResultsBox (this,
                                                vals));

        return res;

    }

    public ObservableMap<Node, Number> errorCountsProperty ()
    {

        return this.errCounts;

    }

    public ObservableMap<Node, Number> userCountsProperty ()
    {

        return this.userCounts;

    }

    private void viewAchievements ()
                            throws GeneralException
    {

        if (this.currentPanelProp.getValue ().getPanelId ().equals (AchievementsPanel.PANEL_ID))
        {

            return;

        }

        Set<javafx.scene.Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .iconName (StyleClassNames.CLOSE)
            .tooltip (getUILanguageStringProperty (actions,clicktoclose))
            .onAction (ev ->
            {

                this.showIds (this.currentCard);

            })
            .build ());

        AchievementsPanel a = new AchievementsPanel (this,
                                                     cons);

        State ps = null;

        if (this.state != null)
        {

            ps = this.state.getAsState (AchievementsPanel.PANEL_ID);

        }

        a.init (ps);

        UIUtils.doOnKeyReleased (a,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.showIds (this.currentCard);

                                 });

        this.cards.getChildren ().add (a.getPanel ());

        this.currentPanelProp.setValue (a.getPanel ());
        a.getPanel ().toFront ();

    }

    public com.quollwriter.ui.fx.SpellChecker getSpellChecker ()
    {

        return this.dictionaryProvider != null ? this.dictionaryProvider.getSpellChecker () : null;

    }

}
