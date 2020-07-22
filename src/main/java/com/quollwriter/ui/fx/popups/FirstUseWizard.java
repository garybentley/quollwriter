package com.quollwriter.ui.fx.popups;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.text.*;
import java.net.*;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.util.*;
import javafx.collections.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.editors.*;
import com.quollwriter.importer.*;
import com.quollwriter.text.Paragraph;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsInfo;
import com.quollwriter.uistrings.UILanguageStrings;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class FirstUseWizard extends PopupContent
{

    public static final String POPUP_ID = "firstuse";

    private final static String SELECT_FILE_STAGE = "select-file";
    private final static String DECIDE_STAGE = "decide";
    private final static String START_STAGE = "start";
    private static final String EXISTING_STAGE = "existing";
    private static final String NEW_PROJECT_STAGE = "newproject";
    private static final String IMPORT_STAGE = "import";
    private static final String SELECT_PROJECT_DB_STAGE = "select-project-db";
    private static final String SPELL_CHECK_LANG_STAGE = "spell-check-lang";

    private Wizard wizard = null;
    private Form fileFindForm = null;
    private QuollFileField fileFind = null;
    private QuollTreeView<NamedObject> itemsTree = null;
    private ComboBox<UILanguageStringsInfo> uilangSel = null;
    private QuollLabel downloading = null;
    private Form uilangSelForm = null;
    private QuollRadioButton importFile = null;
    private QuollRadioButton createNewProject = null;

    private QuollRadioButton findProjects = null;
    private QuollRadioButton selectProjectDB = null;

    private Form projDBFindForm = null;
    private QuollFileField projDBFind = null;
    private Project importProj = null;

    private NewProjectPanel newProjectPanel = null;

    private IPropertyBinder binder = null;
    private Map<String, Wizard.Step> steps = new HashMap<> ();

    public FirstUseWizard ()
    {

        super (null);

        this.binder = new PropertyBinder ();

        this.newProjectPanel = new NewProjectPanel (this.viewer,
                                                    getUILanguageStringProperty (firstusewizard,stages,newproject,LanguageStrings.text),
                                                    false,
                                                    false);

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

        this.wizard = Wizard.builder ()
            .startStepId (START_STAGE)
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

                Wizard.Step ws = this.steps.get (currId);

                if (ws == null)
                {

                    ws = this.getStep (currId);

                    this.steps.put (currId,
                                    ws);

                }

                return ws;

            })
            .onCancel (ev ->
            {

                Environment.showAllProjectsViewer ();
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

                this.enableButtons (ev.getCurrentStepId (),
                                    ev.getWizard ());

            })
            .build ();

        VBox b = new VBox ();
        VBox.setVgrow (this.wizard, Priority.ALWAYS);

        b.getChildren ().add (QuollTextView.builder ()
            .text (getUILanguageStringProperty (firstusewizard,text))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ());

        b.getChildren ().addAll (this.wizard);

        this.getChildren ().addAll (b);

    }

    @Override
    public void close ()
    {

        this.binder.dispose ();

        super.close ();

    }

    public boolean handleStepChange (String oldStage,
                                     String newStage)
    {

        final FirstUseWizard _this = this;

        if (SELECT_FILE_STAGE.equals (oldStage))
        {

            if (!IMPORT_STAGE.equals (newStage))
            {

                return true;

            }

            this.fileFindForm.hideError ();

            Path f = this.fileFind.getFile ();

            if (f == null)
            {

                this.fileFindForm.showError (getUILanguageStringProperty (firstusewizard,stages,selectfile,errors,novalue));

                return false;

            }

            //this.setImportFile (this.fileFind.getFile ());

            return true;

        }

        if (DECIDE_STAGE.equals (oldStage))
        {

            if (START_STAGE.equals (newStage))
            {

                return true;

            }

            return true;
/*
            if (this.importFile.isSelected ())
            {

                this.showStage (SELECT_FILE_STAGE);

                return false;

            }

            if (this.createNewProject.isSelected ())
            {

                this.newProjectPanel.setProject (null);
                this.newProjectPanel.setName (null);

                this.showStage (NEW_PROJECT_STAGE);

                return false;

            }
*/
        }

        if ((START_STAGE.equals (oldStage))
            &&
            (SPELL_CHECK_LANG_STAGE.equals (newStage))
           )
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      true);
            return true;
