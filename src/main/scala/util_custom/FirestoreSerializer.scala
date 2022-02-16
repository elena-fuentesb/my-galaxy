package util_custom

import io.circe.JsonNumber
import io.circe.JsonObject
import io.circe.JsonLong

import java.util
import scala.jdk.CollectionConverters.*
import scala.quoted.*
import io.circe.*
import io.circe.parser.*
import models.Planet

import java.lang.reflect.Field

trait FirestoreSerializable {
  def serializeForFirestore: java.util.HashMap[String, Object]
}

object FirestoreSerializer {
  /**
    * For a given input; iterate over the fields and for each field add to the map:
    * "fieldName" -> fieldValue
    * as given in the input
    *
    * This serializer is for objects that only have primitive variables or sequence of primitives
    *
    * @param input to be serialized
    * @param c     class of the object that is serialized
    * @tparam T type of serializable object
    * @return
    */
  def serializeForFirestore[T <: FirestoreSerializable](input: T, c: Class[T]): java.util.HashMap[String, Object] = {
    val docData = new java.util.HashMap[String, Object]()
    c.getDeclaredFields.map { field =>
      field.setAccessible(true)
      val v = field.get(input)
      // Add primitive fields
      if (isPrimitive(v)) {
        docData.put(field.getName, v)
      } else { //One level down, a Seq with primitive values can be added correctly
        docData.put(field.getName, toJavaMap(v.asInstanceOf[Seq[AnyVal]]))
      }
    }
    docData
  }

  private def serializeForType[T <: FirestoreSerializable](listToSerialize: Seq[Object], fieldName: String, docData: java.util.HashMap[String, Object])(using decoderT: Decoder[T]) = {
    val s = listToSerialize.map { item =>
      val decoded: Either[io.circe.Error, T] = decode[T](item.toString)(using decoderT)
      decoded match {
        case Right(value) => value.serializeForFirestore
        case _ => println(s"Could decode correctly $item")
      }
    }
    docData.put(fieldName, s.asJava)
  }

  private def serializeType[T <: FirestoreSerializable](listToSerialize: Seq[T], fieldName: String, docData: java.util.HashMap[String, Object])(using decoderT: Decoder[T]) = {
    val s = listToSerialize.map { item =>
      item.serializeForFirestore
    }
    docData.put(fieldName, s.asJava)
  }

  /**
    * This serializer is for objects that have one or more variables of type *custom class* or *sequence of custom class*
    * The custom class is of type B
    *
    * @param input         to be serialized
    * @param c             class of the object that is serialized
    * @param variableClass class of the custom class that is used in a variable
    * @param decoderB      decoder for variableClass
    * @tparam A type of serializable object
    * @tparam B type of variableClass
    * @return
    */
  def serializeForFirestore[A <: FirestoreSerializable, B <: FirestoreSerializable](input: A, c: Class[A], variableClass: Class[B])(using decoderB: Decoder[B]): java.util.HashMap[String, Object] = {
    val docData = new java.util.HashMap[String, Object]()
    c.getDeclaredFields.map { field =>
      field.setAccessible(true)
      val v = field.get(input)
      if (isPrimitive(v)) { //Primitive fields
        docData.put(field.getName, v)
      } else if (isOptional(v)) {
        docData.put(field.getName, v)
      } else if (v.isInstanceOf[B]) { //Custom (case) class fields
        val variableValue: B = field.get(input).asInstanceOf[B]
        docData.put(field.getName, variableValue.serializeForFirestore)
      } else if (v.isInstanceOf[Seq[B]]) {
        serializeType[B](v.asInstanceOf[Seq[B]], field.getName, docData)
      } else {
        serializePrimitveSeq(docData, field, v, v.asInstanceOf[Seq[Object]])
      }
    }
    docData
  }

