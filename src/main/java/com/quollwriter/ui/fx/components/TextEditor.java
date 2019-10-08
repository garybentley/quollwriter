package com.quollwriter.ui.fx.components;

import org.fxmisc.richtext.*;
import org.fxmisc.richtext.util.*;
import org.fxmisc.richtext.model.*;
import org.reactfx.*;
import org.reactfx.util.*;
import org.fxmisc.undo.*;
import org.fxmisc.richtext.Selection.Direction;

import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.Node;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.text.Word;

public class TextEditor extends GenericStyledArea<TextEditor.ParaStyle, TextEditor.AbstractSegment, TextEditor.TextStyle>
{

    public static final String ALIGN_LEFT = "Left";
    public static final String ALIGN_RIGHT = "Right";
    public static final String ALIGN_JUSTIFIED = "Justified";

    private static TextStyle SPELLING_ERROR_STYLE = new TextStyle ();
    private static TextStyle NORMAL_STYLE = new TextStyle ();

    static
    {
        SPELLING_ERROR_STYLE.setSpellingError (true);
    }

    private Point2D mousePos = null;
    private TextProperties props = null;
    public QSpellChecker          spellChecker = null;
    private SuspendableYes suspendUndos = null;
    private boolean ignoreDocumentChange = false;
    private BooleanProperty readyForUseProp = new SimpleBooleanProperty (false);
/*
    private TextEditor (StringWithMarkup    text,
                        TextProperties      props,
                        DictionaryProvider2 prov)
    {

        super ();

        this.spellChecker = new QSpellChecker (this,
                                               prov);
        this.props = new TextProperties ();
        this.bindTo (props);

        this.setAutoScrollOnDragDesired (true);
        this.setWrapText (true);

        // Build a custom undo manager that is suspendable, see: https://github.com/FXMisc/RichTextFX/issues/735
        this.suspendUndos = new SuspendableYes ();
        this.setUndoManager (UndoManagerFactory.unlimitedHistoryFactory ().createMultiChangeUM(this.multiRichChanges ().conditionOn (this.suspendUndos),
                                                                            TextChange::invert,
                                                                            UndoUtils.applyMultiRichTextChange(this),
                                                                            TextChange::mergeWith,
                                                                            TextChange::isIdentity));

        // TODO Markup.
        this.setText (text);
        this.setEditable (false);

        this.addEventHandler (MouseEvent.MOUSE_MOVED,
                              ev ->
        {

            this.mousePos = new Point2D (ev.getX (),
                                         ev.getY ());

        });

    }
*/

    private static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps ();
    private static MySegmentOps indentOps = new MySegmentOps ();

    private static final StyledDocument INDENT_DOC = ReadOnlyStyledDocument.fromSegment( new IndentSegment( "\t" ), ParaStyle.EMPTY, TextStyle.EMPTY, indentOps );
        private static final StyledSegment INDENT_SEG = (StyledSegment) INDENT_DOC.getParagraph(0).getStyledSegments().get(0);
        private boolean indent = true;

        public static abstract class AbstractSegment
        {
        	protected final Object  data;

        	private AbstractSegment() { data = null; }

        	public AbstractSegment( Object data )
        	{
        		this.data = data;
        	}

        	public String getText() { return "\ufffc"; }
        	public Object getData() { return data; }
        	public int length() { return 1; }
        	public char charAt( int index )
        	{
                return getText().charAt(0);
        	}

        	public Optional<AbstractSegment> subSequence( int start, int end )
        	{
        		if ( start == 0 )  return Optional.of( this );
        		return Optional.empty();
        	}

        	public Optional<AbstractSegment> join( AbstractSegment nextSeg )
        	{
        		return Optional.empty();
        	}

        	public abstract Node createNode( TextStyle style );

        	/**
        	 * RichTextFX uses this for undo and redo.
        	 */
            @Override public boolean equals( Object obj )
            {
            	if ( obj == this )  return true;
            	else if ( obj instanceof AbstractSegment && getClass().equals( obj.getClass() ) )
                {
                    return getText().equals( ((AbstractSegment) obj).getText() );
                }

                return false;
            }

        }

    public static class IndentSegment extends AbstractSegment
    {
        /**
         * Displays a Label containing indentStr<br>
         * @param indentStr is tabs or spaces depending on the desired indent width
         */
        public IndentSegment( Object indentStr )
        {
            super( indentStr );
        }


        @Override
        public Node createNode( TextStyle style )
        {
            Label  item = new Label( getData().toString() );
            //if ( style != null && ! style.isEmpty() )  item.getStyleClass().add( style );
            return item;
        }

        @Override
        public String getText() { return ""; }
    }

    public static class TextSegment extends AbstractSegment
    {
    	private final String text;

    	public TextSegment( Object text )
    	{
    		super( text );
    		this.text = text.toString();
    	}

    	@Override
    	public Node createNode( TextStyle style )
    	{
    		Text  te = new TextExt( text );
            if (style != null)
            {

                te.setTextOrigin (VPos.TOP);
                te.getStyleClass ().add (StyleClassNames.TEXT);
                te.setStyle (style.toCss ());

            }
    		//if ( style != null && ! style.isEmpty() ) textNode.getStyleClass().add( style );
    		return te;
    	}

