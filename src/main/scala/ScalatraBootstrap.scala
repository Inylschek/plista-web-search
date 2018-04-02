import com.simon.scrapers.SimpleHttpSiteScraper
import com.simon.MyServlet
import com.simon.database.AbstractKeyValueDatabase
import com.simon.database.SimpleKeyValueDatabase
import org.scalatra.LifeCycle
import javax.servlet.ServletContext

// THIS CLASS MUST BE IN THE DEFAULT PACKAGE
class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    
    // TODO escape query string
    // TODO case insensitve searching
    val queryableDatabase = new SimpleKeyValueDatabase[String, String]((k, v, s) => v.toLowerCase.contains(s.toLowerCase))
    
    val emptyDatabaseFactory: () => AbstractKeyValueDatabase[String, String] =
      () => new SimpleKeyValueDatabase[String, String]((k, v, s) => true)
    val scraperFactory: String => SimpleHttpSiteScraper =
      (url: String) => new SimpleHttpSiteScraper(url, emptyDatabaseFactory())

    context mount (new MyServlet(queryableDatabase, scraperFactory), "/*")
  }
}
