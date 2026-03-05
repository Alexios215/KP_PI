package com.example.kp_pi;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CatalogActivity extends AppCompatActivity implements ServiceAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<Service> serviceList;
    private List<Service> cartItems;
    private DBHelper dbHelper;

    private TextView cartCountText;
    private Button checkoutButton;
    private Button addServiceButton;
    private Button viewCartButton;
    private Button fabOrders;

    private AlertDialog orderDialog;
    private EditText customerNameEditText;
    private EditText customerPhoneEditText;
    private EditText carModelEditText;
    private EditText carNumberEditText;
    private TextView orderDateTextView;
    private Calendar selectedDate;

    private String currentUsername;
    private boolean isAdmin;

    private AlertDialog editServiceDialog;
    private EditText editNameEditText;
    private EditText editDescriptionEditText;
    private EditText editPriceEditText;
    private EditText editDurationEditText;
    private EditText editCategoryEditText;
    private Service currentEditingService;

    private static final String PREF_NAME = "cart_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        getSupportActionBar().hide();

        // Получаем данные о пользователе
        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        isAdmin = intent.getBooleanExtra("isAdmin", false);

        // Отладка
        Log.d("CatalogActivity", "currentUsername = " + currentUsername);
        Log.d("CatalogActivity", "isAdmin = " + isAdmin);

        // Инициализация
        dbHelper = new DBHelper(this);
        cartItems = new ArrayList<>();
        serviceList = new ArrayList<>();

        // Находим элементы интерфейса
        recyclerView = findViewById(R.id.recycler_view);
        cartCountText = findViewById(R.id.cart_count_text);
        checkoutButton = findViewById(R.id.checkout_button);
        addServiceButton = findViewById(R.id.add_service_button);
        viewCartButton = findViewById(R.id.view_cart_button);
        fabOrders = findViewById(R.id.fab_orders);

        // Устанавливаем текст на кнопке заказов в зависимости от типа пользователя
        if (isAdmin) {
            fabOrders.setText("Все заказы");
        } else {
            fabOrders.setText("Мои заказы");
        }

        // Скрываем кнопку добавления услуги для обычных клиентов
        if (!isAdmin) {
            addServiceButton.setVisibility(View.GONE);
        }

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(serviceList, this, this, isAdmin);
        recyclerView.setAdapter(adapter);

        // Загрузка услуг
        loadServices();

        // Загрузка корзины для текущего пользователя
        loadCartFromPrefs();

        // Обновление счетчика корзины
        updateCartCount();

        // Обработчики кликов
        checkoutButton.setOnClickListener(v -> showOrderDialog());
        addServiceButton.setOnClickListener(v -> showAddServiceDialog());
        viewCartButton.setOnClickListener(v -> showCartDialog());

        fabOrders.setOnClickListener(v -> {
            // Сохраняем корзину перед переходом
            saveCartToPrefs();

            // Переход к истории заказов
            Intent ordersIntent = new Intent(CatalogActivity.this, OrdersActivity.class);
            ordersIntent.putExtra("username", currentUsername);
            ordersIntent.putExtra("isAdmin", isAdmin);

            // Отладка
            Log.d("CatalogActivity", "Передаем в OrdersActivity: username=" + currentUsername + ", isAdmin=" + isAdmin);

            startActivity(ordersIntent);
        });
    }

    // Загрузка списка услуг из БД

    private void loadServices() {
        serviceList.clear();
        if (isAdmin) {
            // Админ видит все услуги (и активные, и неактивные)
            serviceList.addAll(dbHelper.getAllServices());
        } else {
            // Клиент видит только активные услуги
            serviceList.addAll(dbHelper.getActiveServices());
        }
        adapter.notifyDataSetChanged();
    }

    // Обновление счетчика корзины

    private void updateCartCount() {
        cartCountText.setText(String.format("В корзине: %d", cartItems.size()));
    }

    // Сохранение корзины в SharedPreferences с привязкой к пользователю

    private void saveCartToPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Создаем уникальный ключ для каждого пользователя
        String userCartKey = "cart_items_" + currentUsername;

        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < cartItems.size(); i++) {
            if (i > 0) ids.append(",");
            ids.append(cartItems.get(i).getId());
        }
        editor.putString(userCartKey, ids.toString());
        editor.apply();
    }

    // Загрузка корзины из SharedPreferences для конкретного пользователя

    private void loadCartFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Загружаем корзину только для текущего пользователя
        String userCartKey = "cart_items_" + currentUsername;
        String idsStr = prefs.getString(userCartKey, "");

        cartItems.clear();
        if (!idsStr.isEmpty()) {
            String[] idArray = idsStr.split(",");
            for (String idStr : idArray) {
                try {
                    int serviceId = Integer.parseInt(idStr);
                    // Ищем услугу в загруженном списке
                    for (Service service : serviceList) {
                        if (service.getId() == serviceId) {
                            cartItems.add(service);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        updateCartCount();
    }

    // Очистка корзины пользователя

    private void clearUserCart() {
        cartItems.clear();
        updateCartCount();

        // Очищаем сохраненную корзину в SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String userCartKey = "cart_items_" + currentUsername;
        editor.remove(userCartKey);
        editor.apply();
    }

    // Обработчики кликов из адаптера

    @Override
    public void onItemClick(Service service) {
        showServiceDetailsDialog(service);
    }

    @Override
    public void onAddToCartClick(Service service) {
        cartItems.add(service);
        updateCartCount();
        saveCartToPrefs();
        Toast.makeText(this, "Услуга добавлена в корзину", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Service service) {
        if (!isAdmin) {
            Toast.makeText(this, "Только администратор может редактировать услуги", Toast.LENGTH_SHORT).show();
            return;
        }
        showEditServiceDialog(service);
    }

    @Override
    public void onDeleteClick(Service service) {
        if (!isAdmin) {
            Toast.makeText(this, "Только администратор может деактивировать услуги", Toast.LENGTH_SHORT).show();
            return;
        }

        if (service.isActive()) {
            showDeactivateConfirmationDialog(service);
        }
    }

    @Override
    public void onActivateClick(Service service) {
        if (!isAdmin) {
            Toast.makeText(this, "Только администратор может активировать услуги", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!service.isActive()) {
            showActivateConfirmationDialog(service);
        }
    }

    // Диалоги

    private void showServiceDetailsDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(service.getName());

        String message = String.format(Locale.getDefault(),
                "Описание: %s\n\n" +
                        "Цена: %.2f руб.\n" +
                        "Длительность: %d мин.\n" +
                        "Категория: %s\n" +
                        "Статус: %s",
                service.getDescription(),
                service.getPrice(),
                service.getDuration(),
                service.getCategory(),
                service.isActive() ? "Активна" : "Неактивна");

        builder.setMessage(message);

        if (service.isActive()) {
            builder.setPositiveButton("Добавить в корзину", (dialog, which) -> {
                cartItems.add(service);
                updateCartCount();
                saveCartToPrefs();
                Toast.makeText(CatalogActivity.this, "Услуга добавлена в корзину", Toast.LENGTH_SHORT).show();
            });
        }

        builder.setNegativeButton("Закрыть", null);
        builder.show();
    }

    private void showEditServiceDialog(Service service) {
        currentEditingService = service;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_service, null);
        builder.setView(dialogView);

        editNameEditText = dialogView.findViewById(R.id.service_name);
        editDescriptionEditText = dialogView.findViewById(R.id.service_description);
        editPriceEditText = dialogView.findViewById(R.id.service_price);
        editDurationEditText = dialogView.findViewById(R.id.service_duration);
        editCategoryEditText = dialogView.findViewById(R.id.service_category);

        editNameEditText.setText(service.getName());
        editDescriptionEditText.setText(service.getDescription());
        editPriceEditText.setText(String.valueOf(service.getPrice()));
        editDurationEditText.setText(String.valueOf(service.getDuration()));
        editCategoryEditText.setText(service.getCategory());

        builder.setTitle("Редактирование услуги");
        builder.setPositiveButton("Сохранить", null);
        builder.setNegativeButton("Отмена", null);

        editServiceDialog = builder.create();
        editServiceDialog.show();

        editServiceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateEditForm()) {
                updateService();
                editServiceDialog.dismiss();
            }
        });
    }

    private boolean validateEditForm() {
        if (TextUtils.isEmpty(editNameEditText.getText().toString())) {
            editNameEditText.setError("Введите название");
            return false;
        }

        String priceStr = editPriceEditText.getText().toString();
        if (TextUtils.isEmpty(priceStr)) {
            editPriceEditText.setError("Введите цену");
            return false;
        }

        String durationStr = editDurationEditText.getText().toString();
        if (TextUtils.isEmpty(durationStr)) {
            editDurationEditText.setError("Введите длительность");
            return false;
        }

        try {
            Double.parseDouble(priceStr);
            Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректные числовые значения", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateService() {
        currentEditingService.setName(editNameEditText.getText().toString());
        currentEditingService.setDescription(editDescriptionEditText.getText().toString());
        currentEditingService.setPrice(Double.parseDouble(editPriceEditText.getText().toString()));
        currentEditingService.setDuration(Integer.parseInt(editDurationEditText.getText().toString()));
        currentEditingService.setCategory(editCategoryEditText.getText().toString());

        boolean updated = dbHelper.updateService(currentEditingService);
        if (updated) {
            Toast.makeText(this, "Услуга обновлена", Toast.LENGTH_SHORT).show();
            loadServices();
        } else {
            Toast.makeText(this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeactivateConfirmationDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Деактивация услуги");
        builder.setMessage("Вы уверены, что хотите деактивировать услугу \"" + service.getName() + "\"?\n\n" +
                "Она не будет отображаться для клиентов, но останется в истории заказов.");

        builder.setPositiveButton("Деактивировать", (dialog, which) -> {
            boolean deactivated = dbHelper.deactivateService(service.getId());
            if (deactivated) {
                Toast.makeText(this, "Услуга деактивирована", Toast.LENGTH_SHORT).show();
                loadServices();

                boolean removed = false;
                for (int i = cartItems.size() - 1; i >= 0; i--) {
                    if (cartItems.get(i).getId() == service.getId()) {
                        cartItems.remove(i);
                        removed = true;
                    }
                }
                if (removed) {
                    updateCartCount();
                    saveCartToPrefs();
                }
            } else {
                Toast.makeText(this, "Ошибка при деактивации", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showActivateConfirmationDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Активация услуги");
        builder.setMessage("Вы уверены, что хотите активировать услугу \"" + service.getName() + "\"?\n\n" +
                "Она снова будет отображаться для клиентов.");

        builder.setPositiveButton("Активировать", (dialog, which) -> {
            boolean activated = dbHelper.activateService(service.getId());
            if (activated) {
                Toast.makeText(this, "Услуга активирована", Toast.LENGTH_SHORT).show();
                loadServices();
            } else {
                Toast.makeText(this, "Ошибка при активации", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showAddServiceDialog() {
        if (!isAdmin) {
            Toast.makeText(this, "Только администратор может добавлять услуги", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_service, null);
        builder.setView(dialogView);

        final EditText nameEditText = dialogView.findViewById(R.id.service_name);
        final EditText descriptionEditText = dialogView.findViewById(R.id.service_description);
        final EditText priceEditText = dialogView.findViewById(R.id.service_price);
        final EditText durationEditText = dialogView.findViewById(R.id.service_duration);
        final EditText categoryEditText = dialogView.findViewById(R.id.service_category);

        builder.setTitle("Добавление новой услуги");
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String priceStr = priceEditText.getText().toString();
            String durationStr = durationEditText.getText().toString();
            String category = categoryEditText.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(durationStr)) {
                Toast.makeText(CatalogActivity.this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int duration = Integer.parseInt(durationStr);

                long id = dbHelper.addService(name, description, price, duration, category);

                if (id != -1) {
                    Toast.makeText(CatalogActivity.this, "Услуга добавлена", Toast.LENGTH_SHORT).show();
                    loadServices();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(CatalogActivity.this, "Некорректные числовые значения", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showCartDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Корзина");

        StringBuilder message = new StringBuilder();
        double total = 0;
        int totalDuration = 0;

        for (Service service : cartItems) {
            message.append("• ").append(service.getName())
                    .append(" - ").append(service.getPrice()).append(" руб.\n");
            total += service.getPrice();
            totalDuration += service.getDuration();
        }

        message.append("\nИтого: ").append(total).append(" руб.\n");
        message.append("Общее время: ").append(totalDuration).append(" мин.");

        builder.setMessage(message.toString());

        builder.setPositiveButton("Оформить заказ", (dialog, which) -> showOrderDialog());
        builder.setNegativeButton("Очистить корзину", (dialog, which) -> {
            clearUserCart();
            Toast.makeText(CatalogActivity.this, "Корзина очищена", Toast.LENGTH_SHORT).show();
        });
        builder.setNeutralButton("Вернуться", null);

        builder.show();
    }

    private void showOrderDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Добавьте услуги в корзину", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order, null);
        builder.setView(dialogView);

        customerNameEditText = dialogView.findViewById(R.id.customer_name);
        customerPhoneEditText = dialogView.findViewById(R.id.customer_phone);
        carModelEditText = dialogView.findViewById(R.id.car_model);
        carNumberEditText = dialogView.findViewById(R.id.car_number);
        orderDateTextView = dialogView.findViewById(R.id.order_date);
        Button datePickerButton = dialogView.findViewById(R.id.date_picker_button);

        TextView orderSummary = dialogView.findViewById(R.id.order_summary);
        TextView orderDetails = dialogView.findViewById(R.id.order_details);
        TextView orderTotal = dialogView.findViewById(R.id.order_total);

        customerNameEditText.setText(currentUsername);

        double total = 0;
        StringBuilder details = new StringBuilder();
        for (Service service : cartItems) {
            total += service.getPrice();
            details.append("• ").append(service.getName())
                    .append(" - ").append(service.getPrice()).append(" руб.\n");
        }

        orderSummary.setText(String.format("Итого: %.2f руб.", total));
        orderDetails.setText(details.toString().isEmpty() ?
                "Услуги не выбраны" : details.toString());

        TextView orderTotalTextView = dialogView.findViewById(R.id.order_total);
        orderTotalTextView.setText(String.format("Итого: %.2f руб.", total));

        selectedDate = Calendar.getInstance();
        updateDateTextView();

        datePickerButton.setOnClickListener(v -> showDatePicker());

        builder.setTitle("Оформление заявки");
        builder.setPositiveButton("Оформить", null);
        builder.setNegativeButton("Отмена", null);

        orderDialog = builder.create();
        orderDialog.show();

        orderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateOrderForm()) {
                createOrder();
                orderDialog.dismiss();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateTextView();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateTextView() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        orderDateTextView.setText(sdf.format(selectedDate.getTime()));
    }

    private boolean validateOrderForm() {
        if (TextUtils.isEmpty(customerNameEditText.getText().toString())) {
            customerNameEditText.setError("Введите имя");
            return false;
        }

        if (TextUtils.isEmpty(customerPhoneEditText.getText().toString())) {
            customerPhoneEditText.setError("Введите телефон");
            return false;
        }

        if (TextUtils.isEmpty(carModelEditText.getText().toString())) {
            carModelEditText.setError("Введите модель авто");
            return false;
        }

        return true;
    }

    private void createOrder() {
        Order order = new Order();
        order.setCustomerName(customerNameEditText.getText().toString());
        order.setCustomerPhone(customerPhoneEditText.getText().toString());
        order.setCarModel(carModelEditText.getText().toString());
        order.setCarNumber(carNumberEditText.getText().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        order.setDate(sdf.format(selectedDate.getTime()));
        order.setOrderDate(sdf.format(new Date()));

        order.setStatus("Новый");

        double total = 0;
        for (Service service : cartItems) {
            total += service.getPrice();
        }
        order.setTotalAmount(total);

        long orderId = dbHelper.createOrder(
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCarModel(),
                order.getCarNumber(),
                order.getOrderDate(),
                order.getDate(),
                order.getTotalAmount()
        );

        if (orderId != -1) {
            for (Service service : cartItems) {
                dbHelper.addOrderItem(orderId, service.getId(), 1, service.getPrice());
            }

            Toast.makeText(this, "Заявка №" + orderId + " оформлена!", Toast.LENGTH_LONG).show();
            clearUserCart();
        } else {
            Toast.makeText(this, "Ошибка при сохранении заявки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCartToPrefs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices();
        loadCartFromPrefs();
        updateCartCount();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}