CREATE VIEW tag_v
AS
SELECT p.dbkey dbkey,
       n.name name,
       n.description description,
       n.markup markup,
       n.files files,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.id           id,
       n.version      version,
       n.latest       latest       
FROM   namedobject_v n,
       tag           p
WHERE  p.dbkey = n.dbkey 
