package com.ugd9_x_yyyy.Views;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ugd9_x_yyyy.API.MahasiswaAPI;
import com.ugd9_x_yyyy.API.TransaksiBukuAPI;
import com.ugd9_x_yyyy.Adapters.AdapterTransaksiBuku;
import com.ugd9_x_yyyy.Models.Buku;
import com.ugd9_x_yyyy.Models.DTBuku;
import com.ugd9_x_yyyy.Models.Mahasiswa;
import com.ugd9_x_yyyy.Models.TransaksiBuku;
import com.ugd9_x_yyyy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.GET;

public class ViewsCart extends Fragment{

    private RecyclerView recyclerView;
    private TextView tvTotalBiaya;
    private Button btnBayar;
    private CheckBox checkBox;
    private CardView panelBayar, panelCheckBox;
    private AdapterTransaksiBuku adapter;
    private List<TransaksiBuku> transaksiBukuList;
    private List<DTBuku> dtBuku;
    private View view;
    public Boolean isFullChecked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_views_cart, container, false);

        init();
        setAdapter();
        getTransaksi();
        setAttribut();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu.findItem(R.id.btnSearch) != null)
            menu.findItem(R.id.btnSearch).setVisible(false);
        if(menu.findItem(R.id.btnAdd) != null)
            menu.findItem(R.id.btnAdd).setVisible(false);
    }

    private void init() {
        checkBox        = view.findViewById(R.id.checkBox);
        tvTotalBiaya    = view.findViewById(R.id.totalBiaya);
        btnBayar        = view.findViewById(R.id.btnBayar);
        panelBayar      = view.findViewById(R.id.panelBayar);
        panelCheckBox   = view.findViewById(R.id.panelCheckBox);
        recyclerView    = view.findViewById(R.id.recycler_view);
        panelCheckBox.setVisibility(View.GONE);
        setVisiblePanelBayar(View.GONE);
    }

    private void setAttribut() {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    setVisiblePanelBayar(View.VISIBLE);
                    setChecked(true);
                }
                else
                {
                    setVisiblePanelBayar(View.GONE);
                    if(isFullChecked)
                        setChecked(false);
                }
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void setAdapter(){
        getActivity().setTitle("Cart");
        transaksiBukuList = new ArrayList<>();
        adapter = new AdapterTransaksiBuku(view.getContext(), transaksiBukuList,
                new AdapterTransaksiBuku.OnQuantityChangeListener() {
                    @Override
                    public void onQuantityChange(Double totalBiaya, Double subTotal, Boolean full, Boolean empty) {
                        isFullChecked = full;
                        if(!empty){
                            setVisiblePanelBayar(View.VISIBLE);
                            NumberFormat formatter = new DecimalFormat("#,###");
                            tvTotalBiaya.setText("Rp "+ formatter.format(totalBiaya));
                        }

                        if(full)
                            checkBox.setChecked(true);
                        else
                            checkBox.setChecked(false);
                    }
                });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    public void setChecked(Boolean bool){
        for (int i = 0; i < transaksiBukuList.size(); i++) {
            transaksiBukuList.get(i).isChecked = bool;
            for (int j = 0; j < transaksiBukuList.get(i).getDtBukuList().size(); j++) {
                transaksiBukuList.get(i).getDtBukuList().get(j).isChecked = bool;
            }
        }
    }

    public void setVisiblePanelBayar(int visible){
        panelBayar.setVisibility(visible);
        if(visible == View.VISIBLE)
            recyclerView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.panel_height);
        else
            recyclerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public void getTransaksi() {
        final RequestQueue queue = Volley.newRequestQueue(view.getContext());

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, TransaksiBukuAPI.URL_SELECT
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("transaksibuku");

                    if(!transaksiBukuList.isEmpty())
                        transaksiBukuList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                        String noTransaksi  = jsonObject.optString("noTransaksi");
                        int idToko          = Integer.parseInt(jsonObject.optString("idToko"));
                        String tglTransaksi = jsonObject.optString("tglTransaksi");
                        Double totalBiaya   = Double.parseDouble(jsonObject.optString("totalBiaya"));
                        String namaToko      = jsonObject.optString("namaToko");

                        JSONArray jsonArrays = jsonObject.optJSONArray("dtbuku");

                        List<DTBuku> listDTBuku = new ArrayList<>();

                        for (int j = 0; j < jsonArrays.length(); j++) {
                            JSONObject jsonObjects = (JSONObject) jsonArrays.get(j);

                            String noTransaksis  = jsonObjects.optString("noTransaksi");
                            int idBuku           = Integer.parseInt(jsonObjects.optString("idBuku"));
                            String namaBuku      = jsonObjects.optString("namaBuku");
                            Double harga         = Double.parseDouble(jsonObjects.optString("harga"));
                            int jumlah           = Integer.parseInt(jsonObjects.optString("jumlah"));
                            String gambar        = jsonObjects.optString("gambar");

                            listDTBuku.add(new DTBuku(idBuku, noTransaksis, jumlah, namaBuku, harga, gambar));

                        }

                        transaksiBukuList.add(
                                new TransaksiBuku(noTransaksi, idToko, tglTransaksi, totalBiaya, namaToko, listDTBuku));
                    }
                    adapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                }

                Toast.makeText(view.getContext(), response.optString("message"),
                        Toast.LENGTH_SHORT).show();

                if(transaksiBukuList.isEmpty())
                    panelCheckBox.setVisibility(View.GONE);
                else
                    panelCheckBox.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(view.getContext(), error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }
}