package com.example.kp_pi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";
    private DBHelper dbHelper;
    private ListView ordersListView;
    private Button backCatalog;
    private TextView titleTextView;  // Добавляем TextView для заголовка
    private String currentUsername;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        getSupportActionBar().hide();

        // Получаем данные из Intent
        Intent intent = getIntent();

        // Отладка
        Log.d(TAG, "=== OrdersActivity onCreate ===");

        if (intent != null) {
            currentUsername = intent.getStringExtra("username");
            isAdmin = intent.getBooleanExtra("isAdmin", false);

            Log.d(TAG, "Получен username: '" + currentUsername + "'");
            Log.d(TAG, "Получен isAdmin: " + isAdmin);
        }

        // Инициализация
        dbHelper = new DBHelper(this);
        ordersListView = findViewById(R.id.orders_list_view);
        backCatalog = findViewById(R.id.backCatalog);
        titleTextView = findViewById(R.id.orders_title); // Теперь этот ID существует в разметке

        // Устанавливаем заголовок
        if (titleTextView != null) {
            if (isAdmin) {
                titleTextView.setText("Все заказы");
                Log.d(TAG, "Установлен заголовок: Все заказы");
            } else {
                titleTextView.setText("Мои заказы");
                Log.d(TAG, "Установлен заголовок: Мои заказы");
            }
        } else {
            Log.e(TAG, "titleTextView = null! Проверьте разметку activity_orders.xml");
        }

        backCatalog.setOnClickListener(v -> {
            Log.d(TAG, "Нажата кнопка возврата");
            Intent catalogIntent = new Intent(OrdersActivity.this, CatalogActivity.class);
            catalogIntent.putExtra("username", currentUsername);
            catalogIntent.putExtra("isAdmin", isAdmin);
            startActivity(catalogIntent);
            finish();
        });

        loadOrders();
    }

    private void loadOrders() {
        Log.d(TAG, "Загрузка заказов для пользователя: " + currentUsername + ", isAdmin: " + isAdmin);

        List<Order> orders;

        if (isAdmin) {
            Log.d(TAG, "Загружаем ВСЕ заказы");
            orders = dbHelper.getAllOrders();
        } else {
            Log.d(TAG, "Загружаем заказы только для пользователя: " + currentUsername);
            orders = dbHelper.getOrdersByUsername(currentUsername);
        }

        if (orders == null || orders.isEmpty()) {
            Log.d(TAG, "Заказы не найдены");
            Toast.makeText(this, "Нет заказов", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Найдено заказов: " + orders.size());

        String[] orderStrings = new String[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);

            StringBuilder itemsStr = new StringBuilder();
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    itemsStr.append("\n    • ").append(item.getServiceName());
                }
            }

            orderStrings[i] = String.format("Заявка #%d\nКлиент: %s\nАвто: %s\nСумма: %.2f руб.\nСтатус: %s\nУслуги:%s",
                    order.getId(),
                    order.getCustomerName(),
                    order.getCarModel(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    itemsStr.toString());

            Log.d(TAG, "Заказ #" + order.getId() + " от клиента: " + order.getCustomerName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, orderStrings);
        ordersListView.setAdapter(adapter);
    }
}