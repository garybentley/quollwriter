package com.quollwriter.ui;

import java.awt.Point;
import java.awt.event.*;
import java.awt.datatransfer.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.data.*;

public class ChapterItemTransferHandler extends TransferHandler
{

    private IconColumn ic = null;
    private ChapterItemMoveHandler moveHandler = null;

    public ChapterItemTransferHandler (IconColumn ic)
    {
        
        this.ic = ic;
        
        this.moveHandler = new ChapterItemMoveHandler (this.ic);
        
    }
    
    public void setItem (ChapterItem item)
    {
                
        this.moveHandler.setItem (item);
        
    }
        
    public Icon getVisualRepresentation (Transferable t)
    {
        
        // Never called... bug in java
        return Environment.getObjectIcon (this.moveHandler.getItem (),
                                          Constants.ICON_COLUMN);
        
    }
    
    public int getSourceActions (JComponent comp)
    {
        
        return TransferHandler.COPY_OR_MOVE;
        
    }
    
    public void exportDone (JComponent   comp,
                            Transferable t,
                            int          action)
    {
        
        this.moveHandler.dragFinished ();
        
    }
    
    public Transferable createTransferable (JComponent comp)
    {
                    
        ChapterItem item = this.moveHandler.getItem ();
                    
        return new javax.activation.DataHandler (item, "object/" + item.getObjectType ());
        
    }
    
    public boolean importData (TransferHandler.TransferSupport supp)
    {
                    
        return true;
        
    }
    
    public void exportAsDrag (JComponent comp,
                              InputEvent ev,
                              int        action)
    {
        
        super.exportAsDrag (comp,
                            ev,
                            action);
                                    
        this.moveHandler.startDrag ();

        try
        {

            Point xp = this.ic.getImagePanel (this.moveHandler.getItem ()).getBounds ().getLocation ();
            
            SwingUtilities.convertPointToScreen (xp,
                                                 this.ic);

            new java.awt.Robot ().mouseMove (xp.x,
                                             xp.y);
            
        } catch (Exception e) {}

    }
    
    public boolean canImport (JComponent   comp,
                              DataFlavor[] transferFlavors)
    {

        this.moveHandler.doDrag ();
        
        return true;
                            
    }
    
}
