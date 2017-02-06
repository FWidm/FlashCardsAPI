# Authorization for FlashCards API
(&#10007;= no auth needed | &#10003;=Auth needed via `Authorization: Beater $Token` | &#10003;+=Auth plus additional rights needed)

Operations are noted in two ways. First one is `*` which is short hand for a call on all entities `GET /users`. Second one is `/x` it is short hand for `GET /users/x` where x is an id and describes getting ONE specific entity.
___

## Progress
- Finished
  - User
  - Flashcards (Answer, Tag, Question)
___

## Table
| Operation | Users | FlashCards | Answers | Questions | Tags | UserGroups | CardDecks | Categories | Ratings |
| --------- | ----- | ---------- | ------- | --------- | ---- | ---------- | --------- | ---------- | ------ |
| `GET` * | ? | ? | ? | ? | ? | ? | ? | ? | ? |
| `GET` /x | &#10007; |&#10007; | &#10007; | &#10007; | &#10007; | &#10007; | &#10007; | &#10007; | &#10007; |
| `POST` * | &#10007; | &#10003; | &#10003; | &#10003; | &#10003; | &#10003; | &#10003; | &#10003; |  &#10003; |
| `PUT/PATCH` * | &#10003;+ | &#10003;+ | &#10003;<sup>1</sup> | &#10003;+ | &#10003;<sup>1</sup>  | &#10003;+ | &#10003;+ | &#10003;+ |  &#10003; |
| `DELETE` /x |  &#10003;+ | &#10003;+ | <sup>2</sup> | <sup>2</sup> | <sup>2</sup>  | &#10003;+ | &#10003;+ | &#10003;+ | &#10003; |

___
<sup>1</sup> Answers and Tags can always be appended when using the URL argument `?append=true` even when the user is neither author nor has rights to change things.

<sup>2</sup> Answers, Tags and Question are fixed for a card. They do not have their own way to access them, as such the entry for flashcards is the relevant one.
