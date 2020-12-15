package com.quollwriter.editors.ui.panels;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.geometry.*;

import com.gentlyweb.properties.*;

import org.reactfx.*;
import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.events.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorChapterPanel extends ChapterEditorWithMarginPanelContent<EditorProjectViewer> implements ToolBarSupported
{

   public static final String CHAPTER_INFO_ACTION_NAME = "chapter-info";
   public static final String SET_EDIT_COMPLETE_ACTION_NAME = "set-edit-complete";
   public static final String REMOVE_EDIT_POINT_ACTION_NAME = "remove-edit-point";

   public static final String NEW_COMMENT_ACTION_NAME = "newcomment";

   //private IconColumn<EditorProjectViewer>              iconColumn = null;
   private int                     lastCaret = -1;
   //private ChapterItemTransferHandler chItemTransferHandler = null;
   //private BlockPainter highlight = null;
   private boolean chapterItemEditVisible = false;
   private TextEditor.Highlight highlight = null;

   public EditorChapterPanel (EditorProjectViewer pv,
                              Chapter             c)
                       throws GeneralException
   {

        super (pv,
               c);

        final EditorChapterPanel _this = this;

        this.editor.setEditable (false);
        // TODO this.editor.setCanCopy (false);

        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "chapteredit");
        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "editorchapteredit");

        this.editor.readyForUseProperty ().addListener ((pr, oldv, newv) ->
        {

            if (!newv)
            {

                return;

            }

            UIUtils.forceRunLater (() ->
            {

                this.object.getNotes ().stream ()
                    .forEach (n -> this.createTextPosition (n));

            });

        });

        Nodes.addInputMap (this.editor,
                           InputMap.process (EventPattern.mouseClicked (),
                                             ev ->
                                             {

                                                 if (ev.getClickCount () == 2)
                                                 {

                                                     this.showAddNewComment (this.editor.getSelection ().getStart ());

                                                 }
                                                return InputHandler.Result.PROCEED;

                                            }));

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                 this.showAddNewComment (this.editor.getSelection ().getStart ());

                                             }));

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.mouseReleased (),
                                              ev ->
                                              {

                                                  if ((_this.editor.getSelection ().getEnd () > _this.editor.getSelection ().getStart ())
                                                      &&
                                                      (!_this.isChapterItemEditVisible ())
                                                      &&
                                                      (_this.isReadyForUse ())
                                                     )
                                                  {

                                                      this.showAddNewComment (this.editor.getSelection ().getStart ());

                                                  }

                                              }));

/*
        this.iconColumn = new IconColumn<EditorProjectViewer> (this,
                                                               c,
                                                               this.projectViewer.getIconProvider (),
                                                               this.projectViewer.getChapterItemViewPopupProvider ());

        this.iconColumn.addMouseListener (new MouseEventHandler ()
        {

            public void handleDoublePress (MouseEvent ev)
            {

               _this.getActionListenerForTextPosition (NEW_COMMENT_ACTION_NAME,
                                                       ev.getPoint ()).actionPerformed (new ActionEvent (_this, 1, "show"));

            }

        });
*/

        //this.chItemTransferHandler = new ChapterItemTransferHandler (this.getIconColumn ());

        //this.setTransferHandler (this.chItemTransferHandler);
/*
        this.actions.put (REMOVE_EDIT_POINT_ACTION_NAME,
                          new ActionAdapter ()
                          {

                             public void actionPerformed (ActionEvent ev)
                             {

                                 _this.removeEditPosition ();

                             }

                          });

        this.actions.put (SET_EDIT_COMPLETE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                             public void actionPerformed (ActionEvent ev)
                             {

                                 _this.setEditComplete (true);

                             }

                          });

        this.actions.put (NEW_COMMENT_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       NEW_COMMENT_ACTION_NAME,
                                                       -1);

                              }

                          });

        this.actions.put (CHAPTER_INFO_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.projectViewer.viewEditorChapterInformation (_this.obj);

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show chapter information for: " +
                                                            _this.obj,
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                getUIString (editors,project,LanguageStrings.actions,showchapterinfo,actionerror));
                                                                //Environment.replaceObjectNames ("Unable to show {chapter}."));

                                  }

                              }

                          });
*/
/*
        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Remove ctrl+shift+O from the when_focused set since it conflicts.
        this.editor.getInputMap (JComponent.WHEN_FOCUSED).put (KeyStroke.getKeyStroke ("ctrl shift O"),
                                                               "none");

        im.put (KeyStroke.getKeyStroke ("ctrl shift C"),
                NEW_COMMENT_ACTION_NAME);

        this.highlight = new BlockPainter (UIUtils.getHighlightColor ());
*/
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

    public void setChapterItemEditVisible (boolean v)
    {

         this.chapterItemEditVisible = v;

    }

    public boolean isChapterItemEditVisible ()
    {

         return this.chapterItemEditVisible;

    }
