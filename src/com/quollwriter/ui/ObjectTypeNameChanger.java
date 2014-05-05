package com.quollwriter.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;

public class ObjectTypeNameChanger extends PopupWindow
{

    private Map<String, JTextField> singular = new HashMap ();
    private Map<String, JTextField> plural = new HashMap ();
    
    public ObjectTypeNameChanger (AbstractProjectViewer pv)
    {

        super (pv,
               Component.CENTER_ALIGNMENT);
                
    }
    
    public String getWindowTitle ()
    {

        return "Edit Object Names";

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return "config";

    }

    public void init ()
    {
        
        super.init ();
                
    }
    
    public String getHelpText ()
    {

        return Environment.replaceObjectNames ("After saving any changes to names will appear when you next open the {project}.");

    }

    private void save ()
    {
        
        // See if any of the values are changing, if so we need to reopen the project.
        
        final Map<String, String> sing = new HashMap ();
        final Map<String, String> plur = new HashMap ();
        
        for (String ot : this.singular.keySet ())
        {
            
            JTextField f = this.singular.get (ot);
            
            sing.put (ot,
                      f.getText ().trim ());
                        
        }

        for (String ot : this.plural.keySet ())
        {
            
            JTextField f = this.plural.get (ot);
            
            plur.put (ot,
                      f.getText ().trim ());
                        
        }
        
        boolean changing = false;
        
        if (!Environment.getObjectTypeNames ().values ().containsAll (sing.values ()))
        {
            
            changing = true;
            
        }
        
        if (!Environment.getObjectTypeNamePlurals ().values ().containsAll (plur.values ()))
        {
            
            changing = true;
            
        }

        if (changing)
        {

            final Project proj = this.projectViewer.getProject ();
            final ObjectTypeNameChanger _this = this;
            
            // Offer to reopen project.
            UIUtils.createQuestionPopup (this,
                                         "Confirm name changes?",
                                         Constants.EDIT_ICON_NAME,
                                         "Warning!  To change the object names the {project} must first be saved then reopened.<br /><br />Do you wish to continue?",
                                         "Yes, change the names",
                                         null,
                                         new ActionListener ()
                                         {
                                
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                try
                                                {
                                                
                                                    Environment.setUserObjectTypeNames (sing,
                                                                                        plur);
                                
                                                } catch (Exception e) {
                                                    
                                                    UIUtils.showErrorMessage (_this,
                                                                              "Unable to modify names");
                                                    
                                                    Environment.logError ("Unable to modify names",
                                                                          e);
                                                    
                                                    return;
                                                                                        
                                                }
                                
                                                _this.close ();
                                                
                                                _this.projectViewer.close (true,
                                                                          new ActionListener ()
                                                                          {
                                                                            
                                                                              public void actionPerformed (ActionEvent ev)
                                                                              {
                                                                                                                                
                                                                                    // Open the project again.
                                                                                    try
                                                                                    {
                                                                        
                                                                                        Environment.openProject (proj);
                                                                        
                                                                                    } catch (Exception e)
                                                                                    {
                                                                        
                                                                                        Environment.logError ("Unable to reopen project: " +
                                                                                                              proj,
                                                                                                              e);

                                                                                        UIUtils.showErrorMessage (_this,
                                                                                                                  "Unable to reopen {project}.  Please contact Quoll Writer support for assistance.");
                                                                                        
                                                                                    }
                                
                                                                              }
                                                                            
                                                                          });
                                                
                                            }
                                            
                                         },
                                         null,
                                         null);
            
            return;
            
        }
        
