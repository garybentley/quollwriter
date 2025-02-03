package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.io.*;
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

import com.quollwriter.exporter.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

// TEST ADD
import javafx.beans.value.*;
import javafx.collections.*;

public class ExportProjectPopup extends PopupContent<ProjectViewer>
{

    public static final String POPUP_ID = "exportproject";

    public static final String WHERE_TO_SAVE_STAGE = "where-to-save";
    public static final String SELECT_ITEMS_STAGE = "select-items";

    private static Map<String, String> fileTypes = new LinkedHashMap<> ();
    private static Map<String, Class> handlers = new HashMap<> ();

    static
    {

        Map m = ExportProjectPopup.handlers;

        m.put (Constants.HTML_FILE_EXTENSION,
               HTMLDocumentExporter.class);
        m.put (Constants.DOCX_FILE_EXTENSION,
               MSWordDocXDocumentExporter.class);
        m.put (Constants.EPUB_FILE_EXTENSION,
               EPUBDocumentExporter.class);
        m.put (Constants.PDF_FILE_EXTENSION,
               PDFDocumentExporter.class);
        m.put (Constants.HTML_FILE_EXTENSION,
               HTMLDocumentExporter.class);
        m.put (Constants.PDF_FILE_EXTENSION,
               PDFDocumentExporter.class);
        /*
        m.put (Constants.TXT_FILE_EXTENSION,
               TextDocumentExporter.class);
*/

        m = ExportProjectPopup.fileTypes;

        m.put ("Microsoft Word 2007 (.docx)",
               Constants.DOCX_FILE_EXTENSION);
        m.put ("EPUB (.epub)",
               Constants.EPUB_FILE_EXTENSION);
        /*
        m.put ("PDF (.pdf)",
               Constants.PDF_FILE_EXTENSION);
               */
               /*
               Why are you so bad???
        m.put ("HTML (.html)",
               Constants.HTML_FILE_EXTENSION);
               */
        /*
        m.put ("PDF",
               "pdf");
        m.put ("Web page (.html)",
               "html");
        m.put ("Plain text (.txt)",
               "txt");
         */
    }

    private Project                       proj = null;
    private Map<String, DocumentExporter> exporters = new HashMap<> ();

    private Wizard wizard = null;
    private QuollTreeView<NamedObject> itemsTree = null;

    private QuollFileField dirFind = null;
    private QuollChoiceBox fileType = null;
    private Form dirFindForm = null;

    private Map<String, Wizard.Step> steps = new HashMap<> ();

    public ExportProjectPopup (ProjectViewer viewer)
    {

        super (viewer);

        this.proj = this.viewer.getProject ();

        String def = this.viewer.getProject ().getProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME);

        java.io.File defFile = javax.swing.filechooser.FileSystemView.getFileSystemView ().getDefaultDirectory ();

        if (def != null)
        {

            defFile = new File (def);

        }

