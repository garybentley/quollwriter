package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProjectSideBar extends SideBarContent<ProjectViewer>
{

    public static final String SIDEBAR_ID = "project";

    private Set<String> legacyAssetObjTypes = null;
    private Map<String, ProjectObjectsSidebarItem> items = null;
    private VBox contentBox = null;
    private HBox toolbarBox = null;
    private Pane contentWrapper = null;
    private State state = new State ();

    public ProjectSideBar (ProjectViewer viewer)
    {

        super (viewer);

        this.items = new HashMap<> ();

        Set<String> aObjTypes = new HashSet<> ();

        aObjTypes.add (QCharacter.OBJECT_TYPE);
        aObjTypes.add (Location.OBJECT_TYPE);
        aObjTypes.add (QObject.OBJECT_TYPE);
        aObjTypes.add (ResearchItem.OBJECT_TYPE);

        this.legacyAssetObjTypes = aObjTypes;

        this.toolbarBox = new HBox ();
        this.toolbarBox.getStyleClass ().add (StyleClassNames.TOOLBAR);
        this.toolbarBox.managedProperty ().bind (this.toolbarBox.visibleProperty ());

        this.contentBox = new VBox ();
        this.contentBox.getStyleClass ().add (StyleClassNames.ITEMS);

        this.contentWrapper = new VBox ();
        ScrollPane sp = new ScrollPane (this.contentBox);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);
        this.contentWrapper.getChildren ().addAll (sp);

        this.getChildren ().add (this.contentWrapper);

        this.addSetChangeListener (Environment.getAllTags (),
                                   ev ->
        {

            Tag t = ev.getElementRemoved ();

            if (t != null)
            {

                this.removeTagSection (t);

            }

        });

        this.addChangeListener (this.viewer.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            this.updateToolbar ();

        });

    }

    private void updateToolbar ()
    {

        Panel p = this.viewer.getCurrentPanel ();

        if (p == null)
        {

            return;

        }

        // Update the toolbar.
        if (p.getContent () instanceof ToolBarSupported)
        {

            ToolBar tb = ((ToolBarSupported) p.getContent ()).getToolBar ();

            HBox.setHgrow (tb,
                           Priority.ALWAYS);
            this.toolbarBox.getChildren ().clear ();
            this.toolbarBox.getChildren ().add (tb);

            this.toolbarBox.setVisible (true);

            String loc = this.viewer.getProject ().toolbarLocationProperty ().getValue ();

            if (loc == null)
            {

                loc = Constants.BOTTOM;

            }

            this.contentWrapper.getChildren ().remove (this.toolbarBox);

            if (loc.equals (Constants.TOP))
            {

                this.contentWrapper.getChildren ().add (0,
                                                        this.toolbarBox);

            } else {

                this.contentWrapper.getChildren ().add (this.toolbarBox);

            }

            this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (loc), true);

        } else {

            this.toolbarBox.setVisible (false);

        }

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (project,LanguageStrings.sidebar,LanguageStrings.title);

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.PROJECT)
            .withScrollPane (false)
            .canClose (false)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

    @Override
    public void init (State s)
    {

        super.init (s);

        Set<String> ids = null;

        State aistate = null;

        if (s != null)
        {

            ids = s.getAsSet ("ids",
                              String.class);

            aistate = s.getAsState ("aistate");

        }

        if (aistate == null)
        {

            aistate = new State ();

        }

        this.state = aistate;

        if (ids == null)
        {

            ids = this.getSections (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);

        }

        for (String id : ids)
        {

            ProjectObjectsSidebarItem it = this.createSidebarItemForObjectType (id);

            if (it == null)
            {

                continue;

            }

            this.addSidebarItem (it);

        }

        // Add listener to toolbar location position.
        this.addChangeListener (this.viewer.getProject ().toolbarLocationProperty (),
                                (pr, oldv, newv) ->
        {

            this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (oldv), false);

            this.updateToolbar ();

        });

        this.updateToolbar ();

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        List<String> ids = new ArrayList<> ();

        this.items.values ().stream ()
            .forEach (i ->
            {

                String id = i.getId ();

                s.set (id,
                       i.getState ());

                ids.add (id);

            });

        s.set ("ids",
               ids.stream ()
                    .collect (Collectors.joining ("|")));

        return s;

    }

    public void removeTagSection (Tag tag)
    {

        this.removeSection (TaggedObjectSidebarItem.ID_PREFIX + tag.getKey ());

    }
