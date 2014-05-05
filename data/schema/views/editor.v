CREATE VIEW editor_v
AS
SELECT e.dbkey dbkey,
       n.name name,
       e.id id,
       e.shortname shortname,
       e.avatarimage avatarimage,
       e.publickey publickey,
       e.yourpublickey yourpublickey,
       e.yourprivatekey yourprivatekey,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties
FROM   namedobject_v n,
       editor        e
WHERE  e.dbkey = n.dbkey 
