package com.example.kp_pi;

public class Service {
    private int id;
    private String name;
    private String description;
    private double price;
    private int duration;
    private String category;
    private boolean isActive; // Новое поле

    public Service() {}

    public Service(String name, String description, double price, int duration, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.category = category;
        this.isActive = true; // По умолчанию активна
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}