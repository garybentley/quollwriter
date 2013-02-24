About
=====

Quoll Writer is a desktop writing application, written in Java that is designed to allow you to focus on writing and your words.
It removes standard word processing conventions such as margins, indents and formatting in favor of a simple interface that keeps
you focussed on writing and improving your writing skills.

See the website for more details: http://quollwriter.com


License
=======

The Quoll Writer code, that is everything under the **src**, **data** directories, are licensed under an Apache 2.0 license.  An
exception are the files under the `data/dictionaries` directory which have their own licensing.  Please see the relevant readme files
in the sub-directories for details.
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

The files for creating the Windows installer are not included in this repository.  

For the binary release via the website, Inno Setup (http://www.jrsoftware.org/isinfo.php ) and WinRun4J (http://winrun4j.sourceforge.net/ )
are used.


Linux/Mac
=========

I would dearly like to create versions for Linux/Mac but it won't happen for a while, it may not happen ever I'm afraid.

Supporting multiple platforms, contrary to what some people believe, is a soul destroying and tiresome task.

Some of the issues ahead are:

1. Look-n-feel implementations for Java on Linux are pretty poor and it's difficult to get a nice one that would be compatible with the
existing code.  You can't just switch look-n-feel and hope for the best, especially not with the visual style Quoll Writer uses (I know
I've tried).

2. Mac has a couple of nice look-n-feels (for example Quaqua), however getting support across the various flavors of Mac is difficult.  Also the Mac platform
is positively hostile towards tabbed interfaces.  The Mac UI guide recommends them only for configuration.  However Quoll Writer
currently makes heavy use of tabs.  The reliance on tabs can be removed but it is quite a lot of work to do and time is against me
at the moment.

3. No menu bar.  I imagine Mac users may have a fit about this one, also certain Linux distributions (Ubuntu for example) expect
an application to have a menu bar.  I understand this but my own opinion is that menu bars need to die off from applications.  Mobile
apps don't use them and most modern browsers don't either.  They are an information hiding anacronism that rely on memorization for
their functionality.  This prevent you using an application not facilitate it, don't believe me?  Why do you think Microsoft invented
the ribbon?

4. Mouse triggers.  Different platforms have different buttons that trigger different events.  This is a complete break of the Java
"write once, run anywhere" promise and Swing does not help you out with it.  You can encapsulate yourself from it somewhat but there
is always something you miss.  Personally I don't have time to try and sort that out across 3 platforms.

5. Macs are hostile to Java.  I'm not convinced, now that Oracle are at the helm, that Java will be present on Macs for too much longer.
The iOS/Mac ecosystems are becoming more closed down and controlled so there is little incentive for me to put effort into something
that will probably disappear soon.

6. Linux users tend to be Java averse on their desktops.  Sorry guys but I hear too much "Java is slow", "Java is crap", "Java is pants compared to X".
I personally don't find it to be true but I try hard not to write slow software.

7. Mac users expect their software to have a similar look to their other software.  Quoll Writer is very different to standard apps
you see on a Mac and I won't be changing it to fit in with how Macs work.