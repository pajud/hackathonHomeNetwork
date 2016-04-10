package controllers

import scala.concurrent.duration.DurationInt
import akka.actor.Props.apply
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.Props
import akka.actor._
import math._

object CheckSensorJob {

  def onStart(system: ActorSystem) {
      Logger.info("BEGIN")
      reminderDaemon(system)
  }

  def reminderDaemon(system: ActorSystem) = {
    Logger.info("Scheduling the reminder daemon")

    val reminderActor = system.actorOf(Props(new ReminderActor()))
        system.scheduler.schedule(10 seconds, 0 minutes, reminderActor, "reminderDaemon")
  }

}

class ReminderActor extends Actor {
    var toExclude = Set[Int]()
    def receive() = {
        case _ =>
            var ids = HomeController.ids
            var measures = HomeController.measures
            for (id <- ids) {
                var m = measures.getOrElse(id, List())
                if (m.length > 20){
                    var m2 = m.map(x => x.date.getTime)
                    var m3 = m2.head :: m2
                    var m4 = m2.zip(m3)
                    var m5 = m4.map(x => x._2 - x._1)

                    val currentTime = java.util.Calendar.getInstance.getTime.getTime - m3.max
                    var p = getProba(id, m5, currentTime)
                    if (p < 0.0001 && abs(currentTime) > 2 * 2 * abs(m5.tail.min) && !(toExclude contains id)){
                      Logger.info(currentTime + " " + m5.tail.min.toString)
                      //actor ! ("The device " + HomeController.names.getOrElse(id, "NoName") + " is probably dead !")

                      HomeController.status += (id -> SensorState.Disconnected)

                      toExclude += id
                    }

                    // MyWebSocketActor.mapping.get(id) match {
                    //   case Some(actor) =>
                    //       if (actor != null) {
                    //         val currentTime = java.util.Calendar.getInstance.getTime.getTime - m3.max
                    //         var p = getProba(id, m5, currentTime)
                    //         if (p < 0.0001 && abs(currentTime) > 2 * 2 * abs(m5.tail.min) && !(toExclude contains id)){
                    //           Logger.info(currentTime + " " + m5.tail.min.toString)
                    //           //actor ! ("The device " + HomeController.names.getOrElse(id, "NoName") + " is probably dead !")

                    //           HomeController.status += (id -> SensorState.Disconnected)

                    //           toExclude += id
                    //         }
                    //       }
                    //   case None => ()
                    // }
                }
            }
    }

    def getMean(id : Int, data : List[Long]) = {
       data.sum / data.length
    }

    def getVariance(id : Int, data : List[Long]) = {
      var mean = getMean(id, data)
       var m2 = data.map(x => (x - mean) * (x - mean))
       m2.sum / m2.length
    }

    def getProba(id : Int, data : List[Long], current : Long) = {
      var mean = getMean(id, data)
      var sigma = getVariance(id, data)
      1.0 / math.sqrt(sigma * 2.0 * math.Pi) * math.exp(-(current - mean)*(current - mean) / 2.0 / sigma)
    }
}
