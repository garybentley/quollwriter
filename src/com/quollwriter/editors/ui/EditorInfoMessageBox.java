package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.*;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;

public class EditorInfoMessageBox extends MessageBox<EditorInfoMessage>
{
        
    private Box responseBox = null;
        
    public EditorInfoMessageBox (EditorInfoMessage mess,
                                 AbstractViewer    viewer)
    {
        
        super (mess,
               viewer);
        
    }
        
    public boolean isAutoDealtWith ()
    {
        
        return false;
        
    }
        
    public void doUpdate ()
    {
        
        if (this.message.isDealtWith ())
        {
            
            if (this.responseBox != null)
            {
                
                this.responseBox.setVisible (false);
                
            }
            
        }        
        
    }
    
    public void doInit ()
    {
        
        final EditorInfoMessageBox _this = this;
        
        String text = "Sent name/avatar";
        
        if (!this.message.isSentByMe ())
        {
            
            text = "Received name/avatar update";
                            
        }
        
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.INFO_ICON_NAME);
        
        this.add (h);
            
        Box editorInfo = new Box (BoxLayout.X_AXIS);                
        editorInfo.setAlignmentX (Component.LEFT_ALIGNMENT);
        editorInfo.setBorder (UIUtils.createPadding (5, 10, 5, 5));
                
        this.add (editorInfo);
                
        if (this.message.getAvatar () != null)
        {

            JLabel avatar = new JLabel ();
                    
            avatar.setAlignmentY (Component.TOP_ALIGNMENT);
            avatar.setVerticalAlignment (SwingConstants.TOP);
            
            editorInfo.add (avatar);
            avatar.setOpaque (false);

            avatar.setIcon (new ImageIcon (UIUtils.getScaledImage (this.message.getAvatar (),
                                                                   50)));
            
            avatar.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                  UIUtils.createLineBorder ()));
            
        }
        
        String n = this.message.getName ();
        
        if (n == null)
        {
            
            n = this.message.getEditor ().getShortName ();
            
        }
            
        JLabel name = new JLabel (n);
        editorInfo.add (name);
        
        name.setBorder (null);
        name.setAlignmentY (Component.TOP_ALIGNMENT);
        name.setVerticalAlignment (JLabel.TOP);
        name.setAlignmentX (Component.LEFT_ALIGNMENT);
        name.setFont (name.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (java.awt.Font.PLAIN));
                                
        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
           )
        {
            
            this.responseBox = new Box (BoxLayout.Y_AXIS);
            
            this.responseBox.setBorder (UIUtils.createPadding (0, 5, 0, 5));             
            
            this.add (this.responseBox);
                                
            JButton update = new JButton ("Update");
    
            update.addActionListener (new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    EditorEditor ed = _this.message.getEditor ();
    
                    try
                    {
                        
                        // Just update the info.
                        String newName = _this.message.getName ();
                        
                        if (newName != null)
                        {
                            
                            ed.setName (newName.trim ());
                            
                        }
                        
                        java.awt.image.BufferedImage newImage = _this.message.getAvatar ();
                        
                        if (newImage != null)
                        {
                            
                            ed.setAvatar (newImage);
                            
                        }
                                                                                                
                        EditorsEnvironment.updateEditor (ed);
                                                
                        _this.message.setDealtWith (true);
                        
                        EditorsEnvironment.updateMessage (_this.message);
                                                
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to update {editor}, please contact Quoll Writer support for assitance.");
                        
                        return;
                        
                    }
    
                }
    
            });
                        
            JButton ignore = new JButton ("Ignore");
    
            ignore.addActionListener (new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    try
                    {
                        
                        _this.message.setDealtWith (true);
                                                
                        EditorsEnvironment.updateMessage (_this.message);

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to update message: " +
                                              _this.message,
                                              e);
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to update {editor}, please contact Quoll Writer support for assitance.");
                        
                        return;
                        
                    }
    
                }
    
            });

            JButton[] buts = new JButton[] { update, ignore };
                                                    
            JPanel bb = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); 
            bb.setOpaque (false);                            
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            bb.setBorder (UIUtils.createPadding (5, 0, 0, 0));
            
            this.responseBox.add (bb);             
            
        }
                                            
    }
    
}