package com.example.kp_pi;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    private AlertDialog orderDialog;
    private EditText customerNameEditText;
    private EditText customerPhoneEditText;
    private EditText carModelEditText;
    private EditText carNumberEditText;
    private TextView orderDateTextView;
    private Calendar selectedDate;

    private String currentUsername;
    private boolean isAdmin;

    // Диалог для редактирования услуги
    private AlertDialog editServiceDialog;
    private EditText editNameEditText;
    private EditText editDescriptionEditText;
    private EditText editPriceEditText;
    private EditText editDurationEditText;
    private EditText editCategoryEditText;
    private Service currentEditingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        isAdmin = intent.getBooleanExtra("isAdmin", false);

        dbHelper = new DBHelper(this);
        cartItems = new ArrayList<>();
        serviceList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        cartCountText = findViewById(R.id.cart_count_text);
        checkoutButton = findViewById(R.id.checkout_button);
        addServiceButton = findViewById(R.id.add_service_button);
        viewCartButton = findViewById(R.id.view_cart_button);

        if (!isAdmin) {
            addServiceButton.setVisibility(View.GONE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(serviceList, this, this, isAdmin);
        recyclerView.setAdapter(adapter);

        loadServices();
        updateCartCount();

        checkoutButton.setOnClickListener(v -> showOrderDialog());
        addServiceButton.setOnClickListener(v -> showAddServiceDialog());
        viewCartButton.setOnClickListener(v -> showCartDialog());

        Button fabOrders = findViewById(R.id.fab_orders);
        fabOrders.setOnClickListener(v -> {
            Intent ordersIntent = new Intent(CatalogActivity.this, OrdersActivity.class);
            ordersIntent.putExtra("username", currentUsername);
            ordersIntent.putExtra("isAdmin", isAdmin);
            startActivity(ordersIntent);
        });
    }

    private void loadServices() {
        serviceList.clear();
        serviceList.addAll(dbHelper.getAllServices());
        adapter.notifyDataSetChanged();
    }

    private void updateCartCount() {
        cartCountText.setText(String.format("В корзине: %d", cartItems.size()));
    }

    @Override
    public void onItemClick(Service service) {
        showServiceDetailsDialog(service);
    }

    @Override
    public void onAddToCartClick(Service service) {
        cartItems.add(service);
        updateCartCount();
        Toast.makeText(this, "Услуга добавлена в корзину", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Service service) {
        // Только для администратора
        if (!isAdmin) {
            Toast.makeText(this, "Только администратор может редактировать услуги", Toast.LENGTH_SHORT).show();
            return;
        }
        showEditServiceDialog(service);
    }

    @Override
    public void onDeleteClick(Service service) {
        // Только для администратора
        if (!isAdmin) {
            Toast.makeText(this, "Только администратор может удалять услуги", Toast.LENGTH_SHORT).show();
            return;
        }
        showDeleteConfirmationDialog(service);
    }

    private void showDeleteConfirmationDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление услуги");
        builder.setMessage("Вы уверены, что хотите удалить услугу \"" + service.getName() + "\"?");

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            boolean deleted = dbHelper.deleteService(service.getId());
            if (deleted) {
                Toast.makeText(this, "Услуга удалена", Toast.LENGTH_SHORT).show();
                loadServices(); // Обновляем список
            } else {
                Toast.makeText(this, "Нельзя удалить услугу, которая есть в заказах", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
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

        // Заполняем существующие данные
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

        // Обработчик сохранения
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
            loadServices(); // Обновляем список
        } else {
            Toast.makeText(this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
        }
    }

    private void showServiceDetailsDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(service.getName());

        String message = String.format(Locale.getDefault(),
                "Описание: %s\n\n" +
                        "Цена: %.2f руб.\n" +
                        "Длительность: %d мин.\n" +
                        "Категория: %s",
                service.getDescription(),
                service.getPrice(),
                service.getDuration(),
                service.getCategory());

        builder.setMessage(message);
        builder.setPositiveButton("Добавить в корзину", (dialog, which) -> {
            cartItems.add(service);
            updateCartCount();
            Toast.makeText(CatalogActivity.this, "Услуга добавлена в корзину", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Закрыть", null);
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
            cartItems.clear();
            updateCartCount();
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
        orderTotal.setText(String.format("%.2f руб.", total));
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
            cartItems.clear();
            updateCartCount();
        } else {
            Toast.makeText(this, "Ошибка при сохранении заявки", Toast.LENGTH_SHORT).show();
        }
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

                Service service = new Service(name, description, price, duration, category);
                long id = dbHelper.addService(service.getName(), service.getDescription(), service.getPrice(), service.getDuration(), service.getCategory());

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

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}