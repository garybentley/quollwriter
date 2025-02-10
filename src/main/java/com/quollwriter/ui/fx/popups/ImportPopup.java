package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.net.*;
import java.nio.file.*;

import javafx.event.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.input.*;

import com.quollwriter.importer.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

// TEST ADD
import javafx.beans.value.*;
import javafx.collections.*;

public class ImportPopup extends PopupContent<AbstractViewer>
{

    public static final String POPUP_ID = "import";

    public static final String SELECT_ITEMS_STAGE = "select-items";
    public static final String SELECT_FILE_STAGE = "select-file";
    public static final String SELECT_PROJECT_STAGE = "select-project";
    public static final String CHOOSE_STAGE = "choose";
    public static final String DECIDE_STAGE = "decide";
    public static final String NEW_PROJECT_STAGE = "new-project";

    private boolean         addToProjectOnly = false;
    private boolean         newProjectOnly = false;
    private Path filePathToImport = null;
    private Project         proj = null;
    private Project importFromProj = null;
    private NewProjectPanel newProjectPanel = null;
    private Wizard wizard = null;
    private RadioButton addToProject = null;
    private QuollFileField fileFind = null;
    private Form selectFileForm = null;
    private RadioButton importFromFile = null;
    private RadioButton importFromProject = null;
    private RadioButton createNewProject = null;
    private ProjectViewer pv = null;
    private FileChooser.ExtensionFilter fileFilter = null;
    private QuollTreeView<NamedObject> itemsTree = null;
    private VBox projectList = null;

    private Map<String, Wizard.Step> steps = new HashMap<> ();

    // Old -> New
    private Map<UserConfigurableObjectType, UserConfigurableObjectType> typeKeyMapping = new HashMap<> ();

    // Old -> New
    private Map<UserConfigurableObjectTypeField, UserConfigurableObjectTypeField> fieldKeyMapping = new HashMap<> ();

    public ImportPopup (AbstractViewer viewer)
    {

        super (viewer);

        this.addToProject = QuollRadioButton.builder ()
            .build ();

        if (viewer instanceof ProjectViewer)
        {

            this.pv = (ProjectViewer) viewer;

            this.addToProject = QuollRadioButton.builder ()
                .onAction (ev -> this.wizard.enableButton (Wizard.NEXT_BUTTON_ID, true))
                .label (getUILanguageStringProperty (Arrays.asList (importproject,stages,decide,options,addtoproject),
                                                     pv.getProject ().nameProperty ()))
                .build ();

        } else {

            this.newProjectOnly = true;

        }

        this.fileFind = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (importproject,stages,selectfile,finder,title))
            .limitTo (QuollFileField.Type.file)
            .withViewer (viewer)
            .fileExtensionFilter (getUILanguageStringProperty (importproject,supportedfiletypesdescription),
                                  "docx")
            .build ();

        this.fileFind.fileProperty ().addListener ((p, oldv, newv) ->
        {

            this.filePathToImport = newv;

        });

        this.itemsTree = new QuollTreeView<> ();

        this.itemsTree.setCellProvider (treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            if (n instanceof UserConfigurableObjectType)
            {

                UserConfigurableObjectType uc = (UserConfigurableObjectType) n;

                QuollLabel l = QuollLabel.builder ()
                    .label (uc.objectTypeNamePluralProperty ())
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

        this.itemsTree.setShowRoot (false);

        EventHandler<ActionEvent> hand = ev ->
        {

            this.enableButtons (this.wizard,
                                this.wizard.getCurrentStepId ());

            this.proj = null;

            if (this.itemsTree != null)
            {

                this.itemsTree.setRoot (null);

            }

            if (this.projectList != null)
            {

                UIUtils.unselectChildren (this.projectList);

            }

        };

        this.importFromProject = QuollRadioButton.builder ()
            .label (getUILanguageStringProperty (importproject,stages,choose,options,importfromproject))
            .onAction (hand)
            .build ();

        this.importFromFile = QuollRadioButton.builder ()
            .label (getUILanguageStringProperty (importproject,stages,choose,options,importfromfile))
            .onAction (hand)
            .build ();

        this.createNewProject = QuollRadioButton.builder ()
            .label (getUILanguageStringProperty (importproject,stages,decide,options,newproject))
            .onAction (ev -> this.wizard.enableButton (Wizard.NEXT_BUTTON_ID, true))
            .build ();

        // Maybe move to UIUtils?
        this.wizard = Wizard.builder ()
            .startStepId (this.getStartStepId ())
            .nextStepIdProvider (currId ->
            {

                return getNextStepId (currId);

            })
            .previousStepIdProvider (currId ->
            {

                return getPreviousStepId (currId);

            })
            .stepProvider (currId ->
            {

                return getStep (currId);

            })
            .onCancel (ev ->
            {

                this.close ();

            })
            .onFinish (ev ->
            {

                this.handleFinish ();

                this.close ();

            })
            .nextStepCheck ((currId, nextId) ->
            {

                return this.handleStepChange (currId,
                                              nextId);

            })
            .previousStepCheck ((currId, prevId) ->
            {

                return this.handleStepChange (currId, prevId);

            })
            .onStepShow (ev ->
            {

                this.enableButtons (ev.getWizard (),
                                    ev.getCurrentStepId ());

            })
            .build ();

        VBox b = new VBox ();
        VBox.setVgrow (this.wizard, Priority.ALWAYS);
        b.getChildren ().addAll (this.wizard);

        this.getChildren ().addAll (b);

    }

    public String getStartStepId ()
    {

        if (Environment.getAllProjects ().size () == 1)
        {

            return SELECT_FILE_STAGE;

        }

        if ((this.newProjectOnly)
            &&
            (this.filePathToImport != null)
           )
        {

            return SELECT_ITEMS_STAGE;

        }

        if ((this.addToProjectOnly)
            &&
            (this.filePathToImport != null)
           )
        {

            return SELECT_ITEMS_STAGE;

        }

        return CHOOSE_STAGE;

    }

    public boolean handleStepChange (String oldStepId,
                                     String newStepId)
    {

        final ImportPopup _this = this;

        if (SELECT_PROJECT_STAGE.equals (oldStepId)
            &&
            (SELECT_ITEMS_STAGE.equals (newStepId))
           )
        {

            List<Node> sel = UIUtils.getSelected (this.projectList);

            if (sel.size () == 0)
            {

                return false;

            }

            final ProjectInfo pi = (ProjectInfo) sel.get (0).getUserData (); // TODO Remove this.projectList.getSelectedValue ();

            Runnable r = () ->
            {

                Project p = null;

                try
                {

                    ObjectManager om = Environment.getProjectObjectManager (pi,
                                                                            pi.getFilePassword ());
                    p = om.getProject ();

                    om.closeConnectionPool ();

                } catch (Exception e) {

                    Environment.logError ("Unable to get project for: " +
                                          p,
                                          e);

                    ComponentUtils.showErrorMessage (_this.viewer,
                                                     getUILanguageStringProperty (Arrays.asList (importproject,errors,cantopenproject)));
                                              //"Unable to open {project}");

                    return;

                }

                for (NamedObject n : p.getAllNamedChildObjects ())
                {

                    if (n instanceof Chapter)
                    {

                        Chapter c = (Chapter) n;

                        for (NamedObject cn : c.getAllNamedChildObjects ())
                        {

                            if (cn instanceof ChapterItem)
                            {

                                c.removeChapterItem ((ChapterItem) cn);

                            }

                        }

                    }

                    if (n instanceof IdeaType)
                    {

                        p.getIdeaTypes ().remove ((IdeaType) n);

                    }

                }

                // Need to null the key/id for all items in the project.
                for (NamedObject n : p.getAllNamedChildObjects ())
                {
/*
                    n.setKey (null);
                    n.setId (null);
                    n.setDateCreated (new java.util.Date ());

                    // For all the object fields, null the id/key.

                    if (n instanceof UserConfigurableObject)
                    {

                        UserConfigurableObject cn = (UserConfigurableObject) n;

                        for (UserConfigurableObjectField f : cn.getFields ())
                        {

                            f.setKey (null);
                            f.setId (null);
                            f.setDateCreated (new java.util.Date ());

                        }

                    }
*/
                }

                // Need a selectable tree here.
                _this.itemsTree.setRoot (_this.createTree (p));

                _this.enableButtons (_this.wizard,
                                     newStepId);

                _this.importFromProj = p;

                _this.proj = p;

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

                                                      this.wizard.showStep (SELECT_ITEMS_STAGE);

                                                      UIUtils.runLater (r);

                                                  },
                                                  null,
                                                  this.viewer);

                return false;

            } else {

                UIUtils.runLater (r);

            }

            return true;

        }

