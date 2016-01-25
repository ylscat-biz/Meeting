package arbell.demo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import arbell.demo.meeting.preach.Preach;
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
        File file = new File(extDir, "promotion/scene1.jpg");
//        File file = new File(extDir, "税款征收纠错指标异常指标需增加项目表.xls");
//        File file = new File(extDir, "公司通讯录20150319.xlsx");
        Meeting.sPreach = new Preach("123");
        if(file.exists()) {
            Intent intent = new Intent(this, DocViewer.class);
            intent.putExtra(DocViewer.FILE, file.getPath());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(DocViewer.FILE, file.getPath());
            intent.putExtra(DocViewer.TOPIC_ID, "tid");
            intent.putExtra(DocViewer.SUBJECT_ID, "sid");
            intent.putExtra(DocViewer.FILE_ID, "did");
            int index = 1;
            intent.putExtra(DocViewer.TOPIC_INDEX, String.valueOf(index));
            startActivity(intent);
            return;
        }
//        Workbook workbook = null;
//        try {
//            FileInputStream fis = new FileInputStream(file);
//            workbook = new XSSFWorkbook(fis);
//            fis.close();
//        } catch (IOException e) {
//            Log.e("Meeting", e.getMessage(), e);
//            return;
//        }
//
//        Sheet sheet = workbook.getSheetAt(0);
//
//        ExcelView.SheetData data = new ExcelView.SheetData(sheet,
//                getResources().getDisplayMetrics().density);
//        ExcelView view = new ExcelView(this);
//        view.setData(data);
//        setContentView(view);
//
//        ExcelView.GestureController controller = new ExcelView.GestureController(view);
//        view.setOnTouchListener(controller);
    }
}
