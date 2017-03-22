package com.quollwriter.ui.userobjects;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.event.*;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ScrollableBox;

public class FieldsAddEdit extends Box implements ProjectEventListener
{

    public static DataFlavor COMPONENT_FLAVOR = null;

    private JPanel fieldsPanel = null;
    private Box editPanel = null;
    private Box fieldsList = null;
    private JScrollPane fieldsListScrollPane = null;
    private Box fieldsView = null;
    private JList fields = null;
    private JTextPane fieldsHelp = null;
    private Box fieldsHelpBox = null;
    private Map<UserConfigurableObjectTypeField, FieldViewBox> viewItems = new LinkedHashMap ();
    private Map<UserConfigurableObjectTypeField.Type, Set<FormItem>> formEditFields = new HashMap ();
    private Map<UserConfigurableObjectTypeField.Type, UserConfigurableObjectTypeField> formTypeFields = new HashMap ();
    private UserConfigurableObjectTypeField currentEditItem = null;
    private ProjectViewer viewer = null;
    private UserConfigurableObjectType type = null;
    private boolean showFormTitles = false;
    
    public FieldsAddEdit (ProjectViewer              viewer,
                          UserConfigurableObjectType type,
                          boolean                    showFormTitles)
    {
        
        super (BoxLayout.Y_AXIS);
        
        try
        {
            COMPONENT_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Component[].class.getName() + "\"");
        }
        catch(Exception e)
        {
            
        }
        
        Environment.addUserProjectEventListener (this);
        
        final FieldsAddEdit _this = this;
        
        this.type = type;
        this.viewer = viewer;
        this.showFormTitles = showFormTitles;
        
        this.fieldsPanel = new JPanel ();
        
        this.add (this.fieldsPanel);
        
