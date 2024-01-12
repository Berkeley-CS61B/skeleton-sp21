# Gitlet

## Overview
This is my personal implementation of Gitlet, a version control system inspired by Git, as part of the CS61B Data Structures course at UC Berkeley, Spring 2021. The project specifications can be found [here](https://sp21.datastructur.es/materials/proj/proj2/proj2#going-remote-extra-credit).

Gitlet is a simplified version control system that allows users to track changes in their codebase over time. It supports basic version control operations, such as commit, log, branch, merge, reset, and more.

## Getting Started
```shell
git clone https://github.com/xUser5000/berkeley-cs61b-sp21.git  # download the parent repo
```

### Compiling
```shell
cd berkeley-cs61b-sp21/proj2/  # go into the project's directory
make  # compile the codebase
```

### Usage
```shell
java gitlet.Gitlet <command> [args]
```

Common commands:
```
init: Initializes a new Gitlet repository.
add <filename>: Adds a file to the staging area.
commit <message>: Saves a snapshot of files in the staging area.
rm <file>: Unstage the file if it is currently staged for addition
log: Displays information about past commits.
global-log: Displays information about all commits ever made.
find <message>: Prints the SHA-1 IDs of all commits with the given commit message.
status: Displays the status of the working directory and the staging area.
checkout:
    checkout -- <file>: Restores a file to its state at the current commit.
    checkout <commit-id> -- <file>: Restores a file to its stat at the given commit.
    checkout <branch-name>: Takes all files in the commit at the head of the given branch, and puts them in the working directory.
branch <branch-name>: Creates a new branch.
rm-branch <branch-name>: Deletes the specified branch.
reset <commit-id>: Resets the current branch to the given commit.
merge <branch-name>: Merges the given branch into the current branch
```

### Testing
```shell
make check  # run tests
```
- TODO: push tests in the local repository.
