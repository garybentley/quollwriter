CREATE VIEW editorproject_v
AS
SELECT p.dbkey dbkey,
       n.name name,
       p.id id,
       p.genres genres,
       p.expectations expectations,
       p.wordcounttypelength wordcounttypelength,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v n,
       editorproject p
WHERE  p.dbkey = n.dbkey 
