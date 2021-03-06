package md.apk.Settings;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import md.App;
import md.apk.R;
import md.ble.BLE_Services.BLEConst;
import md.ble.BLE_Services.InfoService;
import md.ble.BLE_Services.MyCustomService;
import md.ble.BleManagerService;

public class SettingsFragment extends Fragment implements
SeekBar.OnSeekBarChangeListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_vthr);
        seekBar.setOnSeekBarChangeListener(this);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        final InfoService<?> sensor = (InfoService<?>) App.DEVICE_DEF.getSensor(MyCustomService.UUID_SERVICE);
        Bundle bundle = new Bundle();
        bundle.putString(BLEConst.DATA, Integer.toString(progress));
        switch (seekBar.getId()) {
            case R.id.seekBar_vthr:
                Activity act = getActivity();
                if (act != null) {
                    final TextView tvPwm = (TextView) act.findViewById(R.id.textView_vthr);
                    if (tvPwm != null) {
                        tvPwm.setText("V thr = " + (progress * 590 + 1000));
                        BleManagerService.getInstance().update((MyCustomService) sensor, MyCustomService.UUID_V_THRESHOLD_ID, bundle);
                    }
                }
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
