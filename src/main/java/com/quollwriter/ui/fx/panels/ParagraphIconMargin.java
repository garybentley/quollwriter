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
import javafx.beans.value.*;

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
    private Set<Node> strucNodes = null;
    private Set<Node> noteNodes = null;
    private BiConsumer<ChapterItem, Node> showItem = null;
    private Function<IndexRange, List<ChapterItem>> getNewItems = null;
    private Pane editMarker = null;
    private ChapterItemSorter sorter = new ChapterItemSorter ();

    private ChangeListener<javafx.scene.Scene> sceneList = null;
    private IPropertyBinder binder = new PropertyBinder ();

    public ParagraphIconMargin (ProjectViewer                           viewer,
                                ProjectChapterEditorPanelContent        editor,
                                int                                     paraNo,
                                Chapter                                 chapter,
                                BiConsumer<ChapterItem, Node>           showItem,
                                Function<IndexRange, List<ChapterItem>> getNewItems)
    {

        this.strucNodes = new TreeSet<> ((o1, o2) ->
        {

            if ((o1 == null)
                ||
                (o2 == null)
               )
            {

                return -1;

            }

            return this.sorter.compare ((ChapterItem) o1.getUserData (),
                                        (ChapterItem) o2.getUserData ());

        });

        this.noteNodes = new TreeSet<> ((o1, o2) ->
        {

            if ((o1 == null)
                ||
                (o2 == null)
               )
            {

                return -1;

            }

            return this.sorter.compare ((ChapterItem) o1.getUserData (),
                                        (ChapterItem) o2.getUserData ());

        });

        this.paraNo = paraNo;
        this.editor = editor.getEditor ();
        this.chapter = chapter;
        this.viewer = viewer;
        this.noteIndent = new SimpleStyleableDoubleProperty (NOTE_INDENT, 0d);
        this.structureItemIndent = new SimpleStyleableDoubleProperty (STRUCTUREITEM_INDENT, 10d);
        this.showItem = showItem;
        this.getNewItems = getNewItems;

        this.editMarker = new Pane ();
        this.editMarker.getStyleClass ().add (StyleClassNames.EDITMARKER);
        this.editMarker.setVisible (false);

        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((oldv != null)
                &&
                (newv == null)
               )
            {

                this.binder.dispose ();

            }

        });

        this.editMarker.setBackground (new Background (new BackgroundFill (UserProperties.getEditMarkerColor (),
                                                                           null,
                                                                           null)));
        this.binder.addChangeListener (UserProperties.editMarkerColorProperty (),
                                       (pr, oldv, newv) ->
        {

            this.editMarker.setBackground (new Background (new BackgroundFill (UserProperties.getEditMarkerColor (),
                                                                               null,
                                                                               null)));

        });

        this.getChildren ().add (this.editMarker);

        this.addStructureItems ();

        this.addNoteItems ();

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

                int pos = this.editor.getTextPositionForMousePosition (0,
                                                                       ev.getY ());
                pos = this.editor.getTextPositionForCurrentMousePosition ();

                int cpos = (pos > -1 ? pos : this.editor.getCaretPosition ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.Scene.OBJECT_TYPE)))
                    .styleClassName (StyleClassNames.SCENE)
                    .accelerator (new KeyCharacterCombination ("S",
                                                               KeyCombination.SHORTCUT_DOWN,
                                                               KeyCombination.SHIFT_DOWN))
                    .onAction (eev ->
                    {

                        this.viewer.createNewScene (this.chapter,
                                                    cpos);

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
                                                          cpos);

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

                        this.viewer.createNewNote (this.chapter,
                                                   cpos);

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

                        this.viewer.createNewEditNeededNote (this.chapter,
                                                             cpos);

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

    public void removeItem (ChapterItem it)
    {

        Set<Node> nodes = null;

        if (it instanceof Note)
        {

            nodes = this.noteNodes;

        } else {

            nodes = this.strucNodes;

        }

        Node n = nodes.stream ()
            .filter (i -> ((ChapterItem) i.getUserData ()).equals (it))
            .findFirst ()
            .orElse (null);

        nodes.remove (n);

        this.getChildren ().remove (n);
        return;

    }

    private void addNoteItems ()
    {

        IndexRange po = this.editor.getParagraphTextRange (this.paraNo);

        Set<Note> its = this.getNotesForTextRange (po);

        for (Note n : its)
        {

            HBox riv = new HBox ();
            riv.getStyleClass ().add (StyleClassNames.ICONBOX);
            Pane p = new Pane ();
            p.getStyleClass ().add ((n.isEditNeeded () ? StyleClassNames.EDITNEEDEDNOTE : StyleClassNames.NOTE) + StyleClassNames.ICON_SUFFIX);
            p.getStyleClass ().add (StyleClassNames.ICON);
            riv.getChildren ().add (p);
            riv.managedProperty ().bind (riv.visibleProperty ());
/*
            ImageView iv = new ImageView ();
            Pane riv = new Pane ();
            riv.getChildren ().add (iv);
*/
            this.getChildren ().add (riv);

            riv.setOnMouseDragged (ev ->
            {

                riv.requestFocus ();

            });
            riv.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                this.showItem.accept (n,
                                      riv);

                ev.consume ();

            });

            riv.setUserData (n);

            this.noteNodes.add (riv);

        }

    }

    private void addStructureItems ()
    {

        IndexRange po = this.editor.getParagraphTextRange (this.paraNo);

        Set<ChapterItem> its = this.getStructureItemsForTextRange (po);

        for (ChapterItem ci : its)
        {

            HBox riv = new HBox ();
            riv.getStyleClass ().add (StyleClassNames.ICONBOX);
            Pane p = new Pane ();
            p.getStyleClass ().add (((ci instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM) + StyleClassNames.ICON_SUFFIX);
            p.getStyleClass ().add (StyleClassNames.ICON);
            riv.getChildren ().add (p);
            riv.managedProperty ().bind (riv.visibleProperty ());
/*
            ImageView iv = new ImageView ();
            Pane riv = new Pane ();
            riv.getChildren ().add (iv);
            riv.getStyleClass ().add ((ci instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM);
*/
            this.getChildren ().add (riv);

            riv.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                this.showItem.accept (ci,
                                      riv);

            });

            riv.setUserData (ci);

            this.strucNodes.add (riv);

        }

    }

    public int getParagraph ()
    {

        return this.paraNo;

    }

    public Node getNodeForChapterItem (ChapterItem ci)
    {

        Set<Node> nodes = null;

        if (ci instanceof Note)
        {

            nodes = this.noteNodes;

        } else {

            nodes = this.strucNodes;

        }

        return nodes.stream ()
            .filter (n -> ((ChapterItem) n.getUserData ()).equals (ci))
            .findFirst ()
            .orElse (null);

    }

    private Set<Note> getNotesForTextRange (IndexRange ir)
    {

        Set<Note> items = new TreeSet<> (new ChapterItemSorter ());

        for (ChapterItem ci : this.getNewItems.apply (ir))
        {

            if (!(ci instanceof Note))
            {

                continue;

            }

            items.add ((Note) ci);

        }

        items.addAll (this.<Note>getItemsForTextRange (ir,
                                                 this.chapter.getNotes ()));

        return items;

    }

    private Set<ChapterItem> getStructureItemsForTextRange (IndexRange ir)
    {

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        for (ChapterItem ci : this.getNewItems.apply (ir))
        {

            if (ci instanceof Note)
            {

                continue;

            }

            items.add (ci);

        }

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

                Bounds b = this.editor.getBoundsForPosition (i.getPosition ());

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toSet ());

    }

    private Map<Double, Set<Node>> mapNodesToPosition (Set<Node> its)
    {

        Map<Double, Set<Node>> ret = new HashMap<> ();

        for (Node t : its)
        {

            ChapterItem ci = (ChapterItem) t.getUserData ();

            if (ci.getKey () < 0)
            {

                continue;

            }

            int p = ci.getPosition ();

            Bounds cb = this.editor.getBoundsForPosition (p);

            if (cb == null)
            {

                continue;

            }

            double y = cb.getMinY ();

            Set<Node> items = ret.get (y);

            if (items == null)
            {

                items = new LinkedHashSet<> ();
                ret.put (y, items);

            }

            items.add (t);

        }

        return ret;

    }


    private <T extends ChapterItem> Map<Double, Set<T>> mapItemsToPosition (Set<T> its)
    {

        Map<Double, Set<T>> ret = new HashMap<> ();

        for (T t : its)
        {

            int p = t.getPosition ();

            Bounds cb = this.editor.getBoundsForPosition (p);

            if (cb == null)
            {

                continue;

            }

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

    private double layoutNodeForItem (ChapterItem ci,
                                      Node        n,
                                      Bounds      thisb,
                                      double      indent)
    {

        int p = ci.getPosition ();

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return 0;

        }

        // Need to redetermine the bounds here otherwise the height will be wrong, for reasons...
        thisb = this.localToScreen (this.getBoundsInLocal ());
        double y = cb.getMinY ();
        double h = n.prefHeight (-1);
        double ny = y - thisb.getMinY () + (cb.getHeight () / 2) - (h / 2);
        //n.resize (n.prefWidth (-1), h);
        n.relocate (indent,
                    ny);

        return 0;

    }

    private void layoutStructureItems (IndexRange paraRange,
                                       Bounds     thisb)
    {

        List<ChapterItem> newis = this.getNewItems.apply (paraRange);

        ChapterItem newi = null;

        if (newis.size () > 0)
        {

            newi = newis.get (0);

        }

        double newy = -1;
        double indent = 0;

        if ((newi != null)
            &&
            (!(newi instanceof Note))
           )
        {

            Node n = this.getNodeForChapterItem (newi);
            n.setVisible (true);

            if (this.structureItemIndent == null)
            {

                // A default indent.
                indent = this.getLayoutBounds ().getWidth () - n.prefWidth (-1) - 4;

            } else {

                indent = this.structureItemIndent.getValue ();

            }

            newy = this.layoutNodeForItem (newi,
                                           n,
                                           thisb,
                                           indent);

        }

        double defIndent = this.structureItemIndent.getValue ();
        double thisw = this.getLayoutBounds ().getWidth ();

        Map<Double, Set<Node>> mapped = this.mapNodesToPosition (this.strucNodes);

        for (Double y : mapped.keySet ())
        {

            Set<Node> nodes = mapped.get (y);

            if (newy == y)
            {

                double _indent = indent;

                nodes.stream ()
                    .forEach (n ->
                    {

                        n.setVisible (false);
                        this.layoutNodeForItem ((ChapterItem) n.getUserData (),
                                                n,
                                                thisb,
                                                _indent);

                    });

                continue;

            }

            Node nfirst = null;
            Bounds cb = null;
            double h = -1;
            double ny = newy;
            //double indent = 0;

            for (Node n : nodes)
            {

                n.setVisible (false);

                if (nfirst == null)
                {

                    nfirst = n;
                    nfirst.setVisible (true);

                    if (this.structureItemIndent == null)
                    {

                        // A default indent.
                        indent = thisw - nfirst.prefWidth (-1) - 4;

                    } else {

                        indent = defIndent;

                    }

                }

                ny = this.layoutNodeForItem ((ChapterItem) n.getUserData (),
                                             n,
                                             thisb,
                                             indent);

/*
                    ChapterItem ci = (ChapterItem) n.getUserData ();

                    cb = this.editor.getBoundsForPosition (ci.getPosition ());
                    h = nfirst.prefHeight (-1);
                    ny = cb.getMinY () - thisb.getMinY () + (cb.getHeight () / 2) - (h / 2);

                    if (this.structureItemIndent == null)
                    {

                        // A default indent.
                        indent = this.getLayoutBounds ().getWidth () - nfirst.prefWidth (-1) - 4;

                    } else {

                        indent = this.structureItemIndent.getValue ();

                    }
*/
                //}
/*
                n.relocate (indent,
                            ny);
*/
            }

        }

    }

    private void layoutNotes (IndexRange paraRange,
                              Bounds     thisb)
    {

        double indent = 4;

        if (this.noteIndent != null)
        {

            indent = this.noteIndent.getValue ();

        }

        List<ChapterItem> newis = this.getNewItems.apply (paraRange);
        ChapterItem newi = null;

        if (newis.size () > 0)
        {

            newi = newis.get (0);

        }

        double newy = -1;

        if ((newi != null)
            &&
            (newi instanceof Note)
           )
        {

            Node n = this.getNodeForChapterItem (newi);
            n.setVisible (true);

            newy = this.layoutNodeForItem (newi,
                                           n,
                                           thisb,
                                           indent);

        }

        Map<Double, Set<Node>> mapped = this.mapNodesToPosition (this.noteNodes);

        for (Double y : mapped.keySet ())
        {

            Set<Node> nodes = mapped.get (y);

            if (newy == y)
            {

                double _indent = indent;

                nodes.stream ()
                    .forEach (n ->
                    {

                        n.setVisible (false);
                        this.layoutNodeForItem ((ChapterItem) n.getUserData (),
                                                n,
                                                thisb,
                                                _indent);

                    });

                continue;

            }

            Node nfirst = null;
            Bounds cb = null;
            double h = -1;
            double ny = -1;

            for (Node n : nodes)
            {

                n.setVisible (false);

                if (nfirst == null)
                {

                    nfirst = n;
                    n.setVisible (true);

                }

                ny = this.layoutNodeForItem ((ChapterItem) n.getUserData (),
                                             n,
                                             thisb,
                                             indent);

            }

        }

    }

    @Override
    protected void layoutChildren ()
    {

        super.layoutChildren ();

        if (!this.editor.isReadyForUse ())
        {

            return;

        }

        this.applyCss ();

        IndexRange po = this.editor.getParagraphTextRange (this.paraNo);
        Bounds thisb = this.localToScreen (this.getBoundsInLocal ());

        this.layoutStructureItems (po,
                                   thisb);

        this.layoutNotes (po,
                          thisb);

        this.editMarker.setVisible (false);

        boolean edited = (this.chapter.isEditComplete ()
                          ||
                          (this.editor.getParagraphForOffset (this.chapter.getEditPosition ()) == this.paraNo)
                         );

        if (UserProperties.isShowEditMarkerInChapter ()
            &&
            ((edited)
             ||
             (this.chapter.getEditPosition () >= po.getEnd ())
            )
           )
        {

            Bounds b = this.getLayoutBounds ();
            this.editMarker.setVisible (true);
            Bounds pb = null;

            if (this.chapter.getEditPosition () > -1)
            {

                pb = this.editor.getBoundsForPosition (this.chapter.getEditPosition ());

            }

            double h = b.getHeight ();

            if (pb != null)
            {

                pb = this.screenToLocal (pb);

                if (pb == null)
                {

                    return;

                }

                h = pb.getMaxY ();

            }

            double w = this.editMarker.prefWidth (-1);
            this.editMarker.setPrefHeight (h);
            this.editMarker.autosize ();
            this.editMarker.relocate (b.getMaxX () - w,
                                      0);

        }

        this.pseudoClassStateChanged (StyleClassNames.EDITED_PSEUDO_CLASS, edited);

    }

}
