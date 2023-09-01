package com.android.libramanage.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.libramanage.R;
import com.android.libramanage.entity.StateClass;
import com.android.libramanage.entity.User;
import com.android.libramanage.entity.dateModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CafeListAdapter extends RecyclerView.Adapter<CafeListAdapter.MyViewHolder> {

    Context context;
    List<StateClass> cafeList;

    public CafeListAdapter(Context context, List<StateClass> restaurantList) {
        this.context = context;
        this.cafeList = restaurantList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cafe_temp, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        StateClass stateClass = cafeList.get(position);

        if (stateClass.getCafe() != null) {
            holder.placeName.setText(stateClass.getCafe().getName());

            holder.placeTypeName.setText(stateClass.getCafe().getPlacesType().toUpperCase());

            String placeType = stateClass.getCafe().getPlacesType();
            int bgColor = ContextCompat.getColor(context, R.color.black);
            switch (placeType) {
                case "Bar":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_bar);
                    bgColor = ContextCompat.getColor(context, R.color.purple_color);
                    break;
                case "Restaurant":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_restaurant_menu_24dp);
                    bgColor = ContextCompat.getColor(context, R.color.restau_color);
                    break;

                case "Parks":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_park_black_24dp_1);
                    bgColor = ContextCompat.getColor(context, R.color.park_color);
                    break;

                case "Gym":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_fitness_center_black_24dp_1);
                    bgColor = ContextCompat.getColor(context, R.color.gym_color);
                    break;

                case "Office":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_apartment_black_24dp_1);
                    bgColor = ContextCompat.getColor(context, R.color.off_color);
                    break;

                case "Shopping":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_shopping_bag_black_24dp_1);
                    bgColor = ContextCompat.getColor(context, R.color.shopp_color);
                    break;

                case "Coffee":
                    holder.serviceIcon.setBackgroundResource(R.drawable.ic_group);
                    bgColor = ContextCompat.getColor(context, R.color.cup_color);
                    break;

            }

            holder.placeTypeName.setTextColor(bgColor);

            holder.serviceBg.getBackground().setTint(bgColor);
            holder.serviceBg.getBackground().setAlpha(30);

            int Daily = 0, Monthly = 0;

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();


            cal2.setTime(new Date());


            for (dateModel datemodel : stateClass.getDateModelList()) {
                for(User user:datemodel.getUserList()) {
                    cal1.setTimeInMillis(user.getLoginedTime()*1000);
                    if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
                        if (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) {
                            Monthly++;
                        }
                    }
                    if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
                        if (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) {
                            if (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                                Daily++;
                            }
                        }
                    }
                }
            }

            holder.monthlyState.setText(String.valueOf(Monthly));
            holder.dailyState.setText(String.valueOf(Daily));
        }

    }

    @Override
    public int getItemCount() {
        return cafeList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, placeTypeName, dailyState, monthlyState;
        LinearLayout serviceBg;
        ImageView serviceIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.tvTitle);
            placeTypeName = itemView.findViewById(R.id.service_name);
            serviceBg = itemView.findViewById(R.id.servce_bg);
            serviceIcon = itemView.findViewById(R.id.service_im);
            dailyState = itemView.findViewById(R.id.tvDaily);
            monthlyState = itemView.findViewById(R.id.tvMonth);
        }
    }
}
