CREATE VIEW chapter_v
AS
SELECT c.dbkey dbkey,
       c.bookdbkey bookdbkey,
       c.text      text,
       c.index     index,
       c.goals     goals,
       c.markup    markup,
       c.plan      plan,
       n.name name,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties
FROM   namedobject_v n,
       chapter       c
WHERE  c.dbkey = n.dbkey
