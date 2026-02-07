package com.example.live.launcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.live.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_APP = 0;
    private static final int TYPE_HEADER = 1;

    public interface Listener {
        void onAppClicked(AppInfo app);
        void onAppLongPressed(AppInfo app);
    }

    private final Listener listener;
    private final boolean showCheckbox;
    private final boolean separateHeaderRows;
    private final Set<String> checkedPackages = new HashSet<>();
    private final List<Item> items = new ArrayList<>();

    private static class Item {
        final AppInfo app;
        final String sectionLetter;

        Item(AppInfo app, String sectionLetter) {
            this.app = app;
            this.sectionLetter = sectionLetter;
        }

        Item(String header) {
            this.app = null;
            this.sectionLetter = header;
        }
    }

    public AppsAdapter(Listener listener, boolean showCheckbox) {
        this(listener, showCheckbox, false);
    }

    public AppsAdapter(Listener listener, boolean showCheckbox, boolean separateHeaderRows) {
        this.listener = listener;
        this.showCheckbox = showCheckbox;
        this.separateHeaderRows = separateHeaderRows;
    }

    public void submit(List<AppInfo> newApps) {
        items.clear();
        if (newApps != null && !newApps.isEmpty()) {
            List<AppInfo> sortedApps = new ArrayList<>(newApps);
            Collections.sort(sortedApps, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    return o1.label.compareToIgnoreCase(o2.label);
                }
            });

            char lastChar = '\0';
            for (AppInfo app : sortedApps) {
                if (app.label == null || app.label.isEmpty()) continue;
                
                char currentChar = Character.toUpperCase(app.label.charAt(0));
                if (Character.isDigit(currentChar)) currentChar = '#';

                if (currentChar != lastChar) {
                    if (separateHeaderRows) {
                        items.add(new Item(String.valueOf(currentChar)));
                    } else {
                        items.add(new Item(app, String.valueOf(currentChar)));
                        lastChar = currentChar;
                        continue;
                    }
                    lastChar = currentChar;
                }
                items.add(new Item(app, null));
            }
        }
        notifyDataSetChanged();
    }

    public void setCheckedPackages(Set<String> packages) {
        checkedPackages.clear();
        if (packages != null) checkedPackages.addAll(packages);
        notifyDataSetChanged();
    }

    public Set<String> getCheckedPackages() {
        return new HashSet<>(checkedPackages);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).app == null ? TYPE_HEADER : TYPE_APP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new AppVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final Item item = items.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).label.setText(item.sectionLetter);
        } else if (holder instanceof AppVH) {
            final AppVH h = (AppVH) holder;
            final AppInfo app = item.app;

            h.label.setText(app.label);
            h.icon.setImageDrawable(app.icon);

            if (item.sectionLetter != null && !separateHeaderRows) {
                h.sectionLetter.setText(item.sectionLetter);
                h.sectionLetter.setVisibility(View.VISIBLE);
                h.sectionDivider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
            } else {
                h.sectionLetter.setVisibility(View.GONE);
                h.sectionDivider.setVisibility(View.GONE);
            }

            if (showCheckbox) {
                h.checkbox.setVisibility(View.VISIBLE);
                h.checkbox.setChecked(checkedPackages.contains(app.packageName));
            } else {
                h.checkbox.setVisibility(View.GONE);
            }

            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (showCheckbox) {
                        if (checkedPackages.contains(app.packageName)) {
                            checkedPackages.remove(app.packageName);
                            h.checkbox.setChecked(false);
                        } else {
                            checkedPackages.add(app.packageName);
                            h.checkbox.setChecked(true);
                        }
                    } else if (listener != null) {
                        listener.onAppClicked(app);
                    }
                }
            });
            
            h.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!showCheckbox && listener != null) {
                        listener.onAppLongPressed(app);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class AppVH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;
        final CheckBox checkbox;
        final TextView sectionLetter;
        final View sectionDivider;

        AppVH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            label = itemView.findViewById(R.id.app_label);
            checkbox = itemView.findViewById(R.id.app_check);
            sectionLetter = itemView.findViewById(R.id.section_letter);
            sectionDivider = itemView.findViewById(R.id.section_divider);
        }
    }

    static final class HeaderVH extends RecyclerView.ViewHolder {
        final TextView label;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.section_header);
        }
    }
}