package com.quollwriter.ui.actionHandlers;

import java.awt.Dimension;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.QTextEditor;


public class EditTextPropertiesActionHandler extends AbstractActionHandler
{

    private JComboBox fonts = null;
    private JComboBox sizes = null;
    private JComboBox line = null;
    private JComboBox align = null;
    private JCheckBox cbox = null;
    private JCheckBox indent = null;

    public EditTextPropertiesActionHandler(AbstractProjectViewer pv)
    {

        super (null,
               pv,
               AbstractActionHandler.ADD,
               true);

    }

    public String getTitle (int mode)
    {

        return "Edit the Text Properties";

    }

    public String getIcon (int mode)
    {

        return "edit-properties";

    }

    public int getShowAtPosition ()
    {

        return -1;

    }

    public JComponent getFocussedField ()
    {

        return this.fonts;

    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        Project proj = this.projectViewer.getProject ();

        // Chapter c = this.getChapter ();

        final EditTextPropertiesActionHandler _this = this;

        List<FormItem> f = new ArrayList ();

        JTextArea note = UIUtils.createTextArea (-1);
        note.setEditable (false);
        note.setOpaque (false);
        note.setBorder (null);
        note.setSize (new Dimension (300,
                                     Short.MAX_VALUE));

        note.setText (Environment.replaceObjectNames ("Changes to the selections are immediately reflected in the current {chapter} however they are only made permanent when the Save button is pressed."));
        note.setPreferredSize (new Dimension (300,
                                              note.getPreferredSize ().height));

        f.add (new FormItem (note,
                             null));

        this.fonts = UIUtils.getFontsComboBox (proj.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME),
                                               this.getEditorPanel ());

        final JComboBox _fonts = this.fonts;

        f.add (new FormItem ("Font",
                             this.fonts));

        this.sizes = UIUtils.getFontSizesComboBox (proj.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME),
                                                   this.getEditorPanel ());

        f.add (new FormItem ("Size",
                             this.sizes));

        this.line = UIUtils.getLineSpacingComboBox (proj.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME),
                                                    this.getEditorPanel ());

        f.add (new FormItem ("Line Spacing",
                             this.line));

        this.align = UIUtils.getAlignmentComboBox (proj.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME),
                                                   this.getEditorPanel ());

        f.add (new FormItem ("Alignment",
                             this.align));

        boolean it = proj.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME);

        this.indent = new JCheckBox ("Indent the first line of each paragraph");

        this.indent.setSelected (it);

        this.indent.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.getEditorPanel ().setFirstLineIndent (_this.indent.isSelected ());

                }

            });

        f.add (new FormItem ("",
                             this.indent));

        this.cbox = new JCheckBox ("Set as defaults");

        f.add (new FormItem ("",
                             this.cbox));

        return f;

    }

    public void handleCancel (int mode)
    {

        QuollPanel qp = this.projectViewer.getCurrentlyVisibleTab ();

        if (qp instanceof AbstractEditorPanel)
        {

            // Nothing to do.
            ((AbstractEditorPanel) qp).initEditor ();

        }

    }

    private Chapter getChapter ()
    {

        QuollPanel qp = this.projectViewer.getCurrentlyVisibleTab ();

        if (qp instanceof AbstractEditorPanel)
        {

            return ((AbstractEditorPanel) qp).getChapter ();

        }

        return null;

    }

    private QTextEditor getEditor ()
    {

        QuollPanel qp = this.projectViewer.getCurrentlyVisibleTab ();

        if (qp instanceof AbstractEditorPanel)
        {

            return ((AbstractEditorPanel) qp).getEditor ();

        }

        return null;

    }

    public AbstractEditorPanel getEditorPanel ()
    {
        
        QuollPanel qp = this.projectViewer.getCurrentlyVisibleTab ();

        if (qp instanceof AbstractEditorPanel)
        {

            return (AbstractEditorPanel) qp;

        }

        return null;
        
    }

/*
    public void actionPerformed (ActionEvent ev)
    {

        this.showPopupAt = (Component) ev.getSource ();
        this.showPopupAtPosition = "above";

        super.actionPerformed (ev);

    }
*/
    public boolean handleSave (int mode)
    {

        Project proj = this.projectViewer.getProject ();

        com.gentlyweb.properties.Properties props = proj.getProperties ();

        com.gentlyweb.properties.Properties userProps = Environment.getUserProperties ();

        // Set the properties.
        // First off see if they should be the defaults, if so set them in the user properties.
/*
        if (cbox.isSelected ())
        {

            props = Environment.getUserProperties ();

        }
*/
        StringProperty alignP = new StringProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME,
                                                    align.getSelectedItem ().toString ());
        alignP.setDescription ("N/A");

        props.setProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME,
                           alignP);

        if (cbox.isSelected ())
        {

            userProps.setProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME,
                                   alignP);

        }

        StringProperty fontP = new StringProperty (Constants.EDITOR_FONT_PROPERTY_NAME,
                                                   fonts.getSelectedItem ().toString ());
        fontP.setDescription ("N/A");

        props.setProperty (Constants.EDITOR_FONT_PROPERTY_NAME,
                           fontP);

        if (cbox.isSelected ())
        {

            userProps.setProperty (Constants.EDITOR_FONT_PROPERTY_NAME,
                                   fontP);

        }

        try
        {

            FloatProperty lspaceP = new FloatProperty (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME,
                                                       Float.parseFloat (line.getSelectedItem ().toString ()));
            lspaceP.setDescription ("N/A");

            props.setProperty (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME,
                               lspaceP);

            if (cbox.isSelected ())
            {

                userProps.setProperty (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME,
                                       lspaceP);

            }

        } catch (Exception e)
        {

            // Ignore.

        }

        try
        {

            IntegerProperty sizeP = new IntegerProperty (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME,
                                                         Integer.parseInt (sizes.getSelectedItem ().toString ()));
            sizeP.setDescription ("N/A");

            props.setProperty (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME,
                               sizeP);

            if (cbox.isSelected ())
            {

                userProps.setProperty (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME,
                                       sizeP);

            }

        } catch (Exception e)
        {

            // Ignore.

        }

        BooleanProperty indenP = new BooleanProperty (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                                                      this.indent.isSelected ());
        indenP.setDescription ("N/A");

        props.setProperty (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                           indenP);

        if (cbox.isSelected ())
        {

            userProps.setProperty (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                                   indenP);

        }

        try
        {

            if (cbox.isSelected ())
            {

                Environment.saveUserProperties (userProps);

            }

            // Update the properties in the project.
            this.projectViewer.saveProject ();

            cbox.setSelected (false);

            // Update all the editors to use the new settings.
            this.projectViewer.reinitAllChapterEditors ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save changes",
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Unable to save text property changes.");

            return false;

        }

        return true;

    }

}
