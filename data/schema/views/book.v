CREATE VIEW book_v
AS
SELECT b.dbkey dbkey,
       n.name name,
       n.description description,
       n.markup markup,
       n.files  files,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       b.projectdbkey projectdbkey,
       n.id           id,
       n.version      version,
       n.latest       latest,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v n,
       book          b
WHERE  b.dbkey = n.dbkey
