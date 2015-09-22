package arbell.demo.meeting.doc;

import android.graphics.Color;
import android.view.View;

import java.util.LinkedHashMap;

import arbell.demo.meeting.R;
import arbell.demo.meeting.view.ExcelView;

/**
 * 2015-09-22 08:58
 */
public class ExcelTabController implements View.OnClickListener {
    private View mActive;
    private LinkedHashMap<View, ExcelView.SheetData> mData = new LinkedHashMap<>();
    private ExcelView mExcel;

    public ExcelTabController(ExcelView mExcel) {
        this.mExcel = mExcel;
    }

    @Override
    public void onClick(View v) {
        if(v == mActive)
            return;
        if(mActive != null)
            mActive.setBackgroundColor(0xFFAAAAAA);
        mExcel.setData(mData.get(v));
        v.setBackgroundColor(0xFFFFBBAA);
        mActive = v;
    }

    public void addTab(View tabView, ExcelView.SheetData sheet) {
        mData.put(tabView, sheet);
        tabView.setBackgroundColor(0xFFAAAAAA);
    }
}
