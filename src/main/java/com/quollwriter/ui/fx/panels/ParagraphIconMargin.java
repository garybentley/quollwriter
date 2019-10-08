package com.quollwriter.ui.fx.panels;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.css.*;
import javafx.scene.Node;
import javafx.css.converter.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ParagraphIconMargin extends Pane
{

    private static final CssMetaData<ParagraphIconMargin, Number> NOTE_INDENT = new CssMetaData<> ("-qw-note-item-indent", SizeConverter.getInstance (), 0d)
    {

        @Override
        public boolean isSettable (ParagraphIconMargin node)
        {

            return (node.noteIndent == null)
                    ||
                   (!node.noteIndent.isBound ());

        }

        @Override
        public StyleableProperty getStyleableProperty (ParagraphIconMargin node)
        {

            return node.noteIndent;

        }

    };

    private static final CssMetaData<ParagraphIconMargin, Number> STRUCTUREITEM_INDENT = new CssMetaData<> ("-qw-structure-item-indent", SizeConverter.getInstance (), 0d)
    {

        @Override
        public boolean isSettable (ParagraphIconMargin node)
        {

            return (node.structureItemIndent == null)
                    ||
                   (!node.structureItemIndent.isBound ());

        }

        @Override
        public StyleableProperty getStyleableProperty (ParagraphIconMargin node)
        {

            return node.structureItemIndent;

        }

    };

    private static List<CssMetaData<? extends Styleable, ?>> styleables;

    static
    {

        List<CssMetaData<? extends Styleable, ?>> temp = new ArrayList<> ();

        temp.add (NOTE_INDENT);
        temp.add (STRUCTUREITEM_INDENT);

        temp.addAll (Pane.getClassCssMetaData ());

        styleables = Collections.unmodifiableList (temp);

    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData ()
    {

        return styleables;

    }

    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData ()
    {

        return ParagraphIconMargin.getClassCssMetaData ();

    }

    private StyleableDoubleProperty noteIndent = null;
    private StyleableDoubleProperty structureItemIndent = null;

    private TextEditor editor = null;
    private Chapter chapter = null;
    private int paraNo = -1;
    private ProjectViewer viewer = null;
    private Map<ChapterItem, Node> structureNodeMap = null;
    private Map<Double, Node> noteNodePosMap = null;
    private BiConsumer<ChapterItem, Node> itemNodeRegister = null;
    private Consumer<ChapterItem> showItem = null;
    private Function<IndexRange, List<ChapterItem>> getNewItems = null;
    //private SetChangeListener<ChapterItem> itemListener = null;
    private Number structureIndent = null;
    private Function<ChapterItem, Double> getItemIndent = null;

    public ParagraphIconMargin (ProjectViewer                           viewer,
                                TextEditor                              editor,
                                int                                     paraNo,
                                Chapter                                 chapter,
                                BiConsumer<ChapterItem, Node>           itemNodeRegister,
                                Consumer<ChapterItem>                   showItem,
                                Function<IndexRange, List<ChapterItem>> getNewItems,
                                Function<ChapterItem, Double>           getItemIndent)
    {

        this.paraNo = paraNo;
        this.editor = editor;
        this.chapter = chapter;
        this.viewer = viewer;
        this.noteIndent = new SimpleStyleableDoubleProperty (NOTE_INDENT, 0d);
        this.structureItemIndent = new SimpleStyleableDoubleProperty (STRUCTUREITEM_INDENT, 0d);
        this.itemNodeRegister = itemNodeRegister;
        this.showItem = showItem;
        this.getNewItems = getNewItems;
        this.getItemIndent = getItemIndent;

        final ParagraphIconMargin _this = this;

        this.addEventHandler (MouseEvent.MOUSE_PRESSED,
                              ev ->
        {

            ev.consume ();

            if ((ev.isPopupTrigger ())
                ||
                (ev.getClickCount () == 2)
               )
            {

                List<String> prefix = Arrays.asList (iconcolumn,doubleclickmenu,items);

                ContextMenu cm = new ContextMenu ();

                Set<MenuItem> items = new LinkedHashSet<> ();

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.Scene.OBJECT_TYPE)))
                    .styleClassName (StyleClassNames.SCENE)
                    .accelerator (new KeyCharacterCombination ("S",
                                                               KeyCombination.SHORTCUT_DOWN,
                                                               KeyCombination.SHIFT_DOWN))
                    .onAction (eev ->
                    {

                        this.viewer.createNewScene (this.chapter,
                                                    this.editor.getTextPositionForMousePosition (0,
                                                                                                 ev.getY ()));

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.OutlineItem.OBJECT_TYPE)))
                    .styleClassName (StyleClassNames.OUTLINEITEM)
                    .accelerator (new KeyCharacterCombination ("O",
                                                               KeyCombination.SHORTCUT_DOWN,
                                                               KeyCombination.SHIFT_DOWN))
                    .onAction (eev ->
                    {

                        this.viewer.createNewOutlineItem (this.chapter,
                                                          this.editor.getTextPositionForMousePosition (0,
                                                                                                       ev.getY ()));

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.Note.OBJECT_TYPE)))
                    .styleClassName (StyleClassNames.NOTE)
                    .accelerator (new KeyCharacterCombination ("N",
                                                               KeyCombination.SHORTCUT_DOWN,
                                                               KeyCombination.SHIFT_DOWN))
                    .onAction (eev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.newnote,
                                                this.chapter,
                                                this.editor.getTextPositionForMousePosition (0,
                                                                                             ev.getY ()));

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.editneedednote)))
                    .accelerator (new KeyCharacterCombination ("E",
                                                               KeyCombination.SHORTCUT_DOWN,
                                                               KeyCombination.SHIFT_DOWN))
                    .styleClassName (StyleClassNames.EDITNEEDEDNOTE)
                    .onAction (eev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.neweditneedednote,
                                                this.chapter,
                                                this.editor.getTextPositionForMousePosition (0,
                                                                                             ev.getY ()));

                    })
                    .build ());

                cm.getItems ().addAll (items);

                cm.setAutoHide (true);

                cm.show (this, ev.getScreenX (), ev.getScreenY ());
                ev.consume ();
