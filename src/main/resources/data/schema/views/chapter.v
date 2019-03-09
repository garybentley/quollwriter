CREATE VIEW chapter_v
AS
SELECT c.dbkey        dbkey,
       c.bookdbkey    bookdbkey,
       c.text         text,
       c.markup       markup,       
       c.index        index,
       c.goals        goals,
       c.goalsmarkup  goalsmarkup,
       c.plan         plan,
       c.planmarkup   planmarkup,
       c.editposition editposition,
       c.editcomplete editcomplete,
       c.projectversiondbkey projectversiondbkey,
       n.name         name,
       n.description  description,
       n.markup       descriptionmarkup,
       n.files        files,
       n.lastmodified lastmodified,
       n.objecttype   objecttype,
       n.datecreated  datecreated,
       n.properties   properties,
       n.id           id,
       n.version      version,
       n.latest       latest,
       n.userobjecttypedbkey userobjecttypedbkey
FROM   namedobject_v  n,
       chapter        c
WHERE  c.dbkey        = n.dbkey
