package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.Runner;

public abstract class AbstractObjectViewPanel<E extends AbstractProjectViewer, O extends UserConfigurableObject> extends ProjectObjectQuollPanel<E, O>
{

    private ActionMap              actions = null;
    private List<Component>        topLevelComps = new ArrayList ();
    private Header                 title = null;
    private JPanel                 linkedTo = null;
    private JSplitPane             leftSplitPane = null;
    private JSplitPane             rightSplitPane = null;
    private JSplitPane             mainSplitPane = null;
    //private JTree                  linkedToEditTree = null;
    //private JTree                  linkedToViewTree = null;
    private boolean                panesInited = false;
    private EditPanel              detailsPanel = null;
    protected JComponent           bottomPanel = null;
    protected LinkedToEditPanel              linkedToPanel = null;
    private ActionListener         deleteObjectAction = null;
    private Map<EditPanel, String> sectionsNeedingSave = new HashMap ();
    private ObjectDocumentsEditPanel objDocsEditPanel = null;
    private int bottomPanelHeight = 0;
    private boolean highlight = false;

    public AbstractObjectViewPanel (E pv,
                                    O n)
                             throws GeneralException
    {

        super (pv,
               n);

    }

    @Override
    public String getTitle ()
    {
        
        return this.getForObject ().getName ();
        
    }
    
    public Header getHeader ()
    {
        
        return this.title;
        
    }
    
    public ObjectDocumentsEditPanel getObjectDocumentsEditPanel ()
    {

        return this.objDocsEditPanel;

    }

    public EditPanel getDetailsPanel ()
    {

        return this.detailsPanel;

    }

    public void init ()
               throws GeneralException
    {

        this.viewer.setLinks (this.obj);

        this.title = UIUtils.createHeader (this.obj.getName (),
                                           Constants.PANEL_TITLE,
                                           this.obj.getUserConfigurableObjectType ().getIcon24x24 (),
                                           null);

        final AbstractObjectViewPanel _this = this;

        final Header tTitle = this.title;

        this.addObjectPropertyChangedListener (new PropertyChangedAdapter ()
        {

            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (ev.getChangeType ().equals (NamedObject.NAME))
                {

                    _this.refresh ();

                }

            }

        });

        this.topLevelComps.add (this.title);

        this.add (this.title,
                  0);

        final Box b = new Box (BoxLayout.Y_AXIS);

        b.setBorder (new EmptyBorder (0, 7, 0, 7));

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setOpaque (true);
        b.setBackground (null);

        this.mainSplitPane = UIUtils.createSplitPane (JSplitPane.HORIZONTAL_SPLIT);
        this.mainSplitPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.mainSplitPane.setBorder (UIUtils.createPadding (0, 0, 3, 0));
        this.mainSplitPane.setResizeWeight (1);

        this.leftSplitPane = UIUtils.createSplitPane (JSplitPane.VERTICAL_SPLIT);
        this.leftSplitPane.setResizeWeight (1d);
            
        this.rightSplitPane = UIUtils.createSplitPane (JSplitPane.VERTICAL_SPLIT);
        this.rightSplitPane.setBorder (null);
        this.rightSplitPane.setResizeWeight (0.5d);

        this.mainSplitPane.setLeftComponent (this.leftSplitPane);
        this.mainSplitPane.setRightComponent (this.rightSplitPane);

        final JComponent botEp = this.getBottomPanel ();

        if (botEp != null)
        {

            if (botEp instanceof RefreshablePanel)
            {
        
                ((RefreshablePanel) botEp).init ();

            }
                
            this.bottomPanel = botEp;

        }

        this.detailsPanel = this.getDetailEditPanel (this.viewer,
                                                     this.obj);

        this.detailsPanel.init ();

