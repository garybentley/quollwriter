package com.quollwriter.ui.actionHandlers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.synonyms.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.HyperlinkAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.QTextEditor;

public class FindSynonymsActionHandler extends ActionAdapter
{

    private QPopup                popup = null;
    private Object                highlight = null;
    private QTextEditor           editor = null;
    private Word word = null;

    public FindSynonymsActionHandler (Word        word,
                                      QTextEditor editor)
    {

        this.word = word;
        this.editor = editor;

        AbstractViewer viewer = UIUtils.getViewer (this.editor);

        if (viewer == null)
        {

            Environment.logError ("Unable to show synonyms for editor that has no viewer parent.");

            return;

        }

        final FindSynonymsActionHandler _this = this;

        // Show a panel of all the items.
        this.popup = new QPopup (String.format (Environment.getUIString (LanguageStrings.synonyms,
                                                                         LanguageStrings.show,
                                                                         LanguageStrings.title),
                                                word.getText ()),
                                //"Synonyms for: " + word,
                                 Environment.getIcon (Constants.FIND_ICON_NAME,
                                                      Constants.ICON_POPUP),
                                 null);

        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              Environment.getUIString (LanguageStrings.actions,
                                                                       LanguageStrings.clicktoclose),
                                            //"Click to close",
                                              null);

        List<JButton> buts = new ArrayList ();
        buts.add (close);

        this.popup.getHeader ().setControls (UIUtils.createButtonBar (buts));

        close.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.popup.setVisible (false);

