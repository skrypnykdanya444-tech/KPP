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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Графічний клієнтський застосунок.
 *
 * <p>Клієнт працює через HTTP-маршрути сервера: Servlet -> Service -> DAO -> MySQL.
 * Прямого доступу клієнта до бази даних немає.</p>
 */
public class MainApp extends Application {

    private final HttpTransportClient api = new HttpTransportClient();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ========== Транспортні засоби ==========
    private final TableView<Vehicle> vehicleTable = new TableView<>();
    private final TextField vCodeField = new TextField();
    private final TextField vNameField = new TextField();
    private final TextField vCategoryField = new TextField();
    private final ProgressBar importProgress = new ProgressBar(0);
    private final Label importStatus = new Label("Імпорт не запущено");
    private Long selectedVehicleId = null;

    // ========== Маршрути ==========
    private final TableView<Route> routeTable = new TableView<>();
    private final TextField rVehicleIdField = new TextField();
    private final TextField rNumberField = new TextField();
    private final TextField rStopField = new TextField();
    private final TextField rFlowField = new TextField();
    private final TextField rImportanceField = new TextField();
    private Long selectedRouteId = null;

    // ========== Інциденти ==========
    private final TableView<Incident> incidentTable = new TableView<>();
    private final TextField iRouteIdField = new TextField();
    private final TextField iTypeField = new TextField();
    private final TextField iDurationField = new TextField();
    private final ComboBox<IncidentState> iStateCombo = new ComboBox<>();
    private Long selectedIncidentId = null;

