package com.quollwriter.editors.ui.panels;

import java.awt.*;
import java.awt.im.*;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.datatransfer.*;

//import org.incava.util.diff.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;

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

public class ChapterCommentsPanel extends AbstractViewOnlyEditorPanel implements ChapterItemViewer
{

     private IconColumn<ProjectSentReceivedViewer> iconColumn = null;
     protected ProjectSentReceivedViewer   projectViewer = null;
     private int                     lastCaret = -1;
     private ChapterItemTransferHandler chItemTransferHandler = null;
     private BlockPainter highlight = null;
     private boolean chapterItemEditVisible = false;
  
     public ChapterCommentsPanel (ProjectSentReceivedViewer pv,
                                  Chapter                   c)
                           throws GeneralException
     {

        super (pv,
               c);
        
        this.projectViewer = pv;

        final ChapterCommentsPanel _this = this;

        this.editor.setEditable (false);
        this.editor.setCanCopy (false);
                
        this.iconColumn = new IconColumn<ProjectSentReceivedViewer> (this,
                                                                     c,
                                                                     this.projectViewer.getIconProvider (),
                                                                     this.projectViewer.getChapterItemViewPopupProvider ());

        this.iconColumn.setItemMoveAllowed (false);
         this.iconColumn.setSinglePopupOnly (true);
                
        this.chItemTransferHandler = new ChapterItemTransferHandler (this.getIconColumn ());
        
        //this.setTransferHandler (this.chItemTransferHandler);
                                                    
        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Remove ctrl+shift+O from the when_focused set since it conflicts.
        this.editor.getInputMap (JComponent.WHEN_FOCUSED).put (KeyStroke.getKeyStroke ("ctrl shift O"),
                                                               "none");
        
        this.highlight = new BlockPainter (Environment.getHighlightColor ());        
                
    }

    @Override
    public void scrollCaretIntoView ()
    {
        
        // Do nothing.
                
    }
    
    /*
    public void showDifferences (Chapter c)
    {
      
         BlockPainter newp = new BlockPainter (Color.GREEN);              
         BlockPainter modp = new BlockPainter (Color.YELLOW);              
         BlockPainter oldp = new BlockPainter (Color.RED);
      
         String ot = TextUtilities.stripNonValidXMLCharacters (this.editor.getTextWithMarkup ().getText ());

         String nt = TextUtilities.stripNonValidXMLCharacters (c.getText ().getText ());      
         
         this.editor.setTextWithMarkup (c.getText ());
      
         String[] oldText = ot.split ("\\n");
         String[] newText = nt.split ("\\n");
System.out.println ("OT: " + ot);
System.out.println ("NT: " + nt);
//LinkedList<Diff>
diff_match_patch dmp = new diff_match_patch ();
   System.out.println ("DIFF2: " + dmp.patch_make(ot, nt));

         List diffs = new Diff (oldText,
                                newText).diff ();
System.out.println ("DIFF: " + diffs);
         for (int i = 0; i < diffs.size (); i++)
         {

             Difference d = (Difference) diffs.get (i);

             if (d.getDeletedEnd () == Difference.NONE)
             {

                 // This is an addition.
                 for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
                 {

                     this.editor.addHighlight (d.getAddedStart (),
                                               d.getAddedEnd (),
                                               newp,
                                               false);                                     

                 }

                 continue;

             }

             if (d.getAddedEnd () == Difference.NONE)
             {

                 // This is a deletion.
                 for (int k = d.getDeletedStart (); k < (d.getDeletedEnd () + 1); k++)
                 {

                     this.editor.addHighlight (d.getDeletedStart (),
                                               d.getDeletedEnd (),
                                               oldp,
                                               false);                                     

                 }

                 continue;

             }

             // This is a modification.
             for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
             {

                  this.editor.addHighlight (d.getAddedStart (),
                                            d.getAddedEnd (),
                                            modp,
                                            false);                                     

             }

         }      
      
    }
    */
    @Override
     public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
     {
          
        if (hasFocus)
        {

            this.editor.grabFocus ();

        }
          
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
      
         final ChapterCommentsPanel _this = this;

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

    @Override
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

        final ChapterCommentsPanel _this = this;

        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };

    }

    private void performAction (ActionEvent ev,
                                String      c,
                                int         pos)
    {

        if (c == null)
        {

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

        final ChapterCommentsPanel _this = this;

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
                                                                                                                             
            buts.add (this.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Find",
                                         Constants.SHOW_FIND_ACTION));

            popup.add (UIUtils.createPopupMenuButtonBar (Environment.replaceObjectNames ("{Chapter}"),
                                                         popup,
                                                         buts));
                                     
        } else {
          
            mi = this.createMenuItem ("Find",
                                      Constants.FIND_ICON_NAME,
                                      Constants.SHOW_FIND_ACTION,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_F);
            popup.add (mi);                                                
                    
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

    public void scrollToItem (ChapterItem i)
                       throws GeneralException
    {

        this.scrollToPosition (i.getPosition ());

    }

    public void showNote (Note n)
                   throws GeneralException
    {

        this.scrollToNote (n);

        this.iconColumn.showItem (n);

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

    public void reinitIconColumn ()
                                  throws GeneralException
    {

        try
        {
    
            this.iconColumn.init ();
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to init icon column",
                                        e);
            
        }
        
    }
    
    @Override
    public void init ()
               throws GeneralException
    {
      
         super.init ();

         final ChapterCommentsPanel _this = this;
        
         this.reinitIconColumn ();

         this.setReadyForUse (true);
               
    }
    
    @Override
    public void close ()
    {

        super.close ();

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

   public void removeItem (ChapterItem c)
   {
     
       throw new UnsupportedOperationException ("Not supported for project comments viewing.");                      
     
   }

   public void addItem (ChapterItem c)
                 throws GeneralException
   {
      
      throw new UnsupportedOperationException ("Not supported for project comments viewing.");                            
      
   }
    
}
