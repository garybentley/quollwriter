package com.quollwriter.ui.fx.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.Set;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import javafx.beans.property.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.events.UserPropertyListener;
import com.quollwriter.events.UserPropertyEvent;
//import com.quollwriter.ui.actionHandlers.*;
// TODO Move into swing pckage.
//import com.quollwriter.ui.components.ActionAdapter;
//import com.quollwriter.ui.components.DocumentAdapter;

import com.quollwriter.ui.fx.TextProperties;
import com.quollwriter.ui.fx.SpellCheckSupported;
import com.quollwriter.ui.fx.State;
import com.quollwriter.ui.fx.TextStylable;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public abstract class AbstractEditorPanel<E extends AbstractProjectViewer> extends ProjectObjectQuollPanel<E> implements SpellCheckSupported, TextStylable, UserPropertyListener
{

    public static final String TOGGLE_SPELLCHECK_ACTION_NAME = "toggle-spellcheck";
    public static final String TOGGLE_WORDCOUNTS_ACTION_NAME = "toggle-wordcounts";
    public static final String EDIT_TEXT_PROPERTIES_ACTION_NAME = "edit-text-properties";

    protected QTextEditor    editor = null;
    private Border origEditorBorder = null;
//    protected ActionMap      actions = null;
    protected Point             lastMousePosition = null;
    protected JScrollPane    scrollPane = null;
    private boolean          ignoreDocumentChange = false;
    private boolean          useTypewriterScrolling = false;
    private ReadabilityIndices readability = null;
    //private ActionListener performAction = null;
    private int scrollOffset = 0;
    private Insets origEditorMargin = null;
    private int softCaret = -1;
    private Runnable wordCountUpdate = null;
    private long lastWordCountUpdateTime = 0;
    private boolean isScrolling = false;

    private javafx.beans.property.StringProperty selectedTextProp = null;

    public AbstractEditorPanel(final E       pv,
                               final Chapter c)
                        throws GeneralException
    {

        super (pv,
               c);

        this.selectedTextProp = new javafx.beans.property.SimpleStringProperty ();

        //this.chapter = c;

        final AbstractEditorPanel _this = this;

        DictionaryProvider2 dp = null;

        try
        {

            dp = pv.getDictionaryProvider ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get dictionary provider.",
                                        e);

        }

        SynonymProvider sp = null;

        try
        {

            sp = pv.getSynonymProvider ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get synonym provider.",
                                        e);

        }

        this.editor = new QTextEditor (dp,
                                       pv.isSpellCheckingEnabled (),
                                       Environment.getProjectTextProperties ());

        this.origEditorBorder = this.editor.getBorder ();

        this.editor.setSynonymProvider (sp);//Environment.getSynonymProvider ());

        // This ensures that the viewport is always in sync with the text area size.
        this.editor.addKeyListener (new KeyAdapter ()
        {

            @Override
            public void keyPressed (KeyEvent ev)
            {

                // Get the caret.
                if (_this.editor.getCaretPosition () >= _this.editor.getText ().length ())
                {

                    Dimension d = _this.editor.getSize ();

                    _this.getScrollPane ().getViewport ().setViewSize (new Dimension (d.width,
                                                                                      d.height + 200));

                }

            }

            @Override
            public void keyTyped (KeyEvent ev)
            {

                if ((ev.getModifiersEx () & KeyEvent.CTRL_DOWN_MASK) != 0)
                {

                    return;

                }

                UserProperties.playKeyStrokeSound ();

            }
        });

        this.editor.setAlignmentX (Component.LEFT_ALIGNMENT);
        //this.editor.addMouseListener (this);
        //this.initEditor ();

        this.actions = this.editor.getActionMap ();

        super.setActionMap (this.actions);

        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
/*
TODO Remove
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                        InputEvent.CTRL_DOWN_MASK),
                EDIT_TEXT_PROPERTIES_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_W,
                                        InputEvent.CTRL_DOWN_MASK),
                TOGGLE_WORDCOUNTS_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_L,
                                        InputEvent.CTRL_DOWN_MASK),
                TOGGLE_SPELLCHECK_ACTION_NAME);
*/
        im = this.editor.getInputMap (JComponent.WHEN_FOCUSED);

        this.addComponentListener (new ComponentAdapter ()
        {

            @Override
            public void componentResized (ComponentEvent ev)
            {

                if (_this.isReadyForUse ())
                {

                    SwingUIUtils.doLater (() ->
                    {

                        _this.scrollCaretIntoView ();

                    });

                }

            }


        });
