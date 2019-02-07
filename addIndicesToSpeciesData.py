import sys
import codecs
import json

if __name__ == "__main__":
  jsonPath = sys.argv[1]
  with open(jsonPath) as f:
    data = json.load(f)
  id = 1
  for item in data:
    item['id'] = id
    id += 1
  with open(jsonPath, 'w') as outfile:
    json.dump(data, outfile)