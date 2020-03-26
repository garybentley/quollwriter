CREATE VIEW userobjecttypefield_v
AS
SELECT o.dbkey dbkey,
       o.userobjecttypedbkey userobjecttypedbkey,
       n.name name,
       o.type type,
       o.definition definition,
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
FROM   namedobject_v       n,
       userobjecttypefield o
WHERE  o.dbkey = n.dbkey
