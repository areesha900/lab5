package com.example.lab5_starter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;


public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;
    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    return;
                }

                cityArrayList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    String name = doc.getString("name");
                    String province = doc.getString("province");
                    cityArrayList.add(new City(name, province));
                }

                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });

        cityListView.setOnItemLongClickListener((adapterView, view, position, id) -> {

            City city = cityArrayAdapter.getItem(position);
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete City")
                    .setMessage("Are you sure you want to delete this city?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (city != null) {
                            citiesRef.document(city.getName()).delete();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {

        String oldName = city.getName();
        city.setName(title);
        city.setProvince(year);
        citiesRef.document(oldName).delete();
        citiesRef.document(city.getName()).set(city);
    }

    @Override
    public void addCity(City city) {
        citiesRef.document(city.getName()).set(city);
    }
}