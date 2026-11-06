"""Flag definitions too close to Wiktionnaire's (licence: our texts must be original).

Needs a local index of the frwiktionary dump: sqlite table page(title, fr) holding each page's French section.
Usage: python3 tools/check_copies.py frwkt.sqlite app/src/main/assets/words.json > flags.json
"""
import difflib, json, re, sqlite3, sys

db = sqlite3.connect(sys.argv[1])
norm = lambda s: re.sub(r"\W+", " ", s.lower().replace("’", "'")).strip()


def clean(line):
    line = re.sub(r"\{\{[^{}]*\}\}", "", line)
    line = re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]*)\]\]", r"\1", line)
    return re.sub(r"'{2,}|<[^>]+>", "", line).strip(" #:*")


flags, missing = [], []
for w in json.load(open(sys.argv[2], encoding="utf-8")):
    row = db.execute("SELECT fr FROM page WHERE title=?", (w["mot"].replace("’", "'"),)).fetchone()
    if not row:
        missing.append(w["mot"])
        continue
    ours = norm(w["definition"])
    for d in (norm(clean(l)) for l in row[0].splitlines() if re.match(r"#[^*:]", l)):
        sm = difflib.SequenceMatcher(None, ours, d)
        lcs = sm.find_longest_match(0, len(ours), 0, len(d)).size
        # short definitions ("Médecin des chevaux.") can't avoid some overlap: judge by shared run length vs our length
        if sm.ratio() >= 0.75 or lcs >= 45 or (lcs >= 25 and lcs >= 0.6 * len(ours)):
            flags.append({"mot": w["mot"], "ratio": round(sm.ratio(), 2), "lcs": lcs, "wiktionnaire": d})
            break
json.dump({"flags": flags, "absents_du_wiktionnaire": missing}, sys.stdout, ensure_ascii=False, indent=1)
print(f"{len(flags)} flagged, {len(missing)} missing", file=sys.stderr)
