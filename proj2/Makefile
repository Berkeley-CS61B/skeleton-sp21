# This makefile is defined to give you the following targets:
#
#    default: The default target: Compiles the program in package db61b.
#    style: Run our style checker on the project source files.  Requires that
#           the source files compile.
#    check: Compiles the db61b package, if needed, and then performs the
#           tests described in testing/Makefile.
#    clean: Remove regeneratable files (such as .class files) produced by
#           other targets and Emacs backup files.
#
# In other words, type 'make' to compile everything; 'make check' to 
# compile and test everything, and 'make clean' to clean things up.
# 
# You can use this file without understanding most of it, of course, but
# I strongly recommend that you try to figure it out, and where you cannot,
# that you ask questions.  The Lab Reader contains documentation.

# Name of package containing main procedure 
PACKAGE = gitlet

STYLEPROG = style61b

# The name of the Python 3 program, used in the 'check' target.  If your system
# has a different name for this program (such as just "python"), run
# the Makefile with
#	make PYTHON=python check
PYTHON = python3

# Flags to pass to tester.py.
TESTER_FLAGS =

RMAKE = "$(MAKE)"

# Targets that don't correspond to files, but are to be treated as commands.
.PHONY: default check integration unit clean style

default:
	$(RMAKE) -C $(PACKAGE) default

check: integration unit

integration: default
	$(RMAKE) -C testing PYTHON=$(PYTHON) TESTER_FLAGS="$(TESTER_FLAGS)" check

unit: default
	$(RMAKE) -C gitlet unit

style:
	$(RMAKE) -C $(PACKAGE) STYLEPROG=$(STYLEPROG) style

# 'make clean' will clean up stuff you can reconstruct.
clean:
	$(RM) *~
	$(RMAKE) -C $(PACKAGE) clean
	$(RMAKE) -C testing clean


