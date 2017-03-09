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
    private BufferedImage origIm = null;
    private JFileChooser chooser = null;
    private java.util.List<ChangeListener> listeners = new ArrayList ();
    
    public ImageSelector (BufferedImage           image,
                          FileNameExtensionFilter filter,
                          Dimension               size)
    {
                
        this.size = size;
        
        this.init (filter);
        
        this.im = image;
        this.origIm = image;
        
        this.updateImage ();
        
    }
    
    public ImageSelector (BufferedImage          image,
                          java.util.List<String> fileTypes,
                          Dimension              size)
    {
        
        this ((File) null,
              fileTypes,
              size);
        
        this.im = image;
        this.origIm = image;
                        
        this.updateImage ();
        
    }

    public ImageSelector (File                   f,
                          java.util.List<String> fileTypes,
                          Dimension              size)
    {
        
        this.file = f;
        this.origFile = f;
        this.size = size;
                
        FileNameExtensionFilter fil = null;
        
        if (fileTypes != null)
        {
            
            StringBuilder b = new StringBuilder ();
            
            for (String t : fileTypes)
            {
                
                if (b.length () > 0)
                {
                    
                    b.append (", ");
                    
                }
                
                b.append (t.toLowerCase ());
                
            }
            
            fil = new FileNameExtensionFilter (b.toString (),
                                               (String[]) fileTypes.toArray (new String[0]));

        }
                                               
        this.init (fil);
        
        this.setFile (f);        
                
    }
    
    public ImageSelector (File                    f,
                          FileNameExtensionFilter filter,
                          Dimension               size)
    {
        
        this.file = f;
        
        this.size = size;
        
        this.init (filter);
        
        this.setFile (f);
        
    }
    
    private void init (FileNameExtensionFilter filter)
    {
        
        this.setToolTipText ("Click to find an image file");
        
        UIUtils.setAsButton (this);
                
        this.chooser = new JFileChooser ();
        this.chooser.setMultiSelectionEnabled (false);
        this.chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        
        this.setMinimumSize (this.size);
        this.setPreferredSize (this.size);
        
        if (filter != null)
        {
        
            this.chooser.setFileFilter (filter);
            
        } else {
            
            this.chooser.setFileFilter (UIUtils.imageFileFilter);
            
        }
        
        final ImageSelector _this = this;
        
        this.addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
                
                if ((_this.file != null)
                    ||
                    (_this.origFile != null)
                   )
                {
                    
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
                                                            
                                                            if (_this.origFile != null)
                                                            {
                                                            
                                                                _this.setFile (_this.origFile);
                                                                
                                                            }
                                                            
                                                            if (_this.origIm != null)
                                                            {
                                                                
                                                                _this.im = _this.origIm;
                                                                
                                                                _this.updateImage ();
                                                                
                                                            }
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);                        
                                                 
                    }
                    
                }

            }
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                                
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
        
    public BufferedImage getImage ()
    {
        
        return (this.im != null ? this.im : this.origIm);        
        
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
        
            if (i.getWidth () != this.size.width)
            {
        
                this.setIcon (new ImageIcon (UIUtils.getScaledImage (i,
                                                                      this.size.width)));
                
            } else {
                
                this.setIcon (new ImageIcon (i));
                            
            }

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