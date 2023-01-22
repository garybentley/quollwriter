package com.quollwriter.ui.fx;

import java.util.*;
import java.util.stream.*;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;
import com.quollwriter.uistrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProblemFinder extends VBox
{

    private VBox problemsBox = null;
    private ObservableSet<Issue> ignoredIssues = null;
    private ProjectChapterEditorPanelContent panel = null;
    private ProjectViewer viewer = null;
    private TextEditor.Highlight lineHighlight = null;
    private TextEditor.Highlight issueHighlight = null;
    private Label ignoredProblemsLabel = null;
    private Label noProblemsLabel = null;
    private Label limitLabel = null;

    private TextBlockIterator    iter = null;

    private int                  start = 0;
    private int                  end = -1;
    private int                  lastCaret = -1;
    private List<QuollCheckBox2> ignores = new ArrayList<> ();
    private boolean inited = false;

    public ProblemFinder (ProjectChapterEditorPanelContent panel)
    {

        this.panel = panel;
        this.viewer = panel.getViewer ();

        panel.getEditor ().caretPositionProperty ().addListener ((pr, oldv, newv) ->
        {

            this.lastCaret = newv;
            this.init (newv, -1);

        });

        this.ignoredIssues = panel.getObject ().getProblemFinderIgnores ();

        this.managedProperty ().bind (this.visibleProperty ());

        List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder);

        UIUtils.addStyleSheet (this,
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               StyleClassNames.PROBLEMFINDER);

        this.getStyleClass ().add (StyleClassNames.PROBLEMFINDER);

       Set<Node> controls = new LinkedHashSet<> ();
       controls.add (QuollButton.builder ()
            .iconName (StyleClassNames.CONFIG)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,headercontrols,items,config,tooltip)))
            .onAction (ev ->
            {

                this.panel.showProblemFinderRuleConfig ();

            })
            .build ());

       controls.add (QuollButton.builder ()
            .iconName (StyleClassNames.FINISH)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,headercontrols,items,cancel,tooltip)))
            .onAction (ev ->
            {

                this.finish ();

            })
            .build ());

       controls.add (UIUtils.createHelpPageButton (this.panel.getViewer (),
                                                   "chapters/problem-finder",
                                                   null));

       this.getChildren ().add (Header.builder ()
            .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
            .controls (controls)
            .iconClassName (StyleClassNames.PROBLEMFINDER)
            .build ());

       this.limitLabel = QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,limit)))
            .styleClassName (StyleClassNames.INFORMATION)
            .build ();

       this.limitLabel.managedProperty ().bind (this.limitLabel.visibleProperty ());
       this.limitLabel.setVisible (false);
       this.getChildren ().add (this.limitLabel);

       this.noProblemsLabel = QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,noproblemsfound)))
            .styleClassName (StyleClassNames.NOPROBLEMS)
            .build ();

       this.noProblemsLabel.managedProperty ().bind (this.noProblemsLabel.visibleProperty ());
       this.noProblemsLabel.setVisible (false);
       this.getChildren ().add (this.noProblemsLabel);

       this.problemsBox = new VBox ();
       this.problemsBox.getStyleClass ().add (StyleClassNames.ISSUES);
       VBox.setVgrow (this.problemsBox,
                      Priority.ALWAYS);

       this.getChildren ().add (this.problemsBox);

       HBox buts = new HBox ();
       this.getChildren ().add (buts);

       QuollButtonBar qbb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .tooltip (getUILanguageStringProperty (Utils.newList (prefix, buttons,previous,tooltip)))
                        .iconName (StyleClassNames.PREVIOUS)
                        .onAction (ev ->
                        {

                            try
                            {

                                this.previous ();

                            } catch (Exception e)
                            {

                                Environment.logError ("Unable to goto previous.",
                                                      e);

                                ComponentUtils.showErrorMessage (this.panel.getViewer (),
                                                                 getUILanguageStringProperty (prefix,previouserror));

                            }

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix, buttons,next,text)))
                        .tooltip (getUILanguageStringProperty (Utils.newList (prefix, buttons,next,tooltip)))
                        .iconName (StyleClassNames.NEXT)
                        .onAction (ev ->
                        {

                            try
                            {

                                this.next ();

                            } catch (Exception e)
                            {

                                Environment.logError ("Unable to goto next problem",
                                                      e);

                                ComponentUtils.showErrorMessage (this.panel.getViewer (),
                                                                 getUILanguageStringProperty (prefix,nexterror));
                                                          //"Unable to go to next sentence.");

                            }

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix, buttons,finish,text)))
                        .tooltip (getUILanguageStringProperty (Utils.newList (prefix, buttons,finish,tooltip)))
                        .iconName (StyleClassNames.FINISH)
                        .onAction (ev ->
                        {

                            this.finish ();

                        })
                        .build ())
            .build ();

       buts.getChildren ().add (qbb);
       HBox.setHgrow (qbb,
                      Priority.ALWAYS);

       this.ignoredProblemsLabel = QuollLabel.builder ()
            .styleClassName (StyleClassNames.WARNING)
            .build ();
       this.ignoredProblemsLabel.managedProperty ().bind (this.ignoredProblemsLabel.visibleProperty ());
       this.ignoredProblemsLabel.setVisible (this.ignoredIssues.size () > 0);

       buts.getChildren ().add (this.ignoredProblemsLabel);

       this.ignoredProblemsLabel.textProperty ().bind (UILanguageStringsManager.createStringBinding (() ->
       {

           int s = this.ignoredIssues.size ();

           List<String> prefix2 = Arrays.asList (project,editorpanel,actions,problemfinder,ignored);

           String t = null;

           if (s == 1)
           {

               t = getUILanguageStringProperty (Utils.newList (prefix2,single)).getValue ();

           } else {

               t = getUILanguageStringProperty (Utils.newList (prefix2,plural),
                                                s).getValue ();

           }

           return t;

       },
       this.ignoredIssues));

       this.panel.addSetChangeListener (this.ignoredIssues,
                                        ev ->
       {

           this.ignoredProblemsLabel.setVisible (this.ignoredIssues.size () > 0);

       });

       this.ignoredProblemsLabel.addEventHandler (MouseEvent.MOUSE_PRESSED,
                                                  ev ->
       {

           String id = this.panel.getObject ().getObjectReference ().asString () + "unignoreconfirm";

           if (this.viewer.getPopupById (id) == null)
           {

               QuollPopup qp = QuollPopup.questionBuilder ()
                    .withViewer (this.viewer)
                    .popupId (id)
                    .title (getUILanguageStringProperty (Utils.newList (prefix, unignore,confirmpopup,title)))
                    .styleClassName (StyleClassNames.PROBLEMFINDER)
                    .message (getUILanguageStringProperty (Utils.newList (prefix,unignore,confirmpopup,text),
                                                           Environment.formatNumber (this.ignoredIssues.size ())))
                    .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,unignore,confirmpopup,confirm)))
                    .onConfirm (eev ->
                    {

                        this.removeAllIgnores ();

                        this.viewer.getPopupById (id).close ();

                    })
                    .build ();

                this.viewer.showPopup (qp,
                                       this.ignoredProblemsLabel,
                                       Side.TOP);

            }

       });

    }

    private void clearIssuesBox ()
    {

        this.problemsBox.getChildren ().clear ();

    }

    private void addNoProblems ()
    {

        this.clearIssuesBox ();

        this.noProblemsLabel.setVisible (true);

    }

    private void getIgnores ()
    {

        for (Node n : this.problemsBox.getChildren ())
        {

            if (!(n instanceof QuollCheckBox2))
            {

                continue;

            }

            QuollCheckBox2 b = (QuollCheckBox2) n;

            Issue iss = (Issue) b.getUserData ();

            if (b.isSelected ())
            {

                this.panel.getObject ().getProblemFinderIgnores ().add (iss);

                this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                              ProjectEvent.Action.ignore);

            } else {

                if (this.panel.getObject ().getProblemFinderIgnores ().remove (iss))
                {

                    this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                                  ProjectEvent.Action.unignore);

                }

            }

            try
            {

                this.viewer.saveProblemFinderIgnores (this.panel.getObject ());

            } catch (Exception e) {

                Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                      this.panel.getObject (),
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (project,editorpanel,actions,problemfinder,ignore,actionerror));

            }

        }

        this.ignores = new ArrayList<> ();

    }

    private void clearHighlights ()
    {

        this.panel.getEditor ().removeHighlight (this.lineHighlight);
        this.panel.getEditor ().removeHighlight (this.issueHighlight);

    }

    private void removeAllIgnores ()
    {

        this.ignoredIssues.clear ();

        try
        {

            this.viewer.saveProblemFinderIgnores (this.panel.getObject ());

        } catch (Exception e) {

            Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                  this.panel.getObject (),
                                  e);

            ComponentUtils.showErrorMessage (this.panel.getViewer (),
                                             getUILanguageStringProperty (project,editorpanel,actions,problemfinder,unignoreall,actionerror));

            return;

        }

        this.showIgnoredIssues ();

        this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                      ProjectEvent.Action.unignore);

    }

    public void reset ()
    {

        this.start = -1;
        this.end = -1;
        this.lastCaret = -1;
        this.getIgnores ();

        this.clearHighlights ();

    }

    public void close ()
    {

        this.finish ();

    }

    private void finish ()
    {

        this.setVisible (false);

        this.inited = false;

        this.reset ();

        this.panel.getEditor ().removeHighlight (this.lineHighlight);
        this.panel.getEditor ().removeHighlight (this.issueHighlight);

        this.panel.getEditor ().setHighlightWritingLine (this.panel.viewer.isHighlightWritingLine ());

        this.panel.getEditor ().requestFocus ();

        this.panel.requestLayout ();

    }

    private int processTextBlock (TextBlock b)
                          // throws Exception
    {

        if (b == null)
        {

            return 0;

        }

        if (b instanceof Paragraph)
        {

            return this.handleParagraph ((Paragraph) b);

        }

        if (b instanceof Sentence)
        {

            return this.handleSentence ((Sentence) b);

        }

        throw new IllegalArgumentException ("Type: " +
                                    b.getClass ().getName () +
                                    " not supported.");

    }

    private int handleParagraph (Paragraph p)
                                // throws    Exception
    {

        List<Issue> issues = RuleFactory.getParagraphIssues (p,
                                                             this.viewer.getProject ().getProperties ());

        this.setPositions (issues);

        return this.handleIssues (issues,
                                  p);

    }

    private int handleSentence (Sentence s)
                                //throws   Exception
    {

        List<Issue> issues = RuleFactory.getSentenceIssues (s,
                                                            this.viewer.getProject ().getProperties ());

        this.setPositions (issues);
        return this.handleIssues (issues,
                                  s);

    }

    public void start ()
    {

        this.next ();

    }

    /*
     Returns the number of issues actually displayed.
     */
    private int handleIssues (final List<Issue> issues,
                               final TextBlock   textBlock)
                        //throws Exception
    {

        Collections.sort (issues,
                          new IssueSorter ());
        this.clearIssuesBox ();

        int ignoredCount = 0;
        int issueCount = 0;

        for (Issue iss : issues)
        {

            QuollCheckBox2 n = this.createIssueItem (iss);
            this.problemsBox.getChildren ().add (n);
            n.managedProperty ().bind (n.visibleProperty ());

            if (this.ignoredIssues.contains (iss))
            {

                n.setVisible (false);
                n.setSelected (true);

                ignoredCount++;
                continue;

            }

            issueCount++;

        }

        if (issueCount > 0)
        {

            this.lastCaret = this.panel.getEditor ().getSelection ().getStart ();

            this.panel.scrollToTextPosition (textBlock.getAllTextStartOffset (),
                                             () ->
            {

                int end = textBlock.getAllTextEndOffset () + 1;

                if (textBlock instanceof Sentence)
                {

                    end = ((Sentence) textBlock).getLastWord ().getAllTextEndOffset ();

                }

                this.lineHighlight = this.panel.getEditor ().addHighlight (new IndexRange (textBlock.getAllTextStartOffset (),
                                                                                           end),
                                                                           UserProperties.getProblemFinderBlockHighlightColor ());

            });

        }

        if (ignoredCount > 0)
        {

            int _ignoredCount = ignoredCount;

            QuollLabel l = QuollLabel.builder ()
                .styleClassName (StyleClassNames.WARNING)
                .label (UILanguageStringsManager.createStringPropertyWithBinding (() ->
                {

                    List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder,unignoreissues);

                    StringProperty t = null;

                    if (_ignoredCount == 1)
                    {

                        t = getUILanguageStringProperty (Utils.newList (prefix,single));

                    } else {

                        t = getUILanguageStringProperty (Utils.newList (prefix,plural),
                                                         _ignoredCount);

                    }

                    return t.getValue ();

                }))
                .build ();

            l.managedProperty ().bind (l.visibleProperty ());

            this.problemsBox.getChildren ().add (l);
            l.addEventHandler (MouseEvent.MOUSE_PRESSED,
                               ev ->
            {

                this.showIgnoredIssues ();

            });

        }

        return issueCount;

    }

    private void showIgnoredIssues ()
    {

        this.problemsBox.getChildren ().stream ()
            .forEach (n ->
            {

                if (n instanceof QuollCheckBox2)
                {

                    n.setVisible (true);

                } else {

                    n.setVisible (false);

                }

                this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                              ProjectEvent.Action.unignore);

            });

    }

    private QuollCheckBox2 createIssueItem (Issue iss)
    {

        StringProperty tt = new SimpleStringProperty ();
        tt.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            String d = iss.getRule ().getDescription ();

            if ((d != null)
                &&
                (d.length () > 0)
               )
            {

                d = d + "<br /><br />";

            }

            if (d == null)
            {

                d = "";

            }

            d = d + getUILanguageStringProperty (problemfinder,ignore,checkbox).getValue ();

            return d;

        }));

        QuollCheckBox2 cb = QuollCheckBox2.builder ()
            .tooltip (tt)
            .label (new SimpleStringProperty (iss.getDescription ()))
            .build ();

        cb.setUserData (iss);

        cb.addEventHandler (MouseEvent.MOUSE_ENTERED,
                            ev ->
        {

            this.issueHighlight = this.panel.getEditor ().addHighlight (new IndexRange (iss.getStartIssuePosition (),
                                                                                        iss.getEndIssuePosition ()),
                                                                        UserProperties.getProblemFinderIssueHighlightColor ());

        });

        cb.addEventHandler (MouseEvent.MOUSE_EXITED,
                            ev ->
        {

            this.panel.getEditor ().removeHighlight (this.issueHighlight);

        });

        ProblemFinder _this = this;

        ContextMenu cm = new ContextMenu ();

        List<String> prefix = Arrays.asList (problemfinder,ignore,popupmenu,items);

        cm.getItems ().add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.FIND)
            .label (getUILanguageStringProperty (Utils.newList (prefix,find)))
            .onAction (ev ->
            {

                this.finish ();

                this.viewer.showProblemFinderRuleSideBar (iss.getRule ());

            })
            .build ());

        cm.getItems ().add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.IGNORE)
            .label (getUILanguageStringProperty (Utils.newList (prefix,ignore)))
            .onAction (ev ->
            {

                ProblemFinderRuleConfigPopup.confirmRuleRemoval (iss.getRule (),
                                                                 this.viewer,
                                                                 () ->
                {

                    _this.removeCheckboxesForRule (iss.getRule ());
                    _this.next ();

                });

            })
            .build ());

        cm.getItems ().add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.EDIT)
            .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
            .onAction (ev ->
            {

                this.viewer.showProblemFinderRuleConfig (config ->
                {

                    config.editRule (iss.getRule ());

                });

            })
            .build ());

        cb.setContextMenu (cm);

        this.ignores.add (cb);

        return cb;

    }

    private void setPositions (List<Issue> issues)
    {

        issues.stream ()
            .forEach (i ->
            {

                i.setStartPosition2 (this.panel.getEditor ().createTextPosition (i.getStartIssuePosition ()));

                // Set the end position of the sentence.
                i.setEndPosition2 (this.panel.getEditor ().createTextPosition (i.getEndIssuePosition ()));

            });

    }

    public void removeCheckboxesForRule (Rule r)
    {

        for (QuollCheckBox2 b : this.ignores)
        {

            Issue iss = (Issue) b.getUserData ();

            if (r.getId ().equals (iss.getRule ().getId ()))
            {

                this.getChildren ().remove (b);

            }

        }

    }

    private void init (IndexRange range)
    {

        this.init (range.getStart (),
                   range.getEnd ());

    }

    private void init (int start,
                       int end)
    {

        this.start = start;
        this.end = end;

        this.getIgnores ();
        this.clearHighlights ();

        if (this.end < this.start)
        {

            this.end = -1;

        }

        if (this.start == this.end)
        {

            this.end = -1;

        }

        this.noProblemsLabel.setVisible (false);
        this.clearIssuesBox ();

        this.limitLabel.setVisible (this.end > -1);

        this.iter = new TextBlockIterator (this.panel.getEditor ().getText (),
                                           this.start,
                                           this.end);

        this.inited = true;

    }

    public void previous ()
                   throws Exception
    {

        if (!this.inited)
        {

            this.init (this.panel.getEditor ().getSelection ());

        }

        this.noProblemsLabel.setVisible (false);

        final ProblemFinder _this = this;

        this.getIgnores ();

        this.clearHighlights ();

        TextBlock b = null;

        while ((b = this.iter.previous ()) != null)
        {

            Environment.logDebugMessage ("Looking for problems in: " + b);

            int c = this.processTextBlock (b);

            if (c > 0)
            {

                Environment.logDebugMessage ("Got: " + c + " problems for text: " + b);

                return;

            }

        }

        this.addNoProblems ();

        if (this.end > -1)
        {

            String id = this.panel.getObject ().getObjectReference ().asString () + "problemfinderprevend";

            QuollPopup qp = this.viewer.getPopupById (id);

            if (qp == null)
            {

                List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder,nomoreproblems,selected,LanguageStrings.end);

                qp = QuollPopup.messageBuilder ()
                    .withViewer (this.viewer)
                    .popupId (id)
                    .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
                    .message (getUILanguageStringProperty (Utils.newList (prefix,text)))
                    .build ();

            }

            this.viewer.showPopup (qp);

            return;

        }

        String id = this.panel.getObject ().getObjectReference ().asString () + "problemfinderprevstart";

        QuollPopup qp = this.viewer.getPopupById (id);

        if (qp == null)
        {

            List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder,nomoreproblems,LanguageStrings.start);

            qp = QuollPopup.messageBuilder ()
                .withViewer (this.viewer)
                .popupId (id)
                .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
                .message (getUILanguageStringProperty (Utils.newList (prefix,text)))
                .build ();

        }

        this.viewer.showPopup (qp);

        return;

    }

    public void next ()
    {

        if (!this.inited)
        {

            this.init (this.panel.getEditor ().getSelection ());

        }

        this.noProblemsLabel.setVisible (false);

        final ProblemFinder _this = this;

        this.getIgnores ();

        this.clearHighlights ();

        TextBlock b = null;

        while ((b = this.iter.next ()) != null)
        {

            Environment.logDebugMessage ("Looking for problems in: " + b);

            int c = this.processTextBlock (b);

            if (c > 0)
            {

                Environment.logDebugMessage ("Got: " + c + " problems for text: " + b);

                return;

            }

        }

        this.addNoProblems ();

        if (this.end > -1)
        {

            String id = this.panel.getObject ().getObjectReference ().asString () + "problemfindernextselectedend";

            QuollPopup qp = this.viewer.getPopupById (id);

            if (qp == null)
            {

                java.util.List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder,nomoreproblems,selected,LanguageStrings.end);

                qp = QuollPopup.messageBuilder ()
                    .withViewer (this.panel.getViewer ())
                    .popupId (id)
                    .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
                    .message (getUILanguageStringProperty (Utils.newList (prefix,text)))
                    .button (QuollButton.builder ()
                                .label (buttons,finish)
                                .onAction (ev ->
                                {

                                    _this.panel.getEditor ().selectRange (_this.start,
                                                                          _this.end);

                                    this.viewer.getPopupById (id).close ();

                                })
                                .build ())
                    .build ();

            }

            this.viewer.showPopup (qp);

            return;

        }

        String id = this.panel.getObject ().getObjectReference ().asString () + "problemfindernextend";

        QuollPopup qp = this.viewer.getPopupById (id);

        if (qp == null)
        {

            List<String> prefix = Arrays.asList (LanguageStrings.project,editorpanel,actions,problemfinder,nomoreproblems,LanguageStrings.end);

            qp = QuollPopup.questionBuilder ()
                .styleClassName (StyleClassNames.PROBLEMFINDER)
                .withViewer (this.panel.getViewer ())
                .popupId (id)
                .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
                .message (getUILanguageStringProperty (Utils.newList (prefix,text)))
                .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)))
                .cancelButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
                .onConfirm (ev ->
                {

                    try
                    {

                        _this.init (0, -1);

                        _this.next ();

                        this.viewer.getPopupById (id).close ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to move back to start",
                                              e);

                        ComponentUtils.showErrorMessage (_this.panel.getViewer (),
                                                         getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                                  //"Unable to move back to start of {chapter}");

                    }

                })
                .onCancel (ev ->
                {

                    _this.reset ();

                })
                .build ();

        }

        this.viewer.showPopup (qp);

    }

    private class TextBlockIterator
    {

        private TextIterator iter = null;
        private Paragraph para = null;
        private Sentence sent = null;
        private int endAt = -1;
        private int startAt = -1;
        private TextBlock current = null;

        public TextBlockIterator (String text,
                                  int    startAt,
                                  int    endAt)
        {

            this.iter = new TextIterator (text);
            this.startAt = startAt;

            this.endAt = endAt;

        }

        public TextBlock next ()
        {

            if (this.para == null)
            {

                this.para = this.iter.getNextClosestParagraphTo (this.startAt);

                if (this.para == null)
                {

                    // At the end.
                    return null;

                }

                if (this.startAt > this.para.getStart ())
                {

                    this.sent = this.para.getNextClosestSentenceTo (this.startAt - this.para.getStart ());

                    return this.sent;

                }

            } else {

                if (this.sent == null)
                {

                    this.sent = this.para.getFirstSentence ();

                } else {

                    this.sent = this.sent.getNext ();

                }

                if (this.sent == null)
                {

                    // Get the next paragraph.
                    this.para = this.para.getNext ();

                } else {

                    if ((this.sent.getAllTextStartOffset () > this.endAt)
                        &&
                        (this.endAt > -1)
                       )
                    {

                        return null;

                    }

                    return this.sent;

                }

                if (this.para == null)
                {

                    // Reached the end;
                    return null;

                }

            }

            if ((this.para.getAllTextStartOffset () > this.endAt)
                &&
                (this.endAt > -1)
               )
            {

                return null;

            }

            return this.para;

        }

        public TextBlock previous ()
        {

            if (this.para == null)
            {

                this.para = this.iter.getPreviousClosestParagraphTo (this.startAt);

                if (this.para == null)
                {

                    return null;

                }

                if (this.startAt > this.para.getEnd ())
                {

                    this.sent = this.para.getLastSentence ();

                } else {

                    this.sent = this.para.getPreviousClosestSentenceTo (this.startAt - this.para.getStart ());

                }

            } else {

                if (this.sent == null)
                {

                    this.para = this.para.getPrevious ();

                    if (this.para == null)
                    {

                        return null;

                    }

                    this.sent = this.para.getLastSentence ();

                } else {

                    this.sent = this.sent.getPrevious ();

                }

                if (this.sent == null)
                {

                    if ((this.startAt > this.para.getAllTextStartOffset ())
                        &&
                        (this.endAt > -1)
                       )
                    {

                        return null;

                    }

                    return this.para;

                }

            }

            if ((this.startAt > this.sent.getAllTextStartOffset ())
                &&
                (this.endAt > 1)
               )
            {

                return null;

            }

            return this.sent;

        }

    }

}
