package com.example.al3malasquez_patitas;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "patitas_data";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_CART = "cart";
    private static final int MAX_QUANTITY = 20;

    private LinearLayout productsContainer;
    private LinearLayout categoriesContainer;
    private NestedScrollView scrollView;
    private View sectionHeader;
    private View emptyState;
    private TextView tvSectionTitle;
    private TextView tvSectionSubtitle;
    private TextView tvCartBadge;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private TextView btnEmptyAction;
    private EditText etSearch;
    private ImageButton btnFavorites;
    private ImageButton btnClearSearch;
    private ImageButton btnCart;

    private SharedPreferences preferences;
    private final Set<Integer> favoriteIds = new HashSet<>();
    private final Map<Integer, Integer> cart = new LinkedHashMap<>();
    private final List<TextView> categoryPills = new ArrayList<>();

    private String selectedCategory = Catalog.ALL;
    private boolean favoritesOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        setupSystemBars();
        bindViews();

        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadSavedState();
        buildCategoryPills();
        setupListeners();
        applyFilters();
        updateCartBadge();

        View root = findViewById(R.id.root);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
    }

    private void setupSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        productsContainer = findViewById(R.id.productsContainer);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        scrollView = findViewById(R.id.scrollView);
        sectionHeader = findViewById(R.id.sectionHeader);
        emptyState = findViewById(R.id.emptyState);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvSectionSubtitle = findViewById(R.id.tvSectionSubtitle);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        btnEmptyAction = findViewById(R.id.btnEmptyAction);
        etSearch = findViewById(R.id.etSearch);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnCart = findViewById(R.id.btnCart);
    }

    private void buildCategoryPills() {
        categoriesContainer.removeAllViews();
        categoryPills.clear();

        for (String category : Catalog.CATEGORIES) {
            TextView pill = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, dp(38));
            params.setMarginEnd(dp(8));
            pill.setLayoutParams(params);
            pill.setText(category);
            pill.setTag(category);
            pill.setGravity(Gravity.CENTER);
            pill.setPadding(dp(17), 0, dp(17), 0);
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            pill.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            pill.setBackgroundResource(R.drawable.bg_category_pill);
            pill.setTextColor(ContextCompat.getColorStateList(this, R.color.category_text));
            pill.setClickable(true);
            pill.setFocusable(true);
            pill.setOnClickListener(v -> selectCategory(category));
            categoriesContainer.addView(pill);
            categoryPills.add(pill);
        }

        updateCategorySelection();
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilters();
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));

        btnFavorites.setOnClickListener(v -> {
            favoritesOnly = !favoritesOnly;
            updateCategorySelection();
            updateFavoritesButton();
            applyFilters();
        });

        btnCart.setOnClickListener(v -> showCart());

        View.OnClickListener seeAll = v -> resetFilters();
        findViewById(R.id.tvSeeAll).setOnClickListener(seeAll);
        btnEmptyAction.setOnClickListener(seeAll);

        View.OnClickListener openCoats = v -> selectCategory("Abrigos");
        findViewById(R.id.heroCard).setOnClickListener(openCoats);
        findViewById(R.id.heroCta).setOnClickListener(openCoats);
    }

    private void selectCategory(String category) {
        favoritesOnly = false;
        selectedCategory = category;
        updateCategorySelection();
        updateFavoritesButton();
        applyFilters();
        hideKeyboard();
        scrollView.post(() -> scrollView.smoothScrollTo(0, Math.max(0, sectionHeader.getTop())));
    }

    private void resetFilters() {
        favoritesOnly = false;
        selectedCategory = Catalog.ALL;
        updateCategorySelection();
        updateFavoritesButton();
        if (etSearch.getText().length() > 0) {
            etSearch.setText("");
        } else {
            applyFilters();
        }
        hideKeyboard();
        scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
    }

    private void updateCategorySelection() {
        for (TextView pill : categoryPills) {
            String category = (String) pill.getTag();
            pill.setSelected(!favoritesOnly && category.equals(selectedCategory));
        }
    }

    private void updateFavoritesButton() {
        btnFavorites.setImageResource(favoritesOnly
                ? R.drawable.ic_heart_filled
                : R.drawable.ic_heart);
        btnFavorites.setColorFilter(ContextCompat.getColor(this,
                favoritesOnly ? R.color.primary : R.color.ink));
        btnFavorites.setBackgroundResource(favoritesOnly
                ? R.drawable.bg_icon_round_active
                : R.drawable.bg_icon_round);
    }

    private void applyFilters() {
        if (etSearch == null) {
            return;
        }
        String query = etSearch.getText().toString().trim();
        List<Product> filtered = Catalog.filter(selectedCategory, query, favoriteIds, favoritesOnly);

        renderProducts(filtered);
        updateSectionHeader(filtered.size(), query);

        boolean isEmpty = filtered.isEmpty();
        productsContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (isEmpty) {
            boolean noFavorites = favoritesOnly && query.isEmpty();
            tvEmptyTitle.setText(noFavorites
                    ? R.string.empty_favorites_title
                    : R.string.empty_results_title);
            tvEmptyText.setText(noFavorites
                    ? R.string.empty_favorites_text
                    : R.string.empty_results_text);
        }
    }

    private void updateSectionHeader(int count, String query) {
        if (favoritesOnly) {
            tvSectionTitle.setText(R.string.section_favorites);
            tvSectionSubtitle.setText(R.string.subtitle_favorites);
        } else if (!query.isEmpty()) {
            tvSectionTitle.setText(R.string.section_search);
            tvSectionSubtitle.setText(subtitleForCount(count));
        } else if (!Catalog.ALL.equals(selectedCategory)) {
            tvSectionTitle.setText(selectedCategory);
            tvSectionSubtitle.setText(subtitleForCount(count));
        } else {
            tvSectionTitle.setText(R.string.section_default);
            tvSectionSubtitle.setText(R.string.subtitle_default);
        }
    }

    private String subtitleForCount(int count) {
        return count == 1
                ? getString(R.string.subtitle_one)
                : getString(R.string.subtitle_count, count);
    }

    private void renderProducts(List<Product> products) {
        productsContainer.removeAllViews();

        for (int index = 0; index < products.size(); index += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (index > 0) {
                rowParams.topMargin = dp(14);
            }
            row.setLayoutParams(rowParams);

            View first = createProductCard(products.get(index));
            row.addView(first, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            if (index + 1 < products.size()) {
                View spacer = new View(this);
                row.addView(spacer, new LinearLayout.LayoutParams(dp(12), 1));
                View second = createProductCard(products.get(index + 1));
                row.addView(second, new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            } else {
                View spacer = new View(this);
                row.addView(spacer, new LinearLayout.LayoutParams(
                        0, 1, 1f));
            }

            productsContainer.addView(row);
        }
    }

    private View createProductCard(Product product) {
        View card = getLayoutInflater().inflate(R.layout.item_product_card, productsContainer, false);
        MaterialCardView cardRoot = card.findViewById(R.id.productCard);
        ImageView image = card.findViewById(R.id.ivProductImage);
        TextView badge = card.findViewById(R.id.tvBadge);
        TextView rating = card.findViewById(R.id.tvRating);
        TextView category = card.findViewById(R.id.tvCategory);
        TextView name = card.findViewById(R.id.tvName);
        TextView price = card.findViewById(R.id.tvPrice);
        ImageButton favorite = card.findViewById(R.id.btnCardFavorite);
        ImageButton add = card.findViewById(R.id.btnAdd);

        image.setImageResource(product.imageRes);
        image.setContentDescription(product.name);

        badge.setText(product.badge);
        badge.setVisibility(product.hasBadge() ? View.VISIBLE : View.GONE);

        rating.setText(String.format(Locale.getDefault(), "%.1f", product.rating));
        category.setText(product.category.toUpperCase(new Locale("es", "CO")));
        name.setText(product.name);
        price.setText(Money.format(product.price));

        bindFavoriteButton(favorite, favoriteIds.contains(product.id), product.name);
        favorite.setOnClickListener(v -> {
            boolean isFavorite = toggleFavorite(product);
            bindFavoriteButton(favorite, isFavorite, product.name);
            if (favoritesOnly) {
                applyFilters();
            }
        });

        add.setContentDescription(getString(R.string.add_product, product.name));
        add.setOnClickListener(v -> {
            addToCart(product, 1);
            showAddedSnackbar();
        });

        cardRoot.setOnClickListener(v -> showProductDetail(product));
        return card;
    }

    private void bindFavoriteButton(ImageButton button, boolean isFavorite, String productName) {
        button.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        button.setColorFilter(ContextCompat.getColor(this,
                isFavorite ? R.color.primary : R.color.ink));
        button.setContentDescription(getString(isFavorite
                ? R.string.unfavorite_product
                : R.string.favorite_product, productName));
    }

    private boolean toggleFavorite(Product product) {
        boolean isFavorite;
        if (favoriteIds.contains(product.id)) {
            favoriteIds.remove(product.id);
            isFavorite = false;
        } else {
            favoriteIds.add(product.id);
            isFavorite = true;
        }
        saveFavorites();
        return isFavorite;
    }

    private void showProductDetail(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_product, null);
        dialog.setContentView(sheet);

        ImageView image = sheet.findViewById(R.id.bsImage);
        ImageButton close = sheet.findViewById(R.id.btnSheetClose);
        ImageButton favorite = sheet.findViewById(R.id.btnSheetFavorite);
        TextView badge = sheet.findViewById(R.id.tvSheetBadge);
        TextView category = sheet.findViewById(R.id.tvDetailCategory);
        TextView name = sheet.findViewById(R.id.tvDetailName);
        TextView price = sheet.findViewById(R.id.tvDetailPrice);
        TextView rating = sheet.findViewById(R.id.tvDetailRating);
        TextView description = sheet.findViewById(R.id.tvDetailDescription);
        LinearLayout sizes = sheet.findViewById(R.id.sizesContainer);
        LinearLayout colors = sheet.findViewById(R.id.colorsContainer);
        TextView quantityLabel = sheet.findViewById(R.id.tvSheetQuantity);
        ImageButton minus = sheet.findViewById(R.id.btnSheetMinus);
        ImageButton plus = sheet.findViewById(R.id.btnSheetPlus);
        MaterialButton addToCart = sheet.findViewById(R.id.btnAddToCart);

        image.setImageResource(product.imageRes);
        image.setContentDescription(product.name);

        badge.setText(product.badge);
        badge.setVisibility(product.hasBadge() ? View.VISIBLE : View.GONE);
        category.setText(product.category.toUpperCase(new Locale("es", "CO")));
        name.setText(product.name);
        price.setText(Money.format(product.price));
        rating.setText(String.format(Locale.getDefault(), "%.1f", product.rating));
        description.setText(product.description);

        bindFavoriteButton(favorite, favoriteIds.contains(product.id), product.name);
        favorite.setOnClickListener(v -> {
            boolean isFavorite = toggleFavorite(product);
            bindFavoriteButton(favorite, isFavorite, product.name);
        });

        String[] selectedSize = new String[]{product.sizes[Math.min(1, product.sizes.length - 1)]};
        buildSizeOptions(sizes, product, selectedSize);
        buildColorOptions(colors, product);

        int[] quantity = {1};
        Runnable updateQuantity = () -> {
            quantityLabel.setText(String.valueOf(quantity[0]));
            minus.setEnabled(quantity[0] > 1);
            minus.setAlpha(quantity[0] > 1 ? 1f : 0.35f);
            plus.setEnabled(quantity[0] < MAX_QUANTITY);
            plus.setAlpha(quantity[0] < MAX_QUANTITY ? 1f : 0.35f);
        };
        updateQuantity.run();

        minus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                updateQuantity.run();
            }
        });
        plus.setOnClickListener(v -> {
            if (quantity[0] < MAX_QUANTITY) {
                quantity[0]++;
                updateQuantity.run();
            }
        });

        addToCart.setOnClickListener(v -> {
            addToCart(product, quantity[0]);
            dialog.dismiss();
            showAddedSnackbar();
        });
        close.setOnClickListener(v -> dialog.dismiss());

        styleSheet(dialog, sheet);
        dialog.show();
    }

    private void buildSizeOptions(LinearLayout container, Product product, String[] selectedSize) {
        container.removeAllViews();
        int defaultIndex = Math.min(1, product.sizes.length - 1);

        for (int i = 0; i < product.sizes.length; i++) {
            String size = product.sizes[i];
            TextView option = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(46), dp(42));
            params.setMarginEnd(dp(8));
            option.setLayoutParams(params);
            option.setGravity(Gravity.CENTER);
            option.setText(size);
            option.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            option.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            option.setIncludeFontPadding(false);
            option.setBackgroundResource(R.drawable.bg_size_selector);
            option.setTextColor(ContextCompat.getColorStateList(this, R.color.size_text));
            option.setContentDescription(getString(R.string.size) + " " + size);
            option.setClickable(true);
            option.setFocusable(true);
            option.setSelected(i == defaultIndex);
            option.setOnClickListener(v -> {
                for (int childIndex = 0; childIndex < container.getChildCount(); childIndex++) {
                    container.getChildAt(childIndex).setSelected(container.getChildAt(childIndex) == v);
                }
                selectedSize[0] = size;
            });
            container.addView(option);
        }
    }

    private void buildColorOptions(LinearLayout container, Product product) {
        container.removeAllViews();

        for (int i = 0; i < product.swatches.length; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(28), dp(28));
            params.setMarginEnd(dp(10));
            dot.setLayoutParams(params);
            dot.setTag(product.swatches[i]);
            dot.setContentDescription("Color " + (i + 1));
            dot.setClickable(true);
            dot.setFocusable(true);
            dot.setOnClickListener(v -> updateColorDots(container, container.indexOfChild(v)));
            container.addView(dot);
        }

        updateColorDots(container, 0);
    }

    private void updateColorDots(LinearLayout container, int selectedIndex) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View dot = container.getChildAt(i);
            int color = (int) dot.getTag();
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            if (i == selectedIndex) {
                drawable.setStroke(dp(3), ContextCompat.getColor(this, R.color.ink));
            }
            dot.setBackground(drawable);
        }
    }

    private void showAddedSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.root),
                R.string.added_to_cart, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.view_cart, v -> showCart());
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.mustard));
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.snackbar));
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }

    private void showCart() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_cart, null);
        dialog.setContentView(sheet);

        LinearLayout items = sheet.findViewById(R.id.cartItemsContainer);
        LinearLayout footer = sheet.findViewById(R.id.cartFooter);
        LinearLayout empty = sheet.findViewById(R.id.cartEmptyState);
        TextView summary = sheet.findViewById(R.id.tvCartSummary);
        TextView total = sheet.findViewById(R.id.tvCartTotal);
        ImageButton close = sheet.findViewById(R.id.btnCartClose);
        TextView explore = sheet.findViewById(R.id.btnCartExplore);
        MaterialButton checkout = sheet.findViewById(R.id.btnCheckout);

        close.setOnClickListener(v -> dialog.dismiss());
        explore.setOnClickListener(v -> dialog.dismiss());
        checkout.setOnClickListener(v -> showDemoDialog());

        renderCart(dialog, items, footer, empty, summary, total);
        styleSheet(dialog, sheet);
        dialog.show();
    }

    private void renderCart(BottomSheetDialog dialog, LinearLayout items, LinearLayout footer,
                            LinearLayout empty, TextView summary, TextView total) {
        items.removeAllViews();
        int count = cartCount();

        summary.setText(count == 1
                ? getString(R.string.cart_summary_one)
                : getString(R.string.cart_summary, count));

        if (cart.isEmpty()) {
            items.setVisibility(View.GONE);
            footer.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            return;
        }

        items.setVisibility(View.VISIBLE);
        footer.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        int totalValue = 0;
        for (Map.Entry<Integer, Integer> entry : new ArrayList<>(cart.entrySet())) {
            Product product = Catalog.byId(entry.getKey());
            if (product == null) {
                continue;
            }

            int quantity = entry.getValue();
            totalValue += product.price * quantity;

            View row = getLayoutInflater().inflate(R.layout.item_cart, items, false);
            ImageView image = row.findViewById(R.id.ivCartImage);
            TextView name = row.findViewById(R.id.tvCartName);
            TextView category = row.findViewById(R.id.tvCartCategory);
            TextView price = row.findViewById(R.id.tvCartPrice);
            TextView quantityView = row.findViewById(R.id.tvCartQuantity);
            ImageButton minus = row.findViewById(R.id.btnCartMinus);
            ImageButton plus = row.findViewById(R.id.btnCartPlus);

            image.setImageResource(product.imageRes);
            image.setContentDescription(product.name);
            name.setText(product.name);
            category.setText(product.category.toUpperCase(new Locale("es", "CO")));
            price.setText(Money.format(product.price * quantity));
            quantityView.setText(String.valueOf(quantity));

            minus.setContentDescription(getString(R.string.decrease) + ": " + product.name);
            plus.setContentDescription(getString(R.string.increase) + ": " + product.name);

            Runnable refresh = () -> {
                saveCart();
                updateCartBadge();
                renderCart(dialog, items, footer, empty, summary, total);
            };

            minus.setOnClickListener(v -> {
                int current = cart.getOrDefault(product.id, 0);
                if (current <= 1) {
                    cart.remove(product.id);
                } else {
                    cart.put(product.id, current - 1);
                }
                refresh.run();
            });

            plus.setOnClickListener(v -> {
                int current = cart.getOrDefault(product.id, 0);
                cart.put(product.id, Math.min(MAX_QUANTITY, current + 1));
                refresh.run();
            });

            items.addView(row);
        }

        total.setText(Money.format(totalValue));
    }

    private void showDemoDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.demo_title)
                .setMessage(R.string.demo_message)
                .setPositiveButton(R.string.demo_ok, null)
                .show();
    }

    private void styleSheet(BottomSheetDialog dialog, View sheet) {
        dialog.setOnShowListener(ignored -> {
            FrameLayout bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }

            ViewCompat.setOnApplyWindowInsetsListener(sheet, (view, insets) -> {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                                | WindowInsetsCompat.Type.displayCutout());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                        view.getPaddingRight(), bars.bottom + dp(6));
                return insets;
            });
            ViewCompat.requestApplyInsets(sheet);
        });
    }

    private void addToCart(Product product, int quantity) {
        int current = cart.getOrDefault(product.id, 0);
        cart.put(product.id, Math.min(99, current + quantity));
        saveCart();
        updateCartBadge();
    }

    private int cartCount() {
        int count = 0;
        for (int quantity : cart.values()) {
            count += quantity;
        }
        return count;
    }

    private void updateCartBadge() {
        int count = cartCount();
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            btnCart.setContentDescription(getString(R.string.cart) + " (" + count + ")");
        } else {
            tvCartBadge.setVisibility(View.GONE);
            btnCart.setContentDescription(getString(R.string.cart));
        }
    }

    private void loadSavedState() {
        favoriteIds.clear();
        String savedFavorites = preferences.getString(KEY_FAVORITES, "");
        if (!savedFavorites.isEmpty()) {
            for (String value : savedFavorites.split(",")) {
                try {
                    favoriteIds.add(Integer.parseInt(value));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        cart.clear();
        String savedCart = preferences.getString(KEY_CART, "");
        if (!savedCart.isEmpty()) {
            for (String entry : savedCart.split(";")) {
                String[] parts = entry.split(":");
                if (parts.length != 2) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(parts[0]);
                    int quantity = Integer.parseInt(parts[1]);
                    if (Catalog.byId(id) != null && quantity > 0) {
                        cart.put(id, Math.min(MAX_QUANTITY, quantity));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private void saveFavorites() {
        StringBuilder builder = new StringBuilder();
        for (Integer id : favoriteIds) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        preferences.edit().putString(KEY_FAVORITES, builder.toString()).apply();
    }

    private void saveCart() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append(':').append(entry.getValue());
        }
        preferences.edit().putString(KEY_CART, builder.toString()).apply();
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused == null) {
            return;
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
        focused.clearFocus();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
