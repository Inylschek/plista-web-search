package com.simon.scrapers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.net.URL

import scala.annotation.tailrec
import scala.collection.JavaConversions._

import com.simon.database.AbstractKeyValueDatabase

class SimpleHttpSiteScraper(startUrl: String,
                            database: AbstractKeyValueDatabase[String, String],
                            val jsoup: JsoupTestableWrapper = new JsoupTestableWrapper)
        extends WebScraper[String, String](startUrl) {

  require(try { jsoup.connect(startUrl); true } catch { case t: Throwable => false })
  val host: String = new URL(startUrl).getHost
  require(host.length > 1)

  val start: String = addTrailingSlash(startUrl)

  def scrape() : AbstractKeyValueDatabase[String, String] = {
    @tailrec
    def siteLoop(visited: Set[String],
                          toVisit: List[String],
                          database: AbstractKeyValueDatabase[String, String])
                : AbstractKeyValueDatabase[String, String] = toVisit match {
      case Nil => return database
      case urlString :: xs => {
        // TODO delete
        Console.println(toVisit.size + ", " + visited.size + ": " + urlString)

        val doc: Document = getDocument(urlString)
        val newLinks: List[String] = findNewLinks(visited, xs, urlString, doc)

        database.add(urlString, doc.text())
        siteLoop(visited + urlString, xs ::: newLinks, database)
      }
    }

    siteLoop(Set(), start :: Nil, database)
  }
  
  // TODO add scraping of relative links
  // TODO ensure links visit are from host site, and do not simply contain host site nested within the url
  // TODO move method to it's own class
  // TODO merge visited and toVisit parameters as single blacklist parameter
  // TODO this method only checks the href attribute of <a> tags. Are other links possible?
  def findNewLinks(visited: Set[String], toVisit: List[String], urlString: String, doc: Document) : List[String] = {
    try {
      // return NIL if not a valid URL
      val url = new URL(urlString)

      doc.select("a[href*=" + host + "]")
        .eachAttr("href")
        .distinct
        .filter((s: String) => s != urlString && s.startsWith("http") && s.split("\\s").length == 1)
        .map((s: String) => addTrailingSlash(s))
        .filter((s: String) => !visited.contains(s) && !toVisit.contains(s)).toList
    }
    catch {
      case t:Throwable => Nil
    }
  }
  
  def addTrailingSlash(url: String) : String = {
    if (url.charAt(url.length - 1) == '/') url else url.concat("/")
  }

  def getDocument(urlString: String) : Document = {
    try {
      jsoup.connect(urlString)
    }
    catch {
      case t: Throwable => new Document("")
    }
  }
}