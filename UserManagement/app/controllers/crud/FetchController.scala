package controllers.crud

import DTOs.{ProfileDTO, UserDTO}
import play.api.libs.json._
import play.api.libs.json.Format.GenericFormat
import play.api.mvc._
import services.{ProfilesService, TokensService, UsersService}
import utils.JsonOP

import java.time.format.DateTimeFormatter
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

@Singleton
class FetchController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    profilesService: ProfilesService,
    tokensService: TokensService)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def fetchUsers(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeRequestData = for {
        tokenHeader <- request.headers.get("Authorization")
        token = tokenHeader.stripPrefix("Bearer ").trim

        json <- request.body.asJson
        jsonData <- JsonOP.parseString(json.toString())
        partialName <- jsonData("name").flatMap(_.asString)
        offset <- jsonData("offset").flatMap(_.asString)
      } yield (token, partialName, offset.toLong)

      maybeRequestData match {
        case Some((token, partialName, offset)) =>
          tokensService.validateToken(token).flatMap {
            case Some((_, role)) =>
              if (role == "admin") {
                handleRequest(partialName, offset, "client")
              } else {
                handleRequest(partialName, offset, "admin")
              }
            case None =>
              Future.successful(Unauthorized(Json.obj("message" -> "Token validation failed")))
          }

        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Missing token in request header")))
      }
  }

  private def usersToXml(user: UserDTO, profile: ProfileDTO): Elem = {
    <data>
      <email>{user.email}</email>
      <role>{user.role}</role>
      <firstName>{profile.firstName}</firstName>
      <lastName>{profile.lastName}</lastName>
      <profilePicUrl>{profile.profilePicUrl}</profilePicUrl>
      <createdAt>{DateTimeFormatter.ISO_INSTANT.format(user.createdAt.get.toInstant)}</createdAt>
    </data>
  }

  private def handleRequest(partialName: String, offset: Long, role: String): Future[Result] = {
    if (offset == -1) {
      fetchMatchingUsersByName(partialName, role)
    } else {
      fetchUsersWithOffset(offset, role)
    }
  }

  private def fetchMatchingUsersByName(partialName: String, role: String): Future[Result] = {
    profilesService.fetchMatchingProfiles(partialName).flatMap {
      case Some(profiles) => fetchUsers(profiles, role)
      case None =>
        Future.successful(
          InternalServerError(Json.obj("message" -> "An error occurred during user reading")))
    }
  }

  private def fetchUsersWithOffset(offset: Long, role: String): Future[Result] = {
    usersService.fetchUsers(offset, role).flatMap {
      case Some(users) => fetchProfiles(users)
      case None =>
        Future.successful(
          InternalServerError(Json.obj("message" -> "An error occurred during user reading")))
    }
  }

  private def fetchProfiles(users: Seq[UserDTO]): Future[Result] = {
    val completeProfiles = users.map {
      userDTO =>
        profilesService.readProfile(userDTO.id.get).map {
          case Some(profileDTO) => Some(usersToXml(userDTO, profileDTO))
          case None => None
        }
    }

    makeResponse(completeProfiles)
  }

  private def fetchUsers(profiles: Seq[ProfileDTO], role: String): Future[Result] = {
    val completeProfiles = profiles.map {
      profileDTO =>
        usersService.readUser(profileDTO.userId.get).map {
          case Some(userDTO) if userDTO.role == role =>
            Some(usersToXml(userDTO, profileDTO))
          case Some(_) => None
          case None => None
        }
    }

    makeResponse(completeProfiles)
  }

  private def makeResponse(completeProfiles: Seq[Future[Option[Elem]]]): Future[Result] = {
    Future.sequence(completeProfiles).map {
      profileOpt =>
        val validProfiles = profileOpt.flatten.filter(_ != None)
        val xmlResponse = <profiles>
          {validProfiles}
        </profiles>
        Ok(xmlResponse).as("text/xml")
    }
  }

}
