CREATE VIEW outlineitem_v
AS
SELECT o.dbkey dbkey,
       o.position position,
       o.chapterdbkey chapterdbkey,
       o.scenedbkey scenedbkey,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties
FROM   namedobject_v n,
       outlineitem   o
WHERE  o.dbkey = n.dbkey