        this.dirFind = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (exportproject,stages,wheretosave,finder,title))
            .limitTo (QuollFileField.Type.directory)
            .initialFile (defFile.toPath ())
            .withViewer (viewer)
            .build ();

        Set<StringProperty> types = new LinkedHashSet<> ();

        ExportProjectPopup.fileTypes.keySet ().stream ()
            .forEach (ft -> types.add (new SimpleStringProperty (ft)));

        this.fileType = QuollChoiceBox.builder ()
            .items (types)
            .build ();

        this.dirFindForm = Form.builder ()
            .item (getUILanguageStringProperty (exportproject,stages,wheretosave,finder,label),
                  this.dirFind)
            .item (getUILanguageStringProperty (exportproject,stages,wheretosave,filetype,label),
                   this.fileType)
            .build ();

        this.itemsTree = new QuollTreeView<> ();

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
                        .styleClassName (com.quollwriter.ui.fx.StyleClassNames.TEXT)
                        .build ();

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

        return WHERE_TO_SAVE_STAGE;

    }

    public boolean handleStepChange (String oldStepId,
                                     String newStepId)
    {

        final ExportProjectPopup _this = this;

        return true;

    }

    private void handleFinish ()
    {

        Path dir = this.dirFind.getFile ();

        try
        {

            DocumentExporter de = this.getExporter ();

            Files.createDirectories (dir);

            de.exportProject (dir,
                              this.getSelectedItems ());

            this.viewer.saveObject (this.proj,
                                    false);

            this.viewer.createActionLogEntry (this.proj,
                                              "Exported project to directory: " +
                                              dir);

            this.viewer.fireProjectEvent (ProjectEvent.Type._export,
                                          ProjectEvent.Action.any);

        } catch (Exception e)
        {

            Environment.logError ("Unable to export project: " +
                                  this.proj,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (exportproject,actionerror));

            return;

        }

        QuollPopup.messageBuilder ()
            .withViewer (this.viewer)
            .styleClassName (StyleClassNames.EXPORT)
            .closeButton ()
            .title (exportproject,exportcompletepopup,title)
            .message (getUILanguageStringProperty (Arrays.asList (exportproject,exportcompletepopup,text),
                                                                  this.proj.getName (),
                                                                  this.dirFind.getFile ().toUri ().toString (),
                                                                  this.dirFind.getFile ()))
            .build ();

        try
        {

            UIUtils.showFile (viewer,
                              dir);

        } catch (Exception e)
        {

        }

    }

    private Wizard.Step createWhereToSaveStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (exportproject,stages,wheretosave,title);
        //"Select the file type and directory to save to";

        ws.content = this.dirFindForm;

        return ws;

    }

    public Wizard.Step getStep (String stepId)
    {

        final ExportProjectPopup _this = this;

        Wizard.Step ws = this.steps.get (stepId);

        if (ws != null)
        {

            return ws;

        }

        ws = null;

        if (WHERE_TO_SAVE_STAGE.equals (stepId))
        {

            ws = this.createWhereToSaveStep ();

        }

        if (SELECT_ITEMS_STAGE.equals (stepId))
        {

            ws = this.createSelectItemsStep ();

        }

        if (ws == null)
        {

            DocumentExporter de = null;

            try
            {

                de = this.getExporter ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to get exporter",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (exportproject,actionerror));

                return null;

            }

            String ft = this.getFileType ();

            if (stepId.startsWith (ft))
            {

                stepId = stepId.substring (ft.length () + 1);

            }

            de.setProject (this.viewer.getProject ());
            ws = de.getStage (stepId);

        }

        this.steps.put (stepId,
                        ws);

        return ws;

    }

    private Wizard.Step createSelectItemsStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (exportproject,stages,selectitems,title);
        //"Select the items you wish to export";

        VBox b = new VBox ();
        b.getChildren ().add (QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (exportproject,stages,selectitems,text))
            .build ());

        this.itemsTree.setRoot (this.createTree (this.viewer.getProject ()));

        ScrollPane sp = new ScrollPane (this.itemsTree);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        b.getChildren ().add (sp);

        ws.content = b;
        ws.content.getStyleClass ().add (SELECT_ITEMS_STAGE);

        return ws;

    }

    private String getFileType ()
    {

        return ExportProjectPopup.fileTypes.get (this.fileType.getSelectionModel ().getSelectedItem ().getValue ());

    }

    public DocumentExporter getExporter ()
                                  throws GeneralException
    {

        String fileType = this.getFileType ();

        DocumentExporter de = this.exporters.get (fileType);

        if (de != null)
        {

            return de;

        }

        Class c = ExportProjectPopup.handlers.get (fileType);

        if (c == null)
        {

            throw new GeneralException ("Unable to find export handler for extension: " +
                                        fileType);

        }

        try
        {

            de = (DocumentExporter) c.getDeclaredConstructor ().newInstance ();
            de.setProject (this.viewer.getProject ());

            this.exporters.put (fileType,
                                de);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create new instance of exporter: " +
                                        c.getName () +
                                        " for file type: " +
                                        fileType,
                                        e);

        }

        return de;

    }
