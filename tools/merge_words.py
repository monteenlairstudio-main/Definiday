"""Merge lexicon batches into app/src/main/assets/words.json (dedup by lemma, sorted)."""
import json, sys
words = {}
for path in sys.argv[1:]:
    for w in json.load(open(path, encoding="utf-8")):
        w = {k: v.replace("'", "’") if isinstance(v, str) else v for k, v in w.items()}  # French typographic apostrophe
        words.setdefault(w["mot"].lower(), w)
out = sorted(words.values(), key=lambda w: w["mot"].lower())
json.dump(out, open("app/src/main/assets/words.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print(len(out), "mots")
