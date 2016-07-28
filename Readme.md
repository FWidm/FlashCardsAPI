# FlashCards RESTful API
## Methods
### Users
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/users` | Retrieve a list of users. Can be filtered via url params `?name=x` or `?email=y`.| - | Creates a new User. | - | - |
| `/users/x` | Retrieve the details of the specified user. can filter by email with `?email=x@y.com` | Update the complete ressource with this id. | - | Partial update of the resource. | Deletes the specified resource. |

### Flashcards
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/cards` | Retrieve a list of all cards that are available. | - | Create a new Flashcard. The Body can contain either Quetion/Answer ids or the completely ressource that in turn will be displayed. | - | - |
| `/cards/x` | Retrieves a specific card by it's id. | Updates one specific card completely, answers ans question can be passed via id or as a new resource. Can be switched to append the list instead of replacing it via `?append=true`. | - | Updates one specific card partially, answers ans question can be passed via id or as a new resource. Can be switched to append the list instead of replacing it via `?append=true`. | - |
### Ratings
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/ratings` | Retrieves a list of ratings can be filtered via `?cardId=x`, `?answerId=x`, `?userId=x`, `?cardRating`, `?answerRating`. | - | Creates a new Rating object. Automatically update the rating of the associated ansers/cards and users. | - | - |
| `/ratings/x` | Retrieves one specific rating by it's id. | - | - | - | - |


for more working routing look at the [routes](conf/routes).
## German Turoial/Insights
If you're interested in reading about the things we use, there is a dev log file that describes problems and other topics we came across in german: [here]((https://github.com/FWidm/FlashCardsAPI/blob/master/_Docs/PlayDokuFabian.md)).

## TODO
- [ ] Decide where appending is useful in the future
- [x] Add append via url paramter to card PUT/PATCH methods
- [x] Session Management/Authentication/Token based system
- [x] Write Ratingsystem
- [ ] Rewrite Group System
- [ ] Implement CardDeck and Categories
- [ ] Write proper Unit-Tests in Postman

## Example Calls
### Users
**Elements needed for REST calls:**
- *String*: name, password, email
- *int*: rating

**Create a new User:**
Use a `POST`  request to `localhost:9000/users` with `Content-Type:Application/JSON`.  The body **must** contain name, password, email (*unique*) and **may** contain a rating.
```json
{
    "name": "name",
    "password": "passwörd",
    "email": "a@a.com",
    "rating": 1
}
```

### Groups
**Elements needed for REST calls:**
- *String*: name, description
- *Array of users*: users

**Create a new Group:**
Post a request to the host with `Content-Type:Application/JSON`.
The body **must** contain name and description and **may** contain a list of users.

`POST localhost:9000/groups`
```json
{
    "name":"xyxxxx",
    "description": "desc",
    "users":[{"userId":8},{"userId":9}]
}
```

**Update a Group:**
Send a `PATCH`or `PUT` request to `localhost:9000/groups/<id>` with `Content-Type:Application/JSON`.  For `PATCH`**any** attributes may be modified via the request. `PUT` expects **every** attribute to be there.

**Delete a Group:**
Use `DELETE` request to `localhost:9000/groups/<id>`.

### Flashcards
**Elements needed for Rest calls:**
- **rating**
- **question** - either a complete object as seen here or the `questionId`
- **answers** - an array of complete answer objects or `answerIds`

**Example**: Creates one new card with new question, answers, tags all from the same author (with userId=1).
```json
{
    "rating": 0,
    "question": {
      "questionText": "hello world!",
      "mediaURI": "www.google.de",
      "author": {
        "userId": 1
      }
    },
    "answers": [
      {
        "answerText": "answer",
        "answerHint": "hint",
        "mediaURI": "www.answer.com",
        "author": {
            "userId":1
        }
      }
    ],
    "author": {
      "userId": 1
    },
    "multipleChoice": false,
    "marked": false,
    "selected": false,
    "tags":[{
              "tagName": "tag1"
    }]
  }
```

### Ratings
**Elements needed for Rest calls**
- Author: userId
- Answer: answerId *ODER*
- Flashcard: FlashcardI
- ratingModifier: um wieviel soll der Wert verändert werden (+1,-1)

**Example**: Create a Rating made by the author with userId=1 for the answer with answerId=1 that modifies the rating by -1.
```json
{
  "ratingId": 1,
  "author": {
    "userId": 1
  },
  "ratingModifier": -1,
  "answer": {
    "answerId": 1
    }
  }
```
### Further Information
see the Postman Collection for more detailled information: [here](https://github.com/FWidm/FlashCardsAPI/blob/master/_Docs/FlashCards.postman_collection.json).

Also refer to the [JsonKeys.java](https://github.com/FWidm/FlashCardsAPI/blob/master/app/util/JsonKeys.java) for more inforations about naming conventions.
## Expected/Possible JSON for the different Objects
Refer to the [JsonKeys.java](/app/util/JsonKeys.java) File, it containis all named JsonProperties for each Class/Model.
