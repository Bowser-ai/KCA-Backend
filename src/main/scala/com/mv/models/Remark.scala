package com.mv.models

import zio.json.*

import java.time.LocalDateTime

sealed trait AbstractRemark {
  val body: String
}
final case class Remark(
    id: Int,
    filiaalId: Int,
    body: String,
    dateCreated: LocalDateTime,
    dateModified: LocalDateTime
) extends AbstractRemark

@jsonNoExtraFields
final case class RemarkWithFiliaal(
    filiaalId: Int,
    body: String
) extends AbstractRemark

@jsonNoExtraFields
final case class RemarkWithId(
    id: Int,
    body: String
) extends AbstractRemark

object Remark {
  implicit val remarkEncoder: JsonEncoder[Remark] =
    DeriveJsonEncoder.gen[Remark]

  implicit val remarkDecoder: JsonDecoder[Remark] =
    DeriveJsonDecoder.gen[Remark]

  implicit val remarkWithFiliaalDecoder: JsonDecoder[RemarkWithFiliaal] =
    DeriveJsonDecoder.gen[RemarkWithFiliaal]

  implicit val remarkWithIdDecoder: JsonDecoder[RemarkWithId] =
    DeriveJsonDecoder.gen[RemarkWithId]
}