    	@Override
    	public char charAt( int index )
    	{
            return text.charAt( index );
    	}

    	@Override
    	public String getText() { return text; }

    	@Override
    	public int length() { return text.length(); }

    	@Override
    	public Optional<AbstractSegment> subSequence( int start, int end )
    	{
    		if ( start == 0 && end == length() )  return Optional.of( this );
    		return Optional.of
    		(
    			new TextSegment( text.substring( start, end ) )
    		);
    	}

    	@Override
    	public Optional<AbstractSegment> join( AbstractSegment nextSeg )
    	{
    		if ( nextSeg instanceof TextSegment )
    		{
    			return Optional.of
    			(
    				new TextSegment( text + nextSeg.getText() )
    			);
    		}
    		return Optional.empty();
    	}

    }

    public static class MySegmentOps implements TextOps<AbstractSegment,TextStyle>
    {
    	private final AbstractSegment EMPTY = new TextSegment("");

    	@Override
    	public AbstractSegment create( String text )
    	{
    		if ( text == null || text.isEmpty() )  return EMPTY;
    		return new TextSegment( text );
    	}

    	@Override
    	public AbstractSegment createEmptySeg()
    	{
    		return EMPTY;
    	}

    	@Override
    	public char charAt( AbstractSegment seg, int index )
    	{
    		return seg.charAt( index );
    	}

    	@Override
    	public String getText( AbstractSegment seg )
    	{
    		return seg.getText();
    	}

    	@Override
    	public int length( AbstractSegment seg )
    	{
    		return seg.length();
    	}

    	@Override
    	public AbstractSegment subSequence( AbstractSegment seg, int start )
    	{
    		return subSequence( seg, start, seg.length() );
    	}

    	@Override
    	public AbstractSegment subSequence( AbstractSegment seg, int start, int end )
    	{
    		if ( start == seg.length() || end == 0 ) return EMPTY;
    		Optional<AbstractSegment>  opt = seg.subSequence( start, end );
    		return opt.orElse( EMPTY );
    	}

    	@Override
    	public Optional<AbstractSegment> joinSeg( AbstractSegment currentSeg, AbstractSegment nextSeg )
    	{
    		return currentSeg.join( nextSeg );
    	}

    }

