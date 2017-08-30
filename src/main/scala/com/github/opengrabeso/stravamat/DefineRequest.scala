package com.github.opengrabeso.stravamat

import java.net.URLEncoder

import Main._
import spark.{Request, Response, Session}

import scala.util.Try
import scala.xml.NodeSeq

sealed trait Method
object Method {
  case object Get extends Method
  case object Put extends Method
  case object Post extends Method
  case object Delete extends Method

}

case class Handle(value: String, method: Method = Method.Get)

object DefineRequest {
  abstract class Post(handleUri: String) extends DefineRequest(handleUri, method = Method.Post)
}

abstract class DefineRequest(val handleUri: String, val method: Method = Method.Get) {

  // some actions (logout) may have their URL prefixed to provide a specific functionality

  def apply(request: Request, resp: Response): AnyRef = {
    println(s"Request ${request.url()}")
    val nodes = html(request, resp)
    if (nodes.nonEmpty) {
      nodes.head match {
        case <html>{_*}</html> =>
          val docType = """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd" >"""
          docType + nodes.toString
        case _ =>
          resp.`type`("text/xml; charset=utf-8")
          val xmlPrefix = """<?xml version="1.0" encoding="UTF-8"?>""" + "\n"
          xmlPrefix + nodes.toString
      }
    } else resp.raw
  }

  def html(request: Request, resp: Response): NodeSeq

  def cond(boolean: Boolean) (nodes: NodeSeq): NodeSeq = {
    if (boolean) nodes else Nil
  }

  def headPrefix: NodeSeq = {
    <meta charset="utf-8"/>
    <link rel="icon" href="static/favicon.ico"/>
  }

  def bodyHeader(auth: Main.StravaAuthResult): NodeSeq = {
    <div id="header" style="background-color:#fca;overflow:auto">
    <table>
      <tr>
        <td>
          <a href="/"><img src="static/stravaUpload32.png"></img></a>
        </td>
        <td>
          <table>
            <tr>
              <td>
                <a href="/">Stravamat</a>
              </td>
            </tr>
            <tr>
              <td>
                Athlete:
                <a href={s"https://www.strava.com/athletes/${auth.id}"}>
                  {auth.name}
                </a>
              </td>
            </tr>
          </table>
        </td>
        <td>
        <form action={"logout"}>
          <input type="submit" value ="Log Out"/>
        </form>
        </td>
      </tr>
    </table>
    </div>
    <p></p>
  }

  def bodyFooter: NodeSeq = {
    <p></p>
    <div id="footer" style="background-color:#fca;overflow:auto">
      <a href="http://labs.strava.com/" id="powered_by_strava" rel="nofollow">
        <img align="left" src="static/api_logo_pwrdBy_strava_horiz_white.png" style="max-height:46px"/>
      </a>
      <p style="color:#fff">© 2016 - 2017 <a href="https://github.com/OndrejSpanel/Stravamat" style="color:inherit">Ondřej Španěl</a></p>
      <div/>
    </div>
  }

  def uniqueSessionId(session: Session): String = {
    // stored using storedQueryParam
    session.attribute[String]("push-session")
  }

  def performAuth(code: String, resp: Response, session: Session): Try[StravaAuthResult] = {
    val authResult = Try(Main.stravaAuth(code))
    authResult.foreach { auth =>
      resp.cookie("authCode", code, 3600 * 24 * 30) // 30 days
      session.attribute("auth", auth)
    }
    authResult

  }

  def withAuth(req: Request, resp: Response)(body: StravaAuthResult => NodeSeq): NodeSeq = {
    val session = req.session()
    val auth = session.attribute[StravaAuthResult]("auth")
    if (auth == null) {
      val codePar = Option(req.queryParams("code"))
      val statePar = Option(req.queryParams("state")).filter(_.nonEmpty)
      codePar.fold{
        val code = Option(req.cookie("authCode"))
        code.flatMap { code =>
          performAuth(code, resp, session).toOption.map(body)
        }.getOrElse(
          loginPage(req, resp, req.url, Option(req.queryString))
        )
      } { code =>
        if (performAuth(code, resp, session).isSuccess) {
          resp.redirect(req.url() + statePar.fold("")("?" + _))
          NodeSeq.Empty
        } else {
          loginPage(req, resp, req.url, statePar)
        }
      }
    } else {
      body(auth)
    }
  }

  def loginPage(request: Request, resp: Response, afterLogin: String, afterLoginParams: Option[String]): NodeSeq = {
    resp.cookie("authCode", "", 0) // delete the cookie
    <html>
      <head>
        {headPrefix}
        <title>Stravamat</title>
      </head>
      <body>
        {
        val secret = Main.secret
        val clientId = secret.appId
        val uri = "https://www.strava.com/oauth/authorize?"
        val state = afterLoginParams.fold("")(pars => "&state=" + URLEncoder.encode(pars, "UTF-8"))
        val action = uri + "client_id=" + clientId + "&response_type=code&redirect_uri=" + afterLogin + state + "&scope=write,view_private&approval_prompt=force"
        <h3>Work in progress, use at your own risk.</h3>
          <p>
            Automated uploading and processing of Suunto data to Strava
          </p>


          <h4>Suunto Upload</h4>
          <p>
            If you want to upload Suunto files, start the Stravamat Start application
            which will open a new web page with the upload progress.
          </p>
          <p>
            The application can be downloaded from <a href="https://github.com/OndrejSpanel/Stravamat/releases">GitHub Stravamat Releases page</a>.
          </p>

          <h4>Working</h4>
          <ul>
            <li>Merges Quest and GPS Track Pod data</li>
            <li>Splits GPS data as needed between Quest activities</li>
            <li>Corrects quest watch time inaccuracies</li>
          </ul>
          <h4>Work in progress</h4>
          <ul>
            <li>Merge activities</li>
            <li>Edit lap information</li>
            <li>Show activity map</li>
            <li>Split activities</li>
          </ul> :+ {
          if (!clientId.isEmpty) {
            <a href={action}>
              <img src="static/ConnectWithStrava.png" alt="Connect with STRAVA"></img>
            </a>
          } else {
            <p>Error:
              {secret.error}
            </p>
          }
        }
        }
        {bodyFooter}
      </body>
    </html>
  }

}
