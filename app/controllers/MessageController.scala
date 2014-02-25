package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import model.Message
import play.api.libs.json.Json
import play.api.libs.json.JsError
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONDateTime
import java.util.Date

object MessageController extends Controller {
  def findById(messageId: String) = Action.async {
    implicit req =>
      Message.findById(messageId).map {
        case Some(message) => {
          render {
            case Accepts.Json() => {
              Logger.debug(message.toString);
              Ok(Json.toJson(message)).as(JSON)
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
            Ok(Json.toJson(messages)).as(JSON)
          }
          case _ => NotAcceptable
        }
      }
  }

  def save() = Action.async(parse.json) { implicit request =>
    request.body.validate[Message].map {
      case (message) => {
        val id = BSONObjectID.generate
        val dateTime = BSONDateTime((new Date).getTime)
        val json = request.body.as[JsObject] ++ Json.obj("_id" -> id,
          "created" -> dateTime)
        Message.save(json).map { lastError =>
          if (lastError.ok) {
            Logger.debug(lastError.toString())
            Created.withHeaders(
              "Location" -> routes.MessageController.findById(id.stringify).absoluteURL())
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
    request.body.validate[Message].map {
      case (message) => {
        Message.update(messageId, message).flatMap { lastError =>
          if (lastError.ok) {
            Logger.debug(lastError.toString())
            Message.findById(messageId).map { messages =>
              render {
                case Accepts.Json() => {
                  Ok(Json.toJson(messages)).as(JSON)
                }
                case _ => NotAcceptable
              }
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