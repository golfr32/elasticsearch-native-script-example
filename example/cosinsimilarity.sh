#!/usr/bin/env bash
curl -s -XPOST "http://localhost:9200/termscore/doc/_search?pretty" -d'
{
   "query": {
      "function_score": {
         "query": {
            "bool": {
               "must": [
                  {
                     "match": {
                        "text": {
                           "query": "Royal takes first Atlas first aircraft British Force -RRB",
                           "operator": "and"
                        }
                     }
                  }
               ]
            }
         },
         "functions": [{
            "script_score": {
              "script": {
                  "inline" : "cosine_sim_script_score",
                  "lang": "native",
                  "params": {
                    "field": "text",
                    "terms": [ "Royal", "takes", "first", "Atlas", "first", "aircraft", "British", "Force", "-RRB"  ],
                    "weights": [  1.0,  1.0,  1.0,  1.0,  1.0,  1.0, 1.0,  1.0,  1.0 ]
                  }
              }
            }
          }
          ],
         "boost_mode": "replace"
      }
   }
}'


curl -s -XPOST "http://localhost:9200/termscore/doc/_search?pretty" -d'
{
  "query":{
    "function_score": {
      "query": {
        "match": {
          "text": {
            "query": "Royal takes first Atlas first aircraft British Force -RRB"
          }
        }
      },
      "functions": [{
        "script_score": {
          "script": {
            "inline" : "cosine_sim_script_score",
            "lang": "native",
            "params": {
              "field": "text",
              "terms": [ "Royal", "takes", "first", "Atlas", "first", "aircraft", "British", "Force", "-RRB"  ],
              "weights": [  1.0,  1.0,  1.0,  1.0,  1.0,  1.0, 1.0,  1.0,  1.0 ]
            }
          }
        }
      }],
      "boost_mode": "replace"
    }
  }
}'
