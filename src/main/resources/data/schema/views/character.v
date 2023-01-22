CREATE VIEW character_v
AS
SELECT c.dbkey dbkey,
       c.aliases aliases,
       n.name name,
       n.description description,
       n.markup markup,
       n.files  files,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       c.projectdbkey projectdbkey,
       n.id           id,
       n.version      version,
       n.latest       latest,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v n,
       character     c
WHERE  c.dbkey = n.dbkey
