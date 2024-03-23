package ru.anotherworld.tests

import org.jsoup.Jsoup
import org.koin.core.component.getScopeName
import ru.anotherworld.Cipher
import ru.anotherworld.RSAKotlin
import ru.anotherworld.features.vk.FromUrl
import ru.anotherworld.features.vk.VkImageAndVideo
import ru.anotherworld.utils.VkPostDatabase2
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

fun generatePairKeys(): Pair<String, String>{ //Private and public keys
    val privateKey: PrivateKey
    val publicKey: PublicKey
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(1024)
    val pair = keyGen.generateKeyPair()
    privateKey = pair.private
    publicKey = pair.public
    return Pair(
        Base64.getEncoder().encodeToString(privateKey.encoded),
        Base64.getEncoder().encodeToString(publicKey.encoded))
}

fun main(){
//    val pair = generatePairKeys()
//    println("PR:${pair.first}\nPU:${pair.second}")
    val url = "https://vk.com/wall-216539100_4470"
//    val url = "https://vk.com/wall-40886007_1182807"
//    getContentFromUrl("https://vk.com/academicart?w=wall-40886007_1182807").image.images.map { it->
//        println(it)
//    }
//    val doc = Jsoup.connect(url).post()
//    val regex = Regex("https://sun(.*?)\"")
//    val matches = regex.findAll(doc.toString())
//    println(matches
//        .map { "https://sun" + it.groupValues[1]
//            .replace("amp;", "") }
//        .filter { "uniq_tag" in it }
//        .filterNot { "blur" in it }
//        .joinToString("\n"))
//    println(doc.select("meta[property=\"og:video\"]").attr("content"))
//    getContentFromUrl2("https://vk.com/lenjj?w=wall-216539100_4470")
}
private fun getContentFromUrl2(url: String): FromUrl{
    val doc = Jsoup.connect("https://vk.com/" + url.substringAfter("?w=")).post()
    val nameGroup = doc.getElementsByClass("ui_ownblock_label").toString()
        .substringAfter(">").substringBefore("<")
        .replace("\n", "")
        .replaceFirst(" ", "")
    val content = doc.getElementsByClass("wall_post_text").toString().replace("<br>", "")

    val regex1 = Regex("mention_id=\"(.*?)\"")
    val matches1 = regex1.findAll(content)
    val listsId = matches1.map { it.groupValues[1] }.toList()

    val regex2 = Regex("\">(.*?)</a>")
    val matches2 = regex2.findAll(content)
    val namesId = matches2.map { it.groupValues[1] }.filterNot { "#" in it }.filterNot { "@" in it }.toList()

    var textPost = doc.head()
        .select("meta[property=\"og:description\"]")
        .attr("content")
        .replace("<br>", "\n")
    if (namesId.isNotEmpty() && listsId.isNotEmpty()){
        for(i in namesId.indices){
            textPost = textPost.replace(namesId[i], "[${namesId[i]}|${listsId[i]}]")
        }
    }

    val regex3 = Regex("https://sun(.*?)\"")
    val matches3 = regex3.findAll(doc.toString())
    val iconGroup = matches3
            .map { "https://sun" + it.groupValues[1]
            .replace("amp;", "") }
            .filter { "cs" in it }
            .toList()[0]
            .substringBeforeLast("&cs=") + "&cs=600x600"

    val regex4 = Regex("https://sun(.*?)\"")
    val matches4 = regex4.findAll(doc.toString())
    val images: List<String> = matches4
        .map { "https://sun" + it.groupValues[1]
            .replace("amp;", "") }
        .filter { "uniq_tag" in it }
        .filterNot { "blur" in it }
        .toList()

    val video = doc.select("meta[property=\"og:video\"]").attr("content")
    val title = doc.head().select("meta[property=\"og:title\"]").attr("content")

    val repost = doc.getElementsByClass("copy_author")

    var origName = ""
    var origPost = ""

    if (repost.toString() != "" && repost.toString() != "null"){ //REPOSTED
        origName = repost.toString().substringAfter(">").substringBefore("<")
        origPost = "https://vk.com/wall" + repost.attr("data-post-id")
        return FromUrl(nameGroup, textPost, VkImageAndVideo(images, video),
            url, iconGroup, title, true, origName, origPost)
    }
    return FromUrl(nameGroup, textPost, VkImageAndVideo(images, video),
        url, iconGroup, title, false, origName, origPost)
}

