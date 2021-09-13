package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.TxOutput
import io.ergolabs.cardano.explorer.core.types.OutRef
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

class OutputsEndpoints {

  val pathPrefix = "outputs"

  def getByOutRef: Endpoint[OutRef, HttpError, TxOutput, Any] =
    baseEndpoint.get
      .in(pathPrefix / path[OutRef])
      .out(jsonBody[TxOutput])
}
