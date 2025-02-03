package com.quollwriter.text.rules;

import java.util.*;

import javafx.collections.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.util.converter.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.*;

import org.dom4j.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class AdverbRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String speechVerbs = "speechVerbs";

    }

    private Set<String> speechVerbs = new HashSet<> ();

    private TextField newVerbs2 = null;
    private ListView<String> verbs2 = null;

    public AdverbRule ()
    {

    }

    @Override
    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        String sw = DOM4JUtils.attributeValue (root,
                                                 XMLConstants.speechVerbs);

        StringTokenizer t = new StringTokenizer (sw,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            this.speechVerbs.add (t.nextToken ().trim ().toLowerCase ());

        }

    }

    public void setSpeechVerbs (Set<String> verbs)
    {

        this.speechVerbs = verbs;

    }

    public boolean isSpeechVerb (String w)
    {

        if (w == null)
        {

            return false;

        }

        return this.speechVerbs.contains (w.toLowerCase ());

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        StringBuilder b = new StringBuilder ();

        for (String w : this.speechVerbs)
        {

            if (b.length () > 0)
            {

                b.append (",");

            }

            b.append (w);

        }

        root.addAttribute (XMLConstants.speechVerbs,
                           b.toString ());

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        List<Issue>  issues = new ArrayList<> ();

        String adverbWT = String.valueOf (Synonyms.ADVERB);

        List<Word> swords = sentence.getWords ();

        for (Word w : swords)
        {

            if (w.isInDialogue ())
            {

                continue;

            }

            if (this.isSpeechVerb (w.getText ()))
            {

                Word nw = w.getNext ();

                if (nw != null)
                {

                    try
                    {

                        SynonymProvider sp = Environment.getSynonymProvider (Constants.ENGLISH);

                        String wt = null;

                        if (sp != null)
                        {

                            wt = sp.getWordTypes (nw.getText ());

                        }

                        if (wt != null)
                        {

                            // We are only interested in those that are purely adverbs (no other word types)
                            if (wt.equals (adverbWT))
                            {

                                // Maybe check to see if it's after a "

                                // Add an issue.
                                Issue iss = new Issue (String.format (Environment.getUIString (LanguageStrings.problemfinder,
                                                                                               LanguageStrings.issues,
                                                                                               LanguageStrings.adverb,
                                                                                               LanguageStrings.text),
                                                                      nw.getText (),
                                                                      w.getText ()),
                                                        //"Use of adverb: <b>" + nw.getText () +
                                                       //"</b> to modify speech verb: <b>" + w.getText () + "</b>",
                                                       sentence,
                                                       sentence.getAllTextStartOffset () + "-" + nw.getText () + "-" + w.getText (),
                                                       this);
                                issues.add (iss);

                            }

                        }


                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to check for word: " +
                                              nw +
                                              " being an adverb.",
                                              e);

                    }

                }

            }


        }

        return issues;

    }

    @Override
    public Set<Form.Item> getFormItems ()
    {

        final AdverbRule _this = this;

        List<String> pref = Arrays.asList (problemfinder,config,rules,adverb,labels);

        Set<Form.Item> items = new LinkedHashSet<> ();

        VBox b = new VBox ();

        this.newVerbs2 = QuollTextField.builder ()
            .build ();

        b.getChildren ().add (this.newVerbs2);

        QuollLabel l = QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (pref,separate)))
            .styleClassName (StyleClassNames.INFORMATION)
            .build ();

        b.getChildren ().add (l);

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,newspeechverbs)),
                                  b));

        Vector v = new Vector<> (this.speechVerbs);

        Collections.sort (v);

        List<String> _vitems = new ArrayList<> (v);

        ObservableList<String> vitems = FXCollections.observableList (_vitems);

        this.verbs2 = new ListView<> (vitems);
        this.verbs2.getStyleClass ().add (StyleClassNames.ITEMS);
        this.verbs2.getSelectionModel ().setSelectionMode (SelectionMode.MULTIPLE);
        this.verbs2.setEditable (true);
        this.verbs2.setCellFactory (view ->
        {

            TextFieldListCell<String> c = new TextFieldListCell<> ()
            {

                @Override
                public void cancelEdit ()
                {

                    this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
                    super.cancelEdit ();

                }

                @Override
                public void commitEdit (String newVal)
                {

                    this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);

                    int ind = -1;

                    newVal = newVal.trim ();

                    for (int i = 0; i < vitems.size (); i++)
                    {

                        if (newVal.equalsIgnoreCase (vitems.get (i)))
                        {

                            ind = i;
                            break;

                        }

                    }

                    if ((ind > -1)
                        &&
                        (ind != this.getIndex ())
                       )
                    {

                        this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);
                        com.quollwriter.ui.fx.UIUtils.setTooltip (this,
                                            getUILanguageStringProperty (manageitems,table,edit,errors,valueexists));

                        return;

                    }

                    String oldVal = this.getItem ();

                    // TODO Check value here and either allow or prevent.
                    super.commitEdit (newVal);

                    vitems.set (this.getIndex (),
                                newVal);

                    _this.speechVerbs = new LinkedHashSet<> (vitems);

                }

            };

            c.setConverter (new DefaultStringConverter ());

            return c;

        });

        HBox bb = new HBox ();
        bb.getChildren ().add (this.verbs2);

        bb.getChildren ().add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (problemfinder,config,rules,adverb,buttons,removespeechverbs,tooltip))
            .iconName (StyleClassNames.DELETE)
            .onAction (ev ->
            {

                vitems.removeAll (verbs2.getSelectionModel ().getSelectedItems ());

            })
            .build ());

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,speechverbs)),
                                    //"Speech Verbs",
                                  bb));

        return items;

    }

    @Override
    public void updateFromForm ()
    {

        Set<String> verbs = new HashSet<> ();

        String n = this.newVerbs2.getText ();

        if (n != null)
        {

            StringTokenizer t = new StringTokenizer (n,
                                                     ";,");

            while (t.hasMoreTokens ())
            {

                verbs.add (t.nextToken ().trim ().toLowerCase ());

            }

        }

        verbs.addAll (this.verbs2.getItems ());

        this.speechVerbs = verbs;

    }

}
