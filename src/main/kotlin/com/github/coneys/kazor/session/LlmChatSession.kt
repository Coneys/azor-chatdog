package com.github.coneys.kazor.session

import com.github.coneys.kazor.chat.Response

interface LlmChatSession {
    fun sendMessage(text: String): Response
    fun getHistory(): SessionHistory
}