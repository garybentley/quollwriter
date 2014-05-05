package com.quollwriter.data.editors;

public class EditorAccount
{
    
    private String email = null;
    private String pwd = null; 
    
    private EditorAuthor author = null;
    private EditorEditor editor = null; 
    
    public EditorAccount ()
    {
        
        
    }
    
    public void setEditor (EditorEditor e)
    {
        
        this.editor = e;
        
    }
    
    public EditorEditor getEditor ()
    {
        
        return this.editor;
        
    }
    
    public EditorAuthor getAuthor ()
    {
        
        return this.author;
        
    }
    
    public void setAuthor (EditorAuthor a)
    {
        
        this.author = a;
        
    }
    
    public void setPassword (String p)
    {
        
        this.pwd = p;
        
    }
    
    public String getPassword ()
    {
        
        return this.pwd;
        
    }
    
    public String getEmail ()
    {
        
        return this.email;
        
    }
    
    public void setEmail (String em)
    {
        
        this.email = em;
        
    }
    
}