# nva-doi-resources-metadata-service

A micro-service that takes an url as request parameter. The parameter is expected to b an doi-url. The micro-service will return eventually a map as json containing metadata on the resource (typically an article) given by its doi.

Today there are three possible endpoints:
* metadata/doi/
* metadata/dc/
* metadata/meta/

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

* __DC__: http://localhost:4567/nva-doi-resource-metadata-service/v1/metadata/dc/http%3A%2F%2Fdoi.org%2F10.3886%2FE101441V1

```json
{
  "dc.contributor.author": [
    "United States Department Of Agriculture. Economic Research Service"
  ],
  "dc.creator": [
    "United States Department of Agriculture. Economic Research Service"
  ],
  "dc.date.issued": [
    "2018"
  ],
  "dc.description.abstract": [
    "Limited access to supermarkets, supercenters, grocery stores, or other sources of healthy and affordable food may make it harder for some Americans to eat a healthy diet. Expanding the availability of nutritious and affordable food by developing and equipping grocery stores, small retailers, corner markets and farmers’ markets in communities with limited access is an important part of the Healthy Food Financing Initiative. There are many ways to define which areas are considered \"food deserts\" and many ways to measure food store access for individuals and for neighborhoods. Most measures and definitions take into account at least some of the following indicators of access:Accessibility to sources of healthy food, as measured by distance to a store or by the number of stores in an area.Individual-level resources that may affect accessibility, such as family income or vehicle availability.Neighborhood-level indicators of resources, such as the average income of the neighborhood and the availability of public transportation. In the Food Access Research Atlas, several options are available to describe food access along these dimensions. The Food Access Research Atlas presents a spatial overview of food access indicators for low-income and other census tracts using different measures of supermarket accessibility. It provides food access data for populations within census tracts and offers census-tract-level data on food access that can be downloaded for community planning or research purposes. This Atlas can be used to create maps showing food access indicators by census tract using different measures and indicators of supermarket accessibility. It can be used to compare food access measures based on 2015 data with the previous 2010 measures, view indicators of food access for selected subpopulations, and download census-tract-level data on food access measures."
  ],
  "dc.identifier.doi": [
    "http://doi.org/10.3886/E101441V1"
  ],
  "dc.publisher": [
    "Inter-university Consortium for Political and Social Research (ICPSR)"
  ],
  "dc.title": [
    "USDA -Food Access"
  ],
  "dc.type": [
    "Dataset"
  ]
}
```

* __META__: http://localhost:4567/nva-doi-resource-metadata-service/v1/metadata/meta/https%3A%2F%2Fdoi.org%2F10.1126%2Fscience.aau2582

```json
{
  "HandheldFriendly": [
    "true"
  ],
  "MobileOptimized": [
    "width"
  ],
  "article:modified_time": [
    "2018-05-23T11:03:39-04:00"
  ],
  "article:published_time": [
    "2018-05-22T16:35:00-04:00"
  ],
  "article:publisher": [
    "https://www.facebook.com/ScienceNOW"
  ],
  "dc.date": [
    "2018-05-22T16:39:00-04:00"
  ],
  "dcterms.date": [
    "2018-05-22T16:39:00-04:00"
  ],
  "description": [
    "New research centers move 15-year project closer to reality"
  ],
  "fb:admins": [
    "1400521805",
    "2703576",
    "678955044"
  ],
  "fb:app_id": [
    "1478542162442556"
  ],
  "fb:pages": [
    "100864590107"
  ],
  "news_authors": [
    "Dennis Normile"
  ],
  "news_doi": [
    "10.1126/science.aau2582"
  ],
  "news_key_words": [
    "Asia, Brain \u0026 Behavior, Health"
  ],
  "news_section": [
    "Latest News"
  ],
  "og:description": [
    "New research centers move 15-year project closer to reality"
  ],
  "og:image": [
    "https://www.sciencemag.org/sites/default/files/styles/article_main_large/public/ca_0525NID_Elderly_People_China_Dementia_online.jpg?itok\u003dmDz-Z9rM"
  ],
  "og:image:height": [
    "720"
  ],
  "og:image:secure_url": [
    "https://www.sciencemag.org/sites/default/files/styles/article_main_large/public/ca_0525NID_Elderly_People_China_Dementia_online.jpg?itok\u003dmDz-Z9rM"
  ],
  "og:image:type": [
    "image/jpeg"
  ],
  "og:image:url": [
    "https://www.sciencemag.org/sites/default/files/styles/article_main_large/public/ca_0525NID_Elderly_People_China_Dementia_online.jpg?itok\u003dmDz-Z9rM"
  ],
  "og:image:width": [
    "1280"
  ],
  "og:site_name": [
    "Science | AAAS"
  ],
  "og:title": [
    "Here’s how China is challenging the U.S. and European brain initiatives"
  ],
  "og:type": [
    "article"
  ],
  "og:updated_time": [
    "2018-05-23T11:03:39-04:00"
  ],
  "og:url": [
    "https://www.sciencemag.org/news/2018/05/heres-how-china-challenging-us-and-european-brain-initiatives"
  ],
  "paywall": [
    "true"
  ],
  "robots": [
    "follow, index, noodp"
  ],
  "thumbnail": [
    "https://www.sciencemag.org/sites/default/files/styles/article_main_large/public/ca_0525NID_Elderly_People_China_Dementia_online.jpg?itok\u003dmDz-Z9rM"
  ],
  "twitter:card": [
    "summary_large_image"
  ],
  "twitter:creator": [
    "@NewsfromScience"
  ],
  "twitter:creator:id": [
    "17089636"
  ],
  "twitter:description": [
    "New research centers move 15-year project closer to reality"
  ],
  "twitter:image": [
    "https://www.sciencemag.org/sites/default/files/styles/article_main_large/public/ca_0525NID_Elderly_People_China_Dementia_online.jpg?itok\u003dmDz-Z9rM"
  ],
  "twitter:image:height": [
    "720"
  ],
  "twitter:image:width": [
    "1280"
  ],
  "twitter:site": [
    "@newsfromscience"
  ],
  "twitter:site:id": [
    "17089636"
  ],
  "twitter:title": [
    "Here’s how China is challenging the U.S. and European brain initiatives"
  ],
  "twitter:url": [
    "https://www.sciencemag.org/news/2018/05/heres-how-china-challenging-us-and-european-brain-initiatives"
  ],
  "viewport": [
    "width\u003ddevice-width, initial-scale\u003d1.0, shrink-to-fit\u003dno"
  ]
}
```
