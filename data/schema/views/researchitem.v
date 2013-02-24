CREATE VIEW researchitem_v
AS
SELECT r.dbkey dbkey,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       r.projectdbkey projectdbkey,
       r.url url
FROM   namedobject_v n,
       researchitem  r
WHERE  r.dbkey = n.dbkey
