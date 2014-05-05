package com.quollwriter.ui;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;

public class ImageSelector extends JLabel
{

    private File file = null;
    private File origFile = null;
    private Dimension size = null;
    private BufferedImage im = null;
    private JFileChooser chooser = null;
    private java.util.List<ChangeListener> listeners = new ArrayList ();
    
    public ImageSelector (File         f,
                          java.util.List<String> fileTypes,
                          Dimension    size)
    {
        
        this.file = f;
        this.origFile = f;
        this.size = size;
        
        this.setFile (f);
        
        this.setToolTipText ("Click to find an image file");
        
        UIUtils.setAsButton (this);
        
        StringBuilder b = new StringBuilder ();
        
        for (String t : fileTypes)
        {
            
            if (b.length () > 0)
            {
                
                b.append (", ");
                
            }
            
            b.append (t.toLowerCase ());
            
        }
        
        this.chooser = new JFileChooser ();
        this.chooser.setMultiSelectionEnabled (false);
        this.chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fil = new FileNameExtensionFilter (b.toString (),
                                                                   (String[]) fileTypes.toArray (new String[0]));
        this.chooser.addChoosableFileFilter (fil);
        
        final ImageSelector _this = this;
        
        this.addMouseListener (new MouseEventHandler ()
        {
           
            public void handlePress (MouseEvent ev)
            {
                
                if ((ev.isPopupTrigger ())
                    &&
                    ((_this.file != null)
                     ||
                     (_this.origFile != null)
                    )
                   )
                {
                    
                    JPopupMenu m = new JPopupMenu ();

                    JMenuItem mi = null;

                    if (_this.file != null)
                    {
                        
                        mi = UIUtils.createMenuItem ("Remove",
                                                     Constants.DELETE_ICON_NAME,
                                                     new ActionAdapter ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            _this.setFile (null);
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);

                        mi = UIUtils.createMenuItem ("View full size",
                                                     Constants.VIEW_ICON_NAME,
                                                     new ActionAdapter ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            try
                                                            {
                                                            
                                                                Desktop.getDesktop ().open (_this.file);
                                                                
                                                            } catch (Exception e) {
                                                                
                                                                UIUtils.showErrorMessage (_this,
                                                                                          "Unable to show file");
                                                                
                                                                Environment.logError ("Unable to show file: " +
                                                                                      _this.file,
                                                                                      e);
                                                                
                                                            }
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);
                                                         
                        mi = UIUtils.createMenuItem ("Show in folder",
                                                     Constants.VIEW_ICON_NAME,
                                                     new ActionAdapter ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            try
                                                            {
                                                            
                                                                Desktop.getDesktop ().open (_this.file.getParentFile ());
                                                                
                                                            } catch (Exception e) {
                                                                
                                                                UIUtils.showErrorMessage (_this,
                                                                                          "Unable to show file in folder");
                                                                
                                                                Environment.logError ("Unable to show file in folder: " +
                                                                                      _this.file,
                                                                                      e);
                                                                
                                                            }
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);

                    }
                                       
                    if (_this.origFile != null)
                    {
                        
                        mi = UIUtils.createMenuItem ("Restore to the default",
                                                     Constants.UNDO_ICON_NAME,
                                                     new ActionAdapter ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            _this.setFile (_this.origFile);
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);                        
                                                 
                    }
                    
                    m.show (_this,
                            10,
                            10);
                    
                    return;
                    
                }
                
                if (_this.file != null)
                {
                    
                    _this.chooser.setSelectedFile (_this.file);
                    
                }
                
                if (_this.chooser.showOpenDialog (_this) == JFileChooser.APPROVE_OPTION)
                {
                    
                    _this.setFile (_this.chooser.getSelectedFile ());

                }
                
            }
            
        });
                
    }
        
    protected void fireFileChangedEvent ()
    {
        
        ChangeEvent ev = new ChangeEvent (this);
        
        for (ChangeListener l : this.listeners)
        {
            
            l.stateChanged (ev);
            
        }
        
    }
    
    public void addChangeListener (ChangeListener c)
    {
        
        this.listeners.add (c);
        
    }
    
    public File getFile ()
    {
        
        return this.file;
        
    }
    
    private void updateImage ()
    {
        
        BufferedImage i = this.im;
        
        if (i == null)
        {
                    
            i = Environment.getImage (Constants.DEFAULT_FIND_IMAGE_FILE);
            
        }
        
        if (this.size == null)
        {
            
            this.setIcon (new ImageIcon (i));
            
        } else {
        
            this.setIcon (new ImageIcon (UIUtils.getScaledImage (i,
                                                                  this.size.width)));
                                                                  //this.size.height)));

        }
        
    }
    
    public void setImageSize (Dimension d)
    {
        
        this.size = d;

        this.updateImage ();

    }
    
    public void setFile (File f)
    {
        
        BufferedImage i = null;
                    
        if ((f != null)
            &&
            (f.exists ())
           )
        {
            
            i = UIUtils.getImage (f);
            
            this.im = i;
            
        } else {
            
            this.im = null;
            
        }
        
        this.file = f;
        
        this.updateImage ();
        
        this.fireFileChangedEvent ();
        
    }
    
}