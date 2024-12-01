package edu.uga.cs.grocerysplit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettleItems extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_items);

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

                double total = 0.0;
                HashMap<String, Double> userTotals = new HashMap<>();

                for (CheckedOutBasket basket : checkedOutBaskets) {
                    if (basket == null || basket.getUserID() == null) {
                        continue;
                    }

                    String userID = basket.getUserID();
                    double basketTotal = basket.getTotalPrice();

                    total += basketTotal;

                    Log.d("DebuggingTotal", "Current total: " + total);

                    userTotals.put(userID, userTotals.getOrDefault(userID, 0.0) + basketTotal);
                }

                for (String userID : userTotals.keySet()) {
                    Log.d("UserTotal", "UserID: " + userID + ", Total: $" + userTotals.get(userID));
                }

                Log.d("DebuggingUpdateUI", "Total passed:" + total);
                updateUI(total, userTotals);

                checkedOutReference.removeEventListener(this);

                checkedOutReference.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseCleanup", "All CheckedOutBasket entries have been removed successfully.");
                    } else {
                        Log.e("FirebaseCleanup", "Error removing CheckedOutBasket entries: " + task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CheckedOutList", "Error loading checked-out baskets: " + error.getMessage());
            }
        });
    }

    void updateUI(double total, HashMap<String, Double> userTotals) {
        LinearLayout itemView = findViewById(R.id.linearlayout5);
        TextView totalText = findViewById(R.id.textView18);
        totalText.setText("Total for all purchases: $" + total);
        TextView averageText = findViewById(R.id.textView22);
        double average = total / userTotals.size();
        averageText.setText("Average for each roommate: $" + average);
        for (String userID : userTotals.keySet()) {
            double userTotal = userTotals.get(userID);
            TextView totalSpent = new TextView(this);
            totalSpent.setText("Total spent by " + userID + ": $" + userTotal);
            TextView differenceText = new TextView(this);
            double difference = userTotal - average;
            differenceText.setText("Difference between total spent and average for " + userID + ": $" + difference );

            itemView.addView(totalSpent);
            itemView.addView(differenceText);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(SettleItems.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}