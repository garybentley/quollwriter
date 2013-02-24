CREATE VIEW ideatype_v
AS
SELECT i.dbkey dbkey,
       n.name name,
       n.description description,
       i.sortby sortby,
       i.icontype icontype,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties
FROM   namedobject_v n,
       ideatype      i
WHERE  i.dbkey = n.dbkey
