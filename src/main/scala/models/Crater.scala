package models

import util_custom.{FirestoreSerializable, FirestoreSerializer}

case class Crater(craterName: String) extends FirestoreSerializable {
  override def serializeForFirestore: java.util.HashMap[String, Object] = {
    FirestoreSerializer.serializeForFirestore(this, classOf[Crater])
  }
}
