package models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import util_custom.{FirestoreSerializable, FirestoreSerializer}

/**
  *
  * @param name  of the planet
  * @param moons that orbit this planet
  */
case class Planet(planetName: String)
  extends FirestoreSerializable {
  override def serializeForFirestore: java.util.HashMap[String, Object] = {
    FirestoreSerializer.serializeForFirestore[Planet](this, classOf[Planet])
  }
}
