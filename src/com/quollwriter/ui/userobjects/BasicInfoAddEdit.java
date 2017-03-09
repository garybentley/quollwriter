package com.quollwriter.ui.userobjects;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;

public class BasicInfoAddEdit extends Box implements ProjectEventListener
{
    
    private JPanel basicInfoPanel = null;
    private Box basicInfoView = null;
    private Box basicInfoEdit = null;
    private Box basicInfoDetails = null;
    private ProjectViewer viewer = null;
    private JTextPane helpText = null;
    private Box helpTextBox = null;
    private UserConfigurableObjectType type = null;
    private Form editForm = null;
    
    public BasicInfoAddEdit (ProjectViewer              viewer,
                             UserConfigurableObjectType type)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.viewer = viewer;
        this.type = type;
        
        Environment.addUserProjectEventListener (this);            
        
        final BasicInfoAddEdit _this = this;
        
        // Create the basic info panel.
        this.basicInfoPanel = new JPanel ();
        
        this.add (this.basicInfoPanel);
        
        this.basicInfoPanel.setLayout (new CardLayout ());
        this.basicInfoPanel.setOpaque (false);
        this.basicInfoPanel.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.basicInfoView = new Box (BoxLayout.Y_AXIS);

        this.basicInfoEdit = new Box (BoxLayout.Y_AXIS);

        this.basicInfoPanel.add (this.basicInfoEdit,
                                 "edit");

        this.basicInfoPanel.add (this.basicInfoView,
                                 "view");

        this.helpTextBox = new Box (BoxLayout.Y_AXIS);

        this.helpText = UIUtils.createHelpTextPane ("",
                                                    viewer);

        this.helpTextBox.add (this.helpText);
        this.helpTextBox.setBorder (UIUtils.createPadding (5, 5, 0, 0));
        this.helpTextBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.basicInfoView.add (this.helpTextBox);

        this.helpTextBox.setVisible (false);

        Box controls = new Box (BoxLayout.X_AXIS);
        controls.setAlignmentX (Component.LEFT_ALIGNMENT);
        controls.setBorder (UIUtils.createBottomLineWithPadding (3, 3, 3, 3));

        List<JButton> buts = new ArrayList ();

