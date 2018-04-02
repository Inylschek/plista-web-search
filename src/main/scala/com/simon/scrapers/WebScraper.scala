package com.simon.scrapers

import com.simon.database.AbstractKeyValueDatabase

abstract class WebScraper[K, V](startUrl: String) {
  
  def scrape() : AbstractKeyValueDatabase[K, V]
}