        this.fieldsPanel.setLayout (new CardLayout ());
        this.fieldsPanel.setOpaque (false);
        this.fieldsPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.fieldsPanel.setPreferredSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                          400));

        this.fieldsList = new ScrollableBox (BoxLayout.Y_AXIS);
        this.fieldsList.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.fieldsView = new Box (BoxLayout.Y_AXIS);
        this.fieldsView.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.fieldsView.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                this.fieldsView.getPreferredSize ().height));
        this.fieldsView.setBorder (null);

        this.fieldsHelpBox = new Box (BoxLayout.Y_AXIS);

        this.fieldsHelp = UIUtils.createHelpTextPane ("",
                                                      viewer);

        this.fieldsHelpBox.setVisible (false);

        this.fieldsHelpBox.add (this.fieldsHelp);
        this.fieldsHelpBox.setBorder (UIUtils.createPadding (5, 5, 0, 0));
        this.fieldsHelpBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.fieldsView.add (this.fieldsHelpBox);

        this.fieldsView.add (Box.createVerticalStrut (0));

        this.fieldsPanel.add (this.fieldsView,
                              "view");

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
                                        new ActionListener ()
                                        {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.showAdd ();

                                            }

                                        }));

        controls.add (UIUtils.createButtonBar (buts));

        controls.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                controls.getPreferredSize ().height));

        this.fieldsView.add (controls);
        
        this.fieldsListScrollPane = UIUtils.createScrollPane (this.fieldsList);
        this.fieldsListScrollPane.getViewport ().setPreferredSize (new Dimension (450,
                                                                                  400));

        this.fieldsListScrollPane.setBorder (null);

        this.fieldsListScrollPane.setTransferHandler (new TransferHandler ()
        {
                
            @Override
            public boolean canImport (TransferSupport support)
            {
                
                if (!support.isDrop ())
                {
                
                    return false;
                }
        
                boolean canImport = support.isDataFlavorSupported (COMPONENT_FLAVOR);
                
                return canImport;
            }
        
            @Override
            public boolean importData (TransferSupport support)
            {
        
                if (!canImport (support))
                {
                    
                    return false;
                }
        
                Component[] components;
        
                try
                {
                    components = (Component[]) support.getTransferable ().getTransferData (COMPONENT_FLAVOR);
                    
                } catch (Exception e) {

                    return false;
                }
                                    
                // Item being transfered.
                FieldViewBox component = (FieldViewBox) components[0];
                                                                                                               
                // Add the item below where we are.                
                _this.fieldsList.add (component);

                _this.fieldsList.revalidate ();
                _this.fieldsList.repaint ();
                
                int c = 0;
                
                for (FieldViewBox f : _this.getFieldViewBoxs ())
                {
                    
                    f.setSelected (false);
                    
                    f.getField ().setOrder (c);
                    
                    c++;
                                                        
                }
                                    
                try
                {
                
                    Environment.updateUserConfigurableObjectType (_this.type);

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to save type: " +
                                          _this.type,
                                          e);
                    
                    UIUtils.showErrorMessage (_this.viewer,
                                              "Unable to move.");
                    
                    return false;
                    
                }
                
                return true;
            
            }
                
            @Override
            public void exportDone (JComponent   c,
                                    Transferable t,
                                    int          action)
            {
                            
            }

        });        
        
        this.fieldsView.add (this.fieldsListScrollPane);
        
        
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
    
    public void saveField (UserConfigurableObjectTypeField field)
    {

        try
        {
    
            Environment.updateUserConfigurableObjectTypeField (field);

        } catch (Exception e) {

            Environment.logError ("Unable to create user config object type field: " +
                                  field,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to create the field.");

            return;

        }
        
    }
    
    private void addField (UserConfigurableObjectTypeField field)
    {

        field.setUserConfigurableObjectType (this.type);
        field.setOrder (this.type.getConfigurableFields ().size ());

        // Need to save first so the hashcode gets set correctly.
        this.saveField (field);

        this.type.addConfigurableField (field);

        FieldViewBox it = new FieldViewBox (field,
                                            this);

        it.init ();

        this.viewItems.put (field,
                            it);

        this.fieldsList.add (it);
                
    }

    private void updateField (UserConfigurableObjectTypeField field)
    {

        FieldViewBox it = this.viewItems.get (field);

        this.saveField (field);

        if (it != null)
        {

            it.update ();

        }

    }

    private void removeField (UserConfigurableObjectTypeField field)
    {
        
        FieldViewBox it = this.viewItems.get (field);
                
        this.type.removeConfigurableField (field);
        
        try
        {

            Environment.removeUserConfigurableObjectTypeField (field);

            Environment.updateUserConfigurableObjectType (this.type);

        } catch (Exception e) {

            Environment.logError ("Unable to delete user config object field: " +
                                  field,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to remove the field.");

            return;

        }

        Environment.fireUserProjectEvent (this.type,
                                          ProjectEvent.USER_OBJECT_TYPE,
                                          ProjectEvent.CHANGED,
                                          this.type);

        //this.refresh ();

    }

    public void setHelpText (String t)
    {
        
        this.fieldsHelp.setText (t);
        this.fieldsHelpBox.setVisible (true);
        
    }

    public void init ()
    {
    
        Set<UserConfigurableObjectTypeField> fields = this.type.getConfigurableFields ();

        this.viewItems = new LinkedHashMap ();
        this.fieldsList.removeAll ();

        for (UserConfigurableObjectTypeField f : fields)
        {

            FieldViewBox it = new FieldViewBox (f,
                                                this);

            it.init ();

            this.viewItems.put (f,
                                it);

            this.fieldsList.add (it);

            this.initFieldViewBox (it);
            
        }

    }

    private void initFieldViewBox (final FieldViewBox it)
    {
        
        final FieldsAddEdit _this = this;
        
        // Used to initiate the drag.
        MouseEventHandler listener = new MouseEventHandler ()
        {
            
            @Override
            public void mouseDragged(MouseEvent e)
            {
                
                JComponent c = (JComponent) e.getSource ();
                TransferHandler handler = it.getTransferHandler ();
                handler.setDragImage (UIUtils.getImageOfComponent (it,
                                                                   it.getWidth (),
                                                                   it.getHeight ()));

                handler.exportAsDrag (it,
                                      e,
                                      TransferHandler.MOVE);
                                
            }
 
        };                
        
        TransferHandler transferHandler = new TransferHandler ()
        {
                
            private JComponent getParentOfType (JComponent comp,
                                                Class      cl)
            {
                
                if (cl.isAssignableFrom (comp.getClass ()))
                {
                    
                    return comp;
                    
                }
                
                while ((comp = (JComponent) comp.getParent ()) != null)
                {
                    
                    if (cl.isAssignableFrom (comp.getClass ()))
                    {
                        
                        return comp;
                        
                    }
                    
                }
                
                return null;
                
            }
        
            private int getIndex (Component c)
            {
                
                Component[] comps = _this.fieldsList.getComponents ();
                
                for (int i = 0; i < comps.length; i++)
                {
                    
                    Component comp = comps[i];
                    
                    if (c == comp)
                    {
                        
                        return i;
                        
                    }
                
                }
                
                return -1;
                
            }
        
            @Override
            public boolean canImport (TransferSupport support)
            {
                
                if (!support.isDrop ())
                {
                
                    return false;
                }
        
                boolean canImport = support.isDataFlavorSupported (COMPONENT_FLAVOR);
                             
                support.setShowDropLocation (false);
                                
                return canImport;
            }
        
            @Override
            public boolean importData (TransferSupport support)
            {
        
                if (!canImport (support))
                {
                    
                    return false;
                }
        
                Component[] components;
        
                try
                {
                    components = (Component[]) support.getTransferable ().getTransferData (COMPONENT_FLAVOR);
                    
                } catch (Exception e) {

                    return false;
                }
        
                // Item being transfered.
                FieldViewBox component = (FieldViewBox) components[0];
                                                
                FieldViewBox it = (FieldViewBox) this.getParentOfType ((JComponent) support.getComponent (),
                                                                       FieldViewBox.class);
                                                               
                // Add the item below where we are.
                int ind = this.getIndex (it);
                
                _this.fieldsList.add (component,
                                      ind);

                _this.fieldsList.revalidate ();
                _this.fieldsList.repaint ();
                
                int c = 0;
                
                for (FieldViewBox f : _this.getFieldViewBoxs ())
                {
                    
                    f.setSelected (false);
                    
                    f.getField ().setOrder (c);
                    
                    c++;
                                                        
                }
                                    
                try
                {
                
                    Environment.updateUserConfigurableObjectType (_this.type);

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to save type: " +
                                          _this.type,
                                          e);
                    
                    UIUtils.showErrorMessage (_this.viewer,
                                              "Unable to move.");
                    
                    return false;
                    
                }
                
                UIUtils.doLater (new ActionListener ()
                {
                    
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.fieldsList.scrollRectToVisible (component.getBounds ());
                        
                    }
                    
                });
        
                return true;

            }

            @Override
            public int getSourceActions (JComponent c)
            {

                return MOVE;
            
            }
        
            @Override
            public Transferable createTransferable (final JComponent c)
            {
                
                return new Transferable ()
                {
                    
                    @Override
                    public Object getTransferData (DataFlavor flavor)
                    {
                        
                        Component[] components = new Component[1];
                        components[0] = c;
                        return components;
                    
                    }
        
                    @Override
                    public DataFlavor[] getTransferDataFlavors()
                    {
                        
                        DataFlavor[] flavors = new DataFlavor[1];
                        flavors[0] = COMPONENT_FLAVOR;
                        return flavors;
                    
                    }
        
                    @Override
                    public boolean isDataFlavorSupported (DataFlavor flavor)
                    {
                        
                        return flavor.equals (COMPONENT_FLAVOR);
                    
                    }
                    
                };
            }
        
            @Override
            public void exportDone (JComponent   c,
                                    Transferable t,
                                    int          action)
            {
                
            }

        };
            
        DropTargetListener dropTargetListener = new DropTargetAdapter ()
        {
                            
            @Override
            public void dragOver (DropTargetDragEvent ev)
            {
                
                java.awt.Point mp = _this.fieldsListScrollPane.getMousePosition ();
                
                if (mp == null)
                {
                    
                    return;
                    
                }
                                
                int a = _this.fieldsListScrollPane.getVerticalScrollBar ().getUnitIncrement ();
                
                java.awt.Point vp = _this.fieldsListScrollPane.getViewport ().getViewPosition ();

                if (mp.y <= 5 * a)
                {
                                       
                    int newy = vp.y - (a / 2);
                    
                    if (newy < 0)
                    {
                        
                        newy = 0;
                        
                    }
                    
                    _this.fieldsListScrollPane.getViewport ().setViewPosition (new java.awt.Point (vp.x, newy));
                    
                    return;
                    
                }

                int h = _this.fieldsListScrollPane.getViewport ().getExtentSize ().height;

                if (mp.y >= (h - (5 * a)))
                {
                                            
                    int newy = vp.y + (a / 2);
                    
                    if (newy > (vp.y + h))
                    {
                        
                        newy = (vp.y + h);
                        
                    }
                    
                    _this.fieldsListScrollPane.getViewport ().setViewPosition (new java.awt.Point (vp.x, newy));
                    
                    return;                        
                                    
                }
                
            }
                        
            @Override
            public void drop (DropTargetDropEvent ev)
            {

            }
                        
            @Override
            public void dragEnter (DropTargetDragEvent ev)
            {
                
                try
                {
                
                    if (ev.getTransferable ().getTransferData (COMPONENT_FLAVOR) == null)
                    {
                      
                        return;
                        
                    }

                } catch (Exception e) {
                    
                    return;
                    
                }
                
                for (FieldViewBox f : _this.getFieldViewBoxs ())
                {
                    
                    f.setSelected (false);
                    
                }
                                
                FieldViewBox f = (FieldViewBox) ev.getDropTargetContext ().getComponent ();

                f.setSelected (true);
                
            }

        };
        
        it.setTransferHandler (transferHandler);
                        
        it.addMouseMotionListener (listener);
        
        try
        {
        
            it.getDropTarget ().addDropTargetListener (dropTargetListener);

        } catch (Exception e) {
                        
        }                
        
    }
    
    private void setSelected (FieldViewBox f,
                              boolean      selected)
    {
        
    }
    
    private Set<FieldViewBox> getFieldViewBoxs ()
    {
        
        Set<FieldViewBox> boxes = new LinkedHashSet ();
        
        for (int i = 0; i < this.fieldsList.getComponentCount (); i++)
        {
            
            Component c = this.fieldsList.getComponent (i);
            
            if (c instanceof FieldViewBox)
            {
                
                boxes.add ((FieldViewBox) c);
                
            }
            
        }

        return boxes;
        
    }
    
    public void refresh ()
    {
        
        this.init ();
        
    }
    
    private void showAdd ()
    {

        this.formTypeFields = new HashMap ();

        this.showFormForAddType ("Add a new field",
                                 UserConfigurableObjectTypeField.Type.text,
                                 null);

        this.showPanel ("edit");

        this.validate ();
        this.repaint ();

    }

    private void showEdit (final UserConfigurableObjectTypeField item)
    {

        this.showFormForEditType (item);

        this.showPanel ("edit");

        this.validate ();
        this.repaint ();

    }
    
    private void showPanel (String type)
    {

        ((CardLayout) this.fieldsPanel.getLayout ()).show (this.fieldsPanel,
                                                           type);

        this.validate ();
        this.repaint ();

    }

    private void showFormForAddType (final String                               formTitle,
                                     final UserConfigurableObjectTypeField.Type type,
                                     final String                               formName)
    {

        this.editPanel.removeAll ();

        if (this.showFormTitles)
        {

            JComponent h = UIUtils.createBoldSubHeader (formTitle,
                                                        null);
    
            h.setBorder (UIUtils.createPadding (3, 5, 0, 0));
    
            this.editPanel.add (h);

        }

        final FieldsAddEdit _this = this;

        Set<FormItem> items = new LinkedHashSet ();

        TextFormItem name = new TextFormItem ("Name",
                                              formName);

        items.add (name);

        // Create

        Vector<String> vals = new Vector ();

        for (UserConfigurableObjectTypeField.Type t : UserConfigurableObjectTypeField.Type.values ())
        {

            if (t == UserConfigurableObjectTypeField.Type.objectname)
            {

                continue;

            }

            // Only allow a single object desc.
            if ((t == UserConfigurableObjectTypeField.Type.objectdesc)
                &&
                (this.type.getObjectDescriptionField () != null)
               )
            {
                
                continue;
                
            }
            
            // Only allow a single object desc.
            if ((t == UserConfigurableObjectTypeField.Type.objectimage)
                &&
                (this.type.getObjectImageField () != null)
               )
            {
                
                continue;
                
            }

            vals.add (t.getName ());

        }

        ComboBoxFormItem types = new ComboBoxFormItem ("Type",
                                                       vals);

        UserConfigurableObjectTypeField field = this.formTypeFields.get (type);

        if (field == null)
        {

            field = UserConfigurableObjectTypeField.Type.getNewFieldForType (type);

            UserConfigurableObjectType fakeType = new UserConfigurableObjectType ();
            fakeType.setObjectTypeName (this.type.getObjectTypeName ());

            field.setUserConfigurableObjectType (fakeType);

            this.formTypeFields.put (type,
                                     field);

        }

        final UserConfigurableObjectTypeFieldConfigHandler handler = field.getConfigHandler ();

        items.add (types);

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

                        UserConfigurableObjectTypeField.Type newType = UserConfigurableObjectTypeField.Type.getTypeForName (types.getValue ());

                        _this.showFormForAddType (formTitle,
                                                  newType,
                                                  (name.getText () != null ? name.getText () : null));

                    }

                });

           }

        });

        Set<FormItem> nitems = handler.getExtraFormItems ();

        if (nitems == null)
        {

            nitems = new HashSet ();

        }

        items.addAll (nitems);

        Map<Form.Button, ActionListener> buttons = new LinkedHashMap ();

        buttons.put (Form.Button.save,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            Form f = (Form) ev.getSource ();

                            if (name.getText () == null)
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

                            field.setUserConfigurableObjectType (null);

                            // Check for the name being the same as another field.
                            String formName = name.getText ().trim ();
                            String lformName = formName.toLowerCase ();

                            for (UserConfigurableObjectTypeField uf : _this.type.getConfigurableFields ())
                            {

                                if (!uf.equals (field))
                                {

                                    if (uf.getFormName ().toLowerCase ().equals (lformName))
                                    {

                                        f.showError (String.format ("Already have a field called <b>%s</b>.",
                                                                    uf.getFormName ()));

                                        UIUtils.resizeParent (f);

                                        return;

                                    }

                                }

                            }

                            Set<String> errs = handler.getExtraFormItemErrors (_this.type);

                            if ((errs != null)
                                &&
                                (errs.size () > 0)
                               )
                            {

                                StringBuilder b = new StringBuilder ();

                                for (String err : errs)
                                {

                                    if (b.length () > 0)
                                    {

                                        b.append ("<br />");

                                    }

                                    b.append (err);

                                }

                                f.showError (b.toString ());

                                UIUtils.resizeParent (f);

                                return;

                            }

                            field.setFormName (formName);

                            handler.updateFromExtraFormItems ();

                            _this.addField (field);

                            _this.showPanel ("view");

                        }

                     });

        buttons.put (Form.Button.cancel,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.showPanel ("view");

                            return;

                        }

                     });

        Form f = new Form (Form.Layout.stacked,
                           items,
                           buttons);
        f.setAlignmentX (Component.LEFT_ALIGNMENT);
        f.setBorder (UIUtils.createPadding (5, 10, 5, 5));

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

    private void showFormForEditType (final UserConfigurableObjectTypeField field)
    {

        this.editPanel.removeAll ();

        final FieldsAddEdit _this = this;

        Set<FormItem> items = new LinkedHashSet ();

        final TextFormItem name = new TextFormItem ("Name",
                                                    field.getFormName ());

        items.add (name);

        final UserConfigurableObjectTypeFieldConfigHandler handler = field.getConfigHandler ();

        items.add (new AnyFormItem ("Type",
                                    UIUtils.createInformationLabel (field.getType ().getName ())));

        Set<FormItem> nitems = handler.getExtraFormItems ();

        if (nitems == null)
        {

            nitems = new HashSet ();

        }

        items.addAll (nitems);

        Map<Form.Button, ActionListener> buttons = new LinkedHashMap ();

        buttons.put (Form.Button.save,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            Form f = (Form) ev.getSource ();

                            // Check for the name being the same as another field.
                            String formName = name.getText ().trim ();
                            String lformName = formName.toLowerCase ();

                            if (formName.equals (""))
                            {

                                f.showError ("The name of the field must be specified.");

                                UIUtils.resizeParent (f);

                                return;

                            }

                            for (UserConfigurableObjectTypeField uf : _this.type.getConfigurableFields ())
                            {

                                if (!uf.equals (field))
                                {

                                    if (uf.getFormName ().toLowerCase ().equals (lformName))
                                    {

                                        f.showError (String.format ("Already have a field called <b>%s</b>.",
                                                                    uf.getFormName ()));

                                        UIUtils.resizeParent (f);

                                        return;

                                    }

                                }

                            }

                            Set<String> errs = handler.getExtraFormItemErrors (_this.type);

                            if ((errs != null)
                                &&
                                (errs.size () > 0)
                               )
                            {

                                StringBuilder b = new StringBuilder ();

                                for (String err : errs)
                                {

                                    if (b.length () > 0)
                                    {

                                        b.append ("<br />");

                                    }

                                    b.append (err);

                                }

                                f.showError (b.toString ());

                                UIUtils.resizeParent (f);

                                return;

                            }

                            field.setFormName (name.getText ().trim ());

                            handler.updateFromExtraFormItems ();

                            _this.updateField (field);

                            _this.showPanel ("view");

                        }

                     });

        buttons.put (Form.Button.cancel,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.showPanel ("view");

                            return;

                        }

                     });


        final Form f = new Form (Form.Layout.stacked,
                                 items,
                                 buttons);
        f.setAlignmentX (Component.LEFT_ALIGNMENT);
        f.setBorder (UIUtils.createPadding (5, 10, 5, 5));

        if (this.showFormTitles)
        {

            JComponent h = UIUtils.createBoldSubHeader ("Edit field",
                                                        null);
    
            h.setBorder (UIUtils.createPadding (3, 5, 0, 0));
    
            this.editPanel.add (h);

        }

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

    private class FieldViewBox extends Box
    {
    
        private UserConfigurableObjectTypeField item = null;
        private FieldsAddEdit edit = null;
        private JLabel formNameLabel = null;
        private JLabel formDescLabel = null;
    
        public FieldViewBox (UserConfigurableObjectTypeField item,
                             FieldsAddEdit                   edit)
        {
    
            super (BoxLayout.X_AXIS);
    
            this.item = item;
            this.edit = edit;
    
        }

        public UserConfigurableObjectTypeField getField ()
        {
            
            return this.item;
            
        }
        
        public void setSelected (boolean sel)
        {
            
            this.setOpaque (true);
            
            if (!sel)
            {
                                
                java.awt.Color c = new JLabel ().getForeground ();
                
                this.formNameLabel.setForeground (c);
                this.formDescLabel.setForeground (c);
                this.setBackground (UIUtils.getComponentColor ());
    
            } else {
                        
                this.formNameLabel.setForeground (UIManager.getColor ("Tree.selectionForeground"));
                this.formDescLabel.setForeground (UIManager.getColor ("Tree.selectionForeground"));
                this.setBackground (UIManager.getColor ("Tree.selectionBackground"));            
                
            }
            
        }
    
        public void update ()
        {
    
            this.formNameLabel.setText (item.getFormName ());
            this.formDescLabel.setText (item.getConfigHandler ().getConfigurationDescription ());
    
        }
    
        public void init ()
        {
    
            final FieldViewBox _this = this;
    
            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setOpaque (false);
    
            this.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
    
            this.formNameLabel = UIUtils.createLabel (null);
    
            this.formDescLabel = UIUtils.createInformationLabel (null);
    
            Box b = new Box (BoxLayout.Y_AXIS);
    
            this.formDescLabel.setBorder (UIUtils.createPadding (3, 5, 3, 0));
    
            b.add (this.formNameLabel);
            b.add (this.formDescLabel);
    
            this.add (b);
            this.add (Box.createHorizontalGlue ());
    
            java.util.List<JButton> buttons = new ArrayList ();
    
            if (this.item.canEdit ())
            {
    
                buttons.add (UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                                   Constants.ICON_MENU,
                                                   "Click to edit this field",
                                                   new ActionListener ()
                                                   {
    
                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
    
                                                            _this.edit.showEdit (_this.item);
    
                                                        }
    
                                                   }));
    
            }
    
            if (this.item.canDelete ())
            {
    
                buttons.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                                   Constants.ICON_MENU,
                                                   "Click to remove this field",
                                                   new ActionListener ()
                                                   {
    
                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
    
                                                            UIUtils.createQuestionPopup (_this.edit.viewer,
                                                                                         "Remove field",
                                                                                         Constants.DELETE_ICON_NAME,
                                                                                         "Remove this field?  Note: any data associated with the field will also be removed.",
                                                                                         "Yes, remove it",
                                                                                         Constants.CANCEL_BUTTON_LABEL_ID,
                                                                                         new ActionListener ()
                                                                                         {
    
                                                                                            @Override
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {
    
                                                                                                _this.edit.removeField (_this.item);
        
                                                                                            }
    
                                                                                         },
                                                                                         null,
                                                                                         null,
                                                                                         SwingUtilities.convertPoint (_this.edit,
                                                                                                                      5,
                                                                                                                      5,
                                                                                                                      _this.edit.viewer));
    
                                                        }
    
                                                   }));
    
            }
    
            JComponent buts = UIUtils.createButtonBar (buttons);
    
            buts.setAlignmentX (Component.LEFT_ALIGNMENT);
    
            this.add (buts);
    
            this.update ();
    
        }
    
    }

}
