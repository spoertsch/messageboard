package controllers

import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.concurrent._
import model.Message
import play.api.Logger
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json.__
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONObjectID
import reactivemongo.api._
import reactivemongo.bson._

object MessageController extends Controller {
  def findById(messageId: String) = Action.async {
    implicit req =>
      Message.findById(messageId).map {
        case Some(message) => {
          render {
            case Accepts.Json() => {
              Logger.debug(message.toString);

              message.transform(outputMessage).map { jsonp =>
                Ok(Json.toJson(jsonp)).as(JSON)
              }.recoverTotal { e =>
                BadRequest((JsError.toFlatJson(e)))
              }

            }
            case _ => NotAcceptable
          }
        }
        case _ => NotFound
      }
  }

  def findAllByRecipient(userId: String) = Action.async {
    implicit req =>
      Message.findByRecipient(userId).map { messages =>
        render {
          case Accepts.Json() => {
            //            val result = for (m <- messages) yield {
            //              m.transform(outputMessage).get
            //            }
            Ok(Json.toJson(messages.map(_.transform(outputMessage).get))).as(JSON)
          }
          case _ => NotAcceptable
        }
      }
  }

  val validateMessage: Reads[JsObject] = (
    (__ \ 'title).json.pickBranch and
    (__ \ 'sender).json.pickBranch and
    (__ \ 'recipient).json.pickBranch and
    (__ \ 'content).json.pickBranch).reduce

  val validateUpdateMessage: Reads[JsObject] = (
    (__ \ 'title).json.pickBranch and
    (__ \ 'sender).json.pickBranch and
    (__ \ 'recipient).json.pickBranch and
    (__ \ 'content).json.pickBranch and
    (__ \ 'status).json.pickBranch).reduce

  /** Generates a new ID and adds it to your JSON using Json extended notation for BSON */
  val generateId = (__ \ '_id \ '$oid).json.put(JsString(BSONObjectID.generate.stringify))

  /** Generates a new date and adds it to your JSON using Json extended notation for BSON */
  val generateCreated = (__ \ 'created \ '$date).json.put(JsNumber((new java.util.Date).getTime))

  val generateStatusNew = (__ \ 'status).json.put(JsString("new"))

  /** Updates Json by adding both ID and date */
  val addMongoIdAndDate: Reads[JsObject] = __.json.update((generateId and generateCreated and generateStatusNew).reduce)

  //  val toObjectId = OWrites[String]{ s => Json.obj("_id" -> Json.obj("$oid" -> s)) }
  //  val fromObjectId = (__ \ '_id).json.copyFrom( (__ \ '_id \ '$oid).json.pick )
  val fromObjectId = __.json.update((__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick)) andThen (__ \ '_id).json.prune
  val fromCreated = __.json.update((__ \ 'created).json.copyFrom((__ \ 'created \ '$date).json.pick))

  val outputMessage =
    fromCreated andThen fromObjectId

  def save() = Action.async(parse.json) { implicit request =>
    request.body.transform(validateMessage andThen addMongoIdAndDate).map {
      message =>
        {
          //        val id = BSONObjectID.generate
          //        val dateTime = BSONDateTime((new Date).getTime)
          //        val json = message
          Logger.debug("New message: " + message)
          Message.save(message).map { lastError =>
            if (lastError.ok) {
              Logger.debug(lastError.toString())

              val id = (message \ "_id" \ "$oid").as[String]
              Created.withHeaders(
                "Location" -> routes.MessageController.findById(id).absoluteURL())
            } else {
              InternalServerError(s"Mongo LastError: $lastError")
            }
          }
        }
    }.recoverTotal {
      e => Future.successful(BadRequest("Detected error:" + JsError.toFlatJson(e)))
    }
  }

  def update(messageId: String) = Action.async(parse.json) { implicit request =>
    request.body.transform(validateUpdateMessage).map {
      case (message) => {
        Logger.debug("message: " + message)
        Message.update(messageId, message).flatMap { lastError =>
          if (lastError.ok) {
            Logger.debug(lastError.toString())
            Message.findById(messageId).map {
              case Some(foundMessage) => {
                render {
                  case Accepts.Json() => {
                    foundMessage.transform(outputMessage).map { jsonp =>
                      Ok(Json.toJson(jsonp)).as(JSON)
                    }.recoverTotal { e =>
                      BadRequest((JsError.toFlatJson(e)))
                    }
                  }
                  case _ => NotAcceptable
                }
              }
              case _ => NotFound
            }
          } else {
            Future.successful(InternalServerError(lastError.message))
          }
        }
      }
    }.recoverTotal {
      e => Future.successful(BadRequest("Detected error:" + JsError.toFlatJson(e)))
    }
  }

  def delete(messageId: String) = Action.async {
    implicit req =>
      Message.delete(messageId).map { lastError =>
        if (lastError.ok) {
          Logger.debug(lastError.toString())
          Ok
        } else {
          InternalServerError(lastError.message)
        }
      }
  }
}