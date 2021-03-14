import sys, re
from subprocess import \
    check_output, PIPE, STDOUT, DEVNULL, CalledProcessError, TimeoutExpired
from os.path import abspath, basename, dirname, exists, join, splitext, isdir
from getopt import getopt, GetoptError
from os import chdir, environ, getcwd, mkdir, remove
from shutil import copyfile, rmtree
from math import log
from glob import glob

SHORT_USAGE = """\
Usage: python3 staff-runner.py OPTIONS TEST.in ...
   OPTIONS may include
       --keep         Keep test directories
       --timeout=SEC  Default number of seconds allowed to each execution
                      of gitlet.
       --src=SRC      Use SRC instead of "src" as the subdirectory containing
                      files referenced by + and =.
       --tolerance=N  Set the maximum allowed edit distance between program
                      output and expected output to N (default 3).
       --verbose      Print extra information about execution.
"""

USAGE = SHORT_USAGE + """\

For each TEST.in, change to an empty directory, and execute the instructions
in TEST.in.  Before executing an instruction, first replace any occurrence
of ${VAR} with the current definition of VAR (see the D command below).
Replace any occurrence of ${N} for non-negative decimal numeral N with
the value of the Nth captured group in the last ">" command's expected
output lines.  Undefined if the last ">" command did not end in "<<<*",
or did not have the indicated group. N=0 indicates the entire matched string.

The instructions each have one of the following forms:

   # ...  A comment, producing no effect.
   I FILE Include.  Replace this statement with the contents of FILE,
          interpreted relative to the directory containing the .in file.
   C DIR  Create, if necessary, and switch to a subdirectory named DIR under
          the main directory for this test.  If DIR is missing, changes
          back to the default directory.  This command is principally
          intended to let you set up remote repositories.
   T N    Set the timeout for gitlet commands in the rest of this test to N
          seconds.
   + NAME F
          Copy the contents of src/F into a file named NAME.
   - NAME
          Delete the file named NAME.
   > COMMAND OPERANDS
   LINE1
   LINE2
   ...
   <<<
          Run staff-gitlet with COMMAND ARGUMENTS as its parameters.  Compare
          its output with LINE1, LINE2, etc., reporting an error if there is
          "sufficient" discrepency.  The <<< delimiter may be followed by
          an asterisk (*), in which case, the preceding lines are treated as
          Python regular expressions and matched accordingly.
   = NAME F
          Check that the file named NAME is identical to src/F, and report an
          error if not.
   * NAME
          Check that the file NAME does not exist, and report an error if it
          does.
   E NAME
          Check that file or directory NAME exists, and report an error if it
          does not.
   D VAR "VALUE"
          Defines the variable VAR to have the literal value VALUE.  VALUE is
          taken to be a raw Python string (as in r"VALUE").  Substitutions are
          first applied to VALUE.

For each TEST.in, reports at most one error.  Without the --show option,
simply indicates tests passed and failed.  If N is postive, also prints details
of the first N failing tests. With --show=all, shows details of all failing
tests.  With --keep, keeps the directories created for the tests (with names
TEST.dir).

When finished, reports number of tests passed and failed, and the number of
faulty TEST.in files."""


DIRECTORY_LAYOUT_ERROR = """\
Your {} folder is not where we expected it. Please ensure that your directory
structure matches the following:

sp21-s***
  ├── library-sp21
  │    └── ...
  ├── proj2
  │   ├── gitlet
  │   ├── testing <==== This should be your CWD
  │   │    ├── runner.py
  │   │    └── ...
  │   └── ...
  └── ...

Note your CWD must be `sp21-s***/proj2/testing`

Also check that your REPO_DIR environment variable is the path to your
`sp21-s***` directory. You can check this by running the command:

    $ echo REPO_DIR
    /Users/omarkhan902/cs61b/61b_sp21_stuff/sp21-s3

That's what mine looks like. Go back to lab1 if you are still having issues"""

