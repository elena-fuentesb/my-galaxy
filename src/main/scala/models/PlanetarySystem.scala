package models

import util_custom.{FirestoreSerializable, FirestoreSerializer}
import util_custom.CustomJsonHelpers.given_Decoder_Planet

case class PlanetarySystem(name: String,
                           stars: Seq[Star])
  extends FirestoreSerializable {
  override def serializeForFirestore: java.util.HashMap[String, Object] = {
    FirestoreSerializer.serializeForFirestore[PlanetarySystem, Star](this, classOf[PlanetarySystem], classOf[Star])(using util_custom.CustomJsonHelpers.given_Decoder_Star)
  }
}