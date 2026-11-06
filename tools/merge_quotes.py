"""Concatenate tools/citations/citations_*.json into app/src/main/assets/citations.json (wording kept, French typography applied)."""
import glob, json, re


def typo(s):
    """Same as merge_words.typo (not imported: that script merges on import): curly apostrophe, U+202F before ; : ! ?"""
    return re.sub(r" ([;:!?])", " \\1", s.replace("'", "’"))


quotes = {}
for path in sorted(glob.glob("tools/citations/citations_*.json")):
    for q in json.load(open(path, encoding="utf-8")):
        q.update({k: typo(q[k]) for k in ("texte", "auteur", "oeuvre") if k in q})  # source is a URL: left alone
        quotes.setdefault(q["texte"], q)
json.dump(list(quotes.values()), open("app/src/main/assets/citations.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print(len(quotes), "citations")
