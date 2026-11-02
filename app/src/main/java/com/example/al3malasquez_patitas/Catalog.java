package com.example.al3malasquez_patitas;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class Catalog {
    public static final String ALL = "Todos";
    public static final String[] CATEGORIES = {
            ALL, "Abrigos", "Suéteres", "Camisetas", "Vestidos", "Pijamas", "Accesorios"
    };

    private static final List<Product> PRODUCTS = buildCatalog();

    private Catalog() {
    }

    public static List<Product> all() {
        return PRODUCTS;
    }

    public static Product byId(int id) {
        for (Product product : PRODUCTS) {
            if (product.id == id) {
                return product;
            }
        }
        return null;
    }

    public static List<Product> filter(String category, String rawQuery,
                                       Set<Integer> favorites, boolean favoritesOnly) {
        String query = normalize(rawQuery);
        List<Product> result = new ArrayList<>();

        for (Product product : PRODUCTS) {
            boolean categoryMatches = ALL.equals(category) || product.category.equals(category);
            boolean favoriteMatches = !favoritesOnly || favorites.contains(product.id);
            boolean queryMatches = query.isEmpty()
                    || normalize(product.name).contains(query)
                    || normalize(product.category).contains(query)
                    || normalize(product.description).contains(query);

            if (categoryMatches && favoriteMatches && queryMatches) {
                result.add(product);
            }
        }

        return result;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private static List<Product> buildCatalog() {
        List<Product> products = new ArrayList<>();

        products.add(new Product(
                1, R.drawable.product_raincoat, 119900,
                "Impermeable Llovizna", "Abrigos",
                "Impermeable liviano con capucha y costuras selladas. Un básico cálido y divertido para pasear bajo la lluvia sin renunciar al estilo.",
                "Nuevo", 4.9f, new int[]{0xFFE3A72F, 0xFF2F4A3F, 0xFFD65A3A}));

        products.add(new Product(
                2, R.drawable.product_sweater, 89900,
                "Suéter Bruma", "Suéteres",
                "Tejido de punto extra suave con cuello alto. Abriga sin apretar y se adapta a todos los movimientos de tu perro.",
                "Más vendido", 4.8f, new int[]{0xFF7B2D3B, 0xFF495B45, 0xFFC9B29B}));

        products.add(new Product(
                3, R.drawable.product_plaid, 139900,
                "Abrigo Dandi", "Abrigos",
                "Abrigo de paño con estampado príncipe de Gales y forro térmico. Elegancia de paseo para los días fríos.",
                "", 4.7f, new int[]{0xFFB7A38A, 0xFF5C6652, 0xFF6B4B3E}));

        products.add(new Product(
                4, R.drawable.product_puffer, 149900,
                "Chaqueta Polar", "Abrigos",
                "Chaqueta acolchada ultraliviana con cierre frontal y acabado repelente al agua. Calor de plumas, comodidad total.",
                "-15%", 4.9f, new int[]{0xFFE0742C, 0xFF334E68, 0xFFB33B33}));

        products.add(new Product(
                5, R.drawable.product_harness, 99900,
                "Arnés Luna", "Accesorios",
                "Arnés ergonómico con hebillas regulables y tejido transpirable. Seguridad y estilo en cada caminata.",
                "", 4.8f, new int[]{0xFF435C92, 0xFFB05A64, 0xFF3E3B39}));

        products.add(new Product(
                6, R.drawable.product_leather, 159900,
                "Chaqueta Moto", "Abrigos",
                "Chaqueta de cuero vegano con forro polar y detalles metálicos. Para perros que marcan tendencia.",
                "Premium", 5.0f, new int[]{0xFF282422, 0xFF6B4D3A, 0xFF8D2F2F}));

        products.add(new Product(
                7, R.drawable.product_windbreaker, 109900,
                "Cortavientos Citrus", "Abrigos",
                "Cortavientos ligero con cierre de cremallera y protección contra el viento. Perfecto para aventuras de media estación.",
                "Nuevo", 4.8f, new int[]{0xFFE98A35, 0xFFDFC56E, 0xFF7A8B65}));

        products.add(new Product(
                8, R.drawable.product_dress, 79900,
                "Vestido Coral", "Vestidos",
                "Vestido de algodón con estampado floral y cierre fácil. Un look fresco y coqueto para ocasiones especiales.",
                "", 4.6f, new int[]{0xFFED7A5D, 0xFF6FA0A8, 0xFFF2D0A4}));

        products.add(new Product(
                9, R.drawable.product_pajama, 69900,
                "Pijama Nubes", "Pijamas",
                "Enterizo de algodón suave con puños elásticos. Pensado para dormir y descansar con total comodidad.",
                "Suave", 4.9f, new int[]{0xFFAFCFD4, 0xFFE8B7B0, 0xFF4A5A6A}));

        products.add(new Product(
                10, R.drawable.product_hoodie, 99900,
                "Hoodie Street", "Camisetas",
                "Felpa ligera con capucha y bolsillo canguro. El equilibrio perfecto entre abrigo y actitud urbana.",
                "", 4.8f, new int[]{0xFF2E2D2C, 0xFFF0EDE6, 0xFF9C3A33}));

        products.add(new Product(
                11, R.drawable.product_shirt, 74900,
                "Camisa Aloha", "Camisetas",
                "Camisa fresca de algodón con estampado tropical. Ideal para días cálidos y salidas con personalidad.",
                "", 4.7f, new int[]{0xFF2D3E57, 0xFF6E8B7C, 0xFFD6C7A1}));

        products.add(new Product(
                12, R.drawable.product_bandana, 34900,
                "Bandana Limón", "Accesorios",
                "Bandana reversible de algodón con nudo ajustable. Un detalle de color para completar cualquier look.",
                "", 4.9f, new int[]{0xFFB7D36A, 0xFFEFB7C2, 0xFFF7E9A0}));

        products.add(new Product(
                13, R.drawable.product_coat, 129900,
                "Abrigo Té", "Abrigos",
                "Abrigo acolchado con forro de felpa y cierre de velcro. Calidez envolvente para los días más fríos.",
                "", 4.7f, new int[]{0xFFE7D9C4, 0xFF9A7B61, 0xFF3D403E}));

        return Collections.unmodifiableList(products);
    }
}
