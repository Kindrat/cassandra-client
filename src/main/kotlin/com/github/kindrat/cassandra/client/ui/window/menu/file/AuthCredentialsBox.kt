package com.github.kindrat.cassandra.client.ui.window.menu.file

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.UIProperties
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.VBox

class AuthCredentialsBox internal constructor(
        private val localeService: MessageByLocaleService,
        uiProperties: UIProperties
) : VBox() {

    private val username: TextField
    private val password: TextField

    fun getUsername(): String {
        return username.text
    }

    fun getPassword(): String {
        return password.text
    }

    fun setUsername(userId: String?) {
        username.text = userId
    }

    fun setPassword(passwd: String?) {
        password.text = passwd
    }

    override fun setWidth(value: Double) {
        super.setWidth(value)
        username.minWidth = value
        username.maxWidth = value
        username.prefWidth = value
        password.minWidth = value
        password.maxWidth = value
        password.prefWidth = value
    }

    private val usernameField: TextField
        get() {
            val username = TextField()
            username.promptText = localeService.getMessage("ui.menu.file.connect.auth.username")
            username.alignment = Pos.CENTER
            return username
        }

    private val passwordField: TextField
        get() {
            val username = TextField()
            username.promptText = localeService.getMessage("ui.menu.file.connect.auth.password")
            username.alignment = Pos.CENTER
            return username
        }

    init {
        username = usernameField
        password = passwordField
        children.add(username)
        children.add(password)
        height = uiProperties.credentialsBoxHeight
        spacing = uiProperties.credentialsBoxSpacing
        isVisible = false
    }
}