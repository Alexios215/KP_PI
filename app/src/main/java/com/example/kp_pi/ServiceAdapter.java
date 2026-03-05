package com.example.kp_pi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private List<Service> serviceList;
    private Context context;
    private OnItemClickListener listener;
    private boolean isAdmin;

    public interface OnItemClickListener {
        void onItemClick(Service service);
        void onAddToCartClick(Service service);
        void onEditClick(Service service);
        void onDeleteClick(Service service);
        void onActivateClick(Service service);
    }

    public ServiceAdapter(List<Service> serviceList, Context context, OnItemClickListener listener, boolean isAdmin) {
        this.serviceList = serviceList;
        this.context = context;
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Service service = serviceList.get(position);

        holder.serviceName.setText(service.getName());
        holder.serviceDescription.setText(service.getDescription());
        holder.servicePrice.setText(String.format("Цена: %.2f руб.", service.getPrice()));
        holder.serviceDuration.setText(String.format("Длительность: %d мин.", service.getDuration()));
        holder.serviceCategory.setText(service.getCategory());

        // Если услуга неактивна, делаем её полупрозрачной для админа
        if (!service.isActive() && isAdmin) {
            holder.itemView.setAlpha(0.5f);
            holder.serviceName.setTextColor(Color.GRAY);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.serviceName.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(service));

        // Кнопка "В корзину" доступна только для активных услуг
        if (service.isActive()) {
            holder.addToCartButton.setVisibility(View.VISIBLE);
            holder.addToCartButton.setOnClickListener(v -> listener.onAddToCartClick(service));
        } else {
            holder.addToCartButton.setVisibility(View.GONE);
        }

        // Для администратора показываем кнопки управления
        if (isAdmin) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> listener.onEditClick(service));

            // Для активных услуг показываем кнопку удаления (деактивации)
            if (service.isActive()) {
                holder.actionButton.setVisibility(View.VISIBLE);
                holder.actionButton.setImageResource(android.R.drawable.ic_menu_delete);
                holder.actionButton.setOnClickListener(v -> listener.onDeleteClick(service));
            }
            // Для неактивных услуг показываем кнопку активации
            else {
                holder.actionButton.setVisibility(View.VISIBLE);
                holder.actionButton.setImageResource(android.R.drawable.ic_menu_revert);
                holder.actionButton.setOnClickListener(v -> listener.onActivateClick(service));
            }
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.actionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        TextView serviceDescription;
        TextView servicePrice;
        TextView serviceDuration;
        TextView serviceCategory;
        Button addToCartButton;
        ImageButton editButton;
        ImageButton actionButton; // Переименовали с deleteButton на actionButton

        public ViewHolder(View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            serviceDescription = itemView.findViewById(R.id.service_description);
            servicePrice = itemView.findViewById(R.id.service_price);
            serviceDuration = itemView.findViewById(R.id.service_duration);
            serviceCategory = itemView.findViewById(R.id.service_category);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
            editButton = itemView.findViewById(R.id.edit_button);
            actionButton = itemView.findViewById(R.id.action_button); // id тоже нужно поменять
        }
    }

    public void updateList(List<Service> newList) {
        serviceList = newList;
        notifyDataSetChanged();
    }
}