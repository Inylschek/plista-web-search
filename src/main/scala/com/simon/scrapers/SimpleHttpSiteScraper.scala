package com.simon.scrapers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import scala.annotation.tailrec
import scala.collection.JavaConversions._

import com.simon.database.AbstractKeyValueDatabase

class SimpleHttpSiteScraper(startUrl: String,
                            newDatabaseFactory: () => AbstractKeyValueDatabase[String, String])
        extends WebScraper[String, String](startUrl) {

  require(try { Jsoup.connect(startUrl).get; true } catch { case t: Throwable => false })
  val host: String = new URL(startUrl).getHost
  require(host.length > 1)

  val start: String = if (startUrl.charAt(startUrl.length - 1) == '/') startUrl else startUrl.concat("/")

  def scrape() : AbstractKeyValueDatabase[String, String] = {
    // TODO add scraping of relative links
    // TODO ensure links visit are from host site, and do not simply contain host site nested within the url
    def findNewLinks(visited: Set[String], toVisit: List[String], urlString: String, doc: Document) : List[String] = {
      try {
        // return NIL if not a valid URL
        val url = new URL(urlString)

        doc.select("a[href*=" + host + "]")
          .eachAttr("href")
          .distinct
          .filter((l: String) => l != urlString && l.startsWith("http") && l.split("\\s").length == 1)
          .map((l: String) => if (l.charAt(l.length - 1) == '/') l else l.concat("/"))
          .filter((l: String) => !visited.contains(l) && !toVisit.contains(l)).toList
      }
      catch {
        case t:Throwable => Nil
      }
    }

    @tailrec def siteLoop(visited: Set[String],
                          toVisit: List[String],
                          database: AbstractKeyValueDatabase[String, String])
                : AbstractKeyValueDatabase[String, String] = toVisit match {
      case Nil => return database
      case urlString :: xs => {
        val doc: Document = getDocument(urlString)
        
        // TODO delete
        Console.println(toVisit.size + ", " + visited.size + ": " + urlString)

        val newLinks: List[String] = findNewLinks(visited, xs, urlString, doc)

        database.add(urlString, doc.text())
        siteLoop(visited + urlString, xs ::: newLinks, database)
      }
    }

    siteLoop(Set(), start :: Nil, newDatabaseFactory())
  }

    def getDocument(urlString: String) : Document = {
      try {
        Jsoup.connect(urlString).get
      }
      catch {
        case t: Throwable => new Document("")
      }
    }
}