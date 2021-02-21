# Check that we can check out a previous version.
I definitions.inc
> init
<<<
+ wug.txt wug.txt
> add wug.txt
<<<
> commit "version 1 of wug.txt"
<<<
+ wug.txt notwug.txt
> add wug.txt
<<<
> commit "version 2 of wug.txt"
<<<
= wug.txt notwug.txt
# Each ${COMMIT_HEAD} captures its commit UID.
> log
===
${COMMIT_HEAD}
version 2 of wug.txt

===
${COMMIT_HEAD}
version 1 of wug.txt

===
${COMMIT_HEAD}
initial commit

<<<*
# UID of second version
D UID2 "${1}"
# UID of current version
D UID1 "${2}"
> checkout ${UID1} -- wug.txt
<<<
= wug.txt wug.txt
> checkout ${UID2} -- wug.txt
<<<
= wug.txt notwug.txt
