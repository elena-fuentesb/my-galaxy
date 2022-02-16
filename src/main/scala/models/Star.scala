package models

import util_custom.{FirestoreSerializable, FirestoreSerializer}

import util_custom.CustomJsonHelpers.given_Decoder_Moon
import util_custom.CustomJsonHelpers.given_Decoder_Crater

/**
  *
  * @param name    of the Star
  * @param planets that orbit this Star
  */
case class Star(starName: String,
                planets: Seq[Planet])
  extends FirestoreSerializable {
  override def serializeForFirestore: java.util.HashMap[String, Object] = {
    FirestoreSerializer.serializeForFirestore[Star, Planet](this, classOf[Star], classOf[Planet])(using util_custom.CustomJsonHelpers.given_Decoder_Planet)
  }
}