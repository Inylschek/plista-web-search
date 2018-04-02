# web-search

To build
mvn clean install

To run
mvn jetty:run

Then navigate to
http://localhost:8080/

Unfortunately an error occurs when running jetty, if the org.scalatest dependency is in the pom.xml
This dependency has been commented out, however to run the tests,

mvn test

, you first need to uncomment this dependency. Sorry :(

To scrape a site, enter it in the scrape field and press enter
To search a site, enter the full text term in the search field and press enter

You may search during scraping, but results from the current scrape will only
be avilable once that scrape is complete

NOTABLE FEATURE ABSENCES:
Relative links are not scraped.

Apart from case-insensitivity no processing is performed on scraped text or query text. Only exact matches return results.

Search results include all the text from a matching webpage. The result is not limited to around the matching string.