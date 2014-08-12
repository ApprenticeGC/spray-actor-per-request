package com.giantcroissant

import akka.actor._
import akka.event.Logging

import spray.http._
import spray.httpx.PlayJsonSupport._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.routing._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

sealed trait RestMessage {
  def result: JsValue
}
case class CodeGenerationRestMessage(
  result: JsValue = Json.obj(),
  data: JsObject
) extends RestMessage
case class CodeGenerationFinishedRestMessage(
  result: JsValue = Json.obj(),
  code: String
) extends RestMessage

case class CreateRestApiCode(data: JsObject)
case class CreateDBCode(data: JsObject)
case class DoneCreateRestApiCode(code: String)
case class DoneCreateDBCode(code: String)

class CodeGenerationRootActor extends Actor with ActorLogging {
  //
  val subActor1 = context.actorOf(Props(new CodeGenerationSubActor1()))
  val subActor2 = context.actorOf(Props(new CodeGenerationSubActor2()))

  var restApiCodeDone = false
  var dbCodeDone = false

  var resultJson: JsValue = Json.obj(
    "restApiCode" -> "",
    "dbCode" -> ""
  )

  //
  def receive = {
    case CodeGenerationRestMessage(result, data) => {
      subActor1 ! CreateRestApiCode(data)
      subActor2 ! CreateDBCode(data)
      context.become(waitSubActorDone)
    }
  }

  def waitSubActorDone: Receive = {
    case DoneCreateRestApiCode(code) => {
      val jsonTransformer = (__ \ 'restApiCode).json.update(of[JsString].map { case JsString(des) => JsString(code) })

      resultJson = (resultJson.transform(jsonTransformer)).get
      restApiCodeDone = true
      responeIfReady()
    }
    case DoneCreateDBCode(code) => {
      val jsonTransformer = (__ \ 'dbCode).json.update(of[JsString].map { case JsString(des) => JsString(code) })

      resultJson = (resultJson.transform(jsonTransformer)).get
      dbCodeDone = true
      responeIfReady()
    }
  }

  def responeIfReady() = {
    if (restApiCodeDone && dbCodeDone) {
      context.parent ! CodeGenerationFinishedRestMessage(result = resultJson, code = "")
    }
  }
}

class CodeGenerationSubActor1 extends Actor with ActorLogging {
  def receive = {
    case CreateRestApiCode(data) => {
      val someCode = "this is great Rest Api code"
      Thread.sleep(10000)
      context.parent ! DoneCreateRestApiCode(someCode)
    }
  }
}

class CodeGenerationSubActor2 extends Actor with ActorLogging {
  def receive = {
    case CreateDBCode(data) => {
      val someCode = "this is great DB code"
      Thread.sleep(10000)
      context.parent ! DoneCreateDBCode(someCode)
    }
  }
}