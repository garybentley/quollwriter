package com.quollwriter.ui.fx.swing;

import java.awt.*;
import java.awt.event.*;

import java.awt.datatransfer.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import com.quollwriter.ui.fx.*;

import com.quollwriter.*;

import com.quollwriter.text.*;

import com.quollwriter.synonyms.SynonymProvider;

public class QTextEditor extends JTextPane implements TextStylable
{

    // TODO Add an exception/error handler for the dispatch thread issue.

    public static final String ALIGN_LEFT = "Left";
    public static final String ALIGN_RIGHT = "Right";
    public static final String ALIGN_JUSTIFIED = "Justified";

    public static final String PASTE_ACTION_NAME = "__paste";
    public static final String COPY_ACTION_NAME = "__copy";
    public static final String CUT_ACTION_NAME = "__cut";
    public static final String UNDO_ACTION_NAME = "__undo";
    public static final String REDO_ACTION_NAME = "__redo";
    public static final String BOLD_ACTION_NAME = "bold";
    public static final String ITALIC_ACTION_NAME = "italic";
    public static final String UNDERLINE_ACTION_NAME = "underline";
    public static final String PRINT_ACTION_NAME = "print";

    public CompoundUndoManager    undoManager = null;
    public QSpellChecker          spellChecker = null;
    private boolean               loading = false;
    public SimpleAttributeSet     styles = new SimpleAttributeSet ();
    private DefaultStyledDocument doc = null;
    public Style                  sectionBreakStyle = null;
    private String                sectionBreak = null;
    private Set<StyleChangeListener> styleChangeListeners = new LinkedHashSet ();
    private LineHighlighter  lineHighlighter = null;
    private boolean               canCopy = true;
    private boolean               canFormat = true;
    private TextProperties textProperties = null;
    private Point mousePosition = null;
    private boolean ignoreDocumentChange = false;

    public QTextEditor(DictionaryProvider2 prov,
                       boolean             spellCheckerEnabled,
                       TextProperties      props)
    {

        this.setCaret (new QCaret ());
        this.getCaret ().setBlinkRate (500);

        final QTextEditor _this = this;

        this.undoManager = new CompoundUndoManager (this);

        // Adapted from: https://community.oracle.com/thread/2376090
        // When there is a long contiguous piece of text it will prevent the text from
        // wrapping.  This allows the wrapping to occur.  However it does not prevent the
        // word wrapping from going nuts in text after this.
        // The bug only triggers when the text is wider than the view.
        /*
        final ViewFactory vf = new ViewFactory ()
        {

            @Override
            public View create (Element elem)
            {

                String kind = elem.getName();

                if (kind != null)
                {

                    if (kind.equals (AbstractDocument.ContentElementName))
                    {

                        return new LabelView (elem)
                        {

                            @Override
                            public float getMinimumSpan (int axis)
                            {

                                if (axis == View.X_AXIS)
                                {

                                    return 0;

                                }

                                if (axis == View.Y_AXIS)
                                {

                                    return super.getMinimumSpan (axis);

                                }

                                throw new IllegalArgumentException ("Invalid axis: " + axis);

                            }

                        };

                    } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                        return new ParagraphView(elem);
                    } else if (kind.equals(AbstractDocument.SectionElementName)) {
                        return new BoxView(elem, View.Y_AXIS);
                    } else if (kind.equals(StyleConstants.ComponentElementName)) {
                        return new ComponentView(elem);
                    } else if (kind.equals(StyleConstants.IconElementName)) {
                        return new IconView(elem);
                    }

                }

                // default to text display
                return new LabelView (elem);

            }

        };
*/
        this.setEditorKit (new QStyledEditorKit ());
/*
        {

            @Override
            public ViewFactory getViewFactory ()
            {

                return vf;

            }

        });
  */
        this.initDocument ();

        this.addMouseMotionListener (new MouseMotionAdapter ()
        {

            @Override
            public void mouseMoved (MouseEvent ev)
            {

                _this.mousePosition = ev.getPoint ();

            }

        });

        this.addMouseListener (new MouseAdapter ()
        {

            @Override
            public void mouseExited (MouseEvent ev)
            {

                _this.mousePosition = null;

            }

        });

        this.setMargin (new Insets (5,
                                    5,
                                    0,
                                    5));

        this.undoManager.discardAllEdits ();

        // Get the files.
        this.spellChecker = new QSpellChecker (this,
                                               prov);
        this.spellChecker.enable (spellCheckerEnabled);

        ActionMap am = this.getActionMap ();

        am.put (REDO_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        if (_this.undoManager.canRedo ())
                        {

                            _this.undoManager.redo ();

                        }

                    }

                });

        am.put (UNDO_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        if (_this.undoManager.canUndo ())
                        {

                            _this.undoManager.undo ();

                        }

                    }

                });

        am.put (PRINT_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        try
                        {

                            _this._print ();

                        } catch (Exception e)
                        {

                            // e.printStackTrace ();

                        }

                    }

                });

        am.put (BOLD_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.toggleBold ();

                    }

                });

        am.put (ITALIC_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.toggleItalic ();

                    }

                });

        am.put (UNDERLINE_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.toggleUnderline ();

                    }

                });

        am.put (PASTE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.paste ();

                              }

                          });

        am.put (COPY_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.copy ();

                              }

                          });

        am.put (CUT_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.cut ();

                              }

                          });
