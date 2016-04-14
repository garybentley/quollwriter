package com.quollwriter.ui;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.events.*;

public class IconColumn extends JPanel implements DocumentListener
{

    public static final Color defaultBGColor = UIUtils.getIconColumnColor ();

    private class ItemWrapper<E extends ChapterItem>
    {
        
        public E item = null;
        public ImagePanel                   imagePanel = null;
        public QPopup popup = null;
        
    }
    
    private class StructureItemWrapper extends ItemWrapper<ChapterItem> {}

    private class NoteWrapper extends ItemWrapper<Note> {}

    public static final int NOTE_INDENT = 8;
    public static final int OUTLINE_ITEM_INDENT = 28;
    public static final int SCENE_INDENT = 28;
    public static final int EDIT_MARK_STROKE_WIDTH = 3;
    
    private AbstractEditorPanel                ep = null;
    private QTextEditor                        editor = null;
    private java.util.List<StructureItemWrapper> structureItems = new ArrayList ();
    private java.util.List<NoteWrapper>        notes = new ArrayList ();
    private BasicStroke editMarkStroke = new BasicStroke (0f + EDIT_MARK_STROKE_WIDTH);
    private Position editPosition = null;
    private ImagePanel dragIcon = null;
    private MouseListener showListener = null;
    private IconProvider iconProv = null;
    private ChapterItemViewPopupProvider popupProvider = null;
    private boolean itemMoveAllowed = true;
    private PropertyChangedListener editPosChange = null;
        
