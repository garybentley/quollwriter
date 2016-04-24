package com.quollwriter.ui;

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.PopupAdapter;
import com.quollwriter.ui.components.PopupEvent;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.data.comparators.*;

public class DefaultChapterItemViewPopupProvider implements ChapterItemViewPopupProvider
{
    
    private Map<String, ChapterItemFormatDetails> formatDetails = new HashMap ();

    private boolean showLinks = true;
    
    public DefaultChapterItemViewPopupProvider ()
    {
    
        this.formatDetails.put (OutlineItem.OBJECT_TYPE,
                                new OutlineItemFormatDetails ());
        this.formatDetails.put (Scene.OBJECT_TYPE,
                                new SceneFormatDetails ());
        this.formatDetails.put (Note.OBJECT_TYPE,
                                new NoteFormatDetails ());
        
    }
    
    public void setShowLinks (boolean v)
    {
        
        this.showLinks = v;
        
    }
    
    public boolean canDelete (ChapterItem item)
    {
        
        return true;
        
    }
    
    public boolean canEdit (ChapterItem item)
    {
        
        return true;
        
    }
    
    public void setFormatDetails (String                   objType,
                                  ChapterItemFormatDetails details)
    {
        
        this.formatDetails.put (objType,
                                details);
        
    }    
    
    public QPopup getViewPopup (final ChapterItem         item,
                                final AbstractEditorPanel panel)
                         throws GeneralException
    {
                        
        final QTextEditor editor = panel.getEditor ();

        Rectangle r = null;
        
        int pos = item.getPosition ();
        
        try
        {

            r = editor.modelToView (pos);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to convert item: " +
                                        item +
                                        " at position: " +
                                        pos,
                                        e);

        }

        int y = r.y;

        int min = pos - 1500;
        int max = pos + 1500;
        
        java.util.Set<? extends ChapterItem> items = null;
        
        if ((item instanceof Scene)
            ||
            (item instanceof OutlineItem)
           )
        {
            
            items = item.getChapter ().getAllStructureItemsWithinRange (min,
                                                                        max);
            
        }
        
        if (item instanceof Note)
        {
            
            items = item.getChapter ().getChapterItems (item.getObjectType ());
            
        }

        AbstractProjectViewer viewer = panel.getViewer ();        
        
        final java.util.Set<ChapterItem> its = new TreeSet (new ChapterItemSorter ());

        for (ChapterItem it : items)
        {

            viewer.setLinks (it);

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
        
        ChapterItem fitem = item;        
        
        if (its.size () > 1)
        {

            fitem = its.iterator ().next ();
        
        }
        
        ChapterItemFormatDetails formatter = formatDetails.get (fitem.getObjectType ());

        if (formatter == null)
        {
            
            throw new IllegalArgumentException ("Item: " +
                                                fitem +
                                                " has no formatter.");
            
        }        
        
        // Show a panel of all the items.
        final QPopup popup = UIUtils.createClosablePopup (Environment.replaceObjectNames (formatter.getTitle (fitem)),
                                                          Environment.getIcon (formatter.getIcon (fitem),
                                                                               Constants.ICON_POPUP),
                                                          null);
        
        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                popup.removeFromParent ();

            }

        };

        popup.setOpaque (false);
        
        Box b = new Box (BoxLayout.Y_AXIS);

        int count = 0;

        for (ChapterItem it : its)
        {
        
            formatter = formatDetails.get (it.getObjectType ());
    
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

            JTextPane t = UIUtils.createObjectDescriptionViewPane (formatter.getItemDescription (it), //null,
                                                                   it,
                                                                   viewer,
                                                                   panel);
/*
            t.setText (UIUtils.getWithHTMLStyleSheet (t,
                                                      UIUtils.markupStringForAssets (formatter.getItemDescription (it),
                                                                                     viewer.getProject (),
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
                                                        viewer,
                                                        popup,
                                                        true),
                        cc.xy (2,
                               row++));

            }

            List<JButton> buts = new ArrayList ();
                           
            JButton mb = null;

            if (this.canEdit (it))
            {
            
                AbstractActionHandler aah = formatter.getEditItemActionHandler (it,
                                                                                panel);
    
                aah.setPopupOver (panel); 
                        
                mb = UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                           Constants.ICON_MENU,
                                           "Click to edit this item.",
                                           aah);
                mb.setTransferHandler (null);
                mb.addActionListener (aa);
                
                buts.add (mb);
                                       
                aah = formatter.getEditItemActionHandler (it,
                                                          panel);
    
                aah.setPopupOver (panel); 
                aah.setShowLinkTo (this.showLinks);
                
                if (this.showLinks)
                {
                
                    mb = UIUtils.createButton (Link.OBJECT_TYPE,
                                               Constants.ICON_MENU,
                                               "Click to link this item to other items/objects.",
                                               aah);
        
                    mb.setActionCommand ("link");
                    mb.addActionListener (aa);
                    buts.add (mb);
    
                }

            }
                
            if (this.canDelete (it))
            {                
            
                ActionListener dah = formatter.getDeleteItemActionHandler (it,
                                                                           panel,
                                                                           true);
                            
                JButton but = UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                                    Constants.ICON_MENU,
                                                    "Click to delete this item.",
                                                    dah);
                but.addActionListener (aa);
                
                buts.add (but);

            }
            
            JPanel pan = pb.getPanel ();
            pan.setOpaque (false);

            pan.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));

            if (count < its.size () - 1)
            {

                pan.setBorder (UIUtils.createBottomLineWithPadding (0, 5, 0, 5));

            } else
            {

                pan.setBorder (UIUtils.createPadding (0, 5, 3, 5));

            }

            if (buts.size () > 0)
            {
            
                pb.add (UIUtils.createButtonBar (buts),
                        cc.xy (2,
                               row++));

            }
                               
            b.add (pan);

            count++;

        }

        // Needed to trigger the component events.
        popup.setVisible (false);
        
        // Need a nicer way of doing this.
        if (panel instanceof ChapterItemViewer)
        {
        
            final ChapterItemViewer civ = (ChapterItemViewer) panel;

            ChapterItem it = null;
            
            if (its.size () == 1)
            {

                it = its.iterator ().next ();

            }
        
            if (it != null)
            {
        
                final ChapterItem iitem = it;
        
                popup.addPopupListener (new PopupAdapter ()
                {
                   
                    @Override
                    public void popupShown (PopupEvent ev)
                    {        
        
                        civ.highlightItemTextInEditor (iitem);
                        
                    }
                    
                    @Override
                    public void popupHidden (PopupEvent ev)
                    {
                        
                        civ.removeItemHighlightTextFromEditor (iitem);
                        
                    }
                    
                });

            }
                
        }
                    
        popup.setContent (b);
        
        return popup;
        
    }
    
}