package com.quollwriter.ui.actionHandlers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Image;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ImagePanel;


public class ShowChapterItemActionHandler extends ActionAdapter
{

    private static Map<String, ChapterItemFormatDetails> formatDetails = new HashMap ();

    static
    {
        
        formatDetails.put (OutlineItem.OBJECT_TYPE,
                           new OutlineItemFormatDetails ());
        formatDetails.put (Scene.OBJECT_TYPE,
                           new SceneFormatDetails ());
        formatDetails.put (Note.OBJECT_TYPE,
                           new NoteFormatDetails ());
        
    }
    
    public static void setFormatDetails (String objType,
                                         ChapterItemFormatDetails details)
    {
        
        formatDetails.put (objType,
                           details);
        
    }
    
    protected ChapterItem      item = null;
    private QPopup             popup = null;
    protected AbstractEditorPanel editorPanel = null;
    protected AbstractProjectViewer    projectViewer = null;
    private BlockPainter       highlight = new BlockPainter (Environment.getHighlightColor ());
    private IconColumn         iconColumn = null;
    
    public ShowChapterItemActionHandler(ChapterItem      item,
                                        AbstractEditorPanel qep,
                                        IconColumn          ic)
    {

        this.item = item;
        this.editorPanel = qep;
        this.iconColumn = ic;
        this.projectViewer = this.editorPanel.getProjectViewer ();
        
        final ShowChapterItemActionHandler _this = this;
/*
        JButton bt = UIUtils.createButton ("cancel",
                                               Constants.ICON_MENU,
                                               "Click to close.",
                                               new ActionAdapter ()
                                               {
                                                    
                                                   public void actionPerformed (ActionEvent ev)
                                                   {
                                        
                                                       _this.hideItem ();
                                                       
                                                   }
                                               
                                               });
        
        List<JButton> buts = new ArrayList ();
        buts.add (bt);
*/
        ChapterItemFormatDetails formatter = ShowChapterItemActionHandler.formatDetails.get (this.item.getObjectType ());

        if (formatter == null)
        {
            
            throw new IllegalArgumentException ("Item: " +
                                                this.item +
                                                " has no formatter.");
            
        }        
        
        // Show a panel of all the items.
        this.popup = UIUtils.createClosablePopup (Environment.replaceObjectNames (formatter.getTitle (item)),
                                                  Environment.getIcon (formatter.getIcon (item),
                                                                       Constants.ICON_POPUP),
                                                  new ActionAdapter ()
                                                  {
                                                    
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            _this.editorPanel.getEditor ().removeAllHighlights (_this.highlight);
                                                            
                                                        }
                                                    
                                                  });

        this.editorPanel.addPopup (this.popup,
                                   true,
                                   true);

        this.popup.setVisible (false);

