package com.simon.scrapers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class JsoupTestableWrapper {

  def connect(url: String) : Document = {
    return Jsoup.connect(url).get
  }
}