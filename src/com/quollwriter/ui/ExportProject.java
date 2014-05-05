package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;


public class ExportProject extends PopupWizard
{

    private static Map<String, String> fileTypes = new LinkedHashMap ();

    private static Map<String, Class> handlers = new HashMap ();

    static
    {

        Map m = ExportProject.handlers;

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
/*
        m.put (Constants.PDF_FILE_EXTENSION,
               PDFDocumentExporter.class);
        m.put (Constants.TXT_FILE_EXTENSION,
               TextDocumentExporter.class);
*/

        m = ExportProject.fileTypes;

        m.put ("Microsoft Word 2007 (.docx)",
               Constants.DOCX_FILE_EXTENSION);
        m.put ("EPUB (.epub)",
               Constants.EPUB_FILE_EXTENSION);
/*
        m.put ("PDF (.pdf)",
               Constants.PDF_FILE_EXTENSION);
               */
        m.put ("HTML (.html)",
               Constants.HTML_FILE_EXTENSION);
        /*
        m.put ("PDF",
               "pdf");
        m.put ("Web page (.html)",
               "html");
        m.put ("Plain text (.txt)",
               "txt");
         */
    }

    private JComboBox                     exportOthersType = null;
    private JComboBox                     exportChaptersType = null;
    private JComboBox                     fileType = null;
    private JTextField                    fileField = null;
    private JTree                         itemsTree = null;
    private JScrollPane                   itemsTreeScroll = null;
    private Project                       proj = null;
    private Map<String, DocumentExporter> exporters = new HashMap ();

    public ExportProject(ProjectViewer pv)
    {

        super (pv);

        this.proj = pv.getProject ();

    }

    public String getWindowTitle ()
    {

        return "Export " + Environment.getObjectTypeName (Project.OBJECT_TYPE);

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return Project.OBJECT_TYPE + "-export";

    }

    public String getHelpText ()
    {

        return "This wizard allows you to export the information in the {project}.";

    }

    public boolean handleFinish ()
    {

        if (this.fileField.getText ().trim ().equals (""))
        {

            UIUtils.showMessage (this,
                                 "Please select a directory.");

            return false;

        }

        File dir = new File (this.fileField.getText ());

        try
        {

            DocumentExporter de = this.getExporter ();

            dir.mkdirs ();

            de.exportProject (dir);

            this.projectViewer.createActionLogEntry (this.proj,
                                                     "Exported project to directory: " +
                                                     this.fileField.getText ());

            this.projectViewer.fireProjectEvent (ProjectEvent.EXPORT,
                                                 ProjectEvent.ANY);

        } catch (Exception e)
        {

            Environment.logError ("Unable to export project: " +
                                  this.proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to export project.");

            return false;

        }

        UIUtils.showMessage (this,
                             Environment.getObjectTypeName (Project.OBJECT_TYPE) + this.proj.getName () + " has been exported to:\n\n   " + this.fileField.getText ());

        try
        {

            Desktop.getDesktop ().open (dir);

        } catch (Exception e)
        {

        }

        return true;

    }

    public void handleCancel ()
    {

    }

    private String getFileType ()
    {

        return ExportProject.fileTypes.get ((String) this.fileType.getSelectedItem ());

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

        Class c = ExportProject.handlers.get (fileType);

        if (c == null)
        {

            throw new GeneralException ("Unable to find export handler for extension: " +
                                        fileType);

        }

        try
        {

            de = (DocumentExporter) c.newInstance ();
            de.setProject (this.proj);

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

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "where-to-save";

        }

        // Create a new exporter.
        DocumentExporter de = null;

        try
        {

            de = this.getExporter ();

        } catch (Exception e)
        {

            UIUtils.showErrorMessage (this,
                                      "Unable to go to next page, please contact support for assistance.");

            Environment.logError ("Unable to get exporter",
                                  e);

            return null;

        }

        String stage = null;

        if (currStage.equals ("where-to-save"))
        {

            // Save the directory selected by the user.
            try
            {

                this.proj.setProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME,
                                       this.fileField.getText ());

            } catch (Exception e)
            {

                // No need to bother the user with this.
                Environment.logError ("Unable to save property: " +
                                      Constants.EXPORT_DIRECTORY_PROPERTY_NAME +
                                      " with value: " +
                                      this.fileField.getText (),
                                      e);

            }

            stage = de.getStartStage ();

        } else
        {

            String ft = this.getFileType ();

            if (currStage.startsWith (ft))
            {

                currStage = currStage.substring (ft.length () + 1);

            }

            stage = de.getNextStage (currStage);

        }

        if (stage == null)
        {

            return null;

        }

        return this.getFileType () + ":" + stage;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("where-to-save"))
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

