package models

import com.google.cloud.firestore.QueryDocumentSnapshot
import util_custom.{FirestoreSerializable, FirestoreSerializer}

import java.util

case class Moon(nameMoon: Option[String] = None) extends FirestoreSerializable {
  override def serializeForFirestore: java.util.HashMap[String, Object] = {
    FirestoreSerializer.serializeForFirestore[Moon](this, classOf[Moon])
  }
}