/*
    public int getTextPositionForMousePosition (Point p)
    {

       Point pp = p;

       if (this.iconColumn.getMousePosition () != null)
       {

          pp = new Point (0,
                          p.y);

       }

       return this.editor.viewToModel (pp);

    }
*/
/*
    public ActionListener getActionListenerForTextPosition (final String actionName,
                                                            final Point  p)
    {

         final EditorChapterPanel _this = this;

         final int pos = this.getTextPositionForMousePosition (p);

         return new ActionAdapter ()
         {

            public void actionPerformed (ActionEvent ev)
            {

               _this.performAction (ev,
                                    actionName,
                                    pos);

            }

         };

    }
*/
/*
    public ChapterItemTransferHandler getChapterItemTransferHandler ()
    {

        return this.chItemTransferHandler;

    }
*/
/*
   public int getIconColumnXOffset (ChapterItem i)
   {

      int xOffset = 22;

      if (i instanceof OutlineItem)
      {

          xOffset = 22;

      }

      return xOffset;

   }
*/
/*
    public JComponent getEditorWrapper (QTextEditor q)
    {

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.iconColumn);
        b.add (q);
        q.setMaximumSize (new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE));

        q.setMinimumSize (new Dimension (200, 200));
        q.setAlignmentY (Component.TOP_ALIGNMENT);
        q.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.iconColumn.setAlignmentY (Component.TOP_ALIGNMENT);
        this.iconColumn.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.iconColumn.setMinimumSize (new Dimension (32, 200));
        this.iconColumn.setPreferredSize (new Dimension (32, 200));
        this.iconColumn.setMaximumSize (new Dimension (32, Integer.MAX_VALUE));

        JPanel p = new ScrollablePanel (new BorderLayout ());
        p.add (b);

        return p;

    }
*/
    @Override
    public Set<Node> getToolBarItems ()
    {

        Set<Node> its = new LinkedHashSet<> ();

        its.add (QuollButton.builder ()
            .tooltip (editors,project,commentspanel,toolbar,newcomment,tooltip)
            .iconName (StyleClassNames.COMMENT)
            /*
            TODO
            .accelerator (new KeyCodeCombination (KeyCode.C,
                                                  KeyCombination.SHORTCUT_DOWN,
                                                  KeyCombination.SHIFT_DOWN))
                                                  */
            .onAction (ev ->
            {

                this.showAddNewComment (this.editor.getSelection ().getStart ());

            })
            .build ());

        its.add (QuollButton.builder ()
            .tooltip (editors,project,commentspanel,toolbar,textproperties,tooltip)
            .iconName (StyleClassNames.EDITPROPERTIES)
            .onAction (ev ->
            {

                this.viewer.runCommand (EditorProjectViewer.CommandId.textproperties);

            })
            .build ());

        return its;

    }
/*
    public void removeItem (ChapterItem c)
    {

        this.iconColumn.removeItem (c);

    }

    public void addItem (ChapterItem c)
                  throws GeneralException
    {

        this.iconColumn.addItem (c);

    }

    public void scrollToItem (ChapterItem i)
                       throws GeneralException
    {

        this.scrollToPosition (i.getPosition ());

    }
*/
/*
    public void editNote (Note n)
                   throws GeneralException
    {

        this.scrollToNote (n);

        new CommentActionHandler (n,
                                  this).actionPerformed (new ActionEvent (this,
                                                                          0,
                                                                          "edit"));

    }
*/
    @Override
    public void showItem (ChapterItem item,
                          boolean     showAllForLine)
    {

        Note n = (Note) item;

        Note top = n;
        Set<Note> items = null;

        if (showAllForLine)
        {

            items = this.getNotesForPosition (item.getPosition ());

            if (items.size () == 0)
            {

                return;

            }

            top = items.iterator ().next ();

        } else {

            items = new LinkedHashSet<> ();
            items.add (n);

        }

        QuollPopup qp = this.viewer.getPopupById (ViewCommentPopup.getPopupIdForComment (top));

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        qp = new ViewCommentPopup (this.viewer,
                                   items).getPopup ();

        this.showPopupForItem (top,
                               qp);

    }

   public void removeItemHighlightTextFromEditor (ChapterItem it)
   {

      this.editor.removeHighlight (this.highlight);

   }

   public void highlightItemTextInEditor (ChapterItem it)
   {

      this.editor.removeHighlight (this.highlight);

      this.highlight = this.editor.addHighlight (new IndexRange (it.getStartPosition (),
                                                                 it.getEndPosition ()),
                                                 UserProperties.getEditorCommentChapterHighlightColor ());

   }

