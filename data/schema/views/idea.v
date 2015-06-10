CREATE VIEW idea_v
AS
SELECT i.dbkey dbkey,
       n.description description,
       i.rating rating,
       i.ideatypedbkey ideatypedbkey,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.id           id,
       n.version      version,
       n.latest       latest       
FROM   namedobject_v n,
       idea      i
WHERE  i.dbkey = n.dbkey
