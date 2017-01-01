CREATE VIEW scene_v
AS
SELECT s.dbkey dbkey,
       s.position position,
       s.chapterdbkey chapterdbkey,
       n.name name,
       n.description description,
       n.markup markup,
       n.files files,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.id           id,
       n.version      version,
       n.latest       latest,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v n,
       scene         s
WHERE  s.dbkey = n.dbkey
