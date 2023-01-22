/**
 * Taken from: http://tips4java.wordpress.com/2008/10/27/compound-undo-manager/
 *
 * All credit to: Rob Camick
 */
package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;


public class CompoundUndoManager extends UndoManager implements UndoableEditListener,
                                                                DocumentListener
{


    class MyCompoundEdit extends CompoundEdit
    {
        public boolean isInProgress ()
        {
            // in order for the canUndo() and canRedo() methods to work
            // assume that the compound edit is never in progress

            return false;
        }

        public void undo ()
                   throws CannotUndoException
        {
            // End the edit so future edits don't get absorbed by this edit

            if (compoundEdit != null)
            {
                compoundEdit.end ();
            }

            super.undo ();

            // Always start a new compound edit after an undo

            compoundEdit = null;
        }
    }

    /*
     *      Perform the Undo and update the state of the undo/redo Actions
     */
    class UndoAction extends AbstractAction
    {
        public UndoAction()
        {
            putValue (Action.NAME,
                      "Undo");
            putValue (Action.SHORT_DESCRIPTION,
                      getValue (Action.NAME));
            putValue (Action.MNEMONIC_KEY,
                      Integer.valueOf (KeyEvent.VK_U));
            putValue (Action.ACCELERATOR_KEY,
                      KeyStroke.getKeyStroke ("control Z"));
            setEnabled (false);
        }

        public void actionPerformed (ActionEvent e)
        {

            try
            {
                undoManager.undo ();
                textComponent.requestFocusInWindow ();
            } catch (CannotUndoException ex)
            {
            }

            updateUndoState ();
            redoAction.updateRedoState ();
        }

        private void updateUndoState ()
        {
            setEnabled (undoManager.canUndo ());
        }
    }

    /*
     *      Perform the Redo and update the state of the undo/redo Actions
     */
    class RedoAction extends AbstractAction
    {
        public RedoAction()
        {
            putValue (Action.NAME,
                      "Redo");
            putValue (Action.SHORT_DESCRIPTION,
                      getValue (Action.NAME));
            putValue (Action.MNEMONIC_KEY,
                      Integer.valueOf (KeyEvent.VK_R));
            putValue (Action.ACCELERATOR_KEY,
                      KeyStroke.getKeyStroke (KeyEvent.VK_Y,
                                              InputEvent.CTRL_MASK));
            setEnabled (false);
        }

        public void actionPerformed (ActionEvent e)
        {

            try
            {
                undoManager.redo ();
                textComponent.requestFocusInWindow ();
            } catch (CannotRedoException ex)
            {
            }

            updateRedoState ();
            undoAction.updateUndoState ();
        }

        protected void updateRedoState ()
        {
            setEnabled (undoManager.canRedo ());
        }
    }

    private UndoManager    undoManager;
    private CompoundEdit   compoundEdit;
    private JTextComponent textComponent;
    private UndoAction     undoAction;
    private RedoAction     redoAction;
    private boolean forceCompoundEdit = false;

    private boolean record = false;

    // These fields are used to help determine whether the edit is an
    // incremental edit. The offset and length should increase by 1 for
    // each character added or decrease by 1 for each character removed.

    private int lastOffset;
    private int lastLength;

    public CompoundUndoManager(JTextComponent textComponent)
    {
        this.textComponent = textComponent;
        undoManager = this;
        undoAction = new UndoAction ();
        redoAction = new RedoAction ();
        textComponent.getDocument ().addUndoableEditListener (this);
    }

    /*
     **  Add a DocumentLister before the undo is done so we can position
     **  the Caret correctly as each edit is undone.
     */
    public void undo ()
    {

        textComponent.getDocument ().addDocumentListener (this);
        super.undo ();
        textComponent.getDocument ().removeDocumentListener (this);

    }

    /*
     **  Add a DocumentLister before the redo is done so we can position
     **  the Caret correctly as each edit is redone.
     */
    public void redo ()
    {
        textComponent.getDocument ().addDocumentListener (this);
        super.redo ();
        textComponent.getDocument ().removeDocumentListener (this);
    }

    public void setRecordUndos (boolean v)
    {

        this.record = v;

    }

    public boolean isRecordUndos ()
    {

        return this.record;

    }

    public void endCompoundEdit ()
    {

        this.forceCompoundEdit = false;

        compoundEdit.end ();

        compoundEdit = null;

    }

    public void startCompoundEdit ()
    {

        if (compoundEdit != null)
        {

            compoundEdit.end ();

            compoundEdit = null;

        }

        this.forceCompoundEdit = true;

    }

    /*
     **  Whenever an UndoableEdit happens the edit will either be absorbed
     **  by the current compound edit or a new compound edit will be started
     */
    public void undoableEditHappened (UndoableEditEvent e)
    {

        // Start a new compound edit

        if (!this.record)
        {

            return;

        }

        if (compoundEdit == null)
        {

            compoundEdit = startCompoundEdit (e.getEdit ());

            return;
        }

        if (this.forceCompoundEdit)
        {

            compoundEdit.addEdit (e.getEdit ());

            return;

        }

        // Check for an attribute change
        AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e.getEdit ();

        int offsetChange = textComponent.getCaretPosition () - lastOffset;

        if (event.getType ().equals (DocumentEvent.EventType.CHANGE))
        {

            if (offsetChange == 0)
            {

                compoundEdit.end ();
                compoundEdit = startCompoundEdit (e.getEdit ());

                return;
            }
        }

        // Check for an incremental edit or backspace.
        // The Change in Caret position and Document length should both be
        // either 1 or -1.

//              int offsetChange = textComponent.getCaretPosition() - lastOffset;
//              int lengthChange = textComponent.getDocument().getLength() - lastLength;


        int lengthChange = textComponent.getDocument ().getLength () - lastLength;

        if ((offsetChange == lengthChange) &&
            (Math.abs (offsetChange) == 1))
        {

            String last = "x";

            try
            {

                last = this.textComponent.getText (this.textComponent.getCaretPosition () - 1,
                                                   1);

            } catch (Exception ex) {

                // Ignore.

            }

            // Get the char, if it's white space then end the edit.
            if (Character.isWhitespace (last.charAt (0)))
            {

                compoundEdit.end ();
                compoundEdit = startCompoundEdit (e.getEdit ());

            } else {

                compoundEdit.addEdit (e.getEdit ());

            }

            lastOffset = textComponent.getCaretPosition ();
            lastLength = textComponent.getDocument ().getLength ();

            return;
        }

        // Not incremental edit, end previous edit and start a new one

        compoundEdit.end ();
        compoundEdit = startCompoundEdit (e.getEdit ());
    }

    /*
     **  Each CompoundEdit will store a group of related incremental edits
     **  (ie. each character typed or backspaced is an incremental edit)
     */
    private CompoundEdit startCompoundEdit (UndoableEdit anEdit)
    {
        // Track Caret and Document information of this compound edit

        lastOffset = textComponent.getCaretPosition ();
        lastLength = textComponent.getDocument ().getLength ();

        // The compound edit is used to store incremental edits

        compoundEdit = new MyCompoundEdit ();
        compoundEdit.addEdit (anEdit);

        // The compound edit is added to the UndoManager. All incremental
        // edits stored in the compound edit will be undone/redone at once

        addEdit (compoundEdit);

        undoAction.updateUndoState ();
        redoAction.updateRedoState ();

        return compoundEdit;
    }

    /*
     *  The Action to Undo changes to the Document.
     *  The state of the Action is managed by the CompoundUndoManager
     */
    public Action getUndoAction ()
    {
        return undoAction;
    }

    /*
     *  The Action to Redo changes to the Document.
     *  The state of the Action is managed by the CompoundUndoManager
     */
    public Action getRedoAction ()
    {
        return redoAction;
    }

//
//  Implement DocumentListener
//
    /*
     *  Updates to the Document as a result of Undo/Redo will cause the
     *  Caret to be repositioned
     */
    public void insertUpdate (final DocumentEvent e)
    {
        SwingUtilities.invokeLater (new Runnable ()
            {
                public void run ()
                {
                    int offset = e.getOffset () + e.getLength ();
                    offset = Math.min (offset,
                                       textComponent.getDocument ().getLength ());
                    textComponent.setCaretPosition (offset);
                }
            });
    }

    public void removeUpdate (DocumentEvent e)
    {
        textComponent.setCaretPosition (e.getOffset ());
    }

    public void changedUpdate (DocumentEvent e)
    {
    }

}
