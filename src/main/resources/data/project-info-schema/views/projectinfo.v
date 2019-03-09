CREATE VIEW projectinfo_v
AS
SELECT p.dbkey dbkey,
       p.lastedited lastedited,
       p.directory directory,
       p.backupdirectory backupdirectory,
       p.status status,
       p.statistics statistics,
       p.type type,
       p.foreditor foreditor,
       p.encrypted encrypted,
       p.nocredentials nocredentials,
       p.icon icon,
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
       projectinfo   p
WHERE  p.dbkey = n.dbkey 
