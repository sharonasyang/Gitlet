# Gitlet Design Document
author: Sharona Yang

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

###Main
Reads in the input and performs the specific function that has been read.

####Instance Variables
* Ran - an objected used to access methods in the Obj Class

###Obj
Actually carries out the functions specified by the input

####Instance Variables
* None at the moment

###Commit
Commits the specified file

####Instance Variables
* Message - contains message of a commit
* Timestamp - time at which a commit was created, assigned by constructor
* Parent - the parent commit of a commit object

###Staging
Acts as the staging area for files before they are committed or removed

####Instance Variables
* added - files to be committed
* removed - files to be removed from staging area


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

###Main Class
* main(String...args): This is the main method in the Main class. It reads in the input and performs the specified function. This class also throws an error if the input is read incorrectly.

###Obj Class
* init(): This method initializes the initial commit as the system will always already start with one initial commit.
* add(String file): This method adds the file to the staging area before it is committed.
* commit(): This function creates a new commit without modifying the contents of the original file. It makes a copy of the file and will start to track its copy.
* log(): This function displays the history of commits, starting with the commit the head is pointing to.
* checkout(): Checkout changes where the current branch is pointing to. 

###Commit Class
* Commit(): This is the constructor of the commit class. It initializes all the variables in a commit object, such as parent, message, and timestamp.
* getMessage(): This function returns the variable message so that it can be accessed by other classes.
* getTimeStamp(): This function returns the variable timestamp so that it can be accessed by other classes.
* getParent(): This function returns the variable parent so that it can be accessed by other classes.

###Stating Class
* Staging(): This is the constructor of the Staging Class. It initializes the _added and _removed variables to empty Hashmaps.
* add(): This adds the new file to the _added hashmap.
* remove(): This removes the specified file from the _removed hashmap.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

Persistance will be implemented by using multiple pointers to point to the different commits so no commits are lost. These commits will be "linked" to each other so these file will always exist.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