  /**
    * This serializer is for objects that have one or more variables of type *custom class* or *sequence of custom class*
    * The custom class is of type B
    *
    * @param input          to be serialized
    * @param c              class of the object that is serialized
    * @param variableClass1 first class of the custom class that is used in a variable
    * @param variableClass2 second class of the custom class that is used in a variable
    * @param decoderB       decoder for variableClass1
    * @param decoderC       decoder for variableClass2
    * @tparam A type of serializable object
    * @tparam B type of variableClass1
    * @tparam C type of variableClass2
    * @return
    */
  def serializeForFirestore[A <: FirestoreSerializable, B <: FirestoreSerializable, C <: FirestoreSerializable]
  (input: A, c: Class[A], variableClass1: Class[B], variableClass2: Class[C])(using decoderB: Decoder[B], decoderC: Decoder[C]): java.util.HashMap[String, Object] = {
    val docData = new java.util.HashMap[String, Object]()
    c.getDeclaredFields.map { field =>
      field.setAccessible(true)
      val v = field.get(input)
      if (isPrimitive(v)) { //Primitive fields
        docData.put(field.getName, v)
      } else if (v.isInstanceOf[B]) { //Custom (case) class fields of first type
        val variableValue: B = field.get(input).asInstanceOf[B]
        docData.put(field.getName, variableValue.serializeForFirestore)
      } else if (v.isInstanceOf[C]) { //Custom (case) class fields of second type
        val variableValue: C = field.get(input).asInstanceOf[C]
        docData.put(field.getName, variableValue.serializeForFirestore)
      } else if (v.isInstanceOf[Seq[B]]) {
        serializeType[B](v.asInstanceOf[Seq[B]], field.getName, docData)
      } else if (v.isInstanceOf[Seq[C]]) {
        serializeType[C](v.asInstanceOf[Seq[C]], field.getName, docData)
      } else {
        serializePrimitveSeq(docData, field, v, v.asInstanceOf[Seq[Object]])
      }
    }
    docData
  }

  private def serializePrimitveSeq[C <: FirestoreSerializable, B <: FirestoreSerializable, A <: FirestoreSerializable](docData: util.HashMap[String, Object], field: Field, v: AnyRef, r: Seq[Object]) = {
    // Can't decode to AnyVal. But in the end everything is stored as String anyway....
    val tryDecodeAsInt = decode[Int](r.head.toString)
    val tryDecodeAsString = decode[String](r.head.toString)
    if (tryDecodeAsInt.isRight) {
      docData.put(field.getName, toJavaMap(v.asInstanceOf[Seq[Int]]))
    } else if (tryDecodeAsString.isRight) {
      docData.put(field.getName, toJavaMap(v.asInstanceOf[Seq[AnyVal]]))
    }
  }

  private def isPrimitive(value: AnyRef): Boolean = {
    value.isInstanceOf[String] ||
      value.isInstanceOf[Char] ||
      value.isInstanceOf[Int] ||
      value.isInstanceOf[Long] ||
      value.isInstanceOf[Short] ||
      value.isInstanceOf[Double] ||
      value.isInstanceOf[Float] ||
      value.isInstanceOf[Boolean]
  }

  private def isOptional(value: AnyRef): Boolean = {
    value.isInstanceOf[Option[Any]]
  }

  private def toJavaMap(value: Seq[AnyVal]): java.util.Map[String, Object] = {
    def toString(s: AnyVal): String = s.toString.replace("\"", "") //removing extra " that were added

    value.zipWithIndex.map { case (s, i) => (i.toString, toString(s)) }.toMap.asJava
  }
}

extension (o: Object)
  def serializeForFirestore: java.util.HashMap[String, Object] = {
    val map = new java.util.HashMap[String, Object]()
    map.put("0", o)
    map
  }

extension (i: Int)
  def serializeForFirestore: java.util.HashMap[String, Object] = {
    val map = new java.util.HashMap[String, Object]()
    map.put("0", i.asInstanceOf[Object])
    map
  }

extension (str: String)
  def serializeForFirestore: java.util.HashMap[String, Object] = {
    val map = new java.util.HashMap[String, Object]()
    map.put("0", str.asInstanceOf[Object])
    map
  }