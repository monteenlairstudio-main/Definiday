"""Concatenate tools/citations/citations_*.json into app/src/main/assets/citations.json (texts kept verbatim)."""
import glob, json
quotes = {}
for path in sorted(glob.glob("tools/citations/citations_*.json")):
    for q in json.load(open(path, encoding="utf-8")):
        quotes.setdefault(q["texte"], q)
json.dump(list(quotes.values()), open("app/src/main/assets/citations.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print(len(quotes), "citations")
