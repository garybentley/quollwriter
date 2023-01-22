package com.quollwriter.editors.ui;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;
import javafx.collections.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.text.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AddEditCommentPopup extends PopupContent<EditorProjectViewer>
{

    private static final String POPUP_ID = "comment";
    private Note item = null;
    private Chapter chapter = null;
    private Form form = null;
    private QuollTextArea desc = null;
    private boolean addMode = false;
    private HyperlinkLinkedToPanel linkedToPanel = null;
    private TextEditor.Highlight highlight = null;

    public AddEditCommentPopup (EditorProjectViewer viewer,
                                Chapter             ch,
                                Note                item)
    {

        super (viewer);

        this.item = item;
        this.chapter = ch;

        if (this.item.getKey () == null)
        {

            throw new IllegalArgumentException ("Expected item to have a key.");

        }

        this.addMode = this.item.getKey () < 0;

        EditorChapterPanel editor = viewer.getEditorForChapter (this.chapter);

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

            this.highlight = this.viewer.getEditorForChapter (this.chapter).getEditor ().addHighlight (new IndexRange (this.item.getStartPosition (),
                                                                                                                       this.item.getEndPosition ()),
                                                                                                       UserProperties.getEditorCommentChapterHighlightColor ());

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

        List<String> prefix = Arrays.asList (comments,addedit,errors);

        Set<StringProperty> errs = new LinkedHashSet<> ();

        String text = this.desc.getText ();

        if ((text == null)
            ||
            (text.trim ().length () == 0)
           )
        {

            errs.add (getUILanguageStringProperty (Utils.newList (prefix,novalue)));
                    //"Please enter a description.");

        }

        return errs;

    }

    private Form.Builder addItems (Form.Builder builder,
                                   String       selectedText)
    {

        List<String> prefix = Arrays.asList (comments,addedit,labels);

        this.desc = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (comments,addedit,labels,comment,tooltip))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .withViewer (this.viewer)
            .formattingEnabled (true)
            .build ();

        builder.item (this.desc);

        this.linkedToPanel = new HyperlinkLinkedToPanel (this.item,
                                                         this.getBinder (),
                                                         this.viewer);

        builder.item (this.linkedToPanel);

        this.desc.setText (this.item.getDescription ());

        return builder;

    }

    private boolean handleSave ()
    {

        String c = this.desc.getText ();

        this.item.setDescription (this.desc.getTextWithMarkup ());

        // Use the first line of the description as the summary.
        Paragraph p = new Paragraph (c,
                                     0);

        this.item.setSummary (p.getFirstSentence ().getText ());

        EditorChapterPanel editor = viewer.getEditorForChapter (this.chapter);

        String type = Note.EDIT_NEEDED_NOTE_TYPE;

        int s = editor.getSelection ().getStart ();
        int e = editor.getSelection ().getEnd ();

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

            this.item.setLinks (this.linkedToPanel.getLinkedToPanel ().getSelected ());

            this.viewer.saveObject (this.item,
                                    true);

            this.viewer.fireProjectEvent (ProjectEvent.Type.note,
                                          (this.addMode ? ProjectEvent.Action._new : ProjectEvent.Action.edit),
                                          this.item);


        } catch (Exception ex)
        {

            Environment.logError ("Unable to save/add note: " +
                                  this.item,
                                  ex);

            this.chapter.removeNote (this.item);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty(comments, (this.addMode ? _new : edit), actionerror));

            return false;

        }

/*
TODO Needed?
        try
        {

            Position pos = this.editor.getDocument ().createPosition (this.item.getPosition ());

            this.item.setTextPosition (pos);

            if (this.item.getEndPosition () > -1)
            {

                this.item.setEndTextPosition (this.editor.getDocument ().createPosition (this.item.getEndPosition ()));

            }

        } catch (Exception ex) {

            Environment.logError ("Unable to set text position",
                                  ex);

        }
*/
/*
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
*/
        if (this.item.getChapter () != null)
        {

            editor.requestFocus ();

        }

        // Need to reindex the chapter to ensure that things are in the right order.
        // TODO Needed? this.chapter.reindex ();

        return true;

    }

    @Override
    public QuollPopup createPopup ()
    {

        StringProperty title = null;

        if (!this.addMode)
        {

            title = getUILanguageStringProperty (comments,edit,LanguageStrings.title);

        } else {

            title = getUILanguageStringProperty (comments,_new,LanguageStrings.title);

        }

        QuollPopup p = QuollPopup.builder ()
            .title (title)
            .styleClassName (this.addMode ? StyleClassNames.ADD : StyleClassNames.EDIT)
            .headerIconClassName (StyleClassNames.COMMENT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (AddEditCommentPopup.getPopupIdForComment (this.item))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.getStyleClass ().add (StyleClassNames.COMMENT);

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            this.desc.requestFocus ();

        });

        p.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                           ev ->
        {

            EditorChapterPanel ed = this.viewer.getEditorForChapter (this.chapter);

            if (ed != null)
            {

                ed.getEditor ().removeHighlight (this.highlight);

            }

        });

        return p;

    }

    public static String getPopupIdForComment (Note ci)
    {

        return POPUP_ID + ci.getObjectReference ().asString ();

    }

}