    // ========== Завдання реагування ==========
    private final TableView<ResponseTask> taskTable = new TableView<>();
    private final TextField tIncidentIdField = new TextField();
    private final TextField tDescriptionField = new TextField();
    private final TextField tPlannedDateField = new TextField();
    private Long selectedTaskId = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Курсова робота — Транспортні засоби та дорожні інциденти");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                createVehicleTab(),
                createRouteTab(),
                createIncidentTab(),
                createTaskTab()
        );

        Scene scene = new Scene(tabPane, 1150, 720);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshAll();
    }

    // =========================================================
    // Вкладка «Транспортні засоби» + фоновий CSV-імпорт
    // =========================================================
    private Tab createVehicleTab() {
        Tab tab = new Tab("Транспортні засоби");

        TableColumn<Vehicle, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Vehicle, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        TableColumn<Vehicle, String> nameCol = new TableColumn<>("Назва");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Vehicle, String> categoryCol = new TableColumn<>("Категорія");
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
        form.add(new Label("Код:"), 0, 0);
        form.add(vCodeField, 1, 0);
        form.add(new Label("Назва:"), 0, 1);
        form.add(vNameField, 1, 1);
        form.add(new Label("Категорія:"), 0, 2);
        form.add(vCategoryField, 1, 2);

        Button btnAdd = new Button("Додати");
        Button btnUpdate = new Button("Змінити");
        Button btnDelete = new Button("Видалити");
        Button btnRefresh = new Button("Оновити");

        btnAdd.setOnAction(e -> handleAddVehicle());
        btnUpdate.setOnAction(e -> handleUpdateVehicle());
        btnDelete.setOnAction(e -> handleDeleteVehicle());
        btnRefresh.setOnAction(e -> refreshVehicles());

        HBox buttons = new HBox(10, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        // --- Фоновий CSV-імпорт реєстру транспортних засобів ---
        Button btnImport = new Button("Імпортувати CSV");
        btnImport.setOnAction(e -> handleImportVehicles());

        importProgress.setProgress(0);
        importProgress.setPrefWidth(260);
        importProgress.setVisible(false);
        importStatus.setWrapText(true);

        VBox importBox = new VBox(6, btnImport, importProgress, importStatus);
        importBox.setPadding(new Insets(10));

        VBox left = new VBox(10, vehicleTable, buttons, importBox);
        left.setPadding(new Insets(10));
        left.setFillWidth(true);
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);

        VBox right = new VBox(10, new Label("Форма транспортного засобу"), form);
        right.setPadding(new Insets(10));

        HBox content = new HBox(10, left, right);
        content.setFillHeight(true);
        HBox.setHgrow(left, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    /**
     * Запускає фоновий CSV-імпорт реєстру транспортних засобів.
     *
     * <p>Імпорт виконується у окремому потоці через JavaFX Task, тому
     * інтерфейс не блокується. Прогрес відображається у ProgressBar.</p>
     */
    private void handleImportVehicles() {
        ImportVehiclesTask task = new ImportVehiclesTask(api);

        importProgress.setProgress(0);
        importProgress.setVisible(true);
        importStatus.setText("Імпорт запущено...");

        // Прогрес і статус оновлюються слухачами: на початку messageProperty()
        // має значення null, тому пряме зв'язування Label wipes «Імпорт не запущено».
        task.progressProperty().addListener((obs, old, val) ->
                importProgress.setProgress(val.doubleValue()));
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
            importStatus.setText("Помилка імпорту: "
                    + (cause != null ? cause.getMessage() : "невідома помилка"));
            showError("Помилка імпорту CSV",
                    cause != null ? cause.getMessage() : "невідома помилка");
        });

        // Запуск у фоновому потоці
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
            showError("Помилка додавання транспортного засобу", ex.getMessage());
        }
    }

    private void handleUpdateVehicle() {
        if (selectedVehicleId == null) {
            showError("Виберіть запис", "Оберіть транспортний засіб зі списку для зміни.");
            return;
        }
        try {
            Vehicle v = new Vehicle(selectedVehicleId, vCodeField.getText(), vNameField.getText(), vCategoryField.getText());
            api.updateVehicle(v);
            selectedVehicleId = null;
            clearVehicleForm();
            refreshVehicles();
        } catch (Exception ex) {
            showError("Помилка оновлення транспортного засобу", ex.getMessage());
        }
    }

    private void handleDeleteVehicle() {
        if (selectedVehicleId == null) {
            showError("Виберіть запис", "Оберіть транспортний засіб зі списку для видалення.");
            return;
        }
        try {
            api.deleteVehicle(selectedVehicleId);
            selectedVehicleId = null;
            clearVehicleForm();
            refreshVehicles();
        } catch (Exception ex) {
            showError("Помилка видалення транспортного засобу", ex.getMessage());
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
            vehicleTable.getItems().setAll(list);
        } catch (Exception ex) {
            showError("Помилка завантаження транспортних засобів", ex.getMessage());
        }
    }

    // =========================================================
    // Вкладка «Маршрути»
    // =========================================================
    private Tab createRouteTab() {
        Tab tab = new Tab("Маршрути");

        TableColumn<Route, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Route, Long> vehicleCol = new TableColumn<>("ТЗ ID");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        TableColumn<Route, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("routeNumber"));
        TableColumn<Route, String> stopCol = new TableColumn<>("Зупинка");
        stopCol.setCellValueFactory(new PropertyValueFactory<>("stopName"));
        TableColumn<Route, Double> flowCol = new TableColumn<>("Пасажиропотік");
        flowCol.setCellValueFactory(new PropertyValueFactory<>("passengerFlow"));
        TableColumn<Route, Double> importanceCol = new TableColumn<>("Важливість");
        importanceCol.setCellValueFactory(new PropertyValueFactory<>("routeImportance"));

        routeTable.getColumns().addAll(idCol, vehicleCol, numberCol, stopCol, flowCol, importanceCol);
        routeTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedRouteId = selected.getId();
                rVehicleIdField.setText(String.valueOf(selected.getVehicleId()));
                rNumberField.setText(selected.getRouteNumber());
                rStopField.setText(selected.getStopName());
                rFlowField.setText(String.valueOf(selected.getPassengerFlow()));
                rImportanceField.setText(String.valueOf(selected.getRouteImportance()));
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));
        form.add(new Label("ТЗ ID:"), 0, 0);
        form.add(rVehicleIdField, 1, 0);
        form.add(new Label("Номер маршруту:"), 0, 1);
        form.add(rNumberField, 1, 1);
        form.add(new Label("Зупинка:"), 0, 2);
        form.add(rStopField, 1, 2);
        form.add(new Label("Пасажиропотік:"), 0, 3);
        form.add(rFlowField, 1, 3);
        form.add(new Label("Важливість (0..10):"), 0, 4);
        form.add(rImportanceField, 1, 4);

        Button btnAdd = new Button("Додати");
        Button btnUpdate = new Button("Змінити");
        Button btnDelete = new Button("Видалити");
        Button btnRefresh = new Button("Оновити");

        btnAdd.setOnAction(e -> handleAddRoute());
        btnUpdate.setOnAction(e -> handleUpdateRoute());
        btnDelete.setOnAction(e -> handleDeleteRoute());
        btnRefresh.setOnAction(e -> refreshRoutes());

        HBox buttons = new HBox(10, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(10, routeTable, buttons);
        left.setPadding(new Insets(10));
        left.setFillWidth(true);
        VBox.setVgrow(routeTable, Priority.ALWAYS);

        VBox right = new VBox(10, new Label("Форма маршруту"), form);
        right.setPadding(new Insets(10));

        HBox content = new HBox(10, left, right);
        content.setFillHeight(true);
        HBox.setHgrow(left, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private void handleAddRoute() {
        try {
            Route r = new Route(
                    null,
                    Long.parseLong(rVehicleIdField.getText()),
                    rNumberField.getText(),
                    rStopField.getText(),
                    Double.parseDouble(rFlowField.getText()),
                    Double.parseDouble(rImportanceField.getText())
            );
            api.addRoute(r);
            clearRouteForm();
            refreshRoutes();
        } catch (Exception ex) {
            showError("Помилка додавання маршруту", ex.getMessage());
        }
    }

    private void handleUpdateRoute() {
        if (selectedRouteId == null) {
            showError("Виберіть запис", "Оберіть маршрут зі списку для зміни.");
            return;
        }
        try {
            Route r = new Route(
                    selectedRouteId,
                    Long.parseLong(rVehicleIdField.getText()),
                    rNumberField.getText(),
                    rStopField.getText(),
                    Double.parseDouble(rFlowField.getText()),
                    Double.parseDouble(rImportanceField.getText())
            );
            api.updateRoute(r);
            selectedRouteId = null;
            clearRouteForm();
            refreshRoutes();
        } catch (Exception ex) {
            showError("Помилка оновлення маршруту", ex.getMessage());
        }
    }

    private void handleDeleteRoute() {
        if (selectedRouteId == null) {
            showError("Виберіть запис", "Оберіть маршрут зі списку для видалення.");
            return;
        }
        try {
            api.deleteRoute(selectedRouteId);
            selectedRouteId = null;
            clearRouteForm();
            refreshRoutes();
        } catch (Exception ex) {
            showError("Помилка видалення маршруту", ex.getMessage());
        }
    }

    private void clearRouteForm() {
        rVehicleIdField.clear();
        rNumberField.clear();
        rStopField.clear();
        rFlowField.clear();
        rImportanceField.clear();
        routeTable.getSelectionModel().clearSelection();
        selectedRouteId = null;
    }

    private void refreshRoutes() {
        try {
            List<Route> list = api.getRoutes();
            routeTable.getItems().setAll(list);
        } catch (Exception ex) {
            showError("Помилка завантаження маршрутів", ex.getMessage());
        }
    }

    // =========================================================
    // Вкладка «Інциденти» — життєвий цикл та інтегральний показник
    // =========================================================
    private Tab createIncidentTab() {
        Tab tab = new Tab("Інциденти");

        TableColumn<Incident, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Incident, Long> routeCol = new TableColumn<>("Маршрут ID");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("routeId"));
        TableColumn<Incident, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("incidentType"));
        TableColumn<Incident, IncidentState> stateCol = new TableColumn<>("Стан");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        TableColumn<Incident, Double> durationCol = new TableColumn<>("Порушення, хв");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("disruptionDuration"));
        TableColumn<Incident, Double> f1Col = new TableColumn<>("F1");
        f1Col.setCellValueFactory(new PropertyValueFactory<>("f1"));
        TableColumn<Incident, Double> f2Col = new TableColumn<>("F2");
        f2Col.setCellValueFactory(new PropertyValueFactory<>("f2"));
        TableColumn<Incident, Double> f3Col = new TableColumn<>("F3");
        f3Col.setCellValueFactory(new PropertyValueFactory<>("f3"));
        TableColumn<Incident, Double> indexCol = new TableColumn<>("I");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("severityIndex"));

        incidentTable.getColumns().addAll(idCol, routeCol, typeCol, stateCol, durationCol,
                f1Col, f2Col, f3Col, indexCol);
        incidentTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIncidentId = selected.getId();
                iRouteIdField.setText(String.valueOf(selected.getRouteId()));
                iTypeField.setText(selected.getIncidentType());
                iDurationField.setText(String.valueOf(selected.getDisruptionDuration()));
                iStateCombo.setValue(selected.getState());
            }
        });

        iStateCombo.getItems().addAll(
                IncidentState.NEW,
                IncidentState.CONFIRMED,
                IncidentState.RESPONSE,
                IncidentState.CLOSED
        );

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));
        form.add(new Label("Маршрут ID:"), 0, 0);
        form.add(iRouteIdField, 1, 0);
        form.add(new Label("Тип інциденту:"), 0, 1);
        form.add(iTypeField, 1, 1);
        form.add(new Label("Порушення руху, хв:"), 0, 2);
        form.add(iDurationField, 1, 2);
        form.add(new Label("Стан:"), 0, 3);
        form.add(iStateCombo, 1, 3);

        Button btnAdd = new Button("Додати");
        Button btnUpdate = new Button("Змінити");
        Button btnDelete = new Button("Видалити");
        Button btnRefresh = new Button("Оновити");
        Button btnNextState = new Button("Наступний стан");

        btnAdd.setOnAction(e -> handleAddIncident());
        btnUpdate.setOnAction(e -> handleUpdateIncident());
        btnDelete.setOnAction(e -> handleDeleteIncident());
        btnRefresh.setOnAction(e -> refreshIncidents());
        btnNextState.setOnAction(e -> handleAdvanceIncidentState());

        HBox buttons = new HBox(10, btnAdd, btnUpdate, btnDelete, btnNextState, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label legend = new Label(
                "Життєвий цикл: ПОВІДОМЛЕНО → ПІДТВЕРДЖЕНО → РЕАГУВАННЯ → ЗАКРИТО. "
                        + "I = 0,40·F1 + 0,35·F2 + 0,25·F3 (усі фактори 0–100).");
        legend.setWrapText(true);

        VBox left = new VBox(10, incidentTable, buttons, legend);
        left.setPadding(new Insets(10));
        left.setFillWidth(true);
        VBox.setVgrow(incidentTable, Priority.ALWAYS);

        VBox right = new VBox(10, new Label("Форма інциденту"), form);
        right.setPadding(new Insets(10));

        HBox content = new HBox(10, left, right);
        content.setFillHeight(true);
        HBox.setHgrow(left, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private void handleAddIncident() {
        try {
            Incident i = new Incident(
                    null,
                    Long.parseLong(iRouteIdField.getText()),
                    iTypeField.getText(),
                    iStateCombo.getValue() == null ? IncidentState.NEW : iStateCombo.getValue(),
                    Double.parseDouble(iDurationField.getText()),
                    0, 0, 0, 0
            );
            api.addIncident(i);
            clearIncidentForm();
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка додавання інциденту", ex.getMessage());
        }
    }

    private void handleUpdateIncident() {
        if (selectedIncidentId == null) {
            showError("Виберіть запис", "Оберіть інцидент зі списку для зміни.");
            return;
        }
        try {
            Incident i = new Incident(
                    selectedIncidentId,
                    Long.parseLong(iRouteIdField.getText()),
                    iTypeField.getText(),
                    iStateCombo.getValue() == null ? IncidentState.NEW : iStateCombo.getValue(),
                    Double.parseDouble(iDurationField.getText()),
                    0, 0, 0, 0
            );
            api.updateIncident(i);
            selectedIncidentId = null;
            clearIncidentForm();
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка оновлення інциденту", ex.getMessage());
        }
    }

    /**
     * Переводить інцидент до наступного стану життєвого циклу.
     *
     * <p>Клієнт лише визначає бажаний стан за життєвим циклом і передає
     * його серверу. Допустимість переходу перевіряє Service.</p>
     */
    private void handleAdvanceIncidentState() {
        if (selectedIncidentId == null) {
            showError("Виберіть запис", "Оберіть інцидент зі списку.");
            return;
        }
        Incident selected = incidentTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getState() == null) {
            showError("Виберіть запис", "Оберіть інцидент зі списку.");
            return;
        }
        IncidentState next = nextState(selected.getState());
        if (next == null) {
            showError("Недопустимий перехід",
                    "Інцидент уже є у кінцевому стані: " + selected.getState().getTitle());
            return;
        }
        try {
            api.changeIncidentState(selectedIncidentId, next);
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка зміни стану інциденту", ex.getMessage());
        }
    }

    /** Визначає наступний стан життєвого циклу інциденту. */
    private static IncidentState nextState(IncidentState current) {
        return switch (current) {
            case NEW -> IncidentState.CONFIRMED;
            case CONFIRMED -> IncidentState.RESPONSE;
            case RESPONSE -> IncidentState.CLOSED;
            case CLOSED -> null;
        };
    }

    private void handleDeleteIncident() {
        if (selectedIncidentId == null) {
            showError("Виберіть запис", "Оберіть інцидент зі списку для видалення.");
            return;
        }
        try {
            api.deleteIncident(selectedIncidentId);
            selectedIncidentId = null;
            clearIncidentForm();
            refreshIncidents();
        } catch (Exception ex) {
            showError("Помилка видалення інциденту", ex.getMessage());
        }
    }

    private void clearIncidentForm() {
        iRouteIdField.clear();
        iTypeField.clear();
        iDurationField.clear();
        iStateCombo.setValue(null);
        incidentTable.getSelectionModel().clearSelection();
        selectedIncidentId = null;
    }

    private void refreshIncidents() {
        try {
            List<Incident> list = api.getIncidents();
            incidentTable.getItems().setAll(list);
        } catch (Exception ex) {
            showError("Помилка завантаження інцидентів", ex.getMessage());
        }
    }

    // =========================================================
    // Вкладка «Завдання реагування»
    // =========================================================
    private Tab createTaskTab() {
        Tab tab = new Tab("Завдання реагування");

        TableColumn<ResponseTask, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<ResponseTask, Long> incidentCol = new TableColumn<>("Інцидент ID");
        incidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        TableColumn<ResponseTask, String> descCol = new TableColumn<>("Опис");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<ResponseTask, String> dateCol = new TableColumn<>("План");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("plannedDate"));

        taskTable.getColumns().addAll(idCol, incidentCol, descCol, dateCol);
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedTaskId = selected.getId();
                tIncidentIdField.setText(String.valueOf(selected.getIncidentId()));
                tDescriptionField.setText(selected.getDescription());
                tPlannedDateField.setText(selected.getPlannedDate() == null
                        ? "" : selected.getPlannedDate().toString());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));
        form.add(new Label("Інцидент ID:"), 0, 0);
        form.add(tIncidentIdField, 1, 0);
        form.add(new Label("Опис:"), 0, 1);
        form.add(tDescriptionField, 1, 1);
        form.add(new Label("План (YYYY-MM-DDTHH:mm):"), 0, 2);
        form.add(tPlannedDateField, 1, 2);

        Button btnAdd = new Button("Додати");
        Button btnUpdate = new Button("Змінити");
        Button btnDelete = new Button("Видалити");
        Button btnRefresh = new Button("Оновити");

        btnAdd.setOnAction(e -> handleAddTask());
        btnUpdate.setOnAction(e -> handleUpdateTask());
        btnDelete.setOnAction(e -> handleDeleteTask());
        btnRefresh.setOnAction(e -> refreshTasks());

        HBox buttons = new HBox(10, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(10, taskTable, buttons);
        left.setPadding(new Insets(10));
        left.setFillWidth(true);
        VBox.setVgrow(taskTable, Priority.ALWAYS);

        VBox right = new VBox(10, new Label("Форма завдання реагування"), form);
        right.setPadding(new Insets(10));

        HBox content = new HBox(10, left, right);
        content.setFillHeight(true);
        HBox.setHgrow(left, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private void handleAddTask() {
        try {
            ResponseTask t = new ResponseTask(
                    null,
                    Long.parseLong(tIncidentIdField.getText()),
                    tDescriptionField.getText(),
                    java.time.LocalDateTime.parse(tPlannedDateField.getText().trim())
            );
            api.addResponseTask(t);
            clearTaskForm();
            refreshTasks();
        } catch (Exception ex) {
            showError("Помилка додавання завдання", ex.getMessage());
        }
    }

    private void handleUpdateTask() {
        if (selectedTaskId == null) {
            showError("Виберіть запис", "Оберіть завдання зі списку для зміни.");
            return;
        }
        try {
            ResponseTask t = new ResponseTask(
                    selectedTaskId,
                    Long.parseLong(tIncidentIdField.getText()),
                    tDescriptionField.getText(),
                    java.time.LocalDateTime.parse(tPlannedDateField.getText().trim())
            );
            api.updateResponseTask(t);
            selectedTaskId = null;
            clearTaskForm();
            refreshTasks();
        } catch (Exception ex) {
            showError("Помилка оновлення завдання", ex.getMessage());
        }
    }

    private void handleDeleteTask() {
        if (selectedTaskId == null) {
            showError("Виберіть запис", "Оберіть завдання зі списку для видалення.");
            return;
        }
        try {
            api.deleteResponseTask(selectedTaskId);
            selectedTaskId = null;
            clearTaskForm();
            refreshTasks();
        } catch (Exception ex) {
            showError("Помилка видалення завдання", ex.getMessage());
        }
    }

    private void clearTaskForm() {
        tIncidentIdField.clear();
        tDescriptionField.clear();
        tPlannedDateField.clear();
        taskTable.getSelectionModel().clearSelection();
        selectedTaskId = null;
    }

    private void refreshTasks() {
        try {
            List<ResponseTask> list = api.getResponseTasks();
            taskTable.getItems().setAll(list);
        } catch (Exception ex) {
            showError("Помилка завантаження завдань реагування", ex.getMessage());
        }
    }

    // =========================================================
    // Спільні методи
    // =========================================================
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