    public IconColumn (AbstractEditorPanel          ep,
                       IconProvider                 iconProv,
                       ChapterItemViewPopupProvider popupProv)
    {

        this.setLayout (null);

        this.ep = ep;
                
        this.popupProvider = popupProv;
        this.iconProv = iconProv;
                
        this.editor = this.ep.getEditor ();

        this.editor.getDocument ().addDocumentListener (this);

        this.setBackground (IconColumn.defaultBGColor);
                
        Image im = null;
                                       
        this.dragIcon = new ImagePanel (im,
                                        im);
        this.add (this.dragIcon);
        this.dragIcon.setVisible (false);
        
        final IconColumn _this = this;
        
        // We keep a reference here because weak listeners are now used on dataobject which means that this
        // could stop being referenced.
        this.editPosChange = new PropertyChangedListener ()
        {
            
            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (ev.getChangeType ().equals (Chapter.EDIT_POSITION))
                {
            
                    try
                    {
    
                        _this.setEditPosition (_this.ep.getChapter ().getEditPosition ());
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to set edit position for chapter: " +
                                              _this.ep.getChapter (),
                                              e);
                        
                    }

                }
                
            }
            
        };
                         
        ep.getChapter ().addPropertyChangedListener (this.editPosChange);
                         
    }

    public void setItemMoveAllowed (boolean v)
    {
        
        this.itemMoveAllowed = v;
        
    }
    
    public IconProvider getIconProvider ()
    {
        
        return this.iconProv;
        
    }
    
    private ItemWrapper getWrapper (ChapterItem it)
    {
        
        if (it instanceof Note)
        {
            
            for (NoteWrapper nw : this.notes)
            {
                
                if (it == nw.item)
                {
                                    
                    return nw;
                    
                }
                
            }
            
        }

        if ((it instanceof Scene)
            ||
            (it instanceof OutlineItem)
           )
        {
            
            for (StructureItemWrapper sw : this.structureItems)
            {
                
                if (it == sw.item)
                {
                    
                    return sw;
                    
                }
                
            }
            
        }

        return null;
        
    }
    
    public void showItem (ChapterItem it)
    {

        if (this.ep.getProjectViewer ().isDistractionFreeModeEnabled ())
        {
            
            this.ep.getProjectViewer ().showNotificationPopup ("Function unavailable",
                                                               "Sorry, you cannot view {Notes}, {Plot Outline Items} and {Scenes} while distraction free mode is enabled.<br /><br /><a href='help:full-screen-mode/distraction-free-mode'>Click here to find out why</a>",
                                                               5);

            return;            
            
        }    
    
        ItemWrapper w = this.getWrapper (it);
        
        if (w != null)
        {
            
            if (w.popup != null)
            {
                
                w.popup.removeFromParent ();
                
            }
            
            Rectangle r = null;
            
            int pos = w.item.getPosition ();
            
            try
            {
    
                r = this.editor.modelToView (pos);
    
            } catch (Exception e)
            {
    
                // BadLocationException!
                Environment.logError ("Unable to convert item: " +
                                      w.item +
                                      " at position: " +
                                      pos,
                                      e);
    
                UIUtils.showErrorMessage (this.ep,
                                          "Unable to display item.");
    
                return;
    
            }
            
            int y = r.y;
                        
            QPopup popup = null;
            
            try
            {
            
                popup = this.popupProvider.getViewPopup (w.item,
                                                         this.ep);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to get popup for item: " +
                                      w.item,
                                      e);
    
                UIUtils.showErrorMessage (this.ep,
                                          "Unable to display item.");
                
                return;
                
            }

            if (popup == null)
            {
                
                Environment.logError ("Unable to get popup for item: " +
                                      w.item +
                                      ", got null popup.");
    
                UIUtils.showErrorMessage (this.ep,
                                          "Unable to display item.");
                
                return;
                
            }
            
            JScrollPane scrollPane = this.ep.getScrollPane ();

            w.popup = popup;
    
            if (w.item instanceof Note)
            {
               
                Note n = (Note) w.item;
                
                if (n.isEditNeeded ())
                {
                    
                    // Try and show the note above the selected text. (So it doesn't obscure it)
                    y = y - popup.getPreferredSize ().height - 22;
                    
                    // Get where the selected text ends.
                    pos = n.getEndPosition ();
                                    
                    try
                    {
                                
                        if (y < scrollPane.getVerticalScrollBar ().getValue ())
                        {
                            
                            // We are going to potentially obscure it, show it below the first line.        
                            y = y + popup.getPreferredSize ().height + 22;
                            
                        }
            
                    } catch (Exception e)
                    {
            
                        // Just ignore.
            
                    }
                    
                    
                }
                
            }
    
            y = 22 + y - scrollPane.getVerticalScrollBar ().getValue ();
    
            // Adjust the bounds so that the form is fully visible.
            if ((y + popup.getPreferredSize ().height) > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ()))
            {
    
                y = y - 22 - popup.getPreferredSize ().height;
    
            }
    
            this.ep.showPopupAt (popup,
                                 new Point (this.getWidth () - 20,
                                            y),
                                 true);
            popup.setDraggable (this.ep);
            
        }

    }
        
    public void hideItem (ChapterItem it)
    {
        
        ItemWrapper w = this.getWrapper (it);
        
        if ((w != null)
            &&
            (w.popup != null)
           )
        {
            
            w.popup.removeFromParent ();
            
        }

    }
    
    public ImagePanel getImagePanel (ChapterItem it)
    {
        
        ItemWrapper w = this.getWrapper (it);
        
        if (w != null)
        {
            
            return w.imagePanel;
            
        }
        
        return null;
        
    }

    public void init ()
                      throws Exception
    {
        
        Chapter c = this.ep.getChapter ();
        
        this.removeAllItems ();

        this.setOutlineItems (c.getOutlineItems ());
                                         
        this.setNotes (c.getNotes ());

        this.setScenes (c.getScenes ());

        this.setEditPosition (c.getEditPosition ());
                
    }
    
    public AbstractEditorPanel getEditorPanel ()
    {
        
        return this.ep;
        
    }
    
    public ImagePanel getDragIcon ()
    {
        
        return this.dragIcon;
        
    }
        
    public void setEditPosition (int    p)
                                 throws Exception
    {
        
        if (p > 0)
        {
            
            this.editPosition = this.editor.getDocument ().createPosition (p);
            
            this.ep.getChapter ().setTextEditPosition (this.editPosition);
            
        } else {
            
            this.editPosition = null;
            
            this.ep.getChapter ().setTextEditPosition (null);
            
        }

        this.validate ();
        this.repaint ();
        
    }
    
    public void changedUpdate (DocumentEvent e)
    {

        this.repaint ();

    }

    public void insertUpdate (DocumentEvent e)
    {

        this.repaint ();

    }

    public void removeUpdate (DocumentEvent e)
    {

        this.repaint ();

    }

    private Set<ChapterItem> getItemsForPosition (int               p,
                                                  java.util.List<? extends ItemWrapper> items)
    {
        
        Set<ChapterItem> its = new TreeSet (new ChapterItemSorter ());

        int y = -1;
        
        try
        {
                        
            y = this.editor.modelToView (p).y;
                                                                   
        } catch (Exception e) {
            
            return null;
            
        }        

        int min = p - 1500;
        int max = p + 1500;
        
        for (ItemWrapper w : items)
        {
        
            int pp = w.item.getPosition ();
        
            if (pp < min || pp > max)
            {
                
                continue;
                
            }
        
            Rectangle r = null;

            try
            {

                r = editor.modelToView (pp);

            } catch (Exception e)
            {

                // BadLocationException!
                Environment.logError ("Unable to convert item: " +
                                      w.item +
                                      " at position: " +
                                      pp,
                                      e);

                continue;

            }
            
            if (r.y == y)
            {

                its.add (w.item);

            }

        }        
        
        return its;
        
    }
    
    private Set<ChapterItem> getStructureItemsForPosition (int p)
    {
        
        return this.getItemsForPosition (p,
                                         this.structureItems);

    }

    private Set<ChapterItem> getNotesForPosition (int p)
    {
    
        return this.getItemsForPosition (p,
                                         this.notes);

    }
    
    private boolean hasSceneItem (Set<ChapterItem> items)
    {
        
        for (ChapterItem it : items)
        {
            
            if (it instanceof Scene)
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
    
    private void paintStructureItems (Graphics g)
    {

        Insets insets = this.getInsets ();

        Iterator iter = this.structureItems.iterator ();

        while (iter.hasNext ())
        {

            StructureItemWrapper w = (StructureItemWrapper) iter.next ();

            Set<ChapterItem> items = this.getStructureItemsForPosition (w.item.getPosition ());

            if (items == null)
            {
                
                continue;
                
            }
            
            ChapterItem c = items.iterator ().next ();

            if (c != null)
            {
                
                if (w.item != c)
                {

                    w.imagePanel.setVisible (false);
                    
                    continue;
                    
                }
                
            }
                                    
            int indent = OUTLINE_ITEM_INDENT;
            
            if (w.item instanceof Scene)
            {
                
                indent = SCENE_INDENT;
                
            }
            
            w.imagePanel.setVisible (true);
            
            w.imagePanel.setBounds (indent + insets.left,
                                    this.getYPosition (w.item.getPosition (),
                                                       g),
                                    w.imagePanel.getPreferredSize ().width,
                                    w.imagePanel.getPreferredSize ().height);
       
        }

    }

    private void paintNotes (Graphics g)
    {

        Insets insets = this.getInsets ();
    
        Iterator iter = this.notes.iterator ();

        while (iter.hasNext ())
        {

            NoteWrapper w = (NoteWrapper) iter.next ();

            Set<ChapterItem> items = this.getNotesForPosition (w.item.getPosition ());

            if (items == null)
            {
                
                continue;
                
            }
            
            ChapterItem c = items.iterator ().next ();

            if (c != null)
            {
                
                if (w.item != c)
                {

                    w.imagePanel.setVisible (false);
                    
                    continue;
                    
                }
                
            }

            w.imagePanel.setVisible (true);

            w.imagePanel.setBounds (NOTE_INDENT + insets.left,
                                    this.getYPosition (w.item.getPosition (),
                                                       g),
                                    w.imagePanel.getPreferredSize ().width,
                                    w.imagePanel.getPreferredSize ().height);

        }

    }
    
    public Point getLocation (ChapterItem item)
    {
        
        return new Point (this.getIconIndent (item),
                          this.getYPosition (item.getPosition (),
                                             null));
        
    }
    
    public int getIconIndent (ChapterItem item)
    {
        
        if (item instanceof Note)
        {
            
            return NOTE_INDENT;
            
        }
        
        if (item instanceof OutlineItem)
        {
            
            return OUTLINE_ITEM_INDENT;
            
        }

        if (item instanceof Scene)
        {
            
            return SCENE_INDENT;
            
        }
        
        return 0;
        
    }
    
    public int getYPosition (int      p,
                             Graphics g)
    {
        
        try
        {
                        
            Rectangle r = this.editor.modelToView (p);
                                     
            if (g == null)
            {
                
                g = UIUtils.createThrowawayGraphicsInstance ();
                
            }
            
            FontMetrics fm = g.getFontMetrics (this.ep.getEditor ().getFontForStyles ());
                                
            return r.y + (int) ((fm.getHeight () - 16) / 2);

        } catch (Exception e) {
            
            Environment.logError ("Unable to get y position for: " +
                                  p,
                                  e);
            
        }
            
        return -1;
        
    }
    
    private void paintEditMarker (Graphics g)
    {
        
        if (this.editPosition == null)
        {
                    
            return;
            
        }
        
        if (!UserProperties.getAsBoolean (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME))
        {
            
            return;
            
        }
        
        int w = this.getSize ().width;
        
        Color c = UIUtils.getColor (UserProperties.get (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME));
        
        try
        {
        
            Rectangle r = this.editor.modelToView (this.editPosition.getOffset ());

            FontMetrics fm = g.getFontMetrics (this.ep.getEditor ().getFontForStyles ());
  
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor (c);
            g2.setStroke (this.editMarkStroke);
            g2.drawLine (w - EDIT_MARK_STROKE_WIDTH,
                         this.editor.modelToView (0).y,
                         w - EDIT_MARK_STROKE_WIDTH,
                         r.y + (int) (fm.getHeight ()));
                                                                                                             
        }catch (Exception e) {
            
        }
        
    }
    
    protected void paintChildren (Graphics g)
    {

        this.paintStructureItems (g);
    
        this.paintNotes (g);

        this.paintEditMarker (g);
        
        super.paintChildren (g);

    }

    public void removeAllItems ()
    {

        java.util.List<StructureItemWrapper> o = new ArrayList (this.structureItems);

        for (StructureItemWrapper i : o)
        {

            this.removeItem (i.item);

        }

        java.util.List<NoteWrapper> n = new ArrayList (this.notes);

        for (NoteWrapper i : n)
        {

            this.removeItem (i.item);

        }

    }

    public void removeItems (java.util.List<? extends ChapterItem> items)
    {

        for (ChapterItem i : items)
        {

            this.removeItem (i);

        }

    }

    public void removeItem (ChapterItem item)
    {
    
        if ((item instanceof OutlineItem)
            ||
            (item instanceof Scene)
           )
        {

            StructureItemWrapper w = null;

            for (int i = 0; i < this.structureItems.size (); i++)
            {

                w = this.structureItems.get (i);

                if (w.item == item)
                {

                    this.structureItems.remove (w);
        
                    this.remove (w.imagePanel);
        
                    w.item.setTextPosition (null);
                    w.item.setEndTextPosition (null);

                    break;

                }

            }
                        
        }

        if (item instanceof Note)
        {

            Note n = (Note) item;

            NoteWrapper w = null;

            for (int i = 0; i < this.notes.size (); i++)
            {

                w = this.notes.get (i);

                if (w.item == n)
                {

                    this.notes.remove (w);
        
                    this.remove (w.imagePanel);
        
                    w.item.setTextPosition (null);
                    w.item.setEndTextPosition (null);

                    break;

                }

            }
            
        }

        this.validate ();
        this.repaint ();

    }

    public void setOutlineItems (java.util.Set<OutlineItem>  items)
                          throws GeneralException
    {

        for (OutlineItem it : items)
        {

            this.addItem (it);

        }

    }

    public void setScenes (java.util.Set<Scene>  items)
                    throws GeneralException
    {
    
        for (Scene s : items)
        {

            this.addItem (s);

            this.setOutlineItems (s.getOutlineItems ());

        }

    }

    public void setNotes (java.util.Set<Note>  items)
                   throws GeneralException
    {

        for (Note n : items)
        {

            this.addItem (n);

        }

    }

    public ImagePanel addItem (ChapterItem      c)
                        throws GeneralException
    {

        if (c instanceof OutlineItem)
        {

            return this.addItem ((OutlineItem) c);

        }

        if (c instanceof Scene)
        {

            return this.addItem ((Scene) c);

        }

        if (c instanceof Note)
        {

            return this.addItem ((Note) c);

        }

        throw new GeneralException ("Type: " +
                                    c +
                                    " not supported.");

    }

    private void setMovable (ChapterItem item)
    {
        
        if (this.itemMoveAllowed)
        {
        
            new ChapterItemMoveMouseHandler (item,
                                             this);                

        }
        
    }
    
    private ImagePanel addStructureItem (final ChapterItem item)
                                  throws GeneralException
    {

        final IconColumn _this = this;
    
        // Placeholder
        Image      img = null;
        ImagePanel p = new ImagePanel (img,
                                       Environment.getTransparentImage ());

        UIUtils.setAsButton (p);
        final StructureItemWrapper w = new StructureItemWrapper ();
        w.imagePanel = p;

        w.imagePanel.setImage (this.iconProv.getIcon (item,
                                                      Constants.ICON_COLUMN).getImage ());            
        
        w.item = item;
        this.add (p);

        try
        {

            Position pos = this.editor.getDocument ().createPosition (item.getPosition ());

            item.setTextPosition (pos);

            this.structureItems.add (w);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add item: " +
                                        item +
                                        ", document position: " +
                                        item.getPosition () +
                                        " is not valid.",
                                        e);

        }
        
        this.setMovable (item);
                
        p.addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                _this.showItem (w.item);
                
            }
            
        });

        this.repaint ();

        return p;
        
    }
    
    public ImagePanel addItem (final OutlineItem      item)
                        throws GeneralException
    {

        return this.addStructureItem (item);

    }

    public ImagePanel addItem (final Scene            item)
                        throws GeneralException
    {

        return this.addStructureItem (item);

    }

    public ImagePanel addItem (final Note             n)
                        throws GeneralException
    {

        final IconColumn _this = this;
    
        // Placeholder
        Image      img = null;
        ImagePanel p = new ImagePanel (img,
                                       Environment.getTransparentImage ());
        UIUtils.setAsButton (p);
        final NoteWrapper w = new NoteWrapper ();
        w.imagePanel = p;
        w.imagePanel.setImage (this.iconProv.getIcon (n,
                                                      Constants.ICON_COLUMN).getImage ());            
        
        w.item = n;

        this.add (p);

        try
        {

            Position pos = this.editor.getDocument ().createPosition (n.getPosition ());

            n.setTextPosition (pos);

            if (n.getEndPosition () > -1)
            {

                n.setEndTextPosition (this.editor.getDocument ().createPosition (n.getEndPosition ()));

            }

            this.notes.add (w);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add note: " +
                                        n,
                                        e);

        }
        
        this.setMovable (n);

        p.addMouseListener (new MouseAdapter ()
        {
                
            public void mouseClicked (MouseEvent ev)
            {

                if (ev.isPopupTrigger ())
                {
                    
                    return;
                    
                }

                _this.showItem (w.item);
                    
            }

        });
                
        this.repaint ();

        return p;

    }

}
