package com.github.kindrat.cassandra.client.model

import org.apache.commons.csv.CSVFormat
import java.io.File

data class CsvTargetMetadata(val table: String, val format: CSVFormat, val target: File)