/*
            String lsid = UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

            try
            {

                if (UILanguageStringsManager.getUILanguageStrings (lsid) == null)
                {

                    this.uilangSelForm.hideError ();
                    this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                              false);
System.out.println ("HERE");
                    // Show the downloading message.
                    this.downloading.setVisible (true);

                    UILanguageStringsManager.downloadUILanguageFile (lsid,
                                                                     () ->
                                                                     {
                                                                        // Set the language.
                                                                        try
                                                                        {

                                                                            Environment.setUILanguage (lsid);

                                                                        } catch (Exception e) {

                                                                            Environment.logError ("Unable to set ui language to: " + lsid,
                                                                                                  e);

                                                                            _this.uilangSelForm.showError (getUILanguageStringProperty (uilanguage,set,actionerror));

                                                                            return;

                                                                        }

                                                                        //_this.dlUILangFile.setVisible (true);
                                                                        _this.wizard.showStep (SPELL_CHECK_LANG_STAGE);
                                                                    },
                                                                    // On error
                                                                    () ->
                                                                    {

                                                                        _this.uilangSelForm.showError (getUILanguageStringProperty (uilanguage,set,actionerror));

                                                                        //_this.downloading.setVisible (false);

                                                                    });

                    return false;

                } else {

                    // Set the language.
                    try
                    {

                        Environment.setUILanguage (lsid);

                    } catch (Exception e) {

                        Environment.logError ("Unable to set ui language to: " + lsid,
                                              e);

                        _this.uilangSelForm.showError (getUILanguageStringProperty (uilanguage,set,actionerror));

                        _this.downloading.setVisible (false);

                        return false;

                    }
System.out.println ("HERE2");
                    _this.downloading.setVisible (false);
                    _this.wizard.showStep (SPELL_CHECK_LANG_STAGE);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get ui language strings for: " + lsid,
                                      e);

                this.uilangSelForm.showError (getUILanguageStringProperty (firstusewizard,stages,start,actionerror));

                return false;

            }
*/
        }

        if ((SPELL_CHECK_LANG_STAGE.equals (oldStage))
            &&
            (DECIDE_STAGE.equals (newStage))
           )
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      false);

            return true;

        }

        if ((newStage == null)
            &&
            (NEW_PROJECT_STAGE.equals (oldStage))
           )
        {

            if (this.importFile.isSelected ())
            {

                // Need to create a project with the selected items.
                this.newProjectPanel.setProject (this.getSelectedItems ());

            }

            return this.newProjectPanel.createProject ();

        }

        if ((EXISTING_STAGE.equals (oldStage))
            &&
            (newStage == null)
           )
        {

            if (this.findProjects.isSelected ())
            {

                Environment.showAllProjectsViewer ();

                this.close ();

                return false;

            }

        }

        return true;

    }

    private void enableButtons (String currentStage,
                                Wizard wizard)
    {

        //wizard.enableButtons ();

        if (START_STAGE.equals (currentStage))
        {

            if (this.uilangSel.getSelectionModel ().getSelectedItem () != null)
            {

                return;

            }

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 false);

        }

        if (NEW_PROJECT_STAGE.equals (currentStage))
        {

            wizard.enableButton (Wizard.FINISH_BUTTON_ID,
                                 true);

        }

        if (DECIDE_STAGE.equals (currentStage))
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 (this.importFile.isSelected () || this.createNewProject.isSelected ()));

        }

        if (SELECT_FILE_STAGE.equals (currentStage))
        {

            boolean v = this.doesFileHaveExtension (this.fileFind.getFile (),
                                                    Constants.DOCX_FILE_EXTENSION);

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 v);

        }

        if (SELECT_PROJECT_DB_STAGE.equals (currentStage))
        {

            Path d = this.projDBFind.getFile ();

            boolean enable = Utils.isProjectDir (d);

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 enable);

            return;

        }

        if (EXISTING_STAGE.equals (currentStage))
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 (this.selectProjectDB.isSelected () || this.findProjects.isSelected ()));

        }

    }

    private boolean doesFileHaveExtension (Path   p,
                                           String ext)
    {

        if (p == null)
        {

            return false;

        }

        return p.getFileName ().toString ().toLowerCase ().endsWith (ext.toLowerCase ());

    }

    public boolean handleFinish ()
    {

        return true;

    }

    private Wizard.Step createNewProjectStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,newproject,title);

        ws.content = this.newProjectPanel;
        return ws;

    }

    private Wizard.Step createSelectProjectDBStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,selectprojectdb,title);

        this.projDBFind = QuollFileField.builder ()
            .chooserTitle (firstusewizard,stages,selectprojectdb,finder,title)
            .limitTo (QuollFileField.Type.directory)
            .initialFile (Environment.getUserQuollWriterDirPath ())
            .build ();

        this.projDBFind.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      true);

        });

        this.projDBFindForm = Form.builder ()
            .description (getUILanguageStringProperty (Arrays.asList (firstusewizard,stages,selectprojectdb,text),
                                        //"Use the finder below to find the directory where your {projects} database file is stored.  The file is called <b>%s%s</b>",
                                                       Environment.getProjectInfoDBPath ().getFileName ().toString () + Constants.H2_DB_FILE_SUFFIX))
            .item (getUILanguageStringProperty (firstusewizard,stages,selectprojectdb,finder,label),
                   this.projDBFind)
            .build ();

        ws.content = this.projDBFindForm;

        return ws;

    }

    private Wizard.Step createExistingProjectDBStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,existing,title);

        this.findProjects = QuollRadioButton.builder ()
            .label (firstusewizard,stages,existing,labels,find)
            .build ();

        this.selectProjectDB = QuollRadioButton.builder ()
            .label (firstusewizard,stages,existing,labels,manual)
            .build ();

        ToggleGroup g = new ToggleGroup ();
        this.findProjects.setToggleGroup (g);
        this.selectProjectDB.setToggleGroup (g);

        Form f = Form.builder ()
            .description (firstusewizard,stages,existing,text)
            .item (this.findProjects)
            .item (this.selectProjectDB)
            .build ();

        this.findProjects.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      true);

        });

        this.selectProjectDB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      true);

        });

        ws.content = f;

        return ws;

    }

    private Wizard.Step createDecideStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,decide,title);

        this.importFile = QuollRadioButton.builder ()
            .label (firstusewizard,stages,decide,labels,importfile)
            .build ();

        this.createNewProject = QuollRadioButton.builder ()
            .label (firstusewizard,stages,decide,labels,newproject)
            .build ();

        ToggleGroup g = new ToggleGroup ();
        this.importFile.setToggleGroup (g);
        this.createNewProject.setToggleGroup (g);

        Form f = Form.builder ()
            .description (firstusewizard,stages,decide,text)
            .item (this.importFile)
            .item (this.createNewProject)
            .build ();

        this.importFile.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      true);

        });

        this.createNewProject.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      true);

        });

        VBox.setVgrow (f,
                       Priority.ALWAYS);

        VBox b = new VBox ();
        b.getChildren ().add (f);

        b.getChildren ().add (QuollHyperlink.builder ()
            .label (firstusewizard,stages,decide,labels,notfirst)
            .styleClassName (StyleClassNames.FIND)
            .onAction (ev ->
            {

                Environment.showAllProjectsViewer ();

                UIUtils.runLater (() ->
                {

                    Environment.getAllProjectsViewer ().runCommand (AllProjectsViewer.CommandId.findprojects);

                });

            })
            .build ());

        ws.content = b;

        return ws;

    }

    private boolean canHandleDragEvent (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if ((files == null)
            ||
            (files.size () == 0)
           )
        {

            return false;

        }

        for (File f : files)
        {

            if (Files.isDirectory (f.toPath ()))
            {

                continue;

            }

            if (f.getName ().toLowerCase ().endsWith (Constants.DOCX_FILE_EXTENSION))
            {

                return true;

            }

        }

        return false;

    }

    private Path getFileFromDragEvent (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if (files == null)
        {

            return null;

        }

        for (File f : files)
        {

            Path p = f.toPath ();

            if ((Files.exists (p))
                &&
                (!Files.isDirectory (p))
               )
            {

                if (p.getFileName ().toString ().toLowerCase ().endsWith (Constants.DOCX_FILE_EXTENSION))
                {

                    return p;

                }

            }

        }

        return null;

    }

    private Wizard.Step createSelectFileToImportStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,selectfile,title);
                    //"Select a file to import";

        this.fileFind = QuollFileField.builder ()
            .chooserTitle (firstusewizard,stages,selectfile,finder,title)
            .limitTo (QuollFileField.Type.file)
            .initialFile (javax.swing.filechooser.FileSystemView.getFileSystemView ().getDefaultDirectory ().toPath ())
            .showClear (true)
            .fileExtensionFilter (getUILanguageStringProperty (firstusewizard,stages,selectfile,finder,label),
                                  Constants.DOCX_FILE_EXTENSION)
            .build ();

        this.fileFind.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setImportFile (newv);

        });

        this.fileFindForm = Form.builder ()
            .description (firstusewizard,stages,selectfile,text)
            .item (this.fileFind)
            .build ();

        this.fileFindForm.setOnDragDropped (ev ->
        {

            if (this.canHandleDragEvent (ev))
            {

                Path p = this.getFileFromDragEvent (ev);

                if (p == null)
                {

                    return;

                }

                this.fileFind.setFile (p);
                ev.consume ();
                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

            }

            ev.consume ();

        });

        this.fileFindForm.setOnDragEntered (ev ->
        {

            ev.consume ();
            //this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

            if (this.canHandleDragEvent (ev))
            {

                Path p = this.getFileFromDragEvent (ev);

                if (p == null)
                {

                    return;

                }

                ev.consume ();
                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

            }

        });

        this.fileFindForm.setOnDragOver (ev ->
        {

            ev.consume ();
            //this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

            if (this.canHandleDragEvent (ev))
            {

                Path p = this.getFileFromDragEvent (ev);

                if (p == null)
                {

                    return;

                }

                ev.consume ();
                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

            }

        });

        ws.content = this.fileFindForm;

        return ws;

    }

    private Wizard.Step createImportStep ()
    {

        Wizard.Step ws = new Wizard.Step ();
        ws.title = getUILanguageStringProperty (firstusewizard,stages,selectitems,title);

        VBox b = new VBox ();
        VBox.setVgrow (this.itemsTree,
                       Priority.ALWAYS);

        b.getChildren ().add (QuollTextView.builder ()
            .text (getUILanguageStringProperty (firstusewizard,stages,selectitems,text))
            .build ());

        ScrollPane sp = new ScrollPane (this.itemsTree);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        b.getChildren ().add (sp);

        ws.content = b;

        return ws;

    }

    private Wizard.Step createSpellCheckLangStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,spellcheck,title);

        ChoiceBox<String> spellcheckLang = new ChoiceBox<> ();

        spellcheckLang.setDisable (true);

        spellcheckLang.setConverter (new StringConverter<String> ()
        {

            @Override
            public String fromString (String s)
            {

                return s;

            }

            @Override
            public String toString (String s)
            {

                return getUILanguageStringProperty (languagenames,s).getValue ();

            }

        });

        Environment.schedule (() ->
        {

            String l = null;

            try
            {

                l = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + UserProperties.get (Constants.QUOLL_WRITER_SUPPORTED_LANGUAGES_URL_PROPERTY_NAME)));

            } catch (Exception e) {

                // Something gone wrong, so just add english.
                l = Constants.ENGLISH;

                Environment.logError ("Unable to get language files url",
                                      e);

            }

            StringTokenizer t = new StringTokenizer (l,
                                                     String.valueOf ('\n'));

            final List<String> langs = new ArrayList<> ();

            while (t.hasMoreTokens ())
            {

                String lang = t.nextToken ().trim ();

                if (lang.equals (""))
                {

                    continue;

                }

                langs.add (lang);

            }

            UIUtils.runLater (() ->
            {

                spellcheckLang.setItems (FXCollections.observableList (langs));
                UILanguageStrings uistrings = UILanguageStringsManager.getCurrentUILanguageStrings ();

                spellcheckLang.getSelectionModel ().select (uistrings.getLanguageName ());
                if (spellcheckLang.getSelectionModel ().getSelectedItem () == null)
                {

                    spellcheckLang.getSelectionModel ().select (0);

                }

                spellcheckLang.setDisable (false);

            });

        },
        1,
        -1);

        StringProperty title = new SimpleStringProperty ();
        title.addListener ((pr, oldv, newv) ->
        {

            // Needed to ensure listeners are notified of changes.
        });

        DownloadPanel langDownload = DownloadPanel.builder ()
            .styleClassName (StyleClassNames.DOWNLOAD)
            .title (title)
            .showStop (true)
            .build ();
        langDownload.managedProperty ().bind (langDownload.visibleProperty ());
        langDownload.setVisible (false);

        Form f = Form.builder ()
            .description (firstusewizard,stages,spellcheck,text)
            .item (spellcheckLang)
            .item (langDownload)
            .build ();

        spellcheckLang.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            if (DictionaryProvider.isLanguageInstalled (newv))
            {

                langDownload.setVisible (false);
                return;

            }

            langDownload.setVisible (true);

            title.unbind ();
            title.bind (getUILanguageStringProperty (Arrays.asList (dictionary,download,notification),
                                                     getUILanguageStringProperty (languagenames,newv)));

            UrlDownloader dl = UIUtils.downloadDictionaryFiles (spellcheckLang.valueProperty ().getValue (),
                                             this.viewer,
                                             // On progress
                                             p ->
                                             {

                                                 langDownload.setProgress (p);

                                             },
                                             // On complete
                                             () ->
                                             {

                                                 title.unbind ();
                                                 title.bind (getUILanguageStringProperty (dictionary,download,complete));
                                                 UserProperties.setDefaultSpellCheckLanguage (spellcheckLang.valueProperty ().getValue ());

                                             },
                                             // On error
                                             ex ->
                                             {

                                                 f.showError (getUILanguageStringProperty (Arrays.asList (dictionary,download,actionerror),
                                                                                           getUILanguageStringProperty (languagenames,spellcheckLang.valueProperty ().getValue ())));

                                             });

            langDownload.setOnStop (() -> dl.stop ());

        });

        ws.content = f;

        return ws;

    }

    private Wizard.Step createStartStep ()
    {

        FirstUseWizard _this = this;

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (firstusewizard,stages,start,title);

        DownloadPanel uilangDownload = DownloadPanel.builder ()
            .styleClassName (StyleClassNames.DOWNLOAD)
            .title (firstusewizard,stages,start,LanguageStrings.downloading)
            .showStop (true)
            .build ();
        uilangDownload.managedProperty ().bind (uilangDownload.visibleProperty ());
        uilangDownload.setVisible (false);

        this.uilangSel = new ComboBox<> ();
        this.uilangSel.setEditable (false);
        this.uilangSel.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            String uid = newv.id;

            try
            {

                if (UILanguageStringsManager.getUILanguageStrings (uid) != null)
                {

                    try
                    {

                        this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                  true);

                        Environment.setUILanguage (uid);

                    } catch (Exception e) {

                        Environment.logError ("Unable to set ui language to: " + uid,
                                              e);

                        this.uilangSelForm.showError (getUILanguageStringProperty (uilanguage,set,actionerror));

                        this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                  false);

                    }

                    return;

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get ui language strings: " + uid,
                                      e);

            }

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      false);
            uilangDownload.setProgress (0);
            uilangDownload.setVisible (true);

            UrlDownloader dl = UILanguageStringsManager.downloadUILanguageFile2 (uid,
                                                              // On progress
                                                              (downloaded, total) ->
                                                              {

                                                                  UIUtils.runLater (() ->
                                                                  {

                                                                      uilangDownload.setProgress ((double) downloaded / (double) total);

                                                                  });

                                                              },
                                                              // On stop
                                                              null,
                                                              // On complete
                                                              () ->
                                                              {

                                                                  UIUtils.runLater (() ->
                                                                  {

                                                                      try
                                                                      {

                                                                          this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                                                                    true);

                                                                          uilangDownload.setVisible (false);
                                                                          Environment.setUILanguage (uid);

                                                                      } catch (Exception e) {

                                                                          Environment.logError ("Unable to set ui language to: " + uid,
                                                                                                e);

                                                                          this.uilangSelForm.showError (getUILanguageStringProperty (uilanguage,set,actionerror));

                                                                      }

                                                                  });

                                                              },
                                                              // On error
                                                              excep ->
                                                              {

                                                                  UIUtils.runLater (() ->
                                                                  {

                                                                      uilangDownload.setVisible (false);
                                                                      this.uilangSelForm.showError (getUILanguageStringProperty (firstusewizard,stages,start,actionerror));

                                                                  });

                                                              });

            uilangDownload.setOnStop (() -> dl.stop ());

        });

        Callback<ListView<UILanguageStringsInfo>, ListCell<UILanguageStringsInfo>> cellFactory = (lv ->
        {

            return new ListCell<UILanguageStringsInfo> ()
            {

                @Override
                protected void updateItem (UILanguageStringsInfo item,
                                           boolean               empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        StringProperty textProp = null;//item.nativeName;

                        if (!UILanguageStrings.isEnglish (item.id))
                        {

                            textProp = getUILanguageStringProperty (Arrays.asList (uilanguage,set,LanguageStrings.item),
                                                                    item.nativeName,
                                                                    item.languageName,
                                                                    Environment.formatNumber (item.percentComplete),
                                                                    item.user ? getUILanguageStringProperty (uilanguage,set,createdbyyou) : "");

                        } else {

                            textProp = new SimpleStringProperty (item.nativeName);

                        }

                        this.textProperty ().bind (textProp);

                    }

                }

            };

        });

        this.uilangSel.setCellFactory (cellFactory);
        this.uilangSel.setButtonCell (cellFactory.call (null));
        this.uilangSel.setDisable (true);

        Environment.schedule (() ->
        {

            Set<UILanguageStringsInfo> _uilangs = Environment.getAvailableUILanguageStrings ();

            Set<UILanguageStringsInfo> uilangs = _uilangs.stream ()
                .sorted ((o1, o2) ->
                {

                    if (UILanguageStrings.isEnglish (o1.id))
                    {

                        return -1 * Integer.MAX_VALUE;

                    }

                    if (UILanguageStrings.isEnglish (o2.id))
                    {

                        return -1 * Integer.MAX_VALUE;

                    }

                    if (o1.nativeName.equals (o2.nativeName))
                    {

                        return Integer.compare (o1.percentComplete, o2.percentComplete);

                    }

                    return o1.nativeName.compareTo (o2.nativeName);

                })
                .collect (Collectors.toSet ());

            UIUtils.runLater (() ->
            {

                uilangSel.getItems ().addAll (uilangs);

                uilangSel.setDisable (false);

                String sel = UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

                uilangs.stream ()
                    .forEach (in ->
                    {

                        if (in.id.equals (sel))
                        {

                            _this.uilangSel.getSelectionModel ().select (in);

                        }

                    });

            });

        },
        0,
        -1);

        this.uilangSelForm = Form.builder ()
            .description (firstusewizard,stages,start,text)
            .item (this.uilangSel)
            .item (uilangDownload)
            .build ();
        ws.content = this.uilangSelForm;

        return ws;

    }

    private void setImportFile (final Path f)
    {

        if (f == null)
        {

            return;

        }

        final FirstUseWizard _this = this;

        try
        {

            Importer.importProject (f.toUri (),
                                    new ImportCallback ()
                                    {

                                        @Override
                                        public void exceptionOccurred (Exception e,
                                                                       URI       uri)
                                        {

                                            Environment.logError ("Unable to import file: " +
                                                                  f,
                                                                  e);

                                            _this.fileFindForm.showError (getUILanguageStringProperty (firstusewizard,stages,importfile,actionerror));

                                        }

                                        @Override
                                        public void projectCreated (final Project p,
                                                                    URI           uri)
                                        {

                                            UIUtils.runLater (() ->
                                            {

                                                _this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                                           true);

                                                if (_this.itemsTree != null)
                                                {

                                                    _this.itemsTree.setRoot (_this.createTree (p));

                                                    _this.itemsTree.expandAll ();

                                                    _this.importProj = p;

                                                    _this.newProjectPanel.setName (p.getName ());

                                                    //_this.newProjectPanel.setProject (p);

                                                }

                                            });

                                        }

                                    });

        } catch (Exception e) {

            Environment.logError ("Unable to import file: " +
                                  f,
                                  e);

            this.fileFindForm.showError (getUILanguageStringProperty (firstusewizard,stages,importfile,actionerror));
                                      //"Unable to import file");

        }

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
                    chi.setIndependent (true);

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

        Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = p.getAssets (t);

            this.addAssetsToTree (root,
                                  t,
                                  as);

        }

        return root;

    }

    private void addAssetsToTree (TreeItem<NamedObject>      root,
                                  UserConfigurableObjectType type,
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
                          NamedObjectSorter.getInstance ());

        for (Asset a : lassets)
        {

            CheckBoxTreeItem<NamedObject> cai = new CheckBoxTreeItem<> (a);
            cai.setIndependent (true);
            ci.getChildren ().add (cai);

            String t = this.getFirstLastSentence (a.getDescriptionText ());

            if (t.length () > 0)
            {

                // Get the first and last sentence.
                cai.getChildren ().add (new TreeItem<NamedObject> (new TreeParentNode ("_string", t)));

            }

        }

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

    private void getSelectedObjects (TreeItem<NamedObject> t,
                                     Set<NamedObject>      objs)
    {

        if (t instanceof CheckBoxTreeItem)
        {

            CheckBoxTreeItem ct = (CheckBoxTreeItem) t;

            if (ct.isSelected ())
            {

                objs.add ((NamedObject) ct.getValue ());

            }

        }

        t.getChildren ().stream ()
            .forEach (c -> this.getSelectedObjects (c,
                                                    objs));

    }

    private Project getSelectedItems ()
    {

        Project p = new Project (this.importProj.getName ());

        Book b = new Book (p,
                           null);

        p.addBook (b);
        b.setName (p.getName ());

        Set<NamedObject> objs = new HashSet<> ();

        this.getSelectedObjects (this.itemsTree.getRoot (),
                                 objs);

        for (NamedObject o : objs)
        {

            if (o instanceof Asset)
            {

                p.addAsset ((Asset) o);

            }

            if (o instanceof Chapter)
            {

                b.addChapter ((Chapter) o);

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

    public Wizard.Step getStep (String stage)
    {

        final FirstUseWizard _this = this;

        if (NEW_PROJECT_STAGE.equals (stage))
        {

            return this.createNewProjectStep ();

        }

        if (SELECT_PROJECT_DB_STAGE.equals (stage))
        {

            return this.createSelectProjectDBStep ();

        }

        if (EXISTING_STAGE.equals (stage))
        {

            return this.createExistingProjectDBStep ();

        }

        if (DECIDE_STAGE.equals (stage))
        {

            return this.createDecideStep ();

        }

        if (SELECT_FILE_STAGE.equals (stage))
        {

            return this.createSelectFileToImportStep ();

        }

        if (IMPORT_STAGE.equals (stage))
        {

            return this.createImportStep ();

        }

        if (SPELL_CHECK_LANG_STAGE.equals (stage))
        {

            return this.createSpellCheckLangStep ();

        }

        if (START_STAGE.equals (stage))
        {

            return this.createStartStep ();

        }

        return null;

    }

    public String getPreviousStepId (String currStage)
    {

        if (SELECT_PROJECT_DB_STAGE.equals (currStage))
        {

            return EXISTING_STAGE;

        }

        if (SPELL_CHECK_LANG_STAGE.equals (currStage))
        {

            return START_STAGE;

        }

        if (START_STAGE.equals (currStage))
        {

            return null;

        }

        if (NEW_PROJECT_STAGE.equals (currStage))
        {

            if (this.importFile.isSelected ())
            {

                return IMPORT_STAGE;

            }

            return DECIDE_STAGE;

        }

        if (EXISTING_STAGE.equals (currStage))
        {

            return START_STAGE;

        }

        if (IMPORT_STAGE.equals (currStage))
        {

            return SELECT_FILE_STAGE;

        }

        if (SELECT_FILE_STAGE.equals (currStage))
        {

            return DECIDE_STAGE;

        }

        if (DECIDE_STAGE.equals (currStage))
        {

            return SPELL_CHECK_LANG_STAGE;

        }

        return null;

    }

    public String getNextStepId (String currStage)
    {

        if (START_STAGE.equals (currStage))
        {

            return SPELL_CHECK_LANG_STAGE;

        }

        if (SPELL_CHECK_LANG_STAGE.equals (currStage))
        {

            return DECIDE_STAGE;

        }

        if (SELECT_FILE_STAGE.equals (currStage))
        {

            return IMPORT_STAGE;

        }

        if (EXISTING_STAGE.equals (currStage))
        {

            return SELECT_PROJECT_DB_STAGE;

        }

        if (IMPORT_STAGE.equals (currStage))
        {

            return NEW_PROJECT_STAGE;

        }

        if (DECIDE_STAGE.equals (currStage))
        {

            if (this.createNewProject.isSelected ())
            {

                return NEW_PROJECT_STAGE;

            }

            if (this.importFile.isSelected ())
            {

                return SELECT_FILE_STAGE;

            }

            return IMPORT_STAGE;

        }

        return null;

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (firstusewizard,title)
            .styleClassName (StyleClassNames.FIRSTUSEWIZARD)
            .styleSheet (StyleClassNames.FIRSTUSEWIZARD)
            .headerIconClassName (StyleClassNames.QW)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
