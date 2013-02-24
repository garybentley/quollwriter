package com.quollwriter.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;

public abstract class AbstractSideBar<E extends AbstractProjectViewer> extends Box implements MainPanelListener
{
        
    private boolean inited = false;
        
    protected E projectViewer = null;
    protected Header header = null;
    
    public AbstractSideBar (E pv)
    {

        super (BoxLayout.Y_AXIS);
        
        this.projectViewer = pv;

        this.setMinimumSize (new Dimension (200,
                                            200));
        
    }

    public abstract String getTitle ();
    
    public abstract String getIconType ();
    
    public abstract boolean canClose ();
    
    public abstract JComponent getContent ();
    
    public abstract List<JButton> getHeaderControls ();
    
    public abstract void onClose ();
    
    public abstract boolean removeOnClose ();
    
    public void setTitle (String t)
    {
        
        if (this.header != null)
        {
            
            this.header.setTitle (t);
            
        } else {
            
            this.createHeader (t);
            
        }
        
    }
    
    public void init ()
    {
        
        if (this.inited)
        {
            
            return;
            
        }
        
        this.inited = true;
                
        this.projectViewer.addMainPanelListener (this);
        
        String t = this.getTitle ();
        
        if (t != null)
        {

            this.createHeader (t);
        
        }
                    
        JComponent c = this.getContent ();
        
        if (c.getBorder () == null)
        {
            
            c.setBorder (new EmptyBorder (0, 5, 0, 0));
            
        }

        this.add (c);
        
    }

    private void createHeader (String t)
    {

        final AbstractSideBar _this = this;
        
        List<JButton> buts = this.getHeaderControls ();
                
        if (buts == null)
        {
            
            buts = new ArrayList ();
                
        }
        
        if (this.canClose ())
        {
            
            buts.add (UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                            Constants.ICON_SIDEBAR,
                                            "Click to close",
                                            new ActionAdapter ()
                                            {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.projectViewer.showMainSideBar ();
                                                                                                            
                                                    _this.onClose ();
                                                    
                                                    if (_this.removeOnClose ())
                                                    {
                                                        
                                                        _this.projectViewer.removeSideBar (_this);
                                                        
                                                        _this.projectViewer.removeMainPanelListener (_this);
                                                        
                                                    }
                                                    
                                                }
                                                
                                            }));
                      
        }
    
        Header h = UIUtils.createHeader (t,
                                         Constants.SUB_PANEL_TITLE,
                                         this.getIconType (),
                                         UIUtils.createButtonBar (buts));

        this.header = h;        

        if (this.getComponentCount () == 0)
        {
            
            this.add (this.header);
            
        } else {
            
            this.add (this.header,
                      0);
            
        }
        
    }
    
    protected JScrollPane wrapInScrollPane (JComponent c)
    {
        
        if (c.getBorder () == null)
        {
        
            c.setBorder (new EmptyBorder (0, 0, 0, 5));
            
        }

        JScrollPane sp = new JScrollPane (c);
        
        sp.setOpaque (false);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (new EmptyBorder (0, 5, 0, 0));
        sp.getViewport ().setOpaque (false);
        sp.getVerticalScrollBar ().setUnitIncrement (20);

        return sp;        
        
    }
    
    public void panelShown (MainPanelEvent ev)
    {

    }
    
    public void onShow ()
    {
        
    }
        
}