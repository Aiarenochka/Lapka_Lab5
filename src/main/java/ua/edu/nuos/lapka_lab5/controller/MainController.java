package ua.edu.nuos.lapka_lab5.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ua.edu.nuos.lapka_lab5.config.DatabaseConfig;
import ua.edu.nuos.lapka_lab5.dao.ApplicationDao;
import ua.edu.nuos.lapka_lab5.dao.PetDao;
import ua.edu.nuos.lapka_lab5.dao.ShelterDao;
import ua.edu.nuos.lapka_lab5.model.*;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final Map<String, String> SPECIES = map("dog", "Собака", "cat", "Кіт");
    private static final Map<String, String> SEX = map("male", "Хлопчик", "female", "Дівчинка");
    private static final Map<String, String> STATUS = map(
            "available", "Шукає дім",
            "reserved", "Резерв",
            "adopted", "Прилаштовано",
            "archived", "В архіві");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final List<String> QUERIES = List.of(
            "1. Тварини заданого виду, які шукають дім, у заданому місті",
            "2. Картка тварини з медичною книжкою",
            "3. Статистика притулків",
            "4. Тварини, яким потрібна вакцинація",
            "5. Подати анкету на усиновлення",
            "6. Схвалити анкету");

    @FXML private TableView<Pet> petTable;
    @FXML private TableColumn<Pet, Integer> colPetId;
    @FXML private TableColumn<Pet, String> colPetName;
    @FXML private TableColumn<Pet, String> colPetSpecies;
    @FXML private TableColumn<Pet, String> colPetSex;
    @FXML private TableColumn<Pet, String> colPetBreed;
    @FXML private TableColumn<Pet, Integer> colPetAge;
    @FXML private TableColumn<Pet, String> colPetStatus;
    @FXML private TableColumn<Pet, String> colPetShelter;

    @FXML private TextField petNameField;
    @FXML private ComboBox<String> petSpeciesBox;
    @FXML private ComboBox<String> petSexBox;
    @FXML private TextField petBreedField;
    @FXML private TextField petAgeField;
    @FXML private ComboBox<String> petStatusBox;
    @FXML private ComboBox<Shelter> petShelterBox;
    @FXML private TextArea petDescriptionArea;

    @FXML private TableView<Shelter> shelterTable;
    @FXML private TableColumn<Shelter, Integer> colShelterId;
    @FXML private TableColumn<Shelter, String> colShelterName;
    @FXML private TableColumn<Shelter, String> colShelterCity;
    @FXML private TableColumn<Shelter, String> colShelterPhone;
    @FXML private TableColumn<Shelter, String> colShelterEmail;
    @FXML private TableColumn<Shelter, String> colShelterAddress;

    @FXML private TextField shelterNameField;
    @FXML private TextField shelterCityField;
    @FXML private TextField shelterPhoneField;
    @FXML private TextField shelterEmailField;
    @FXML private TextField shelterAddressField;

    @FXML private ComboBox<String> queryBox;
    @FXML private VBox paramSpecies;
    @FXML private VBox paramCity;
    @FXML private VBox paramPetId;
    @FXML private VBox paramMonths;
    @FXML private VBox paramFullName;
    @FXML private VBox paramPhone;
    @FXML private VBox paramReason;
    @FXML private VBox paramApplication;
    @FXML private ComboBox<String> querySpeciesBox;
    @FXML private ComboBox<String> queryCityBox;
    @FXML private TextField queryPetIdField;
    @FXML private TextField queryMonthsField;
    @FXML private TextField queryFullNameField;
    @FXML private TextField queryPhoneField;
    @FXML private TextField queryReasonField;
    @FXML private ComboBox<AdoptionApplication> queryApplicationBox;
    @FXML private TextArea resultArea;

    @FXML private Label statusLabel;

    private PetDao petDao;
    private ShelterDao shelterDao;
    private ApplicationDao applicationDao;
    private final ObservableList<Pet> pets = FXCollections.observableArrayList();
    private final ObservableList<Shelter> shelters = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        DatabaseConfig config = new DatabaseConfig();
        petDao = new PetDao(config);
        shelterDao = new ShelterDao(config);
        applicationDao = new ApplicationDao(config);

        colPetId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPetName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPetSpecies.setCellValueFactory(c -> new SimpleStringProperty(SPECIES.get(c.getValue().getSpecies())));
        colPetSex.setCellValueFactory(c -> new SimpleStringProperty(SEX.get(c.getValue().getSex())));
        colPetBreed.setCellValueFactory(new PropertyValueFactory<>("breed"));
        colPetAge.setCellValueFactory(new PropertyValueFactory<>("ageMonths"));
        colPetStatus.setCellValueFactory(c -> new SimpleStringProperty(STATUS.get(c.getValue().getStatus())));
        colPetShelter.setCellValueFactory(new PropertyValueFactory<>("shelterName"));
        petTable.setItems(pets);

        colShelterId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colShelterName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colShelterCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colShelterPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colShelterEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colShelterAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        shelterTable.setItems(shelters);

        petSpeciesBox.getItems().addAll(SPECIES.values());
        petSexBox.getItems().addAll(SEX.values());
        petStatusBox.getItems().addAll(STATUS.values());
        petShelterBox.setItems(shelters);

        petTable.getSelectionModel().selectedItemProperty().addListener((obs, oldPet, pet) -> {
            if (pet != null) {
                fillPetForm(pet);
            }
        });
        shelterTable.getSelectionModel().selectedItemProperty().addListener((obs, oldShelter, shelter) -> {
            if (shelter != null) {
                fillShelterForm(shelter);
            }
        });

        queryBox.getItems().addAll(QUERIES);
        querySpeciesBox.getItems().addAll(SPECIES.values());
        queryBox.valueProperty().addListener((obs, oldQuery, query) -> updateQueryParams());
        queryBox.getSelectionModel().selectFirst();

        loadData();
    }

    private void loadData() {
        try {
            shelters.setAll(shelterDao.findAll());
            pets.setAll(petDao.findAll());
            queryCityBox.getItems().setAll(shelterDao.findCities());
            queryApplicationBox.getItems().setAll(applicationDao.findNew());
            statusLabel.setText("Завантажено: притулків " + shelters.size() + ", тварин " + pets.size());
        } catch (SQLException e) {
            showError("Не вдалося завантажити дані з бази:\n" + e.getMessage());
        }
    }

    @FXML
    private void onAddPet() {
        Pet pet = readPetForm(0);
        if (pet == null) {
            return;
        }
        try {
            petDao.create(pet);
            loadData();
            onClearPet();
            statusLabel.setText("Тварину «" + pet.getName() + "» додано");
        } catch (SQLException e) {
            showError("Не вдалося додати тварину:\n" + e.getMessage());
        }
    }

    @FXML
    private void onUpdatePet() {
        Pet selected = petTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Спочатку оберіть тварину в таблиці");
            return;
        }
        Pet pet = readPetForm(selected.getId());
        if (pet == null) {
            return;
        }
        try {
            petDao.update(pet);
            loadData();
            onClearPet();
            statusLabel.setText("Дані тварини «" + pet.getName() + "» оновлено");
        } catch (SQLException e) {
            showError("Не вдалося оновити тварину:\n" + e.getMessage());
        }
    }

    @FXML
    private void onDeletePet() {
        Pet selected = petTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Спочатку оберіть тварину в таблиці");
            return;
        }
        if (!confirm("Видалити тварину «" + selected.getName() + "»?\n"
                + "Разом з нею видаляться її медичні записи та анкети.")) {
            return;
        }
        try {
            petDao.delete(selected.getId());
            loadData();
            onClearPet();
            statusLabel.setText("Тварину «" + selected.getName() + "» видалено");
        } catch (SQLException e) {
            showError("Не вдалося видалити тварину:\n" + e.getMessage());
        }
    }

    @FXML
    private void onClearPet() {
        petTable.getSelectionModel().clearSelection();
        petNameField.clear();
        petSpeciesBox.setValue(null);
        petSexBox.setValue(null);
        petBreedField.clear();
        petAgeField.clear();
        petStatusBox.setValue(null);
        petShelterBox.setValue(null);
        petDescriptionArea.clear();
    }

    private void fillPetForm(Pet pet) {
        petNameField.setText(pet.getName());
        petSpeciesBox.setValue(SPECIES.get(pet.getSpecies()));
        petSexBox.setValue(SEX.get(pet.getSex()));
        petBreedField.setText(pet.getBreed());
        petAgeField.setText(String.valueOf(pet.getAgeMonths()));
        petStatusBox.setValue(STATUS.get(pet.getStatus()));
        petDescriptionArea.setText(pet.getDescription());
        for (Shelter s : shelters) {
            if (s.getId() == pet.getShelterId()) {
                petShelterBox.setValue(s);
            }
        }
    }

    private Pet readPetForm(int id) {
        String name = petNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Введіть кличку");
            return null;
        }
        if (petSpeciesBox.getValue() == null || petSexBox.getValue() == null
                || petStatusBox.getValue() == null || petShelterBox.getValue() == null) {
            showError("Оберіть вид, стать, статус і притулок");
            return null;
        }
        int age;
        try {
            age = Integer.parseInt(petAgeField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Вік має бути цілим числом");
            return null;
        }
        if (age < 0) {
            showError("Вік не може бути від'ємним");
            return null;
        }
        String breed = petBreedField.getText().trim();
        if (breed.isEmpty()) {
            breed = "метис";
        }
        String description = petDescriptionArea.getText().trim();
        Shelter shelter = petShelterBox.getValue();

        return new Pet(id, shelter.getId(), shelter.getName(), name,
                key(SPECIES, petSpeciesBox.getValue()),
                key(SEX, petSexBox.getValue()),
                breed, age,
                description.isEmpty() ? null : description,
                key(STATUS, petStatusBox.getValue()));
    }

    @FXML
    private void onAddShelter() {
        Shelter shelter = readShelterForm(0);
        if (shelter == null) {
            return;
        }
        try {
            shelterDao.create(shelter);
            loadData();
            onClearShelter();
            statusLabel.setText("Притулок «" + shelter.getName() + "» додано");
        } catch (SQLIntegrityConstraintViolationException e) {
            showError("Притулок з таким email уже існує");
        } catch (SQLException e) {
            showError("Не вдалося додати притулок:\n" + e.getMessage());
        }
    }

    @FXML
    private void onUpdateShelter() {
        Shelter selected = shelterTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Спочатку оберіть притулок у таблиці");
            return;
        }
        Shelter shelter = readShelterForm(selected.getId());
        if (shelter == null) {
            return;
        }
        try {
            shelterDao.update(shelter);
            loadData();
            onClearShelter();
            statusLabel.setText("Дані притулку «" + shelter.getName() + "» оновлено");
        } catch (SQLIntegrityConstraintViolationException e) {
            showError("Притулок з таким email уже існує");
        } catch (SQLException e) {
            showError("Не вдалося оновити притулок:\n" + e.getMessage());
        }
    }

    @FXML
    private void onDeleteShelter() {
        Shelter selected = shelterTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Спочатку оберіть притулок у таблиці");
            return;
        }
        long petCount = pets.stream().filter(p -> p.getShelterId() == selected.getId()).count();
        if (!confirm("Видалити притулок «" + selected.getName() + "»?\n"
                + "Разом з ним буде видалено тварин: " + petCount)) {
            return;
        }
        try {
            shelterDao.delete(selected.getId());
            loadData();
            onClearShelter();
            statusLabel.setText("Притулок «" + selected.getName() + "» видалено");
        } catch (SQLException e) {
            showError("Не вдалося видалити притулок:\n" + e.getMessage());
        }
    }

    @FXML
    private void onClearShelter() {
        shelterTable.getSelectionModel().clearSelection();
        shelterNameField.clear();
        shelterCityField.clear();
        shelterPhoneField.clear();
        shelterEmailField.clear();
        shelterAddressField.clear();
    }

    private void fillShelterForm(Shelter shelter) {
        shelterNameField.setText(shelter.getName());
        shelterCityField.setText(shelter.getCity());
        shelterPhoneField.setText(shelter.getPhone());
        shelterEmailField.setText(shelter.getEmail());
        shelterAddressField.setText(shelter.getAddress());
    }

    private Shelter readShelterForm(int id) {
        String name = shelterNameField.getText().trim();
        String city = shelterCityField.getText().trim();
        String phone = shelterPhoneField.getText().trim();
        String email = shelterEmailField.getText().trim();
        String address = shelterAddressField.getText().trim();

        if (name.isEmpty() || city.isEmpty()) {
            showError("Введіть назву і місто");
            return null;
        }
        if (!phone.matches("\\+380\\d{9}")) {
            showError("Телефон має бути у форматі +380XXXXXXXXX");
            return null;
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            showError("Введіть коректний email");
            return null;
        }
        return new Shelter(id, name, city, phone, email, address.isEmpty() ? null : address);
    }

    private void updateQueryParams() {
        int q = queryBox.getSelectionModel().getSelectedIndex() + 1;
        show(paramSpecies, q == 1);
        show(paramCity, q == 1);
        show(paramPetId, q == 2 || q == 5);
        show(paramMonths, q == 4);
        show(paramFullName, q == 5);
        show(paramPhone, q == 5);
        show(paramReason, q == 5);
        show(paramApplication, q == 6);
        resultArea.clear();
    }

    private void show(VBox param, boolean visible) {
        param.setVisible(visible);
        param.setManaged(visible);
    }

    @FXML
    private void onRunQuery() {
        int q = queryBox.getSelectionModel().getSelectedIndex() + 1;
        try {
            switch (q) {
                case 1 -> runAvailablePets();
                case 2 -> runPetCard();
                case 3 -> runShelterStats();
                case 4 -> runVaccination();
                case 5 -> runSubmitApplication();
                case 6 -> runApproveApplication();
            }
        } catch (SQLException | RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void runAvailablePets() throws SQLException {
        if (querySpeciesBox.getValue() == null || queryCityBox.getValue() == null) {
            showError("Оберіть вид і місто");
            return;
        }
        String species = key(SPECIES, querySpeciesBox.getValue());
        List<Pet> list = petDao.findAvailable(species, queryCityBox.getValue());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-10s %-8s %-10s %-34s %s%n", "ID", "Кличка", "Вид", "Стать", "Порода", "Вік (міс.)"));
        for (Pet p : list) {
            sb.append(String.format("%-4d %-10s %-8s %-10s %-34s %d%n", p.getId(), p.getName(),
                    SPECIES.get(p.getSpecies()), SEX.get(p.getSex()), p.getBreed(), p.getAgeMonths()));
        }
        sb.append("\nЗнайдено тварин: ").append(list.size());
        resultArea.setText(sb.toString());
    }

    private void runPetCard() throws SQLException {
        int petId = readInt(queryPetIdField, "ID тварини");
        Pet pet = petDao.findById(petId);
        if (pet == null) {
            showError("Тварину з ID " + petId + " не знайдено");
            return;
        }
        Shelter shelter = null;
        for (Shelter s : shelters) {
            if (s.getId() == pet.getShelterId()) {
                shelter = s;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(SPECIES.get(pet.getSpecies())).append(" ").append(pet.getName()).append("\n\n");
        sb.append("Стать:    ").append(SEX.get(pet.getSex())).append("\n");
        sb.append("Порода:   ").append(pet.getBreed()).append("\n");
        sb.append("Вік:      ").append(pet.getAgeMonths()).append(" міс.\n");
        sb.append("Статус:   ").append(STATUS.get(pet.getStatus())).append("\n");
        sb.append("Опис:     ").append(pet.getDescription() == null ? "" : pet.getDescription()).append("\n\n");
        if (shelter != null) {
            sb.append("Куратор:  ").append(shelter.getName()).append(", м. ").append(shelter.getCity()).append("\n");
            sb.append("Телефон:  ").append(shelter.getPhone()).append("\n\n");
        }
        sb.append("Медична книжка:\n");
        List<MedicalRecord> records = petDao.findMedicalRecords(petId);
        if (records.isEmpty()) {
            sb.append("  записів немає\n");
        }
        for (MedicalRecord r : records) {
            sb.append(String.format("  %s  %-24s %s%n", r.getRecordDate().format(DATE), r.getProcedureName(),
                    r.getNotes() == null ? "" : r.getNotes()));
        }
        resultArea.setText(sb.toString());
    }

    private void runShelterStats() throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-26s %-8s %6s %10s %7s %13s %6s %12s%n",
                "Притулок", "Місто", "Усього", "Шукає дім", "Резерв", "Прилаштовано", "Архів", "Нові анкети"));
        for (ShelterStats s : shelterDao.getStatistics()) {
            sb.append(String.format("%-26s %-8s %6d %10d %7d %13d %6d %12d%n",
                    s.getShelterName(), s.getCity(), s.getTotal(), s.getAvailable(), s.getReserved(),
                    s.getAdopted(), s.getArchived(), s.getNewApplications()));
        }
        resultArea.setText(sb.toString());
    }

    private void runVaccination() throws SQLException {
        int months = readInt(queryMonthsField, "Кількість місяців");
        List<PetVaccination> list = petDao.findNeedingVaccination(months);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-10s %-28s %s%n", "ID", "Кличка", "Притулок", "Остання вакцинація"));
        for (PetVaccination v : list) {
            String date = v.getLastVaccination() == null ? "не вакциновано" : v.getLastVaccination().format(DATE);
            sb.append(String.format("%-4d %-10s %-28s %s%n", v.getPetId(), v.getPetName(), v.getShelterName(), date));
        }
        sb.append("\nЗнайдено тварин: ").append(list.size());
        resultArea.setText(sb.toString());
    }

    private void runSubmitApplication() throws SQLException {
        int petId = readInt(queryPetIdField, "ID тварини");
        String fullName = queryFullNameField.getText().trim();
        String phone = queryPhoneField.getText().trim();
        if (fullName.isEmpty()) {
            showError("Введіть ПІБ");
            return;
        }
        if (!phone.matches("\\+380\\d{9}")) {
            showError("Телефон має бути у форматі +380XXXXXXXXX");
            return;
        }
        String reason = queryReasonField.getText().trim();
        int id = applicationDao.submit(petId, fullName, phone, reason.isEmpty() ? null : reason);
        loadData();
        resultArea.setText("Анкету №" + id + " збережено.\nТварина з ID " + petId + " тепер у статусі «Резерв».");
    }

    private void runApproveApplication() throws SQLException {
        AdoptionApplication application = queryApplicationBox.getValue();
        if (application == null) {
            showError("Оберіть анкету");
            return;
        }
        int rejected = applicationDao.approve(application.getId());
        loadData();
        resultArea.setText("Анкету №" + application.getId() + " схвалено.\n"
                + application.getPetName() + " тепер має статус «Прилаштовано».\n"
                + "Інших анкет на цю тварину відхилено: " + rejected);
    }

    private int readInt(TextField field, String name) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException(name + " має бути цілим числом");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Підтвердження");
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private static Map<String, String> map(String... pairs) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(pairs[i], pairs[i + 1]);
        }
        return result;
    }

    private static String key(Map<String, String> map, String label) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(label)) {
                return entry.getKey();
            }
        }
        return null;
    }
}