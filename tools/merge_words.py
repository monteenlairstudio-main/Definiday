"""Merge lexicon batches into app/src/main/assets/words.json.

Usage: python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json
Later files override earlier ones for the same lemma; {"mot": ..., "_supprimer": true} removes it.
"""
import json, re, sys, unicodedata


def typo(s):
    """French typography: curly apostrophe, narrow no-break space before ; : ! ? so they never wrap alone."""
    return re.sub(r" ([;:!?])", "\u202f\\1", s.replace("'", "’"))


words = {}
for path in sys.argv[1:]:
    for w in json.load(open(path, encoding="utf-8")):
        key = w["mot"].replace("'", "’").lower()
        if w.get("_supprimer"):
            words.pop(key, None)
            continue
        # French typographic apostrophe; drop QA-only keys like "_raison"
        words[key] = {k: typo(v) if isinstance(v, str) else v for k, v in w.items() if not k.startswith("_")}
out = sorted(words.values(), key=lambda w: unicodedata.normalize("NFD", w["mot"].lower()))  # é sorts with e
json.dump(out, open("app/src/main/assets/words.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print(len(out), "mots")
