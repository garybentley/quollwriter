CREATE VIEW project_v
AS
SELECT p.dbkey dbkey,
       p.lastedited lastedited,
       n.name name,
       p.type type,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       p.schema_version schema_version,
       n.id           id,
       n.version      version,
       n.latest       latest       
FROM   namedobject_v n,
       project       p
WHERE  p.dbkey = n.dbkey 
