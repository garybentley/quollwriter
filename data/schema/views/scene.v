CREATE VIEW scene_v
AS
SELECT s.dbkey dbkey,
       s.position position,
       s.chapterdbkey chapterdbkey,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties
FROM   namedobject_v n,
       scene         s
WHERE  s.dbkey = n.dbkey
