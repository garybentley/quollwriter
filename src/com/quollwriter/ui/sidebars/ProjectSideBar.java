package com.quollwriter.ui.sidebars;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.image.*;
import java.awt.event.*;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.components.Dragger;
import com.quollwriter.ui.components.DragListener;
import com.quollwriter.ui.components.DragEvent;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.ColorPainter;

public class ProjectSideBar extends AbstractSideBar<ProjectViewer>
{

    private JScrollPane content = null;
    private JComponent contentBox = null;
    
    private static DataFlavor COMPONENT_FLAVOR = null;
    private Set<String> assetObjTypes = null;
        
    public ProjectSideBar (ProjectViewer v)
    {
        
        super (v);
        
        this.contentBox = new Box (BoxLayout.Y_AXIS);
        
        this.contentBox.setOpaque (false);
        this.contentBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.contentBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                       Short.MAX_VALUE));
                                          
        this.content = this.wrapInScrollPane (this.contentBox);
             
        final ProjectSideBar _this = this;
        
        this.content.setTransferHandler (new TransferHandler ()
        {
                
            @Override
            public boolean canImport (TransferSupport support)
            {
                
                if (!support.isDrop ())
                {
                
                    return false;
                }
        
                boolean canImport = support.isDataFlavorSupported (COMPONENT_FLAVOR);
                
                return canImport;
            }
        
            @Override
            public boolean importData (TransferSupport support)
            {
        
                if (!canImport (support))
                {
                    
                    return false;
                }
        
                Component[] components;
        
                try
                {
                    components = (Component[]) support.getTransferable ().getTransferData (COMPONENT_FLAVOR);
                    
                } catch (Exception e) {

                    return false;
                }
        
                // Item being transfered.
                Component component = components[0];
                                                
                _this.contentBox.add (component);

                _this.contentBox.revalidate ();
                _this.contentBox.repaint ();
        
                return true;
            
            }
                
            @Override
            public void exportDone (JComponent   c,
                                    Transferable t,
                                    int          action)
            {

                _this.updateSectionsList ();
                            
            }

        });
        
        this.contentBox.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                _this.addAddSectionMenu (m,
                                         null);