        this.popup.setDraggable (this.editorPanel);

    }
    
    public void showItem ()
    {

        if (this.projectViewer.isDistractionFreeModeEnabled ())
        {
            
            this.projectViewer.showNotificationPopup ("Function unavailable",
                                                      "Sorry, you cannot view {Notes}, {Plot Outline Items} and {Scenes} while distraction free mode is enabled.<br /><br /><a href='help:full-screen-mode/distraction-free-mode'>Click here to find out why</a>",
                                                      5);

            return;            
            
        }
    
        final ShowChapterItemActionHandler _this = this;
        
        QTextEditor editor = this.editorPanel.getEditor ();

        Rectangle r = null;
        
        int pos = this.item.getPosition ();
        
        try
        {

            r = editor.modelToView (pos);

        } catch (Exception e)
        {

            // BadLocationException!
            Environment.logError ("Unable to convert item: " +
                                  this.item +
                                  " at position: " +
                                  pos,
                                  e);

            UIUtils.showErrorMessage (this.editorPanel,
                                      "Unable to display Items");

            return;

        }

        int y = r.y;

        int min = pos - 1500;
        int max = pos + 1500;
        
        java.util.Set<? extends ChapterItem> items = null;
        
        if ((this.item instanceof Scene)
            ||
            (this.item instanceof OutlineItem)
           )
        {
            
            items = this.item.getChapter ().getAllStructureItemsWithinRange (min,
                                                                             max);
            
        }
        
        if (this.item instanceof Note)
        {
            
            items = this.item.getChapter ().getChapterItems (this.item.getObjectType ());
            
        }

        java.util.Set<ChapterItem> its = new TreeSet (new ChapterItemSorter ());

        for (ChapterItem it : items)
        {

            this.projectViewer.setLinks (it);

            r = null;

            try
            {

                r = editor.modelToView (it.getPosition ());

            } catch (Exception e)
            {

                // BadLocationException!
                Environment.logError ("Unable to convert item: " +
                                      it +
                                      " at position: " +
                                      it.getPosition (),
                                      e);

                continue;

            }

            if (r.y == y)
            {

                its.add (it);

            }

        }

        if (its.size () > 1)
        {

            this.popup.getHeader ().setIcon (Environment.getIcon (this.item.getObjectType (),
                                                                  Constants.ICON_POPUP));
            this.popup.getHeader ().setTitle (Environment.getObjectTypeNamePlural (item.getObjectType ()));

        }

        // Show a panel of all the items.
        final QPopup p = this.popup;

        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                p.setVisible (false);

            }

        };

        p.setOpaque (false);

        // QuollPanel qp = this.projectViewer.getEditorForChapter (this.item.getChapter ());

        Box b = new Box (BoxLayout.Y_AXIS);

        int count = 0;

        for (ChapterItem it : its)
        {
        
            ChapterItemFormatDetails formatter = ShowChapterItemActionHandler.formatDetails.get (it.getObjectType ());
    
            if (formatter == null)
            {
                
                throw new IllegalArgumentException ("Item: " +
                                                    it +
                                                    " has no formatter.");
                
            }        
        
            int row = 1;

            FormLayout   summOnly = new FormLayout ("5px, fill:380px:grow, 5px",
                                                    "p, p, p");
            PanelBuilder pb = new PanelBuilder (summOnly);

            CellConstraints cc = new CellConstraints ();

            JTextPane t = UIUtils.createObjectDescriptionViewPane (it.getDescription (),
                                                                   it,
                                                                   this.projectViewer,
                                                                   this.editorPanel);
/*
            t.setText (UIUtils.getWithHTMLStyleSheet (t,
                                                      UIUtils.markupStringForAssets (formatter.getItemDescription (it),
                                                                                     this.projectViewer.getProject (),
                                                                                     it)));
*/

            // t.setText (it.getDescription ());
            // Annoying that we have to do this but it prevents the text from being too small.
            t.setSize (380,
                       Short.MAX_VALUE);
                       
            pb.add (t,
                    cc.xy (2,
                           row++));

            if ((it.getLinks () != null) &&
                (it.getLinks ().size () > 0))
            {

                pb.add (UIUtils.createLinkedToItemsBox (it,
                                                        this.projectViewer,
                                                        p,
                                                        true),
                        cc.xy (2,
                               row++));

            }

            List<JButton> buts = new ArrayList ();
                           
            JButton mb = null;
            
            AbstractActionHandler aah = formatter.getEditItemActionHandler (it,
                                                                            this.editorPanel);

            aah.setPopupOver (this.editorPanel); 
            aah.setOnShowAction (aa);
            
            mb = UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                       Constants.ICON_MENU,
                                       "Click to edit this item.",
                                       aah);
            mb.setTransferHandler (null);
            
            buts.add (mb);
                                       
            aah = formatter.getEditItemActionHandler (it,
                                                      this.editorPanel);

            aah.setPopupOver (this.editorPanel); 
            aah.setShowLinkTo (true);
            aah.setOnShowAction (aa);

            mb = UIUtils.createButton (Link.OBJECT_TYPE,
                                       Constants.ICON_MENU,
                                       "Click to link this item to other items/objects.",
                                       aah);

            mb.setActionCommand ("link");
            buts.add (mb);

            ActionListener dah = formatter.getDeleteItemActionHandler (it,
                                                                       this.editorPanel,
                                                                       true);
                        
            JButton but = UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                                Constants.ICON_MENU,
                                                "Click to delete this item.",
                                                dah);
            but.addActionListener (aa);
            
            buts.add (but);
            
            JPanel pan = pb.getPanel ();
            pan.setOpaque (false);

            pan.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));

            Border bor = new EmptyBorder (0,
                                          5,
                                          0,
                                          5);

            if (count < its.size () - 1)
            {

                pan.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                    0,
                                                                    1,
                                                                    0,
                                                                    Environment.getBorderColor ()),
                                                   bor));

            } else
            {

                pan.setBorder (bor);

            }

            pb.add (UIUtils.createButtonBar (buts),
                    cc.xy (2,
                           row++));

            // pan.setBorder (new EmptyBorder (3, 3, 3, 3));

            b.add (pan);

            // Add a highlight for the edit needed items.
            if (it.getEndPosition () > -1)
            {

                // Add a highlight.
                editor.addHighlight (it.getStartPosition (),
                                     it.getEndPosition (),
                                     this.highlight,
                                     false);

            }

            count++;

        }

        this.popup.setContent (b);

        JScrollPane scrollPane = this.editorPanel.getScrollPane ();

        if (this.item instanceof Note)
        {
           
            Note n = (Note) this.item;
            
            if (n.isEditNeeded ())
            {
                
                // Try and show the note above the selected text. (So it doesn't obscure it)
                y = y - this.popup.getPreferredSize ().height - 22;
                
                // Get where the selected text ends.
                pos = n.getEndPosition ();
                                
                try
                {
                            
                    if (y < scrollPane.getVerticalScrollBar ().getValue ())
                    {
                        
                        // We are going to potentially obscure it, show it below the first line.        
                        y = y + this.popup.getPreferredSize ().height + 22;
                        
                    }
        
                } catch (Exception e)
                {
        
                    // Just ignore.
        
                }
                
                
            }
            
        }

        y = 22 + y - scrollPane.getVerticalScrollBar ().getValue ();

        // Adjust the bounds so that the form is fully visible.
        if ((y + this.popup.getPreferredSize ().height) > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ()))
        {

            y = y - 22 - this.popup.getPreferredSize ().height;

        }

        this.editorPanel.showPopupAt (this.popup,
                                      new Point (this.iconColumn.getWidth () - 20,
                                                 y),
                                      true);

    }
        
    public void hideItem ()
    {

        this.editorPanel.removePopup (this.popup);

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.showItem ();

    }

    
    
}
