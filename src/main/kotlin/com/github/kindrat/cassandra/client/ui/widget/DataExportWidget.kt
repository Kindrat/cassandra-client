package com.github.kindrat.cassandra.client.ui.widget

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.model.CsvTargetMetadata
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.ui.fx.component.LabeledComponentColumn
import com.github.kindrat.cassandra.client.ui.listener.CheckBoxListener.Companion.create
import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import com.github.kindrat.cassandra.client.util.UIUtil.setWidth
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections.observableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVFormat.Predefined
import org.apache.commons.csv.QuoteMode
import org.apache.commons.lang3.StringUtils
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import java.io.File
import java.util.*
import java.util.stream.Collectors

class DataExportWidget(
        parent: Stage,
        private val table: String,
        localeService: MessageByLocaleService,
        private val properties: UIProperties
) : Stage() {

    private val metadataProcessor = MonoProcessor.create<CsvTargetMetadata>()

    val metadata: Mono<CsvTargetMetadata>
        get() {
            show()
            return metadataProcessor
        }

    private fun buildScene(): Scene {
        val container = AnchorPane()
        val editor = VBox()
        editor.spacing = properties.exportSpacing
        editor.alignment = Pos.TOP_CENTER
        container.children.add(editor)
        fillParent(editor)
        val pathField = TextField()
        val location = this.javaClass.protectionDomain.codeSource.location
        val defaultDir = File(location.toURI()).parentFile
        pathField.text = "$defaultDir/$table.csv"
        val startButton = Button("Start export")
        val settingsContainer = HBox()
        settingsContainer.prefWidthProperty().bind(editor.widthProperty())
        val formatLabel = Label("CSV format")
        val formats = buildFormatBox(200)
        val customizeCheckbox = CheckBox("override format settings")
        customizeCheckbox.selectedProperty().addListener(create(
                Runnable {
                    val formatSettingsBox = buildFormatSettingsBox(formats)
                    formatSettingsBox.prefWidthProperty().bind(settingsContainer.widthProperty())
                    settingsContainer.children.add(formatSettingsBox)
                    startButton.onAction = EventHandler {
                        if (customizeCheckbox.isSelected) {
                            val csvFormat = formatSettingsBox.build()
                            val target = File(pathField.text)
                            metadataProcessor.onNext(CsvTargetMetadata(table, csvFormat, target))
                            hide()
                        }
                    }
                },
                Runnable { settingsContainer.children.clear() })
        )
        startButton.onAction = EventHandler {
            if (!customizeCheckbox.isSelected) {
                val formatSettingsBox = buildFormatSettingsBox(formats)
                val csvFormat = formatSettingsBox.build()
                val target = File(pathField.text)
                metadataProcessor.onNext(CsvTargetMetadata(table, csvFormat, target))
                hide()
            }
        }
        formats.selectionModel.selectedItemProperty()
                .addListener { _: ObservableValue<out String>, oldValue: String, newValue: String ->
                    if (oldValue != newValue) {
                        customizeCheckbox.isSelected = !customizeCheckbox.isSelected
                        customizeCheckbox.isSelected = !customizeCheckbox.isSelected
                    }
                }
        val format = HBox(formatLabel, formats, customizeCheckbox)
        format.alignment = Pos.CENTER
        format.spacing = properties.exportSpacing
        pathField.prefWidthProperty().bind(editor.widthProperty().multiply(0.6))
        val destinationButton = Button("Choose destination")
        val savePath = HBox(pathField, destinationButton)
        savePath.alignment = Pos.CENTER
        savePath.spacing = properties.exportSpacing
        destinationButton.onAction = EventHandler {
            val savePathProvider = FileChooser()
            savePathProvider.title = "Save CSV"
            savePathProvider.initialFileName = "$table.csv"
            savePathProvider.selectedExtensionFilter = FileChooser.ExtensionFilter("CSV", "csv")
            val file = savePathProvider.showSaveDialog(this)
            if (file != null) {
                pathField.text = file.absolutePath
            }
        }
        editor.children.addAll(format, savePath, settingsContainer, startButton)
        return Scene(container, properties.exportWidth, properties.exportHeight)
    }

    private fun buildFormatBox(width: Int): ComboBox<String> {
        val box = ComboBox(observableList(CSV_FORMATS))
        box.selectionModel.select(Predefined.Default.toString())
        box.minWidth = width - 20.toDouble()
        box.maxWidth = width - 20.toDouble()
        return box
    }

    private fun buildFormatSettingsBox(formatBox: ComboBox<String>): FormatSettingsBox {
        val selectedFormat = formatBox.selectionModel.selectedItem
        return FormatSettingsBox(CSVFormat.valueOf(selectedFormat), properties.exportSpacing.toInt())
    }

    internal class FormatSettingsBox(private val format: CSVFormat, spacing: Int) : HBox() {
        private val delimiter: TextField = TextField(format.delimiter.toString())
        private val quoteCharacter: TextField = TextField(format.quoteCharacter.toString())
        private val quoteModeComboBox: ComboBox<QuoteMode> = ComboBox(observableList(QuoteMode.values().toList()))
        private val commentStart: TextField
        private val escapeCharacter: TextField
        private val ignoreSurroundingSpaces: CheckBox
        private val allowMissingColumnNames: CheckBox
        private val ignoreEmptyLines: CheckBox
        private val recordSeparator: TextField
        private val nullString: TextField
        private val headerComments: TextField
        private val headers: TextField
        private val skipHeaderRecord: CheckBox
        private val ignoreHeaderCase: CheckBox
        private val trailingDelimiter: CheckBox
        private val trim: CheckBox
        fun build(): CSVFormat {
            return format
        }

        private fun createColumn(spacing: Int): LabeledComponentColumn {
            val column = LabeledComponentColumn(spacing.toDouble())
            column.prefWidthProperty().bind(widthProperty().multiply(0.4))
            column.alignment = Pos.CENTER
            return column
        }

        private fun encloseRecordSeparator(recordSeparator: String): String {
            when (recordSeparator) {
                StringUtils.LF -> return "\\n"
                StringUtils.CR -> return "\\r"
                "\r\n" -> return "\\r\\n"
            }
            return recordSeparator
        }

        init {
            quoteModeComboBox.selectionModel.select(format.quoteMode)
            commentStart = TextField(format.commentMarker.toString())
            escapeCharacter = TextField(format.escapeCharacter.toString())
            ignoreSurroundingSpaces = CheckBox()
            ignoreSurroundingSpaces.selectedProperty().value = format.ignoreSurroundingSpaces
            allowMissingColumnNames = CheckBox()
            allowMissingColumnNames.selectedProperty().value = format.allowMissingColumnNames
            ignoreEmptyLines = CheckBox()
            ignoreEmptyLines.selectedProperty().value = format.ignoreEmptyLines
            recordSeparator = TextField(encloseRecordSeparator(format.recordSeparator))
            nullString = TextField(format.nullString)
            headerComments = TextField(Arrays.toString(format.headerComments))
            headers = TextField(Arrays.toString(format.header))
            skipHeaderRecord = CheckBox()
            skipHeaderRecord.selectedProperty().value = format.skipHeaderRecord
            ignoreHeaderCase = CheckBox()
            ignoreHeaderCase.selectedProperty().value = format.ignoreHeaderCase
            trailingDelimiter = CheckBox()
            trailingDelimiter.selectedProperty().value = format.trailingDelimiter
            trim = CheckBox()
            trim.selectedProperty().value = format.trailingDelimiter
            setWidth(delimiter, 50.0)
            setWidth(quoteCharacter, 50.0)
            setWidth(commentStart, 50.0)
            setWidth(escapeCharacter, 50.0)
            setWidth(recordSeparator, 50.0)
            setWidth(nullString, 50.0)
            setWidth(headerComments, 100.0)
            setWidth(headers, 100.0)
            val firstColumn = createColumn(spacing)
            firstColumn.addLabeledElement("Delimiter", delimiter)
            firstColumn.addLabeledElement("Quote Character", quoteCharacter)
            firstColumn.addLabeledElement("Quote Mode", quoteModeComboBox)
            firstColumn.addLabeledElement("Comment Marker", commentStart)
            firstColumn.addLabeledElement("Escape Character", escapeCharacter)
            firstColumn.addLabeledElement("Ignore Surrounding Spaces", ignoreSurroundingSpaces)
            firstColumn.addLabeledElement("Allow Missing Column Names", allowMissingColumnNames)
            firstColumn.addLabeledElement("Ignore Empty Lines", ignoreEmptyLines)
            val secondColumn = createColumn(spacing)
            secondColumn.addLabeledElement("Record Separator", recordSeparator)
            secondColumn.addLabeledElement("Null String", nullString)
            secondColumn.addLabeledElement("Header Comments", headerComments)
            secondColumn.addLabeledElement("Headers", headers)
            secondColumn.addLabeledElement("Skip Header Record", skipHeaderRecord)
            secondColumn.addLabeledElement("Ignore Header Case", ignoreHeaderCase)
            secondColumn.addLabeledElement("Trailing delimiter", trailingDelimiter)
            secondColumn.addLabeledElement("Trim", trim)
            setMargin(firstColumn, Insets(0.0, 20.0, 0.0, 20.0))
            setMargin(secondColumn, Insets(0.0, 20.0, 0.0, 20.0))
            children.addAll(firstColumn, secondColumn)
        }
    }

    companion object {
        private val CSV_FORMATS = Arrays.stream(Predefined.values())
                .map { obj: Predefined -> obj.toString() }
                .collect(Collectors.toList())
    }

    init {
        title = localeService.getMessage("ui.editor.export.title")
        icons.add(Image("cassandra_ico.png"))
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        scene = buildScene()
        minWidth = properties.exportWidth
        minHeight = properties.exportHeight
    }
}