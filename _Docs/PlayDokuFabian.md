# Play Framework - FlashCards API
___
![FlashCards Logo](img/flash_icon_250.png)
___
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
            //node conains now the single vals in th aray [v1,v2,..]
            Answer tmpA = parseAnswer(node);
            answers.add(tmpA);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
```
### JSON Property Names
Können in der Klasse mittels `@JsonProperty(<Strng>)` festgeleg wrden. Wenn eine Konstante als String eingesetzt wird, kannes nötig sein die Klasse erneut zu speihern, damit sich die Änderung durchsetzt.
___
## JPA/EBEAN
In diesem [Wikibook](https://en.wikibooks.org/wiki/Java_Persistence) gibt es eine schöne Einleitung über die Elementaren Beziehungen, nicht alle der vorgestellten Operationen funktionieren mit jeder konkreten ORM Implementierung, die Relationen aber definitiv. Wir benutzen zur Zeit Ebean, Link zur  [Dokumentation](https://en.wikibooks.org/wiki/Java_Persistence).
### Relationen
Können uni- oder bidirektional sein, und ein Objekt ist immer der Owner der Relation. Im folgenden werden benutzte Relationen kurz vorgestellt:
k``````````````
### OneToOne (1:1)
Ermöglicht eine 1:1 Vebindung und wird dazu genutzt um `FlashCard` Attribute von den `Question` Attributen zu trennen, obwohl beide voneinander voll abhängig sind.

In `FlashCard` finden sich dann folgende Annotationen über dem `Question`-Objekt.
```java
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="question_id", referencedColumnName = JsonKeys.QUESTION_ID)
    private Question question;
```
Damit hat die `FlashCard` die `ID`der Frage in ihrer Datenbank und es entsteht die unidirektionale 1:1 Verbindung: `FlashCard` &rarr; `Question`. Da die Frage meist sowieso nach der Karte geladen wird reicht diese Relation aus.

### OneToMany/ManyToOne (1:n, n:1)
`FlashCard`: Keine Spalte in der DB mit ids, aber durch das mappedby erhält die Klasse direkt alle zugehörigen Antworten.
```java
@OneToMany(cascade=CascadeType.ALL,mappedBy = "card")
private List<Answer> answers;
```
`Answer`: Eine Spalte mit `parent_card_id` in der eigenen Tabelle, die eine Antwort genau einer Frage zuordnet.
```java
@ManyToOne
@JoinColumn(name="parent_card_id")
@JsonIgnore
private FlashCard card;
```

### ManyToMany (N:M)
`Flashcard` besitzt die Relation und definiert die `JoinTable`, diese erzeugt eine Tabelle in der Datenbank mit zwei Spalten`||card_id|tag_id||`, die die N:M Zuordnung ermöglicht. Ohne Cascade ist es jetzt nötig vorher zu definieren, was mit den Tags passiert wenn die Klasse gelöscht wird. Entweder entfernt man von allen Tags die Referenz auf dieses Objekt. Ein Löschen des Tags wäre auch möglich aber macht wenig Sinn.
```java
@ManyToMany
@JoinTable(name="join_cards_tag",
        joinColumns = @JoinColumn(name="card_id", referencedColumnName=JsonKeys.FLASHCARD_ID),
        inverseJoinColumns = @JoinColumn(name="tag_id", referencedColumnName = JsonKeys.TAG_ID))
private List<Tag> tags;
```

Die Gegenseite `Tag` muss jetzt nur angeben wie sie gemappt werden soll. Wird ein Tag gelöscht verschwinden auch zugehörige Relationen in der `JoinTable`.
```java
@ManyToMany(mappedBy = "tags")
@JsonProperty(JsonKeys.TAG_CARDS)
private List<FlashCard> cards;
```
___
##Postman als REST-API Tester
Postman erlaubt es uns einfache HTTP Requests mit verschiedensten Methoden, Headern und Body an eine beliebige URL schicken.

Zusätzlich erlaubt es ein abspeichern von Requests in Collections und das Anlegen von Umgebungsvariablen, die dann wiederum in den Requests genutzt werden können. **Collections** und  **Umgebungsvariablen** lassen sich als Json Datei exportieren. Das ermöglicht zusätzlich das teilen dieser Dateien im Team. Für die FlashCards API gibt es beide files zum download.

 - Collection: [Link](FlashCardsApi.postman_collection.json)
 - Environment: [Link](FlashCardsApi.postman_environment.json)
### Tutorial für das Nutzen von Variablen:
[Link](http://blog.getpostman.com/2014/02/20/using-variables-inside-postman-and-collection-runner/)