/*
        am.put (UNDO_ACTION_NAME,
                          this.getEditor ().getUndoManager ().getUndoAction ());

        am.put (REDO_ACTION_NAME,
                          this.getEditor ().getUndoManager ().getRedoAction ());
  */
/*
        am.put ("redo",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        if (_this.undoManager.canRedo ())
                        {

                            _this.undoManager.redo ();

                        }

                    }

                });

        am.put ("undo",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        if (_this.undoManager.canUndo ())
                        {

                            _this.undoManager.undo ();

                        }

                    }

                });

        am.put ("print",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        try
                        {

                            _this._print ();

                        } catch (Exception e)
                        {

                            // e.printStackTrace ();

                        }

                    }

                });

        am.put ("bold",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.toggleBold ();

                    }

                });

        am.put ("italic",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.toggleItalic ();

                    }

                });

        am.put ("underline",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.toggleUnderline ();

                    }

                });
*/
        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_Z,
                                        InputEvent.CTRL_DOWN_MASK),
                UNDO_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_Y,
                                        InputEvent.CTRL_DOWN_MASK),
                REDO_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                        InputEvent.CTRL_DOWN_MASK),
                PRINT_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_B,
                                        InputEvent.CTRL_DOWN_MASK),
                BOLD_ACTION_NAME);//new StyledEditorKit.BoldAction ());
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_I,
                                        InputEvent.CTRL_DOWN_MASK),
                ITALIC_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_U,
                                        InputEvent.CTRL_DOWN_MASK),
                UNDERLINE_ACTION_NAME);

        this.lineHighlighter = new LineHighlighter (Color.LIGHT_GRAY);

        this.textProperties = new TextProperties ();

        this.bindTo (props);

        this.textProperties.fontFamilyProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setFontFamily (newv);
            this.applyStyles ();

        });

        this.textProperties.fontSizeProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setFontSize (newv.intValue ());
            this.applyStyles ();

        });

        this.textProperties.backgroundColorProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setBackgroundColor (SwingUIUtils.toSwingColor (newv));

        });

        this.textProperties.textColorProperty ().addListener ((pr, oldv, newv) ->
        {
System.out.println ("HERE: " + newv);
            this.setTextColor (SwingUIUtils.toSwingColor (newv));
            this.applyStyles ();

        });

        this.textProperties.highlightWritingLineProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setHighlightWritingLine (newv);

        });

        this.textProperties.textBorderProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setTextBorder (newv.intValue ());

        });

        this.textProperties.writingLineColorProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setWritingLineColor (SwingUIUtils.toSwingColor (newv));

        });

        this.textProperties.lineSpacingProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setLineSpacing (newv.floatValue ());
            this.applyStyles ();

        });

        this.textProperties.alignmentProperty ().addListener ((prv, oldv, newv) ->
        {

            this.setAlignment (newv);
            this.applyStyles ();

        });

        this.textProperties.firstLineIndentProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setFirstLineIndent (newv);
            this.applyStyles ();

        });

    }

    public Point getMousePosition ()
    {

        return this.mousePosition;

    }

    public void initDocument ()
    {

        this.doc = new DefaultStyledDocument ();

        this.doc.putProperty (DefaultEditorKit.EndOfLineStringProperty,
                              "\n");

        this.sectionBreakStyle = this.doc.addStyle ("section-break",
                                                    null);
        StyleConstants.setAlignment (this.sectionBreakStyle,
                                     StyleConstants.ALIGN_CENTER);

        this.doc.addUndoableEditListener (this.undoManager);

        this.doc.setParagraphAttributes (0,
                                         0,
                                         this.styles,
                                         true);
        this.doc.setCharacterAttributes (0,
                                         0,
                                         this.styles,
                                         true);

        this.setDocument (this.doc);

    }

    public void setCanFormat (boolean c)
    {

        this.canFormat = c;

    }

    public boolean isCanFormat ()
    {

        return this.canFormat;

    }

    public void setCanCopy (boolean c)
    {

        this.canCopy = c;

    }

    public boolean isCanCopy ()
    {

        return this.canCopy;

    }

    @Override
    public void copy ()
    {

        if (!this.canCopy)
        {

            return;

        }

        super.copy ();

    }

    @Override
    public void paste ()
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.paste ();

            });

            return;

        }

        if (!this.canCopy)
        {

            return;

        }

        Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents (null);

        if ((trans != null)
            &&
            (trans.isDataFlavorSupported (DataFlavor.stringFlavor))
           )
        {

            try
            {

                String s = (String) trans.getTransferData (DataFlavor.stringFlavor);

                s = com.quollwriter.text.TextUtilities.sanitizeText (s);

                TransferHandler transferHandler = this.getTransferHandler();
                transferHandler.importData (this,
                                            new StringSelection (s));

            } catch (Exception e) {

                super.paste ();

            }

        }

    }

    protected QTextEditor()
    {


    }

    public void setWritingLineColor (Color c)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setWritingLineColor (c);

            });

            return;

        }

        this.lineHighlighter.setPaint (c);
        this.validate ();
        this.repaint ();

    }

    public void setHighlightWritingLine (boolean v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setHighlightWritingLine (v);

            });

            return;

        }

        if (v)
        {

            this.lineHighlighter.install (this);

        } else {

            this.lineHighlighter.uninstall ();

        }

    }

    public SynonymProvider getSynonymProvider ()
    {

        if (this.spellChecker != null)
        {

            return this.spellChecker.getSynonymProvider ();

        }

        return null;

    }

    public void setSynonymProvider (SynonymProvider sp)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setSynonymProvider (sp);

            });

            return;

        }

        if (this.spellChecker != null)
        {

            this.spellChecker.setSynonymProvider (sp);

        }

    }

    public void unbindTextProperties ()
    {

        this.textProperties.unbindAll ();

    }

    public void bindTo (TextProperties props)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.bindTo (props);

            });

            return;

        }

        this.setIgnoreDocumentChanges (true);
        this.setFirstLineIndent (props.getFirstLineIndent ());
        this.setAlignment (props.getAlignment ());
        this.setLineSpacing (props.getLineSpacing ());
        this.setWritingLineColor (SwingUIUtils.toSwingColor (props.getWritingLineColor ()));
        this.setTextBorder (props.getTextBorder ());
        this.setHighlightWritingLine (props.isHighlightWritingLine ());
        this.setTextColor (SwingUIUtils.toSwingColor (props.getTextColor ()));
        this.setBackgroundColor (SwingUIUtils.toSwingColor (props.getBackgroundColor ()));
        this.setFontFamily (props.getFontFamily ());
        this.setFontSize (props.getFontSize ());
        this.applyStyles ();
        this.setIgnoreDocumentChanges (false);

        this.textProperties.bindTo (props);

    }

    public boolean _print ()
                    throws java.awt.print.PrinterException
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                try
                {

                    this._print ();

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to print",
                                                e);

                }

            });

            return false;

        }

        QTextEditor qt = new QTextEditor (null,
                                          false,
                                          this.textProperties);

        qt.setSectionBreak (this.sectionBreak);

        qt.setLineSpacing (this.getLineSpacing ());
        qt.setFontSize (this.getPrintFontSize (this.getFontSize ()));
        qt.setFontFamily (this.getFontFamily ());
        qt.setAlignment (this.getAlignment ());
        qt.setFirstLineIndent (this.getFirstLineIndent ());
        qt.setMargin (null);
        qt.setTextWithMarkup (this.getTextWithMarkup ());

        int ppi = java.awt.Toolkit.getDefaultToolkit ().getScreenResolution ();

        // A4 - 17cm wide, 25.7cm high.
        // 6.7" wide, 10.12" high

        float pageHeight = 10.12f;
        float pageWidth = 8.5f;

        qt.setSize (new Dimension ((int) (pageWidth * ppi),
                                   Integer.MAX_VALUE));

        qt.setSize (new Dimension ((int) (pageWidth * ppi),
                                   qt.getPreferredSize ().height));

        return qt.print ();

    }

    public void setDictionaryProvider (DictionaryProvider2 dp)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setDictionaryProvider (dp);

            });

            return;

        }

        this.spellChecker.setDictionaryProvider (dp);

        this.checkSpelling ();

    }

    public void applyStyle (Object style,
                            int    start,
                            int    end)
                     throws Exception
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                try
                {

                    this.applyStyle (style,
                                     start,
                                     end);

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to apply style");

                }

            });

            return;

        }

        MutableAttributeSet attrs = new SimpleAttributeSet ();
        attrs.addAttribute (style,
                            true);

        this.doc.setCharacterAttributes (start,
                                         end - start,
                                         attrs,
                                         false);

    }

    public String getSelectedText ()
    {

        int start = this.getSelectionStart ();

        if (start < 0)
        {

            return "";

        }

        int end = this.getSelectionEnd ();

        if (end < 0)
        {

            return "";

        }

        return this.getText ().substring (start,
                                          end);

    }

    public StringWithMarkup getTextWithMarkup ()
    {

        return new StringWithMarkup (this.getText (),
                                     new Markup (this.getDocument ()));

    }

    public void addStyleChangeListener (StyleChangeListener l)
    {

        this.styleChangeListeners.add (l);

    }

    public void removeStyleChangeListener (StyleChangeListener l)
    {

        this.styleChangeListeners.remove (l);

    }

    protected void fireStyleChangeEvent (int     start,
                                         int     end,
                                         String  styleType,
                                         boolean on)
    {

        StyleChangeEvent ev = new StyleChangeEvent (this,
                                                    start,
                                                    end,
                                                    styleType,
                                                    on);

        for (StyleChangeListener l : this.styleChangeListeners)
        {

            l.styleChanged (ev);

        }

    }

    private void clearBoldItalicUnderline ()
    {

        SimpleAttributeSet attrs = new SimpleAttributeSet ();

        StyleConstants.setBold (attrs,
                                false);
        StyleConstants.setItalic (attrs,
                                  false);
        StyleConstants.setUnderline (attrs,
                                     false);

        StyledEditorKit k = (StyledEditorKit) this.getEditorKit ();

        MutableAttributeSet inAttrs = k.getInputAttributes ();

        inAttrs.addAttributes (attrs);

    }

    public void setStyle (MutableAttributeSet attrs,
                          TextRange           range)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setStyle (attrs,
                               range);

            });

            return;

        }

        if (range.start != range.end)
        {

            this.doc.setCharacterAttributes (range.start,
                                             range.end - range.start,
                                             attrs,
                                             false);

        }

        StyledEditorKit k = (StyledEditorKit) this.getEditorKit ();

        MutableAttributeSet inAttrs = k.getInputAttributes ();

        inAttrs.addAttributes (attrs);

    }

    public void toggleBold ()
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.toggleBold ();

            });

            return;

        }

        if (!this.canFormat)
        {

            return;

        }

        int start = this.getSelectionStart ();

        if (start < 0)
        {

            start = this.getCaret ().getDot ();

        }

        if (this.getSelectionEnd () == start)
        {

            if (start > 0)
            {

                start--;

            }

        }

        AbstractDocument.AbstractElement el = (AbstractDocument.AbstractElement) this.doc.getCharacterElement (start);

        SimpleAttributeSet attr = new SimpleAttributeSet ();

        StyleConstants.setBold (attr,
                                !StyleConstants.isBold (el.getAttributes ()));

        TextRange tr = new TextRange (this);

        this.setStyle (attr,
                       tr);

        this.fireStyleChangeEvent (tr.start,
                                   tr.end,
                                   StyleChangeEvent.BOLD,
                                   StyleConstants.isBold (attr));

    }

    public void toggleItalic ()
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.toggleItalic ();

            });

            return;

        }

        if (!this.canFormat)
        {

            return;

        }

        int start = this.getSelectionStart ();

        if (start < 0)
        {

            start = this.getCaret ().getDot ();

        }

        if (this.getSelectionEnd () == start)
        {

            if (start > 0)
            {

                start--;

            }

        }

        AbstractDocument.AbstractElement el = (AbstractDocument.AbstractElement) this.doc.getCharacterElement (start);

        SimpleAttributeSet attr = new SimpleAttributeSet ();

        StyleConstants.setItalic (attr,
                                  !StyleConstants.isItalic (el.getAttributes ()));

        TextRange tr = new TextRange (this);

        this.setStyle (attr,
                       tr);

        this.fireStyleChangeEvent (tr.start,
                                   tr.end,
                                   StyleChangeEvent.ITALIC,
                                   StyleConstants.isItalic (attr));

    }

    public void toggleUnderline ()
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.toggleUnderline ();

            });

            return;

        }

        if (!this.canFormat)
        {

            return;

        }

        int start = this.getSelectionStart ();

        if (start < 0)
        {

            start = this.getCaret ().getDot ();

        }

        if (this.getSelectionEnd () == start)
        {

            if (start > 0)
            {

                start--;

            }

        }

        AbstractDocument.AbstractElement el = (AbstractDocument.AbstractElement) this.doc.getCharacterElement (start);

        SimpleAttributeSet attr = new SimpleAttributeSet ();

        StyleConstants.setUnderline (attr,
                                     !StyleConstants.isUnderline (el.getAttributes ()));

        TextRange tr = new TextRange (this);

        this.setStyle (attr,
                       tr);

        this.fireStyleChangeEvent (tr.start,
                                   tr.end,
                                   StyleChangeEvent.UNDERLINE,
                                   StyleConstants.isUnderline (attr));

    }

    public void replaceText (String replace)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.replaceText (replace);

            });

            return;

        }

        if (this.getText ().length () == 0)
        {

            this.setTextWithMarkup (new StringWithMarkup (replace));

            return;

        }

        int caret = this.getCaret ().getDot ();

        this.startCompoundEdit ();

        this.select (0,
                     this.getText ().length ());

        this.replaceSelection (replace);

        this.endCompoundEdit ();

        this.getCaret ().setDot (caret);

    }

    public void replaceText (int    start,
                             int    end,
                             String replace)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.replaceText (start,
                                  end,
                                  replace);

            });

            return;

        }

        int caret = this.getCaret ().getDot ();

        this.startCompoundEdit ();

        this.select (start,
                     end);

        this.replaceSelection (replace);

        this.endCompoundEdit ();

        this.getCaret ().setDot (caret);

    }

    public void startCompoundEdit ()
    {

        this.undoManager.startCompoundEdit ();

    }

    public void endCompoundEdit ()
    {

        this.undoManager.endCompoundEdit ();

    }

    public CompoundUndoManager getUndoManager ()
    {

        return this.undoManager;

    }
