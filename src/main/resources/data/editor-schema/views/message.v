CREATE VIEW message_v
AS
SELECT m.dbkey dbkey,
       m.when when,
       m.type type,
       m.sentbyme sentbyme,
       m.forprojectid forprojectid,
       m.origmessage origmessage,
       m.message message,
       m.dealtwith dealtwith,
       m.messageid messageid,
       d.datecreated datecreated,
       d.objecttype objecttype,
       d.id id,
       d.properties properties,
       m.editordbkey editordbkey
FROM   dataobject d,
       message    m
WHERE  m.dbkey = d.dbkey
