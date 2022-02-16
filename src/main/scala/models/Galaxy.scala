package models

import util_custom.{FirestoreSerializable, FirestoreSerializer}
import util_custom.CustomJsonHelpers.given_Decoder_Planet

case class Galaxy(name: String,
                  planetarySystems: List[PlanetarySystem])
  extends FirestoreSerializable {
  override def serializeForFirestore: java.util.HashMap[String, Object] = {
    FirestoreSerializer.serializeForFirestore[Galaxy, Star](this, classOf[Galaxy], classOf[Star])(using util_custom.CustomJsonHelpers.given_Decoder_Star)
  }
}