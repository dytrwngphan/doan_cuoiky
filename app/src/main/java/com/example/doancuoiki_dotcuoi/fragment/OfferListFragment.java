package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.OfferAdapter;
import com.example.doancuoiki_dotcuoi.model.Offer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class OfferListFragment extends Fragment {
    private RecyclerView rvOffers;
    private OfferAdapter adapter;
    private List<Offer> offerList = new ArrayList<>();
    private String currentUserId;
    private TextView tvNoOffer, tvToolbarTitle;
    private ImageButton btnBack;

    private ListenerRegistration offerListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offer_list, container, false);
        rvOffers = v.findViewById(R.id.rvOffers);
        tvNoOffer = v.findViewById(R.id.tvNoOffer);
        btnBack = v.findViewById(R.id.btnBack);
        tvToolbarTitle = v.findViewById(R.id.tvToolbarTitle);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvOffers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OfferAdapter(getContext(), offerList, currentUserId);
        rvOffers.setAdapter(adapter);

        btnBack.setOnClickListener(view -> requireActivity().onBackPressed());

        listenOffersRealtime();

        return v;
    }

    private void listenOffersRealtime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy realtime tất cả offer mà mình là seller hoặc buyer (KHÔNG dùng whereIn vì Firestore không cho OR trực tiếp)
        // => Giải pháp: dùng 2 listener riêng biệt rồi merge kết quả, tránh trùng lặp

        // --- Lắng nghe OFFER mình là seller ---
        Query sellerQuery = db.collection("offers")
                .whereEqualTo("sellerId", currentUserId);

        // --- Lắng nghe OFFER mình là buyer ---
        Query buyerQuery = db.collection("offers")
                .whereEqualTo("buyerId", currentUserId);

        // --- Lắng nghe đồng thời cả hai ---
        offerListener = new MultiListener(sellerQuery, buyerQuery).attach(new MultiListener.ListenerCallback() {
            @Override
            public void onOffersUpdate(List<Offer> mergedOffers) {
                offerList.clear();
                offerList.addAll(mergedOffers);
                adapter.notifyDataSetChanged();
                tvNoOffer.setVisibility(offerList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (offerListener != null) offerListener.remove();
    }

    // ===== MultiListener để merge hai listener vào một callback =====
    private static class MultiListener {
        private final Query query1;
        private final Query query2;
        private ListenerRegistration reg1;
        private ListenerRegistration reg2;
        private final List<Offer> offers1 = new ArrayList<>();
        private final List<Offer> offers2 = new ArrayList<>();
        private ListenerCallback callback;

        public MultiListener(Query query1, Query query2) {
            this.query1 = query1;
            this.query2 = query2;
        }

        public ListenerRegistration attach(ListenerCallback callback) {
            this.callback = callback;
            reg1 = query1.addSnapshotListener((snap, err) -> {
                offers1.clear();
                if (snap != null) {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Offer o = doc.toObject(Offer.class);
                        if (o != null) offers1.add(o);
                    }
                }
                notifyMerged();
            });
            reg2 = query2.addSnapshotListener((snap, err) -> {
                offers2.clear();
                if (snap != null) {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Offer o = doc.toObject(Offer.class);
                        if (o != null) offers2.add(o);
                    }
                }
                notifyMerged();
            });
            // Trả về reg2 để dùng reg2.remove() là huỷ hết (vì cùng class MultiListener)
            return new ListenerRegistration() {
                @Override
                public void remove() {
                    if (reg1 != null) reg1.remove();
                    if (reg2 != null) reg2.remove();
                }
            };
        }

        private void notifyMerged() {
            // Gộp hai list lại, tránh trùng lặp (dùng Set offerId)
            Map<String, Offer> map = new LinkedHashMap<>();
            for (Offer o : offers1) map.put(o.getOfferId(), o);
            for (Offer o : offers2) map.put(o.getOfferId(), o);
            callback.onOffersUpdate(new ArrayList<>(map.values()));
        }

        interface ListenerCallback {
            void onOffersUpdate(List<Offer> mergedOffers);
        }
    }
}
