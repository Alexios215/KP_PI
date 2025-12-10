package com.example.kp_pi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    // Версия базы данных увеличена для добавления новых таблиц
    public static final int DATABASE_VERSION = 2; // Изменено с 1 на 2
    public static final String DATABASE_NAME = "autoservice.db"; // Изменено имя базы

    // Таблица пользователей (уже есть)
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_TYPE = "user_type"; // Новая колонка

    // Новые таблицы для услуг
    public static final String TABLE_SERVICES = "services";
    public static final String COLUMN_SERVICE_ID = "service_id";
    public static final String COLUMN_SERVICE_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CATEGORY = "category";

    // Новые таблицы для заказов
    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_ID = "order_id";
    public static final String COLUMN_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_CUSTOMER_PHONE = "customer_phone";
    public static final String COLUMN_CAR_MODEL = "car_model";
    public static final String COLUMN_CAR_NUMBER = "car_number";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_APPOINTMENT_DATE = "appointment_date";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount";

    // Новые таблицы для элементов заказа
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_ORDER_ID_FK = "order_id";
    public static final String COLUMN_SERVICE_ID_FK = "service_id";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_ITEM_PRICE = "item_price";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаем таблицу пользователей (ваш существующий код)
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_USER_TYPE + " TEXT DEFAULT 'client')");

        // Создаем таблицу услуг
        db.execSQL("CREATE TABLE " + TABLE_SERVICES + "(" +
                COLUMN_SERVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SERVICE_NAME + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PRICE + " REAL NOT NULL, " +
                COLUMN_DURATION + " INTEGER NOT NULL, " +
                COLUMN_CATEGORY + " TEXT)");

        // Создаем таблицу заказов
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + "(" +
                COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CUSTOMER_NAME + " TEXT NOT NULL, " +
                COLUMN_CUSTOMER_PHONE + " TEXT NOT NULL, " +
                COLUMN_CAR_MODEL + " TEXT NOT NULL, " +
                COLUMN_CAR_NUMBER + " TEXT, " +
                COLUMN_ORDER_DATE + " TEXT NOT NULL, " +
                COLUMN_APPOINTMENT_DATE + " TEXT, " +
                COLUMN_STATUS + " TEXT DEFAULT 'Новый', " +
                COLUMN_TOTAL_AMOUNT + " REAL NOT NULL)");

        // Создаем таблицу элементов заказа
        db.execSQL("CREATE TABLE " + TABLE_ORDER_ITEMS + "(" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ORDER_ID_FK + " INTEGER, " +
                COLUMN_SERVICE_ID_FK + " INTEGER, " +
                COLUMN_QUANTITY + " INTEGER DEFAULT 1, " +
                COLUMN_ITEM_PRICE + " REAL NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_ORDER_ID_FK + ") REFERENCES " + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "), " +
                "FOREIGN KEY(" + COLUMN_SERVICE_ID_FK + ") REFERENCES " + TABLE_SERVICES + "(" + COLUMN_SERVICE_ID + "))");

        // Добавляем тестовые данные
        addTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Добавляем колонку типа пользователя
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_TYPE + " TEXT DEFAULT 'client'");

            // Создаем новые таблицы
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SERVICES + "(" +
                    COLUMN_SERVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SERVICE_NAME + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_PRICE + " REAL NOT NULL, " +
                    COLUMN_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_CATEGORY + " TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + "(" +
                    COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CUSTOMER_NAME + " TEXT NOT NULL, " +
                    COLUMN_CUSTOMER_PHONE + " TEXT NOT NULL, " +
                    COLUMN_CAR_MODEL + " TEXT NOT NULL, " +
                    COLUMN_CAR_NUMBER + " TEXT, " +
                    COLUMN_ORDER_DATE + " TEXT NOT NULL, " +
                    COLUMN_APPOINTMENT_DATE + " TEXT, " +
                    COLUMN_STATUS + " TEXT DEFAULT 'Новый', " +
                    COLUMN_TOTAL_AMOUNT + " REAL NOT NULL)");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_ITEMS + "(" +
                    COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ORDER_ID_FK + " INTEGER, " +
                    COLUMN_SERVICE_ID_FK + " INTEGER, " +
                    COLUMN_QUANTITY + " INTEGER DEFAULT 1, " +
                    COLUMN_ITEM_PRICE + " REAL NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_ORDER_ID_FK + ") REFERENCES " + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_SERVICE_ID_FK + ") REFERENCES " + TABLE_SERVICES + "(" + COLUMN_SERVICE_ID + "))");

            // Добавляем тестовые данные
            addTestData(db);
        }
    }

    // Ваши существующие методы
    public Boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);
        long res = db.insert(TABLE_USERS, null, cv);
        db.close();
        return res != -1;
    }

    public Boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Новые методы для работы с услугами

    public long addService(String name, String description, double price, int duration, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVICE_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_CATEGORY, category);

        long id = db.insert(TABLE_SERVICES, null, values);
        db.close();
        return id;
    }

    public List<Service> getAllServices() {
        List<Service> serviceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SERVICES + " ORDER BY " + COLUMN_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                Service service = new Service();
                service.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_ID)));
                service.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_NAME)));
                service.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                service.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                service.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)));
                service.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                serviceList.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return serviceList;
    }

    public List<Service> getServicesByCategory(String category) {
        List<Service> serviceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SERVICES + " WHERE " + COLUMN_CATEGORY + "=? ORDER BY " + COLUMN_SERVICE_NAME,
                new String[]{category});

        if (cursor.moveToFirst()) {
            do {
                Service service = new Service();
                service.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_ID)));
                service.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_NAME)));
                service.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                service.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                service.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)));
                service.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                serviceList.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return serviceList;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_CATEGORY + " FROM " + TABLE_SERVICES + " ORDER BY " + COLUMN_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    // Новые методы для работы с заказами

    public long createOrder(String customerName, String customerPhone, String carModel,
                            String carNumber, String orderDate, String appointmentDate,
                            double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customerName);
        values.put(COLUMN_CUSTOMER_PHONE, customerPhone);
        values.put(COLUMN_CAR_MODEL, carModel);
        values.put(COLUMN_CAR_NUMBER, carNumber);
        values.put(COLUMN_ORDER_DATE, orderDate);
        values.put(COLUMN_APPOINTMENT_DATE, appointmentDate);
        values.put(COLUMN_TOTAL_AMOUNT, totalAmount);
        values.put(COLUMN_STATUS, "Новый");

        long orderId = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return orderId;
    }

    public boolean addOrderItem(long orderId, int serviceId, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_ID_FK, orderId);
        values.put(COLUMN_SERVICE_ID_FK, serviceId);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_ITEM_PRICE, price);

        long result = db.insert(TABLE_ORDER_ITEMS, null, values);
        db.close();
        return result != -1;
    }

    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " ORDER BY " + COLUMN_ORDER_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)));
                order.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)));
                order.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_PHONE)));
                order.setCarModel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAR_MODEL)));
                order.setCarNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAR_NUMBER)));
                order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE)));
                order.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DATE)));
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT)));

                // Получаем элементы заказа
                order.setItems(getOrderItems(order.getId()));
                orderList.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orderList;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT oi.*, s." + COLUMN_SERVICE_NAME +
                        " FROM " + TABLE_ORDER_ITEMS + " oi " +
                        " JOIN " + TABLE_SERVICES + " s ON oi." + COLUMN_SERVICE_ID_FK + " = s." + COLUMN_SERVICE_ID +
                        " WHERE oi." + COLUMN_ORDER_ID_FK + " = ?",
                new String[]{String.valueOf(orderId)});

        if (cursor.moveToFirst()) {
            do {
                OrderItem item = new OrderItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)));
                item.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID_FK)));
                item.setServiceId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_ID_FK)));
                item.setServiceName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE_NAME)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)));
                item.setItemPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_PRICE)));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemList;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        int rowsAffected = db.update(TABLE_ORDERS, values,
                COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
        db.close();
        return rowsAffected > 0;
    }

    // Вспомогательные методы

    private void addTestData(SQLiteDatabase db) {
        // Добавляем тестового администратора
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_USER_TYPE, "admin");
        db.insert(TABLE_USERS, null, adminValues);

        // Добавляем тестовые услуги
        String[][] services = {
                {"Замена масла", "Полная замена моторного масла и фильтра", "1500", "60", "Техобслуживание"},
                {"Замена тормозных колодок", "Замена передних/задних тормозных колодок", "3000", "90", "Тормозная система"},
                {"Диагностика двигателя", "Компьютерная диагностика двигателя", "2000", "45", "Диагностика"},
                {"Замена аккумулятора", "Замена и утилизация старого аккумулятора", "2500", "30", "Электрика"},
                {"Шиномонтаж", "Сезонная замена и балансировка колес", "2000", "120", "Колеса"},
                {"Замена свечей зажигания", "Замена комплекта свечей зажигания", "1200", "45", "Техобслуживание"},
                {"Развал-схождение", "Регулировка углов установки колес", "2500", "60", "Ходовая часть"},
                {"Замена тормозной жидкости", "Полная замена тормозной жидкости", "1800", "60", "Тормозная система"}
        };

        for (String[] service : services) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SERVICE_NAME, service[0]);
            values.put(COLUMN_DESCRIPTION, service[1]);
            values.put(COLUMN_PRICE, Double.parseDouble(service[2]));
            values.put(COLUMN_DURATION, Integer.parseInt(service[3]));
            values.put(COLUMN_CATEGORY, service[4]);
            db.insert(TABLE_SERVICES, null, values);
        }
    }

    // Новые методы для проверки администратора

    public boolean isAdmin(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_TYPE + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + "=?", new String[]{username});

        boolean isAdmin = false;
        if (cursor.moveToFirst()) {
            String userType = cursor.getString(0);
            isAdmin = "admin".equals(userType);
        }
        cursor.close();
        db.close();
        return isAdmin;
    }
}