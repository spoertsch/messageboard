MessageBoard
=====================================
This is a project should demonstrate the creation of a REST API. The API is consumed by a AngularJS frontend created in another project.

In order to run the application you need to have a MongoDB up and running.

### API

##### Get message by id
```bash
curl --header "Accept: application/json" --request GET http://localhost:9000/message/<messageId>
```

##### Get all messages by an user
```bash
curl --header "Accept: application/json" --request GET http://localhost:9000/message/user/<user> -v
```

##### Create new message
```bash
curl --header "Content-type: application/json" --request POST --data '{ "title": "Title", "sender": "sender", "recipient": "recipient", "content":"k"}' http://localhost:9000/message -v
```

##### Update message
```bash
curl --header "Content-type: application/json" --request PUT --data '{"title":"Updated Title","sender":"sender","recipient":"recipient","content":"Content","status":"new"}' http://localhost:9000/message/<messageId> -v
```

##### Delete message by id (only sets status to deleted)
```bash
curl --request DELETE http://localhost:9000/message/<messageId>
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
