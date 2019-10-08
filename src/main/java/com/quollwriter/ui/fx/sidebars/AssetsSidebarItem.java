package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.beans.property.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AssetsSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    public static final String ID_PREFIX = "asset:";

    private UserConfigurableObjectType objType = null;
    private UserConfigurableObjectTypeField sortField = null;
    private Comparator<Asset> sorter = null;
    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;

    public AssetsSidebarItem (UserConfigurableObjectType objType,
                              IPropertyBinder            binder,
                              ProjectViewer              pv)
    {

        super (pv,
               binder);

        this.objType = objType;

        // TODO Have better handling of the changes.
        ObservableSet<Asset> assts = pv.getProject ().getAssets (objType);

        this.addSetChangeListener (assts,
                                   ev -> this.reloadTree ());

        this.countProp = new SimpleIntegerProperty (0);

        this.sortField = objType.getPrimaryNameField ();

        this.sorter = this.getSorter (this.sortField);

        this.tree = new QuollTreeView<> ();
        this.tree.setShowRoot (false);
        this.tree.setCellProvider (treeItem ->
        {

            if (treeItem.getValue () instanceof Project)
            {

                return new Label ();

            }

            Asset n = (Asset) treeItem.getValue ();

            QuollLabel l = QuollLabel.builder ()
                .label (n.nameProperty ())
                .styleClassName (n.getObjectType ())
                .build ();

            //l.getGraphic ().managedProperty ().bind (l.getGraphic ().visibleProperty ());

            List<String> prefix = Arrays.asList (assets,treepopupmenu,LanguageStrings.items);

            ContextMenu m = new ContextMenu ();

            m.getItems ().add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                .styleClassName (StyleClassNames.VIEW)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                            n);

                })
                .build ());

            m.getItems ().add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                .styleClassName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.editasset,
                                            n);

                })
                .build ());

             Menu tm = UIUtils.createTagsMenu (n,
                                               this.viewer);

             if (tm != null)
             {

                 m.getItems ().add (tm);

             }

             m.getItems ().add (QuollMenuItem.builder ()
                 .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                 .styleClassName (StyleClassNames.EDIT)
                 .onAction (ev ->
                 {

                     this.viewer.runCommand (ProjectViewer.CommandId.deleteasset,
                                             n);

                 })
                 .build ());

            l.setContextMenu (m);

            return l;

        });

    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.ASSET;

    }

    @Override
    public StringProperty getTitle ()
    {

        return this.objType.objectTypeNamePluralProperty ();

    }

    @Override
    public String getId ()
    {

        return this.objType.getObjectTypeId ();

    }

    public UserConfigurableObjectType getUserConfigurableObjectType ()
    {

        return this.objType;

    }

    @Override
    public void init (State ss)
    {

        super.init (ss);

        this.getAccordionItem ().getHeader ().getIcon ().imageProperty ().bind (this.objType.icon16x16Property ());

        Number sortFieldKey = (ss != null ? ss.getAsInt ("sortFieldKey") : null);

        if (sortFieldKey != null)
        {

            try
            {

                this.sortBy (Environment.getUserConfigurableObjectTypeField (sortFieldKey.longValue ()));

            } catch (Exception e) {

                Environment.logError ("Unable to get user config object type field: " +
                                      sortFieldKey,
                                      e);

            }

        } else {

            Set<String> objRefs = (ss != null ? ss.getAsSet ("order",
                                                             String.class) : null);

            if (objRefs != null)
            {

                final Map<String, Number> order = new HashMap ();

                int c = 0;

                for (String ref : objRefs)
                {

                    order.put (ref,
                               c++);

                }

                this.sorter = new Comparator<Asset> ()
                {

                    @Override
                    public boolean equals (Object o)
                    {

                        return this.equals (o);

                    }

                    @Override
                    public int compare (Asset o1,
                                        Asset o2)
                    {

                        Number nv1 = order.get (o1.getObjectReference ().asString ());

                        Number nv2 = order.get (o2.getObjectReference ().asString ());

                        if (nv1 == null)
                        {

                            nv1 = new Integer (Short.MAX_VALUE);

                        }

                        if (nv2 == null)
                        {

                            nv2 = new Integer (Short.MAX_VALUE);

                        }

                        return nv1.intValue () - nv2.intValue ();

                    }

                };

            }

            this.reloadTree ();

        }

    }

    @Override
    public BooleanProperty showItemCountOnHeader ()
    {

        // TODO Make a style?
        return new SimpleBooleanProperty (true);

    }

    @Override
    public State getState ()
    {

        State ss = super.getState ();

        if (ss == null)
        {

            ss = new State ();

        }

        if (this.sortField != null)
        {

            ss.set ("sortFieldKey",
                    this.sortField.getKey ());

        } else {

            ss.set ("order",
                    this.tree.getRoot ().getChildren ().stream ()
                         .map (c -> c.getValue ().getObjectReference ().asString ())
                         .collect (Collectors.toList ()));

        }

        return ss;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        List<String> prefix = Arrays.asList (assets,headerpopupmenu,LanguageStrings.items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings._new),
                                                          //"Add a new %s",
                                                     this.objType.getObjectTypeName ()))
                .styleClassName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    /*
                    AbstractAction addNewItem = UIUtils.createAddAssetActionListener (this.objType,
                                                                                      this.viewer,
                                                                                      null,
                                                                                      null);
*/


                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,edit),
                                                     this.objType.getObjectTypeName ()))
                .styleClassName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    UIUtils.showObjectTypeEdit (this.objType,
                                                this.viewer);

                })
                .build ());

            // Get all the sortable fields for the object.
            Set<UserConfigurableObjectTypeField> fields = this.objType.getSortableFields ();

            if (fields.size () > 0)
            {

                Set<MenuItem> its = new LinkedHashSet<> ();

                ToggleGroup group = new ToggleGroup ();

                for (UserConfigurableObjectTypeField field : fields)
                {

                    final UserConfigurableObjectTypeField f = field;

                    RadioMenuItem i = new RadioMenuItem ();
                    // TODO Make a property.
                    i.textProperty ().bind (new SimpleStringProperty (f.getFormName ()));

                    group.getToggles ().add (i);

                    i.setOnAction (ev -> this.sortBy (f));

                    its.add (i);

                }

                QuollMenu mi = QuollMenu.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,sort),
                                                        //"Sort the %s by",
                                                         this.objType.getObjectTypeNamePlural ()))
                    .styleClassName (StyleClassNames.SORT)
                    .items (its)
                    .build ();

                items.add (mi);

            }

            return items;

        };

    }