/*
                ContextMenu _cm = _this.contextMenus.get ("iconcolumn");

                if (_cm != null)
                {

                    _cm.hide ();
                    _this.contextMenus.remove ("iconcolumn");

                }

                _this.contextMenus.put ("iconcolumn", cm);
*/
            }

        });

    }

    public int getParagraph ()
    {

        return this.paraNo;

    }

    public Node getStructureNodeForItem (ChapterItem i)
    {

        return this.structureNodeMap.get (i);

    }

    private Set<ChapterItem> getStructureItemsForTextRange (IndexRange ir)
    {

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        items.addAll (this.getItemsForTextRange (ir,
                                                 this.chapter.getScenes ()));

        this.chapter.getScenes ().stream ()
            .forEach (s ->
            {

                items.addAll (this.getItemsForTextRange (ir,
                                                         s.getOutlineItems ()));

            });

        items.addAll (this.getItemsForTextRange (ir,
                                                 this.chapter.getOutlineItems ()));

        return items;

    }

    private Set<ChapterItem> getStructureItemsForParagraph ()
    {

        return this.getStructureItemsForTextRange (this.editor.getParagraphTextRange (this.paraNo));

    }

    private <T extends ChapterItem> Set<T> getItemsForPosition (Set<T> its,
                                                                int    p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        double y = cb.getMinY ();

        return its.stream ()
            // Only interested in those that have the same y value.  i.e. on the same line.
            .filter (i ->
            {

                Bounds b = this.editor.getBoundsForPosition (i.getPosition () + 5);

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toSet ());

    }

    private <T extends ChapterItem> Map<Double, Set<T>> mapItemsToPosition (Set<T> its)
    {

        Map<Double, Set<T>> ret = new HashMap<> ();

        for (T t : its)
        {

            int p = t.getPosition ();

            Bounds cb = this.editor.getBoundsForPosition (p);

            double y = cb.getMinY ();

            Set<T> items = ret.get (y);

            if (items == null)
            {

                items = new LinkedHashSet<> ();
                ret.put (y, items);

            }

            items.add (t);

        }

        return ret;

    }

    private <T extends ChapterItem> Set<T> getItemsForTextRange (IndexRange ir,
                                                                 Set<T>     items)
    {

        Set<T> its = new TreeSet<> (new ChapterItemSorter ());

        int s = ir.getStart ();
        int e = ir.getEnd ();

        its.addAll (items.stream ()
                .filter (sc -> sc.getPosition () >= s && sc.getPosition () <= e)
                .collect (Collectors.toSet ()));

        return its;

    }

    private Set<Note> getNotesForParagraph ()
    {

        return this.<Note>getItemsForTextRange (this.editor.getParagraphTextRange (this.paraNo),
                                                this.chapter.getNotes ());

    }

    private void layoutNewItems (IndexRange po)
    {

        Bounds thisb = this.localToScreen (this.getBoundsInLocal ());

        List<ChapterItem> newItems = this.getNewItems.apply (po);

        for (ChapterItem ci : newItems)
        {

            int pos = ci.getPosition ();
            Bounds cb = this.editor.getBoundsForPosition (pos);

            if (cb == null)
            {

                continue;

            }

            ImageView iv = new ImageView ();
            Pane riv = new Pane ();
            riv.getChildren ().add (iv);

            riv.getStyleClass ().add (StyleClassNames.NEW);
            riv.getStyleClass ().add ((ci instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM);

            this.getChildren ().add (riv);
            riv.applyCss ();
            riv.requestLayout ();

            this.itemNodeRegister.accept (ci,
                                          riv);

            double indent = 0;

            if (this.structureItemIndent == null)
            {

                // A default indent.
                indent = this.getLayoutBounds ().getWidth () - riv.prefWidth (-1) - 4;

            } else {

                indent = this.structureItemIndent.getValue ();

            }

            riv.relocate (indent,
                          cb.getMinY () - thisb.getMinY () + (cb.getHeight () / 2) - (riv.prefHeight (-1) / 2));

        }

    }

    private void layoutStructureItems (IndexRange po)
    {

        this.structureNodeMap = new HashMap<> ();

        Bounds thisb = this.localToScreen (this.getBoundsInLocal ());

        Set<ChapterItem> its = this.getStructureItemsForTextRange (po);

        Map<Double, Set<ChapterItem>> mitems = this.mapItemsToPosition (its);

        for (Double y : mitems.keySet ())
        {

            Set<ChapterItem> items = mitems.get (y);

            ImageView iv = new ImageView ();
            //Label iv = new Label ("POS: " + y);
            Pane riv = new Pane ();
            riv.getChildren ().add (iv);

            ChapterItem first = items.iterator ().next ();

            if (items.size () == 1)
            {

                riv.getStyleClass ().add ((first instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM);

            } else {

                riv.getStyleClass ().add (items.stream ().anyMatch (i -> i instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM);

            }

            this.getChildren ().add (riv);

            riv.applyCss ();
            //riv.requestLayout ();

            riv.setOnMouseClicked (ev ->
            {

                this.showItem.accept (first);

            });

            items.stream ()
                .forEach (i ->
                {

                    this.itemNodeRegister.accept (i,
                                                  riv);

                    this.structureNodeMap.put (i,
                                               riv);

                });

            double indent = 0;

            if (this.structureItemIndent == null)
            {

                // A default indent.
                indent = this.getLayoutBounds ().getWidth () - riv.prefWidth (-1) - 4;

            } else {

                //indent = this.structureIndent.doubleValue ();
                indent = this.structureItemIndent.getValue ();
                //indent = 20;

            }

            Bounds cb = this.editor.getBoundsForPosition (first.getPosition ());

            iv.relocate (indent,
                         cb.getMinY () - thisb.getMinY () + (cb.getHeight () / 2) - (riv.prefHeight (-1) / 2));

        }

        if (true)
        {
            return;
        }

        for (ChapterItem ci : its)
        {

            Set<ChapterItem> items = this.getItemsForPosition (its,
                                                               ci.getPosition ());

            if (items.size () == 0)
            {

                continue;

            }

            ChapterItem _ci = items.iterator ().next ();
            int pos = _ci.getPosition ();
            Bounds cb = this.editor.getBoundsForPosition (pos);

            if (cb == null)
            {

                continue;

            }

            //ImageView iv = new ImageView ();
            Label iv = new Label ("POS: " + pos);
            Pane riv = new Pane ();
            riv.getChildren ().add (iv);

            if (items.size () == 1)
            {

                riv.getStyleClass ().add ((_ci instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM);

            } else {

                riv.getStyleClass ().add (items.stream ().anyMatch (i -> i instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM);

            }

            this.getChildren ().add (riv);

            //riv.applyCss ();
            //riv.requestLayout ();

            riv.setOnMouseClicked (ev ->
            {

                this.showItem.accept (_ci);

            });

            items.stream ()
                .forEach (i ->
                {

                    this.itemNodeRegister.accept (i,
                                                  riv);

                    this.structureNodeMap.put (i,
                                               riv);

                });

            double indent = 0;

            if (this.structureItemIndent == null)
            {

                // A default indent.
                indent = this.getLayoutBounds ().getWidth () - riv.prefWidth (-1) - 4;

            } else {

                //indent = this.structureIndent.doubleValue ();
                indent = this.structureItemIndent.getValue ();
                //indent = 20;

            }

            iv.relocate (indent,
                         cb.getMinY () - thisb.getMinY () + (cb.getHeight () / 2) - (riv.prefHeight (-1) / 2));

        }

        super.layoutChildren ();

    }

    private void layoutNotes (IndexRange po)
    {

        this.noteNodePosMap = new HashMap<> ();

        Bounds thisb = this.localToScreen (this.getBoundsInLocal ());

        Set<Note> its = this.<Note>getItemsForTextRange (po,
                                                         this.chapter.getNotes ());

        for (Note n : its)
        {

            Set<Note> items = this.<Note>getItemsForPosition (its,
                                                              n.getPosition ());

            if (items.size () == 0)
            {

                continue;
                //throw new IllegalStateException ("No structure items for position: " + ci.getPosition ());

            }

            Note _n = items.iterator ().next ();

            Bounds cb = this.editor.getBoundsForPosition (_n.getPosition ());

            if (cb == null)
            {

                continue;

            }

            ImageView iv = new ImageView ();
            Pane riv = new Pane ();
            riv.getChildren ().add (iv);

            riv.getStyleClass ().add (_n.isEditNeeded () ? StyleClassNames.EDITNEEDEDNOTE : StyleClassNames.NOTE);
            this.getChildren ().add (riv);
            riv.applyCss ();
            riv.requestLayout ();

            this.noteNodePosMap.put (cb.getMinY (),
                                     riv);

            double indent = 0;

            if (this.noteIndent == null)
            {

                // A default indent.
                indent = 4;

            } else {

                indent = this.noteIndent.getValue ();

            }

            riv.relocate (indent,
                          cb.getMinY () - thisb.getMinY () + (cb.getHeight () / 2) - (riv.prefHeight (-1) / 2));

        }

    }

    @Override
    protected void layoutChildren ()
    {

        this.getChildren ().clear ();

        IndexRange po = this.editor.getParagraphTextRange (this.paraNo);

        this.layoutStructureItems (po);

        //this.layoutNotes (po);

        //this.layoutNewItems (po);

        // TODO Draw a node to the right of the margin for the distance that has been edited.
        boolean edited = (this.chapter.isEditComplete ()
                          ||
                          (this.editor.getParagraphForOffset (this.chapter.getEditPosition ()) == this.paraNo)
                         );

        this.pseudoClassStateChanged (StyleClassNames.EDITED_PSEUDO_CLASS, edited);

    }

}
