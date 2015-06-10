CREATE VIEW projecteditor_v
AS
SELECT p.dbkey dbkey,
       p.editordbkey editordbkey,
       p.forprojectid forprojectid,
       p.forprojectname forprojectname,
       p.status status,
       p.statusmessage statusmessage,
       p.current current,
       p.editorfrom editorfrom,
       p.editorto editorto,
       d.objecttype objecttype,
       d.datecreated datecreated,
       d.properties  properties,
       d.id           id,
       d.version      version,
       d.latest       latest
FROM   dataobject    d,
       projecteditor p
WHERE  p.dbkey = d.dbkey 