/*
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.assets);
        prefix.add (LanguageStrings.headerpopupmenu);
        prefix.add (LanguageStrings.items);

        final AssetAccordionItem _this = this;

        AbstractAction addNewItem = UIUtils.createAddAssetActionListener (this.objType,
                                                                          this.viewer,
                                                                          null,
                                                                          null);

        m.add (UIUtils.createMenuItem (String.format (Environment.getUIString (prefix,
                                                                               LanguageStrings._new),
                                                      //"Add a new %s",
                                                      this.objType.getObjectTypeName ()),
                                       Constants.ADD_ICON_NAME,
                                       addNewItem));

        m.add (UIUtils.createMenuItem (String.format (Environment.getUIString (prefix,
                                                                               LanguageStrings.edit),
                                                      //"Edit the %s information/fields",
                                                      this.objType.getObjectTypeName ()),
                                       Constants.EDIT_ICON_NAME,
                                        new ActionListener ()
                                        {

                                             @Override
                                             public void actionPerformed (ActionEvent ev)
                                             {

                                                 UIUtils.showObjectTypeEdit (_this.objType,
                                                                             _this.viewer);

                                             }

                                        }));

        // Get all the sortable fields for the object.
        Set<UserConfigurableObjectTypeField> fields = this.objType.getSortableFields ();

        if (fields.size () > 0)
        {

            JMenu sortMenu = new JMenu (String.format (Environment.getUIString (prefix,
                                                                                LanguageStrings.sort),
                                                       //"Sort the %s by",
                                                       this.objType.getObjectTypeNamePlural ()));

            m.add (sortMenu);

            ButtonGroup group = new ButtonGroup ();

            sortMenu.setIcon (Environment.getIcon (Constants.SORT_ICON_NAME,
                                                   Constants.ICON_MENU));

            for (UserConfigurableObjectTypeField field : fields)
            {

                final UserConfigurableObjectTypeField f = field;

                JMenuItem mi = new JRadioButtonMenuItem (f.getFormName ());

                group.add (mi);

                mi.addActionListener (new ActionAdapter ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.sortBy (f);

                    }

                });

                sortMenu.add (mi);

            }

        }

    }
*/
    private Comparator<Asset> getSorter (final UserConfigurableObjectTypeField f)
    {

        return new Comparator<Asset> ()
        {

            @Override
            public boolean equals (Object o)
            {

                return this.equals (o);

            }

            @Override
            public int compare (Asset o1,
                                Asset o2)
            {

                if (f instanceof ObjectNameUserConfigurableObjectTypeField)
                {

                    return o1.getName ().toLowerCase ().compareTo (o2.getName ().toLowerCase ());

                }

                Object v1 = o1.getValueForField (f);
                Object v2 = o2.getValueForField (f);

                if ((v1 != null)
                    &&
                    (v2 == null)
                   )
                {

                    return 1;

                }

                if ((v1 == null)
                    &&
                    (v2 != null)
                   )
                {

                    return -1;

                }

                if ((v1 == null)
                    &&
                    (v2 == null)
                   )
                {

                    return 0;

                }

                if ((v1 instanceof String)
                    &&
                    (v2 instanceof String)
                   )
                {

                    return ((String) v1).toLowerCase ().compareTo (((String) v2).toLowerCase ());

                }

                if ((v1 instanceof Number)
                    &&
                    (v2 instanceof Number)
                   )
                {

                    Double d1 = ((Number) v1).doubleValue ();
                    Double d2 = ((Number) v2).doubleValue ();

                    return d1.compareTo (d2);

                }

                if ((v1 instanceof Date)
                    &&
                    (v2 instanceof Date)
                   )
                {

                    return ((Date) v1).compareTo ((Date) v2);

                }

                String sv1 = v1.toString ().toLowerCase ();
                String sv2 = v2.toString ().toLowerCase ();

                return sv1.compareTo (sv2);

            }

        };

    }

    private void sortBy (final UserConfigurableObjectTypeField f)
    {

        this.sorter = this.getSorter (f);
        this.reloadTree ();

        this.sortField = f;

    }

    @Override
    public void reloadTree ()
    {

        if (this.sortField == null)
        {

            final Map<Asset, Number> objs = new HashMap<> ();

            int i = 0;

            // Could probably use a stream here.
            for (TreeItem<NamedObject> c : this.tree.getRoot ().getChildren ())
            {

                objs.put ((Asset) c.getValue (),
                          i++);

            }

            this.sorter = new Comparator<Asset> ()
            {

                @Override
                public boolean equals (Object o)
                {

                    return this.equals (o);

                }

                @Override
                public int compare (Asset o1,
                                    Asset o2)
                {

                    Number nv1 = objs.get (o1);

                    Number nv2 = objs.get (o2);

                    if (nv1 == null)
                    {

                        nv1 = new Integer (Short.MAX_VALUE);

                    }

                    if (nv2 == null)
                    {

                        nv2 = new Integer (Short.MAX_VALUE);

                    }

                    return nv1.intValue () - nv2.intValue ();

                }

            };

        }

        // Set the root of the tree.
        this.tree.setRoot (this.createTree ());

    }

    private TreeItem<NamedObject> createTree ()
    {

        TreeItem<NamedObject> root = new TreeItem<> (this.viewer.getProject ());

        Set<Asset> assts = this.viewer.getProject ().getAssets (this.objType);

        if (assts == null)
        {

            return root;

        }

        List<Asset> objs = new ArrayList<> (assts);

        Collections.sort (objs,
                          this.sorter);

        for (Asset item : objs)
        {

            root.getChildren ().add (new TreeItem<NamedObject> (item));

        }

        this.countProp.setValue (objs.size ());

        return root;

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.countProp;

    }

    @Override
    public boolean isDragEnabled ()
    {

        return true;

    }

