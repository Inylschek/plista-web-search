package com.simon.scrapers

import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.mockito.Mockito

import java.net.URL

import com.simon.database.SimpleKeyValueDatabase
import com.simon.database.AbstractKeyValueDatabase

class SimpleHttpSiteScraperTest {

  val exampleUrl: String = "http://www.example.com/"

  // @formatter:off
  def testFindNewLinks_BadUrlReturnsEmptyList() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, "not a real url", doc)

    assert(received == Nil)
  }

  def testFindNewLinks_NoAnchorsReturnsNoLinks() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  def testFindNewLinks_AnchorWithoutHrefReturnsNoLinks() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a>text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  // FIXME We don't want to ignore relative links.
  def testFindNewLinks_RelativeLinkIgnored() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"/relative/link\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == Nil)
    // TODO assert(received == "http://www.example.com/relative/link/ :: Nil)
  }

  def testFindNewLinks_AbsoluteLinkToDifferentSiteIgnored() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.unknown.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  // FIXME when the host is embedded within a link to another site, we should ignore it
  def testFindNewLinks_HostEmbeddedInAbsoluteLinkToDifferentSiteNotIgnored() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.unknown.com/www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == "http://www.unknown.com/www.example.com/tags/my-tag/" :: Nil)
//    assert(received == Nil)
  }

  def testFindNewLinks_SingleAbsoluteLinkToThisSiteReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags/my-tag/\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == "http://www.example.com/tags/my-tag/" :: Nil)
  }

  def testFindNewLinks_SingleAbsoluteLinkWithoutTrailingSlashReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == "http://www.example.com/tags/my-tag/" :: Nil)
  }

  def testFindNewLinks_LinkAppearsTwiceIsReturnedOnce() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags/my-tag\">text</a>" +
                       "    <a href=\"http://www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == "http://www.example.com/tags/my-tag/" :: Nil)
  }

  def testFindNewLinks_TwoDifferentLinksBothReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags/my-tag1\">text</a>" +
                       "    <a href=\"http://www.example.com/tags/my-tag2\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received.size == 2)
    assert(received.contains("http://www.example.com/tags/my-tag1/"))
    assert(received.contains("http://www.example.com/tags/my-tag2/"))
  }

  def testFindNewLinks_LinkWithWhitespaceIsNotReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags my tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received ==  Nil)
  }

  def testFindNewLinks_LinkToThisPageIsNotReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  def testFindNewLinks_AlreadyVisitedLinkIsNotReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set("http://www.example.com/tags/my-tag/"), Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  def testFindNewLinks_KnownLinkToBeVisitedIsNotReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), "http://www.example.com/tags/my-tag/" :: Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  def testFindNewLinks_LinkToHttpsIsReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"https://www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == "https://www.example.com/tags/my-tag/" :: Nil)
  }

  def testFindNewLinks_LinkToNonHttpOrHttpsProtocolIsNotReturned() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"ftp://www.example.com/tags/my-tag\">text</a>" +
                       "</body>"

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def doc: Document = Jsoup.parse(html)

    def received = scraper.findNewLinks(Set(), Nil, exampleUrl, doc)

    assert(received == Nil)
  }

  def testScrape_PageWithNoLinksReturnsMapWithOnePair() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "</body>"
    def pageText: String = Jsoup.parse(html).text

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def received: List[(String, String)] = scraper.scrape().getTraversable().toList

    assert(received.size == 1)
    assert(received.contains((exampleUrl, pageText)))
  }

  def testScrape_PageWithLinkToItselfReturnsMapWithOnePair() {
    def html: String = "<head>" +
                       "    <title>My Test Page</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading</h1>" +
                       "    <p>stuff</p>" +
                       "    <a href=\"http://www.example.com/\">text</a>" +
                       "</body>"
    def pageText: String = Jsoup.parse(html).text

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(exampleUrl -> html))
    def received: List[(String, String)] = scraper.scrape().getTraversable().toList

    assert(received.size == 1)
    assert(received.contains((exampleUrl, pageText)))
  }

  def testScrape_TwoPagesLinkingToEachOtherBothReturned() {
    def url1 = exampleUrl;
    def html1: String = "<head>" +
                       "    <title>Page 1</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading 1</h1>" +
                       "    <p>stuff 1</p>" +
                       "    <a href=\"http://www.example.com/2\">text1</a>" +
                       "</body>"
    def page1Text: String = Jsoup.parse(html1).text

    def url2 = "http://www.example.com/2/"
    def html2: String = "<head>" +
                       "    <title>Page 2</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading 2</h1>" +
                       "    <p>stuff 2</p>" +
                       "    <a href=\"http://www.example.com/\">text2</a>" +
                       "</body>"
    def page2Text: String = Jsoup.parse(html2).text

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(url1 -> html1, url2 -> html2))
    def received: List[(String, String)] = scraper.scrape().getTraversable().toList

    assert(received.size == 2)
    assert(received.contains((url1, page1Text)))
    assert(received.contains((url2, page2Text)))
  }

  def testScrape_ThreePageSiteExternalLinksNotReturned() {
    def url1 = exampleUrl;
    def html1: String = "<head>" +
                       "    <title>Page 1</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading 1</h1>" +
                       "    <p>stuff 1</p>" +
                       "    <a href=\"http://www.example.com/\">text1</a>" +
                       "    <a href=\"http://www.example.com/2/\">text1</a>" +
                       "    <a href=\"http://www.example.com/3/\">text1</a>" +
                       "    <a href=\"http://www.external.com/\">text1</a>" +
                       "</body>"
    def page1Text: String = Jsoup.parse(html1).text

    def url2 = "http://www.example.com/2/"
    def html2: String = "<head>" +
                       "    <title>Page 2</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading 2</h1>" +
                       "    <p>stuff 2</p>" +
                       "    <a href=\"http://www.example.com/\">text2</a>" +
                       "    <a href=\"http://www.example.com/2/\">text2</a>" +
                       "    <a href=\"http://www.example.com/3/\">text2</a>" +
                       "    <a href=\"http://www.external.com/\">text1</a>" +
                       "</body>"
    def page2Text: String = Jsoup.parse(html2).text

    def url3 = "http://www.example.com/3/"
    def html3: String = "<head>" +
                       "    <title>Page 3</title>" +
                       "</head>" +
                       "<body>" +
                       "    <h1>Heading 3</h1>" +
                       "    <p>stuff 3</p>" +
                       "    <a href=\"http://www.example.com/\">text3</a>" +
                       "    <a href=\"http://www.example.com/2/\">text3</a>" +
                       "    <a href=\"http://www.example.com/3/\">text3</a>" +
                       "    <a href=\"http://www.external.com/\">text1</a>" +
                       "</body>"
    def page3Text: String = Jsoup.parse(html3).text

    def scraper = newSimpleHttpSiteScraper(exampleUrl, Map(url1 -> html1, url2 -> html2, url3 -> html3))
    def received: List[(String, String)] = scraper.scrape().getTraversable().toList

    assert(received.size == 3)
    assert(received.contains((url1, page1Text)))
    assert(received.contains((url2, page2Text)))
    assert(received.contains((url3, page3Text)))
  }

  // @formatter:on

  def newSimpleHttpSiteScraper() : SimpleHttpSiteScraper = {
    val jsoup: JsoupTestableWrapper = Mockito.mock(classOf[JsoupTestableWrapper])
    new SimpleHttpSiteScraper(exampleUrl, new SimpleKeyValueDatabase[String, String]((k, v, s) => true), jsoup)
  }

  def newSimpleHttpSiteScraper(url: String, map: Map[String, String]) : SimpleHttpSiteScraper = {
    val jsoup: JsoupTestableWrapper = Mockito.mock(classOf[JsoupTestableWrapper])
    map.foreach(kv => Mockito.when(jsoup.connect(kv._1)).thenReturn(Jsoup.parse(kv._2)))
    new SimpleHttpSiteScraper(url, new SimpleKeyValueDatabase[String, String]((k, v, s) => true), jsoup)
  }
}