private fun getContentFromUrl(url: String): FromUrl {
    val doc = Jsoup.connect(url).post()
    val nameGroup = doc.title().substringBefore("|")
    val content = doc.head().select("meta[property=\"og:description\"]").attr("content")
        .replace("<br>", "\n")
    val image = doc.head().select("meta[property=\"og:image\"]").attr("content").replace("&amp", "")
    val originalUrl = doc.head().select("meta[property=\"og:url\"]").attr("content")
    val urlToIconGroup = "https://vk.com/" + doc.getElementsByClass("redesigned-group-avatar").attr("href")
    val doc2 = Jsoup.connect(urlToIconGroup).post()
    val iconGroup = doc2.head().select("meta[name=\"og:image\"]").attr("value").replace("&amp", "")
    val title = doc.head().select("meta[property=\"og:title\"]").attr("content")

    val doc3 = Jsoup.connect("https://vk.com/" + url.substringAfter("?w=")).post()
    val repost = doc3.getElementsByClass("copy_author")
    val rep: Boolean
    var origName2 = ""
    var origPost = ""
    if (repost.toString() != "" && repost.toString() != "null"){
        origName2 = repost.toString().substringAfter(">").substringBefore("<")
        origPost = "https://vk.com/wall" + repost.attr("data-post-id")

        val doc4 = Jsoup.connect(origPost).post()
        val listImages = doc4.getElementsByClass("PhotoPrimaryAttachment__interactive PhotoPrimaryAttachment__interactive--clickable")
            .toString().replace("amp;", "").split("\n")
            .map { it -> it.split(" ").filter { "src" in it } }
            .filter { it.isNotEmpty() }.map { it[1].substringAfter("src=\"").substringBefore("\"") }

        val video = doc.select("meta[property=\"og:video\"]").attr("content")
        rep = true

        if(listImages.isEmpty()){
            val newImage = doc4.getElementsByClass("MediaGrid__imageSingle").attr("src")
            return FromUrl(nameGroup, content, VkImageAndVideo(listOf(newImage), video),
                originalUrl, iconGroup, title, rep, origName2, origPost)
        }

        return FromUrl(nameGroup, content, VkImageAndVideo(listImages, video),
            originalUrl, iconGroup, title, rep, origName2, origPost)
    }
    else {
        val listImages = doc3.getElementsByClass("PhotoPrimaryAttachment__interactive PhotoPrimaryAttachment__interactive--clickable")
            .toString().replace("amp;", "").split("\n")
            .map { it -> it.split(" ").filter { "src" in it } }
            .filter { it.isNotEmpty() }.map { it[1].substringAfter("src=\"").substringBefore("\"") }
        if(listImages.isEmpty()){
            //TODO
        }
        val video = doc3.select("meta[property=\"og:video\"]").attr("content")
        rep = false

        return FromUrl(nameGroup, content, VkImageAndVideo(listImages, video),
            originalUrl, iconGroup, title, rep, origName2, origPost)
    }
}

private fun getLastIdPost(url: String): String{
    val doc = Jsoup.connect(url.substringBefore("?")).post()
    val lastPost = doc.getElementsByClass("wall_posts own mark_top ").toString()
        .substringBefore("onclick").substringAfter("data-post-id=")
        .substringAfter("_").substringBefore("\"")
    return lastPost
}

private fun getLastIdPost2(url: String): String{
    val doc = Jsoup.connect(url.substringBefore("?")).post()
    val lastPost = doc.getElementsByClass("wall_posts all mark_top ").toString()
        .substringBefore("onclick")
        .substringBefore(" class=\"_post")
        .substringAfterLast("id=")
        .substringAfter("_")
        .substringBefore("\"")

    return lastPost
}