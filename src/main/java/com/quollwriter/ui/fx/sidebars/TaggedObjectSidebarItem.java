package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.input.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TaggedObjectSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    //public static final DataFormat PROJECT_OBJECT_DATA_FORMAT = new DataFormat ("project/object");

    public static final String ID_PREFIX = "tag:";

    private Comparator<NamedObject> sorter = null;
    private Tag tag = null;
    private Set<NamedObject> items = null;
    private IntegerProperty count = new SimpleIntegerProperty (0);
    private QuollTreeView<NamedObject> tree = null;

    public TaggedObjectSidebarItem (Tag             tag,
                                    IPropertyBinder binder,
                                    ProjectViewer   pv)
    {

        super (pv,
               binder);

        TaggedObjectSidebarItem _this = this;

        this.tag = tag;

        this.sorter = new NamedObjectSorter (pv.getProject ());

        ObservableSet<NamedObject> its = this.viewer.getProject ().getAllObjectsWithTag (this.tag);

        this.items = its;

        this.addSetChangeListener (its,
                                   ev ->
        {

            // We are just adding a change listener here to help us trigger a reload.
            // TODO Add for specific add/removes then update tree as necessary.
            this.reloadTree ();

        });

        this.tree = NamedObjectTree.builder ()
            .labelProvider (treeItem ->
            {

                NamedObject n = treeItem.getValue ();

                QuollLabel l = null;

                if ((n instanceof UserConfigurableObject)
                    &&
                    (!(n instanceof Chapter))
                   )
                {

                    UserConfigurableObject nu = (UserConfigurableObject) n;

                    UserConfigurableObjectType uc = nu.getUserConfigurableObjectType ();

                    QuollLabel nl = QuollLabel.builder ()
                        .label (n.nameProperty ())
                        .build ();

                    ImageView iv = new ImageView ();
                    iv.imageProperty ().bind (uc.icon16x16Property ());

                    nl.setGraphic (iv);

                    l = nl;

                } else {

                    l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .styleClassName (n.getObjectType ())
                    .build ();

                }

                return l;

            })
            .onDragDropped (obj ->
            {

                try
                {

                    obj.addTag (this.tag);

                    this.viewer.saveObject (obj,
                                            true);

                } catch (Exception e) {

                    Environment.logError ("Unable to add tag: " +
                                          this.tag,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (project,actions,addtag,actionerror));
                                              //"Unable to remove tag.");

                }

            })
            .project (this.viewer.getProject ())
            .canImport ((objOver, objImport) ->
            {

                // TODO
                return true;

            })
            .canExport (obj ->
            {

                // TODO
                return true;

            })
            .viewObjectOnClick (true)
            .withViewer (this.viewer)
            .contextMenuItemSupplier (obj ->
            {

                List<String> prefix = Arrays.asList (project,sidebar,tags,treepopupmenu,LanguageStrings.items);

                Set<MenuItem> _its = new LinkedHashSet<> ();

                _its.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                    .iconName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        this.viewer.viewObject (obj);

                    })
                    .build ());

                Menu tm = UIUtils.createTagsMenu (obj,
                                                  this.viewer);

                if (tm != null)
                {

                    _its.add (tm);

                }

                _its.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,remove)))
                    .iconName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        try
                        {

                            obj.removeTag (_this.tag);

                            _this.viewer.saveObject (obj,
                                                     true);

                            _this.reloadTree ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to remove tag: " +
                                                  _this.tag,
                                                  e);

                            ComponentUtils.showErrorMessage (_this.viewer,
                                                             getUILanguageStringProperty (project,actions,deletetag,actionerror));
                                                      //"Unable to remove tag.");

                        }

                    })
                    .build ());

                return _its;

            })
            .build ();

    }

    @Override
    public boolean canImport (NamedObject o)
    {

        return !o.hasTag (this.tag);

    }

    @Override
    public void importObject (NamedObject o)
    {

        try
        {

            o.addTag (this.tag);

            this.viewer.saveObject (o,
                                    true);

            //this.reloadTree ();

        } catch (Exception e) {

            Environment.logError ("Unable to add tag: " +
                                  this.tag,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (project,actions,addtag,actionerror));

        }

    }

    @Override
    public StringProperty getTitle ()
    {

        return this.tag.nameProperty ();

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.count;

    }

    @Override
    public List<String> getStyleClassNames ()
    {

        return Arrays.asList (StyleClassNames.TAG);

    }
