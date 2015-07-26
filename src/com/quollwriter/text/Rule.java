package com.quollwriter.text;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import com.gentlyweb.xml.*;

import com.quollwriter.ui.components.*;

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

    public boolean isUserRule ();

    public String getCreateType ();

    public Form getEditForm (ActionListener onSaveComplete,
                             boolean        add);
                                 
    public void updateFromForm ();
    
}
