# FlashCards RESTful API
![FlashCards Logo](_Docs/img/flash_icon_250.png)

## TODO
- Implement Token invalidation after time
- Implement auth for nearly all Update/Delete operations
- Test Production build
- Implement Backend for challenge/leaderbords

## Methods
### Users
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/users` | Retrieve a list of users. Can be filtered via url params `?name=x` or `?email=y`.| - | Creates a new user. | - | - |
| `/users/x` | Retrieve the details of the specified user. can filter by email with `?email=x@y.com` | Update the complete resource with this id. | - | Partial update of the resource. | Deletes the specified resource. |
| `/users/x/groups` | Retrieve all groups for a specific user. | | | | | |
### UserGroups
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/groups`| A list of all groups, can be filtered with `?empty=y` where y={true,false}. | | Create a new resource with a name, description and a list of user ids. | | |
| `/groups/x`| Retrieve one group |  Update a resource completely with name, description, users. | | Update a resource partially with name, description, users. | Delete a group. |
| `/groups/x/users`| Retrieve users from one group. | | | | | |
| `/groups/x/decks`| Retrieve decks of one group. | | | | | |
### Flashcards
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/cards` | Retrieve a list of all cards that are available. | - | Create a new Flashcard. The body has to contain the question and answer as a whole (without an id, as both can be only part of a card), additionally tags can be either in the form of an id or the complete tag information. | - | - |
| `/cards/x` | Retrieves a specific card by  id. | Updates one specific card completely, answers and questions do need to be passed as a complete json file (referencing via id does not work), tags can be referenced or put in as complete resource. Can be switched to append the list instead of replacing it via `?append=true`. | - | Updates one specific card partially, answers and questions do need to be passed as a complete json file (referencing via id does not work), tags can be referenced or put in as complete resource. Can be switched to append the list instead of replacing it via `?append=true`. | - |
| `/cards/x/question` | Retrieves a specific cards question by card id. | | | | |
| `/cards/x/answers` | Retrieves a specific cards answers by card id, `size=y` can be used as optional parameter to get a number of answers to display. | | | | | |
| `/cards/x/author` | Retrieves a specific cards author by card id. | | | | |

### Ratings
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/ratings` | Retrieves a list of ratings can be filtered via `?cardId=x`, `?answerId=x`, `?userId=x`, `?cardRating`, `?answerRating`. | - | Creates a new Rating object. Automatically update the rating of the associated ansers/cards and users. | - | - |
| `/ratings/x` | Retrieves one specific rating by  id. | - | - | - | - |
### CardDecks
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/cardDecks` | Retrieve a list of all CardDecks that are available. | - | Create a new CardDeck. | - | - |
| `/cardDecks/x` | Retrieve a CardDeck. | Update a carddeck completely. Usable URL parameters: `append={true/false}` to append the list or replace it and `reloacte={true/false}` to enable or disable relocating cards from one deck to another one.| -  | Partial update of the resoruce, all parameters from put work as well. | Delete one specific card deck including every attached card. |
| `/cardDecks/x/cards` | Retrieve cards from the card deck. Can contain the `?start=x` parameter that specifies the start of the returned sublist (e.g. `?start=2` starts the sublist at element[3]. May also specify `?size=y` to limit the number of returned entities. (e.g. `?size=1` returns exactly one element). Both can be combined.| | | | | |
### Categories
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| `/categories` | Retrieve a list of all categories that are available. Usable URL params: `?root=true` to get all nodes that have no parent and are thus root directories. | - | Create a new category. | - | - |
| `/categories/x` | Retrieve a category by id. | Update a category completely. Usable URL parameters: `append={true/false}` to append the list or replace it. | -  | Partial update of the category, all parameters from put work as well. | -|
| `/categories/x/children` | Retrieve children of a specific category | | | | | |
| `/categories/x/decks` | Retrieve decks of a specific category | | | | | |
for more working routing look at the [routes](conf/routes).

## German Tutorial/Insights
If you're interested in reading about the things we use, there is a dev log file that describes problems and other topics we came across in German: [here](https://github.com/FWidm/FlashCardsAPI/blob/master/_Docs/PlayDokuFabian.md).


## Example Calls
### Users
**Elements needed for REST calls:**
- **String**: name, password, email
- **int**: rating

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
- **String**: name, description
- **Array of users**: users

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
- **int**: rating
- **question** - either a complete object as seen here or the `questionId`
- **answers** - an array of complete answer objects or `answerIds`

**Example**: Creates one new card with new question, answers, tags all from the same author (with userId=1).
```json
{
    "rating": 0,
    "question": {
      "questionText": "hello world!",
      "uri": "www.google.de",
      "author": {
        "userId": 1
      }
    },
    "answers": [
      {
        "answerText": "answer",
        "answerHint": "hint",
        "uri": "www.answer.com",
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
- **Author**: userId
- **Answer**: answerId **or** a complete new answer
- **Flashcard**: Flashcard
- **ratingModifier**: how should the rating value change (e.g. +1,-1)

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

### CardDecks

**Example**: Create a Carddeck with a specific cardDeckName, a description saying "", two FlashCards (can be an empty list), one UserGroup (mandatory).
```json
{
    "cardDeckName": "{{cardDeckName}}",
    "cardDeckDescpription": "",
    "cards": [
              {
        "flashcardId": 1
      },
      {
        "flashcardId": 2
      }
    ],
    "userGroup":{
        "groupId":1
    }
}
```

### Categories
The hierarchy of the categories looks like this:
```
root (parent=null)
  |-level 1 (parent=root)
    |-level 2 (parent=level 1)
      |-level 3 (parent=level 2)
        |-level 4 (parent=level 3)
  |-level 1' (parent=root)
  |-level 1'' (parent=root)
    |-level 2' (parent=level 1'')
```
But there is no rule to only have one root, more than one root directory is planned.
**Example**: creates a new category with a name, an array of carddecks and a parent that decides the tree structure.
```json
{
  "categoryName": "Name",
  "cardDecks": [
    {
      "cardDeckId": 1
    }],
  "parent": {"categoryId": 1}
}
```
### Further Information
see the Postman Collection for more detailled information: [here](https://github.com/FWidm/FlashCardsAPI/blob/master/_Docs/FlashCards.postman_collection.json).

Also refer to the [JsonKeys.java](https://github.com/FWidm/FlashCardsAPI/blob/master/app/util/JsonKeys.java) for more inforations about naming conventions.
## Expected/Possible JSON for the different Objects
Refer to the [JsonKeys.java](/app/util/JsonKeys.java) File, it containis all named JsonProperties for each Class/Model.
