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
import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ScrollableBox;

public abstract class AbstractSideBar<V extends AbstractViewer> extends ScrollableBox implements MainPanelListener, SideBarListener
{

    private boolean inited = false;

    protected V viewer = null;
    protected Header header = null;
    private JComponent content = null;
    protected JButton otherSideBarsButton = null;

    public AbstractSideBar (V viewer)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = viewer;

        final AbstractSideBar _this = this;

        this.otherSideBarsButton = UIUtils.createButton ("sidebars",
                                                         Constants.ICON_SIDEBAR,
                                                         Environment.getUIString (LanguageStrings.sidebars,
                                                                                  LanguageStrings.othersidebarselect,
                                                                                  LanguageStrings.tooltip),
                                                         //"Click to select another sidebar",
                                                         new ActionAdapter ()
                                                         {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                JPopupMenu menu = _this.viewer.getShowOtherSideBarsPopupSelector ();

                                                                Component s = (Component) ev.getSource ();

                                                                java.awt.Point p = s.getMousePosition ();

                                                                menu.show (s,
                                                                           (int) p.getX (),
                                                                           (int) p.getY ());

                                                            }

                                                         });

        this.otherSideBarsButton.setVisible (false);

        this.viewer.addSideBarListener (this);

    }

    /**
     * Override to return the id just in case a reference is missed.
     *
     * @returns The id.
     */
    @Override
    public String getName ()
    {
        
        return this.getId ();
        
    }
    
    @Override
    public void sideBarHidden (SideBarEvent ev)
    {

        this.otherSideBarsButton.setVisible (this.viewer.getActiveSideBarCount () > 1 && this.viewer.getActiveOtherSideBar () != null);

    }

    @Override
    public void sideBarShown (SideBarEvent ev)
    {

        this.otherSideBarsButton.setVisible (this.viewer.getActiveSideBarCount () > 1 && this.viewer.getActiveOtherSideBar () != null);

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
    public AbstractViewer getViewer ()
    {

        return this.viewer;

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

    public String getId ()
    {
        
        return null;
        
    }
    
    public String getSaveState ()
    {
        
        return null;
        
    }
        
    //public abstract boolean removeOnClose ();

    public void setTitle (String t)
    {

        if (this.header != null)
        {

            this.header.setTitle (t);

        } else {

            this.createHeader (t);

        }

    }

    public void init (String saveState)
               throws GeneralException
    {

        if (this.inited)
        {

            return;

        }

        this.inited = true;

        //this.projectViewer.addMainPanelListener (this);

        String t = this.getTitle ();

        if (t != null)
        {

            this.createHeader (t);

        }

        JComponent c = this.getContent ();

        if (c.getBorder () == null)
        {

            c.setBorder (UIUtils.createPadding (0, 5, 0, 0));

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
                                            Environment.getUIString (LanguageStrings.sidebars,
                                                                     LanguageStrings.close,
                                                                     LanguageStrings.tooltip),
                                            //"Click to close",
                                            new ActionAdapter ()
                                            {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    //_this.projectViewer.showMainSideBar ();

                                                    //_this.onClose ();

                                                    _this.viewer.closeSideBar ();
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

        JComponent _buts = UIUtils.createButtonBar (buts);
        _buts.setBorder (UIUtils.createPadding (0, 0, 0, 3));

        Header h = UIUtils.createHeader (t,
                                         Constants.SUB_PANEL_TITLE,
                                         this.getIconType (),
                                         _buts);

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

        return this.wrapInScrollPane (c,
                                      false);

    }

    protected JScrollPane wrapInScrollPane (JComponent c,
                                            boolean    addTopBorderOnScroll)
    {

        if (c.getBorder () == null)
        {

            c.setBorder (UIUtils.createPadding (0, 0, 0, 5));

        }

        final JScrollPane sp = new JScrollPane (c);

        sp.setOpaque (false);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (UIUtils.createPadding (0, 5, 0, 0));
        sp.getViewport ().setOpaque (false);
        sp.getVerticalScrollBar ().setUnitIncrement (20);

        final Border spBorder = sp.getBorder ();

        if (addTopBorderOnScroll)
        {

            sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
            {

                public void adjustmentValueChanged (AdjustmentEvent ev)
                {

                    if (sp.getVerticalScrollBar ().getValue () > 0)
                    {

                        sp.setBorder (new CompoundBorder (new MatteBorder (1, 0, 0, 0,
                                                                           UIUtils.getInnerBorderColor ()),
                                                          spBorder));

                    } else {

                        sp.setBorder (spBorder);//new EmptyBorder (1, 0, 0, 0));

                    }

                }

            });

        }

        return sp;

    }

    public void panelShown (MainPanelEvent ev)
    {

    }

    /**
     * Allows a subclass sidebar to indicate it is "for" a particular object.
     *
     * @returns Always null, override where necessary.
     */
    public NamedObject getForObject ()
    {

        return null;

    }

}
