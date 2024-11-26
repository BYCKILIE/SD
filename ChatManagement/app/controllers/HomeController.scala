package controllers

import play.api.mvc._

import javax.inject._

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index(): Action[AnyContent] = Action { _ =>
    Ok("<h1>Chat Management Microservice</h1>").as("text/html")
  }
}
