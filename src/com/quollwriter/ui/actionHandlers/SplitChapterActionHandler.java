package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.text.*;

public class SplitChapterActionHandler extends AbstractFormPopup<ProjectViewer, Chapter>
{

    private TextFormItem  nameField = null;
    private Chapter       addFrom = null;

    public SplitChapterActionHandler (Chapter       addFrom,
                                      ProjectViewer pv)
    {

        super (new Chapter (addFrom.getBook (),
                            null),
               pv,
               AbstractFormPopup.ADD);

        this.addFrom = addFrom;

        this.nameField = new TextFormItem (Environment.getUIString (LanguageStrings.project,
                                                                    LanguageStrings.actions,
                                                                    LanguageStrings.splitchapter,
                                                                    LanguageStrings.labels,
                                                                    LanguageStrings.newchaptername),
                                           //"New {Chapter} Name",
                                           null);

        final SplitChapterActionHandler _this = this;

        this.nameField.setDoOnReturnPressed (this.getSaveAction ());

    }

    @Override
    public void handleCancel ()
    {

        // Nothing to do.

    }

    @Override
    public Set<String> getFormErrors ()
    {

        Set<String> errs = new LinkedHashSet ();

        if (this.nameField.getValue () == null)
        {

            errs.add (Environment.getUIString (LanguageStrings.project,
                                               LanguageStrings.actions,
                                               LanguageStrings.splitchapter,
                                               LanguageStrings.errorlabel));
                            //"Please select a {chapter} name.");

        }

        return errs;

    }

