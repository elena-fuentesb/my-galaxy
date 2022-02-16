

## About The Project

### Built With


* [Scala 3](https://scala-lang.org/)
* [http4s](https://http4s.org/)
* [Circe](https://circe.github.io/circe/)
* [Firebase](https://firebase.google.com/)

## Getting Started

### Prerequisites

1. Create a Firebase project
2. Go to Project Settings -> Service Accounts -> Create Service Account
3. Generate a new private key and store the json file
4. Initiate Firestore database with 2 collections: stars and galaxies

### Run backend

Set environmental variables:
* FIREBASE_SECRET_FILE_NAME (where the Firebase priv key is stored)
* FIREBASE_PROJECT_NAME (name of the project in Firebase)

Run this backend

  ```sh
  sbt compile
  sbt run
  ```

### Example requests

prefix with localhost:8080

* GET /stars
* GET /galaxies

* POST /stars, with body:
```json
{
  "starName":"Al3ssia8",
  "planets":[
    {
      "planetName":"Winky-u"
    },
    {
      "planetName":"Wonky"
    }
  ]
}
```

* POST /series, with body:
```json
{
  "name":"Holis Galaxy",
  "planetarySystems":[
    {
      "name":"S-P1",
      "stars":[
        {
          "starName":"Al3ssia8",
          "planets":[
            {
              "planetName":"Winky-u"
            }
          ]
        },
        {
          "starName":"star22",
          "planets":[
            {
              "planetName":"09Lo"
            }
          ]
        }
      ]
    },
    {
      "name":"S-P2",
      "stars":[
        {
          "starName":"Aurora",
          "planets":[
            {
              "planetName":"Bb"
            },
            {
              "planetName":"Mon"
            }
          ]
        }
      ]
    }
  ]
}
```