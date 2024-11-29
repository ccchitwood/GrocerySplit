package edu.uga.cs.grocerysplit;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss", Locale.getDefault());

        for (CheckedOutBasket basket : baskets) {
            TextView basketDetails = new TextView(this);
            Date date = new Date((long) basket.getDate() * 1000);
            String formattedDate = sdf.format(date);
            basketDetails.setText("User: " + basket.getUserID() + "\nDate: " + formattedDate + "\nTotal Price: $" + basket.getTotalPrice());
            checkedOutLayout.addView(basketDetails);

            Button viewItemsButton = new Button(this);
            viewItemsButton.setText("View Items");
            viewItemsButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CheckOutItems.class);
                intent.putParcelableArrayListExtra("items", new ArrayList<>(basket.getItems()));
                startActivity(intent);
            });

            checkedOutLayout.addView(viewItemsButton);

            Button editCostButton = new Button(this);
            editCostButton.setText("Edit Total Cost");
            editCostButton.setOnClickListener(v -> showEditCostDialog(basket));
            checkedOutLayout.addView(editCostButton);
        }
    }

    private void showEditCostDialog(CheckedOutBasket basket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Total Cost");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter new total cost");
        builder.setView(input);

        // Set up the dialog buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newCostString = input.getText().toString();
            if (!newCostString.isEmpty()) {
                double newCost = Double.parseDouble(newCostString);
                updateTotalCostInFirebase(basket, newCost);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateTotalCostInFirebase(CheckedOutBasket basket, double newCost) {
        DatabaseReference checkedOutReference = FirebaseDatabase.getInstance().getReference("checkedOutBaskets");

        // Find the basket in Firebase by matching its key or unique identifier
        checkedOutReference.orderByChild("userID").equalTo(basket.getUserID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot basketSnapshot : snapshot.getChildren()) {
                            CheckedOutBasket firebaseBasket = basketSnapshot.getValue(CheckedOutBasket.class);

                            if (firebaseBasket != null && firebaseBasket.getDate() == basket.getDate()) {
                                basketSnapshot.getRef().child("totalPrice").setValue(newCost)
                                        .addOnSuccessListener(aVoid -> Log.d("CheckedOutList", "Total cost updated successfully"))
                                        .addOnFailureListener(e -> Log.e("CheckedOutList", "Failed to update total cost: " + e.getMessage()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CheckedOutList", "Error updating total cost: " + error.getMessage());
                    }
                });
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