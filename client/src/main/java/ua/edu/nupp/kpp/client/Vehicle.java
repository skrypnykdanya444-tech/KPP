package ua.edu.nupp.kpp.client;

/**
 * Клієнтська модель транспортного засобу (DTO).
 *
 * <p>Дублює серверну сутність {@code Vehicle}, але живе у клієнтському
 * модулі: клієнт не залежить від серверного модуля і спілкується з ним
 * лише через HTTP/JSON.</p>
 */
public class Vehicle {

    private Long id;
    private String code;
    private String name;
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
