package com.quollwriter.ui.fx.popups;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AddEditNotePopup extends PopupContent<ProjectViewer>
{

    private static final String POPUP_ID = "note";
    private Note item = null;
    private Chapter chapter = null;
    private Form form = null;
    private QuollTextField summary = null;
    private QuollComboBox type = null;
    private QuollTextArea desc = null;
    private CheckBox addToChapter = null;
    private boolean addMode = false;
    private HyperlinkLinkedToPanel linkedToPanel = null;
    private TextEditor.Highlight highlight = null;

    public AddEditNotePopup (ProjectViewer viewer,
                             Note          item,
                             Chapter       chapter)
    {

        super (viewer);

        this.item = item;
        this.chapter = chapter;

        if (this.item.getKey () == null)
        {

            throw new IllegalArgumentException ("Expected item to have a key.");

        }

        this.addMode = this.item.getKey () < 0;

        ChapterEditorPanelContent editor = viewer.getEditorForChapter (this.chapter);

        this.form = this.addItems (Form.builder (),
                                   editor.getSelectedText ())
            .confirmButton (getUILanguageStringProperty (buttons,save))
            .cancelButton (getUILanguageStringProperty (buttons,cancel))
            .build ();

        this.form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                                   ev ->
        {

            this.close ();

        });

        this.form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
        ev ->
        {

            this.form.hideError ();

            Set<StringProperty> err = this.getFormErrors ();

            if (err.size () > 0)
            {

                this.form.showErrors (err);
                return;

            }

            if (this.handleSave ())
            {

                this.close ();

            }

        });

        if (!this.addMode)
        {

            if ((this.item.isEditNeeded ())
                &&
                (this.item.getEndPosition () > this.item.getStartPosition ())
               )
            {

                this.highlight = this.viewer.getEditorForChapter (this.item.getChapter ()).getEditor ().addHighlight (new IndexRange (this.item.getStartPosition (),
                                                                                                                                      this.item.getEndPosition ()),
                                                                                                                      UserProperties.getEditNeededNoteChapterHighlightColor ());

            }

        }

        this.getChildren ().add (this.form);

    }

    public void setOnClose (Runnable r)
    {

        this.getPopup ().setOnClose (r);

    }

    public void setOnCancel (EventHandler<Form.FormEvent> h)
    {

        this.form.setOnCancel (h);

    }

    private Set<StringProperty> getFormErrors ()
    {

        List<String> prefix = Arrays.asList (notes,addedit,errors,novalue);

        Set<StringProperty> errs = new LinkedHashSet ();

        if (this.item.isEditNeeded ())
        {

            String text = this.desc.getText ();

            if ((text == null)
                ||
                (text.trim ().length () == 0)
               )
            {

                errs.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.description)));
                        //"Please enter a description.");

            }

        } else
        {

            if (this.summary.getText ().equals (""))
            {

                errs.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.summary)));
                        //"Please enter a summary.");

            }

        }

        return errs;

    }

    private Form.Builder addItems (Form.Builder builder,
                                   String       selectedText)
    {

        List<String> prefix = Arrays.asList (notes,addedit,labels);
        this.summary = QuollTextField.builder ()
            .styleClassName (StyleClassNames.SUMMARY)
            .build ();

        this.desc = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (chapteritems,labels,description,tooltip))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .withViewer (this.viewer)
            .formattingEnabled (true)
            .build ();

        if (!this.item.isEditNeeded ())
        {

            builder.item (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.summary)),
                          this.summary);

        }

        builder.item (getUILanguageStringProperty (Utils.newList (prefix,description)),
                      this.desc);

        if (!this.item.isEditNeeded ())
        {

            this.type = QuollComboBox.builder ()
                .items (UserProperties.getNoteTypes ())
                .build ();
            UIUtils.setTooltip (this.type,
                                getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.type,tooltip)));

            builder.item (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.type,text)),
                          this.type);

        }

        this.linkedToPanel = new HyperlinkLinkedToPanel (this.item,
                                                         this.getBinder (),
                                                         this.viewer);

        builder.item (this.linkedToPanel);

        if (this.addMode)
        {

            if ((selectedText != null)
                &&
                (selectedText.trim ().length () > 0)
                &&
                (!this.item.isEditNeeded ())
               )
            {

                Paragraph p = new Paragraph (selectedText,
                                             0);

                this.summary.setText (p.getFirstSentence ().getText ());

                if (p.getSentenceCount () > 1)
                {

                    this.desc.setText (new StringWithMarkup (selectedText.substring (p.getFirstSentence ().getNext ().getAllTextStartOffset ()).trim ()));

                }

            }

        } else {

            this.summary.setText (this.item.getSummary ());
            this.desc.setText (this.item.getDescription ());

        }

        return builder;

    }

    private boolean handleSave ()
    {

        ChapterEditorPanelContent editor = this.viewer.getEditorForChapter (this.chapter);

        if (this.item.isEditNeeded ())
        {

            StringWithMarkup text = this.desc.getTextWithMarkup ();

            Paragraph p = new Paragraph (text.getText (),
                                         0);

            this.item.setSummary (p.getFirstSentence ().getText ());

        } else
        {

            // Fill up the note.
            this.item.setSummary (this.summary.getText ());

        }

        this.item.setDescription (this.desc.getTextWithMarkup ());

        String type = null;

        if (!this.item.isEditNeeded ())
        {

            type = this.type.getSelectionModel ().getSelectedItem ().getValue ();

            this.item.setType (type);

        } else
        {

            type = Note.EDIT_NEEDED_NOTE_TYPE;

        }

        // If the type is "edit needed" then get any selectd text indices.
        if (this.item.isEditNeeded ())
        {

            IndexRange r = editor.getEditor ().getSelection ();
            int s = r.getStart ();
            int e = r.getEnd ();

            if ((!this.addMode)
                &&
                (s != e)
                &&
                (e > s)
               )
            {

                this.item.setPosition (s);
                this.item.setEndPosition (e);

            }

            if (this.addMode)
            {

                this.item.setPosition (s);
                this.item.setEndPosition (e);

            }

        }

        // See if we are adding at the end of the chapter.
        if (editor.isPositionAtTextEnd (this.item.getPosition ()))
        {

            try
            {

                // Add a newline to the end of the chapter.
                editor.insertText (this.item.getPosition (),
                                   "\n");

                this.item.setTextPosition2 (editor.createTextPosition (this.item.getPosition () - 1));

            } catch (Exception e) {

                Environment.logError ("Unable to insert newline at end of chapter",
                                      e);

            }

        }

        try
        {

            if (this.addMode)
            {

                // Add the item to the chapter.
                this.item.setKey (null);
                this.item.setChapter (this.chapter);

                // Need to save the object first so the key is setup correctly.
                this.viewer.saveObject (this.item,
                                        true);

                this.chapter.addNote (this.item);

            }

            this.viewer.saveObject (this.item,
                                    true);

            this.viewer.fireProjectEvent (ProjectEvent.Type.note,
                                          (this.addMode ? ProjectEvent.Action._new : ProjectEvent.Action.edit),
                                          this.item);


        } catch (Exception e)
        {

            Environment.logError ("Unable to save/add note: " +
                                  this.item,
                                  e);

            this.chapter.removeNote (this.item);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty(notes, (this.addMode ? _new : edit), actionerror));

            return false;

        }

        if (this.item.isEditNeeded ())
        {

            try
            {

                this.item.setTextPosition2 (editor.createTextPosition (this.item.getPosition ()));

                if (this.item.getEndPosition () > -1)
                {

                    this.item.setEndTextPosition2 (editor.createTextPosition (this.item.getEndPosition ()));

                }

            } catch (Exception e) {

                Environment.logError ("Unable to set text position",
                                      e);

            }

        }

        if (this.item.getChapter () != null)
        {

            editor.requestFocus ();

        }

        // Need to reindex the chapter to ensure that things are in the right order.
        this.chapter.reindex ();

        String t = type;

        if (this.item.isEditNeeded ())
        {

            //t = getUIString (notetypes,editneededtype);

        }

        UserProperties.addNoteType (t);

        return true;

    }

    @Override
    public QuollPopup createPopup ()
    {

        StringProperty title = null;

        if (!this.addMode)
        {

            title = getUILanguageStringProperty (notes,edit,LanguageStrings.title);

        } else {

            title = getUILanguageStringProperty (notes,_new, (this.item.isEditNeeded () ? editneededtitle : LanguageStrings.title));

        }

        QuollPopup p = QuollPopup.builder ()
            .title (title)
            .styleClassName (this.addMode ? StyleClassNames.ADD : StyleClassNames.EDIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (AddEditNotePopup.getPopupIdForNote (this.item))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        //p.getStyleClass ().add (StyleClassNames.NOTE);
        p.getStyleClass ().add (this.item.isEditNeeded () ? StyleClassNames.EDITNEEDED : StyleClassNames.NOTE);

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            this.summary.requestFocus ();

        });

        p.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                           ev ->
        {

            ProjectChapterEditorPanelContent ed = this.viewer.getEditorForChapter (this.item.getChapter ());

            if (ed != null)
            {

                ed.getEditor ().removeHighlight (this.highlight);

            }

        });

        return p;

    }

    public static String getPopupIdForNote (Note ci)
    {

        return POPUP_ID + ci.getObjectReference ().asString ();

    }

}
