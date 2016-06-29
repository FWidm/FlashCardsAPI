# Play Framework
[Play Framework Website](https://www.playframework.com/)
___
## Allgemeines für Play v2.2
### Projekt anlegen
```bash
$ cd /PathToProbject/
$ play new Project // activator new Project
```
### Projekt starten
```bash
$ cd /PathToProbject/Project
$ play run // activator run
```
### Projekt in IDEs nutzen
```bash
$ cd /PathToProbject/Project
$ play eclipse // activator eclipse
```
Alternativ einfach in Intellij importieren.
Für neuere Versionen gibt es einen [guide](https://www.playframework.com/documentation/2.6.x/IDE).
### Routen Anlegen
Kommentare werden mit `#`gestartet. Es wird ein Tripel aus (Method, Target, Methode) erwartet. Methoden müssen  eine [`play.api.mvc.Action`](https://www.playframework.com/documentation/2.5.x/api/java/play/mvc/Action.html) zurückgeben wie bspw. `ok`.
Variablen in der URL werden mit Doppelpunkt begonnen: `:id`. Es scheint nicht möglich die Routen zu überladen bspw. `/users/:id` und `/users/:email`.
```
GET		        /users				controllers.UserController.getUserList
GET		        /users/:id         	controllers.UserController.getUser(id:Long)
GET		        /users/e/:email     controllers.UserController.getUserByEmail(email:String)
POST	        /users				controllers.UserController.addUser
PUT		        /users/:id			controllers.UserController.updateUser(id:Long)
DELETE 	        /users/:id			controllers.UserController.deleteUser(id:Long)
```

Es ist auch möglich dynamische Pfade anzulegen, die mehrere Verzeichnisse umfassen:
```
GET   /files/*name          controllers.Application.download(name)
```

Oder die dynamischen Teile mit regulären Ausdrücken auszurüsten:
```
GET   /items/$id<[0-9]+>    controllers.Items.show(id: Long)
```

Zusätzlich lassen sich auch weitere Routen nach folgendem Schema anlegen:
```
GET /items/$id/description controllers.Items.getDescription(id:Long)
```
#### PATCH vs. PUT
Patch wird zum partiellen update verwendet, put nur für die komplette Ressource [link]()
### Validieren von Werten
Es ist möglich sicher zu stellen, dass bestimmte Variablen einer Klasse bestimmte Werte besitzen. 
- `@Email` - ermöglicht es per `EmailValidator` zu checken ob ein Wert eine valide email ist. `emailValidator.isValid(val)`.
- `@MinLength(x)` ermöglicht es sicherzustellen, dass die Werte eine Mindestlänge einhalten, x muss eine Konstante sein. (Prüfen: `minLengthValidator.isValid(val)`
___
# HTTP Status Code Informationen
Gute Übersichtsseite [hier](http://www.restapitutorial.com/httpstatuscodes.html). Grafik: 

![alt text](https://camo.githubusercontent.com/4e15cccf2a9277dcca2c8824092547dee7058744/68747470733a2f2f7261776769746875622e636f6d2f666f722d4745542f687474702d6465636973696f6e2d6469616772616d2f6d61737465722f6874747064642e706e67 "Übersicht")
Quelle: [HTTP Decision Diagram](https://github.com/for-GET/http-decision-diagram/)

___
## JSON Nutzung
### Json im Body eines Requests
Um den JSON Body zu erhalten braucht die Methode einen `@BodyParser.Of(BodyParser.Json.class)`. Dieser erlaubt den Zugriff auf den mitgeschickten JSON Body. Es wird vorrausgesetzt, dass `Content-Type: application/json` beim Request gesetzt ist.  
Um auf diese Daten zuzugreifen, kann jetzt der Body in der Funktion zugegriffen werden `JsonNode json = request().body().asJson();` um daraufhin dann per `json.findPath(<attribut>)` auf die einzelnen Attribute zuzugreifen. 

**Beispiel**: Das hinzufügen eines Nutzers über ein `HTTP PUT` sieht wie folgt aus. Im Body des Requests wird folgendes JSON mitgeschickt:
```json
    {
    "name":"anon",
    "email":"email@example.com",
    "password":"asdfgh",
    }
```
Um daraufhin in der folgenden Methode einen Nutzer anhand der genannten Daten anzulegen.
```java
    @BodyParser.Of(BodyParser.Json.class)
	public static Result addUser() {
		JsonNode json = request().body().asJson();
		String name = json.findPath("name").textValue();
		String email = json.findPath("email").textValue();
		String password = json.findPath("password").textValue();
		User u = new User(name, email, password, rating);
		u.save();
	}
```
### Neues JSON Objekt anlegen
```java
public static ObjectNode prepareJsonStatus(int statuscode,
			String description) {
		ObjectNode result = Json.newObject();
		result.put("statuscode", statuscode);
		result.put("description", description);
		return result;
	}
```
### Umwandlung von Java Objekten in JSON mittels Jackson
Im  folgenden werden beliebige Java Objekte in JSON umgewandelt. Das `SimpleDateFormat` soll hierbei helfen eine einheitliche Angabe für Timestamps in JSON zu erzeugen, ist grundsätzlich aber nur nötig, falls Timestamps bei Objekten benötigt werden.
```java
public static JsonNode getJson(Object o) {
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat outputFormat = new SimpleDateFormat(dateformat);
		mapper.setDateFormat(outputFormat);
		Json.setObjectMapper(mapper);
		return Json.toJson(o);
	}
```
### Parsen von JSON Arrays
```java
for (JsonNode node : answersNode) {
        try {
            Answer tmpA = parseAnswer(node);
            answers.add(tmpA);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
```
___
## JPA/EBEAN
In diesem [Wikibook](https://en.wikibooks.org/wiki/Java_Persistence) gibt es eine schöne Einleitung über die Elementaren Beziehungen, nicht alle der vorgestellten Operationen funktionieren mit jeder konkreten ORM Implementierung, die Relationen aber definitiv. Wir benutzen zur Zeit Ebean, Link zur  [Dokumentation](https://en.wikibooks.org/wiki/Java_Persistence).

