package com.quollwriter.editors.ui.panels;

import java.awt.*;
import java.awt.im.*;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.datatransfer.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ScrollablePanel;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.renderers.*;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;


public class EditorChapterPanel extends AbstractViewOnlyEditorPanel implements ChapterItemViewer
{

   public static final String SHOW_EDITORS_ACTION_NAME = "show-editors";
   public static final String CHAPTER_INFO_ACTION_NAME = "chapter-info";
   public static final String SET_EDIT_COMPLETE_ACTION_NAME = "set-edit-complete";
   public static final String REMOVE_EDIT_POINT_ACTION_NAME = "remove-edit-point";

   public static final String NEW_COMMENT_ACTION_NAME = "newcomment";
       
   private IconColumn<EditorProjectViewer>              iconColumn = null;
   protected EditorProjectViewer   projectViewer = null;
   private int                     lastCaret = -1;
   private ChapterItemTransferHandler chItemTransferHandler = null;
   private BlockPainter highlight = null;
   private boolean chapterItemEditVisible = false;

   public EditorChapterPanel (EditorProjectViewer pv,
                              Chapter             c)
                       throws GeneralException
   {

        super (pv,
               c);
        
        this.projectViewer = pv;

        final EditorChapterPanel _this = this;

        this.editor.setEditable (false);
        this.editor.setCanCopy (false);
                
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
        
        this.editor.addCaretListener (new CaretListener ()
        {
         
            public void caretUpdate (CaretEvent ev)
            {
               
               if ((_this.editor.getSelectionEnd () > _this.editor.getSelectionStart ())
                   &&
                   (!_this.isChapterItemEditVisible ())
                   &&
                   (_this.isReadyForUse ())
                  )
               {
                  
                  _this.performAction (new ActionEvent (_this, 1, "show"),
                                       NEW_COMMENT_ACTION_NAME,
                                       _this.editor.getSelectionStart ());
                  
               }
               
            }
         
        });
        
        this.chItemTransferHandler = new ChapterItemTransferHandler (this.getIconColumn ());
        
        this.setTransferHandler (this.chItemTransferHandler);

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

                                      _this.projectViewer.viewEditorChapterInformation (_this.chapter);

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show chapter information for: " +
                                                            _this.chapter,
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                Environment.replaceObjectNames ("Unable to show {chapter}."));

                                  }

                              }

                          });
                                                    
        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Remove ctrl+shift+O from the when_focused set since it conflicts.
        this.editor.getInputMap (JComponent.WHEN_FOCUSED).put (KeyStroke.getKeyStroke ("ctrl shift O"),
                                                               "none");
        
        im.put (KeyStroke.getKeyStroke ("ctrl shift C"),
                NEW_COMMENT_ACTION_NAME);

        this.highlight = new BlockPainter (Environment.getHighlightColor ());        
                
    }
    
    public void setChapterItemEditVisible (boolean v)
    {
      
         this.chapterItemEditVisible = v;
      
    }
    
    public boolean isChapterItemEditVisible ()
    {
      
         return this.chapterItemEditVisible;
      
    }
    
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
    
    public ChapterItemTransferHandler getChapterItemTransferHandler ()
    {
        
        return this.chItemTransferHandler;
        
    }
    
   public int getIconColumnXOffset (ChapterItem i)
   {
     
      int xOffset = 22;

      if (i instanceof OutlineItem)
      {

          xOffset = 22;

      }
      
      return xOffset;
     
   }
    
    public JComponent getEditorWrapper (QTextEditor q)
    {

        Box b = new Box /*com.quollwriter.ui.components.ScrollableBox*/ (BoxLayout.X_AXIS);
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

    public void doFillToolsPopupMenu (ActionEvent ev,
                                      JPopupMenu  p)
    {

    }

   @Override
   public void fillToolBar (JToolBar acts,
                            final boolean  fullScreen)
   {

      final AbstractEditorPanel _this = this;

      this.doFillToolBar (acts);

      acts.add (this.createToolbarButton (Constants.EDIT_PROPERTIES_ICON_NAME,
                                          "Click to edit the text properties",
                                          EDIT_TEXT_PROPERTIES_ACTION_NAME));
      
   }
    
    public void doFillToolBar (JToolBar acts)
    {

        final EditorChapterPanel _this = this;

        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };

        final JButton b = UIUtils.createToolBarButton ("new",
                                                       "Click to add a new {Comment}",
                                                       "new",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                _this.addNewItemsForPopupMenu (m,
                                               b,
                                               -1,
                                               false);

                Component c = (Component) ev.getSource ();

                m.show (c,
                        10,
                        10);

            }

        };

        b.addActionListener (ab);

        acts.add (b);

    }

    private void performAction (ActionEvent ev,
                                String      c,
                                int         pos)
    {

        if (c == null)
        {

            return;

        }

        if (c.equals (NEW_COMMENT_ACTION_NAME))
        {

            new CommentActionHandler<EditorProjectViewer> (this.chapter,
                                                           this,
                                                           pos).actionPerformed (ev);

            return;

        }

    }

    private void performAction (ActionEvent ev,
                                int         pos)
    {

        String c = ev.getActionCommand ();

        this.performAction (ev,
                            c,
                            pos);

    }

    private void addNewItemsForPopupMenu (final JComponent popup,
                                                Component  showAt,
                                                int        pos,
                                                boolean    compress)
    {

        final EditorChapterPanel _this = this;

        final PositionActionAdapter aa = new PositionActionAdapter (pos)
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev,
                                     this.pos);

            }

        };

        if (compress)
        {
        
            List<JComponent> buts = new ArrayList ();

            buts.add (this.createButton (Constants.COMMENT_ICON_NAME,
                                         Constants.ICON_MENU,
                                         String.format ("Add a new {Comment}"),
                                         NEW_COMMENT_ACTION_NAME,
                                         aa));

            if (popup instanceof JPopupMenu)
            {                                            
            
               JPopupMenu pm = (JPopupMenu) popup;
            
               pm.addSeparator ();
                                                           
               popup.add (UIUtils.createPopupMenuButtonBar ("New",
                                                            pm,
                                                            buts));
                            
            }
                                          
        } else {
        
            String pref = "Shortcut: Ctrl+Shift+";
            
            JMenuItem mi = null;
            
            mi = this.createMenuItem ("Comment",
                                      Constants.COMMENT_ICON_NAME,
                                      NEW_COMMENT_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);
                                         
            char fc = 'C';
                                          
            mi.setMnemonic (fc);
            mi.setToolTipText (pref + fc);
    
        }
                                                 
    }
    
    public void setEditPosition (int textPos)
    {

        try
        {

            int tl = Utils.stripEnd (this.editor.getText ()).length ();
        
            if (textPos > tl)
            {
                
                textPos = tl;
                
            }
        
            this.iconColumn.setEditPosition (textPos);
            
            // See if we are on the last line (it may be that the user is in the icon
            // column).
            Rectangle pp = this.editor.modelToView (textPos);
            
            if (UserProperties.getAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME))
            {
            
                if (textPos < tl)
                {
                    
                    Rectangle ep = this.editor.modelToView (tl);

                    boolean complete = false;
                    
                    if (ep.y == pp.y)                    
                    {
                        
                        complete = true;
                        
                    }
                        
                    // Last line.
                    this.chapter.setEditComplete (complete);
                                                                
                }

            }

            this.projectViewer.reloadChapterTree ();
            
            this.projectViewer.saveObject (this.chapter,
                                           false);
                                                                
        } catch (Exception e) {
            
            Environment.logError ("Unable to set edit position for chapter: " +
                                  this.chapter,
                                  e);
                                                            
        }
        
    }
    
    public void setEditPosition (Point mouseP)
    {

        this.setEditPosition (this.editor.viewToModel (mouseP));
        
    }
    
    public void removeEditPosition ()
    {
        
        try
        {
            
            this.iconColumn.setEditPosition (-1);

            this.setEditComplete (false);
            
            this.projectViewer.saveObject (this.chapter,
                                           false);

            this.projectViewer.reloadChapterTree ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set edit position for chapter: " +
                                  this.chapter,
                                  e);
                                                            
        }
        
    }
    
    public void setEditComplete (boolean v)
    {
        
        try
        {
            
            this.chapter.setEditComplete (v);
            
            if (v)
            {
            
                int tl = Utils.stripEnd (this.editor.getText ()).length ();
            
                this.iconColumn.setEditPosition (tl);
                                
            }
            
            this.projectViewer.saveObject (this.chapter,
                                           false);

            this.iconColumn.init ();

            this.projectViewer.reloadChapterTree ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set edit complete for chapter: " +
                                  this.chapter,
                                  e);
                                                            
        }        
        
    }

    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

      boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);    
      
      this.doFillPopupMenu (ev,
                            popup,
                            compress);
    
    }
    
    public void doFillPopupMenu (final MouseEvent ev,
                                 final JPopupMenu popup,
                                       boolean    compress)
    {

        final EditorChapterPanel _this = this;

        final QTextEditor         editor = this.editor;

        JMenuItem mi = null;
        
        // Get the mouse position, don't get it later since the mouse could have moved.
        Point mP = this.editor.getMousePosition ();        
        
        if (mP == null)
        {
            
            mP = this.iconColumn.getMousePosition ();
            
        }
        
        final Point mouseP = mP;
        
        int pos = this.getTextPositionForMousePosition (ev.getPoint ());

        // This is needed to move to the correct character, the call above seems to get the character
        // before what was clicked on.
        // pos++;
                  
        if (compress)
        {
                         
            List<JComponent> buts = new ArrayList ();
                                                                                                                             
            buts.add (UIUtils.createButton (Constants.EDIT_IN_PROGRESS_ICON_NAME,
                                            Constants.ICON_MENU,
                                            "Set Edit Point",
                                            new ActionAdapter ()
                                            {
                                               
                                               public void actionPerformed (ActionEvent ev)
                                               {
                                                   
                                                   _this.setEditPosition (mouseP);
                                                   
                                               }
                                               
                                            }));

            if (this.chapter.getEditPosition () > 0)
            {
    
                buts.add (this.createButton (Constants.REMOVE_EDIT_POINT_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Remove Edit Point",
                                             REMOVE_EDIT_POINT_ACTION_NAME));
                   
            }

            if (!this.chapter.isEditComplete ())
            {
                
                buts.add (this.createButton (Constants.EDIT_COMPLETE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Set as Edit Complete",
                                             SET_EDIT_COMPLETE_ACTION_NAME));
                    
            } 

            buts.add (this.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Find",
                                         Constants.SHOW_FIND_ACTION));
            
            popup.add (UIUtils.createPopupMenuButtonBar ("{Chapter}",
                                                         popup,
                                                         buts));

            this.addNewItemsForPopupMenu (popup,
                                          this,
                                          pos,
                                          compress);        
                         
        } else {

            // Save.
                            
            JMenu m = new JMenu (Environment.replaceObjectNames ("{Chapter} Edit"));
            m.setIcon (Environment.getIcon (Constants.EDIT_ICON_NAME,
                                            Constants.ICON_MENU));
    
            popup.add (m);
                        
            mi = UIUtils.createMenuItem ("Set Edit Point",
                                         Constants.EDIT_IN_PROGRESS_ICON_NAME,
                                         new ActionAdapter ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                _this.setEditPosition (mouseP);
                                                
                                            }
                                            
                                         });
                
            m.add (mi);
    
            if (this.chapter.getEditPosition () > 0)
            {
    
                m.add (this.createMenuItem ("Remove Edit Point",
                                            Constants.REMOVE_EDIT_POINT_ICON_NAME,
                                            REMOVE_EDIT_POINT_ACTION_NAME,
                                            null));
    
            }
    
            if (!this.chapter.isEditComplete ())
            {
                
                m.add (this.createMenuItem ("Set as Edit Complete",
                                            Constants.EDIT_COMPLETE_ICON_NAME,
                                            SET_EDIT_COMPLETE_ACTION_NAME,
                                            null));
    
            } 
            
            JMenu nm = new JMenu ("New");
            nm.setIcon (Environment.getIcon (Constants.NEW_ICON_NAME,
                                             Constants.ICON_MENU));

            popup.add (nm);

            this.addNewItemsForPopupMenu (nm,
                                          this,
                                          pos,
                                          compress);        
                                            
        }
                    
    }

    public IconColumn getIconColumn ()
    {

        return this.iconColumn;

    }

    public QTextEditor getEditor ()
    {

        return this.editor;

    }

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

    public void editNote (Note n)
                   throws GeneralException
    {

        this.scrollToNote (n);

        new CommentActionHandler (n,
                                  this).actionPerformed (new ActionEvent (this,
                                                                          0,
                                                                          "edit"));

    }

    public void showNote (Note           n,
                          ActionListener doAfterShow)
                   throws GeneralException
    {

        this.scrollToNote (n);

        this.iconColumn.showItem (n);

        if (doAfterShow != null)
        {
         
            doAfterShow.actionPerformed (new ActionEvent (this, 1, "noteshown"));
         
        }
        
    }

   public void removeItemHighlightTextFromEditor (ChapterItem it)
   {
      
      this.editor.removeAllHighlights (this.highlight);      
    
   }
   
   public void highlightItemTextInEditor (ChapterItem it)
   {
      
      this.editor.removeAllHighlights (this.highlight);
      this.editor.addHighlight (it.getStartPosition (),
                                it.getEndPosition (),
                                this.highlight,
                                false);                    
      
   }
    
    public void scrollToNote (Note n)
                       throws GeneralException
    {

        this.scrollToPosition (n.getPosition ());

    }

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();
        l.add (this.iconColumn);
        l.add (this.editor);

        return l;

    }

    public void refresh (NamedObject n)
    {

        // No need to do anything.

    }
    
   @Override
   public void init ()
              throws GeneralException
   {
     
        super.init ();

        try
        {
    
            this.iconColumn.init ();
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to init icon column",
                                        e);
            
        }

        this.setCaretPosition (0);
        
        this.setReadyForUse (true);         
               
   }

    public void restoreBackgroundColor ()
    {

        super.restoreBackgroundColor ();

        //this.iconColumn.setBackground (IconColumn.defaultBGColor);

    }

    public void setBackgroundColor (Color c)
    {

        super.setBackgroundColor (c);
/*
        if (c.equals (Color.white))
        {

            this.iconColumn.setBackground (IconColumn.defaultBGColor);

        } else
        {

            this.iconColumn.setBackground (c);

        }
*/
    }

   private static class CutNPasteTransferHandler extends TransferHandler 
   {
        public void exportToClipboard(JComponent comp, Clipboard clipboard,
                                      int action)
        throws IllegalStateException
        {
         
            if (comp instanceof JTextComponent)
            {
               
                JTextComponent text = (JTextComponent)comp;
                int p0 = text.getSelectionStart();
                int p1 = text.getSelectionEnd();
                if (p0 != p1) {
                    try {
                        Document doc = text.getDocument();
                        String srcData = doc.getText(p0, p1 - p0);
         
                        StringSelection contents =new StringSelection(srcData);

                        // this may throw an IllegalStateException,
                        // but it will be caught and handled in the
                        // action that invoked this method
                        clipboard.setContents(contents, null);

                        if (action == TransferHandler.MOVE) {
                            doc.remove(p0, p1 - p0);
                        }
                    } catch (BadLocationException ble) {}
                }
            }
        }
        
        public boolean importData(JComponent comp, Transferable t)
        {
        
            if (comp instanceof JTextComponent) {
                DataFlavor flavor = getFlavor(t.getTransferDataFlavors());

                if (flavor != null) {
                    InputContext ic = comp.getInputContext();
                    if (ic != null) {
                        ic.endComposition();
                    }
                    try {
                        String data = (String)t.getTransferData(flavor);

                        ((JTextComponent)comp).replaceSelection(data);
                        return true;
                    } catch (UnsupportedFlavorException ufe) {
                    } catch (IOException ioe) {
                    }
                }
            }
            return false;
        }
        
        public boolean canImport(JComponent comp,
                                 DataFlavor[] transferFlavors)
        {
         
            JTextComponent c = (JTextComponent)comp;
            if (!(c.isEditable() && c.isEnabled())) {
                return false;
            }
            return (getFlavor(transferFlavors) != null);
        }
        
        public int getSourceActions(JComponent c)
        {
        
            return NONE;
        
        }
        
        private DataFlavor getFlavor(DataFlavor[] flavors)
        {
            if (flavors != null) {
                for (int counter = 0; counter < flavors.length; counter++) {
                    if (flavors[counter].equals(DataFlavor.stringFlavor)) {
                        return flavors[counter];
                    }
                }
            }
            return null;
        }
    }    
    
}
