package edu.uga.cs.grocerysplit;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CheckedOutList extends AppCompatActivity {

    private LinearLayout checkedOutLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseReference checkedOutReference = FirebaseDatabase.getInstance().getReference("checkedOutBaskets");

        checkedOutReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CheckedOutBasket> checkedOutBaskets = new ArrayList<>();

                for (DataSnapshot basketSnapshot : snapshot.getChildren()) {
                    CheckedOutBasket basket = basketSnapshot.getValue(CheckedOutBasket.class);
                    checkedOutBaskets.add(basket);
                }

                // Update the UI with the list of checked-out baskets
                displayCheckedOutBaskets(checkedOutBaskets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CheckedOutList", "Error loading checked-out baskets: " + error.getMessage());
            }
        });
    }

    private void displayCheckedOutBaskets(List<CheckedOutBasket> baskets) {
        checkedOutLayout = findViewById(R.id.linearLayout1);
        checkedOutLayout.removeAllViews();

        for (CheckedOutBasket basket : baskets) {
            TextView basketDetails = new TextView(this);
            basketDetails.setText("User: " + basket.getUserID() + "\nTotal Price: $" + basket.getTotalPrice());
            checkedOutLayout.addView(basketDetails);

            Button viewItemsButton = new Button(this);
            viewItemsButton.setText("View Items");
            viewItemsButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CheckOutItems.class);
                intent.putParcelableArrayListExtra("items", new ArrayList<>(basket.getItems()));
                startActivity(intent);
            });

            checkedOutLayout.addView(viewItemsButton);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(CheckedOutList.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}