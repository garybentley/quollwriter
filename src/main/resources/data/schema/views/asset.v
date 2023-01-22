CREATE VIEW asset_v
AS
SELECT o.dbkey dbkey,
       n.userobjecttypedbkey userobjecttypedbkey,
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
       n.latest       latest       
FROM   namedobject_v n,
       asset         o
WHERE  o.dbkey = n.dbkey 
