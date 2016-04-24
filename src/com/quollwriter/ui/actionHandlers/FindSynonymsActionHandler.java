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

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.HyperlinkAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.QTextEditor;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;


public class FindSynonymsActionHandler extends ActionAdapter
{

    private AbstractProjectViewer projectViewer = null;
    private String                word = null;
    private int                   position = -1;
    //private Chapter               chapter = null;
    private QPopup                popup = null;
    private AbstractEditorPanel   editorPanel = null;
    private Object                highlight = null;
    
    public FindSynonymsActionHandler (String              word,
                                      int                 position,
                                      Chapter             c,
                                      AbstractEditorPanel p)
    {

        this.word = word;
        this.position = position;
        //this.chapter = c;
        this.editorPanel = p;
        this.projectViewer = this.editorPanel.getViewer ();

        final FindSynonymsActionHandler _this = this;
        
        // Show a panel of all the items.
        this.popup = new QPopup ("Synonyms for: " + word,
                                 Environment.getIcon (Constants.FIND_ICON_NAME,
                                                      Constants.ICON_POPUP),
                                 null);

        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "Click to close",
                                              null);
        
        List<JButton> buts = new ArrayList ();
        buts.add (close);
        
        this.popup.getHeader ().setControls (UIUtils.createButtonBar (buts));
                                                    
        close.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.popup.setVisible (false);

                _this.editorPanel.getEditor ().removeHighlight (_this.highlight);
                
            }

        });
                                 
        p.addPopup (this.popup,
                    true,
                    true);
/*
        this.highlight = this.editorPanel.getEditor ().addHighlight (position,
                                                                     position + word.length (),
                                                                     null,
                                                                     true);
  */                                      
        this.popup.setOpaque (false);
        this.popup.setVisible (false);

        this.popup.setDraggable (this.editorPanel);

    }

    public void showItem ()
    {

        if (!this.editorPanel.getViewer ().isLanguageFunctionAvailable ())
        {
            
            return;
            
        }
    
        this.highlight = this.editorPanel.getEditor ().addHighlight (this.position,
                                                                     this.position + this.word.length (),
                                                                     null,
                                                                     true);    
    
        final FindSynonymsActionHandler _this = this;

        QTextEditor editor = this.editorPanel.getEditor ();

        Rectangle r = null;

        try
        {

            r = editor.modelToView (this.position);

        } catch (Exception e)
        {

            // BadLocationException!
            Environment.logError ("Location: " +
                                  this.position +
                                  " is not valid",
                                  e);

            UIUtils.showErrorMessage (this.editorPanel,
                                      "Unable to display synonyms.");

            return;

        }

        int y = r.y;

        // Show a panel of all the items.
        final QPopup p = this.popup;

        p.setOpaque (false);

        Synonyms syns = null;

        try
        {

            syns = this.projectViewer.getSynonymProvider ().getSynonyms (this.word);

        } catch (Exception e)
        {

            UIUtils.showErrorMessage (this.editorPanel,
                                      "Unable to display synonyms.");

            Environment.logError ("Unable to lookup synonyms for: " +
                                  word,
                                  e);

            return;

        }

        if ((syns.words.size () == 0) &&
            (this.word.toLowerCase ().endsWith ("ed")))
        {

            // Trim off the ed and try again.
            try
            {

                syns = this.projectViewer.getSynonyms (this.word.substring (0,
                                                                            this.word.length () - 2));

            } catch (Exception e)
            {

                UIUtils.showErrorMessage (this.editorPanel,
                                          "Unable to display synonyms.");

                Environment.logError ("Unable to lookup synonyms for: " +
                                      word,
                                      e);

                return;

            }

        }

        if ((syns.words.size () == 0) &&
            (this.word.toLowerCase ().endsWith ("s")))
        {

            // Trim off the ed and try again.
            try
            {

                syns = this.projectViewer.getSynonyms (this.word.substring (0,
                                                                            this.word.length () - 1));

            } catch (Exception e)
            {

                UIUtils.showErrorMessage (this.editorPanel,
                                          "Unable to display synonyms.");

                Environment.logError ("Unable to lookup synonyms for: " +
                                      word,
                                      e);

                return;

            }

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
/*
            if (syns.words.size () > 0)
            {

                sb.append (",5px");

            }
  */          
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
                   "Adjectives");
        names.put (Synonyms.NOUN + "",
                   "Nouns");
        names.put (Synonyms.VERB + "",
                   "Verbs");
        names.put (Synonyms.ADVERB + "",
                   "Adverbs");
        names.put (Synonyms.OTHER + "",
                   "Other");

        if (syns.words.size () == 0)
        {

            JLabel l = new JLabel ("No synonyms found.");
            l.setFont (l.getFont ().deriveFont (Font.ITALIC));

            pb.add (l,
                    cc.xy (2,
                           2));

        }

        // Determine what type of word we are looking for.
        for (Synonyms.Part i : syns.words)
        {

            JLabel l = new JLabel (names.get (i.type + ""));

            l.setFont (l.getFont ().deriveFont (Font.ITALIC));
            l.setFont (l.getFont ().deriveFont ((float) UIUtils.getEditorFontSize (10)));
            l.setBorder (new CompoundBorder (new MatteBorder (0,
                                                              0,
                                                              1,
                                                              0,
                                                              Environment.getBorderColor ()),
                                             new EmptyBorder (0, 0, 3, 0)));

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

                buf.append ("<a class='x' href='http://" + w + "'>" + w + "</a>");

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
                            
                            QTextEditor ed = _this.editorPanel.getEditor ();
                            
                            ed.replaceText (_this.position,
                                            _this.position + _this.word.length (),
                                            ev.getURL ().getHost ());

                            ed.removeHighlight (_this.highlight);
                                                        
                            _this.popup.setVisible (false);

                            _this.projectViewer.fireProjectEvent (ProjectEvent.SYNONYM,
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

        // r.y -= this.editorPanel.getScrollPane ().getVerticalScrollBar ().getValue ();

        Point po = SwingUtilities.convertPoint (editor,
                                                r.x,
                                                r.y,
                                                this.editorPanel);

        r.x = po.x;
        r.y = po.y;

        // Subtract the insets of the editorPanel.
        Insets ins = this.editorPanel.getInsets ();

        r.x -= ins.left;
        r.y -= ins.top;

        this.editorPanel.showPopupAt (this.popup,
                                      r,
                                      "above",
                                      true);

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.showItem ();

    }

}
