package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.event.*;

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

public class ChapterFindResultsBox extends FindResultsBox<AbstractProjectViewer>
{

    private Map<Chapter, List<SentenceMatches>> snippets = null;
    private Set<TextEditor.Highlight> highlightIds = new HashSet<> ();
    private TextEditor highlightedEditor = null;
    private Chapter highlightedChapter = null;
    private QuollTreeView<Object> tree = null;
    private AccordionItem acc = null;
    private EventHandler<MouseEvent> mouseList = null;
    private EventHandler<KeyEvent> keyList = null;

    public ChapterFindResultsBox (Map<Chapter, List<SentenceMatches>> snippets,
                                  AbstractProjectViewer               viewer)
    {

        super (viewer,
               null);

        this.snippets = snippets;

        this.mouseList = ev ->
        {

            this.clearHighlight ();

        };

        this.keyList = ev ->
        {

            this.clearHighlight ();

        };

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

        StringProperty tProp = new SimpleStringProperty ();
        tProp.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            return String.format ("%1$s (%2$s)",
                                  getUILanguageStringProperty (objectnames,plural,Chapter.OBJECT_TYPE).getValue (),
                                  Environment.formatNumber (this.snippets.size ()));

        }));

        this.acc = AccordionItem.builder ()
            .title (tProp)
            .styleClassName (Chapter.OBJECT_TYPE)
            .openContent (this.tree)
            .build ();

        return this.acc;

    }

    private QuollTreeView<Object> createTree ()
    {

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

            if (n instanceof SentenceMatches)
            {

                SentenceMatches s = (SentenceMatches) n;

                String value = s.getSentence ().getText ();

                QuollLabel l = QuollLabel.builder ()
                    .styleClassName (StyleClassNames.SNIPPET)
                    .label (new SimpleStringProperty (value))
                    .build ();

                l.addEventHandler (MouseEvent.MOUSE_PRESSED,
                                   ev ->
                {

                    if (ev.isPopupTrigger ())
                    {

                        return;

                    }

                    TreeItem<Object> parent = treeItem.getParent ();

                    this.showSnippet ((Chapter) parent.getValue (),
                                      s);

                });

                return l;

            }

            throw new IllegalStateException ("How did we get here?");

        };

        // Create the model.
        TreeItem<Object> root = new TreeItem<> ();
        root.setValue (this.viewer.getProject ());

        for (Chapter c : this.snippets.keySet ())
        {

            TreeItem<Object> ci = new TreeItem<> ();
            ci.setValue (c);
            root.getChildren ().add (ci);

            List<SentenceMatches> segs = this.snippets.get (c);

            for (SentenceMatches s : segs)
            {

                TreeItem<Object> ii = new TreeItem<> ();
                ii.setValue (s);
                ci.getChildren ().add (ii);

            }

        }

        QuollTreeView tree = new QuollTreeView<> ();
        tree.setShowRoot (false);
        tree.getStyleClass ().add (StyleClassNames.CHAPTER);
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

            this.highlightedEditor.removeEventHandler (KeyEvent.KEY_RELEASED,
                                                       this.keyList);

            this.highlightedEditor.removeEventHandler (MouseEvent.MOUSE_RELEASED,
                                                       this.mouseList);

            if (this.viewer.getEditorForChapter (this.highlightedChapter) != null)
            {

                this.highlightIds.stream ()
                    .forEach (i -> this.highlightedEditor.removeHighlight (i));

            }
            this.highlightIds.clear ();

            this.highlightedChapter = null;
            this.highlightedEditor = null;

        }

    }

    public void showSnippet (final Chapter         c,
                             final SentenceMatches s)
    {

        this.clearHighlight ();

        this.viewer.viewObject (c,
                                () ->
        {

            ChapterEditorPanelContent p = this.viewer.getEditorForChapter (c);

            try
            {

                int si = s.getSentence ().getAllTextStartOffset ();

                p.scrollToTextPosition (si,
                                        () ->
                {

                    this.highlightIds.clear ();

                    int ml = s.getMatch ().length ();

                    for (Integer i : s.getIndices ())
                    {

                        IndexRange r = new IndexRange (s.getSentence ().getWord (i).getAllTextStartOffset (),
                                                       s.getSentence ().getWord (i).getAllTextEndOffset ());

                        this.highlightIds.add (p.getEditor ().addHighlight (r,
                                                                            UserProperties.getFindHighlightColor ()));

                    }

                    this.highlightedChapter = c;
                    this.highlightedEditor = p.getEditor ();

                    this.highlightedEditor.addEventHandler (KeyEvent.KEY_RELEASED,
                                                            this.keyList);

                    this.highlightedEditor.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                                            this.mouseList);

                });

            } catch (Exception e) {

                Environment.logError ("Unable to scroll to: " + s.getSentence ().getAllTextStartOffset (),
                                      e);

                return;

            }

        });

    }

}
