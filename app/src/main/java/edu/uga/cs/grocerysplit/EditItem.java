package edu.uga.cs.grocerysplit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

public class EditItem extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference productsReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText name = findViewById(R.id.editTextText2);
        EditText cost = findViewById(R.id.editTextNumberDecimal2);
        EditText quantity = findViewById(R.id.editTextNumber2);
        Button editItemButton = findViewById(R.id.button9);

        Bundle productInfo = getIntent().getExtras();

        String productName = productInfo.getString("name");
        double productCost = productInfo.getDouble("cost");
        int productQuantity = productInfo.getInt("quantity");

        name.setText(productName);
        cost.setText(String.valueOf(productCost));
        quantity.setText(String.valueOf(productQuantity));

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        productsReference = database.getReference("products");

        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = name.getText().toString().trim();
                String newCostString = cost.getText().toString().trim();
                String newQuantityString = quantity.getText().toString().trim();

                // Check if any fields are empty
                if (newName.isEmpty() || newCostString.isEmpty() || newQuantityString.isEmpty()) {
                    Toast.makeText(EditItem.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // Convert cost and quantity to appropriate types
                    double newCost = Double.parseDouble(newCostString);
                    int newQuantity = Integer.parseInt(newQuantityString);

                    // Query to find the product by original values (name, cost, and quantity)
                    productsReference.orderByChild("name").equalTo(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Product product = snapshot.getValue(Product.class);

                                if (product != null && product.getCost() == productCost && product.getQuantity() == productQuantity) {
                                    snapshot.getRef().child("name").setValue(newName);
                                    snapshot.getRef().child("cost").setValue(newCost);
                                    snapshot.getRef().child("quantity").setValue(newQuantity);

                                    Toast.makeText(EditItem.this, "Item updated successfully", Toast.LENGTH_SHORT).show();

                                    // Redirect back to the shopping list or another activity
                                    startActivity(new Intent(EditItem.this, ShoppingList.class));
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(EditItem.this, "Invalid cost or quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(EditItem.this, ShoppingList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
