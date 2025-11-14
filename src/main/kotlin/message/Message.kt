package message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Message {

    @Serializable
    @SerialName("text")
    class Text(val text: String) : Message
}