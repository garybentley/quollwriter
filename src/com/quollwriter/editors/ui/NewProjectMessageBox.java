package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.events.*;

public class NewProjectMessageBox extends MessageBox<NewProjectMessage> implements EditorChangedListener
{
        
    private Box responseBox = null;
    private ProjectSentReceivedViewer sentViewer = null;
    private JLabel previousLabel = null;
        
    public NewProjectMessageBox (NewProjectMessage mess,
                                 AbstractViewer    viewer)
    {
        
        super (mess,
               viewer);
        
    }
    
    @Override    
    public void editorChanged (EditorChangedEvent ev)
    {
        
        if (ev.getEditor () == this.message.getEditor ())
        {
        
            this.updateForEditor ();
            
        }
        
    }
    
    @Override
    public boolean isAutoDealtWith ()
    {
        
        return false;
        
    }    
    
    @Override
    public boolean isShowAttentionBorder ()
    {
        
        if (this.message.getEditor ().isPrevious ())
        {
            
            return false;
            
        }
        
        return super.isShowAttentionBorder ();
        
    }
    
    private void updateForEditor ()
    {
        
        this.previousLabel.setVisible (false);
        
        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPrevious ())
           )
        {
            
            this.previousLabel.setText (String.format ("<b>%s</b> is a previous {contact}.  This message can no longer be acted upon.",
                                                       this.message.getEditor ().getShortName ()));        
            
            this.previousLabel.setVisible (true);
            
            this.responseBox.setVisible (false);
                        
        }
        
    }
    
    public void doUpdate ()
    {
        
        this.responseBox.setVisible (!this.message.isDealtWith ());
        
    }
    
    public void doInit ()
    {
        
        EditorsEnvironment.addEditorChangedListener (this);
        
        final NewProjectMessageBox _this = this;
                                
        String text = "Sent {project}";
        
        if (!this.message.isSentByMe ())
        {
            
            text = "Received an invitation to edit a {project}";
                            
        }
        
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.PROJECT_ICON_NAME);
        
        this.add (h);

        JComponent bp = EditorsUIUtils.getProjectMessageDetails (this.message,
                                                                 this.viewer,
                                                                 this);
        
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));        
        
        this.add (bp);             
        
        this.responseBox = new Box (BoxLayout.Y_AXIS);
        
        this.responseBox.setVisible (false);
                
        this.add (this.responseBox);
        
        this.previousLabel = UIUtils.createInformationLabel ("");
        
        this.add (this.previousLabel);
                
        this.updateForEditor ();
        
        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
            &&
            (!this.message.getEditor ().isPrevious ())
           )
        {
            
            this.responseBox.setVisible (true);
            
            JComponent l = UIUtils.createBoldSubHeader ("Select your response below",
                                                        null);
            
            this.responseBox.add (l);           
            
            JButton accept = UIUtils.createButton ("Accept",
                                                   null);

            accept.setToolTipText ("Click to accept the invitation");
            accept.addActionListener (new ActionListener ()
                                                   {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            // Show a message box.
                                                            _this.showResponseMessagePopup (true);
                                                            
                                                        }
                                                        
                                                   });
            
            JButton reject = UIUtils.createButton ("Reject",
                                                   null);

            reject.setToolTipText ("Click to reject the invitation");
            reject.addActionListener (new ActionListener ()
                                                   {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                                   
                                                            _this.showResponseMessagePopup (false); 
                                                            
                                                        }
                                                        
                                                   });

            JButton[] buts = new JButton[] { accept, reject };

            JComponent bb = UIUtils.createButtonBar2 (buts,
                                                      Component.LEFT_ALIGNMENT);
                        
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            this.responseBox.add (bb); 
            
        }
                     
    }
    
    private void showResponseMessagePopup (final boolean accepted)
    {
        
        final NewProjectMessageBox _this = this;
        
        EditorsUIUtils.handleNewProjectResponse (_this.viewer,
                                                 _this.message,
                                                 accepted);
        
    }
    
}