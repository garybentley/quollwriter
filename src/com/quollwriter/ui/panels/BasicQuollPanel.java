package com.quollwriter.ui.panels;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.Header;

/**
 * A basic panel that provides a header across the top with optional buttons and a scrollable
 * content pane and is added to a background image panel.
 */
public abstract class BasicQuollPanel<E extends AbstractViewer> extends QuollPanel<E>
{
    
    private Header header = null;
    private BackgroundImagePanel bg = null;
    private JScrollPane scroll = null;
    private List<? extends JComponent> headerButtons = null;
    
    private String iconType = null;
    private String title = null;
    
    public BasicQuollPanel (E                          pv,
                            String                     title,
                            String                     iconType,
                            List<? extends JComponent> buttons)
    {
        
        super (pv);
        
        this.title = title;
        this.iconType = iconType;
        this.headerButtons = buttons;
        
    }

    public BasicQuollPanel (E      pv,
                            String title,
                            String iconType)
    {
        
        super (pv);
        
        this.title = title;
        this.iconType = iconType;
        
    }
    
    public abstract boolean isWrapContentInScrollPane ();
    
    public List<? extends JComponent> getHeaderButtons ()
    {
        
        return this.headerButtons;
        
    }
        
    public void setHeaderButtons (List<? extends JComponent> buttons)
    {
        
        if (this.header != null)
        {
            
            if (buttons != null)
            {
                            
                this.header.setControls (UIUtils.createButtonBar (buttons));
                
            } else {
                
                this.header.setControls (null);
                
            }
            
        }
        
        this.headerButtons = buttons;
        
    }
    
    public Header getHeader ()
    {
        
        return this.header;
        
    }
    
    public BackgroundImagePanel getBackgroundPanel ()
    {
        
        return this.bg;
        
    }
    
    public JScrollPane getScrollPane ()
    {
        
        return this.scroll;
        
    }
    
    @Override
    public void close ()
    {
        
    }
    
    public void setTitle (String t)
    {
        
        if (this.header != null)
        {
        
            this.header.setTitle (t);
            
        }
        
        this.title = t;
        
    }
    
    /**
     * Called by the init to get the content of the panel.
     */
    public abstract JComponent getContent ()
                                    throws GeneralException;

    @Override
    public void init ()
               throws GeneralException
    {

        this.header = UIUtils.createHeader (this.title,
                                            Constants.PANEL_TITLE,
                                            this.iconType,
                                            (this.headerButtons != null ? UIUtils.createButtonBar (this.headerButtons) : null));

        this.bg = new BackgroundImagePanel ();
    
        this.bg.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
        this.bg.setMinimumSize (new Dimension (300,
                                         200));

        this.add (this.header);    
        
        JComponent c = this.getContent ();
        c.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
        c.setMinimumSize (new Dimension (300,
                                         200));
        
        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);

        this.bg.add (c);
    
        if (this.isWrapContentInScrollPane ())
        {
    
            final JScrollPane cscroll = new JScrollPane (c);//this.bg);
    
            this.scroll = cscroll;
                    
            cscroll.setBorder (UIUtils.createPadding (1, 0, 0, 0));
         
            cscroll.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
            {
               
                public void adjustmentValueChanged (AdjustmentEvent ev)
                {
                    
                    if (cscroll.getVerticalScrollBar ().getValue () > 0)
                    {
                    
                        cscroll.setBorder (new MatteBorder (1, 0, 0, 0,
                                                            UIUtils.getInnerBorderColor ()));
    
                    } else {
                        
                        cscroll.setBorder (UIUtils.createPadding (1, 0, 0, 0));
                        
                    }
                        
                }
                
            });
    
            cscroll.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            cscroll.getVerticalScrollBar ().setUnitIncrement (20);
            cscroll.setOpaque (false);
            cscroll.setBackground (null);
            cscroll.getViewport ().setBorder (null);
            cscroll.getViewport ().setOpaque (false);
    
            this.add (cscroll);    
          
            UIUtils.doLater (new ActionListener ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
                    
                    cscroll.getVerticalScrollBar ().setValue (0);
                    
                }
                
            });            
          
        } else {
          
            this.add (c);
            
        }
                                    
    }

    @Override
    public void getState (Map<String, Object> s)
    {
        
    }
    
    @Override
    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {
        
    }

    @Override
    public ImageIcon getIcon (int type)
    {
        
        return Environment.getIcon (this.iconType,
                                    type);
        
    }

    @Override
    public String getTitle ()
    {
        
        return this.title;
        
    }
    
    @Override
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {
                
    }
    
    @Override
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {
        
    }

    @Override
    public List<Component> getTopLevelComponents ()
    {
        
        return new ArrayList ();
        
    }
    
}