        this.detailsPanel.addActionListener (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                if (ev.getActionCommand ().equals ("edit-visible"))
                {
                    
                    botEp.setVisible (false);
                    _this.bottomPanelHeight = botEp.getSize ().height;
                    
                }
                
                if (ev.getActionCommand ().equals ("view-visible"))
                {
                  
                    botEp.setPreferredSize (new Dimension (botEp.getPreferredSize ().width,
                                                           _this.bottomPanelHeight));
                    botEp.setVisible (true);
                    
                    _this.leftSplitPane.setDividerLocation (_this.leftSplitPane.getSize ().height - _this.bottomPanelHeight);

                }

            }
            
        });
                
        this.leftSplitPane.setTopComponent (this.detailsPanel);
        this.leftSplitPane.setBottomComponent (botEp);

        this.linkedToPanel = new LinkedToEditPanel<UserConfigurableObject, AbstractProjectViewer> (this.obj,
                                                                                                   this.viewer);

        this.rightSplitPane.setBottomComponent (this.linkedToPanel);

        this.linkedToPanel.init ();

        this.linkedToPanel.setMinimumSize (new Dimension (200, 100));

        this.linkedToPanel.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (ev.getID () == EditPanel.EDIT_VISIBLE)
                    {

                        _this.setHasUnsavedChanges (_this.linkedToPanel,
                                                    true);

                    }

                    if ((ev.getID () == EditPanel.CANCELLED) ||
                        (ev.getID () == EditPanel.VIEW_VISIBLE) ||
                        (ev.getID () == EditPanel.SAVED))
                    {

                        _this.setHasUnsavedChanges (_this.linkedToPanel,
                                                    false);

                    }

                }

            });

        this.objDocsEditPanel = new ObjectDocumentsEditPanel (this.viewer,
                                                              this.obj);

        this.objDocsEditPanel.init ();

        this.rightSplitPane.setTopComponent (this.objDocsEditPanel);

        b.add (this.mainSplitPane);

        this.refresh ();

        this.add (b);
        this.setOpaque (false);

        this.repaint ();
        this.repaint ();

        this.doInit ();
        
        ActionMap actions = this.getActionMap ();

        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                        Event.CTRL_MASK),
                "edit");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_D,
                                        Event.CTRL_MASK),
                "adddocument");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_L,
                                        Event.CTRL_MASK),
                "editlinkedto");

        actions.put ("edit",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.editObject ();
                        
                    }

                });

        actions.put ("adddocument",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.objDocsEditPanel.showAddDocument ();

                    }

                });

        actions.put ("editlinkedto",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.linkedToPanel.showEditPanel ();

                    }

                });
        
        this.setReadyForUse (true);        
        
        UIUtils.doLater (new ActionListener ()
        {
            
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.leftSplitPane.setDividerLocation (0.8d);
                _this.rightSplitPane.setDividerLocation (0.5d);
                
            }
            
        });

    }

    public void editObject ()
    {
        
        this.detailsPanel.showEditPanel ();
        
    }
    
    public abstract JComponent getBottomPanel ();

    public abstract EditPanel getDetailEditPanel (E           viewer,
                                                  NamedObject obj)
                                           throws GeneralException;

    public abstract ActionListener getDeleteObjectAction (E           viewer,
                                                          NamedObject obj);

    public abstract void doInit ();

    public abstract void doRefresh ();

    public abstract void doFillToolBar (JToolBar tb,
                                        boolean  fullScreen);

    public void fillToolBar (JToolBar tb,
                             boolean  fullScreen)
    {

        final AbstractObjectViewPanel _this = this;

        this.doFillToolBar (tb,
                            fullScreen);
/*
        tb.add (UIUtils.createToolBarButton ("print",
                                             "Click to print the " + Environment.getObjectTypeName (this.obj.getObjectType ()) + " details",
                                             "print",
                                             UIUtils.getComingSoonAction (this.viewer)));
*/
/*
        tb.add (UIUtils.createToolBarButton ("delete",
                                             "Click to delete this " + Environment.getObjectTypeName (this.obj.getObjectType ()),
                                             "delete",
                                             this.getDeleteObjectAction (this.viewer,
                                                                         this.obj)));
*/
    }

    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {

        try
        {

            int v = Integer.parseInt (s.get (Constants.ASSET_MAIN_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME));

            if (v <= 0)
            {

                return;

            }

            this.mainSplitPane.setDividerLocation (v);

        } catch (Exception e)
        {

            return;

        }

        try
        {

            int v = Integer.parseInt (s.get (Constants.ASSET_LEFT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME));

            if (v > 0)
            {

                this.leftSplitPane.setDividerLocation (v);

            }

        } catch (Exception e)
        {

        }

        try
        {

            int v = Integer.parseInt (s.get (Constants.ASSET_RIGHT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME));

            if (v > 0)
            {

                this.rightSplitPane.setDividerLocation (v);

            }

        } catch (Exception e)
        {

        }

        this.panesInited = true;

    }

    public void getState (Map<String, Object> m)
    {

        m.put (Constants.ASSET_MAIN_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.mainSplitPane.getDividerLocation ());
        m.put (Constants.ASSET_LEFT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.leftSplitPane.getDividerLocation ());
        m.put (Constants.ASSET_RIGHT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.rightSplitPane.getDividerLocation ());

    }

    public List<Component> getTopLevelComponents ()
    {

        return this.topLevelComps;

    }

    public void refreshLinkedToTree ()
    {

        this.linkedToPanel.refreshLinkedToTree ();
    /*
        List exclude = new ArrayList ();
        exclude.add (this.obj);

        // Painful but just about the only way.
        this.viewer.setLinks (this.obj);

        // Get all the "other objects" for the links the note has.
        Iterator<Link> it = this.obj.getLinks ().iterator ();

        Set links = new HashSet ();

        while (it.hasNext ())
        {

            links.add (it.next ().getOtherObject (this.obj));

        }

        DefaultTreeModel m = new DefaultTreeModel (UIUtils.createLinkToTree (this.viewer.getProject (),
                                                                             exclude,
                                                                             links,
                                                                             false));

        this.linkedToViewTree.setModel (m);

        UIUtils.expandAllNodesWithChildren (this.linkedToViewTree);
*/
    }

    @Override
    public void refresh ()
    {

        this.title.setTitle (this.obj.getName ());

        this.viewer.setLinks (this.obj);

        this.detailsPanel.refreshViewPanel ();

        this.linkedToPanel.refreshViewPanel ();

        if ((this.bottomPanel != null)
            &&
            (this.bottomPanel instanceof RefreshablePanel)
           )
        {

            ((RefreshablePanel) this.bottomPanel).refresh ();

        }

        this.doRefresh ();

        this.repaint ();

    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        Iterator<EditPanel> iter = this.sectionsNeedingSave.keySet ().iterator ();

        while (iter.hasNext ())
        {

            if (!iter.next ().doSave ())
            {

                return false;

            }

        }

        return true;

    }

    public void setHasUnsavedChanges (EditPanel ep,
                                      boolean   hasChanges)
    {

        if (hasChanges)
        {

            this.sectionsNeedingSave.put (ep,
                                          "");

        } else
        {

            this.sectionsNeedingSave.remove (ep);

        }

        this.setHasUnsavedChanges (this.sectionsNeedingSave.size () > 0);

    }

}