/*
    private Wizard.Step createSelectItemsStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (Arrays.asList (importproject,stages,selectitems,title));
        //"Select the items you wish to import";

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        desc.textProperty ().bind (Bindings.createStringBinding (() ->
        {

            return String.format (getUILanguageStringProperty (importproject,stages,selectitems,text).getValue (),
                                  (this.addToProject.isSelected () ? getUILanguageStringProperty (importproject,stages,selectitems,textextra).getValue () : ""));

        },
        this.addToProject.selectedProperty ()));
        //"Check the items below to ensure that they match what is in your file.  The first and last sentences of the description (if present) are shown for each item." + (this.addToProject.isSelected () ? ("  Only items not already in the " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + " will be listed.") : "");

        ScrollPane sp = new ScrollPane (this.itemsTree);
        //this.createFileSystemTree ());

        //this.itemsTree);
        VBox b = new VBox ();

        b.getChildren ().addAll (desc, sp); //this.itemsTree);

        ws.content = b;

        return ws;

    }
*/

    public String getNextStepId (String currStage)
    {

        if (currStage == null)
        {

            return WHERE_TO_SAVE_STAGE;

        }

        if (WHERE_TO_SAVE_STAGE.equals (currStage))
        {

            // Save the directory selected by the user.
            try
            {

                this.proj.setProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME,
                                       this.dirFind.getFile ().toString ());

            } catch (Exception e)
            {

                // No need to bother the user with this.
                Environment.logError ("Unable to save property: " +
                                      Constants.EXPORT_DIRECTORY_PROPERTY_NAME +
                                      " with value: " +
                                      this.dirFind.getFile (),
                                      e);

            }

            return SELECT_ITEMS_STAGE;

        }

        // Create a new exporter.
        DocumentExporter de = null;

        try
        {

            de = this.getExporter ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to get exporter",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (exportproject,errors,createexporter));
            //"Unable to go to next page, please contact support for assistance.");

            return null;

        }

        String ft = this.getFileType ();

        if (currStage.startsWith (ft))
        {

            currStage = currStage.substring (ft.length () + 1);

        }

        return de.getNextStage (currStage);

    }

    public String getPreviousStepId (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (SELECT_ITEMS_STAGE.equals (currStage))
        {

            return WHERE_TO_SAVE_STAGE;

        }

        if (WHERE_TO_SAVE_STAGE.equals (currStage))
        {

            return null;

        }

        // Create a new exporter.
        DocumentExporter de = null;

        try
        {

            de = this.getExporter ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to get exporter",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (exportproject,errors,createexporter));
                                      //"Unable to go to previous page, please contact support for assistance.");

            return null;

        }

        String ft = this.getFileType ();

        if (currStage.startsWith (ft))
        {

            currStage = currStage.substring (ft.length () + 1);

        }

        String stage = de.getPreviousStage (currStage);

        if (stage == null)
        {

            return SELECT_ITEMS_STAGE;
            //WHERE_TO_SAVE_STAGE;

        }

        return this.getFileType () + ":" + stage;

    }

    private void getSelectedObjects (TreeItem<NamedObject> item,
                                     Set<NamedObject>      selected)
    {

        if (item instanceof CheckBoxTreeItem)
        {

            CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) item;

            if ((cti.isSelected ())
                &&
                (!(cti.getValue () instanceof TreeParentNode))
               )
            {

                selected.add (cti.getValue ());

            }

        }

        item.getChildren ().stream ()
            .forEach (it -> getSelectedObjects (it, selected));

    }

    private Project getSelectedItems ()
    {

        Project p = new Project (this.proj.getName ());

        Book b = new Book (p,
                           null);

        p.addBook (b);
        b.setName (p.getName ());

        Set<NamedObject> selected = new LinkedHashSet<> ();
        this.getSelectedObjects (this.itemsTree.getRoot (),
                                 selected);

        for (NamedObject n : selected)
        {

            if (n instanceof Chapter)
            {

                b.addChapter ((Chapter) n);

            }

            if (n instanceof Asset)
            {

                Asset a = (Asset) n;

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

    private boolean checkForFile ()
    {

        this.dirFindForm.hideError ();

        Path f = this.dirFind.getFile ();

        Path file = null;

        if (f != null)
        {

            file = f;

        }

        if (file == null)
        {

            this.dirFindForm.showError (getUILanguageStringProperty (exportproject,errors,nodirselected));
                                        //"Please select a file to import.");
            return false;

        }
/*
        if (Files.notExists (file))
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,filenotexist));
            //"File does not exist, please select a valid file.");
            return false;

        }
*/

        return true;

    }

    private CheckBoxTreeItem<NamedObject> createTree (Project p)
    {

        CheckBoxTreeItem<NamedObject> root = new CheckBoxTreeItem<> (p);

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

                    CheckBoxTreeItem<NamedObject> chi = new CheckBoxTreeItem<> (ch);
                    ci.getChildren ().add (chi);
                    chi.setExpanded (true);

                }

            }

        }

        Set<UserConfigurableObjectType> assetTypes = p.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = p.getAssets (t);

            this.addAssetsToTree (root,
                                  t,
                                  p,
                                  as);

        }

        root.addEventHandler (CheckBoxTreeItem.checkBoxSelectionChangedEvent (),
                              ev ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      this.getSelectedCount (root) > 0);

        });

        return root;

    }

    private int getSelectedCount ()
    {

        return this.getSelectedCount (this.itemsTree.getRoot ());

    }

    private int getSelectedCount (TreeItem<NamedObject> item)
    {

        int c = 0;

        if (item instanceof CheckBoxTreeItem)
        {

            c += ((CheckBoxTreeItem<NamedObject>) item).isSelected () ? 1 : 0;

        }

        c += item.getChildren ().stream ()
            .mapToInt (it ->
            {

                int _c = 0;

                if (it instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cit = (CheckBoxTreeItem<NamedObject>) it;

                    _c = cit.isSelected () ? 1 : 0 + this.getSelectedCount (cit);

                }

                return _c;

            })
            .sum ();

        return c;

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

            CheckBoxTreeItem<NamedObject> cai = new CheckBoxTreeItem<> (a);

            ci.getChildren ().add (cai);

        }

    }

    private void enableButtons (Wizard wizard,
                                String currentStepId)
    {

        wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                             true);

        if (WHERE_TO_SAVE_STAGE.equals (currentStepId))
        {

            if (this.dirFind.getFile () == null)
            {

                wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                     false);

            }

        }

        if (SELECT_ITEMS_STAGE.equals (currentStepId))
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 this.getSelectedCount () > 0);

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (exportproject,LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.EXPORT)
            .styleSheet (StyleClassNames.EXPORT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type._export,
                                          ProjectEvent.Action.show);

        return p;

    }

}