/*
    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv)
    {

        final AssetAccordionItem _this = this;

        return new DragActionHandler<Asset> ()
        {

            @Override
            public boolean canImportForeignObject (NamedObject obj)
            {

                // Allow support of addition of same object type.
                return false;

            }

            @Override
            public boolean importForeignObject (NamedObject obj,
                                                int         insertRow)
            {

                return false;

            }

            @Override
            public boolean handleMove (int   fromRow,
                                       int   toRow,
                                       Asset object)
                                throws GeneralException
            {

                _this.sortField = null;

                QuollPanel p = _this.viewer.getCurrentlyVisibleTab ();

                if (p != null)
                {

                    if (p instanceof ProjectObjectQuollPanel)
                    {

                        ProjectObjectQuollPanel pp = (ProjectObjectQuollPanel) p;

                        if (pp.getForObject ().equals (object))
                        {

                            return true;

                        }

                    }

                }

                _this.getTree ().removeSelectionPath (UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) _this.getTree ().getModel ().getRoot (),
                                                      object));

                return true;

            }

            @Override
            public boolean performAction (int         removeRow,
                                          NamedObject removeObject,
                                          int         insertRow,
                                          NamedObject insertObject)
                                   throws GeneralException
            {

                return true;

            }

        };

    }
*/

}
