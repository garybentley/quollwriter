package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.Chapter;
import com.quollwriter.text.Rule;
import com.quollwriter.text.Issue;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProblemFinderSideBar extends SideBarContent<ProjectViewer>
{

    public static final String SIDEBAR_ID = "problemfinder";

    private Rule rule = null;
    private ScheduledFuture updateTimer = null;
    private QuollLabel2 searchingLabel = null;
    private QuollLabel2 ignoredProblemsLabel = null;
    private QuollLabel2 noMatchesLabel = null;
    private BasicHtmlTextFlow ruleSummary = null;
    private VBox contentBox = null;
    private ObservableSet<Issue> ignored = FXCollections.observableSet (new HashSet<> ());
    private Set<TreeItem<Object>> openPaths = null;
    private ChapterProblemResultsBox results = null;

    public ProblemFinderSideBar (ProjectViewer viewer,
                                 Rule          rule)
    {

        super (viewer);
        this.rule = rule;

        this.ruleSummary = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.RULE)
            .text (this.rule.getSummary ())
            .build ();

        this.searchingLabel = QuollLabel2.builder ()
            .styleClassName (StyleClassNames.SEARCHING)
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,problemfinder,loading))
            .build ();
        this.searchingLabel.setVisible (false);

        this.noMatchesLabel = QuollLabel2.builder ()
            .styleClassName (StyleClassNames.NOMATCHES)
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,problemfinder,notfound))
            .build ();
        this.noMatchesLabel.setVisible (false);

        this.ignoredProblemsLabel = QuollLabel2.builder ()
            .styleClassName (StyleClassNames.IGNORED)
            .build ();
        this.ignored.addListener ((SetChangeListener<Issue>) set ->
        {

            this.ignoredProblemsLabel.setVisible (this.ignored.size () > 0);

        });
        this.ignoredProblemsLabel.textProperty ().bind (UILanguageStringsManager.createStringBinding (() ->
        {

            List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,problemfinder,LanguageStrings.ignored);

            if (ignored.size () > 0)
            {

                int s = ignored.size ();

                String t = "";

                if (s == 1)
                {

                    t = getUILanguageStringProperty (Utils.newList (prefix,single)).getValue ();

                } else {

                    t = getUILanguageStringProperty (Utils.newList (prefix,plural),
                                                     s).getValue ();

                }

                return t;

            }

            return "";

        },
        this.ignored));
        this.ignoredProblemsLabel.setVisible (false);
        this.ignoredProblemsLabel.addEventHandler (MouseEvent.MOUSE_PRESSED,
                                                   ev ->
        {

            List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,problemfinder,unignore);

            Set<Chapter> chaps = new HashSet<> ();

            for (Issue ig : this.ignored)
            {

                ig.getChapter ().getProblemFinderIgnores ().remove (ig);

                chaps.add (ig.getChapter ());

            }

            try
            {

                for (Chapter c : chaps)
                {

                    this.viewer.saveProblemFinderIgnores (c);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to update problem finder ignores",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                          //"Unable to update problem finder ignores.");

            }

            this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                          ProjectEvent.Action.unignore);

            this.find ();
/*
TODO Remove - popup not needed
            String id = ProblemFinderSideBar.getSideBarId (this.rule) + "ignored";

            QuollPopup qp = this.viewer.getPopupById (id);

            if (qp == null)
            {

                List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,problemfinder,unignore);
                qp = QuollPopup.questionBuilder ()
                    .withViewer (this.viewer)
                    .popupId (id)
                    .styleClassName (StyleClassNames.PROBLEMFINDER)
                    .title (getUILanguageStringProperty (Utils.newList (prefix,confirmpopup,title)))
                    .message (getUILanguageStringProperty (Utils.newList (prefix,confirmpopup,text),
                                                           this.ignored.size (),
                                                           this.rule.getSummary ()))
                    .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,confirmpopup,confirm)))
                    .onConfirm (eev ->
                    {

                        Set<Chapter> chaps = new HashSet<> ();

                        for (Issue ig : this.ignored)
                        {

                            ig.getChapter ().getProblemFinderIgnores ().remove (ig);

                            chaps.add (ig.getChapter ());

                        }

                        try
                        {

                            for (Chapter c : chaps)
                            {

                                this.viewer.saveProblemFinderIgnores (c);

                            }

                        } catch (Exception e) {

                            Environment.logError ("Unable to update problem finder ignores",
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                                      //"Unable to update problem finder ignores.");

                        }

                        this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                                      ProjectEvent.Action.unignore);

                        this.find ();

                        this.viewer.getPopupById (id).close ();

                    })
                    .build ();

            }

            this.viewer.showPopup (qp,
                                   this.ignoredProblemsLabel,
                                   Side.BOTTOM);
*/
        });

        VBox content = new VBox ();
        this.getChildren ().add (content);

        this.contentBox = new VBox ();
        VBox.setVgrow (this.contentBox,
                       Priority.ALWAYS);

        content.getChildren ().addAll (this.ruleSummary, this.ignoredProblemsLabel, this.searchingLabel, this.noMatchesLabel, this.contentBox);

        this.find ();

    }

    @Override
    public SideBar createSideBar ()
    {

        List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,problemfinder);

        StringProperty title = getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.title));

        Set<Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .iconName (StyleClassNames.REFRESH)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,headercontrols,items,findall,tooltip)))
            .onAction (ev ->
            {

                this.find ();

            })
            .build ());

        cons.add (QuollButton.builder ()
            .iconName (StyleClassNames.EDIT)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,headercontrols,items,edit,tooltip)))
            .onAction (ev ->
            {

                this.viewer.showProblemFinderRuleConfig (popup ->
                {

                    popup.editRule (this.rule);

                });

            })
            .build ());

        cons.add (QuollButton.builder ()
            .iconName (StyleClassNames.CONFIG)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,headercontrols,items,config,tooltip)))
            .onAction (ev ->
            {

                this.viewer.showProblemFinderRuleConfig ();

            })
            .build ());

        SideBar sb = SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.PROBLEMFINDER)
            .headerIconClassName (StyleClassNames.PROBLEMFINDER)
            .styleSheet (StyleClassNames.PROBLEMFINDER)
            .withScrollPane (true)
            .canClose (true)
            .headerControls (cons)
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (ProblemFinderSideBar.getSideBarId (this.rule))
            .build ();

        sb.addEventHandler (SideBar.SideBarEvent.CLOSE_EVENT,
                            ev ->
        {

            this.updateTimer.cancel (true);
            this.updateTimer = null;

            if (this.results != null)
            {

                this.results.dispose ();

            }

        });


        sb.addEventHandler (SideBar.SideBarEvent.HIDE_EVENT,
                            ev ->
        {

            if (this.results != null)
            {

                this.results.clearHighlight ();

            }

        });
        return sb;

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

    public static String getSideBarId (Rule    r)
    {

        return SIDEBAR_ID + r.getId ();

    }

    private void update (final Map<Chapter, Set<Issue>> probs,
                         final Set<Issue>               ignored)
    {

        final ProblemFinderSideBar _this = this;

        boolean expandSearchResults = false;

        if (this.results != null)
        {

            this.openPaths = this.results.getTree ().getExpandedTreeItems ();

        } else {

            expandSearchResults = UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME);

        }

        this.contentBox.getChildren ().clear ();

        this.ignored.clear ();
        this.ignored.addAll (ignored);

        // Remove the ignored.
        // This is very inefficient and may need some pre-filtering such as putting the ignored
        // into chapter groups.
        for (Issue igi : ignored)
        {

            Chapter igc = igi.getChapter ();

            if (igc != null)
            {

                Set<Issue> issues = probs.get (igc);

                Iterator<Issue> iter = issues.iterator ();

                while (iter.hasNext ())
                {

                    Issue i = iter.next ();

                    if (igi.equals (i))
                    {

                        iter.remove ();

                    }

                }

                if (issues.size () == 0)
                {

                    probs.remove (igc);

                }

            }

        }

        ChapterProblemResultsBox r = new ChapterProblemResultsBox (this.viewer,
                                                                   probs);

        this.results = r;

        ScrollPane sp = new ScrollPane (this.results.getContent ());
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        this.contentBox.getChildren ().add (sp);

        if (this.openPaths != null)
        {

            this.results.getTree ().expandPathToRoot (this.openPaths.stream ()
                .map (p -> p.getValue ())
                .collect (Collectors.toSet ()));

        }

        if (expandSearchResults)
        {

            this.results.getTree ().expandAll ();

        }

        this.noMatchesLabel.setVisible (probs.size () == 0);

        UIUtils.runLater (() ->
        {

            this.searchingLabel.setVisible (false);

        });

    }

    public void find ()
    {

         if (this.updateTimer != null)
         {

             this.updateTimer.cancel (true);

         }

         this.searchingLabel.setVisible (true);
         this.ignoredProblemsLabel.setVisible (false);

         if (this.results != null)
         {

            this.results.clearHighlight ();
            this.contentBox.getChildren ().clear ();

         }

         this.updateTimer = this.viewer.schedule (() ->
         {

            Set<Issue> _ignored = new HashSet<> ();

            try
            {

                _ignored = this.viewer.getProblemFinderIgnores (this.rule);

            } catch (Exception e) {

                Environment.logError ("Unable to get problem finder ignores for rule: " +
                                      this.rule,
                                      e);

            }

            final Set<Issue> ignored = _ignored;
            final Map<Chapter, Set<Issue>> probs = this.viewer.getProblemsForAllChapters (this.rule);

            UIUtils.runLater (() -> this.update (probs, ignored));

        },
        0,
        -1);

    }

}
