package edu.uga.cs.grocerysplit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddToList extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference productsReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_list_screen);

        EditText name = findViewById(R.id.editTextText);
        EditText cost = findViewById(R.id.editTextNumberDecimal);
        EditText quantity = findViewById(R.id.editTextNumber);
        Button addItemButton = findViewById(R.id.button7);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        productsReference = database.getReference("products");

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productName = name.getText().toString().trim();
                String costString = cost.getText().toString().trim();
                String quantityString = quantity.getText().toString().trim();

                if (productName.isEmpty() || costString.isEmpty() || quantityString.isEmpty()) {
                    Toast.makeText(AddToList.this, "Please fill all fields",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                double costDouble = Double.parseDouble(costString);
                int quantity = Integer.parseInt(quantityString);

                Product product = new Product(productName, costDouble, quantity);
                String productId = productsReference.push().getKey();
                if (productId != null) {
                    productsReference.child(productId).setValue(product)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddToList.this, "Product added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddToList.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddToList.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

    }
}
