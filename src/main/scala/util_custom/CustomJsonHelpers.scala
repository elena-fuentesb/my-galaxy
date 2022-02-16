package util_custom

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import models.*
import io.circe.*
import io.circe.syntax.*

import java.util.Base64
import scala.util.Try

import io.circe.Encoder.AsArray.importedAsArrayEncoder
import io.circe.Encoder.AsObject.importedAsObjectEncoder
import io.circe.Encoder.AsRoot.importedAsRootEncoder

object CustomJsonHelpers {
  def decodeListTolerantly[A: Decoder]: Decoder[List[A]] =
    Decoder.decodeList(Decoder[Json]).map(
      _.asInstanceOf[List[A]]
    )

  inline given intListDecoder: Decoder[List[Int]] = decodeListTolerantly[Int]

  inline given stringListDecoder: Decoder[List[String]] = decodeListTolerantly[String]

  inline given moonListDecoder: Decoder[List[Moon]] = decodeListTolerantly[Moon]

  inline given starListDecoder: Decoder[List[Star]] = decodeListTolerantly[Star]

  inline given galaxyListDecoder: Decoder[List[Galaxy]] = decodeListTolerantly[Galaxy]

  inline given planetListDecoder: Decoder[List[Planet]] = decodeListTolerantly[Planet]

  inline given planetarySystemListDecoder: Decoder[List[PlanetarySystem]] = decodeListTolerantly[PlanetarySystem]

  inline given mapListDecoder: Decoder[List[Map[String, String]]] = decodeListTolerantly[Map[String, String]]

  inline given Decoder[Shape] = deriveDecoder

  inline given Decoder[Star] = deriveDecoder

  inline given Decoder[Moon] = deriveDecoder

  inline given Decoder[Crater] = deriveDecoder

  inline given Decoder[Planet] = deriveDecoder

  inline given Decoder[Galaxy] = deriveDecoder

  inline given Decoder[PlanetarySystem] = deriveDecoder

  //Encoders

  inline given Encoder[Shape] = deriveEncoder

  inline given Encoder[Star] = deriveEncoder

  inline given Encoder[Moon] = deriveEncoder

  inline given Encoder[Crater] = deriveEncoder

  inline given Encoder[Planet] = deriveEncoder

  inline given Encoder[Galaxy] = deriveEncoder

  inline given Encoder[PlanetarySystem] = deriveEncoder

  implicit val encodeIntOrString: Encoder[Map[String, String]] = new Encoder[Map[String, String]] {
    override def apply(values: Map[String, String]): Json = {
      values.toList
        .map(pair => Json.obj(
          (pair._1, pair._2.asJson)
        )).asJson
    }
  }
}
