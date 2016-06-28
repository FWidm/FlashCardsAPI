# FlashCards RESTful API
## Current HTTP Methods that should be working:
```
#Flash Cards API
#Flash Cards API
GET     /testCards          controllers.HomeController.testCards
GET     /testGroups         controllers.HomeController.testGroups
GET     /test               controllers.HomeController.test

#Users
GET		/users				controllers.UserController.getUserList
GET		/users/:id          controllers.UserController.getUser(id:Long)
GET		/users/e/:email     controllers.UserController.getUserByEmail(email:String)
POST	/users				controllers.UserController.addUser
PUT		/users/:id			controllers.UserController.updateUser(id:Long)
PATCH	/users/:id			controllers.UserController.updateUser(id:Long)

DELETE 	/users/:id			controllers.UserController.deleteUser(id:Long)

#Groups
GET 	/groups				controllers.UserGroupController.getUserGroupList
GET 	/groups/:id			controllers.UserGroupController.getUserGroup(id:Long)
PUT 	/groups/:id			controllers.UserGroupController.updateUserGroup(id:Long)
PATCH 	/groups/:id			controllers.UserGroupController.updateUserGroup(id:Long)
POST 	/groups				controllers.UserGroupController.addUserGroup
DELETE	/groups/:id			controllers.UserGroupController.deleteUserGroup(id:Long)

#Flashcards
GET     /cards              controllers.FlashCardController.getFlashCardList
GET     /cards/:id          controllers.FlashCardController.getFlashCard(id:Long)
GET     /cards/:id/questionText controllers.FlashCardController.getQuestion(id:Long)
GET     /cards/:id/answers?size  controllers.FlashCardController.getAnswers(id:Long)
GET     /cards/:id/author   controllers.FlashCardController.getAuthor(id:Long)
POST    /cards              controllers.FlashCardController.addFlashCard
DELETE  /cards/:id          controllers.FlashCardController.deleteFlashCard(id:Long)
PATCH   /cards/:id          controllers.FlashCardController.updateFlashCard(id:Long)
PUT     /cards/:id          controllers.FlashCardController.updateFlashCard(id:Long)
```
## Example Calls
### Users
**Elements needed for REST calls:**
-*String*: name, password, email
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

### Further Information
see the Postman Collection for more detailled information: [here](https://github.com/FWidm/FlashCardsAPI/blob/master/_PostManCollection/FlashCards.postman_collection.json).
## Expected/Possible JSON for the different Objects
#### User
Currently creation works without specifying a group, which sets the group of the user to null. The group can then be set via `PATCH` or `PUT` if it is specified like this:
```Json
{
    "name": "hello",
    "password": "passwörd",
    "email": "hello1@world.com",
    "rating": 1,
    "group":{
                "groupId": 3,
                "name": "y",
                "description": "y"
            }
}
```
#### Group
```json
{
      "groupId": 3,
      "name": "y",
      "description": "y",
      "users":[{"userID":1}, {}]
    }
```


#### Card
```json
  {
    "id": 4,
    "rating": 0,
    "created": "2016-06-22 10:10:06 UTC",
    "lastUpdated": "2016-06-22 10:10:06 UTC",
    "questionText": {},
    "answers": [{},{}],
    "author": { <see user> },
    "multipleChoice": false,
    "marked": false,
    "selected": false
  }
]
```
#### Questions
```json
{
      "id": 4,
      "questionText": "Question",
      "mediaURI": null,
      "author": {
        <see user>
      }
}
```

#### Answers
```json
 {
        "id": 4,
        "answerText": "Answer",
        "hintText": "No hintText available - 404",
        "mediaURI": null,
        "author": { <see user> },
        "created": "2016-06-22 10:10:06 UTC",
        "rating": 0
 }
```