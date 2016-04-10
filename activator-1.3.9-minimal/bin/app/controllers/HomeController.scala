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
import math._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

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
    Ok(views.html.main("MAIN", names.toList))
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
        case Some(actor) => if (actor != null) {
            var proba = getProba(id, data)
            if (measures.getOrElse(id, List()).length > 10 && proba < 0.001) {
                actor ! "There is a huge problem somewhere !"
            }
            actor ! (currentTime.getTime() + " " + data)
        }
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
       Ok(views.html.chart(id, names.getOrElse(id, "")))
    } else {
      NoContent
    }
  }

  def getMean(id : Int) = {
     var m = measures.getOrElse(id, List[Measure]())
     var m2 = m.map(x => x.measure)
     m2.sum / m2.length
  }

  def getVariance(id : Int) = {
    var mean = getMean(id)
     var m = measures.getOrElse(id, List[Measure]())
     var m2 = m.map(x => (x.measure - mean) * (x.measure - mean))
     m2.sum / m2.length
  }

  def getProba(id : Int, data : Float) = {
    var mean = getMean(id)
    var sigma = getVariance(id)
    1.0 / math.sqrt(sigma * 2.0 * math.Pi) * math.exp(-(data - mean)*(data - mean) / 2.0 / sigma)
  }

  def sensor = Action {
    Ok(views.html.sensor())
  }

  val SensorParamsForm = Form(
      mapping(
          "id" -> number,
          "name" -> text,
          "cl" -> number
      )(SensorParams.apply)(SensorParams.unapply))


  def modifyParams = Action { implicit request =>
      SensorParamsForm.bindFromRequest.fold(
          formWithErrors => {
              BadRequest("Check data")
          },
          sensor => {
              names += (sensor.id -> sensor.name)
              classes += (sensor.id -> sensor.cl)
              Redirect(routes.HomeController.index)
          }
      )
  }

  def params = Action { implicit request =>
    var result = Ok("Nothing to change")
    var SensorParamsFormFill : Form[SensorParams] = null
    for(id <- ids){
        names.get(id) match {
            case Some(name) => ()
            case None => 
              SensorParamsFormFill = SensorParamsForm.fill(SensorParams(id,"",0))
              result = Ok(views.html.params(id, SensorParamsFormFill))
        }
    }
    result
  }

}

case class SensorParams(id : Int, name : String, cl : Int)
