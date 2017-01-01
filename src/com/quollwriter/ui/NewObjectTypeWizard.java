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


public class NewObjectTypeWizard extends Wizard<ProjectViewer>
{

    private JTextField  name = null;
    private JTextField pluralName = null;

    private FileFinder      iconFind = null;
    private JLabel          error = null;
    
    public NewObjectTypeWizard (ProjectViewer pv)
    {

        super (pv);

    }

    public String getFirstHelpText ()
    {

        return "This wizard allows you to add a new type of object.";

    }

    public boolean handleFinish ()
    {

        return true;

    }

    public void handleCancel ()
    {

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "name";

        }

        String stage = null;

        if (currStage.equals ("name"))
        {

        }

        if (stage == null)
        {

            return null;

        }

        return null;

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
        
        return null;

    }
    
    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {
            
        this.resize ();
    
        return true;

    }

    public String getStartStage ()
    {

        return "name";

    }

    public WizardStep getStage (String stage)
    {

        WizardStep ws = new WizardStep ();

        if (stage.equals ("name"))
        {

            ws.title = "Select the file type and directory to save to";
/*
            FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 10px",
                                            "p, 6px, p");

            Box b = new Box (BoxLayout.Y_AXIS);
            
            this.fileFindError = UIUtils.createErrorLabel ("Please select a directory.");
            this.fileFindError.setVisible (false);
            this.fileFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            b.add (this.fileFindError);
                                            
            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.fileFind = new FileFinder ();
        
            this.fileFind.setOnSelectHandler (new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
            
                    _this.checkSelectedFile ();
                    
                }
                
            });
                        
            builder.addLabel ("Directory",
                              cc.xy (2,
                                     1));
            builder.add (this.fileFind, 
                         cc.xy (4,
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
                                  2,
                                  1));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            b.add (p);
            
            ws.panel = b;
            */
            return ws;

        }

        return null;

    }

}
