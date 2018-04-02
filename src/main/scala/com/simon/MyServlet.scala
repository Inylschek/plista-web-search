package com.simon

import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import scala.collection.JavaConversions._
import scala.collection.immutable.Map

import org.jsoup.{Jsoup, Connection}
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import java.net.URL
import com.simon.scrapers.WebScraper
import com.simon.database.AbstractKeyValueDatabase

// TODO Logging
class MyServlet[K, V](database: AbstractKeyValueDatabase[K, V],
    scraperFactory: (String) => WebScraper[K, V]) extends ScalatraServlet with ScalateSupport {

  var error = ""
  var results: Map[K, V] = Map()

  get("/") {
    contentType = "text/html"
    jade("/index", "error" -> error, "results" -> results.map({ case (k, v) => (k.toString, v.toString)}))
  }

  post("/scrape") {
    error = try {
      val scraper: WebScraper[K, V] = scraperFactory(params.get("url").get)
      val f: Future[Unit] = Future {
        	database.addAll(scraper.scrape())
      }
      ""
    }
    catch {
      case t: Throwable => t.getClass.getName + ": " + t.getMessage + " while scraping " + params.get("url").get
    }

    redirect("/")
  }

  post("/search") {
    val searchTerm = params.get("searchTerm").get
    results = database.queryValues(searchTerm)
    redirect("/")
  }
}