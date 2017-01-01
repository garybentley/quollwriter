package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.*;
import java.math.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import com.toedter.calendar.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.DnDTabbedPane;
import com.quollwriter.ui.components.ScrollableBox;

import com.quollwriter.ui.actionHandlers.*;
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;

public class UserConfigurableObjectTypeEdit extends Box 
{

    private ProjectViewer viewer = null;
    private String objType = null;
    private JPanel fieldsPanel = null;
    private Box editPanel = null;
    private Box fieldsList = null;
    private Box fieldsView = null;
    private JList fields = null;
    private Map<UserConfigurableObjectTypeField, FieldViewBox> viewItems = new LinkedHashMap ();
    private Map<UserConfigurableObjectTypeField.Type, Set<FormItem>> formEditFields = new HashMap ();
    private Map<UserConfigurableObjectTypeField.Type, UserConfigurableObjectTypeField> formTypeFields = new HashMap ();
    private UserConfigurableObjectTypeField currentEditItem = null;
    private DnDTabbedPane tabs = null;
    private JPanel basicInfoPanel = null;
    private Box basicInfoView = null;
    private UserConfigurableObjectType userObjType = null;

    public UserConfigurableObjectTypeEdit (ProjectViewer pv,
                                           String        objType)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = pv;
        this.objType = objType;
        
        this.userObjType = pv.getUserConfigurableObjectType (this.objType);
    
    }
    
    public void init ()
    {
                
        final UserConfigurableObjectTypeEdit _this = this;
                        
        this.tabs = new DnDTabbedPane ();
        // Load the "rules to ignore".

        this.tabs.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);

        // Create the basic info panel.
        this.basicInfoPanel = new JPanel ();
        this.basicInfoPanel.setLayout (new CardLayout ());
        this.basicInfoPanel.setOpaque (false);
        this.basicInfoPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.basicInfoView = new Box (BoxLayout.Y_AXIS);

        this.basicInfoPanel.add (this.basicInfoView,
                                 "view");
        
        // Name
        // Plural name
        // Icon - sizes?
        // Preview format
        // Allow quick add
        
        this.tabs.add ("General",
                       this.basicInfoPanel);
                
        this.fieldsPanel = new JPanel ();
        this.fieldsPanel.setLayout (new CardLayout ());
        this.fieldsPanel.setOpaque (false);
        this.fieldsPanel.setAlignmentX (Component.LEFT_ALIGNMENT);        
        this.fieldsPanel.setPreferredSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                          400));
    
        this.fieldsList = new Box (BoxLayout.Y_AXIS);
        this.fieldsList.setAlignmentX (Component.LEFT_ALIGNMENT);        
        
        this.fieldsView = new Box (BoxLayout.Y_AXIS);
        this.fieldsView.setAlignmentX (Component.LEFT_ALIGNMENT);        
                
        JTextPane m = UIUtils.createHelpTextPane (String.format ("Use this button below to add a new field for this {%s}.  Drag-n-drop the fields to change the order of them.  Use the layout tab above to change how the fields are laid out and displayed.",
                                                                 this.objType),
                                                  viewer);

        this.fieldsView.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                this.fieldsView.getPreferredSize ().height));
        this.fieldsView.setBorder (null);
        
        Box hb = new Box (BoxLayout.Y_AXIS);
        hb.add (m);
        hb.setBorder (UIUtils.createPadding (5, 5, 0, 0));
        hb.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.fieldsView.add (hb);
        
        this.fieldsView.add (Box.createVerticalStrut (0));        
                
        this.fieldsPanel.add (this.fieldsView,
                              "view");
        
        Set<UserConfigurableObjectTypeField> fields = this.userObjType.getConfigurableFields ();
        
        fields = new LinkedHashSet ();
        
        UserConfigurableObjectTypeField f = new TextUserConfigurableObjectTypeField ();
        f.setFormName ("Name");

        fields.add (f);
        
        f = new MultiTextUserConfigurableObjectTypeField ();
        f.setFormName ("Description");

        fields.add (f);

        this.initFields (fields);
        
        this.editPanel = new Box (BoxLayout.Y_AXIS);
        
        this.fieldsPanel.add (this.editPanel,
                              "edit");
        
        Box controls = new Box (BoxLayout.X_AXIS);
        controls.setAlignmentX (Component.LEFT_ALIGNMENT);
        controls.setBorder (UIUtils.createBottomLineWithPadding (3, 3, 3, 3));
        
        List<JButton> buts = new ArrayList ();

        buts.add (UIUtils.createButton (Constants.ADD_ICON_NAME,
                                        Constants.ICON_MENU,
                                        "Click to add a new field",
                                        new ActionAdapter ()
                                        {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.showAdd ();
                                            
                                            }

                                        }));

        controls.add (UIUtils.createButtonBar (buts));

        controls.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                controls.getPreferredSize ().height));
                        
        this.fieldsView.add (controls);
        
        JScrollPane sp = UIUtils.createScrollPane (this.fieldsList);
        sp.getViewport ().setPreferredSize (new Dimension (450,
                                                           400));

        sp.setBorder (null);
        
        this.fieldsView.add (sp);
        
        this.showPanel ("view");
                   
        this.tabs.add ("Fields",
                       this.fieldsPanel);