            UIUtils.showErrorMessage (this,
                                      "Unable to go to previous page, please contact support for assistance.");

            Environment.logError ("Unable to get exporter",
                                  e);

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

            return "where-to-save";

        }

        return this.getFileType () + ":" + stage;

    }

    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {

        return true;

    }

    public int getMaximumContentHeight ()
    {

        return 200;

    }

    public String getStartStage ()
    {

        return "where-to-save";

    }

    public WizardStep getStage (String stage)
    {

        final ExportProject _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("where-to-save"))
        {

            ws.title = "Select the file type and the directory to save to";

            FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 2px, p, 10px",
                                            "p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            if (this.fileField == null)
            {

                this.fileField = UIUtils.createTextField ();

                String def = this.proj.getProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME);

                if (def == null)
                {

                    def = FileSystemView.getFileSystemView ().getDefaultDirectory ().toString ();

                }

                this.fileField.setText (def);

            }

            builder.addLabel ("Directory",
                              cc.xy (2,
                                     1));
            builder.add (this.fileField,
                         cc.xy (4,
                                1));

            JButton findBut = new JButton (Environment.getIcon ("find",
                                                                Constants.ICON_MENU));

            findBut.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        String ff = _this.fileField.getText ();

                        JFileChooser f = new JFileChooser ();
                        f.setDialogTitle ("Select a directory to export to");
                        f.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
                        f.setApproveButtonText ("Select");
                        // f.setFileFilter (new FileNameExtensionFilter ("Supported Files (docx, doc)", "docx", "doc"));

                        if (ff != null)
                        {

                            f.setCurrentDirectory (new File (ff));

                        }

                        // Need to run: attrib -r "%USERPROFILE%\My Documents" on XP to allow a new directory
                        // to be created in My Documents.

                        if (f.showOpenDialog (_this) == JFileChooser.APPROVE_OPTION)
                        {

                            _this.fileField.setText (f.getSelectedFile ().getPath ());

                            _this.enableButton ("next",
                                                true);

                        }

                    }

                });

            builder.add (findBut,
                         cc.xy (6,
                                1));

            Vector fileTypes = new Vector (ExportProject.fileTypes.keySet ());

            this.fileType = new JComboBox (fileTypes);
            this.fileType.setOpaque (false);
            this.fileType.setMaximumSize (this.fileType.getPreferredSize ());

            builder.addLabel ("File Type",
                              cc.xy (2,
                                     3));

            Box bb = new Box (BoxLayout.X_AXIS);

            bb.add (this.fileType);
            bb.add (Box.createHorizontalGlue ());

            builder.add (bb,
                         cc.xywh (4,
                                  3,
                                  3,
                                  1));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            ws.panel = p;

            return ws;

        }

        DocumentExporter de = null;

        try
        {

            de = this.getExporter ();

        } catch (Exception e)
        {

            UIUtils.showErrorMessage (this,
                                      "Unable to view, please contact support for assistance.");

            Environment.logError ("Unable to get exporter",
                                  e);

            return null;

        }

        String ft = this.getFileType ();

        if (stage.startsWith (ft))
        {

            stage = stage.substring (ft.length () + 1);

        }

        return de.getStage (stage);

    }

}
