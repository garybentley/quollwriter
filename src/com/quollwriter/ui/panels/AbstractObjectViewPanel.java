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
import com.quollwriter.data.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.Runner;

public abstract class AbstractObjectViewPanel extends QuollPanel
{

    private ActionMap              actions = null;
    private List<Component>        topLevelComps = new ArrayList ();
    private Header                 title = null;
    private JPanel                 linkedTo = null;
    private JSplitPane             leftSplitPane = null;
    private JSplitPane             rightSplitPane = null;
    private JSplitPane             mainSplitPane = null;
    private JTree                  linkedToEditTree = null;
    private JTree                  linkedToViewTree = null;
    private boolean                panesInited = false;
    private DetailsEditPanel       detailsPanel = null;
    protected EditPanel              bottomDetailsPanel = null;
    protected EditPanel              linkedToPanel = null;
    private ActionListener         deleteObjectAction = null;
    private Map<EditPanel, String> sectionsNeedingSave = new HashMap ();
    private ObjectDocumentsEditPanel objDocsEditPanel = null;
    //protected ProjectViewer        projectViewer = null;

    public AbstractObjectViewPanel (AbstractProjectViewer pv,
                                    NamedObject           n)
                             throws GeneralException
    {

        super (pv,
               n);

    }

    public ObjectDocumentsEditPanel getObjectDocumentsEditPanel ()
    {
        
        return this.objDocsEditPanel;
        
    }
    
    public DetailsEditPanel getDetailsPanel ()
    {
        
        return this.detailsPanel;
        
    }
    
    public void initDividers ()
    {

        // This method doesn't work for startup.

    }

    public void init ()
               throws GeneralException
    {
    
        this.projectViewer.setLinks (this.obj);

        this.title = UIUtils.createHeader (this.obj.getName (),
                                           Constants.PANEL_TITLE,
                                           this.getIconType (),
                                           null);

        final AbstractObjectViewPanel _this = this;
                                           
        final Header tTitle = this.title;

        this.addObjectPropertyChangedListener (new PropertyChangedAdapter ()
        {

            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (ev.getChangeType ().equals (NamedObject.NAME))
                {
            
                    _this.refresh (_this.obj);
                    
                }
                
            }

        });

        this.topLevelComps.add (this.title);

        this.add (this.title,
                  0);

        JToolBar titleC = new JToolBar ();
        titleC.setFloatable (false);
        titleC.setOpaque (false);

        JButton sb = new JButton (Environment.getIcon ("delete",
                                                       Constants.ICON_PANEL_ACTION));
        sb.setOpaque (false);
        titleC.add (sb);
        //title.setControls (titleC);
        sb.setToolTipText ("Click to delete this " + Environment.getObjectTypeName (this.obj));
        UIUtils.setAsButton (sb);

        this.deleteObjectAction = this.getDeleteObjectAction (this.projectViewer,
                                                              this.obj);

        sb.addActionListener (this.deleteObjectAction);

        final Box b = new Box (BoxLayout.Y_AXIS);

        b.setBorder (new EmptyBorder (0, 7, 0, 7));

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setOpaque (true);
        b.setBackground (null);

