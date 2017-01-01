CREATE VIEW note_v
AS
SELECT n.dbkey dbkey,
       n.position position,
       n.end_position end_position,
       n.objectdbkey objectdbkey,
       n.objectversion objectversion,
       n.type type,
       n.due due,
       n.dealtwith dealtwith,
       nn.name name,
       nn.description description,
       nn.markup markup,
       nn.files  files,
       nn.lastmodified lastmodified,
       nn.objecttype objecttype,
       nn.datecreated datecreated,
       nn.properties  properties,
       nn.id           id,
       nn.version      version,
       nn.latest       latest,
       nn.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v nn,
       note          n
WHERE  n.dbkey = nn.dbkey
