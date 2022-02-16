package service

import com.github.andyglow.config.*
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.database.DatabaseReference
import com.typesafe.config.{Config, ConfigFactory}

import java.io.FileInputStream


case class FirebaseException(s: String) extends Exception(s)

object Firebase {

  import com.google.auth.oauth2.GoogleCredentials
  import com.google.cloud.firestore.Firestore
  import com.google.firebase.{FirebaseApp, FirebaseOptions}

  val config = ConfigFactory.load("application.conf")
  val fileName = config.get[String]("firebase.secret.file.name")
  val projectName = config.get[String]("firebase.project.name")

  val serviceAccount = new FileInputStream(fileName)

  val options = FirebaseOptions.builder.setCredentials(GoogleCredentials.fromStream(serviceAccount))
    .setProjectId(projectName)
    .build()
  FirebaseApp.initializeApp(options)

  def db = FirestoreClient.getFirestore()
}