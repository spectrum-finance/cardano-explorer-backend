package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.OptionT
import cats.syntax.traverse._
import io.ergolabs.cardano.explorer.api.v1.models.{Paging, TxOutput, UtxoSearch}
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.types.{Addr, Asset32, OutRef}
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Outputs[F[_]] {

  def getByOutRef(ref: OutRef): F[Option[TxOutput]]

  def getUnspentByAddr(addr: Addr, paging: Paging): F[List[TxOutput]]

  def getUnspentByAssetId(asset: Asset32, paging: Paging): F[List[TxOutput]]

  def searchUnspent(addr: Addr, assets: List[Asset32], paging: Paging): F[List[TxOutput]]

  def searchUnspentUnion(addr: Addr, assets: List[Asset32], paging: Paging): F[List[TxOutput]]
}

object Outputs {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): Outputs[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends Outputs[F] {
    import repos._

    def getByOutRef(ref: OutRef): F[Option[TxOutput]] =
      (for {
        out    <- OptionT(outputs.getByRef(ref))
        assets <- OptionT.liftF(assets.getByOutputId(out.id))
      } yield TxOutput.inflate(out, assets)).value ||> txr.trans

    def getUnspentByAddr(addr: Addr, paging: Paging): F[List[TxOutput]] =
      (for {
        outs   <- outputs.getUnspentOutputsByAddr(paging.offset, paging.limit, addr)
        assets <- outs.flatTraverse(out => assets.getByOutputId(out.id))
        result = outs.map(out => TxOutput.inflate(out, assets))
      } yield result) ||> txr.trans

    def getUnspentByAssetId(asset: Asset32, paging: Paging): F[List[TxOutput]] =
      (for {
        outs              <- outputs.getUnspentOutputsByAsset(paging.offset, paging.limit, asset)
        assetsWIthOutputs <- outs.traverse(out => assets.getByOutputId(out.id).map(assetsList => out -> assetsList))
        result = assetsWIthOutputs.map { case (out, assetsList) => TxOutput.inflate(out, assetsList) }
      } yield result) ||> txr.trans

    def searchUnspent(addr: Addr, assetsToSearch: List[Asset32], paging: Paging): F[List[TxOutput]] =
      (for {
        outs           <- outputs.getUnspentOutputsByAddr(paging.offset, paging.limit, addr)
        outsWithAssets <- outs.traverse(out => assets.getByOutputId(out.id).map(assetsList => out -> assetsList))
        result = outsWithAssets.collect {
                   case (out, outAssets)
                       if outAssets.map(_.name).intersect(assetsToSearch).length == assetsToSearch.length =>
                     TxOutput.inflate(out, outAssets)
                 }
      } yield result) ||> txr.trans

    def searchUnspentUnion(addr: Addr, assetsToSearch: List[Asset32], paging: Paging): F[List[TxOutput]] =
      (for {
        outs           <- outputs.getUnspentOutputsByAddr(paging.offset, paging.limit, addr)
        outsWithAssets <- outs.traverse(out => {
          println(out)
          assets.getByOutputId(out.id).map(assetsList => out -> assetsList)
        })
        result = outsWithAssets.collect {
            case (out, outAssets) if outAssets.map(_.name).intersect(assetsToSearch).nonEmpty =>
              TxOutput.inflate(out, outAssets)
          }
      } yield result) ||> txr.trans
  }
}
