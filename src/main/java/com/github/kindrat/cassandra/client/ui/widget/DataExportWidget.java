package com.github.kindrat.cassandra.client.ui.widget;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.model.CsvTargetMetadata;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.fx.component.LabeledComponentColumn;
import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.kindrat.cassandra.client.ui.listener.CheckBoxListener.create;
import static com.github.kindrat.cassandra.client.util.UIUtil.fillParent;

@Slf4j
public class DataExportWidget extends Stage {
    private static final List<String> CSV_FORMATS = Arrays.stream(CSVFormat.Predefined.values())
            .map(Object::toString)
            .collect(Collectors.toList());

    private final MonoProcessor<CsvTargetMetadata> metadataProcessor = MonoProcessor.create();
    private final String table;
    private final UIProperties properties;

    public DataExportWidget(Stage parent, String table, MessageByLocaleService localeService, UIProperties properties) {
        this.table = table;
        this.properties = properties;
        setTitle(localeService.getMessage("ui.editor.export.title"));
        getIcons().add(new Image("cassandra_ico.png"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setScene(buildScene());
        setMinWidth(properties.getExportWidth());
        setMinHeight(properties.getExportHeight());
    }

    public Mono<CsvTargetMetadata> getMetadata() {
        show();
        return metadataProcessor;
    }

    @SneakyThrows
    private Scene buildScene() {
        AnchorPane container = new AnchorPane();
        VBox editor = new VBox();
        editor.setSpacing(properties.getExportSpacing());
        editor.setAlignment(Pos.TOP_CENTER);
        container.getChildren().add(editor);
        fillParent(editor);

        TextField pathField = new TextField();
        URL location = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        File defaultDir = new File(location.toURI()).getParentFile();
        pathField.setText(defaultDir + "/" + table + ".csv");
        Button startButton = new Button("Start export");

        HBox settingsContainer = new HBox();
        settingsContainer.prefWidthProperty().bind(editor.widthProperty());

        Label formatLabel = new Label("CSV format");
        ComboBox<String> formats = buildFormatBox(200);

        CheckBox customizeCheckbox = new CheckBox("override format settings");
        customizeCheckbox.selectedProperty().addListener(create(
                () -> {
                    FormatSettingsBox formatSettingsBox = buildFormatSettingsBox(formats);
                    formatSettingsBox.prefWidthProperty().bind(settingsContainer.widthProperty());
                    settingsContainer.getChildren().add(formatSettingsBox);
                    startButton.setOnAction(event -> {
                        if (customizeCheckbox.isSelected()) {
                            CSVFormat csvFormat = formatSettingsBox.build();
                            File target = new File(pathField.getText());
                            metadataProcessor.onNext(new CsvTargetMetadata(table, csvFormat, target));
                            hide();
                        }
                    });
                },
                () -> settingsContainer.getChildren().clear())
        );

        startButton.setOnAction(event -> {
            if (!customizeCheckbox.isSelected()) {
                FormatSettingsBox formatSettingsBox = buildFormatSettingsBox(formats);
                CSVFormat csvFormat = formatSettingsBox.build();
                File target = new File(pathField.getText());
                metadataProcessor.onNext(new CsvTargetMetadata(table, csvFormat, target));
                hide();
            }
        });

        formats.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                customizeCheckbox.setSelected(!customizeCheckbox.isSelected());
                customizeCheckbox.setSelected(!customizeCheckbox.isSelected());
            }
        });
        HBox format = new HBox(formatLabel, formats, customizeCheckbox);
        format.setAlignment(Pos.CENTER);
        format.setSpacing(properties.getExportSpacing());

        pathField.prefWidthProperty().bind(editor.widthProperty().multiply(0.6));
        Button destinationButton = new Button("Choose destination");
        HBox savePath = new HBox(pathField, destinationButton);
        savePath.setAlignment(Pos.CENTER);
        savePath.setSpacing(properties.getExportSpacing());

