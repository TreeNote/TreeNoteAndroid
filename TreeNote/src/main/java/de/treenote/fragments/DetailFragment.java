package de.treenote.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.treenote.R;
import de.treenote.activities.NavigationDrawerActivity;
import de.treenote.pojo.TreeNode;
import de.treenote.pojo.TreeNodeType;
import de.treenote.util.Constants;
import de.treenote.util.TreeNoteDataHolder;
import de.treenote.util.TreeNoteUtil;
import lombok.Setter;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class DetailFragment extends Fragment {
    final private int ASK_LOCATION = 124;
    final private int ASK_SD_CARD = 125;
    public static final String CORRESPONDING_TREE_NODE_ID_KEY = "CORRESPONDING_TREE_NODE_ID_KEY";
    public static final String[] PERMISSIONS_TO_REQUEST = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int ALL_REQUESTED_PERMISSIONS_ALLOWED = 123;
    private TreeNode treeNode;
    private ImageView imageView;
    private LocationManager locationManager;
    private MapView mapView;
    static final int REQUEST_TAKE_PHOTO = 1;

    private static File currentPhoto = null;

    public static DetailFragment newInstance(TreeNode treeNode) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle(1);
        args.putInt(DetailFragment.CORRESPONDING_TREE_NODE_ID_KEY, treeNode.getID());
        detailFragment.setArguments(args);
        return detailFragment;
    }

    @Override
    public void onPause() {
        TreeNoteUtil.hideSoftKeyboard(getActivity());
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_layout, container, false);

        Activity activity = getActivity();
        if (activity instanceof NavigationDrawerActivity) {
            //noinspection ConstantConditions
            ((AppCompatActivity) activity).getSupportActionBar().setTitle("Details");
        }

        setHasOptionsMenu(true);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setTitle("Details");
        }

        final int treeNodeID = getArguments().getInt(CORRESPONDING_TREE_NODE_ID_KEY);
        treeNode = TreeNoteDataHolder.getTreeNode(treeNodeID);

        final EditText editText = (EditText) view.findViewById(R.id.editText);
        editText.setText(treeNode.getText());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                treeNode.setText(editText.getText().toString());
            }
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.type_spinner);

        ArrayList<String> spinnerArray = new ArrayList<>();

        for (TreeNodeType treeNodeType : TreeNodeType.values()) {
            spinnerArray.add(enumToString(treeNodeType));
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        TreeNodeType type = treeNode.getType();
        spinner.setSelection(spinnerArray.indexOf(enumToString(type)));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                treeNode.setType(TreeNodeType.values()[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        EditText startDateEditText = (EditText) view.findViewById(R.id.startDateEditText);
        if (treeNode.getStartDate() != null) {
            startDateEditText.setText(TreeNoteUtil.dateToText(treeNode.getStartDate()));
        }
        startDateEditText.setOnClickListener(new DateEditClickListener(startDateEditText, treeNode));

        ((TextView) view.findViewById(R.id.createdAt)).setText(TreeNoteUtil.dateTimeToText(treeNode.getCreationDatetime()));
        ((TextView) view.findViewById(R.id.lastModifiedAt)).setText(TreeNoteUtil.dateTimeToText(treeNode.getEditDatetime()));

        view.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TreeNoteDataHolder.deleteTreeNode(treeNodeID);
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    if (Build.VERSION.SDK_INT < 23 || checkPermissions(ASK_SD_CARD)) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String fileName = SimpleDateFormat.getDateInstance().format(new Date()) + ".jpg";
                        String folder = Environment.getExternalStorageDirectory()
                                + File.separator + Constants.OWNCLOUD_FOLDER;

                        //noinspection ResultOfMethodCallIgnored
                        new File(folder).mkdirs();
                        currentPhoto = new File(folder, fileName);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentPhoto));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        imageView = (ImageView) view.findViewById(R.id.imageView);
        if (treeNode.getImageFileName() != null) {
            Picasso.with(getActivity()).load(new File(Environment.getExternalStorageDirectory() + File.separator
                    + Constants.OWNCLOUD_FOLDER + File.separator + treeNode.getImageFileName())).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mapView = (MapView) view.findViewById(R.id.map);
        updateMapLocation();

        //ClickListener für aktuelle Position hinzufügen
        view.findViewById(R.id.addCurrentLocationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 23 || checkPermissions(ASK_LOCATION)) {
                    addCurrentLocationToTreeNode();
                }
            }
        });

        return view;
    }

    @TargetApi(Build.VERSION_CODES.M) // Diese Methode wird nur aufgerufen, wenn das API Level >= 23
    private boolean checkPermissions(int requestCode) {
        List<String> permissions = new ArrayList<>();
        if (requestCode == ASK_LOCATION) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, requestCode);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        if (requestCode == ASK_SD_CARD || requestCode == ASK_LOCATION) {
            Map<String, Integer> perms = new HashMap<>();
            if (requestCode == ASK_LOCATION) {
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            }
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);

            Boolean storagePermissionGranted = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (requestCode == ASK_SD_CARD && !storagePermissionGranted) {
                Toast.makeText(getActivity(), R.string.perms_not, Toast.LENGTH_LONG).show();
            } else if (requestCode == ASK_LOCATION && (!(perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || !storagePermissionGranted)) {
                Toast.makeText(getActivity(), R.string.perms_not, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.perms_granted, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private String enumToString(TreeNodeType treeNodeType) {
        Resources res = getResources();
        return res.getString(res.getIdentifier(treeNodeType.name(), "string", getActivity().getPackageName()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Picasso.with(getActivity()).load(currentPhoto).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                treeNode.setImageFileName(currentPhoto.getName());
            }
        }
    }

    private void addCurrentLocationToTreeNode() {

        int fine = ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_TO_REQUEST, ALL_REQUESTED_PERMISSIONS_ALLOWED);
            return;
        }

        boolean noProviderEnabled
                = !locationManager.isProviderEnabled(GPS_PROVIDER)
                && !locationManager.isProviderEnabled(NETWORK_PROVIDER);

        if (noProviderEnabled) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (lastKnownLocation != null) {
                Toast.makeText(
                        getActivity(),
                        R.string.noProviderFound_useLastKnownLocation,
                        Toast.LENGTH_SHORT).show();
                setLocation(lastKnownLocation);
            } else {
                Toast.makeText(
                        getActivity(),
                        R.string.notPossibleToDetermineLocation,
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(
                    getActivity(),
                    R.string.determiningCurrentLocation,
                    Toast.LENGTH_SHORT).show();

            TreeNoteLocationListener treeNoteLocationListener = new TreeNoteLocationListener();
            locationManager.requestLocationUpdates(GPS_PROVIDER, 10, 0, treeNoteLocationListener);
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, 10, 0, treeNoteLocationListener);
        }
    }

    private void setLocation(Location location) {
        treeNode.setLatitude(location.getLatitude());
        treeNode.setLongitude(location.getLongitude());

        updateMapLocation();

        Toast.makeText(getActivity(), R.string.newLocationFound, Toast.LENGTH_SHORT).show();
    }

    private void updateMapLocation() {

        if (!treeNode.hasLocation()) {
            mapView.setVisibility(View.GONE);
            return;
        }

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = mapView.getController();
        mapController.setZoom(100);
        GeoPoint startPoint = new GeoPoint(treeNode.getLatitude(), treeNode.getLongitude());
        mapController.setCenter(startPoint);

        ArrayList<OverlayItem> overlayItemArray = new ArrayList<>();
        OverlayItem overlayItem = new OverlayItem("", "", startPoint);
        Drawable marker = getActivity().getDrawable(R.drawable.ic_location_on_red_900_36dp);
        overlayItem.setMarker(marker);
        overlayItemArray.add(overlayItem);
        ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<>(
                getActivity(), overlayItemArray, null);
        mapView.getOverlays().add(itemizedIconOverlay);
        // disable touch, because it is not compatible with the scrolling view
        mapView.setVisibility(View.VISIBLE);
        mapView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int actionMasked = MotionEventCompat.getActionMasked(event);
                if (actionMasked == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:" + treeNode.getLatitude() + "," + treeNode.getLongitude()));
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    class DateEditClickListener implements View.OnClickListener {
        EditText editText;
        TreeNode treeNode;

        public DateEditClickListener(EditText editText, TreeNode treeNode) {
            this.editText = editText;
            this.treeNode = treeNode;
        }

        @Override
        public void onClick(View view) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setDateEdit(editText);
            datePickerFragment.setTreeNode(treeNode);
            datePickerFragment.show(getFragmentManager(), "datePicker");
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Setter
        EditText dateEdit;
        @Setter
        TreeNode treeNode;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.DarkFont, this, year, month, day);
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            dateEdit.setText(TreeNoteUtil.dateToText(year, month, day));
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            treeNode.setStartDate(cal.getTime());
        }
    }

    private class TreeNoteLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

            setLocation(location);

            //noinspection MissingPermission
            locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
