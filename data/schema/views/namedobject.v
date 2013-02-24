CREATE VIEW namedobject_v
AS
SELECT n.dbkey        dbkey,
       n.name         name,
       n.description  description,
       n.lastmodified lastmodified,
       d.objecttype   objecttype,
       d.datecreated  datecreated,
       d.properties   properties
FROM   namedobject n,
       dataobject  d
WHERE  d.dbkey = n.dbkey
