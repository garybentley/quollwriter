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
    private Map<String, ContextMenu> contextMenus = new HashMap<> ();
    private List<QuollPopup> popupsToCloseOnClick = new ArrayList<> ();
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
                //ev.getSource ().setTextPosition2 (this.editor.createTextPosition (ev.getSource ().getPosition ()));

            }

            if (ev.getType () == CollectionEvent.Type.remove)
            {

                ChapterItem ci = ev.getSource ();
                ci.positionProperty ().unbind ();
                ci.endPositionProperty ().unbind ();
                this.textPositions.remove (ci).dispose ();

                TextEditor.Position p = this.endTextPositions.remove (ci);

                if (p != null)
                {

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
                           InputMap.process (EventPattern.keyPressed (),
                                             ev ->
                                             {

                                                 this.hidePopups ();

                                                 return InputHandler.Result.PROCEED;

                                             }));

       Nodes.addInputMap (this.editor,
                          InputMap.process (EventPattern.mousePressed (),
                                            ev ->
                                            {

                                               this.hidePopups ();

                                               // TODO Handle right click on chapter items in margin.
                                               if ((ev.isPopupTrigger ())
                                                   &&
                                                   // TODO NEED BETTER WAY
                                                   (!(ev.getTarget () instanceof ParagraphIconMargin))
                                                  )
                                               {

                                                   this.setContextMenu ();

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

                                                      this.setContextMenu ();

                                                  }

                                                  return InputHandler.Result.PROCEED;

                                              }));

    }

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
        ci.positionProperty ().unbind ();
        ci.positionProperty ().bind (p.positionProperty ());
        this.textPositions.put (ci,
                                p);

        if (ci.getEndPosition () > -1)
        {

            if (ci.getEndPosition () > l)
            {

                ci.setEndPosition (l);

            }

            TextEditor.Position ep = this.editor.createTextPosition (ci.getEndPosition ());
            ci.endPositionProperty ().unbind ();
            ci.endPositionProperty ().bind (ep.positionProperty ());
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

    private void setContextMenu ()
    {

        //Set<MenuItem> items = this.editor.getSpellingSynonymItemsForContextMenu (this.viewer);//new LinkedHashSet<> ();
/*
        Point2D p = this.editor.getMousePosition ();

        // TODO? this.lastMousePosition = p;

        if (p != null)
        {

            TextIterator iter = new TextIterator (this.editor.getText ());

            final Word w = iter.getWordAt (this.editor.getTextPositionForMousePosition (p.getX (),
                                                                                        p.getY ()));

            if (w != null)
            {

                final String word = w.getText ();

                final int loc = w.getAllTextStartOffset ();

                List<String> l = this.editor.getSpellCheckSuggestions (w);

                if (l != null)
                {

                    List<String> prefix = Arrays.asList (dictionary,spellcheck,popupmenu,LanguageStrings.items);

                    if (l.size () == 0)
                    {

                        MenuItem mi = QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (prefix,nosuggestions)))
                            .styleClassName (StyleClassNames.NOSUGGESTIONS)
                            .onAction (ev ->
                            {

                                this.editor.addWordToDictionary (word);

                                this.viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
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

                            int cp = this.editor.getCaretPosition ();

                            this.editor.replaceText (loc,
                                                     loc + word.length (),
                                                     repWord);

                            this.editor.moveTo (cp - 1);

                            this.viewer.fireProjectEvent (ProjectEvent.Type.spellcheck,
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
                        .styleClassName (StyleClassNames.ADDWORD)
                        .onAction (ev ->
                        {

                            this.editor.addWordToDictionary (word);

                            this.viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
                                                          ProjectEvent.Action.addword,
                                                          word);

                        })
                        .build ());

                    items.add (new SeparatorMenuItem ());

                } else
                {

                    if (this.viewer.synonymLookupsSupported ())
                    {

                        // TODO Check this...
                        if (this.viewer.isLanguageFunctionAvailable ())
                        {

                            if ((word != null) &&
                                (word.length () > 0))
                            {

                                //String mt = "No synonyms found for: " + word;

                                try
                                {

                                    // See if there are any synonyms.
                                    if (this.editor.getSynonymProvider ().hasSynonym (word))
                                    {

                                        items.add (QuollMenuItem.builder ()
                                            .styleClassName (StyleClassNames.FIND)
                                            .label (getUILanguageStringProperty (Arrays.asList (synonyms,popupmenu,LanguageStrings.items,find),
                                                                                 word))
                                            .onAction (ev ->
                                            {

                                                QuollPopup qp = this.viewer.getPopupById (ShowSynonymsPopup.getPopupIdForChapter (this.object));

                                                if (qp != null)
                                                {

                                                    qp.toFront ();
                                                    return;

                                                }

                                                qp = new ShowSynonymsPopup (this.viewer,
                                                                            w,
                                                                            this.object).getPopup ();

                                                Bounds b = this.viewer.screenToLocal (this.editor.getBoundsForPosition (loc));

                                                this.viewer.showPopup (qp,
                                                                       b,
                                                                       Side.TOP);
                                                                       //b.getMinX (),
                                                                       //b.getMinY () - b.getHeight ());

                                            })
                                            .build ());

                                    } else {

                                        MenuItem mi = QuollMenuItem.builder ()
                                            .styleClassName (StyleClassNames.NOSYNONYMS)
                                            .label (getUILanguageStringProperty (Arrays.asList (synonyms,popupmenu,LanguageStrings.items,nosynonyms),
                                                                                 word))
                                            .build ();
                                        mi.setDisable (true);
                                        items.add (mi);

                                    }

                                    items.add (new SeparatorMenuItem ());

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
*/
        if (this.editor.getProperties ().get ("context-menu") != null)
        {

            ((ContextMenu) this.editor.getProperties ().get ("context-menu")).hide ();

        }

        ContextMenu cm = new ContextMenu ();

        boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

        cm.getItems ().addAll (this.getContextMenuItems (compress));

        this.editor.setContextMenu (cm);

        this.editor.getProperties ().put ("context-menu", cm);
        cm.setAutoFix (true);
        cm.setAutoHide (true);
        cm.setHideOnEscape (true);

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

    public void bindTextPropertiesTo (TextProperties p)
    {

        this.editor.bindTo (p);

    }

    private void setUseTypewriterScrolling (boolean v)
    {
/*
TODO

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setUseTypewriterScrolling (v);

            });

            return;

        }

        if (!v)
        {

            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            // Reset the margin.
            this.editor.setMargin (this.origEditorMargin);
            this.showIconColumn (true);

        } else {

            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            this.showIconColumn (false);

        }

        this.scrollCaretIntoView (null);

        this.scrollPane.getViewport ().setViewSize (this.editor.getPreferredSize ());

        this.editor.requestFocus ();
*/
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

                    ci.positionProperty ().unbind ();
                    this.textPositions.get (ci).dispose ();
                });
            this.endTextPositions.keySet ().stream ()
                .forEach (ci ->
                {

                    ci.endPositionProperty ().unbind ();
                    TextEditor.Position p = this.endTextPositions.get (ci);

                    if (p != null)
                    {

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

    public abstract Set<MenuItem> getContextMenuItems (boolean    compress);
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

            UIUtils.forceRunLater (() ->
            {

                this.showPopupForItem (item,
                                       popup);

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

        UIUtils.forceRunLater (() ->
        {

            this.viewer.showPopup (popup,
                                   this.getNodeForChapterItem (item),
                                   Side.BOTTOM);

        });

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
    private void hideAllContextMenus ()
    {

        UIUtils.runLater (() ->
        {

            this.contextMenus.values ().stream ()
                .forEach (cm -> cm.hide ());

        });

    }

    public Node getNodeForChapterItem (ChapterItem ci)
    {

        ParagraphIconMargin<E> m = (ParagraphIconMargin<E>) this.editor.getParagraphGraphic (this.editor.getParagraphForOffset (ci.getPosition ()));

        return m.getNodeForChapterItem (ci);

    }

    public void hidePopups ()
    {

        Set<QuollPopup> popups = new HashSet<> (this.popupsToCloseOnClick);

        popups.stream ()
           .forEach (p -> p.close ());

        this.popupsToCloseOnClick.clear ();

    }

}
