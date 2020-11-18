package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.text.*;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.text.Rule;
import com.quollwriter.text.TextBlock;
import com.quollwriter.text.rules.WordFinder;
import com.quollwriter.text.rules.RuleFactory;
import com.quollwriter.text.rules.ParagraphRule;
import com.quollwriter.text.rules.SentenceRule;

import org.josql.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProblemFinderRuleConfigPopup extends PopupContent<ProjectViewer>
{

    public static final String POPUP_ID = "problemfinderruleconfig";

    private VBox wordsEdit = null;
    private VBox wordsView = null;
    private VBox allWords = null;

    private VBox paraEdit = null;
    private VBox paraView = null;
    private VBox allParas = null;
    private ToolBar paraButs = null;

    private VBox sentEdit = null;
    private VBox sentView = null;
    private VBox allSents = null;
    private ToolBar sentButs = null;

    public ProblemFinderRuleConfigPopup (ProjectViewer viewer)
    {

        super (viewer);

        VBox content = new VBox ();

        TabPane tp = new TabPane ();

        List<String> pref = Arrays.asList (problemfinder,config,tabtitles);

        Tab wordsTab = new Tab ();
        wordsTab.setClosable (false);
        wordsTab.textProperty ().bind (getUILanguageStringProperty (Utils.newList (pref, words)));
        wordsTab.setContent (this.createWordsPanel ());
        wordsTab.getStyleClass ().add (StyleClassNames.WORDS);

        Tab sentTab = new Tab ();
        sentTab.setClosable (false);
        sentTab.textProperty ().bind (getUILanguageStringProperty (Utils.newList (pref, sentence)));
        sentTab.getStyleClass ().add (StyleClassNames.SENTENCES);
        sentTab.setContent (this.createSentencesPanel ());

        Tab paraTab = new Tab ();
        paraTab.setClosable (false);
        paraTab.textProperty ().bind (getUILanguageStringProperty (Utils.newList (pref, paragraph)));
        paraTab.setContent (this.createParagraphsPanel ());
        paraTab.getStyleClass ().add (StyleClassNames.PARAGRAPHS);

        tp.getTabs ().addAll (wordsTab, sentTab, paraTab);

        content.getChildren ().add (tp);

        content.getChildren ().add (QuollButtonBar.builder ()
            .styleClassName (StyleClassNames.BUTTONS)
            .button (QuollButton.builder ()
                .label (buttons,finish)
                .onAction (ev ->
                {

                    viewer.getPopupById (POPUP_ID).close ();

                })
                .build ())
            .build ());

        this.getChildren ().add (content);

    }

    private void removeRuleBox (RuleViewBox box)
    {

        if (box.getRule () instanceof WordFinder)
        {

            this.allWords.getChildren ().remove (box);

        }

        if (box.getRule () instanceof ParagraphRule)
        {

            this.allParas.getChildren ().remove (box);
            this.paraButs.setVisible (this.getParagraphIgnores ().size () > 0);

        }

        if (box.getRule () instanceof SentenceRule)
        {

            this.allSents.getChildren ().remove (box);
            this.sentButs.setVisible (this.getSentenceIgnores ().size () > 0);

        }

    }

    private void addRule (WordFinder r)
    {

        this.editRule (r,
                       true);

    }

    public <E extends TextBlock> void editRule (Rule<E> r)
    {

        this.editRule (r,
                       false);

    }

    private <E extends TextBlock> void editRule (Rule<E>    r,
                           boolean add)
    {

        Pane edit = null;
        Pane view = null;
        Pane addTo = null;

        if (r instanceof SentenceRule)
        {

            edit = this.sentEdit;
            view = this.sentView;
            addTo = this.allSents;

        }

        if (r instanceof ParagraphRule)
        {

            edit = this.paraEdit;
            view = this.paraView;
            addTo = this.allParas;

        }

        // Do this last since a WordFinder is also a SentenceRule.
        if (r instanceof WordFinder)
        {

            edit = this.wordsEdit;
            view = this.wordsView;
            addTo = this.allWords;
        }

        Pane _edit = edit;
        Pane _view = view;
        Pane _addTo = addTo;

        edit.getChildren ().clear ();

        edit.getChildren ().add (Header.builder ()
            .title (problemfinder,config,(add ? addrule : editrule),title)
            .iconClassName (StyleClassNames.EDIT)
            .build ());

        QuollTextField summary = QuollTextField.builder ()
            .text (r.getSummary ())
            .build ();

        QuollTextArea desc = QuollTextArea.builder ()
            .text (r.getDescription ())
            .withViewer (this.viewer)
            .build ();

        Form.Builder b = Form.builder ()
            .confirmButton (buttons,save)
            .cancelButton (buttons,cancel);
        b.item (getUILanguageStringProperty (form,labels,LanguageStrings.summary),
                summary);

        r.getFormItems2 ().stream ()
            .forEach (i -> b.item (i));

        b.item (getUILanguageStringProperty (form,labels,description),
                desc);

        Form form = b.build ();

        form.setOnConfirm (ev ->
        {

            StringProperty error = r.getFormError2 ();

            if (error != null)
            {

                form.showError (error);

                return;

            }

            r.setDescription (desc.getText ().trim ());

            String summ = summary.getText ();

            if (summ == null)
            {

                summ = "";

            } else {

                summ = summ.trim ();

            }

            if (summ.length () == 0)
            {

                summ = r.getSummary ();

            }

            if (summ == null)
            {

                summ = r.getDefaultSummary ();

            }

            if (summ == null)
            {

                form.showError (getUILanguageStringProperty (problemfinder,config,entersummaryerror));

                return;

            }

            r.setSummary (summ);

            r.updateFromForm2 ();

            try
            {

                RuleFactory.saveUserRule (r);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save user rule: " +
                                      r,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (problemfinder,config,saveruleerror));
                                          //"Unable to save rule");
                return;

            }

            // Add a new item at the top.
            RuleViewBox rb = new RuleViewBox (r,
                                              this,
                                              this.viewer);

            if (add)
            {

                _addTo.getChildren ().add (0,
                                           rb);

            } else {

                for (Node n : _addTo.getChildren ())
                {

                    if ((n instanceof RuleViewBox)
                        &&
                        (n.getUserData ().equals (r))
                       )
                    {

                        RuleViewBox rn = (RuleViewBox) n;

                        rn.update ();

                    }

                }

            }

            _edit.setVisible (false);
            _view.setVisible (true);
            _view.toFront ();

        });

        form.setOnCancel (ev ->
        {

            _edit.setVisible (false);
            _view.setVisible (true);
            _view.toFront ();

        });

        edit.getChildren ().add (form);

        edit.toFront ();
        view.setVisible (false);

        UIUtils.forceRunLater (() ->
        {
            _edit.setVisible (true);

            this.requestLayout ();
        });

    }

    private Node createSentencesPanel ()
    {

        ProblemFinderRuleConfigPopup _this = this;

        StackPane sentWrapper = new StackPane ();

        this.sentView = new VBox ();
        this.sentView.getStyleClass ().add (StyleClassNames.VIEW);
        this.sentView.managedProperty ().bind (this.sentView.visibleProperty ());
        this.sentEdit = new VBox ();
        this.sentEdit.getStyleClass ().add (StyleClassNames.EDIT);
        this.sentEdit.managedProperty ().bind (this.sentEdit.visibleProperty ());
        this.sentEdit.setVisible (false);

        QuollMenuButton addRule = QuollMenuButton.builder ()
            .iconName (StyleClassNames.ADD)
            .tooltip (getUILanguageStringProperty (problemfinder,config,addrule,sentence,buttons,add,tooltip))
            .items (() ->
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                List<Rule> igs = this.getSentenceIgnores ();

                for (Rule r : igs)
                {

                    BasicHtmlTextFlow l = BasicHtmlTextFlow.builder ()
                        //.tooltip (getUILanguageStringProperty (problemfinder,config,addrule,paragraph,rulebox,tooltip))
                        .text (r.getSummary ())
                        .build ();

                    CustomMenuItem mi = new CustomMenuItem (BasicHtmlTextFlow.builder ()
                        .styleClassName (StyleClassNames.ADD)
                        //.tooltip (getUILanguageStringProperty (problemfinder,config,addrule,paragraph,rulebox,tooltip))
                        .text (r.getSummary ())
                        .build ());
                    mi.setHideOnClick (true);
                    mi.setOnAction (ev ->
                    {

                        RuleViewBox rb = new RuleViewBox (r,
                                                          this,
                                                          this.viewer);

                        _this.allSents.getChildren ().add (0,
                                                           rb);

                        _this.removeIgnore (r,
                                            (_this.isProjectIgnore (r) ? RuleFactory.PROJECT : RuleFactory.USER));

                    });

                    items.add (mi);

                }

                return items;

            })
            .build ();

        this.sentButs = new ToolBar ();
        this.sentButs.getStyleClass ().add (StyleClassNames.BUTTONS);
        this.sentButs.managedProperty ().bind (this.sentButs.visibleProperty ());
        this.sentButs.setVisible (this.getSentenceIgnores ().size () > 0);

        this.sentButs.getItems ().add (addRule);

        this.sentView.getChildren ().add (this.sentButs);

        this.allSents = new VBox ();
        this.allSents.getStyleClass ().add (StyleClassNames.RULES);

        List<Rule> sentRules = RuleFactory.getSentenceRules ();

        if (sentRules != null)
        {

            Map<String, String> ignores = RuleFactory.getIgnores (RuleFactory.ALL,
                                                                  this.viewer.getProject ().getProperties ());

            for (Rule r : sentRules)
            {

                if (ignores.containsKey (r.getId ()))
                {

                    continue;

                }

                RuleViewBox rb = new RuleViewBox (r,
                                                  this,
                                                  this.viewer);
                this.allSents.getChildren ().add (rb);

            }

        }

        this.sentView.getChildren ().add (new ScrollPane (this.allSents));

        sentWrapper.getChildren ().addAll (this.sentView, this.sentEdit);

        this.sentView.toFront ();

        return sentWrapper;

    }

    private Node createParagraphsPanel ()
    {

        ProblemFinderRuleConfigPopup _this = this;

        StackPane paraWrapper = new StackPane ();

        this.paraView = new VBox ();
        this.paraView.getStyleClass ().add (StyleClassNames.VIEW);
        this.paraView.managedProperty ().bind (this.paraView.visibleProperty ());
        this.paraEdit = new VBox ();
        this.paraEdit.getStyleClass ().add (StyleClassNames.EDIT);
        this.paraEdit.managedProperty ().bind (this.paraEdit.visibleProperty ());
        this.paraEdit.setVisible (false);

        QuollMenuButton addRule = QuollMenuButton.builder ()
            .iconName (StyleClassNames.ADD)
            .tooltip (getUILanguageStringProperty (problemfinder,config,addrule,paragraph,buttons,add,tooltip))
            .items (() ->
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                List<Rule> igs = this.getParagraphIgnores ();

                for (Rule r : igs)
                {

                    BasicHtmlTextFlow l = BasicHtmlTextFlow.builder ()
                        //.tooltip (getUILanguageStringProperty (problemfinder,config,addrule,paragraph,rulebox,tooltip))
                        .text (r.getSummary ())
                        .build ();

                    CustomMenuItem mi = new CustomMenuItem (BasicHtmlTextFlow.builder ()
                        .styleClassName (StyleClassNames.ADD)
                        //.tooltip (getUILanguageStringProperty (problemfinder,config,addrule,paragraph,rulebox,tooltip))
                        .text (r.getSummary ())
                        .build ());
                    mi.setHideOnClick (true);
                    mi.setOnAction (ev ->
                    {

                        RuleViewBox rb = new RuleViewBox (r,
                                                          this,
                                                          this.viewer);

                        _this.allParas.getChildren ().add (0,
                                                           rb);

                        _this.removeIgnore (r,
                                            (_this.isProjectIgnore (r) ? RuleFactory.PROJECT : RuleFactory.USER));

                    });

                    items.add (mi);

                }

                return items;

            })
            .build ();

        this.paraButs = new ToolBar ();
        this.paraButs.getStyleClass ().add (StyleClassNames.BUTTONS);
        this.paraButs.managedProperty ().bind (this.paraButs.visibleProperty ());
        this.paraButs.setVisible (this.getParagraphIgnores ().size () > 0);

        this.paraButs.getItems ().add (addRule);

        this.paraView.getChildren ().add (this.paraButs);

        this.allParas = new VBox ();
        this.allParas.getStyleClass ().add (StyleClassNames.RULES);

        List<Rule> paraRules = RuleFactory.getParagraphRules ();

        if (paraRules != null)
        {

            Map<String, String> ignores = RuleFactory.getIgnores (RuleFactory.ALL,
                                                                  this.viewer.getProject ().getProperties ());

            for (Rule r : paraRules)
            {

                if (ignores.containsKey (r.getId ()))
                {

                    continue;

                }

                RuleViewBox rb = new RuleViewBox (r,
                                                  this,
                                                  this.viewer);
                this.allParas.getChildren ().add (rb);

            }

        }

        this.paraView.getChildren ().add (new ScrollPane (this.allParas));

        paraWrapper.getChildren ().addAll (this.paraView, this.paraEdit);

        this.paraView.toFront ();

        return paraWrapper;

    }

    private Node createWordsPanel ()
    {

        StackPane wordsWrapper = new StackPane ();

        this.wordsView = new VBox ();
        this.wordsView.getStyleClass ().add (StyleClassNames.VIEW);
        this.wordsView.managedProperty ().bind (this.wordsView.visibleProperty ());
        this.wordsEdit = new VBox ();
        this.wordsEdit.getStyleClass ().add (StyleClassNames.EDIT);
        this.wordsEdit.managedProperty ().bind (this.wordsEdit.visibleProperty ());
        this.wordsEdit.setVisible (false);

        Button addWordRule = QuollButton.builder ()
            .iconName (StyleClassNames.ADD)
            .tooltip (getUILanguageStringProperty (problemfinder,config,addrule,words,buttons,add,tooltip))
            .onAction (ev ->
            {

                //this.wordsEdit.toFront ();

                WordFinder wf = new WordFinder ();
                wf.setUserRule (true);

                this.addRule (wf);

            })
            .build ();

        ToolBar buts = new ToolBar ();
        buts.getStyleClass ().add (StyleClassNames.BUTTONS);

        buts.getItems ().add (addWordRule);

        this.wordsView.getChildren ().add (buts);

        this.allWords = new VBox ();
        this.allWords.getStyleClass ().add (StyleClassNames.RULES);

        // Get all the "word" rules.
        List<Rule> wordRules = RuleFactory.getWordRules ();

        Query q = new Query ();

        try
        {

            q.parse ("SELECT * FROM com.quollwriter.text.rules.WordFinder ORDER BY word.toLowerCase");

            QueryResults qr = q.execute (wordRules);

            wordRules = (List<Rule>) qr.getResults ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to sort word rules",
                                  e);

            // Just carry on.

        }

        if (wordRules != null)
        {

            Map<String, String> ignores = RuleFactory.getIgnores (RuleFactory.ALL,
                                                                  this.viewer.getProject ().getProperties ());

            for (Rule r : wordRules)
            {

                if (ignores.containsKey (r.getId ()))
                {

                    continue;

                }

                RuleViewBox rb = new RuleViewBox (r,
                                                  this,
                                                  this.viewer);

                this.allWords.getChildren ().add (rb);

            }

        }

        this.wordsView.getChildren ().add (new ScrollPane (this.allWords));

        wordsWrapper.getChildren ().addAll (this.wordsView, this.wordsEdit);

        this.wordsView.toFront ();

        return wordsWrapper;

    }

    public void confirmRuleRemoval (Rule     rule,
                                    Runnable onRemove)
    {

        // TODO

    }

    private List<Rule> getParagraphIgnores ()
    {

        List<Rule> rules = new ArrayList<> ();

        Iterator<String> iter = RuleFactory.getIgnores (RuleFactory.ALL,
                                                        this.viewer.getProject ().getProperties ()).keySet ().iterator ();

        while (iter.hasNext ())
        {

            Rule r = RuleFactory.getRuleById (iter.next ());

            if (r == null)
            {

                continue;

            }

            if (!r.getCategory ().equals (Rule.PARAGRAPH_CATEGORY))
            {

                continue;

            }

            rules.add (r);

        }

        return rules;

    }

    private List<Rule> getSentenceIgnores ()
    {

        List<Rule> rules = new ArrayList<> ();

        Iterator<String> iter = RuleFactory.getIgnores (RuleFactory.ALL,
                                                        this.viewer.getProject ().getProperties ()).keySet ().iterator ();

        while (iter.hasNext ())
        {

            Rule r = RuleFactory.getRuleById (iter.next ());

            if (r == null)
            {

                continue;

            }

            if (!r.getCategory ().equals (Rule.SENTENCE_CATEGORY))
            {

                continue;

            }

            rules.add (r);

        }

        return rules;

    }

    private boolean isProjectIgnore (Rule r)
    {

        return RuleFactory.getIgnores (RuleFactory.PROJECT,
                                       this.viewer.getProject ().getProperties ()).containsKey (r.getId ());

    }

    private void removeIgnore (Rule r,
                               int  type)
    {

        RuleFactory.removeIgnore (r,
                                  type,
                                  this.viewer.getProject ().getProperties ());

        this.sentButs.setVisible (this.getSentenceIgnores ().size () != 0);

        this.paraButs.setVisible (this.getParagraphIgnores ().size () != 0);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (problemfinder,config,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.PROBLEMFINDERCONFIG)
            .styleSheet (StyleClassNames.PROBLEMFINDERCONFIG)
            .headerIconClassName (StyleClassNames.PROBLEMFINDER)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

    private static class RuleViewBox extends VBox
    {

//        private BasicHtmlTextFlow info = null;
//        private BasicHtmlTextFlow desc = null;
        private QuollTextView info = null;
        private QuollTextView desc = null;
        private SimpleStringProperty infoText = null;
        private SimpleStringProperty descText = null;
        private Rule rule = null;

        public RuleViewBox (Rule                         r,
                            ProblemFinderRuleConfigPopup conf,
                            ProjectViewer                viewer)
        {

            this.rule = r;
            this.setUserData (r);
            this.getStyleClass ().add (StyleClassNames.RULE);

            // TODO Move to the Rule.
            this.infoText = new SimpleStringProperty (r.getSummary ());
            this.descText = new SimpleStringProperty (r.getDescription ());

            this.info = QuollTextView.builder ()
                //BasicHtmlTextFlow.builder ()
                .text (this.infoText)
                .styleClassName (StyleClassNames.SUMMARY)
                .inViewer (viewer)
                .build ();
            HBox.setHgrow (this.info,
                           Priority.ALWAYS);
            this.desc = QuollTextView.builder ()
                //BasicHtmlTextFlow.builder ()
                .text (this.descText)
                .styleClassName (StyleClassNames.DESCRIPTION)
                //.withHandler (viewer)
                .inViewer (viewer)
                .build ();
            this.desc.managedProperty ().bind (this.desc.visibleProperty ());
            this.desc.setVisible (false);

            HBox main = new HBox ();
            main.getStyleClass ().add (StyleClassNames.DETAILS);
            ToolBar buts = new ToolBar ();
            buts.getStyleClass ().add (StyleClassNames.BUTTONS);

            buts.getItems ().add (QuollButton.builder ()
                .tooltip (problemfinder,config,rulebox,buttons,find,tooltip)
                .iconName (StyleClassNames.FIND)
                .onAction (ev ->
                {

                    viewer.showProblemFinderRuleSideBar (this.rule);

                })
                .build ());

            buts.getItems ().add (QuollButton.builder ()
                .tooltip (problemfinder,config,rulebox,buttons,edit,tooltip)
                .iconName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    conf.editRule (rule);

                })
                .build ());

            buts.getItems ().add (QuollButton.builder ()
                .tooltip (problemfinder,config,rulebox,buttons,LanguageStrings.info,tooltip)
                .iconName (StyleClassNames.INFORMATION)
                .onAction (ev ->
                {

                    this.desc.setVisible (!this.desc.isVisible ());

                })
                .build ());

            buts.getItems ().add (QuollButton.builder ()
                .tooltip (problemfinder,config,rulebox,buttons,delete,tooltip)
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    String id = "problemfinder" + this.rule.getId () + "delete";

                    if (viewer.getPopupById (id) == null)
                    {

                        QuollPopup qp = QuollPopup.messageBuilder ()
                            .withViewer (viewer)
                            .styleClassName (StyleClassNames.DELETE)
                            .title (problemfinder,config,removerule,title)
                            .removeOnClose (true)
                            .popupId (id)
                            .message (getUILanguageStringProperty (Arrays.asList (problemfinder,config,removerule,text),
                                                                   this.rule.getSummary ()))
                            .button (QuollButton.builder ()
                                .label (problemfinder,config,removerule,buttons,thisproject)
                                .buttonType (ButtonBar.ButtonData.APPLY)
                                .onAction (eev ->
                                {

                                    RuleFactory.addIgnore (r,
                                                           RuleFactory.PROJECT,
                                                           viewer.getProject ().getProperties ());

                                    conf.removeRuleBox (this);

                                    Environment.fireUserProjectEvent (conf,
                                                                      ProjectEvent.Type.problemfinder,
                                                                      ProjectEvent.Action.removerule,
                                                                      this.rule);

                                    viewer.getPopupById (id).close ();

                                })
                                .build ())
                            .button (QuollButton.builder ()
                                .label (problemfinder,config,removerule,buttons,allprojects)
                                .buttonType (ButtonBar.ButtonData.APPLY)
                                .onAction (eev ->
                                {

                                    RuleFactory.addIgnore (r,
                                                           RuleFactory.USER,
                                                           viewer.getProject ().getProperties ());

                                    conf.removeRuleBox (this);

                                    Environment.fireUserProjectEvent (conf,
                                                                      ProjectEvent.Type.problemfinder,
                                                                      ProjectEvent.Action.removerule,
                                                                      this.rule);

                                    viewer.getPopupById (id).close ();

                                })
                                .build ())
                            .button (QuollButton.builder ()
                                .label (buttons,cancel)
                                .onAction (eev ->
                                {

                                    viewer.getPopupById (id).close ();

                                })
                                .build ())
                            .build ();

                        viewer.showPopup (qp,
                                               this,
                                               Side.BOTTOM);

                    }

                })
                .build ());

            main.getChildren ().addAll (this.info, buts);
            this.getChildren ().addAll (main, this.desc);

        }

        public Rule getRule ()
        {

            return this.rule;

        }

        public void update ()
        {

            this.descText.setValue (this.rule.getDescription ());
            this.infoText.setValue (this.rule.getSummary ());

        }

    }

}
