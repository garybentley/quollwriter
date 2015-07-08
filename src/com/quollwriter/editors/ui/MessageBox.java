package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Container;
import java.awt.AWTEvent;
import javax.swing.plaf.LayerUI;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

public abstract class MessageBox<E extends EditorMessage> extends Box implements EditorMessageListener
{
    
    protected AbstractProjectViewer projectViewer = null;
    protected E message = null;
    protected boolean showAttentionBorder = true;
    private Box content = null;
    private PropertyChangedListener updateListener = null;
    
    public MessageBox (E                     mess,
                       AbstractProjectViewer viewer)
    {
        
        super (BoxLayout.Y_AXIS);

        this.content = new Box (BoxLayout.Y_AXIS);
        
        final MessageBox _this = this;
        
        final Timer update = new Timer (750,
                                        new ActionListener ()
                                        {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                            
                                                _this.message.setDealtWith (true);
                                                
                                                try
                                                {
                                                    
                                                    EditorsEnvironment.updateMessage (_this.message);
                                                    
                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to update message: " +
                                                                          _this.message,
                                                                          e);
                                                    
                                                } 
                                                
                                            }
                                            
                                        });
        
        update.setRepeats (false);
        
        this.add (new JLayer<JComponent> (this.content, new LayerUI<JComponent> ()
        {
            
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                // enable mouse motion events for the layer's subcomponents
                ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
            }

            @Override
            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                // reset the layer event mask
                ((JLayer) c).setLayerEventMask(0);
            }
             
            @Override
            public void processMouseEvent (MouseEvent                   ev,
                                           JLayer<? extends JComponent> l)
            {
                
                if ((ev.isPopupTrigger ())
                    &&
                    (!_this.message.isSentByMe ())
                   )
                {
                    
                    ev.consume ();
                    
                    JPopupMenu popup = new JPopupMenu ();
                    
                    popup.add (UIUtils.createMenuItem ("Report this message",
                                                       Constants.ERROR_ICON_NAME,
                                                       new ActionListener ()
                                                       {
                                                
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                
                                                                EditorsUIUtils.showReportMessage (_this,
                                                                                                  _this.projectViewer);
                                                
                                                            }
                                                
                                                        }));
                                                        
                    popup.show (_this,
                                ev.getX (),
                                ev.getY ());
                    
                    return;
                    
                }
                
                if ((!_this.message.isDealtWith ())
                    &&
                    (_this.isAutoDealtWith ())
                   )
                {
                
                    if (ev.getID () == MouseEvent.MOUSE_EXITED)
                    {
                        
                        Point p = SwingUtilities.convertPoint ((Component) ev.getSource (),
                                                               ev.getPoint (),
                                                               _this);
                
                        if (!SwingUtilities.getLocalBounds (_this).contains (p))
                        {
    
                            update.stop ();
                            
                        }
                        
                        return;
                        
                    }
                
                    if (ev.getID () == MouseEvent.MOUSE_ENTERED)
                    {
                        
                        update.start ();
                               
                        return;
                                                
                    }
                    
                }
             
            }
            
        }));
                             
        this.setOpaque (false);
        this.setBackground (null);
                
        this.content.setBackground (UIUtils.getComponentColor ());

        this.message = mess;
        this.projectViewer = viewer;
        this.setAlignmentX (Component.LEFT_ALIGNMENT);
                
    }
    
    public abstract boolean isAutoDealtWith ();
    
    @Override
    public Component add (Component c)
    {

        if (c instanceof JLayer)
        {
            
            return super.add (c);
            
        }
    
        return this.content.add (c);
        
    }
    
    public void setShowAttentionBorder (boolean v)
    {
        
        this.showAttentionBorder = v;        
        
    }
    
    public boolean isShowAttentionBorder ()
    {
        
        return this.showAttentionBorder;
        
    }
    
    public EditorMessage getMessage ()
    {
        
        return this.message;
        
    }
    
    public boolean isDealtWith ()
    {
        
        return this.message.isDealtWith ();
        
    }

    public abstract void doUpdate ();
    
    public abstract void doInit ()
                          throws GeneralException;
    
    public String getOpenMessageLink (EditorMessage m,
                                      String        link)
    {
        
        return String.format ("<a href='%s:%s'>%s</a>",
                              Constants.OPENEDITORMESSAGE_PROTOCOL,
                              m.getKey (),
                              Environment.replaceObjectNames (link));
        
    }
    
    public void update ()
    {
        
        this.doUpdate ();
        
        if (this.message.isDealtWith ())
        {
            
            this.setToolTipText (null);
            this.setBorder (null);
            
        }
        
    }
        
    public void init ()
               throws GeneralException
    {
        
        this.doInit ();
        
        if ((!this.message.isDealtWith ())
            &&
            (this.showAttentionBorder)
           )
        {
            
            this.setToolTipText ("This message needs your attention!");
            
            this.setBorder (new CompoundBorder (new MatteBorder (0, 2, 0, 0, UIUtils.getColor ("#ff0000")),
                                                UIUtils.createPadding (0, 5, 0, 0)));
                        
        }
        
        // Add ourselves as a message listener in case our message gets updated in a different context.
        EditorsEnvironment.addEditorMessageListener (this);
                                    
    }
    
    @Override
    /**
     * Listens for message events relating to the underlying message this box is displaying.
     * If the event is for our message and the message has been changed then {@link #update()} is called.
     *
     * @param ev The event.
     */
    public void handleMessage (EditorMessageEvent ev)
    {

        // Is this message for us?
        if ((ev.getMessage ().equals (this.message))
            &&
            (ev.getType () == EditorMessageEvent.MESSAGE_CHANGED)
           )
        {
            
            this.update ();
            
        }
        
    }
    
    protected JComponent getMessageQuoteComponent (String message)
    {
        
        Box b = new Box (BoxLayout.X_AXIS);
        
        ImagePanel ip = new ImagePanel (Environment.getIcon (Constants.MESSAGE_ICON_NAME,
                                                             Constants.ICON_POPUP),
                                        null);
        
        ip.setAlignmentY (Component.TOP_ALIGNMENT);
        
        //b.add (ip);
        //b.add (Box.createHorizontalStrut (5));
        
        JComponent t = UIUtils.createHelpTextPane (message,
                                                   this.projectViewer);
        t.setAlignmentY (Component.TOP_ALIGNMENT);

        t.setBorder (null);
        t.setOpaque (false);
                
        b.add (t);

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        return b;        
        
    }
    
    protected JComponent getMessageComponent (String message,
                                              String iconName)
    {
        
        Box b = new Box (BoxLayout.X_AXIS);
        
        if (iconName != null)
        {
        
            ImagePanel ip = new ImagePanel (Environment.getIcon (iconName,
                                                                 Constants.ICON_EDITOR_MESSAGE),
                                            null);
            
            ip.setAlignmentY (Component.TOP_ALIGNMENT);
            
            b.add (ip);
            b.add (Box.createHorizontalStrut (5));

        }
        
        JComponent t = UIUtils.createHelpTextPane (message,
                                                   this.projectViewer);
        t.setAlignmentY (Component.TOP_ALIGNMENT);

        t.setBorder (null);
        t.setOpaque (false);
        
        b.add (t);

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        return b;        

    }
    
}