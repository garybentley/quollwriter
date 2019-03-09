CREATE VIEW idea_v
AS
SELECT i.dbkey dbkey,
       n.description description,
       n.markup markup,
       n.files  files,
       i.rating rating,
       i.ideatypedbkey ideatypedbkey,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.id           id,
       n.version      version,
       n.latest       latest,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v n,
       idea      i
WHERE  i.dbkey = n.dbkey
