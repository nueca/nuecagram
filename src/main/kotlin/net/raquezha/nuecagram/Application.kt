@file:OptIn(ExperimentalHoplite::class)

package net.raquezha.nuecagram

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import eu.vendeli.tgbot.api.botactions.getMe
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import net.raquezha.nuecagram.di.SystemEnvImpl
import net.raquezha.nuecagram.di.appModule
import net.raquezha.nuecagram.plugins.configureRouting
import net.raquezha.nuecagram.plugins.configureSerialization
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(): Unit =
    runBlocking {
        val bot = telegramBot(SystemEnvImpl.getBotApi())
        bot.buildBehaviourWithLongPolling {
            println(getMe())
            onCommand("start") {
                reply(it, "Hello World!")
            }
        }
        val config = config("/application.json")
        embeddedServer(
            Netty,
            watchPaths = listOf("nuecagram"),
            port = config.port,
            module = Application::module,
        ).start(true)
    }

@OptIn(ExperimentalHoplite::class)
fun config(filename: String): Config {
    val config =
        ConfigLoaderBuilder
            .default()
            .addResourceSource(filename)
            .withExplicitSealedTypes()
            .build()
            .loadConfigOrThrow<Config>()
    return config
}

fun configWithSecrets(
    filename: String,
    botApi: String,
    secretToken: String,
): ConfigWithSecrets {
    val config = config(filename)

    return ConfigWithSecrets(
        name = config.name,
        env = config.env,
        host = config.host,
        port = config.port,
        botApi = botApi,
        secretToken = secretToken,
    )
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule())
    }
    configureSerialization()
    configureRouting()
}