JAVA_COMMAND = "java"
STAFF_COMMAND = "staff-gitlet"
JAVAC_COMMAND = "javac -d ."
JVM_COMMAND = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
TIMEOUT = 10
DEBUG = False
DEBUG_MSG = \
    """
   ============================================================================
  |                   ~~~~~  You are in debug mode  ~~~~~                      |
  |   In this mode, you will be shown each command from the test case.         |
  |                                                                            |
  |   There are three commands:                                                |
  |                                                                            |
  |   1. 'n' - type in 'n' to go to the next command without debugging the     |
  |            current one (analogous to "Step Over" in IntelliJ).             |
  |                                                                            |
  |   2. 's' - type in 's' to debug the current command (analogous to          |
  |            "Step Into" in IntelliJ). Make sure to set breakpoints!         |
  |                                                                            |
  |   3. 'q' - type in 'q' to quit and stop debugging. If you had the `--keep` |
  |            flag, then your directory state will be saved and you can       |
  |            investigate it.                                                 |
   ============================================================================
"""

def Usage():
    print(SHORT_USAGE, file=sys.stderr)
    sys.exit(1)

Mat = None
def Match(patn, s):
    global Mat
    Mat = re.match(patn, s)
    return Mat

def Group(n):
    return Mat.group(n)

def contents(filename):
    try:
        with open(filename) as inp:
            return inp.read()
    except FileNotFoundError:
        return None

def editDistance(s1, s2):
    dist = [list(range(len(s2) + 1))] + \
           [ [i] + [ 0 ] * len(s2) for i in range(1, len(s1) + 1) ]
    for i in range(1, len(s1) + 1):
        for j in range(1, len(s2) + 1):
            dist[i][j] = min(dist[i-1][j] + 1,
                             dist[i][j-1] + 1,
                             dist[i-1][j-1] + (s1[i-1] != s2[j-1]))
    return dist[len(s1)][len(s2)]

def nextCommand(full_cmnd, timeout):
    return check_output(full_cmnd, shell=True, universal_newlines=True,
                        stdin=DEVNULL, stderr=STDOUT, timeout=timeout)
def stepIntoCommand(full_cmnd):
    out = check_output(full_cmnd, shell=True, universal_newlines=True,
                       stdin=DEVNULL, stderr=STDOUT, timeout=None)
    return out.split("\n", 1)[1]

def createTempDir(base):
    for n in range(100):
        name = "{}_{}".format(base, n)
        try:
            mkdir(name)
            return name
        except OSError:
            pass
    else:
        raise ValueError("could not create temp directory for {}".format(base))

def cleanTempDir(dir):
    rmtree(dir, ignore_errors=True)

def doDelete(name, dir):
    try:
        remove(join(dir, name))
    except OSError:
        pass

def doCopy(dest, src, dir):
    try:
        doDelete(dest, dir)
        copyfile(join(src_dir, src), join(dir, dest))
    except OSError:
        raise ValueError("file {} could not be copied to {}".format(src, dest))

def doCompile(target):
    out = ""
    try:
        full_cmnd = "{} {}".format(JAVAC_COMMAND, target)
        out = check_output(full_cmnd, shell=True, universal_newlines=True,
                           stdin=DEVNULL, stderr=STDOUT)
        return "OK", out
    except CalledProcessError as excp:
        return ("javac exited with code {}".format(excp.args[0]),
                excp.output)

def doExecute(cmnd, dir, timeout):
    here = getcwd()
    out = ""
    try:
        chdir(dir)
        full_cmnd = "{} {}".format(STAFF_COMMAND, cmnd)

        out = nextCommand(full_cmnd, timeout)

        if superverbose:
            print(out)

        return "OK", out
    except CalledProcessError as excp:
        return ("java gitlet.Main exited with code {}".format(excp.args[0]),
                excp.output)
    except TimeoutExpired:
        return "timeout", None
    finally:
        chdir(here)

def canonicalize(s):
    if s is None:
        return None
    return re.sub('\r', '', s)

def fileExists(f, dir):
    return exists(join(dir, f))

def correctFileOutput(name, expected, dir):
    userData = canonicalize(contents(join(dir, name)))
    stdData = canonicalize(contents(join(src_dir, expected)))
    return userData == stdData

