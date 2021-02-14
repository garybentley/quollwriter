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
import javafx.scene.Cursor;
import javafx.scene.Parent;

import org.fxmisc.flowless.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ParagraphIconMargin<E extends AbstractProjectViewer> extends Pane
{

    private static final DataFormat DRAG_FORMAT = new DataFormat("application/x-java-serialized-object");

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
    private E viewer = null;
    private Set<Node> strucNodes = null;
    private Set<Node> noteNodes = null;
    private BiConsumer<ChapterItem, Node> showItem = null;
    private Function<IndexRange, List<ChapterItem>> getNewItems = null;
    private Function<Integer, Set<MenuItem>> contextMenuItemSupplier = null;
    private Pane editMarker = null;
    private ChapterItemSorter sorter = new ChapterItemSorter ();

    private ChangeListener<javafx.scene.Scene> sceneList = null;
    private IPropertyBinder binder = new PropertyBinder ();
    private Function<ChapterItem, Node> nodeProvider = null;

    public ParagraphIconMargin (E                                       viewer,
                                TextEditor                              editor,
                                int                                     paraNo,
                                Chapter                                 chapter,
                                Function<ChapterItem, Node>             nodeProvider,
                                BiConsumer<ChapterItem, Node>           showItem,
                                Function<IndexRange, List<ChapterItem>> getNewItems,
                                Function<Integer, Set<MenuItem>>        contextMenuItemSupplier)
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

        this.contextMenuItemSupplier = contextMenuItemSupplier;
        this.paraNo = paraNo;
        this.editor = editor;
        this.chapter = chapter;
        this.viewer = viewer;
        this.noteIndent = new SimpleStyleableDoubleProperty (NOTE_INDENT, 0d);
        this.structureItemIndent = new SimpleStyleableDoubleProperty (STRUCTUREITEM_INDENT, 10d);
        this.showItem = showItem;
        this.getNewItems = getNewItems;
        this.nodeProvider = nodeProvider;

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

        this.addEventHandler (MouseEvent.MOUSE_DRAGGED,
                              ev ->
        {

            ev.consume ();

        });

        this.addEventHandler (MouseEvent.MOUSE_RELEASED,
                              ev ->
        {

            ev.consume ();

            this.editor.hideContextMenu ();

            if (ev.isPopupTrigger ())
            {

                this.clearContextMenu ();

                this.setContextMenu (ev);

            }

        });

        this.setOnContextMenuRequested (ev ->
        {

            ev.consume ();

        });

        this.addEventHandler (MouseEvent.MOUSE_CLICKED,
                              ev ->
        {

            ev.consume ();

            this.editor.hideContextMenu ();

            if (ev.getClickCount () == 2)
            {

                this.setContextMenu (ev);

            }

        });

        this.addEventHandler (MouseEvent.MOUSE_PRESSED,
                              ev ->
        {

            ev.consume ();

            this.editor.hideContextMenu ();

            if (ev.isPopupTrigger ())
            {

                this.clearContextMenu ();
                this.setContextMenu (ev);

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

        this.setOnDragOver (ev ->
        {

            if (ev.getDragboard ().getContent (DRAG_FORMAT) != null)
            {

                ev.acceptTransferModes (TransferMode.ANY);

                Point2D p = this.editor.sceneToLocal (ev.getSceneX (),
                                                      ev.getSceneY ());

                Bounds b = this.editor.getLayoutBounds ();

                if (p.getY () >= b.getMaxY () - (b.getHeight () * 0.15d))
                {

                    this.scrollYBy (this.editor,
                                    12);
                }

                if (p.getY () <= b.getMinY () + (b.getHeight () * 0.15d))
                {

                    this.scrollYBy (this.editor,
                                    -12);

                }

            }

        });

        this.setOnDragDropped (ev ->
        {

            this.updateChapterItemPosition (ev);

        });

        this.setOnDragEntered (ev ->
        {

            if (ev.getDragboard ().getContent (DRAG_FORMAT) != null)
            {

                ev.acceptTransferModes (TransferMode.ANY);

            }

            ev.consume ();

        });

    }

    private void setContextMenu (MouseEvent ev)
    {

        ContextMenu cm = new ContextMenu ();

        int pos = this.editor.getTextPositionForMousePosition (0,
                                                               ev.getY ());
        pos = this.editor.getTextPositionForCurrentMousePosition ();

        int cpos = (pos > -1 ? pos : this.editor.getCaretPosition ());

        Set<MenuItem> items = new LinkedHashSet<> ();

        if (this.contextMenuItemSupplier != null)
        {

            items.addAll (this.contextMenuItemSupplier.apply (cpos));

        }

        //cm.getItems ().addAll (items);

        this.getProperties ().put ("context-menu", cm);
        cm.setAutoFix (true);
        cm.setAutoHide (true);
        cm.show (this.viewer.getViewer (), ev.getScreenX (), ev.getScreenY ());

    }

    private void clearContextMenu ()
    {

        if (this.getProperties ().get ("context-menu") != null)
        {

            ((ContextMenu) this.getProperties ().get ("context-menu")).hide ();

        }

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

            Node riv = this.nodeProvider.apply (n);
            this.getChildren ().add (riv);
/*
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
*/
            riv.setUserData (n);
            this.makeDraggable (riv,
                                n);

            this.noteNodes.add (riv);

        }

    }

    private void addStructureItems ()
    {

        IndexRange po = this.editor.getParagraphTextRange (this.paraNo);

        Set<ChapterItem> its = this.getStructureItemsForTextRange (po);

        for (ChapterItem ci : its)
        {

            Node riv = this.nodeProvider.apply (ci);
            this.getChildren ().add (riv);
/*
            riv.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }
xxx
                this.showItem.accept (ci,
                                      riv);

            });
*/
            riv.setUserData (ci);
            this.makeDraggable (riv,
                                ci);

            this.strucNodes.add (riv);

        }

    }

    private void scrollYBy (Node riv,
                            double amount)
    {

        VirtualizedScrollPane vsp = null;

        if (riv instanceof VirtualizedScrollPane)
        {

            vsp = (VirtualizedScrollPane) riv;

        } else {

            Parent p = riv.getParent ();

            while (p != null)
            {

                if (p instanceof VirtualizedScrollPane)
                {

                    vsp = (VirtualizedScrollPane) p;
                    break;

                }

            }

        }

        if (vsp != null)
        {

            vsp.scrollYBy (amount);

        }

    }

    private void makeDraggable (Node        riv,
                                ChapterItem ci)
    {

        riv.setOnDragDetected (ev ->
        {

            Dragboard db = riv.startDragAndDrop (TransferMode.ANY);
            db.setDragView (riv.snapshot (null, null));
            ClipboardContent c = new ClipboardContent ();
            c.put (DRAG_FORMAT, ci.getObjectReference ().asString ());
            db.setContent (c);
            riv.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
            ev.consume ();

        });

        riv.setOnDragDone (ev ->
        {

            riv.setCursor (Cursor.DEFAULT);
            riv.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

        });

        riv.setOnDragDropped (ev ->
        {

            this.updateChapterItemPosition (ev);

        });

    }

    private void updateChapterItemPosition (DragEvent   ev)
    {

        if (ev.getDragboard ().getContent (DRAG_FORMAT) != null)
        {

            String val = ev.getDragboard ().getContent (DRAG_FORMAT).toString ();
//xxx handle moving item or scene...
            try
            {

                Point2D p = this.editor.sceneToLocal (ev.getSceneX (),
                                                      ev.getSceneY ());

                int np = this.editor.getTextPositionForMousePosition (0,
                                                                      p.getY ());

                ChapterItem ci = (ChapterItem) this.viewer.getProject ().getObjectForReference (ObjectReference.parseObjectReference (val));

                int oldpos = ci.getPosition ();

                Chapter c = ci.getChapter ();

                if (ci instanceof Note)
                {

                    ci.setPosition (np);

                }

                if (ci instanceof OutlineItem)
                {

                    OutlineItem oi = (OutlineItem) ci;

                    Scene s = c.getLastScene (np);

                    if (s == null)
                    {

                        c.addOutlineItem (oi);

                    } else {

                        s.addOutlineItem (oi);

                    }

                    ci.setPosition (np);

                }

                if (ci instanceof Scene)
                {

                    //Scene s = (Scene) ci;
                    ci.setPosition (np);

                    List<Scene> scs = new ArrayList<> (c.getScenes ());

                    Collections.sort (scs,
                                      new ChapterItemSorter ());
                    Collections.reverse (scs);

                    List<OutlineItem> objs = new ArrayList<> ();

                    for (Scene s : c.getScenes ())
                    {

                        objs.addAll (s.getOutlineItems ());

                    }

                    objs.addAll (c.getOutlineItems ());

                    objs.stream ()
                        .forEach (i ->
                        {

                            Scene scene = null;

                            for (Scene _s : scs)
                            {

                                if (i.getPosition () > _s.getPosition ())
                                {

                                    scene = _s;
                                    break;

                                }

                            }

                            if (scene != null)
                            {

                                scene.addOutlineItem (i);

                            } else {

                                c.addOutlineItem (i);

                            }

                        });

                }

                this.viewer.saveObject (ci,
                                        true);

            } catch (Exception e) {

                Environment.logError ("Unable to move item: " + val,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (iconcolumn,moveitem,actionerror));

            }

        }

        ev.setDropCompleted (true);
        ev.consume ();

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

        if (thisb == null)
        {

            return 0;

        }

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
