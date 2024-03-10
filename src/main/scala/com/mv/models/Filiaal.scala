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

object Filiaal {
  given filiaalEncoder: JsonEncoder[Filiaal] =
    DeriveJsonEncoder.gen[Filiaal]

  given filiaalDecoder: JsonDecoder[Filiaal] =
    DeriveJsonDecoder.gen[Filiaal]
}
