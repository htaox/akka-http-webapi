package com.javacodegeeks.http

import java.security.KeyStore
import akka.http.scaladsl.HttpsConnectionContext
import javax.net.ssl.KeyManagerFactory
import java.io.InputStream
import akka.http.scaladsl.ConnectionContext
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.SecureRandom
import com.typesafe.config.ConfigFactory
import resource._

trait SslSupport {
  val configuration = ConfigFactory.load()
  val password = configuration.getString("keystore.password").toCharArray()

  val https: HttpsConnectionContext = managed(getClass.getResourceAsStream("/akka-http-webapi.jks")).map { in =>
    val keyStore: KeyStore = KeyStore.getInstance("JKS")
    keyStore.load(in, password)
  
    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password)
  
    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(keyStore)
  
    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    
    ConnectionContext.https(sslContext)
  }.opt.get 
}