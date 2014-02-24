package model

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import play.api.libs.json.Json
import play.api.libs.json.JsString
import reactivemongo.bson.BSONString
import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.Future
import reactivemongo.api.QueryOpts

case class Message(
  val title: String,
  val sender: String,
  val recipient: String,
  val content: Option[String],
  val status: String,
  val createdOn: Option[LocalDateTime] = Some(new LocalDateTime()),
  val id: Option[String] = Some(BSONObjectID.generate.stringify)) {

  /**
   * Converts createdAt to printable date time.
   */
  def createdAtISO(): String = ISODateTimeFormat.dateTimeNoMillis().print(createdOn.get)

}

object Message {
  implicit val readsJodaLocalDateTime = Reads[LocalDateTime](js =>
    js.validate[String].map[LocalDateTime](dtString =>
      LocalDateTime.parse(dtString, ISODateTimeFormat.dateTimeNoMillis())))

  implicit val writesJodaLocalDateTime = Writes[LocalDateTime](dt =>
    JsString(ISODateTimeFormat.dateTimeNoMillis().print(dt)))

  implicit val messageWrites = Json.writes[Message]
  implicit val messageReads = Json.reads[Message]

  // Does the mapping BSON <-> Scala object
  implicit object MessageBSONReader extends BSONDocumentReader[Message] {
    def read(doc: BSONDocument): Message = Message(
      doc.getAs[String]("title").get,
      doc.getAs[String]("sender").get,
      doc.getAs[String]("recipient").get,
      doc.getAs[String]("content"),
      doc.getAs[String]("status").get,
      doc.getAs[BSONDateTime]("created_on").map(bdt => new LocalDateTime(bdt.value)),
      doc.getAs[BSONObjectID]("_id").map(_.stringify))
  }

  implicit object MessageBSONWriter extends BSONDocumentWriter[Message] {
    def write(message: Message) = {
      BSONDocument(
        "_id" -> BSONObjectID(message.id.get),
        "title" -> message.title,
        "sender" -> message.sender,
        "recipient" -> message.recipient,
        "content" -> message.content.map(BSONString(_)),
        "status" -> message.status,
        "created_on" -> BSONDateTime(message.createdOn.get.toDateTime().getMillis()))
    }
  }

  def db = ReactiveMongoPlugin.db
  def collection = db.collection[BSONCollection]("Message")

  def save(message: Message) = {
    collection.insert(message)
  }

  def findById(id: String): Future[Option[Message]] = {
    collection.find(BSONDocument("_id" -> BSONObjectID(id))).one[Message]
  }

  def findAll(recipient: Option[String]) = {
    val query = recipient match {
      case Some(recipient) => BSONDocument("recipient" -> recipient)
      case _ => BSONDocument()
    }
    collection.find(query).sort(BSONDocument("created_on" -> -1)).options(QueryOpts().batchSize(10)).cursor[Message].collect[List]()
  }

  def update(message: Message) = {
    val selector = BSONDocument("_id" -> BSONObjectID(message.id.get))
    		
    val modifier = BSONDocument(
      "$set" -> BSONDocument(
        "title" -> message.title,
        "content" -> message.content,
        "status" -> message.status
      )
    )
    
    collection.update(selector, modifier)
  }
  
  def delete(messageId: String) = {
    val selector = BSONDocument("_id" -> BSONObjectID(messageId))

    val modifier = BSONDocument(
      "$set" -> BSONDocument(
        "status" -> BSONString("deleted")))

    collection.update(selector, modifier)
  }
}