package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.beans.property.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.panels.*;

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

        ObservableSet<Asset> assts = pv.getProject ().getAssets (objType);

        if (assts == null)
        {

            throw new IllegalArgumentException ("Unable to find container for type: " + objType);

        }

        this.addSetChangeListener (assts,
                                   ev ->
        {

            this.reloadTree ();

        });

        this.countProp = new SimpleIntegerProperty (0);

        this.sortField = objType.getPrimaryNameField ();

        this.sorter = this.getSorter (this.sortField);

        this.tree = NamedObjectTree.builder ()
            .project (pv.getProject ())
            .root (this.createTree ())
            .withViewer (this.viewer)
            .contextMenuItemSupplier (obj ->
            {

                Asset n = (Asset) obj;

                List<String> prefix = Arrays.asList (assets,treepopupmenu,LanguageStrings.items);

                Set<MenuItem> its = new LinkedHashSet<> ();

                its.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                    .iconName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        this.viewer.viewObject (n);

                    })
                    .build ());

                its.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                    .iconName (StyleClassNames.EDIT)
                    .onAction (ev ->
                    {

                        this.viewer.editObject (n);

                    })
                    .build ());

                its.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,rename),
                                                         n.getUserConfigurableObjectType ().nameProperty ()))
                    .iconName (StyleClassNames.RENAME)
                    .onAction (ev ->
                    {

                        String pid = RenameAssetPopup.getPopupIdForAsset (n);

                        if (this.viewer.getPopupById (pid) != null)
                        {

                            return;

                        }

                        QuollPopup qp = new RenameAssetPopup (this.viewer,
                                                              n).getPopup ();

                        this.viewer.showPopup (qp,
                                               this.tree.getCellForObject (n),
                                               Side.BOTTOM);

                    })
                    .build ());

                 Menu tm = UIUtils.createTagsMenu (n,
                                                   this.viewer);

                 if (tm != null)
                 {

                     its.add (tm);

                 }

                 its.add (QuollMenuItem.builder ()
                     .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                     .iconName (StyleClassNames.DELETE)
                     .onAction (ev ->
                     {

                         this.viewer.showDeleteAsset (n);

                     })
                     .build ());

                return its;

            })
            .onDragDropped (obj ->
            {

                // Update the save state?

            })
            .canImport ((objOver, objImport) ->
            {

                if (objImport instanceof UserConfigurableObject)
                {

                    UserConfigurableObject uobj = (UserConfigurableObject) objImport;

                    if (uobj.getUserConfigurableObjectType ().equals (objType))
                    {

                        return true;

                    }

                }

                return false;

            })
            .canExport (obj -> true)
            .viewObjectOnClick (true)
            .build ();

        this.addChangeListener (this.viewer.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            this.selectItem ();

        });

        this.selectItem ();

    }

    private void selectItem ()
    {

        this.tree.select (null);
        this.tree.requestLayout ();

        Panel p = this.viewer.getCurrentPanel ();

        if (p != null)
        {

            if (p.getContent () instanceof AssetViewPanel)
            {

                AssetViewPanel nc = (AssetViewPanel) p.getContent ();

                if (nc.getObject () instanceof Asset)
                {

                    this.tree.select ((Asset) nc.getObject ());
                    this.tree.requestLayout ();

                    return;

                }

            }

        }

    }

    @Override
    public boolean canImport (NamedObject o)
    {

        if (o instanceof UserConfigurableObject)
        {

            UserConfigurableObject ut = (UserConfigurableObject) o;

            // TODO Support drag-n-drop import from other projects.
            if ((ut.getParent ().equals (this.objType.getParent ()))
                &&
                (ut.getUserConfigurableObjectType ().equals (this.objType))
                &&
                (this.tree.getTreeItemForObject (ut) == null)
               )
            {

                return true;

            }

        }

        return false;

    }

    @Override
    public void importObject (NamedObject o)
    {

        new IllegalStateException ("Shouldnt be possible.");

    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public List<String> getStyleClassNames ()
    {

        List<String> cn = new ArrayList<> ();

        cn.add (StyleClassNames.ASSET);

        if (this.objType.isLegacyObjectType ())
        {

            cn.add (this.objType.getUserObjectType ());

        }

        return cn;

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

        this.getAccordionItem ().getHeader ().getIcon ().setImage (this.objType.icon16x16Property (),
                                                                   this.getBinder ());

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
                                                     this.objType.objectTypeNameProperty ()))
                .iconName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.viewer.showAddNewAsset (this.objType);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,edit),
                                                     this.objType.objectTypeNameProperty ()))
                .iconName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    this.viewer.showEditUserConfigurableType (this.objType);

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
                                                         this.objType.objectTypeNamePluralProperty ()))
                    .styleClassName (StyleClassNames.SORT)
                    .items (its)
                    .build ();

                items.add (mi);

            }

            return items;

        };

    }

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

                    if (o1.getName () == null)
                    {

                        return -1;

                    }

                    if (o2.getName () == null)
                    {

                        return 1;

                    }

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

    private void reloadTree ()
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

        this.tree.setRoot (this.createTree ());

        this.tree.requestLayout ();

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

}
