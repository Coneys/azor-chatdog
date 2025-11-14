package console

fun getSessionIdFromCli(args: Array<String>): String? {
    var sessionId: String? = null

    for (i in args.indices) {
        when {
            args[i] == "--session-id" && i + 1 < args.size ->
                sessionId = args[i + 1]

            args[i].startsWith("--session-id=") ->
                sessionId = args[i].substringAfter("=")
        }
    }

    return sessionId
}
