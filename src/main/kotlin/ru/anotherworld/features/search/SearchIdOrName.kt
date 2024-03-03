package ru.anotherworld.features.search

import ru.anotherworld.utils.TokenDatabase2


fun searchIdOrName(query: String): List<Pair<String, String>> {
    val tokenDatabase = TokenDatabase2()
    //Отображать логин:id
    if("id:" in query) return tokenDatabase.searchFieldsId(query.substringAfter("id:"))
    else if("name:" in query) return tokenDatabase.searchFieldsLogin(query.substringAfter("name:"))
    return tokenDatabase.searchFieldsId(query).plus(tokenDatabase.searchFieldsLogin(query))
}