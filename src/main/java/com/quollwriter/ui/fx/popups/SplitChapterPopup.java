package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import com.quollwriter.*;
import com.quollwriter.data.Chapter;
import com.quollwriter.data.ChapterItem;
import com.quollwriter.text.Paragraph;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class SplitChapterPopup extends PopupContent<ProjectViewer>
{

    public static final String POPUP_ID = "splitchapter";

    private QuollTextField name = null;
    private Chapter toSplit = null;
    private IndexRange range = null;

    public SplitChapterPopup (ProjectViewer viewer,
                              Chapter       ch)
    {

        super (viewer);

        this.toSplit = ch;

        this.name = QuollTextField.builder ()
            .build ();

        Form.Builder f = Form.builder ()
            .layoutType (Form.LayoutType.stacked)
            .confirmButton (getUILanguageStringProperty (buttons,save))
            .cancelButton (getUILanguageStringProperty (buttons,cancel))
            .item (getUILanguageStringProperty (project,actions,splitchapter,labels,newchaptername),
                   this.name);

        TextEditor ed = this.viewer.getEditorForChapter (this.toSplit).getEditor ();
        IndexRange r = ed.getSelection ();

        this.range = r;

       String text = this.getSelectedText ();

       Paragraph para = new Paragraph (text,
                                       0);

       int count = para.getWordCount ();

       // Get the first sentence.
       text = para.getFirstSentence ().getText ();

       if (text != null)
       {

           text = text.trim ();

           if (text.length () > 150)
           {

               text = text.trim ().substring (0, 150) + getUILanguageStringProperty (project,actions,splitchapter,moretextindicator);
               //"...";

           }

           QuollTextView st = QuollTextView.builder ()
            .text (text)
            .build ();

           f = f.item (getUILanguageStringProperty (project,actions,splitchapter,labels,startat),
                       st);

           int start = r.getStart ();
           int end = r.getEnd ();

           text = para.getLastSentence ().getText ();

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

               QuollTextView et = QuollTextView.builder ()
                .text (text)
                .build ();

               f = f.item (getUILanguageStringProperty (project,actions,splitchapter,labels,endat),
                           et);

           }

           f = f.item (getUILanguageStringProperty (project,actions,splitchapter,labels,words),
                       QuollLabel.builder ()
                        .label (Environment.formatNumber (count))
                        .build ());

       }

       Form form = f.build ();

       form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                             ev ->
       {

           this.close ();

       });

       form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                             ev ->
       {

           String n = this.name.getText ();

           if (n != null)
           {

               n = n.trim ();

           }

           if (n.length () == 0)
           {

               form.showError (getUILanguageStringProperty (project,actions,splitchapter,errors,novalue));

               return;

           }

           Set<Chapter> cs = this.toSplit.getBook ().getAllChaptersWithName (n);

           if (cs.size () > 0)
           {

               form.showError (getUILanguageStringProperty (project,actions,renamechapter,errors,valueexists));
               //"Another {chapter} with that name already exists.";

               return;

           }

           if (this.handleSave ())
           {

               this.close ();

           }

       });

       this.getChildren ().add (form);

    }

    public static String getPopupIdForChapter (Chapter c)
    {

        return POPUP_ID + c.getObjectReference ().asString ();

    }

    private String getSelectedText ()
    {

        TextEditor ed = this.viewer.getEditorForChapter (this.toSplit).getEditor ();

        IndexRange r = ed.getSelection ();

        int start = r.getStart ();
        int end = r.getEnd ();

        if (start == end)
        {

            end = ed.getText ().length ();

        }

        return ed.getText ().substring (start,
                                        end);

    }

    public boolean handleSave ()
    {

        String n = this.name.getText ();

        try
        {

            ChapterEditorPanelContent panel = this.viewer.getEditorForChapter (this.toSplit);

            TextEditor ed = panel.getEditor ();

            int start = this.range.getStart ();
            int end = this.range.getEnd ();

            if (start == end)
            {

                end = ed.getText ().length ();

            }

            int shiftBy = -1 * start;

            Chapter newc = this.toSplit.getBook ().createChapterAfter (this.toSplit,
                                                                       n);

            //this.object = c;

            StringWithMarkup edT = ed.getTextWithMarkup ();

            String newText = edT.getText ().substring (start,
                                                       end);

            // Get the markup and shift
            Markup newM = new Markup (edT.getMarkup (),
                                      start,
                                      end);
            newM.shiftBy (shiftBy);

            newc.setText (new StringWithMarkup (newText,
                                                newM));

            this.viewer.saveObject (newc,
                                    true);

            List toSave = new ArrayList ();

            // Handle notes, scenes, outline items.
            Set<ChapterItem> its = this.toSplit.getChapterItemsWithPositionBetween (start,
                                                                                    end);

            for (ChapterItem it : its)
            {

                // Null out the standard Position objects and set the underlying start/end positions.
                // Unbind the positions.
                it.positionProperty ().unbind ();
                it.endPositionProperty ().unbind ();
                it.shiftPositionBy (shiftBy);

                // Set the chapter.
                it.setChapter (newc);

                // Save the item.
                toSave.add (it);

            }

            this.viewer.saveObjects (toSave,
                                     true);

            ed.replaceText (start,
                            end,
                            "");

            this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                          ProjectEvent.Action._new,
                                          newc);

            // Save the chapter.
            panel.saveObject ();

            // Reload existing chapter?
            //panel.reinitIconColumn ();

            //this.viewer.reloadChapterTree ();

            // Open the new chapter.
            this.viewer.editChapter (newc);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add new chapter with name: " +
                                  this.name.getText (),
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.project,
                                                               LanguageStrings.actions,
                                                               LanguageStrings.splitchapter,
                                                               LanguageStrings.actionerror));
                                      //"An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }

        return true;

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (project,actions,splitchapter,title)
            .styleClassName (StyleClassNames.SPLITCHAPTER)
            .headerIconClassName (StyleClassNames.SPLIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        return p;

    }

}
