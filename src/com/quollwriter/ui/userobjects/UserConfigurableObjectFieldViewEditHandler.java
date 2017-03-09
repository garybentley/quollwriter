package com.quollwriter.ui.userobjects;

import java.awt.event.*;
import java.util.Set;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.forms.*;

public interface UserConfigurableObjectFieldViewEditHandler<T extends UserConfigurableObjectTypeField, E>
{
        
    /**
     * Get the form items that should be used for getting the input for the associated object field.
     *
     * @param initValue The value that should be used to init the field if there is no object field
     *                  associated with the handler.
     * @param formSave An action that can be called to save the "form" associated with this item.
     *                 This would be typically called when the used presses "return" in a single line text field
     *                 or "ctrl+return" in a multi-line text area.
     * @return The set of items that will be used to collect the input for the object field.
     */
    public Set<FormItem> getInputFormItems (String                 initValue,
                                            ActionListener         formSave);
    
    /**
     * Grab focus on the relevant input form item.
     */
    public void grabInputFocus ();
    
    public Set<String> getInputFormItemErrors ();
    
    public void updateFieldFromInput ()
                               throws GeneralException;
    
    public E getInputSaveValue ()
                         throws GeneralException;
    
    public Set<FormItem> getViewFormItems ();
    
    public E getFieldValue ();
    
    public T getTypeField ();
    
    public UserConfigurableObjectField getField ();
    
    /**
     * Convert the object type to a string value that is suitable for saving to the db.
     *
     * Strictly speaking this is not the right place for this method but it will do for now.
     *
     * @param E the type to convert to a string.
     * @return The string representation of the value, suitable for saving to the db.
     */
    public String valueToString (E val)
                          throws GeneralException;
    
    /**
     * Conver the string to a real object type that is used by the field.
     * 
     * Strictly speaking this is not the right place for this method but it will do for now.
     *
     * @param s The string to convert to an instance of E.
     * @return The real object type created from the data in s.
     */
    public E stringToValue (String s)
                     throws GeneralException;

}