package com.giantcroissant

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import akka.event.Logging

import spray.http.StatusCodes._
import spray.routing.RequestContext
import spray.http.StatusCode

import spray.http._
import spray.httpx.PlayJsonSupport._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import com.giantcroissant.PerRequest._

import play.api.libs.json._

//
trait PerRequest extends Actor {

  import context._

  def requestContext: RequestContext
  def target: ActorRef
  def message: RestMessage

  target ! message

  def receive = {
    case restMessage: RestMessage => complete(OK, restMessage.result)
  }

  def complete(status: StatusCode, result: JsValue) = {
    requestContext.complete(status, result)
    stop(self)
  }

  //def complete[T <: AnyRef](status: StatusCode, obj: T) = {
  //def complete(status: StatusCode, someString: String) = {
    //requestContext.complete(status, someString)
    //requestContext.complete(status, obj)
    //stop(self)
  //}
}

//
object PerRequest {
  case class WithActorRef(requestContext: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest
  case class WithProps(requestContext: RequestContext, props: Props, message: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

//
trait PerRequestActorCreator {
  this: Actor =>

  def perRequest(requestContext: RequestContext, target: ActorRef, message: RestMessage) =
    context.actorOf(Props(new WithActorRef(requestContext, target, message)))

  def perRequest(requestContext: RequestContext, props: Props, message: RestMessage) =
    context.actorOf(Props(new WithProps(requestContext, props, message)))
}