    @Override
    public boolean handleSave ()
    {

        String n = this.nameField.getValue ();

        try
        {

            QuollEditorPanel panel = this.viewer.getEditorForChapter (this.addFrom);

            QTextEditor ed = panel.getEditor ();

            int start = ed.getSelectionStart ();
            int end = ed.getSelectionEnd ();

            if (start == end)
            {

                end = ed.getText ().length ();

            }

            int shiftBy = -1 * start;

            Chapter c = this.addFrom.getBook ().createChapterAfter (this.addFrom,
                                                                    n);

            this.object = c;

            StringWithMarkup edT = ed.getTextWithMarkup ();

            String newText = edT.getText ().substring (start,
                                                       end);

            // Get the markup and shift
            Markup newM = new Markup (edT.getMarkup (),
                                      start,
                                      end);
            newM.shiftBy (shiftBy);

            c.setText (new StringWithMarkup (newText,
                                             newM));

            this.viewer.saveObject (c,
                                    true);

            List toSave = new ArrayList ();

            // Handle notes, scenes, outline items.
            Set<ChapterItem> its = this.addFrom.getChapterItemsWithPositionBetween (start,
                                                                                    end);

            for (ChapterItem it : its)
            {

                // Null out the standard Position objects and set the underlying start/end positions.
                it.shiftPositionBy (shiftBy);

                // Set the chapter.
                it.setChapter (c);

                // Save the item.
                toSave.add (it);

            }

            this.viewer.saveObjects (toSave,
                                     true);

            ed.removeText (start,
                           end - start);

            this.viewer.fireProjectEvent (this.object.getObjectType (),
                                          ProjectEvent.NEW,
                                          this.object);

            // Save the chapter.
            panel.saveObject ();

            // Reload existing chapter?
            panel.reinitIconColumn ();

            this.viewer.reloadChapterTree ();

            // Open the new chapter.
            this.viewer.editChapter (c);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add new chapter with name: " +
                                  this.nameField.getText (),
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.project,
                                                               LanguageStrings.actions,
                                                               LanguageStrings.splitchapter,
                                                               LanguageStrings.actionerror));
                                      //"An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }
/*
        Chapter nc = (Chapter) this.dataObject;

        try
        {

            this.projectViewer.editChapter (nc);

            this.projectViewer.addChapterToTreeAfter (nc,
                                                      this.addFrom);

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  nc,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to edit " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }
*/
        return true;

    }

    private String getSelectedText ()
    {

        QTextEditor ed = this.viewer.getEditorForChapter (this.addFrom).getEditor ();

        int start = ed.getSelectionStart ();
        int end = ed.getSelectionEnd ();

        if (start == end)
        {

            end = ed.getText ().length ();

        }

        return ed.getText ().substring (start,
                                        end);

    }

    @Override
    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.project,
                                        LanguageStrings.actions,
                                        LanguageStrings.splitchapter,
                                        LanguageStrings.title);

    }

    @Override
    public Icon getIcon (int iconSizeType)
    {

        return Environment.getIcon (Chapter.OBJECT_TYPE + "-split",
                                    iconSizeType);

    }

    @Override
    public Set<FormItem> getFormItems (String selectedText)
    {

        Set<FormItem> items = new LinkedHashSet ();

        items.add (this.nameField);

        String text = this.getSelectedText ();

        Paragraph para = new Paragraph (text,
                                        0);

        //SentenceIterator iter = new SentenceIterator (text);

        int count = para.getWordCount ();

        //int count = UIUtils.getWordCount (text);

        // Get the first sentence.
        text = para.getFirstSentence ().getText ();

        //text = iter.next ();

        if (text != null)
        {

            text = text.trim ();

            if (text.length () > 150)
            {

                text = text.trim ().substring (0, 150) + Environment.getUIString (LanguageStrings.project,
                                                                                  LanguageStrings.actions,
                                                                                  LanguageStrings.splitchapter,
                                                                                  LanguageStrings.moretextindicator);
                //"...";

            }

            JTextArea t = new JTextArea (text);

            t.setLineWrap (true);
            t.setWrapStyleWord (true);
            t.setOpaque (false);
            t.setBorder (null);
            t.setEditable (false);
            t.setSize (new Dimension (300, 300));

            Box p = new Box (BoxLayout.X_AXIS);
            p.add (t);
            p.setOpaque (false);

            items.add (new AnyFormItem (Environment.getUIString (LanguageStrings.project,
                                                                 LanguageStrings.actions,
                                                                 LanguageStrings.splitchapter,
                                                                 LanguageStrings.labels,
                                                                 LanguageStrings.startat),
                                        //"Start at",
                                        p));

            QTextEditor ed = this.viewer.getEditorForChapter (this.addFrom).getEditor ();

            int start = ed.getSelectionStart ();
            int end = ed.getSelectionEnd ();

            text = para.getLastSentence ().getText ();//iter.last ();

            if ((end > start)
                &&
                (text != null)
               )
            {

                text = text.trim ();

                if (text.length () > 150)
                {

                    text = "... " + text.trim ().substring (text.length () - 150);

                }

                JTextArea et = new JTextArea (text);

                et.setLineWrap (true);
                et.setWrapStyleWord (true);
                et.setOpaque (false);
                et.setBorder (null);
                et.setEditable (false);
                et.setSize (new Dimension (300, 300));

                Box ep = new Box (BoxLayout.X_AXIS);
                ep.add (et);
                ep.setOpaque (false);

                items.add (new AnyFormItem (Environment.getUIString (LanguageStrings.project,
                                                                     LanguageStrings.actions,
                                                                     LanguageStrings.splitchapter,
                                                                     LanguageStrings.labels,
                                                                     LanguageStrings.endat),
                                            //"End at",
                                            ep));

            }

            items.add (new AnyFormItem (Environment.getUIString (LanguageStrings.project,
                                                                 LanguageStrings.actions,
                                                                 LanguageStrings.splitchapter,
                                                                 LanguageStrings.labels,
                                                                 LanguageStrings.words),
                                        //"Words",
                                        UIUtils.createLabel (Environment.formatNumber (count))));

        }

        if (text != null)
        {

        }

        return items;

    }

    @Override
    public JComponent getFocussedField ()
    {

        return this.nameField;

    }

}
