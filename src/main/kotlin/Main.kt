import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.URL

const val BASE_ADDRESS = "https://www.zoopla.co.uk";

const val CACHE_DIR = "responses-cache/"

const val LISTINGS_CLASS = ".listing-results-price"
const val PRICE_CLASS = ".ui-pricing__main-price"

fun makeGoodFilename(filename: String): String {
    return filename.replace("[^a-zA-Z0-9.\\-]".toRegex(), "_")
}

fun sendQuery(url: String, useCache: Boolean = true): String? {
    val fileCache = File(CACHE_DIR + makeGoodFilename(url))
    if (useCache && fileCache.exists()) {
        val cachedRes = fileCache.readText()
        if (cachedRes.isEmpty()) {
            return null;
        }
        return cachedRes;
    }
    return try {
        val response = URL(url).readText()
        fileCache.writeText(response)
        response
    } catch (e: IOException) {
        fileCache.writeText("");
        null;
    }
}

fun buildLink(relative: String) = BASE_ADDRESS + relative

fun getPhotosLink(id: Int) = "https://www.zoopla.co.uk/to-rent/details/photos/$id"

fun getIdFromLink(link: String): Int {
    return link.substring(link.lastIndexOf('/') + 1).toInt();
}

fun parseMonthPrice(s: String): Int? {
    if (s[0] != '£') {
        return null;
    }
    if (!s.endsWith("pcm")) {
        return null;
    }
    return s.substring(1).replace(",", "").split(' ').first().toInt()
}

fun handleResponse(response: String) {
    val properties = Jsoup.parse(response).select(LISTINGS_CLASS)

    val links = properties.map {
        it.attr("href")
    }

    println("total " + links.size + " properties!")

    val firstLink = buildLink(links[0])

    links.forEach { linkIt ->
        val link = buildLink(linkIt)
        System.err.println("Send query $link")
        val propertyId = getIdFromLink(link)
        val property = sendQuery(link)
        val priceStr = Jsoup.parse(property).selectFirst(PRICE_CLASS).text()
        val pricePoundsPerMonth = parseMonthPrice(priceStr)

        System.err.println("price (pounds / month) = $pricePoundsPerMonth")

        val photosPage = sendQuery(getPhotosLink(propertyId))
        if (photosPage != null) {
            val photos = Jsoup.parse(photosPage).getElementsByTag("img").filter {
                !it.attr("style").isEmpty()
            }.map { it.attr("src") }
        }


    }

    println(firstLink)


}

fun sendRequest() {
    val url = BASE_ADDRESS + "/to-rent/property/london/britton-street/ec1m-5ny/?added=24_hours&include_shared_accommodation=false&price_frequency=per_month&q=ec1m%205ny&radius=1&results_sort=newest_listings&search_source=home&page_size=100"
    val response = sendQuery(url)!!
    handleResponse(response)
}

fun main(args: Array<String>) {
    println("Start!")
    sendRequest()
}