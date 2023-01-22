CREATE VIEW editor_v
AS
SELECT e.dbkey dbkey,
       n.name name,
       e.email email,
       e.status status,
       e.mynameforeditor mynameforeditor,
       e.avatarimage avatarimage,
       e.myavatarimageforeditor myavatarimageforeditor,
       e.theirpublickey theirpublickey,
       e.invitedbyme invitedbyme,
       e.messagingusername messagingusername,
       e.servicename servicename,
       n.description description,
       n.lastmodified lastmodified,
       n.objecttype objecttype,
       n.datecreated datecreated,
       n.properties  properties,
       n.id           id,
       n.version      version,
       n.latest       latest
FROM   namedobject_v n,
       editor        e
WHERE  e.dbkey = n.dbkey 
