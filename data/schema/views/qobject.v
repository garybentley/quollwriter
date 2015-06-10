CREATE VIEW qobject_v
AS
SELECT o.dbkey dbkey,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       o.projectdbkey projectdbkey,
       o.type type,
       n.id           id,
       n.version      version,
       n.latest       latest       
FROM   namedobject_v n,
       qobject       o
WHERE  o.dbkey = n.dbkey
