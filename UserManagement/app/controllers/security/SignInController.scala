package controllers.security

import DTOs.{ProfileDTO, UserDTO}
import controllers.client.ClientImpl
import play.api.libs.json._
import play.api.mvc._
import services.{ProfilesService, TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignInController @Inject() (
    cc: ControllerComponents,
    ci: ClientImpl,
    usersService: UsersService,
    profileService: ProfilesService,
    tokensService: TokensService)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def createUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractUserProfileData(request) match {
        case Some((userDTO, profileDTO)) =>
          handleUserCreation(userDTO, profileDTO)

        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  private def extractUserProfileData(
      request: Request[AnyContent]): Option[(UserDTO, ProfileDTO)] = {
    for {
      json <- request.body.asJson
      userJson <- JsonOP.parseString(json.toString())
      email <- userJson("email").flatMap(_.asString)
      password <- userJson("password").flatMap(_.asString)
      firstName <- userJson("firstName").flatMap(_.asString)
      lastName <- userJson("lastName").flatMap(_.asString)
    } yield (
      UserDTO(None, email, password),
      ProfileDTO(None, firstName, lastName)
    )
  }

  private def handleUserCreation(userDTO: UserDTO, profileDTO: ProfileDTO): Future[Result] = {
    usersService.createUser(userDTO).flatMap {
      case Some(userId) =>
        val profileDTOWithUserId = profileDTO.copy(userId = Some(userId))
        for {
          profileSuccess <- profileService.createProfile(profileDTOWithUserId)
          tokenSuccess <- tokensService.createToken(userId)
          result <- processCreationResult(profileSuccess, tokenSuccess, userId, userDTO.email)
        } yield result

      case None =>
        Future.successful(InternalServerError(Json.obj("message" -> "User creation failed")))
    }
  }

  private def processCreationResult(
      profileSuccess: Boolean,
      tokenSuccess: Boolean,
      userId: UUID,
      email: String): Future[Result] = {
    if (profileSuccess && tokenSuccess) {
      ci.streamUser(userId, email).flatMap {
        case 200 /*OK*/ => Future.successful(Ok(Json.obj("message" -> "User created successfully")))
        case _ => rollbackUserCreation(userId)
      }
    } else {
      rollbackUserCreation(userId)
    }
  }

  private def rollbackUserCreation(userId: UUID): Future[Result] = {
    usersService.deleteUser(userId).map {
      _ => InternalServerError(Json.obj("message" -> "Failed to create user profile or token"))
    }
  }
}
