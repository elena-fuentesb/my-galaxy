package util_custom

import com.google.cloud.firestore.QueryDocumentSnapshot
import models.*
import util_custom.DeserializerHelper.*

import scala.jdk.CollectionConverters.*
import java.util
import scala.collection.mutable


object FirestoreDeserializer {
  def deserializeFromFirestore[T](input: QueryDocumentSnapshot, deserializeFromMap: mutable.Map[String, Object] => Option[T]): Option[T] = {
    val dataMap = input.getData.asScala
    deserializeFromMap(dataMap)
  }

  def deserializeStarFromFirestore(input: QueryDocumentSnapshot): Option[Star] = {
    deserializeFromFirestore(input, deserializeStarFromMap)
  }

  def deserializeGalaxyFromFirestore(input: QueryDocumentSnapshot): Option[Galaxy] = {
    deserializeFromFirestore(input, deserializeGalaxyFromMap)
  }

  def deserializeStarFromMap(dataMap: mutable.Map[String, Object]): Option[Star] = {
    try {
      val name: String = getObjectForKey("starName", dataMap)
      val planets: List[Planet] = getObjectsForKey("planets", dataMap)
      Some(Star(name, planets))
    } catch { _ => None }
  }

  def deserializeGalaxyFromMap(dataMap: mutable.Map[String, Object]): Option[Galaxy] = {
    try {
      val name: String = getObjectForKey("name", dataMap)
      val planetarySystems: List[PlanetarySystem] = getObjectsForKey("planetarySystems", dataMap)
      Some(Galaxy(name, planetarySystems))
    } catch { _ => None }
  }
  /*  def deserializeStarFromMap(dataMap: mutable.Map[String, Object]): Star = {
      val name: String = getObjectForKey("name", dataMap)
      val isSun: Boolean = getObjectForKey("isSun", dataMap)
      val ints: List[Int] = getObjectsForKey("ints", dataMap)
      val strings: List[String] = getObjectsForKey("strings", dataMap)
      val moons: List[Moon] = getObjectsForKey("moons", dataMap)
      val crater: Crater = getObjectForKey("crater", dataMap)
  
      Star(name, isSun, ints, strings, moons, crater)
    }*/

  def deserializeMoonFromMap(dataMap: mutable.Map[String, Object]): Option[Moon] = {
    try {
      val name = dataMap.getOrElse("nameMoon", default = "").toString
      val crater = deserializeCraterFromMap(getDataOrEmptyMap("crater", dataMap))
      Some(Moon(if (name.isBlank) None else Some(name)))
    } catch { _ => None }
  }

  def deserializeCraterFromMap(dataMap: mutable.Map[String, Object]): Option[Crater] = {
    try {
      val name = dataMap.getOrElse("craterName", default = "").toString
      Some(Crater(name))
    } catch { _ => None }
  }

  def deserializePlanetFromMap(dataMap: mutable.Map[String, Object]): Option[Planet] = {
    try {
      val name: String = dataMap.getOrElse("planetName", default = "").toString
      val moons: List[Moon] = getObjectsForKey("moons", dataMap)
      Some(Planet(name))
    } catch { _ => None }
  }

  def deserializePlanetarySystemFromMap(dataMap: mutable.Map[String, Object]): Option[PlanetarySystem] = {
    try {
      val name: String = dataMap.getOrElse("name", default = "").toString
      val stars: List[Star] = getObjectsForKey("stars", dataMap)
      Some(PlanetarySystem(name, stars))
    } catch { _ => None }
  }
}

object DeserializerHelper {

  def objectsToList(key: String, dataMap: mutable.Map[String, Object]): List[String] = {
    val dataMapForKey = dataMap.getOrElse(key, default = Nil)
    dataMapForKey match {
      case Nil => Nil
      case _ => dataMapForKey.asInstanceOf[java.util.Map[String, Object]].asScala.values.toList.map(_.toString)
    }
  }

  def makeInt(s: String): Option[Int] =
    try
      Some(Integer.parseInt(s.trim))
    catch
      case e: Exception => None

  trait ObjectForKeyImpl[T] {
    def single(key: String, dataMap: mutable.Map[String, Object]): T

    def seq(key: String, dataMap: mutable.Map[String, Object]): List[T]
  }

  implicit object objectForKeyString extends ObjectForKeyImpl[String] {
    override def single(key: String, dataMap: mutable.Map[String, Object]) = dataMap.getOrElse(key, default = "").toString

    override def seq(key: String, dataMap: mutable.Map[String, Object]) = objectsToList(key, dataMap)
  }

