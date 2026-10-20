package ua.kpp.model;

public class Vehicle {
    private Long id;           // PK[cite: 1]
    private String code;       //[cite: 1]
    private String name;       //[cite: 1]
    private String category;   //[cite: 1]

    public Vehicle() {}

    public Vehicle(Long id, String code, String name, String category) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "Vehicle{id=" + id + ", code='" + code + "', name='" + name + "', category='" + category + "'}";
    }
}