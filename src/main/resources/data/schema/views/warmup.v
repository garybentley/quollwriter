CREATE VIEW warmup_v
AS
SELECT w.dbkey dbkey,
       w.chapterdbkey chapterdbkey,
       w.promptid     promptid,
       n.name         name,
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
       warmup        w
WHERE  w.dbkey = n.dbkey
