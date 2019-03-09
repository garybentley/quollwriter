CREATE VIEW location_v
AS
SELECT l.dbkey dbkey,
       n.name name,
       n.userobjecttypedbkey userobjecttypedbkey,
       n.description description,
       n.markup markup,
       n.files  files,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       l.projectdbkey projectdbkey,
       n.id           id,
       n.version      version,
       n.latest       latest       
FROM   namedobject_v n,
       location      l
WHERE  l.dbkey = n.dbkey