        if (SELECT_FILE_STAGE.equals (oldStepId))
        {

            if (CHOOSE_STAGE.equals (newStepId))
            {

                return true;

            }

            return this.checkForFileToImport ();

        }

        if (SELECT_ITEMS_STAGE.equals (newStepId))
        {

            if (this.importFromProject.isSelected ())
            {

                return true;

            }

            if (!SELECT_PROJECT_STAGE.equals (oldStepId))
            {

                this.importFromProj = null;

                return this.checkForFileToImport ();

            }

        }

        if ((NEW_PROJECT_STAGE.equals (oldStepId))
            &&
            (newStepId == null)
           )
        {

            if (!this.newProjectPanel.checkForm ())
            {

                return false;

            }

        }

        return true;

    }

    private UserConfigurableObjectType addUserConfigurableObjectType (Connection                 conn,
                                                                      ObjectManager              om,
                                                                      UserConfigurableObjectType type,
                                                                      Project                    thisProj)
                                                               throws Exception
    {

        // See if we already have this type
        UserConfigurableObjectType t = thisProj.getUserConfigurableObjectTypeByName (type.getObjectTypeName ());

        if (t == null)
        {

            long oldKey = type.getKey ();

            // Create a clone and add to "thisproj".
            UserConfigurableObjectType nt = new UserConfigurableObjectType (thisProj);
            nt.setName (type.getName ());
            nt.setDescription (type.getDescription ());
            nt.setObjectTypeName (type.getObjectTypeName ());
            nt.setObjectTypeNamePlural (type.getObjectTypeNamePlural ());
            nt.setIcon16x16 (type.getIcon16x16 ());
            nt.setIcon24x24 (type.getIcon24x24 ());
            nt.setLayout (type.getLayout ());
            nt.setUserObjectType (type.getUserObjectType ());
            nt.setAssetObjectType (type.isAssetObjectType ());
            nt.setCreateShortcutKeyStroke (type.getCreateShortcutKeyStroke ());
            nt.setIgnoreFieldsState (true);

            // Create the new type.
            om.saveObject (nt,
                           conn);

            this.typeKeyMapping.put (type,
                                     nt);

            UserConfigurableObjectTypeField oldnamefield = type.getPrimaryNameField ();
            if (oldnamefield != null)
            {

                // Add the primary name field.
                ObjectNameUserConfigurableObjectTypeField newnamefield = (ObjectNameUserConfigurableObjectTypeField) UserConfigurableObjectTypeField.Type.getNewFieldForType (UserConfigurableObjectTypeField.Type.objectname);

                newnamefield.setFormName (oldnamefield.getFormName ());
                newnamefield.setUserConfigurableObjectType (nt);

                Map<String, Object> ndefs = new HashMap<> ();
                ndefs.putAll (oldnamefield.getDefinition ());
                newnamefield.setDefinition (ndefs);

                om.saveObject (newnamefield,
                               conn);

                nt.setPrimaryNameField (newnamefield);

                this.fieldKeyMapping.put (oldnamefield,
                                          newnamefield);

            }

            // Get the fields and create them.
            for (UserConfigurableObjectType.FieldsColumn fc : type.getSortableFieldsColumns ())
            {

                List<UserConfigurableObjectTypeField> nfcfields = new ArrayList<> ();

                // Clone/create fields.
                for (UserConfigurableObjectTypeField fft : fc.fields ())
                {

                    // Get the field.
                    UserConfigurableObjectTypeField nft = UserConfigurableObjectTypeField.Type.getNewFieldForType (fft.getType ());

                    nft.setFormName (fft.getFormName ());
                    nft.setUserConfigurableObjectType (nt);

                    Map<String, Object> defs = new HashMap<> ();
                    defs.putAll (fft.getDefinition ());
                    nft.setDefinition (defs);
                    nft.setDefaultValue (fft.getDefaultValue ());

                    nft.setOrder (fft.getOrder ());

                    om.saveObject (nft,
                                   conn);

                    this.fieldKeyMapping.put (fft,
                                              nft);

                    nfcfields.add (nft);

                }

                UserConfigurableObjectType.FieldsColumn nfc = nt.addNewColumn (nfcfields);

                nfc.setTitle (fc.getTitle ());
                nfc.setShowFieldLabels (fc.isShowFieldLabels ());

            }

            nt.setIgnoreFieldsState (false);
            nt.updateSortableFieldsState ();

            // Save the type again.
            om.saveObject (nt,
                           conn);

            return nt;

        } else {

            t.setIgnoreFieldsState (true);

            this.typeKeyMapping.put (type,
                                     t);

            // We have the type, are the fields compatible?
            for (UserConfigurableObjectTypeField f : type.getConfigurableFields ())
            {

                // Does the current project have the field we are trying to import?
                UserConfigurableObjectTypeField of = t.getConfigurableFields ().stream ()
                    .filter (pf ->
                    {

                        // If the field legacy?
                        if ((f.isLegacyField ())
                            &&
                            (pf.isLegacyField ())
                            &&
                            (f.getLegacyFieldId ().equals (pf.getLegacyFieldId ()))
                           )
                        {

                            return true;

                        }

                        if (pf.isLegacyField ())
                        {

                            return false;

                        }

                        // Not legacy.
                        if (f.getName ().equals (pf.getName ()))
                        {

                            return true;

                        }

                        if ((f.getFormName () != null)
                            &&
                            (f.getFormName ().equals (pf.getFormName ()))
                           )
                        {

                            return true;

                        }

                        return false;

                    })
                    .findFirst ()
                    .orElse (null);

                if (of == null)
                {

                    // Create the field.
                    of = UserConfigurableObjectTypeField.Type.getNewFieldForType (f.getType ());

                    of.setFormName (f.getFormName ());
                    of.setUserConfigurableObjectType (t);

                    Map<String, Object> defs = new HashMap<> ();
                    defs.putAll (f.getDefinition ());
                    of.setDefinition (defs);
                    of.setDefaultValue (f.getDefaultValue ());

                    of.setOrder (f.getOrder ());

                    om.saveObject (of,
                                   conn);

                    this.fieldKeyMapping.put (f,
                                              of);

                    // Add to the first column.
                    t.addFieldToColumn (0,
                                        of);

                } else {

                    this.fieldKeyMapping.put (f,
                                              of);

                }

            }

            t.setIgnoreFieldsState (false);
            t.updateSortableFieldsState ();

            // Save the type again - this saves all the fields/state
            om.saveObject (t,
                           conn);

            return t;

        }

    }

    private void addSelectedItemsToProject (Connection conn)
                                     throws Exception
    {

        String importingFrom = this.importFromProject.isSelected () ? " project [" + this.importFromProj.getName () + "]" : " file [" + this.fileFind.getFile () + "]";

        Project thisProj = this.pv.getProject ();

        Book b = this.pv.getProject ().getBooks ().get (0);

        Set<String> filesToCopy = new LinkedHashSet<> ();

        Set<NamedObject> selected = new LinkedHashSet<> ();
        this.getSelectedObjects (this.itemsTree.getRoot (),
                                 selected);

        for (NamedObject n : selected)
        {

            if (n instanceof Chapter)
            {

                Chapter c = (Chapter) n;

                c.setBook (b);

                this.pv.saveObject (c,
                                    conn);

                b.addChapter (c);

                this.pv.saveObject (c,
                                    conn);

                this.pv.createActionLogEntry (c,
                                              "Imported chapter from: " +
                                              importingFrom,
                                              conn);

            }

            if (n instanceof UserConfigurableObjectType)
            {

                UserConfigurableObjectType uo = (UserConfigurableObjectType) n;

                uo = this.addUserConfigurableObjectType (conn,
                                                         this.pv.getObjectManager (),
                                                         uo,
                                                         thisProj);

                this.pv.createActionLogEntry (uo,
                                              prefix + " asset type from: " +
                                              importingFrom,
                                              conn);

                thisProj.addUserConfigurableObjectType (uo);

            }

            if (n instanceof Asset)
            {

                Asset a = (Asset) n;

                String prefix = "Imported";

                int max = 0;

                String name = a.getName ();

                // See if we should merge.
                Asset oa = this.pv.getProject ().getAssetByName (name,
                                                                 this.typeKeyMapping.get (a.getUserConfigurableObjectType ()));

                if (oa != null)
                {

                    for (Asset pa : this.pv.getProject ().getAssets (this.typeKeyMapping.get (a.getUserConfigurableObjectType ())))
                    {

                         if (pa.getName ().toLowerCase ().startsWith (name.toLowerCase ()))
                         {

                             // See if there is a number at the end.
                             int ind = pa.getName ().lastIndexOf ("(");

                             if (ind != -1)
                             {

                                 int ind2 = pa.getName ().indexOf (")",
                                                                   ind + 1);

                                 if (ind2 != -1)
                                 {

                                     String nn = pa.getName ().substring (ind + 1,
                                                                          ind2);

                                     try
                                     {

                                         int m = Integer.parseInt (nn);

                                         if (m > max)
                                         {

                                             max = m;

                                         }

                                     } catch (Exception e)
                                     {

                                         // Ignore.

                                     }

                                 }

                             } else
                             {

                                 if (max == 0)
                                 {

                                     max = 1;

                                 }

                             }

                         }

                     }

                     if (max > 0)
                     {

                         max++;

                         name = name + " (" + max + ")";

                     }

                     a.setName (name);

                    //continue;
                    // Don't merge but don't add either, merging is no longer viable.
                    /*
                    // Merge.
                    oa.merge (a);

                    a = oa;

                    prefix = "Merged";
                    */

                }

                UserConfigurableObjectType at = a.getUserConfigurableObjectType ();

                at = this.addUserConfigurableObjectType (conn,
                                                         this.pv.getObjectManager (),
                                                         at,
                                                         thisProj);

                a.setUserConfigurableObjectType (at);
                a.setKey (null);
                a.setId (null);
                a.setDateCreated (new java.util.Date ());

                this.pv.saveObject (a,
                                    conn);

                // Set up the fields
                for (UserConfigurableObjectField of : a.getFields ())
                {
                    // Update the key of user config object type field.
                    // Set the field key to be null (so a new one is created)
                    of.setUserConfigurableObjectTypeField (this.fieldKeyMapping.get (of.getUserConfigurableObjectTypeField ()));
                    of.setKey (null);
                    of.setId (null);
                    of.setDateCreated (new java.util.Date ());

                    for (String fn : of.getProjectFileNames ())
                    {

                        if (fn == null)
                        {

                            continue;

                        }

                        filesToCopy.add (fn);

                    }

                    this.pv.saveObject (of,
                                        conn);

                }

                thisProj.addUserConfigurableObjectType (at);

                thisProj.addAsset (a);

                this.pv.createActionLogEntry (a,
                                              prefix + " asset from: " +
                                              importingFrom,
                                              conn);

                this.pv.openObjectSection (a.getObjectType ());

            }

        }

        for (String fn : filesToCopy)
        {

            if (this.importFromProj != null)
            {

                File file = new File (this.importFromProj.getFilesDirectory (),
                                      fn);

                if (file.exists ())
                {

                    thisProj.saveToFilesDirectory (file,
                                                   fn);

                }

            }

        }

    }

    private void createNewProject ()
    {

        Project p = new Project (this.newProjectPanel.getName ());

        String pwd = this.newProjectPanel.getPassword ();
        p.setFilePassword (pwd);

        p.setProjectDirectory (this.newProjectPanel.getSaveDirectory ().toFile ());

        ObjectManager om = null;
        Connection conn = null;

        try
        {

            om = Environment.createProject (p.getProjectDirectory ().toPath (),
                                            p);

            conn = om.getConnection ();

            Book b = new Book (p);

            p.addBook (b);

            om.saveObject (b,
                           conn);

            Set<String> filesToCopy = new LinkedHashSet<> ();
            Set<NamedObject> selected = new LinkedHashSet<> ();
            this.getSelectedObjects (this.itemsTree.getRoot (),
                                     selected);

            for (NamedObject n : selected)
            {

                if (n instanceof Chapter)
                {

                    n.setKey (null);
                    n.setDateCreated (new java.util.Date ());
                    n.setId (null);

                    b.addChapter ((Chapter) n);

                    om.saveObject (n,
                                   conn);

                }

                // Old -> New key mapping
                Map<Long, Long> tmapping = new HashMap<> ();

                if (n instanceof UserConfigurableObjectType)
                {

                    UserConfigurableObjectType uo = (UserConfigurableObjectType) n;

                    uo = this.addUserConfigurableObjectType (conn,
                                                             om,
                                                             uo,
                                                             p);

                    p.addUserConfigurableObjectType (uo);

                }

                if (n instanceof Asset)
                {

                    Asset a = (Asset) n;

                    UserConfigurableObjectType at = a.getUserConfigurableObjectType ();

                    n.setKey (null);
                    n.setDateCreated (new java.util.Date ());
                    n.setId (null);

                    this.addUserConfigurableObjectType (conn,
                                                        om,
                                                        at,
                                                        p);

                    a.setUserConfigurableObjectType (this.typeKeyMapping.get (at));

                    om.saveObject (a,
                                   conn);

                    p.addUserConfigurableObjectType (a.getUserConfigurableObjectType ());

                    p.addAsset (a);

                    // Set up the fields
                    for (UserConfigurableObjectField of : a.getFields ())
                    {

                        // Update the key of user config object type field.
                        // Set the field key to be null (so a new one is created)
                        of.setKey (null);
                        of.setId (null);
                        of.setDateCreated (new java.util.Date ());
                        of.setUserConfigurableObjectTypeField (this.fieldKeyMapping.get (of.getUserConfigurableObjectTypeField ()));

                        om.saveObject (of,
                                       conn);

                        for (String fn : of.getProjectFileNames ())
                        {

                            if (fn == null)
                            {

                                continue;

                            }

                            filesToCopy.add (fn);

                        }

                    }

                    om.createActionLogEntry (a,
                                             prefix + " asset from: " +
                                             this.fileFind.getFile (),
                                             null,
                                             conn);

                }

            }

            Chapter c = b.getFirstChapter ();

            // Create a new chapter for the book.
            if (c == null)
            {

                c = new Chapter (b,
                                 Environment.getDefaultChapterName ());

                b.addChapter (c);

                om.saveObject (c,
                               conn);

            }

            om.createActionLogEntry (p,
                                     "Project imported from: " +
                                     this.fileFind.getFile (),
                                     null,
                                     conn);

            for (String fn : filesToCopy)
            {

                if (this.importFromProj != null)
                {

                    File file = new File (this.importFromProj.getFilesDirectory (),
                                          fn);

                    if (file.exists ())
                    {

                        p.saveToFilesDirectory (file,
                                                fn);

                    }

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  p,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (Arrays.asList (importproject,errors,unabletocreateproject),
                                                                          p.getName ()));
                                      //"Unable to create new project: " + this.proj.getName ());

        } finally {

            if (conn != null)
            {

                om.releaseConnection (conn);
                om.closeConnectionPool ();

            }

        }

        try
        {

            Environment.openProject (p);

        } catch (Exception e) {

            Environment.logError ("Unable to open project: " +
                                  p,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (Arrays.asList (importproject,errors,cantopenproject),
                                                                          p.getName ()));

        }


    }

    private void handleFinish ()
    {

        final ImportPopup _this = this;

        if (this.addToProject.isSelected ())
        {

            this.pv.doAsTransaction (conn ->
            {

                try
                {

                    this.addSelectedItemsToProject (conn);

                } catch (Exception e) {

                    Environment.logError ("Unable to save imported objects",
                                          e);

                    ComponentUtils.showErrorMessage (_this.pv,
                                                     getUILanguageStringProperty (Arrays.asList (importproject,errors,unabletosave)));

                }

                UIUtils.runLater (() ->
                {

                    this.pv.fireProjectEvent (ProjectEvent.Type._import,
                                              ProjectEvent.Action.any);

                    QuollPopup.messageBuilder ()
                        .title (importproject,importcompletepopup,title)
                        .withViewer (this.pv)
                        .message (importproject,importcompletepopup,text)
                        .closeButton ()
                        .build ();

                });

            });

        } else {

            this.createNewProject ();

        }

    }

    public void setFilePathToImport (Path p)
    {

        if (p == null)
        {

            this.filePathToImport = null;

            return;

        }

        String fn = p.getFileName ().toString ().toLowerCase ();

        if (!fn.endsWith (".docx"))
        {

            throw new IllegalArgumentException ("File type not supported for path: " + p);

        }

        if (this.fileFind != null)
        {

            this.fileFind.setFile (p);

        }

        this.filePathToImport = p;
        this.addToProject.setSelected (true);
        this.wizard.showStep (SELECT_FILE_STAGE);
        this.handleStepChange (SELECT_FILE_STAGE,
                               SELECT_ITEMS_STAGE);
        this.wizard.showStep (SELECT_ITEMS_STAGE);

    }

    public Wizard.Step getStep (String stepId)
    {

        final ImportPopup _this = this;

        Wizard.Step ws = this.steps.get (stepId);

        if (ws != null)
        {

            return ws;

        }

        ws = new Wizard.Step ();

        if (stepId.equals (NEW_PROJECT_STAGE))
        {

            ws = this.createNewProjectStep ();

        }

        if (stepId.equals (DECIDE_STAGE))
        {

            ws = this.createDecideStep ();

        }

        if (stepId.equals (SELECT_ITEMS_STAGE))
        {

            ws = this.createSelectItemsStep ();

        }

        if (stepId.equals (SELECT_FILE_STAGE))
        {

            ws = this.createSelectFileStep ();

        }

        if (stepId.equals (CHOOSE_STAGE))
        {

            ws = this.createChooseStep ();

        }

        if (stepId.equals (SELECT_PROJECT_STAGE))
        {

            ws = this.createSelectProjectStep ();

        }

        ws.content.getStyleClass ().add (stepId);

        this.steps.put (stepId,
                        ws);

        return ws;

    }

    private Wizard.Step createNewProjectStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,newproject,title);
        //"Enter the new {Project} details";

        VBox b = new VBox ();

        //"To create a new {Project} enter the name below and select the directory where it should be saved.";

        this.newProjectPanel = new NewProjectPanel (this.viewer,
                                                    getUILanguageStringProperty (importproject,stages,newproject,LanguageStrings.text),
                                                    false,
                                                    false);

        b.getChildren ().addAll (this.newProjectPanel);

        // TODO Need to handle the buttons.

        ws.content = b;