        this.mainSplitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                             false);
        this.mainSplitPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.mainSplitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.mainSplitPane.setBorder (null);
        this.mainSplitPane.setOpaque (false);
        this.mainSplitPane.setContinuousLayout (true);
        this.mainSplitPane.setBorder (UIUtils.createPadding (0, 0, 3, 0));
        this.mainSplitPane.setResizeWeight (1);
        
        this.leftSplitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT,
                                             false);
        this.leftSplitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.leftSplitPane.setBorder (null);
        this.leftSplitPane.setOpaque (false);
        this.leftSplitPane.setContinuousLayout (true);
        this.leftSplitPane.setResizeWeight (1);
        
        this.rightSplitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT,
                                              false);
        this.rightSplitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.rightSplitPane.setBorder (null);
        this.rightSplitPane.setOpaque (false);
        this.rightSplitPane.setContinuousLayout (true);
        this.rightSplitPane.setResizeWeight (1);

        //this.mainSplitPane.
        
        this.mainSplitPane.setLeftComponent (this.leftSplitPane);
        this.mainSplitPane.setRightComponent (this.rightSplitPane);

        EditPanel botEp = this.getBottomEditPanel ();

        if (botEp != null)
        {

            botEp.init ();
            
            this.bottomDetailsPanel = botEp;

        }

        this.detailsPanel = this.getDetailEditPanel (this.projectViewer,
                                                     this.obj);
                                                     
        this.detailsPanel.init (this);
                
        this.leftSplitPane.setTopComponent (this.detailsPanel);
        this.leftSplitPane.setBottomComponent (botEp);
        
        this.linkedToPanel = this.createLinkedToPanel ();

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
                
        this.objDocsEditPanel = new ObjectDocumentsEditPanel (this.projectViewer,
                                                              this.obj);
        
        this.objDocsEditPanel.init ();
        
        this.rightSplitPane.setTopComponent (this.objDocsEditPanel);

        b.add (this.mainSplitPane);

        this.refresh (this.obj);

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
  
                        _this.detailsPanel.showEditPanel ();
                        
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

    }

    public abstract EditPanel getBottomEditPanel ();

    public abstract String getIconType ();

    public abstract DetailsEditPanel getDetailEditPanel (AbstractProjectViewer pv,
                                                         NamedObject           obj)
                                                  throws GeneralException;

    public abstract ActionListener getDeleteObjectAction (AbstractProjectViewer pv,
                                                          NamedObject           obj);

    public abstract void doInit ();

    public abstract void doRefresh ();

    public abstract void doFillToolBar (JToolBar tb,
                                        boolean  fullScreen);

    private EditPanel createLinkedToPanel ()
    {

        final AbstractObjectViewPanel _this = this;

        return new EditPanel (false)
        {

            public void refreshViewPanel ()
            {

                List exclude = new ArrayList ();
                exclude.add (_this.obj);

                // Painful but just about the only way.
                _this.projectViewer.setLinks (_this.obj);

                // Get all the "other objects" for the links the note has.
                Iterator<Link> it = _this.obj.getLinks ().iterator ();

                Set links = new HashSet ();

                while (it.hasNext ())
                {

                    links.add (it.next ().getOtherObject (_this.obj));

                }

                DefaultTreeModel m = new DefaultTreeModel (UIUtils.createLinkToTree (_this.projectViewer.getProject (),
                                                                                     exclude,
                                                                                     links,
                                                                                     false));

                _this.linkedToViewTree.setModel (m);

                UIUtils.expandAllNodesWithChildren (_this.linkedToViewTree);

            }

            public List<FormItem> getEditItems ()
            {

                return null;

            }

            public List<FormItem> getViewItems ()
            {

                return null;

            }

            public boolean handleSave ()
            {

                // Get all the link items from the tree.
                DefaultTreeModel dtm = (DefaultTreeModel) _this.linkedToEditTree.getModel ();

                Set s = new HashSet ();

                try
                {

                    UIUtils.getSelectedObjects ((DefaultMutableTreeNode) dtm.getRoot (),
                                                _this.obj,
                                                s);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to get objects to link to for: " +
                                          _this.obj,
                                          e);

                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "An internal error has occurred.\n\nUnable to add/edit object.");

                    return false;

                }

                // Save the links
                try
                {

                    _this.projectViewer.saveLinks (_this.obj,
                                                   s);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to save links: " +
                                          _this.obj,
                                          e);

                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "An internal error has occurred.\n\nUnable to save links.");

                    return false;

                }

                _this.refreshLinkedToTree ();

                java.util.Set<NamedObject> otherObjects = _this.obj.getOtherObjectsInLinks ();

                _this.projectViewer.refreshObjectPanels (otherObjects);

                return true;

            }

            public boolean handleCancel ()
            {

                return true;

            }

            public void handleEditStart ()
            {

                _this.linkedToEditTree.setModel (UIUtils.getLinkedToTreeModel (_this.projectViewer,
                                                                               _this.obj,
                                                                               true));

                UIUtils.expandPathsForLinkedOtherObjects (_this.linkedToEditTree,
                                                          _this.obj);

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
                            
                            name = Link.OBJECT_TYPE;
                            
                        }
                        
                        return super.getIcon (name,
                                              type);
                        
                    }
                    
                };
                
                return iconProv;

            }

            public String getHelpText ()
            {

                return "Select the objects you wish to link the " + Environment.getObjectTypeName (_this.obj) + " to, press the Edit icon again to save.";

            }

            public String getTitle ()
            {

                return "Linked to";

            }

            public JComponent getEditPanel ()
            {

                _this.linkedToEditTree = UIUtils.createLinkedToTree (_this.projectViewer,
                                                                     _this.obj,
                                                                     true);

                return _this.linkedToEditTree;

            }

            public JComponent getViewPanel ()
            {

                _this.linkedToViewTree = UIUtils.createLinkedToTree (_this.projectViewer,
                                                                     _this.obj,
                                                                     false);

                JScrollPane treeScroll = UIUtils.createScrollPane (_this.linkedToViewTree);

                treeScroll.setBorder (null);
                UIUtils.expandAllNodesWithChildren (_this.linkedToViewTree);
                
                treeScroll.setMinimumSize (new Dimension (150, 0));                
                
                return treeScroll;

            }

        };

    }

    public void fillToolBar (JToolBar tb,
                             boolean  fullScreen)
    {

        final AbstractObjectViewPanel _this = this;

        this.doFillToolBar (tb,
                            fullScreen);

        final JButton b = UIUtils.createToolBarButton ("new",
                                                       "Click to add a new " + Environment.getObjectTypeName (QCharacter.OBJECT_TYPE) + ", " + Environment.getObjectTypeName (Location.OBJECT_TYPE) + ", " + Environment.getObjectTypeName (QObject.OBJECT_TYPE) + " etc.",
                                                       "new",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                UIUtils.addNewAssetItemsToPopupMenu (m,
                                                     b,
                                                     _this.projectViewer,
                                                     null,
                                                     null);

                Component c = (Component) ev.getSource ();

                m.show (c,
                        c.getX (),
                        c.getY ());

            }

        };

        b.addActionListener (ab);

        tb.add (b);

        tb.add (UIUtils.createToolBarButton ("print",
                                             "Click to print the " + Environment.getObjectTypeName (this.obj.getObjectType ()) + " details",
                                             "print",
                                             UIUtils.getComingSoonAction (this.projectViewer)));

        tb.add (UIUtils.createToolBarButton ("delete",
                                             "Click to delete this " + Environment.getObjectTypeName (this.obj.getObjectType ()),
                                             "delete",
                                             this.getDeleteObjectAction (this.projectViewer,
                                                                         this.obj)));

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
        
        this.setReadyForUse (true);

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

        List exclude = new ArrayList ();
        exclude.add (this.obj);

        // Painful but just about the only way.
        this.projectViewer.setLinks (this.obj);

        // Get all the "other objects" for the links the note has.
        Iterator<Link> it = this.obj.getLinks ().iterator ();

        Set links = new HashSet ();

        while (it.hasNext ())
        {

            links.add (it.next ().getOtherObject (this.obj));

        }

        DefaultTreeModel m = new DefaultTreeModel (UIUtils.createLinkToTree (this.projectViewer.getProject (),
                                                                             exclude,
                                                                             links,
                                                                             false));

        this.linkedToViewTree.setModel (m);

        UIUtils.expandAllNodesWithChildren (this.linkedToViewTree);

    }

    public void refresh (NamedObject n)
    {

        this.title.setTitle (this.obj.getName ());

        this.projectViewer.setLinks (this.obj);

        this.detailsPanel.refreshViewPanel ();

        this.linkedToPanel.refreshViewPanel ();

        if (this.bottomDetailsPanel != null)
        {

            this.bottomDetailsPanel.refreshViewPanel ();

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
