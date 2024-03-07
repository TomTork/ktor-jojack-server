package ru.anotherworld.utils

import ru.anotherworld.globalPath
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class VkIdDatabase {
    private companion object{
        private const val path = "jdbc:sqlite:$globalPath/sqldatabase/VkDatabase/VkIdDatabase.db"
        private const val tableName = "VkIdPosts"
        private const val initializeFirst = "create table if not exists $tableName(" +
                "id integer primary key," +
                "name text," +
                "url text," +
                "startId integer);"
    }
    private var conn: Connection? = null
    init {
        try {
            conn = DriverManager.getConnection(path)
            if (conn != null){
                val statement = conn!!.createStatement()
                statement.execute(initializeFirst)
            }
//            val base = "Архангельск; https://vk.com/joejackarh?w=wall-220189983_\n" +
//                    "Астрахань; https://vk.com/public221243147?w=wall-221243147_\n" +
//                    "Башкортостан; https://vk.com/bashkortjojacktar?w=wall-219916869_\n" +
//                    "Беларусь; https://vk.com/beljack?w=wall-219949363_\n" +
//                    "Белгород и белгородская область; https://vk.com/joejecks.off.belg_rad?w=wall-219932007_\n" +
//                    "Брянск/Брянская область; https://vk.com/jojack32?w=wall-219965837_\n" +
//                    "Бурятия; https://vk.com/club223289620?w=wall-223289620_\n" +
//                    "Владимир и Владимирская область; https://vk.com/jojackvladimir?w=wall-219974055_\n" +
//                    "Волгоград; https://vk.com/hive_cell_vlg?w=wall-220147865_\n" +
//                    "Воронеж; https://vk.com/vrn_jojack?w=wall-219881772_\n" +
//                    "Германия; https://vk.com/jojackgermany?w=wall-221683458_\n" +
//                    "Дальний Восток; https://vk.com/club219890941?w=wall-219890941_\n" +
//                    "Иркутск/Иркутская область; https://vk.com/iojojacks?w=wall-220875384_\n" +
//                    "Казахстан; https://vk.com/public220018815?w=wall-220018815_\n" +
//                    "Калиниград и область; https://vk.com/konigjj?w=wall-219974608_\n" +
//                    "Кировская область; https://vk.com/public219974877?w=wall-219974877_\n" +
//                    "Крым; https://vk.com/crimea_jojack?w=wall-188472836_\n" +
//                    "Кубань и Адыгея; https://vk.com/kubanjojack?w=wall-219932282_\n" +
//                    "ЛДНР; https://vk.com/jojack_ldnr?w=wall-219988167_\n" +
//                    "Липецк; https://vk.com/jojack_lip?w=wall-219981183_\n" +
//                    "Москва и область; https://vk.com/msk_jojack?w=wall-219898545_\n" +
//                    "Мурманская область/Республика Карелия; https://vk.com/polar_jojack?w=wall-218830789_\n" +
//                    "Нижегородская область; https://vk.com/nn_jojacki?w=wall-219094373_\n" +
//                    "Омск; https://vk.com/jojacki_55?w=wall-220444945_\n" +
//                    "Орёл/Орловская область; https://vk.com/club219964727?w=wall-219964727_\n" +
//                    "Пенза; https://vk.com/jojack_penza?w=wall-220864155_\n" +
//                    "Прибалтика; https://vk.com/jojackbaltic?w=wall-219936693_\n" +
//                    "Республика Удмуртия; https://vk.com/udm.jojack?w=wall-219989601_\n" +
//                    "Ростовская область; https://vk.com/club209476896?w=wall-209476896_\n" +
//                    "Рязань/Рязанская область; https://vk.com/rznjojack?w=wall-220383424_\n" +
//                    "Самара; https://vk.com/jojacki63?w=wall-219978581_\n" +
//                    "Санкт-Петербург и область; https://vk.com/lenjj?w=wall-216539100_\n" +
//                    "Саратов и Саратовская обл; https://vk.com/sarjoejack?w=wall-220241767_\n" +
//                    "Сибирь; https://vk.com/sibjoejacks?w=wall-222273262_\n" +
//                    "Смоленск; https://vk.com/public219915508?w=wall-219915508_\n" +
//                    "Татарстан; https://vk.com/tatjojacks?w=wall-220140959_\n" +
//                    "Томск; https://vk.com/public222687271?w=wall-222687271_\n" +
//                    "Тула; https://vk.com/club220024149?w=wall-220024149_\n" +
//                    "Тюмень/Тюменская область; https://vk.com/joejack_tyumen?w=wall-220020325_\n" +
//                    "Урал; https://vk.com/zjoida?w=wall-219938646_\n" +
//                    "Черноземье; https://vk.com/joejacks_off_chernozemia?w=wall-219992563_\n" +
//                    "Чувашия; https://vk.com/jojack_21?w=wall-220256747_\n" +
//                    "Ярославль и Ярославская область; https://vk.com/yaroslavljojack?w=wall-220447544_\n" +
//                    "Мятежник Джек; https://vk.com/rebel_jack?w=wall-185808385_\n" +
//                    "Uncle Joe & Neon Cherry; https://vk.com/unclejoe_neoncherry?w=wall-147353381_\n" +
//                    "Журнал \"ЖОЖ\"; https://vk.com/journal_joj?w=wall-221144264_\n" +
//                    "ВечноБОРОД; https://vk.com/levoborod?w=wall-186696888_\n" +
//                    "МГС КНДР; https://vk.com/dprk_solidarity_group?w=wall-144745573_"
//            val rowGroups = base.split("\n").map { it.split("; ") }
//            val rowIds = "108\n110\n2918\n1216\n191\n220\n1\n198\n153\n212\n82\n4505\n413\n941\n103\n158\n1591\n5658\n31\n102\n5441\n749\n991\n32\n1045\n85\n680\n266\n714\n986\n207\n4188\n323\n3595\n3658\n1435\n172\n175\n449\n6643\n96\n84\n821\n1112869\n231987\n16749\n99037\n18859"
//                .split("\n").map { it.toInt() }
//            for(i in rowGroups.indices){
//                insertAll(rowGroups[i][0], rowGroups[i][1], rowIds[i])
//            }
        } catch (e: SQLException){
            println(e.message)
        }
    }
    fun setStartId(id: Int, value: Int){
        val sql = "update $tableName set startId=? where id=?"
        try {
            val statement = conn!!.prepareStatement(sql)
            statement.setInt(1, value)
            statement.setInt(2, id)
            statement.executeUpdate()
        } catch (e: SQLException){
            println(e.message)
        }
    }
    fun getStartId(id: Int): Int{
        val sql = "select startId from $tableName where id=$id"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            return res.getInt("startId")
        } catch (e: SQLException){
            println(e.message)
            return -1
        }
    }
    fun getUrl(id: Int): String{
        val sql = "select url from $tableName where id=$id"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            return res.getString("url")
        } catch (e: SQLException){
            println(e.message)
            return ""
        }
    }
    fun selectAllStartIds(): String{
        val sql = "SELECT * FROM $tableName"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            var result = ""
            while (res.next()){
                result += "${res.getInt("startId")};"
            }
            return result
        }catch (e: SQLException){
            println(e.message)
            return "null"
        }
    }
    fun selectAllUrls(): String{
        val sql = "SELECT * FROM $tableName"
        try {
            val statement = conn!!.createStatement()
            val res = statement.executeQuery(sql)
            var result = ""
            while (res.next()){
                result += "${res.getString("url")};"
            }
            return result
        }catch (e: SQLException){
            println(e.message)
            return "null"
        }
    }
    fun dropTable(){
        val sql = "drop table $tableName"
        val statement = conn!!.createStatement()
        statement.execute(sql)
    }
    fun insertAll(name: String, url: String, startId: Int){
        val sql = "insert into ${tableName}(name,url,startId) values(?,?,?)"
        try {
            val statement = conn!!.prepareStatement(sql)
            statement.setString(1, name)
            statement.setString(2, url)
            statement.setInt(3, startId)
            statement.executeUpdate()
            statement.close()
        } catch (e: SQLException){
            println(e.message)
        }
    }
    fun updateAll(id: Int, name: String, url: String, startId: Int){
        val sql = "update $tableName set name=?,url=?,startId=? where id=?"
        try {
            val statement = conn!!.prepareStatement(sql)
            statement.setString(1, name)
            statement.setString(2, url)
            statement.setInt(3, startId)
            statement.setInt(4, id)
            statement.executeUpdate()
            statement.close()
        } catch (e: SQLException){
            println(e.message)
        }
    }
    fun deleteUser(id: Int){
        val sql = "delete from $tableName where id=?"
        try {
            val statement = conn!!.prepareStatement(sql)
            statement.setInt(1, id)
            statement.executeUpdate()
        } catch (e: SQLException){
            println(e.message)
        }
    }
}