/*
        ws.panel = this.newProjectPanel.createPanel (this,
                                                     null,
                                                     false,
                                                     null,
                                                     false);
*/
        return ws;

    }

    private Wizard.Step createDecideStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,decide,title);
        //"What would you like to do?";

                                   //"Add to " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + ": " + pv.getProject ().getName ());
/*
TODO Remove
            this.createNewProject = new JRadioButton (Environment.getUIString (LanguageStrings.importproject,
                                                                               LanguageStrings.stages,
                                                                               LanguageStrings.decide,
                                                                               LanguageStrings.options,
                                                                               LanguageStrings.newproject));
                                                      //"Create a new " + Environment.getObjectTypeName (Project.OBJECT_TYPE));

            this.createNewProject.setOpaque (false);
*/

        QuollRadioButtons buts = QuollRadioButtons.builder ()
            .button (this.addToProject)
            .button (this.createNewProject)
            .build ();

        ws.content = buts;

        return ws;

    }

    private Wizard.Step createSelectItemsStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (Arrays.asList (importproject,stages,selectitems,title));
        //"Select the items you wish to import";

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        desc.textProperty ().bind (UILanguageStringsManager.createStringBinding (() ->
        {

            String extra = "";

            if (this.addToProject.isSelected ())
            {

                extra = getUILanguageStringProperty (importproject,stages,selectitems,textextra).getValue ();

            }

            return getUILanguageStringProperty (Arrays.asList (importproject,stages,selectitems,LanguageStrings.text),
                                                extra).getValue ();

        }));
        // TODO ADD? this.addToProject.selectedProperty ()));
        //"Check the items below to ensure that they match what is in your file.  The first and last sentences of the description (if present) are shown for each item." + (this.addToProject.isSelected () ? ("  Only items not already in the " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + " will be listed.") : "");

