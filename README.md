# Cassandra GUI client

Simple client to show table data and DDL and update it (hopefully will be added soon). All columns are threaten as 
strings:
```
        tableMetadata.getColumns().forEach(columnMetadata -> {
            TableColumn<Row, String> column = new TableColumn<>();
            Label columnLabel = new Label(columnMetadata.getName());
            columnLabel.setTooltip(new Tooltip(columnMetadata.getType().asFunctionParameterString()));
            column.setCellValueFactory(param -> {
                String value = Objects.toString(param.getValue().getObject(columnMetadata.getName()), "null");
                return new SimpleStringProperty(value);
            });
            column.setGraphic(columnLabel);
            dataTbl.getColumns().add(column);
        });
```

What it already can:
* connect to cassandra
* load and show tables
* show table DDL
* show table data (simple table view with header)
* apply simple filter to loaded data
* execute query

Planned:
* composite filters
* respect field type in filtering
* editable values
* add/delete tables
* add/save connections
* packaging

### GUI

#### Main window
![main window](https://raw.githubusercontent.com/Kindrat/cassandra-client/master/doc/window.png)

#### Connect
![connect popup](https://raw.githubusercontent.com/Kindrat/cassandra-client/master/doc/connect_popup.png)

#### List tables
![list tables](https://raw.githubusercontent.com/Kindrat/cassandra-client/master/doc/list_tables.png)

#### Show DDL
![show DDL](https://raw.githubusercontent.com/Kindrat/cassandra-client/master/doc/show_ddl.png)

#### Show data
![show DDL](https://raw.githubusercontent.com/Kindrat/cassandra-client/master/doc/show_data.png)
