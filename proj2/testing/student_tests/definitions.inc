# Standard definitions
D DATE "Date: \w\w\w \w\w\w \d+ \d\d:\d\d:\d\d \d\d\d\d [-+]\d\d\d\d"
# A status log header RE.  Captures the commit id in its sole group.
D COMMIT_HEAD "commit ([a-f0-9]+)[ \t]*\n(?:Merge:\s+[0-9a-f]{7}\s+[0-9a-f]{7}[ ]*\n)?${DATE}"
# A full log entry.  Captures the entry. Assume logs messages don't contain
# "==="
D COMMIT_LOG "(===[ ]*\ncommit [a-f0-9]+[ ]*\n(?:Merge:\s+[0-9a-f]{7}\s+[0-9a-f]{7}[ ]*\n)?${DATE}[ ]*\n(?:.|\n)*?(?=\Z|\n===))"
# An arbitrary line of text (works even with ?s)
D ARBLINE "[^\n]*(?=\n|\Z)"
# Zero or more arbitrary full lines of text.
D ARBLINES "(?:(?:.|\n)*(?:\n|\Z)|\A|\Z)"