/*
    public void scrollToNote (Note n)
                       throws GeneralException
    {

        this.scrollToPosition (n.getPosition ());

    }
*/
   @Override
   public void init (State s)
              throws GeneralException
   {

        super.init (s);

        this.editor.getCaretSelectionBind ().moveTo (0);

        this.setReadyForUse ();

   }
/*
    public void restoreBackgroundColor ()
    {

        super.restoreBackgroundColor ();

        //this.iconColumn.setBackground (IconColumn.defaultBGColor);

    }
*/
    //public void setBackgroundColor (Color c)
    //{

    //    super.setBackgroundColor (c);
/*
        if (c.equals (Color.white))
        {

            this.iconColumn.setBackground (IconColumn.defaultBGColor);

        } else
        {

            this.iconColumn.setBackground (c);

        }
*/
    //}
/*
    private void setContextMenu ()
    {

        Set<MenuItem> items = new LinkedHashSet<> ();

        if (this.editor.getProperties ().get ("context-menu") != null)
        {

            ((ContextMenu) this.editor.getProperties ().get ("context-menu")).hide ();

        }

        ContextMenu cm = new ContextMenu ();
        cm.getItems ().addAll (items);

        boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

        cm.getItems ().addAll (this.getContextMenuItems (compress));

        this.editor.setContextMenu (cm);

        this.editor.getProperties ().put ("context-menu", cm);
        cm.setAutoFix (true);
        cm.setAutoHide (true);
        cm.setHideOnEscape (true);

    }
*/
    @Override
    public Set<MenuItem> getContextMenuItems (boolean    compress)
    {

        Set<MenuItem> ret = new LinkedHashSet<> ();

        int pos = this.editor.getTextPositionForCurrentMousePosition ();

        // This is needed to move to the correct character, the call above seems to get the character
        // before what was clicked on.
        // pos++;
        List<String> prefix = Arrays.asList (editors,project,commentspanel,popupmenu,Chapter.OBJECT_TYPE);

        if (compress)
        {

            List<Node> row1 = new ArrayList<> ();

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.EDITPROPERTIES)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,items,textproperties,tooltip)))
                .onAction (ev ->
                {

                    this.viewer.runCommand (EditorProjectViewer.CommandId.textproperties);

                })
                .build ());

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.FIND)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,items,find,tooltip)))
                .onAction (ev ->
                {

                    this.viewer.runCommand (EditorProjectViewer.CommandId.find);

                })
                .build ());

            List<Node> row2 = new ArrayList<> ();

            row2.add (QuollButton.builder ()
                .iconName (StyleClassNames.SETEDITPOSITION)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,items,seteditposition,tooltip)))
                .onAction (ev ->
                {

                    this.viewer.setChapterEditPosition (this.object,
                                                        pos);

                    this.recreateVisibleParagraphs ();

                })
                .build ());

            if (this.object.getEditPosition () > 0)
            {

                row2.add (QuollButton.builder ()
                    .iconName (StyleClassNames.GOTOEDITPOSITION)
                    .tooltip (getUILanguageStringProperty (Utils.newList (prefix,items,gotoeditposition,tooltip)))
                    .onAction (ev ->
                    {

                        this.editor.moveTo (this.object.getEditPosition ());
                        this.editor.requestFollowCaret ();

                    })
                    .build ());

                row2.add (QuollButton.builder ()
                    .iconName (StyleClassNames.REMOVEEDITPOSITION)
                    .tooltip (getUILanguageStringProperty (Utils.newList (prefix,items,removeeditposition,tooltip)))
                    .onAction (ev ->
                    {

                        this.viewer.removeChapterEditPosition (this.object);

                        this.recreateVisibleParagraphs ();

                    })
                    .build ());

            }

            if (!this.object.isEditComplete ())
            {

                row2.add (QuollButton.builder ()
                    .iconName (StyleClassNames.EDITCOMPLETE)
                    .tooltip (getUILanguageStringProperty (Utils.newList (prefix,items,seteditcomplete,tooltip)))
                    .onAction (ev ->
                    {

                        this.viewer.setChapterEditComplete (this.object,
                                                            true);

                        IntStream.range (0,
                                         this.editor.getVisibleParagraphs ().size ())
                            .forEach (i ->
                            {

                                this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                            });

                    })
                    .build ());

            }

            CustomMenuItem n = UIUtils.createCompressedMenuItem (getUILanguageStringProperty (editors,project,commentspanel,popupmenu,Chapter.OBJECT_TYPE,compresstext),
                                                                 row1,
                                                                 row2);

            ret.add (n);

        } else {

            ret.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.SETEDITPOSITION)
                .label (getUILanguageStringProperty (Utils.newList (prefix,items,seteditposition,text)))
                .onAction (ev ->
                {

                    this.viewer.setChapterEditPosition (this.object,
                                                        pos);

                    IntStream.range (0,
                                     this.editor.getVisibleParagraphs ().size ())
                        .forEach (i ->
                        {

                            this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                        });

                })
                .build ());

            if (this.object.getEditPosition () > 0)
            {

                ret.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.GOTOEDITPOSITION)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,items,gotoeditposition,text)))
                    .onAction (ev ->
                    {

                        this.editor.moveTo (this.object.getEditPosition ());
                        this.editor.requestFollowCaret ();

                    })
                    .build ());

                ret.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.REMOVEEDITPOSITION)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,items,removeeditposition,text)))
                    .onAction (ev ->
                    {

                        this.viewer.removeChapterEditPosition (this.object);

                        IntStream.range (0,
                                         this.editor.getVisibleParagraphs ().size ())
                            .forEach (i ->
                            {

                                this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                            });

                    })
                    .build ());

            }

            if (!this.object.isEditComplete ())
            {

                ret.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.EDITCOMPLETE)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,items,seteditcomplete,text)))
                    .onAction (ev ->
                    {

                        this.viewer.setChapterEditComplete (this.object,
                                                            true);

                    })
                    .build ());

            } else {

                ret.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.EDITNEEDED)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,items,seteditneeded,text)))
                    .onAction (ev ->
                    {

                        this.viewer.setChapterEditComplete (this.object,
                                                            false);

                    })
                    .build ());

            }

        }

        return ret;

    }

    public void editComment (Note n)
    {

        QuollPopup qp = this.viewer.getPopupById (AddEditCommentPopup.getPopupIdForComment (n));

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        qp = new AddEditCommentPopup (this.viewer,
                                      this.object,
                                      n).getPopup ();

        this.showPopupForItem (n,
                               qp);

    }

    public void showDeleteCommentPopup (Note n,
                                        Node showAt)
    {

        QuollPopup.questionBuilder ()
            .title (comments,delete,popup,title)
            .inViewer (this.viewer)
            .styleClassName (StyleClassNames.DELETE)
            .message (getUILanguageStringProperty (Arrays.asList (comments,delete,popup,text),
                                                   n.getSummary ()))
            .confirmButtonLabel (comments,delete,popup,buttons,confirm)
            .cancelButtonLabel (comments,delete,popup,buttons,cancel)
            .onConfirm (ev ->
            {

                try
                {

                    this.viewer.deleteObject (n,
                                              false);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to delete comment: " +
                                          n,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (comments,delete,actionerror));
                                                                 //"Unable to delete.");

                }


            })
            .showAt (showAt, Side.BOTTOM)
            .build ();

    }

    public void showAddNewComment (int pos)
    {

        Note item = new Note ();
        item.setPosition (pos);
        item.setType (Note.EDIT_NEEDED_NOTE_TYPE);

        // Here we generate a bogus negative key so that the hashCode/equals still works.
        item.setKey (-1 * System.currentTimeMillis ());

        this.addNewItem (item);

        AddEditCommentPopup p = new AddEditCommentPopup (this.viewer,
                                                         this.object,
                                                         item);
        p.setOnCancel (ev ->
        {

            this.removeNewItem (item);

            this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

        });

        p.setOnClose (() ->
        {

            this.removeNewItem (item);

            this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

        });

        UIUtils.forceRunLater (() ->
        {

            this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

        });

        QuollPopup qp = p.getPopup ();

        UIUtils.forceRunLater (() ->
        {

            this.showPopupForItem (item,
                                   qp);

        });

    }

    @Override
    public Map<KeyCombination, Runnable> getActionMappings ()
    {

        return new HashMap<> ();

    }

    public Set<Note> getNotesForPosition (int p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        int paraNo = this.editor.getParagraphForOffset (p);

        double y = cb.getMinY ();

        Set<Note> ret = new TreeSet<> (new ChapterItemSorter ());

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

    @Override
    public Node getMarginNodeForChapterItem (ChapterItem ci)
    {

        if (ci instanceof Note)
        {

            Note n = (Note) ci;

            IconBox riv = IconBox.builder ()
                .iconName (StyleClassNames.COMMENT)
                .build ();

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

                this.showItem (n,
                               true);

                ev.consume ();

            });

            return riv;

        }

        throw new UnsupportedOperationException ("Object not supported: " + ci);

    }

    @Override
    public Set<MenuItem> getMarginContextMenuItems (int cpos)
    {

        List<String> prefix = Arrays.asList (iconcolumn,doubleclickmenu,items);

        Set<MenuItem> items = new LinkedHashSet<> ();

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,comment)))
            .iconName (StyleClassNames.COMMENT)
            .accelerator (new KeyCharacterCombination ("C",
                                                       KeyCombination.SHORTCUT_DOWN,
                                                       KeyCombination.SHIFT_DOWN))
            .onAction (eev ->
            {

                this.showAddNewComment (this.editor.getSelection ().getStart ());

            })
            .build ());

         return items;

    }

}