  implicit object objectForKeyBoolean extends ObjectForKeyImpl[Boolean] {
    override def single(key: String, dataMap: mutable.Map[String, Object]) = {
      dataMap.getOrElse(key, default = false).toString.toBooleanOption.getOrElse(false)
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]) = objectsToList(key, dataMap).map(_.toBooleanOption.getOrElse(false))
  }

  implicit object objectForKeyInt extends ObjectForKeyImpl[Int] {
    override def single(key: String, dataMap: mutable.Map[String, Object]) = {
      makeInt(dataMap.getOrElse(key, default = false).toString).getOrElse(0)
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]) = {
      objectsToList(key, dataMap).map(o => makeInt(o)).map(_.getOrElse(0))
    }
  }

  private def deserializeCustomDataSeq[T](key: String, dataMap: mutable.Map[String, Object], deserializer: mutable.Map[String, Object] => Option[T]): List[T] = {
    val dataMapForKey = dataMap.getOrElse(key, default = Nil)
    dataMapForKey match {
      case Nil => Nil
      case _ =>
        val list = dataMapForKey.asInstanceOf[util.ArrayList[Object]].asScala.toList
        val mapped = list.map(_.asInstanceOf[java.util.HashMap[String, Object]]).map(_.asScala)
        mapped.map(deserializer).filter(_.isDefined).map(_.get)
    }
  }

  def getDataOrEmptyMap(key: String, dataMap: mutable.Map[String, Object]): mutable.Map[String, Object] = {
    val a: java.util.HashMap[String, Object] = dataMap.getOrElse(key, default = java.util.HashMap[String, Object]()).asInstanceOf[java.util.HashMap[String, Object]]
    val b = a.asScala
    b
  }

  implicit object objectForKeyMoon extends ObjectForKeyImpl[Moon] {
    override def single(key: String, dataMap: mutable.Map[String, Object]) = {
      val moonData = getDataOrEmptyMap(key, dataMap)
      FirestoreDeserializer.deserializeMoonFromMap(moonData)
        .getOrElse(Moon())
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]) = {
      deserializeCustomDataSeq[Moon](key, dataMap, FirestoreDeserializer.deserializeMoonFromMap)
    }
  }

  implicit object objectForKeyCrater extends ObjectForKeyImpl[Crater] {
    override def single(key: String, dataMap: mutable.Map[String, Object]): Crater = {
      val craterData = getDataOrEmptyMap(key, dataMap)
      FirestoreDeserializer.deserializeCraterFromMap(craterData)
        .getOrElse(Crater(""))
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]): List[Crater] =
      deserializeCustomDataSeq[Crater](key, dataMap, FirestoreDeserializer.deserializeCraterFromMap)
  }

  implicit object objectForKeyPlanet extends ObjectForKeyImpl[Planet] {
    override def single(key: String, dataMap: mutable.Map[String, Object]): Planet = {
      val planetData = getDataOrEmptyMap(key, dataMap)
      FirestoreDeserializer.deserializePlanetFromMap(planetData)
        .getOrElse(Planet("None"))
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]): List[Planet] =
      deserializeCustomDataSeq[Planet](key, dataMap, FirestoreDeserializer.deserializePlanetFromMap)
  }

  implicit object objectForKeyStar extends ObjectForKeyImpl[Star] {
    override def single(key: String, dataMap: mutable.Map[String, Object]): Star = {
      val starData = getDataOrEmptyMap(key, dataMap)
      FirestoreDeserializer.deserializeStarFromMap(starData)
        .getOrElse(Star("", Nil))
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]): List[Star] =
      deserializeCustomDataSeq[Star](key, dataMap, FirestoreDeserializer.deserializeStarFromMap)
  }

  implicit object objectForKeyPlanetarySystem extends ObjectForKeyImpl[PlanetarySystem] {
    override def single(key: String, dataMap: mutable.Map[String, Object]): PlanetarySystem = {
      val planetarySystemData = getDataOrEmptyMap(key, dataMap)
      FirestoreDeserializer.deserializePlanetarySystemFromMap(planetarySystemData)
        .getOrElse(PlanetarySystem("", Nil))
    }

    override def seq(key: String, dataMap: mutable.Map[String, Object]): List[PlanetarySystem] =
      deserializeCustomDataSeq[PlanetarySystem](key, dataMap, FirestoreDeserializer.deserializePlanetarySystemFromMap)
  }

  def getObjectForKey[T](key: String, dataMap: mutable.Map[String, Object])(implicit impl: ObjectForKeyImpl[T]) = impl.single(key, dataMap)

  def getObjectsForKey[T](key: String, dataMap: mutable.Map[String, Object])(implicit impl: ObjectForKeyImpl[T]) = impl.seq(key, dataMap)

}