/*
        this.itemsTree.addMouseListener (new MouseAdapter ()
            {

                private void selectAllChildren (DefaultTreeModel       model,
                                                DefaultMutableTreeNode n,
                                                boolean                v)
                {

                    Enumeration<TreeNode> en = n.children ();

                    while (en.hasMoreElements ())
                    {

                        DefaultMutableTreeNode c = (DefaultMutableTreeNode) en.nextElement ();

                        Object uo = c.getUserObject ();

                        if (uo instanceof SelectableDataObject)
                        {

                            SelectableDataObject s = (SelectableDataObject) uo;

                            s.selected = v;

                            // Tell the model that something has changed.
                            model.nodeChanged (c);

                            // Iterate.
                            this.selectAllChildren (model,
                                                    c,
                                                    v);

                        }

                    }

                }

                public void mousePressed (MouseEvent ev)
                {

                    TreePath tp = _this.itemsTree.getPathForLocation (ev.getX (),
                                                                      ev.getY ());

                    if (tp != null)
                    {

                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                        // Tell the model that something has changed.
                        DefaultTreeModel model = (DefaultTreeModel) _this.itemsTree.getModel ();

                        SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                        s.selected = !s.selected;

                        model.nodeChanged (n);

                        this.selectAllChildren (model,
                                                n,
                                                s.selected);

                    }

                }

            });

        this.itemsTree.setCellRenderer (new SelectableProjectTreeCellRenderer ());
*/

        ScrollPane sp = new ScrollPane (this.itemsTree);
        //this.createFileSystemTree ());

        //this.itemsTree);
        VBox b = new VBox ();

        b.getChildren ().addAll (desc, sp); //this.itemsTree);

        ws.content = b;

        return ws;

    }

    private Wizard.Step createSelectFileStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,selectfile,title);
        //"Select a file to import";

        VBox b = new VBox ();
        b.getChildren ().add (QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (importproject,stages,selectfile,text))
            .build ());
        //"Microsoft Word files (.doc and .docx) are supported.  Please check <a href='help://projects/importing-a-file'>the import guide</a> to ensure your file has the correct format.";

        // TODO Add the error.
        this.selectFileForm = Form.builder ()
            .item (getUILanguageStringProperty (importproject,stages,selectfile,finder,label),
                   this.fileFind)
            .build ();

        b.getChildren ().add (this.selectFileForm);
