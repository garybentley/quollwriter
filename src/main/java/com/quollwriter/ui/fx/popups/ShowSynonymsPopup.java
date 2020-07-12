package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.text.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.beans.property.*;
import javafx.scene.input.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.Word;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ShowSynonymsPopup extends PopupContent<AbstractViewer>
{

    private static final String POPUP_ID = "showsynonyms";

    private Word word = null;
    private TextEditor editor = null;
    private TextEditor.Highlight highlight = null;

    public ShowSynonymsPopup (AbstractViewer viewer,
                              Word           word,
                              TextEditor     editor)
    {

        super (viewer);

        this.word = word;
        this.editor = editor;

        VBox b = new VBox ();
        //this.getChildren ().add (new ScrollPane (b));
        this.getChildren ().add (b);

        final ShowSynonymsPopup _this = this;

        this.highlight = this.editor.addHighlight (new IndexRange (this.word.getAllTextStartOffset (),
                                                                   this.word.getAllTextEndOffset ()),
                                                   UserProperties.getSynonymHighlightColor ());

        List<String> prefix = Arrays.asList (synonyms,show);

        Synonyms syns = null;

        try
        {

            syns = this.editor.getSynonymProvider ().getSynonyms (this.word.getText ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to lookup synonyms for: " +
                                  word,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (prefix,actionerror));

            return;

        }

        Map<String, StringProperty> names = new HashMap ();
        names.put (Synonyms.ADJECTIVE + "",
                   getUILanguageStringProperty (Utils.newList (prefix,wordtypes,adjectives)));
                    //"Adjectives");
        names.put (Synonyms.NOUN + "",
                   getUILanguageStringProperty (Utils.newList (prefix,wordtypes,nouns)));
                   //"Nouns");
        names.put (Synonyms.VERB + "",
                   getUILanguageStringProperty (Utils.newList (prefix,wordtypes,verbs)));
                   //"Verbs");
        names.put (Synonyms.ADVERB + "",
                   getUILanguageStringProperty (Utils.newList (prefix,wordtypes,adverbs)));
                   //"Adverbs");
        names.put (Synonyms.OTHER + "",
                   getUILanguageStringProperty (Utils.newList (prefix,wordtypes,other)));
                   //"Other");

        if (syns.words.size () == 0)
        {

            Label l = new Label ();
            l.getStyleClass ().add (StyleClassNames.INFORMATION);
            l.textProperty ().bind (getUILanguageStringProperty (Utils.newList (prefix, nosynonyms)));
            /*
            Label l = UIUtils.createInformationLabel (Environment.getUIString (prefix,
                                                                                LanguageStrings.nosynonyms));
                                                            //"No synonyms found.");
*/
            b.getChildren ().add (l);

            return;

        }

        // Determine what type of word we are looking for.
        for (Synonyms.Part i : syns.words)
        {

            Header h = Header.builder ()
                .title (names.get (i.type + ""))
                .build ();

            b.getChildren ().add (h);

            List<Node> words = new ArrayList<> ();

            for (int x = 0; x < i.words.size (); x++)
            {

                String w = (String) i.words.get (x);

                Text l = new Text ();
                l.getStyleClass ().add (StyleClassNames.LINK);
                l.setText (w);
                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    this.editor.removeHighlight (this.highlight);

                    this.highlight = null;

                    this.editor.replaceText (this.word.getAllTextStartOffset (),
                                             this.word.getAllTextEndOffset (),
                                             w);

                    _this.close ();

                    viewer.fireProjectEvent (ProjectEvent.Type.synonym,
                                             ProjectEvent.Action.replace,
                                             w);

                });

                words.add (l);

                if (x < (i.words.size () - 1))
                {

                    Text sep = new Text ();
                    sep.getStyleClass ().add (StyleClassNames.SEPARATOR);
                    sep.textProperty ().bind (getUILanguageStringProperty (Utils.newList (prefix,wordseparator)));
                    words.add (sep);

                }

            }

            TextFlow tf = new TextFlow (words.toArray (new Node[0]));
            tf.getStyleClass ().add (StyleClassNames.WORDS);
            tf.minHeightProperty ().bind (tf.prefHeightProperty ());

            b.getChildren ().add (new ScrollPane (tf));

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        StringProperty t = getUILanguageStringProperty (Arrays.asList (synonyms,show,title),
                                                        word.getText ());

        QuollPopup p = QuollPopup.builder ()
            .title (t)
            .styleClassName (StyleClassNames.SYNONYMS)
            .styleSheet (StyleClassNames.SYNONYMS)
            .hideOnEscape (true)
            .withClose (true)
            .onClose (() ->
            {

                if (this.highlight != null)
                {

                    this.editor.removeHighlight (this.highlight);

                }

            })
            .content (this)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
