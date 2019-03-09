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
import java.util.Collection;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.actionHandlers.*;
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
import com.quollwriter.ui.components.QPopup;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class ProjectSideBar extends AbstractSideBar<ProjectViewer>
{

    private JScrollPane content = null;
    private JComponent contentBox = null;

    public static DataFlavor COMPONENT_FLAVOR = null;
    private Set<String> legacyAssetObjTypes = null;

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

                for (AccordionItem it : _this.getAccordionItems ())
                {

                    _this.setSelected (it,
                                       false);

                }

                return true;

            }

            @Override
            public void exportDone (JComponent   c,
                                    Transferable t,
                                    int          action)
            {

            }

        });

        this.contentBox.setToolTipText (Environment.getUIString (LanguageStrings.project,
                                                                 LanguageStrings.sidebar,
                                                                 LanguageStrings.tooltip));
        //"Double click to add a new type of Object, right click to see the menu.");
        this.contentBox.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handleDoublePress (MouseEvent ev)
            {

                UIUtils.showAddNewObjectType (_this.viewer);

            }
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                _this.addAddSectionMenu (m,
                                         null);

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

        this.legacyAssetObjTypes = aObjTypes;

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

        return getUIString (project,sidebar,title);
        //"{Project}";

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

    @Override
    public String getSaveState ()
    {

        Map ss = new HashMap ();

        StringBuilder ats = new StringBuilder ();

        Set<AccordionItem> ais = this.getAccordionItems ();

        Map<String, String> aisState = new LinkedHashMap ();

        Set<String> aiIds = new LinkedHashSet ();

        ss.put ("ids", aiIds);
        ss.put ("aistate", aisState);

        for (AccordionItem ai : ais)
        {

            if (ai.getId () != null)
            {

                aiIds.add (ai.getId ());

                aisState.put (ai.getId (),
                              ai.getSaveState ());

            }

        }

        try
        {

            return JSONEncoder.encode (ss);

        } catch (Exception e) {

            Environment.logError ("Unable to encode state: " +
                                  aisState,
                                  e);

            return "";

        }
/*
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
  */
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

    private void setSelected (AccordionItem it,
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

        this.validate ();
        this.repaint ();

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

        final ProjectSideBar _this = this;

        // Used to initiate the drag.
        MouseEventHandler listener = new MouseEventHandler ()
        {

            @Override
            public void mouseDragged(MouseEvent e)
            {

                JComponent c = (JComponent) e.getSource ();
                TransferHandler handler = item.getTransferHandler ();
                handler.setDragImage (UIUtils.getImageOfComponent (item.getHeader (),
                                                                item.getHeader ().getWidth (),
                                                                item.getHeader ().getHeight ()));

                handler.exportAsDrag (item,
                                      e,
                                      TransferHandler.MOVE);

            }

        };

        // TODO: Make this a single instance.
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

                for (AccordionItem _it : _this.getAccordionItems ())
                {

                    _this.setSelected (_it,
                                       false);

                }

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
        /*
            @Override
            public java.awt.Image getDragImage ()
            {

                JComponent c = item.getHeader ();

                this.dragImage = UIUtils.getImageOfComponent (c,
                                                                c.getWidth (),
                                                                c.getHeight ());

                return this.dragImage;

            }
        */
            @Override
            public int getSourceActions (JComponent c)
            {
                                    /*
                c = item.getHeader ();

                this.dragImage = UIUtils.getImageOfComponent (c,
                                                                c.getWidth (),
                                                                c.getHeight ());

                this.setDragImage (this.dragImage);
        */
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

            }

            @Override
            public void drop (DropTargetDropEvent ev)
            {

                for (AccordionItem it : _this.getAccordionItems ())
                {

                    _this.setSelected (it,
                                       false);

                }

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

                for (AccordionItem it : _this.getAccordionItems ())
                {

                    _this.setSelected (it,
                                       false);

                }

                //Point l = ev.getLocation ();

                AccordionItem it = (AccordionItem) ev.getDropTargetContext ().getComponent ();

                _this.setSelected (it,
                                   true);

            }

        };

        item.setTransferHandler (transferHandler);

        item.init ();

        item.getHeader ().addMouseMotionListener (listener);

        try
        {

            item.getDropTarget ().addDropTargetListener (dropTargetListener);
            item.getHeader ().getDropTarget ().addDropTargetListener (dropTargetListener);
            item.getContent ().getDropTarget ().addDropTargetListener (dropTargetListener);

        } catch (Exception e) {

        }

    }

    private AssetAccordionItem getAssetAccordionItemForObjectType (UserConfigurableObjectType type)
    {

        Component[] comps = this.contentBox.getComponents ();

        for (int i = 0; i < comps.length; i++)
        {

            Component comp = comps[i];

            if (comp instanceof AssetAccordionItem)
            {

                AssetAccordionItem pi = (AssetAccordionItem) comp;

                if (pi.getUserConfigurableObjectType ().equals (type))
                {

                    return pi;

                }

            }

        }

        return null;

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

    public void removeTagSection (Tag tag)
    {

        this.removeSection (TaggedObjectAccordionItem.ID_PREFIX + tag.getKey ());

    }

    public void removeSection (UserConfigurableObjectType objType)
    {

        if (objType.isAssetObjectType ())
        {

            AssetAccordionItem aai = this.getAssetAccordionItemForObjectType (objType);

            if (aai != null)
            {

                this.contentBox.remove (aai);

                this.validate ();
                this.repaint ();

            }

        }

    }

    private void removeSection (String objType)
    {

        AccordionItem ai = this.getAccordionItemForObjectType (objType);

        if (ai != null)
        {

            this.contentBox.remove (ai);

            this.validate ();
            this.repaint ();

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

        if (m.getComponentCount () > 0)
        {

            m.addSeparator ();

        }

        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.headerpopupmenu);
        prefix.add (LanguageStrings.items);

        m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                LanguageStrings.addtag),
                                       //"Add/Manage the Tag(s)",
                                       Constants.EDIT_ICON_NAME,
                                       new ActionListener ()
                                       {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.viewer.showEditTags ();

                                            }

                                       }));

        m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                LanguageStrings.newobject),
                                                                //"Add new Type of Object",
                                       Constants.NEW_ICON_NAME,
                                       new ActionListener ()
                                       {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                UIUtils.showAddNewObjectType (_this.viewer);

                                            }

                                       }));

        // Get all the sections currently not visible.
        Set<String> defSections = this.getSections (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);

        // Get all the user asset types.
        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (false);

        for (UserConfigurableObjectType t : types)
        {

            defSections.add (t.getObjectTypeId ());

        }

        Set<Tag> tags = null;

        try
        {

            tags = Environment.getAllTags ();

        } catch (Exception e) {

            tags = new HashSet ();

            Environment.logError ("Unable to get all tags",
                                  e);

        }

        for (Tag t : tags)
        {

            defSections.add (TaggedObjectAccordionItem.ID_PREFIX + t.getKey ());

        }

        for (AccordionItem ai : this.getAccordionItems ())
        {

            defSections.remove (ai.getId ());

        }

        if (defSections.size () > 0)
        {

            JMenu sm = new JMenu (Environment.getUIString (prefix,
                                                           LanguageStrings.addsection,
                                                           (belowObjType != null ? LanguageStrings.below : LanguageStrings.normal)));
                                  //"Add section" + (belowObjType != null ? " below" : ""));

            for (final String sect : defSections)
            {

                // TODO: Need a better way to handle this.
                String mName = this.getMenuNameForObjectType (sect);

                if (mName == null)
                {

                    continue;

                }

                sm.add (UIUtils.createMenuItem (Environment.replaceObjectNames (mName),
                                                this.getMenuIconForObjectType (sect),
                                                new ActionListener ()
                                                {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        AccordionItem it = _this.createAccordionItemForObjectType (sect);

                                                        _this.addAccordionItem (it,
                                                                                belowObjType);

                                                        _this.validate ();
                                                        _this.repaint ();

                                                        if (belowObjType != null)
                                                        {

                                                            // Scroll the item into view.
                                                            _this.contentBox.scrollRectToVisible (it.getBounds (null));

                                                        }

                                                    }

                                                }));

            }

            // Only add the section menu if there are children.
            if (sm.getMenuComponentCount () > 0)
            {

                m.add (sm);

            }

        }

    }

    private void addHideSectionMenuItem (final JPopupMenu m,
                                         final String     objType)
    {

        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.headerpopupmenu);
        prefix.add (LanguageStrings.items);

        final ProjectSideBar _this = this;

        m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                LanguageStrings.hidesection),
                                                           //"Hide this section",
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

    private String getMenuNameForObjectType (String objType)
    {

        String name = null;
        int count = -1;

        if (objType.startsWith (TaggedObjectAccordionItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (TaggedObjectAccordionItem.ID_PREFIX.length ()));

            } catch (Exception e) {

                // Ignore.
                Environment.logError ("Unable to get key for: " +
                                      objType,
                                      e);

                return null;

            }

            Tag tag = null;

            try
            {

                tag = Environment.getTagByKey (key);

            } catch (Exception e) {

                Environment.logError ("Unable to get type for: " +
                                      key,
                                      e);

                return null;

            }

            count = this.viewer.getProject ().getAllObjectsWithTag (tag).size ();

            name = tag.getName ();

        }

        if (ProjectEditor.OBJECT_TYPE.equals (objType))
        {

            name = "{Editors}";

            Set eds = this.viewer.getProject ().getProjectEditors ();

            if (eds != null)
            {

                count = eds.size ();

            }

        }

        if (Note.OBJECT_TYPE.equals (objType))
        {

            name = Environment.getObjectTypeNamePlural (objType);

            count = this.viewer.getProject ().getAllNamedChildObjects (Note.class).size ();

        }

        if (Chapter.OBJECT_TYPE.equals (objType))
        {

            UserConfigurableObjectType utype = Environment.getUserConfigurableObjectType (objType);

            name = utype.getObjectTypeNamePlural ();

            count = this.viewer.getProject ().getAllNamedChildObjects (Chapter.class).size ();

        }

        if (this.legacyAssetObjTypes.contains (objType))
        {

            UserConfigurableObjectType utype = Environment.getUserConfigurableObjectType (objType);

            if (utype == null)
            {

                return null;

            }

            name = utype.getObjectTypeNamePlural ();

            count = this.viewer.getProject ().getAllNamedChildObjects (utype).size ();

        }

        if (objType.startsWith (AssetAccordionItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (AssetAccordionItem.ID_PREFIX.length ()));

            } catch (Exception e) {

                // Ignore.
                Environment.logError ("Unable to get key for: " +
                                      objType,
                                      e);

                return null;

            }

            UserConfigurableObjectType t = null;

            try
            {

                t = Environment.getUserConfigurableObjectType (key);

            } catch (Exception e) {

                Environment.logError ("Unable to get type for: " +
                                      key,
                                      e);

                return null;

            }

            name = t.getObjectTypeNamePlural ();

            count = this.viewer.getProject ().getAllNamedChildObjects (t).size ();

        }

        if (count > -1)
        {

            name = String.format ("%s (%s)",
                                  name,
                                  Environment.formatNumber (count));

        }

        return name;

    }

    private ImageIcon getMenuIconForObjectType (String objType)
    {

        if (objType.startsWith (TaggedObjectAccordionItem.ID_PREFIX))
        {

            objType = Constants.TAG_ICON_NAME;

        }

        if (objType.startsWith (AssetAccordionItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (AssetAccordionItem.ID_PREFIX.length ()));

            } catch (Exception e) {

                // Ignore.
                Environment.logError ("Unable to get key for: " +
                                      objType,
                                      e);

                return null;

            }

            UserConfigurableObjectType t = null;

            try
            {

                t = Environment.getUserConfigurableObjectType (key);

            } catch (Exception e) {

                Environment.logError ("Unable to get type for: " +
                                      key,
                                      e);

                return null;

            }

            if (t != null)
            {

                return t.getIcon16x16 ();

            }

        }

        if (ProjectEditor.OBJECT_TYPE.equals (objType))
        {

            objType = Constants.EDIT_ICON_NAME;

        }

        if (this.legacyAssetObjTypes.contains (objType))
        {

            return Environment.getUserConfigurableObjectType (objType).getIcon16x16 ();

        }

        return Environment.getIcon (objType,
                                    Constants.ICON_MENU);

    }

    public AccordionItem createAssetAccordionItem (final UserConfigurableObjectType type)
    {

        // Type can be null, i.e. it has been deleted elsewhere.
        if (type == null)
        {

            return null;

        }

        if (!type.isAssetObjectType ())
        {

            throw new IllegalArgumentException ("Type is not for assets: " +
                                                type);

        }

        final ProjectSideBar _this = this;

        return new AssetAccordionItem (type,
                                       this.viewer)
        {

            @Override
            public void fillHeaderPopupMenu (JPopupMenu m,
                                             MouseEvent ev)
            {

                super.fillHeaderPopupMenu (m,
                                           ev);

                _this.addAddSectionMenu (m,
                                         type.getObjectTypeId ());

                _this.addHideSectionMenuItem (m,
                                              type.getObjectTypeId ());

                m.add (UIUtils.createMenuItem (String.format (Environment.getUIString (LanguageStrings.project,
                                                                                       LanguageStrings.sidebar,
                                                                                       LanguageStrings.assets,
                                                                                       LanguageStrings.headerpopupmenu,
                                                                                       LanguageStrings.items,
                                                                                       LanguageStrings.deleteall),
                                                              type.getObjectTypeNamePlural ()),
                                               //"Delete all " + type.getObjectTypeNamePlural (),
                                               Constants.DELETE_ICON_NAME,
                                               new ActionListener ()
                                               {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        new YesDeleteConfirmTextInputActionHandler<ProjectViewer> (viewer,
                                                                                                                   null)
                                                        {

                                                            @Override
                                                            public String getHelp ()
                                                            {

                                                                return String.format (Environment.getUIString (LanguageStrings.assets,
                                                                                                               LanguageStrings.deleteall,
                                                                                                               LanguageStrings.text),
                                                                                      type.getObjectTypeNamePlural ());
/*
                                                                String.format ("To delete all <b>%s</b> please enter the word <b>Yes</b> into the box below.<br /><br />Warning!  All <b>%s</b> you have created will be deleted and <b>%s</b> won't be available in your other {projects}.",
                                                                                      type.getObjectTypeNamePlural (),
                                                                                      type.getObjectTypeNamePlural (),
                                                                                      type.getObjectTypeNamePlural ());
                                                                */
                                                            }

                                                            @Override
                                                            public String getDeleteType ()
                                                            {

                                                                return type.getObjectTypeNamePlural ();

                                                            }

                                                            public boolean onConfirm (String v)
                                                                                      throws Exception
                                                            {

                                                                try
                                                                {

                                                                    _this.viewer.deleteAllAssetsOfType (type);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to remove all: " +
                                                                                          type,
                                                                                          e);

                                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                                              getUIString (assets,deleteall,actionerror));
                                                                                              //String.format ("Unable to remove all %1$s.",
                                                                                                //             type.getObjectTypeNamePlural ()));

                                                                    return false;

                                                                }

                                                                try
                                                                {

                                                                    Environment.removeUserConfigurableObjectType (type);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to remove user object type: " +
                                                                                          type,
                                                                                          e);

                                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                                              Environment.getUIString (LanguageStrings.assets,
                                                                                                                       LanguageStrings.deleteall,
                                                                                                                       LanguageStrings.actionerror));
                                                                                              //"Unable to remove object.");

                                                                    return false;

                                                                }

                                                                _this.removeSection (type);

                                                                return true;

                                                            }

                                                        }.actionPerformed (new ActionEvent (type, 1, "show"));

                                                    }

                                               }));

            }

        };

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

        if (objType.startsWith (TaggedObjectAccordionItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (TaggedObjectAccordionItem.ID_PREFIX.length ()));

            } catch (Exception e) {

                // Ignore.
                Environment.logError ("Unable to get key for: " +
                                      objType,
                                      e);

                return null;

            }

            Tag tag = null;

            try
            {

                tag = Environment.getTagByKey (key);

            } catch (Exception e) {

                Environment.logError ("Unable to get type for: " +
                                      key,
                                      e);

                return null;

            }

            if (tag == null)
            {

                return null;

            }

            return new TaggedObjectAccordionItem (tag,
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

        if (objType.startsWith (AssetAccordionItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (AssetAccordionItem.ID_PREFIX.length ()));

            } catch (Exception e) {

                // Ignore.
                Environment.logError ("Unable to get key for: " +
                                      objType,
                                      e);

                return null;

            }

            UserConfigurableObjectType t = null;

            try
            {

                t = Environment.getUserConfigurableObjectType (key);

            } catch (Exception e) {

                Environment.logError ("Unable to get type for: " +
                                      key,
                                      e);

                return null;

            }

            if (t != null)
            {

                return this.createAssetAccordionItem (t);

            } else {

                return null;

            }

        }

        // Pre-v2.6
        if (this.legacyAssetObjTypes.contains (objType))
        {

            return this.createAssetAccordionItem (Environment.getUserConfigurableObjectType (objType));

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
    public void init (String saveState)
               throws GeneralException
    {

        super.init (saveState);

        if (saveState != null)
        {

            Map<String, Object> stateO = (Map<String, Object>) JSONDecoder.decode (saveState);

            Collection<String> aiIds = (Collection<String>) stateO.get ("ids");

            if (aiIds == null)
            {

                aiIds = this.getSections (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);

            }

            Map<String, String> aiState = (Map<String, String>) stateO.get ("aistate");

            if (aiState == null)
            {

                aiState = new HashMap ();

            }

            for (String aiId : aiIds)
            {

                String state = (String) aiState.get (aiId);

                AccordionItem it = this.createAccordionItemForObjectType (aiId);

                if (it == null)
                {

                    continue;

                }

                this.addAccordionItem (it);

                if (state != null)
                {

                    it.initFromSaveState (state);

                }

            }

        } else {

            for (String objType : this.getSections (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME))
            {

                AccordionItem it = this.createAccordionItemForObjectType (objType);

                if (it == null)
                {

                    continue;

                }

                this.addAccordionItem (it);

            }

        }

        /*
        for (String objType : this.getSections (Constants.PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME))
        {

            AccordionItem it = this.createAccordionItemForObjectType (objType);

            if (it == null)
            {

                continue;

            }

            this.addAccordionItem (it);

        }
*/
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
 /*
    public void initOpenObjectTypes (final Set<String> types)
    {

        Set<AccordionItem> ais = this.getAccordionItems ();

        for (AccordionItem ai : ais)
        {

            ai.setContentVisible (types.contains (ai.getId ()));

        }

    }
 */
    @Override
    public String getId ()
    {

        return "project";

    }

}
