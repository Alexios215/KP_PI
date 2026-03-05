package com.example.kp_pi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView ordersListView;
    private Button backCatalog;
    private String currentUsername;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        getSupportActionBar().hide();

        // Получаем данные о пользователе
        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        isAdmin = intent.getBooleanExtra("isAdmin", false);

        dbHelper = new DBHelper(this);
        ordersListView = findViewById(R.id.orders_list_view);
        backCatalog = findViewById(R.id.backCatalog);

        backCatalog.setOnClickListener(v -> {
            Intent catalogIntent = new Intent(OrdersActivity.this, CatalogActivity.class);
            catalogIntent.putExtra("username", currentUsername);
            catalogIntent.putExtra("isAdmin", isAdmin);
            startActivity(catalogIntent);
        });

        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders;

        // Если администратор - показываем все заказы
        if (isAdmin) {
            orders = dbHelper.getAllOrders();
        } else {
            // Если клиент - показываем только его заказы
            orders = dbHelper.getOrdersByUsername(currentUsername);
        }

        if (orders.isEmpty()) {
            Toast.makeText(this, "Нет заказов", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] orderStrings = new String[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            orderStrings[i] = String.format("Заявка #%d\nКлиент: %s\nАвто: %s\nСумма: %.2f руб.\nСтатус: %s",
                    order.getId(),
                    order.getCustomerName(),
                    order.getCarModel(),
                    order.getTotalAmount(),
                    order.getStatus());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, orderStrings);
        ordersListView.setAdapter(adapter);
    }
}