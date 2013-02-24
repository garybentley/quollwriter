About
=====

Quoll Writer is a desktop writing application, written in Java that is designed to allow you to focus on writing and your words.
It removes standard word processing conventions such as margins, indents and formatting in favor of a simple interface that keeps
you focussed on writing and improving your writing skills.

See the website for more details: http://quollwriter.com


License
=======

The Quoll Writer code, that is everything under the **src** directory, is licensed under an Apache 2.0 license.
The license is included in the repository and also can be found at: http://www.apache.org/licenses/LICENSE-2.0.html


Requirements
============

At least Java 1.6 is required to run Quoll Writer.

Ant is required to execute the build file.  


Running
=======

To run Quoll Writer either use the default task or the command:

    ant run

Note: the ability to create a Windows installer is not

Directories/files
=================

The following is a brief outline of the major directories and files in the repository.

* `data` - directory containing the data files used by Quoll Writer such as properties and definitions.

* `data/dictionaries` - directory containing the dictionary and thesaurus files.

* `data/prompts` - directory containing the writing prompts.

* `data/schema` - directory containing the schema definition files including the upgrade scripts and view definitions.

* `imgs` - directory containing the images used throughout the application.  Quoll Writer expects to use .png files for icons.

* `imgs/bgs` - directory containing the background image files used in the Idea Board and full screen mode, generally these are jpgs.

* `src` - directory where the java source code resides.

* `src/com/quollwriter` - directory containing general classes.  The `Startup` class has the main method and is called to start things up.
It then passes responsibility onto the static method `Environment.init` which then decides what should be done (either start the last project
or show the find/open window.  `Environment` is the central hub controlling and tracking the various project viewer instances.  It keeps
track of which projects are open, it also handles the user and system properties.

* `src/com/quollwriter/ui` - directory containing the UI classes.  `AbstractProjectViewer` is the main class to be starting with.

* `src/com/quollwriter/db` - directory containing the database classes.  `ObjectManager` is the class that handles most of the db
interactions.  The various \*Handler classes handle object specific actions, i.e. the `ChapterDataHandler` handles the columns and actions
specific to the `com.quollwriter.data.Chapter` class.  Each `AbstractProjectViewer` has a reference to an `ObjectManager` instance that
manages a `Project`.

* `src/com/quollwriter/data` - directory containing the data model classes.  `Project` represents the "top-level" object used by the
`AbstractProjectViewer`.  The project is basically a tree of objects, with lists of `Book`s, `QCharacter`s, `QObject`'s, `ResearchItem`'s and
`IdeaType` within the `Project` instance.  Each `Book` then contains a list of `Chapter`'s.  `Chapter` then can contain `Scene`'s and `OutlineItem`.
Each of these objects extends from `NamedObject` which in turn extends from `DataObject`.  `Note`'s can be attached to any `NamedObject`.
Note: eventually I'll convert the List instances to Sets.


DB/Schema
=========

The project information is held in a H2 database backed by a file store.  By default project files are written to a sub-direcory of the
*projects* directory in the users home directory, gained via `System.getProperty ("user.dir")`.  The project name is used as a sub-directory
within the projects directory.  The user can specify the location where the project is stored, however the project name is still
used as a sub-directory name.

For instance on Windows 7 using the default settings and a project name of "My New Project" the H2 db files would be stored in:

C:/Users/Me/QuollWriter/projects/my new project

The schema version is tracked using a property stored within the project itself.  File: `data/schema-version.txt` contains the
current version of the schema.  When an existing project is opened the two versions are compared and any upgrade files executed.
Upgrade files are stored in: `data/schema/update-scripts`.  They are named X-Y.xml, where X is the from version and Y is to version.
Thus if a project is currently at schema version 2 and the current version of Quoll Writer is using version 7 then files: 2-3.xml,
3-4.xml, 4-5.xml, 6-7.xml will be executed against the schema before the project is opened (if the files exist).

When a new project is created file: 0-1.xml is run.

After an upgrade `data/schema/update-scripts/create-views.xml` is always run to update the views.

The files defining the views used are held in: `data/schema/views`.

Windows Installer
=================

Linux/Mac
=========
