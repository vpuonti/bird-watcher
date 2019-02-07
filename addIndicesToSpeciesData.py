import sys
import json
import io

if __name__ == "__main__":
  jsonPath = sys.argv[1]
  with open(jsonPath) as f:
    data = json.load(f)
  id = 1
  for item in data:
    item['id'] = id
    id += 1
  with io.open('species_with_indices.json', 'w', encoding='utf8') as outfile:
    jsonStr = json.dumps(data, 
    outfile, 
    ensure_ascii=False,
    sort_keys=True,
    indent=4)
    outfile.write(unicode(jsonStr))