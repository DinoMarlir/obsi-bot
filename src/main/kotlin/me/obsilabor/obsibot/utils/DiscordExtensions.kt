package me.obsilabor.obsibot.utils

import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.Guild
import me.obsilabor.obsibot.data.ObsiGuild
import me.obsilabor.obsibot.database.MongoManager
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

@KordPreview
fun Guild.obsify(): ObsiGuild? {
    return MongoManager.guilds.findOne { ObsiGuild::id eq id }
}

@KordPreview
fun Guild.createObsiGuild(): ObsiGuild {
    val obsiGuild = ObsiGuild.newDocument(id)
    MongoManager.guilds.insertOne(obsiGuild)
    return obsiGuild
}