package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.io.*;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TimerTask;
import java.util.ArrayList;

import javax.swing.text.*;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ScrollablePanel;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.VerticalLayout;

public class ObjectDocumentsEditPanel extends EditPanel 
{

    private AbstractProjectViewer viewer = null;
    private NamedObject obj = null;
    private JComponent files = null;
    private JScrollPane sp = null;
        
    public ObjectDocumentsEditPanel (AbstractProjectViewer viewer,
                                     NamedObject           obj)
    {
        
        super (true);
        
        this.obj = obj;
        this.viewer = viewer;
                
        this.files = new ScrollableBox (BoxLayout.Y_AXIS);//new JPanel (null);
        //l.setDragEnabled (true);
        //l.setDropMode (DropMode.ON);
        /*
        this.files.setTransferHandler (new TransferHandler ()
        {

            public boolean canImport (TransferHandler.TransferSupport s)
            {
                
                return true;
                
            }
            
            public boolean importData (TransferHandler.TransferSupport s)
            {

                return true;
            
            }
            
            
        });
        */

        this.files.setOpaque (false);
                
    }

    public void refreshViewPanel ()
    {

    }

    public NamedObject getForObject ()
    {
        
        return this.obj;
        
    }
    
    public boolean handleSave ()
    {

        return true;

    }

    public List<FormItem> getEditItems ()
    {

        return null;

    }

    public List<FormItem> getViewItems ()
    {

        return null;

    }

    public boolean handleCancel ()
    {

        return true;

    }

    public void handleEditStart ()
    {


    }

    public IconProvider getIconProvider ()
    {

        DefaultIconProvider iconProv = new DefaultIconProvider ()
        {
          
            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {
                
                if (name.equals ("header"))
                {
                    
                    name = "documents";
                    
                }
                
                return super.getIcon (name,
                                      type);
                
            }
            
        };

        return iconProv;

    }

    public String getHelpText ()
    {

        return null;

    }

    public String getTitle ()
    {

        return "{Documents}";

    }

    public JComponent getEditPanel ()
    {

        return null;

    }
        
    public void showAddDocument ()
    {
        
        JFileChooser fc = new JFileChooser ();
        fc.setDialogTitle ("Select a File");
        fc.setApproveButtonText ("Select");
        
        if (fc.showOpenDialog (this) == JFileChooser.APPROVE_OPTION)
        {
            
            File f = fc.getSelectedFile ();
                                                                                                                                
            this.addFile (f,
                          false);
                                                                                                 
            
        }
        
    }
    public JComponent getViewPanel ()
    {

        final ObjectDocumentsEditPanel _this = this;

        java.util.List<JComponent> buttons = new ArrayList ();
            
        JButton but = UIUtils.createButton (Environment.getIcon (Constants.ADD_ICON_NAME,
                                                          Constants.ICON_PANEL_SECTION_ACTION),
                                               "Click to add a new {document}",
                                               new ActionAdapter ()
                                               {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.showAddDocument ();
                                                        
                                                    }
                                                
                                               });

        buttons.add (but);
        
        this.header.setControls (UIUtils.createButtonBar (buttons));
                
        this.sp = UIUtils.createScrollPane (this.files);
                        
        this.sp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        //this.sp.setPreferredSize (new Dimension (250, 300));
        
        //this.sp.setPreferredSize (new Dimension (250, 300));
        
        sp.getVerticalScrollBar ().setUnitIncrement (80);

