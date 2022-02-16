package service

import models.{Galaxy, Planet, Star, PlanetarySystem}
import org.joda.time.LocalDateTime
import util_custom.FirestoreDeserializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

object FirebaseService {

  def addStar(star: Star) = {
    Future {
      Firebase.db.collection("stars")
        .document(LocalDateTime.now().toString + star.starName)
        .set(star.serializeForFirestore)
        .get()
    }
  }

  def getStars() = {
    Future {
      Firebase.db.collection("stars").get.get
    }
      .map { querySnapshot =>
        val stars: Seq[Star] = querySnapshot.getDocuments.asScala
          .map(FirestoreDeserializer.deserializeStarFromFirestore)
          .toSeq.filter(_.isDefined).map(_.get)
        stars
      }
  }

  def addGalaxy(galaxy: Galaxy) = {
    Future {
      Firebase.db.collection("galaxies")
        .document(LocalDateTime.now().toString + galaxy.name)
        .set(galaxy.serializeForFirestore)
        .get()
    }
  }

  def getGalaxies() = {
    Future {
      Firebase.db.collection("galaxies").get.get
    }
      .map { querySnapshot =>
        val stars: Seq[Galaxy] = querySnapshot.getDocuments.asScala
          .map(FirestoreDeserializer.deserializeGalaxyFromFirestore)
          .toSeq.filter(_.isDefined).map(_.get)
        stars
      }
  }
}
