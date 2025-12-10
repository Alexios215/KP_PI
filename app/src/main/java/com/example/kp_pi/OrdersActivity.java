package com.example.kp_pi;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView ordersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        dbHelper = new DBHelper(this);
        ordersListView = findViewById(R.id.orders_list_view);

        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = dbHelper.getAllOrders();

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