/*
        this.performAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };
*/

    }

    public javafx.beans.property.StringProperty selectedTextProperty ()
    {

        return this.selectedTextProp;

    }

    @Override
    public void propertyChanged (UserPropertyEvent ev)
    {

        TextProperties props = this.viewer.getTextProperties ();

        if (ev.getName ().equals (Constants.EDITOR_FONT_PROPERTY_NAME))
        {

            this.setFontFamily (props.getFontFamily ());

        }

        if (ev.getName ().equals (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME))
        {

            this.setFontSize (props.getFontSize ());

        }

        if (ev.getName ().equals (Constants.EDITOR_BGCOLOR_PROPERTY_NAME))
        {

            this.setBackgroundColor (props.getBackgroundColor ());

        }

        if (ev.getName ().equals (Constants.EDITOR_FONT_COLOR_PROPERTY_NAME))
        {

            this.setTextColor (props.getTextColor ());

        }

        if (ev.getName ().equals (Constants.EDITOR_TEXT_BORDER_PROPERTY_NAME))
        {

            this.setTextBorder (props.getTextBorder ());

        }

        if (ev.getName ().equals (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME))
        {

            this.setAlignment (props.getAlignment ());

        }

        if (ev.getName ().equals (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME))
        {

            this.setFirstLineIndent (props.getFirstLineIndent ());

        }

        if (ev.getName ().equals (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME))
        {

            this.setLineSpacing (props.getLineSpacing ());

        }

        if (ev.getName ().equals (Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME))
        {

            this.setWritingLineColor (props.getWritingLineColor ());

        }

        if (ev.getName ().equals (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME))
        {

            this.setHighlightWritingLine (props.isHighlightWritingLine ());

        }

    }

    public abstract JComponent getEditorWrapper (QTextEditor editor);

    public void setWritingLineColor (javafx.scene.paint.Color c)
    {

        this.editor.setWritingLineColor (SwingUIUtils.toSwingColor (c));

    }

    @Override
    public void close ()
    {

    }

    public void setHighlightWritingLine (boolean v)
    {

        this.editor.setHighlightWritingLine (v);

    }

    protected void setReadabilityIndices (ReadabilityIndices r)
    {

        this.readability = r;

    }

    public ReadabilityIndices getReadabilityIndices ()
    {

        return this.readability;

    }

    public void setIgnoreDocumentChanges (boolean v)
    {

        this.ignoreDocumentChange = v;

    }

    public boolean isIgnoreDocumentChanges ()
    {

        return this.ignoreDocumentChange;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        final AbstractEditorPanel _this = this;

        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        if (javafx.application.Platform.isFxApplicationThread ())
        {

            throw new GeneralException ("Wrong thread.");

        }

        this.ignoreDocumentChange = true;

        this.editor.setTextWithMarkup (this.chapter.getText ());

        this.ignoreDocumentChange = false;

        JComponent p = this.getEditorWrapper (this.editor);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        //p.setPreferredSize (new Dimension (500, 500));
        p.setMaximumSize (new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE));

        this.scrollPane = new JScrollPane (p);

        this.scrollPane.setMaximumSize (new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE));
        this.scrollPane.setBorder (null);
        this.scrollPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.scrollPane.getViewport ().setOpaque (true);
        this.scrollPane.getVerticalScrollBar ().setUnitIncrement (20);

        this.origEditorMargin = this.editor.getMargin ();
        this.add (this.scrollPane);
        this.setMaximumSize (new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE));

        this.scrollPane.addMouseWheelListener (new MouseWheelListener ()
        {

            public void mouseWheelMoved (MouseWheelEvent ev)
            {

                if (_this.useTypewriterScrolling)
                {

                    try
                    {

                        int c = ev.getWheelRotation ();

                        Rectangle r = SwingUIUtils.getRectForOffset (_this.editor,
                                                                     _this.editor.getCaret ().getDot ());

                        int ny = r.y + (c * r.height);

                        int d = _this.editor.viewToModel2D (new Point (r.x,
                                                                      ny));

                        _this.editor.getCaret ().setDot (d);

                    } catch (Exception e) {

                        // Just ignore

                    }

                }

            }

        });

        this.editor.addStyleChangeListener (new StyleChangeAdapter ()
        {

            public void styleChanged (StyleChangeEvent ev)
            {

                _this.initScrollBar ();

            }

        });

        this.editor.addCaretListener (new CaretListener ()
        {


            public void caretUpdate (final CaretEvent ev)
            {

                SwingUIUtils.doLater (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        if (_this.useTypewriterScrolling)
                        {

                            _this.updateViewportPositionForTypewriterScrolling ();

                        }

                    }

                });

            }

        });

        this.initEditor ();

        /*
         *Experimental... not quite right for page-up.
        final ActionListener origPageDown = this.editor.getActionMap ().get ("page-down");
        final ActionListener origPageUp = this.editor.getActionMap ().get ("page-up");

        this.editor.getActionMap ().put ("page-up",
                                         new TextAction ("here")
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    int c = _this.editor.getCaret ().getDot ();

                    int l = _this.editor.getText ().length ();

                    c--;

                    if (c <= 0)
                    {

                        return;

                    }

                    // Find the first non-whitespace.
                    for (int i = c; i > -1; i--)
                    {

                        if (!Character.isWhitespace (_this.editor.getText ().charAt (i)))
                        {

                            c = i;

                            break;
                        }

                    }

                    // Find the first newline.
                    for (int i = c; i > -1; i--)
                    {

                        if (_this.editor.getText ().charAt (i) == '\n')
                        {

                            _this.editor.getCaret ().setDot (i + 1);

                            _this.editor.scrollRectToVisible (_this.editor.modelToView (i + 1));

                            break;

                        }

                    }

                } catch (Exception e) {

                    e.printStackTrace ();


                }
            }

        });

        this.editor.getActionMap ().put ("page-down",
                                         new TextAction ("here")
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    int c = _this.editor.getCaret ().getDot ();

                    int l = _this.editor.getText ().length ();

                    if (c >= l)
                    {

                        return;

                    }

                    int n = _this.editor.getText ().indexOf ('\n', c);

                    if (n > c)
                    {

                        // Find the first non-whitespace.
                        for (int i = n; i < _this.editor.getText ().length (); i++)
                        {

                            if (!Character.isWhitespace (_this.editor.getText ().charAt (i)))
                            {

                                _this.editor.getCaret ().setDot (i);

                                _this.editor.scrollRectToVisible (_this.editor.modelToView (i));

                                break;

                            }

                        }

                    }

                } catch (Exception e) {

                    e.printStackTrace ();


                }
            }

        });
        */

        SwingUIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    JScrollBar sc = _this.scrollPane.getVerticalScrollBar ();

                    int o = s.getAsInt (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME);

                    sc.setValue (o);

                    int v = s.getAsInt (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME);

                    _this.editor.setSelectionStart (v);
                    _this.editor.setSelectionEnd (v);
                    _this.editor.getCaret ().setDot (v);

                } catch (Exception e)
                {

                    // Ignore it.

                } finally {

                    _this.initScrollBar ();

                }

            }

        });

        //this.scrollCaretIntoView ();