        destinationButton.setOnAction(event -> {
            FileChooser savePathProvider = new FileChooser();
            savePathProvider.setTitle("Save CSV");
            savePathProvider.setInitialFileName(table + ".csv");
            savePathProvider.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("CSV", "csv"));
            File file = savePathProvider.showSaveDialog(this);
            if (file != null) {
                pathField.setText(file.getAbsolutePath());
            }
        });

        editor.getChildren().addAll(format, savePath, settingsContainer, startButton);
        return new Scene(container, properties.getExportWidth(), properties.getExportHeight());
    }

    private ComboBox<String> buildFormatBox(int width) {
        ComboBox<String> box = new ComboBox<>(FXCollections.observableList(CSV_FORMATS));
        box.getSelectionModel().select(CSVFormat.Predefined.Default.toString());
        box.setMinWidth(width - 20);
        box.setMaxWidth(width - 20);
        return box;
    }

    private FormatSettingsBox buildFormatSettingsBox(ComboBox<String> formatBox) {
        String selectedFormat = formatBox.getSelectionModel().getSelectedItem();
        return new FormatSettingsBox(CSVFormat.valueOf(selectedFormat), properties.getExportSpacing());
    }

    static class FormatSettingsBox extends HBox {
        private final CSVFormat format;
        private final TextField delimiter;
        private final TextField quoteCharacter;
        private final ComboBox<QuoteMode> quoteModeComboBox;
        private final TextField commentStart;
        private final TextField escapeCharacter;
        private final CheckBox ignoreSurroundingSpaces;
        private final CheckBox allowMissingColumnNames;
        private final CheckBox ignoreEmptyLines;
        private final TextField recordSeparator;
        private final TextField nullString;
        private final TextField headerComments;
        private final TextField headers;
        private final CheckBox skipHeaderRecord;
        private final CheckBox ignoreHeaderCase;
        private final CheckBox trailingDelimiter;
        private final CheckBox trim;

        FormatSettingsBox(CSVFormat format, Integer spacing) {
            this.format = format;
            delimiter = new TextField(String.valueOf(format.getDelimiter()));
            quoteCharacter = new TextField(String.valueOf(format.getQuoteCharacter()));
            quoteModeComboBox = new ComboBox<>(FXCollections.observableList(Arrays.asList(QuoteMode.values())));
            quoteModeComboBox.getSelectionModel().select(format.getQuoteMode());
            commentStart = new TextField(String.valueOf(format.getCommentMarker()));
            escapeCharacter = new TextField(String.valueOf(format.getEscapeCharacter()));
            ignoreSurroundingSpaces = new CheckBox();
            ignoreSurroundingSpaces.selectedProperty().setValue(format.getIgnoreSurroundingSpaces());
            allowMissingColumnNames = new CheckBox();
            allowMissingColumnNames.selectedProperty().setValue(format.getAllowMissingColumnNames());
            ignoreEmptyLines = new CheckBox();
            ignoreEmptyLines.selectedProperty().setValue(format.getIgnoreEmptyLines());
            recordSeparator = new TextField(encloseRecordSeparator(format.getRecordSeparator()));
            nullString = new TextField(format.getNullString());
            headerComments = new TextField(Arrays.toString(format.getHeaderComments()));
            headers = new TextField(Arrays.toString(format.getHeader()));
            skipHeaderRecord = new CheckBox();
            skipHeaderRecord.selectedProperty().setValue(format.getSkipHeaderRecord());
            ignoreHeaderCase = new CheckBox();
            ignoreHeaderCase.selectedProperty().setValue(format.getIgnoreHeaderCase());
            trailingDelimiter = new CheckBox();
            trailingDelimiter.selectedProperty().setValue(format.getTrailingDelimiter());
            trim = new CheckBox();
            trim.selectedProperty().setValue(format.getTrailingDelimiter());

            UIUtil.setWidth(delimiter, 50);
            UIUtil.setWidth(quoteCharacter, 50);
            UIUtil.setWidth(commentStart, 50);
            UIUtil.setWidth(escapeCharacter, 50);
            UIUtil.setWidth(recordSeparator, 50);
            UIUtil.setWidth(nullString, 50);
            UIUtil.setWidth(headerComments, 100);
            UIUtil.setWidth(headers, 100);

            LabeledComponentColumn firstColumn = createColumn(spacing);
            firstColumn.addLabeledElement("Delimiter", delimiter);
            firstColumn.addLabeledElement("Quote Character", quoteCharacter);
            firstColumn.addLabeledElement("Quote Mode", quoteModeComboBox);
            firstColumn.addLabeledElement("Comment Marker", commentStart);
            firstColumn.addLabeledElement("Escape Character", escapeCharacter);
            firstColumn.addLabeledElement("Ignore Surrounding Spaces", ignoreSurroundingSpaces);
            firstColumn.addLabeledElement("Allow Missing Column Names", allowMissingColumnNames);
            firstColumn.addLabeledElement("Ignore Empty Lines", ignoreEmptyLines);

            LabeledComponentColumn secondColumn = createColumn(spacing);
            secondColumn.addLabeledElement("Record Separator", recordSeparator);
            secondColumn.addLabeledElement("Null String", nullString);
            secondColumn.addLabeledElement("Header Comments", headerComments);
            secondColumn.addLabeledElement("Headers", headers);
            secondColumn.addLabeledElement("Skip Header Record", skipHeaderRecord);
            secondColumn.addLabeledElement("Ignore Header Case", ignoreHeaderCase);
            secondColumn.addLabeledElement("Trailing delimiter", trailingDelimiter);
            secondColumn.addLabeledElement("Trim", trim);

            HBox.setMargin(firstColumn, new Insets(0, 20, 0, 20));
            HBox.setMargin(secondColumn, new Insets(0, 20, 0, 20));
            getChildren().addAll(firstColumn, secondColumn);
        }

        CSVFormat build() {
            return format;
        }

        private LabeledComponentColumn createColumn(Integer spacing) {
            LabeledComponentColumn column = new LabeledComponentColumn(spacing);
            column.prefWidthProperty().bind(widthProperty().multiply(0.4));
            column.setAlignment(Pos.CENTER);
            return column;
        }

        private String encloseRecordSeparator(String recordSeparator) {
            switch (recordSeparator) {
                case StringUtils.LF:
                    return "\\n";
                case StringUtils.CR:
                    return "\\r";
                case "\r\n":
                    return "\\r\\n";
            }
            return recordSeparator;
        }
    }
}