        buts.add (UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                        Constants.ICON_MENU,
                                        "Click to edit the information",
                                        new ActionListener ()
                                        {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.showEdit (new ActionListener ()
                                                {
                                        
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                        
                                                        try
                                                        {
                                        
                                                            Environment.updateUserConfigurableObjectType (_this.type);
                                        
                                                        } catch (Exception e) {
                                        
                                                            Environment.logError ("Unable to update user object type: " +
                                                                                  _this.type,
                                                                                  e);
                                        
                                                            UIUtils.showErrorMessage (_this.viewer,
                                                                                      "Unable to update.");
                                        
                                                            return;
                                        
                                                        }
                                        
                                                        // Update the usage of the field.
                                        
                                                        ((CardLayout) _this.basicInfoPanel.getLayout ()).show (_this.basicInfoPanel,
                                                                                                               "view");
                                        
                                                    }
                                        
                                                },
                                                // On cancel
                                                new ActionListener ()
                                                {
                                        
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        ((CardLayout) _this.basicInfoPanel.getLayout ()).show (_this.basicInfoPanel,
                                                                                                               "view");
                                        
                                                    }
                                                                                                               
                                                },
                                                new ActionListener ()
                                                {
                                                  
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        UIUtils.resizeParent (_this);
                                                        
                                                    }
                                                    
                                                },
                                                // Show the buttons.
                                                true);

                                            }

                                        }));

        controls.add (UIUtils.createButtonBar (buts));

        controls.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                controls.getPreferredSize ().height));

        this.basicInfoView.add (controls);

        this.basicInfoDetails = new Box (BoxLayout.Y_AXIS);

        this.basicInfoView.add (this.basicInfoDetails);

        this.basicInfoView.add (Box.createVerticalGlue ());
        
        
    }
    
    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        if (ev.getType ().equals (ProjectEvent.USER_OBJECT_TYPE))
        {

            if (ev.getSource ().equals (this.type))
            {

                this.refresh ();

            }

        }

    }        
    
    public void setHelpText (String t)
    {
        
        if (t == null)
        {
            
            this.helpTextBox.setVisible (false);
            
            return;
            
        }
        
        this.helpText.setText (String.format ("Use the button below to change the general information for your %s such as the name and what icons to use.",
                                              this.type.getObjectTypeNamePlural ().toLowerCase ()));
        
        this.helpTextBox.setVisible (true);            
        
    }
    
    public void refresh ()
    {
        
        this.basicInfoDetails.removeAll ();
    
        Set<FormItem> items = new LinkedHashSet ();

        items.add (new AnyFormItem ("Name",
                                    UIUtils.createInformationLabel (this.type.getObjectTypeName ())));

        items.add (new AnyFormItem ("Plural",
                                    UIUtils.createInformationLabel (this.type.getObjectTypeNamePlural ())));

        items.add (new AnyFormItem ("Header Icon (24px by 24px)",
                                    new JLabel (this.type.getIcon24x24 ())));

        items.add (new AnyFormItem ("Sidebar/Popup Icon (16px by 16px)",
                                    new JLabel (this.type.getIcon16x16 ())));

        final Form f = new Form (Form.Layout.stacked,
                                 items,
                                 null);

        f.setPreferredSize (new Dimension (450,
                                           400));

        f.setBorder (UIUtils.createPadding (5, 5, 0, 0));

        this.basicInfoDetails.add (f);

        ((CardLayout) this.basicInfoPanel.getLayout ()).show (this.basicInfoPanel,
                                                              "view");
            
    }
    
    private Form createEditForm (final ActionListener onSave,
                                 final ActionListener onCancel,
                                 final ActionListener onError,
                                 final boolean        showButtons)
    {

        final BasicInfoAddEdit _this = this;

        Set<FormItem> items = new LinkedHashSet ();

        final TextFormItem name = new TextFormItem ("Name",
                                                    this.type.getObjectTypeName ());

        items.add (name);

        final TextFormItem plname = new TextFormItem ("Plural",
                                                      this.type.getObjectTypeNamePlural ());

        items.add (plname);

        final int headerIconWidth = Environment.getIconPixelWidthForType (Constants.ICON_TITLE);

        final ImageSelectorFormItem headerIcon = new ImageSelectorFormItem (String.format ("Header Icon (%spx by %spx)",
                                                                                           headerIconWidth,
                                                                                           headerIconWidth),
                                                                            UIUtils.imageFileFilter,
                                                                            UIUtils.iconToImage (this.type.getIcon24x24 ()),
                                                                            new Dimension (headerIconWidth, headerIconWidth),
                                                                            true,
                                                                            null);

        headerIcon.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                // If the image size isn't 24/24 then show a warning.
                BufferedImage im = headerIcon.getImage ();

                if (im.getWidth () != headerIconWidth)
                {

                    UIUtils.showMessage (_this.viewer,
                                         "Warning!",
                                         String.format ("Note: the image you have selected will be resized to: %spx by %spx when saved.",
                                                        headerIconWidth,
                                                        headerIconWidth),
                                         Constants.CONFIRM_BUTTON_LABEL_ID,
                                         null,
                                         SwingUtilities.convertPoint (headerIcon.getComponent (),
                                                                                               5,
                                                                                               5,
                                                                                               _this.viewer));

                }

            }

        });

        items.add (headerIcon);

        final int sidebarIconWidth = Environment.getIconPixelWidthForType (-1);

        final ImageSelectorFormItem sidebarIcon = new ImageSelectorFormItem (String.format ("Sidebar/Popup Icon (%spx by %spx)",
                                                                                            sidebarIconWidth,
                                                                                            sidebarIconWidth),
                                                                             UIUtils.imageFileFilter,
                                                                             UIUtils.iconToImage (this.type.getIcon16x16 ()),
                                                                             new Dimension (sidebarIconWidth, sidebarIconWidth),
                                                                             true,
                                                                             null);

        sidebarIcon.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                // If the image size isn't 16/16 then show a warning.
                // If the image size isn't 24/24 then show a warning.
                BufferedImage im = sidebarIcon.getImage ();

                if (im.getWidth () != sidebarIconWidth)
                {

                    UIUtils.showMessage (_this.viewer,
                                         "Warning!",
                                         String.format ("Note: the image you have selected will be resized to: %spx by %spx when saved.",
                                                        sidebarIconWidth,
                                                        sidebarIconWidth),
                                         Constants.CONFIRM_BUTTON_LABEL_ID,
                                         null,
                                         SwingUtilities.convertPoint (headerIcon.getComponent (),
                                                                                               5,
                                                                                               5,
                                                                                               _this.viewer));

                }

            }

        });

        items.add (sidebarIcon);
        
        Map<Form.Button, ActionListener> buttons = new LinkedHashMap ();

        if (showButtons)
        {
        
            buttons.put (Form.Button.save,
                         new ActionListener ()
                         {
    
                            @Override
                            public void actionPerformed (ActionEvent ev)
                            {
    
                                Form f = (Form) ev.getSource ();
    
                                if (f.checkForm ())
                                {
                                    
                                    if (onSave != null)
                                    {
                        
                                        onSave.actionPerformed (new ActionEvent (_this.type,
                                                                                 1,
                                                                                 "save"));
                        
                                    }
                                    
                                }

                            }
    
                         });
    
            buttons.put (Form.Button.cancel,
                         new ActionListener ()
                         {
    
                            @Override
                            public void actionPerformed (ActionEvent ev)
                            {
    
                                if (onCancel != null)
                                {
                                    
                                    onCancel.actionPerformed (new ActionEvent (_this,
                                                                               1,
                                                                               "cancel"));
                                    
                                }
                            
                            }
    
                         });

        }
                         
        final Form f = new Form (Form.Layout.stacked,
                                 items,
                                 buttons)
        {
          
            @Override
            public boolean checkForm ()
            {

                if (!super.checkForm ())
                {
                    
                    return false;
                    
                }

                if (name.getText () == null)
                {
    
                    this.showError ("Name must be provided.");
                            
                    return false;
    
                }

                if (plname.getText () == null)
                {
    
                    this.showError ("Plural name must be provided.");
    
                    return false;
    
                }
    
                if (sidebarIcon.getImage () == null)
                {
    
                    this.showError ("Sidebar/Popup icon must be provided.");
    
                    return false;
    
                }
    
                if (sidebarIcon.getImage () == null)
                {
    
                    this.showError ("Header icon must be provided.");
    
                    return false;
    
                }
    
                // Check that the name(s) aren't already in use.
                Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (false);
                
                for (UserConfigurableObjectType t : types)
                {
                    
                    if (t.equals (_this.type))
                    {
                        
                        continue;
                        
                    }
                    
                    if (t.getObjectTypeName ().equalsIgnoreCase (name.getText ()))
                    {
                        
                        this.showError ("You already have a type called: " + name.getText ());
                        
                        return false;
                        
                    }
                    
                    if (t.getObjectTypeNamePlural ().equalsIgnoreCase (plname.getText ()))
                    {
                        
                        this.showError ("You already have a type called: " + plname.getText ());
                        
                        return false;
                        
                    }

                }
                
                _this.type.setObjectTypeName (name.getText ());
                _this.type.setObjectTypeNamePlural (plname.getText ());
    
                BufferedImage him = UIUtils.getScaledImage (headerIcon.getImage (),
                                                            headerIconWidth,
                                                            headerIconWidth);
    
                _this.type.setIcon24x24 (new ImageIcon (him));
    
                BufferedImage sim = UIUtils.getScaledImage (sidebarIcon.getImage (),
                                                            sidebarIconWidth,
                                                            sidebarIconWidth);
    
                // Get the small.
                _this.type.setIcon16x16 (new ImageIcon (sim));                
                
                return true;
                
            }
            
        };

        f.setOnError (onError);
                                                                        
        return f;

    }

    public boolean checkForm ()
    {
        
        if (this.editForm == null)
        {
            
            return true;
            
        }
        
        return this.editForm.checkForm ();
        
    }

    public Form showEdit (ActionListener onSave,
                          ActionListener onCancel,
                          ActionListener onError,
                          boolean        showButtons)
    {

        final BasicInfoAddEdit _this = this;

        Form f = this.createEditForm (onSave,
                                      onCancel,
                                      onError,
                                      showButtons);

        this.editForm = f;

        f.setPreferredSize (new Dimension (450,
                                           400));

        f.setBorder (UIUtils.createPadding (5, 5, 0, 0));

        this.basicInfoEdit.removeAll ();

        this.basicInfoEdit.add (f);

        ((CardLayout) this.basicInfoPanel.getLayout ()).show (this.basicInfoPanel,
                                                              "edit");

        this.validate ();
        this.repaint ();

        return f;

    }
    
}
