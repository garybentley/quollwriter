package com.quollwriter.ui;

import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.Header;

public class ImportTransferHandler extends TransferHandler 
{
    
    private Timer timer = null;
    private ActionListener onFileDraggedOver = null;
    private ActionListener onDragExit = null;
    private ActionListener onFileDropped = null;
    private File file = null;
    private FileFilter filter = null;
            
    public ImportTransferHandler (ActionListener onFileDraggedOver,
                                  ActionListener onFileDropped,
                                  ActionListener onDragExit,
                                  FileFilter     filter)
    {
        
        this.onFileDraggedOver = onFileDraggedOver;
        this.onDragExit = onDragExit;
        this.onFileDropped = onFileDropped;
        this.filter = filter;
        
        final ImportTransferHandler _this = this;
    
        if (this.timer == null)
        {
            
            this.timer = new javax.swing.Timer (250,
                                                new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                    
                    if (_this.onDragExit != null)
                    {
                        
                        _this.file = null;
                        _this.onDragExit.actionPerformed (new ActionEvent (_this, 1, "exited"));
                        
                    }
                                        
                }
                
            });
            
            this.timer.setRepeats (false);
            
        }

    }
    
    private File getFile (TransferHandler.TransferSupport s)
    {
        
        try
        {
        
            java.util.List<File> l = (java.util.List<File>) s.getTransferable ().getTransferData (DataFlavor.javaFileListFlavor);
            
            if (l.size () == 1)
            {
                
                return l.get (0);
            
            }

        } catch (Exception e) {
            
            
        }
        
        return null;
        
    }
                
    public boolean canImport (TransferHandler.TransferSupport s)
    {

        this.file = null;
    
        if (!s.isDataFlavorSupported (DataFlavor.javaFileListFlavor))
        {
                                    
            return false;
            
        }
        
        try
        {
                        
            java.util.List l = (java.util.List) s.getTransferable ().getTransferData (DataFlavor.javaFileListFlavor);
            
            if (l.size () > 1)
            {
                                      
                return false;
                
            } else {
                
                File f = (File) l.get (0);

                if (!this.filter.accept (f))
                {
                
                    return false;
                
                } else {
                    
                    this.timer.stop ();
                                    
                    if (this.onFileDraggedOver != null)
                    {
                        
                        this.onFileDraggedOver.actionPerformed (new ActionEvent (f, 1, "draggedOver"));
                                                
                    }
                    
                    this.timer.start ();
                    
                }
                
            }
            
        } catch (Exception e) {
            
        }
        
        s.setDropAction (COPY);
        
        return true;
        
    }
    
    public boolean importData (TransferHandler.TransferSupport s)
    {
        
        this.timer.stop ();
        
        if (!this.canImport (s))
        {
            
            this.timer.stop ();

            return false;
            
        }
        
        this.file = this.getFile (s);
        
        if (this.onFileDropped != null)
        {
            
            this.onFileDropped.actionPerformed (new ActionEvent (this.file, 1, "filedropped"));
            
        }
                
        return true;
        
    }
    
}
