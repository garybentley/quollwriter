CREATE VIEW book_v
AS
SELECT b.dbkey dbkey,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       b.projectdbkey projectdbkey
FROM   namedobject_v n,
       book          b
WHERE  b.dbkey = n.dbkey
