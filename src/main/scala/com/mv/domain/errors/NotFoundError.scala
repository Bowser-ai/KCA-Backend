package com.mv.domain.errors

class NotFoundError(msg: String) extends ApplicationError(msg)

object NotFoundError {
  def unapply(e: NotFoundError): Option[String] = Option(e.msg)
}
