package com.delivery.site.fragment.listing;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.delivery.R;
import com.delivery.site.fragment.adapter.FavouritesAdapter;
import com.delivery.site.fragment.entity.SiteEntity;
import com.delivery.utils.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_DISPLAY_TYPE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_ID;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_NAME;

public class Favourites extends Fragment implements View.OnClickListener {
    private static final String TAG = Favourites.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerViewBookmarkSites;
    private List<SiteEntity> siteEntityList;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    private ShimmerFrameLayout spinner;
    private LinearLayout linearLayoutError;
    private TextView errorHeading, errorMessage;
    private ImageView errorImage;
    private DatabaseHelper db;
    private View view;

    public Favourites() {

    }

    public static Favourites newInstance(String param1, String param2) {
        Favourites fragment = new Favourites();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favourites, container, false);
        spinner = view.findViewById(R.id.shimmer_loading);
        db = new DatabaseHelper(getActivity());
        errorImage = view.findViewById(R.id.error_image);
        errorHeading = view.findViewById(R.id.error_heading);
        errorMessage = view.findViewById(R.id.details_message);
        getBookmarkSiteList(view);
        return view;
    }

    private void getBookmarkSiteList(View view) {
        recyclerViewBookmarkSites = view.findViewById(R.id.bookmark_recycler_view);
        linearLayoutError = view.findViewById(R.id.network_error_message);
        recyclerViewBookmarkSites.setNestedScrollingEnabled(false);
        siteEntityList = new ArrayList<>();
        recyclerViewBookmarkSites.setLayoutManager(new LinearLayoutManager(getActivity()));
        getRecentData();
        if (siteEntityList.size() == 0 || siteEntityList == null) {
            linearLayoutError.setVisibility(View.VISIBLE);
            errorImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_empty_search));
            errorHeading.setText(getActivity().getText(R.string.stringDraftLiveList));
            errorMessage.setText(getActivity().getText(R.string.stringEmptyDraftListMessage));
            recyclerViewBookmarkSites.setVisibility(View.GONE);
        } else {
            recyclerViewBookmarkSites.setVisibility(View.VISIBLE);
            FavouritesAdapter adapter = new FavouritesAdapter(getActivity(), siteEntityList);
            recyclerViewBookmarkSites.setAdapter(adapter);
        }
    }

    public void getRecentData() {
        linearLayoutError.setVisibility(View.GONE);
        spinner.stopShimmer();
        spinner.setVisibility(View.GONE);
        recyclerViewBookmarkSites.setVisibility(View.VISIBLE);
        if (db != null) {
            Cursor cursor = db.getAllData();
            while (cursor.moveToNext()) {
                SiteEntity item = new SiteEntity();
                item.setSiteId(cursor.getString(cursor.getColumnIndex(SITE_ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(SITE_NAME)));
                item.setPhotosUrl(cursor.getString(cursor.getColumnIndex(SITE_IMAGE_URL)));
                item.setSiteDisplayType(cursor.getString(cursor.getColumnIndex(SITE_DISPLAY_TYPE)));
                item.setStatusType("favourite");
                siteEntityList.add(item);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.address_holder:
                showNotificationAccept();
                break;
        }
    }

    public void showNotificationAccept() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_accept_reuqest, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(requestAccepted);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (siteEntityList != null) {
            siteEntityList.clear();
            getBookmarkSiteList(view);
        } else {
            getBookmarkSiteList(view);
        }
    }
}
