package com.example.live.launcher;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class YearGridAdapter extends RecyclerView.Adapter<YearGridAdapter.DayViewHolder> {

    private final List<DayItem> days = new ArrayList<>();

    public YearGridAdapter() {
        Calendar now = Calendar.getInstance();
        int totalDays = now.getActualMaximum(Calendar.DAY_OF_YEAR);
        int dayOfYear = now.get(Calendar.DAY_OF_YEAR);
        int hour = now.get(Calendar.HOUR_OF_DAY);

        for (int i = 1; i <= totalDays; i++) {
            int color;
            if (i < dayOfYear) {
                color = 0xFF212121;
            } else if (i == dayOfYear) {
                color = 0xFF212121; // Alpha will be set in onBindViewHolder
            } else {
                color = 0xFFF0F0F0;
            }
            days.add(new DayItem(color, i == dayOfYear));
        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new View(parent.getContext());
        float density = parent.getResources().getDisplayMetrics().density;
        int dotSize = (int) (11 * density);
        int margin = (int) (2 * density);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(dotSize, dotSize);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayItem item = days.get(position);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(item.color);
        if (item.isToday) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int alpha = 80 + (int) (hour / 24.0f * 175);
            shape.setAlpha(alpha);
        }
        holder.view.setBackground(shape);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        final View view;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }

    static class DayItem {
        final int color;
        final boolean isToday;

        DayItem(int color, boolean isToday) {
            this.color = color;
            this.isToday = isToday;
        }
    }
}