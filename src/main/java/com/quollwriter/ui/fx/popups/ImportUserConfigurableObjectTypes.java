package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.util.*;
import java.sql.*;

import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.comparators.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ImportUserConfigurableObjectTypes extends PopupContent<AbstractProjectViewer>
{

    public static final String POPUP_ID = "importuserconfigobjtypes";
    private Map<String, Wizard.Step> steps = new HashMap<> ();
    private Wizard wizard = null;
    private VBox projectList = null;
    private QuollTreeView<NamedObject> itemsTree = null;
    private Project importFromProj = null;

    public ImportUserConfigurableObjectTypes (AbstractProjectViewer viewer)
    {

        super (viewer);

        this.projectList = new VBox ();

        this.itemsTree = new QuollTreeView<> ();
        this.itemsTree.setShowRoot (false);
        this.itemsTree.setCellProvider (treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            if (n instanceof UserConfigurableObjectType)
            {

                UserConfigurableObjectType uc = (UserConfigurableObjectType) n;

                QuollLabel l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .build ();

                l.setGraphic (new ImageView (uc.getIcon16x16 ()));

                return l;

            }

            if (n instanceof TreeParentNode)
            {

                Node l = null;

                TreeParentNode tn = (TreeParentNode) treeItem.getValue ();

                if (tn.getForObjectType ().equals ("_string"))
                {

                    l = BasicHtmlTextFlow.builder ()
                        .text (n.nameProperty ())
                        .styleClassName (StyleClassNames.TEXT)
                        .build ();
/*
                    l = QuollTextView.builder ()
                        .text (n.nameProperty ())
                        .styleClassName (StyleClassNames.TEXT)
                        .build ();
*/
                } else {

                    l = QuollLabel.builder ()
                        .styleClassName (tn.getForObjectType ())
                        .label (n.nameProperty ())
                        .build ();

                }

                return l;

            }

            return QuollLabel.builder ()
                .label (n.nameProperty ())
                .build ();

        });

        ImportUserConfigurableObjectTypes _this = this;

        this.wizard = Wizard.builder ()
            .startStepId ("projectlist")
            .nextStepIdProvider (currId ->
            {

                if ("projectlist".equals (currId))
                {

                    return "select";

                }

                return null;

            })
            .previousStepIdProvider (currId ->
            {

                if ("select".equals (currId))
                {

                    return "projectlist";

                }

                return null;

            })
            .stepProvider (currId ->
            {

                Wizard.Step s = _this.steps.get (currId);

                if (s != null)
                {

                    return s;

                }

                if ("select".equals (currId))
                {

                    Wizard.Step ws = new Wizard.Step ();

                    ws.title = getUILanguageStringProperty (Arrays.asList (userobjects,type,importtypes,LanguageStrings.popup,selectitems,title));

                    BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
                        .styleClassName (StyleClassNames.DESCRIPTION)
                        .text (getUILanguageStringProperty (userobjects,type,importtypes,LanguageStrings.popup,selectitems,text))
                        .build ();

                    ProjectInfo pi = (ProjectInfo) UIUtils.getSelected (_this.projectList).get (0).getUserData ();

                    Runnable r = () ->
                    {

                        _this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                   false);

                        Project p = null;

                        try
                        {

                            p = Environment.getProjectObjectManager (pi,
                                                                     pi.getFilePassword ()).getProject ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to get project for: " +
                                                  p,
                                                  e);

                            ComponentUtils.showErrorMessage (_this.viewer,
                                                             getUILanguageStringProperty (Arrays.asList (importproject,errors,cantopenproject)));
                                                      //"Unable to open {project}");

                            return;

                        }

                        TreeItem<NamedObject> root = new TreeItem<> (p);

                        Set<UserConfigurableObjectType> assetTypes = p.getAssetUserConfigurableObjectTypes (true);

                        for (UserConfigurableObjectType t : assetTypes)
                        {

                            CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (t);

                            root.getChildren ().add (ci);

                        }

                        root.addEventHandler (CheckBoxTreeItem.checkBoxSelectionChangedEvent (),
                                              ev ->
                        {

                            _this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                       UIUtils.getSelectedCount (root) > 0);

                        });

                        _this.itemsTree.setRoot (root);
                        _this.importFromProj = p;

                    };

                    if ((pi != null)
                        &&
                        (pi.isEncrypted ())
                        &&
                        (Environment.getProjectViewer (pi) == null)
                        &&
                        (pi.getFilePassword () == null)
                       )
                    {

                        UIUtils.askForPasswordForProject (pi,
                                                          null,
                                                          pwd ->
                                                          {

                                                              pi.setFilePassword (pwd);

                                                              UIUtils.runLater (r);

                                                          },
                                                          null,
                                                          this.viewer);

                    } else {

                        UIUtils.runLater (r);

                    }

                    ScrollPane sp = new ScrollPane (itemsTree);
                    VBox b = new VBox ();
                    VBox.setVgrow (sp, Priority.ALWAYS);

                    b.getChildren ().addAll (desc, sp);

                    ws.content = b;

                    return ws;

                }

                if ("projectlist".equals (currId))
                {

                    List<ProjectInfo> projs = null;

                    try
                    {

                        projs = new ArrayList<> (Environment.getAllProjects ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to get all projects",
                                              e);

                        ComponentUtils.showErrorMessage (_this.viewer,
                                                         getUILanguageStringProperty (userobjects,type,importtypes,LanguageStrings.popup,projectslist,error));
                                                  //"Unable to get all the {projects}.");

                        _this.close ();

                        return null;

                    }

                    Collections.sort (projs,
                                      new NamedObjectSorter (_this.viewer.getProject ()));

                    _this.projectList.getStyleClass ().add (StyleClassNames.PROJECTS);

                    projs.stream ()
                        .filter (pr ->
                        {

                            if ((Files.notExists (pr.getProjectDirectory ()))
                                ||
                                (pr.isEditorProject ())
                                ||
                                (pr.isWarmupsProject ())
                                ||
                                (pr.equals (_this.viewer.getProject ()))
                               )
                            {

                                return false;

                            }

                            return true;

                        })
                        .forEach (pr ->
                        {

                            Header l = Header.builder ()
                                .title (pr.nameProperty ())
                                .styleClassName ("project")
                                .iconClassName (_this.getStyleName (pr))
                                .tooltip (userobjects,type,importtypes,projectslist,tooltip)
                                .build ();

                            l.setUserData (pr);

                            l.setOnMouseClicked (ev ->
                            {

                                if (ev.getButton () != MouseButton.PRIMARY)
                                {

                                    return;

                                }

                                UIUtils.toggleSelected (projectList,
                                                        l);

                                _this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                           UIUtils.isSelected (l));

                            });

                            _this.projectList.getChildren ().add (l);

                        });

                    ScrollPane sp = new ScrollPane (_this.projectList);

                    Wizard.Step ws = new Wizard.Step ();
                    ws.title = getUILanguageStringProperty (importproject,stages,selectproject,title);
                    ws.content = sp;
                    ws.content.getStyleClass ().add ("projects");

                    _this.steps.put ("projectslist",
                                     ws);

                    return ws;

                }

                return null;

            })
            .onCancel (ev ->
            {

                _this.close ();

            })
            .onFinish (ev ->
            {

                Set<UserConfigurableObjectType> ptypes = _this.viewer.getProject ().getUserConfigurableObjectTypes ();

                Map<String, UserConfigurableObjectType> ntypes = new HashMap<> ();
                Map<String, UserConfigurableObjectType> nptypes = new HashMap<> ();

                ptypes.stream ()
                    .forEach (t ->
                    {

                        ntypes.put (t.getObjectTypeName ().toLowerCase (),
                                    t);
                        nptypes.put (t.getObjectTypeNamePlural ().toLowerCase (),
                                     t);

                    });

                List<UserConfigurableObjectType> toAdd = new ArrayList<> ();

                _this.itemsTree.walkTree (ti ->
                {

                    List<NamedObject> toSave = new ArrayList<> ();

                    if (ti instanceof CheckBoxTreeItem)
                    {

                        CheckBoxTreeItem cti = (CheckBoxTreeItem) ti;

                        if (cti.isSelected ())
                        {



                            // Import the type.
                            UserConfigurableObjectType type = (UserConfigurableObjectType) cti.getValue ();

                            toAdd.add (type);

                        }

                    }

                });

                List<UserConfigurableObjectType> newTypes = new ArrayList<> ();

                Connection conn = null;
                ObjectManager objMan = _this.viewer.getObjectManager ();

                try
                {

                    // Get a connection, we need to do this in one transaction so we don't wind up in a weird half-way state.
                    conn = objMan.getConnection ();

                    for (UserConfigurableObjectType type : toAdd)
                    {

                        String layoutInfo = type.getProperty (Constants.USER_CONFIGURABLE_OBJECT_TYPE_SORTABLE_FIELDS_LAYOUT_PROPERTY_NAME);

                        String nname = type.getObjectTypeName ().toLowerCase ();
                        String pname = type.getObjectTypeNamePlural ().toLowerCase ();

                        String newname = type.getObjectTypeName ();
                        String newpname = type.getObjectTypeNamePlural ();

                        if (ntypes.containsKey (nname))
                        {

                            newname += " [2]";

                        }

                        if (nptypes.containsKey (pname))
                        {

                            newpname += " [2]";

                        }

                        // Create a new type and add it.
                        UserConfigurableObjectType nt = new UserConfigurableObjectType (_this.viewer.getProject ());
                        nt.setObjectTypeName (newname);
                        nt.setDescription (type.getDescription ());
                        nt.setObjectTypeNamePlural (newpname);
                        nt.setIcon16x16 (type.getIcon16x16 ());
                        nt.setIcon24x24 (type.getIcon24x24 ());
                        nt.setLayout (type.getLayout ());
                        //nt.setUserObjectType (type.getUserObjectType ());
                        nt.setAssetObjectType (type.isAssetObjectType ());
                        nt.setCreateShortcutKeyStroke (type.getCreateShortcutKeyStroke ());
                        nt.setIgnoreFieldsState (true);

                        objMan.saveObject (nt,
                                           conn);

                        newTypes.add (nt);

                        String objId = "";

                        UserConfigurableObjectTypeField oldnamefield = type.getPrimaryNameField ();
                        if (oldnamefield != null)
                        {

                            objId = "\"" + oldnamefield.getObjectReference ().asString () + "\"";

                            // Add the primary name field.
                            ObjectNameUserConfigurableObjectTypeField newnamefield = (ObjectNameUserConfigurableObjectTypeField) UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.objectname);

                            newnamefield.setFormName (oldnamefield.getFormName ());
                            newnamefield.setUserConfigurableObjectType (nt);

                            Map<String, Object> ndefs = new HashMap<> ();
                            ndefs.putAll (oldnamefield.getDefinition ());
                            newnamefield.setDefinition (ndefs);

                            nt.setPrimaryNameField (newnamefield);

                            objMan.saveObject (newnamefield,
                                               conn);

                            layoutInfo = Utils.replaceString (layoutInfo,
                                                              objId,
                                                              "\"" + newnamefield.getObjectReference ().asString () + "\"");

                        }

                        // Get the fields and create them.
                        for (UserConfigurableObjectType.FieldsColumn fc : type.getSortableFieldsColumns ())
                        {

                            Set<UserConfigurableObjectTypeField> nfcfields = new LinkedHashSet<> ();

                            // Clone/create fields.
                            for (UserConfigurableObjectTypeField fft : fc.fields ())
                            {

                                objId = "\"" + fft.getObjectReference ().asString () + "\"";

                                // Get the field.
                                UserConfigurableObjectTypeField nft = UserConfigurableObjectTypeField.Type.getNewFieldForType (fft.getType ());

                                nft.setFormName (fft.getFormName ());
                                nft.setUserConfigurableObjectType (nt);

                                Map<String, Object> defs = new HashMap<> ();
                                defs.putAll (fft.getDefinition ());
                                nft.setDefinition (defs);
                                nft.setDefaultValue (fft.getDefaultValue ());

                                nft.setOrder (fft.getOrder ());

                                objMan.saveObject (nft,
                                                   conn);

                                nfcfields.add (nft);

                                layoutInfo = Utils.replaceString (layoutInfo,
                                                                  objId,
                                                                  "\"" + nft.getObjectReference ().asString () + "\"");

                            }

                            UserConfigurableObjectType.FieldsColumn nfc = nt.addNewColumn (nfcfields);
                            nfc.setTitle (fc.getTitle ());
                            nfc.setShowFieldLabels (fc.isShowFieldLabels ());

                        }

                        nt.setIgnoreFieldsState (false);
                        nt.updateSortableFieldsState ();

                        nt.setProperty (Constants.USER_CONFIGURABLE_OBJECT_TYPE_SORTABLE_FIELDS_LAYOUT_PROPERTY_NAME,
                                        layoutInfo);

                        objMan.saveObject (nt,
                                           conn);

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to import types",
                                          e);

                    ComponentUtils.showErrorMessage (_this.viewer,
                                                     getUILanguageStringProperty (userobjects,type,importtypes,LanguageStrings.popup,actionerror));

                } finally {

                    if (conn != null)
                    {

                        objMan.releaseConnection (conn);

                    }

                }

                // Add the new types to the project.
                newTypes.stream ()
                    .forEach (t -> _this.viewer.getProject ().addUserConfigurableObjectType (t));

                _this.close ();

            })
            .nextStepCheck ((currId, nextId) ->
            {

                return true;

            })
            .previousStepCheck ((currId, prevId) ->
            {

                return true;

            })
            .onStepShow (ev ->
            {

                if ("projectlist".equals (ev.getCurrentStepId ()))
                {

                    ev.getWizard ().enableButton (Wizard.NEXT_BUTTON_ID,
                                                  false);

                }

            })
            .build ();

        VBox b = new VBox ();

        b.getChildren ().add (this.wizard);
