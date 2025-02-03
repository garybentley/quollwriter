package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.sidebars.*;

import com.quollwriter.text.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class ChapterProblemResultsBox extends FindResultsBox<ProjectViewer>
{

    private Map<Chapter, Set<Issue>> problems = null;
    private TextEditor.Highlight highlightId = null;
    private TextEditor.Highlight lineHighlightId = null;
    private TextEditor highlightedEditor = null;
    private QuollTreeView<Object> tree = null;

    public ChapterProblemResultsBox (ProjectViewer            viewer,
                                     Map<Chapter, Set<Issue>> problems)
    {

        super (viewer,
               null);

        this.problems = problems;

    }

    @Override
    public void dispose ()
    {

        this.clearHighlight ();

    }

    public QuollTreeView<Object> getTree ()
    {

        if (this.tree == null)
        {

            this.tree = this.createTree ();

        }

        return this.tree;

    }

    @Override
    public Node getContent ()
    {

        if (this.tree == null)
        {

            this.tree = this.getTree ();

        }

        return this.tree;

    }

    private void saveIgnores (Set<Issue> ignore,
                              Chapter    c)
    {

        c.getProblemFinderIgnores ().addAll (ignore);

        try
        {

            this.viewer.saveProblemFinderIgnores (c);

            //this.sidebar.find ();

            this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                          ProjectEvent.Action.ignore);

        } catch (Exception e) {

            Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                  c,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (project,LanguageStrings.sidebar,problemfinder,LanguageStrings.ignore,actionerror));
                                      //"Unable to save ignore.");

        }

    }

    private QuollTreeView<Object> createTree ()
    {

        final ChapterProblemResultsBox _this = this;

        Function<TreeItem<Object>, Node> cellProvider = (treeItem) ->
        {

            Object n = treeItem.getValue ();

            if (n instanceof Project)
            {

                return new Label ();

            }

            if (n instanceof Chapter)
            {

                Chapter c = (Chapter) n;

                QuollLabel l = QuollLabel.builder ()
                    .styleClassName (c.getObjectType ())
                    .build ();

                l.textProperty ().bind (UILanguageStringsManager.createStringBinding (() ->
                {

                    String t = "%1$s (%2$s)";

                    return String.format (t,
                                          c.getName (),
                                          treeItem.getChildren ().size ());

                },
                treeItem.getChildren (),
                c.nameProperty ()));

                List<String> mprefix = Arrays.asList (project,LanguageStrings.sidebar,problemfinder,results,treepopupmenu,items);

                ContextMenu cm = new ContextMenu ();

                cm.getItems ().add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.IGNORE)
                    .label (getUILanguageStringProperty (Utils.newList (mprefix, ignoreall)))
                    .onAction (ev ->
                    {

                        Set<Issue> issues = treeItem.getChildren ().stream ()
                            .map (ti -> (Issue) ti.getValue ())
                            .collect (Collectors.toSet ());

                        this.saveIgnores (issues,
                                          c);

                        this.tree.removeObject (treeItem);

                    })
                    .build ());

                l.setContextMenu (cm);

                l.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                   ev ->
                {

                    if (ev.isPopupTrigger ())
                    {

                        return;

                    }

                    this.tree.toggleOpen (treeItem);

                });

                return l;

            }

            if (n instanceof Issue)
            {

                Issue i = (Issue) n;

                TextBlock b = i.getTextBlock ();

                String value = null;

                if (b instanceof Paragraph)
                {

                    value = ((Paragraph) b).getFirstSentence ().getText ();

                } else {

                    value = b.getText ();

                }

                QuollLabel l = QuollLabel.builder ()
                    .styleClassName (StyleClassNames.ISSUE)
                    .label (new SimpleStringProperty (value))
                    //.tooltip (new SimpleStringProperty (i.getDescription ()))
                    .build ();

                Tooltip t = new Tooltip ();
                t.setGraphic (QuollTextView.builder ()
                    .text (i.getDescription ())
                    .build ());

                l.setTooltip (t);

                List<String> mprefix = Arrays.asList (project,LanguageStrings.sidebar,problemfinder,results,treepopupmenu,items);

                ContextMenu cm = new ContextMenu ();

                cm.getItems ().add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.IGNORE)
                    .label (getUILanguageStringProperty (Utils.newList (mprefix, ignore)))
                    .onAction (ev ->
                    {

                        Chapter c = i.getChapter ();

                        Set<Issue> issues = new HashSet<> ();

                        issues.add (i);

                        this.tree.removeObject (treeItem.getValue ());

                        this.clearHighlight ();

                        this.saveIgnores (issues,
                                          c);

                    })
                    .build ());

                l.setContextMenu (cm);

                l.addEventHandler (MouseEvent.MOUSE_PRESSED,
                                   ev ->
                {

                    if (ev.isPopupTrigger ())
                    {

                        return;

                    }

                    this.showChapterIssue (i);

                });

                return l;

            }

            throw new IllegalStateException ("How did we get here?");

        };

        // Create the model.
        TreeItem<Object> root = new TreeItem<> ();
        root.setValue (this.viewer.getProject ());

        for (Chapter c : this.problems.keySet ())
        {

            TreeItem<Object> ci = new TreeItem<> ();
            ci.setValue (c);
            root.getChildren ().add (ci);

            Set<Issue> issues = this.problems.get (c);

//            Set<Issue> ignores = c.getProblemFinderIgnores ();
            for (Issue iss : issues)
            {
/*
                if (ignores.contains (iss))
                {

                    continue;

                }
*/
                TreeItem<Object> ii = new TreeItem<> ();
                ii.setValue (iss);
                ci.getChildren ().add (ii);

            }

        }

        QuollTreeView tree = new QuollTreeView<> ();
        tree.setShowRoot (false);
        tree.getStyleClass ().add (StyleClassNames.PROBLEMFINDER);
        tree.setCellProvider (cellProvider);
        tree.setRoot (root);

        if (UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME))
        {

            tree.expandAll ();

        }

        return tree;

    }

    public void clearHighlight ()
    {

        if (this.highlightedEditor != null)
        {

            this.highlightedEditor.removeHighlight (this.highlightId);
            this.highlightedEditor.removeHighlight (this.lineHighlightId);

        }

    }

    public void showChapterIssue (Issue   i)
    {

        this.clearHighlight ();

        this.viewer.editChapter (i.getChapter (),
                                 () ->
        {

            ChapterEditorPanelContent p = this.viewer.getEditorForChapter (i.getChapter ());

            p.scrollToTextPosition (i.getStartIssuePosition (),
                                    () ->
            {

                TextBlock textBlock = i.getTextBlock ();

                final TextEditor ed = p.getEditor ();

                int end = textBlock.getAllTextEndOffset () + 1;

                if (textBlock instanceof Sentence)
                {

                    end = ((Sentence) textBlock).getLastWord ().getAllTextEndOffset ();

                }

                this.lineHighlightId = ed.addHighlight (new IndexRange (textBlock.getAllTextStartOffset (),
                                                                        textBlock.getAllTextEndOffset ()),
                                                        UserProperties.getProblemFinderBlockHighlightColor ());

                this.highlightId = ed.addHighlight (new IndexRange (i.getStartIssuePosition (),
                                                                    i.getEndIssuePosition ()),
                                                    UserProperties.getProblemFinderIssueHighlightColor ());

                this.highlightedEditor = ed;

            });

        });

    }

}
