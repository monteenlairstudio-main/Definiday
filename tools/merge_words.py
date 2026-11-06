"""Merge lexicon batches into app/src/main/assets/words.json.

Usage: python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json tools/lexique/enrich_*.json
Later files patch earlier ones field by field for the same lemma (e.g. enrich_* only adds "anecdote"/"source");
{"mot": ..., "_supprimer": true} removes it. Patches for a missing lemma without a definition are ignored.
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
        if key not in words and "definition" not in w:
            continue  # patch (anecdote…) for a word removed by QA
        words.setdefault(key, {}).update({k: typo(v) if isinstance(v, str) and k != "source" else v for k, v in w.items() if not k.startswith("_")})
out = sorted(words.values(), key=lambda w: unicodedata.normalize("NFD", w["mot"].lower()))  # é sorts with e
json.dump(out, open("app/src/main/assets/words.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print(len(out), "mots")