/*
TODO Needed?
    public void removeSection (UserConfigurableObjectType objType)
    {

        if (objType.isAssetObjectType ())
        {

            AssetSidebarItem aai = this.getAssetSidebarItemForObjectType (objType);

            if (aai != null)
            {

                this.contentBox.getChildren ().remove (aai);

            }

        }

    }
*/
    private void removeSection (String objType)
    {

        AccordionItem ai = this.getAccordionItemForObjectType (objType);

        if (ai != null)
        {

            this.contentBox.getChildren ().remove (ai);
            this.items.remove (objType);

        }

    }

    private Set<MenuItem> getAddSectionMenu (final String belowObjType)
    {

        final ProjectSideBar _this = this;

        List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,headerpopupmenu,LanguageStrings.items);

        Set<MenuItem> items = new LinkedHashSet<> ();

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,addtag)))
            .styleClassName (StyleClassNames.EDIT)
            .onAction (ev ->
            {

                // TODO this.viewer.showEditTags ();

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,newobject)))
            .styleClassName (StyleClassNames.EDIT)
            .onAction (ev ->
            {

                // TODO UIUtils.showAddNewObjectType (this.viewer);

            })
            .build ());

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

            defSections.add (TaggedObjectSidebarItem.ID_PREFIX + t.getKey ());

        }

        // Remove all the sections we already have.
        defSections.removeAll (this.items.keySet ());

        if (defSections.size () > 0)
        {

            Set<MenuItem> its = new LinkedHashSet<> ();

            for (final String sect : defSections)
            {

                // TODO: Need a better way to handle this.
                StringProperty mName = this.getMenuNameForObjectType (sect);

                if (mName == null)
                {

                    continue;

                }

                String style = this.getStyleForObjectType (sect);

                QuollMenuItem mi = QuollMenuItem.builder ()
                    .label (mName)
                    .styleClassName ((style != null ? style : sect))
                    .onAction (ev ->
                    {

                        ProjectObjectsSidebarItem it = _this.createSidebarItemForObjectType (sect);

                        _this.addSidebarItem (it,
                                              belowObjType);

                        if (belowObjType != null)
                        {

                            // Scroll the item into view.
                            UIUtils.scrollIntoView (it.getAccordionItem (),
                                                    null);

                        }

                    })
                    .build ();

                if (style == null)
                {

                    Node g = getIconForObjectType (sect);

                    mi.setGraphic (g);

                } else {

                    ImageView iv = new ImageView ();
                    iv.getStyleClass ().add (style);

                    mi.setGraphic (iv);

                }

                its.add (mi);

            }

            Menu sm = QuollMenu.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,addsection, (belowObjType != null ? LanguageStrings.below : LanguageStrings.normal))))
                .styleClassName (StyleClassNames.ADD)
                .items (its)
                .build ();
                //"Add section" + (belowObjType != null ? " below" : ""));

            items.add (sm);

        }

        return items;

    }

    private MenuItem getHideSectionMenuItem (String objType)
    {

        List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,headerpopupmenu,LanguageStrings.items);

        return QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,hidesection)))
            .styleClassName (StyleClassNames.CLOSE)
            .onAction (ev ->
            {

                this.removeSection (objType);

            })
            .build ();

    }

    public AssetsSidebarItem createAssetSidebarItem (final UserConfigurableObjectType type)
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

        return new AssetsSidebarItem (type,
                                      this,
                                      this.viewer)
        {

            @Override
            public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
            {

                Set<MenuItem> items = super.getHeaderContextMenuItemSupplier ().get ();

                return () ->
                {

                    Set<MenuItem> items2 = new LinkedHashSet<> (items);

                    items2.add (_this.getHideSectionMenuItem (type.getObjectTypeId ()));

                    Set<MenuItem> its = _this.getAddSectionMenu (type.getObjectTypeId ());

                    if (its.size () > 0)
                    {

                        items2.add (new SeparatorMenuItem ());

                    }

                    items2.addAll (its);

                    items2.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,assets,headerpopupmenu,LanguageStrings.items,deleteall),
                                                             type.getObjectTypeNamePlural ()))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {
/*
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

                                        ComponentUtils.showErrorMessage (_this.viewer,
                                                                         getUILanguageStringProperty (assets,deleteall,actionerror));
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

                                        ComponentUtils.showErrorMessage (_this.viewer,
                                                                         getUILanguageStringProperty (assets,deleteall,actionerror));
                                                                  //"Unable to remove object.");

                                        return false;

                                    }

                                    _this.removeSection (type.getObjectTypeId ());

                                    return true;

                                }

                            };
*/
                        })
                        .build ());

                    return items2;

                };

            }

        };

    }

    private ProjectObjectsSidebarItem createSidebarItemForObjectType (final String objType)
    {

        final ProjectSideBar _this = this;

        if (objType.equals (Chapter.OBJECT_TYPE))
        {

            return new ChaptersSidebarItem (this.viewer,
                                            this)
            {

                @Override
                public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
                {

                    Set<MenuItem> items = super.getHeaderContextMenuItemSupplier ().get ();

                    return () ->
                    {

                        Set<MenuItem> items2 = new LinkedHashSet<> (items);

                        items2.add (_this.getHideSectionMenuItem (Chapter.OBJECT_TYPE));

                        Set<MenuItem> its = _this.getAddSectionMenu (Chapter.OBJECT_TYPE);

                        if (its.size () > 0)
                        {

                            items2.add (new SeparatorMenuItem ());

                        }

                        items2.addAll (its);

                        return items2;

                    };

                }

            };

        }

        if (objType.equals (ProjectEditor.OBJECT_TYPE))
        {

/*
TODO
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
*/
        }

        if (objType.startsWith (TaggedObjectSidebarItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (TaggedObjectSidebarItem.ID_PREFIX.length ()));

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

            return new TaggedObjectSidebarItem (tag,
                                                this,
                                                this.viewer)
            {

                @Override
                public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
                {

                    Set<MenuItem> items = super.getHeaderContextMenuItemSupplier ().get ();

                    return _this.getHeaderContextMenuItemSupplier (objType,
                                                                   items);

                }
/*
TODO REmove
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
*/
            };

        }

        if (objType.startsWith (AssetsSidebarItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (AssetsSidebarItem.ID_PREFIX.length ()));

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

                return this.createAssetSidebarItem (t);

            } else {

                return null;

            }

        }

        // Pre-v2.6
        if (this.legacyAssetObjTypes.contains (objType))
        {

            return this.createAssetSidebarItem (Environment.getUserConfigurableObjectType (objType));

        }

        if (objType.equals (Note.OBJECT_TYPE))
        {
/*
TODO
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
*/
        }

        return null;

    }

    private Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier (String        objType,
                                                                      Set<MenuItem> items)
    {

        return () ->
        {

            items.add (this.getHideSectionMenuItem (objType));

            Set<MenuItem> its = this.getAddSectionMenu (objType);

            if (its.size () > 0)
            {

                items.add (new SeparatorMenuItem ());

            }

            items.addAll (its);

            return items;

        };


    }

    // TODO Move to UserProperties?
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

    private StringProperty getMenuNameForObjectType (String objType)
    {

        StringProperty name = null;
        int count = -1;

        if (objType.startsWith (TaggedObjectSidebarItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (TaggedObjectSidebarItem.ID_PREFIX.length ()));

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

            name = tag.nameProperty ();

        }

        if (ProjectEditor.OBJECT_TYPE.equals (objType))
        {

            name = Environment.getObjectTypeNamePlural (EditorEditor.OBJECT_TYPE);
            //"{Editors}";

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

            name = utype.objectTypeNamePluralProperty ();

            count = this.viewer.getProject ().getAllNamedChildObjects (Chapter.class).size ();

        }

        if (this.legacyAssetObjTypes.contains (objType))
        {

            UserConfigurableObjectType utype = Environment.getUserConfigurableObjectType (objType);

            if (utype == null)
            {

                return null;

            }

            name = utype.objectTypeNamePluralProperty ();

            count = this.viewer.getProject ().getAllNamedChildObjects (utype).size ();

        }

        if (objType.startsWith (AssetsSidebarItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (AssetsSidebarItem.ID_PREFIX.length ()));

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

            name = t.objectTypeNamePluralProperty ();

            count = this.viewer.getProject ().getAllNamedChildObjects (t).size ();

        }

        if (count > -1)
        {

            int count2 = count;
            String n = name.getValue ();

            StringProperty name2 = new SimpleStringProperty ();
            name2.bind (Bindings.createStringBinding (() ->
            {

                return String.format ("%1$s (%2$s)",
                                      n,
                                      Environment.formatNumber (count2));

            }));

            name = name2;

        }

        return name;

    }

    private Node getIconForObjectType (String objType)
    {

        if (objType.startsWith (AssetsSidebarItem.ID_PREFIX))
        {

            long key = 0;

            try
            {

                key = Long.parseLong (objType.substring (AssetsSidebarItem.ID_PREFIX.length ()));

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

                ImageView iv = new ImageView ();
                iv.imageProperty ().bind (t.icon16x16Property ());

                return iv;

            }

        }

        if (this.legacyAssetObjTypes.contains (objType))
        {

            ImageView iv = new ImageView ();
            iv.imageProperty ().bind (Environment.getUserConfigurableObjectType (objType).icon16x16Property ());
            return iv;

        }

        return null;

    }

    private String getStyleForObjectType (String objType)
    {

        if (Chapter.OBJECT_TYPE.equals (objType))
        {

            return StyleClassNames.CHAPTER;

        }

        if (objType.startsWith (TaggedObjectSidebarItem.ID_PREFIX))
        {

            return StyleClassNames.TAG;

        }

        if (ProjectEditor.OBJECT_TYPE.equals (objType))
        {

            return StyleClassNames.EDITOR;

        }

        if (Note.OBJECT_TYPE.equals (objType))
        {

            return StyleClassNames.NOTE;

        }

        return null;

        //return objType;

    }

    public void addSidebarItem (ProjectObjectsSidebarItem item)
    {

        this.addSidebarItem (item,
                             null);

    }

    private AccordionItem getAccordionItemForObjectType (String objType)
    {

        ProjectObjectsSidebarItem pai = this.items.get (objType);

        if (pai == null)
        {

            return null;

        }

        return pai.getAccordionItem ();

    }

    private int getIndex (String objType)
    {

        AccordionItem it = this.getAccordionItemForObjectType (objType);

        if (it != null)
        {

            return this.getIndex (it);

        }

        return -1;

    }

    private int getIndex (Node n)
    {

        return this.contentBox.getChildren ().indexOf (n);

    }

    public void addSidebarItem (final ProjectObjectsSidebarItem item,
                                final String                    belowObjType)
    {

        if (item == null)
        {

            throw new NullPointerException ("Item must be provided.");

        }

        int ind = this.getIndex (belowObjType);

        if (this.state != null)
        {

            item.init (this.state.getAsState (item.getId ()));

        }

        AccordionItem ai = item.getAccordionItem ();

        if ((ind > -1)
            &&
            (ind < this.contentBox.getChildren ().size ())
           )
        {

            this.contentBox.getChildren ().add (ind + 1,
                                                ai);

        } else {

            this.contentBox.getChildren ().add (ai);

        }

        this.items.put (item.getId (),
                        item);

    }

}
