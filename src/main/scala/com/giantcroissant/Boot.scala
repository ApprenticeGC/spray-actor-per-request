package com.giantcroissant

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {

  implicit val actorSystem = ActorSystem()

  val serviceActor = actorSystem.actorOf(Props[MyServiceActor], name = "handler")

  IO(Http) ! Http.Bind(serviceActor, interface = "localhost", port = 8080)
}
