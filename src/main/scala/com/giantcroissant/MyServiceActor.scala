package com.giantcroissant

import akka.actor._
import akka.event.Logging

import spray.http._
import spray.httpx.PlayJsonSupport._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.routing._

import play.api.libs.json._

class MyServiceActor extends Actor with ActorLogging with MyService {
  def actorRefFactory = context

  def receive = runRoute(combinedRoute)
}

trait MyService extends HttpService with PerRequestActorCreator {
  this: Actor with ActorLogging =>

  val generationRoute = {
    path("generation") {
      post {
        entity(as[JsObject]) { data => ctx =>
          val targetActorRef = Props(new CodeGenerationRootActor())
          perRequest(ctx, targetActorRef, CodeGenerationRestMessage(data = data))
        }
      }
    }
  }

  val combinedRoute = generationRoute
}
