# FlashCards RESTful API
## Methods
| Resource | GET | PUT | POST | PATCH | DELETE|
| -------- | --- | --- | ---- | ----- | ----- |
| /users | retrieve a list of users. Can be filtered via url params ?name=x or ?email=y| - | creates a new User | - | - |
| /users/5 | retrieve the details of the specified user | update the complete ressource with this id | - | partial update of the resource | deletes the specified resource |

for more working routing look at the [routes](conf/routes).
## German Turoial/Insights
If you're interested in reading about the things we use, there is a dev log file that describes problems and other topics we came across in german: [here]((https://github.com/FWidm/FlashCardsAPI/blob/master/_Docs/PlayDokuFabian.md)).


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
see the Postman Collection for more detailled information: [here](https://github.com/FWidm/FlashCardsAPI/blob/master/_Docs/FlashCards.postman_collection.json).

Also refer to the [JsonKeys.java](https://github.com/FWidm/FlashCardsAPI/blob/master/app/util/JsonKeys.java) for more inforations about naming conventions.
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
    "questionText": {},
    "answers": [{},{}],
    "author": { /*see user*/ },
    "multipleChoice": false,
    "tags":[{},{}]
  }
```
#### Questions
```json
{
      "id": 4,
      "questionText": "Question",
      "mediaURI": null,
      "author": {
        /*see user*/
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
        "author": { /*see user*/ },
        "created": "2016-06-22 10:10:06 UTC",
        "rating": 0
 }
```


