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
import com.quollwriter.ui.components.ScrollableBox;

public abstract class AbstractSideBar<E extends AbstractProjectViewer> extends ScrollableBox implements MainPanelListener, SideBarListener
{
        
    private boolean inited = false;
        
    protected E projectViewer = null;
    protected Header header = null;
    private JComponent content = null;
    protected JButton otherSideBarsButton = null;
    
    public AbstractSideBar (E pv)
    {

        super (BoxLayout.Y_AXIS);
        
        this.projectViewer = pv;

        final AbstractSideBar _this = this;
        
        this.otherSideBarsButton = UIUtils.createButton ("sidebars",
                                                         Constants.ICON_SIDEBAR,
                                                         "Click to select another sidebar",
                                                         new ActionAdapter ()
                                                         {
                                                            
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                                
                                                                JPopupMenu menu = _this.projectViewer.getShowOtherSideBarsPopupSelector ();
                                                                
                                                                Component s = (Component) ev.getSource ();
                                                                
                                                                java.awt.Point p = s.getMousePosition ();
                                                                
                                                                menu.show (s,
                                                                           (int) p.getX (),
                                                                           (int) p.getY ());
                
                                                            }
                                                            
                                                         });
        
        this.otherSideBarsButton.setVisible (false);
        
        this.projectViewer.addSideBarListener (this);
        
    }
        
    @Override
    public void sideBarHidden (SideBarEvent ev)
    {
        
        this.otherSideBarsButton.setVisible (this.projectViewer.getActiveSideBarCount () > 1 && this.projectViewer.getActiveOtherSideBar () != null);
        
    }
    
    @Override
    public void sideBarShown (SideBarEvent ev)
    {
        
        this.otherSideBarsButton.setVisible (this.projectViewer.getActiveSideBarCount () > 1 && this.projectViewer.getActiveOtherSideBar () != null);
        
    }

    /**
     * Always 250, 250.
     */
    @Override
    public Dimension getMinimumSize ()
    {
        
        return new Dimension (250,
                              250);        
    }
    
    /*
    public Dimension getPreferredSize ()
    {
        
        if (this.content != null)
        {
            
            return this.content.getPreferredSize ();
            
        }
        
        return super.getPreferredSize ();
        
    }
    */
    public E getProjectViewer ()
    {
        
        return this.projectViewer;
        
    }
    
    public String getActiveIconType ()
    {
        
        return this.getIconType ();
        
    }
    
    public String getActiveTitle ()
    {
        
        return this.getTitle ();
        
    }
    
    public abstract String getTitle ();
    
    public abstract String getIconType ();
    
    public abstract boolean canClose ();
    
    public abstract JComponent getContent ();
    
    public abstract List<JComponent> getHeaderControls ();
    
    public abstract void onClose ();
    
    public abstract void onShow ();
    
    public abstract void onHide ();
    
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
               throws GeneralException
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

        this.content = c;
        
        this.add (c);
        
    }

    private void createHeader (String t)
    {

        final AbstractSideBar _this = this;
        
        List<JComponent> buts = this.getHeaderControls ();
                
        if (buts == null)
        {
            
            buts = new ArrayList ();
                
        }
        
        buts.add (this.otherSideBarsButton);

        if (this.canClose ())
        {
            
            buts.add (UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                            Constants.ICON_SIDEBAR,
                                            "Click to close",
                                            new ActionAdapter ()
                                            {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    //_this.projectViewer.showMainSideBar ();
                                                                                                            
                                                    //_this.onClose ();
                                                    
                                                    _this.projectViewer.closeSideBar ();
                                                    /*
                                                    if (_this.removeOnClose ())
                                                    {
                                                        
                                                        _this.projectViewer.removeSideBar (_this);
                                                        
                                                        _this.projectViewer.removeMainPanelListener (_this);
                                                        
                                                    }
                                                    */
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
            
}
