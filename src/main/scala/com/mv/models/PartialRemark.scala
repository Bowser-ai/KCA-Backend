package com.mv.models

import zio.json.{
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  JsonDecoder,
  JsonEncoder,
  jsonNoExtraFields
}

@jsonNoExtraFields
final case class PartialRemark(
    filiaalId: Int,
    body: String
)
object PartialRemark {
  given partialRemarkEncoder: JsonEncoder[PartialRemark] =
    DeriveJsonEncoder.gen[PartialRemark]

  given partialRemarkDecoder: JsonDecoder[PartialRemark] =
    DeriveJsonDecoder.gen[PartialRemark]
}
