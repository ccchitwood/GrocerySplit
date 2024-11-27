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
import java.util.List;

public class ShoppingList extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference productsReference;
    private LinearLayout itemView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        productsReference = database.getReference("products");
        itemView = findViewById(R.id.linearLayout1);

        productsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> shoppingList = new ArrayList<>();

                for (DataSnapshot products : snapshot.getChildren()) {
                    Product product = products.getValue(Product.class);
                    shoppingList.add(product);
                }

                if (shoppingList.isEmpty()) {
                    TextView noItemsMessage = new TextView(getApplicationContext());
                    noItemsMessage.setText("No items currently on the shopping list.");
                    itemView.addView(noItemsMessage);
                    return;
                }

                for (Product product : shoppingList) {
                    TextView productName = new TextView(getApplicationContext());
                    productName.setText("Name: " + product.getName());
                    Log.d("Shopping List", product.getName());
                    TextView productCost = new TextView(getApplicationContext());
                    productCost.setText("Cost: $" + product.getCost());
                    Log.d("Shopping List", product.getCost() + "");
                    TextView productQuantity = new TextView(getApplicationContext());
                    productQuantity.setText("Quantity: " + product.getQuantity());
                    Log.d("Shopping List", product.getQuantity() + "");

                    Button deleteButton = new Button(getApplicationContext());
                    deleteButton.setText("Delete item");
                    Button editButton = new Button(getApplicationContext());
                    editButton.setText("Edit item");
                    Button markAsPurchasedButton = new Button(getApplicationContext());
                    markAsPurchasedButton.setText("Mark item as purchased");


                    deleteButton.setOnClickListener(x -> deleteItem(product.getName(),
                            product.getCost(), product.getQuantity()));

                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ShoppingList.this, EditItem.class);
                            intent.putExtra("name", product.getName());
                            intent.putExtra("cost", product.getCost());
                            intent.putExtra("quantity", product.getQuantity());
                            startActivity(intent);
                            finish();
                        }
                    });

                    markAsPurchasedButton.setOnClickListener(x -> markAsPurchased(
                            product.getName(),
                            product.getCost(),
                            product.getQuantity()
                    ));

                    itemView.addView(productName);
                    itemView.addView(productCost);
                    itemView.addView(productQuantity);

                    itemView.addView(deleteButton);
                    itemView.addView(editButton);
                    itemView.addView(markAsPurchasedButton);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void deleteItem(String name, double cost, int quantity) {
        productsReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null && product.getCost() == cost && product.getQuantity() == quantity) {
                        productSnapshot.getRef().removeValue();
                    }
                }
                finish();
                startActivity(getIntent());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Shopping List", "Error deleting product: " + error.getMessage());
            }
        });
    }

    public void editItem(String name, double cost, int quantity) {

    }

    // Method to move an item to the shopping basket
    public void markAsPurchased(String name, double cost, int quantity) {
        productsReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null && product.getCost() == cost && product.getQuantity() == quantity) {
                        // Add the product to the "basket" node
                        DatabaseReference basketReference = FirebaseDatabase.getInstance()
                                .getReference("basket")
                                .child(productSnapshot.getKey()); // Use the same key for uniqueness

                        basketReference.setValue(product).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Remove the product from the "products" node
                                productSnapshot.getRef().removeValue();
                                Log.d("Shopping List", "Item moved to shopping basket successfully.");
                            } else {
                                Log.e("Shopping List", "Error moving item to basket: " + task.getException().getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Shopping List", "Error moving product: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ShoppingList.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
