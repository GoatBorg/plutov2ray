package com.bycomsolutions.bycomvpn.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bycomsolutions.bycomvpn.BuildConfig;
import com.bycomsolutions.bycomvpn.Preference;
import com.bycomsolutions.bycomvpn.R;
import com.bycomsolutions.bycomvpn.dialog.CountryData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static com.bycomsolutions.bycomvpn.utils.BillConfig.PRIMIUM_STATE;

import unified.vpn.sdk.Location;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {

    public Context context;
    private Preference preference;
    private List<CountryData> regions;

    private Set<String> workingRegions = new HashSet<>();
    private RegionListAdapterInterface listAdapterInterface;

    public LocationListAdapter(RegionListAdapterInterface listAdapterInterface, Activity cntec) {
        this.listAdapterInterface = listAdapterInterface;
        this.context = cntec;
        preference = new Preference(this.context);








    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_list_free, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final CountryData datanew = this.regions.get(holder.getAdapterPosition());
        final Location data = datanew.getCountryvalue();
        Locale locale = new Locale("", data.getLabels().getCountry());

        if (position == 0) {
            holder.flag.setImageResource(R.drawable.rocket);
            holder.app_name.setText(R.string.best_performance_server);
            holder.ll_server_type.setVisibility(View.GONE);
        } else {

            ImageView imageView = holder.flag;
            Resources resources = context.getResources();
            String sb = "drawable/" + data.getLabels().getCountry().toLowerCase();
            imageView.setImageResource(resources.getIdentifier(sb, null, context.getPackageName()));
            holder.app_name.setText(locale.getDisplayCountry());
            holder.limit.setVisibility(View.GONE);
            holder.ll_server_type.setVisibility(View.VISIBLE);

        }


        holder.tv_server_type.setText(data.getLabels().getCity());


        if (datanew.isPro()) {
            holder.pro.setVisibility(View.VISIBLE);
            //holder.tv_server_type.setText(R.string.pro_server);
            holder.tv_latency.setText((new Random().nextInt(28) + 21)+"ms");
        } else {
            holder.pro.setVisibility(View.GONE);
            //holder.tv_server_type.setText(R.string.free_server);
            holder.tv_latency.setText((new Random().nextInt(98) + 91)+"ms");
        }
        holder.itemView.setOnClickListener(view -> listAdapterInterface.onCountrySelected(regions.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return regions != null ? regions.size() : 0;
    }

    public void setRegions(List<Location> list) {

        // Working Servers
        workingRegions.add("australia-sydney");
        workingRegions.add("canada-toronto");
        workingRegions.add("czechia-prague");
        workingRegions.add("denmark-copenhagen");
        workingRegions.add("france-paris");
        workingRegions.add("germany-frankfurt");
        workingRegions.add("hong-kong-hong-kong");
        workingRegions.add("ireland-dublin");
        workingRegions.add("italy-milan");
        workingRegions.add("japan-tokyo");
        workingRegions.add("mexico-mexico-city");
        workingRegions.add("netherlands-amsterdam");
        workingRegions.add("norway-oslo");
        workingRegions.add("romania-bucharest");
        workingRegions.add("russia-moscow");
        workingRegions.add("singapore-singapore");
        workingRegions.add("spain-madrid");
        workingRegions.add("sweden-stockholm");
        workingRegions.add("switzerland-zurich");
        workingRegions.add("turkey-istanbul");
        workingRegions.add("ukraine-kyiv");
        workingRegions.add("united-kingdom-london");
        workingRegions.add("united-kingdom-manchester");
        workingRegions.add("us-los-angeles");
        workingRegions.add("us-miami");
        workingRegions.add("us-new-york");
        workingRegions.add("us-newark");
        workingRegions.add("us-san-jose");





       /*

Not Working Servers

       algeria-algiers-city
       argentina-buenos-aires
       austria-vienna
       belgium-brussels
       brazil-sao-paulo
       bulgaria-sofia
       cambodia-phnom-penh
       canada-montreal
       canada-vancouver
       chile-Santiago
       colombia-bogota
       egypt-cairo
       estonia-tallinn
       greece-athens
       hungary-budapest
       iceland-reykjavik
       india-mumbai
       indonesia-jakarta
       israel-tel-aviv
       kazakhstan-astana
       lithuania-vilnius
       luxembourg-luxembourg
       malaysia-kuala-lumpur
       new-zealand-auckland
       pakistan-karachi
       philippines-manila
       poland-warsaw
       portugal-lisbon
       russia-novosibirsk
       slovenia-ljubljana
       south-africa-johannesburg
       south-korea-seoul
       taiwan-taipei
       thailand-bangkok
       uae-dubai
       us-atlanta
       us-charlotte
       us-chicago
       us-dallas
       us-denver
       us-houston
       us-orlando
       us-phoenix
       us-portland
       us-seattle
       us-washington
       vietnam-hanoi



     */




        regions = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CountryData newData = new CountryData();
            newData.setCountryvalue(list.get(i));

            if(!BuildConfig.USE_IN_APP_PURCHASE) newData.setPro(false);
            else if (preference.isBooleenPreference(PRIMIUM_STATE)) newData.setPro(true);
            else newData.setPro(i >= 6);

            if(workingRegions.contains(newData.getCountryvalue().getName()))     // Filtering only working servers
                regions.add(newData);


        }


        if(regions.size()>1) regions.add(0,regions.get(0)); // Add First Server as Fastest Server


        notifyDataSetChanged();
    }

    public interface RegionListAdapterInterface {
        void onCountrySelected(CountryData item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView app_name,tv_server_type,tv_latency;
        ImageView flag, pro;
        ImageView limit;

        LinearLayout ll_server_type;

        ViewHolder(View v) {
            super(v);
            this.app_name = itemView.findViewById(R.id.region_title);
            this.limit = itemView.findViewById(R.id.region_limit);
            this.flag = itemView.findViewById(R.id.country_flag);
            this.pro = itemView.findViewById(R.id.pro);
            this.tv_server_type = itemView.findViewById(R.id.tv_server_type);
            this.tv_latency = itemView.findViewById(R.id.tv_latency);
            this.ll_server_type = itemView.findViewById(R.id.ll_server_type);
        }
    }
}