        this.close ();
                
    }

    private void reset ()
    {
        
        final ObjectTypeNameChanger _this = this;
        final Project proj = this.projectViewer.getProject ();

        // Offer to reopen project.
        UIUtils.createQuestionPopup (this,
                                     "Confirm name changes?",
                                     Constants.EDIT_ICON_NAME,
                                     "Warning!  To reset the object names back to the defaults the {project} must first be saved then reopened.<br /><br />Do you wish to continue?",
                                     "Yes, reset the names",
                                     null,
                                     new ActionListener ()
                                     {
                                        
                                        public void actionPerformed (ActionEvent ev)
                                        {
                                                            
                                            try
                                            {
                                            
                                                Environment.resetObjectTypeNamesToDefaults ();
                                
                                            } catch (Exception e) {
                                                
                                                UIUtils.showErrorMessage (_this,
                                                                          "Unable to modify names");
                                                
                                                Environment.logError ("Unable to modify names",
                                                                      e);
                                                
                                                return;
                                                                                    
                                            }
                                
                                            _this.close ();

                                            _this.projectViewer.close (true,
                                                                       new ActionListener ()
                                                                       {
                                                                        
                                                                          public void actionPerformed (ActionEvent ev)
                                                                          {
                                                                                                                            
                                                                                // Open the project again.
                                                                                try
                                                                                {
                                                                    
                                                                                    Environment.openProject (proj);
                                                                    
                                                                                } catch (Exception e)
                                                                                {
                                                                    
                                                                                    Environment.logError ("Unable to reopen project: " +
                                                                                                          proj,
                                                                                                          e);
                                                                                    
                                                                                    UIUtils.showErrorMessage (null,
                                                                                                              "Unable to reopen project.  Please contact Quoll Writer support for assistance.");
                                                                                    
                                                                                }
                                
                                                                          }
                                                                        
                                                                       });
                                            
                                        }
                                     },
                                     null,
                                     null);
                            
    }
    
    private boolean addRow (final String       objType,
                            PanelBuilder builder,
                            int          row)
    {

        final ObjectTypeNameChanger _this = this;
    
        CellConstraints cc = new CellConstraints ();
        
        ImageIcon ii = Environment.getIcon (objType,
                                            Constants.ICON_MENU);
        
        if (ii == null)
        {
            
            return false;
            
        }
        
        builder.add (new ImagePanel (ii,
                                     null),
                     cc.xy (1,
                            row));
        
        final JTextField s = new JTextField (Environment.getObjectTypeName (objType)); 
        final JTextField p = new JTextField (Environment.getObjectTypeNamePlural (objType));
        
        this.singular.put (objType,
                           s);
                
        this.plural.put (objType,
                         p);

        builder.add (s,
                     cc.xy (3,
                            row));
        builder.add (p,
                     cc.xy (5,
                            row));        
        
        return true;
        
    }
    
    public JComponent getContentPanel ()
    {

        List<String> objTypes = new ArrayList ();
        objTypes.add (Chapter.OBJECT_TYPE);
        objTypes.add (QCharacter.OBJECT_TYPE);
        objTypes.add (Location.OBJECT_TYPE);
        objTypes.add (QObject.OBJECT_TYPE);
        objTypes.add (ResearchItem.OBJECT_TYPE);
        objTypes.add (OutlineItem.OBJECT_TYPE);
        objTypes.add (Scene.OBJECT_TYPE);
        objTypes.add (Note.OBJECT_TYPE);
        objTypes.add (Project.OBJECT_TYPE);
        objTypes.add (Warmup.OBJECT_TYPE);
        
        StringBuilder rows = new StringBuilder ("p, 5px");
        
        for (String ot : objTypes)
        {
            
            if (Environment.getIcon (ot,
                                     Constants.ICON_MENU) != null)
            {
            
                rows.append (", center:p, 10px");
                
            } 
            
        }

        rows.append (",p");
        
        FormLayout fl = new FormLayout ("p, 6px, 180px:grow, 20px, 180px:grow",
                                        rows.toString ());

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;
        
        builder.addLabel ("Singular",
                     cc.xy (3,
                            row));
        builder.addLabel ("Plural",
                     cc.xy (5,
                            row));

        row += 2;
        
        for (String ot : objTypes)
        {
            
            this.addRow (ot,
                         builder,
                         row);
            
            row += 2;
            
        }
/*
        this.thisProjectOnly = new JCheckBox (Environment.replaceObjectNames ("Changes apply to this {project} only."));
        this.thisProjectOnly.setOpaque (false);
        
        builder.add (this.thisProjectOnly,
                     cc.xywh (4,
                              row,
                              3,
                              1));
  */      
        JPanel p = builder.getPanel ();
        p.setBorder (null);
            
        return p;
    
    }

    public JButton[] getButtons ()
    {

        final ObjectTypeNameChanger _this = this;

        JButton save = new JButton ("Save");

        save.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.save ();
            
            }

        });

        JButton cancel = new JButton ("Cancel");

        cancel.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.close ();

            }

        });

        JButton reset = new JButton ("Reset to defaults");

        reset.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.reset ();
            
            }

        });
            
        JButton[] buts = new JButton[3];
        buts[0] = save;
        buts[1] = reset;
        buts[2] = cancel;
        
        return buts;

    }
    
}