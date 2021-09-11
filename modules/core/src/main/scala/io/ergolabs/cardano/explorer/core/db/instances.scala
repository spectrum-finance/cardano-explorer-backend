package io.ergolabs.cardano.explorer.core.db

import doobie.util.Put
import doobie.{Get, Meta}

object instances {

  implicit val getBigInt: Get[BigInt] = implicitly[Get[BigDecimal]].temap(x =>
    x.toBigIntExact.fold[Either[String, BigInt]](Left(s"Failed to convert '$x' to BigInt"))(Right(_))
  )

  implicit val putBigInt: Put[BigInt]   = implicitly[Put[BigDecimal]].contramap[BigInt](BigDecimal(_))

  implicit val metaBigInt: Meta[BigInt] = new Meta(getBigInt, putBigInt)
}