def correctProgramOutput(expected, actual, last_groups, is_regexp):
    expected = re.sub(r'[ \t]+\n', '\n', '\n'.join(expected))
    expected = re.sub(r'(?m)^[ \t]+', ' ', expected)
    actual = re.sub(r'[ \t]+\n', '\n', actual)
    actual = re.sub(r'(?m)^[ \t]+', ' ', actual)

    last_groups[:] = (actual,)
    if is_regexp:
        try:
            if not Match(expected.rstrip() + r"\Z", actual) \
                    and not Match(expected.rstrip() + r"\Z", actual.rstrip()):
                return False
        except:
            raise ValueError("bad pattern")
        last_groups[:] += Mat.groups()
    elif editDistance(expected.rstrip(), actual.rstrip()) > output_tolerance:
        return False
    return True

def reportDetails(test, included_files, line_num):
    if show is None:
        return
    if show <= 0:
        print("   Limit on error details exceeded.")
        return
    direct = dirname(test)

    print("    Error on line {} of {}".format(line_num, basename(test)))

    for base in [basename(test)] + included_files:
        full = join(dirname(test), base)
        print(("-" * 20 + " {} " + "-" * 20).format(base))
        text_lines = list(enumerate(re.split(r'\n\r?', contents(full))))[:-1]
        fmt = "{{:{}d}}. {{}}".format(round(log(len(text_lines), 10)))
        text = '\n'.join(map(lambda p: fmt.format(p[0] + 1, p[1]), text_lines))
        print(text)
        print("-" * (42 + len(base)))

def chop_nl(s):
    if s and s[-1] == '\n':
        return s[:-1]
    else:
        return s

def line_reader(f, prefix):
    n = 0
    try:
        with open(f) as inp:
            while True:
                L = inp.readline()
                if L == '':
                    return
                n += 1
                included_file = yield (prefix + str(n), L)
                if included_file:
                    yield None
                    yield from line_reader(included_file, prefix + str(n) + ".")
    except FileNotFoundError:
        raise ValueError("file {} not found".format(f))

