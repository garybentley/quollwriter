package com.quollwriter.text.rules;

import java.util.*;

import com.quollwriter.text.*;

public interface ParagraphRule extends Rule<Paragraph>
{

    public List<Issue> getIssues (Paragraph paragraph);

}
