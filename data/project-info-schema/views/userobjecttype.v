CREATE VIEW userobjecttype_v
AS
SELECT o.dbkey dbkey,
       n.name name,
       o.userobjtype userobjtype,
       o.pluralname pluralname,
       o.icon24x24 icon24x24,
       o.icon16x16 icon16x16,
       o.layout layout,
       o.createshortcutkey createshortcutkey,
       o.assetobjtype assetobjtype,
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
FROM   namedobject_v  n,
       userobjecttype o
WHERE  o.dbkey = n.dbkey 

