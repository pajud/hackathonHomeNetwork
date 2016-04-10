package controllers

import akka.actor._
import play.api.libs.json.JsValue

object MyWebSocketActor {
  var mapping = Map[Int, ActorRef]()
  def props(out: ActorRef, id : Int)(implicit factory: ActorRefFactory) = {
    var temp  = Props(new MyWebSocketActor(out))
    mapping += (id -> factory.actorOf(temp))
    temp
  }
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! msg
  }
}
