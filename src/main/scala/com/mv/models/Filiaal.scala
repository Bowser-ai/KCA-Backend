package com.mv.models

import zio.json.*
import zio.json.JsonEncoder.list
import zio.json.internal.Write

final case class Filiaal(
    filiaalNumber: Int,
    address: Option[String],
    tel: Option[String],
    zipcode: Option[String],
    info: Option[String]
)

object Filiaal {
  implicit val filiaalEncoder: JsonEncoder[Filiaal] =
    DeriveJsonEncoder.gen[Filiaal]
}
