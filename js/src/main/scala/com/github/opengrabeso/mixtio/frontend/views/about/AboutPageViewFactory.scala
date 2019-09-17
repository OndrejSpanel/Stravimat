package com.github.opengrabeso.mixtio
package frontend
package views.about

import com.github.opengrabeso.mixtio.rest
import routing._
import io.udash._

import scala.concurrent.Future

/** Prepares model, view and presenter for demo view. */
class AboutPageViewFactory(
  application: Application[RoutingState],
) extends ViewFactory[AboutPageState.type] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def create(): (View, Presenter[AboutPageState.type]) = {
    // TODO: do not switch to view until the API has returned
    val model = ModelProperty(
      AboutPageModel(null)
    )

    for (userAPI <- facade.UdashApp.currentUserId.toOption.map(rest.RestAPIClient.api.userAPI)) {
      userAPI.name.foreach(name => model.subProp(_.athleteName).set(name))
    }

    val presenter = new AboutPagePresenter(model, application)
    val view = new AboutPageView(model, presenter)
    (view, presenter)
  }
}