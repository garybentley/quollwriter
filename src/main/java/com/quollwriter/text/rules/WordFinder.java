package com.quollwriter.text.rules;

import java.util.*;

import javafx.scene.control.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.uistrings.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.QuollTextField;
import com.quollwriter.ui.fx.components.QuollCheckBox;

import org.dom4j.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WordFinder extends AbstractDialogueRule
{

    public class XMLConstants
    {

        public static final String word = "word";

    }

    private String word = null;

    private TextField words2 = null;
    private CheckBox ignoreInDialogueCB2 = null;
    private CheckBox onlyInDialogueCB2 = null;
    private ChoiceBox<String> where2 = null;

    //private TextFormItem words = null;
    private List<Word> tWords = null;
    //private CheckboxFormItem ignoreInDialogueCB = null;
    //private CheckboxFormItem onlyInDialogueCB = null;
    //private ComboBoxFormItem whereCB = null;

    public WordFinder ()
    {

    }

    public boolean isForLanguage (String language)
    {

        return UILanguageStrings.isEnglish (language);

    }

    public String toString ()
    {

        return "rule(word=" + this.word + ")";

    }

    public String getCategory ()
    {

        return Rule.WORD_CATEGORY;

    }

    public void setWord (String w)
    {

        this.word = w;

        this.tWords = TextUtilities.getAsWords (this.word.toLowerCase ());

    }

    @Override
    public String getSummary ()
    {

        if (this.word == null)
        {

            return null;

        }

        return String.format ("\"<b>%s</b>\" %s",
                              this.word,
                              this.getWhereDesc ());

    }

    @Override
    public String getEditFormTitle (boolean add)
    {

        return (add ? Environment.getUIString (LanguageStrings.problemfinder,
                                               LanguageStrings.config,
                                               LanguageStrings.rules,
                                               LanguageStrings.wordfinder,
                                               LanguageStrings.addtitle)
                    : Environment.getUIString (LanguageStrings.problemfinder,
                                               LanguageStrings.config,
                                               LanguageStrings.rules,
                                               LanguageStrings.wordfinder,
                                               LanguageStrings.edittitle));

    }

    private String getWhereDesc ()
    {

        String suffix = "";

        if (this.where != null)
        {

            List<String> pref = new ArrayList ();
            pref.add (LanguageStrings.problemfinder);
            pref.add (LanguageStrings.config);
            pref.add (LanguageStrings.rules);
            pref.add (LanguageStrings.wordfinder);
            pref.add (LanguageStrings.description);

            String s = (this.ignoreInDialogue ? Environment.getUIString (pref,
                                                                         LanguageStrings.suffixes,
                                                                         LanguageStrings.ignoreindialogue) :
                                                Environment.getUIString (pref,
                                                                         LanguageStrings.suffixes,
                                                                         LanguageStrings.onlyindialogue));

            if (this.where.equals (DialogueConstraints.ANYWHERE))
            {

                suffix = String.format (Environment.getUIString (pref,
                                                                 LanguageStrings.anywhere),
                                        s);
                //"anywhere in a sentence";

            } else
            {

                suffix = String.format (Environment.getUIString (pref,
                                                                 LanguageStrings.insentence),
                                        //"at the %s of a sentence",
                                        this.where,
                                        s);

            }
/*
            if (this.ignoreInDialogue)
            {

                suffix += ", ignore in dialogue";

            }

            if (this.onlyInDialogue)
            {

                suffix += ", but only in dialogue";

            }
*/
        }

        return suffix;

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        this.setWord (DOM4JUtils.attributeValue (root,
                                                 XMLConstants.word));

    }

    public String getWord ()
    {

        return this.word;

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.addAttribute (XMLConstants.word,
                           this.word);

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        // Check our list of words.

        List<Issue>  issues = new ArrayList ();

        Set<Integer> inds = sentence.find (this.tWords,
                                           this.getConstraints ());

        if ((inds != null)
            &&
            (inds.size () > 0)
           )
        {

            for (Integer i : inds)
            {

                Word w = sentence.getWord (i);

                int l = w.getText ().length ();

                if (this.tWords.size () > 1)
                {

                    Word nw = w.getWordsAhead (this.tWords.size () - 1);

                    l = nw.getAllTextEndOffset () - w.getAllTextStartOffset ();

                }

                String suffix = "";

                List<String> pref = new ArrayList ();

                pref.add (LanguageStrings.problemfinder);
                pref.add (LanguageStrings.issues);
                pref.add (LanguageStrings.wordfinder);

                if (this.onlyInDialogue)
                {

                    suffix = Environment.getUIString (pref,
                                                      LanguageStrings.suffixes,
                                                      LanguageStrings.onlyindialogue);
                                                      //" (in dialogue)";

                }

                if (!this.where.equals (DialogueConstraints.ANYWHERE))
                {

                    if (this.onlyInDialogue)
                    {

                        suffix = Environment.getUIString (pref,
                                                          LanguageStrings.suffixes,
                                                          LanguageStrings.indialogue);

                    } else {

                        suffix = Environment.getUIString (pref,
                                                          LanguageStrings.suffixes,
                                                          LanguageStrings.notindialogue);

                    }

                    suffix = String.format (suffix,
                                            this.where);

                }

                Issue iss = new Issue (String.format (Environment.getUIString (pref,
                                                                               LanguageStrings.text),
                                                      this.word,
                                                      suffix),
                                        //"Contains: <b>" + this.word + "</b>" + suffix,
                                       sentence,
                                       w.getAllTextStartOffset (),
                                       l,
                                       w.getAllTextStartOffset () + "-" + this.word,
                                       this);
                issues.add (iss);

            }

        }

        return issues;

    }

    public void updateFromForm2 ()
    {

        this.setOnlyInDialogue (this.onlyInDialogueCB2.isSelected ());
        this.setIgnoreInDialogue (this.ignoreInDialogueCB2.isSelected ());

        int ws = this.where2.getSelectionModel ().getSelectedIndex ();

        if (ws == 0)
        {

            this.setWhere (DialogueConstraints.ANYWHERE);

        }

        if (ws == 1)
        {

            this.setWhere (DialogueConstraints.START);

        }

        if (ws == 2)
        {

            this.setWhere (DialogueConstraints.END);

        }

        this.setWord (this.words2.getText ().trim ());

    }

    public void updateFromForm ()
    {
/*
TODO Remove
        this.setOnlyInDialogue (this.onlyInDialogueCB.isSelected ());
        this.setIgnoreInDialogue (this.ignoreInDialogueCB.isSelected ());

        int ws = this.whereCB.getSelectedIndex ();

        if (ws == 0)
        {

            this.setWhere (DialogueConstraints.ANYWHERE);

        }

        if (ws == 1)
        {

            this.setWhere (DialogueConstraints.START);

        }

        if (ws == 2)
        {

            this.setWhere (DialogueConstraints.END);

        }

        this.setWord (this.words.getText ().trim ());
*/
    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        if (d == null)
        {

            return null;

        }

        return Utils.replaceString (d,
                                          "[WORD]",
                                          ((this.word == null) ? "[WORD]" : this.word));

    }

    @Override
    public Set<Form.Item> getFormItems2 ()
    {

        Set<Form.Item> items = new LinkedHashSet<> ();

        List<String> pref = Arrays.asList (problemfinder,config,rules,wordfinder,labels);

        this.words2 = QuollTextField.builder ()
            .text (this.word)
            .build ();

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref, wordphrase)),
                                  this.words2));

        this.where2 = new ChoiceBox<> ();

        this.where2.getItems ().addAll (getUILanguageStringProperty (Utils.newList (pref, anywhere)).getValue (),
                                       getUILanguageStringProperty (Utils.newList (pref, startofsentence)).getValue (),
                                       getUILanguageStringProperty (Utils.newList (pref, endofsentence)).getValue ());

        int sel = 0;

        String loc = this.getWhere ();

        if (loc.equals (DialogueConstraints.START))
        {

            sel = 1;

        }

        if (loc.equals (DialogueConstraints.END))
        {

            sel = 2;

        }

        this.where2.getSelectionModel ().select (sel);

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref, where)),
                                  this.where2));

        this.ignoreInDialogueCB2 = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (pref, ignoreindialogue)))
            .build ();

        items.add (new Form.Item (this.ignoreInDialogueCB2));

        this.onlyInDialogueCB2 = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (pref, onlyindialogue)))
            .build ();

        items.add (new Form.Item (this.onlyInDialogueCB2));

        this.ignoreInDialogueCB2.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv)
            {

                this.onlyInDialogueCB2.setSelected (false);

            }

        });

        this.onlyInDialogueCB2.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv)
            {

                this.ignoreInDialogueCB2.setSelected (false);

            }

        });

        this.ignoreInDialogueCB2.setSelected (this.isIgnoreInDialogue ());
        this.onlyInDialogueCB2.setSelected (this.isOnlyInDialogue ());

        items.add (new Form.Item (this.ignoreInDialogueCB2));

        items.add (new Form.Item (this.onlyInDialogueCB2));

        return items;

    }

    @Override
    // TODO REmove
    public Set<com.quollwriter.ui.forms.FormItem> getFormItems ()
    {

        Set<com.quollwriter.ui.forms.FormItem> items = new LinkedHashSet<> ();

/*
        List<String> pref = new ArrayList ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.config);
        pref.add (LanguageStrings.rules);
        pref.add (LanguageStrings.wordfinder);
        pref.add (LanguageStrings.labels);

        this.words = new TextFormItem (Environment.getUIString (pref,
                                                                LanguageStrings.wordphrase),
                                                                //"Word/Phrase",
                                       this.word);

        items.add (this.words);

        Vector<String> whereVals = new Vector<> ();
        whereVals.add (Environment.getUIString (pref,
                                                LanguageStrings.anywhere));
        //"Anywhere");
        whereVals.add (Environment.getUIString (pref,
                                                LanguageStrings.startofsentence));
        //"Start of sentence");
        whereVals.add (Environment.getUIString (pref,
                                                LanguageStrings.endofsentence));
        //"End of sentence");

        String selected = whereVals.get (0);

        String loc = this.getWhere ();

        if (loc.equals (DialogueConstraints.START))
        {

            selected = whereVals.get (1);

        }

        if (loc.equals (DialogueConstraints.END))
        {

            selected = whereVals.get (2);

        }

        this.whereCB = new ComboBoxFormItem (Environment.getUIString (pref,
                                                                      LanguageStrings.where),
        //"Where",
                                             whereVals,
                                             selected,
                                             null);

        final WordFinder _this = this;

        items.add (this.whereCB);

        this.ignoreInDialogueCB = new CheckboxFormItem (null,
                                                        Environment.getUIString (pref,
                                                                                 LanguageStrings.ignoreindialogue));
                                                        //"Ignore in dialogue");
        this.onlyInDialogueCB = new CheckboxFormItem (null,
                                                      Environment.getUIString (pref,
                                                                               LanguageStrings.onlyindialogue));
                                                      //"Only in dialogue");

        this.ignoreInDialogueCB.addItemListener (new ItemListener ()
        {

            @Override
            public void itemStateChanged (ItemEvent ev)
            {

                if (_this.ignoreInDialogueCB.isSelected ())
                {

                    _this.onlyInDialogueCB.setSelected (false);

                }

            }

        });

        this.onlyInDialogueCB.addItemListener (new ItemListener ()
        {

            @Override
            public void itemStateChanged (ItemEvent ev)
            {

                if (_this.onlyInDialogueCB.isSelected ())
                {

                    _this.ignoreInDialogueCB.setSelected (false);

                }

            }

        });

        this.ignoreInDialogueCB.setSelected (this.isIgnoreInDialogue ());
        this.onlyInDialogueCB.setSelected (this.isOnlyInDialogue ());

        items.add (this.ignoreInDialogueCB);

        items.add (this.onlyInDialogueCB);
*/
        return items;

    }

    public StringProperty getFormError2 ()
    {

        String newWords = this.words2.getText ();

        if ((newWords == null)
            ||
            (newWords.trim ().length () == 0)
           )
        {

            return getUILanguageStringProperty (problemfinder,config,rules,wordfinder,nowordserror);

        }

        return null;

    }

    public String getFormError ()
    {
/*
TODO Remove
        String newWords = this.words.getText ();

        if ((newWords == null)
            ||
            (newWords.trim ().length () == 0)
           )
        {

            return Environment.getUIString (LanguageStrings.problemfinder,
                                            LanguageStrings.config,
                                            LanguageStrings.rules,
                                            LanguageStrings.wordfinder,
                                            LanguageStrings.nowordserror);

        }
*/
        return null;

    }

}