/*
        this.tabs.add ("Sentence Structure",
                       this.sentenceWrapper);
*/
        //this.tabs.setBorder (UIUtils.createPadding (10, 5, 0, 5));

        Box b = new Box (BoxLayout.Y_AXIS);

        this.tabs.add ("Layout",
                       b);

        m = UIUtils.createHelpTextPane (String.format ("The layout tells Quoll Writer how you want to display the fields for the %s.  Certain fields like the %s description and picture are given special locations in the layouts.  For <b>Columns of fields</b> it just means that the fields are laid out in the order given in the <b>Fields</b> tab.",
                                                                 this.objType,
                                                                 this.objType),
                                                  viewer);
        
        hb = new Box (BoxLayout.Y_AXIS);
        hb.add (m);
        hb.setBorder (UIUtils.createPadding (5, 5, 0, 0));
        hb.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        b.add (hb);

        b.add (Box.createVerticalStrut (5));
        
        Vector<String> layoutTypes = new Vector ();
        layoutTypes.add (Constants.ASSET_LAYOUT_1);
        layoutTypes.add (Constants.ASSET_LAYOUT_2);
        layoutTypes.add (Constants.ASSET_LAYOUT_3);
        layoutTypes.add (Constants.ASSET_LAYOUT_4);
        layoutTypes.add (Constants.ASSET_LAYOUT_5);
        layoutTypes.add (Constants.ASSET_LAYOUT_6);
        layoutTypes.add (Constants.ASSET_LAYOUT_7);
        layoutTypes.add (Constants.ASSET_LAYOUT_8);
        
        final JList<String> layoutL = new JList (layoutTypes);
        
        layoutL.setCellRenderer (new DefaultListCellRenderer ()
        {

            private Map<String, ImageIcon> images = new HashMap ();

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                super.getListCellRendererComponent (list,
                                                    value,
                                                    index,
                                                    isSelected,
                                                    cellHasFocus);

                String imName = value.toString ();

                ImageIcon icon = this.images.get (imName);

                if (icon == null)
                {

                    icon = new ImageIcon (Environment.getImage (Constants.DATA_DIR + imName + ".png"));

                    this.images.put (imName,
                                     icon);

                }

                this.setIcon (icon);
                String text = "";

                if (imName.equals (Constants.ASSET_LAYOUT_1))
                {

                    text = "The object image, if present, is displayed in the top left corner and the object description is shown to the right.  Two columns of fields are displayed underneath. (Default)";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_2))
                {

                    text = "The object image is shown in the top left corner with the object description underneath.  The other fields are shown in a column on the right.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_3))
                {

                    text = "Two columns of fields are displayed.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_4))
                {

                    text = "The object image is shown in the top right corner with the object description underneath.  The other fields are shown in a column on the left.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_5))
                {

                    text = "The object description is shown in a column on the right, all others fields are shown in a column on the left.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_6))
                {

                    text = "The object description is shown in a column on the left, all others fields are shown in a column on the right.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_7))
                {

                    text = "The object description is shown in a column on the left, the object image is shown in the top right corner with all other fields in a column underneath.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_8))
                {

                    text = "The object image is shown in the top left corner with the object description in a column on the right.  The other object fields are shown underneath the object image.";

                }

                this.setText (String.format ("<html>%s</html>",
                                             Environment.replaceObjectNames (text)));
                this.setBorder (UIUtils.createPadding (5, 3, 5, 3));
                this.setVerticalTextPosition (SwingConstants.TOP);

                if (isSelected)
                {

                    this.setBorder (new CompoundBorder (UIUtils.createLineBorder (),
                                                        this.getBorder ()));

                }

                return this;

            }

        });

        UIUtils.setAsButton (layoutL);
                
        Box lb = new ScrollableBox (BoxLayout.Y_AXIS);
        lb.add (layoutL);
        
        sp = UIUtils.createScrollPane (lb);
        sp.getViewport ().setPreferredSize (new Dimension (450,
                                                           400));

        sp.setBorder (null);
                
        b.add (sp);
                
        this.add (this.tabs);
        
        UIUtils.doLater (new ActionListener ()
        {
            
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                layoutL.setSelectedValue (Constants.ASSET_LAYOUT_8,
                                          true);                
                                
            }
            
        });
        
    }
    
    private void addField (UserConfigurableObjectTypeField field)
    {
        
        FieldViewBox it = new FieldViewBox (field,
                                            this);

        it.init ();
                              
        try
        {
            
            this.viewer.saveObject (this.userObjType,
                                    true);

        } catch (Exception e) {
            
            Environment.logError ("Unable to create user config object type: " +
                                  this.userObjType,
                                  e);
            
            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to create the field.");
            
            return;
            
        }
        
        this.viewItems.put (field,
                            it);
                                      
        this.fieldsList.add (it,
                             0);
        
        this.userObjType.addConfigurableField (field);
        
    }
    
    private void updateField (UserConfigurableObjectTypeField field)
    {
        
        FieldViewBox it = this.viewItems.get (field);
        
        try
        {
            
            this.viewer.saveObject (this.userObjType,
                                    true);

        } catch (Exception e) {
            
            Environment.logError ("Unable to update user config object type: " +
                                  this.userObjType,
                                  e);
            
            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to update the field.");
            
            return;
            
        }
        
        if (it != null)
        {
            
            it.update ();
            
        }
                
    }

    private void initFields (Set<UserConfigurableObjectTypeField> fields)
    {
        
        this.fieldsList.removeAll ();
        
        long fakeKey = 1;
        
        for (UserConfigurableObjectTypeField f : fields)
        {
            
            FieldViewBox it = new FieldViewBox (f,
                                                this);
            f.setKey (fakeKey++);
    
            it.init ();
                          
            this.viewItems.put (f,
                                it);
                                          
            this.fieldsList.add (it);

        }
        
    }

    private void showPanel (String type)
    {
    
        ((CardLayout) this.fieldsPanel.getLayout ()).show (this.fieldsPanel,
                                                           type);
        
        this.validate ();
        this.repaint ();
        
    }
        
    private void showFormForFieldType (final String                               formTitle,
                                       final UserConfigurableObjectTypeField.Type type,
                                       final String                               formName)
    {
        
        this.editPanel.removeAll ();        
        
        final UserConfigurableObjectTypeEdit _this = this;
        
        List<FormItem> items = new ArrayList ();
                
        final JTextField name = UIUtils.createTextField ();
                
        if (formName != null)
        {
            
            name.setText (formName);
                
        }
        
        items.add (new FormItem ("Name",
                                 name));
                
        Vector<String> vals = new Vector ();
                
        for (UserConfigurableObjectTypeField.Type t : UserConfigurableObjectTypeField.Type.values ())
        {
            
            vals.add (t.getName ());
            
        }
                        
        final JComboBox<String> types = new JComboBox<String> (vals);
                
        UserConfigurableObjectTypeField field = this.formTypeFields.get (type);

        if (field == null)
        {
            
            field = UserConfigurableObjectTypeField.Type.getNewFieldForType (type);
            
            this.formTypeFields.put (type,
                                     field);
            
        }
        
        if (field.getKey () != null)
        {                
                        
            items.add (new FormItem ("Type",
                                     UIUtils.createLabel (field.getType ().getName ())));

        } else {
            
            items.add (new FormItem ("Type",
                                     types));            
                         
            types.setEditable (false);
               
            types.setSelectedItem (type.getName ());
    
            types.addItemListener (new ItemListener ()
            {
    
               public void itemStateChanged (ItemEvent ev)
               {
    
                    if (ev.getStateChange () != ItemEvent.SELECTED)
                    {
    
                        return;
    
                    }
             
                    UIUtils.doLater (new ActionListener ()
                    {
                        
                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {
             
                            UserConfigurableObjectTypeField.Type newType = UserConfigurableObjectTypeField.Type.getTypeForName ((String) types.getSelectedItem ());
                                                                                 
                            _this.showFormForFieldType (formTitle,
                                                        newType,
                                                        name.getText ().trim ());
                            
                        }
                        
                    });
                    
               }
               
            });

        }
                   
        Set<FormItem> nitems = field.getExtraFormItems ();
            
        if (nitems == null)
        {
            
            nitems = new HashSet ();
            
        }
            
        items.addAll (nitems);
                
        final Form f = new Form (formTitle,
                                 null,
                                 items,
                                 null,
                                 Form.SAVE_CANCEL_BUTTONS,
                                 false,
                                 false);

        f.addFormListener (new FormAdapter ()
        {

            public void actionPerformed (FormEvent ev)
            {

                if (ev.getActionCommand ().equals (FormEvent.CANCEL_ACTION_NAME))
                {

                    _this.showPanel ("view");
                
                    return;
                
                }
            
                if (name.getText ().trim ().equals (""))
                {
                    
                    f.showError ("The name of the field must be specified.");
                    
                    UIUtils.resizeParent (f);
                    
                    return;
                    
                }
                
                UserConfigurableObjectTypeField field = _this.formTypeFields.get (type);                                

                if (field == null)
                {
                    
                    UIUtils.showErrorMessage (_this.viewer,
                                              "An internal error has occurred, unable to add/update field.");
                    
                    Environment.logError ("No field for type: " + type,
                                          null);
                                
                    return;
                                
                }
                
                Set<String> errs = field.getExtraFormItemErrors ();

                if ((errs != null)
                    &&
                    (errs.size () > 0)
                   )
                {
                    
                    f.showError (errs.toString ());
                                
                    UIUtils.resizeParent (f);
                    
                    return;
                                
                }
                                    
                field.setFormName (name.getText ().trim ());

                field.updateFromExtraFormItems ();
                
                if (field.getKey () != null)
                {
                                        
                    _this.updateField (field);
                                        
                } else {
                                                                                
                    _this.addField (field);
                    
                }
                
                _this.showPanel ("view");
                
            }
            
        });
        
        f.setAlignmentX (Component.LEFT_ALIGNMENT);
        f.setBorder (null);

        this.editPanel.add (f);        
        
        UIUtils.doLater (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                name.requestFocus ();
                
                UIUtils.resizeParent (_this);
                
            }
            
        });
                
    }
    
    private void showAdd ()
    {
                
        this.formTypeFields = new HashMap ();
                                
        this.showFormForFieldType ("Add a new field",
                                   UserConfigurableObjectTypeField.Type.text,
                                   null);
            
        this.showPanel ("edit");

        this.validate ();            
        this.repaint ();
        
    }
        
    private void showEdit (final UserConfigurableObjectTypeField item)
    {

        this.formTypeFields = new HashMap ();
        this.formTypeFields.put (item.getType (),
                                 item);
                
        this.showFormForFieldType ("Edit field",
                                   item.getType (),
                                   item.getFormName ());
            
        this.showPanel ("edit");
            
        this.validate ();
        this.repaint ();
                
    }
        
    private class FieldViewBox extends Box
    {
        
        private UserConfigurableObjectTypeField item = null;
        private UserConfigurableObjectTypeEdit edit = null;
        private JLabel formNameLabel = null;
        
        public FieldViewBox (UserConfigurableObjectTypeField item,
                             UserConfigurableObjectTypeEdit  edit)
        {
            
            super (BoxLayout.X_AXIS);
            
            this.item = item;
            this.edit = edit;
            
        }
                
        public void update ()
        {
            
            this.formNameLabel.setText (item.getFormName ());
                
        }
                
        public void init ()
        {
                
            final FieldViewBox _this = this;
                
            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setOpaque (false);
    
            this.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
    
            this.formNameLabel = UIUtils.createLabel (null);
            
            this.add (this.formNameLabel);
            this.add (Box.createHorizontalGlue ());
            
            java.util.List<JButton> buttons = new ArrayList ();
    
            buttons.add (UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to edit this field",
                                               new ActionAdapter ()
                                               {
    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
    
                                                        _this.edit.showEdit (_this.item);
                                                    
                                                    }
    
                                               }));
    
            buttons.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to remove this field",
                                               new ActionAdapter ()
                                               {
    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
    
                                                    }
    
                                               }));
    
            JComponent buts = UIUtils.createButtonBar (buttons);
    
            buts.setAlignmentX (Component.LEFT_ALIGNMENT);
    
            this.add (buts);        
            
            this.update ();
            
        }
        
    }

}
