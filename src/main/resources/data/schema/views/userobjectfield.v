CREATE VIEW userobjectfield_v
AS
SELECT o.dbkey dbkey,
       o.namedobjectdbkey namedobjectdbkey,
       o.userobjecttypefielddbkey userobjecttypefielddbkey,
       o.value value,
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
FROM   namedobject_v   n,
       userobjectfield o
WHERE  o.dbkey = n.dbkey 
