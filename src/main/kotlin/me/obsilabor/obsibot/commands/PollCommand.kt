package me.obsilabor.obsibot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.long
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import me.obsilabor.obsibot.data.Poll
import me.obsilabor.obsibot.localization.globalText
import me.obsilabor.obsibot.localization.localText
import me.obsilabor.obsibot.utils.*

@KordPreview
class PollCommand : Extension() {

    override val name: String = "pollcommand"

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "poll"
            description = globalText("command.poll.description")

            ephemeralSubCommand(::PollCreateArgs) {
                name = "create"
                description = globalText("command.poll.create.description")

                action {
                    val obsiGuild = guild?.asGuild()?.obsify() ?: guild?.asGuild()?.createObsiGuild()!!
                    if(member?.asMember()?.hasRole(obsiGuild.pollRole?: Snowflake(0)) == false) {
                        respond {
                            embed {
                                title = localText("generic.nopermissions.short", obsiGuild)
                                description = localText("command.poll.rolerequired", obsiGuild)
                                applyDefaultFooter()
                            }
                        }
                        return@action
                    }
                    val customId = StringUtils.getRandomID()+user.id.toString()
                    val options = arguments.options.split(",")
                    val map = hashMapOf<String, Int>()
                    options.forEach {
                        map[it] = 0
                    }
                    var totalVotes = 0
                    map.values.forEach {
                        totalVotes+=it
                    }
                    if(totalVotes == 0) {
                        totalVotes = 1
                    }
                    val message = channel.createMessage {
                        content = "**${localText("poll", obsiGuild)}**"
                        embed {
                            color = Color(7462764)
                            author {
                                name = member?.asMember()?.displayName
                                icon = user.asUser().avatar?.url
                            }
                            title = localText("poll", obsiGuild)
                            val builder = StringBuilder()
                            options.forEachIndexed { index, it ->
                                kotlin.runCatching {
                                    val votes = map.getOrDefault(it, 0)
                                    val percentage = votes / totalVotes
                                    builder.append("${index+1}: $it - $percentage% - $votes ${localText("poll.votes", obsiGuild)}")
                                    builder.appendLine()
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }
                            builder.appendLine()
                            builder.appendLine(localText("poll.instructions", hashMapOf("endtimestamp" to arguments.endTimestamp), obsiGuild))
                            description = builder.toString()
                            applyDefaultFooter()
                        }
                        actionRow {
                            selectMenu(customId) {
                                options.forEach {
                                    option(it, it) {
                                        description = localText("poll.option.description", hashMapOf("option" to it), obsiGuild)
                                    }
                                }
                            }
                        }
                    }

                    val poll = Poll(guild?.id?:return@action, channel.id, customId, message.id, user.id, map, arguments.endTimestamp, hashMapOf(), false)
                    obsiGuild.adoptNewPoll(poll)
                    obsiGuild.update()
                    respond {
                        content = localText("command.poll.create.success", obsiGuild)
                    }
                }
            }
        }
    }

    inner class PollCreateArgs : Arguments() {
        val endTimestamp by long {
            name = "endtimestamp"
            description = globalText("command.poll.create.arguments.endtimestamp.description")
        }

        val options by string {
            name = "options"
            description = globalText("command.poll.create.arguments.options.description")
        }
    }
}