/*
        Form f = Form.builder ()
            .description (userobjects,LanguageStrings.type,_new,wizard,basic,text)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.name),
                   this.name)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.plural),
                   this.plural)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.bigicon),
                   this.bigIcon)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.smallicon),
                   this.smallIcon)
            .confirmButton (buttons,save)
            .cancelButton (buttons,cancel)
            .build ();

        f.setOnCancel (ev ->
        {

            this.close ();

        });

        f.setOnConfirm (ev ->
        {

            f.hideError ();
            ev.consume ();

            if (name.getText () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors, LanguageStrings.name,novalue));
                return;

            }

            if (plural.getText () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors, LanguageStrings.plural,novalue));
                return;

            }

            if (smallIcon.getImage () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors,LanguageStrings.smallicon,novalue));
                return;

            }

            if (bigIcon.getImage () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors,LanguageStrings.bigicon,novalue));
                return;

            }

            // Check that the name(s) aren't already in use.
            Set<UserConfigurableObjectType> types = this.viewer.getProject ().getAssetUserConfigurableObjectTypes (false);

            for (UserConfigurableObjectType t : types)
            {

                if (t.equals (this.type))
                {

                    continue;

                }

                if (t.getObjectTypeName ().equalsIgnoreCase (name.getText ()))
                {

                    f.showError (getUILanguageStringProperty (Arrays.asList (userobjects,basic,edit,errors,LanguageStrings.name,valueexists),
                                                              name.getText ()));
                    return;

                }

                if (t.getObjectTypeNamePlural ().equalsIgnoreCase (plural.getText ()))
                {

                    f.showError (getUILanguageStringProperty (Arrays.asList (userobjects,basic,edit,errors,LanguageStrings.plural,valueexists),
                                                              plural.getText ()));
                    return;

                }

            }

            try
            {

                boolean showAdd = false;

                this.type.setObjectTypeName (name.getText ());
                this.type.setObjectTypeNamePlural (plural.getText ());
                this.type.setIcon16x16 (UIUtils.getScaledImage (smallIcon.getImage (), smallIconWidth));
                this.type.setIcon24x24 (UIUtils.getScaledImage (bigIcon.getImage (), bigIconWidth));

                if (this.type.getKey () == null)
                {

                    showAdd = true;
                    // Name
                    ObjectNameUserConfigurableObjectTypeField nameF = new ObjectNameUserConfigurableObjectTypeField ();

                    nameF.setFormName (getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,defaults,fields,LanguageStrings.name).getValue ());
                    nameF.setUserConfigurableObjectType (this.type);

                    // Description
                    ObjectDescriptionUserConfigurableObjectTypeField cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

                    cdescF.setSearchable (true);
                    cdescF.setFormName (getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,defaults,fields,description).getValue ());
                    //"Description");
                    cdescF.setUserConfigurableObjectType (this.type);

                    this.type.setAssetObjectType (true);

                    this.type.setConfigurableFields (Arrays.asList (nameF, cdescF));

                }

                // Save the type.
                this.viewer.saveObject (this.type,
                                        false);

                this.viewer.getProject ().addUserConfigurableObjectType (this.type);

                this.type.updateSortableFieldsState ();

                this.viewer.saveObject (this.type,
                                        false);

                if ((showAdd)
                    &&
                    (this.viewer instanceof ProjectViewer)
                   )
                {

                    ((ProjectViewer) this.viewer).showAddNewAsset (new Asset (this.type));

                }

            } catch (Exception e) {

                Environment.logError ("Unable to add/edit type: " +
                                      this.type,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (userobjects,LanguageStrings.type, (this.type.getKey () != null ? edit : _new),actionerror));

            }

            this.close ();

        });
*/
        this.getChildren ().add (b);

    }

    private String getStyleName (ProjectInfo pr)
    {

        String n = StyleClassNames.PROJECT;
        //Project.OBJECT_TYPE;

        if (Files.notExists (pr.getProjectDirectory ()))
        {

            // Return a problem icon.
            n = StyleClassNames.ERROR;
            return n;

        }

        if (pr.isEncrypted ())
        {

            // Return the lock icon.
            n = StyleClassNames.ENCRYPTED;
            return n;

        }

        if (pr.isEditorProject ())
        {

            // Return an editor icon.
            n = StyleClassNames.EDITOR;
            return n;

        }

        if (pr.isWarmupsProject ())
        {

            // Return an editor icon.
            n = Warmup.OBJECT_TYPE;
            return n;

        }

        return n;

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (userobjects,LanguageStrings.type,importtypes,LanguageStrings.popup,LanguageStrings.title)
            .styleClassName ("importuserconfigobjtypes")
            .styleSheet ("importuserconfigobjtypes")
            .headerIconClassName (StyleClassNames.IMPORT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.toFront ();

        return p;

    }

}