/*
        Box b = new Box (BoxLayout.Y_AXIS);

        this.fileFindError.setVisible (false);
        this.fileFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
        b.add (this.fileFindError);
        */
/*
        FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 10px",
                                        "p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        this.fileFind.setOnSelectHandler (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.file = _this.fileFind.getSelectedFile ();

                //_this.checkForFileToImport ();

            }

        });
*/
/*
        this.fileFind.setApproveButtonText (Environment.getUIString (LanguageStrings.importproject,
                                                                     LanguageStrings.stages,
                                                                     LanguageStrings.selectfile,
                                                                     LanguageStrings.finder,
                                                                     LanguageStrings.button));
                                            //"Select");
        this.fileFind.setFinderSelectionMode (JFileChooser.FILES_ONLY);
        this.fileFind.setFinderTitle (Environment.getUIString (LanguageStrings.importproject,
                                                               LanguageStrings.stages,
                                                               LanguageStrings.selectfile,
                                                               LanguageStrings.finder,
                                                               LanguageStrings.title));
*/
        String def = null;

        if (this.pv != null)
        {

            def = this.pv.getProject ().getProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME);

        }

        Path f = null;

        if (this.filePathToImport != null)
        {

            f = this.filePathToImport;

        }

        if (f == null)
        {

            if (def != null)
            {

                f = Paths.get (def);

            }
        }

        if (f == null)
        {

            f = Paths.get (System.getProperty ("user.home"));
            // TODO Remove? FileSystemView.getFileSystemView ().getDefaultDirectory ();

        }

        this.fileFind.setFile (f);
