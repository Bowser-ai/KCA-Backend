package com.mv.domain.errors

class ClientError(msg: String) extends ApplicationError(msg)

object ClientError {
  def unapply(e: ClientError): Option[String] = Option(e.msg)
}
