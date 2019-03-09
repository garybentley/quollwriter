CREATE VIEW projectversion_v
AS
SELECT pv.dbkey       dbkey,
       pv.due         due,
       n.name         name,
       n.description  description,
       n.markup       markup,
       n.lastmodified lastmodified,
       n.objecttype   objecttype,
       n.datecreated  datecreated,
       n.properties   properties,
       n.id           id,
       n.version      version,
       n.latest       latest       
FROM   namedobject_v  n,
       projectversion pv
WHERE  pv.dbkey       = n.dbkey
