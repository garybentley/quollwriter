CREATE VIEW note_v
AS
SELECT n.dbkey dbkey,
       n.position position,
       n.end_position end_position,
       n.objectdbkey objectdbkey,
       n.type type,
       n.due due,
       nn.name name,
       nn.description description,
       nn.lastmodified lastmodified,
       nn.objecttype objecttype,
       nn.datecreated datecreated,
       nn.properties  properties
FROM   namedobject_v nn,
       note          n
WHERE  n.dbkey = nn.dbkey