/*
                // Get all the sections currently not visible.
                Set<String> defSections = _this.getSections (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);
            
                Set<String> sections = _this.getSections (Constants.PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);
                
                defSections.removeAll (sections);

                if (defSections.size () > 0)
                {
            
                    JMenu sm = new JMenu ("Add");
                    
                    m.add (sm);
                    
                    for (String sect : defSections)
                    {
            
                        final String _sect = sect;
            
                        sm.add (this.createMenuItem (Environment.getObjectTypeNamePlural (sect),
                                                     sect,
                                                     "add",
                                                     null,
                                                     new ActionListener ()
                                                     {
                                                        
                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            AccordionItem it = _this.createAccordionItemForObjectType (_sect);

                                                            _this.addAccordionItem (it);
                                                            
                                                            _this.validate ();
                                                            _this.repaint ();

                                                            // Scroll the item into view.
                                                            _this.contentBox.scrollRectToVisible (it.getBounds (null));
                                                                                                                        
                                                            _this.updateSectionsList ();
                                                            
                                                        }
                                                        
                                                     }));
                        
                    }
                    
                }
                */
            
            }
                
        });
        
        try
        {
            COMPONENT_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Component[].class.getName() + "\"");
        }
        catch(Exception e)
        {
            
        }
                             
        Set<String> aObjTypes = new HashSet ();
        
        aObjTypes.add (QCharacter.OBJECT_TYPE);
        aObjTypes.add (Location.OBJECT_TYPE);
        aObjTypes.add (QObject.OBJECT_TYPE);
        aObjTypes.add (ResearchItem.OBJECT_TYPE);

        this.assetObjTypes = aObjTypes;        
                
    }
    
    private void updateSectionsList ()
    {
        
        StringBuilder b = new StringBuilder ();
    
        // Need to save the ordering.
        Set<AccordionItem> ais = this.getAccordionItems ();
        
        for (AccordionItem ai : ais)
        {
                                    
            if (b.length () > 0)
            {
                
                b.append ("|");
                
            }
            
            b.append (ai.getId ());
            
        }
    
        UserProperties.set (Constants.PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME,
                            b.toString ());
        
    }
            
    @Override
    public void onHide ()
    {
        
    }
    
    @Override
    public void onShow ()
    {
        
    }    
    
    public void onClose ()
    {
        
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public String getActiveTitle ()
    {
        
        return "{Project}";
        
    }
    
    public String getTitle ()
    {
        
        return null;
        
    }
    
    public String getActiveIconType ()
    {
        
        return Project.OBJECT_TYPE;
        
    }
    
    public String getIconType ()
    {
        
        return null;
        
    }
    
    public Dimension getMinimumSize ()
    {
        
        return new Dimension (UIUtils.getScreenScaledWidth (250),
                              200);
        
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public boolean canClose ()
    {
        
        return false;
        
    }
    
    public JComponent getContent ()
    {
                
        return this.content;
                
    }

    public void panelShown (MainPanelEvent ev)
    {

        this.setObjectSelectedInSidebar (ev.getForObject ());
    
    }    
    
    public void setObjectSelectedInSidebar (DataObject d)
    {
        
        for (AccordionItem ai : this.getAccordionItems ())
        {
            
            if (ai instanceof ProjectObjectsAccordionItem)
            {
                
                ProjectObjectsAccordionItem pai = (ProjectObjectsAccordionItem) ai;
                
                pai.clearSelectedItemInTree ();
                
                if (d != null)
                {
                    
                    if (d.getObjectType ().equals (pai.getId ()))
                    {
                        
                        pai.setObjectSelectedInTree (d);
                        
                    }
                    
                }
            }
            
        }
        
    }

    private ProjectObjectsAccordionItem getProjectObjectsAccordionItem (String objType)
    {

        Set<AccordionItem> ais = this.getAccordionItems ();
        
        for (AccordionItem ai : ais)
        {
            
            if (ai.getId ().equals (objType))
            {
                
                if (ai instanceof ProjectObjectsAccordionItem)
                {

                    ProjectObjectsAccordionItem pai = (ProjectObjectsAccordionItem) ai;

                    return pai;
                    
                }
                
            }
            
        }
        
        return null;
        
    }
    
    public void reloadTreeForObjectType (String objType)
    {
        
        ProjectObjectsAccordionItem pai = this.getProjectObjectsAccordionItem (objType);
        
        if (pai == null)
        {
            
            return;
            
        }
        
        pai.update ();                    

    }

    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {
        
        JTree tree = this.getTreeForObjectType (treeObjType);
        
        if (tree == null)
        {
            
            return;
            
        }
        
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                           obj));
        
    }
    
    public JTree getTreeForObjectType (String objType)
    {
                
        ProjectObjectsAccordionItem pai = this.getProjectObjectsAccordionItem (objType);
        
        if (pai == null)
        {
            
            return null;
            
        }

        return pai.getTree ();
                    
    }
    
    public String getSaveState ()
    {
        
        StringBuilder ats = new StringBuilder ();

        Set<AccordionItem> ais = this.getAccordionItems ();
        
        for (AccordionItem ai : ais)
        {
            
            if (ai.isContentVisible ())
            {
                
                if (ats.length () > 0)
                {

                    ats.append ("|");

                }

                ats.append (ai.getId ());
                
            }
            
        }

        return ats.toString ();        
        
    }
 
    public JComponent getContentBox ()
    {
        
        return this.contentBox;
        
    }
  
    public void addAccordionItem (AccordionItem item)
    {
        
        this.addAccordionItem (item,
                               null);
        
    }
  
    private int getIndex (Component c)
    {
        
        Component[] comps = this.contentBox.getComponents ();
        
        for (int i = 0; i < comps.length; i++)
        {
            
            Component comp = comps[i];
            
            if (c == comp)
            {
                
                return i;
                
            }
        
        }
        
        return -1;
        
    }
  
    public void addAccordionItem (final AccordionItem item,
                                  final String        belowObjType)
    {
        
        int ind = -1;
        
        if (belowObjType != null)
        {
            
            AccordionItem ai = this.getAccordionItemForObjectType (belowObjType);
            
            if (ai != null)
            {
                
                ind = this.getIndex (ai);
                
            }
            
        }
        
        item.getHeader ().setPadding (new Insets (0, 0, 2, 0));
        item.getHeader ().getLabel ().setBorder (UIUtils.createPadding (0, 5, 0, 0));
        
        item.setBorder (UIUtils.createPadding (0, 0, 3, 0));
        
        if ((ind > -1)
            &&
            (ind < this.contentBox.getComponentCount ())
           )
        {
        
            this.contentBox.add (item,
                                 ind + 1);

        } else {
            
            this.contentBox.add (item);
                                 
        }
        item.init ();

        final ProjectSideBar _this = this;
        
        // Used to initiate the drag.
        MouseEventHandler listener = new MouseEventHandler ()
        {
            
            @Override
            public void mouseDragged(MouseEvent e)
            {
                
                JComponent c = (JComponent) e.getSource ();
                TransferHandler handler = item.getTransferHandler ();
                handler.exportAsDrag (item,
                                      e,
                                      TransferHandler.MOVE);
                                
            }
 
        };        
        
        TransferHandler transferHandler = new TransferHandler ()
        {
        
            private JComponent getParentOfType (JComponent comp,
                                                Class      cl)
            {
                
                if (cl.isAssignableFrom (comp.getClass ()))
                {
                    
                    return comp;
                    
                }
                
                while ((comp = (JComponent) comp.getParent ()) != null)
                {
                    
                    if (cl.isAssignableFrom (comp.getClass ()))
                    {
                        
                        return comp;
                        
                    }
                    
                }
                
                return null;
                
            }
        
            private int getIndex (Component c)
            {
                
                Component[] comps = _this.contentBox.getComponents ();
                
                for (int i = 0; i < comps.length; i++)
                {
                    
                    Component comp = comps[i];
                    
                    if (c == comp)
                    {
                        
                        return i;
                        
                    }
                
                }
                
                return -1;
                
            }
        
            @Override
            public boolean canImport (TransferSupport support)
            {
                
                if (!support.isDrop ())
                {
                
                    return false;
                }
        
                boolean canImport = support.isDataFlavorSupported (COMPONENT_FLAVOR);
                             
                support.setShowDropLocation (false);
                                
                return canImport;
            }
        
            @Override
            public boolean importData (TransferSupport support)
            {
        
                if (!canImport (support))
                {
                    
                    return false;
                }
        
                Component[] components;
        
                try
                {
                    components = (Component[]) support.getTransferable ().getTransferData (COMPONENT_FLAVOR);
                    
                } catch (Exception e) {

                    return false;
                }
        
                // Item being transfered.
                final Component component = components[0];
                                                
                AccordionItem it = (AccordionItem) this.getParentOfType ((JComponent) support.getComponent (),
                                                                         AccordionItem.class);
                                                               
                // Add the item below where we are.
                int ind = this.getIndex (it);

                _this.contentBox.add (component,
                                      ind);

                _this.contentBox.revalidate ();
                _this.contentBox.repaint ();
        
                _this.updateSectionsList ();        

                UIUtils.doLater (new ActionListener ()
                {
                    
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.contentBox.scrollRectToVisible (component.getBounds ());
                        
                    }
                    
                });
        
                return true;

            }
        
            @Override
            public int getSourceActions (JComponent c)
            {
                                    
                c = item.getHeader ();
                                    
                this.setDragImage (UIUtils.getImageOfComponent (c,
                                                                c.getWidth (),
                                                                c.getHeight ()));
        
                return MOVE;
            
            }
        
            @Override
            public Transferable createTransferable (final JComponent c)
            {
                
                return new Transferable ()
                {
                    
                    @Override
                    public Object getTransferData (DataFlavor flavor)
                    {
                        
                        Component[] components = new Component[1];
                        components[0] = c;
                        return components;
                    
                    }
        
                    @Override
                    public DataFlavor[] getTransferDataFlavors()
                    {
                        
                        DataFlavor[] flavors = new DataFlavor[1];
                        flavors[0] = COMPONENT_FLAVOR;
                        return flavors;
                    
                    }
        
                    @Override
                    public boolean isDataFlavorSupported (DataFlavor flavor)
                    {
                        
                        return flavor.equals (COMPONENT_FLAVOR);
                    
                    }
                    
                };
            }
        
            @Override
            public void exportDone (JComponent   c,
                                    Transferable t,
                                    int          action)
            {
                
            }

        };
            
        DropTargetListener dropTargetListener = new DropTargetAdapter ()
        {
                            
            @Override
            public void dragOver (DropTargetDragEvent ev)
            {
                
                java.awt.Point mp = _this.content.getMousePosition ();
                
                if (mp == null)
                {
                    
                    return;
                    
                }
                
                int a = _this.content.getVerticalScrollBar ().getUnitIncrement ();
                
                java.awt.Point vp = _this.content.getViewport ().getViewPosition ();

                if (mp.y <= 5 * a)
                {
                                            
                    int newy = vp.y - (a / 2);
                    
                    if (newy < 0)
                    {
                        
                        newy = 0;
                        
                    }
                    
                    _this.content.getViewport ().setViewPosition (new java.awt.Point (vp.x, newy));
                    
                    return;
                    
                }

                int h = _this.content.getViewport ().getExtentSize ().height;

                if (mp.y >= (h - (5 * a)))
                {
                                            
                    int newy = vp.y + (a / 2);
                    
                    if (newy > (vp.y + h))
                    {
                        
                        newy = (vp.y + h);
                        
                    }
                    
                    _this.content.getViewport ().setViewPosition (new java.awt.Point (vp.x, newy));
                    
                    return;                        
                                    
                }
                
                AccordionItem it = (AccordionItem) ev.getDropTargetContext ().getComponent ();
                /*
                this.setSelected (it,
                                  true);
                  */           
            }
            
            public void setSelected (AccordionItem it,
                                     boolean       val)
            {
                
                Header h = it.getHeader ();
                
                if (!val)
                {
                    
                    h.setTitleColor (UIUtils.getTitleColor ());
                    h.setPaintProvider (new ColorPainter (UIUtils.getComponentColor ()));
        
                } else {
                            
                    h.setTitleColor (UIManager.getColor ("Tree.selectionForeground"));
                    h.setPaintProvider (new ColorPainter (UIManager.getColor ("Tree.selectionBackground")));            
                    
                }
            
                _this.validate ();
                _this.repaint ();
            
            }            
            
            @Override
            public void drop (DropTargetDropEvent ev)
            {
                
                AccordionItem it = (AccordionItem) ev.getDropTargetContext ().getComponent ();
             
                this.setSelected (it,
                                  false);

            }
            
            @Override
            public void dragExit (DropTargetEvent ev)
            {
                
                AccordionItem it = (AccordionItem) ev.getDropTargetContext ().getComponent ();

                this.setSelected (it,
                                  false);

            }
            
            @Override
            public void dragEnter (DropTargetDragEvent ev)
            {
                
                try
                {
                
                    if (ev.getTransferable ().getTransferData (COMPONENT_FLAVOR) == null)
                    {
                        
                        return;
                        
                    }

                } catch (Exception e) {
                    
                    return;
                    
                }
                
                AccordionItem it = (AccordionItem) ev.getDropTargetContext ().getComponent ();

                this.setSelected (it,
                                  true);

            }

        };
        
        item.getHeader ().addMouseMotionListener (listener);

        item.setTransferHandler (transferHandler);
        
        try
        {
        
            item.getDropTarget ().addDropTargetListener (dropTargetListener);

        } catch (Exception e) {
            
            e.printStackTrace ();
            
        }
        
        item.setContentVisible (false);
        
    }
 
    private AccordionItem getAccordionItemForObjectType (String objType)
    {
        
        Component[] comps = this.contentBox.getComponents ();
        
        for (int i = 0; i < comps.length; i++)
        {
            
            Component comp = comps[i];
            
            if (comp instanceof AccordionItem)
            {
            
                AccordionItem pi = (AccordionItem) comp;

                if (pi.getId ().equals (objType))
                {
                    
                    return pi;
                    
                }
                
            }
            
        }
        
        return null;
        
    }
 
    private void removeSection (String objType)
    {
        
        AccordionItem ai = this.getAccordionItemForObjectType (objType);
        
        if (ai != null)
        {
            
            this.contentBox.remove (ai);
            
            this.validate ();
            this.repaint ();
            
            this.updateSectionsList ();
            
        }
        
    }
 
    private Set<AccordionItem> getAccordionItems ()
    {
        
        Set<AccordionItem> ret = new LinkedHashSet ();
        
        Component[] comps = this.contentBox.getComponents ();
        
        for (int i = 0; i < comps.length; i++)
        {
            
            Component comp = comps[i];
            
            if (comp instanceof AccordionItem)
            {
            
                AccordionItem pi = (AccordionItem) comp;

                ret.add (pi);

            }
            
        }
        
        return ret;
        
    }
 
    private void addAddSectionMenu (final JPopupMenu m,
                                    final String     belowObjType)
    {
        
        final ProjectSideBar _this = this;
        
        // Get all the sections currently not visible.
        Set<String> defSections = this.getSections (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);
    
        Set<String> sections = this.getSections (Constants.PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);
        
        defSections.removeAll (sections);
        
        if (defSections.size () > 0)
        {
    
            JMenu sm = new JMenu ("Add section" + (belowObjType != null ? " below" : ""));
            
            m.add (sm);
            
            for (String sect : defSections)
            {
    
                final String _sect = sect;
    
                sm.add (UIUtils.createMenuItem (Environment.getObjectTypeNamePlural (sect),
                                                sect,
                                                new ActionListener ()
                                                {
                                                
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        AccordionItem it = _this.createAccordionItemForObjectType (_sect);
    
                                                        _this.addAccordionItem (it,
                                                                                belowObjType);
                                                        
                                                        _this.validate ();
                                                        _this.repaint ();
    
                                                        if (belowObjType != null)
                                                        {
                                                        
                                                            // Scroll the item into view.
                                                            _this.contentBox.scrollRectToVisible (it.getBounds (null));
    
                                                        }
                                                                                                                    
                                                        _this.updateSectionsList ();
                                                        
                                                    }
                                                
                                                }));
                
            }
            
        }
                
    }
 
    private void addHideSectionMenuItem (final JPopupMenu m,
                                         final String     objType)
    {
        
        final ProjectSideBar _this = this;
        
        m.addSeparator ();

        m.add (UIUtils.createMenuItem ("Hide this section",
                                       Constants.CLOSE_ICON_NAME,
                                       new ActionListener ()
                                       {
                                        
                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                _this.removeSection (objType);
                                                
                                            }
                                        
                                       }));
        
    }
 
    private AccordionItem createAccordionItemForObjectType (final String objType)
    {
        
        final ProjectSideBar _this = this;
        
        if (objType.equals (Chapter.OBJECT_TYPE))
        {
            
            return new ChaptersAccordionItem (this.viewer)
            {

                @Override                
                public void fillHeaderPopupMenu (JPopupMenu m,
                                                 MouseEvent ev)
                {
                
                    super.fillHeaderPopupMenu (m,
                                               ev);
                    
                    _this.addHideSectionMenuItem (m,
                                                  objType);
                                    
                    _this.addAddSectionMenu (m,
                                             objType);
                                    
                }
                
            };
                                        
        }
        
        if (objType.equals (ProjectEditor.OBJECT_TYPE))
        {
                        
            return new ProjectEditorsAccordionItem (this.viewer)
            {

                @Override                
                public void fillHeaderPopupMenu (JPopupMenu m,
                                                 MouseEvent ev)
                {
                
                    super.fillHeaderPopupMenu (m,
                                               ev);
                    
                    _this.addHideSectionMenuItem (m,
                                                  objType);

                    _this.addAddSectionMenu (m,
                                             objType);
                                    
                }
                
            };
            
        }
        
        if (this.assetObjTypes.contains (objType))
        {
            
            return new AssetAccordionItem (objType,
                                           this.viewer)
            {

                @Override                
                public void fillHeaderPopupMenu (JPopupMenu m,
                                                 MouseEvent ev)
                {
                
                    super.fillHeaderPopupMenu (m,
                                               ev);
                    
                    _this.addHideSectionMenuItem (m,
                                                  objType);

                    _this.addAddSectionMenu (m,
                                             objType);
                                    
                }
            
            };
                    
        }

        if (objType.equals (Note.OBJECT_TYPE))
        {
    
            return new NotesAccordionItem (this.viewer)
            {

                @Override                
                public void fillHeaderPopupMenu (JPopupMenu m,
                                                 MouseEvent ev)
                {
                
                    super.fillHeaderPopupMenu (m,
                                               ev);
                    
                    _this.addHideSectionMenuItem (m,
                                                  objType);

                    _this.addAddSectionMenu (m,
                                             objType);
                                    
                }
                
            };
        
        }
        
        return null;
        
    }
 
    private Set<String> getSections (String propName)
    {
        
        Set<String> objTypes = new LinkedHashSet ();
        
        // Get the object types.
        String v = UserProperties.get (propName);

        StringTokenizer t = new StringTokenizer (v,
                                                 "|");
        
        while (t.hasMoreTokens ())
        {
            
            objTypes.add (t.nextToken ().trim ());
            
        }

        return objTypes;
        
    }
 
    @Override
    public void init ()
               throws GeneralException
    {
        
        super.init ();
        
        for (String objType : this.getSections (Constants.PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME))
        {
            
            AccordionItem it = this.createAccordionItemForObjectType (objType);
            
            if (it == null)
            {
                
                continue;
                
            }
            
            this.addAccordionItem (it);
                        
        }

    }
 
    public void setObjectsOpen (String objType)
    {
        
        AccordionItem ai = this.getAccordionItemForObjectType (objType);
        
        if (ai == null)
        {
            
            return;
            
        }
        
        ai.setContentVisible (true);

    }
 
    public void initOpenObjectTypes (final Set<String> types)
    {
        
        Set<AccordionItem> ais = this.getAccordionItems ();
        
        for (AccordionItem ai : ais)
        {
            
            ai.setContentVisible (types.contains (ai.getId ()));

        }
        
    }
    
}