/*
    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        // TODO: Change to use a name change instead.
        if ((this.tag.equals (ev.getContextObject ()))
            &&
            (ev.getType ().equals (ProjectEvent.TAG))
            &&
            (ev.getAction ().equals (ProjectEvent.CHANGED))
           )
        {

            this.updateTitle ();

        }

    }
*/
    @Override
    public String getId ()
    {

        return ID_PREFIX + this.tag.getKey ();

    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public void init (State ss)
    {

        super.init (ss);

        final TaggedObjectSidebarItem _this = this;

        if (ss != null)
        {

            Set<String> objRefs = ss.getAsSet ("order",
                                               String.class);

            if (objRefs != null)
            {

                final Map<String, Number> order = new HashMap<> ();

                int c = 0;

                for (String ref : objRefs)
                {

                    order.put (ref,
                               c++);

                }

                this.sorter = new Comparator<NamedObject> ()
                {

                    @Override
                    public boolean equals (Object o)
                    {

                        return this.equals (o);

                    }

                    @Override
                    public int compare (NamedObject o1,
                                        NamedObject o2)
                    {

                        Number nv1 = order.get (o1.getObjectReference ().asString ());

                        Number nv2 = order.get (o2.getObjectReference ().asString ());

                        if ((nv1 == null)
                            ||
                            (nv2 == null)
                           )
                        {

                            return new NamedObjectSorter (_this.viewer.getProject ()).compare (o1, o2);

                        }

                        return nv1.intValue () - nv2.intValue ();

                    }

                };

            }

        }

        this.reloadTree ();

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        if (s == null)
        {

            s = new State ();

        }

        s.set ("order",
               this.tree.getRoot ().getChildren ().stream ()
                    .map (c -> c.getValue ().getObjectReference ().asString ())
                    .collect (Collectors.toList ()));

        return s;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        TaggedObjectSidebarItem _this = this;

        List<String> prefix = Arrays.asList (project,sidebar,tags,headerpopupmenu,LanguageStrings.items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,rename)))
                .iconName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    QuollPopup qp = this.viewer.getPopupById (RenameTagPopup.getPopupIdForTag (this.tag));

                    if (qp != null)
                    {

                        return;

                    }

                    new RenameTagPopup (this.viewer,
                                        this.tag).show ();

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    String pid = "deletetag" + this.tag.getKey ();

                    QuollPopup qp = this.viewer.getPopupById (pid);

                    if (qp != null)
                    {

                        return;

                    }

                    List<String> prefix2 = Arrays.asList (project,actions,deletetag);

                    Set<Button> buttons = new LinkedHashSet<> ();

                    buttons.add (QuollButton.builder ()
                                    .label (getUILanguageStringProperty (Utils.newList (prefix2,allprojects)))
                                    .onAction (eev ->
                                    {

                                        try
                                        {

                                            _this.viewer.deleteTag (_this.tag);

                                            _this.viewer.getPopupById (pid).close ();

                                        } catch (Exception e) {

                                            Environment.logError ("Unable to delete tag: " +
                                                                  _this.tag,
                                                                  e);

                                            ComponentUtils.showErrorMessage (_this.viewer,
                                                                             getUILanguageStringProperty (Utils.newList (prefix2,actionerror)));
                                                                      //"Unable to delete tag.");

                                        }

                                    })
                                    .build ());

                    buttons.add (QuollButton.builder ()
                                    .label (getUILanguageStringProperty (Utils.newList (prefix2,thisproject)))
                                    .onAction (eev ->
                                    {

                                        try
                                        {

                                            _this.viewer.removeTag (_this.tag);

                                            _this.viewer.getPopupById (pid).close ();

                                        } catch (Exception e) {

                                            Environment.logError ("Unable to remove tag: " +
                                                                  _this.tag,
                                                                  e);

                                            ComponentUtils.showErrorMessage (_this.viewer,
                                                                             getUILanguageStringProperty (prefix2,actionerror));
                                                                      //"Unable to delete tag.");

                                        }

                                    })
                                    .build ());

                    QuollPopup.questionBuilder ()
                        .withViewer (_this.viewer)
                        .popupId (pid)
                        .title (getUILanguageStringProperty (Utils.newList (prefix2,title)))
                        .styleClassName (StyleClassNames.DELETE)
                        .message (getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                               _this.tag.nameProperty ()))
                        .buttons (buttons)
                        .build ();

                })
                .build ());

            return items;

        };

    }

    public void reloadTree ()
    {

        Set<NamedObject> its = this.items;

        if (this.sorter != null)
        {

            List<NamedObject> sobjs = new ArrayList<> (its);

            Collections.sort (sobjs,
                              this.sorter);

            its = new LinkedHashSet<> (sobjs);

        }

        this.count.setValue (its.size ());

        // Set the root of the tree.
        TreeItem<NamedObject> root = new TreeItem<> (this.viewer.getProject ());

        for (NamedObject item : its)
        {

            root.getChildren ().add (new TreeItem<NamedObject> (item));

        }

        this.tree.setRoot (root);

        this.tree.requestLayout ();

    }

    public BooleanProperty showItemCountOnHeader ()
    {

        return new SimpleBooleanProperty (true);

    }

}
