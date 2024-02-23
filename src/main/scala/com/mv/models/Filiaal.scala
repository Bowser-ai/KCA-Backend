package com.mv.models

import zio.json.*

@jsonNoExtraFields
final case class Filiaal(
    filiaalNumber: Int,
    address: Option[String],
    tel: Option[String],
    zipcode: Option[String],
    info: Option[String]
)

@jsonNoExtraFields
final case class PartialFiliaal(
    address: Option[String],
    tel: Option[String],
    zipcode: Option[String],
    info: Option[String]
) {
  def asFiliaal(filiaalNumber: Int): Filiaal = Filiaal(
    filiaalNumber,
    address,
    tel,
    zipcode,
    info
  )
}

object Filiaal {
  given filiaalEncoder: JsonEncoder[Filiaal] =
    DeriveJsonEncoder.gen[Filiaal]

  given filiaalDecoder: JsonDecoder[Filiaal] =
    DeriveJsonDecoder.gen[Filiaal]

  given partialFiliaalEncoder: JsonEncoder[PartialFiliaal] =
    DeriveJsonEncoder.gen[PartialFiliaal]

  given partialFiliaalDecoder: JsonDecoder[PartialFiliaal] =
    DeriveJsonDecoder.gen[PartialFiliaal]
}