        sp.setBorder (UIUtils.createPadding (0, 5, 5, 0));
        sp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                          Short.MAX_VALUE));
        sp.setMinimumSize (new Dimension (200,
                                          100));
        
        for (File f : this.obj.getFiles ())
        {
            
            this.addFile (f,
                          true);
            
        }
                
        return sp;
    
    }
    
    public void addFile (final File    f,
                         final boolean noCheck)
    {
        
        final ObjectDocumentsEditPanel _this = this;

        if (!noCheck)
        {
        
            if (this.obj.getFiles ().contains (f))
            {

                UIUtils.showMessage ((PopupsSupported) this.viewer,
                                     "Already have that {document}",
                                     String.format ("<b>%s</b> already has that {document} associated with it.",
                                                    this.obj.getName ()));
                
                return;
            
            }
        
            try
            {
            
                this.obj.addFile (f);
                
                this.viewer.saveObject (this.obj,
                                        false);
    
            } catch (Exception e) {
                
                this.obj.getFiles ().remove (f);
                
                Environment.logError ("Unable to add file to: " +
                                      this.obj,
                                      e);
                
                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to add file, please contact Quoll Writer support for assistance.");
                
                return;
                
            }

        }
        
        FileBox fb = new FileBox (f,
                                  this.obj,
                                  this.viewer,
                                  this);
                
        if (!noCheck)
        {                
        
            this.files.add (fb);
            
        } else {
            
            this.files.add (fb,
                            0);
            
        }
                                   
        this.validate ();
        this.repaint ();
        
    }
    
    private void removeFile (FileBox fb)
    {

        File f = fb.getFile ();
    
        if (this.obj.getFiles ().remove (f))
        {
         
            try
            {
                
                this.viewer.saveObject (this.obj,
                                        true);

            } catch (Exception e) {
                
                Environment.logError ("Unable to remove file: " +
                                      f +
                                      " from: " +
                                      this.obj,
                                      e);
                
                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to remove {document}, please contact Quoll Writer support for assistance.");
                
                return;
                
            }
                          
            this.files.remove (fb);
             
            Dimension s = this.files.getPreferredSize ();
                
            this.validate ();
            this.repaint ();                                                                
         
        } else {

             Environment.logError ("Can't remove file: " +
                                   f +
                                   " from: " +
                                   this.obj);
             
             UIUtils.showErrorMessage (this.viewer,
                                       "Unable to remove {document}, please contact Quoll Writer support for assistance.");
             
             return;                                                            
         
        }
        
    }
    
    public void close ()
    {
                
    }

    private class FileBox extends ScrollableBox
    {
        
        private File file = null;
        private boolean showBackground = false;
        private AbstractProjectViewer viewer = null;
        private NamedObject obj = null;
        private ObjectDocumentsEditPanel parent = null;
        
        public FileBox (final File               f,
                        final NamedObject        obj,
                        AbstractProjectViewer    viewer,
                        ObjectDocumentsEditPanel parent)
        {
            
            super (BoxLayout.Y_AXIS);
            
            this.file = f;
            this.viewer = viewer;
            this.obj = obj;
            this.parent = parent;
            
            if (this.isValidFile ())
            {
            
                UIUtils.setAsButton (this);
                
            }
            
            final FileBox _this = this;
            
            String ext = "...";
            String name = f.getName ();
                        
            int ind = name.lastIndexOf (".");
            
            if ((ind > 0)
                &&
                (ind < name.length ())
               )
            {
                        
                ext = name.substring (ind + 1);
                name = name.substring (0, ind);
                        
            }
            
            String toolTip = "Click to view the file, right click to see the menu.";
                
            boolean _validFile = true;
            
            boolean addBorder = false;
            
            Icon ic = null;
            
            if ((!f.exists ())
                ||
                (f.isDirectory ())
               )
            {
                
                ic = new ImageIcon (Environment.getImage (Constants.DOCUMENT_ERROR_FILE_NAME));
                
                toolTip = "Unable to find file, right click to remove.";
                
                _validFile = false;
                
            } else {
            
                if (UIUtils.imageFileFilter.accept (f))
                {
                
                    ic = new ImageIcon (UIUtils.getScaledImage (UIUtils.getImage (f),
                                                                48,
                                                                48));
                
                    addBorder = true;
                
                } else {
                                                          
                    ic = new ImageIcon (UIUtils.replaceColorInImage (UIUtils.drawStringOnImage (Environment.getImage (Constants.DOCUMENT_NORMAL_FILE_NAME),
                                                                                                ext,
                                                                                                new JLabel ().getFont ().deriveFont (java.awt.Font.BOLD),
                                                                                                UIUtils.getColor ("4d4d4f"),
                                                                                                new java.awt.Point (0, 12)),
                                                                     UIUtils.getColor ("000000"),
                                                                     UIUtils.getColor ("4d4d4f")));            
                    
                }
    
            }
                            
            JLabel b = new JLabel (name,
                                   ic,
                                   SwingConstants.LEFT)
            {
              
                @Override
                public void setText (String t)
                {
                    
                    super.setText ("<html>" + t + "</html>");
                    
                }
                
            };
    
            if (addBorder)
            {
                
                //b.setBorder (UIUtils.createLineBorder ());
                
            }
    
            this.add (b);
    
            b.setAlignmentX (Component.LEFT_ALIGNMENT);
    
            b.setEnabled (_this.isValidFile ());                                 
                                                 
            b.setToolTipText ("<html>" + f.getName () + "<br />" + toolTip + "</html>");
            b.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
                    
            b.addMouseListener (new MouseEventHandler ()
            {
                
                @Override
                public void fillPopup (JPopupMenu menu,
                                       MouseEvent ev)
                {
    
                    final MouseEventHandler _thisEv = this;
        
                    if (_this.isValidFile ())
                    {
        
                        menu.add (UIUtils.createMenuItem ("Open",
                                                          Constants.OPEN_PROJECT_ICON_NAME,
                                                          new ActionListener ()
                                                          {
                                                               
                                                              public void actionPerformed (ActionEvent ev)
                                                              {
                                                              
                                                                    _thisEv.handlePress (null);
                                                                                                                                  
                                                              }
                                                               
                                                          }));

                    }
                                                          
                    if (f.getParentFile ().exists ())
                    {
                    
                        menu.add (UIUtils.createMenuItem ("Show Folder",
                                                           Constants.FOLDER_ICON_NAME,
                                                           new ActionListener ()
                                                           {
                                                    
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
        
                                                                    UIUtils.showFile (null,
                                                                                      f.getParentFile ());
                                                                                                                                                                                
                                                                }
                                                    
                                                            }));
                        
                    }
                                                      
                    menu.add (UIUtils.createMenuItem ("Remove",
                                                      Constants.DELETE_ICON_NAME,
                                                      new ActionListener ()
                                                      {
                                                           
                                                          public void actionPerformed (ActionEvent ev)
                                                          {
                                                               
                                                               _this.parent.removeFile (_this);
                                                                                                                              
                                                          }
                                                           
                                                      }));
                                        
                }
                
                @Override
                public void mouseExited (MouseEvent ev)
                {
                    
                    if (!_this.isValidFile ())
                    {
                        
                        return;                        
                        
                    }
                    
                    //_this.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
                    			
					_this.showBackground = false;
						
					_this.validate ();
						
					_this.repaint ();

				}
				
				@Override
                public void mouseEntered (MouseEvent ev)
                {
                    			
                    if (!_this.isValidFile ())
                    {
                        
                        return;
                                			
                    }
                    
					//_this.setBorder (UIUtils.createLineBorderWithPadding (5, 5, 5, 5));
					_this.showBackground = true;
						
					_this.validate ();
						
					_this.repaint ();
													   
				}
                
                @Override
                public void handlePress (MouseEvent ev)
                {
                    
                    if (!_this.isValidFile ())
                    {
                        
                        UIUtils.showMessage (_this,
                                             "File cannot be opened",
                                             "The file cannot be opened, this is usually because the file or its parent folder have been moved or deleted.");
                        
                        return;
                                    
                    }
                    
                    try
                    {
                        
                        UIUtils.openURL (_this.viewer,
                                         f.toURI ().toURL ());
                    
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to open file: " +
                                              f,
                                              e);
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to open file: " + f + ", please contact Quoll Writer support for assistance.");
                        
                    }
    
                }
    
            });
                    
        }

        public File getFile ()
        {
            
            return this.file;
            
        }
        
        private boolean isValidFile ()
        {
            
            if ((!this.file.exists ())
                ||
                (this.file.isDirectory ())
               )
            {
                
                return false;
                
            }
            
            return true;
            
        }
        
        public void paintComponent (Graphics g)
        {
        
            if ((this.showBackground)
                &&
                (this.isValidFile ())
               )
            {
        
                Graphics2D g2d = (Graphics2D) g;
        
                Dimension s = this.getSize ();
        
                GradientPaint gp = new GradientPaint (0, 0,
                                                      UIUtils.getColor ("#ffffff"),
                                                      0, (s.height * 1f),
                                                      UIUtils.getColor ("#dddddd"),
                                                      false);
        
                g2d.setPaint (gp);
                g2d.fill (g2d.getClip ());
        
                g2d.setPaint (new Color (0,
                                         0,
                                         0,
                                         1));
                g2d.fill (g2d.getClip ());

            }
        
        }
        
    }

}