                _this.editor.removeHighlight (_this.highlight);

            }

        });

        this.popup.setOpaque (false);
        this.popup.setVisible (false);

        this.popup.setDraggable (viewer);

    }

    public void showItem ()
    {

        final AbstractViewer viewer = UIUtils.getViewer (this.editor);

        this.highlight = this.editor.addHighlight (this.word.getAllTextStartOffset (),
                                                   this.word.getAllTextEndOffset (),
                                                   null,
                                                   true);

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.synonyms);
        prefix.add (LanguageStrings.show);

        final FindSynonymsActionHandler _this = this;

        // Show a panel of all the items.
        final QPopup p = this.popup;

        p.setOpaque (false);

        Synonyms syns = null;

        try
        {

            syns = this.editor.getSynonymProvider ().getSynonyms (this.word.getText ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to lookup synonyms for: " +
                                  word,
                                  e);

            UIUtils.showErrorMessage (viewer,
                                      Environment.getUIString (prefix,
                                                               LanguageStrings.actionerror));
                                                               //"Unable to display synonyms.");

            return;

        }

        StringBuilder sb = new StringBuilder ();

        if (syns.words.size () > 0)
        {

            sb.append ("6px");

            for (int i = 0; i < syns.words.size (); i++)
            {

                if (sb.length () > 0)
                {

                    sb.append (", ");

                }

                sb.append ("p, 3px, [p,90px], 5px");

            }

        } else
        {

            sb.append ("6px, p, 6px");

        }

        FormLayout   summOnly = new FormLayout ("3px, fill:380px:grow, 3px",
                                                sb.toString ());
        PanelBuilder pb = new PanelBuilder (summOnly);

        CellConstraints cc = new CellConstraints ();

        int ind = 2;

        Map<String, String> names = new HashMap ();
        names.put (Synonyms.ADJECTIVE + "",
                   Environment.getUIString (prefix,
                                            LanguageStrings.wordtypes,
                                            LanguageStrings.adjectives));
                    //"Adjectives");
        names.put (Synonyms.NOUN + "",
                   Environment.getUIString (prefix,
                                            LanguageStrings.wordtypes,
                                            LanguageStrings.nouns));
                   //"Nouns");
        names.put (Synonyms.VERB + "",
                   Environment.getUIString (prefix,
                                            LanguageStrings.wordtypes,
                                            LanguageStrings.verbs));
                   //"Verbs");
        names.put (Synonyms.ADVERB + "",
                   Environment.getUIString (prefix,
                                            LanguageStrings.wordtypes,
                                            LanguageStrings.adverbs));
                   //"Adverbs");
        names.put (Synonyms.OTHER + "",
                   Environment.getUIString (prefix,
                                            LanguageStrings.wordtypes,
                                            LanguageStrings.other));
                   //"Other");

        if (syns.words.size () == 0)
        {

            JLabel l = UIUtils.createInformationLabel (Environment.getUIString (prefix,
                                                                                LanguageStrings.nosynonyms));
                                                            //"No synonyms found.");

            pb.add (l,
                    cc.xy (2,
                           2));

        }

        // Determine what type of word we are looking for.
        for (Synonyms.Part i : syns.words)
        {

            JLabel l = UIUtils.createInformationLabel (names.get (i.type + ""));

            //l.setFont (l.getFont ().deriveFont (Font.ITALIC));
            l.setFont (l.getFont ().deriveFont ((float) UIUtils.getEditorFontSize (10)));
            l.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));
            pb.add (l,
                    cc.xy (2,
                           ind));

            ind += 2;

            HTMLEditorKit kit = new HTMLEditorKit ();
            HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

            JTextPane t = new JTextPane (doc);
            t.setEditorKit (kit);
            t.setEditable (false);
            t.setOpaque (false);

            StringBuilder buf = new StringBuilder ("<style>a { text-decoration: none; } a:hover { text-decoration: underline; }</style><span style='color: #000000; font-size: " + ((int) UIUtils.getEditorFontSize (10)/*t.getFont ().getSize () + 2*/) + "pt; font-family: " + t.getFont ().getFontName () + ";'>");

            for (int x = 0; x < i.words.size (); x++)
            {

                String w = (String) i.words.get (x);

                buf.append (String.format ("<a class='x' href='http://%1$s'>%1$s</a>",
                                           w));

                if (x < (i.words.size () - 1))
                {

                    buf.append (", ");

                }

            }

            buf.append ("</span>");

            t.setText (buf.toString ());

            t.addHyperlinkListener (new HyperlinkAdapter ()
                {

                    public void hyperlinkUpdate (HyperlinkEvent ev)
                    {

                        if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                        {

                            _this.editor.replaceText (_this.word.getAllTextStartOffset (),
                                                      _this.word.getAllTextEndOffset (),
                                                      ev.getURL ().getHost ());

                            _this.editor.removeHighlight (_this.highlight);

                            _this.popup.setVisible (false);

                            viewer.fireProjectEvent (ProjectEvent.SYNONYM,
                                                     ProjectEvent.REPLACE,
                                                     ev.getURL ().getHost ());

                        }

                    }

                });

            // Annoying that we have to do this but it prevents the text from being too small.

            t.setSize (new Dimension (380,
                                      Short.MAX_VALUE));

            JScrollPane sp = new JScrollPane (t);

            t.setCaretPosition (0);

            sp.setOpaque (false);
            sp.getVerticalScrollBar ().setValue (0);
/*
            sp.setPreferredSize (t.getPreferredSize ());
            sp.setMaximumSize (new Dimension (380,
                                              75));
*/
            sp.getViewport ().setOpaque (false);
            sp.setOpaque (false);
            sp.setBorder (null);
            sp.getViewport ().setBackground (Color.WHITE);
            sp.setAlignmentX (Component.LEFT_ALIGNMENT);

            pb.add (sp,
                    cc.xy (2,
                           ind));

            ind += 2;

        }

        JPanel pan = pb.getPanel ();
        pan.setOpaque (true);
        pan.setBackground (Color.WHITE);

        this.popup.setContent (pan);

        Rectangle r = null;

        try
        {

            r = this.editor.modelToView (this.word.getAllTextStartOffset ());

        } catch (Exception e) {

            Environment.logError ("Unable to get view location of: " +
                                  this.word,
                                  e);

           UIUtils.showErrorMessage (this.editor,
                                     Environment.getUIString (prefix,
                                                              LanguageStrings.actionerror));

            return;

        }

        Point po = SwingUtilities.convertPoint (this.editor,
                                                r.x,
                                                r.y,
                                                viewer);

        r.setLocation (po);

        this.popup.setOpaque (false);

        this.popup.setDraggable (viewer);

        viewer.showPopupAt (this.popup,
                            r,
                            "above",
                            true);

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.showItem ();

    }

}
