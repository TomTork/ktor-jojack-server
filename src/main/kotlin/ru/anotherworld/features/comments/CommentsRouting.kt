package ru.anotherworld.features.comments

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.anotherworld.utils.CommentsData
import ru.anotherworld.utils.CommentsDatabase
import ru.anotherworld.utils.IncomingCommentsData
import ru.anotherworld.utils.TokenDatabase2

fun Application.configureCommentsRouting() {
    val commentsDatabase = CommentsDatabase()
    val tokenDatabase2 = TokenDatabase2()
    routing {
        get("/comments"){
            val id = call.parameters["idx"]
            if(id != null) call.respond(GetComments(commentsDatabase.getAllCommentsById(id.toInt())))
        }
        post("/new-comment"){
            val data = call.receive<IncomingCommentsData>()
            if(tokenDatabase2.getLoginByToken(data.token) == data.author){
                commentsDatabase.insertAll(
                    CommentsData(
                        id = data.id,
                        author = data.author,
                        text = data.text,
                        likes = data.likes,
                        answer = data.answer
                    )
                )
            }
        }
        post("/edit-comment"){
            val data = call.receive<IncomingCommentsData>()
            if(tokenDatabase2.getLoginByToken(data.token) == data.author){
                commentsDatabase.editAll(
                    CommentsData(
                        id = data.id,
                        author = data.author,
                        text = data.text,
                        likes = data.likes,
                        answer = data.answer
                    )
                )
            }
        }
    }
}

@Serializable
data class GetComments(
    val list: List<CommentsData>
)