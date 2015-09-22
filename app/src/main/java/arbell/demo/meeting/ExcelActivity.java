package arbell.demo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static arbell.demo.meeting.view.ExcelView.CellData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import arbell.demo.meeting.view.ExcelView;

/**
 * 2015-09-18 23:33
 */
public class ExcelActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File extDir = Environment.getExternalStorageDirectory();
//        File file = new File(extDir, "客户报表2.xls");
        File file = new File(extDir, "拓展店铺信息汇总表.xlsx");
//        File file = new File(extDir, "税款征收纠错指标异常指标需增加项目表.xls");
//        File file = new File(extDir, "公司通讯录20150319.xlsx");
        if(file.exists()) {
            Intent intent = new Intent(this, DocViewer.class);
            intent.putExtra(DocViewer.FILE, file.getPath());
            startActivity(intent);
            return;
        }
        Workbook workbook = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            fis.close();
        } catch (IOException e) {
            Log.e("Meeting", e.getMessage(), e);
            return;
        }

        Sheet sheet = workbook.getSheetAt(0);

        ExcelView.SheetData data = new ExcelView.SheetData(sheet,
                getResources().getDisplayMetrics().density);
        ExcelView view = new ExcelView(this);
        view.setData(data);
        setContentView(view);

        ExcelView.GestureController controller = new ExcelView.GestureController(view);
        view.setOnTouchListener(controller);
    }
}
