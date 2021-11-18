package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.models.MetaData
import io.ergolabs.cardano.explorer.core.db.models.EpochParams
import io.ergolabs.cardano.explorer.core.db.models.EpochStakes

final class NetworkParamsSql(implicit lh: LogHandler) {

  def getMeta: Query0[MetaData] =
    sql"select start_time, network_name from meta".query

  def getLastEpochParams: Query0[EpochParams] =
    sql"""
      select 
        epoch_no,
        protocol_major,
        protocol_minor,
        decentralization,
        entropy,
        max_bh_size,
        max_block_size,
        max_tx_size,
        min_fee_b,
        min_fee_a,
        min_utxo_value,
        key_deposit,
        pool_deposit,
        min_pool_cost,
        max_epoch,
        optimal_pool_count
      from epoch_param order by epoch_no desc limit 1""".stripMargin.query

  def getEpochStakes(epochNo: Int): Query0[Int] =
    sql"select pool_id from epoch_stake where epoch_no = $epochNo".query
  
}
