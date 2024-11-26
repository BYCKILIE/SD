package DTOs

import java.util.UUID

case class TokenDTO(userId: UUID, token: Option[String] = None)
