package com.example.kp_pi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private List<Service> serviceList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Service service);
        void onAddToCartClick(Service service);
    }

    public ServiceAdapter(List<Service> serviceList, Context context, OnItemClickListener listener) {
        this.serviceList = serviceList;
        this.context = context;
        this.listener = listener;
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

        holder.itemView.setOnClickListener(v -> listener.onItemClick(service));
        holder.addToCartButton.setOnClickListener(v -> listener.onAddToCartClick(service));
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

        public ViewHolder(View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            serviceDescription = itemView.findViewById(R.id.service_description);
            servicePrice = itemView.findViewById(R.id.service_price);
            serviceDuration = itemView.findViewById(R.id.service_duration);
            serviceCategory = itemView.findViewById(R.id.service_category);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }
    }

    public void updateList(List<Service> newList) {
        serviceList = newList;
        notifyDataSetChanged();
    }
}