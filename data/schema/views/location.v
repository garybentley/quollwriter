CREATE VIEW location_v
AS
SELECT l.dbkey dbkey,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       l.projectdbkey projectdbkey
FROM   namedobject_v n,
       location      l
WHERE  l.dbkey = n.dbkey