/*
TODO Add tool tip?
        this.fileFind.setFileFilter (ImportProject.fileFilter);

        this.fileFind.setFindButtonToolTip (Environment.getUIString (LanguageStrings.importproject,
                                                                     LanguageStrings.stages,
                                                                     LanguageStrings.selectfile,
                                                                     LanguageStrings.finder,
                                                                     LanguageStrings.title));
*/
        ws.content = b;

        return ws;

    }

    private Wizard.Step createChooseStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        VBox b = new VBox ();

        b.getStyleClass ().add (CHOOSE_STAGE);

        b.getChildren ().add (QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (importproject,stages,choose,text))
            .build ());

        this.importFromFile.setSelected (false);
        this.importFromProject.setSelected (false);

        QuollRadioButtons rbs = QuollRadioButtons.builder ()
            .button (this.importFromFile)
            .button (this.importFromProject)
            .build ();

        b.getChildren ().add (rbs);

        ws.content = b;
        ws.title = getUILanguageStringProperty (importproject,stages,choose,title);
        //"What would you like to import?";
        //"Select whether you wish to import from a file or from one of your {projects}.";

        return ws;

    }

    private Wizard.Step createSelectProjectStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,selectproject,title);
        //"Select a {Project} to import from";
        // TODO: Fix this, we need some text otherwise the textbox collapses.

        List<ProjectInfo> projs = null;

        try
        {

            projs = new ArrayList<> (Environment.getAllProjects ());

        } catch (Exception e) {

            Environment.logError ("Unable to get all projects",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (importproject,stages,selectproject,getallprojectserror));
                                      //"Unable to get all the {projects}.");

            return null;

        }

        if (this.pv != null)
        {

            projs.remove (Environment.getProjectInfo (this.pv.getProject ()));

        }

        Collections.sort (projs,
                          new ProjectInfoSorter ());

        this.projectList = new VBox ();
        this.projectList.getStyleClass ().add (StyleClassNames.PROJECTS);

        projs.stream ()
            .forEach (pr ->
            {

                QuollLabel l = QuollLabel.builder ()
                    .label (pr.nameProperty ())
                    .tooltip (importproject,stages,selectproject,projectslist,tooltip)
                    .build ();

                l.setUserData (pr);

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    UIUtils.toggleSelected (this.projectList,
                                            l);

                    if (UIUtils.isSelected (l))
                    {

                        this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                  true);

                    }

                });

                this.projectList.getChildren ().add (l);

            });
