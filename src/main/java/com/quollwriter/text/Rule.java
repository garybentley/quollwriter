package com.quollwriter.text;

import java.util.*;
import javafx.beans.property.*;
//import com.quollwriter.ui.forms.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.*;

import org.dom4j.*;


public interface Rule<E extends TextBlock>
{

    public static final String WORD_CATEGORY = "word";
    public static final String SENTENCE_CATEGORY = "sentence";
    public static final String PARAGRAPH_CATEGORY = "paragraph";

    public String getDescription ();

    public void setDescription (String d);

    public String getDefaultSummary ();

    public String getSummary ();

    public void setSummary (String i);
/*
    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue);
*/

    public List<Issue> getIssues (E block);

    public void init (Element root)
               throws GeneralException;

    public Element getAsElement ();

    public String getId ();

    public void setId (String id);

    public String getCategory ();

    public void setUserRule (boolean u);

    public boolean isUserRule ();

    public String getEditFormTitle (boolean add);

    public com.quollwriter.ui.forms.Form getEditForm (java.awt.event.ActionListener        onSaveComplete,
                             java.awt.event.ActionListener        onCancel,
                             com.quollwriter.ui.AbstractProjectViewer viewer,
                             boolean               add);

    public void updateFromForm ();
    public void updateFromForm2 ();

    public Set<Form.Item> getFormItems2 ();
    public StringProperty getFormError2 ();

}
