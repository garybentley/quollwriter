package com.quollwriter.ui.actionHandlers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;

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


public abstract class ShowChapterItemActionHandler extends ActionAdapter
{

    protected ChapterItem      item = null;
    private QPopup             popup = null;
    protected QuollEditorPanel quollEditorPanel = null;
    protected ProjectViewer    projectViewer = null;
    private BlockPainter       highlight = new BlockPainter (Environment.getHighlightColor ());

    public ShowChapterItemActionHandler(ChapterItem      item,
                                        QuollEditorPanel qep)
    {

        this.item = item;
        this.quollEditorPanel = qep;
        this.projectViewer = (ProjectViewer) this.quollEditorPanel.getProjectViewer ();

        final ShowChapterItemActionHandler _this = this;

        String ot = item.getObjectType ();

        if ((item instanceof Note) &&
            (((Note) item).isEditNeeded ()))
        {

            ot = "edit-needed-note";

        }

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
        
        // Show a panel of all the items.
        this.popup = new QPopup (Environment.getObjectTypeName (item.getObjectType ()),
                                 Environment.getIcon (ot,
                                                      Constants.ICON_POPUP),
                                 UIUtils.createButtonBar (buts))
        {

            public void setVisible (boolean v)
            {

                if (!v)
                {

                    _this.quollEditorPanel.getEditor ().removeAllHighlights (_this.highlight);


                }

                super.setVisible (v);

            }

        };
                
/*
        QuollEditorPanel qep = this.projectViewer.getEditorForChapter (this.item.getChapter ());

        if (qep == null)
        {

            throw new IllegalArgumentException ("Unable to find chapter for item: " +
                                                this.item);

        }
*/
        this.quollEditorPanel.addPopup (this.popup,
                                        true,
                                        true);

        this.popup.setVisible (false);

        this.popup.setDraggable (this.quollEditorPanel);

    }

    public abstract AbstractActionHandler getEditItemActionHandler (ChapterItem item);

    public abstract ActionListener getDeleteItemActionHandler (ChapterItem item);

    public abstract String getItemDescription (ChapterItem item);

    public void showItem ()
    {

        // QuollEditorPanel qep = this.projectViewer.getEditorForChapter (this.item.getChapter ());

        QTextEditor editor = this.quollEditorPanel.getEditor ();

        Rectangle r = null;

        try
        {

            r = editor.modelToView (this.item.getPosition ());

        } catch (Exception e)
        {

            // BadLocationException!
            Environment.logError ("Unable to convert item: " +
                                  this.item +
                                  " at position: " +
                                  this.item.getPosition (),
                                  e);

            UIUtils.showErrorMessage (this.quollEditorPanel,
                                      "Unable to display Items");

            return;

        }

        int y = r.y;

        java.util.Set<? extends ChapterItem> items = null;

        if (this.item.getScene () != null)
        {

            items = this.item.getScene ().getOutlineItems ();

        } else
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

            int row = 1;

            FormLayout   summOnly = new FormLayout ("5px, fill:380px:grow, 5px",
                                                    "p, p, p");
            PanelBuilder pb = new PanelBuilder (summOnly);

            CellConstraints cc = new CellConstraints ();

            JTextPane t = UIUtils.createObjectDescriptionViewPane (it.getDescription (),
                                                                   it,
                                                                   this.projectViewer,
                                                                   this.quollEditorPanel);

            t.setText (UIUtils.getWithHTMLStyleSheet (t,
                                                      UIUtils.markupStringForAssets (this.getItemDescription (it),
                                                                                     this.projectViewer.getProject (),
                                                                                     it)));


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
            
            AbstractActionHandler aah = this.getEditItemActionHandler (it);

            aah.setPopupOver (this.quollEditorPanel); 
            aah.setOnShowAction (aa);
            
            buts.add (UIUtils.createButton ("edit",
                                            Constants.ICON_MENU,
                                            "Click to edit this item.",
                                            aah));

            aah = this.getEditItemActionHandler (it);

            aah.setPopupOver (this.quollEditorPanel); 
            aah.setShowLinkTo (true);
            aah.setOnShowAction (aa);

            buts.add (UIUtils.createButton (Link.OBJECT_TYPE,
                                            Constants.ICON_MENU,
                                            "Click to link this item to other items/objects.",
                                            aah));

            ActionListener dah = this.getDeleteItemActionHandler (it);
            
            JButton but = UIUtils.createButton ("delete",
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

        JScrollPane scrollPane = this.quollEditorPanel.getScrollPane ();

        if (this.item instanceof Note)
        {
           
            Note n = (Note) this.item;
            
            if (n.isEditNeeded ())
            {
                
                y = y - this.popup.getPreferredSize ().height - 22;
                
            }
            
        }

        y = 22 + y - scrollPane.getVerticalScrollBar ().getValue ();

        // JScrollPane scrollPane = this.quollEditorPanel.getScrollPane ();

        // Adjust the bounds so that the form is fully visible.
        if ((y + this.popup.getPreferredSize ().height) > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ()))
        {

            y = y - 22 - this.popup.getPreferredSize ().height;

        }

        this.quollEditorPanel.showPopupAt (this.popup,
                                           new Point (this.quollEditorPanel.getIconColumn ().getWidth () - 20,
                                                      y));

    }

    public void hideItem ()
    {

        // QuollEditorPanel qep = this.projectViewer.getEditorForChapter (this.item.getChapter ());

        this.quollEditorPanel.removePopup (this.popup);

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.showItem ();

    }

}