/*
        this.projectList = new JList (new Vector (projs));
        this.projectList.setLayoutOrientation (JList.VERTICAL);
        this.projectList.setOpaque (true);
        this.projectList.setBackground (UIUtils.getComponentColor ());
        this.projectList.setToolTipText (Environment.getUIString (LanguageStrings.importproject,
                                                                  LanguageStrings.stages,
                                                                  LanguageStrings.selectproject,
                                                                  LanguageStrings.projectslist,
                                                                  LanguageStrings.tooltip));
                                         //Environment.replaceObjectNames ("Click to select this {Project}."));
        UIUtils.setAsButton (this.projectList);

        this.projectList.addListSelectionListener (new ListSelectionListener ()
        {

            @Override
            public void valueChanged (ListSelectionEvent ev)
            {

                this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                   true);

            }

        });

        this.projectList.setCellRenderer (new DefaultListCellRenderer ()
        {

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                JLabel c = (JLabel) super.getListCellRendererComponent (list,
                                                                        value,
                                                                        index,
                                                                        isSelected,
                                                                        cellHasFocus);

                ProjectInfo p = (ProjectInfo) value;
                c.setFont (c.getFont ().deriveFont (UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
                c.setText (p.getName ());
                c.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

                if (isSelected)
                {

                    c.setForeground (UIUtils.getComponentColor ());

                } else {

                    c.setForeground (UIUtils.getTitleColor ());

                }

                if (cellHasFocus)
                {

                    c.setBackground (list.getSelectionBackground ());

                }

                return c;

            }

        });
*/
        ScrollPane sp = new ScrollPane (this.projectList);

        VBox b = new VBox ();

        b.getStyleClass ().add (SELECT_PROJECT_STAGE);
        b.getChildren ().add (sp);

        ws.content = b;

        return ws;

    }

    public String getNextStepId (String currStage)
    {

        if (currStage == null)
        {

            return CHOOSE_STAGE;

        }

        if (currStage.equals (CHOOSE_STAGE))
        {

            if (this.importFromFile.isSelected ())
            {

                return SELECT_FILE_STAGE;

            }

            if (this.importFromProject.isSelected ())
            {

                return SELECT_PROJECT_STAGE;

            }

            return CHOOSE_STAGE;

        }

        if (currStage.equals (SELECT_FILE_STAGE))
        {

            return SELECT_ITEMS_STAGE;

        }

        if (SELECT_PROJECT_STAGE.equals (currStage))
        {

            return SELECT_ITEMS_STAGE;

        }

        if (SELECT_ITEMS_STAGE.equals (currStage))
        {

            if (this.newProjectOnly)
            {

                return NEW_PROJECT_STAGE;

            }

            return DECIDE_STAGE;

            //return null;

        }

        if (currStage.equals (DECIDE_STAGE))
        {

            if (this.addToProject.isSelected ())
            {

                return null;

            }

            return NEW_PROJECT_STAGE;

        }

        if (NEW_PROJECT_STAGE.equals (currStage))
        {

            return null;

        }

        return null;

    }

    public String getPreviousStepId (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (SELECT_FILE_STAGE.equals (currStage))
        {

            if (Environment.getAllProjects ().size () == 1)
            {

                return null;

            }

        }

        if (currStage.equals (NEW_PROJECT_STAGE))
        {

            if (this.newProjectOnly)
            {

                return SELECT_ITEMS_STAGE;

            }

            return DECIDE_STAGE;

        }

        if (currStage.equals (SELECT_FILE_STAGE))
        {

            return CHOOSE_STAGE;

        }

        if (currStage.equals (SELECT_PROJECT_STAGE))
        {

            return CHOOSE_STAGE;

        }

        if (currStage.equals (DECIDE_STAGE))
        {

            return SELECT_ITEMS_STAGE;

        }

        if (currStage.equals (SELECT_ITEMS_STAGE))
        {

            if (this.importFromProject.isSelected ())
            {

                return SELECT_PROJECT_STAGE;

            }

            return SELECT_FILE_STAGE;

        }

        return null;

    }

    private void getSelectedObjects (TreeItem<NamedObject> item,
                                     Set<NamedObject>      selected)
    {

        if (item instanceof CheckBoxTreeItem)
        {

            CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) item;

            if (cti.isSelected ())
            {

                selected.add (cti.getValue ());

            }

        }

        item.getChildren ().stream ()
            .forEach (it -> getSelectedObjects (it, selected));

    }

    private Project getSelectedItems ()
    {

        String projName = null;

        if (this.newProjectPanel == null)
        {

            projName = this.pv.getProject ().getName ();

        } else {

            projName = this.newProjectPanel.getName ();

        }

        //this.filesToCopy = new HashSet<> ();

        Project p = new Project (projName);

        Book b = new Book (p,
                           null);

        if (this.proj != null)
        {

            p.setProjectDirectory (this.proj.getProjectDirectory ());

        }

        p.addBook (b);
        b.setName (projName);

        Set<NamedObject> selected = new LinkedHashSet<> ();
        this.getSelectedObjects (this.itemsTree.getRoot (),
                                 selected);

        for (NamedObject n : selected)
        {

            if (n instanceof Chapter)
            {

                b.addChapter ((Chapter) n);

            }

            Project thisProj = this.pv.getProject ();

            if (n instanceof UserConfigurableObjectType)
            {

                UserConfigurableObjectType uo = (UserConfigurableObjectType) n;

                // See if we already have this type
                UserConfigurableObjectType t = thisProj.getUserConfigurableObjectTypeByName (uo.getObjectTypeName ());

                if (t == null)
                {

                    // Add the type to this project.

                } else {

                    // We have the type, are the fields compatible?
                    for (UserConfigurableObjectTypeField f : uo.getConfigurableFields ())
                    {

                        // Does the current project have the field we are trying to import?
                        UserConfigurableObjectTypeField of = t.getConfigurableFieldByFormName (f.getFormName ());

                        if (of == null)
                        {

                            // Doesn't have, add a new field.  Add it to the first layout column.

                        }

                    }

                }

            }

            if (n instanceof Asset)
            {

                Asset a = (Asset) n;

                UserConfigurableObjectType at = a.getUserConfigurableObjectType ();

                // Do we have the type?
                UserConfigurableObjectType ot = thisProj.getUserConfigurableObjectTypeByName (at.getObjectTypeName ());

                if (ot == null)
                {

                    // Add the type to this project.

                } else {

                    a.setUserConfigurableObjectType (ot);

                    // We have the type, are the fields compatible?
                    for (UserConfigurableObjectTypeField f : at.getConfigurableFields ())
                    {

                        // Does the current project have the field we are trying to import?
/*
                        UserConfigurableObjectTypeField of = t.getConfigurableTypeFieldByFormName (f.getFormName ());

                        if (of == null)
                        {

                            // Doesn't have, add a new field.  Add it to the first layout column.
System.out.println ("DONT HAVE FIELD: " + f.getFormName ());
                        }
*/
                        f.setKey (null);

                    }

                }

                p.addAsset (a);

            }

        }

        if ((b.getChapters () != null)
            &&
            (b.getChapters ().size () == 0)
           )
        {

            p.getBooks ().remove (b);

        }

        return p;

    }

    private boolean checkForFileToImport ()
    {

        final ImportPopup _this = this;

        this.selectFileForm.hideError ();

        Path f = this.fileFind.getFile ();

        Path file = null;

        if (f != null)
        {

            file = f;

        }

        if (file == null)
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,nofileselected));
                                        //"Please select a file to import.");
            return false;

        }

        if (Files.notExists (file))
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,filenotexist));
            //"File does not exist, please select a valid file.");
            return false;

        }

        // TODO, remove this can't happen.
        if (Files.isDirectory (file))
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,dirselected));
            return false;

        }

        try
        {

            Importer.importProject (file.toUri (),
                                    new ImportCallback ()
            {

                @Override
                public void projectCreated (Project p,
                                            URI     u)
                {

                    UIUtils.runLater (() ->
                    {

                        if (_this.itemsTree != null)
                        {

                            _this.itemsTree.setRoot (_this.createTree (p));

                            // TODO Needed? UIUtils.expandAllNodesWithChildren (_this.itemsTree.getRoot ());
                            _this.proj = p;

                            _this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                       true);

                        }

                    });

                }

                public void exceptionOccurred (Exception e,
                                               URI       u)
                {

                    Environment.logError ("Unable to import file/directory: " +
                                          u,
                                          e);

                    UIUtils.runLater (() ->
                    {

                        ComponentUtils.showErrorMessage (_this.getViewer (),
                                                         getUILanguageStringProperty (importproject,errors,general));
                                                      //"Unable to import file");

                    });

                }

            });

        } catch (Exception e)
        {

            Environment.logError ("Unable to convert: " +
                                  file +
                                  " to a uri",
                                  e);

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,openfile));

            return false;

        }

        return true;

    }

    private TreeItem<NamedObject> createTree (Project p)
    {

        TreeItem<NamedObject> root = new TreeItem<> (p);

        if (p.getBooks ().size () > 0)
        {

            Book b = p.getBooks ().get (0);

            if (b.getChapters ().size () > 0)
            {

                TreeParentNode c = new TreeParentNode (Chapter.OBJECT_TYPE,
                                                       // Should use the property instead?
                                                       Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE).getValue ());

                CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (c);
                root.getChildren ().add (ci);

                for (Chapter ch : b.getChapters ())
                {

                    if ((this.pv != null) &&
                        (this.addToProject.isSelected ()))
                    {

                        if (this.pv.getProject ().getBooks ().get (0).getChapterByName (ch.getName ()) != null)
                        {

                            continue;

                        }

                    }

                    CheckBoxTreeItem<NamedObject> chi = new CheckBoxTreeItem<> (ch);
                    ci.getChildren ().add (chi);
                    chi.setExpanded (true);

                    String t = this.getFirstLastSentence (ch.getChapterText ());

                    if (t.length () > 0)
                    {

                        TreeParentNode n = new TreeParentNode ("_string",
                                                               t);

                        // Get the first and last sentence.
                        chi.getChildren ().add (new TreeItem<NamedObject> (n));

                    }

                }

            }

        }

        Set<UserConfigurableObjectType> assetTypes = p.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = p.getAssets (t);

            if (as.size () > 0)
            {

                this.addAssetsToTree (root,
                                      t,
                                      p,
                                      as);

            } else {

                CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (t);

                root.getChildren ().add (ci);

            }

        }

        root.addEventHandler (CheckBoxTreeItem.checkBoxSelectionChangedEvent (),
                              ev ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      UIUtils.getSelectedCount (root) > 0);

        });

        return root;

    }

    private void addAssetsToTree (TreeItem<NamedObject>      root,
                                  UserConfigurableObjectType type,
                                  Project                    p,
                                  Set<Asset>                 assets)
    {

        if ((assets == null)
            ||
            (assets.size () == 0)
           )
        {

            return;

        }

        CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (type);

        root.getChildren ().add (ci);

        List<Asset> lassets = new ArrayList<> (assets);

        Collections.sort (lassets,
                          new NamedObjectSorter (p));

        for (Asset a : lassets)
        {

            if ((this.pv != null) &&
                (this.addToProject.isSelected ()))
            {

                if (this.pv.getProject ().hasAsset (a))
                {

                    continue;

                }

            }

            CheckBoxTreeItem<NamedObject> cai = new CheckBoxTreeItem<> (a);

            ci.getChildren ().add (cai);

            String t = this.getFirstLastSentence (a.getDescriptionText ());

            if (t.length () > 0)
            {

                // Get the first and last sentence.
                cai.getChildren ().add (new TreeItem<NamedObject> (new TreeParentNode ("_string", t)));

            }

        }

    }

    private String getFirstLastSentence (String s)
    {

        if (s == null)
        {

            return "";

        }

        Paragraph p = new Paragraph (s,
                                     0);

        if (p.getSentenceCount () == 0)
        {

            return "";

        }

        StringBuilder b = new StringBuilder (p.getFirstSentence ().getText ());

        if (p.getSentenceCount () > 1)
        {

            b.append (getUILanguageStringProperty (importproject,moretextindicator).getValue ());

            b.append (p.getFirstSentence ().getNext ().getText ());

        }

        return b.toString ();

    }

    public void setAddToProjectOnly (boolean v)
    {

        this.addToProjectOnly = v;

    }

    public void setNewProjectOnly (boolean v)
    {

        this.newProjectOnly = v;

    }

    private void enableButtons (Wizard wizard,
                                String currentStepId)
    {

        wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                             true);

        if (CHOOSE_STAGE.equals (currentStepId))
        {

            if ((!this.importFromFile.isSelected ())
                &&
                (!this.importFromProject.isSelected ())
               )
            {

                wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                     false);

            }

        }

        if ((SELECT_ITEMS_STAGE.equals (currentStepId))
            &&
            (this.proj == null)
           )
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 false);

        }

        if ((SELECT_ITEMS_STAGE.equals (currentStepId))
            &&
            (this.itemsTree.getRoot () != null)
           )
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 UIUtils.getSelectedCount (this.itemsTree.getRoot ()) > 0);

        }

        if (SELECT_PROJECT_STAGE.equals (currentStepId))
        {

            if (UIUtils.getSelected (this.projectList).size () == 0)
            {

                wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                     false);

            }

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (importproject,title))
            .styleClassName (StyleClassNames.IMPORT)
            .styleSheet (StyleClassNames.IMPORT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type._import,
                                          ProjectEvent.Action.show);

        return p;

    }

}