    public TextEditor (StringWithMarkup text,
                       TextProperties   props,
                       DictionaryProvider2 prov)
    {

        super (ParaStyle.EMPTY,
               // para -> TextFlow
               // style -> ParaStyle
               (para, style) ->
               {

                   para.setStyle (style.toCss ());

               },
               TextStyle.EMPTY,
               indentOps,
               // Node factory function, converts a StyledSegment<String, TextStyle> into a Node.
               (seg ->
               {

                   return seg.getSegment ().createNode (seg.getStyle ());

/*
                       TextExt te = new TextExt (txt);
                       te.setTextOrigin (VPos.TOP);
                       te.getStyleClass ().add (StyleClassNames.TEXT);
                       te.setStyle (seg.getStyle ().toCss ());

                       return te;
*/

               }));

        this.props = new TextProperties ();
        this.bindTo (props);

        // Build a custom undo manager that is suspendable, see: https://github.com/FXMisc/RichTextFX/issues/735
        this.suspendUndos = new SuspendableYes ();
        this.setUndoManager (UndoManagerFactory.unlimitedHistoryFactory ().createMultiChangeUM(this.multiRichChanges ().conditionOn (this.suspendUndos),
                                                                            TextChange::invert,
                                                                            UndoUtils.applyMultiRichTextChange(this),
                                                                            TextChange::mergeWith,
                                                                            TextChange::isIdentity));

        this.setAutoScrollOnDragDesired (true);
        this.setWrapText (true);

        Runnable r = () ->
        {


        };

        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv != null)
            {

                this.getScene ().addPostLayoutPulseListener (() ->
                {

                    this.readyForUseProp.setValue (true);

                });

            } else {

                this.readyForUseProp.setValue (false);

            }

        });

        if (this.getScene () != null)
        {

            this.getScene ().addPostLayoutPulseListener (() ->
            {

                this.readyForUseProp.setValue (true);

            });

        }

        this.spellChecker = new QSpellChecker (this,
                                               prov);

        // TODO Markup.
        this.setText (text);
        this.setEditable (false);

        this.caretPositionProperty ().addListener ((pr, oldv, newv) ->
        {

System.out.println ("HERE: " + newv);

        });

        // Hijack Ctrl+Left (incl Shift) to navigate around an indent.
        addEventFilter( KeyEvent.KEY_PRESSED, KE ->
        {
            if ( KE.isShortcutDown() ) switch ( KE.getCode() )
            {
                case LEFT : case KP_LEFT : {
                    this.skipToPrevWord( KE.isShiftDown() );
                    KE.consume();
                    break;
                }
            }
        });

        // Prevent the caret from appearing on the left hand side of an indent.
        caretPositionProperty().addListener( (ob,oldPos,newPos) ->
        {
            if ( indent && getCaretColumn() == 0 ) {
                AbstractSegment seg = getParagraph( getCurrentParagraph() ).getSegments().get(0);
                if ( seg instanceof IndentSegment ) {
                    displaceCaret( newPos + 1 );
                }
            }
        });

        addEventHandler( KeyEvent.KEY_PRESSED, KE ->
                {
                    if ( indent && KE.getCode() == KeyCode.ENTER ) {
                        int caretPosition = getCaretPosition();
                        //UIUtils.runLater ( () -> replace( caretPosition, caretPosition, INDENT_DOC ) );

                        if ( getParagraph( getCurrentParagraph() ).length() == 0 ) {
                            UIUtils.runLater ( () -> replace( caretPosition, caretPosition, INDENT_DOC ) );
                        }

                    }
                });

        this.addEventHandler (MouseEvent.MOUSE_MOVED,
                              ev ->
        {

            this.mousePos = new Point2D (ev.getX (),
                                         ev.getY ());

        });

    }

    @Override // Navigating around/over indents
        public void nextChar( SelectionPolicy policy )
        {
            if ( getCaretPosition() < getLength() ) {
                // offsetByCodePoints throws an IndexOutOfBoundsException unless colPos is adjusted to accommodate any indents, see this.moveTo
                moveTo( Direction.RIGHT, policy, (paragraphText,colPos) -> Character.offsetByCodePoints( paragraphText, colPos, +1 ) );
            }
        }

        @Override // Navigating around/over indents
        public void previousChar( SelectionPolicy policy )
        {
            if ( getCaretPosition() > 0 ) {
                // offsetByCodePoints throws an IndexOutOfBoundsException unless colPos is adjusted to accommodate any indents, see this.moveTo
                moveTo( Direction.LEFT, policy, (paragraphText,colPos) -> Character.offsetByCodePoints( paragraphText, colPos, -1 ) );
            }
        }

        // Handles Ctrl+Left and Ctrl+Shift+Left
        private void skipToPrevWord( boolean isShiftDown )
        {
            int caretPos = getCaretPosition();
            if ( caretPos >= 1 )
            {
                boolean prevCharIsWhiteSpace = false;
                if ( indent && getCaretColumn() == 1 ) {
                    // Check for indent as charAt(0) throws an IndexOutOfBoundsException because Indents aren't represented by a character
                    AbstractSegment seg = getParagraph( getCurrentParagraph() ).getSegments().get(0);
                    prevCharIsWhiteSpace = seg instanceof IndentSegment;
                }
                if ( ! prevCharIsWhiteSpace ) prevCharIsWhiteSpace = Character.isWhitespace( getText( caretPos-1, caretPos ).charAt(0) );
                wordBreaksBackwards( prevCharIsWhiteSpace ? 2 : 1, isShiftDown ? SelectionPolicy.ADJUST : SelectionPolicy.CLEAR );
            }
        }

        /**
         * Skips n number of word boundaries backwards.
         */
        @Override // Accommodating Indent
        public void wordBreaksBackwards( int n, SelectionPolicy selection )
        {
            if( getLength() == 0 ) return;

            moveTo( Direction.LEFT, selection, (paragraphText,colPos) ->
            {
                BreakIterator wordIterator = BreakIterator.getWordInstance();
                wordIterator.setText( paragraphText );
                wordIterator.preceding( colPos );
                for ( int i = 1; i < n; i++ ) {
                    wordIterator.previous();
                }
                return wordIterator.current();
            });
        }

        /**
         * Skips n number of word boundaries forward.
         */
        @Override // Accommodating Indent
        public void wordBreaksForwards( int n, SelectionPolicy selection )
        {
            if( getLength() == 0 ) return;

            moveTo( Direction.RIGHT, selection, (paragraphText,colPos) ->
            {
                BreakIterator wordIterator = BreakIterator.getWordInstance();
                wordIterator.setText( paragraphText );
                wordIterator.following( colPos );
                for ( int i = 1; i < n; i++ ) {
                    wordIterator.next();
                }
                return wordIterator.current();
            });
        }

        /**
         * Because Indents are not represented in the text by a character there is a discrepancy
         * between the caret position and the text position which has to be taken into account.
         * So this method ADJUSTS the caret position before invoking the supplied function.
         *
         * @param dir LEFT for backwards, and RIGHT for forwards
         * @param selection CLEAR or ADJUST
         * @param colPosCalculator a function that receives PARAGRAPH text and an ADJUSTED
         * starting column position as parameters and returns an end column position.
         */
        private void moveTo( Direction dir, SelectionPolicy selection, BiFunction<String,Integer,Integer> colPosCalculator )
        {
            int colPos = getCaretColumn();
            int pNdx = getCurrentParagraph();
            Paragraph p = getParagraph( pNdx );
            int pLen = p.length();

            boolean adjustCol = indent && p.getSegments().get(0) instanceof IndentSegment;
            if ( adjustCol ) colPos--;

            if ( dir == Direction.LEFT && colPos == 0 ) {
                p = getParagraph( --pNdx );
                adjustCol = indent && p.getSegments().get(0) instanceof IndentSegment;
                colPos = p.getText().length(); // don't simplify !
            }
            else if ( dir == Direction.RIGHT && (pLen == 0 || colPos >= pLen-1) )
            {
                p = getParagraph( ++pNdx );
                adjustCol = indent && p.getSegments().get(0) instanceof IndentSegment;
                colPos = 0;
            }
            else colPos = colPosCalculator.apply( p.getText(), colPos );

            if ( adjustCol ) colPos++;

            moveTo( pNdx, colPos, selection );
        }

    public void setIndentOn( boolean indent ) {
        this.indent = indent;
    }

    public boolean isIndentOn() {
        return indent;
    }

    public void append( AbstractSegment customSegment )
    {
        insert( getLength(), customSegment );
    }

    public void insert( int pos, AbstractSegment customSegment )
    {
        insert( pos, ReadOnlyStyledDocument.fromSegment( customSegment, ParaStyle.EMPTY, TextStyle.EMPTY, indentOps ) );
    }

    @Override
    public void replace( int start, int end, StyledDocument replacement )
    {
        if ( ! indent ) super.replace( start, end, replacement );
        else
        {
            List<Paragraph> pl = replacement.getParagraphs();
            ReadOnlyStyledDocumentBuilder db = new ReadOnlyStyledDocumentBuilder( indentOps, ParaStyle.EMPTY );

            for ( int p = 0; p < pl.size(); p++ )
            {
                List segments = pl.get(p).getStyledSegments();

                if ( p > 1 && pl.get( p-1 ).length() == 0 )
                {
                    if ( ! (pl.get( p ).getSegments().get(0) instanceof IndentSegment) )
                    {
                        if ( segments instanceof AbstractList ) {
                            segments = new ArrayList<>( segments );
                        }
                        segments.add( 0, INDENT_SEG );
                    }
                }

                db.addParagraph( segments );
            }

            super.replace( start, end, db.build() );
        }
    }

    public BooleanProperty readyForUseProperty ()
    {

        return this.readyForUseProp;

    }

    public boolean isReadyForUse ()
    {

        return this.readyForUseProp.getValue ();

    }

    public boolean isIgnoreDocumentChanges ()
    {

        return this.ignoreDocumentChange;

    }

    public void setText (StringWithMarkup text)
    {

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.insertText (0,
                             text != null ? text.getText () : "");

            this.ignoreDocumentChange = false;

        });

    }

    public int getTextPositionForMousePosition (double x,
                                                double y)
    {

        return this.hit (x,
                         y).getInsertionIndex ();

    }

    public int getTextPositionForCurrentMousePosition ()
    {

        return this.getTextPositionForMousePosition (this.mousePos.getX (),
                                                     this.mousePos.getY ());

    }

    public Point2D getMousePosition ()
    {

        return this.mousePos;

    }

    public StringWithMarkup getTextWithMarkup ()
    {

        // TODO Get markup.
        return new StringWithMarkup (this.getText ());

    }

    public void toggleBold ()
    {

        // TODO

    }

    public void toggleItalic ()
    {

        // TODO

    }

    public void toggleUnderline ()
    {

        // TODO

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

        if (this.spellChecker != null)
        {

            this.spellChecker.setSynonymProvider (sp);

        }

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

    public void setDictionaryProvider (DictionaryProvider2 dp)
    {

        this.spellChecker.setDictionaryProvider (dp);

        this.checkSpelling ();

    }

    public void checkSpelling ()
    {

        if (this.spellChecker != null)
        {

            if (this.spellChecker.isEnabled ())
            {

                this.spellChecker.checkAll ();

            }

        }

    }

    public List getSpellCheckSuggestions (Word word)
    {

        if (this.spellChecker == null)
        {

            return new ArrayList ();

        }

        return this.spellChecker.getSuggestions (word);

    }

    @Override
    public void dispose ()
    {

        this.props.unbindAll ();
        super.dispose ();

    }

    public int getParagraphForOffset (int offset)
    {

        int c = 0;
        int pi = 0;

        for (Paragraph p : this.getParagraphs ())
        {

            int pl = p.length () + 1;

            if ((offset >= c)
                &&
                (offset < c + pl)
               )
            {

                return pi;

            }

            c += pl;
            pi++;

        }

        return -1;

    }

    /*
     * Get the character/paragraph bounds for a position/offset in the text.  Note: this is an expensive call that should be made
     * only when absolutely necessary.
     *
     * @param pos The offset into the text.
     * @return The bounds or null if the offset is outside the the text.
     */
    public Bounds getBoundsForPosition (int pos)
    {

        if ((pos + 1) >= this.getText ().length ())
        {

            return null;

        }

        Bounds cb = this.getCharacterBoundsOnScreen (pos, pos + 1).orElse (null);

        if (cb == null)
        {

            cb = this.getParagraphBoundsOnScreen (this.getParagraphForOffset (pos)).orElse (null);

        }

        return cb;

    }

    public IndexRange getParagraphTextRange (int paraNo)
    {

        int st = 0;

        if (paraNo > 0)
        {

            st = this.getParagraphTextOffset (paraNo - 1);

        }

        int l = this.getParagraph (paraNo).length ();

        return new IndexRange (st,
                               st + l);

    }

    public int getParagraphTextOffset (int paraNo)
    {

        return this.getParagraphs ().subList (0, paraNo + 1).stream ()
            .collect (Collectors.summingInt (p -> p.length () + 1));

    }

    private void updateStyleInSelection (TextStyle mixin)
    {

        IndexRange selection = this.getSelection();
        if (selection.getLength () != 0)
        {
/*
            StyleSpans<TextStyle> styles = this.getStyleSpans(selection);
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(mixin));
            this.setStyleSpans(selection.getStart(), newStyles);
*/
        }

    }

    public void removeSpellingError (IndexRange r)
    {

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.getContent ().setStyleSpans (r.getStart (),
                                              this.getContent ().getStyleSpans (r.getStart (),
                                                                                r.getEnd ()).mapStyles (ss ->
            {

                TextStyle _s = new TextStyle (ss);
                _s.setSpellingError (false);
                return _s;

            }));

            this.ignoreDocumentChange = false;

        });

    }

    public void clearAllSpellingErrors ()
    {

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.getContent ().setStyleSpans (0,
                                              this.getContent ().getStyleSpans (0,
                                                                                this.getText ().length ()).mapStyles (ss ->
            {

                ss.setSpellingError (false);
                return ss;

            }));

            this.ignoreDocumentChange = false;

        });

    }

    public void setSpellingErrors (int start,
                                   int end,
                                   List<IndexRange> errs)
    {

//        UIUtils.runLater (() ->
//        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {
                long s2 = System.currentTimeMillis ();

                this.getContent ().setStyleSpans (start,
                                                  this.getContent ().getStyleSpans (start,
                                                                                    end).mapStyles (ss ->
                {

                    ss.setSpellingError (false);
                    return ss;

                }));

                errs.stream ()
                    .forEach (r ->
                    {

                        this.getContent ().setStyleSpans (r.getStart (),
                                                          this.getContent ().getStyleSpans (r.getStart (),
                                                                                            r.getEnd ()).mapStyles (ss ->
                        {

                            TextStyle _s = new TextStyle (ss);
                            _s.setSpellingError (true);
                            return _s;

                        }));

                    });

                    this.ignoreDocumentChange = false;

                });

        //});

    }

    public void addSpellingError (IndexRange r)
    {

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.getContent ().setStyleSpans (r.getStart (),
                                              this.getContent ().getStyleSpans (r.getStart (),
                                                                                r.getEnd ()).mapStyles (ss ->
            {

                TextStyle _s = new TextStyle (ss);
                _s.setSpellingError (true);
                return _s;

            }));

            this.ignoreDocumentChange = false;

        });

    }

    private void updateTextStyle (Function<TextStyle, TextStyle> updater)
    {

        StyleSpans<TextStyle> styles = this.getStyleSpans (0, this.getText ().length ());
        StyleSpans<TextStyle> newStyles = styles.mapStyles (style ->
        {
            style = updater.apply (style);
            return style;
        });
        this.clearStyle (0, this.getText ().length ());
        this.setStyleSpans (0, newStyles);

    }

    private void updateParagraphStyle (Function<ParaStyle, ParaStyle> updater)
    {

        for(int i = 0; i < this.getParagraphs ().size (); ++i)
        {

            Paragraph<ParaStyle, AbstractSegment, TextStyle> p = this.getParagraph(i);
            this.setParagraphStyle (i, updater.apply (p.getParagraphStyle ()));

        }

    }

    private void updateParagraphStyleInSelection(Function<ParaStyle, ParaStyle> updater)
    {
/*
        IndexRange selection = this.getSelection ();
        int startPar = this.offsetToPosition(selection.getStart(), TwoDimensional.Bias.Forward).getMajor();
        int endPar = this.offsetToPosition(selection.getEnd(), TwoDimensional.Bias.Backward).getMajor();
        for(int i = startPar; i <= endPar; ++i) {
            Paragraph<ParaStyle, String, TextStyle> paragraph = this.getParagraph(i);
            this.setParagraphStyle(i, updater.apply(paragraph.getParagraphStyle()));
        }
*/

    }

    public void bindTo (TextProperties props)
    {

        this.props.unbindAll ();

        if (props == null)
        {

            return;

        }

        this.props.bindTo (props);

        //this.setIgnoreDocumentChanges (true);

        //this.ts.setFontSize (props.getFontSize ());
        //this.ts.setFontFamily (props.getFontFamily ());
        //this.ts.setTextColor (props.getTextColor ());

        //this.ps.setLineSpacing (props.getLineSpacing ());
        //this.ps.setAlignment (props.getAlignment ());

        this.props.highlightWritingLineProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.setLineHighlighterOn (newv);
                this.ignoreDocumentChange = false;

            });

        });

        this.props.writingLineColorProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.setLineHighlighterFill (newv);
                this.ignoreDocumentChange = false;

            });

        });

        this.props.fontSizeProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                NORMAL_STYLE.updateFontSize (props.getFontSize ());
                SPELLING_ERROR_STYLE.updateFontSize (props.getFontSize ());
                this.updateTextStyle (style -> style.updateFontSize (props.getFontSize ()));
                this.updateParagraphStyle (style -> style.updateFontSize (props.getFontSize ()));

                this.ignoreDocumentChange = false;

            });

        });

        this.props.fontFamilyProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                NORMAL_STYLE.updateFontFamily (props.getFontFamily ());
                SPELLING_ERROR_STYLE.updateFontFamily (props.getFontFamily ());
                this.updateTextStyle (style -> style.updateFontFamily (props.getFontFamily ()));

                this.ignoreDocumentChange = false;

            });

        });

        this.props.alignmentProperty ().addListener ((pr, oldv, newv) ->
        {


            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style -> style.updateAlignment (props.getAlignment ()));

                this.ignoreDocumentChange = false;

            });

        });

        this.props.textColorProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                NORMAL_STYLE.updateTextColor (props.getTextColor ());
                SPELLING_ERROR_STYLE.updateTextColor (props.getTextColor ());
                this.updateTextStyle (style -> style.updateTextColor (props.getTextColor ()));
                this.ignoreDocumentChange = false;

            });

        });

        this.props.lineSpacingProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style -> style.updateLineSpacing (props.getLineSpacing ()));
                this.ignoreDocumentChange = false;

            });

        });

        this.props.textBorderProperty ().addListener ((pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style -> style.updateTextBorder (props.getTextBorder ()));
                this.ignoreDocumentChange = false;

            });

        });

        NORMAL_STYLE.setFontSize (props.getFontSize ());
        SPELLING_ERROR_STYLE.setFontSize (props.getFontSize ());
        NORMAL_STYLE.setFontFamily (props.getFontFamily ());
        SPELLING_ERROR_STYLE.setFontFamily (props.getFontFamily ());
        NORMAL_STYLE.updateTextColor (props.getTextColor ());
        SPELLING_ERROR_STYLE.updateTextColor (props.getTextColor ());

        this.updateTextStyle (style ->
        {

            return style.updateFontSize (props.getFontSize ())
                .updateFontFamily (props.getFontFamily ())
                .updateTextColor (props.getTextColor ());

        });

        this.updateParagraphStyle (style ->
        {

            return style.updateLineSpacing (props.getLineSpacing ())
                .updateAlignment (props.getAlignment ())
                .updateTextBorder (props.getTextBorder ());

        });

        this.setLineHighlighterOn (props.isHighlightWritingLine ());
        this.setLineHighlighterFill (props.getWritingLineColor ());

    }

    private static class Styleable extends HashSet<String>
    {

        public String toCss ()
        {

            return this.stream ()
                .collect (Collectors.joining (" "));

        }

    }

    public static class TextStyle extends Styleable
    {

        public static final TextStyle EMPTY = new TextStyle();
/*
        public static TextStyle bold(boolean bold) { return EMPTY.updateBold(bold); }
        public static TextStyle italic(boolean italic) { return EMPTY.updateItalic(italic); }
        public static TextStyle underline(boolean underline) { return EMPTY.updateUnderline(underline); }
        public static TextStyle strikethrough(boolean strikethrough) { return EMPTY.updateStrikethrough(strikethrough); }
        public static TextStyle fontSize(int fontSize) { return EMPTY.updateFontSize(fontSize); }
        public static TextStyle fontFamily(String family) { return EMPTY.updateFontFamily(family); }
        public static TextStyle textColor(Color color) { return EMPTY.updateTextColor(color); }
        public static TextStyle backgroundColor(Color color) { return EMPTY.updateBackgroundColor(color); }
*/
        Optional<Boolean> bold = null;
        Optional<Boolean> italic;
        Optional<Boolean> underline;
        Optional<Boolean> strikethrough;
        Optional<Integer> fontSize;
        Optional<String> fontFamily;
        Optional<Color> textColor;
        Optional<Color> backgroundColor;
        Optional<Boolean> spellingError = null;

        static String cssColor(Color color) {
            int red = (int) (color.getRed() * 255);
            int green = (int) (color.getGreen() * 255);
            int blue = (int) (color.getBlue() * 255);
            return "rgb(" + red + ", " + green + ", " + blue + ")";
        }

        public TextStyle ()
        {

            this(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            );
        }

        public TextStyle (TextStyle from)
        {

            this (from.bold,
                  from.italic,
                  from.underline,
                  from.strikethrough,
                  from.fontSize,
                  from.fontFamily,
                  from.textColor,
                  from.backgroundColor,
                  from.spellingError);

        }

        public TextStyle(
                Optional<Boolean> bold,
                Optional<Boolean> italic,
                Optional<Boolean> underline,
                Optional<Boolean> strikethrough,
                Optional<Integer> fontSize,
                Optional<String> fontFamily,
                Optional<Color> textColor,
                Optional<Color> backgroundColor,
                Optional<Boolean> spellingError) {

            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            this.strikethrough = strikethrough;
            this.fontSize = fontSize;
            this.fontFamily = fontFamily;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.spellingError = spellingError;
        }

        public TextStyle updateSpellingError (boolean v)
        {

            this.setSpellingError (v);
            return this;

        }

        public void setSpellingError (boolean v)
        {

            this.spellingError = Optional.of (v);
            //SPELLING_ERROR_STYLE.spellingError = Optional.of (v);

        }

        public TextStyle updateFontSize (Integer i)
        {

            this.setFontSize (i);
            return this;

        }

        public void setFontSize (Integer i)
        {

            this.fontSize = Optional.of (i);

        }

        public TextStyle updateFontFamily (String i)
        {

            this.setFontFamily (i);
            return this;

        }

        public void setFontFamily (String i)
        {

            this.fontFamily = Optional.of (i);

        }

        public TextStyle updateTextColor (Color i)
        {

            this.setTextColor (i);
            return this;

        }

        public void setTextColor (Color i)
        {

            this.textColor = Optional.of (i);

        }

        public TextStyle updateBold (boolean bold)
        {

            this.setBold (bold);
            return this;

        }

        public void setBold (boolean bold)
        {

            this.bold = Optional.of (bold);

        }

        public void setItalic (boolean italic)
        {

            this.italic = Optional.of (italic);

        }

        public TextStyle updateItalic (boolean italic)
        {

            this.setItalic (italic);
            return this;

        }

        public void setUnderline (boolean underline)
        {

            this.underline = Optional.of (underline);

        }

        public TextStyle updateUnderline (boolean underline)
        {

            this.setUnderline (underline);
            return this;

        }

        public void setStrikethrough (boolean v)
        {

            this.strikethrough = Optional.of (v);

        }

        public TextStyle updateStrikethrough (boolean strikethrough)
        {

            this.setStrikethrough (strikethrough);
            return this;

        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    bold, italic, underline, strikethrough,
                    fontSize, fontFamily, textColor, backgroundColor);
        }

        @Override
        public boolean equals(Object other) {
            if(other instanceof TextStyle) {
                TextStyle that = (TextStyle) other;
                return Objects.equals(this.bold,            that.bold) &&
                       Objects.equals(this.italic,          that.italic) &&
                       Objects.equals(this.underline,       that.underline) &&
                       Objects.equals(this.strikethrough,   that.strikethrough) &&
                       Objects.equals(this.fontSize,        that.fontSize) &&
                       Objects.equals(this.fontFamily,      that.fontFamily) &&
                       Objects.equals(this.textColor,       that.textColor) &&
                       Objects.equals(this.backgroundColor, that.backgroundColor) &&
                       Objects.equals(this.spellingError, that.spellingError);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            List<String> styles = new ArrayList<>();

            bold           .ifPresent(b -> styles.add(b.toString()));
            italic         .ifPresent(i -> styles.add(i.toString()));
            underline      .ifPresent(u -> styles.add(u.toString()));
            strikethrough  .ifPresent(s -> styles.add(s.toString()));
            fontSize       .ifPresent(s -> styles.add(s.toString()));
            fontFamily     .ifPresent(f -> styles.add(f.toString()));
            textColor      .ifPresent(c -> styles.add(c.toString()));
            backgroundColor.ifPresent(b -> styles.add(b.toString()));

            return String.join(",", styles);
        }

        public String toCss() {
            StringBuilder sb = new StringBuilder();

            if(bold.isPresent()) {
                if(bold.get()) {
                    sb.append("-fx-font-weight: bold;");
                } else {
                    sb.append("-fx-font-weight: normal;");
                }
            }

            if(italic.isPresent()) {
                if(italic.get()) {
                    sb.append("-fx-font-style: italic;");
                } else {
                    sb.append("-fx-font-style: normal;");
                }
            }

            if(underline.isPresent()) {
                if(underline.get()) {
                    sb.append("-fx-underline: true;");
                } else {
                    sb.append("-fx-underline: false;");
                }
            }

            if(strikethrough.isPresent()) {
                if(strikethrough.get()) {
                    sb.append("-fx-strikethrough: true;");
                } else {
                    sb.append("-fx-strikethrough: false;");
                }
            }

            if(fontSize.isPresent()) {
                sb.append("-fx-font-size: " + fontSize.get() + "pt;");
            }

            if(fontFamily.isPresent()) {
                sb.append("-fx-font-family: \"" + fontFamily.get() + "\";");
            }

            if(textColor.isPresent()) {
                Color color = textColor.get();
                sb.append("-fx-fill: " + cssColor(color) + ";");
            }

            if(backgroundColor.isPresent()) {
                Color color = backgroundColor.get();
                sb.append("-rtfx-background-color: " + cssColor(color) + ";");
            }

            if ((this.spellingError.isPresent ())
                &&
                (this.spellingError.get ())
               )
            {

                sb.append ("-fx-underline: false;");
                sb.append ("-rtfx-underline-width: 2px;");
                sb.append ("-rtfx-underline-color: red;");

            }

            return sb.toString();
        }
/*
        public TextStyle updateWith(TextStyle mixin) {
                return new TextStyle(
                        mixin.bold.isPresent() ? mixin.bold : bold,
                        mixin.italic.isPresent() ? mixin.italic : italic,
                        mixin.underline.isPresent() ? mixin.underline : underline,
                        mixin.strikethrough.isPresent() ? mixin.strikethrough : strikethrough,
                        mixin.fontSize.isPresent() ? mixin.fontSize : fontSize,
                        mixin.fontFamily.isPresent() ? mixin.fontFamily : fontFamily,
                        mixin.textColor.isPresent() ? mixin.textColor : textColor,
                        mixin.backgroundColor.isPresent() ? mixin.backgroundColor : backgroundColor);
        }
*/
/*
        public TextStyle updateFontSize(int fontSize) {
            return new TextStyle(bold, italic, underline, strikethrough, Optional.of(fontSize), fontFamily, textColor, backgroundColor);
        }
        */
/*
        public TextStyle updateFontFamily(String fontFamily) {
            return new TextStyle(bold, italic, underline, strikethrough, fontSize, Optional.of(fontFamily), textColor, backgroundColor);
        }
        */
/*
        public TextStyle updateTextColor(Color textColor) {
            return new TextStyle(bold, italic, underline, strikethrough, fontSize, fontFamily, Optional.of(textColor), backgroundColor);
        }
*/
/*
        public TextStyle updateBackgroundColor(Color backgroundColor) {
            return new TextStyle(bold, italic, underline, strikethrough, fontSize, fontFamily, textColor, Optional.of(backgroundColor));
        }
*/
    }

    public static class ParaStyle extends Styleable
    {

        public static final ParaStyle EMPTY = new ParaStyle();

        private Optional<Float> lineSpacing = Optional.empty ();
        private Optional<String> alignment = Optional.empty ();
        private Optional<Integer> textBorder = Optional.empty ();
        private int fontSize = 12;

        public ParaStyle ()
        {

        }

        public String toCss() {
            StringBuilder sb = new StringBuilder();

            if(this.alignment.isPresent()) {

                String a = this.alignment.get ();

                if (a.equals (ALIGN_JUSTIFIED))
                {

                    a = "justify";

                }

                sb.append (String.format ("-fx-text-alignment: %1$s;",
                                          a));
            }

            if (this.lineSpacing.isPresent ())
            {

                float s = (this.lineSpacing.get () - 1) * this.fontSize;

                sb.append (String.format ("-fx-line-spacing: %1$spt;",
                                          s));

            }

            if (this.textBorder.isPresent ())
            {

                float s = (this.lineSpacing.get () - 1) * this.fontSize;

                sb.append (String.format ("-fx-padding: 0 %1$s %2$spt %1$s;",
                                          this.textBorder.get () + 3,
                                          s));

            }

            return sb.toString ();
        }

        public ParaStyle updateFontSize (int s)
        {

            this.fontSize = s;
            return this;

        }

        public void setLineSpacing (Float f)
        {

            this.lineSpacing = Optional.of (f);

        }

        public ParaStyle updateLineSpacing (float l)
        {

            this.setLineSpacing (l);
            return this;

        }

        public ParaStyle updateTextBorder (int i)
        {

            this.setTextBorder (i);
            return this;

        }

        public void setTextBorder (int i)
        {

            this.textBorder = Optional.of (i);

        }

        public void setAlignment (String a)
        {

            this.alignment = Optional.of (a);

        }

        public ParaStyle updateAlignment (String a)
        {

            this.setAlignment (a);
            return this;

        }

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

    public Position createTextPosition (int pos)
    {

        return new Position (pos,
                             this);

    }

    /**
     * @returns A new builder.
     */
    public static TextEditor.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, TextEditor>
    {

        private String styleName = null;
        private TextProperties props = null;
        private StringWithMarkup text = null;
        private DictionaryProvider2 dictProv = null;
        private SynonymProvider synProv = null;

        private Builder ()
        {

        }

        public Builder synonymProvider (SynonymProvider prov)
        {

            this.synProv = null;
            return _this ();

        }

        public Builder dictionaryProvider (DictionaryProvider2 prov)
        {

            this.dictProv = prov;
            return _this ();

        }

        public Builder text (StringWithMarkup t)
        {

            this.text = t;
            return _this ();

        }

        public Builder textProperties (TextProperties p)
        {

            this.props = p;
            return _this ();

        }

        @Override
        public TextEditor build ()
        {

            TextEditor.TextStyle ts = new TextStyle ();
            TextEditor.ParaStyle ps = new ParaStyle ();
            TextEditor ed = new TextEditor (text,
                                            props,
                                            dictProv);

            ed.setSynonymProvider (synProv);
/*
            TextEditor ed = new TextEditor (ts,
                                            ps,
                                            text,
                                            props);
*/
            ed.getStyleClass ().add (StyleClassNames.TEXTEDITOR);

            if (this.styleName != null)
            {

                ed.getStyleClass ().add (this.styleName);

            }

            return ed;

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

    }

    public class Position
    {

        private Subscription sub = null;
        private int pos = 0;
        private IntegerProperty posProp = new SimpleIntegerProperty ();

        public Position (int pos,
                         TextEditor ed)
        {

            this.pos = pos;
            this.posProp.setValue (pos);

            this.sub = ed.getContent ().multiPlainChanges ().subscribe (changes ->
            {

                for (PlainTextChange c : changes)
                {

                    if (c.getPosition () <= this.pos)
                    {

                        this.pos += c.getNetLength ();
                        this.posProp.setValue (this.pos);

                    }

                }

            });

        }

        public IntegerProperty positionProperty ()
        {

            return this.posProp;

        }

        public void dispose ()
        {

            this.sub.unsubscribe ();

        }

    }

}
