package com.ugd9_x_yyyy.Adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ugd9_x_yyyy.Models.Buku;
import com.ugd9_x_yyyy.R;
import com.ugd9_x_yyyy.Views.TambahEditBuku;
import com.ugd9_x_yyyy.Views.ViewsBuku;
import com.ugd9_x_yyyy.Views.ZoomImage;
import com.zolad.zoominimageview.ZoomInImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.POST;

public class AdapterBuku extends RecyclerView.Adapter<AdapterBuku.adapterBukuViewHolder> {

    private final String url = "https://asdospbp2020.000webhostapp.com/api/buku";
    private List<Buku> bukuList;
    private List<Buku> bukuListFiltered;
    private Context context;
    private View view;

    public AdapterBuku(Context context, List<Buku> bukuList) {
        this.context=context;
        this.bukuList = bukuList;
        this.bukuListFiltered = bukuList;
    }

    @NonNull
    @Override
    public AdapterBuku.adapterBukuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        view = layoutInflater.inflate(R.layout.activity_adapter_buku, parent, false);
        return new AdapterBuku.adapterBukuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterBuku.adapterBukuViewHolder holder, int position) {
        final Buku buku = bukuListFiltered.get(position);

        NumberFormat formatter = new DecimalFormat("#,###");
        holder.txtNama.setText(buku.getNamaBuku());
        holder.txtPengarang.setText(buku.getPengarang());
        holder.txtHarga.setText("Rp "+ formatter.format(buku.getHarga()));
        Glide.with(context)
                .load("https://asdospbp2020.000webhostapp.com/images/"+buku.getGambar())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.ivGambar);

        holder.cardBuku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
                View views = layoutInflater.inflate(R.layout.pilih_aksi, null);

                final AlertDialog alertD = new AlertDialog.Builder(view.getContext()).create();

                Button btnEdit      = views.findViewById(R.id.btnEdit);
                Button btnHapus     = views.findViewById(R.id.btnHapus);
                ImageView ivBack    = views.findViewById(R.id.ivBack);

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertD.dismiss();
                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
                        Bundle data = new Bundle();
                        data.putSerializable("buku", buku);
                        data.putString("status", "edit");
                        TambahEditBuku tambahEditBuku = new TambahEditBuku();
                        tambahEditBuku.setArguments(data);
                        loadFragment(tambahEditBuku);
                    }
                });

                btnHapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertD.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Anda yakin ingin menghapus buku ?");
                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteBuku(buku.getIdBuku());

                            }
                        });
                        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                alertD.show();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

                ivBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertD.dismiss();
                    }
                });

                alertD.setView(views);
                alertD.show();
            }
        });

        holder.ivGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Bundle data = new Bundle();
                data.putSerializable("buku", buku);
                ZoomImage zoomImage = new ZoomImage();
                zoomImage.setArguments(data);
                loadFragment(zoomImage);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (bukuListFiltered != null) ? bukuListFiltered.size() : 0;
    }

    public class adapterBukuViewHolder extends RecyclerView.ViewHolder {
        private TextView txtNama, txtPengarang, txtHarga;
        private ImageView ivGambar;
        private CardView cardBuku;

        public adapterBukuViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama         = itemView.findViewById(R.id.tvNamaBuku);
            txtPengarang    = itemView.findViewById(R.id.tvPengarang);
            txtHarga        = itemView.findViewById(R.id.tvHarga);
            ivGambar        = itemView.findViewById(R.id.ivGambar);
            cardBuku        = itemView.findViewById(R.id.cardBuku);
        }
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String userInput = charSequence.toString();
                if (userInput.isEmpty()) {
                    bukuListFiltered = bukuList;
                }
                else {
                    List<Buku> filteredList = new ArrayList<>();
                    for(Buku buku : bukuList) {
                        if(String.valueOf(buku.getNamaBuku()).toLowerCase().contains(userInput) ||
                                buku.getPengarang().toLowerCase().contains(userInput)) {
                            filteredList.add(buku);
                        }
                    }
                    bukuListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = bukuListFiltered;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                bukuListFiltered = (ArrayList<Buku>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void deleteBuku(int idBuku){
        RequestQueue queue = Volley.newRequestQueue(context);
        String urlDelete = "https://asdospbp2020.000webhostapp.com/api/buku/delete/" + idBuku;

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("loading....");
        progressDialog.setTitle("Menghapus data buku");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        //Meminta
        StringRequest stringRequest = new StringRequest(POST, urlDelete, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                    loadFragment(new ViewsBuku());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    public void loadFragment(Fragment fragment) {
        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_view_buku,fragment)
                .commit();
    }
}