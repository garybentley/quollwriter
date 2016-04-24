package com.quollwriter.ui;

import java.util.*;

import java.awt.Cursor;
import java.awt.Color;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Image;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ImagePanel;

public class ChapterItemMoveHandler 
{
    
    private boolean dragging = false;
    private int fontHeight = 0;
    private int lastY = -1;

    protected ChapterItem item = null;
    protected IconColumn iconColumn = null;
    protected ImagePanel dragIcon = null;
    protected AbstractEditorPanel ep = null;
    protected QTextEditor editor = null;
    
    public ChapterItemMoveHandler (IconColumn ic)
    {

        this.iconColumn = ic;
    
        this.dragIcon = this.iconColumn.getDragIcon ();
        
        this.editor = ic.getEditorPanel ().getEditor ();
        this.ep = ic.getEditorPanel ();        
    
    }        

    public ChapterItem getItem ()
    {
        
        return this.item;
        
    }
    
    public void setItem (ChapterItem item)
    {
        
        this.item = item;        
                    
    }
    
    public void dragFinished ()
    {
                       
        this.fontHeight = 0;
        this.lastY = -1;
        
        ((ChapterItemViewer) this.ep).removeItemHighlightTextFromEditor (this.item);        
        
        this.iconColumn.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
        this.dragIcon.setVisible (false);
        this.dragIcon.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));

        Point p = SwingUtilities.convertPoint (this.ep,
                                               this.ep.getMousePosition (),
                                               this.iconColumn);
        
        if (p == null)
        {
            
            return;
            
        }
        
        p.x = this.iconColumn.getIconIndent (this.item);

        int v = this.editor.viewToModel (p);
        
        p.y = this.iconColumn.getYPosition (v,
                                            this.ep.getGraphics ());
        
        this.dragIcon.setBounds (p.x,
                                 p.y,
                                 this.dragIcon.getPreferredSize ().width,
                                 this.dragIcon.getPreferredSize ().height);

        // Set the new position in the note.
        try
        {

            int newEnd = v + (this.item.getEndPosition () - this.item.getStartPosition ());
                    
            Position pos = this.editor.getDocument ().createPosition (v);

            this.item.setTextPosition (pos);

            if (this.item.getEndPosition () > -1)
            {

                this.item.setEndTextPosition (this.editor.getDocument ().createPosition (newEnd));

            }

            Chapter c = this.item.getChapter ();
            
            c.reindex ();
            
            if ((this.item instanceof Scene)
                ||
                (this.item instanceof OutlineItem)
               )
            {
            
                // Reorder all the outline items.
                for (Scene ss : c.getScenes ())
                {
            
                    for (OutlineItem i : ss.getOutlineItems ())
                    {

                        Scene s = c.getLastScene (i.getPosition ());

                        if (s != null)
                        {
            
                            s.addOutlineItem (i);
            
                        } else {
                            
                            i.setScene (null);
                            
                            c.addOutlineItem (i);
                            
                        }
                        
                    }
                                
                }

                for (OutlineItem i : c.getOutlineItems ())
                {

                    Scene s = c.getLastScene (i.getPosition ());

                    if (s != null)
                    {
        
                        s.addOutlineItem (i);
        
                    }
                    
                }
                
                if (this.item instanceof OutlineItem)
                {
                    
                    OutlineItem it = (OutlineItem) this.item;
                    
                    Scene s = c.getLastScene (it.getPosition ());
        
                    if (s != null)
                    {
        
                        s.addOutlineItem (it);
        
                    } else {
                        
                        it.setScene (null);
                        
                        c.addOutlineItem (it);
                        
                    }
    
                }

            }
            
            // Save the chapter, this deals with side-effects.
            this.ep.getViewer ().saveObject (c,
                                             true);

            this.iconColumn.repaint ();

            this.ep.getViewer ().reloadTreeForObjectType (Chapter.OBJECT_TYPE);            
            
        } catch (Exception e)
        {

            UIUtils.showErrorMessage (this.ep.getViewer (),
                                      "Unable to move item");
            
            Environment.logError ("Unable to move item: " +
                                  this.item,
                                  e);

        }
                                  
    }

    public void doDrag ()
    {

        Point p = this.ep.getMousePosition ();
        
        if (p == null)
        {
            
            return;
            
        }
      
        int top = this.ep.getInsets ().top;
                                
        this.iconColumn.hideItem (this.item);
        
        this.dragIcon.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));
        this.iconColumn.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));
        
        p.x = this.iconColumn.getIconIndent (this.item);
                        
        if (p.y < (top + 5))
        {
            
            p.y = (top + 5);
            
        }

        if (p.y > (top + this.lastY))
        {

            p.y = this.lastY + top;
            
        }
     
        if (p.y > (this.ep.getBounds ().height + top) - 5)
        {

            p.y = (this.ep.getBounds ().height + top) - 5;
            
        }

        Point pp = SwingUtilities.convertPoint (this.ep,
                                                p,
                                                this.iconColumn);

        pp.x = p.x;
                                
        Point ip = SwingUtilities.convertPoint (this.iconColumn,
                                                this.dragIcon.getBounds ().getLocation (),
                                                this.ep);
                
        if (ip.y > (this.ep.getBounds ().height + top) - 56)
        {
            
            this.ep.incrementScrollPositionBy (this.fontHeight);
            
        } 

        if (ip.y < (top + 40))
        {
            
            this.ep.incrementScrollPositionBy (-1 * (this.fontHeight));
                                
        }

        this.ep.showPopupAt (this.dragIcon,
                             pp,
                             false);

        SwingUtilities.convertPointToScreen (pp,
                                             this.iconColumn);
                         
        // Move the mouse to the current point, this helps prevent "jumping"
        // when scrolling upwards.
        try
        {


            new Robot ().mouseMove (pp.x,
                                    pp.y);
            
        } catch (Exception e) {}
        
    }

    public void startDrag ()
    {

        Color c = UIUtils.getDragIconColor ();
        
        String id = this.item.getObjectType () + UIUtils.colorToHex (c);
        
        Image image = this.iconColumn.getIconProvider ().getIcon (this.item,
                                                                  Constants.ICON_COLUMN).getImage ();

        ImageIcon im = UIUtils.getColoredIcon (image,
                                               c);
            
        this.dragIcon.setIcon (im);
        
        this.fontHeight = this.ep.getGraphics ().getFontMetrics (this.editor.getFontForStyles ()).getHeight ();

        if (this.item.getEndPosition () > this.item.getStartPosition ())
        {
            
            // Add a highlight.
            ((ChapterItemViewer) this.ep).highlightItemTextInEditor (this.item);
            
        }
        
        try
        {
            
            this.lastY = this.iconColumn.getYPosition (this.editor.getText ().length (),
                                                       this.ep.getGraphics ());

        } catch (Exception e) {

        }
        
    }
        
}