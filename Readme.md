# FlashCards RESTful API
## Current HTTP Methods that should be working:
```Scala
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
PATCH	/users/:id			controllers.UserController.partiallyUpdateUser(id:Long)

DELETE 	/users/:id			controllers.UserController.deleteUser(id:Long)

#Groups
GET 	/groups				controllers.UserGroupController.getUserGroupList
GET 	/groups/:id			controllers.UserGroupController.getUserGroup(id:Long)
PUT 	/groups/:id			controllers.UserGroupController.updateUserGroup(id:Long)
POST 	/groups				controllers.UserGroupController.addUserGroup
DELETE	/groups/:id			controllers.UserGroupController.deleteUserGroup(id:Long)

#Flashcards
GET     /cards              controllers.FlashCardController.getFlashCardList
POST    /cards              controllers.FlashCardController.addFlashCard
DELETE /cards/:id           controllers.FlashCardController.deleteFlashCard(id:Long)
```

## Expected/Possible JSON for the different Objects
##### User
Currently creation works without specifying a group, which sets the group of the user to null. The group can then be set via `PATCH` or `PUT` if it is specified like this:
```Json  
{
    "name": "hello",
    "password": "passwörd",
    "email": "hello1@world.com",
    "rating": 1
    "group":{
                "groupId": 3,
                "name": "y",
                "description": "y"
            }
}
```
##### Group
```json  
{
    "groupId": 1,
    "name": "y",
    "description": "y"
 }
```
##### Group
```json
{
      "groupId": 3,
      "name": "y",
      "description": "y"
    }
```


##### Card
```json  
  {
    "id": 4,
    "rating": 0,
    "created": "2016-06-22 10:10:06 UTC",
    "lastUpdated": "2016-06-22 10:10:06 UTC",
    "question": {}
    "answers": [{},{},..]
    ],
    "author": { <see user> },
    "multipleChoice": false,
    "marked": false,
    "selected": false
  }
]
```
##### Questions
```json
{
      "id": 4,
      "question": "Question",
      "mediaURI": null,
      "author": {
        <see user>
      }
}
```

##### Answers
##### Answer
```json  
 {
        "id": 4,
        "answerText": "Answer",
        "hint": "No hint available - 404",
        "mediaURI": null,
        "author": { <see user> },
        "created": "2016-06-22 10:10:06 UTC",
        "rating": 0
 }
```