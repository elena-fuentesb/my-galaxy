import cats.effect.*
import com.google.api.core.ApiFuture
import com.google.cloud.firestore.{DocumentSnapshot, QueryDocumentSnapshot, QuerySnapshot}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import models.{Galaxy, Planet, Star}
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.blaze.server.*
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.{EntityDecoder, HttpRoutes, MediaType}
import service.FirebaseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*
import scala.jdk.FutureConverters.*
import util_custom.CustomJsonHelpers.given_Decoder_Star
import util_custom.CustomJsonHelpers.given_Decoder_Galaxy
import util_custom.CustomJsonHelpers.given_Decoder_Planet
import util_custom.CustomJsonHelpers.given_Decoder_PlanetarySystem
import util_custom.CustomJsonHelpers.given_Encoder_Star
import util_custom.CustomJsonHelpers.given_Encoder_Planet
import util_custom.CustomJsonHelpers.given_Encoder_Galaxy
import util_custom.CustomJsonHelpers.given_Encoder_PlanetarySystem
import util_custom.CustomJsonHelpers.starListDecoder
import util_custom.CustomJsonHelpers.planetListDecoder
import util_custom.CustomJsonHelpers.intListDecoder
import util_custom.CustomJsonHelpers.stringListDecoder
import util_custom.CustomJsonHelpers.moonListDecoder
import util_custom.CustomJsonHelpers.galaxyListDecoder

object Main extends IOApp {

  val helloWorldService = HttpRoutes.of[IO] {

    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")

    case req@POST -> Root / "stars" =>
      req.as[Star]
        .flatMap { starInput =>
          IO.fromFuture {
            IO(FirebaseService.addStar(starInput))
          }
            .flatMap(_ => Ok(s"Welcome to the universe, ${starInput.starName}."))
        }

    case req@POST -> Root / "galaxies" =>
      req.as[Galaxy]
        .flatMap { galaxyInput =>
          IO.fromFuture {
            IO(FirebaseService.addGalaxy(galaxyInput))
          }
            .flatMap(_ => Ok(s"Welcome to the universe, ${galaxyInput.name}."))
        }

    case req@GET -> Root / "stars" =>
      IO.fromFuture {
        IO {
          FirebaseService.getStars()
        }
      }.flatMap(Ok(_))

    case req@GET -> Root / "galaxies" =>
      IO.fromFuture {
        IO {
          FirebaseService.getGalaxies()
        }
      }.flatMap(Ok(_))

  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(helloWorldService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

