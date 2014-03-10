package model

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.Play.current
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDateTime

case class Message(
  val title: String,
  val sender: String,
  val recipient: String,
  val content: Option[String],
  val status: String = "new",
  val createdOn: Option[BSONDateTime] = None,
  val id: Option[BSONObjectID] = None) {
}

object Message {

  import play.modules.reactivemongo.json.BSONFormats._
  implicit val messageFormat = Json.format[Message]

  def db = ReactiveMongoPlugin.db
  def collection: JSONCollection = db.collection[JSONCollection]("Message")

  def save(message: JsValue) = {
    collection.insert[JsValue](message)
  }

  //  def save(message: Message) = {
  //    collection.insert(message)
  //  }

  def findById(id: String): Future[Option[JsValue]] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    collection.find(query).one[JsValue]
  }

  def findByRecipient(recipient: String): Future[List[JsObject]] = {
    val cursor: Cursor[JsObject] = collection
      .find(Json.obj("recipient" -> recipient))
      .sort(Json.obj("created_on" -> -1))
      .cursor[JsObject]
    cursor.collect[List]()
  }

  def update(messageId: String, message: JsObject) = {
    val selector = Json.obj("_id" -> Json.obj("$oid" -> messageId))

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "title" -> (message \ "title"),
        "content" -> (message \ "content"),
        "status" -> (message \ "status")))

    collection.update(selector, modifier)
  }

  def delete(messageId: String) = {
    val selector = Json.obj("_id" -> Json.obj("$oid" -> messageId))

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "status" -> "deleted"))

    collection.update(selector, modifier)
  }
}