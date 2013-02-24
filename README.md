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

Windows Installer
=================

Linux/Mac
=========
