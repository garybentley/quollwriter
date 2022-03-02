package com.quollwriter.ui.fx.components;

import org.fxmisc.richtext.*;
import org.fxmisc.richtext.util.*;
import org.fxmisc.richtext.model.*;
import org.reactfx.*;
import org.reactfx.util.*;
import org.fxmisc.undo.*;
import org.fxmisc.wellbehaved.event.*;
import org.fxmisc.richtext.Selection.Direction;

import java.io.*;
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
import javafx.scene.image.*;
import javafx.scene.Node;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.text.Word;
import com.quollwriter.text.TextIterator;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class TextEditor extends GenericStyledArea<TextEditor.ParaStyle, TextEditor.AbstractSegment, TextEditor.TextStyle>
{

    public static final String ALIGN_LEFT = "Left";
    public static final String ALIGN_RIGHT = "Right";
    public static final String ALIGN_JUSTIFIED = "Justified";

    private Point2D mousePos = null;
    private TextProperties props = null;
    public QSpellChecker          spellChecker = null;
    private SuspendableYes suspendUndos = null;
    private boolean ignoreDocumentChange = false;
    private BooleanProperty readyForUseProp = new SimpleBooleanProperty (false);
    private Map<Object, Paint> highlights = new HashMap<> ();

    private static MySegmentOps indentOps = new MySegmentOps ();
    private boolean formattingEnabled = false;
    private StringProperty placeholder = null;
    private PropertyBinder textPropsBinder = new PropertyBinder ();
    private Node caretNode = null;
    private ObjectProperty<Color> backgroundColorProp = new SimpleObjectProperty (null);

        private boolean indent = true;

        public static abstract class AbstractSegment<E extends Node>
        {
        	protected final Object  data;
            protected Collection<String> styleClassNames = null;

            public static final Codec<AbstractSegment> CODEC = new Codec<> ()
            {

                @Override
                public String getName ()
                {

                    return "segment";

                }

                @Override
                public void encode (DataOutputStream os,
                                    AbstractSegment  s)
                             throws IOException
                {

                    os.writeUTF (s.getText ());

                }

                @Override
                public AbstractSegment decode (DataInputStream is)
                                        throws IOException
                {

                    return new TextSegment (is.readUTF ());

                }

            };

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

        	public abstract E createNode( TextStyle style );

        	/**
        	 * RichTextFX uses this for undo and redo.
        	 */
            @Override public boolean equals( Object obj )
            {
            	if ( obj == this )  return true;
            	else if ( obj instanceof AbstractSegment && getClass().equals( obj.getClass() ) )
                {

                    boolean v = getText().equals( ((AbstractSegment) obj).getText() );
                    return v;
                }

                return false;
            }

            public void setStyleClassNames (Collection<String> styleClassNames)
            {

                this.styleClassNames = styleClassNames;

            }

        }

    public static abstract class SegmentedSegment<X extends Region> extends AbstractSegment<X>
    {

        protected List<AbstractSegment> segs = null;

        public SegmentedSegment (List<AbstractSegment> segs)
        {

            this.segs = segs;

        }

    }

    public static class HRSegment extends AbstractSegment<Path>
    {

        public HRSegment ()
        {

            super ();

        }

        @Override
        public Path createNode (TextStyle s)
        {

            HLineTo h = new HLineTo ();

            // Return a path that is the width of the bounds.
            Path p = new Path ();
            p.getElements ().add (h);
            // TODO h.xProperty ().bind (p.layoutBounds ().widthProperty ());
            return p;

        }

    }

    public static class ImageSegment extends AbstractSegment<ImageView>
    {

        public ImageSegment (String src)
        {

            // TODO

        }

        @Override
        public ImageView createNode (TextStyle style)
        {

            ImageView iv = new ImageView ();

            if (this.styleClassNames != null)
            {

                iv.getStyleClass ().addAll (this.styleClassNames);

            }

            return iv;

        }

    }

    public static class InlineSegment extends SegmentedSegment<HBox>
    {

        protected InlineSegment ()
        {

            super (new ArrayList<> ());

        }

        public InlineSegment (List<AbstractSegment> segs)
        {

            super (segs);

        }

        @Override
        public HBox createNode (TextStyle style)
        {

            HBox b = new HBox ();

            if (this.styleClassNames != null)
            {

                b.getStyleClass ().addAll (this.styleClassNames);

            }

            b.getChildren ().addAll (this.segs.stream ()
                .map (s -> s.createNode (style))
                .collect (Collectors.toList ()));

            return b;

        }

    }

    public static class BlockSegment extends SegmentedSegment<VBox>
    {

        public BlockSegment (List<AbstractSegment> segs)
        {

            super (segs);

        }

        @Override
        public VBox createNode (TextStyle style)
        {

            VBox b = new VBox ();

            if (this.styleClassNames != null)
            {

                b.getStyleClass ().addAll (this.styleClassNames);

            }

            b.getChildren ().addAll (this.segs.stream ()
                .map (s -> s.createNode (style))
                .collect (Collectors.toList ()));

            return b;

        }

    }

    public static class LISegment extends InlineSegment
    {

        public LISegment (List<AbstractSegment> segs)
        {

            super (segs);

        }

        @Override
        public HBox createNode (TextStyle s)
        {

            HBox b = super.createNode (s);

            ImageView im = new ImageView ();
            im.getStyleClass ().add (StyleClassNames.BULLET);

            b.getChildren ().add (0,
                                  im);

            return b;

        }

    }

    public void runOnReady (Runnable r)
    {

        if (this.readyForUseProp.getValue ())
        {

            UIUtils.forceRunLater (r);

        } else {

            this.readyForUseProp.addListener ((pr, oldv, newv) ->
            {

                if (newv)
                {

                    UIUtils.forceRunLater (r);

                }

            });

        }

    }

    public static class HyperlinkSegment extends InlineSegment
    {

        private String display = null;
        private Consumer<Node> onClick = null;

        public HyperlinkSegment (List<AbstractSegment> segs,
                                 Consumer<Node>        onClick)
        {
            super (segs);
            this.onClick = onClick;

        }

        @Override
        public HBox createNode( TextStyle style )
        {

            HBox te = super.createNode (style);

            te.setOnMouseClicked (ev ->
            {

                try
                {

                    this.onClick.accept (te);

                } catch (Exception e) {

                    Environment.logError ("Unable to run",
                                          e);

                    // TODO Show error to user.

                }

            });

            return te;
        }

    }

    public static class TextSegment extends AbstractSegment<TextExt>
    {
    	private final String text;

    	public TextSegment( Object text )
    	{
    		super( text );
    		this.text = text.toString();
    	}

    	@Override
    	public TextExt createNode( TextStyle style )
    	{
    		TextExt  te = new TextExt( text );
            te.setTextOrigin (VPos.TOP);
            te.getStyleClass ().add (StyleClassNames.TEXT);

            if (style != null)
            {

                te.setStyle (style.toCss ());

            }

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
    		return new TextSegment ("");
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
    		if ( start == seg.length() || end == 0 ) return new TextSegment ("");
    		Optional<AbstractSegment>  opt = seg.subSequence( start, end );
    		return opt.orElse( new TextSegment ("") );
    	}

    	@Override
    	public Optional<AbstractSegment> joinSeg( AbstractSegment currentSeg, AbstractSegment nextSeg )
    	{
    		return currentSeg.join( nextSeg );
    	}

    }
/*
TODO
    public void setAsLink (int               start,
                           int               end,
                           Consumer<Node> onClick)
    {

        this.replace (start,
                      end,
                      ReadOnlyStyledDocument.fromSegment (new HyperlinkSegment (this.getText (start, end),
                                                                                onClick),
                                                          null,
                                                          this.getStyleAtPosition (start),
                                                          indentOps));

    }
*/
    public TextEditor (StringWithMarkup text,
                       TextProperties   props,
                       DictionaryProvider2 prov)
    {

        super (new ParaStyle (),
               // para -> TextFlow
               // style -> ParaStyle
               (para, style) ->
               {

                   if (style != null)
                   {

                       para.setStyle (style.toCss ());

                   }

               },
               new TextStyle (),
               indentOps,
               // Node factory function, converts a StyledSegment<String, TextStyle> into a Node.
               (seg ->
               {

                   return seg.getSegment ().createNode (seg.getStyle ());

               }));

        // Build a custom undo manager that is suspendable, see: https://github.com/FXMisc/RichTextFX/issues/735
        this.suspendUndos = new SuspendableYes ();

        // Needed for encoding style information during copy and paste.
        this.setStyleCodecs (
            ParaStyle.CODEC,
            Codec.styledSegmentCodec (AbstractSegment.CODEC,
                                      TextStyle.CODEC)
        );

        this.setUseInitialStyleForInsertion (false);//true);
        this.props = new TextProperties ();
        this.bindTo (props);

        this.parentProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv == null)
            {

                this.ignoreDocumentChange = true;

            }

            if (newv != null)
            {

                UIUtils.runLater (() ->
                {

                    this.ignoreDocumentChange = false;

                });

            }

        });

        if (props != null)
        {

            TextStyle ts = new TextStyle ();
            ts.updateFontSize (props.getFontSize ())
                .updateFontFamily (props.getFontFamily ())
                .updateTextColor (props.getTextColor ());

            this.setTextInsertionStyle (ts);

            ParaStyle ps = new ParaStyle ();
            ps.updateLineSpacing (props.getLineSpacing ())
                .updateParagraphSpacing (props.getParagraphSpacing ())
                .updateTextBorder (props.getTextBorder ())
                .updateAlignment (props.getAlignment ())
                .updateFontSize (props.getFontSize ());

            this.setParagraphInsertionStyle (ps);

        }

        this.selectionProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv.getStart () != newv.getEnd ())
            {

                this.ignoreDocumentChange = true;

                this.suspendUndos.suspendWhile (() ->
                {

                    this.ignoreDocumentChange = false;

                });

            } else {

            }

        });

        this.setUndoManager (org.fxmisc.richtext.util.UndoUtils.richTextSuspendableUndoManager (this, this.suspendUndos));
        /*
        this.setUndoManager (UndoManagerFactory.unlimitedHistoryFactory ().createMultiChangeUM(this.multiRichChanges ().conditionOn (this.suspendUndos),
                                                                            TextChange::invert,
                                                                            UndoUtils.applyMultiRichTextChange(this),
                                                                            TextChange::mergeWith,
                                                                            TextChange::isIdentity));
*/
        this.setAutoScrollOnDragDesired (true);
        this.setWrapText (true);

        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv != null)
            {

                this.readyForUseProp.setValue (true);

                this.getScene ().addPostLayoutPulseListener (() ->
                {

                    //this.readyForUseProp.setValue (true);

                });

            } else {

                //this.readyForUseProp.setValue (false);

            }

        });

        if (this.getScene () != null)
        {

            this.getScene ().addPostLayoutPulseListener (() ->
            {

                this.readyForUseProp.setValue (true);

            });

        }

        this.readyForUseProp.addListener ((pr, oldv, newv) ->
        {

            if (newv)
            {

                if (text != null)
                {

                    this.setText (text);

                }

                this.requestLayout ();

            }

        });

        this.spellChecker = new QSpellChecker (this,
                                               prov);

        //this.setText (text);
        this.setEditable (false);

        this.addEventHandler (MouseEvent.MOUSE_MOVED,
                              ev ->
        {

            this.mousePos = new Point2D (ev.getX (),
                                         ev.getY ());

        });

        this.focusedProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateCaretNode ();

        });

        this.caretPositionProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setUseInitialStyleForInsertion (false);
            this.setTextInsertionStyle (this.getStyleOfChar (newv));

            this.updateCaretNode ();

        });

        Nodes.addInputMap (this,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.B, KeyCombination.SHORTCUT_DOWN),
                                          ev ->
                                          {

                                              this.toggleBold ();

                                          }));

        Nodes.addInputMap (this,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.I, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                 this.toggleItalic ();

                                             }));

         Nodes.addInputMap (this,
                            InputMap.consume (EventPattern.keyPressed (KeyCode.U, KeyCombination.SHORTCUT_DOWN),
                                              ev ->
                                              {

                                                  this.toggleUnderline ();

                                              }));

    }

    public ObjectProperty<Color> backgroundColorProperty ()
    {

        return this.backgroundColorProp;

    }

    private void updateCaretNode ()
    {

        if (this.caretNode == null)
        {

            Node n = this.lookup (".caret");

            if (n != null)
            {

                this.caretNode = n;

                if (this.props.getTextColor () != null)
                {

                    this.caretNode.setStyle ((n.getStyle () != null ? n.getStyle () : "") + " " + " -fx-stroke: " + UIUtils.colorToHex (this.props.getTextColor ()) + ";");

                }

            }

        } else {

            if (this.props.getTextColor () != null)
            {

                this.caretNode.setStyle (" -fx-stroke: " + UIUtils.colorToHex (this.props.getTextColor ()) + ";");

            }

        }

    }

    public void setPlaceholder (StringProperty p)
    {

        if (p == null)
        {

            return;

        }

        this.placeholder = p;

        TextFlow tf = new TextFlow (new Text (p.getValue ()));
        tf.getStyleClass ().add (StyleClassNames.PLACEHOLDER);
        tf.prefWidthProperty ().bind (this.widthProperty ());
        tf.maxHeightProperty ().bind (tf.heightProperty ());
        tf.minHeightProperty ().bind (tf.heightProperty ());
        tf.setCenterShape (false);

        this.setPlaceholder (tf);

    }

    public Set<MenuItem> getSpellingSynonymItemsForContextMenu (AbstractViewer viewer)
    {

        Set<MenuItem> items = new LinkedHashSet<> ();

        Point2D p = this.getMousePosition ();

        if (p != null)
        {

            TextIterator iter = new TextIterator (this.getText ());

            final Word w = iter.getWordAt (this.getTextPositionForMousePosition (p.getX (),
                                                                                 p.getY ()));

            if (w != null)
            {

                final String word = w.getText ();

                final int loc = w.getAllTextStartOffset ();

                List<String> l = this.getSpellCheckSuggestions (w);

                if (l != null)
                {

                    List<String> prefix = Arrays.asList (dictionary,spellcheck,popupmenu,LanguageStrings.items);

                    if (l.size () == 0)
                    {

                        MenuItem mi = QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,nosuggestions)))
                            .iconName (StyleClassNames.NOSUGGESTIONS)
                            .onAction (ev ->
                            {

                                this.addWordToDictionary (word);

                                viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
                                                         ProjectEvent.Action.addword,
                                                         word);

                            })
                            .build ();
                        mi.setDisable (true);
                        items.add (mi);

                    } else
                    {

                        if (l.size () > 15)
                        {

                            l = l.subList (0, 15);

                        }

                        Consumer<String> replace = (repWord ->
                        {

                            int cp = this.getCaretPosition ();

                            this.replaceText (loc,
                                              loc + word.length (),
                                              repWord);

                            this.moveTo (cp - 1);

                            viewer.fireProjectEvent (ProjectEvent.Type.spellcheck,
                                                     ProjectEvent.Action.replace,
                                                     repWord);

                        });

                        List<String> more = null;

                        if (l.size () > 5)
                        {

                            more = l.subList (5, l.size ());
                            l = l.subList (0, 5);

                        }

                        items.addAll (l.stream ()
                            .map (repWord ->
                            {

                                return QuollMenuItem.builder ()
                                    .label (new SimpleStringProperty (repWord))
                                    .onAction (ev -> replace.accept (repWord))
                                    .build ();

                            })
                            .collect (Collectors.toList ()));

                        if (more != null)
                        {

                            items.add (QuollMenu.builder ()
                                .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.more)))
                                .styleClassName (StyleClassNames.MORE)
                                .items (new LinkedHashSet<> (more.stream ()
                                    .map (repWord ->
                                    {

                                        return QuollMenuItem.builder ()
                                            .label (new SimpleStringProperty (repWord))
                                            .onAction (ev -> replace.accept (repWord))
                                            .build ();

                                    })
                                    .collect (Collectors.toList ())))
                                .build ());

                        }

                    }

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,add)))
                        .iconName (StyleClassNames.ADDWORD)
                        .onAction (ev ->
                        {

                            this.addWordToDictionary (word);

                            viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
                                                     ProjectEvent.Action.addword,
                                                     word);

                        })
                        .build ());

                    items.add (new SeparatorMenuItem ());

                } else
                {

                    // TODO Make nicer...
                    if (viewer instanceof AbstractProjectViewer)
                    {

                        AbstractProjectViewer pv = (AbstractProjectViewer) viewer;

                        if (pv.synonymLookupsSupported ())
                        {

                            // TODO Check this...
                            if (pv.isLanguageFunctionAvailable ())
                            {

                                if ((word != null) &&
                                    (word.length () > 0))
                                {

                                    //String mt = "No synonyms found for: " + word;

                                    try
                                    {

                                        // See if there are any synonyms.
                                        if ((this.getSynonymProvider () != null)
                                            &&
                                            (this.getSynonymProvider ().hasSynonym (word))
                                           )
                                        {

                                            items.add (QuollMenuItem.builder ()
                                                .iconName (StyleClassNames.FIND)
                                                .label (getUILanguageStringProperty (Arrays.asList (synonyms,popupmenu,LanguageStrings.items,find),
                                                                                     word))
                                                .onAction (ev ->
                                                {

                                                    String pid = "synonym" + word + this.hashCode ();

                                                    QuollPopup qp = viewer.getPopupById (pid);

                                                    if (qp != null)
                                                    {

                                                        qp.toFront ();
                                                        return;

                                                    }

                                                    qp = new ShowSynonymsPopup (viewer,
                                                                                w,
                                                                                this).getPopup ();
                                                    qp.setPopupId (pid);

                                                    Bounds b = viewer.screenToLocal (this.getBoundsForPosition (loc));

                                                    viewer.showPopup (qp,
                                                                      b,
                                                                      Side.BOTTOM);

                                                })
                                                .build ());

                                        } else {

                                            MenuItem mi = QuollMenuItem.builder ()
                                                .iconName (StyleClassNames.NOSYNONYMS)
                                                .label (getUILanguageStringProperty (Arrays.asList (synonyms,popupmenu,LanguageStrings.items,nosynonyms),
                                                                                     word))
                                                .build ();
                                            mi.setDisable (true);
                                            items.add (mi);

                                        }

                                    } catch (Exception e) {

                                        Environment.logError ("Unable to determine whether word: " +
                                                              word +
                                                              " has synonyms.",
                                                              e);

                                    }

                                }

                            }

                        }

                    }

                }

            }

        }

        return items;

    }

    public MenuItem getCompressedEditItemsForContextMenu ()
    {

        List<String> prefix = Arrays.asList (formatting,edit,popupmenu,items);

        String sel = this.getSelectedText ();

        List<Node> buts = new ArrayList<> ();

        // Only add if there is something to cut.
        if (!sel.equals (""))
        {

            buts.add (QuollButton.builder ()
                .iconName (StyleClassNames.CUT)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,cut,tooltip)))
                .onAction (ev ->
                {

                    this.cut ();

                })
                .build ());
            buts.add (QuollButton.builder ()
                .iconName (StyleClassNames.COPY)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,copy,tooltip)))
                .onAction (ev ->
                {

                    this.copy ();

                })
                .build ());

        }

        if (UIUtils.clipboardHasContent ())
        {

            buts.add (QuollButton.builder ()
                .iconName (StyleClassNames.PASTE)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,paste,tooltip)))
                .onAction (ev ->
                {

                    this.paste ();

                })
                .build ());

        }

        if (this.getUndoManager ().isUndoAvailable ())
        //if (this.editor.getUndoManager ().canUndo ())
        {

            // Only add if there is an undo available.
            buts.add (QuollButton.builder ()
                .iconName (StyleClassNames.UNDO)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,undo,tooltip)))
                .onAction (ev ->
                {

                    this.getUndoManager ().undo ();

                })
                .build ());

        }

        if (this.getUndoManager ().isRedoAvailable ())
        {

            buts.add (QuollButton.builder ()
                .iconName (StyleClassNames.REDO)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,redo,tooltip)))
                .onAction (ev ->
                {

                    this.getUndoManager ().redo ();

                })
                .build ());

        }

        if (buts.size () > 0)
        {

            return UIUtils.createCompressedMenuItem (getUILanguageStringProperty (formatting,edit,popupmenu,title),
                                                     buts);


        }

        return null;

    }

    @Override
    public void paste ()
    {

        // Copied from richtext/ClipboardActions.paste.
        // Here we need to update/set the correct styles for the current text.
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if(getStyleCodecs().isPresent()) {
            Tuple2<Codec<ParaStyle>, Codec<StyledSegment<AbstractSegment, TextStyle>>> codecs = getStyleCodecs().get();
            Codec<StyledDocument<ParaStyle, AbstractSegment, TextStyle>> codec = ReadOnlyStyledDocument.codec(codecs._1, codecs._2, getSegOps());
            DataFormat format = DataFormat.lookupMimeType(codec.getName ());
            if(format == null) {
                format = new DataFormat(codec.getName ());
            }
            if(clipboard.hasContent(format)) {
                byte[] bytes = (byte[]) clipboard.getContent(format);
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(is);
                StyledDocument<ParaStyle, AbstractSegment, TextStyle> doc = null;
                try {
                    doc = codec.decode(dis);
                } catch (IOException e) {
                    System.err.println("Codec error: Failed to decode '" + codec.getName() + "':");
                    e.printStackTrace();
                }
                if(doc != null) {

                    TextStyle ts = this.getTextInsertionStyle ();
                    System.out.println ("SS: " + ts);
                    ReadOnlyStyledDocumentBuilder b = new ReadOnlyStyledDocumentBuilder (getSegOps (),
                                                                                         this.getParagraphInsertionStyle ());

                    for (Paragraph<ParaStyle, AbstractSegment, TextStyle> p : doc.getParagraphs ())
                    {

                        b.addParagraph (p.getSegments (),
                                        p.getStyleSpans ().mapStyles (ss ->
                                        {

                                            if (ss == null)
                                            {

                                                return ss;

                                            }

                                            if (ts == null)
                                            {

                                                return ss;

                                            }

                                            TextStyle s = new TextStyle (ss);
                                            ss.setFontSize (ts.getFontSize ());
                                            ss.setFontFamily (ts.getFontFamily ());
                                            ss.setTextColor (ts.getTextColor ());
                                            return ss;

                                        }));

                    }

                    replaceSelection(b.build ());
                    return;
                }
            }
        }

        super.paste ();

    }

    public void setFormattingEnabled (boolean v)
    {

        this.formattingEnabled = v;

    }

    public MenuItem getCompressedFormatItemsForContextMenu ()
    {

        String sel = this.getSelectedText ();

        if ((!sel.equals (""))
            &&
            (this.formattingEnabled)
           )
        {

            List<String> prefix = Arrays.asList (formatting,format,popupmenu,items);

            List<Node> buts = new ArrayList<> ();

            buts.add (QuollButton.builder ()
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,bold,tooltip)))
                .iconName (StyleClassNames.BOLD)
                .onAction (ev ->
                {

                    this.toggleBold ();

                })
                .build ());

            buts.add (QuollButton.builder ()
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,italic,tooltip)))
                .iconName (StyleClassNames.ITALIC)
                .onAction (ev ->
                {

                    this.toggleItalic ();

                })
                .build ());

            buts.add (QuollButton.builder ()
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,underline,tooltip)))
                .iconName (StyleClassNames.UNDERLINE)
                .onAction (ev ->
                {

                    this.toggleUnderline ();

                })
                .build ());

            return UIUtils.createCompressedMenuItem (getUILanguageStringProperty (formatting,format,popupmenu,title),
                                                     buts);

        } else {

            return null;

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

        if (!this.readyForUseProp.getValue ())
        {

            this.readyForUseProp.addListener ((pr, oldv, newv) ->
            {

                if (newv)
                {

                    UIUtils.runLater (() ->
                    {

                        this.setText (text);

                    });

                }

            });
/*
            // TODO Handle the case where the editor is removed from the scene.
            UIUtils.runLater (() ->
            {

                this.setText (text);

            });
*/
            return;

        }

        if ((text != null)
            &&
            (text.getText () == null)
           )
        {

            return;

        }

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.clear ();

            //this.setUseInitialStyleForInsertion (true);

            this.insertText (0,
                             text != null ? text.getText () : "");

            this.applyMarkup (text != null ? text.getMarkup () : null);

            this.ignoreDocumentChange = false;

        });

    }

    private void applyMarkup (Markup markup)
    {

        if (markup != null)
        {

            markup.apply (this);

        }

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

        return new StringWithMarkup (this.getText (),
                                     this.getMarkup ());

    }

    private Markup getMarkup ()
    {

        return new Markup (this);

    }

    private void toggle (Consumer<TextStyle> mixin)
    {

        IndexRange r = this.getSelection ();

        if (r.getStart () == r.getEnd ())
        {

            //mixin.accept (this.getInitialTextStyle ());
/*
            TextStyle _s = new TextStyle (this.getStyleAtPosition (r.getStart ()));
            mixin.accept (_s);
            this.setInitialTextStyle (_s);
            this.setUseInitialStyleForInsertion (true);
*/
            return;

        }

        this.setStyleSpans (r.getStart (),
                                          this.getStyleSpans (r).mapStyles (ss ->
        {

            TextStyle _s = new TextStyle (ss);
            mixin.accept (_s);

            return _s;

        }));

    }

    public void toggleBold ()
    {

        if (!this.formattingEnabled)
        {

            return;

        }

        IndexRange r = this.getSelection ();

        if (r.getStart () != r.getEnd ())
        {

            TextStyle s = this.getStyleAtPosition (r.getStart () + 1);

            boolean t = !s.isBold ();

            this.toggle (_s -> _s.setBold (t));

        } else {

            TextStyle s = this.getStyleAtPosition (r.getStart ());

            boolean t = !s.isBold ();
            TextStyle _s = new TextStyle (s);
            _s.setBold (t);

            this.setUseInitialStyleForInsertion (false);
            this.setTextInsertionStyle (_s);

        }

    }

    public void setItalic (IndexRange r)
    {

        if (!this.formattingEnabled)
        {

            return;

        }

        this.getContent ().setStyleSpans (r.getStart (),
                                          this.getContent ().getStyleSpans (r.getStart (),
                                                                            r.getEnd ()).mapStyles (ss ->
        {

            TextStyle _s = new TextStyle (ss);
            _s.setItalic (true);
            return _s;

        }));

    }

    public void setUnderline (IndexRange r)
    {

        if (!this.formattingEnabled)
        {

            return;

        }

        this.getContent ().setStyleSpans (r.getStart (),
                                          this.getContent ().getStyleSpans (r.getStart (),
                                                                            r.getEnd ()).mapStyles (ss ->
        {

            TextStyle _s = new TextStyle (ss);
            _s.setUnderline (true);
            return _s;

        }));

    }

    public void toggleItalic ()
    {

        if (!this.formattingEnabled)
        {

            return;

        }

        IndexRange r = this.getSelection ();

        if (r.getStart () != r.getEnd ())
        {

            TextStyle s = this.getStyleAtPosition (r.getStart () + 1);

            boolean t = !s.isItalic ();

            this.toggle (_s -> _s.setItalic (t));

        } else {

            TextStyle s = this.getStyleAtPosition (r.getStart ());

            boolean t = !s.isItalic ();
            TextStyle _s = new TextStyle (s);
            _s.setItalic (t);

            this.setUseInitialStyleForInsertion (false);
            this.setTextInsertionStyle (_s);

        }

    }

    public void toggleUnderline ()
    {

        if (!this.formattingEnabled)
        {

            return;

        }

        IndexRange r = this.getSelection ();

        if (r.getStart () != r.getEnd ())
        {

            TextStyle s = this.getStyleAtPosition (r.getStart () + 1);

            boolean t = !s.isUnderline ();

            this.toggle (_s -> _s.setUnderline (t));

        } else {

            TextStyle s = this.getStyleAtPosition (r.getStart ());

            boolean t = !s.isUnderline ();
            TextStyle _s = new TextStyle (s);
            _s.setUnderline (t);

            this.setUseInitialStyleForInsertion (false);
            this.setTextInsertionStyle (_s);

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

    /**
     * Switch on/off the writing line highlight, this doesn't affect the user property.
     */
    public void setHighlightWritingLine (boolean v)
    {

        this.setLineHighlighterOn (v);

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
        this.textPropsBinder.dispose ();
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
                (offset < (c + pl))
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

        if (pos > this.getText ().length ())
        {

            pos = this.getText ().length ();

        }

        if (pos < 0)
        {

            pos = 0;

        }

        int para = this.getParagraphForOffset (pos);
/*
        int vpara = this.allParToVisibleParIndex (para).orElse (-1);

        if (vpara < 0)
        {

            return null;

        }
*/
        Paragraph p = this.getParagraph (para);

        String t = p.getText ();

        if (t.length () == 0)
        {

            Bounds cb = this.getParagraphBoundsOnScreen (para).orElse (null);

            if (cb == null)
            {

                return null;

            }

            cb = new BoundingBox (cb.getMinX (),
                                  cb.getMinY (),
                                  cb.getMinZ (),
                                  cb.getWidth (),
                                  ((this.props.getLineSpacing () * 2) + this.props.getFontSize ()),//cb.getHeight () - ((this.props.getLineSpacing () - 1) * this.props.getFontSize ()),
                                  cb.getDepth ());

            return cb;

        } else {

            if (pos == 0)
            {

                Bounds cb = this.getCharacterBoundsOnScreen (pos, pos + 1).orElse (null);

                return cb;

            } else {

                IndexRange ir = this.getParagraphTextRange (para);

                if (pos == ir.getStart ())
                {

                    Bounds cb = this.getCharacterBoundsOnScreen (pos, pos + 1).orElse (null);

                    return cb;

                } else {

                    if ((pos == ir.getEnd ())
                        &&
                        (pos > 0)
                       )
                    {

                        Bounds cb = this.getCharacterBoundsOnScreen (pos - 1, pos).orElse (null);

                        return cb;

                    }

                    // TODO Check to see if this makes a difference?
                    Bounds cb = this.getCharacterBoundsOnScreen (pos, pos + 1).orElse (null);

                    return cb;

                }

            }

        }
/*
        if (pos == this.getText ().length ())
        {

            Bounds cb = this.getParagraphBoundsOnScreen (this.getParagraphForOffset (pos)).orElse (null);

            if (cb == null)
            {

                return null;

            }

            cb = new BoundingBox (cb.getMinX (),
                                  cb.getMinY (),
                                  cb.getMinZ (),
                                  cb.getWidth (),
                                  cb.getHeight () - (this.props.getLineSpacing () * this.props.getFontSize ()),
                                  cb.getDepth ());
System.out.println ("HERE! " + cb);
            return cb;

        }

        if ((pos == 0)
            &&
            (this.getText ().length () == 0)
           )
        {

            return this.getParagraphBoundsOnScreen (0).orElse (null);

        }

        Bounds cb = this.getCharacterBoundsOnScreen (pos, pos + 1).orElse (null);

        if (cb == null)
        {

            //int para = this.getParagraphForOffset (pos);
System.out.println ("PARA: " + para + ", " + pos);
            IndexRange r = this.getParagraphTextRange (para);

            //Paragraph p = this.getParagraph (para);

            if (p.getText ().length () == 0)
            {

                cb = this.getParagraphBoundsOnScreen (this.getParagraphForOffset (pos)).orElse (null);

                cb = new BoundingBox (cb.getMinX (),
                                      cb.getMinY (),
                                      cb.getMinZ (),
                                      cb.getWidth (),
                                      cb.getHeight () - (this.props.getLineSpacing () * this.props.getFontSize ()),
                                      cb.getDepth ());
    System.out.println ("HERE2! " + cb);
                return cb;


            }

            if (r.getStart () < r.getEnd ())
            {

                cb = this.getCharacterBoundsOnScreen (r.getEnd () - 1, r.getEnd ()).orElse (null);
System.out.println ("HEREX: " + r.getEnd () + ", " + cb);
            } else {

                cb = this.getParagraphBoundsOnScreen (para).orElse (null);
System.out.println ("HEREY: " + cb);
                if (cb == null)
                {

                    return null;

                }

                cb = new BoundingBox (cb.getMinX (),
                                      cb.getMinY (),
                                      cb.getMinZ (),
                                      cb.getWidth (),
                                      cb.getHeight () - (this.props.getLineSpacing () * this.props.getFontSize ()),
                                      cb.getDepth ());
System.out.println ("HEREZ: " + cb);
            }

        } else {
            System.out.println ("HEREXX: " + cb);
        }

        return cb;
*/
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

        UIUtils.runLater (() ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {


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

        });

    }

    public void setBold (IndexRange r)
    {

        this.getContent ().setStyleSpans (r.getStart (),
                                          this.getContent ().getStyleSpans (r.getStart (),
                                                                            r.getEnd ()).mapStyles (ss ->
        {

            TextStyle _s = new TextStyle (ss);
            _s.setBold (true);
            return _s;

        }));

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
            TextStyle ts = new TextStyle (style);
            ts = updater.apply (ts);
            return ts;
        });
        //this.clearStyle (0, this.getText ().length ());
        this.setStyleSpans (0, newStyles);
        //this.requestLayout ();

    }

    private void updateTextStyle (int startOffset,
                                  int endOffset,
                                  Function<TextStyle, TextStyle> updater)
    {

        int diff = endOffset - startOffset;

        StyleSpans<TextStyle> styles = this.getStyleSpans (startOffset, endOffset);
        StyleSpans<TextStyle> newStyles = styles.mapStyles (style ->
        {
            TextStyle ts = new TextStyle (style);
            ts = updater.apply (ts);
            return ts;
        });
        this.clearStyle (startOffset, endOffset);
        this.setStyleSpans (startOffset, newStyles);

    }

    public Highlight addHighlight (IndexRange r,
                                   Color      background)
    {

        Highlight obj = new Highlight (r.getStart (),
                                       r.getEnd ());

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.setStyleSpans (r.getStart (),
                                              this.getStyleSpans (r).mapStyles (ss ->
            {

                TextStyle _s = new TextStyle (ss);
                _s.addBackgroundColor (obj,
                                       background);
                return _s;

            }));

            this.ignoreDocumentChange = false;

        });

        return obj;

    }

    public void removeHighlight (Highlight h)
    {

        if (h == null)
        {

            return;

        }

        this.ignoreDocumentChange = true;

        this.suspendUndos.suspendWhile (() ->
        {

            this.getContent ().setStyleSpans (h.start,
                                              this.getContent ().getStyleSpans (h.start,
                                                                                h.end).mapStyles (ss ->
            {

                TextStyle _s = new TextStyle (ss);
                _s.removeBackgroundColor (h);
                return _s;

            }));

            this.requestLayout ();

            this.ignoreDocumentChange = false;

        });

    }

    @Override
    protected void configurePlaceholder (Node placeholder)
    {

        Region r = (Region) placeholder;

        r.prefWidthProperty().bind( this.widthProperty() );
        r.maxHeightProperty().bind(r.heightProperty() );
        r.minHeightProperty().bind(r.heightProperty() );
        r.setCenterShape( false );
    }

    public void setCaretPosition (int p)
    {

        this.getCaretSelectionBind ().moveTo (p);

    }

    private void updateParagraphStyle (Function<ParaStyle, ParaStyle> updater)
    {

        for(int i = 0; i < this.getParagraphs ().size (); ++i)
        {

            Paragraph<ParaStyle, AbstractSegment, TextStyle> p = this.getParagraph(i);
            ParaStyle ps = new ParaStyle (p.getParagraphStyle ());
            ps = updater.apply (ps);
            this.setParagraphStyle (i, ps);

        }

        ParaStyle ps = new ParaStyle ();
        ps.updateLineSpacing (props.getLineSpacing ())
            .updateParagraphSpacing (props.getParagraphSpacing ())
            .updateTextBorder (props.getTextBorder ())
            .updateAlignment (props.getAlignment ())
            .updateFontSize (props.getFontSize ());

        this.setParagraphInsertionStyle (ps);

    }

    public void setIgnoreDocumentChanges (boolean v)
    {

        this.ignoreDocumentChange = v;

    }

    public void bindTo (TextProperties props)
    {

        this.props.unbindAll ();
        this.textPropsBinder.dispose ();

        if (props == null)
        {

            return;

        }

        this.suspendUndos.suspendWhile (() ->
        {

            this.ignoreDocumentChange = true;

            this.props.bindTo (props);

            this.ignoreDocumentChange = false;


        });

        this.textPropsBinder.addChangeListener (this.props.backgroundColorProperty (),
                                                (pr, oldv, newv) ->
        {

            this.backgroundColorProp.setValue (newv);

        });

        this.textPropsBinder.addChangeListener (this.props.highlightWritingLineProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.setHighlightWritingLine (newv);
                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.writingLineColorProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.setLineHighlighterFill (newv);
                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.fontSizeProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateTextStyle (style -> style.updateFontSize (props.getFontSize ()));
                this.updateParagraphStyle (style -> style.updateFontSize (props.getFontSize ()));

                TextStyle ts = new TextStyle ();
                ts.updateFontSize (props.getFontSize ())
                    .updateFontFamily (props.getFontFamily ())
                    .updateTextColor (props.getTextColor ());

                this.setTextInsertionStyle (ts);

                ParaStyle ps = new ParaStyle ();
                ps.updateLineSpacing (props.getLineSpacing ())
                    .updateParagraphSpacing (props.getParagraphSpacing ())
                    .updateTextBorder (props.getTextBorder ())
                    .updateAlignment (props.getAlignment ())
                    .updateFontSize (props.getFontSize ());

                this.setParagraphInsertionStyle (ps);

                this.updateCaretNode ();

                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.fontFamilyProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateTextStyle (style -> style.updateFontFamily (props.getFontFamily ()));
                TextStyle ts = new TextStyle ();
                ts.updateFontSize (props.getFontSize ())
                    .updateFontFamily (props.getFontFamily ())
                    .updateTextColor (props.getTextColor ());

                this.setTextInsertionStyle (ts);
                this.updateCaretNode ();

                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.alignmentProperty (),
                                                (pr, oldv, newv) ->
        {


            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style -> style.updateAlignment (props.getAlignment ()));

                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.textColorProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateTextStyle (style -> style.updateTextColor (this.props.getTextColor ()));
                TextStyle ts = new TextStyle ();
                ts.updateFontSize (props.getFontSize ())
                    .updateFontFamily (props.getFontFamily ())
                    .updateTextColor (props.getTextColor ());

                this.setTextInsertionStyle (ts);
                this.updateCaretNode ();
                this.setUseInitialStyleForInsertion (false);
                this.ignoreDocumentChange = false;

                this.requestLayout ();

            });

        });

        this.textPropsBinder.addChangeListener (this.props.lineSpacingProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style ->
                {

                    style.updateParagraphSpacing (props.getParagraphSpacing ());
                    style.updateFontSize (props.getFontSize ());
                    style.updateLineSpacing (props.getLineSpacing ());
                    return style;
                });

                ParaStyle ps = new ParaStyle ();
                ps.updateLineSpacing (props.getLineSpacing ())
                    .updateParagraphSpacing (props.getParagraphSpacing ())
                    .updateTextBorder (props.getTextBorder ())
                    .updateAlignment (props.getAlignment ())
                    .updateFontSize (props.getFontSize ());

                this.setParagraphInsertionStyle (ps);

                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.paragraphSpacingProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style ->
                {

                    style.updateParagraphSpacing (props.getParagraphSpacing ());
                    style.updateFontSize (props.getFontSize ());
                    style.updateLineSpacing (props.getLineSpacing ());
                    return style;

                });

                ParaStyle ps = new ParaStyle ();
                ps.updateLineSpacing (props.getLineSpacing ())
                    .updateParagraphSpacing (props.getParagraphSpacing ())
                    .updateTextBorder (props.getTextBorder ())
                    .updateAlignment (props.getAlignment ())
                    .updateFontSize (props.getFontSize ());

                this.setParagraphInsertionStyle (ps);

                this.ignoreDocumentChange = false;

            });

        });

        this.textPropsBinder.addChangeListener (this.props.textBorderProperty (),
                                                (pr, oldv, newv) ->
        {

            this.ignoreDocumentChange = true;

            this.suspendUndos.suspendWhile (() ->
            {

                this.updateParagraphStyle (style -> style.updateTextBorder (props.getTextBorder ()));
                this.ignoreDocumentChange = false;

                ParaStyle ps = new ParaStyle ();
                ps.updateLineSpacing (props.getLineSpacing ())
                    .updateParagraphSpacing (props.getParagraphSpacing ())
                    .updateTextBorder (props.getTextBorder ())
                    .updateAlignment (props.getAlignment ())
                    .updateFontSize (props.getFontSize ());

                this.setParagraphInsertionStyle (ps);

            });

        });

        this.updateTextStyle (style ->
        {

            return style.updateFontSize (props.getFontSize ())
                .updateFontFamily (props.getFontFamily ())
                .updateTextColor (props.getTextColor ());

        });

        this.updateParagraphStyle (style ->
        {

            return style.updateLineSpacing (props.getLineSpacing ())
                .updateParagraphSpacing (props.getParagraphSpacing ())
                .updateAlignment (props.getAlignment ())
                .updateTextBorder (props.getTextBorder ())
                .updateFontSize (props.getFontSize ());

        });

        TextStyle ts = new TextStyle ();
        ts.updateFontSize (props.getFontSize ())
            .updateFontFamily (props.getFontFamily ())
            .updateTextColor (props.getTextColor ());

        this.setUseInitialStyleForInsertion (false);
        this.setTextInsertionStyle (ts);

        ParaStyle ps = new ParaStyle ();
        ps.updateLineSpacing (props.getLineSpacing ())
            .updateParagraphSpacing (props.getParagraphSpacing ())
            .updateTextBorder (props.getTextBorder ())
            .updateAlignment (props.getAlignment ())
            .updateFontSize (props.getFontSize ());

        this.setParagraphInsertionStyle (ps);

        UIUtils.forceRunLater (() ->
        {

            this.setHighlightWritingLine (props.isHighlightWritingLine ());

        });

        // TODO, Remove
        //this.setLineHighlighterOn (false);
        this.setLineHighlighterFill (props.getWritingLineColor ());
        this.backgroundColorProp.setValue (props.getBackgroundColor ());

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

        Optional<Boolean> bold = null;
        Optional<Boolean> italic;
        Optional<Boolean> underline;
        Optional<Boolean> strikethrough;
        Optional<Integer> fontSize;
        Optional<String> fontFamily;
        Optional<Color> textColor;
        //Optional<Paint> backgroundColor;
        Optional<Boolean> spellingError = null;
        Map<Object, Color> backgroundColors = new LinkedHashMap<> ();

        /*
         * Here we encode the style information about a text segment.
         * We write/read 3 booleans, one each for (and in this order):
         *     bold
         *     italic
         *     underline
         * If a value isn't present we write false.
         * On a read, the rest of the style information we grab from the insertion style.
         */
        public static final Codec<TextStyle> CODEC = new Codec<TextStyle> ()
        {

            @Override
            public String getName ()
            {

                return "qw-text-style";

            }

            @Override
            public void encode (DataOutputStream os,
                                TextStyle        ts)
                         throws IOException
            {

                os.writeBoolean (ts.bold.orElse (false));
                os.writeBoolean (ts.italic.orElse (false));
                os.writeBoolean (ts.underline.orElse (false));

            }

            @Override
            public TextStyle decode (DataInputStream is)
                              throws IOException
            {

                TextStyle ts = new TextStyle (Optional.of (is.readBoolean ()),
                                              Optional.of (is.readBoolean ()),
                                              Optional.of (is.readBoolean ()),
                                              Optional.empty (),
                                              Optional.empty (),
                                              Optional.empty (),
                                              Optional.empty (),
                                              null,
                                              Optional.empty ());

                return ts;

            }

        };

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
                null,
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
                  from.backgroundColors,
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
                Map<Object, Color> backgroundColors,
                Optional<Boolean> spellingError) {

            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            this.strikethrough = strikethrough;
            this.fontSize = fontSize;
            this.fontFamily = fontFamily;
            this.textColor = textColor;
            this.backgroundColors = backgroundColors != null ? new LinkedHashMap<> (backgroundColors) : null;
            this.spellingError = spellingError;
        }

        public TextStyle addBackgroundColor (Object obj,
                                             Color  p)
        {

            if (this.backgroundColors == null)
            {

                this.backgroundColors = new LinkedHashMap<> ();

            }

            this.backgroundColors.put (obj,
                                       p);
            return this;

        }

        public TextStyle removeBackgroundColor (Object obj)
        {

            if (this.backgroundColors == null)
            {

                return this;

            }

            this.backgroundColors.remove (obj);

            return this;

        }

        public TextStyle updateSpellingError (boolean v)
        {

            this.setSpellingError (v);
            return this;

        }

        public void setSpellingError (boolean v)
        {

            this.spellingError = Optional.of (v);

        }

        public TextStyle updateFontSize (Integer i)
        {

            this.setFontSize (i);
            return this;

        }

        public Integer getFontSize ()
        {

            return this.fontSize.orElse (12);

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

        public String getFontFamily ()
        {

            return this.fontFamily.orElse ("Georgia");

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

        public Color getTextColor ()
        {

            return this.textColor.orElse (Color.BLACK);

        }

        public void setTextColor (Color i)
        {

            this.textColor = Optional.ofNullable (i);

        }

        public boolean isBold ()
        {

            return this.bold.orElse (false);

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

        public boolean isItalic ()
        {

            return this.italic.orElse (false);

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

        public boolean isUnderline ()
        {

            return this.underline.orElse (false);

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
            return Objects.hash (
                    bold, italic, underline, strikethrough,
                    fontSize, fontFamily, textColor, backgroundColors);
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
                       Objects.equals(this.spellingError, that.spellingError) &&
                       Objects.equals (this.backgroundColors, that.backgroundColors);
            }

            return false;
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

            if (this.backgroundColors != null)
            {

                styles.add (this.backgroundColors.toString ());

            }

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

            if (this.backgroundColors != null)
            {

                Set<Object> keys = this.backgroundColors.keySet ();

                Color bgc = this.backgroundColors.get (keys.stream ()
                    .skip ((keys.size () > 0 ? keys.size () - 1 : 0))
                    .findFirst ()
                    .orElse (null));

                if (bgc != null)
                {

                    sb.append("-rtfx-background-color: " + cssColor(bgc) + ";");

                }

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

        private Optional<Float> lineSpacing = Optional.of (1f);//empty ();
        private Optional<Float> paraSpacing = Optional.of (0f);//empty ();
        private Optional<String> alignment = Optional.of ("left");//empty ();
        private Optional<Integer> textBorder = Optional.of (0);//empty ();
        private int fontSize = 12;

        public static final Codec<ParaStyle> CODEC = new Codec<> ()
        {

            @Override
            public String getName ()
            {

                return "para-style";

            }

            @Override
            public void encode (DataOutputStream os,
                                ParaStyle        ps)
                         throws IOException
            {

            }

            @Override
            public ParaStyle decode (DataInputStream is)
                              throws IOException
            {

                return new ParaStyle ();

            }

        };

        public ParaStyle ()
        {

        }

        public ParaStyle (ParaStyle ps)
        {

            this (ps.lineSpacing,
                  ps.alignment,
                  ps.textBorder,
                  ps.paraSpacing,
                  ps.fontSize);

        }

        public ParaStyle(
                Optional<Float> lineSpacing,
                Optional<String> alignment,
                Optional<Integer> textBorder,
                Optional<Float>   paraSpacing,
                int               fontSize)
        {

            this.lineSpacing = lineSpacing;
            this.alignment = alignment;
            this.textBorder = textBorder;
            this.paraSpacing = paraSpacing;
            this.fontSize = fontSize;

        }

        @Override
        public int hashCode() {
            return Objects.hash (
                    lineSpacing, paraSpacing, alignment, textBorder, fontSize);
        }

        @Override
        public boolean equals(Object other) {
            if(other instanceof ParaStyle) {
                ParaStyle that = (ParaStyle) other;
                return Objects.equals(this.lineSpacing,  that.lineSpacing) &&
                       Objects.equals(this.alignment,    that.alignment) &&
                       Objects.equals(this.textBorder,   that.textBorder) &&
                       Objects.equals(this.paraSpacing,   that.paraSpacing) &&
                       Objects.equals(this.fontSize,     that.fontSize);
            }

            return false;
        }

        public String toString ()
        {

            return this.toCss ();

        }

        public String toCss() {
            StringBuilder sb = new StringBuilder();

            if(this.alignment.isPresent()) {

                String a = this.alignment.get ();

                if (a.equalsIgnoreCase (ALIGN_JUSTIFIED))
                {

                    a = "justify";

                }

                sb.append (String.format ("-fx-text-alignment: %1$s;",
                                          a.toLowerCase ()));
            }

            if (this.lineSpacing.isPresent ())
            {

                float s = (this.lineSpacing.get () - 1) * this.fontSize;

                sb.append (String.format ("-fx-line-spacing: %1$spt;",
                                          s));

            }

            float ps = 0f;

            if (this.paraSpacing.isPresent ())
            {

                ps = this.paraSpacing.get () * this.fontSize;

            }

            int tb = this.textBorder.isPresent () ? this.textBorder.get () : 0;

            tb += 3;

            sb.append (String.format ("-fx-padding: 0 %1$spx %2$spt %1$spx;",
                                      tb,
                                      ps));

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

        public void setParagraphSpacing (Float f)
        {

            this.paraSpacing = Optional.of (f);

        }

        public ParaStyle updateParagraphSpacing (float l)
        {

            this.setParagraphSpacing (l);
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

        int l = this.getText ().length ();

        if ((pos < 0)
/*
            ||
            (pos)
*/
           )
        {

            throw new IllegalArgumentException ("Position: " + pos + ", is not valid.");

        }

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
        private boolean formattingEnabled = false;
        private String styleSheet = null;

        private Builder ()
        {

        }

        public Builder styleSheet (String s)
        {

            this.styleSheet = s;
            return _this ();

        }

        public Builder formattingEnabled (boolean v)
        {

            this.formattingEnabled = v;
            return _this ();

        }

        public Builder synonymProvider (SynonymProvider prov)
        {

            this.synProv = prov;
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

            UIUtils.addStyleSheet (ed,
                                   Constants.COMPONENT_STYLESHEET_TYPE,
                                   StyleClassNames.FILEFIND);
            if (this.styleSheet != null)
            {

                ed.getStylesheets ().add (this.styleSheet);

            }

            if (this.styleName != null)
            {

                ed.getStyleClass ().add (this.styleName);

            }

            ed.setFormattingEnabled (this.formattingEnabled);

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
        //private int pos = 0;
        private IntegerProperty posProp = new SimpleIntegerProperty ();

        public Position (int pos,
                         TextEditor ed)
        {

            this.posProp.setValue (pos);

            this.sub = ed.getContent ().multiPlainChanges ().subscribe (changes ->
            {

                for (PlainTextChange c : changes)
                {

                    if (c.getNetLength () < 0)
                    {

                        int p = this.posProp.getValue ();

                        if (c.getPosition () < p)
                        {

                            p += c.getNetLength ();

                        }

                        if (p < 0)
                        {

                            p = 0;

                        }

                        this.posProp.setValue (p);

                    } else {

                        int p = this.posProp.getValue ();

                        if (c.getPosition () <= p)
                        {

                            p += c.getNetLength ();

                            if (p < 0)
                            {

                                p = 0;

                            }

                            this.posProp.setValue (p);

                        }

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

    public class Highlight
    {

        public int start = -1;
        public int end = -1;

        public Highlight (int start,
                          int end)
        {

            this.start = start;
            this.end = end;

        }

    }

}
