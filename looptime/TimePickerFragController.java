package jkit.looptime;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.TimePicker;

import java.text.DateFormat;

/**
 * Created by jkgra on 11/12/2017.
 */

public class TimePickerFragController extends AppCompatDialogFragment implements TimePickerDialog.OnTimeSetListener
{
    private static final String    TAG = "Time Picker Log";
    public static final String HOUR = "hourInput";
    public static final String MIN = "minInput";
    private static int h;
    private static int m;

    onTimePickedListener mOwnerActivity;
    public interface onTimePickedListener
    {
        public void onTimePicked(int hour, int min);
    }

    public static TimePickerFragController newInstance(int hourIn, int minIn) {
        
        Bundle args = new Bundle();
        args.putInt(HOUR, hourIn);
        args.putInt(MIN, minIn);
        TimePickerFragController fragment = new TimePickerFragController();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOwnerActivity = (onTimePickedListener)context;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() + " should have implemented onTimePickedListener ");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        h = getArguments().getInt(HOUR);
        m = getArguments().getInt(MIN);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mOwnerActivity = (onTimePickedListener)getActivity();
        return new TimePickerDialog(getActivity(), this, h, m, true);

    }

    public void onTimeSet(TimePicker view, int hourofDay, int min)
    {
        if(mOwnerActivity != null)
            mOwnerActivity.onTimePicked(hourofDay, min);
    }
}
