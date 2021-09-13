package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.OptionT
import io.ergolabs.cardano.explorer.api.v1.models.TxOutput
import io.ergolabs.cardano.explorer.core.db.repositories.TxRepoBundle
import io.ergolabs.cardano.explorer.core.types.OutRef
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr

trait Outputs[F[_]] {

  def getByOutRef(ref: OutRef): F[Option[TxOutput]]
}

object Outputs {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: TxRepoBundle[D]
  ): Outputs[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: TxRepoBundle[D]) extends Outputs[F] {
    import repos._

    def getByOutRef(ref: OutRef): F[Option[TxOutput]] =
      (for {
        out    <- OptionT(outputs.getByRef(ref))
        assets <- OptionT.liftF(assets.getByOutputId(out.id))
      } yield TxOutput.inflate(out, assets)).value ||> txr.trans
  }
}
