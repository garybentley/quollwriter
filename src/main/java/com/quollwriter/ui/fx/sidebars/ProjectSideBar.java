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
import javafx.scene.input.*;
import javafx.geometry.*;

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

    public static final DataFormat PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT = new DataFormat ("projectsidebar/section");

    public static final String SIDEBAR_ID = "project";

    private Set<String> legacyAssetObjTypes = null;
    private Map<String, ProjectObjectsSidebarItem> items = null;
    private VBox contentBox = null;
    private HBox toolbarBox = null;
    private Pane contentWrapper = null;
    private State state = new State ();
    private Map<Panel, ToolBar> toolbars = null;

    public ProjectSideBar (ProjectViewer viewer)
    {

        super (viewer);

        this.items = new HashMap<> ();
        this.toolbars = new HashMap<> ();

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
        VBox.setVgrow (this.contentBox,
                       Priority.ALWAYS);
        this.contentBox.getStyleClass ().add (StyleClassNames.ITEMS);

        /*
        This tooltip won't limit itself to the context box, it shows above other items as well... grrr...
        UIUtils.setTooltip (this.contentBox,
                            getUILanguageStringProperty (project,LanguageStrings.sidebar,tooltip));
                            */
        this.contentWrapper = new VBox ();
        ScrollPane sp = new ScrollPane (this.contentBox);
        UIUtils.makeDraggable (sp);
        this.contentBox.prefHeightProperty ().bind (sp.prefViewportHeightProperty ());
        this.contentBox.addEventHandler (MouseEvent.MOUSE_CLICKED,
                                         ev ->
        {

            if (ev.getTarget () != this.contentBox)
            {

                return;

            }

            if (ev.isPopupTrigger ())
            {

                return;

            }

            if (ev.getSource () == this.contentBox)
            {

                if (ev.getClickCount () == 2)
                {

                    this.viewer.runCommand (AbstractViewer.CommandId.newuserobject);

                }

            }

        });
        this.contentBox.setOnContextMenuRequested (ev ->
        {

            if (ev.getSource () != this.contentBox)
            {

                return;

            }

            UIUtils.showContextMenu (this.contentBox,
                                     this.getAddSectionMenu (null),
                                     ev.getScreenX (),
                                     ev.getScreenY ());
            ev.consume ();

        });

        this.contentBox.setOnDragExited (ev ->
        {

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT);

            if (o != null)
            {

                this.contentBox.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            }

        });

        this.contentBox.setOnDragDone (ev ->
        {

            this.contentBox.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
            this.contentBox.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

        });

        this.contentBox.setOnDragDropped (ev ->
        {

            this.contentBox.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT);

            if (o != null)
            {

                String id = o.toString ();

                AccordionItem other = this.items.get (id).getAccordionItem ();

                this.contentBox.getChildren ().remove (other);

                this.contentBox.getChildren ().add (other);

            }

        });

        this.contentBox.setOnDragOver (ev ->
        {

            this.contentBox.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT);

            if (o != null)
            {

                String id = o.toString ();

                //this.contentBox.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                ev.acceptTransferModes (TransferMode.MOVE);

                return;

            }

        });

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

        this.viewer.addEventHandler (Panel.PanelEvent.CLOSE_EVENT,
                                     ev ->
        {

            this.toolbars.remove (ev.getPanel ());

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

            ToolBar tb = this.toolbars.get (p);

            if (tb == null)
            {

                tb = new ToolBar ();
                tb.getStyleClass ().add (StyleClassNames.TOOLBAR);

                ToolBarSupported tbs = (ToolBarSupported) p.getContent ();

                tb.getItems ().addAll (tbs.getToolBarItems ());

                HBox.setHgrow (tb,
                               Priority.ALWAYS);

                this.toolbars.put (p,
                                   tb);

            }

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

        this.requestLayout ();

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

        if (s != null)
        {

            String _ids = s.getAsString ("ids");

            if (_ids != null)
            {

                ids = this.getSections (_ids);

            }


        }

        if (s == null)
        {

            s = new State ();

        }
System.out.println ("STATE: " + s);
        this.state = s;

        if (ids == null)
        {

            ids = this.getSectionsFromProperty (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);

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

        for (Node n : this.contentBox.getChildren ())
        {

            if (n instanceof AccordionItem)
            {

                AccordionItem ai = (AccordionItem) n;

                String id = ai.getAccordionId ();

                ids.add (id);
                s.set (id,
                       this.items.get (id).getState ());

            }

        }

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

                this.viewer.runCommand (AbstractViewer.CommandId.edittags);

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,newobject)))
            .styleClassName (StyleClassNames.EDIT)
            .onAction (ev ->
            {

                this.viewer.runCommand (AbstractViewer.CommandId.newuserobject);

            })
            .build ());

        // Get all the sections currently not visible.
        Set<String> defSections = this.getSectionsFromProperty (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);

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
    private Set<String> getSectionsFromProperty (String propName)
    {

        Set<String> objTypes = new LinkedHashSet<> ();

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

    private Set<String> getSections (String value)
    {

        Set<String> objTypes = new LinkedHashSet<> ();

        if (value == null)
        {

            return objTypes;

        }

        StringTokenizer t = new StringTokenizer (value,
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

        Header h = ai.getHeader ();

        h.setOnDragDetected (ev ->
        {

            Dragboard db = h.startDragAndDrop (TransferMode.MOVE);

            ClipboardContent c = new ClipboardContent ();
            c.put (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT,
                   item.getId ());

            db.setContent (c);
            db.setDragView (UIUtils.getImageOfNode (ai));
            ai.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
            ev.consume ();

        });

        ai.setOnDragExited (ev ->
        {

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT);

            if (o != null)
            {

                ai.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            }

        });

        ai.setOnDragDone (ev ->
        {

            ai.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
            ai.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

        });

        ai.setOnDragDropped (ev ->
        {

            ai.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT);

            if (o != null)
            {

                String id = o.toString ();

                int _ind = this.contentBox.getChildren ().indexOf (ai);

                AccordionItem other = this.items.get (id).getAccordionItem ();

                this.contentBox.getChildren ().remove (other);

                this.contentBox.getChildren ().add (_ind,
                                                    other);
                ev.consume ();

            }

        });

        ai.setOnDragOver (ev ->
        {

            ai.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT);

            if (o != null)
            {

                String id = o.toString ();

                if (!id.equals (item.getId ()))
                {

                    ai.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                    ev.acceptTransferModes (TransferMode.MOVE);
                    //ev.consume ();

                    return;

                }

            }

            ev.consume ();

        });

    }

}
