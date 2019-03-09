package com.quollwriter.text;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import com.gentlyweb.xml.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;

import org.jdom.*;


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
               throws JDOMException;

    public Element getAsElement ();

    public String getId ();

    public void setId (String id);

    public String getCategory ();

    public void setUserRule (boolean u);

    public boolean isUserRule ();

    public String getEditFormTitle (boolean add);

    public Form getEditForm (ActionListener        onSaveComplete,
                             ActionListener        onCancel,
                             AbstractProjectViewer viewer,
                             boolean               add);

    public void updateFromForm ();

}
