package com.quollwriter.ui.fx.sidebars;

import java.awt.image.*;
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
import com.quollwriter.editors.ui.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProjectSideBar extends BaseSideBar<ProjectViewer>
{

    public static final DataFormat PROJECT_OBJECT_SIDEBAR_ITEM_DATA_FORMAT = new DataFormat ("projectsidebar/section");

    public static final String SIDEBAR_ID = "project";

    private Set<String> legacyAssetObjTypes = null;
    private Map<String, ProjectObjectsSidebarItem> items = null;
    private VBox contentBox = null;
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

        this.contentBox = new VBox ();
        VBox.setVgrow (this.contentBox,
                       Priority.ALWAYS);
        this.contentBox.getStyleClass ().add (StyleClassNames.ITEMS);

        /*
        This tooltip won't limit itself to the context box, it shows above other items as well... grrr...
        UIUtils.setTooltip (this.contentBox,
                            getUILanguageStringProperty (project,LanguageStrings.sidebar,tooltip));
                            */
        ScrollPane sp = new ScrollPane (this.contentBox);
        UIUtils.makeDraggable (sp);

        this.contentBox.getChildren ().addListener ((ListChangeListener<Node>) ch ->
        {

            // We turn on/off fit to width depending upon whether we have any children otherwise
            // content box won't stretch when it's empty.
            sp.setStyle (this.contentBox.getChildren ().size () == 0 ? "-fx-fit-to-width: true; " : "-fx-fit-to-width: false;");

        });

        sp.setStyle (this.contentBox.getChildren ().size () == 0 ? "-fx-fit-to-width: true; " : "-fx-fit-to-width: false;");

        //this.contentBox.prefWidthProperty ().bind (sp.prefViewportWidthProperty ());
        this.contentBox.prefHeightProperty ().bind (sp.prefViewportHeightProperty ());
        //this.contentBox.prefWidthProperty ().bind (sp.prefViewportWidthProperty ());
        this.contentBox.addEventHandler (MouseEvent.MOUSE_CLICKED,
                                         ev ->
        {

            if (ev.getTarget () != this.contentBox)
            {

                return;

            }

            if (ev.getButton () != MouseButton.PRIMARY)
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
        this.setContent (sp);

        this.addSetChangeListener (Environment.getUserConfigurableObjectTypes (),
                                   ch ->
        {

            if (ch.wasRemoved ())
            {

                this.removeSection (ch.getElementRemoved ());

            }

            if (ch.wasAdded ())
            {

                if (ch.getElementAdded ().isAssetObjectType ())
                {

                    this.addSidebarItem (this.createAssetSidebarItem (ch.getElementAdded ()));

                }

            }

        });

        this.addSetChangeListener (Environment.getAllTags (),
                                   ev ->
        {

            Tag t = ev.getElementRemoved ();

            if (t != null)
            {

                this.removeTagSection (t);

            }

        });

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
            .styleSheet (StyleClassNames.PROJECT)
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

    public void removeSection (UserConfigurableObjectType objType)
    {

        if (objType.isAssetObjectType ())
        {

            AssetsSidebarItem aai = this.getAssetSidebarItemForObjectType (objType);

            if (aai != null)
            {

                this.contentBox.getChildren ().remove (aai.getAccordionItem ());
                this.items.remove (aai.getId ());

                this.state.set (aai.getId (),
                                aai.getAccordionItem ().getState ());

            }

        }

    }

    private AssetsSidebarItem getAssetSidebarItemForObjectType (UserConfigurableObjectType t)
    {

        for (ProjectObjectsSidebarItem pi : this.items.values ())
        {

            if (pi instanceof AssetsSidebarItem)
            {

                AssetsSidebarItem ai = (AssetsSidebarItem) pi;

                if (ai.getUserConfigurableObjectType ().equals (t))
                {

                    return ai;

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

            this.contentBox.getChildren ().remove (ai);
            this.items.remove (objType);

            this.state.set (objType,
                            ai.getState ());

        }

    }

    private Set<MenuItem> getAddSectionMenu (final String belowObjType)
    {

        final ProjectSideBar _this = this;

        List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,headerpopupmenu,LanguageStrings.items);

        Set<MenuItem> items = new LinkedHashSet<> ();

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,addtag)))
            .iconName (StyleClassNames.TAG)
            .onAction (ev ->
            {

                this.viewer.runCommand (AbstractViewer.CommandId.edittags);

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,newobject)))
            .iconName (StyleClassNames.ASSET)
            .onAction (ev ->
            {

                this.viewer.showAddNewUserConfigurableType ();

            })
            .build ());

        // Get all the sections currently not visible.
        Set<String> defSections = this.getSectionsFromProperty (Constants.DEFAULT_PROJECT_SIDEBAR_SECTIONS_PROPERTY_NAME);

        // Get all the user asset types.
        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

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

        Set<StringProperty> noteTypes = new LinkedHashSet<> ();

        for (StringProperty p : UserProperties.getNoteTypes ())
        {

            if (this.items.keySet ().contains (Note.OBJECT_TYPE + "/" + p.getValue ()))
            {

                continue;

            }

            noteTypes.add (p);

        }

        if ((defSections.size () > 0)
            ||
            (noteTypes.size () > 0)
           )
        {

            Set<MenuItem> its = new LinkedHashSet<> ();

            if (noteTypes.size () > 0)
            {

                // Add the note types.
                Menu m = QuollMenu.builder ()
                    .label (getUILanguageStringProperty (objectnames,plural,Note.OBJECT_TYPE))
                    .styleClassName (StyleClassNames.NOTE)
                    .build ();

                for (StringProperty p : noteTypes)
                {

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (p)
                        .onAction (ev ->
                        {

                            NotesSidebarItem it = new NotesSidebarItem (this.viewer,
                                                                        p,
                                                                        this)
                            {

                                @Override
                                public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
                                {

                                    Set<MenuItem> items = super.getHeaderContextMenuItemSupplier ().get ();

                                    return () ->
                                    {

                                        Set<MenuItem> items2 = new LinkedHashSet<> (items);

                                        items2.add (_this.getHideSectionMenuItem (this.getId ()));//Chapter.OBJECT_TYPE));

                                        Set<MenuItem> its = _this.getAddSectionMenu (this.getId ());//Chapter.OBJECT_TYPE);

                                        if (its.size () > 0)
                                        {

                                            items2.add (new SeparatorMenuItem ());

                                        }

                                        items2.addAll (its);

                                        return items2;

                                    };

                                }

                            };

                            _this.addSidebarItem (it,
                                                  belowObjType);

                            if (belowObjType != null)
                            {

                                // Scroll the item into view.
                                UIUtils.scrollIntoView (it.getAccordionItem (),
                                                        null);

                            }

                        })
                        .build ());

                }

                its.add (m);

            }

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
                    .iconName ((style != null ? style : sect))
                    .icon (getIconForObjectType (sect))
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
/*
TODO Remove
                if (style == null)
                {

                    Node g = getIconForObjectType (sect);

                    mi.setGraphic (g);

                } else {

                    ImageView iv = new ImageView ();
                    iv.getStyleClass ().add (style);

                    mi.setGraphic (iv);

                }
*/
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
            .iconName (StyleClassNames.CLOSE)
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

                return () ->
                {

                    Set<MenuItem> items = super.getHeaderContextMenuItemSupplier ().get ();

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
                        .iconName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            this.viewer.showDeleteUserConfigurableType (type);
/*
                            String pid = "deleteall" + type.getObjectReference ().asString ();

                            QuollPopup.yesConfirmTextEntryBuilder ()
                                .withViewer (_this.viewer)
                                .title (getUILanguageStringProperty (Arrays.asList (assets,deleteall,title),
                                                                     type.objectTypeNamePluralProperty ()))
                                .popupId (pid)
                                .styleClassName (StyleClassNames.DELETE)
                                .description (getUILanguageStringProperty (Arrays.asList (assets,deleteall,text),
                                                                       type.objectTypeNamePluralProperty ()))
                                .confirmButtonLabel (assets,deleteall,buttons,confirm)
                                .cancelButtonLabel (assets,deleteall,buttons,cancel)
                                .onConfirm (eev ->
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

                                        return;

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

                                        return;

                                    }

                                    _this.viewer.getPopupById (pid).close ();

                                })
                                .build ();
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

        if (objType.startsWith (Note.OBJECT_TYPE))
        {

            return new NotesSidebarItem (this.viewer,
                                         NotesSidebarItem.getNoteTypeForId (objType),
                                         this)
            {

                @Override
                public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
                {

                    Set<MenuItem> items = super.getHeaderContextMenuItemSupplier ().get ();

                    return () ->
                    {

                        Set<MenuItem> items2 = new LinkedHashSet<> (items);

                        items2.add (_this.getHideSectionMenuItem (this.getId ()));//Chapter.OBJECT_TYPE));

                        Set<MenuItem> its = _this.getAddSectionMenu (this.getId ());//Chapter.OBJECT_TYPE);

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

            return new ProjectEditorsAccordionItem (this.viewer,
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

                    return () ->
                    {

                        Set<MenuItem> items2 = new LinkedHashSet<> (items);

                        items2.add (_this.getHideSectionMenuItem (this.getId ()));

                        Set<MenuItem> its = _this.getAddSectionMenu (this.getId ());

                        if (its.size () > 0)
                        {

                            items2.add (new SeparatorMenuItem ());

                        }

                        items2.addAll (its);

                        return items2;

                    };

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

    private ObjectProperty<Image> getIconForObjectType (String objType)
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

                //ImageView iv = new ImageView ();
                //iv.imageProperty ().bind (t.icon16x16Property ());

                return t.icon16x16Property ();

            }

        }

        if (this.legacyAssetObjTypes.contains (objType))
        {

            //ImageView iv = new ImageView ();
            //iv.imageProperty ().bind (Environment.getUserConfigurableObjectType (objType).icon16x16Property ());
            //return iv;
            return Environment.getUserConfigurableObjectType (objType).icon16x16Property ();

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
