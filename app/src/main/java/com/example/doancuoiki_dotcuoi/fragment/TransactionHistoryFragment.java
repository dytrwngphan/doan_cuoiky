package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.TransactionAdapter;
import com.example.doancuoiki_dotcuoi.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class TransactionHistoryFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<CartItem> transactionList = new ArrayList<>();
    private TextView tvNoTransaction;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transaction_history, container, false);
        rvTransactions = v.findViewById(R.id.rvTransactions);
        tvNoTransaction = v.findViewById(R.id.tvNoTransaction);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(getContext(), transactionList);
        rvTransactions.setAdapter(adapter);

        loadTransactions();

        return v;
    }

    private void loadTransactions() {
        FirebaseFirestore.getInstance()
                .collection("cart")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "paid")
                .addSnapshotListener((snapshots, error) -> {
                    transactionList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) transactionList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvNoTransaction.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}
