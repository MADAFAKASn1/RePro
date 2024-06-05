package com.example.repro.ui.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.repro.R;

public class AdaptadorPersonalizado extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    public AdaptadorPersonalizado(Context ctx, int txtViewResourceId, String[] objects){
        super(ctx, txtViewResourceId, objects);
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
        return crearFilaPersonalizada(position, cnvtView, prnt);
    }
    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt){
        return crearFilaPersonalizada(pos, cnvtView, prnt);
    }
    public View crearFilaPersonalizada(int position, View convertView, ViewGroup parent){
        String[] heroes = { "Batman","Capitan América",
                "Iron man","Spiderman","Black Panther" };


        View miFila = inflater.inflate(R.layout.adaptador_personalizado_inicio, parent, false);
        /*TextView nombre = (TextView) miFila.findViewById(R.id.textoHeroes);
        nombre.setText(heroes[position]);
        ImageView imagen = (ImageView) miFila.findViewById(R.id.imagen);
        imagen.setImageResource(imagenes[position]);*/
        return miFila;
    }
}
/*
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<StorageReference> mData;

    public MyAdapter(List<StorageReference> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        StorageReference item = mData.get(position);
        // Here you can set the data to your views in the item layout
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // Here you can initialize your views in the item layout

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Here you can find your views in the item layout
        }
    }
}*/
