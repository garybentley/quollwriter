package com.quollwriter.ui.fx.panels;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.css.*;
import javafx.css.converter.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.concurrent.*;

import org.reactfx.*;
import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import com.quollwriter.StringWithMarkup;
import com.quollwriter.UserProperties;
import com.quollwriter.synonyms.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.Constants;
import com.quollwriter.LanguageStrings;
import com.quollwriter.Environment;
import com.quollwriter.GeneralException;
import com.quollwriter.DictionaryProvider2;
import com.quollwriter.Utils;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.swing.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.text.TextIterator;
import com.quollwriter.text.Word;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class ChapterEditorWithMarginPanelContent<E extends AbstractProjectViewer> extends ChapterEditorPanelContent<E>
{

    private boolean isScrolling = false;
    protected Subscription itemsSubscription = null;
    protected Subscription itemsPositionSubscription = null;
    protected ObservableList<ChapterItem> newItems = FXCollections.observableList (new ArrayList<> ());
    protected Map<ChapterItem, TextEditor.Position> textPositions = new HashMap<> ();
    protected Map<ChapterItem, TextEditor.Position> endTextPositions = new HashMap<> ();

    public ChapterEditorWithMarginPanelContent (E       viewer,
                                                Chapter chapter)
                                         throws GeneralException
    {

        super (viewer,
               chapter,
               viewer.getTextProperties ());

/*
TODO
        this.addChangeListener (chapter.editPositionProperty (),
                                (pr, oldv, newv) ->
        {

            this.recreateVisibleParagraphs ();

        });

        this.addChangeListener (chapter.editCompleteProperty (),
                                               (pr, oldv, newv) ->
        {

            this.recreateVisibleParagraphs ();

        });
*/

        Pane marginbg = new Pane ();
        marginbg.getStyleClass ().add (StyleClassNames.MARGINBG);
        marginbg.prefHeightProperty ().bind (this.getBackgroundPane ().heightProperty ());
        marginbg.managedProperty ().bind (marginbg.visibleProperty ());

        this.getBackgroundPane ().getChildren ().add (marginbg);

        this.addChangeListener (UserProperties.showEditMarkerInChapterProperty (),
                                (pr, oldv, newv) ->
        {

            this.recreateVisibleParagraphs ();

        });

        this.itemsSubscription = this.object.chapterItemsEvents ().subscribe (ev ->
        {

            if (ev.getType () == CollectionEvent.Type.add)
            {

                this.createTextPosition (ev.getSource ());

            }

            if (ev.getType () == CollectionEvent.Type.remove)
            {

                ChapterItem ci = ev.getSource ();

                TextEditor.Position p = this.textPositions.remove (ci);

                if (p != null)
                {

                    ci.positionProperty ().unbindBidirectional (p.positionProperty ());
                    p.dispose ();

                }

                p = this.endTextPositions.remove (ci);

                if (p != null)
                {

                    ci.endPositionProperty ().unbindBidirectional (p.positionProperty ());
                    p.dispose ();

                }

            }

            this.updateParagraphForItem (ev.getSource ());

        });

        this.itemsPositionSubscription = this.object.chapterItemsPositionEvents ().subscribe (ev ->
        {

            int oldParaNo = this.editor.getParagraphForOffset (ev.getOld ().intValue ());
            int newParaNo = this.editor.getParagraphForOffset (ev.getNew ().intValue ());

            if (oldParaNo != newParaNo)
            {

                if (oldParaNo > -1)
                {

                    this.editor.recreateParagraphGraphic (oldParaNo);

                }

                if (newParaNo > -1)
                {

                    this.editor.recreateParagraphGraphic (newParaNo);

                }

            }

        });

        Runnable r = () ->
        {

            this.editor.setParagraphGraphicFactory (paraNo ->
            {

                if (paraNo == -1)
                {

                    return new Pane ();

                }

                ParagraphIconMargin<E> margin = new ParagraphIconMargin<> (this.viewer,
                                                       this.getEditor (),
                                                       paraNo,
                                                       this.object,
                                                       item ->
                                                       {

                                                           return this.getMarginNodeForChapterItem (item);

                                                       },
                                                       (item, node) ->
                                                       {

                                                           this.showItem (item,
                                                                          true);

                                                       },
                                                       range ->
                                                       {

                                                           return this.getNewItemsForRange (range);

                                                       },
                                                       cpos ->
                                                       {

                                                           return this.getMarginContextMenuItems (cpos);

                                                       });
                margin.getStyleClass ().add (StyleClassNames.MARGIN);
                margin.managedProperty ().bind (margin.visibleProperty ());

                return margin;

            });

        };

        if (this.viewer.getViewer ().isShowing ())
        {

            r.run ();

        } else {

            this.viewer.getViewer ().addEventHandler (javafx.stage.WindowEvent.WINDOW_SHOWN,
                                                      ev ->
            {

                r.run ();

            });

        }

       Nodes.addInputMap (this.editor,
                          InputMap.process (EventPattern.mousePressed (),
                                            ev ->
                                            {

                                               //this.hidePopups ();

                                               // TODO Handle right click on chapter items in margin.
                                               if ((ev.isPopupTrigger ())
                                                   &&
                                                   // TODO NEED BETTER WAY
                                                   (!(ev.getTarget () instanceof ParagraphIconMargin))
                                                  )
                                               {

                                                   //this.setContextMenu ();

                                                   return InputHandler.Result.PROCEED;

                                               }

                                               return InputHandler.Result.PROCEED;

                                           }));

        Nodes.addInputMap (this.editor,
                           InputMap.process (EventPattern.mouseReleased (),
                                              ev ->
                                              {

                                                  // TODO Handle right click on chapter items in margin.
                                                  if ((ev.isPopupTrigger ())
                                                      &&
                                                      // TODO NEED BETTER WAY
                                                      (!(ev.getTarget () instanceof ParagraphIconMargin))
                                                     )
                                                  {

                                                      //this.setContextMenu ();

                                                      return InputHandler.Result.PROCEED;

                                                  }

                                                  return InputHandler.Result.PROCEED;

                                              }));

    }

    public abstract Node getMarginNodeForChapterItem (ChapterItem it);

    public abstract Set<MenuItem> getMarginContextMenuItems (int cpos);

    public void createTextPosition (ChapterItem ci)
    {

        String t = this.object.getChapterText ();

        int l = 0;

        if (t != null)
        {

            l = t.length ();

        }

        if ((ci.getPosition () > l)
             ||
            (ci.getPosition () < 0)
           )
        {

            ci.setPosition ((l > 0 ? l : 0));

        }

        TextEditor.Position p = this.editor.createTextPosition (ci.getPosition ());

        TextEditor.Position _p = this.textPositions.get (ci);

        if (_p != null)
        {

            ci.positionProperty ().unbindBidirectional (_p.positionProperty ());

        }

        ci.positionProperty ().bindBidirectional (p.positionProperty ());
        this.textPositions.put (ci,
                                p);

        if (ci.getEndPosition () > -1)
        {

            if (ci.getEndPosition () > l)
            {

                ci.setEndPosition (l);

            }

            TextEditor.Position ep = this.editor.createTextPosition (ci.getEndPosition ());

            TextEditor.Position _ep = this.endTextPositions.get (ci);

            if (_ep != null)
            {

                ci.endPositionProperty ().unbindBidirectional (_ep.positionProperty ());

            }

            ci.endPositionProperty ().bindBidirectional (ep.positionProperty ());
            this.endTextPositions.put (ci,
                                       ep);

        }

    }

    public void addNewItem (ChapterItem ci)
    {

        this.newItems.add (ci);

    }

    public void removeNewItem (ChapterItem ci)
    {

        this.newItems.remove (ci);

    }

    private List<ChapterItem> getNewItemsForRange (IndexRange range)
    {

        List<ChapterItem> items = new ArrayList<> ();

        for (ChapterItem i : this.newItems)
        {

            if ((i.getPosition () >= range.getStart ())
                &&
                (i.getPosition () <= range.getEnd ())
               )
            {

                items.add (i);

            }

        }

        return items;

    }

    public void recreateVisibleParagraphs ()
    {

        int s = this.editor.getVisibleParagraphs ().size ();

        IntStream.range (0,
                         s)
            .forEach (i ->
            {

                if (i < s)
                {

                    this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                }

            });

    }

    private void updateParagraphForPosition (int pos)
    {

        if (pos < 0)
        {

            return;

        }

        int paraNo = this.editor.getParagraphForOffset (pos);

        this.editor.recreateParagraphGraphic (paraNo);

    }

    private void updateParagraphForItem (ChapterItem i)
    {

        if (i != null)
        {

            this.updateParagraphForPosition (i.getPosition ());

        }

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        this.getPanel ().addEventHandler (Panel.PanelEvent.CLOSE_EVENT,
                                          ev ->
        {

            this.itemsSubscription.unsubscribe ();
            this.itemsPositionSubscription.unsubscribe ();
            this.textPositions.keySet ().stream ()
                .forEach (ci ->
                {

                    TextEditor.Position p = this.textPositions.get (ci);

                    if (p != null)
                    {

                        ci.positionProperty ().unbindBidirectional (p.positionProperty ());

                    }

                    p.dispose ();

                });

            this.endTextPositions.keySet ().stream ()
                .forEach (ci ->
                {

                    TextEditor.Position p = this.endTextPositions.get (ci);

                    if (p != null)
                    {

                        ci.endPositionProperty ().unbindBidirectional (p.positionProperty ());
                        p.dispose ();

                    }

                });
            this.textPositions.clear ();
            this.endTextPositions.clear ();

            this.editor.dispose ();

        });

    }

    @Override
    public void saveObject ()
    {

        try
        {

            this.object.setText (this.editor.getTextWithMarkup ());

            super.saveObject ();

        } catch (Exception e) {

            Environment.logError ("Unable to save chapter: " + this.object,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (project,editorpanel,actions,save,actionerror));

        }

    }
/*
    public Set<ChapterItem> getStructureItemsForPosition (int p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        int paraNo = this.editor.getParagraphForOffset (p);

        double y = cb.getMinY ();

        List<ChapterItem> sitems = new ArrayList<> ();

        this.object.getScenes ().stream ()
            .forEach (s ->
            {

                sitems.add (s);
                sitems.addAll (s.getOutlineItems ());

            });

        sitems.addAll (this.object.getOutlineItems ());

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        items.addAll (sitems.stream ()
            // Only interested in those that have the same y value.  i.e. on the same line.
            .filter (i ->
            {

                // See if we are in the same paragraph.
                if (this.editor.getParagraphForOffset (i.getPosition ()) != paraNo)
                {

                    return false;

                }

                Bounds b = this.editor.getBoundsForPosition (i.getPosition ());

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toList ()));

        return items;

    }
*/
/*
    public Set<ChapterItem> getNotesForPosition (int p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        int paraNo = this.editor.getParagraphForOffset (p);

        double y = cb.getMinY ();

        Set<ChapterItem> ret = new TreeSet<> (new ChapterItemSorter ());

        ret.addAll (this.object.getNotes ().stream ()
            // Only interested in those that have the same y value.  i.e. on the same line.
            .filter (i ->
            {

                // See if we are in the same paragraph.
                if (this.editor.getParagraphForOffset (i.getPosition ()) != paraNo)
                {

                    return false;

                }

                Bounds b = this.editor.getBoundsForPosition (i.getPosition ());

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toSet ()));

        return ret;

    }
*/
    public void showPopupForItem (ChapterItem item,
                                  QuollPopup  popup)
    {

        Bounds b = this.editor.getBoundsForPosition (item.getPosition ());

        if (b == null)
        {

            int paraNo = this.editor.getParagraphForOffset (item.getPosition ());
            this.editor.showParagraphAtTop (paraNo);
System.out.println ("HEREX");
            this.showPopupForItem (item,
                                   popup);

            UIUtils.forceRunLater (() ->
            {

                //this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

            });

            return;

        }

        b = this.editor.getBoundsForPosition (item.getPosition ());
        Bounds eb = this.editor.localToScreen (this.editor.getBoundsInLocal ());

        if (!eb.contains (b))
        {

            double diff = b.getMinY () - eb.getMinY () - (eb.getHeight () / 2);

            this.editor.scrollYBy (diff);

        }

        this.hidePopups ();

        this.viewer.showPopup (popup,
                               this.getNodeForChapterItem (item),
                               Side.RIGHT);
        this.popupsToCloseOnClick.add (popup);

        popup.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                               ev ->
        {

            this.popupsToCloseOnClick.remove (popup);

        });

    }

    private ParagraphIconMargin<E> getParagraphIconMargin (int pos)
    {

        return (ParagraphIconMargin<E>) this.editor.getParagraphGraphic (this.editor.getParagraphForOffset (pos));

    }

    private ParagraphIconMargin<E> getParagraphIconMargin (ChapterItem item)
    {

        return this.getParagraphIconMargin (item.getPosition ());

    }

    public abstract <X extends ChapterItem> void showItem (X item,
                                   boolean     showAllForLine);

/*
TODO
    public int getTextPositionForMousePosition (Point p)
    {

       Point pp = p;

       if (this.iconColumn.getMousePosition () != null)
       {

          pp = new Point (0,
                          p.y);

       }

       return this.editor.viewToModel2D (pp);

    }
*/

    public Node getNodeForChapterItem (ChapterItem ci)
    {

        ParagraphIconMargin<E> m = (ParagraphIconMargin<E>) this.editor.getParagraphGraphic (this.editor.getParagraphForOffset (ci.getPosition ()));

        return m.getNodeForChapterItem (ci);

    }

}
