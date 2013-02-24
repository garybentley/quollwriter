package com.quollwriter.ui;

import java.awt.Dimension;
import java.awt.Component;

import java.io.File;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.events.*;

public class FileFinder extends Box
{
    
    private JTextField text = null;
    private JFileChooser chooser = null;
    private File file = null;
    private ActionListener onSelect = null;
    private ActionListener onCancel = null;
    private JButton cancelButton = null;
    private FileFilter fileFilter = null;
    
    private String finderTitle = null;
    private int finderSelectionMode = -1;
    private String approveButtonText = null;
    private String findButtonToolTip = null;
    private boolean allowUserEntry = false;
    private boolean clearOnCancel = false;
    
    public FileFinder ()
    {

        super (BoxLayout.X_AXIS);

    }

    public void setClearOnCancel (boolean v)
    {
        
        this.clearOnCancel = v;
        
    }
    
    public void setFileFilter (FileFilter f)
    {
        
        this.fileFilter = f;
        
    }
    
    public void setAllowUserEntry (boolean v)
    {
        
        this.allowUserEntry = v;
        
    }
    
    public void showCancel (boolean        v,
                            ActionListener onCancel)
    {

        this.cancelButton = UIUtils.createButton ("cancel",
                                                  Constants.ICON_MENU,
                                                  "Click to cancel.",
                                                  null);
                
        this.onCancel = onCancel;
        
    }
    
    public void setFindButtonToolTip (String v)
    {
        
        this.findButtonToolTip = v;
        
    }
    
    public void setOnSelectHandler (ActionListener l)
    {
        
        this.onSelect = l;
        
    }
    
    public void setApproveButtonText (String v)
    {
        
        this.approveButtonText = v;
        
    }
    
    public void setFinderSelectionMode (int v)
    {
        
        this.finderSelectionMode = v;
        
    }
    
    public void setFileChooser (JFileChooser f)
    {
        
        this.chooser = f;
        
    }
    
    public void setFile (File f)
    {
        
        this.file = f;
        
        if (this.text != null)
        {
            
            this.text.setText (f.getPath ());
            
        }
        
        if (this.chooser != null)
        {
            
            this.chooser.setSelectedFile (f);
            
        }
        
    }
    
    public void setFinderTitle (String t)
    {
        
        this.finderTitle = t;
        
    }
    
    public void init ()
    {
    
        final FileFinder _this = this;
    
        final JTextField text = UIUtils.createTextField ();
        
        this.text = text;
        
        if (this.file != null)
        {
        
            this.text.setText (this.file.getPath ());
            
        }
        
        this.text.setEditable (this.allowUserEntry);
        this.text.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.text.setPreferredSize (new Dimension (350,
                                                   this.text.getPreferredSize ().height));
        this.text.setMaximumSize (new Dimension (350,
                                                 this.text.getPreferredSize ().height));

        this.setAlignmentX (Component.LEFT_ALIGNMENT);                                              
                                              
        this.add (text);
        this.add (Box.createHorizontalStrut (2));

        final JButton dbut = UIUtils.createButton ("find",
                                                   Constants.ICON_MENU,
                                                   (this.findButtonToolTip != null ? this.findButtonToolTip : "Click to find the file/directory."),
                                                   null);
        
        this.add (dbut);

        if (this.chooser == null)
        {
            
            this.chooser = new JFileChooser ();
            this.chooser.setDialogTitle (this.finderTitle);
            this.chooser.setFileSelectionMode (this.finderSelectionMode);
            this.chooser.setApproveButtonText (this.approveButtonText);
            this.chooser.setCurrentDirectory (new File (this.text.getText ()));
            
        }
        
        final ActionListener aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                // Need to run: attrib -r "%USERPROFILE%\My Documents" on XP to allow a new directory
                // to be created in My Documents.

                if (_this.fileFilter != null)
                {
                    
                    _this.chooser.setFileFilter (_this.fileFilter);
                    
                }
                
                if (_this.chooser.showOpenDialog (text) == JFileChooser.APPROVE_OPTION)
                {

                    if (_this.onSelect != null)
                    {
                        
                        VetoableActionEvent ac = new VetoableActionEvent (_this,
                                                                          0,
                                                                          "fileSelected");
                        
                        _this.onSelect.actionPerformed (ac);
                        
                        if (ac.isCancelled ())
                        {
                            
                            return;
                            
                        }
                        
                    }
                
                    _this.text.setText (_this.chooser.getSelectedFile ().getPath ());
                
                }

            }

        };

        dbut.addActionListener (aa);
        
        this.text.addMouseListener (new MouseAdapter ()
        {
           
            public void mousePressed (MouseEvent ev)
            {
                
                aa.actionPerformed (new ActionEvent (text,
                                                     0,
                                                     "mousepressed"));
                
            }
            
        });
            
        if (this.cancelButton != null)
        {
            
            this.add (Box.createHorizontalStrut (2));
        
            this.add (this.cancelButton);    
                
            if (this.onCancel != null)
            {
                
                this.cancelButton.addActionListener (this.onCancel);                
                
                if (this.clearOnCancel)
                {
                    
                    this.cancelButton.addActionListener (new ActionAdapter ()
                    {
                       
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            _this.chooser.setSelectedFile (null);
                            _this.text.setText ("");
                            
                        }
                        
                    });
                    
                }
                
            }
            
        }
    }

    public File getSelectedFile ()
    {
        
        File f = this.chooser.getSelectedFile ();
        
        if (f != null)
        {
            
            return f;
            
        }
        
        return this.file;
        
    }
     
}