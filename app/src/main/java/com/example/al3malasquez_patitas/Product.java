package com.example.al3malasquez_patitas;

public final class Product {
    public final int id;
    public final int imageRes;
    public final int price;
    public final String name;
    public final String category;
    public final String description;
    public final String badge;
    public final float rating;
    public final int[] swatches;
    public final String[] sizes;

    public Product(int id, int imageRes, int price, String name, String category,
                   String description, String badge, float rating, int[] swatches) {
        this.id = id;
        this.imageRes = imageRes;
        this.price = price;
        this.name = name;
        this.category = category;
        this.description = description;
        this.badge = badge;
        this.rating = rating;
        this.swatches = swatches;
        this.sizes = new String[]{"S", "M", "L", "XL"};
    }

    public boolean hasBadge() {
        return badge != null && !badge.isEmpty();
    }
}