/*
    public TextProperties getTextProperties ()
    {

        return new TextProperties (this,
                                   this.getFontFamily (),
                                   this.getFontSize (),
                                   this.getAlignmentAsString (),
                                   this.getFirstLineIndent (),
                                   this.getLineSpacing () + 1,
                                   this.getTextColor (),
                                   this.getBackgroundColor ());

    }
  */
    public String getAlignmentAsString ()
    {

        int v = this.getAlignment ();

        if (v == StyleConstants.ALIGN_LEFT)
        {

            return QTextEditor.ALIGN_LEFT;

        }

        if (v == StyleConstants.ALIGN_RIGHT)
        {

            return QTextEditor.ALIGN_RIGHT;

        }

        if (v == StyleConstants.ALIGN_JUSTIFIED)
        {

            return QTextEditor.ALIGN_JUSTIFIED;

        }

        return QTextEditor.ALIGN_LEFT;

    }

    public int getAlignment ()
    {

        return StyleConstants.getAlignment (this.styles);

    }

    public void setAlignment (int v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setAlignment (v);

            });

            return;

        }

        StyleConstants.setAlignment (this.styles,
                                     v);

    }

    public boolean getFirstLineIndent ()
    {

        return StyleConstants.getFirstLineIndent (this.styles) != 0;

    }

    public void setFirstLineIndent (boolean v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setFirstLineIndent (v);

            });

            return;

        }

        float f = 0f;

        if (v)
        {

            f = 30f;

        }

        StyleConstants.setFirstLineIndent (this.styles,
                                           f);

    }

    public void setAlignment (String v)
    {

        int a = StyleConstants.ALIGN_LEFT;

        if (v.equalsIgnoreCase (QTextEditor.ALIGN_LEFT))
        {

            a = StyleConstants.ALIGN_LEFT;

        }

        if (v.equalsIgnoreCase (QTextEditor.ALIGN_RIGHT))
        {

            a = StyleConstants.ALIGN_RIGHT;

        }

        if (v.equalsIgnoreCase (QTextEditor.ALIGN_JUSTIFIED))
        {

            a = StyleConstants.ALIGN_JUSTIFIED;

        }

        this.setAlignment (a);

    }

    public Color getTextColor ()
    {

        return this.getFontColor ();

    }

    public Color getFontColor ()
    {

        return StyleConstants.getForeground (this.styles);

    }

    public void setTextColor (Color c)
    {

        this.setFontColor (c);

    }

    public void setFontColor (Color c)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setFontColor (c);

            });

            return;

        }

        // Change the caret color to match.
        this.setCaretColor (c);

        StyleConstants.setForeground (this.styles,
                                      c);

    }

    /**
     * Note: is here for compatibility, but will do nothing, parent components need to
     * implement TextStyleable.setTextBorder with their own behaviour.
     */
    @Override
    public void setTextBorder (int v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setTextBorder (v);

            });

            return;

        }

        this.setBorder (SwingUIUtils.createPadding (0, v, 0, v));

    }

    public int getFontSize ()
    {

        return StyleConstants.getFontSize (this.styles);

    }

    public Font getFontForStyles ()
    {

        return new Font (this.getFontFamily (),
                         Font.PLAIN,
                         this.getFontSize ());

    }

    public void setFontSize (int v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setFontSize (v);

            });

            return;

        }

        // Need to take the current screen resolution into account.
        int _v = Math.round ((float) v * ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f));

        StyleConstants.setFontSize (this.styles,
                                    _v);

        this.fireStyleChangeEvent (0,
                                   this.doc.getEndPosition ().getOffset (),
                                   StyleChangeEvent.FONT_SIZE,
                                   false);

    }

    public String getFontFamily ()
    {

        return StyleConstants.getFontFamily (this.styles);

    }

    public void setFontFamily (String name)
    {

        if (name == null)
        {

            throw new IllegalArgumentException ("Font family name cannot be null");

        }

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setFontFamily (name);

            });

            return;

        }

        StyleConstants.setFontFamily (this.styles,
                                      name);

        this.fireStyleChangeEvent (0,
                                   this.doc.getEndPosition ().getOffset (),
                                   StyleChangeEvent.FONT_NAME,
                                   false);

    }

    public void applyStyles ()
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.applyStyles ();

            });

        }

        this.setIgnoreDocumentChanges (true);

        this.doc.setParagraphAttributes (0,
                                         this.doc.getEndPosition ().getOffset (),
                                         this.styles,
                                         true);

        this.setIgnoreDocumentChanges (false);

        this.initSectionBreaks (this.getText ());

    }

    public int getLineHeight ()
    {

        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage (1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics ();

        return (int) (g.getFontMetrics (new Font (this.getFontFamily (), Font.PLAIN, this.getFontSize ())).getHeight () * this.getLineSpacing ());

    }

    public float getLineSpacing ()
    {

        return StyleConstants.getLineSpacing (this.styles) + 1;

    }

    public void setLineSpacing (float v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setLineSpacing (v);

            });

            return;

        }

        StyleConstants.setLineSpacing (this.styles,
                                       v - 1);

    }

    public void addWordToDictionary (String word)
    {

        if (this.spellChecker != null)
        {

            this.spellChecker.addWord (word);

        }

    }

    public void setSpellCheckEnabled (boolean v)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setSpellCheckEnabled (v);

            });

            return;

        }

        if (this.spellChecker != null)
        {

            this.spellChecker.enable (v);

        }

    }

    public boolean isSpellCheckEnabled ()
    {

        if (this.spellChecker != null)
        {

            return this.spellChecker.isEnabled ();

        }

        return false;

    }

    public List getSpellCheckSuggestions (Word word)
    {

        if (this.spellChecker == null)
        {

            return new ArrayList ();

        }

        return this.spellChecker.getSuggestions (word);

    }

    public boolean isLoadingText ()
    {

        return this.loading;

    }

    public void setSectionBreak (String b)
    {

        this.sectionBreak = b;

    }

    private void initSectionBreaks (String t)
    {

        if (t == null)
        {

            return;

        }

        if (this.sectionBreak != null)
        {

            int ind = t.indexOf (this.sectionBreak);

            while (ind != -1)
            {

                this.doc.setParagraphAttributes (ind,
                                                 1,
                                                 this.sectionBreakStyle,
                                                 false);

                Style ls = this.doc.addStyle (null,
                                              null);
                StyleConstants.setAlignment (ls,
                                             StyleConstants.ALIGN_LEFT);

                this.doc.setCharacterAttributes (ind + this.sectionBreak.length (),
                                                 1,
                                                 ls,
                                                 false);

                ind = t.indexOf (this.sectionBreak,
                                 ind + this.sectionBreak.length () + 1);

            }

        }

    }

    public void setText (String t)
    {

        throw new UnsupportedOperationException ("Not supported, use setTextWithMarkup");

    }

    public void removeHighlight (Object o)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.removeHighlight (o);

            });

            return;

        }

        this.getHighlighter ().removeHighlight (o);

    }

    public Object addHighlight (int                          start,
                                int                          end,
                                Highlighter.HighlightPainter painter,
                                boolean                      removeHighlightOnActivity)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            throw new IllegalStateException ("Not on swing event dispatch thread.");

        }

        final Highlighter h = this.getHighlighter ();

        try
        {

            final Object o = h.addHighlight (start,
                                             end,
                                             ((painter != null) ? painter : javax.swing.text.DefaultHighlighter.DefaultPainter));

            if (removeHighlightOnActivity)
            {

                final QTextEditor _this = this;

                this.addKeyListener (new KeyAdapter ()
                {

                    public void keyPressed (KeyEvent ev)
                    {

                        _this.removeHighlight (o);

                        _this.removeKeyListener (this);

                    }

                });

                this.addMouseListener (new MouseAdapter ()
                {

                    public void mousePressed (MouseEvent ev)
                    {

                        _this.removeHighlight (o);

                        _this.removeMouseListener (this);

                    }

                });


            }

            return o;

        } catch (Exception e)
        {

            return null;

        }

    }

    public void removeAllHighlights (Highlighter.HighlightPainter painter)
    {

        Highlighter.HighlightPainter _painter = painter;

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.removeAllHighlights (_painter);

            });

            return;

        }

        Highlighter h = this.getHighlighter ();

        Highlighter.Highlight[] highlights = h.getHighlights ();

        if (painter == null)
        {

            painter = javax.swing.text.DefaultHighlighter.DefaultPainter;

        }

        for (int k = highlights.length; --k >= 0;)
        {

            Highlighter.Highlight hh = highlights[k];

            if (hh.getPainter () == painter)
            {

                h.removeHighlight (hh);

            }

        }

    }

    public void removeHighlightsForElement (Element                      el,
                                            Highlighter.HighlightPainter painter)
    {

        Highlighter.HighlightPainter _painter = painter;

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.removeHighlightsForElement (el,
                                                 _painter);

            });

            return;

        }

        int i = el.getStartOffset ();
        int j = el.getEndOffset ();

        if (i == j)
        {

            return;

        }

        if (painter == null)
        {

            painter = javax.swing.text.DefaultHighlighter.DefaultPainter;

        }

        Highlighter h = this.getHighlighter ();

        Highlighter.Highlight[] highlights = h.getHighlights ();

        for (int k = highlights.length; --k >= 0;)
        {

            Highlighter.Highlight hh = highlights[k];

            int s = hh.getStartOffset ();

            int e = hh.getEndOffset ();

            if (((i <= s) && (s <= j)) ||
                ((i <= e) && (e <= j)))
            {

                if (hh.getPainter () == painter)
                {

                    h.removeHighlight (hh);

                }

            }

        }

    }

    public void setTextWithMarkup (StringWithMarkup text)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setTextWithMarkup (text);

            });

            return;

        }

        boolean enabled = false;

        this.setIgnoreDocumentChanges (true);

        if (this.spellChecker != null)
        {

            enabled = this.spellChecker.isEnabled ();

            this.spellChecker.enable (false);

        }

        String t = null;
        Markup markup = null;

        if (text != null)
        {

            t = text.getText ();
            markup = text.getMarkup ();

        }

        if (t != null)
        {

            t =Utils.replaceString (t,
                                           String.valueOf ('\r'),
                                           "");

        }

        //this.styles = new SimpleAttributeSet ();

        this.clearBoldItalicUnderline ();

        super.setText (t);

        //this.applyStyles ();

        this.initSectionBreaks (t);

        if (markup != null)
        {

            //Markup m = new Markup (markup);

            markup.apply (this);

        }

        this.setIgnoreDocumentChanges (false);

        if (this.spellChecker != null)
        {

            this.spellChecker.enable (enabled);

        }

    }

    public void checkSpelling ()
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.checkSpelling ();

            });

            return;

        }

        if (this.spellChecker != null)
        {

            if (this.spellChecker.isEnabled ())
            {

                this.spellChecker.checkAll ();

            }

        }

    }

    public void appendText (String t)
                     throws BadLocationException
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                try
                {

                    this.appendText (t);

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to append text: " + t,
                                                e);

                }

            });

            return;

        }

        this.doc.insertString (this.doc.getEndPosition ().getOffset () - 1,
                               t,
                               null);

    }

    public void insertText (int    where,
                            String t)
                     throws BadLocationException
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                try
                {

                    this.insertText (where,
                                     t);

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to insert text at: " + where,
                                                e);

                }

            });

            return;

        }

        this.doc.insertString (where,
                               t,
                               null);

    }

    public void removeText (int where,
                            int length)
                     throws BadLocationException
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                try
                {

                    this.removeText (where,
                                     length);

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to remove text from: " + where,
                                                e);

                }

            });

            return;

        }

        this.doc.remove (where,
                         length);

        this.removeAllHighlights (null);

    }

    public void setBackgroundColor (Color c)
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setBackgroundColor (c);

            });

            return;

        }

        this.setBackground (c);

    }

    public Color getBackgroundColor ()
    {

        return this.getBackground ();

    }

    public static int getPrintFontSize (int size)
    {

        return Math.round ((float) size / ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f));

    }

    public boolean isPositionAtTextEnd (int p)
    {

        int cl = this.getText ().length ();

        if (cl == 0)
        {

            return p == cl;

        }

        return p >= cl;

    }

    public void setIgnoreDocumentChanges (boolean v)
    {

        this.ignoreDocumentChange = v;

        this.undoManager.setRecordUndos (!v);

    }

    public boolean isIgnoreDocumentChanges ()
    {

        return this.ignoreDocumentChange;

    }

}