/*
 TODO ?
        if (hasFocus)
        {

            this.editor.grabFocus ();

        }
        */
    }

    public void setSoftCaret (int c)
    {

        this.softCaret = c;

        this.updateViewportPositionForTypewriterScrolling ();

    }

    public void updateViewportPositionForTypewriterScrolling ()
    {

        if (!this.useTypewriterScrolling)
        {

            return;

        }

        final AbstractEditorPanel _this = this;

        SwingUIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                int dot = (_this.softCaret > -1 ? _this.softCaret : _this.editor.getCaret ().getDot ());

                Rectangle r = null;

                try
                {

                    r = SwingUIUtils.getRectForOffset (_this.editor,
                                                       dot);

                } catch (Exception e) {

                    // Ignore.
                    return;

                }

                _this.softCaret = -1;

                if (r == null)
                {

                    return;

                }

                Insets i = _this.editor.getMargin ();
                int hh = _this.scrollPane.getSize ().height / 2;
                int y = r.y;

                if (i != null)
                {

                    y -= i.top;

                }

                if ((y - hh) < 0)
                {

                    _this.editor.setMargin (new Insets (-1 * (y - hh),
                                                              _this.origEditorMargin.left,
                                                              _this.origEditorMargin.bottom,
                                                              _this.origEditorMargin.right));

                    _this.scrollPane.getViewport ().setViewPosition (new Point (0, 0));

                } else {

                    if (y > (_this.editor.getSize ().height - hh - i.bottom - (r.height / 2)))
                    {

                        _this.editor.setMargin (new Insets (_this.origEditorMargin.top,
                                                            _this.origEditorMargin.left,
                                                            //hh - (_this.editor.getSize ().height - y - i.bottom - (r.height / 2)),
                                                            hh - (_this.editor.getSize ().height - y - i.bottom - Math.round ((float) r.height / 2f)),
                                                            _this.origEditorMargin.right));

                    } else {

                        _this.editor.setMargin (new Insets (_this.origEditorMargin.top,
                                                            _this.origEditorMargin.left,
                                                            _this.origEditorMargin.bottom,
                                                            _this.origEditorMargin.right));

                    }

                    Point p = new Point (0, y - hh + (r.height /2));

                    _this.scrollPane.getViewport ().setViewPosition (p);

                }

                _this.validate ();
                _this.repaint ();

            }

        });

    }

    public void setUseTypewriterScrolling (boolean v)
    {

        this.useTypewriterScrolling = v;

        if (!v)
        {

            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            // Reset the margin.
            this.editor.setMargin (this.origEditorMargin);

        } else {

            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        }

        this.scrollCaretIntoView ();

        this.scrollPane.getViewport ().setViewSize (this.editor.getPreferredSize ());

        this.editor.grabFocus ();

    }

    public QTextEditor getEditor ()
    {

        return this.editor;

    }

    public int getScrollOffset ()
    {

        return this.scrollPane.getVerticalScrollBar ().getValue ();

    }

    public void incrementScrollPositionBy (int p)
    {

        this.scrollPane.getVerticalScrollBar ().setValue (this.scrollPane.getVerticalScrollBar ().getValue () + p);

    }

    public void scrollToPosition (final int p)
                           throws GeneralException
    {

        if (this.useTypewriterScrolling)
        {

            // Not compatible with typewriter scrolling.
            return;

        }

        if (this.isScrolling)
        {

            final AbstractEditorPanel _this = this;

            SwingUIUtils.doLater (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        _this.scrollToPosition (p);

                    } catch (Exception e) {

                        Environment.logError ("Unable to scroll to: " + p,
                                              e);

                    }

                }

            });

            return;

        }

        try
        {

            this.isScrolling = true;

            Rectangle r = null;

            try
            {

                r = SwingUIUtils.getRectForOffset (this.editor,
                                                   p);

            } catch (Exception e)
            {

                // BadLocationException!
                throw new GeneralException ("Position: " +
                                            p +
                                            " is not valid.",
                                            e);

            }

            if (r == null)
            {

                throw new GeneralException ("Position: " +
                                            p +
                                            " is not valid.");

            }

            int y = r.y - r.height;

            if (y < 0)
            {

                y = 0;

            }

            this.scrollPane.getVerticalScrollBar ().setValue (y);

        } finally {

            this.isScrolling = false;

        }

    }

    private void initScrollBar ()
    {

        int l = this.editor.getLineHeight ();

        JScrollBar sc = this.scrollPane.getVerticalScrollBar ();

        int o = sc.getValue ();

        sc.setUnitIncrement (l);

        sc.setValue ((int) (Math.floor (o / l) * l));

        sc.setBlockIncrement (l);

    }

    @Override
    public State getState ()
    {

        State s = new State ();

        s.set (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME,
               this.scrollPane.getVerticalScrollBar ().getValue ());
        s.set (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME,
               this.editor.getSelectionStart ());

        return s;

    }

    public void initEditor (TextProperties props)
    {

        this.ignoreDocumentChange = true;

        this.setLineSpacing (props.getLineSpacing ());
        this.setFontSize (props.getFontSize ());
        this.setFontFamily (props.getFontFamily ());
        this.setAlignment (props.getAlignment ());
        this.setFirstLineIndent (props.getFirstLineIndent ());

        this.setBackgroundColor (props.getBackgroundColor ());
        this.setTextColor (props.getTextColor ());
        this.setWritingLineColor (props.getWritingLineColor ());
        this.setHighlightWritingLine (props.isHighlightWritingLine ());
        this.setTextBorder (props.getTextBorder ());

        this.ignoreDocumentChange = false;

        // Add a listener for changes to the properties.
        UserProperties.addListener (this);

    }

    public void initEditor ()
    {

        this.ignoreDocumentChange = true;

        TextProperties props = this.viewer.getTextProperties ();

        this.initEditor (props);

        this.ignoreDocumentChange = false;

    }

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();
        l.add (this.editor);

        return l;

    }

    public void refresh ()
    {

        // No need to do anything.

    }

    public void setSynonymProvider (SynonymProvider sp)
    {

        this.editor.setSynonymProvider (sp);

    }

    public void setDictionaryProvider (DictionaryProvider2 dp)
    {

        this.editor.setDictionaryProvider (dp);

    }

    public void checkSpelling ()
    {

        this.editor.checkSpelling ();

    }

    public void setSpellCheckingEnabled (boolean v)
    {

        this.editor.setSpellCheckEnabled (v);

        String type = (v ? "off" : "on");

        this.setToolBarButtonIcon ("toggle-spellcheck",
                                   getUIString (dictionary,spellcheck,buttons,(v ? off : on),tooltip),
                                   //"Click to turn the spell checker " + type,
                                   "spellchecker-turn-" + type);

    }

    @Override
    public String getTitle ()
    {

        return this.chapter.getName ();

    }

    @Override
    public ImageIcon getIcon (int type)
    {

        return Environment.getIcon (this.chapter.getObjectType (),
                                    type);

    }

    public void setCaretPosition (int dot)
    {

        this.editor.getCaret ().setDot (dot);

    }

    public void setFontColor (javafx.scene.paint.Color c)
    {

        this.ignoreDocumentChange = true;

        this.editor.setFontColor (SwingUIUtils.toSwingColor (c));

        this.editor.setCaretColor (SwingUIUtils.toSwingColor (c));

        this.ignoreDocumentChange = false;

    }

    public void setTextColor (javafx.scene.paint.Color c)
    {

        this.setFontColor (c);

    }

    public void restoreFontColor ()
    {

        this.ignoreDocumentChange = true;

        this.editor.setFontColor (Color.black);
        this.editor.setCaretColor (Color.black);

        this.ignoreDocumentChange = false;

    }

    public void restoreBackgroundColor ()
    {

        this.editor.setBackground (Color.white);

    }

    public void setBackgroundColor (javafx.scene.paint.Color c)
    {

        this.editor.setBackground (SwingUIUtils.toSwingColor (c));

    }

    public Color getBackgroundColor ()
    {

        return this.editor.getBackground ();

    }

    public JScrollPane getScrollPane ()
    {

        return this.scrollPane;

    }

    public void scrollCaretIntoView ()
    {

        this.scrollCaretIntoView (null);

    }

    public void scrollCaretIntoView (final Runnable runAfterScroll)
    {

        final AbstractEditorPanel _this = this;

        SwingUIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    int c = _this.editor.getCaret ().getDot ();

                    if (c > -1)
                    {

                        _this.scrollToPosition (c);

                    }

                    _this.updateViewportPositionForTypewriterScrolling ();

                    if (runAfterScroll != null)
                    {

                        SwingUtilities.invokeLater (runAfterScroll);

                    }

                } catch (Exception e)
                {

                    // Ignore it.

                }

            }

        });

    }

    public TextProperties getTextProperties ()
    {

        return Environment.getProjectTextProperties ();

    }

    public void setFontSize (int v)
    {

        this.ignoreDocumentChange = true;

        this.editor.setFontSize (SwingUIUtils.getEditorFontSize (v));

        this.ignoreDocumentChange = false;

    }

    public void setFontFamily (String name)
    {

        this.ignoreDocumentChange = true;

        this.editor.setFontFamily (name);

        this.ignoreDocumentChange = false;

    }

    public void setAlignment (String v)
    {

        this.ignoreDocumentChange = true;

        this.editor.setAlignment (v);

        this.ignoreDocumentChange = false;

    }

    public void setFirstLineIndent (boolean v)
    {

        this.ignoreDocumentChange = true;

        this.editor.setFirstLineIndent (v);

        this.ignoreDocumentChange = false;

    }

    @Override
    public void setTextBorder (int v)
    {

        this.ignoreDocumentChange = true;

        this.editor.setBorder (new CompoundBorder (this.origEditorBorder,
                                                   SwingUIUtils.createPadding (0, v, 0, v)));

        this.ignoreDocumentChange = false;

    }

    public void setLineSpacing (float v)
    {

        this.ignoreDocumentChange = true;

        this.editor.setLineSpacing (v);

        this.ignoreDocumentChange = false;

    }

    public Point getLastMousePosition ()
    {

        return this.lastMousePosition;

    }

}
