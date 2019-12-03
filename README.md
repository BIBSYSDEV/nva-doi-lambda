# nva-doi-resources-metadata-service

A micro-service that takes an url as request parameter. The parameter is expected to b an doi-url. The micro-service will return eventually a map as json containing metadata on the resource (typically an article) given by its doi.

Today there is only one possible endpoint:
* metadata/doi/

## examples

* __DOI__: http://localhost:4567/nva-doi-resource-metadata-service/v1/metadata/doi/https%3A%2F%2Fdoi.org%2F10.1126%2Fscience.169.3946.635

```json
{
  "type": "article-journal",
  "id": "https://doi.org/10.1126/science.169.3946.635",
  "author": [
    {
      "family": "Frank",
      "given": "H. S."
    }
  ],
  "issued": {
    "date-parts": [
      [
        1970,
        8,
        14
      ]
    ]
  },
  "container-title": "Science",
  "DOI": "10.1126/science.169.3946.635",
  "volume": "169",
  "issue": "3946",
  "page": "635-641",
  "publisher": "(:unav)",
  "title": "The Structure of Ordinary Water: New data and interpretations are yielding new insights into this fascinating substance",
  "URL": "http://www.sciencemag.org/cgi/doi/10.1126/science.169.3946.635"
}
```

