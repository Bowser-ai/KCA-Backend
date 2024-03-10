package com.mv.models

import zio.json.{
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  JsonDecoder,
  JsonEncoder,
  jsonNoExtraFields
}

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

object PartialFiliaal {
  given partialFiliaalEncoder: JsonEncoder[PartialFiliaal] =
    DeriveJsonEncoder.gen[PartialFiliaal]

  given partialFiliaalDecoder: JsonDecoder[PartialFiliaal] =
    DeriveJsonDecoder.gen[PartialFiliaal]
}
