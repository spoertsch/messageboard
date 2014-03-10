MessageBoard
=====================================
This is a project should demonstrate the creation of a Json REST API with the Play Framework. The API is consumed by a AngularJS frontend created in another project.

In order to run the application you need to have a MongoDB up and running.

### API

##### GET message by id: /messages
```bash
curl --header "Accept: application/json" --request GET http://localhost:9000/messages/<messageId>
```

##### GET all messages by an user: /messages/user/{user}
```bash
curl --header "Accept: application/json" --request GET http://localhost:9000/messages/user/<user> -v
```

##### POST new message: /messages
```bash
curl --header "Content-type: application/json" --request POST --data '{ "title": "Title", "sender": "sender", "recipient": "recipient", "content":"k"}' http://localhost:9000/messages -v
```

##### PUT (update) message: /messages/{messageId}
```bash
curl --header "Content-type: application/json" --request PUT --data '{"title":"Updated Title","sender":"sender","recipient":"recipient","content":"Content","status":"new"}' http://localhost:9000/messages/<messageId> -v
```

##### DELETE message by id (only sets status to deleted): /messages/{messageId}
```bash
curl --request DELETE http://localhost:9000/messages/<messageId>
```

### Resources
* [Play Framework](http://www.playframework.com/)
* [Play Framework documentation](http://www.playframework.com/documentation/2.2.x/Home)
* [Play Framework Usergroup](https://groups.google.com/forum/#!forum/play-framework)
* [Reactive Mongo](http://reactivemongo.org/)
* [Reactive Mongo Usergroup](https://groups.google.com/forum/#!forum/reactivemongo)
* [Play Reactive Mongo](https://github.com/ReactiveMongo/Play-ReactiveMongo)
* [MongoDB](http://www.mongodb.org/)
* [Scala](http://www.scala-lang.org/)
* [Typesafe](http://typesafe.com/)
* [Reactive Manifesto](http://www.reactivemanifesto.org/)
* [JSON coast-to-coast](http://mandubian.com/2013/01/13/JSON-Coast-to-Coast/#sample)
* [ReactiveMongo 0.9 and Lossless Persistence](http://matthiasnehlsen.com/blog/2013/04/26/data-model-upgrade/)
* [Playframework 2.2 configurable cors filter] (https://gist.github.com/jeantil/7214962)
