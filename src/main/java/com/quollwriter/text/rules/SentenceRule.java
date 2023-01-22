package com.quollwriter.text.rules;

import java.util.*;

import com.quollwriter.text.*;

public interface SentenceRule extends Rule<Sentence>
{

    public List<Issue> getIssues (Sentence sentence);
    
}
