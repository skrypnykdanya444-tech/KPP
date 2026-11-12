package ua.edu.nupp.kpp.server;

/**
 * Транспортний засіб (основна сутність №1).
 *
 * <p>Відповідає таблиці {@code Vehicle}. Транспортний засіб є кореневою
 * сутністю системи: саме реєстр транспортних засобів імпортується
 * з CSV-файлу у фоновому режимі (JavaFX Task з відображенням прогресу).</p>
 */
public class Vehicle {

    /** Власний первинний ключ. До запису в БД значення може бути {@code null}. */
    private Long id;

    /** Код транспортного засобу, наприклад {@code TR-01}. */
    private String code;

    /** Назва (марка) транспортного засобу. */
    private String name;

    /** Категорія транспортного засобу: Автобус, Тролейбус, Трамвай тощо. */
    private String category;

    public Vehicle() {
    }

    public Vehicle(Long id, String code, String name, String category) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
