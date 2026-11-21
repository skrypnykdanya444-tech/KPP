package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private final HttpTransportClient api = new HttpTransportClient();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final TableView<Vehicle> vehicleTable = new TableView<>();
    private final TextField vCodeField = new TextField();
    private final TextField vNameField = new TextField();
    private final TextField vCategoryField = new TextField();
    private final ProgressBar importProgress = new ProgressBar();
    private final Label importStatus = new Label("Модуль імпорту не активований");
    private Long selectedVehicleId = null;

    private final TableView<Route> routeTable = new TableView<>();
    private final TextField rVehicleIdField = new TextField();
    private final TextField rNumberField = new TextField();
    private final TextField rStopField = new TextField();
    private final Spinner<Double> rFlowSpinner = new Spinner<>();
    private final Spinner<Double> rF2Spinner = new Spinner<>();
    private Long selectedRouteId = null;

    private final TableView<Incident> incidentTable = new TableView<>();
    private final TextField iTypeField = new TextField();
    private final Spinner<Double> iDurationSpinner = new Spinner<>();
    private final ComboBox<IncidentState> iStateCombo = new ComboBox<>();
    private IncidentState comboCurrentState;
    private Long selectedIncidentId = null;

    private final TableView<ResponseTask> taskTable = new TableView<>();
    private final TextArea tDescriptionArea = new TextArea();
    private final TextField tIncidentIdField = new TextField();
    private final TextField tPlannedDateField = new TextField();
    private Long selectedTaskId = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("⚙ Адміністративний центр транспорту");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                createVehicleTab(),
                createRouteTab(),
                createIncidentTab(),
                createTaskTab()
        );

        Scene scene = new Scene(tabPane, 1350, 860);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshAll();
    }

    private Tab createVehicleTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setFillWidth(true);

        Label title = new Label("Каталог транспортних засобів");
        title.setPrefWidth(Double.MAX_VALUE);

        TableColumn<Vehicle, Long> idCol = new TableColumn<>("№");
        idCol.setPrefWidth(40);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Vehicle, String> codeCol = new TableColumn<>("Код");
        codeCol.setPrefWidth(90);
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        TableColumn<Vehicle, String> nameCol = new TableColumn<>("Модель");
        nameCol.setPrefWidth(230);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Vehicle, String> categoryCol = new TableColumn<>("Група");
        categoryCol.setPrefWidth(150);
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        vehicleTable.getColumns().addAll(idCol, codeCol, nameCol, categoryCol);
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedVehicleId = selected.getId();
                vCodeField.setText(selected.getCode());
                vNameField.setText(selected.getName());
                vCategoryField.setText(selected.getCategory());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Код транспортного засобу:"), 0, 0);
        form.add(vCodeField, 1, 0);
        form.add(new Label("Назва моделі:"), 0, 1);
        form.add(vNameField, 1, 1);
        form.add(new Label("Категорія:"), 0, 2);
        form.add(vCategoryField, 1, 2);

        Button btnAdd = new Button("Новий запис");
        Button btnUpdate = new Button("Зберегти зміни");
        Button btnDelete = new Button("Вилучити");
        Button btnRefresh = new Button("Обновити список");
        Button btnImport = new Button("Завантажити CSV реєстр");

        btnAdd.setOnAction(e -> handleAddVehicle());
        btnUpdate.setOnAction(e -> handleUpdateVehicle());
        btnDelete.setOnAction(e -> handleDeleteVehicle());
        btnRefresh.setOnAction(e -> refreshVehicles());
        btnImport.setOnAction(e -> handleImportVehicles());

        HBox buttons = new HBox(10, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        importProgress.setProgress(0);
        importProgress.setPrefWidth(400);
        importProgress.setVisible(false);
        importStatus.setWrapText(true);

        root.getChildren().addAll(title, new Separator(), vehicleTable,
                new Separator(), buttons, new Separator(),
                new Label("Картка запису:"), form, new Separator(), btnImport,
                importProgress, importStatus);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        Tab tab = new Tab("Автопарк");
        tab.setContent(scroll);
        return tab;
    }

    private void handleImportVehicles() {
        ImportVehiclesTask task = new ImportVehiclesTask(api);

        importProgress.setProgress(0);
        importProgress.setVisible(true);
        importProgress.setProgress(Double.NaN);
        importStatus.setText("Завантаження у процесі...");

        task.progressProperty().addListener((obs, old, val) -> {
            if (Double.isNaN(val.doubleValue())) {
                importProgress.setProgress(Double.NaN);
            } else {
                importProgress.setProgress(val.doubleValue());
            }
        });
        task.messageProperty().addListener((obs, old, val) -> {
            if (val != null) {
                importStatus.setText(val);
            }
        });

        task.setOnSucceeded(e -> {
            importProgress.setVisible(false);
            importProgress.setProgress(1.0);
            importStatus.setText("Імпорт завершено: " + task.getValue() + " записів");
            refreshVehicles();
        });

        task.setOnFailed(e -> {
            importProgress.setVisible(false);
            Throwable cause = task.getException();
            importStatus.setText("Помилка завантаження: "
                    + (cause != null ? cause.getMessage() : "невідома помилка"));
            showError("Помилка імпорту CSV",
                    cause != null ? cause.getMessage() : "невідома помилка");
        });

        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    private void handleAddVehicle() {
        try {
            Vehicle v = new Vehicle(null, vCodeField.getText(), vNameField.getText(), vCategoryField.getText());
            api.addVehicle(v);
            clearVehicleForm();
            refreshVehicles();
        } catch (Exception ex) {
            showError("Помилка створення", ex.getMessage());
        }
    }

    private void handleUpdateVehicle() {
        if (selectedVehicleId == null) {
            showError("Виберіть запис", "Оберіть засіб зі списку.");
            return;
        }
        try {
            Vehicle v = new Vehicle(selectedVehicleId, vCodeField.getText(), vNameField.getText(), vCategoryField.getText());
            api.updateVehicle(v);
            selectedVehicleId = null;
            clearVehicleForm();
            refreshVehicles();
        } catch (Exception ex) {
            showError("Помилка збереження", ex.getMessage());
        }
    }

    private void handleDeleteVehicle() {
        if (selectedVehicleId == null) {
            showError("Виберіть запис", "Оберіть засіб зі списку для вилучення.");
            return;
        }
        try {
            api.deleteVehicle(selectedVehicleId);
            selectedVehicleId = null;
            clearVehicleForm();
            refreshVehicles();
        } catch (Exception ex) {
            showError("Помилка вилучення", ex.getMessage());
        }
    }

    private void clearVehicleForm() {
        vCodeField.clear();
        vNameField.clear();
        vCategoryField.clear();
        vehicleTable.getSelectionModel().clearSelection();
        selectedVehicleId = null;
    }

    private void refreshVehicles() {
        try {
            List<Vehicle> list = api.getVehicles();
            vehicleTable.getSelectionModel().clearSelection();
            vehicleTable.getItems().setAll(list);
        } catch (Exception ex) {
            showError("Помилка завантаження", ex.getMessage());
        }
    }

    private Tab createRouteTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setFillWidth(true);

        Label title = new Label("Маршрути транспортних засобів");
        title.setPrefWidth(Double.MAX_VALUE);

        TableColumn<Route, Long> idCol = new TableColumn<>("№");
        idCol.setPrefWidth(40);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Route, Long> vehicleCol = new TableColumn<>("ТЗ код");
        vehicleCol.setPrefWidth(80);
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        TableColumn<Route, String> numberCol = new TableColumn<>("Номер");
        numberCol.setPrefWidth(90);
        numberCol.setCellValueFactory(new PropertyValueFactory<>("routeNumber"));
        TableColumn<Route, String> stopCol = new TableColumn<>("Зупинка");
        stopCol.setPrefWidth(260);
        stopCol.setCellValueFactory(new PropertyValueFactory<>("stopName"));
        TableColumn<Route, Double> flowCol = new TableColumn<>("Пасажири");
        flowCol.setPrefWidth(90);
        flowCol.setCellValueFactory(new PropertyValueFactory<>("passengerFlow"));
        TableColumn<Route, Double> f2Col = new TableColumn<>("F2");
        f2Col.setPrefWidth(60);
        f2Col.setCellValueFactory(new PropertyValueFactory<>("f2"));

        routeTable.getColumns().addAll(idCol, vehicleCol, numberCol, stopCol, flowCol, f2Col);
        routeTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedRouteId = selected.getId();
                rVehicleIdField.setText(String.valueOf(selected.getVehicleId()));
                rNumberField.setText(selected.getRouteNumber());
                rStopField.setText(selected.getStopName());
                ((DoubleSpinnerValueFactory) rFlowSpinner.getValueFactory()).setValue(selected.getPassengerFlow());
                ((DoubleSpinnerValueFactory) rF2Spinner.getValueFactory()).setValue(selected.getF2());
            }
        });

        DoubleSpinnerValueFactory rFlowFactory = new DoubleSpinnerValueFactory(0.0, 9999.0);
        rFlowFactory.setValue(0.0);
        rFlowSpinner.setValueFactory(rFlowFactory);
        rFlowSpinner.setEditable(true);

        DoubleSpinnerValueFactory rF2Factory = new DoubleSpinnerValueFactory(0.0, 100.0);
        rF2Factory.setValue(0.0);
        rF2Spinner.setValueFactory(rF2Factory);
        rF2Spinner.setEditable(true);

        VBox left = new VBox(8, routeTable);
        left.setFillWidth(true);
        VBox.setVgrow(routeTable, Priority.ALWAYS);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.add(new Label("ТЗ ID:"), 0, 0);
        form.add(rVehicleIdField, 1, 0);
        form.add(new Label("Номер маршруту:"), 0, 1);
        form.add(rNumberField, 1, 1);
        form.add(new Label("Зупинка:"), 0, 2);
        form.add(rStopField, 1, 2);
        form.add(new Label("Пасажиропотік:"), 0, 3);
        form.add(rFlowSpinner, 1, 3);
        form.add(new Label("F2:"), 0, 4);
        form.add(rF2Spinner, 1, 4);

        Button btnAdd = new Button("Додати маршрут");
        Button btnUpdate = new Button("Зберегти зміни");
        Button btnDelete = new Button("Вилучити");
        Button btnRefresh = new Button("Обновити");
        btnAdd.setOnAction(e -> handleAddRoute());
        btnUpdate.setOnAction(e -> handleUpdateRoute());
        btnDelete.setOnAction(e -> handleDeleteRoute());
        btnRefresh.setOnAction(e -> refreshRoutes());

        HBox buttons = new HBox(8, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, new Separator(), routeTable,
                new Separator(), buttons, new Separator(),
                new Label("Картка маршруту:"), form);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Tab tab = new Tab("Маршрути");
        tab.setContent(scroll);
        return tab;
    }

    private void handleAddRoute() {
        try {
            Route r = new Route(null, Long.parseLong(rVehicleIdField.getText()), rNumberField.getText(), rStopField.getText(),
                    rFlowSpinner.getValue(), rF2Spinner.getValue());
            api.addRoute(r);
            clearRouteForm();
            refreshRoutes();
        } catch (Exception ex) {
            showError("Помилка створення маршруту", ex.getMessage());
        }
    }

    private void handleUpdateRoute() {
        if (selectedRouteId == null) {
            showError("Виберіть запис", "Оберіть маршрут.");
            return;
        }
        try {
            Route r = new Route(selectedRouteId, Long.parseLong(rVehicleIdField.getText()), rNumberField.getText(), rStopField.getText(),
                    rFlowSpinner.getValue(), rF2Spinner.getValue());
            api.updateRoute(r);
            selectedRouteId = null;
            clearRouteForm();
            refreshRoutes();
        } catch (Exception ex) {
            showError("Помилка збереження маршруту", ex.getMessage());
        }
    }

    private void handleDeleteRoute() {
        if (selectedRouteId == null) {
            showError("Виберіть запис", "Оберіть маршрут.");
            return;
        }
        try {
            api.deleteRoute(selectedRouteId);
            selectedRouteId = null;
            clearRouteForm();
            refreshRoutes();
        } catch (Exception ex) {
            showError("Помилка вилучення маршруту", ex.getMessage());
        }
    }

    private void clearRouteForm() {
        rVehicleIdField.clear();
        rNumberField.clear();
        rStopField.clear();
        ((DoubleSpinnerValueFactory) rFlowSpinner.getValueFactory()).setValue(0.0);
        ((DoubleSpinnerValueFactory) rF2Spinner.getValueFactory()).setValue(0.0);
        routeTable.getSelectionModel().clearSelection();
        selectedRouteId = null;
    }

    private void refreshRoutes() {
        try {
            routeTable.getSelectionModel().clearSelection();
            routeTable.getItems().setAll(api.getRoutes());
        } catch (Exception ex) {
            showError("Помилка завантаження маршрутів", ex.getMessage());
        }
    }

    private Tab createIncidentTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setFillWidth(true);

        Label title = new Label("Транспортні інциденти");
        title.setPrefWidth(Double.MAX_VALUE);

        TableColumn<Incident, Long> idCol = new TableColumn<>("№");
        idCol.setPrefWidth(35);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Incident, Long> routeCol = new TableColumn<>("Маршрут");
        routeCol.setPrefWidth(60);
        routeCol.setCellValueFactory(new PropertyValueFactory<>("routeId"));
        TableColumn<Incident, String> typeCol = new TableColumn<>("Тип");
        typeCol.setPrefWidth(140);
        typeCol.setCellValueFactory(new PropertyValueFactory<>("incidentType"));
        TableColumn<Incident, String> stateCol = new TableColumn<>("Статус");
        stateCol.setPrefWidth(80);
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        TableColumn<Incident, Double> durationCol = new TableColumn<>("Порушення (хв)");
        durationCol.setPrefWidth(100);
        durationCol.setCellValueFactory(new PropertyValueFactory<>("disruptionDuration"));
        TableColumn<Incident, Double> f1Col = new TableColumn<>("F1");
        f1Col.setPrefWidth(50);
        f1Col.setCellValueFactory(new PropertyValueFactory<>("f1"));
        TableColumn<Incident, Double> f2Col = new TableColumn<>("F2");
        f2Col.setPrefWidth(50);
        f2Col.setCellValueFactory(new PropertyValueFactory<>("f2"));
        TableColumn<Incident, Double> f3Col = new TableColumn<>("F3");
        f3Col.setPrefWidth(50);
        f3Col.setCellValueFactory(new PropertyValueFactory<>("f3"));
        TableColumn<Incident, Double> indexCol = new TableColumn<>("Індекс I");
        indexCol.setPrefWidth(70);
        indexCol.setCellValueFactory(new PropertyValueFactory<>("severityIndex"));

        incidentTable.getColumns().addAll(idCol, routeCol, typeCol, stateCol, durationCol, f1Col, f2Col, f3Col, indexCol);
        incidentTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIncidentId = selected.getId();
                iTypeField.setText(selected.getIncidentType());
                ((DoubleSpinnerValueFactory) iDurationSpinner.getValueFactory()).setValue(selected.getDisruptionDuration());
                updateStateCombo(selected.getState());
                iStateCombo.setValue(selected.getState());
            }
        });

        iStateCombo.setPrefWidth(320);
        iStateCombo.setCellFactory(combo -> new javafx.scene.control.ListCell<IncidentState>() {
            @Override
            protected void updateItem(IncidentState item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item.getTitle());
                    IncidentState cur = comboCurrentState;
                    if (cur == null) {
                        setDisable(true);
                    } else {
                        IncidentState nxt = nextState(cur);
                        setDisable(item != cur && item != nxt);
                    }
                }
            }
        });
        iStateCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null && n != IncidentState.CLOSED) {
                Incident selected = incidentTable.getSelectionModel().getSelectedItem();
                if (selected != null && n != selected.getState()) {
                    IncidentState current = selected.getState() == null ? IncidentState.NEW : selected.getState();
                    IncidentState next = nextState(current);
                    if (n != current && n != next) {
                        iStateCombo.getSelectionModel().select(current);
                        return;
                    }
                    try {
                        api.changeIncidentState(selectedIncidentId, n);
                        refreshIncidents();
                    } catch (Exception ex) {
                        showError("Помилка переходу", ex.getMessage());
                    }
                }
            }
        });

        VBox left = new VBox(8, incidentTable);
        left.setFillWidth(true);
        VBox.setVgrow(incidentTable, Priority.ALWAYS);

        DoubleSpinnerValueFactory iDurationFactory = new DoubleSpinnerValueFactory(0.0, 1000.0);
        iDurationFactory.setValue(0.0);
        iDurationSpinner.setValueFactory(iDurationFactory);
        iDurationSpinner.setEditable(true);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.add(new Label("Тип інциденту:"), 0, 0);
        form.add(iTypeField, 1, 0);
        form.add(new Label("Тривалість (хв):"), 0, 1);
        form.add(iDurationSpinner, 1, 1);
        form.add(new Label("Статус:"), 0, 2);
        form.add(new Label("(перемикачі нижче)"), 1, 2);

        Button btnAdd = new Button("Створити інцидент");
        Button btnUpdate = new Button("Зберегти зміни");
        Button btnDelete = new Button("Вилучити");
        Button btnRefresh = new Button("Обновити");
        btnAdd.setOnAction(e -> handleAddIncident());
        btnUpdate.setOnAction(e -> handleUpdateIncident());
        btnDelete.setOnAction(e -> handleDeleteIncident());
        btnRefresh.setOnAction(e -> refreshIncidents());

        HBox buttons = new HBox(8, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, new Separator(), incidentTable,
                new Separator(), buttons, new Separator(),
                new Label("Картка інциденту:"), form,
                new Separator(), new Label("Статус життєвого циклу:"), iStateCombo);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Tab tab = new Tab("Інциденти");
        tab.setContent(scroll);
        return tab;
    }

    private void updateStateCombo(IncidentState current) {
        this.comboCurrentState = current;
        iStateCombo.getItems().clear();
        iStateCombo.getItems().addAll(IncidentState.values());
        if (current != null && !current.isFinal()) {
            iStateCombo.setValue(current);
        } else {
            iStateCombo.setValue(current);
        }
        iStateCombo.setDisable(current != null && current.isFinal());
    }

    private void handleAddIncident() {
        try {
            Incident i = new Incident(null, getRouteId(), iTypeField.getText(),
                    IncidentState.NEW, iDurationSpinner.getValue(), 0, 0, 0, 0);
            api.addIncident(i);
            clearIncidentForm();
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка створення інциденту", ex.getMessage());
        }
    }

    private long getRouteId() {
        for (Route r : routeTable.getItems()) {
            return r.getId();
        }
        return 1;
    }

    private void handleUpdateIncident() {
        if (selectedIncidentId == null) {
            showError("Виберіть запис", "Оберіть інцидент.");
            return;
        }
        try {
            Incident i = new Incident(selectedIncidentId, getRouteId(), iTypeField.getText(),
                    IncidentState.NEW, iDurationSpinner.getValue(), 0, 0, 0, 0);
            api.updateIncident(i);
            selectedIncidentId = null;
            clearIncidentForm();
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка збереження інциденту", ex.getMessage());
        }
    }

    private void handleDeleteIncident() {
        if (selectedIncidentId == null) {
            showError("Виберіть запис", "Оберіть інцидент.");
            return;
        }
        try {
            api.deleteIncident(selectedIncidentId);
            selectedIncidentId = null;
            clearIncidentForm();
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка вилучення інциденту", ex.getMessage());
        }
    }

    private void clearIncidentForm() {
        iTypeField.clear();
        ((DoubleSpinnerValueFactory) iDurationSpinner.getValueFactory()).setValue(0.0);
        incidentTable.getSelectionModel().clearSelection();
        selectedIncidentId = null;
        updateStateCombo(null);
    }

    private void refreshIncidents() {
        try {
            List<Incident> list = api.getIncidents();
            incidentTable.getSelectionModel().clearSelection();
            incidentTable.getItems().setAll(list);
        } catch (Exception ex) {
            showError("Помилка завантаження інцидентів", ex.getMessage());
        }
    }

    private static IncidentState nextState(IncidentState current) {
        return switch (current) {
            case NEW -> IncidentState.CONFIRMED;
            case CONFIRMED -> IncidentState.RESPONSE;
            case RESPONSE -> IncidentState.CLOSED;
            case CLOSED -> null;
        };
    }

    private Tab createTaskTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setFillWidth(true);

        Label title = new Label("Оперативні завдання");
        title.setPrefWidth(Double.MAX_VALUE);

        TableColumn<ResponseTask, Long> idCol = new TableColumn<>("№");
        idCol.setPrefWidth(35);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<ResponseTask, Long> incidentCol = new TableColumn<>("Інцидент ID");
        incidentCol.setPrefWidth(80);
        incidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        TableColumn<ResponseTask, String> descCol = new TableColumn<>("Опис завдання");
        descCol.setPrefWidth(400);
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<ResponseTask, String> dateCol = new TableColumn<>("Планова дата");
        dateCol.setPrefWidth(180);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("plannedDate"));

        taskTable.getColumns().addAll(idCol, incidentCol, descCol, dateCol);
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedTaskId = selected.getId();
                tIncidentIdField.setText(String.valueOf(selected.getIncidentId()));
                tDescriptionArea.setText(selected.getDescription());
                tPlannedDateField.setText(selected.getPlannedDate() == null ? "" : selected.getPlannedDate().toString());
            }
        });

        tDescriptionArea.setPrefRowCount(5);
        tDescriptionArea.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.add(new Label("Інцидент ID:"), 0, 0);
        form.add(tIncidentIdField, 1, 0);
        form.add(new Label("Опис:"), 0, 1);
        form.add(tDescriptionArea, 1, 1, 1, 3);
        form.add(new Label("Планова дата (ДД-ММ-РРРР ГГ:ХХ):"), 0, 4);
        form.add(tPlannedDateField, 1, 4);

        Button btnAdd = new Button("Створити завдання");
        Button btnUpdate = new Button("Зберегти зміни");
        Button btnDelete = new Button("Вилучити");
        Button btnRefresh = new Button("Обновити");
        btnAdd.setOnAction(e -> handleAddTask());
        btnUpdate.setOnAction(e -> handleUpdateTask());
        btnDelete.setOnAction(e -> handleDeleteTask());
        btnRefresh.setOnAction(e -> refreshTasks());

        HBox buttons = new HBox(8, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, new Separator(), taskTable,
                new Separator(), buttons, new Separator(),
                new Label("Картка завдання:"), form);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Tab tab = new Tab("Завдання реагування");
        tab.setContent(scroll);
        return tab;
    }

    private void handleAddTask() {
        try {
            ResponseTask t = new ResponseTask(null, Long.parseLong(tIncidentIdField.getText()),
                    tDescriptionArea.getText(), java.time.LocalDateTime.parse(tPlannedDateField.getText().trim()));
            api.addResponseTask(t);
            clearTaskForm();
            refreshTasks();
        } catch (Exception ex) {
            showError("Помилка створення завдання", ex.getMessage());
        }
    }

    private void handleUpdateTask() {
        if (selectedTaskId == null) {
            showError("Виберіть запис", "Оберіть завдання.");
            return;
        }
        try {
            ResponseTask t = new ResponseTask(selectedTaskId, Long.parseLong(tIncidentIdField.getText()),
                    tDescriptionArea.getText(), java.time.LocalDateTime.parse(tPlannedDateField.getText().trim()));
            api.updateResponseTask(t);
            selectedTaskId = null;
            clearTaskForm();
            refreshTasks();
        } catch (Exception ex) {
            showError("Помилка збереження завдання", ex.getMessage());
        }
    }

    private void handleDeleteTask() {
        if (selectedTaskId == null) {
            showError("Виберіть запис", "Оберіть завдання.");
            return;
        }
        try {
            api.deleteResponseTask(selectedTaskId);
            selectedTaskId = null;
            clearTaskForm();
            refreshTasks();
        } catch (Exception ex) {
            showError("Помилка вилучення завдання", ex.getMessage());
        }
    }

    private void clearTaskForm() {
        tIncidentIdField.clear();
        tDescriptionArea.clear();
        tPlannedDateField.clear();
        taskTable.getSelectionModel().clearSelection();
        selectedTaskId = null;
    }

    private void refreshTasks() {
        try {
            taskTable.getSelectionModel().clearSelection();
            taskTable.getItems().setAll(api.getResponseTasks());
        } catch (Exception ex) {
            showError("Помилка завантаження завдань", ex.getMessage());
        }
    }

    private void refreshAll() {
        refreshVehicles();
        refreshRoutes();
        refreshIncidents();
        refreshTasks();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
