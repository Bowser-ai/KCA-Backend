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

@jsonNoExtraFields
final case class PartialRemark(
    filiaalId: Int,
    body: String
)

object Remark {
  given remarkEncoder: JsonEncoder[Remark] =
    DeriveJsonEncoder.gen[Remark]

  given remarkDecoder: JsonDecoder[Remark] =
    DeriveJsonDecoder.gen[Remark]

  given partialRemarkEncoder: JsonEncoder[PartialRemark] =
    DeriveJsonEncoder.gen[PartialRemark]

  given partialRemarkDecoder: JsonDecoder[PartialRemark] =
    DeriveJsonDecoder.gen[PartialRemark]
}
