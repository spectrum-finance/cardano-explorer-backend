package io.ergolabs.cardano.explorer.api.v1

import cats.Applicative
import cats.syntax.either._
import tofu.Catches
import tofu.syntax.handle._
import tofu.syntax.monadic._

object syntax {

  implicit class ServiceOps[F[_], A](protected val fa: F[A]) extends AnyVal {

    def eject(implicit F: Applicative[F], C: Catches[F]): F[Either[HttpError, A]] =
      fa.map(_.asRight[HttpError]).handle[Throwable](e => HttpError.Unknown(500, e.getMessage).asLeft)
  }

  implicit class ServiceOptionOps[F[_], A](protected val fa: F[Option[A]]) extends AnyVal {

    def orNotFound(what: String)(implicit F: Applicative[F], C: Catches[F]): F[Either[HttpError, A]] =
      fa.map(_.fold[Either[HttpError, A]](HttpError.NotFound(what).asLeft)(_.asRight))
        .handle[Throwable](e => HttpError.Unknown(500, e.getMessage).asLeft)
  }
}
