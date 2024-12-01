package edu.uga.cs.grocerysplit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CheckOutItems extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference checkedOutReference;
    private DatabaseReference productsReference;
    private LinearLayout itemView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        checkedOutReference = database.getReference("checkedOutBaskets");
        productsReference = database.getReference("products");
        itemView = findViewById(R.id.linearLayout1);

        ArrayList<Product> basketItems = getIntent().getParcelableArrayListExtra("basketItems");
        int basketDate = getIntent().getIntExtra("basketDate", -1);

        if (basketItems == null || basketItems.isEmpty()) {
            TextView noItemsMessage = new TextView(getApplicationContext());
            noItemsMessage.setText("No items in this checked out basket.");
            itemView.addView(noItemsMessage);
            return;
        } else {
            Log.d("CheckOutItems", "basketItems size: " + basketItems.size());
        }

        for (Product product : basketItems) {
            TextView productName = new TextView(getApplicationContext());
            productName.setText("Name: " + product.getName());

            TextView productCost = new TextView(getApplicationContext());
            productCost.setText("Cost: $" + product.getCost());

            TextView productQuantity = new TextView(getApplicationContext());
            productQuantity.setText("Quantity: " + product.getQuantity());

            Button removeButton = new Button(getApplicationContext());
            removeButton.setText("Remove and Add Back to Shopping List");
            removeButton.setOnClickListener(v -> removeItemFromBasket(basketDate, product));

            itemView.addView(productName);
            itemView.addView(productCost);
            itemView.addView(productQuantity);
            itemView.addView(removeButton);
        }
    }

    private void removeItemFromBasket(int basketDate, Product product) {
        // Locate the basket by its date
        checkedOutReference.orderByChild("date").equalTo(basketDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot basketSnapshot : snapshot.getChildren()) {
                            DataSnapshot itemsSnapshot = basketSnapshot.child("items");

                            for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                                Product firebaseProduct = itemSnapshot.getValue(Product.class);

                                if (firebaseProduct != null &&
                                        firebaseProduct.getName().equals(product.getName()) &&
                                        firebaseProduct.getCost() == product.getCost() &&
                                        firebaseProduct.getQuantity() == product.getQuantity()) {

                                    // Remove the item from the basket
                                    itemSnapshot.getRef().removeValue();

                                    // Add the item back to the shopping list
                                    productsReference.push().setValue(product).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("CheckOutItems", "Item added back to shopping list.");

                                            runOnUiThread(() -> {
                                                int childCount = itemView.getChildCount();
                                                for (int i = 0; i < childCount; i++) {
                                                    View view = itemView.getChildAt(i);

                                                    // Find and remove views related to the removed product
                                                    if (view instanceof TextView && ((TextView) view).getText().toString().contains(product.getName())) {
                                                        itemView.removeView(view); // Remove product name
                                                        itemView.removeViewAt(i);  // Remove cost
                                                        itemView.removeViewAt(i);  // Remove quantity
                                                        itemView.removeViewAt(i);  // Remove button
                                                        break;
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.e("CheckOutItems", "Error adding item back to shopping list: " +
                                                    task.getException().getMessage());
                                        }
                                    });

                                    break; // Break out of the loop after finding and removing the item
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CheckOutItems", "Error locating basket by date: " + error.getMessage());
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(CheckOutItems.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
