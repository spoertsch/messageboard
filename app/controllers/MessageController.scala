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

object MessageController extends Controller {
  def findById(messageId: String) = Action.async {
    implicit req =>
      Message.findById(messageId).map(message =>
        message match {
          case Some(message) => {
            render {
              case Accepts.Json() => {
                Ok(Json.toJson(message)).as(JSON)
              }
              case _ => NotAcceptable
            }
          }
          case _ => NotFound
        })
  }

  def findAllByRecipient(userId: String) = Action.async {
    implicit req =>
      Logger.debug(req.headers.toString)
      Message.findAll(Some(userId)).map { messages =>
        render {
          case Accepts.Json() => {
            Ok(Json.toJson(messages)).as(JSON)
          }
          case _ => NotAcceptable
        }
      }
  }

  implicit val rds = (
    (__ \ 'title).read[String] and
    (__ \ 'sender).read[String] and
    (__ \ 'recipient).read[String] and
    (__ \ 'content).read[Option[String]]) tupled

  def save() = Action.async(parse.json) { implicit request =>
    request.body.validate[(String, String, String, Option[String])].map {
      case (title, sender, recipient, content) => {
        val newMessage = Message(title, sender, recipient, content)
        Message.save(newMessage).map { lastError =>
          if (lastError.ok) {
            Logger.debug(lastError.toString())
            //            Ok(Json.toJson(newMessage)).as(JSON)
            Created.withHeaders(
              "Location" -> routes.MessageController.findById(newMessage.id.get).absoluteURL())
          } else {
            InternalServerError(lastError.message)
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
        Message.update(message).map { lastError =>
          if (lastError.ok) {
            Logger.debug(lastError.toString())
            Ok(Json.toJson(message)).as(JSON)
          } else {
            InternalServerError(lastError.message)
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