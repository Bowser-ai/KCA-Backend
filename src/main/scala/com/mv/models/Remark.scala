package com.mv.models

import zio.json.*

import java.time.LocalDateTime

@jsonNoExtraFields
final case class Remark(
    id: Int,
    filiaalId: Int,
    body: String,
    dateCreated: LocalDateTime,
    dateModified: LocalDateTime
)

object Remark {
  implicit val remarkEncoder: JsonEncoder[Remark] =
    DeriveJsonEncoder.gen[Remark]

  implicit val remarkDecoder: JsonDecoder[Remark] =
    DeriveJsonDecoder.gen[Remark]
}
