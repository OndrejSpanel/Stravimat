package com.github.opengrabeso.mixtio
package frontend
package views
package edit

import common.Formatting
import common.model._
import common.css._
import io.udash._
import io.udash.bootstrap.button.UdashButton
import io.udash.bootstrap.table.UdashTable
import io.udash.component.ComponentId
import io.udash.css._
import io.udash.bootstrap._
import BootstrapStyles._

class PageView(
  model: ModelProperty[PageModel],
  presenter: PagePresenter,
) extends FinalView with CssView {
  val s = EditPageStyles

  import scalatags.JsDom.all._

  private val submitButton = UdashButton(componentId = ComponentId("about"))(_ => "Submit")

  def buttonOnClick(button: UdashButton)(callback: => Unit): Unit = {
    button.listen {
      case UdashButton.ButtonClickEvent(_, _) =>
        callback
    }
  }

  buttonOnClick(submitButton){presenter.gotoSelect()}

  model.subProp(_.routeJS).listen {
    _.foreach { geojson =>
      displayMapboxMap(geojson)
    }
  }

  def getTemplate: Modifier = {

    // value is a callback
    type EditAttrib = TableFactory.TableAttrib[EditEvent]
    val EditAttrib = TableFactory.TableAttrib

    //case class EditEvent(action: String, time: Int, km: Double, originalAction: String)
    val attribs = Seq[EditAttrib](
      EditAttrib("Action", (e, _, _) => e.action.render),
      EditAttrib("Time", (e, _, _) => Formatting.displaySeconds(e.time).render),
      EditAttrib("Distance", (e, _, _) => Formatting.displayDistance(e.time).render),
      EditAttrib("Event", (e, _, _) => e.originalAction.render),
    )

    val table = UdashTable(model.subSeq(_.events), striped = true.toProperty, bordered = true.toProperty, hover = true.toProperty, small = true.toProperty)(
      headerFactory = Some(TableFactory.headerFactory(attribs )),
      rowFactory = TableFactory.rowFactory(attribs)
    )

    div(
      s.container,

      div(
        showIfElse(model.subProp(_.loading))(
          p("Loading...").render,
          div(
            table.render,
          ).render
        )
      ),

      div(
        s.map,
        id := "map"
      )

    )
  }

  import facade.UdashApp._
  import facade.mapboxgl
  import scala.scalajs.js
  import js.Dynamic.literal

  def displayMapboxMap(geojson: String): Unit = {
    mapboxgl.accessToken = mapBoxToken;
    val map = new mapboxgl.Map(js.Dynamic.literal(
      container = "map", // container id
      style = "mapbox://styles/ospanel/cjkbfwccz11972rmt4xvmvme6", // stylesheet location
      center = js.Array(14.5, 49.8), // starting position [lng, lat]
      zoom = 13 // starting zoom
    ))
    map.on("load", { () =>
      renderRoute(map, geojson)
    })

  }

  def renderRoute(map: mapboxgl.Map, routeJson: String): Unit = {
    val route = js.JSON.parse(routeJson)

    val routeLL = route.asInstanceOf[js.Array[js.Array[Double]]].map { i =>
      js.Array(i(0).toDouble, i(1).toDouble)
    }

    map.addSource("route", literal (
      `type` = "geojson",
      data = literal(
        `type` = "Feature",
        properties = literal(),
        geometry = literal(
          `type` = "LineString",
          coordinates = routeLL
        )
      )
    ));

    map.addLayer(literal(
      id ="route",
      `type` = "line",
      source = "route",
      layout = literal(
        "line-join" -> "round",
        "line-cap" -> "round"
      ),
      paint = literal(
        "line-color" -> "#F44",
        "line-width" -> 3
      )
    ))
  }

}
