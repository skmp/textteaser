package com.textteaser.summarizer

import java.nio.charset.StandardCharsets
import com.typesafe.config._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import com.google.inject.Guice
import com.mongodb._
import net.liftweb.mongodb._
import com.textteaser.summarizer.models.Keyword
import com.foursquare.rogue.LiftRogue._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.slf4j._
import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._
//import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

import akka.actor.ActorSystem
import akka.actor.Props
import org.mashupbots.socko.events.HttpRequestEvent
import akka.actor.Actor
import java.util.Date

//object qstitle extends QueryStringName("title")
//object qstext extends QueryStringName("text")

//class req(ec: HttpRequestEvent, tc: String, txtc: String) {
//  var e: HttpRequestEvent = ec
//  var title: String = tc
//  var text: String = txtc
//}

case class JSONR(id: String, title: String, text: String)

object Main extends App {



//  val log = guice.instance[Logger]

 // log.info("Starting...")


      // STEP #1 - Define Actors and Start Akka
      // See `HelloHandler`
      //
      val mc = ConfigFactory.parseString("http {\nmax-initial-line-length=4096000\n }");
      val aconfig = ConfigFactory.load()
      val actorSystem = ActorSystem("HelloExampleActorSystem",mc.withFallback(aconfig))

      //
      // STEP #2 - Define Routes
      // Dispatch all HTTP GET events to a newly instanced `HelloHandler` actor for processing.
      // `HelloHandler` will `stop()` itself after processing each request.
      //
      val routes = Routes({
	case OPTIONS(request) => {
          actorSystem.actorOf(Props[OptionsHandler]) ! request
        }

        case POST(request)  => {
          actorSystem.actorOf(Props[HelloHandler]) ! request
        }
      })

      //
      // STEP #3 - Start and Stop Socko Web Server
      //
  //    def main(args: Array[String]) {
        val webServer = new WebServer(WebServerConfig(hostname="0.0.0.0"), routes, actorSystem)
        webServer.start()

        Runtime.getRuntime.addShutdownHook(new Thread {
          override def run { webServer.stop() }
        })

//        System.out.println("Open your browser and navigate to http://localhost:8888")
//      }
}

 class HelloHandler extends Actor {
      def receive = {
        case event : HttpRequestEvent =>
          event.response.headers += ("Access-Control-Allow-Origin" -> "*" )
	  event.response.headers += ("Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept")
          event.response.contentType = "application/json"

          implicit val formats = DefaultFormats // Brings in default date formats etc.

          val config = new Config
          val guice = new ScalaInjector(Guice.createInjector(new GuiceModule(config)))
          MongoDB.defineDb(DefaultMongoIdentifier, guice.instance[Mongo], config.db.name)
          var summarizer = guice.instance[Summarizer]

          var json = parse(event.request.content.toString(StandardCharsets.UTF_8))
          var ep = json.extract[JSONR]

          val article = Article(ep.id,ep.title,ep.text)
          val summary = summarizer.summarize(article.article, article.title, article.id, article.blog, article.category)
	  event.response.write(summarizer.toJSON(summary))
          context.stop(self)
      }
    }

 class OptionsHandler extends Actor {
      def receive = {
        case event : HttpRequestEvent =>
          event.response.headers += ("Access-Control-Allow-Origin" -> "*" )
	  event.response.headers += ("Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept")
	  event.response.write(new HttpResponseStatus(200))
          context.stop(self)
      }
    }
