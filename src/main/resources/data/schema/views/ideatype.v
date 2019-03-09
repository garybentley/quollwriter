CREATE VIEW ideatype_v
AS
SELECT i.dbkey dbkey,
       n.name name,
       n.description description,
       n.markup markup,
       n.files  files,
       i.sortby sortby,
       i.icontype icontype,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.id           id,
       n.version      version,
       n.latest       latest,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v n,
       ideatype      i
WHERE  i.dbkey = n.dbkey
