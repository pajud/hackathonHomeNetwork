package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import play.api.libs.streams._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    //Ok(ids.mkString(" "))
    Ok(views.html.main("MAIN", ids.toList))
  }

  var ids = Set[Int]()

  val random = scala.util.Random

  case class Measure(date : java.util.Date, measure : Float)

  var measures = Map[Int, List[Measure]]()
  var classes = Map[Int, Int]()
  var names = Map[Int, String]()
  var flows = Map[Int, Flow[String, String, _]]()

  def register = Action {
    var id = random.nextInt
    while (ids contains id){
      id = random.nextInt
    }
    ids += id
    Ok(id + "").withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")

  }

  def measureData(id : Int, data : Float) = Action {
    if (ids contains id) {
      val currentTime = java.util.Calendar.getInstance.getTime
      measures += (id -> (Measure(currentTime, data) :: measures.getOrElse(id, List[Measure]())))
      MyWebSocketActor.mapping.get(id) match {
        case Some(actor) => if (actor != null) actor ! (currentTime.getTime() + " " + data)
        case None => ()
      }
      Ok("You are " + id + " and you measured " + data).withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")

    } else {
      BadRequest("Do not try to cheat me !").withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
    }
  }

  def setClass(id : Int, cl : Int) = Action {
      if (ids contains id){
          classes += (id -> cl)
          Ok("")
      } else {
          BadRequest("")
      }
  }

  def setName(id : Int, name : String) = Action {
      if (ids contains id){
          names += (id -> name)
          Ok("")
      } else {
          BadRequest("")
      }
  }

  def socket(id : Int) = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => MyWebSocketActor.props(out, id))
  }

  def chart(id : Int) = Action { implicit request =>
    if (ids contains id) {
       Ok(views.html.chart(id))
    } else {
      NoContent
    }
  }
}