def doTest(test):
    last_groups = []
    base = splitext(basename(test))[0]
    print("{}:".format(base), end=" \n")
    cdir = tmpdir = createTempDir(base)
    if verbose:
        print("Testing directory: {}".format(tmpdir))
    timeout = TIMEOUT
    defns = {}

    def do_substs(L):
        c = 0
        L0 = None
        while L0 != L and c < 10:
            c += 1
            L0 = L
            L = re.sub(r'\$\{(.*?)\}', subst_var, L)
        return L

    def subst_var(M):
        key = M.group(1)
        if Match(r'\d+$', key):
            try:
                return last_groups[int(key)]
            except IndexError:
                raise ValueError("FAILED (nonexistent group: {{{}}})"
                                 .format(key))
        elif M.group(1) in defns:
            return defns[M.group(1)]
        else:
            raise ValueError("undefined substitution: ${{{}}}".format(M.group(1)))

    try:
        line_num = None
        inp = line_reader(test, '')
        included_files = []
        while True:
            line_num, line = next(inp, (line_num, ''))
            if line == "":
                print("OK")
                return True
            if not Match(r'\s*#', line):
                line = do_substs(line)
            if verbose:
                print("+ {}".format(line.rstrip()))
            if Match(r'\s*#', line) or Match(r'\s+$', line):
                pass
            elif Match(r'I\s+(\S+)', line):
                inp.send(join(dirname(test), Group(1)))
                included_files.append(Group(1))
            elif Match(r'C\s*(\S*)', line):
                if Group(1) == "":
                    cdir = tmpdir
                else:
                    cdir = join(tmpdir, Group(1))
                    if not exists(cdir):
                        mkdir(cdir)
            elif Match(r'T\s*(\S+)', line):
                try:
                    timeout = float(Group(1))
                except:
                    ValueError("bad time: {}".format(line))
            elif Match(r'\+\s*(\S+)\s+(\S+)', line):
                doCopy(Group(1), Group(2), cdir)
            elif Match(r'-\s*(\S+)', line):
                doDelete(Group(1), cdir)
            elif Match(r'>\s*(.*)', line):
                cmnd = Group(1)
                expected = []
                while True:
                    line_num, L = next(inp, (line_num, ''))
                    if L == '':
                        raise ValueError("unterminated command: {}"
                                         .format(line))
                    L = L.rstrip()
                    if Match(r'<<<(\*?)', L):
                        is_regexp = Group(1)
                        break
                    expected.append(do_substs(L))
                msg, out = doExecute(cmnd, cdir, timeout)
                if verbose:
                    if out:
                        print(re.sub(r'(?m)^', '- ', chop_nl(out)))
                if msg == "OK":
                    if not correctProgramOutput(expected, out, last_groups,
                                                is_regexp):
                        msg = "incorrect output"
                elif msg == "User Exit":
                    print("Exiting Debug mode ...")
                    break
                if msg != 'OK':
                    print("ERROR ({})".format(msg))
                    reportDetails(test, included_files, line_num)
                    return False
            elif Match(r'=\s*(\S+)\s+(\S+)', line):
                if not correctFileOutput(Group(1), Group(2), cdir):
                    print("ERROR (file {} has incorrect content)"
                          .format(Group(1)))
                    reportDetails(test, included_files, line_num)
                    return False
            elif Match(r'\*\s*(\S+)', line):
                if fileExists(Group(1), cdir):
                    print("ERROR (file {} present)".format(Group(1)))
                    reportDetails(test, included_files, line_num)
                    return False
            elif Match(r'E\s*(\S+)', line):
                if not fileExists(Group(1), cdir):
                    print("ERROR (file or directory {} not present)"
                          .format(Group(1)))
                    reportDetails(test, included_files, line_num)
                    return False
            elif Match(r'(?s)D\s*([a-zA-Z_][a-zA-Z_0-9]*)\s*"(.*)"\s*$', line):
                defns[Group(1)] = Group(2)
            else:
                raise ValueError("bad test line at {}".format(line_num))
    finally:
        if not keep:
            cleanTempDir(tmpdir)
        else:
            print(f"\nDirectory state saved in {tmpdir}")

if __name__ == "__main__":
    show = None
    keep = False
    lib_dir = None
    verbose = False
    superverbose = False
    src_dir = 'src'
    gitlet_dir = join(dirname(abspath(getcwd())), "gitlet")
    output_tolerance = 0

    try:
        opts, files = \
            getopt(sys.argv[1:], '',
                   ['show=', 'keep', 'lib=', 'verbose', 'src=',
                    'tolerance=', 'superverbose', 'debug'])
        for opt, val in opts:
            if opt == '--show':
                show = int(val)
            elif opt == "--keep":
                keep = True
            elif opt == "--lib":
                lib_dir = val
            elif opt == "--src":
                src_dir = val
            elif opt == "--verbose":
                verbose = True
            elif opt == "--tolerance":
                output_tolerance = int(val)
            elif opt == "--superverbose":
                superverbose = True
            elif opt == "--debug":
                DEBUG = True
                TIMEOUT = 100000
    except GetoptError:
        Usage()
    if not files:
        print(USAGE)
        sys.exit(0)

    matching_files = []
    for path in files:
        matching_files += glob(path)
    files = matching_files

    num_tests = len(files)
    errs = 0
    fails = 0

    print(DEBUG_MSG)

    for test in files:
        try:
            if not exists(test):
                num_tests -= 1
            elif not doTest(test):
                errs += 1
                if type(show) is int:
                    show -= 1
        except ValueError as excp:
            print("FAILED ({})".format(excp.args[0]))
            fails += 1

    cleanTempDir(join(abspath(getcwd()), "gitlet"))

    print()
    print("Ran {} tests. ".format(num_tests), end="")
    if errs == fails == 0:
        print("All passed.")
    else:
        print("{} passed.".format(num_tests - errs - fails))
        sys.exit(1)
