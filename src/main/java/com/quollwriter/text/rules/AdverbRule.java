package com.quollwriter.text.rules;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import javafx.collections.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.util.converter.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;

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
    private JTextField          newVerbs = null;
    private DefaultListModel    listModel = null;

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
                                              " being an adverb.");

                    }

                }

            }


        }

        return issues;

    }

    @Override
    public Set<Form.Item> getFormItems2 ()
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
    public Set<FormItem> getFormItems ()
    {

        final AdverbRule _this = this;

        List<String> pref = new ArrayList<> ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.config);
        pref.add (LanguageStrings.rules);
        pref.add (LanguageStrings.adverb);
        pref.add (LanguageStrings.labels);

        Set<FormItem> items = new LinkedHashSet<> ();

        Box b = new Box (BoxLayout.Y_AXIS);

        this.newVerbs = com.quollwriter.ui.UIUtils.createTextField ();

        b.add (newVerbs);

        JLabel label = new JLabel (Environment.getUIString (pref,
                                                                LanguageStrings.separate));
                                   //"(separate with , or ;)");
        label.setBorder (com.quollwriter.ui.UIUtils.createPadding (0, 5, 0, 0));

        b.add (label);

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.newspeechverbs),
                                    //"New Speech Verbs",
                                    b));

        Vector v = new Vector<> (this.speechVerbs);

        Collections.sort (v);

        this.listModel = new DefaultListModel ();

        for (int i = 0; i < v.size (); i++)
        {

            this.listModel.addElement (v.get (i));

        }

        b = new Box (BoxLayout.X_AXIS);

        final JList verbs = new JList<> (this.listModel);

        verbs.setVisibleRowCount (5);
        verbs.setMaximumSize (verbs.getPreferredSize ());

        b.add (new JScrollPane (verbs));

        b.add (Box.createHorizontalStrut (5));

        Box bb = new Box (BoxLayout.Y_AXIS);

        List<JComponent> buts = new ArrayList<> ();

        buts.add (com.quollwriter.ui.UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                        Constants.ICON_MENU,
                                        Environment.getUIString (LanguageStrings.problemfinder,
                                                                 LanguageStrings.config,
                                                                 LanguageStrings.rules,
                                                                 LanguageStrings.adverb,
                                                                 LanguageStrings.buttons,
                                                                 LanguageStrings.removespeechverbs,
                                                                 LanguageStrings.tooltip),
                                        //"Click to remove the selected Speech Verbs",
                                        new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                // Get the selected items, remove them from the model.
                int[] inds = verbs.getSelectedIndices ();

                if (inds != null)
                {

                    for (int i = inds.length - 1; i > -1; i--)
                    {

                        _this.listModel.remove (inds[i]);

                    }

                }

            }

        }));

/*

        ImagePanel del = new ImagePanel (Environment.getIcon ("delete",
                                                              Constants.ICON_MENU),
                                         null);
        del.setBorder (null);
        del.setOpaque (false);
        del.setToolTipText ("Click to remove the selected Speech Verbs");
        com.quollwriter.ui.UIUtils.setAsButton (del);
        del.setAlignmentY (Component.TOP_ALIGNMENT);

        del.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    // Get the selected items, remove them from the model.
                    int[] inds = verbs.getSelectedIndices ();

                    if (inds != null)
                    {

                        for (int i = inds.length - 1; i > -1; i--)
                        {

                            _this.listModel.remove (inds[i]);

                        }

                    }

                }

            });
*/
        bb.add (com.quollwriter.ui.UIUtils.createButtonBar (buts));
        bb.add (Box.createVerticalGlue ());

        b.add (bb);
        b.add (Box.createHorizontalGlue ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.speechverbs),
                                    //"Speech Verbs",
                                    b));

        return items;

    }

    @Override
    public String getFormError ()
    {

        return null;

    }

    @Override
    public void updateFromForm2 ()
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

    public void updateFromForm ()
    {

        // Reset the speech verbs.
        Set<String> verbs = new HashSet<> ();

        String n = this.newVerbs.getText ();

        if (n != null)
        {

            StringTokenizer t = new StringTokenizer (n,
                                                     ";,");

            while (t.hasMoreTokens ())
            {

                verbs.add (t.nextToken ().trim ().toLowerCase ());

            }

        }

        Enumeration en = this.listModel.elements ();

        while (en.hasMoreElements ())
        {

            verbs.add ((String) en.nextElement ());

        }

        this.speechVerbs = verbs;

    }

}
